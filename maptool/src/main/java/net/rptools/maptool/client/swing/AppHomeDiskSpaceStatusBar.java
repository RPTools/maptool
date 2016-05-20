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

import java.awt.event.MouseAdapter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.language.I18N;

public class AppHomeDiskSpaceStatusBar extends JLabel {

	private static Icon diskSpaceIcon;

	static {
		try {
			diskSpaceIcon = new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/disk-space.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public AppHomeDiskSpaceStatusBar() {
		setIcon(diskSpaceIcon);
		setToolTipText(I18N.getString("AppHomeDiskSpaceStatusBar.toolTip"));
		update(AppUtil.getFreeDiskSpace(AppUtil.getAppHome()));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					update(AppUtil.getFreeDiskSpace(AppUtil.getAppHome()));
				}
			}
		});
	}

	public void clear() {
		setText("");
	}

	public void update(String diskUsed) {
		setText(diskUsed);
	}

	public void update() {
		update(AppUtil.getFreeDiskSpace(AppUtil.getAppHome()));
	}
}
