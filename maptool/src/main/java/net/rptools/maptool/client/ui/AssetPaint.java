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

package net.rptools.maptool.client.ui;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import net.rptools.maptool.model.Asset;
import net.rptools.maptool.util.ImageManager;

public class AssetPaint extends TexturePaint {

	private Asset asset;

	public AssetPaint(Asset asset) {
		this(ImageManager.getImageAndWait(asset.getId()));
		this.asset = asset;
	}

	// Only used to avoid a bunch of calls to getImageAndWait() that the compiler may
	// not be able to optimize (method calls may not be optimizable when side effects
	// of the method are not known to the compiler).
	private AssetPaint(BufferedImage img) {
		super(img, new Rectangle2D.Float(0, 0, img.getWidth(), img.getHeight()));
	}

	public Asset getAsset() {
		return asset;
	}
}
