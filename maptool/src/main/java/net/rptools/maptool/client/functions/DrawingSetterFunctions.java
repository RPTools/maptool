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

import java.util.List;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

public class DrawingSetterFunctions extends DrawingFunctions {
	private static final DrawingSetterFunctions instance = new DrawingSetterFunctions();

	public static DrawingSetterFunctions getInstance() {
		return instance;
	}

	private DrawingSetterFunctions() {
		super(3, 3, "setDrawingLayer", "setDrawingOpacity", "setDrawingProperties", "setPenColor", "setFillColor", "setDrawingEraser", "setPenWidth", "setLineCap");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		checkTrusted(functionName);
		checkNumberOfParameters(functionName, parameters, 3, 3);
		String mapName = parameters.get(0).toString();
		String id = parameters.get(1).toString();
		Zone map = getNamedMap(functionName, mapName).getZone();
		GUID guid = getGUID(functionName, id);
		if ("setDrawingLayer".equalsIgnoreCase(functionName)) {
			Layer layer = getLayer(parameters.get(2).toString());
			return changeLayer(map, layer, guid);
		} else if ("setDrawingOpacity".equalsIgnoreCase(functionName)) {
			String opacity = parameters.get(2).toString();
			float op = getFloatPercent(functionName, opacity);
			setDrawingOpacity(functionName, map, guid, op);
			return "";
		} else if ("setDrawingProperties".equalsIgnoreCase(functionName)) {
			Pen pen = (Pen) parameters.get(2);
			setPen(functionName, map, guid, pen);
			return "";
		} else if ("setPenColor".equalsIgnoreCase(functionName)) {
			String paint = parameters.get(2).toString();
			if ("".equalsIgnoreCase(paint))
				getPen(functionName, map, guid).setForegroundMode(Pen.MODE_TRANSPARENT);
			else {
				getPen(functionName, map, guid).setForegroundMode(Pen.MODE_SOLID);
				getPen(functionName, map, guid).setPaint(paintFromString(paint));
			}
			return "";
		} else if ("setFillColor".equalsIgnoreCase(functionName)) {
			String paint = parameters.get(2).toString();
			if ("".equalsIgnoreCase(paint))
				getPen(functionName, map, guid).setBackgroundMode(Pen.MODE_TRANSPARENT);
			else {
				getPen(functionName, map, guid).setBackgroundMode(Pen.MODE_SOLID);
				getPen(functionName, map, guid).setBackgroundPaint(paintFromString(paint));
			}
			return "";
		} else if ("setDrawingEraser".equalsIgnoreCase(functionName)) {
			boolean eraser = parseBoolean(functionName, parameters, 2);
			Pen p = getPen(functionName, map, guid);
			p.setEraser(eraser);
			return "";
		} else if ("setPenWidth".equalsIgnoreCase(functionName)) {
			String penWidth = parameters.get(2).toString();
			float pw = getFloat(functionName, penWidth);
			getPen(functionName, map, guid).setThickness(pw);
			return "";
		} else if ("setLineCap".equalsIgnoreCase(functionName)) {
			boolean squareCap = parseBoolean(functionName, parameters, 2);
			Pen p = getPen(functionName, map, guid);
			p.setSquareCap(squareCap);
			return "";
		}
		return null;
	}
}
