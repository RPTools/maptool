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
import java.io.*;
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
    implements IClientConnection,
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

  private PipedOutputStream received;
  private PipedInputStream is;
  private DataInputStream dis;

  private PipedInputStream toSend;
  private PipedOutputStream os;
  private DataOutputStream dos;

  private boolean started = false;
  private StreamSendThread sendThread;
  private Thread handleConnnect;

  // used from client side
  public WebRTCClientConnection(String id, ServerConfig config) throws IOException {
    this.id = id;
    this.config = config;
    init();

    startSignaling();
  }

  // this is used from the server side
  public WebRTCClientConnection(OfferMessage message, WebRTCServerConnection webRTCServerConnection)
      throws IOException {
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
            var msg = new LoginMessage();
            msg.source = getId();
            send(gson.toJson(msg));
          }

          @Override
          public void onMessage(String message) {
            // log.info("Got message: " + message + "\n");
            handleSignalingMessage(message);
          }

          @Override
          public void onClose(int code, String reason, boolean remote) {
            log.info("Websocket connected\n");
          }

          @Override
          public void onError(Exception ex) {
            log.info("Websocket error: " + ex.toString() + "\n");
          }
        };
    signalingCLient.connect();
  }

  private void init() throws IOException {
    RTCIceServer iceServer = new RTCIceServer();
    iceServer.urls.add("stun:stun.l.google.com:19302");

    rtcConfig = new RTCConfiguration();
    rtcConfig.iceServers.add(iceServer);

    received = new PipedOutputStream();
    is = new PipedInputStream(received);
    dis = new DataInputStream(is);

    os = new PipedOutputStream();
    dos = new DataOutputStream(os);

    toSend = new PipedInputStream(os);
    sendThread = new StreamSendThread(this);
    sendThread.start();
  }

  @Override
  public void sendMessage(byte[] message) {
    sendMessage(null, message);
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    addMessage(channel, message);
    synchronized (sendThread) {
      sendThread.notify();
    }
  }

  //  @Override
  public void start() {
    started = true;
  }

  @Override
  public boolean isAlive() {
    return localDataChannel.getState() == RTCDataChannelState.OPEN;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void open() throws IOException {}

  private void handleSignalingMessage(String message) {
    var msg = gson.fromJson(message, Message.class);
    switch (msg.type) {
      case "login":
        {
          var loginMsg = gson.fromJson(message, LoginMessage.class);
          onLogin(loginMsg);
          break;
        }
        //     case "offer": // client side should not get offers
        //  onOffer(data.getString("offer"), data.getString("name"));
        //      break;
      case "answer":
        {
          var answerMessage = gson.fromJson(message, AnswerMessage.class);
          onAnswer(answerMessage);
          break;
        }
      case "candidate":
        {
          var candiateMessage = gson.fromJson(message, CandidateMessage.class);
          onIceCandidate(candiateMessage.candidate);
          break;
        }
      default:
        log.error("unhandled signaling: " + msg.type);
    }
  }

  private void onAnswer(AnswerMessage answerMessage) {
    peerConnection.setRemoteDescription(answerMessage.answer, this);
  }

  private void onLogin(LoginMessage message) {
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
    return serverConnection == null ? "C " : "S ";
  }

  @Override
  public void onSignalingChange(RTCSignalingState state) {
    log.info(prefix() + "PeerConnection.onSignalingChange: " + state);
  }

  @Override
  public void onConnectionChange(RTCPeerConnectionState state) {
    log.info(prefix() + "PeerConnection.onConnectionChange " + state);
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
    var msg = new CandidateMessage();

    if (serverConnection == null) {
      msg.destination = config.getServerName();
      msg.source = getId();
    } else {
      msg.source = config.getServerName();
      msg.destination = getId();
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

    if (serverConnection != null) {
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

    if (serverConnection != null) {
      var msg = new AnswerMessage();
      msg.source = serverConnection.getConfig().getServerName();
      msg.destination = getId();
      msg.answer = description;
      signalingCLient.send(gson.toJson(msg));
    } else {
      var msg = new OfferMessage();
      msg.offer = description;
      msg.source = getId();
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
  }

  // datachannel
  @Override
  public void onMessage(RTCDataChannelBuffer channelBuffer) {
    // log.info(prefix() + "localDataChannel onMessage: got " + buffer.data.capacity() + " bytes" );
    var buffer = channelBuffer.data;

    if (Thread.currentThread().getContextClassLoader() == null) {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
    }

    int len = buffer.capacity();

    var byteArray = new byte[len];
    buffer.get(byteArray);
    synchronized (this) {
      if (!started) {
        try {
          received.write(byteArray);
          return;
        } catch (Exception e) {
          log.error(e.toString());
        }
      }
    }

    int b32 = byteArray[0] & 0xff;
    int b24 = byteArray[1] & 0xff;
    int b16 = byteArray[2] & 0xff;
    int b8 = byteArray[3] & 0xff;

    int length = (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;

    if (length != len - 4) {
      log.info("size differs");
    }

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

  @Override
  public void close() {
    if (sendThread.stopRequested) {
      return;
    }
    sendThread.requestStop();
    peerConnection.close();
  }

  private class StreamSendThread extends Thread {
    private boolean stopRequested = false;
    private IClientConnection connection;

    public StreamSendThread(IClientConnection connection) {
      super("StreamSendThread_" + connection.getId());
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
      while (!stopRequested) {
        try {
          if (localDataChannel == null
              || localDataChannel.getState() != RTCDataChannelState.OPEN
              || (toSend.available() == 0 && !started)) Thread.sleep(100);
          else if (!started || toSend.available() > 0) {

            var bytes = toSend.readNBytes(toSend.available());
            var buffer = ByteBuffer.wrap(bytes);

            localDataChannel.send(new RTCDataChannelBuffer(buffer, true));
            log.info("sent " + bytes.length + " bytes");
          } else {
            try {
              while (!stopRequested && connection.isAlive()) {
                try {
                  while (connection.hasMoreMessages()) {
                    try {
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
                    } catch (IndexOutOfBoundsException e) {
                      // just ignore and wait
                    }
                  }
                  synchronized (this) {
                    if (!stopRequested) {
                      this.wait();
                    }
                  }
                } catch (InterruptedException e) {
                  // do nothing
                }
              }
            } catch (IOException e) {
              fireDisconnect();
            }
          }
        } catch (Exception e) {
          System.out.println(e.toString());
          e.printStackTrace();
          log.error(e.toString());
        }
      }
    }
  }
}
