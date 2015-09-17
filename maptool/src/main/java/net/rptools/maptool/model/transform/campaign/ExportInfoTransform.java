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

package net.rptools.maptool.model.transform.campaign;

import java.util.regex.Pattern;

import net.rptools.lib.ModelVersionTransformation;

/**
 * This should be applied to any campaign file version 1.3.74 and earlier
 * due to the deletion of the ExportInfo class afterwards.
 */
public class ExportInfoTransform implements ModelVersionTransformation {
	private static final String blockStart = "<exportInfo>";
	private static final String blockEnd = "</exportInfo>";
	private static final String regex = blockStart + ".*" + blockEnd;
	private static final String replacement = "";

	private static final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

	/**
	 * Delete the block containing the now-obsolete exportInfo class data, since
	 * there is no place to put it (and therefore generates an XStream error)
	 */
	public String transform(String xml) {
		// Same as: return xml.replaceAll(regex, replacement);
		// except that we can specify the flag DOTALL
		return pattern.matcher(xml).replaceAll(replacement);
	}
}
