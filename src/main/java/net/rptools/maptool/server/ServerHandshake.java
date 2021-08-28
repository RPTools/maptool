package net.rptools.maptool.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.server.Handshake.State;
import net.rptools.maptool.server.proto.ClientInitMsg;
import net.rptools.maptool.server.proto.HandshakeMsg;
import net.rptools.maptool.server.proto.HandshakeResponseMsg;
import net.rptools.maptool.server.proto.ResponseCodeDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerHandshake implements MessageHandler {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  private final PlayerDatabase playerDatabase;
  private final ClientConnection connection;

  private final List<ServerHandshakeObserver> observerList = new CopyOnWriteArrayList<>();

  private String errorMessage;
  private Exception exception;
  private Player player;


  private State currentState = State.AwaitingInitialMacSalt;

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



  private void sendErrorResponseAndNotify(ResponseCodeDto errorCode) {
    var responseMsg =
        HandshakeResponseMsg.newBuilder()
            .setCode(errorCode)
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
      var msgType = handshakeMsg.getMessageTypeCase();

      switch (currentState) {
        case AwaitingClientInit:
          if (msgType == HandshakeMsg.MessageTypeCase.CLIENT_INIT_MSG) {
            handle(handshakeMsg.getClientInitMsg());
          } else {
            errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
          }
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

  /**
   * This method handles the initial message that the client sends as part of the handshake process.
   * @param clientInitMsg The initial message sent from the client.
   */
  private void handle(ClientInitMsg clientInitMsg) {
    var server = MapTool.getServer();
    if (server.isPlayerConnected(clientInitMsg.getPlayerName())) {
      errorMessage = I18N.getText("Handshake.msg.duplicateName");
      sendErrorResponseAndNotify(ResponseCodeDto.PLAYER_CONNECTED);
      return;
    }

    if (!MapTool.isDevelopment() && MapTool.getVersion().equals(clientInitMsg.getVersion())) {
      errorMessage = I18N.getText("Handshake.msg.wrongVersion");
      sendErrorResponseAndNotify(ResponseCodeDto.WRONG_VERSION);
    }

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
   * The states that the server side of the handshake process can be in.
   */
  private enum State {
    Error,
    AwaitingClientInit,
    AwaitingInitialMacSalt,
    AwaitingRequest,
    AwaitingChallenge,
    AwaitingChallengeResponse,
    Success,
    AwaitingResponse
  }


}
