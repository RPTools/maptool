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

package net.rptools.maptool.model;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AssetImageConverter extends EncodedByteArrayConverter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		// Ignore the image when creating 1.3.b65+ campaigns with assets...
		//System.out.println(context.toString());	// uncomment to set a breakpoint
	}

	// @formatter:off
	/*
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		// But be sure to read them in if they exist.
		return super.unmarshal(reader, context);
	}
	*/
	// @formatter:on

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return true; // Tell XStream that we can convert the image so it uses our methods
	}
}
