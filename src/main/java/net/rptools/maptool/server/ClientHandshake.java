package net.rptools.maptool.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.server.proto.ClientInitMsg;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.PublicPrivateKeyStore;

public class ClientHandshake  implements MessageHandler {


  private final ClientConnection connection;
  private final LocalPlayer player;
  private final PlayerDatabase playerDatabase;

  /** Observers that want to be notified when the status changes. */
  private final List<ClientHandshakeObserver> observerList = new CopyOnWriteArrayList<>();

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
   * The current state of the handshake process.
   */
  private State currentState = State.AwaitingClientInit;



  public ClientHandshake(ClientConnection connection, PlayerDatabase playerDatabase,
      LocalPlayer player) {
    this.connection = connection;
    this.player = player;
    this.playerDatabase = playerDatabase;
  }


  /**
   * Entry point for server side of the handshake.
   */
  public void startHandshake()  throws ExecutionException, InterruptedException {
    var md5key = CipherUtil.publicKeyMD5(
        new PublicPrivateKeyStore().getKeys().get().getKey().publicKey());
    var clientInitMsg = ClientInitMsg.newBuilder()
        .setPlayerName(player.getName())
        .setVersion(MapTool.getVersion())
        .setPublicKeyMd5(md5key.toString())
        .build();
    connection.sendMessage(clientInitMsg.toByteArray());
    currentState = State.AwaitingUseAuthType;
  }

  @Override
  public void handleMessage(String id, byte[] message) {

  }


  /**
   * Adds an observer to the handshake process.
   * @param observer the observer of the handshake process.
   */
  public void addObserver(ClientHandshakeObserver observer) {
    observerList.add(observer);
  }

  /**
   * Removes an observer from the handshake process.
   * @param observer the observer of the handshake process.
   */
  public void removeObserver(ClientHandshakeObserver observer) {
    observerList.remove(observer);
  }

  /**
   * Notifies observers that the handshake has completed or errored out..
   */
  private void notifyObservers() {
    for (var observer : observerList) observer.onCompleted(this);
  }



  public interface ClientHandshakeObserver {
    void onCompleted(ClientHandshake handshake);
  }


  /**
   * The states that the server side of the server side of the handshake process can be in.
   */
  private enum State {
    Error,
    Success,
    AwaitingUseAuthType,
    AwaitingConnectionSuccessful
  }
}
