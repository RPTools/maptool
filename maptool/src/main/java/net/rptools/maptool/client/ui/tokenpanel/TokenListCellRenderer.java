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

package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

public class TokenListCellRenderer extends DefaultListCellRenderer {

	private BufferedImage image;
	private String name;

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof Token) {
			Token token = (Token) value;
			image = ImageManager.getImage(token.getImageAssetId(), this);
			name = token.getName();

			setText(" "); // hack to keep the row height the right size
		}
		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		if (image != null) {

			Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
			SwingUtil.constrainTo(imageSize, getSize().height);
			g.drawImage(image, 0, 0, imageSize.width, imageSize.height, this);
			g.drawString(name, imageSize.width + 2, g.getFontMetrics().getAscent());
		}
	}
}
