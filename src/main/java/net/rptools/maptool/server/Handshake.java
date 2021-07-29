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
import com.rometools.rome.io.impl.Base64;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.*;
import net.rptools.clientserver.hessian.HessianUtils;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.PasswordGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class Handshake {

  public interface Code {
    int UNKNOWN = 0;
    int OK = 1;
    int ERROR = 2;
  }

  private static final String USERNAME_FIELD = "username:";
  private static final String VERSION_FIELD = "version:";

  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);


  private final PlayerDatabase playerDatabase;


  public Handshake(PlayerDatabase database) {
    playerDatabase = database;
  }


  /**
   * Server side of the handshake
   *
   * @param server the MapTool server instance
   * @param s the server socket
   * @throws IOException if an I/O error occurs when creating the input stream, the socket is
   *     closed, the socket is not connected, or the socket input has been shutdown using
   * @return A player structure for the connected player or null on issues
   * @throws IOException if there is a problem reading from the socket.
   */
  public Player receiveHandshake(MapToolServer server, Socket s)
      throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

    DataInputStream dis = new DataInputStream(s.getInputStream());
    int nameLen = dis.readInt();
    byte[] nameBytes = dis.readNBytes(nameLen);

    if (nameLen != nameBytes.length) {
      throw new IOException("Unable to read username");
    }

    String username = new String(nameBytes);


    // Send the initial salt we expect the first message to use as the MAC
    byte[] initialMacSalt = CipherUtil.createSalt();
    dos.writeInt(initialMacSalt.length);
    dos.write(initialMacSalt);

    if (!playerDatabase.playerExists(username)) {
      log.error(username + " does not exist.");
      return null;
    }
    System.err.println(playerDatabase);

    Optional<CipherUtil.Key> key = playerDatabase.getPlayerPassword(username);
    CipherUtil.Key playerPassword;
    CipherUtil.Key gmPassword;
    byte[] passwordSalt;
    if (key.isPresent()) {
      Role playerRole = playerDatabase.getPlayer(username).getRole();
      playerPassword = playerRole == Role.PLAYER ? key.get() : null;
      gmPassword = playerRole == Role.GM ? key.get() : null;
      passwordSalt = key.get().salt();
    } else { // Role based authentication
      playerPassword = playerDatabase.getRolePassword(Role.PLAYER).orElse(null);
      gmPassword = playerDatabase.getRolePassword(Role.GM).orElse(null);
      passwordSalt = playerPassword.salt();
    }

    dos.writeInt(passwordSalt.length);
    dos.write(passwordSalt);

    Response response = new Response();
    Request request =
        decodeRequest(
            s,
            playerPassword,
            gmPassword,
            username,
            initialMacSalt,
            passwordSalt);
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
      // Allows a version running without a 'version.txt' to act as client or server to any other
      // version

      response.code = Code.ERROR;
      String clientUsed = request.version;
      String serverUsed = MapTool.getVersion();
      response.message = I18N.getText("Handshake.msg.wrongVersion", clientUsed, serverUsed);
    } else {
      response.code = Code.OK;
    }

    Player player = null;
    dos.writeInt(response.code);
    if (response.code == Code.OK) {

      Player.Role role = Player.Role.valueOf(request.role);
      CipherUtil.Key passwordToUse = role == Player.Role.GM ? gmPassword : playerPassword;

      player = new Player(request.name, Player.Role.valueOf(request.role), passwordToUse);
      HandshakeChallenge handshakeChallenge = new HandshakeChallenge();

      CipherUtil cipherUtil = CipherUtil.fromKey(passwordToUse);

      byte[] challenge =
          cipherUtil.encode(handshakeChallenge.getChallenge().getBytes(StandardCharsets.UTF_8));

      dos.writeInt(passwordToUse.salt().length);
      dos.write(passwordToUse.salt());
      dos.writeInt(challenge.length);
      dos.write(challenge);
      dos.write(CipherUtil.generateMacAndSalt(
          CipherUtil.encodeBase64(passwordToUse.secretKeySpec())
      ));
      dos.flush();

      // Now read the response
      int saltLen = dis.readInt();
      byte[] responseSalt = dis.readNBytes(saltLen);
      if (responseSalt.length != saltLen) {
        return null; // if the salt cant be read then the handshake is invalid
      }

      int len = dis.readInt();
      byte[] bytes = dis.readNBytes(len);
      if (bytes.length != len) {
        return null; // If the message bytes can not be read then the handshake is invalid
      }

      byte[] mac = CipherUtil.readMac(dis);
      if (!CipherUtil.validateMac(mac,
          CipherUtil.encodeBase64(passwordToUse.secretKeySpec()))) {
        return null;
      }

      byte[] responseBytes = cipherUtil.decode(bytes);
      String challengeResponse = new String(responseBytes);

      if (handshakeChallenge.getExpectedResponse().equals(challengeResponse)) {
        response.policy = server.getPolicy();
        response.role = player.getRole();
      } else {
        response.message = I18N.getText("Handshake.msg.badChallengeResponse", player.getName());
        response.code = Code.ERROR;
        player = null;
      }

      HessianOutput output = new HessianOutput(s.getOutputStream());
      output.getSerializerFactory().setAllowNonSerializable(true);
      output.writeObject(response);
    } else {
      dos.writeInt(response.message.length());
      dos.writeBytes(response.message);
    }
    return player;
  }

  private static Request extractRequestDetails(byte[] bytes, Role role) {
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
   * @param socket The network socket for the connection.
   * @param playerPassword
   * @param gmPassword
   * @return The decrypted {@link Request}.
   */
  private static Request decodeRequest(
      Socket socket, CipherUtil.Key playerPassword, CipherUtil.Key gmPassword, String username,
      byte[] expectedInitialMacSalt,
      byte[] passwordSalt)
      throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

    socket.getInetAddress().getAddress();
    InputStream inputStream = socket.getInputStream();
    DataInputStream dis = new DataInputStream(inputStream);

    byte[] decrypted = null;

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

    byte[] mac = CipherUtil.readMac(dis);

    if (Arrays.compare(expectedInitialMacSalt, CipherUtil.getMacSalt(mac)) != 0) {
      return null; // handshake is invalid
    }

    CipherUtil.Key cipherKey = null;
    Role playerRole = null;
    if (playerPassword != null && CipherUtil.validateMac(mac,
        CipherUtil.encodeBase64(playerPassword.secretKeySpec()))) {
      cipherKey = playerPassword;
      playerRole = Role.PLAYER;
    } else if (gmPassword != null && CipherUtil.validateMac(mac,
        CipherUtil.encodeBase64(gmPassword.secretKeySpec()))) {
      cipherKey = gmPassword;
      playerRole = Role.GM;
    } else {
      // If neither MAC checks out then return null Request which will be logged as invalid
      // password.
      return null;
    }

    try {
      CipherUtil cipherUtil = CipherUtil.fromKey(cipherKey);
      decrypted = cipherUtil.decode(message);
    } catch (Exception ex) {
      log.warn(I18N.getText("Handshake.msg.failedLogin", socket.getInetAddress()));
      log.warn(I18N.getText("Handshake.msg.failedLoginDecode"), ex);
      return null;
    }

    return extractRequestDetails(decrypted, playerRole);
  }

  /**
   * Client side of the handshake
   *
   * @param request the handshake request
   * @param s the socket to send the request on
   * @throws IOException if an I/O error occurs when creating the input stream, the socket is
   *     closed, the socket is not connected, or the socket input has been shutdown using
   * @return the response from the srever
   */
  public Response sendHandshake(Request request, Socket s)
      throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {

    // First send the username
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    dos.writeInt(request.name.length());
    dos.write(request.name.getBytes(StandardCharsets.UTF_8));

    DataInputStream dis = new DataInputStream(s.getInputStream());

    // Read the salt we are expected to use for initial messages MAC
    int macSaltLen = dis.readInt();
    byte[] macSalt = dis.readNBytes(macSaltLen);


    if (macSaltLen != macSalt.length) {
      Response response = new Response();
      response.code = Code.ERROR;
      response.message = I18N.getString("Handshake.msg.wrongPassword");
      return response;
    }

    // Read the salt we are expected to user for player password
    int passwordSaltLen = dis.readInt();
    byte[] passwordSalt = dis.readNBytes(passwordSaltLen);

    System.out.println("DEBUG: sendHS PW Salt = " + new String(Base64.encode(passwordSalt)));

    if (passwordSaltLen != passwordSalt.length) {
      Response response = new Response();
      response.code = Code.ERROR;
      response.message = I18N.getString("Handshake.msg.wrongPassword");
      return response;
    }


    byte[] reqBytes = buildRequest(request, macSalt, passwordSalt);
    dos.write(reqBytes);
    dos.flush();

    // wait for and read the challenge
    int code = dis.readInt();

    if (code == Code.OK) {
      int saltLen = dis.readInt();
      byte[] salt = dis.readNBytes(saltLen);
      if (salt.length != saltLen) {
        Response response = new Response();
        response.code = Code.ERROR;
        response.message = I18N.getString("Handshake.msg.wrongPassword");
        return response;
      }

      int len = dis.readInt();
      byte[] bytes = dis.readNBytes(len);
      if (bytes.length != len) {
        Response response = new Response();
        response.code = Code.ERROR;
        response.message = I18N.getString("Handshake.msg.wrongPassword");
        return response;
      }

      byte[] mac = CipherUtil.readMac(dis);

      CipherUtil cipherUtil = CipherUtil.fromSharedKey(request.password, passwordSalt);


      if (!CipherUtil.validateMac(mac,
          CipherUtil.encodeBase64(cipherUtil.getKey().secretKeySpec()))) {
        Response response = new Response();
        response.code = Code.ERROR;
        response.message = I18N.getString("Handshake.msg.wrongPassword");
        return response;
      }


      byte[] resp = cipherUtil.decode(bytes);
      HandshakeChallenge handshakeChallenge = new HandshakeChallenge(new String(resp));
      byte[] responseSalt = CipherUtil.createSalt();
      byte[] response =
          cipherUtil.encode(handshakeChallenge.getExpectedResponse().getBytes(StandardCharsets.UTF_8));
      dos.writeInt(responseSalt.length);
      dos.write(responseSalt);
      dos.writeInt(response.length);
      dos.write(response);
      dos.write(CipherUtil.generateMacAndSalt(CipherUtil.encodeBase64(cipherUtil.getKey().secretKeySpec())));
    } else {
      Response response = new Response();
      response.code = code;
      int len = dis.readInt();
      byte[] msg = dis.readNBytes(len);
      response.message = new String(msg);
      return response;
    }

    // If we are here the handshake succeeded so wait for the server policy
    HessianInput input = HessianUtils.createSafeHessianInput(s.getInputStream());
    Response response = (Response) input.readObject();
    MapTool.getPlayer().setRole(response.role);
    return response;
  }

  private static byte[] buildRequest(Request request, byte[] macSalt, byte[] passwordSalt)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(USERNAME_FIELD);
    sb.append(request.name);
    sb.append("\n");
    sb.append(VERSION_FIELD);
    sb.append(request.version);
    sb.append("\n");

    CipherUtil cipherUtil = CipherUtil.fromSharedKey(request.password, passwordSalt);

    byte[] cipherBytes =  cipherUtil.encode(sb.toString().getBytes(StandardCharsets.UTF_8));

    System.out.println("DEBUG: Send = " + CipherUtil.encodeBase64(cipherUtil.getKey().secretKeySpec()));
    byte[] mac = CipherUtil.generateMacWithSalt(
        CipherUtil.encodeBase64(cipherUtil.getKey().secretKeySpec()),
        macSalt
    );

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    dataOutputStream.writeInt(passwordSalt.length);
    dataOutputStream.write(passwordSalt);
    dataOutputStream.writeInt(cipherBytes.length);
    dataOutputStream.write(cipherBytes);
    dataOutputStream.write(mac);
    dataOutputStream.flush();

    return byteArrayOutputStream.toByteArray();
  }

  public static class Request {
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
}
