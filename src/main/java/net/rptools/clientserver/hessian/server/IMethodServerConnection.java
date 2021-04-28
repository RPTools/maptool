package net.rptools.clientserver.hessian.server;

import net.rptools.clientserver.simple.IConnection;
import net.rptools.clientserver.simple.server.IServerConnection;

public interface IMethodServerConnection extends IServerConnection {
  void broadcastCallMethod(String method, Object... parameters);
  void broadcastCallMethod(String[] exclude, String method, Object... parameters);
  void callMethod(String id, String method, Object... parameters);
  void callMethod(String id, Object channel, String method, Object... parameters);
}
