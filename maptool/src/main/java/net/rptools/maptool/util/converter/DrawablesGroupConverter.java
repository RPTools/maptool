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

package net.rptools.maptool.util.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import net.rptools.maptool.model.drawing.DrawablesGroup;

/**
 * This is a null converter to tell Xstream to not serialize the class.
 * Note: Custom content could be written/read using such converters if needed.
 * @author Jamz
 *
 */
public class DrawablesGroupConverter implements Converter {
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return null;
	}

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(DrawablesGroup.class);
	}
}