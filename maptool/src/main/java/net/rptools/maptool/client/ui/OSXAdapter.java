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

/*
 * File: OSXAdapter.java
 *
 * Abstract: Hooks existing preferences/about/quit functionality from an
 * existing Java app into handlers for the Mac OS X application menu. Uses a
 * Proxy object to dynamically implement the com.apple.eawt.ApplicationListener
 * interface and register it with the com.apple.eawt.Application object. This
 * allows the complete project to be both built and run on any platform without
 * any stubs or placeholders. Useful for developers looking to implement Mac OS
 * X features while supporting multiple platforms with minimal impact.
 *
 * (Now replaced with called to {@link java.awt.Desktop}, which is Java 1.6+)
 */

package net.rptools.maptool.client.ui;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitHandler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import net.rptools.lib.FileUtil;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;

/**
 * Apple's new platform API has deprecated the <code>ApplicationListener</code>
 * class. The preferred approach going forward is to use the various set*()
 * methods of <code>Application</code> instead. They are:
 * <ul>
 * <li><code>requestToggleFullScreen({@link java.awt.Window})
 * <li><code>setAboutHandler({@link java.awt.desktop.AboutHandler})
 * <li><code>setDefaultMenuBar({@link javax.swing.JMenuBar})
 * <li><code>setDockIconBadge({@link java.lang.String})
 * <li><code>setDockIconImage({@link java.awt.Image})
 * <li><code>setDockIconProgress(int)
 * <li><code>setDockMenu({@link java.awt.PopupMenu})
 * <li><code>setOpenFileHandler({@link java.awt.desktop.OpenFilesHandler})
 * <li><code>setOpenURIHandler({@link java.awt.desktop.OpenURIHandler})
 * <li><code>setPreferencesHandler({@link java.awt.desktop.PreferencesHandler})
 * <li><code>setPrintFileHandler({@link java.awt.desktop.PrintFilesHandler})
 * <li><code>setQuitHandler({@link java.awt.desktop.QuitHandler})
 * <li><code>setQuitStrategy({@link java.awt.desktop.QuitStrategy})
 * <li><code>addAppEventListener({@link java.awt.desktop.SystemEventListener})
 * <li><code>removeAppEventListener({@link java.awt.desktop.SystemEventListener})
 * </ul>
 */
public class OSXAdapter {
	private static final Logger log = Logger.getLogger(OSXAdapter.class);
	private static Desktop dt = Desktop.getDesktop();

	/**
	 * Sets the quit handler for the main menu on macOS so that it invokes the
	 * proper method of the {@link java.awt.desktop.QuitHandler} object.
	 *
	 * @param h
	 *            the object to delegate the event to
	 */
	public static void setQuitHandler(QuitHandler h) {
		dt.setQuitHandler(h);
		dt.disableSuddenTermination();
	}

	/**
	 * Sets the quit handler for the main menu on macOS so that it invokes the
	 * proper method of the {@link java.awt.desktop.AboutHandler} object.
	 *
	 * @param h
	 *            the object to delegate the event to
	 */
	public static void setAboutHandler(AboutHandler h) {
		dt.setAboutHandler(h);
	}

	/**
	 * Sets the quit handler for the main menu on macOS so that it invokes the
	 * proper method of the {@link java.awt.desktop.PreferencesHandler} object.
	 *
	 * @param h
	 *            the object to delegate the event to
	 */
	public static void setPreferencesHandler(PreferencesHandler h) {
		dt.setPreferencesHandler(h);
	}

	/**
	 * Sets the quit handler for the main menu on macOS so that it invokes the
	 * proper method of the {@link java.awt.desktop.OpenFilesHandler} object.
	 *
	 * @param h
	 *            the object to delegate the event to
	 */
	public static void setFileHandler(OpenFilesHandler h) {
		dt.setOpenFileHandler(h);
	}

	/**
	 * If we're running on macOS, we call this method to download and install
	 * the MapTool logo from the main web site. We cache this image so that it
	 * appears correctly if the application is later executed in "offline" mode,
	 * so to speak.
	 */
	public static void macOSXicon() {
		// If we're running on OSX, add the dock icon image
		// -- and change our application name to just "MapTool" (not currently)
		// We wait until after we call initialize() so that the asset and image managers
		// are configured.
		Image img = null;
		File logoFile = new File(AppUtil.getAppHome("config"), "maptool-dock-icon.png");
		URL logoURL = null;
		try {
			logoURL = new URL("http://www.rptools.net/images/logo/RPTools_Map_Logo.png");
		} catch (MalformedURLException e) {
			MapTool.showError("Attemping to form URL -- shouldn't happen as URL is hard-coded", e);
		}
		try {
			img = ImageUtil.bytesToImage(FileUtils.readFileToByteArray(logoFile));
		} catch (IOException e) {
			log.debug("Attemping to read cached icon: " + logoFile, e);
			try {
				img = ImageUtil.bytesToImage(FileUtil.getBytes(logoURL));
				// If we did download the logo, save it to the 'config' dir for later use.
				BufferedImage bimg = ImageUtil.createCompatibleImage(img, img.getWidth(null), img.getHeight(null), null);
				FileUtils.writeByteArrayToFile(logoFile, ImageUtil.imageToBytes(bimg, "png"));
				img = bimg;
			} catch (IOException e1) {
				log.warn("Cannot read '" + logoURL + "' or  cached '" + logoFile + "'; no dock icon", e1);
			}
		}

		if (Taskbar.isTaskbarSupported()) {
			try {
				Taskbar tb = Taskbar.getTaskbar();
				if (img != null)
					tb.setIconImage(img);
				// We could also modify the popup menu that displays when the user right-clicks the dock image...
				// And we could use the tb.setProgressValue() call to represent campaign loading/saving...

//				if (MapToolUtil.isDebugEnabled()) {
//					String vers = MapTool.getVersion();
//					vers = vers.substring(vers.length() - 2);
//					vers = vers.replaceAll("[^0-9]", "0"); // Convert all non-digits to zeroes
//					tb.setIconBadge(vers);
//				}
			} catch (Exception e) {
				log.info("Error accessing the Taskbar API?!", e);
			}
		}
	}
}
