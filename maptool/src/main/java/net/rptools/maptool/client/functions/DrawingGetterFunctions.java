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

package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

public class DrawingGetterFunctions extends DrawingFunctions {
	private static final DrawingGetterFunctions instance = new DrawingGetterFunctions();

	public static DrawingGetterFunctions getInstance() {
		return instance;
	}

	private DrawingGetterFunctions() {
		super(2, 2, "getDrawingLayer", "getDrawingOpacity", "getDrawingProperties", "getPenColor", "getFillColor", "getDrawingEraser", "getPenWidth");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		checkTrusted(functionName);
		checkNumberOfParameters(functionName, parameters, 2, 2);
		String mapName = parameters.get(0).toString();
		String id = parameters.get(1).toString();
		Zone map = getNamedMap(functionName, mapName).getZone();
		GUID guid = getGUID(functionName, id);
		if ("getDrawingLayer".equalsIgnoreCase(functionName)) {
			return getDrawable(functionName, map, guid).getLayer();
		} else if ("getDrawingOpacity".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).getOpacity();
		} else if ("getDrawingProperties".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid);
		} else if ("getPenColor".equalsIgnoreCase(functionName)) {
			String result = paintToString(getPen(functionName, map, guid).getPaint());
			return result;
		} else if ("getFillColor".equalsIgnoreCase(functionName)) {
			String result = paintToString(getPen(functionName, map, guid).getBackgroundPaint());
			return result;
		} else if ("getDrawingEraser".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).isEraser() ? BigDecimal.ONE : BigDecimal.ZERO;
		} else if ("getPenWidth".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).getThickness();
		}
		return null;
	}
}
