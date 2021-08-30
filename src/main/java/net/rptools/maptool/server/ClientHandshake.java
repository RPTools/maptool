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
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase;
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

public class ClientHandshake implements MessageHandler {

  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(ClientHandshake.class);

  /** The index in the array for the GM handshake challenge, only used for role based auth */
  private static final int GM_CHALLENGE = 0;
  /** The index in the array for the Player handshake challenge, only used for role based auth */
  private static final int PLAYER_CHALLENGE = 1;
  private final ClientConnection connection;
  private LocalPlayer player;
  private final PlayerDatabase playerDatabase;
  /** Observers that want to be notified when the status changes. */
  private final List<ClientHandshakeObserver> observerList = new CopyOnWriteArrayList<>();
  /** Message for any error that has occurred, {@code null} if no error has occurred. */
  private String errorMessage;

  /**
   * Any exception that occurred that causes an error, {@code null} if no exception which causes an
   * error has occurred.
   */
  private Exception exception;

  /** The current state of the handshake process. */
  private State currentState = State.AwaitingUseAuthType;

  public ClientHandshake(
      ClientConnection connection, PlayerDatabase playerDatabase, LocalPlayer player) {
    this.connection = connection;
    this.player = player;
    this.playerDatabase = playerDatabase;
  }

  /** Entry point for server side of the handshake. */
  public void startHandshake() throws ExecutionException, InterruptedException {
    var md5key =
        CipherUtil.publicKeyMD5(new PublicPrivateKeyStore().getKeys().get().getKey().publicKey());
    var clientInitMsg =
        ClientInitMsg.newBuilder()
            .setPlayerName(player.getName())
            .setVersion(MapTool.getVersion())
            .setPublicKeyMd5(md5key.toString())
            .build();
    connection.sendMessage(clientInitMsg.toByteArray());
    currentState = State.AwaitingUseAuthType;
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

      switch(currentState) {
        case AwaitingUseAuthType:
          if (msgType == MessageTypeCase.USE_AUTH_TYPE_MSG) {
            handle(handshakeMsg.getUseAuthTypeMsg());
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
            currentState = State.Error;
            notifyObservers();
          }
        case AwaitingConnectionSuccessful:
          if (msgType == MessageTypeCase.CONNECTION_SUCCESSFUL_MSG) {
            handle(handshakeMsg.getConnectionSuccessfulMsg());
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
            currentState = State.Error;
            notifyObservers();
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

  private void handle(UseAuthTypeMsg useAuthTypeMsg)
      throws ExecutionException, InterruptedException, IllegalBlockSizeException,
   BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

    HandshakeChallenge handshakeChallenge = null;

    if (useAuthTypeMsg.getAuthType() == AuthTypeEnum.ASYMMETRIC_KEY) {
      CipherUtil cipherUtil = new PublicPrivateKeyStore().getKeys().get();
      handshakeChallenge = HandshakeChallenge.fromChallengeBytes(player.getName(),
          useAuthTypeMsg.getChallenge(0).toByteArray(), cipherUtil.getKey());
    } else {
      for (int i = 0; i < useAuthTypeMsg.getChallengeCount(); i++) {
        try {
          Key key = playerDatabase.getPlayerPassword(player.getName()).get();
          handshakeChallenge = HandshakeChallenge.fromChallengeBytes(player.getName(),
              useAuthTypeMsg.toByteArray(), key);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
          // ignore exception unless is the last one
          if (i == useAuthTypeMsg.getChallengeCount() - 1) {
            throw e;
          }
        }
      }
    }

    var clientAuthMsg = ClientAuthMsg.newBuilder()
        .setChallengeResponse(ByteString.copyFrom(handshakeChallenge.getExpectedResponse()))
        .build();

    connection.sendMessage(clientAuthMsg.toByteArray());
    currentState = State.AwaitingConnectionSuccessful;
  }


  private void handle(ConnectionSuccessfulMsg connectionSuccessfulMsg)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var policy = Mapper.map(connectionSuccessfulMsg.getServerPolicyDto());
    MapTool.setServerPolicy(policy);
    player = (LocalPlayer) playerDatabase.getPlayerWithRole(
        player.getName(),
        connectionSuccessfulMsg.getRoleDto() == RoleDto.GM ? Role.GM : Role.PLAYER
    );
    currentState = State.Success;
    notifyObservers();
  }


  /**
   * Adds an observer to the handshake process.
   *
   * @param observer the observer of the handshake process.
   */
  public void addObserver(ClientHandshakeObserver observer) {
    observerList.add(observer);
  }

  /**
   * Removes an observer from the handshake process.
   *
   * @param observer the observer of the handshake process.
   */
  public void removeObserver(ClientHandshakeObserver observer) {
    observerList.remove(observer);
  }

  /** Notifies observers that the handshake has completed or errored out.. */
  private void notifyObservers() {
    for (var observer : observerList) observer.onCompleted(this);
  }

  /** The states that the server side of the server side of the handshake process can be in. */
  private enum State {
    Error,
    Success,
    AwaitingUseAuthType,
    AwaitingConnectionSuccessful
  }

  public interface ClientHandshakeObserver {
    void onCompleted(ClientHandshake handshake);
  }
}
