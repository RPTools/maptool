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

package net.rptools.maptool.client.ui.token;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

/**
 * Token overlay for bar meters.
 * 
 * @author Jay
 */
public class SingleImageBarTokenOverlay extends BarTokenOverlay {

	/**
	 * ID of the bar image displayed in the overlay.
	 */
	private MD5Key assetId;

	/**
	 * Needed for serialization
	 */
	public SingleImageBarTokenOverlay() {
		this(AbstractTokenOverlay.DEFAULT_STATE_NAME, null);
	}

	/**
	 * Create the complete image overlay.
	 * 
	 * @param name Name of the new token overlay
	 * @param theAssetId Id of the bar image
	 */
	public SingleImageBarTokenOverlay(String name, MD5Key theAssetId) {
		super(name);
		assetId = theAssetId;
	}

	/**
	 * @see net.rptools.maptool.client.ui.token.AbstractTokenOverlay#clone()
	 */
	@Override
	public Object clone() {
		BarTokenOverlay overlay = new SingleImageBarTokenOverlay(getName(), assetId);
		overlay.setOrder(getOrder());
		overlay.setGroup(getGroup());
		overlay.setMouseover(isMouseover());
		overlay.setOpacity(getOpacity());
		overlay.setIncrements(getIncrements());
		overlay.setSide(getSide());
		overlay.setShowGM(isShowGM());
		overlay.setShowOwner(isShowOwner());
		overlay.setShowOthers(isShowOthers());
		return overlay;
	}

	/**
	 * @see net.rptools.maptool.client.ui.token.BarTokenOverlay#paintOverlay(java.awt.Graphics2D, net.rptools.maptool.model.Token, java.awt.Rectangle, double)
	 */
	@Override
	public void paintOverlay(Graphics2D g, Token token, Rectangle bounds, double value) {

		// Get the images
		BufferedImage image = ImageManager.getImageAndWait(assetId);

		Dimension d = bounds.getSize();
		Dimension size = new Dimension(image.getWidth(), image.getHeight());
		SwingUtil.constrainTo(size, d.width, d.height);

		// Find the position of the images according to the size and side where they are placed
		int x = 0;
		int y = 0;
		switch (getSide()) {
		case RIGHT:
			x = d.width - size.width;
			break;
		case BOTTOM:
			y = d.height - size.height;
		}

		Composite tempComposite = g.getComposite();
		if (getOpacity() != 100) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
		}
		int width = (getSide() == Side.TOP || getSide() == Side.BOTTOM) ? calcBarSize(image.getWidth(), value) : image.getWidth();
		int height = (getSide() == Side.LEFT || getSide() == Side.RIGHT) ? calcBarSize(image.getHeight(), value) : image.getHeight();

		int screenWidth = (getSide() == Side.TOP || getSide() == Side.BOTTOM) ? calcBarSize(size.width, value) : size.width;
		int screenHeight = (getSide() == Side.LEFT || getSide() == Side.RIGHT) ? calcBarSize(size.height, value) : size.height;

		g.drawImage(image, x + size.width - screenWidth, y + size.height - screenHeight, x + size.width, y + size.height, image.getWidth() - width, image.getHeight() - height, image.getWidth(),
				image.getHeight(), null);
		g.setComposite(tempComposite);
	}

	/** @return Getter for assetId */
	public MD5Key getAssetId() {
		return assetId;
	}

	/** @param topAssetId Setter for assetId */
	public void setAssetId(MD5Key topAssetId) {
		this.assetId = topAssetId;
	}
}
