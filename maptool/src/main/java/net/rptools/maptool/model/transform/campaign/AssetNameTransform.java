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

/**
 * This transform is for asset filenames, not the actual XML data.  So the XML passed
 * to the {@link #transform(String)} method should be the asset's base name, typically
 * <code>ASSET_DIR + key</code>.  This means that this transform should <b>NOT</b>
 * be registered with any ModelVersionManager or it will be executed in the wrong
 * context.
 *
 * pre-1.3.51:  asset names had ".dat" tacked onto the end and held only binary data
 * 1.3.51-63:  assets were stored in XML under their asset name, no extension
 * 1.3.64+:  asset objects are in XML (name, MD5key), but the image is in another
 * file with the asset's image type as an extension (.jpeg, .png)
 *
 * @author frank
 */
public class AssetNameTransform implements ModelVersionTransformation {
	private final String regexOld;
	private final String regexNew;

	public AssetNameTransform(String from, String to) {
		regexOld = from;
		regexNew = to;
	}

	public String transform(String name) {
		return name.replace(regexOld, regexNew);
	}
}
