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

package net.rptools.maptool.model;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.ImageManager;

public class ZoneFactory {

	public static final String DEFAULT_MAP_NAME = "Grasslands";
	public static MD5Key defaultImageId;

	static {
		// TODO: I really don't like this being hard wired this way, need to make it a preference or something
		File grassImage = new File(AppUtil.getAppHome("resource/Default/Textures").getAbsolutePath() + "/Grass.png");
		if (grassImage.exists()) {
			try {
				Asset asset = new Asset(DEFAULT_MAP_NAME, FileUtils.readFileToByteArray(grassImage));
				defaultImageId = asset.getId();

				// Make sure the image is loaded to avoid a flash screen when it becomes visible
				ImageManager.getImageAndWait(asset.getId());
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		}
	}

	public static Zone createZone() {

		Zone zone = new Zone();

		zone.setName(DEFAULT_MAP_NAME);
		zone.setBackgroundPaint(new DrawableTexturePaint(defaultImageId));
		zone.setFogPaint(new DrawableColorPaint(Color.black));

		zone.setVisible(AppPreferences.getNewMapsVisible());
		zone.setHasFog(AppPreferences.getNewMapsHaveFOW());
		zone.setUnitsPerCell(AppPreferences.getDefaultUnitsPerCell());
		zone.setTokenVisionDistance(AppPreferences.getDefaultVisionDistance());

		zone.setGrid(GridFactory.createGrid(AppPreferences.getDefaultGridType(), AppPreferences.getFaceEdge(), AppPreferences.getFaceVertex()));
		zone.setGridColor(AppPreferences.getDefaultGridColor().getRGB());
		zone.getGrid().setSize(AppPreferences.getDefaultGridSize());
		zone.getGrid().setOffset(0, 0);

		return zone;
	}

}
