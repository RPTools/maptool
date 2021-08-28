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

import com.google.protobuf.ByteString;
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
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.server.proto.ChallengeRequestMsg;
import net.rptools.maptool.server.proto.ChallengeResponseMsg;
import net.rptools.maptool.server.proto.HandshakeMsg;
import net.rptools.maptool.server.proto.HandshakeRequestMsg;
import net.rptools.maptool.server.proto.HandshakeResponseMsg;
import net.rptools.maptool.server.proto.InitialSaltMsg;
import net.rptools.maptool.server.proto.ResponseCodeDto;
import net.rptools.maptool.server.proto.RoleDto;
import net.rptools.maptool.util.PasswordGenerator;
import net.rptools.maptool.util.cipher.CipherUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class Handshake implements MessageHandler {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  private static final String USERNAME_FIELD = "username:";
  private static final  String VERSION_FIELD = "version:";
  private State currentState = State.AwaitingInitialMacSalt;
  private Request request;
  private Exception exception;
  private Player player;
  private ClientConnection connection;
  private List<HandshakeObserver> observerList = new CopyOnWriteArrayList<>();
  private byte[] initialMacSalt;
  private HandshakeChallenge handshakeChallenge;
  private String errorMessage;
  private final PlayerDatabase playerDatabase;

  public Handshake(ClientConnection connection, PlayerDatabase playerDatabase) {
    this.connection = connection;
    this.playerDatabase = playerDatabase;
  }

  /**
   * Client side of the handshake
   *
   * @param player the player who wants to handshake
   */
  public Handshake(ClientConnection connection, PlayerDatabase playerDatabase, Player player) {
    this(connection, playerDatabase);
    this.player = player;
  }

  public boolean isSuccessful() {
    return currentState == State.Success;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public ClientConnection getConnection() {
    return connection;
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
    for (var observer : observerList) observer.onCompleted(this);
  }

  /** Server side of the handshake */
  public void triggerHandshake() {
    exception = null;

    // Send the initial salt we expect the first message to use as the MAC
    initialMacSalt = CipherUtil.createSalt();
    var initialSaltMsg =
        InitialSaltMsg.newBuilder().setSalt(ByteString.copyFrom(initialMacSalt)).build();

    var handshakeMsg = HandshakeMsg.newBuilder().setInitialSaltMsg(initialSaltMsg).build();
    connection.sendMessage(handshakeMsg.toByteArray());
    currentState = State.AwaitingRequest;
  }

  private byte[] decode(byte[] bytes, SecretKeySpec passwordKey) {
    try {
      return CipherUtil.fromSecretKeySpec(passwordKey).decode(bytes);
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
      return CipherUtil.fromSecretKeySpec(passwordKey).decode(bytes);
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
      HandshakeRequestMsg handshakeRequestMsg,
      String playerPassword,
      String gmPassword,
      byte[] expectedInitialMacSalt)
      throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

    byte[] decrypted = null;
    Exception playerEx = null;
    Exception gmEx = null;

    // First retrieve the salt for the message
    byte[] salt = handshakeRequestMsg.getSalt().toByteArray();

    // retrieve the cipher message
    byte[] message = handshakeRequestMsg.getCypherText().toByteArray();

    byte[] mac = handshakeRequestMsg.getMac().toByteArray();

    if (Arrays.compare(expectedInitialMacSalt, CipherUtil.getMacSalt(mac)) != 0) {
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

  private HandshakeMsg buildRequest(Request request, byte[] macSalt)
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

    var req =
        HandshakeRequestMsg.newBuilder()
            .setSalt(ByteString.copyFrom(salt))
            .setCypherText(ByteString.copyFrom(cipherBytes))
            .setMac(ByteString.copyFrom(mac))
            .build();

    return HandshakeMsg.newBuilder().setHandshakeRequestMsg(req).build();
  }

  private void sendErrorResponseAndNotify() {
    var responseMsg =
        HandshakeResponseMsg.newBuilder()
            .setCode(ResponseCodeDto.ERROR)
            .setMessage(errorMessage)
            .build();
    var msg = HandshakeMsg.newBuilder().setHandshakeResponseMsg(responseMsg).build();
    connection.sendMessage(msg.toByteArray());
    currentState = State.Error;
    notifyObservers();
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var handshakeMsg = HandshakeMsg.parseFrom(message);
      switch (currentState) {
        case AwaitingInitialMacSalt:
          if (handshakeMsg.getMessageTypeCase() == HandshakeMsg.MessageTypeCase.INITIAL_SALT_MSG)
            handle(handshakeMsg.getInitialSaltMsg());
          break;
        case AwaitingRequest:
          if (handshakeMsg.getMessageTypeCase()
              == HandshakeMsg.MessageTypeCase.HANDSHAKE_REQUEST_MSG)
            handle(handshakeMsg.getHandshakeRequestMsg());
          break;
        case AwaitingChallenge:
          if (handshakeMsg.getMessageTypeCase()
              == HandshakeMsg.MessageTypeCase.CHALLENGE_REQUEST_MSG)
            handle(handshakeMsg.getChallengeRequestMsg());
          // we only accept error responses in this state
          if (handshakeMsg.getMessageTypeCase()
                  == HandshakeMsg.MessageTypeCase.HANDSHAKE_RESPONSE_MSG
              && handshakeMsg.getHandshakeResponseMsg().getCode() != ResponseCodeDto.OK)
            handle(handshakeMsg.getHandshakeResponseMsg());
          break;
        case AwaitingChallengeResponse:
          if (handshakeMsg.getMessageTypeCase()
              == HandshakeMsg.MessageTypeCase.CHALLENGE_RESPONSE_MSG)
            handle(handshakeMsg.getChallengeResponseMsg());
          // we only accept error responses in this state
          if (handshakeMsg.getMessageTypeCase()
                  == HandshakeMsg.MessageTypeCase.HANDSHAKE_RESPONSE_MSG
              && handshakeMsg.getHandshakeResponseMsg().getCode() != ResponseCodeDto.OK)
            handle(handshakeMsg.getHandshakeResponseMsg());
          break;
        case AwaitingResponse:
          handle(handshakeMsg.getHandshakeResponseMsg());
          break;
      }
    } catch (Exception e) {
      exception = e;
      log.warn(e.toString());
      currentState = State.Error;
      errorMessage = e.getMessage();
      notifyObservers();
    }
  }

  private void handle(InitialSaltMsg initialSaltMsg)
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
          BadPaddingException, InvalidKeySpecException, IOException, InvalidKeyException {
    request =
        new Request(player.getName(), player.getPassword(), player.getRole(), MapTool.getVersion());
    var requestMsg = buildRequest(request, initialSaltMsg.getSalt().toByteArray());
    connection.sendMessage(requestMsg.toByteArray());
    currentState = State.AwaitingChallenge;
  }

  private void handle(HandshakeRequestMsg handshakeRequestMsg)
      throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
    var server = MapTool.getServer();
    request =
        decodeRequest(
            handshakeRequestMsg,
            server.getConfig().getPlayerPassword(),
            server.getConfig().getGmPassword(),
            initialMacSalt);
    if (request == null) {
      errorMessage = I18N.getString("Handshake.msg.wrongPassword");
      sendErrorResponseAndNotify();
      return;
    }
    if (server.isPlayerConnected(request.name)) { // Enforce a unique name
      errorMessage = I18N.getString("Handshake.msg.duplicateName");
      sendErrorResponseAndNotify();
      return;
    }
    if (!MapTool.isDevelopment()
        && !MapTool.getVersion().equals(request.version)
        && !"DEVELOPMENT".equals(request.version)
        && !"@buildNumber@".equals(request.version)) {
      // Allows a version running without a 'version.txt' to act as client or server to
      // any other
      // version

      String clientUsed = request.version;
      String serverUsed = MapTool.getVersion();

      errorMessage = I18N.getText("Handshake.msg.wrongVersion", clientUsed, serverUsed);
      sendErrorResponseAndNotify();
      return;
    }

    player = new Player(request.name, Player.Role.valueOf(request.role), request.password);
    handshakeChallenge = new HandshakeChallenge();
    String passwordToUse =
        player.isGM()
            ? MapTool.getServer().getConfig().getGmPassword()
            : MapTool.getServer().getConfig().getPlayerPassword();

    byte[] salt = CipherUtil.getInstance().createSalt();
    SecretKeySpec passwordKey = CipherUtil.getInstance().createSecretKeySpec(passwordToUse, salt);

    byte[] challenge =
        encode(handshakeChallenge.getChallenge().getBytes(StandardCharsets.UTF_8), passwordKey);

    var challengeMsg =
        ChallengeRequestMsg.newBuilder()
            .setSalt(ByteString.copyFrom(salt))
            .setChallenge(ByteString.copyFrom(challenge))
            .setMac(ByteString.copyFrom(CipherUtil.getInstance().generateMacAndSalt(passwordToUse)))
            .build();
    var msg = HandshakeMsg.newBuilder().setChallengeRequestMsg(challengeMsg).build();
    connection.sendMessage(msg.toByteArray());
    currentState = State.AwaitingChallengeResponse;
  }

  private void handle(ChallengeRequestMsg challengeRequestMsg)
      throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
    byte[] salt = challengeRequestMsg.getSalt().toByteArray();
    byte[] bytes = challengeRequestMsg.getChallenge().toByteArray();
    byte[] mac = challengeRequestMsg.getMac().toByteArray();
    if (!CipherUtil.getInstance().validateMac(mac, request.password)) {
      errorMessage = I18N.getString("Handshake.msg.wrongPassword");
      sendErrorResponseAndNotify();
      return;
    }

    SecretKeySpec key = CipherUtil.getInstance().createSecretKeySpec(request.password, salt);
    byte[] resp = decode(bytes, key);
    HandshakeChallenge handshakeChallenge = new HandshakeChallenge(new String(resp));
    byte[] responseSalt = CipherUtil.getInstance().createSalt();
    SecretKeySpec responseKey =
        CipherUtil.getInstance().createSecretKeySpec(request.password, responseSalt);
    byte[] response = encode(handshakeChallenge.getExpectedResponse().getBytes(), responseKey);

    var challengeResp =
        ChallengeResponseMsg.newBuilder()
            .setSalt(ByteString.copyFrom(responseSalt))
            .setResponse(ByteString.copyFrom(response))
            .setMac(
                ByteString.copyFrom(CipherUtil.getInstance().generateMacAndSalt(request.password)))
            .build();
    var msg = HandshakeMsg.newBuilder().setChallengeResponseMsg(challengeResp).build();
    connection.sendMessage(msg.toByteArray());
    currentState = State.AwaitingResponse;
  }

  private void handle(ChallengeResponseMsg challengeResponseMsg)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var server = MapTool.getServer();
    byte[] responseSalt = challengeResponseMsg.getSalt().toByteArray();
    byte[] bytes = challengeResponseMsg.getResponse().toByteArray();

    String passwordToUse =
        player.isGM() ? server.getConfig().getGmPassword() : server.getConfig().getPlayerPassword();

    byte[] mac = challengeResponseMsg.getMac().toByteArray();
    if (!CipherUtil.getInstance().validateMac(mac, passwordToUse)) {
      errorMessage = I18N.getText("Handshake.msg.wrongPassword");
      sendErrorResponseAndNotify();
      return;
    }
    var passwordKey = CipherUtil.getInstance().createSecretKeySpec(passwordToUse, responseSalt);
    byte[] responseBytes = decode(bytes, passwordKey);
    String challengeResponse = new String(responseBytes);

    if (!handshakeChallenge.getExpectedResponse().equals(challengeResponse)) {
      errorMessage = I18N.getText("Handshake.msg.badChallengeResponse", player.getName());
      player = null;
      sendErrorResponseAndNotify();
      return;
    }

    var policy = Mapper.map(server.getPolicy());
    var role = RoleDto.valueOf(player.getRole().name());

    currentState = State.Success;

    var responseMsg =
        HandshakeResponseMsg.newBuilder()
            .setCode(ResponseCodeDto.OK)
            .setPolicy(policy)
            .setRole(role)
            .build();

    var msg = HandshakeMsg.newBuilder().setHandshakeResponseMsg(responseMsg).build();
    connection.sendMessage(msg.toByteArray());
    notifyObservers();
  }

  private void handle(HandshakeResponseMsg handshakeResponseMsg) {
    switch (handshakeResponseMsg.getCode()) {
      case ERROR:
        errorMessage = handshakeResponseMsg.getMessage();
        currentState = State.Error;
        notifyObservers();
        return;
      case OK:
        // If we are here the handshake succeeded so wait for the server policy
        MapTool.getPlayer().setRole(Role.valueOf(handshakeResponseMsg.getRole().name()));
        var policy = Mapper.map(handshakeResponseMsg.getPolicy());
        MapTool.setServerPolicy(policy);
        currentState = State.Success;
        notifyObservers();
        break;
    }
  }

  private enum State {
    Error,
    AwaitingInitialMacSalt,
    AwaitingRequest,
    AwaitingChallenge,
    AwaitingChallengeResponse,
    Success,
    AwaitingResponse
  }

  public interface HandshakeObserver {
    void onCompleted(Handshake handshake);
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
