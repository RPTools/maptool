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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool.util.CipherUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class Handshake {

  public interface Code {
    public static final int UNKNOWN = 0;
    public static final int OK = 1;
    public static final int ERROR = 2;
  }

  private static String USERNAME_FIELD = "username:";
  private static String VERSION_FIELD = "version:";


  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  /** The {@link Cipher} used for decoding using player password. */
  private static Cipher playerCipher;
  /** The {@link Cipher} used for decoding using gm password. */
  private static Cipher gmCipher;


  /**
   * Set the encryption keys used for decrypting the handshake / login.
   * @param playerKey The key used for decrypting using the player password.
   * @param gmKey The key used for decrypting using the gm password.
   * @throws NoSuchAlgorithmException If the encryption algorithm is not supported.
   * @throws InvalidKeyException If the decryption key is invalid.
   * @throws NoSuchPaddingException If the padding mechanism is not supported..
   */
  public static void setEncryptionKeys(SecretKeySpec playerKey, SecretKeySpec gmKey)
      throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
    playerCipher = CipherUtil.getInstance().createDecrypter(MapTool.getServer().getP)
    gmCipher = Cipher.getInstance("AES");

    playerCipher.init(Cipher.DECRYPT_MODE, MapTool.getServer().getConfig().getPlayerPasswordKey());
    gmCipher.init(Cipher.DECRYPT_MODE, MapTool.getServer().getConfig().getGMPasswordKey());
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
  public static Player receiveHandshake(MapToolServer server, Socket s) throws IOException {

    Response response = new Response();
    Request request = decodeRequest(s);
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


    HessianOutput output = new HessianOutput(s.getOutputStream());
    response.policy = server.getPolicy();
    output.writeObject(response);
    return response.code == Code.OK
        ? new Player(request.name, Player.Role.valueOf(request.role), request.password)
        : null;
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
   * @param socket The network socket for the connection.
   * @return The decrypted {@link Request}.
   */
  private static Request decodeRequest(Socket socket) throws IOException {
    InputStream inputStream = socket.getInputStream();
    String text = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
    byte[] decrypted = null;
    Exception playerEx = null;
    Exception gmEx = null;

    // First try to decode with the player password
    try {
      decrypted = playerCipher.doFinal(text.getBytes());
    } catch (Exception ex) {
      playerEx = ex;
      // Do nothing as we will report error later if GM password also fails
    }

    Request request = null;
    if (decrypted != null) {
      request = extractRequestDetails(decrypted, Role.PLAYER);
    }

    if (request == null) {
      try {
        decrypted = gmCipher.doFinal(text.getBytes());
        request = extractRequestDetails(decrypted, Role.PLAYER);
      } catch (Exception ex) {
        gmEx = ex;
        // Do nothing as weil will report error along with player password error.
      }
    }


    if (playerEx != null || gmEx != null) {
      log.warn(I18N.getText("Handshake.msg.failedLogin", socket.getInetAddress()));
      log.warn(I18N.getText("Handshake.msg.failedLoginPlayer", playerEx));
      log.warn(I18N.getText("Handshake.msg.failedLoginGM", gmEx));
    }

    return request;

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
  public static Response sendHandshake(Request request, Socket s) throws IOException {
    HessianInput input = new HessianInput(s.getInputStream());
    // HessianOutput output = new HessianOutput(s.getOutputStream());
    // Jamz: Method renamed in Hessian 4.0.+
    // output.findSerializerFactory().setAllowNonSerializable(true);
    // output.getSerializerFactory().setAllowNonSerializable(true);
    //output.writeObject(request);

    byte[] reqBytes = buildRequest(request);


    return (Response) input.readObject();
  }

  private static byte[] buildRequest(Request request) {
    StringBuilder sb = new StringBuilder();
    sb.append(USERNAME_FIELD);
    sb.append(request.name);
    sb.append("\n");
    sb.append(VERSION_FIELD);
    sb.append(request.version);
    sb.append("\n");

    Cipher cipher = Cipher.getInstance("AES");

    SecretKeySpec passordKey = new SecretKeySpec(request.password.getBytes(), "AES");

    cipher.init(Cipher.DECRYPT_MODE, MapTool.getServer().getConfig().getPlayerPasswordKey());
    return sb.toString().getBytes();
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
  }
}
