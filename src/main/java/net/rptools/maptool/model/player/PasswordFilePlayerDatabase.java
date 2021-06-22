package net.rptools.maptool.model.player;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.util.CipherUtil;
import net.rptools.maptool.util.CipherUtil.Key;

public class PasswordFilePlayerDatabase implements PlayerDatabase {


  private static record PlayerDetails(String name, Role role, CipherUtil.Key password) {};

  private final File passwordFile;

  private final Map<String, PlayerDetails> playerDetails = new ConcurrentHashMap<>();


  public PasswordFilePlayerDatabase(File file)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    passwordFile = file;
    readPasswordFile();
  }

  private void readPasswordFile()
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    boolean needsRewrite = false;
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(passwordFile))) {
      JsonObject passwordsJson = JsonParser.parseReader(reader).getAsJsonObject();
      if (!passwordsJson.has("passwords")) {
        throw new IOException("Missing passwords field");
      }
      JsonArray passwords = passwordsJson.get("passwords").getAsJsonArray();
      for (JsonElement entry : passwords) {
        JsonObject passwordEntry = entry.getAsJsonObject();
        String name = passwordEntry.get("name").getAsString();
        String passwordString = passwordEntry.get("password").getAsString();
        Role role = Role.valueOf(passwordEntry.get("role").getAsString());

        CipherUtil.Key passwordKey;
        if (passwordEntry.has("salt")) {
          SecretKeySpec password = CipherUtil.getInstance().decodeBase64(passwordString);
          byte[] salt = Base64.getDecoder().decode(passwordEntry.get("salt").getAsString());
          passwordKey = new CipherUtil.Key(password, salt);
        } else {
          passwordKey = CipherUtil.getInstance().createKey(passwordString);
          needsRewrite = true;
        }

        // TODO: CDW check for existing
        playerDetails.put(name, new PlayerDetails(name, role, passwordKey));
      }

      if (needsRewrite) {
        writePasswordFile();
      }
    }

  }

  private void writePasswordFile() {
    JsonObject passwordDetails = new JsonObject();
    JsonArray passwords = new JsonArray();
    passwordDetails.add("passwords", passwords);
    playerDetails.forEach( (k, v) -> {
      JsonObject pwObject = new JsonObject();
      pwObject.addProperty("name", v.name());
      pwObject.addProperty("password", CipherUtil.getInstance().encodeBase64(v.password()));
      pwObject.addProperty("salt", Base64.getEncoder().withoutPadding().encodeToString(v.password.salt()));
    });
  }

  @Override
  public boolean playerExists(String playerName) {
    return playerDetails.containsKey(playerName);
  }

  @Override
  public Player getPlayer(String playerName)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PlayerDetails pd = playerDetails.get(playerName);
    if (pd != null) {
      return new Player(playerName, pd.role(), pd.password());
    } else  {
      return null;
    }
  }

  @Override
  public Optional<Key> getPlayerPassword(String playerName) {
    if (!playerExists(playerName)) {
      return Optional.empty();
    }
    return Optional.ofNullable(playerDetails.get(playerName).password());
  }

  @Override
  public byte[] getPlayerPasswordSalt(String playerName) {
    if (!playerExists(playerName)) {
      return new byte[0];
    }
    return playerDetails.get(playerName).password().salt();
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
}
