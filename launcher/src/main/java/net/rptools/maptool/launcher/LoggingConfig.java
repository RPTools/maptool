/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 * 
 * Created on May 30, 2010, 10:27:59 AM Lee: Features extended on February, 2013
 */

package net.rptools.maptool.launcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;

/**
 * Represents a single XML file used to configure logging for MapTool. It
 * implements Comparable so that sorting is based on the description field (or
 * filename, if no description is available).
 * 
 * @author frank
 */
public class LoggingConfig implements Comparable<LoggingConfig> {
	File fname;
	Map<String, String> properties = new HashMap<String, String>(2);
	JCheckBox chkbox;

	LoggingConfig(File f, JCheckBox c) {
		fname = f;
		chkbox = c;
	}

	public String getProperty(String key) {
		if (key == null) {
			return null;
		}
		return properties.get(key.toLowerCase());
	}

	public void addProperty(String key, String value) {
		if (key != null) {
			properties.put(key.toLowerCase(), value);
		}
	}

	@Override
	public int compareTo(LoggingConfig arg) {
		if (this.equals(arg)) {
			return 0;
		}
		final String desc = properties.get("desc"); //$NON-NLS-1$
		if (desc != null) {
			return desc.compareTo(arg.properties.get("desc")); //$NON-NLS-1$
		}
		return fname.getName().compareTo(arg.fname.getName());
	}
}