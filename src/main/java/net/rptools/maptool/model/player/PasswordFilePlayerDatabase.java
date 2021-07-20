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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.util.CipherUtil;
import net.rptools.maptool.util.CipherUtil.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PasswordFilePlayerDatabase implements PlayerDatabase {

  private static final Logger log = LogManager.getLogger(PasswordFilePlayerDatabase.class);

  private final File passwordFile;
  private final File backupPasswordFile;
  private final File additionalUsers;

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
  }

  public void readPasswordFile()
      throws PasswordDatabaseException, NoSuchAlgorithmException, InvalidKeySpecException {
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

  public void initialize() throws PasswordDatabaseException, NoSuchAlgorithmException, InvalidKeySpecException {
    transientPlayerDetails.clear();
    readPasswordFile();
  }

  private Map<String, PlayerDetails> readPasswordFile(File file)
      throws PasswordDatabaseException, NoSuchAlgorithmException, InvalidKeySpecException {

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
        String passwordString = passwordEntry.get("password").getAsString();
        Role role = Role.valueOf(passwordEntry.get("role").getAsString().toUpperCase());

        CipherUtil.Key passwordKey;
        if (passwordEntry.has("salt")) {
          SecretKeySpec password = CipherUtil.getInstance().decodeBase64(passwordString);
          byte[] salt = Base64.getDecoder().decode(passwordEntry.get("salt").getAsString());
          passwordKey = new CipherUtil.Key(password, salt);
        } else {
          passwordKey = CipherUtil.getInstance().createKey(passwordString);
          dirty.set(true);
        }

        String disabledReason = "";
        if (passwordEntry.has("disabled")) {
          disabledReason = passwordEntry.get("disabled").getAsString();
        }

        Set<PlayTime> playTimes = new HashSet<>();

        if (passwordEntry.has("times")) {
          JsonArray times = passwordEntry.get("times").getAsJsonArray();
          for (JsonElement t : times) {
            JsonObject time = t.getAsJsonObject();
            PlayTime playTime = new PlayTime(
                DayOfWeek.of(time.get("day").getAsInt()),
                LocalTime.parse(time.get("start").getAsString()),
                LocalTime.parse(time.get("end").getAsString())
            );
            playTimes.add(playTime);
          }
        }

        players.put(name, new PlayerDetails(
            name,
            role,
            passwordKey,
            disabledReason,
            Collections.unmodifiableSet(playTimes)
        ));
      }
      return players;
    } catch (IOException ioe) {
      throw new PasswordDatabaseException("msg.err.passFile.errorReadingFile", ioe);
    }
  }

  private void writePasswordFile() throws PasswordDatabaseException {

    if (dirty.compareAndSet(true, false)) {

      try {
      Files.copy(passwordFile.toPath(), backupPasswordFile.toPath(),
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
            pwObject.addProperty("password", CipherUtil.getInstance().encodeBase64(v.password()));
            pwObject.addProperty(
                "salt", Base64.getEncoder().withoutPadding().encodeToString(v.password.salt()));
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
  public boolean supportsPlayTimes() {
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

    PlayerDetails newDetails = new PlayerDetails(
        details.name(),
        details.role(),
        details.password(),
        reason,
        details.playTimes()
    );
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
    PlayerDetails details = playerDetails.get(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }
    return details.disabledReason();
  }

  @Override
  public Set<PlayTime> getPlayTimes(Player player) {
    PlayerDetails details = playerDetails.get(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }
    return details.playTimes();
  }

  @Override
  public void setPlayTimes(Player player, Collection<PlayTime> times) throws PasswordDatabaseException {
    PlayerDetails details = playerDetails.get(player.getName());
    if (details == null) {
      throw new IllegalArgumentException(I18N.getText("msg.error.playerNotInDatabase"));
    }

    Set<PlayTime> playTimes = Set.copyOf(times);

    PlayerDetails newDetails = new PlayerDetails(
        details.name(),
        details.role(),
        details.password(),
        details.disabledReason(),
        details.playTimes()
    );
    playerDetails.put(player.getName(), newDetails);

    dirty.set(true);
    writePasswordFile();
  }


  public void putPlayer(String name, Role role, String password, Set<PlayTime> playTimes,
      boolean persisted) throws NoSuchAlgorithmException, InvalidKeySpecException, PasswordDatabaseException {
    PlayerDetails newDetails = new PlayerDetails(
        name,
        role,
        CipherUtil.getInstance().createKey(password),
        "",
        playTimes
    );
    if (persisted) {
      playerDetails.put(name, newDetails);
      dirty.set(true);
      writePasswordFile();
    } else {
      transientPlayerDetails.put(name, newDetails);
    }
  }


  private static record PlayerDetails(String name, Role role, CipherUtil.Key password,
                                      String disabledReason, Set<PlayTime> playTimes) {}


}
