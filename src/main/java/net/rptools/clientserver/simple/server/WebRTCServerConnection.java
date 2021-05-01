package net.rptools.clientserver.simple.server;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import net.rptools.clientserver.simple.webrtc.CandidateMessage;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.swing.*;
import java.net.URI;
import java.nio.charset.CharacterCodingException;

public class WebRTCServerConnection extends AbstractServerConnection implements PeerConnectionObserver {
  private static final Logger log = Logger.getLogger(WebRTCServerConnection.class);

  private final PeerConnectionFactory factory = new PeerConnectionFactory();
  private final WebSocketClient signalingCLient;
  private RTCPeerConnection peerConnection;

  public WebRTCServerConnection(IHandshake handshake) {
    super(handshake);
    URI uri = null;

    try {
      uri = new URI("ws://172.24.181.158:9090");
    } catch (Exception e) {}

    signalingCLient = new WebSocketClient(uri) {
      @Override
      public void onOpen(ServerHandshake handshakedata) {
        log.debug("Websocket connected\n");
      }

      @Override
      public void onMessage(String message) {
        log.debug("Got message: " + message + "\n");
        handleSignalingMessage(JSONObject.fromObject(message));
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        log.debug("Websocket connected\n");
      }

      @Override
      public void onError(Exception ex) {
        log.debug("Websocket error: " + ex.toString() + "\n");
      }
    };
  }

  private void handleSignalingMessage(JSONObject data) {
    var type = data.get("type").toString();
    switch (type) {
      case "login":
        // onLogin(data.getBoolean("success"));
        break;
      case "offer":
        //  onOffer(data.getString("offer"), data.getString("name"));
        break;
      case "answer":
        //  onAnswer(data.getString("answer"));
        break;
      case "candidate":
        //  onCandidate(data.getString("candidate"));
        break;
    }
  }

  private void onLogin(boolean success) {
    if(!success) {
      JOptionPane.showMessageDialog(null, "oops...try a different username");
      return;
    }

    RTCIceServer iceServer = new RTCIceServer();
    iceServer.urls.add("stun:stun.l.google.com:19302");

    RTCConfiguration config = new RTCConfiguration();
    config.iceServers.add(iceServer);

    peerConnection = factory.createPeerConnection(config, this);
  }

  @Override
  public void onSignalingChange(RTCSignalingState state) {
    log.debug("PeerConnection.onSignalingChange");
  }

  @Override
  public void onConnectionChange(RTCPeerConnectionState state) {
    log.debug("PeerConnection.onConnectionChange");
  }

  @Override
  public void onIceConnectionChange(RTCIceConnectionState state) {
    log.debug("PeerConnection.onIceConnectionChange");
  }

  @Override
  public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
    log.debug("PeerConnection.onStandardizedIceConnectionChange");
  }

  @Override
  public void onIceConnectionReceivingChange(boolean receiving) {
    log.debug("PeerConnection.onIceConnectionReceivingChange");
  }

  @Override
  public void onIceGatheringChange(RTCIceGatheringState state) {
    log.debug("PeerConnection.onIceGatheringChange");
  }

  @Override
  public void onIceCandidate(RTCIceCandidate candidate) {
/*    var msg = new CandidateMessage();
    msg.name = connectedUser;
    msg.candidate = candidate;
    send(gson.toJson(msg));*/
  }

  @Override
  public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
    log.debug("PeerConnection.onIceCandidateError");
  }

  @Override
  public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {
    log.debug("PeerConnection.onIceCandidatesRemoved");
  }

  @Override
  public void onAddStream(MediaStream stream) {
    log.debug("PeerConnection.onAddStream");
  }

  @Override
  public void onRemoveStream(MediaStream stream) {
    log.debug("PeerConnection.onRemoveStream");
  }

  @Override
  public void onDataChannel(RTCDataChannel dataChannel) {
    log.debug("PeerConnection.onDataChannel");
/*
    dataChannel.registerObserver(new RTCDataChannelObserver() {
      @Override
      public void onBufferedAmountChange(long previousAmount) {

      }

      @Override
      public void onStateChange() {

      }

      @Override
      public void onMessage(RTCDataChannelBuffer buffer) {
        try {
          output.append(decoder.decode(buffer.data) + "\n");
        } catch (CharacterCodingException e) {
          e.printStackTrace();
        }
      }
    });*/
  }

  @Override
  public void onRenegotiationNeeded() {
    log.debug("PeerConnection.onRenegotiationNeeded");
  }

  @Override
  public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
    log.debug("PeerConnection.onTrack(multiple Streams)");
  }

  @Override
  public void onRemoveTrack(RTCRtpReceiver receiver) {
    log.debug("PeerConnection.onRemoveTrack");
  }

  @Override
  public void onTrack(RTCRtpTransceiver transceiver) {
    log.debug("PeerConnection.onTrack");
  }
}
