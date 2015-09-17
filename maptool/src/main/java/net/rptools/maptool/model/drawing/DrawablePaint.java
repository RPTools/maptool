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

package net.rptools.maptool.model.drawing;

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.ImageObserver;
import java.io.Serializable;

import net.rptools.maptool.client.ui.AssetPaint;
import net.rptools.maptool.model.Asset;

public abstract class DrawablePaint implements Serializable {
	public abstract Paint getPaint(ImageObserver... observers);

	public abstract Paint getPaint(int offsetX, int offsetY, double scale, ImageObserver... observers);

	public static DrawablePaint convertPaint(Paint paint) {
		if (paint == null) {
			return null;
		}
		if (paint instanceof Color) {
			return new DrawableColorPaint((Color) paint);
		}
		if (paint instanceof AssetPaint) {
			Asset asset = ((AssetPaint) paint).getAsset();
			return new DrawableTexturePaint(asset);
		}
		throw new IllegalArgumentException("Invalid type of paint: " + paint.getClass().getName());
	}
}
