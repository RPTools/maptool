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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.server.proto.AuthTypeEnum;
import net.rptools.maptool.server.proto.ClientAuthMsg;
import net.rptools.maptool.server.proto.ClientInitMsg;
import net.rptools.maptool.server.proto.ConnectionSuccessfulMsg;
import net.rptools.maptool.server.proto.HandshakeMsg;
import net.rptools.maptool.server.proto.HandshakeMsg.MessageTypeCase;
import net.rptools.maptool.server.proto.HandshakeResponseCodeMsg;
import net.rptools.maptool.server.proto.RoleDto;
import net.rptools.maptool.server.proto.UseAuthTypeMsg;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.CipherUtil.Key;
import net.rptools.maptool.util.cipher.PublicPrivateKeyStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class that implements the client side of the handshake. */
public class ClientHandshake implements Handshake, MessageHandler {

  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(ClientHandshake.class);

  /** The index in the array for the GM handshake challenge, only used for role based auth */
  private static final int GM_CHALLENGE = 0;
  /** The index in the array for the Player handshake challenge, only used for role based auth */
  private static final int PLAYER_CHALLENGE = 1;

  /** The connection for the handshake. */
  private final ClientConnection connection;
  /** The player for the client. */
  private final LocalPlayer player;
  /** Observers that want to be notified when the status changes. */
  private final List<HandshakeObserver> observerList = new CopyOnWriteArrayList<>();
  /** Message for any error that has occurred, {@code null} if no error has occurred. */
  private String errorMessage;

  /**
   * Any exception that occurred that causes an error, {@code null} if no exception which causes an
   * error has occurred.
   */
  private Exception exception;

  /** The current state of the handshake process. */
  private State currentState = State.AwaitingUseAuthType;

  public ClientHandshake(ClientConnection connection, LocalPlayer player) {
    this.connection = connection;
    this.player = player;
  }

  @Override
  public void startHandshake() throws ExecutionException, InterruptedException {
    var md5key =
        CipherUtil.publicKeyMD5(new PublicPrivateKeyStore().getKeys().get().getKey().publicKey());
    var clientInitMsg =
        ClientInitMsg.newBuilder()
            .setPlayerName(player.getName())
            .setVersion(MapTool.getVersion())
            .setPublicKeyMd5(md5key.toString());
    var handshakeMsg = HandshakeMsg.newBuilder().setClientInitMsg(clientInitMsg).build();

    sendMessage(handshakeMsg);
    currentState = State.AwaitingUseAuthType;
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
        } else if (code.equals(HandshakeResponseCodeMsg.PLAYER_ALREADY_CONNECTED)) {
          errorMessage = I18N.getText("Handshake.msg.playerAlreadyConnected");
        } else if (code.equals(HandshakeResponseCodeMsg.WRONG_VERSION)) {
          errorMessage = I18N.getText("Handshake.msg.wrongVersion");
        } else {
          errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
        }
        notifyObservers();
        return;
      }

      switch (currentState) {
        case AwaitingUseAuthType:
          if (msgType == MessageTypeCase.USE_AUTH_TYPE_MSG) {
            handle(handshakeMsg.getUseAuthTypeMsg());
          } else if (msgType == MessageTypeCase.PLAYER_BLOCKED_MSG) {
            errorMessage =
                I18N.getText(
                    "Handshake.msg.playerBlocked", handshakeMsg.getPlayerBlockedMsg().getReason());
            notifyObservers();
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
            currentState = State.Error;
            notifyObservers();
          }
          break;
        case AwaitingConnectionSuccessful:
          if (msgType == MessageTypeCase.CONNECTION_SUCCESSFUL_MSG) {
            handle(handshakeMsg.getConnectionSuccessfulMsg());
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
            currentState = State.Error;
            notifyObservers();
          }
          break;
      }

    } catch (Exception e) {
      log.warn(e.toString());
      exception = e;
      currentState = State.Error;
      errorMessage = I18N.getText("Handshake.msg.incorrectPassword");
      notifyObservers();
    }
  }

  private void handle(UseAuthTypeMsg useAuthTypeMsg)
      throws ExecutionException, InterruptedException, IllegalBlockSizeException,
          BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException,
          InvalidKeyException, InvalidKeySpecException {

    HandshakeChallenge handshakeChallenge = null;

    if (useAuthTypeMsg.getAuthType() == AuthTypeEnum.ASYMMETRIC_KEY) {
      CipherUtil cipherUtil = new PublicPrivateKeyStore().getKeys().get();
      handshakeChallenge =
          HandshakeChallenge.fromChallengeBytes(
              player.getName(), useAuthTypeMsg.getChallenge(0).toByteArray(), cipherUtil.getKey());
    } else {
      player.setPasswordSalt(useAuthTypeMsg.getSalt().toByteArray());
      for (int i = 0; i < useAuthTypeMsg.getChallengeCount(); i++) {
        try {
          Key key = player.getPassword();
          // Key key = playerDatabase.getPlayerPassword(player.getName()).get();
          handshakeChallenge =
              HandshakeChallenge.fromChallengeBytes(
                  player.getName(), useAuthTypeMsg.getChallenge(i).toByteArray(), key);
          break;
        } catch (Exception e) {
          // ignore exception unless is the last one
          if (i == useAuthTypeMsg.getChallengeCount() - 1) {
            throw e;
          }
        }
      }
    }

    var clientAuthMsg =
        ClientAuthMsg.newBuilder()
            .setChallengeResponse(ByteString.copyFrom(handshakeChallenge.getExpectedResponse()));

    var handshakeMsg = HandshakeMsg.newBuilder().setClientAuthMessage(clientAuthMsg).build();
    sendMessage(handshakeMsg);
    currentState = State.AwaitingConnectionSuccessful;
  }

  private void handle(ConnectionSuccessfulMsg connectionSuccessfulMsg)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var policy = Mapper.map(connectionSuccessfulMsg.getServerPolicyDto());
    MapTool.setServerPolicy(policy);
    player.setRole(connectionSuccessfulMsg.getRoleDto() == RoleDto.GM ? Role.GM : Role.PLAYER);
    currentState = State.Success;
    notifyObservers();
  }

  @Override
  public void addObserver(HandshakeObserver observer) {
    observerList.add(observer);
  }

  @Override
  public void removeObserver(HandshakeObserver observer) {
    observerList.remove(observer);
  }

  /** Notifies observers that the handshake has completed or errored out.. */
  private void notifyObservers() {
    for (var observer : observerList) observer.onCompleted(this);
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

  /** The states that the server side of the server side of the handshake process can be in. */
  private enum State {
    Error,
    Success,
    AwaitingUseAuthType,
    AwaitingConnectionSuccessful
  }
}
