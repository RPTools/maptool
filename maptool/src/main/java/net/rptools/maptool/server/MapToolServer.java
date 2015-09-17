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

package net.rptools.maptool.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.client.ClientCommand;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.transfer.AssetChunk;
import net.rptools.maptool.transfer.AssetProducer;
import net.rptools.maptool.transfer.AssetTransferManager;

import org.apache.log4j.Logger;

/**
 * @author drice
 */
public class MapToolServer {
	private static final Logger log = Logger.getLogger(MapToolServer.class);
	private static final int ASSET_CHUNK_SIZE = 5 * 1024;

	private final MapToolServerConnection conn;
	private final ServerMethodHandler handler;
	private final ServerConfig config;

	private final Map<String, AssetTransferManager> assetManagerMap = Collections.synchronizedMap(new HashMap<String, AssetTransferManager>());
	private final Map<String, ClientConnection> connectionMap = Collections.synchronizedMap(new HashMap<String, ClientConnection>());
	private final AssetProducerThread assetProducerThread;

	private Campaign campaign;
	private ServerPolicy policy;
	private HeartbeatThread heartbeatThread;

	public MapToolServer(ServerConfig config, ServerPolicy policy) throws IOException {
		handler = new ServerMethodHandler(this);
		conn = new MapToolServerConnection(this, config.getPort());
		conn.addMessageHandler(handler);

		campaign = new Campaign();

		assetProducerThread = new AssetProducerThread();
		assetProducerThread.start();

		this.config = config;
		this.policy = policy;

		// Start a heartbeat if requested
		if (config.isServerRegistered()) {
			heartbeatThread = new HeartbeatThread();
			heartbeatThread.start();
		}
	}

	public void configureClientConnection(ClientConnection connection) {
		String id = connection.getId();
		assetManagerMap.put(id, new AssetTransferManager());
		connectionMap.put(id, connection);
	}

	public ClientConnection getClientConnection(String id) {
		return connectionMap.get(id);
	}

	public String getConnectionId(String playerId) {
		return conn.getConnectionId(playerId);
	}

	/**
	 * Forceably disconnects a client and cleans up references to it
	 * 
	 * @param id
	 *            the connection ID
	 */
	public void releaseClientConnection(String id) {
		ClientConnection connection = getClientConnection(id);
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				log.error("Could not release connection: " + id, e);
			}
		}
		assetManagerMap.remove(id);
		connectionMap.remove(id);
	}

	public void addAssetProducer(String connectionId, AssetProducer producer) {
		AssetTransferManager manager = assetManagerMap.get(connectionId);
		manager.addProducer(producer);
	}

	public void addObserver(ServerObserver observer) {
		if (observer != null) {
			conn.addObserver(observer);
		}
	}

	public void removeObserver(ServerObserver observer) {
		conn.removeObserver(observer);
	}

	public boolean isHostId(String playerId) {
		return config.getHostPlayerId() != null && config.getHostPlayerId().equals(playerId);
	}

	public MapToolServerConnection getConnection() {
		return conn;
	}

	public boolean isPlayerConnected(String id) {
		return conn.getPlayer(id) != null;
	}

	public void setCampaign(Campaign campaign) {
		// Don't allow null campaigns, but allow the campaign to be cleared out
		if (campaign == null) {
			campaign = new Campaign();
		}
		this.campaign = campaign;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public ServerPolicy getPolicy() {
		return policy;
	}

	public void updateServerPolicy(ServerPolicy policy) {
		this.policy = policy;
	}

	public ServerMethodHandler getMethodHandler() {
		return handler;
	}

	public ServerConfig getConfig() {
		return config;
	}

	public void stop() {
		try {
			conn.close();
			if (heartbeatThread != null) {
				heartbeatThread.shutdown();
			}
			if (assetProducerThread != null) {
				assetProducerThread.shutdown();
			}
		} catch (IOException e) {
			// Not too concerned about this
			e.printStackTrace();
		}
	}

	private static final Random random = new Random();

	private class HeartbeatThread extends Thread {
		private boolean stop = false;
		private static final int HEARTBEAT_DELAY = 7 * 60 * 1000; // 7 minutes
		private static final int HEARTBEAT_FLUX = 20 * 1000; // 20 seconds

		@Override
		public void run() {
			while (!stop) {
				try {
					Thread.sleep(HEARTBEAT_DELAY + (int) (HEARTBEAT_FLUX * random.nextFloat()));
				} catch (InterruptedException ie) {
					// This means stop
					break;
				}
				// Pulse
				MapToolRegistry.heartBeat(config.getPort());
			}
		}

		public void shutdown() {
			stop = true;
			interrupt();
		}
	}

	////
	// CLASSES
	private class AssetProducerThread extends Thread {
		private boolean stop = false;

		@Override
		public void run() {
			while (!stop) {
				try {
					boolean lookForMore = false;
					for (Entry<String, AssetTransferManager> entry : assetManagerMap.entrySet()) {
						AssetChunk chunk = entry.getValue().nextChunk(ASSET_CHUNK_SIZE);
						if (chunk != null) {
							lookForMore = true;
							getConnection().callMethod(entry.getKey(), MapToolConstants.Channel.IMAGE, ClientCommand.COMMAND.updateAssetTransfer.name(), chunk);
						}
					}
					if (lookForMore) {
						continue;
					}
					// Sleep for a bit
					synchronized (this) {
						Thread.sleep(500);
					}
				} catch (Exception e) {
					e.printStackTrace();
					// keep on going
				}
			}
		}

		public void shutdown() {
			stop = true;
		}
	}

	////
	// STANDALONE SERVER
	public static void main(String[] args) throws IOException {
		// This starts the server thread.
		MapToolServer server = new MapToolServer(new ServerConfig(), new ServerPolicy());
	}
}
