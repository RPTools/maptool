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
import net.rptools.maptool.client.ui.MapToolDockListener;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;

public class AssetCacheStatusBar extends JLabel {
	private static final Logger log = Logger.getLogger(AssetCacheStatusBar.class);
	private static Icon assetCacheIcon;

	static {
		try {
			assetCacheIcon = new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/asset-status.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public AssetCacheStatusBar() {
		setIcon(assetCacheIcon);
		setToolTipText(I18N.getString("AssetCacheStatusBar.toolTip"));
		update(AppUtil.getDiskSpaceUsed(AppUtil.getAppHome("assetcache")));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					log.info("Clearing asset cache...");
					AssetManager.clearCache();
					update(AppUtil.getDiskSpaceUsed(AppUtil.getAppHome("assetcache")));
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
