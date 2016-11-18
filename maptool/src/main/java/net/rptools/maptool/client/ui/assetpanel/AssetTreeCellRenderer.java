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

package net.rptools.maptool.client.ui.assetpanel;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 */
public class AssetTreeCellRenderer extends DefaultTreeCellRenderer {
	// Jamz: Add PDF's as a "Leaf" and show the extracted images in the asset window...
	final private static Icon PDF_FOLDER = new ImageIcon(
			AssetTreeCellRenderer.class.getClassLoader().getResource("net/rptools/maptool/client/image/pdf_folder.png"));
	// Jamz: Add Hero Lab Portfolio's as a "Leaf" and show the extracted characters in the asset window...
	final private static Icon HERO_LAB_FOLDER = new ImageIcon(
			AssetTreeCellRenderer.class.getClassLoader().getResource("net/rptools/maptool/client/image/hero_lab_folder.png"));

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		setBorder(null);
		//System.out.println("expanded: " + expanded);
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof Directory) {
			setText(((Directory) value).getPath().getName());
			if (((Directory) value).isPDF()) {
				setIcon(PDF_FOLDER);
			}
			if (((Directory) value).isHeroLabPortfolio()) {
				setIcon(HERO_LAB_FOLDER);
			}
		}

		// Root node...
		if (row == 0) {
			setIcon(null);
		}

		return this;
	}

}
