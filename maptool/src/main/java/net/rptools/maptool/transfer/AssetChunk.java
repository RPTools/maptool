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

package net.rptools.maptool.transfer;

import java.io.Serializable;

public class AssetChunk implements Serializable {
	private Serializable id;
	private byte[] data;

	public AssetChunk(Serializable id, byte[] data) {
		this.id = id;
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public Serializable getId() {
		return id;
	}
}
