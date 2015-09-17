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

import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.maptool.model.CampaignFactory;

/**
 * This class handles when the server inexplicably disconnects
 */
public class ServerDisconnectHandler implements DisconnectHandler {
	// TODO: This is a temporary hack until I can come up with a cleaner mechanism
	public static boolean disconnectExpected;

	public void handleDisconnect(AbstractConnection arg0) {
		// Update internal state
		MapTool.disconnect();

		// TODO: attempt to reconnect if this was unexpected
		if (!disconnectExpected) {
			MapTool.showError("Server has disconnected.");

			// hide map so player doesn't get a brief GM view
			MapTool.getFrame().setCurrentZoneRenderer(null);

			try {
				MapTool.startPersonalServer(CampaignFactory.createBasicCampaign());
			} catch (IOException ioe) {
				MapTool.showError("Could not restart personal server");
			}
		} else if (!MapTool.isPersonalServer() && !MapTool.isHostingServer()) {
			// expected disconnect from someone else's server
			// hide map so player doesn't get a brief GM view
			MapTool.getFrame().setCurrentZoneRenderer(null);
		}
		disconnectExpected = false;
	}
}
