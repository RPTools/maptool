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
package net.rptools.clientserver.simple.connection;

import com.google.gson.Gson;
import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import net.rptools.clientserver.simple.server.WebRTCServer;
import net.rptools.clientserver.simple.webrtc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebRTCConnection extends AbstractConnection implements Connection {
  public interface Listener {
    void onLoginError();
  }

  private static final Logger log = LogManager.getLogger(WebRTCConnection.class);

  private final PeerConnectionObserver peerConnectionObserver = new PeerConnectionObserverImpl();
  private final RTCDataChannelObserver rtcDataChannelObserver = new RTCDataChannelObserverImpl();
  private final PeerConnectionFactory factory = new PeerConnectionFactory();
  private final String serverName;
  private final String id;
  private final Gson gson = new Gson();
  private final Listener listener;
  private WebSocketClient signalingClient;
  // only set on server side
  private WebRTCServer server;
  private RTCConfiguration rtcConfig;
  private RTCPeerConnection peerConnection;
  private RTCDataChannel localDataChannel;
  private String lastError = null;

  private final SendThread sendThread = new SendThread();
  private Thread handleDisconnect;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  // used from client side
  public WebRTCConnection(String id, String serverName, Listener listener) {
    this.id = id;
    this.serverName = serverName;
    this.listener = listener;
    init();
  }

  // this is used from the server side
  public WebRTCConnection(OfferMessageDto message, WebRTCServer webRTCServer) {
    this.id = message.source;
    this.server = webRTCServer;
    this.serverName = server.getName();
    this.listener = () -> {};
    this.signalingClient = server.getSignalingClient();
    init();

    peerConnection = factory.createPeerConnection(rtcConfig, peerConnectionObserver);
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
                    msg.source = serverName;
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
    return server != null;
  }

  private String getSource() {
    // on server side the id is already user@server
    if (isServerSide()) {
      return getId();
    }

    return getId() + "@" + serverName;
  }

  private void startSignaling() {
    URI uri = null;
    try {
      uri = new URI(WebRTCServer.WebSocketUrl);
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
            if (!isAlive()) {
              fireDisconnectAsync();
            }
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
  public void sendMessage(Object channel, byte[] message) {
    log.debug(prefix() + "added message");
    addMessage(channel, message);
  }

  @Override
  public boolean isAlive() {
    if (peerConnection == null) {
      return false;
    }

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
      listener.onLoginError();
      return;
    }

    peerConnection = factory.createPeerConnection(rtcConfig, peerConnectionObserver);

    var initDict = new RTCDataChannelInit();
    localDataChannel = peerConnection.createDataChannel("myDataChannel", initDict);
    localDataChannel.registerObserver(rtcDataChannelObserver);

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
                    msg.destination = serverName;
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

  public void addIceCandidate(RTCIceCandidate candidate) {
    log.info(prefix() + "PeerConnection.addIceCandidate: " + candidate.toString());
    peerConnection.addIceCandidate(candidate);
  }

  private void fireDisconnectAsync() {
    handleDisconnect =
        new Thread(
            () -> {
              fireDisconnect();
            },
            "WebRTCConnection.handleDisconnect");
    handleDisconnect.start();
  }

  @Override
  protected void onClose() {
    // signalingClient should be closed if connection was established
    if (!isServerSide() && signalingClient.isOpen()) {
      signalingClient.close();
    }

    sendThread.interrupt();
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
    public SendThread() {
      super("WebRTCConnection.SendThread_" + WebRTCConnection.this.getId());
    }

    @Override
    public void run() {
      log.debug(prefix() + " sendThread started");

      while (!WebRTCConnection.this.isClosed() && WebRTCConnection.this.isAlive()) {
        // Blocks for a time until a message is received.
        byte[] message = WebRTCConnection.this.nextMessage();
        if (message == null) {
          // No message available. Thread may also have been interrupted as part of stopping.
          continue;
        }

        ByteBuffer buffer = ByteBuffer.allocate(message.length + Integer.BYTES);
        buffer.putInt(message.length).put(message).rewind();

        int chunkSize = 16 * 1024;

        while (buffer.remaining() > 0) {
          var amountToSend = Math.min(buffer.remaining(), chunkSize);
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
          try {
            localDataChannel.send(new RTCDataChannelBuffer(part, true));
          } catch (Exception e) {
            log.error(prefix() + e);
            fireDisconnect();
            return;
          }
          log.debug(prefix() + " sent " + part.capacity() + " bytes");
        }
      }

      log.debug(prefix() + " sendThread ended");
    }
  }

  private final class PeerConnectionObserverImpl implements PeerConnectionObserver {
    @Override
    public void onIceCandidate(RTCIceCandidate candidate) {
      var msg = new CandidateMessageDto();

      if (isServerSide()) {
        msg.source = serverName;
        msg.destination = getSource();
      } else {
        msg.destination = serverName;
        msg.source = getSource();
      }
      msg.candidate = candidate;
      sendSignalingMessage(gson.toJson(msg));
    }

    @Override
    public void onAddStream(MediaStream stream) {
      log.info(prefix() + "PeerConnection.onAddStream");
    }

    @Override
    public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
      log.info(prefix() + "PeerConnection.onTrack(multiple Streams)");
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
      }
    }

    @Override
    public void onDataChannel(RTCDataChannel newDataChannel) {
      log.info(prefix() + "PeerConnection.onDataChannel");
      localDataChannel = newDataChannel;
      localDataChannel.registerObserver(rtcDataChannelObserver);

      if (isServerSide()) {
        server.onDataChannelOpened(WebRTCConnection.this);
      }
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
    public void onIceConnectionChange(RTCIceConnectionState state) {
      log.info(prefix() + "PeerConnection.onIceConnectionChange " + state);
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
    public void onRemoveStream(MediaStream stream) {
      log.info(prefix() + "PeerConnection.onRemoveStream");
    }

    @Override
    public void onRemoveTrack(RTCRtpReceiver receiver) {
      log.info(prefix() + "PeerConnection.onRemoveTrack");
    }

    @Override
    public void onRenegotiationNeeded() {
      // set thread name for better logs
      Thread.currentThread().setName("WebRTCConnection.WebRTCThread_" + getId());
      log.info(prefix() + "PeerConnection.onRenegotiationNeeded");
    }

    @Override
    public void onSignalingChange(RTCSignalingState state) {
      // set thread name for better logs.
      Thread.currentThread().setName("WebRTCConnection.WebRTCThread_" + getId());
      log.info(prefix() + "PeerConnection.onSignalingChange: " + state);
    }

    @Override
    public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
      log.info(prefix() + "PeerConnection.onStandardizedIceConnectionChange " + state);
    }

    @Override
    public void onTrack(RTCRtpTransceiver transceiver) {
      log.info(prefix() + "PeerConnection.onTrack");
    }
  }

  private final class RTCDataChannelObserverImpl implements RTCDataChannelObserver {
    @Override
    public void onBufferedAmountChange(long previousAmount) {
      log.info(prefix() + "dataChannel onBufferedAmountChange " + previousAmount);
    }

    @Override
    public void onStateChange() {
      var state = localDataChannel.getState();
      log.info(prefix() + "localDataChannel onStateChange " + state);
      switch (state) {
        case OPEN -> {
          // connection established we don't need the signaling server anymore
          // for now disabled. We may get additional ice candidates.
          if (!isServerSide() && signalingClient.isOpen()) {
            signalingClient.close();
          }

          sendThread.start();
        }
        case CLOSED -> {
          close();
          fireDisconnectAsync();
        }
      }
    }

    @Override
    public void onMessage(RTCDataChannelBuffer channelBuffer) {
      log.debug(
          prefix() + "localDataChannel onMessage: got " + channelBuffer.data.capacity() + " bytes");

      if (Thread.currentThread().getContextClassLoader() == null) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
      }

      var message = readMessage(channelBuffer.data);
      if (message != null) {
        dispatchCompressedMessage(message);
      }
    }
  }
}
