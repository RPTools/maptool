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

package net.rptools.maptool.protocol.syrinscape;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Support "syrinscape-fantasy" URI in Swing components
 * 
 * @author Jamz
 */
public class SyrinscapeConnection extends URLConnection {
	public SyrinscapeConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub
	}
}