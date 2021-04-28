package net.rptools.clientserver.simple.server;

import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.IConnection;
import net.rptools.clientserver.simple.MessageHandler;

public interface IServerConnection extends IConnection  {
  void handleDisconnect(AbstractConnection conn);
  void handleMessage(String id, byte[] message);
  void addObserver(ServerObserver observer);
  void removeObserver(ServerObserver observer);
  void broadcastMessage(byte[] message);
  void broadcastMessage(String[] exclude, byte[] message);
  void sendMessage(String id, byte[] message);
  void sendMessage(String id, Object channel, byte[] message);
}
