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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.SwingUtilities;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.player.PasswordDatabaseException;
import net.rptools.maptool.model.player.PersistedPlayerDatabase;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerAwaitingApproval;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.model.player.ServerSidePlayerDatabase;
import net.rptools.maptool.server.proto.AuthTypeEnum;
import net.rptools.maptool.server.proto.ClientAuthMsg;
import net.rptools.maptool.server.proto.ClientInitMsg;
import net.rptools.maptool.server.proto.ConnectionSuccessfulMsg;
import net.rptools.maptool.server.proto.HandshakeMsg;
import net.rptools.maptool.server.proto.HandshakeMsg.MessageTypeCase;
import net.rptools.maptool.server.proto.HandshakeResponseCodeMsg;
import net.rptools.maptool.server.proto.PlayerBlockedMsg;
import net.rptools.maptool.server.proto.PublicKeyAddedMsg;
import net.rptools.maptool.server.proto.PublicKeyUploadMsg;
import net.rptools.maptool.server.proto.RequestPublicKeyMsg;
import net.rptools.maptool.server.proto.RoleDto;
import net.rptools.maptool.server.proto.UseAuthTypeMsg;
import net.rptools.maptool.util.PasswordGenerator;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.CipherUtil.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class used to handle the server side part of the connection handshake. */
public class ServerHandshake implements Handshake<Player>, MessageHandler {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(ServerHandshake.class);

  private final CompletableFuture<Player> future = new CompletableFuture<>();
  private CompletionStage<Player> stage = future;

  /** The database used for retrieving players. */
  private final ServerSidePlayerDatabase playerDatabase;

  private final MapToolServer server;

  /** The connection to the client. */
  private final Connection connection;

  /** The index in the array for the GM handshake challenge, only used for role based auth */
  private static final int GM_CHALLENGE = 0;

  /** The index in the array for the Player handshake challenge, only used for role based auth */
  private static final int PLAYER_CHALLENGE = 1;

  /** The pin for the new public key easy connect request. */
  private String easyConnectPin;

  /** The username for the new public key easy connect request. */
  private String easyConnectName;

  /** The player that this connection is for. */
  private Player player;

  /** The current state of the handshake process. */
  private State currentState = State.AwaitingClientInit;

  /** Challenges sent to the client. */
  private HandshakeChallenge[] handshakeChallenges;

  private MD5Key playerPublicKeyMD5;

  private final boolean useEasyConnect;

  /**
   * Creates a new {@code ServerHandshake} instance.
   *
   * @param connection The client connection for the handshake.
   * @param playerDatabase The database of players.
   * @param useEasyConnect If true, the client will use the easy connect method.
   */
  public ServerHandshake(
      MapToolServer server,
      Connection connection,
      ServerSidePlayerDatabase playerDatabase,
      boolean useEasyConnect) {
    this.server = server;
    this.connection = connection;
    this.playerDatabase = playerDatabase;
    this.useEasyConnect = useEasyConnect;

    whenComplete(
        (result, error) -> {
          connection.removeMessageHandler(this);

          if (getEasyConnectName() != null) {
            SwingUtilities.invokeLater(
                () ->
                    MapTool.getFrame()
                        .getConnectionPanel()
                        .removeAwaitingApproval(easyConnectName));
          }
        });
  }

  @Override
  public void whenComplete(BiConsumer<? super Player, ? super Throwable> callback) {
    stage = stage.whenComplete(callback);
  }

  private synchronized void setPlayer(Player player) {
    this.player = player;
  }

  private synchronized void setCurrentState(State state) {
    log.debug("Transitioning from {} to {}", currentState, state);
    currentState = state;
  }

  private synchronized String getEasyConnectName() {
    return easyConnectName;
  }

  /**
   * Sends an error response to the client and notifies any observers of the handshake that the
   * status has changed.
   *
   * @param errorCode The error code that should be sent to the client.
   */
  private void sendErrorResponseAndNotify(HandshakeResponseCodeMsg errorCode) {
    var msg = HandshakeMsg.newBuilder().setHandshakeResponseCodeMsg(errorCode).build();
    sendMessage(State.PlayerBlocked, msg);
    // Do not notify users as it will disconnect and client won't get message instead wait
    // for client to disconnect after getting this message, if they don't then it will fail
    // with invalid handshake.
  }

  private void sendMessage(State newState, HandshakeMsg message) {
    setCurrentState(newState);

    var msgType = message.getMessageTypeCase();
    log.debug("Server sent to {}: {}", connection.getId(), msgType);
    connection.sendMessage(message.toByteArray());
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var handshakeMsg = HandshakeMsg.parseFrom(message);
      var msgType = handshakeMsg.getMessageTypeCase();

      log.debug("from {} got: {}", id, msgType);

      if (msgType == MessageTypeCase.HANDSHAKE_RESPONSE_CODE_MSG) {
        HandshakeResponseCodeMsg code = handshakeMsg.getHandshakeResponseCodeMsg();
        String errorMessage;
        if (code.equals(HandshakeResponseCodeMsg.INVALID_PASSWORD)) {
          errorMessage = I18N.getText("Handshake.msg.incorrectPassword");
        } else if (code.equals(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY)) {
          errorMessage = I18N.getText("Handshake.msg.incorrectPublicKey");
        } else {
          errorMessage = I18N.getText("Handshake.msg.invalidHandshake");
        }
        future.completeExceptionally(new Failure(errorMessage));
        return;
      }

      switch (currentState) {
        case PlayerBlocked:
          sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_HANDSHAKE);
          break;
        case AwaitingClientInit:
          if (msgType == HandshakeMsg.MessageTypeCase.CLIENT_INIT_MSG) {
            handle(handshakeMsg.getClientInitMsg());
          } else {
            sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_HANDSHAKE);
          }
          break;
        case AwaitingClientPasswordAuth:
        case AwaitingClientPublicKeyAuth:
          if (msgType == MessageTypeCase.CLIENT_AUTH_MESSAGE) {
            handle(handshakeMsg.getClientAuthMessage());
          } else {
            sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_HANDSHAKE);
          }
          break;
        case AwaitingPublicKey:
          if (msgType == MessageTypeCase.PUBLIC_KEY_UPLOAD_MSG) {
            handle(handshakeMsg.getPublicKeyUploadMsg());
          }
      }
    } catch (Exception e) {
      log.warn("Unexpected exception during server handshake", e);
      setCurrentState(State.Error);
      future.completeExceptionally(new Failure(I18N.getText("Handshake.msg.unexpectedError"), e));
    }
  }

  private void handle(PublicKeyUploadMsg publicKeyUploadMsg) {
    var pendingPlayer =
        new PlayerAwaitingApproval(
            easyConnectName,
            easyConnectPin,
            Role.PLAYER,
            publicKeyUploadMsg.getPublicKey(),
            this::acceptNewPublicKey,
            this::denyNewPublicKey);
    SwingUtilities.invokeLater(
        () -> MapTool.getFrame().getConnectionPanel().addAwaitingApproval(pendingPlayer));
  }

  private void denyNewPublicKey(PlayerAwaitingApproval p) {
    sendErrorResponseAndNotify(HandshakeResponseCodeMsg.SERVER_DENIED);
  }

  private void acceptNewPublicKey(PlayerAwaitingApproval p) {
    if (getEasyConnectName() == null) {
      return; // Protect from event being fired more than once
    }
    easyConnectName = null;
    try {
      var playerDb = (PersistedPlayerDatabase) playerDatabase;
      var pl = playerDatabase.getPlayer(p.name());
      if (pl == null) {
        playerDb.addPlayerAsymmetricKey(p.name(), p.role(), Set.of(p.publicKey()));
      } else {
        playerDb.addAsymmetricKeys(pl.getName(), Set.of(p.publicKey()));
        if (pl.getRole() != p.role()) {
          playerDb.setRole(pl.getName(), p.role());
          setPlayer(playerDatabase.getPlayer(pl.getName()));
        }
      }
      playerDb.commitChanges();

      var publicKeyAddedMsgBuilder = PublicKeyAddedMsg.newBuilder();
      publicKeyAddedMsgBuilder.setPublicKey(p.publicKey());
      var msg = HandshakeMsg.newBuilder().setPublicKeyAddedMsg(publicKeyAddedMsgBuilder).build();
      sendMessage(State.AwaitingClientInit, msg);
    } catch (NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidAlgorithmParameterException
        | InvalidKeySpecException
        | InvalidKeyException
        | PasswordDatabaseException e) {
      log.error("Error adding public key", e);
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY);
    }
  }

  private void handle(ClientAuthMsg clientAuthMessage)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          ExecutionException,
          InterruptedException,
          NoSuchPaddingException,
          IllegalBlockSizeException,
          BadPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    byte[] response = clientAuthMessage.getChallengeResponse().toByteArray();
    if (handshakeChallenges.length > 1) {
      var iv = clientAuthMessage.getIv().toByteArray();
      if (Arrays.compare(response, handshakeChallenges[GM_CHALLENGE].getExpectedResponse(iv))
          == 0) {
        setPlayer(playerDatabase.getPlayerWithRole(player.getName(), Role.GM));
      } else if (Arrays.compare(
              response, handshakeChallenges[PLAYER_CHALLENGE].getExpectedResponse(iv))
          == 0) {
        setPlayer(playerDatabase.getPlayerWithRole(player.getName(), Role.PLAYER));
      } else {
        sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PASSWORD);
        return;
      }
    } else if (currentState == State.AwaitingClientPasswordAuth) {
      var iv = clientAuthMessage.getIv().toByteArray();
      if (Arrays.compare(response, handshakeChallenges[0].getExpectedResponse(iv)) != 0) {
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
    var connectionSuccessfulMsg =
        ConnectionSuccessfulMsg.newBuilder()
            .setRoleDto(player.isGM() ? RoleDto.GM : RoleDto.PLAYER)
            .setServerPolicyDto(server.getPolicy().toDto())
            .setGameDataDto(new DataStoreManager().toDto().get())
            .setAddOnLibraryListDto(new LibraryManager().addOnLibrariesToDto().get());
    var handshakeMsg =
        HandshakeMsg.newBuilder().setConnectionSuccessfulMsg(connectionSuccessfulMsg).build();
    sendMessage(State.Success, handshakeMsg);
    future.complete(player);
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
      throws ExecutionException,
          InterruptedException,
          NoSuchPaddingException,
          IllegalBlockSizeException,
          NoSuchAlgorithmException,
          BadPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    if (server.isPlayerConnected(clientInitMsg.getPlayerName())) {
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.PLAYER_ALREADY_CONNECTED);
      return;
    }

    if (!MapTool.isDevelopment() && !MapTool.getVersion().equals(clientInitMsg.getVersion())) {
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.WRONG_VERSION);
    }

    playerPublicKeyMD5 = new MD5Key(clientInitMsg.getPublicKeyMd5());

    try {
      setPlayer(playerDatabase.getPlayer(clientInitMsg.getPlayerName()));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      // Error fetching player is sent to client as invalid password intentionally.
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PASSWORD);
      return;
    }

    if (player == null) {
      if (useEasyConnect) {
        requestPublicKey(clientInitMsg.getPlayerName());
        return;
      }
      // Unknown player is sent to client as invalid password intentionally.
      sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PASSWORD);
      return;
    }

    if (playerDatabase.isBlocked(player)) {
      var blockedMsg =
          PlayerBlockedMsg.newBuilder().setReason(playerDatabase.getBlockedReason(player)).build();
      var msg = HandshakeMsg.newBuilder().setPlayerBlockedMsg(blockedMsg).build();
      sendMessage(State.Error, msg);
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
  }

  private void requestPublicKey(String playerName) {
    var requestPublicKeyBuilder = RequestPublicKeyMsg.newBuilder();
    easyConnectPin = String.format("%04d", new SecureRandom().nextInt(9999));
    easyConnectName = playerName;
    requestPublicKeyBuilder.setPin(easyConnectPin);
    var msg = HandshakeMsg.newBuilder().setRequestPublicKeyMsg(requestPublicKeyBuilder).build();
    sendMessage(State.AwaitingPublicKey, msg);
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
      throws NoSuchPaddingException,
          IllegalBlockSizeException,
          NoSuchAlgorithmException,
          BadPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    byte[] playerPasswordSalt = playerDatabase.getPlayerPasswordSalt(player.getName());

    SecureRandom rnd = new SecureRandom();
    byte[] iv = new byte[CipherUtil.CIPHER_BLOCK_SIZE];
    rnd.nextBytes(iv);
    String password = new PasswordGenerator().getPassword();
    handshakeChallenges = new HandshakeChallenge[1];
    Key key = playerDatabase.getPlayerPassword(player.getName()).get();
    handshakeChallenges[0] =
        HandshakeChallenge.createSymmetricChallenge(player.getName(), password, key, iv);

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.SHARED_PASSWORD)
            .setSalt(ByteString.copyFrom(playerPasswordSalt))
            .setIv(ByteString.copyFrom(iv))
            .addChallenge(ByteString.copyFrom(handshakeChallenges[0].getChallenge()));
    var handshakeMsg = HandshakeMsg.newBuilder().setUseAuthTypeMsg(authTypeMsg).build();
    sendMessage(State.AwaitingClientPasswordAuth, handshakeMsg);
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
      throws NoSuchPaddingException,
          IllegalBlockSizeException,
          NoSuchAlgorithmException,
          BadPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    byte[] playerPasswordSalt = playerDatabase.getPlayerPasswordSalt(player.getName());
    String[] password = new String[2];
    password[GM_CHALLENGE] = new PasswordGenerator().getPassword();
    password[PLAYER_CHALLENGE] = new PasswordGenerator().getPassword();

    SecureRandom rnd = new SecureRandom();
    byte[] iv = new byte[CipherUtil.CIPHER_BLOCK_SIZE];
    rnd.nextBytes(iv);
    handshakeChallenges[GM_CHALLENGE] =
        HandshakeChallenge.createSymmetricChallenge(
            player.getName(),
            password[GM_CHALLENGE],
            playerDatabase.getRolePassword(Role.GM).get(),
            iv);
    handshakeChallenges[PLAYER_CHALLENGE] =
        HandshakeChallenge.createSymmetricChallenge(
            player.getName(),
            password[GM_CHALLENGE],
            playerDatabase.getRolePassword(Role.PLAYER).get(),
            iv);

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.SHARED_PASSWORD)
            .setSalt(ByteString.copyFrom(playerPasswordSalt))
            .setIv(ByteString.copyFrom(iv))
            .addChallenge(ByteString.copyFrom(handshakeChallenges[GM_CHALLENGE].getChallenge()))
            .addChallenge(
                ByteString.copyFrom(handshakeChallenges[PLAYER_CHALLENGE].getChallenge()));
    var handshakeMsg = HandshakeMsg.newBuilder().setUseAuthTypeMsg(authTypeMsg).build();
    sendMessage(State.AwaitingClientPasswordAuth, handshakeMsg);
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
      throws ExecutionException,
          InterruptedException,
          NoSuchPaddingException,
          IllegalBlockSizeException,
          NoSuchAlgorithmException,
          BadPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException {
    handshakeChallenges = new HandshakeChallenge[1];
    if (!playerDatabase.hasPublicKey(player, playerPublicKeyMD5).join()) {
      if (useEasyConnect) {
        requestPublicKey(player.getName());
      } else {
        sendErrorResponseAndNotify(HandshakeResponseCodeMsg.INVALID_PUBLIC_KEY);
      }
    }
    CipherUtil.Key publicKey = playerDatabase.getPublicKey(player, playerPublicKeyMD5).get();
    String password = new PasswordGenerator().getPassword();
    handshakeChallenges[0] =
        HandshakeChallenge.createAsymmetricChallenge(player.getName(), password, publicKey);

    var authTypeMsg =
        UseAuthTypeMsg.newBuilder()
            .setAuthType(AuthTypeEnum.ASYMMETRIC_KEY)
            .addChallenge(ByteString.copyFrom(handshakeChallenges[0].getChallenge()));
    var handshakeMsg = HandshakeMsg.newBuilder().setUseAuthTypeMsg(authTypeMsg).build();
    sendMessage(State.AwaitingClientPublicKeyAuth, handshakeMsg);
  }

  @Override
  public void startHandshake() {
    connection.addMessageHandler(this);
    setCurrentState(State.AwaitingClientInit);
  }

  /** The states that the server side of the server side of the handshake process can be in. */
  private enum State {
    Error,
    Success,
    AwaitingClientInit,
    AwaitingClientPasswordAuth,
    AwaitingClientPublicKeyAuth,
    AwaitingPublicKey,
    PlayerBlocked
  }
}
