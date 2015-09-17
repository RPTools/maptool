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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.transfer.ConsumerListener;

import org.apache.commons.io.FileUtils;

/**
 * Handles incoming segmented assets
 * 
 * @author trevor
 */
public class AssetTransferHandler implements ConsumerListener {
	public void assetComplete(Serializable id, String name, File data) {
		byte[] assetData = null;
		try {
			assetData = FileUtils.readFileToByteArray(data);
		} catch (IOException ioe) {
			MapTool.showError("Error loading composed asset file: " + id);
			return;
		}
		Asset asset = new Asset(name, assetData);
		if (!asset.getId().equals(id)) {
			MapTool.showError("Received an invalid image: " + id);
			return;
		}
		// Install it into our system
		AssetManager.putAsset(asset);

		// Remove the temp file
		data.delete();
		MapTool.getFrame().refresh();
	}

	public void assetUpdated(Serializable id) {
		// Nothing to do
	}

	public void assetAdded(Serializable id) {
		// Nothing to do
	}
}
