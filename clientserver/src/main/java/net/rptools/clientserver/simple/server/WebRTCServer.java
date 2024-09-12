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
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import net.rptools.clientserver.simple.connection.WebRTCConnection;
import net.rptools.clientserver.simple.webrtc.CandidateMessageDto;
import net.rptools.clientserver.simple.webrtc.LoginMessageDto;
import net.rptools.clientserver.simple.webrtc.MessageDto;
import net.rptools.clientserver.simple.webrtc.OfferMessageDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebRTCServer extends AbstractServer {
  private static final Logger log = LogManager.getLogger(WebRTCServer.class);

  public interface Listener {
    void onLoginError();

    void onUnexpectedClose();
  }

  private final Listener listener;
  private WebSocketClient signalingClient;
  private final String serverName;
  private final Gson gson = new Gson();
  private String lastError = null;
  private URI webSocketUri = null;
  private int reconnectCounter = -1;
  private Thread reconnectThread;
  private final Map<String, WebRTCConnection> openConnections = new HashMap<>();

  public static String WebSocketUrl = "ws://webrtc1.rptools.net:8080";
  private boolean disconnectExpected;

  public WebRTCServer(String serverName, Listener listener) {
    this.listener = listener;
    this.serverName = serverName;

    try {
      webSocketUri = new URI(WebSocketUrl);
    } catch (Exception e) {
    }

    signalingClient = createSignalingClient();
  }

  private void sendSignalingMessage(String message) {
    log.debug("S sent signaling message: " + message);
    signalingClient.send(message);
  }

  private WebSocketClient createSignalingClient() {
    return new WebSocketClient(webSocketUri) {
      @Override
      public void onOpen(ServerHandshake handshakeData) {
        reconnectCounter = 30;
        log.info("S WebSocket connected\n");
        var msg = new LoginMessageDto();
        msg.source = serverName;

        sendSignalingMessage(gson.toJson(msg));
      }

      @Override
      public void onMessage(String message) {
        handleSignalingMessage(message);
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        lastError = "WebSocket closed: remote:" + remote + " (" + code + ") " + reason;
        log.info("S " + lastError);
        if (disconnectExpected) {
          return;
        }

        // if the connection get closed remotely the rptools.net server disconnected. Try to
        // reconnect.
        if (reconnectCounter > 0) {
          retryConnect();
        } else {
          listener.onUnexpectedClose();
        }
      }

      @Override
      public void onError(Exception ex) {
        lastError = "WebSocket error: " + ex.toString() + "\n";
        log.error("S " + lastError);
        // onClose will be called after this method
      }
    };
  }

  private void handleSignalingMessage(String message) {
    log.debug("S got signaling message: " + message);
    var msg = gson.fromJson(message, MessageDto.class);

    switch (msg.type) {
      case "login" -> {
        var loginMsg = gson.fromJson(message, LoginMessageDto.class);
        if (!loginMsg.success) {
          listener.onLoginError();
        }
      }
      case "offer" -> {
        var offerMsg = gson.fromJson(message, OfferMessageDto.class);
        var clientConnection = new WebRTCConnection(offerMsg, this);
        openConnections.put(offerMsg.source, clientConnection);
      }
      case "candidate" -> {
        var candidateMessage = gson.fromJson(message, CandidateMessageDto.class);
        openConnections.get(candidateMessage.source).addIceCandidate(candidateMessage.candidate);
      }
      default -> log.error("S unhandled signaling: " + msg.type);
    }
  }

  public WebSocketClient getSignalingClient() {
    return signalingClient;
  }

  public String getName() {
    return serverName;
  }

  public void onDataChannelOpened(WebRTCConnection connection) {
    try {
      fireClientConnect(connection);
    } catch (Exception e) {
      log.error(e);
    }
  }

  public String getError() {
    return lastError;
  }

  @Override
  public void start() throws IOException {
    signalingClient.connect();
  }

  @Override
  public void close() {
    disconnectExpected = true;
    reconnectCounter = -1;
    signalingClient.close();
  }

  void retryConnect() {
    reconnectThread =
        new Thread(
            () -> {
              log.info(
                  "S Trying to reconnect to signaling server after "
                      + reconnectCounter
                      + " seconds.");
              try {
                Thread.sleep(reconnectCounter * 1000);
              } catch (InterruptedException e) {
              }
              reconnectCounter *= 2;
              signalingClient = createSignalingClient();
              signalingClient.connect();
            });
    reconnectThread.setName("WebRTCServer.reconnectThread");
    reconnectThread.start();
  }
}
