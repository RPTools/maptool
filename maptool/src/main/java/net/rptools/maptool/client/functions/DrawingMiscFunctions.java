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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.sf.json.JSONArray;

public class DrawingMiscFunctions extends DrawingFunctions {
	private static final DrawingMiscFunctions instance = new DrawingMiscFunctions();

	public static DrawingMiscFunctions getInstance() {
		return instance;
	}

	private DrawingMiscFunctions() {
		super(2, 3, "findDrawings", "refreshDrawing", "bringDrawingToFront", "sendDrawingToBack");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		checkTrusted(functionName);
		String mapName = parameters.get(0).toString();
		String drawing = parameters.get(1).toString();
		Zone map = getNamedMap(functionName, mapName).getZone();
		if ("findDrawings".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 3);
			List<DrawnElement> drawableList = map.getAllDrawnElements();
			List<String> drawingList = findDrawings(drawableList, drawing);
			String delim = parameters.size() > 2 ? parameters.get(2).toString() : ",";
			if ("json".equalsIgnoreCase(delim))
				return JSONArray.fromObject(drawingList);
			else
				return StringFunctions.getInstance().join(drawingList, delim);
		} else {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			GUID guid = getGUID(functionName, drawing);
			if ("refreshDrawing".equalsIgnoreCase(functionName)) {
				DrawnElement de = getDrawnElement(functionName, map, guid);
				MapTool.getFrame().updateDrawTree();
				MapTool.serverCommand().updateDrawing(map.getId(), de.getPen(), de);
				return "";
			} else if ("bringDrawingToFront".equalsIgnoreCase(functionName)) {
				bringToFront(map, guid);
				return "";
			} else if ("sendDrawingToBack".equalsIgnoreCase(functionName)) {
				sendToBack(map, guid);
				return "";
			}
		}
		return null;
	}

	private List<String> findDrawings(List<DrawnElement> drawableList, String name) {
		List<String> drawingList = new LinkedList<String>();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable() instanceof AbstractDrawing) {
				if (name.equals(((AbstractDrawing) de.getDrawable()).getName())) {
					drawingList.add(de.getDrawable().getId().toString());
				}
			}
			if (de.getDrawable() instanceof DrawablesGroup) {
				List<DrawnElement> glist = ((DrawablesGroup) de.getDrawable()).getDrawableList();
				drawingList.addAll(findDrawings(glist, name));
			}
		}
		return drawingList;
	}
}
