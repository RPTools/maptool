/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client;

import java.io.IOException;
import java.net.Socket;

import net.rptools.clientserver.hessian.client.ClientConnection;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.server.Handshake;

/**
 * @author trevor
 */
public class MapToolConnection extends ClientConnection {
	private final Player player;

	public MapToolConnection(String host, int port, Player player) throws IOException {
		super(host, port, null);
		this.player = player;
	}

	public MapToolConnection(Socket socket, Player player) throws IOException {
		super(socket, null);
		this.player = player;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rptools.clientserver.simple.client.ClientConnection#sendHandshake(java.net.Socket)
	 */
	@Override
	public boolean sendHandshake(Socket s) throws IOException {
		Handshake.Response response = Handshake.sendHandshake(new Handshake.Request(player.getName(), player.getPassword(), player.getRole(), MapTool.getVersion()), s);

		if (response.code != Handshake.Code.OK) {
			MapTool.showError("ERROR: " + response.message);
			return false;
		}
		boolean result = response.code == Handshake.Code.OK;
		if (result) {
			MapTool.setServerPolicy(response.policy);
		}
		return result;
	}
}
