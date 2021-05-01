package net.rptools.clientserver.simple.client;

import net.rptools.clientserver.simple.IConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IClientConnection extends IConnection {
  DataInputStream getInputStream();
  DataOutputStream getOutputSream();
  void sendMessage(byte[] message);
  void sendMessage(Object channel, byte[] message);
  void start() throws IOException;
  boolean isAlive();
  String getId();
}
