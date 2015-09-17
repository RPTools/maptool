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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;

/**
 */
public class TransferableAssetReference implements Transferable {

	public static final DataFlavor dataFlavor = new DataFlavor(GUID.class, "AssetReference");

	private MD5Key assetID;

	public TransferableAssetReference(Asset asset) {
		this.assetID = asset.getId();
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return assetID;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { dataFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(dataFlavor);
	}
}
