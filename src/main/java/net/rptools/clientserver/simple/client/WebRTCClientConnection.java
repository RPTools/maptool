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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.server.WebRTCServerConnection;
import net.rptools.clientserver.simple.webrtc.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.server.ServerConfig;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebRTCClientConnection extends AbstractConnection
    implements ClientConnection,
        PeerConnectionObserver,
        SetSessionDescriptionObserver,
        CreateSessionDescriptionObserver,
        RTCDataChannelObserver {
  private static final Logger log = Logger.getLogger(WebRTCClientConnection.class);

  private final PeerConnectionFactory factory = new PeerConnectionFactory();
  private final ServerConfig config;
  private final String id;
  private final Gson gson = new Gson();
  private WebSocketClient signalingCLient;
  // only set on server side
  private WebRTCServerConnection serverConnection;
  private RTCConfiguration rtcConfig;
  private RTCPeerConnection peerConnection;
  private RTCDataChannel localDataChannel;
  private RTCDataChannel remoteDataChannel;
  private String lastError = null;

  private SendThread sendThread = new SendThread(this);;
  private Thread handleConnnect;
  private Thread handleDisconnect;

  // used from client side
  public WebRTCClientConnection(String id, ServerConfig config) throws IOException {
    this.id = id;
    this.config = config;
    init();
  }

  // this is used from the server side
  public WebRTCClientConnection(
      OfferMessageDto message, WebRTCServerConnection webRTCServerConnection) throws IOException {
    this.id = message.source;
    this.serverConnection = webRTCServerConnection;
    this.config = serverConnection.getConfig();
    this.signalingCLient = serverConnection.getSignalingCLient();
    init();

    peerConnection = factory.createPeerConnection(rtcConfig, this);
    peerConnection.setRemoteDescription(message.offer, this);

    var answerOptions = new RTCAnswerOptions();
    peerConnection.createAnswer(answerOptions, this);
  }

  private boolean isServerSide() {
    return serverConnection != null;
  }

  private String getSource() {
    // on server side the id is alread user@server
    if (isServerSide()) return getId();

    return getId() + "@" + config.getServerName();
  }

  private void startSignaling() {
    URI uri = null;
    try {
      uri = new URI(WebRTCServerConnection.WebSocketUrl);
    } catch (Exception e) {
    }

    signalingCLient =
        new WebSocketClient(uri) {
          @Override
          public void onOpen(ServerHandshake handshakedata) {
            log.info("Websocket connected\n");
            var msg = new LoginMessageDto();
            msg.source = getSource();
            send(gson.toJson(msg));
          }

          @Override
          public void onMessage(String message) {
            // log.info("Got message: " + message + "\n");
            handleSignalingMessage(message);
          }

          @Override
          public void onClose(int code, String reason, boolean remote) {
            lastError = "Websocket closed: (" + code + ") " + reason;
            log.info(lastError);
            if (!isAlive()) fireDisconnectAsync();
          }

          @Override
          public void onError(Exception ex) {
            lastError = "Websocket error: " + ex.toString() + "\n";
            log.info(lastError);
            // onClose will be called after this
          }
        };
    signalingCLient.connect();
  }

  private void init() throws IOException {
    RTCIceServer iceServer = new RTCIceServer();
    iceServer.urls.add("stun:stun.l.google.com:19302");

    rtcConfig = new RTCConfiguration();
    rtcConfig.iceServers.add(iceServer);
  }

  @Override
  public void sendMessage(byte[] message) {
    sendMessage(null, message);
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    log.debug(prefix() + "added message");
    addMessage(channel, message);
    synchronized (sendThread) {
      sendThread.notify();
    }
  }

  @Override
  public boolean isAlive() {
    if (peerConnection == null) return false;

    return peerConnection.getConnectionState() == RTCPeerConnectionState.CONNECTED;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void open() throws IOException {
    startSignaling();
  }

  private void handleSignalingMessage(String message) {
    var msg = gson.fromJson(message, MessageDto.class);
    switch (msg.type) {
      case "login":
        {
          var loginMsg = gson.fromJson(message, LoginMessageDto.class);
          onLogin(loginMsg);
          break;
        }
        //     case "offer": // client side should not get offers
        //  onOffer(data.getString("offer"), data.getString("name"));
        //      break;
      case "answer":
        {
          var answerMessage = gson.fromJson(message, AnswerMessageDto.class);
          onAnswer(answerMessage);
          break;
        }
      case "candidate":
        {
          var candiateMessage = gson.fromJson(message, CandidateMessageDto.class);
          onIceCandidate(candiateMessage.candidate);
          break;
        }
      default:
        log.error("unhandled signaling: " + msg.type);
    }
  }

  private void onAnswer(AnswerMessageDto answerMessage) {
    peerConnection.setRemoteDescription(answerMessage.answer, this);
  }

  private void onLogin(LoginMessageDto message) {
    if (!message.success) {
      MapTool.showError("Player already taken!");
      return;
    }

    peerConnection = factory.createPeerConnection(rtcConfig, this);

    var initDict = new RTCDataChannelInit();
    localDataChannel = peerConnection.createDataChannel("myDataChannel", initDict);
    localDataChannel.registerObserver(this);

    var offerOptions = new RTCOfferOptions();
    peerConnection.createOffer(offerOptions, this);
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
    if (state == RTCPeerConnectionState.DISCONNECTED) {
      peerConnection = null;
      fireDisconnectAsync();
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
    signalingCLient.send(gson.toJson(msg));
  }

  @Override
  public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
    log.info(prefix() + "PeerConnection.onIceCandidateError " + event.getErrorText());
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
      handleConnnect =
          new Thread(
              () -> {
                serverConnection.onDataChannelOpened(this);
              },
              "handeConnect_" + id);
      if (handleConnnect.getContextClassLoader() == null) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        handleConnnect.setContextClassLoader(cl);
      }
      handleConnnect.start();
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
    log.info("PeerConnection.onTrack(multiple Streams)");
  }

  @Override
  public void onRemoveTrack(RTCRtpReceiver receiver) {
    log.info("PeerConnection.onRemoveTrack");
  }

  @Override
  public void onTrack(RTCRtpTransceiver transceiver) {
    log.info("PeerConnection.onTrack");
  }

  // setSession
  @Override
  public void onSuccess() {}

  @Override
  public void onSuccess(RTCSessionDescription description) {
    peerConnection.setLocalDescription(description, this);

    if (isServerSide()) {
      var msg = new AnswerMessageDto();
      msg.source = serverConnection.getConfig().getServerName();
      msg.destination = getId();
      msg.answer = description;
      signalingCLient.send(gson.toJson(msg));
    } else {
      var msg = new OfferMessageDto();
      msg.offer = description;
      msg.source = getSource();
      msg.destination = config.getServerName();
      signalingCLient.send(gson.toJson(msg));
    }
  }

  @Override
  public void onFailure(String error) {
    log.error(error);
  }

  public void addIceCandidate(RTCIceCandidate candidate) {
    peerConnection.addIceCandidate(candidate);
  }

  // datachannel
  @Override
  public void onBufferedAmountChange(long previousAmount) {
    log.info(prefix() + "dataChannel onBufferedAmountChange " + previousAmount);
  }

  // datachannel
  @Override
  public void onStateChange() {
    log.info(prefix() + "localDataChannel onStateChange " + localDataChannel.getState());
    if (localDataChannel.getState() == RTCDataChannelState.OPEN) {
      // connection established we don't need the signaling server anymore
      if (!isServerSide() && signalingCLient.isOpen()) signalingCLient.close();

      sendThread.start();
    }
  }

  // datachannel
  @Override
  public void onMessage(RTCDataChannelBuffer channelBuffer) {
    log.debug(
        prefix() + "localDataChannel onMessage: got " + channelBuffer.data.capacity() + " bytes");
    var buffer = channelBuffer.data;

    if (Thread.currentThread().getContextClassLoader() == null) {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
    }

    int len = buffer.capacity();
    var byteArray = new byte[len];
    buffer.get(byteArray);

    InputStream byteStream = new ByteArrayInputStream(byteArray);
    try {
      while (byteStream.available() > 0) {
        var message = readMessage(byteStream);
        dispatchMessage(id, message);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void fireDisconnectAsync() {
    handleDisconnect =
        new Thread(
            () -> {
              fireDisconnect();
              if (isServerSide()) serverConnection.clearClients();
            },
            "WebRTCClientConnection.handeDisconnect");
    handleDisconnect.start();
  }

  @Override
  public void close() {
    // signalingClient should be closed if connection was established
    if (!isServerSide() && signalingCLient.isOpen()) signalingCLient.close();

    if (sendThread.stopRequested) return;

    sendThread.requestStop();
    if (peerConnection != null) peerConnection.close();
  }

  @Override
  public String getError() {
    return lastError;
  }

  private class SendThread extends Thread {
    private boolean stopRequested = false;
    private ClientConnection connection;

    public SendThread(ClientConnection connection) {
      super("WebRTCClientConnection.SendThread_" + connection.getId());
      this.connection = connection;
    }

    public void requestStop() {
      this.stopRequested = true;
      synchronized (this) {
        this.notify();
      }
    }

    @Override
    public void run() {
      log.debug(prefix() + " sendthread started");
      try {
        while (!stopRequested && connection.isAlive()) {
          while (connection.hasMoreMessages()) {
            byte[] message = connection.nextMessage();
            if (message == null) {
              continue;
            }

            var length = message.length;
            var buffer = ByteBuffer.allocate(length + 4);

            buffer.put((byte) (length >> 24));
            buffer.put((byte) (length >> 16));
            buffer.put((byte) (length >> 8));
            buffer.put((byte) (length));
            buffer.put(message);

            localDataChannel.send(new RTCDataChannelBuffer(buffer, true));
            log.debug(prefix() + " sent " + (length + 4) + " bytes");
          }
          synchronized (this) {
            if (!stopRequested) {
              try {
                log.debug(prefix() + "sendthread -> sleep");
                this.wait();
                log.debug(prefix() + "sendthread -> woke up");
              } catch (InterruptedException e) {
                log.debug(prefix() + "sendthread -> interrupted");
              }
            }
          }
        }
      } catch (Exception e) {
        log.error(e.toString());
        fireDisconnect();
      }
      log.debug(prefix() + " sendthread ended");
    }
  }
}
