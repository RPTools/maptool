package net.rptools.clientserver.simple.webrtc;

public class LoginMessage extends Message {
  public String name;

  public LoginMessage()
  {
    type = "login";
  }
}
