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

import net.rptools.maptool.model.Asset;

/**
 */
public class TransferableAsset implements Transferable {

	public static final DataFlavor dataFlavor = new DataFlavor(Asset.class, "Asset");

	private Asset asset;

	public TransferableAsset(Asset asset) {
		this.asset = asset;
	}

	public Asset getAsset() {
		return asset;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return asset;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { dataFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(dataFlavor);
	}
}
