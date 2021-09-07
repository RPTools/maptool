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
package net.rptools.clientserver.simple.server;

import com.google.gson.Gson;
import dev.onvoid.webrtc.*;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import net.rptools.clientserver.simple.client.WebRTCClientConnection;
import net.rptools.clientserver.simple.webrtc.CandidateMessageDto;
import net.rptools.clientserver.simple.webrtc.LoginMessageDto;
import net.rptools.clientserver.simple.webrtc.MessageDto;
import net.rptools.clientserver.simple.webrtc.OfferMessageDto;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.server.ServerConfig;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebRTCServerConnection extends AbstractServerConnection {
  private static final Logger log = Logger.getLogger(WebRTCServerConnection.class);

  private WebSocketClient signalingCLient;
  private final ServerConfig config;
  private final Gson gson = new Gson();
  private RTCConfiguration rtcConfig;
  private String lastError = null;
  private URI websocketUri = null;
  private int reconnectCounter = -1;
  private Thread reconnectThread;
  private Map<String, WebRTCClientConnection> openConnections = new HashMap<>();

  // public static String WebSocketUrl = "ws://172.31.222.156:8080";
  public static String WebSocketUrl = "ws://webrtc1.rptools.net:8080";
  private boolean disconnectExpected;

  public WebRTCServerConnection(ServerConfig config, HandshakeProvider handshake) {
    super(handshake);
    this.config = config;

    RTCIceServer iceServer = new RTCIceServer();
    iceServer.urls.add("stun:stun.l.google.com:19302");

    rtcConfig = new RTCConfiguration();
    rtcConfig.iceServers.add(iceServer);

    try {
      websocketUri = new URI(WebSocketUrl);
    } catch (Exception e) {
    }

    signalingCLient = createSignalingClient();
  }

  private WebSocketClient createSignalingClient() {
    return new WebSocketClient(websocketUri) {
      @Override
      public void onOpen(ServerHandshake handshakedata) {
        reconnectCounter = 30;
        log.info("Websocket connected\n");
        var msg = new LoginMessageDto();
        msg.source = config.getServerName();

        send(gson.toJson(msg));
      }

      @Override
      public void onMessage(String message) {
        // log.info("Got message: " + message + "\n");
        handleSignalingMessage(message);
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        lastError = "Websocket closed: remote:" + remote + " (" + code + ") " + reason;
        log.info(lastError);
        if (disconnectExpected) return;

        // if the connection get closed remotely the rptools.net server disconnected. Try to
        // reconnect.
        if (reconnectCounter > 0) retryConnect();
        else MapTool.stopServer();
      }

      @Override
      public void onError(Exception ex) {
        lastError = "Websocket error: " + ex.toString() + "\n";
        log.error(lastError);
        // onClose will be called after this method
      }
    };
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
      case "offer":
        {
          var offerMsg = gson.fromJson(message, OfferMessageDto.class);
          onOffer(offerMsg);
          break;
        }
        //      case "answer": //server side should not get answers
        //  onAnswer(data.getString("answer"));
        //        break;
      case "candidate":
        {
          var candiateMessage = gson.fromJson(message, CandidateMessageDto.class);
          onCandidate(candiateMessage);
          break;
        }
      default:
        log.error("unhandled signaling: " + msg.type);
    }
  }

  private void onLogin(LoginMessageDto msg) {
    if (!msg.success) {
      MapTool.showError("Servername already taken!");
      return;
    }
  }

  private void onOffer(OfferMessageDto message) {
    WebRTCClientConnection clientConnection = null;
    try {
      clientConnection = new WebRTCClientConnection(message, this);

      openConnections.put(message.source, clientConnection);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void onCandidate(CandidateMessageDto message) {
    openConnections.get(message.source).addIceCandidate(message.candidate);
  }

  public WebSocketClient getSignalingCLient() {
    return signalingCLient;
  }

  public ServerConfig getConfig() {
    return config;
  }

  public void onDataChannelOpened(WebRTCClientConnection connection) {
    try {
      handleConnection(connection);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getError() {
    return lastError;
  }

  @Override
  public void open() throws IOException {
    signalingCLient.connect();
  }

  @Override
  public void close() {
    super.close();
    disconnectExpected = true;
    reconnectCounter = -1;
    signalingCLient.close();
  }

  void retryConnect() {
    reconnectThread =
        new Thread(
            () -> {
              log.info(
                  "Trying to reconnect to signaling server after "
                      + reconnectCounter
                      + " seconds.");
              try {
                Thread.sleep(reconnectCounter * 1000);
              } catch (InterruptedException e) {
              }
              reconnectCounter *= 2;
              signalingCLient = createSignalingClient();
              signalingCLient.connect();
            });
    reconnectThread.setName("WebRTCServerConnection.reconnectThread");
    reconnectThread.start();
  }

  public void clearClients() {
    reapClients();
  }
}
