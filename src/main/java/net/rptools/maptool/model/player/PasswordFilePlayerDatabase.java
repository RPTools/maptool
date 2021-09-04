/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.CipherUtil.Key;
import net.rptools.maptool.util.cipher.PublicPrivateKeyStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PasswordFilePlayerDatabase implements PlayerDatabase {

  private static final Logger log = LogManager.getLogger(PasswordFilePlayerDatabase.class);
  private static final String PUBLIC_KEY_DIR = "keys";

  private final File passwordFile;
  private final File backupPasswordFile;
  private final File additionalUsers;
  private final CipherUtil.Key serverPublicPrivateKey;

  private final Map<String, PlayerDetails> playerDetails = new ConcurrentHashMap<>();
  private final Map<String, PlayerDetails> transientPlayerDetails = new ConcurrentHashMap<>();
  private final AtomicBoolean dirty = new AtomicBoolean(false);

  public PasswordFilePlayerDatabase(File passwordFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    this(passwordFile, null);
  }

  PasswordFilePlayerDatabase(File passwordFile, File additionalUsers)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    Objects.requireNonNull(passwordFile);
    this.passwordFile = passwordFile;
    this.backupPasswordFile = new File(passwordFile + ".backup");
    this.additionalUsers = additionalUsers;
    try {
      this.serverPublicPrivateKey = new PublicPrivateKeyStore().getKeys().get().getKey();
    } catch (InterruptedException | ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      if (e.getCause() instanceof NoSuchAlgorithmException) {
        throw (NoSuchAlgorithmException) e.getCause();
      } else if (e.getCause() instanceof InvalidKeySpecException) {
        throw (InvalidKeySpecException) e.getCause();
      } else {
        throw new IOException(e.getCause());
      }
    }
  }

  public void readPasswordFile()
      throws PasswordDatabaseException, NoSuchAlgorithmException, InvalidKeySpecException,
          NoSuchPaddingException, InvalidKeyException {
    playerDetails.clear();
    if (this.passwordFile.exists()) {
      playerDetails.putAll(readPasswordFile(this.passwordFile));
    }
    if (additionalUsers != null && additionalUsers.exists()) {
      playerDetails.putAll(readPasswordFile(additionalUsers));
      additionalUsers.delete();
    }
    writePasswordFile(); // Write out the password file if there were any passwords generated
  }

  public void initialize()
      throws PasswordDatabaseException, NoSuchAlgorithmException, InvalidKeySpecException,
          NoSuchPaddingException, InvalidKeyException {
    transientPlayerDetails.clear();
    readPasswordFile();
  }

  private Map<String, PlayerDetails> readPasswordFile(File file)
      throws PasswordDatabaseException, NoSuchAlgorithmException, InvalidKeySpecException,
          NoSuchPaddingException, InvalidKeyException {

    Map<String, PlayerDetails> players = new HashMap<>();

    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
      JsonObject passwordsJson;
      try {
        passwordsJson = JsonParser.parseReader(reader).getAsJsonObject();
      } catch (Exception e) {
        String msg = I18N.getText("msg.error.passFile.errorInJson");
        log.error(msg, e);
        throw new PasswordDatabaseException(msg, e);
      }

      if (!passwordsJson.has("passwords")) {
        String msg = I18N.getText("msg.error.passFile.missingPasswordsField");
        log.error(msg);
        throw new PasswordDatabaseException(msg);
      }
      JsonArray passwords = passwordsJson.get("passwords").getAsJsonArray();
      for (JsonElement entry : passwords) {
        JsonObject passwordEntry = entry.getAsJsonObject();
        String name = passwordEntry.get("username").getAsString();
        String passwordString = null;
        if (passwordEntry.has("password")) {
          passwordString = passwordEntry.get("password").getAsString();
        }

        Role role = Role.valueOf(passwordEntry.get("role").getAsString().toUpperCase());

        CipherUtil.Key passwordKey = null;
        Map<MD5Key, CipherUtil> publicKeys = new HashMap<>();
        String publicKeyFile = null;
        List<String> publicKeyFileList = new ArrayList<>();
        if (passwordString != null && passwordEntry.has("salt")) {
          SecretKeySpec password = CipherUtil.decodeBase64(passwordString);
          byte[] salt = Base64.getDecoder().decode(passwordEntry.get("salt").getAsString());
          passwordKey = new CipherUtil.Key(password, salt);
        } else if (passwordString != null) {
          CipherUtil cipherUtil = CipherUtil.fromSharedKeyNewSalt(passwordString);
          passwordKey = cipherUtil.getKey();
          dirty.set(true);
        } else if (passwordEntry.has("publicKeys")) {
          JsonArray pkeys = passwordEntry.get("publicKeys").getAsJsonArray();
          Path publicKeyDir = passwordFile.getParentFile().toPath().resolve(PUBLIC_KEY_DIR);
          for (JsonElement je : pkeys) {
            publicKeyFile = je.getAsString();
            String pkString =
                String.join("\n", Files.readAllLines(publicKeyDir.resolve(publicKeyFile)));

            for (String pk : CipherUtil.splitPublicKeys(pkString)) {
              publicKeys.put(CipherUtil.publicKeyMD5(pk), CipherUtil.fromPublicKeyString(pk));
            }

            publicKeyFileList.add(publicKeyFile);
          }
        }

        String disabledReason = "";
        if (passwordEntry.has("disabled")) {
          disabledReason = passwordEntry.get("disabled").getAsString();
        }

        players.put(
            name,
            new PlayerDetails(
                name,
                role,
                passwordKey,
                publicKeys,
                publicKeyFileList.size() > 0 ? publicKeyFileList.toArray(new String[0]) : null,
                disabledReason));
      }
      return players;
    } catch (IOException ioe) {
      throw new PasswordDatabaseException("msg.err.passFile.errorReadingFile", ioe);
    }
  }

  private void writePasswordFile() throws PasswordDatabaseException {

    if (dirty.compareAndSet(true, false)) {

      try {
        Files.copy(
            passwordFile.toPath(),
            backupPasswordFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ioe) {
        String msg = I18N.getText("msg.err.passFile.errorCopyingBackup");
        log.error(msg, ioe);
        throw new PasswordDatabaseException(msg, ioe);
      }

      JsonObject passwordDetails = new JsonObject();
      JsonArray passwords = new JsonArray();
      passwordDetails.add("passwords", passwords);
      playerDetails.forEach(
          (k, v) -> {
            JsonObject pwObject = new JsonObject();
            pwObject.addProperty("username", v.name());
            if (v.publicKeys() != null && v.publicKeys().size() > 0) {
              JsonArray pubKeysArray = new JsonArray();
              v.publicKeys().values().forEach(pk -> pubKeysArray.add(pk.getEncodedPublicKeyText()));
              pwObject.add("publicKeys", pubKeysArray);
            } else {
              pwObject.addProperty(
                  "password", CipherUtil.encodeBase64(v.password().secretKeySpec()));
              pwObject.addProperty(
                  "salt", Base64.getEncoder().withoutPadding().encodeToString(v.password.salt()));
            }
            pwObject.addProperty("role", v.role().toString());
            passwords.add(pwObject);
          });
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      try (FileWriter writer = new FileWriter(passwordFile)) {
        gson.toJson(passwordDetails, writer);
      } catch (IOException ioe) {
        String msg = I18N.getText("msg.err.passFile.errorWritingFile");
        log.error(msg, ioe);
        throw new PasswordDatabaseException(msg, ioe);
      }
    }
  }

  @Override
  public boolean playerExists(String playerName) {
    if (transientPlayerDetails.containsKey(playerName)) {
      return true;
    }
    return playerDetails.containsKey(playerName);
  }

  private PlayerDetails getPlayerDetails(String playerName) {
    PlayerDetails pd = transientPlayerDetails.get(playerName);
    if (pd != null) {
      return pd;
    }
    return playerDetails.get(playerName);
  }

  @Override
  public Player getPlayer(String playerName) {
    PlayerDetails pd = getPlayerDetails(playerName);
    if (pd != null) {
      return new Player(playerName, pd.role(), pd.password());
    } else {
      return null;
    }
  }

  @Override
  public Optional<Key> getPlayerPassword(String playerName) {
    if (!playerExists(playerName)) {
      return Optional.empty();
    }
    return Optional.ofNullable(getPlayer(playerName).getPassword());
  }

  @Override
  public byte[] getPlayerPasswordSalt(String playerName) {
    if (!playerExists(playerName)) {
      return new byte[0];
    }
    return getPlayer(playerName).getPassword().salt();
  }

  @Override
  public Player getPlayerWithRole(String playerName, Role role)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    Optional<Key> playerPassword = getPlayerPassword(playerName);
    return playerPassword.map(key -> new Player(playerName, role, key)).orElse(null);
  }

  @Override
  public Optional<Key> getRolePassword(Role role) {
    return Optional.empty();
  }

  @Override
  public boolean supportsDisabling() {
    return true;
  }

  @Override
  public boolean supportsAsymmetricalKeys() {
    return true;
  }

  @Override
  public boolean supportsRolePasswords() {
    return false;
  }

  @Override
  public void disablePlayer(Player player, String reason) throws PasswordDatabaseException {
    PlayerDetails details = getPlayerDetails(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }

    PlayerDetails newDetails =
        new PlayerDetails(
            details.name(),
            details.role(),
            details.password(),
            details.publicKeys(),
            details.publicKeyFile(),
            reason);
    playerDetails.put(player.getName(), newDetails);

    dirty.set(true);
    writePasswordFile();
  }

  @Override
  public boolean isDisabled(Player player) {
    return getDisabledReason(player).length() > 0;
  }

  @Override
  public String getDisabledReason(Player player) {
    PlayerDetails details = getPlayerDetails(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }
    return details.disabledReason();
  }

  @Override
  public AuthMethod getAuthMethod(Player player) {
    PlayerDetails details = getPlayerDetails(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }
    return details.publicKeys().size() == 0 ? AuthMethod.PASSWORD : AuthMethod.ASYMMETRIC_KEY;
  }

  @Override
  public CompletableFuture<CipherUtil> getPublicKey(Player player, MD5Key md5key)
      throws ExecutionException, InterruptedException {
    PlayerDetails pd = getPlayerDetails(player.getName());
    if (pd == null) {
      CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.completedFuture(pd.publicKeys.get(md5key));
  }

  @Override
  public boolean isPlayerRegistered(String name)
      throws InterruptedException, InvocationTargetException {
    return playerExists(name);
  }

  @Override
  public Set<Player> getAllPlayers() throws InterruptedException, InvocationTargetException {
    Set<Player> players = new HashSet<>(getOnlinePlayers());

    players.addAll(
        playerDetails.keySet().stream().map(this::getPlayer).collect(Collectors.toSet()));
    players.addAll(
        transientPlayerDetails.keySet().stream().map(this::getPlayer).collect(Collectors.toSet()));

    return players;
  }

  @Override
  public boolean recordsOnlyConnectedPlayers() {
    return false;
  }

  public void putPlayer(
      String name,
      Role role,
      String password,
      Set<String> publicKeyStrings,
      String[] publicKeyFile,
      boolean persisted)
      throws NoSuchAlgorithmException, InvalidKeySpecException, PasswordDatabaseException,
          NoSuchPaddingException, InvalidKeyException {
    CipherUtil cipherUtil = null;
    if (password != null && password.length() > 0) {
      cipherUtil = CipherUtil.fromSharedKeyNewSalt(password);
    }

    HashMap<MD5Key, CipherUtil> publicKeys = new HashMap<>();
    for (String pk : publicKeyStrings) {
      publicKeys.put(new MD5Key(pk), CipherUtil.fromPublicKeyString(pk));
    }

    PlayerDetails newDetails =
        new PlayerDetails(
            name,
            role,
            cipherUtil == null ? null : cipherUtil.getKey(),
            publicKeys,
            publicKeyFile,
            "");
    if (persisted) {
      playerDetails.put(name, newDetails);
      dirty.set(true);
      writePasswordFile();
    } else {
      transientPlayerDetails.put(name, newDetails);
    }
  }

  private static record PlayerDetails(
      String name,
      Role role,
      CipherUtil.Key password,
      Map<MD5Key, CipherUtil> publicKeys,
      String[] publicKeyFile,
      String disabledReason) {}
}
