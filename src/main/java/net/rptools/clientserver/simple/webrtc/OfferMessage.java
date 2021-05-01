package net.rptools.clientserver.simple.webrtc;

import dev.onvoid.webrtc.RTCSessionDescription;

public class OfferMessage extends Message {
  public RTCSessionDescription offer;
  public String name;

  public OfferMessage() {
    type = "offer";
  }

}
