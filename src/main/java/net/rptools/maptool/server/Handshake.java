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
package net.rptools.maptool.server;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.hessian.HessianUtils;
import net.rptools.clientserver.simple.client.IClientConnection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool.util.CipherUtil;
import net.rptools.maptool.util.PasswordGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class Handshake {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  private static String USERNAME_FIELD = "username:";
  private static String VERSION_FIELD = "version:";
  private Response response;
  private Exception exception;
  private Thread handshakeThread;
  private Player player;
  private IClientConnection connection;
  private List<HandshakeObserver> observerList = new CopyOnWriteArrayList<>();

  public Handshake(IClientConnection connection) {
    this.connection = connection;
  }

  public boolean isSuccessful() {
    return response != null && response.code == Code.OK;
  }

  public IClientConnection getConnection() {
    return connection;
  }

  public Response getResponse() {
    return response;
  }

  public Exception getException() {
    return exception;
  }

  public Player getPlayer() {
    return player;
  }

  public void addObserver(HandshakeObserver observer) {
    observerList.add(observer);
  }

  public void removeObserver(HandshakeObserver observer) {
    observerList.remove(observer);
  }

  private void notifyObservers() {
    for(var observer: observerList)
      observer.onCompleted(this);
  }

  /**
   * Server side of the handshake
   */
  public void receiveHandshake() {
    handshakeThread =
        new Thread(
            () -> {
              try {
                var server = MapTool.getServer();
                exception = null;
                var dos = connection.getOutputSream();
                var dis = connection.getInputStream();

                // Send the initial salt we expect the first message to use as the MAC
                byte[] initialMacSalt = CipherUtil.getInstance().createSalt();
                dos.writeInt(initialMacSalt.length);
                dos.write(initialMacSalt);

                response = new Response();
                Request request =
                    decodeRequest(
                        dis,
                        server.getConfig().getPlayerPassword(),
                        server.getConfig().getGmPassword(),
                        initialMacSalt);
                if (request == null) {
                  response.code = Code.ERROR;
                  response.message = I18N.getString("Handshake.msg.wrongPassword");
                } else if (server.isPlayerConnected(request.name)) { // Enforce a unique name
                  response.code = Code.ERROR;
                  response.message = I18N.getString("Handshake.msg.duplicateName");
                } else if (!MapTool.isDevelopment()
                    && !MapTool.getVersion().equals(request.version)
                    && !"DEVELOPMENT".equals(request.version)
                    && !"@buildNumber@".equals(request.version)) {
                  // Allows a version running without a 'version.txt' to act as client or server to
                  // any other
                  // version

                  response.code = Code.ERROR;
                  String clientUsed = request.version;
                  String serverUsed = MapTool.getVersion();
                  response.message =
                      I18N.getText("Handshake.msg.wrongVersion", clientUsed, serverUsed);
                } else {
                  response.code = Code.OK;
                }

                player = null;
                dos.writeInt(response.code);
                if (response.code == Code.OK) {
                  player =
                      new Player(request.name, Player.Role.valueOf(request.role), request.password);
                  HandshakeChallenge handshakeChallenge = new HandshakeChallenge();
                  String passwordToUse =
                      player.isGM()
                          ? MapTool.getServer().getConfig().getGmPassword()
                          : MapTool.getServer().getConfig().getPlayerPassword();

                  byte[] salt = CipherUtil.getInstance().createSalt();
                  SecretKeySpec passwordKey =
                      CipherUtil.getInstance().createSecretKeySpec(passwordToUse, salt);

                  byte[] challenge =
                      encode(
                          handshakeChallenge.getChallenge().getBytes(StandardCharsets.UTF_8),
                          passwordKey);

                  dos.writeInt(salt.length);
                  dos.write(salt);
                  dos.writeInt(challenge.length);
                  dos.write(challenge);
                  dos.write(CipherUtil.getInstance().generateMacAndSalt(passwordToUse));
                  dos.flush();

                  // Now read the response
                  int saltLen = dis.readInt();
                  byte[] responseSalt = dis.readNBytes(saltLen);
                  if (responseSalt.length != saltLen) {
                    response.code = Code.ERROR;
                    notifyObservers();
                    return; // if the salt cant be read then the handshake is invalid
                  }

                  int len = dis.readInt();
                  byte[] bytes = dis.readNBytes(len);
                  if (bytes.length != len) {
                    response.code = Code.ERROR;
                    notifyObservers();
                    return; // If the message bytes can not be read then the handshake is invalid
                  }

                  byte[] mac = CipherUtil.getInstance().readMac(dis);
                  if (!CipherUtil.getInstance().validateMac(mac, passwordToUse)) {
                    response.code = Code.ERROR;
                    notifyObservers();
                    return;
                  }
                  passwordKey =
                      CipherUtil.getInstance().createSecretKeySpec(passwordToUse, responseSalt);
                  byte[] responseBytes = decode(bytes, passwordKey);
                  String challengeResponse = new String(responseBytes);

                  if (handshakeChallenge.getExpectedResponse().equals(challengeResponse)) {
                    response.policy = server.getPolicy();
                    response.role = player.getRole();
                  } else {
                    response.message =
                        I18N.getText("Handshake.msg.badChallengeResponse", player.getName());
                    response.code = Code.ERROR;
                    player = null;
                  }

                  HessianOutput output = new HessianOutput(dos);
                  output.getSerializerFactory().setAllowNonSerializable(true);
                  output.writeObject(response);
                } else {
                  dos.writeInt(response.message.length());
                  dos.writeBytes(response.message);
                }
                notifyObservers();
              } catch (Exception e) {
                exception = e;
                log.warn(e.toString());
                response = new Response();
                response.code = Code.ERROR;
                response.message = I18N.getString("Handshake.msg.wrongPassword");
                notifyObservers();
              }
            });
    handshakeThread.start();
  }

  private byte[] decode(byte[] bytes, SecretKeySpec passwordKey) {
    try {
      Cipher cipher = CipherUtil.getInstance().createDecryptor(passwordKey);
      return cipher.doFinal(bytes);
    } catch (NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException
        | BadPaddingException
        | IllegalBlockSizeException e) {
      throw new IllegalStateException(e);
    }
  }

  private byte[] encode(byte[] bytes, SecretKeySpec passwordKey) {
    try {
      Cipher cipher = CipherUtil.getInstance().createEncrypter(passwordKey);
      return cipher.doFinal(bytes);
    } catch (NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException
        | BadPaddingException
        | IllegalBlockSizeException e) {
      throw new IllegalStateException(e);
    }
  }

  private Request extractRequestDetails(byte[] bytes, Role role) {
    String[] lines = new String(bytes).split("\n");
    Request request = new Request();
    for (String line : lines) {
      if (line.startsWith(USERNAME_FIELD)) {
        request.name = line.replace(USERNAME_FIELD, "");
      } else if (line.startsWith(VERSION_FIELD)) {
        request.version = line.replace(VERSION_FIELD, "");
      }
    }

    if (request.name != null && request.version != null) {
      request.role = role.name();
      request.password = ""; // It doesn't really matter
      return request;
    } else {
      return null;
    }
  }

  /**
   * Decrypts the handshake / login request.
   *
   * @param playerPassword
   * @param gmPassword
   * @return The decrypted {@link Request}.
   */
  private Request decodeRequest(
      DataInputStream inputStream,
      String playerPassword,
      String gmPassword,
      byte[] expectedInitialMacSalt)
      throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

    DataInputStream dis = new DataInputStream(inputStream);

    byte[] decrypted = null;
    Exception playerEx = null;
    Exception gmEx = null;

    // First retrieve the salt for the message
    int saltLen = dis.readInt();
    byte[] salt = dis.readNBytes(saltLen);
    if (salt.length != saltLen) {
      return null; // if the salt cant be read then the handshake is invalid
    }

    // retrieve the cipher message
    int messageLen = dis.readInt();
    byte[] message = dis.readNBytes(messageLen);
    if (message.length != messageLen) {
      return null; // if the message cant be read then the handshake is invalid
    }

    byte[] mac = CipherUtil.getInstance().readMac(dis);

    if (Arrays.compare(expectedInitialMacSalt, CipherUtil.getInstance().getMacSalt(mac)) != 0) {
      return null; // handshake is invalid
    }

    SecretKeySpec cipherKey = null;
    Role playerRole = null;
    if (CipherUtil.getInstance().validateMac(mac, playerPassword)) {
      cipherKey = CipherUtil.getInstance().createSecretKeySpec(playerPassword, salt);
      playerRole = Role.PLAYER;
    } else if (CipherUtil.getInstance().validateMac(mac, gmPassword)) {
      cipherKey = CipherUtil.getInstance().createSecretKeySpec(gmPassword, salt);
      playerRole = Role.GM;
    } else {
      // If neither MAC checks out then return null Request which will be logged as invalid
      // password.
      return null;
    }

    Request request = null;
    try {
      Cipher playerCipher = CipherUtil.getInstance().createDecryptor(cipherKey);
      decrypted = playerCipher.doFinal(message);
    } catch (Exception ex) {
      log.warn(I18N.getText("Handshake.msg.failedLogin", ""));
      log.warn(I18N.getText("Handshake.msg.failedLoginDecode"), ex);
      return null;
    }

    request = extractRequestDetails(decrypted, playerRole);

    return request;
  }

  /**
   * Client side of the handshake
   *
   * @param player the player who wants to handshake
   */
  public void sendHandshake(Player player) {
    handshakeThread =
        new Thread(
            () -> {
              try {
                exception = null;
                var request =
                    new Request(
                        player.getName(),
                        player.getPassword(),
                        player.getRole(),
                        MapTool.getVersion());

                var dis = connection.getInputStream();
                var dos = connection.getOutputSream();

                // Read the salt we are expected to use for initial messages MAC
                int macSaltLen = dis.readInt();
                byte[] macSalt = dis.readNBytes(macSaltLen);

                if (macSaltLen != macSalt.length) {
                  response = new Response();
                  response.code = Code.ERROR;
                  response.message = I18N.getString("Handshake.msg.wrongPassword");
                  notifyObservers();
                  return;
                }

                byte[] reqBytes = buildRequest(request, macSalt);
                dos.write(reqBytes);
                dos.flush();

                // wait for and read the challenge
                int code = dis.readInt();

                if (code == Code.OK) {
                  int saltLen = dis.readInt();
                  byte[] salt = dis.readNBytes(saltLen);
                  if (salt.length != saltLen) {
                    response = new Response();
                    response.code = Code.ERROR;
                    response.message = I18N.getString("Handshake.msg.wrongPassword");
                    notifyObservers();
                  }

                  int len = dis.readInt();
                  byte[] bytes = dis.readNBytes(len);
                  if (bytes.length != len) {
                    response = new Response();
                    response.code = Code.ERROR;
                    response.message = I18N.getString("Handshake.msg.wrongPassword");
                    notifyObservers();
                  }

                  byte[] mac = CipherUtil.getInstance().readMac(dis);
                  if (!CipherUtil.getInstance().validateMac(mac, request.password)) {
                    response = new Response();
                    response.code = Code.ERROR;
                    response.message = I18N.getString("Handshake.msg.wrongPassword");
                    notifyObservers();
                  }

                  SecretKeySpec key =
                      CipherUtil.getInstance().createSecretKeySpec(request.password, salt);
                  byte[] resp = decode(bytes, key);
                  HandshakeChallenge handshakeChallenge = new HandshakeChallenge(new String(resp));
                  byte[] responseSalt = CipherUtil.getInstance().createSalt();
                  SecretKeySpec responseKey =
                      CipherUtil.getInstance().createSecretKeySpec(request.password, responseSalt);
                  byte[] response =
                      encode(handshakeChallenge.getExpectedResponse().getBytes(), responseKey);
                  dos.writeInt(responseSalt.length);
                  dos.write(responseSalt);
                  dos.writeInt(response.length);
                  dos.write(response);
                  dos.write(CipherUtil.getInstance().generateMacAndSalt(request.password));
                } else {
                  response = new Response();
                  response.code = code;
                  int len = dis.readInt();
                  byte[] msg = dis.readNBytes(len);
                  response.message = new String(msg);
                  notifyObservers();
                }

                // If we are here the handshake succeeded so wait for the server policy
                HessianInput input = HessianUtils.createSafeHessianInput(dis);
                response = (Response) input.readObject();
                MapTool.getPlayer().setRole(response.role);
                notifyObservers();
              } catch (Exception e) {
                exception = e;
                log.warn(e.toString());
                response = new Response();
                response.code = Code.ERROR;
                response.message = I18N.getString("Handshake.msg.wrongPassword");
                notifyObservers();
              }
            });
    handshakeThread.start();
  }

  private byte[] buildRequest(Request request, byte[] macSalt)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(USERNAME_FIELD);
    sb.append(request.name);
    sb.append("\n");
    sb.append(VERSION_FIELD);
    sb.append(request.version);
    sb.append("\n");

    byte[] salt = CipherUtil.getInstance().createSalt();
    Cipher cipher = CipherUtil.getInstance().createEncryptor(request.password, salt);

    byte[] cipherBytes = cipher.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));

    byte[] mac = CipherUtil.getInstance().generateMacWithSalt(request.password, macSalt);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    dataOutputStream.writeInt(salt.length);
    dataOutputStream.write(salt);
    dataOutputStream.writeInt(cipherBytes.length);
    dataOutputStream.write(cipherBytes);
    dataOutputStream.write(mac);
    dataOutputStream.flush();

    return byteArrayOutputStream.toByteArray();
  }

  public interface Code {
    int UNKNOWN = 0;
    int OK = 1;
    int ERROR = 2;
  }

  private static class Request {
    public String name;
    public String role;
    public String password;
    public String version;

    public Request() {
      // for serialization
    }

    public Request(String name, String password, Player.Role role, String version) {
      this.name = name;
      this.password = password;
      this.role = role.name();
      this.version = version;
    }
  }

  public static class Response {
    public int code;
    public String message;
    public ServerPolicy policy;
    public Role role;
  }

  private static class HandshakeChallenge {
    private final String challenge;
    private final String expectedResponse;

    HandshakeChallenge() {
      StringBuilder challengeSb = new StringBuilder();
      StringBuilder expectedSb = new StringBuilder();
      PasswordGenerator passwordGenerator = new PasswordGenerator();
      for (int i = 0; i < 20; i++) {
        String pass = passwordGenerator.getPassword();
        challengeSb.append(pass).append("\n");
        if (i % 2 == 0) {
          expectedSb.append(pass).append("\n");
        }
      }

      challenge = challengeSb.toString();
      expectedResponse = expectedSb.toString();
    }

    HandshakeChallenge(String challengeString) {
      challenge = challengeString;
      String[] strings = challenge.split("\n");
      StringBuilder expectedSb = new StringBuilder();
      for (int i = 0; i < strings.length; i++) {
        if (i % 2 == 0) {
          expectedSb.append(strings[i]).append("\n");
        }
      }
      expectedResponse = expectedSb.toString();
    }

    public String getChallenge() {
      return challenge;
    }

    public String getExpectedResponse() {
      return expectedResponse;
    }
  }

  public interface HandshakeObserver {
    void onCompleted(Handshake handshake);
  }
}
