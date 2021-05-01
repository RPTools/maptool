package net.rptools.clientserver.simple;

import net.rptools.clientserver.ActivityListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IConnection {
  void addMessageHandler(MessageHandler handler);
  void removeMessageHandler(MessageHandler handler);
  void addMessage(Object channel, byte[] message);
  boolean hasMoreMessages();
  byte[] nextMessage();
  void fireDisconnect();
  void addActivityListener(ActivityListener listener);
  void removeActivityListener(ActivityListener listener);
  void addDisconnectHandler(DisconnectHandler handler);
  void removeDisconnectHandler(DisconnectHandler handler);
  void close() throws IOException;
}
