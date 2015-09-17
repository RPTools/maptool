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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

public class TokenPanelTreeCellRenderer extends DefaultTreeCellRenderer {

	private BufferedImage image;
	private int row;
	private int rowWidth;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		setBorder(null);

		String text = "";
		this.row = row;

		setBackgroundNonSelectionColor(Color.white);
		if (value instanceof Token) {
			Token token = (Token) value;

			int height = getPreferredSize().height;
			if (height < 1) {
				height = 15;
			}
			if (image == null || image.getHeight() != height) {
				image = new BufferedImage(height, height, Transparency.BITMASK);
			} else {
				ImageUtil.clearImage(image);
			}

			// Make a thumbnail of the image
			// TODO: This could be cached somehow, right now it's quick enough though
			BufferedImage tokenImage = ImageManager.getImage(token.getImageAssetId(), this);
			Dimension dim = new Dimension(tokenImage.getWidth(), tokenImage.getHeight());
			SwingUtil.constrainTo(dim, height);

			Graphics g = image.getGraphics();
			// TODO: Center the image
			g.drawImage(tokenImage, 0, 0, dim.width, dim.height, this);
			g.dispose();

			text = token.getName();
			if (MapTool.getPlayer().isGM() && token.getGMName() != null && token.getGMName().length() > 0) {
				text += " (" + token.getGMName() + ")";
			}

			if (!token.isVisible()) {
				setBackgroundNonSelectionColor(Color.lightGray);
			}
		}
		if (value instanceof TokenPanelTreeModel.View) {
			TokenPanelTreeModel.View view = (TokenPanelTreeModel.View) value;

			text = view.getDisplayName();
		}

		super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);

		Icon icon = getIcon();
		rowWidth = (icon != null ? icon.getIconWidth() + 2 : 0) + SwingUtilities.computeStringWidth(getFontMetrics(getFont()), text);

		return this;
	}

	@Override
	public Dimension getPreferredSize() {
		int height = row > 0 ? getFontMetrics(getFont()).getHeight() + 4 : 0;
		return new Dimension(super.getPreferredSize().width, height);
	}

	@Override
	public Icon getLeafIcon() {
		return new ImageIcon(image);
	}
}
