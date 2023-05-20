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
package net.rptools.clientserver.simple.client;

import com.google.gson.Gson;
import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import net.rptools.clientserver.simple.server.WebRTCServerConnection;
import net.rptools.clientserver.simple.webrtc.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.server.ServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebRTCClientConnection extends AbstractClientConnection
    implements ClientConnection, PeerConnectionObserver, RTCDataChannelObserver {
  private static final Logger log = LogManager.getLogger(WebRTCClientConnection.class);

  private final PeerConnectionFactory factory = new PeerConnectionFactory();
  private final ServerConfig config;
  private final String id;
  private final Gson gson = new Gson();
  private WebSocketClient signalingClient;
  // only set on server side
  private WebRTCServerConnection serverConnection;
  private RTCConfiguration rtcConfig;
  private RTCPeerConnection peerConnection;
  private RTCDataChannel localDataChannel;
  private String lastError = null;

  private final SendThread sendThread = new SendThread();
  private Thread handleDisconnect;

  // used from client side
  public WebRTCClientConnection(String id, ServerConfig config) {
    this.id = id;
    this.config = config;
    init();
  }

  // this is used from the server side
  public WebRTCClientConnection(
      OfferMessageDto message, WebRTCServerConnection webRTCServerConnection) {
    this.id = message.source;
    this.serverConnection = webRTCServerConnection;
    this.config = serverConnection.getConfig();
    this.signalingClient = serverConnection.getSignalingClient();
    init();

    peerConnection = factory.createPeerConnection(rtcConfig, this);
    peerConnection.setRemoteDescription(
        message.offer,
        new SetSessionDescriptionObserver() {
          @Override
          public void onSuccess() {
            log.info(prefix() + " setRemoteDescription success.");
          }

          @Override
          public void onFailure(String error) {
            log.error(prefix() + " error setting remote description: " + error);
          }
        });

    var answerOptions = new RTCAnswerOptions();
    peerConnection.createAnswer(
        answerOptions,
        new CreateSessionDescriptionObserver() {
          @Override
          public void onSuccess(RTCSessionDescription description) {
            peerConnection.setLocalDescription(
                description,
                new SetSessionDescriptionObserver() {
                  @Override
                  public void onSuccess() {
                    var msg = new AnswerMessageDto();
                    msg.source = serverConnection.getConfig().getServerName();
                    msg.destination = getId();
                    msg.answer = description;
                    sendSignalingMessage(gson.toJson(msg));
                  }

                  @Override
                  public void onFailure(String error) {
                    log.error(prefix() + "Error setting answer as local description: " + error);
                  }
                });
          }

          @Override
          public void onFailure(String error) {
            log.error(prefix() + "Error creating answer: " + error);
          }
        });
  }

  private boolean isServerSide() {
    return serverConnection != null;
  }

  private String getSource() {
    // on server side the id is already user@server
    if (isServerSide()) return getId();

    return getId() + "@" + config.getServerName();
  }

  private void startSignaling() {
    URI uri = null;
    try {
      uri = new URI(WebRTCServerConnection.WebSocketUrl);
    } catch (Exception e) {
    }

    signalingClient =
        new WebSocketClient(uri) {
          @Override
          public void onOpen(ServerHandshake handshakeData) {
            log.info(prefix() + "WebSocket connected\n");
            var msg = new LoginMessageDto();
            msg.source = getSource();
            sendSignalingMessage(gson.toJson(msg));
          }

          @Override
          public void onMessage(String message) {
            handleSignalingMessage(message);
          }

          @Override
          public void onClose(int code, String reason, boolean remote) {
            lastError = "WebSocket closed: (" + code + ") " + reason;
            log.info(prefix() + lastError);
            if (!isAlive()) fireDisconnectAsync();
          }

          @Override
          public void onError(Exception ex) {
            lastError = "WebSocket error: " + ex.toString() + "\n";
            log.info(prefix() + lastError);
            // onClose will be called after this
          }
        };
    signalingClient.connect();
  }

  private void init() {
    rtcConfig = new RTCConfiguration();

    var googleStun = new RTCIceServer();
    googleStun.urls.add("stun:stun.l.google.com:19302");
    googleStun.urls.add("stun:stun1.l.google.com:19302");
    googleStun.urls.add("stun:stun2.l.google.com:19302");
    googleStun.urls.add("stun:stun3.l.google.com:19302");
    googleStun.urls.add("stun:stun4.l.google.com:19302");
    rtcConfig.iceServers.add(googleStun);

    var openRelayStun = new RTCIceServer();
    openRelayStun.urls.add("stun:openrelay.metered.ca:80");
    rtcConfig.iceServers.add(openRelayStun);

    var openRelayTurn = new RTCIceServer();
    openRelayTurn.urls.add("turn:openrelay.metered.ca:80");
    openRelayTurn.username = "openrelayproject";
    openRelayTurn.password = "openrelayproject";
    rtcConfig.iceServers.add(openRelayTurn);

    var openRelayTurn2 = new RTCIceServer();
    openRelayTurn2.urls.add("turn:openrelay.metered.ca:443");
    openRelayTurn2.username = "openrelayproject";
    openRelayTurn2.password = "openrelayproject";
    rtcConfig.iceServers.add(openRelayTurn2);

    var openRelayTurn3 = new RTCIceServer();
    openRelayTurn3.urls.add("turn:openrelay.metered.ca:443?transport=tcp");
    openRelayTurn3.username = "openrelayproject";
    openRelayTurn3.password = "openrelayproject";
    rtcConfig.iceServers.add(openRelayTurn3);
  }

  @Override
  public void sendMessage(byte[] message) {
    sendMessage(null, message);
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    log.debug(prefix() + "added message");
    addMessage(channel, message);
    if (peerConnection != null
        && peerConnection.getConnectionState() == RTCPeerConnectionState.CONNECTED) {
      synchronized (sendThread) {
        sendThread.notify();
      }
    }
  }

  @Override
  public boolean isAlive() {
    if (peerConnection == null) return false;

    return switch (peerConnection.getConnectionState()) {
      case CONNECTED, DISCONNECTED -> true;
      default -> false;
    };
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void open() throws IOException {
    startSignaling();
  }

  private void sendSignalingMessage(String message) {
    log.debug(prefix() + "sent signaling message: " + message);
    signalingClient.send(message);
  }

  private void handleSignalingMessage(String message) {
    log.debug(prefix() + "got signaling message: " + message);
    var msg = gson.fromJson(message, MessageDto.class);
    switch (msg.type) {
      case "login" -> {
        var loginMsg = gson.fromJson(message, LoginMessageDto.class);
        onLogin(loginMsg);
      }
      case "answer" -> {
        var answerMessage = gson.fromJson(message, AnswerMessageDto.class);
        onAnswer(answerMessage);
      }
      case "candidate" -> {
        var candidateMessage = gson.fromJson(message, CandidateMessageDto.class);
        addIceCandidate(candidateMessage.candidate);
      }
      default -> log.error(prefix() + "unhandled signaling: " + msg.type);
    }
  }

  private void onAnswer(AnswerMessageDto answerMessage) {
    peerConnection.setRemoteDescription(
        answerMessage.answer,
        new SetSessionDescriptionObserver() {
          @Override
          public void onSuccess() {
            log.info(prefix() + " setRemoteDescription success.");
          }

          @Override
          public void onFailure(String error) {
            log.error(prefix() + " error setting remote description: " + error);
          }
        });
  }

  private void onLogin(LoginMessageDto message) {
    if (!message.success) {
      MapTool.showError("Handshake.msg.playerAlreadyConnected");
      return;
    }

    peerConnection = factory.createPeerConnection(rtcConfig, this);

    var initDict = new RTCDataChannelInit();
    localDataChannel = peerConnection.createDataChannel("myDataChannel", initDict);
    localDataChannel.registerObserver(this);

    var offerOptions = new RTCOfferOptions();
    peerConnection.createOffer(
        offerOptions,
        new CreateSessionDescriptionObserver() {
          @Override
          public void onSuccess(RTCSessionDescription description) {
            peerConnection.setLocalDescription(
                description,
                new SetSessionDescriptionObserver() {
                  @Override
                  public void onSuccess() {
                    var msg = new OfferMessageDto();
                    msg.offer = description;
                    msg.source = getSource();
                    msg.destination = config.getServerName();
                    sendSignalingMessage(gson.toJson(msg));
                  }

                  @Override
                  public void onFailure(String error) {
                    log.error(prefix() + "Error setting answer as local description: " + error);
                  }
                });
          }

          @Override
          public void onFailure(String error) {
            log.error(prefix() + "Error creating offer: " + error);
          }
        });
  }

  private String prefix() {
    return isServerSide() ? "S " : "C ";
  }

  @Override
  public void onSignalingChange(RTCSignalingState state) {
    // set thread name for better logs.
    Thread.currentThread().setName("WebRTCClientConnection.WebRTCThread_" + getId());
    log.info(prefix() + "PeerConnection.onSignalingChange: " + state);
  }

  @Override
  public void onConnectionChange(RTCPeerConnectionState state) {
    log.info(prefix() + "PeerConnection.onConnectionChange " + state);
    switch (state) {
      case FAILED -> {
        lastError = "PeerConnection failed";
        peerConnection = null;
        fireDisconnectAsync();
      }
      case CONNECTED -> {
        if (hasMoreMessages()) {
          synchronized (sendThread) {
            sendThread.notify();
          }
        }
      }
    }
  }

  @Override
  public void onIceConnectionChange(RTCIceConnectionState state) {
    log.info(prefix() + "PeerConnection.onIceConnectionChange " + state);
  }

  @Override
  public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
    log.info(prefix() + "PeerConnection.onStandardizedIceConnectionChange " + state);
  }

  @Override
  public void onIceConnectionReceivingChange(boolean receiving) {
    log.info(prefix() + "PeerConnection.onIceConnectionReceivingChange " + receiving);
  }

  @Override
  public void onIceGatheringChange(RTCIceGatheringState state) {
    log.info(prefix() + "PeerConnection.onIceGatheringChange " + state);
  }

  @Override
  public void onIceCandidate(RTCIceCandidate candidate) {
    var msg = new CandidateMessageDto();

    if (isServerSide()) {
      msg.source = config.getServerName();
      msg.destination = getSource();
    } else {
      msg.destination = config.getServerName();
      msg.source = getSource();
    }
    msg.candidate = candidate;
    sendSignalingMessage(gson.toJson(msg));
  }

  @Override
  public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
    log.debug(
        prefix()
            + "PeerConnection.onIceCandidateError: code:"
            + event.getErrorCode()
            + " url: "
            + event.getUrl()
            + " address/port: "
            + event.getAddress()
            + ":"
            + event.getPort()
            + " text: "
            + event.getErrorText());
  }

  @Override
  public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {
    log.info(prefix() + "PeerConnection.onIceCandidatesRemoved");
  }

  @Override
  public void onAddStream(MediaStream stream) {
    log.info(prefix() + "PeerConnection.onAddStream");
  }

  @Override
  public void onRemoveStream(MediaStream stream) {
    log.info(prefix() + "PeerConnection.onRemoveStream");
  }

  @Override
  public void onDataChannel(RTCDataChannel newDataChannel) {
    log.info(prefix() + "PeerConnection.onDataChannel");
    this.localDataChannel = newDataChannel;
    localDataChannel.registerObserver(this);

    if (isServerSide()) {
      serverConnection.onDataChannelOpened(this);
    }
  }

  @Override
  public void onRenegotiationNeeded() {
    // set thread name for better logs
    Thread.currentThread().setName("WebRTCClientConnection.WebRTCThread_" + getId());
    log.info(prefix() + "PeerConnection.onRenegotiationNeeded");
  }

  @Override
  public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
    log.info(prefix() + "PeerConnection.onTrack(multiple Streams)");
  }

  @Override
  public void onRemoveTrack(RTCRtpReceiver receiver) {
    log.info(prefix() + "PeerConnection.onRemoveTrack");
  }

  @Override
  public void onTrack(RTCRtpTransceiver transceiver) {
    log.info(prefix() + "PeerConnection.onTrack");
  }

  public void addIceCandidate(RTCIceCandidate candidate) {
    log.info(prefix() + "PeerConnection.addIceCandidate: " + candidate.toString());
    peerConnection.addIceCandidate(candidate);
  }

  // dataChannel
  @Override
  public void onBufferedAmountChange(long previousAmount) {
    log.info(prefix() + "dataChannel onBufferedAmountChange " + previousAmount);
  }

  // dataChannel
  @Override
  public void onStateChange() {
    var state = localDataChannel.getState();
    log.info(prefix() + "localDataChannel onStateChange " + state);
    switch (state) {
      case OPEN -> {
        // connection established we don't need the signaling server anymore
        // for now disabled. We may get additional ice candidates.
        if (!isServerSide() && signalingClient.isOpen()) signalingClient.close();

        sendThread.start();
      }
      case CLOSED -> {
        close();
        fireDisconnectAsync();
      }
    }
  }

  // dataChannel
  @Override
  public void onMessage(RTCDataChannelBuffer channelBuffer) {
    log.debug(
        prefix() + "localDataChannel onMessage: got " + channelBuffer.data.capacity() + " bytes");

    if (Thread.currentThread().getContextClassLoader() == null) {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
    }

    var message = readMessage(channelBuffer.data);
    if (message != null) dispatchCompressedMessage(id, message);
  }

  private void fireDisconnectAsync() {
    handleDisconnect =
        new Thread(
            () -> {
              fireDisconnect();
              if (isServerSide()) serverConnection.clearClients();
            },
            "WebRTCClientConnection.handleDisconnect");
    handleDisconnect.start();
  }

  @Override
  public void close() {
    // signalingClient should be closed if connection was established
    if (!isServerSide() && signalingClient.isOpen()) signalingClient.close();

    if (sendThread.stopRequested) return;

    sendThread.requestStop();
    if (peerConnection != null) {
      peerConnection.close();
      peerConnection = null;
    }
  }

  @Override
  public String getError() {
    return lastError;
  }

  private class SendThread extends Thread {
    private boolean stopRequested = false;

    public SendThread() {
      super("WebRTCClientConnection.SendThread_" + WebRTCClientConnection.this.getId());
    }

    public void requestStop() {
      this.stopRequested = true;
      synchronized (this) {
        this.notify();
      }
    }

    @Override
    public void run() {
      log.debug(prefix() + " sendThread started");
      try {
        while (!stopRequested && WebRTCClientConnection.this.isAlive()) {
          while (WebRTCClientConnection.this.hasMoreMessages()
              && peerConnection.getConnectionState() == RTCPeerConnectionState.CONNECTED) {
            byte[] message = WebRTCClientConnection.this.nextMessage();
            if (message == null) {
              continue;
            }

            ByteBuffer buffer = ByteBuffer.allocate(message.length + Integer.BYTES);
            buffer.putInt(message.length).put(message).rewind();

            int chunkSize = 16 * 1024;

            while (buffer.remaining() > 0) {
              var amountToSend = buffer.remaining() <= chunkSize ? buffer.remaining() : chunkSize;
              ByteBuffer part = buffer;

              if (amountToSend != buffer.capacity()) {
                // we need to allocation a new ByteBuffer because send calls ByteBuffer.array()
                // which would return
                // the whole byte[] and not only the slice. But the lib doesn't use
                // ByteBuffer.arrayOffset().
                var slice = buffer.slice(buffer.position(), amountToSend);
                part = ByteBuffer.allocate(amountToSend);
                part.put(slice);
              }

              buffer.position(buffer.position() + amountToSend);
              localDataChannel.send(new RTCDataChannelBuffer(part, true));
              log.debug(prefix() + " sent " + part.capacity() + " bytes");
            }
          }
          synchronized (this) {
            if (!stopRequested) {
              try {
                log.debug(prefix() + "sendThread -> sleep");
                this.wait();
                log.debug(prefix() + "sendThread -> woke up");
              } catch (InterruptedException e) {
                log.debug(prefix() + "sendThread -> interrupted");
              }
            }
          }
        }
      } catch (Exception e) {
        log.error(prefix() + e);
        fireDisconnect();
      }
      log.debug(prefix() + " sendThread ended");
    }
  }
}
