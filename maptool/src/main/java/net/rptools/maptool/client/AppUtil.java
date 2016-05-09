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

package net.rptools.maptool.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class provides utility functions for maptool client.
 */
public class AppUtil {
	private static final Logger log = Logger.getLogger(AppUtil.class);

	public static final String DEFAULT_DATADIR_NAME = ".maptool";
	public static final String DATADIR_PROPERTY_NAME = "MAPTOOL_DATADIR";

	private static File dataDirPath;

	private static final String CLIENT_ID_FILE = "client-id";

	/**
	 * Returns a File object for USER_HOME if USER_HOME is non-null, otherwise null.
	 * 
	 * @return the users home directory as a File object
	 */
	private static File getUserHome() {
		return new File(System.getProperty("user.home"));
	}

	/**
	 * Returns a {@link File} path that points to the AppHome base directory along with the subpath denoted in the
	 * "subdir" argument.
	 * <p>
	 * For example <code>getAppHome("cache")</code> will return the path <code>{APPHOME}/cache</code>.
	 * <p>
	 * As a side-effect the function creates the directory pointed to by File.
	 * 
	 * @param subdir
	 *            of the maptool home directory
	 * @return the maptool data directory name subdir
	 * @see getAppHome()
	 */
	public static File getAppHome(String subdir) {
		File path = getDataDir();
		if (!StringUtils.isEmpty(subdir)) {
			path = new File(path.getAbsolutePath(), subdir);
		}
		// Now check for characters known to cause problems.  See getDataDir() for details.
		if (path.getAbsolutePath().matches("!"))
			throw new RuntimeException(I18N.getText("msg.error.unusableDir", path.getAbsolutePath()));

		if (!path.exists()) {
			path.mkdirs();
			// Now check our work
			if (!path.exists()) {
				RuntimeException re = new RuntimeException(I18N.getText("msg.error.unableToCreateDataDir", path.getAbsolutePath()));
				if (log.isInfoEnabled())
					log.info("msg.error.unableToCreateDataDir", re);
				throw re;
			}
		}
		return path;
	}

	/**
	 * Set the state back to uninitialized
	 */
	// Package protected for testing
	static void reset() {
		dataDirPath = null;
	}

	/**
	 * Determine the actual directory to store data files, derived from the environment
	 */
	// Package protected for testing
	static File getDataDir() {
		if (dataDirPath == null) {
			String path = System.getProperty(DATADIR_PROPERTY_NAME);
			if (StringUtils.isEmpty(path)) {
				path = DEFAULT_DATADIR_NAME;
			}
			if (path.indexOf("/") < 0 && path.indexOf("\\") < 0) {
				path = getUserHome() + "/" + path;
			}
			// Now we need to check for characters that are known to cause problems in
			// path names.  We want to allow the local platform to make this decision, but
			// the built-in "jar://" URL uses the "!" as a separator between the archive name
			// and the archive member. :(  Right now we're only checking for that one character
			// but the list may need to be expanded in the future.
			if (path.matches("!"))
				throw new RuntimeException(I18N.getText("msg.error.unusableDataDir", path));

			dataDirPath = new File(path);
		}
		return dataDirPath;
	}

	/**
	 * Returns a File path representing the base directory to store local data. By default this is a ".maptool"
	 * directory in the user's home directory.
	 * <p>
	 * If you want to change the dir for data storage you can set the system property MAPTOOL_DATADIR. If the value of
	 * the MAPTOOL_DATADIR has any file separator characters in it, it will assume you are using an absolute path. If
	 * the path does not include a file separator it will use it as a subdirectory in the user's home directory
	 * <p>
	 * As a side-effect the function creates the directory pointed to by File.
	 * 
	 * @return the maptool data directory
	 */
	public static File getAppHome() {
		return getAppHome("");
	}

	/**
	 * Returns a File object for the maptool tmp directory, or null if the users home directory could not be determined.
	 * 
	 * @return the maptool tmp directory
	 */
	public static File getTmpDir() {
		return getAppHome("tmp");
	}

	/**
	 * Returns true if the player owns the token, otherwise false. If the player is GM this function always returns
	 * true. If strict token management is disabled then this function always returns true.
	 * 
	 * @param token
	 * @return true if the player owns the token
	 */
	public static boolean playerOwns(Token token) {
		Player player = MapTool.getPlayer();
		if (player.isGM()) {
			return true;
		}
		if (!MapTool.getServerPolicy().useStrictTokenManagement()) {
			return true;
		}
		return token.isOwner(player.getName());
	}

	/**
	 * Returns true if the token is visible in the zone. If the view is the GM view then this function always returns
	 * true.
	 * 
	 * @param zone
	 *            to check for visibility
	 * @param token
	 *            to check for visibility in zone
	 * @param view
	 *            to use when checking visibility
	 * @return true if token is visible in zone given the view
	 */
	public static boolean tokenIsVisible(Zone zone, Token token, PlayerView view) {
		if (view.isGMView()) {
			return true;
		}
		return zone.isTokenVisible(token);
	}

	public static String readClientId() {
		Path clientFile = Paths.get(getAppHome().getAbsolutePath(), CLIENT_ID_FILE);
		String clientId = "unknown";
		if (!clientFile.toFile().exists()) {
			clientId = UUID.randomUUID().toString();
			try {
				Files.write(clientFile, clientId.getBytes());
			} catch (IOException e) {
				log.info("msg.error.unableToCreateClientIdFile", e);
			}
		} else {
			try {
				clientId = new String(Files.readAllBytes(clientFile));
			} catch (IOException e) {
				log.info("msg.error.unableToReadClientIdFile", e);
			}
		}
		return clientId;

	}
}
