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
import net.rptools.clientserver.simple.webrtc.CandidateMessage;
import net.rptools.clientserver.simple.webrtc.LoginMessage;
import net.rptools.clientserver.simple.webrtc.Message;
import net.rptools.clientserver.simple.webrtc.OfferMessage;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.server.ServerConfig;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebRTCServerConnection extends AbstractServerConnection {
  private static final Logger log = Logger.getLogger(WebRTCServerConnection.class);

  private final PeerConnectionFactory factory = new PeerConnectionFactory();
  private final WebSocketClient signalingCLient;
  private final ServerConfig config;
  private final Gson gson = new Gson();
  private RTCConfiguration rtcConfig;

  public static String WebSocketUrl = "ws://mt-test2.azurewebsites.net";

  public WebRTCServerConnection(ServerConfig config, HandshakeProvider handshake) {
    super(handshake);
    this.config = config;
    URI uri = null;

    RTCIceServer iceServer = new RTCIceServer();
    iceServer.urls.add("stun:stun.l.google.com:19302");

    rtcConfig = new RTCConfiguration();
    rtcConfig.iceServers.add(iceServer);

    try {
      uri = new URI(WebSocketUrl);
    } catch (Exception e) {
    }

    signalingCLient =
        new WebSocketClient(uri) {
          @Override
          public void onOpen(ServerHandshake handshakedata) {
            log.info("Websocket connected\n");
            var msg = new LoginMessage();
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
            log.info("Websocket closed\n");
          }

          @Override
          public void onError(Exception ex) {
            log.error("Websocket error: " + ex.toString() + "\n");
          }
        };
    signalingCLient.connect();
  }

  private void handleSignalingMessage(String message) {
    var msg = gson.fromJson(message, Message.class);

    switch (msg.type) {
      case "login":
        {
          var loginMsg = gson.fromJson(message, LoginMessage.class);
          onLogin(loginMsg);
          break;
        }
      case "offer":
        {
          var offerMsg = gson.fromJson(message, OfferMessage.class);
          onOffer(offerMsg);
          break;
        }
        //      case "answer": //server side should not get answers
        //  onAnswer(data.getString("answer"));
        //        break;
      case "candidate":
        {
          var candiateMessage = gson.fromJson(message, CandidateMessage.class);
          onCandidate(candiateMessage);
          break;
        }
      default:
        log.error("unhandled signaling: " + msg.type);
    }
  }

  private void onLogin(LoginMessage msg) {
    if (!msg.success) {
      MapTool.showError("Servername already taken!");
      return;
    }
  }

  private Map<String, WebRTCClientConnection> openConnections = new HashMap<>();

  private void onOffer(OfferMessage message) {
    WebRTCClientConnection clientConnection = null;
    try {
      clientConnection = new WebRTCClientConnection(message, this);

      openConnections.put(message.source, clientConnection);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void onCandidate(CandidateMessage message) {
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
}
