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
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.library.LibraryManager;
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
import net.rptools.maptool.server.proto.PlayerBlockedMsg;
import net.rptools.maptool.server.proto.RoleDto;
import net.rptools.maptool.server.proto.UseAuthTypeMsg;
import net.rptools.maptool.util.PasswordGenerator;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.CipherUtil.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class used to handle the server side part of the connection handshake. */
public class ServerHandshake implements Handshake, MessageHandler {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(ServerHandshake.class);

  /** The database used for retrieving players. */
  private final PlayerDatabase playerDatabase;

  /** The connection to the client. */
  private final ClientConnection connection;

  /** Observers that want to be notified when the status changes. */
  private final List<HandshakeObserver> observerList = new CopyOnWriteArrayList<>();

  /** The index in the array for the GM handshake challenge, only used for role based auth */
  private static final int GM_CHALLENGE = 0;
  /** The index in the array for the Player handshake challenge, only used for role based auth */
  private static final int PLAYER_CHALLENGE = 1;

  /** Message for any error that has occurred, {@code null} if no error has occurred. */
  private String errorMessage;

  /**
   * Any exception that occurred that causes an error, {@code null} if no exception which causes an
   * error has occurred.
   */
  private Exception exception;

  /** The player that this connection is for. */
  private Player player;

  /** The current state of the handshake process. */
  private State currentState = State.AwaitingClientInit;

  /** Challenges sent to the client. */
  private HandshakeChallenge[] handshakeChallenges;

  private MD5Key playerPublicKeyMD5;

  /**
   * Creates a new {@code ServerHandshake} instance.
   *
   * @param connection The client connection for the handshake.
   * @param playerDatabase The database of players.
   */
  public ServerHandshake(ClientConnection connection, PlayerDatabase playerDatabase) {
    this.connection = connection;
    this.playerDatabase = playerDatabase;
  }

  @Override
  public boolean isSuccessful() {
    return currentState == State.Success;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public ClientConnection getConnection() {
    return connection;
  }

  @Override
  public Exception getException() {
    return exception;
  }

  @Override
  public Player getPlayer() {
    return player;
  }

  /**
   * Sends an error response to the client and notifies any observers of the handshake that the
   * status has changed.
   *
   * @param errorCode The error code that should be sent to the client.
   */
  private void sendErrorResponseAndNotify(HandshakeResponseCodeMsg errorCode) {
    var msg = HandshakeMsg.newBuilder().setHandshakeResponseCodeMsg(errorCode).build();
    sendMessage(msg);
    currentState = State.PlayerBlocked;
    // Do not notify users as it will disconnect and client won't get message instead wait
    // for client to disconnect after getting this message, if they don't then it will fail
    // with invalid handshake.
  }

  private void sendMessage(HandshakeMsg message) {
    var msgType = message.getMessageTypeCase();
    log.info(connection.getId() + " :send: " + msgType);
    connection.sendMessage(message.toByteArray());
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var handshakeMsg = HandshakeMsg.parseFrom(message);
      var msgType = handshakeMsg.getMessageTypeCase();

      log.info(id + " :got: " + msgType);

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
        case PlayerBlocked:
          errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
          sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_HANDSHAKE);
          break;
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
          break;
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
      throws NoSuchAlgorithmException, InvalidKeySpecException, ExecutionException,
          InterruptedException {
    byte[] response = clientAuthMessage.getChallengeResponse().toByteArray();
    if (handshakeChallenges.length > 1) {
      if (Arrays.compare(response, handshakeChallenges[GM_CHALLENGE].getExpectedResponse()) == 0) {
        player = playerDatabase.getPlayerWithRole(player.getName(), Role.GM);
      } else if (Arrays.compare(
              response, handshakeChallenges[PLAYER_CHALLENGE].getExpectedResponse())
          == 0) {
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

  private void sendConnectionSuccessful() throws ExecutionException, InterruptedException {
    var server = MapTool.getServer();
    var policy = Mapper.map(server.getPolicy());
    var connectionSuccessfulMsg =
        ConnectionSuccessfulMsg.newBuilder()
            .setRoleDto(player.isGM() ? RoleDto.GM : RoleDto.PLAYER)
            .setServerPolicyDto(policy)
            .setGameDataDto(new DataStoreManager().toDto().get())
            .setAddOnLibraryListDto(new LibraryManager().addOnLibrariesToDto().get());
    var handshakeMsg =
        HandshakeMsg.newBuilder().setConnectionSuccessfulMsg(connectionSuccessfulMsg).build();
    sendMessage(handshakeMsg);
    currentState = State.Success;
    notifyObservers();
  }

  /**
   * This method handles the initial message that the client sends as part of the handshake process.
   *
   * @param clientInitMsg The initial message sent from the client.
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

    if (!MapTool.isDevelopment() && !MapTool.getVersion().equals(clientInitMsg.getVersion())) {
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

    if (playerDatabase.isBlocked(player)) {
      var blockedMsg =
          PlayerBlockedMsg.newBuilder().setReason(playerDatabase.getBlockedReason(player)).build();
      var msg = HandshakeMsg.newBuilder().setPlayerBlockedMsg(blockedMsg).build();
      sendMessage(msg);
      currentState = State.Error;
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
   *
   * @throws NoSuchPaddingException when there is an error during encryption.
   * @throws IllegalBlockSizeException when there is an error during encryption.
   * @throws NoSuchAlgorithmException when there is an error during encryption.
   * @throws BadPaddingException when there is an error during encryption.
   * @throws InvalidKeyException when there is an error during encryption.
   */
  private void sendSharedPasswordAuthType()
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
          BadPaddingException, InvalidKeyException {
    byte[] playerPasswordSalt = playerDatabase.getPlayerPasswordSalt(player.getName());
    String password = new PasswordGenerator().getPassword();
    handshakeChallenges = new HandshakeChallenge[1];
    Key key = playerDatabase.getPlayerPassword(player.getName()).get();
    handshakeChallenges[0] = HandshakeChallenge.createChallenge(player.getName(), password, key);

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.SHARED_PASSWORD)
            .setSalt(ByteString.copyFrom(playerPasswordSalt))
            .addChallenge(ByteString.copyFrom(handshakeChallenges[0].getChallenge()));
    var handshakeMsg = HandshakeMsg.newBuilder().setUseAuthTypeMsg(authTypeMsg).build();
    sendMessage(handshakeMsg);
  }

  /**
   * Send the authentication type message when using role based shared passwords.
   *
   * @throws NoSuchPaddingException when there is an error during encryption.
   * @throws IllegalBlockSizeException when there is an error during encryption.
   * @throws NoSuchAlgorithmException when there is an error during encryption.
   * @throws BadPaddingException when there is an error during encryption.
   * @throws InvalidKeyException when there is an error during encryption.
   */
  private void sendRoleSharedPasswordAuthType()
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
          BadPaddingException, InvalidKeyException {
    byte[] playerPasswordSalt = playerDatabase.getPlayerPasswordSalt(player.getName());
    String[] password = new String[2];
    password[GM_CHALLENGE] = new PasswordGenerator().getPassword();
    password[PLAYER_CHALLENGE] = new PasswordGenerator().getPassword();

    handshakeChallenges[GM_CHALLENGE] =
        HandshakeChallenge.createChallenge(
            player.getName(),
            password[GM_CHALLENGE],
            playerDatabase.getRolePassword(Role.GM).get());
    handshakeChallenges[PLAYER_CHALLENGE] =
        HandshakeChallenge.createChallenge(
            player.getName(),
            password[GM_CHALLENGE],
            playerDatabase.getRolePassword(Role.PLAYER).get());

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.SHARED_PASSWORD)
            .setSalt(ByteString.copyFrom(playerPasswordSalt))
            .addChallenge(ByteString.copyFrom(handshakeChallenges[GM_CHALLENGE].getChallenge()))
            .addChallenge(
                ByteString.copyFrom(handshakeChallenges[PLAYER_CHALLENGE].getChallenge()));
    var handshakeMsg = HandshakeMsg.newBuilder().setUseAuthTypeMsg(authTypeMsg).build();
    sendMessage(handshakeMsg);
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
      throws ExecutionException, InterruptedException, NoSuchPaddingException,
          IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
          InvalidKeyException {
    handshakeChallenges = new HandshakeChallenge[1];
    CipherUtil cipherUtil = playerDatabase.getPublicKey(player, playerPublicKeyMD5).get();
    String password = new PasswordGenerator().getPassword();
    handshakeChallenges[0] =
        HandshakeChallenge.createChallenge(player.getName(), password, cipherUtil.getKey());

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.ASYMMETRIC_KEY)
            .addChallenge(ByteString.copyFrom(handshakeChallenges[0].getChallenge()));
    var handshakeMsg = HandshakeMsg.newBuilder().setUseAuthTypeMsg(authTypeMsg).build();
    sendMessage(handshakeMsg);
  }

  /**
   * Adds an observer to the handshake process.
   *
   * @param observer the observer of the handshake process.
   */
  public void addObserver(HandshakeObserver observer) {
    observerList.add(observer);
  }

  /**
   * Removes an observer from the handshake process.
   *
   * @param observer the observer of the handshake process.
   */
  public void removeObserver(HandshakeObserver observer) {
    observerList.remove(observer);
  }

  /** Notifies observers that the handshake has completed or errored out.. */
  private void notifyObservers() {
    for (var observer : observerList) observer.onCompleted(this);
  }

  @Override
  public void startHandshake() {
    currentState = State.AwaitingClientInit;
  }

  /** The states that the server side of the server side of the handshake process can be in. */
  private enum State {
    Error,
    Success,
    AwaitingClientInit,
    AwaitingClientAuth,
    PlayerBlocked
  }
}
