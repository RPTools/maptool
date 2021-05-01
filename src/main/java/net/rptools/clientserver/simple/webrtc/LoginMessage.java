package net.rptools.clientserver.simple.webrtc;

public class LoginMessage extends Message {
  public boolean success;

  public LoginMessage()
  {
    type = "login";
  }
}
