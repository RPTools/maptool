package net.rptools.clientserver.simple.webrtc;

import dev.onvoid.webrtc.RTCSessionDescription;

public class OfferMessage extends Message {
  public RTCSessionDescription offer;

  public OfferMessage() {
    type = "offer";
  }

}
