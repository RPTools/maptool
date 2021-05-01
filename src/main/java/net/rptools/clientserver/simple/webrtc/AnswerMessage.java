package net.rptools.clientserver.simple.webrtc;

import dev.onvoid.webrtc.RTCSessionDescription;

public class AnswerMessage extends Message {
  public String name;
  public RTCSessionDescription answer;

  public AnswerMessage() {
    type = "answer";
  }

}
