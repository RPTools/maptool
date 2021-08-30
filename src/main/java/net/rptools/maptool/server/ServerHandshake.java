package net.rptools.maptool.server;

import com.google.protobuf.ByteString;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.server.proto.AuthTypeEnum;
import net.rptools.maptool.server.proto.ClientAuthMsg;
import net.rptools.maptool.server.proto.ClientInitMsg;
import net.rptools.maptool.server.proto.ConnectionSuccessfulMsg;
import net.rptools.maptool.server.proto.HandshakeMsg;
import net.rptools.maptool.server.proto.HandshakeMsg.MessageTypeCase;
import net.rptools.maptool.server.proto.HandshakeResponseCodeMsg;
import net.rptools.maptool.server.proto.RoleDto;
import net.rptools.maptool.server.proto.UseAuthTypeMsg;
import net.rptools.maptool.util.PasswordGenerator;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.CipherUtil.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to handle the server side part of the connection handshake.
 */
public class ServerHandshake implements MessageHandler {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(ServerHandshake.class);

  /** The database used for retrieving players. */
  private final PlayerDatabase playerDatabase;

  /** The connection to the client. */
  private final ClientConnection connection;

  /** Observers that want to be notified when the status changes. */
  private final List<ServerHandshakeObserver> observerList = new CopyOnWriteArrayList<>();

  /** The index in the array for the GM handshake challenge, only used for role based auth */
  private final static int GM_CHALLENGE = 0;
  /** The index in the array for the Player handshake challenge, only used for role based auth */
  private final static int PLAYER_CHALLENGE = 1;


  /** Message for any error that has occurred, {@code null} if no error has occurred. */
  private String errorMessage;

  /**
   * Any exception that occurred that causes an error, {@code null} if no
   * exception which causes an error has occurred.
   */
  private Exception exception;

  /**
   * The player that this connection is for.
   */
  private Player player;


  /**
   * The current state of the handshake process.
   */
  private State currentState = State.AwaitingClientInit;

  /**
   * Challenges sent to the client.
   */
  private HandshakeChallenge[] handshakeChallenges;

  private MD5Key playerPublicKeyMD5;

  /**
   * Creates a new {@code ServerHandshake} instance.
   * @param connection The client connection for the handshake.
   * @param playerDatabase The database of players.
   */
  public ServerHandshake(ClientConnection connection, PlayerDatabase playerDatabase) {
    this.connection = connection;
    this.playerDatabase = playerDatabase;
  }

  /**
   * Returns if the handshake has been successful or not.
   * @return {@code true} if the handshake has been successful, {code false} if it has failed or is
   * still in progress.
   */
  public boolean isSuccessful() {
    return currentState == State.Success;
  }

  /**
   * Returns the message for the error -- if any -- that occurred during the handshake.
   * @return the message for the error that occurred during handshake.
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Returns the connection for this {@code ServerHandshake}.
   * @return the connection for this {@code ServerHandshake}.
   */
  public ClientConnection getConnection() {
    return connection;
  }

  /**
   * Returns the exception -- if any -- that occurred during processing of the handshake.
   * @return the exception that occurred during the processing of the handshake.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Returns the player associated with the handshake.
   * @return the player associated with the handshake.
   */
  public Player getPlayer() {
    return player;
  }


  /**
   * Sends an error response to the client and notifies any observers of the handshake that the
   * status has changed.
   * @param errorCode The error code that should be sent to the client.
   */
  private void sendErrorResponseAndNotify(HandshakeResponseCodeMsg errorCode) {
    var msg = HandshakeMsg.newBuilder().setHandshakeResponseCodeMsg(errorCode).build();
    connection.sendMessage(msg.toByteArray());
    currentState = State.Error;
    notifyObservers();
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var handshakeMsg = HandshakeMsg.parseFrom(message);
      var msgType = handshakeMsg.getMessageTypeCase();

      if (msgType == MessageTypeCase.HANDSHAKE_RESPONSE_CODE_MSG) {
        HandshakeResponseCodeMsg code = handshakeMsg.getHandshakeResponseCodeMsg();
        if (code.equals(HandshakeResponseCodeMsg.INVALID_PASSWORD)) {
          errorMessage = I18N.getText("Handshake.msg.incorrectPassword");
        } else if (code.equals(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY)) {
          errorMessage = I18N.getText("Handshake.msg.incorrectPublicKey");
        } else {
          errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
        }

        notifyObservers();
        return;
      }

      switch (currentState) {
        case AwaitingClientInit:
          if (msgType == HandshakeMsg.MessageTypeCase.CLIENT_INIT_MSG) {
            handle(handshakeMsg.getClientInitMsg());
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
            sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_HANDSHAKE);
          }
          break;
        case AwaitingClientAuth:
          if (msgType == MessageTypeCase.CLIENT_AUTH_MESSAGE) {
            handle(handshakeMsg.getClientAuthMessage());
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
            sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_HANDSHAKE);
          }
      }
    } catch (Exception e) {
      log.warn(e.toString());
      exception = e;
      currentState = State.Error;
      errorMessage = e.getMessage();
      notifyObservers();
    }

  }

  private void handle(ClientAuthMsg clientAuthMessage)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] response = clientAuthMessage.getChallengeResponse().toByteArray();
    if (handshakeChallenges.length > 1) {
      if (Arrays.compare(response, handshakeChallenges[GM_CHALLENGE].getExpectedResponse()) == 0) {
        player = playerDatabase.getPlayerWithRole(player.getName(), Role.GM);
      } else if ( Arrays.compare(response,
          handshakeChallenges[PLAYER_CHALLENGE].getExpectedResponse()) == 0) {
        player = playerDatabase.getPlayerWithRole(player.getName(), Role.PLAYER);
      } else {
        sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PASSWORD);
        return;
      }
    } else {
      if (Arrays.compare(response, handshakeChallenges[0].getExpectedResponse()) != 0) {
        sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY);
        return;
      }
    }
    sendConnectionSuccessful();
  }

  private void sendConnectionSuccessful() {
    var server = MapTool.getServer();
    var policy = Mapper.map(server.getPolicy());
    var connectionSuccessfulMsg = ConnectionSuccessfulMsg.newBuilder()
        .setRoleDto(player.isGM() ? RoleDto.GM : RoleDto.PLAYER)
        .setServerPolicyDto(policy)
        .build();
    connection.sendMessage(connectionSuccessfulMsg.toByteArray());
  }

  /**
   * This method handles the initial message that the client sends as part of the handshake process.
   *
   * @param clientInitMsg The initial message sent from the client.
   *
   * @throws ExecutionException when there is an error fetching the public key.
   * @throws InterruptedException when there is an error fetching the public key.
   * @throws NoSuchPaddingException when there is an error during encryption.
   * @throws IllegalBlockSizeException when there is an error during encryption.
   * @throws NoSuchAlgorithmException when there is an error during encryption.
   * @throws BadPaddingException when there is an error during encryption.
   * @throws InvalidKeyException when there is an error during encryption.
   */
  private void handle(ClientInitMsg clientInitMsg)
      throws ExecutionException, InterruptedException, NoSuchPaddingException,
          IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
          InvalidKeyException {
    var server = MapTool.getServer();
    if (server.isPlayerConnected(clientInitMsg.getPlayerName())) {
      errorMessage = I18N.getText("Handshake.msg.duplicateName");
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.PLAYER_ALREADY_CONNECTED);
      return;
    }

    if (!MapTool.isDevelopment() && MapTool.getVersion().equals(clientInitMsg.getVersion())) {
      errorMessage = I18N.getText("Handshake.msg.wrongVersion");
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.WRONG_VERSION);
    }

    playerPublicKeyMD5 = new MD5Key(clientInitMsg.getPublicKeyMd5());

    try {
      player = playerDatabase.getPlayer(clientInitMsg.getPlayerName());
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      errorMessage = I18N.getText("Handshake.msg.encodeInitFail", clientInitMsg.getPlayerName());
      // Error fetching player is sent to client as invalid password intentionally.
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PASSWORD);
      return;
    }

    if (player == null) {
      // Unknown player is sent to client as invalid password intentionally.
      errorMessage = I18N.getText("Handshake.msg.unknownPlayer", clientInitMsg.getPlayerName());
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PASSWORD);
      return;
    }

    if (playerDatabase.getAuthMethod(player) == AuthMethod.ASYMMETRIC_KEY) {
      sendAsymmetricKeyAuthType();
    } else {
      handshakeChallenges = new HandshakeChallenge[2];
      if (playerDatabase.supportsRolePasswords()) {
        sendRoleSharedPasswordAuthType();
      } else {
        sendSharedPasswordAuthType();
      }
    }
    currentState = State.AwaitingClientAuth;
  }

  /**
   * Send the authentication type message when using per player shared passwords.
   * @throws NoSuchPaddingException when there is an error during encryption.
   * @throws IllegalBlockSizeException when there is an error during encryption.
   * @throws NoSuchAlgorithmException when there is an error during encryption.
   * @throws BadPaddingException when there is an error during encryption.
   * @throws InvalidKeyException when there is an error during encryption.
   */
  private void sendSharedPasswordAuthType()
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    byte[] playerPasswordSalt = playerDatabase.getPlayerPasswordSalt(player.getName());
    String password = new PasswordGenerator().getPassword();
    handshakeChallenges = new HandshakeChallenge[0];
    Key key = playerDatabase.getPlayerPassword(player.getName()).get();
    handshakeChallenges[0] = HandshakeChallenge.createChallenge(player.getName(), password, key);

    var authTypeMsg = UseAuthTypeMsg.newBuilder()
        .setAuthType(AuthTypeEnum.SHARED_PASSWORD)
        .setSalt(ByteString.copyFrom(playerPasswordSalt))
        .addChallenge(ByteString.copyFrom(handshakeChallenges[0].getChallenge()))
        .build();
    connection.sendMessage(authTypeMsg.toByteArray());
  }

  /**
   * Send the authentication type message when using role based shared passwords.
   * @throws NoSuchPaddingException when there is an error during encryption.
   * @throws IllegalBlockSizeException when there is an error during encryption.
   * @throws NoSuchAlgorithmException when there is an error during encryption.
   * @throws BadPaddingException when there is an error during encryption.
   * @throws InvalidKeyException when there is an error during encryption.
   */
  private void sendRoleSharedPasswordAuthType()
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    byte[] playerPasswordSalt = playerDatabase.getPlayerPasswordSalt(player.getName());
    String[] password = new String[2];
    password[GM_CHALLENGE] = new PasswordGenerator().getPassword();
    password[PLAYER_CHALLENGE] = new PasswordGenerator().getPassword();

    handshakeChallenges[GM_CHALLENGE] =
        HandshakeChallenge.createChallenge(
            player.getName(), password[GM_CHALLENGE], playerDatabase.getRolePassword(Role.GM).get());
    handshakeChallenges[PLAYER_CHALLENGE] =
        HandshakeChallenge.createChallenge(
            player.getName(), password[GM_CHALLENGE], playerDatabase.getRolePassword(Role.PLAYER).get());

    var authTypeMsg = UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.SHARED_PASSWORD)
            .setSalt(ByteString.copyFrom(playerPasswordSalt))
            .addChallenge(ByteString.copyFrom(handshakeChallenges[GM_CHALLENGE].getChallenge()))
            .addChallenge(ByteString.copyFrom(handshakeChallenges[PLAYER_CHALLENGE].getChallenge()))
            .build();
    connection.sendMessage(authTypeMsg.toByteArray());
  }


  /**
   * Send the authentication type message when using asymmetric keys
   *
   * @throws ExecutionException when there is an error fetching the public key.
   * @throws InterruptedException when there is an error fetching the public key.
   * @throws NoSuchPaddingException when there is an error during encryption.
   * @throws IllegalBlockSizeException when there is an error during encryption.
   * @throws NoSuchAlgorithmException when there is an error during encryption.
   * @throws BadPaddingException when there is an error during encryption.
   * @throws InvalidKeyException when there is an error during encryption.
   */
  private void sendAsymmetricKeyAuthType()
      throws ExecutionException, InterruptedException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    handshakeChallenges = new HandshakeChallenge[1];
    CipherUtil cipherUtil = playerDatabase.getPublicKey(player, playerPublicKeyMD5).get();
    String password = new PasswordGenerator().getPassword();
    handshakeChallenges[1] = HandshakeChallenge.createChallenge(player.getName(),
        password, cipherUtil.getKey());

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.ASYMMETRIC_KEY)
            .addChallenge(ByteString.copyFrom(handshakeChallenges[0].getChallenge()))
            .build();
    connection.sendMessage(authTypeMsg.toByteArray());
  }

  /**
   * Adds an observer to the handshake process.
   * @param observer the observer of the handshake process.
   */
  public void addObserver(ServerHandshakeObserver observer) {
    observerList.add(observer);
  }

  /**
   * Removes an observer from the handshake process.
   * @param observer the observer of the handshake process.
   */
  public void removeObserver(ServerHandshakeObserver observer) {
    observerList.remove(observer);
  }

  /**
   * Notifies observers that the handshake has completed or errored out..
   */
  private void notifyObservers() {
    for (var observer : observerList) observer.onCompleted(this);
  }

  /**
   * Entry point for server side of the handshake.
   */
  public void startHandshake() {
    currentState = State.AwaitingClientInit;
  }


  public interface ServerHandshakeObserver {
    void onCompleted(ServerHandshake handshake);
  }

  /**
   * The states that the server side of the server side of the handshake process can be in.
   */
  private enum State {
    Error,
    Success,
    AwaitingClientInit,
    AwaitingClientAuth
  }

}
