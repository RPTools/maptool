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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.campaign.CampaignManager;
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.gamedata.GameDataImporter;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.model.player.LocalPlayerDatabase;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabaseFactory;
import net.rptools.maptool.model.player.PlayerDatabaseFactory.PlayerDatabaseType;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.server.proto.HandshakeMsg.MessageTypeCase;
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
  private final Connection connection;

  /** The player for the client. */
  private final LocalPlayer player;

  /** Observers that want to be notified when the status changes. */
  private final List<HandshakeObserver> observerList = new CopyOnWriteArrayList<>();

  /** Message for any error that has occurred, {@code null} if no error has occurred. */
  private String errorMessage;

  /** PIN for sending public key to client */
  private String pin;

  private JDialog easyConnectDialog;

  private WindowListener easyConnectWindowListener;

  /**
   * Any exception that occurred that causes an error, {@code null} if no exception which causes an
   * error has occurred.
   */
  private Exception exception;

  /** The current state of the handshake process. */
  private State currentState = State.AwaitingUseAuthType;

  public ClientHandshake(Connection connection, LocalPlayer player) {
    this.connection = connection;
    this.player = player;
  }

  private synchronized JDialog getEasyConnectDialog() {
    return easyConnectDialog;
  }

  private synchronized void setEasyConnectDialog(JDialog easyConnectDialog) {
    this.easyConnectDialog = easyConnectDialog;
  }

  private synchronized WindowListener getEasyConnectWindowListener() {
    return easyConnectWindowListener;
  }

  private synchronized void setEasyConnectWindowListener(WindowListener easyConnectWindowListener) {
    this.easyConnectWindowListener = easyConnectWindowListener;
  }

  @Override
  public void startHandshake() throws ExecutionException, InterruptedException {
    var md5key = CipherUtil.publicKeyMD5(new PublicPrivateKeyStore().getKeys().get().publicKey());
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
    log.debug(connection.getId() + " sent: " + msgType);
    connection.sendMessage(message.toByteArray());
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var handshakeMsg = HandshakeMsg.parseFrom(message);
      var msgType = handshakeMsg.getMessageTypeCase();

      log.debug(id + " got: " + msgType);

      if (msgType == MessageTypeCase.HANDSHAKE_RESPONSE_CODE_MSG) {
        HandshakeResponseCodeMsg code = handshakeMsg.getHandshakeResponseCodeMsg();
        if (code.equals(HandshakeResponseCodeMsg.INVALID_PASSWORD)) {
          errorMessage = I18N.getText("Handshake.msg.incorrectPassword");
        } else if (code.equals(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY)) {
          errorMessage = I18N.getText("Handshake.msg.incorrectPublicKey");
        } else if (code.equals(HandshakeResponseCodeMsg.SERVER_DENIED)) {
          errorMessage = I18N.getText("Handshake.msg.deniedEasyConnect");
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
          if (msgType == MessageTypeCase.REQUEST_PUBLIC_KEY_MSG) {
            handle(handshakeMsg.getRequestPublicKeyMsg());
          } else if (msgType == MessageTypeCase.USE_AUTH_TYPE_MSG) {
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
        case AwaitingPublicKeyAddition:
          if (msgType == MessageTypeCase.PUBLIC_KEY_ADDED_MSG) {
            handle(handshakeMsg.getPublicKeyAddedMsg());
          } else {
            errorMessage = I18N.getText("Handshake.msg.gmDeniedRequest");
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

  private void handle(PublicKeyAddedMsg publicKeyAddedMsg)
      throws ExecutionException, InterruptedException {
    SwingUtilities.invokeLater(this::closeEasyConnectDialog);
    startHandshake();
  }

  private void handle(RequestPublicKeyMsg requestPublicKeyMsg) {
    pin = requestPublicKeyMsg.getPin();
    var publicKey = new PublicPrivateKeyStore().getKeys().join();
    var publicKeyUploadBuilder = PublicKeyUploadMsg.newBuilder();
    publicKeyUploadBuilder.setPublicKey(publicKey.getEncodedPublicKeyText());
    sendMessage(HandshakeMsg.newBuilder().setPublicKeyUploadMsg(publicKeyUploadBuilder).build());
    currentState = State.AwaitingPublicKeyAddition;
    SwingUtilities.invokeLater(
        () -> {
          JOptionPane pane = new JOptionPane();
          pane.setOptions(new Object[] {}); // No buttons
          pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
          pane.setMessage(I18N.getText("pendingConnection.sendPublicKey", pin));
          var dialog =
              pane.createDialog(
                  MapTool.getFrame(), I18N.getText("pendingConnection.sendPublicKey.title"));
          dialog.setModal(false);
          dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
          dialog.setVisible(true);
          var windowListener =
              new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                  var msg =
                      HandshakeMsg.newBuilder()
                          .setHandshakeResponseCodeMsg(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY)
                          .build();
                  sendMessage(msg);
                }
              };
          dialog.addWindowListener(windowListener);
          setEasyConnectDialog(dialog);
          setEasyConnectWindowListener(windowListener);
        });
  }

  private void handle(UseAuthTypeMsg useAuthTypeMsg)
      throws ExecutionException,
          InterruptedException,
          IllegalBlockSizeException,
          BadPaddingException,
          NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidKeySpecException,
          InvalidAlgorithmParameterException {

    var clientAuthMsg = ClientAuthMsg.newBuilder();

    if (useAuthTypeMsg.getAuthType() == AuthTypeEnum.ASYMMETRIC_KEY) {
      CipherUtil.Key publicKey = new PublicPrivateKeyStore().getKeys().get();
      var handshakeChallenge =
          HandshakeChallenge.fromAsymmetricChallengeBytes(
              player.getName(), useAuthTypeMsg.getChallenge(0).toByteArray(), publicKey);
      var expectedResponse = handshakeChallenge.getExpectedResponse();
      clientAuthMsg = clientAuthMsg.setChallengeResponse(ByteString.copyFrom(expectedResponse));
    } else {
      SecureRandom rnd = new SecureRandom();
      byte[] responseIv = new byte[CipherUtil.CIPHER_BLOCK_SIZE];
      rnd.nextBytes(responseIv);

      player.setPasswordSalt(useAuthTypeMsg.getSalt().toByteArray());
      var iv = useAuthTypeMsg.getIv().toByteArray();
      for (int i = 0; i < useAuthTypeMsg.getChallengeCount(); i++) {
        try {
          Key key = player.getPassword();
          // Key key = playerDatabase.getPlayerPassword(player.getName()).get();
          var handshakeChallenge =
              HandshakeChallenge.fromSymmetricChallengeBytes(
                  player.getName(), useAuthTypeMsg.getChallenge(i).toByteArray(), key, iv);
          var expectedResponse = handshakeChallenge.getExpectedResponse(responseIv);
          clientAuthMsg =
              clientAuthMsg
                  .setChallengeResponse(ByteString.copyFrom(expectedResponse))
                  .setIv(ByteString.copyFrom(responseIv));
          break;
        } catch (Exception e) {
          // ignore exception unless is the last one
          if (i == useAuthTypeMsg.getChallengeCount() - 1) {
            throw e;
          }
        }
      }
    }

    var handshakeMsg = HandshakeMsg.newBuilder().setClientAuthMessage(clientAuthMsg).build();
    sendMessage(handshakeMsg);
    currentState = State.AwaitingConnectionSuccessful;
  }

  private void handle(ConnectionSuccessfulMsg connectionSuccessfulMsg) throws IOException {
    var policy = ServerPolicy.fromDto(connectionSuccessfulMsg.getServerPolicyDto());
    MapTool.setServerPolicy(policy);
    player.setRole(connectionSuccessfulMsg.getRoleDto() == RoleDto.GM ? Role.GM : Role.PLAYER);
    MapTool.getFrame()
        .getToolbarPanel()
        .getMapselect()
        .setVisible((!policy.getMapSelectUIHidden()) || MapTool.getPlayer().isGM());
    if ((!policy.getDisablePlayerAssetPanel()) || MapTool.getPlayer().isGM()) {
      MapTool.getFrame().getAssetPanel().enableAssets();
    } else {
      MapTool.getFrame().getAssetPanel().disableAssets();
    }
    if (!MapTool.isHostingServer()) {
      PlayerDatabaseFactory.setCurrentPlayerDatabase(PlayerDatabaseType.LOCAL_PLAYER);
      var playerDb = (LocalPlayerDatabase) PlayerDatabaseFactory.getCurrentPlayerDatabase();
      playerDb.setLocalPlayer(player);
      if (!MapTool.isPersonalServer()) {
        new CampaignManager().clearCampaignData();
        if (connectionSuccessfulMsg.hasGameDataDto()) {
          var dataStore = new DataStoreManager().getDefaultDataStoreForRemoteUpdate();
          try {
            new GameDataImporter(dataStore).importData(connectionSuccessfulMsg.getGameDataDto());
          } catch (ExecutionException | InterruptedException e) {
            log.error(I18N.getText("data.error.importGameData"), e);
            throw new IOException(e.getCause());
          }
        }
        if (!policy.isUseIndividualViews()) {
          MapTool.getFrame().getToolbarPanel().setTokenSelectionGroupEnabled(false);
          log.info("No individual views, disabling FoW buttons");
        }
        var libraryManager = new LibraryManager();
        for (var library : connectionSuccessfulMsg.getAddOnLibraryListDto().getLibrariesList()) {
          var md5key = new MD5Key(library.getMd5Hash());
          AssetManager.getAssetAsynchronously(
              md5key,
              a -> {
                Asset asset = AssetManager.getAsset(a);
                try {
                  var addOnLibrary = new AddOnLibraryImporter().importFromAsset(asset);
                  libraryManager.reregisterAddOnLibrary(addOnLibrary);
                } catch (IOException e) {
                  SwingUtilities.invokeLater(
                      () -> {
                        MapTool.showError(
                            I18N.getText(
                                "library.import.error", library.getDetails().getNamespace()),
                            e);
                      });
                }
              });
        }
      }
    }
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
    SwingUtilities.invokeLater(this::closeEasyConnectDialog);
    for (var observer : observerList) {
      observer.onCompleted(this);
    }
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
  public Connection getConnection() {
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

  private void closeEasyConnectDialog() {
    var dialog = getEasyConnectDialog();
    if (dialog != null) {
      dialog.removeWindowListener(getEasyConnectWindowListener());
      dialog.setVisible(false);
      dialog.dispose();
      setEasyConnectDialog(null);
    }
  }

  /** The states that the server side of the server side of the handshake process can be in. */
  private enum State {
    Error,
    Success,
    AwaitingUseAuthType,
    AwaitingConnectionSuccessful,
    AwaitingPublicKeyAddition
  }
}
