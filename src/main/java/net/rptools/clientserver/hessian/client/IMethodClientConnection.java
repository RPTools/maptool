package net.rptools.clientserver.hessian.client;

import net.rptools.clientserver.simple.client.IClientConnection;

public interface IMethodClientConnection extends IClientConnection {
  void callMethod(String method, Object... parameters);
}
