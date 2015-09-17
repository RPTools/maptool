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

public class ClientCommand {

	public static enum COMMAND {
		startAssetTransfer, updateAssetTransfer, bootPlayer, setCampaign, putZone, removeZone, putAsset, getAsset, removeAsset, putToken, removeToken, draw, clearAllDrawings, setZoneGridSize, setZoneVisibility, playerConnected, playerDisconnected, message, undoDraw, showPointer, hidePointer, movePointer, startTokenMove, stopTokenMove, toggleTokenMoveWaypoint, updateTokenMove, enforceZoneView, setZoneHasFoW, exposeFoW, hideFoW, setFoW, putLabel, removeLabel, enforceZone, setServerPolicy, addTopology, removeTopology, renameZone, updateCampaign, updateInitiative, updateTokenInitiative, setUseVision, updateCampaignMacros, setTokenLocation, // NOTE: This is to support third party token placement and shouldn't be depended on for general purpose token movement
		setLiveTypingLabel, // Experimental chat notification
		enforceNotification, // enforces notification of typing in the chat window
		exposePCArea, setBoard, updateExposedAreaMeta
	};
}
