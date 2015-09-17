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

public class AssetHeader implements Serializable {
	private Serializable id;
	private String name;
	private long size;

	public AssetHeader(Serializable id, String name, long size) {
		this.id = id;
		this.size = size;
		this.name = name;
	}

	public Serializable getId() {
		return id;
	}

	public long getSize() {
		return size;
	}

	public String getName() {
		return name;
	}
}
