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

import net.rptools.lib.ModelVersionTransformation;

public class PCVisionTransform implements ModelVersionTransformation {
	private static final String searchFor = "<tokenType>PC";
	private static final String subField = "<hasSight>";

	public String transform(String xml) {
		int index = 0;
		int start = 0;
		while ((start = xml.indexOf(searchFor, index)) > 0) {
			int sightPos = xml.indexOf(subField, start) + subField.length();
			while (Character.isWhitespace(xml.charAt(sightPos)))
				sightPos++;
			if (xml.charAt(sightPos) == 'f') {
				String pre = xml.substring(0, sightPos);
				String post = xml.substring(sightPos + "false".length());

				xml = pre + "true" + post;
			}
			index = sightPos;
		}

		return xml;
	}
}
