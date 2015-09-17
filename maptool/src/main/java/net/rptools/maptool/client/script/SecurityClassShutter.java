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

package net.rptools.maptool.client.script;

import org.mozilla.javascript.ClassShutter;

public class SecurityClassShutter implements ClassShutter {

	public boolean visibleToScripts(String cname) {
		// Everything in java.lang excluding the system class.
		if (cname.startsWith("java.lang")) {
			if (cname.equals("java.lang.System")) {
				return false;
			}
			return true;
		}

		// Everything in java.util
		if (cname.startsWith("java.util")) {
			return true;
		}

		// Everything in java.math
		if (cname.startsWith("java.math")) {
			return true;
		}

		// Maptool JavaScript macro api classes.
		if (cname.startsWith("net.rptools.maptool.client.script.api")) {
			return true;
		}

		// Allow the mozilla javascript classes
		if (cname.startsWith("org.mozilla.javascript")) {
			return true;
		}

		return false;
	}
}
