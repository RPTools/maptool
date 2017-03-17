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
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.language.I18N;

public class AppHomeDiskSpaceStatusBar extends JLabel {
	private static final long serialVersionUID = 3149155977860280954L;
	private static final Logger LOGGER = Logger.getLogger(AppHomeDiskSpaceStatusBar.class);
	private static final File CACHE_DIR = AppUtil.getAppHome();
	private static final long POLLING_INTERVAL = 10000;
	private static long lastChecked = 0;
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
		update();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					update();
				}
			}
		});

		try {
			FileAlterationObserver observer = new FileAlterationObserver(CACHE_DIR);
			FileAlterationMonitor monitor = new FileAlterationMonitor(POLLING_INTERVAL);
			FileAlterationListener listener = new FileAlterationListenerAdaptor() {
				// Is triggered when a file is created in the monitored folder
				@Override
				public void onFileCreate(File file) {
					update();
				}

				// Is triggered when a file is deleted from the monitored folder
				@Override
				public void onFileDelete(File file) {
					update();
				}
			};

			observer.addListener(listener);
			monitor.addObserver(observer);
			monitor.start();
		} catch (Exception e) {
			LOGGER.error("Unable to register file change listener for " + CACHE_DIR.getAbsolutePath());
		}
	}

	public void clear() {
		setText("");
	}

	public void update() {
		// Only update once per polling interval as event will fire for every file created/deleted since last interval
		if (System.currentTimeMillis() - lastChecked >= POLLING_INTERVAL) {
			setText(AppUtil.getFreeDiskSpace(CACHE_DIR));
			lastChecked = System.currentTimeMillis();
			LOGGER.info("AppHomeDiskSpaceStatusBar updated...");
		}
	}
}
