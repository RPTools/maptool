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

package net.rptools.maptool.client.swing;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 */
public class TitleMenuItem extends JMenuItem {

	private String title;

	public TitleMenuItem(String title) {
		super(title);
		setEnabled(false);

		this.title = title;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {

		g.setColor(Color.darkGray);
		g.fillRect(0, 0, getSize().width, getSize().height);

		g.setColor(Color.white);
		FontMetrics fm = g.getFontMetrics();

		int x = (getSize().width - SwingUtilities.computeStringWidth(fm, title)) / 2;
		int y = (getSize().height - fm.getHeight()) / 2 + fm.getAscent();

		g.drawString(title, x, y);
	}
}
