package net.rptools.clientserver.simple.server;

import net.rptools.clientserver.simple.client.IClientConnection;

public interface IHandshake {
  boolean handleConnectionHandshake(IClientConnection conn);
}
