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

import org.apache.log4j.Logger;

import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;

public class ImageCacheStatusBar extends JLabel {
	private static final Logger log = Logger.getLogger(ImageCacheStatusBar.class);
	private static Icon imageCacheIcon;

	static {
		try {
			imageCacheIcon = new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/thumbnail-status.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public ImageCacheStatusBar() {
		setIcon(imageCacheIcon);
		setToolTipText(I18N.getString("ImageCacheStatusBar.toolTip"));
		update(AppUtil.getDiskSpaceUsed(AppUtil.getAppHome("imageThumbs")));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					log.info("Clearing imageThumbs cache...");
					MapTool.getThumbnailManager().clearImageThumbCache();
					update(AppUtil.getDiskSpaceUsed(AppUtil.getAppHome("imageThumbs")));
					MapTool.getFrame().getAppHomeDiskSpaceStatusBar().update();
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
}
