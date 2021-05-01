package net.rptools.clientserver.simple.webrtc;

import dev.onvoid.webrtc.RTCIceCandidate;

public class CandidateMessage extends Message {
  public RTCIceCandidate candidate;
  public String name;

  public CandidateMessage() {
    type = "candidate";
  }

}
