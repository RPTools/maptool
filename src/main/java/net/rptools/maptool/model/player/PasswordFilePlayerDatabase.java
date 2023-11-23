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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
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

public final class PasswordFilePlayerDatabase
    implements PlayerDatabase, PersistedPlayerDatabase, PlayerDBPropertyChange {

  private static final Logger log = LogManager.getLogger(PasswordFilePlayerDatabase.class);
  private static final String PUBLIC_KEY_DIR = "keys";

  private final File passwordFile;
  private final File backupPasswordFile;
  private final File additionalUsers;
  private final CipherUtil.Key serverPublicPrivateKey;

  private final LoggedInPlayers loggedInPlayers = new LoggedInPlayers();

  private final Map<String, PlayerDetails> playerDetails = new ConcurrentHashMap<>();
  private final Map<String, PlayerDetails> savedDetails = new ConcurrentHashMap<>();
  private final Set<String> removedPubKeyFiles = ConcurrentHashMap.newKeySet();
  private final Map<String, PlayerDetails> transientPlayerDetails = new ConcurrentHashMap<>();
  private final AtomicBoolean dirty = new AtomicBoolean(false);
  private final Map<PublicKeyDetails, PlayerDetails> dirtyPublicKeys = new ConcurrentHashMap<>();

  private final ReentrantLock passwordFileLock = new ReentrantLock();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
      this.serverPublicPrivateKey = new PublicPrivateKeyStore().getKeys().get();
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
      throws PasswordDatabaseException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {

    try {
      passwordFileLock.lock();
      playerDetails.clear();
      if (this.passwordFile.exists()) {
        playerDetails.putAll(readPasswordFile(this.passwordFile));
      }
      if (additionalUsers != null && additionalUsers.exists()) {
        playerDetails.putAll(readPasswordFile(additionalUsers));
        additionalUsers.delete();
      }
      writePasswordFile(); // Write out the password file if there were any passwords generated
    } finally {
      passwordFileLock.unlock();
    }
  }

  public void initialize()
      throws PasswordDatabaseException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    transientPlayerDetails.clear();
    readPasswordFile();
    savedDetails.putAll(playerDetails);
    propertyChangeSupport.firePropertyChange(
        PlayerDBPropertyChange.PROPERTY_CHANGE_DATABASE_CHANGED, null, this);
  }

  private Map<String, PlayerDetails> readPasswordFile(File file)
      throws PasswordDatabaseException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {

    try {
      passwordFileLock.lock();

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
          String publicKeyFile = null;
          Set<PublicKeyDetails> publicKeyDetails = new HashSet<>();
          if (passwordString != null && passwordEntry.has("salt")) {
            SecretKeySpec password = CipherUtil.decodeBase64(passwordString);
            byte[] salt = Base64.getDecoder().decode(passwordEntry.get("salt").getAsString());
            passwordKey = new CipherUtil.Key(password, salt);
          } else if (passwordString != null) {
            passwordKey = CipherUtil.fromSharedKeyNewSalt(passwordString);
            dirty.set(true);
          } else if (passwordEntry.has("publicKeys")) {
            JsonArray pkeys = passwordEntry.get("publicKeys").getAsJsonArray();
            Path publicKeyDir = passwordFile.getParentFile().toPath().resolve(PUBLIC_KEY_DIR);
            for (JsonElement je : pkeys) {
              publicKeyFile = je.getAsString();
              String pkString =
                  String.join("\n", Files.readAllLines(publicKeyDir.resolve(publicKeyFile)));

              for (String pk : CipherUtil.splitPublicKeys(pkString)) {
                MD5Key md5Key = CipherUtil.publicKeyMD5(pk);
                publicKeyDetails.add(
                    new PublicKeyDetails(
                        pk, md5Key, CipherUtil.fromPublicKeyString(pk), publicKeyFile));
              }
            }
          }

          String blockedReason = "";
          if (passwordEntry.has("blocked")) {
            blockedReason = passwordEntry.get("blocked").getAsString();
          }

          players.put(
              name, new PlayerDetails(name, role, passwordKey, publicKeyDetails, blockedReason));
        }
        return players;
      } catch (IOException ioe) {
        throw new PasswordDatabaseException("msg.error.passFile.errorReadingFile", ioe);
      }
    } finally {
      passwordFileLock.unlock();
    }
  }

  private void writePasswordFile() throws PasswordDatabaseException {

    try {
      passwordFileLock.lock();

      if (dirty.compareAndSet(true, false)) {

        try {
          if (passwordFile.exists()) {
            Files.copy(passwordFile.toPath(), backupPasswordFile.toPath(), REPLACE_EXISTING);
          }
        } catch (IOException ioe) {
          String msg = I18N.getText("msg.err.passFile.errorCopyingBackup");
          log.error(msg, ioe);
          throw new PasswordDatabaseException(msg, ioe);
        }

        JsonObject passwordDetails = new JsonObject();
        JsonArray passwords = new JsonArray();
        passwordDetails.add("passwords", passwords);
        for (Entry<String, PlayerDetails> entry : playerDetails.entrySet()) {
          PlayerDetails v = entry.getValue();
          JsonObject pwObject = new JsonObject();
          pwObject.addProperty("username", v.name());
          if (v.publicKeyDetails() != null && v.publicKeyDetails.size() > 0) {
            try {
              writePublicKeys(v);
            } catch (IOException e) {
              throw new PasswordDatabaseException(I18N.getText("Password.publicKeyWriteFailed"));
            }

            JsonArray pubKeysArray = new JsonArray();
            v.publicKeyDetails.stream().map(PublicKeyDetails::filename).forEach(pubKeysArray::add);
            pwObject.add("publicKeys", pubKeysArray);
          } else {
            pwObject.addProperty("password", CipherUtil.encodeBase64(v.password().secretKeySpec()));
            pwObject.addProperty(
                "salt", Base64.getEncoder().withoutPadding().encodeToString(v.password.salt()));
          }
          pwObject.addProperty("role", v.role().toString());
          passwords.add(pwObject);

          if (v.blockedReason() != null && !v.blockedReason.isEmpty()) {
            pwObject.addProperty("blocked", v.blockedReason());
          }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(passwordFile)) {
          gson.toJson(passwordDetails, writer);
        } catch (IOException ioe) {
          String msg = I18N.getText("msg.err.passFile.errorWritingFile");
          log.error(msg, ioe);
          throw new PasswordDatabaseException(msg, ioe);
        }
      }
    } finally {
      passwordFileLock.unlock();
    }
  }

  /**
   * Writes the public keys for the specified player.
   *
   * @param playerDetails The player to write the public keys for.
   * @throws IOException if an error occurs writing out the public keys.
   */
  private void writePublicKeys(PlayerDetails playerDetails) throws IOException {
    try {
      passwordFileLock.lock();
      File keyDir = passwordFile.getParentFile().toPath().resolve(PUBLIC_KEY_DIR).toFile();
      if (!keyDir.exists()) {
        keyDir.mkdirs();
      }
      File keyBackupDir =
          passwordFile.getParentFile().toPath().resolve(PUBLIC_KEY_DIR).resolve("backup").toFile();
      if (!keyBackupDir.exists()) {
        keyBackupDir.mkdirs();
      }

      Set<PublicKeyDetails> publicKeyDetails = playerDetails.publicKeyDetails();

      // First get all the public keys files with a public key marked dirty
      Set<PublicKeyDetails> playerDirtyKeys =
          publicKeyDetails.stream()
              .filter(dirtyPublicKeys::containsKey)
              .collect(Collectors.toSet());

      if (playerDirtyKeys.size() == 0) {
        return; // Nothing to do here
      }

      Set<String> dirtyFiles =
          playerDirtyKeys.stream().map(PublicKeyDetails::filename).collect(Collectors.toSet());

      // Backup they key file and overwrite
      for (String filename : dirtyFiles) {
        Path pkFile =
            passwordFile.getParentFile().toPath().resolve(PUBLIC_KEY_DIR).resolve(filename);
        if (pkFile.toFile().exists()) {
          Path pkFileBackup =
              passwordFile
                  .getParentFile()
                  .toPath()
                  .resolve(PUBLIC_KEY_DIR)
                  .resolve("backup")
                  .resolve(filename);
          Files.copy(pkFile, pkFileBackup, REPLACE_EXISTING);
        }

        Set<PublicKeyDetails> keysInFile =
            publicKeyDetails.stream()
                .filter(pkd -> pkd.filename().equals(filename))
                .collect(Collectors.toSet());

        keysInFile.forEach(dirtyPublicKeys::remove);

        String fileKeys =
            publicKeyDetails.stream()
                .filter(pkd -> pkd.filename().equals(filename))
                .map(PublicKeyDetails::keyString)
                .collect(Collectors.joining("\n"));
        Files.writeString(pkFile, fileKeys, CREATE, TRUNCATE_EXISTING);
      }
    } finally {
      passwordFileLock.unlock();
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
  public void disablePlayer(String player, String reason) throws PasswordDatabaseException {
    PlayerDetails details = getPlayerDetails(player);
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }

    PlayerDetails newDetails =
        new PlayerDetails(
            details.name(), details.role(), details.password(), details.publicKeyDetails(), reason);
    playerDetails.put(details.name(), newDetails);
  }

  @Override
  public void addPlayerSharedPassword(String name, Role role, String password)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException {
    if (playerExists(name)) {
      throw new PasswordDatabaseException(I18N.getText("Password.playerExists", name));
    }
    putUncommittedPlayerHashPassword(name, role, password, Set.of(), "", true);
  }

  @Override
  public void addPlayerAsymmetricKey(String name, Role role, Set<String> publicKeyStrings)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    if (playerExists(name)) {
      throw new PasswordDatabaseException(I18N.getText("Password.playerExists", name));
    }
    putUncommittedPlayer(name, role, publicKeyStrings, "", true);
  }

  @Override
  public void setSharedPassword(String name, String password)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException {

    var pd = getPlayerDetails(name);
    pd.publicKeyDetails().forEach(pk -> removedPubKeyFiles.add(pk.filename()));
    boolean persisted = isPersisted(name);
    putUncommittedPlayerHashPassword(
        pd.name(), pd.role(), password, Set.of(), pd.blockedReason, persisted);
  }

  @Override
  public boolean isPersisted(String name) {
    return playerDetails.containsKey(name);
  }

  @Override
  public void deletePlayer(String name) {
    if (playerDetails.containsKey(name)) {
      playerDetails
          .get(name)
          .publicKeyDetails()
          .forEach(pk -> removedPubKeyFiles.add(pk.filename()));
      playerDetails.remove(name);
      propertyChangeSupport.firePropertyChange(
          PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_REMOVED, name, null);
    }
  }

  @Override
  public void blockPlayer(String name, String reason) {
    var pd = getPlayerDetails(name);
    if (pd.blockedReason().equals(reason)) {
      return; // Noting to do here
    }
    boolean persisted = isPersisted(name);
    putUncommittedPlayer(
        pd.name(), pd.role(), pd.password(), pd.publicKeyDetails(), reason, persisted);
  }

  @Override
  public void unblockPlayer(String name) {
    var pd = getPlayerDetails(name);
    if (pd.blockedReason() == null || pd.blockedReason().isEmpty()) {
      return; // Noting to do here
    }
    boolean persisted = isPersisted(name);
    putUncommittedPlayer(pd.name(), pd.role(), pd.password(), pd.publicKeyDetails(), "", persisted);
  }

  @Override
  public void setRole(String name, Role role) {
    var pd = getPlayerDetails(name);
    if (pd.role() == role) {
      return; // Noting to do here
    }
    boolean persisted = isPersisted(name);
    putUncommittedPlayer(
        pd.name(), role, pd.password(), pd.publicKeyDetails(), pd.blockedReason(), persisted);
  }

  @Override
  public void setAsymmetricKeys(String name, Set<String> keys)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {

    var pd = getPlayerDetails(name);

    Set<PublicKeyDetails> publicKeyDetails = createPublicKeyDetails(keys, pd.name());
    boolean persisted = isPersisted(name);
    var newpd =
        putUncommittedPlayer(
            pd.name(), pd.role(), null, publicKeyDetails, pd.blockedReason(), persisted);
    publicKeyDetails.forEach(p -> dirtyPublicKeys.put(p, newpd));
  }

  @Override
  public void addAsymmetricKeys(String name, Set<String> keys)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    if (!playerExists(name)) {
      throw new PasswordDatabaseException(I18N.getText("msg.error.playerNotInDatabase", name));
    }

    var pd = getPlayerDetails(name);
    Set<PublicKeyDetails> pKeys = pd.publicKeyDetails();

    Set<PublicKeyDetails> newKeys = createPublicKeyDetails(keys, name);

    pKeys.addAll(newKeys);

    boolean persisted = isPersisted(name);
    var newpd = putUncommittedPlayer(name, pd.role(), null, newKeys, pd.blockedReason(), persisted);

    newKeys.forEach(p -> dirtyPublicKeys.put(p, newpd));
  }

  @Override
  public void commitChanges()
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException {
    if (savedDetails.size() > 0) {
      removeOldPublicKeys();
      savedDetails.clear();
      savedDetails.putAll(playerDetails);
    }
    dirty.set(true);
    writePasswordFile();
  }

  /** Removes old public key files for players that have been removed. */
  private void removeOldPublicKeys() {
    for (String filename : removedPubKeyFiles) {
      Path pkFile = passwordFile.getParentFile().toPath().resolve(PUBLIC_KEY_DIR).resolve(filename);
      Path pkFileBackup =
          passwordFile
              .getParentFile()
              .toPath()
              .resolve(PUBLIC_KEY_DIR)
              .resolve("backup")
              .resolve(filename);
      try {
        if (Files.exists(pkFile)) {
          Files.delete(pkFile);
        }
      } catch (IOException e) {
        log.error(e);
      }

      try {
        if (Files.exists(pkFileBackup)) {
          Files.delete(pkFileBackup);
        }
      } catch (IOException e) {
        log.error(e);
      }
    }
    removedPubKeyFiles.clear();
  }

  @Override
  public void rollbackChanges()
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException {
    if (savedDetails.size() > 0) {
      playerDetails.clear();
      playerDetails.putAll(savedDetails);
    }
    propertyChangeSupport.firePropertyChange(
        PlayerDBPropertyChange.PROPERTY_CHANGE_DATABASE_CHANGED, null, this);
  }

  @Override
  public boolean isBlocked(Player player) {
    return getBlockedReason(player).length() > 0;
  }

  @Override
  public String getBlockedReason(Player player) {
    PlayerDetails details = getPlayerDetails(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }
    return details.blockedReason();
  }

  @Override
  public AuthMethod getAuthMethod(Player player) {
    PlayerDetails details = getPlayerDetails(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }
    return details.publicKeyDetails().size() == 0 ? AuthMethod.PASSWORD : AuthMethod.ASYMMETRIC_KEY;
  }

  @Override
  public CompletableFuture<CipherUtil.Key> getPublicKey(Player player, MD5Key md5key)
      throws ExecutionException, InterruptedException {
    PlayerDetails pd = getPlayerDetails(player.getName());
    if (pd == null) {
      CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.supplyAsync(
        () -> {
          assert pd != null;
          Optional<PublicKeyDetails> key =
              pd.publicKeyDetails().stream().filter(pk -> pk.md5Key().equals(md5key)).findFirst();
          if (key.isPresent()) {
            return key.get().publicKey();
          } else {
            throw new CompletionException(
                new IllegalArgumentException(I18N.getText("Password" + ".publicKeyNotFound")));
          }
        });
  }

  @Override
  public Set<String> getEncodedPublicKeys(String name) {
    if (playerDetails.containsKey(name)) {
      return playerDetails.get(name).publicKeyDetails().stream()
          .map(PublicKeyDetails::keyString)
          .collect(Collectors.toSet());
    } else {
      return Set.of();
    }
  }

  @Override
  public CompletableFuture<Boolean> hasPublicKey(Player player, MD5Key md5key) {
    PlayerDetails pd = getPlayerDetails(player.getName());
    if (pd == null) {
      CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.supplyAsync(
        () -> {
          assert pd != null;
          Optional<PublicKeyDetails> key =
              pd.publicKeyDetails().stream().filter(pk -> pk.md5Key().equals(md5key)).findFirst();
          return key.isPresent();
        });
  }

  @Override
  public boolean isPlayerRegistered(String name)
      throws InterruptedException, InvocationTargetException {
    return playerExists(name);
  }

  @Override
  public void playerSignedIn(Player player) {
    loggedInPlayers.playerSignedIn(player);
  }

  @Override
  public void playerSignedOut(Player player) {
    loggedInPlayers.playerSignedOut(player);
  }

  @Override
  public boolean isPlayerConnected(String name) {
    return loggedInPlayers.isLoggedIn(name);
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
  public Set<Player> getOnlinePlayers() throws InterruptedException, InvocationTargetException {
    return new HashSet<>(loggedInPlayers.getPlayers());
  }

  @Override
  public boolean recordsOnlyConnectedPlayers() {
    return false;
  }

  /**
   * Adds a player to the database with the specified password. The {@link #commitChanges} method
   * must be called to commit these changes to persistent storage.
   *
   * @param name The name of the player to add.
   * @param role The role of the player to add.
   * @param password The password for the player.
   * @param blockedReason The reason player was blocked, null or empty string if not diabled.
   * @param persisted should the player entry be persisted or not.
   * @return the newly added {@link PlayerDetails}
   * @throws PasswordDatabaseException if there is a problem added the player
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  private PlayerDetails putUncommittedPlayer(
      String name, Role role, String password, String blockedReason, boolean persisted)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException {
    return putUncommittedPlayerHashPassword(
        name, role, password, Set.of(), blockedReason, persisted);
  }

  /**
   * Adds a player to the database with the specified public keys. The {@link #commitChanges} method
   * must be called to commit these changes to persistent storage.
   *
   * @param name The name of the player to add.
   * @param role The role of the player to add.
   * @param publicKeyStrings The public key Strings
   * @param blockedReason The reason player was blocked, null or empty string if not diabled.
   * @param persisted should the player entry be persisted or not.
   * @return the newly added {@link PlayerDetails}
   * @throws PasswordDatabaseException if there is a problem added the player
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  private PlayerDetails putUncommittedPlayer(
      String name, Role role, Set<String> publicKeyStrings, String blockedReason, boolean persisted)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    return putUncommittedPlayer(
        name, role, null, createPublicKeyDetails(publicKeyStrings, name), blockedReason, persisted);
  }

  /**
   * Adds a player to the database with the specified password or public keys. The {@link
   * #commitChanges} method * must be called to commit these * changes to persistent storage.
   *
   * @param name The name of the player to add.
   * @param role The role of the player to add.
   * @param password The password for the player.
   * @param publicKeyDetails The public keys
   * @param blockedReason The reason player was blocked, null or empty string if not blocked.
   * @param persisted should the player entry be persisted or not.
   * @return the newly added {@link PlayerDetails}
   */
  private PlayerDetails putUncommittedPlayer(
      String name,
      Role role,
      CipherUtil.Key password,
      Set<PublicKeyDetails> publicKeyDetails,
      String blockedReason,
      boolean persisted) {
    blockedReason = Objects.requireNonNullElse(blockedReason, "");

    var oldPd = getPlayerDetails(name);
    var pd = new PlayerDetails(name, role, password, publicKeyDetails, blockedReason);
    if (persisted) {
      publicKeyDetails.forEach(p -> dirtyPublicKeys.put(p, pd));
      playerDetails.put(name, pd);
    } else {
      transientPlayerDetails.put(name, pd);
    }

    if (oldPd != null) {
      propertyChangeSupport.firePropertyChange(
          PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_CHANGED, null, name);
    } else {
      propertyChangeSupport.firePropertyChange(
          PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_ADDED, null, name);
    }
    return pd;
  }

  /**
   * Adds a player to the database with the specified password or public keys. If the password is
   * not null it will be hashed. The {@link #commitChanges} method * must be called to commit these
   * changes to persistent storage.
   *
   * @param name The name of the player to add.
   * @param role The role of the player to add.
   * @param password The password for the player.
   * @param publicKeyDetails The public keys
   * @param blockedReason The reason player was blocked, null or empty string if not diabled.
   * @param persisted should the player entry be persisted or not.
   * @return the newly added {@link PlayerDetails}
   * @throws PasswordDatabaseException if there is a problem added the player
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  private PlayerDetails putUncommittedPlayerHashPassword(
      String name,
      Role role,
      String password,
      Set<PublicKeyDetails> publicKeyDetails,
      String blockedReason,
      boolean persisted)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException {

    return putUncommittedPlayer(
        name,
        role,
        password != null & !password.isEmpty() ? CipherUtil.fromSharedKeyNewSalt(password) : null,
        publicKeyDetails,
        blockedReason,
        persisted);
  }

  /**
   * Creates the public key details from the specified string.
   *
   * @param publicKeyStrings the string containing encoded public keys.
   * @param playerName the name of the player
   * @return the public key details
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the passwordn
   */
  private Set<PublicKeyDetails> createPublicKeyDetails(
      Set<String> publicKeyStrings, String playerName)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    Set<PublicKeyDetails> pkDetails = new HashSet<>();

    String pkFilename = derivePublicKeyFilename(playerName);
    for (String pk : publicKeyStrings) {
      MD5Key md5Key = CipherUtil.publicKeyMD5(pk);
      pkDetails.add(
          new PublicKeyDetails(pk, md5Key, CipherUtil.fromPublicKeyString(pk), pkFilename));
    }

    return pkDetails;
  }

  /**
   * Returns a file safe filename based on the player name
   *
   * @param name the name of the player.
   * @return a file safe filename based on the player name
   */
  private String derivePublicKeyFilename(String name) {
    if (playerDetails.containsKey(name) && playerDetails.get(name).publicKeyDetails().size() > 0) {
      return playerDetails.get(name).publicKeyDetails().stream().findFirst().get().filename();
    }

    String sanitised = name.replaceAll("[^A-Za-z0-9_\\-]", "");
    sanitised = sanitised.length() > 110 ? sanitised.substring(0, 127) : sanitised;
    Random random = new Random();
    return sanitised + "_" + random.nextInt(10000) + ".key";
  }

  /**
   * Adds a temporary non persisted player to the database. This is useful for things like ensuring
   * that the player that created the server is registered and can log in.
   *
   * @param name the name of the player to add
   * @param role the role for the player
   * @param password the password for the player
   * @throws PasswordDatabaseException if there is a problem added the player
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  public void addTemporaryPlayer(String name, Role role, String password)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException {
    putUncommittedPlayerHashPassword(name, role, password, Set.of(), "", false);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /** Record containing player details */
  private record PlayerDetails(
      String name,
      Role role,
      CipherUtil.Key password,
      Set<PublicKeyDetails> publicKeyDetails,
      String blockedReason) {}

  /** Record containing the public key information */
  private static record PublicKeyDetails(
      String keyString, MD5Key md5Key, CipherUtil.Key publicKey, String filename) {}
}
