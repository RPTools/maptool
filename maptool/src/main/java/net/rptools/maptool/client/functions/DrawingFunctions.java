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
import java.util.List;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class DrawingFunctions extends AbstractFunction {
	private static final DrawingFunctions instance = new DrawingFunctions();

	public static DrawingFunctions getInstance() {
		return instance;
	}

	private DrawingFunctions() {
		super(2, 3, "getDrawingLayer", "setDrawingLayer", "bringDrawingToFront", "sendDrawingToBack",
				"getDrawingOpacity", "setDrawingOpacity", "getDrawingProperties", "setDrawingProperties",
				"setPenColor", "getPenColor", "setFillColor", "getFillColor",
				"findDrawings");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		checkTrusted(functionName);
		if ("getDrawingLayer".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			return getDrawable(functionName, map, guid).getLayer();
		} else if ("setDrawingLayer".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 3, 3);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Layer layer = getLayer(parameters.get(2).toString());
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			return changeLayer(map, layer, guid);
		} else if ("bringDrawingToFront".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			bringToFront(map, guid);
			return "";
		} else if ("sendDrawingToBack".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			sendToBack(map, guid);
			return "";
		} else if ("getDrawingOpacity".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			return getPen(functionName, map, guid).getOpacity();
		} else if ("setDrawingOpacity".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 3, 3);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			String opacity = parameters.get(2).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			float op = getFloatPercent(functionName, opacity);
			setDrawingOpacity(map, guid, op);
			return "";
		} else if ("getDrawingProperties".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			return getPen(functionName, map, guid);
		} else if ("setDrawingProperties".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 3, 3);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Pen pen = (Pen) parameters.get(2);
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			setPen(functionName, map, guid, pen);
			return "";
		} else if ("findDrawings".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 3);
			String mapName = parameters.get(0).toString();
			String drawingName = parameters.get(1).toString();
			String delim = parameters.size() > 2 ? parameters.get(2).toString() : ",";
			Zone map = getNamedMap(functionName, mapName).getZone();
			List<DrawnElement> drawableList = map.getAllDrawnElements();
			Iterator<DrawnElement> iter = drawableList.iterator();
			String result = "";
			while (iter.hasNext()) {
				DrawnElement de = iter.next();
				if (de.getDrawable() instanceof AbstractDrawing) {
					if (drawingName.equals(((AbstractDrawing) de.getDrawable()).getName())) {
						result = result + (result == "" ? "" : delim) + de.getDrawable().getId();
					}
				}
			}
			return result;
		} else if ("getPenColor".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			String result = paintToString(getPen(functionName, map, guid).getPaint());
			return result;
		} else if ("setPenColor".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 3, 3);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			String paint = parameters.get(2).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			if ("".equalsIgnoreCase(paint))
				getPen(functionName, map, guid).setForegroundMode(Pen.MODE_TRANSPARENT);
			else {
				getPen(functionName, map, guid).setForegroundMode(Pen.MODE_SOLID);
				getPen(functionName, map, guid).setPaint(paintFromString(paint));
			}
			return "";
		} else if ("getFillColor".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			String result = paintToString(getPen(functionName, map, guid).getBackgroundPaint());
			return result;
		} else if ("setFillColor".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 3, 3);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			String paint = parameters.get(2).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			if ("".equalsIgnoreCase(paint))
				getPen(functionName, map, guid).setBackgroundMode(Pen.MODE_TRANSPARENT);
			else {
				getPen(functionName, map, guid).setBackgroundMode(Pen.MODE_SOLID);
				getPen(functionName, map, guid).setBackgroundPaint(paintFromString(paint));
			}
			return "";
		}
		return null;
	}

	public void bringToFront(Zone map, GUID guid) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				map.removeDrawable(de.getDrawable().getId());
				MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
				map.addDrawable(new DrawnElement(de.getDrawable(), de.getPen()));
				MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
			}
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
	}

	private Layer changeLayer(Zone map, Layer layer, GUID guid) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getLayer() != layer && de.getDrawable().getId().equals(guid)) {
				map.removeDrawable(de.getDrawable().getId());
				MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
				de.getDrawable().setLayer(layer);
				map.addDrawable(de);
				MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
			}
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
		return layer;
	}

	/**
	 * Checks that the number of objects in the list <code>parameters</code>
	 * is within given bounds (inclusive). Throws a <code>ParserException</code>
	 * if the check fails.
	 *
	 * @param    functionName    this is used in the exception message
	 * @param    parameters      a list of parameters
	 * @param    min             the minimum amount of parameters (inclusive)
	 * @param    max             the maximum amount of parameters (inclusive)
	 * @throws   ParserException    if there were more or less parameters than allowed
	 */
	private void checkNumberOfParameters(String functionName, List<Object> parameters, int min, int max) throws ParserException {
		int numberOfParameters = parameters.size();
		if (numberOfParameters < min) {
			throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
		} else if (numberOfParameters > max) {
			throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, max, numberOfParameters));
		}
	}

	/**
	 * Checks whether or not the function is trusted
	 * 
	 * @param functionName     Name of the macro function
	 * @throws ParserException Returns trust error message and function name 
	 */
	private void checkTrusted(String functionName) throws ParserException {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
		}
	}

	/**
	 * Looks for a drawing on a specific map that matches a specific id.
	 * Throws a <code>ParserException</code> if the drawing is not found.
	 *
	 * @param    functionName    this is used in the exception message
	 * @param    map             the zone that should contain the drawing
	 * @param    guid            the id of the drawing.
	 * @throws   ParserException if the drawing is not found.
	 */
	private Drawable getDrawable(String functionName, Zone map, GUID guid) throws ParserException {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				return de.getDrawable();
			}
		}
		throw new ParserException(I18N.getText("macro.function.drawingFunction.unknownDrawing", functionName, guid.toString()));
	}

	/**
	 * Validates the float
	 * @param  functionName String Name of the calling function. Used for error messages.
	 * @param  f String value of percentage
	 * @return float
	 * @throws ParserException thrown on invalid foat
	 */
	private float getFloatPercent(String functionName, String f) throws ParserException {
		try {
			Float per = Float.parseFloat(f);
			while (per > 1)
				per = per / 100;
			return per;
		} catch (Exception e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", functionName, f));
		}
	}

	/**
	 * Validates the GUID
	 * @param  functionName String Name of the calling function. Used for error messages.
	 * @param  id String value of GUID
	 * @return GUID
	 * @throws ParserException thrown on invalid GUID
	 */
	private GUID getGUID(String functionName, String id) throws ParserException {
		try {
			return GUID.valueOf(id);
		} catch (Exception e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeG", functionName, id));
		}
	}

	/**
	 * Take a string and return a layer
	 * @param  layer String naming the layer
	 * @return Layer
	 */
	private Layer getLayer(String layer) {
		if ("GM".equalsIgnoreCase(layer))
			return Layer.GM;
		else if ("OBJECT".equalsIgnoreCase(layer))
			return Layer.OBJECT;
		else if ("BACKGROUND".equalsIgnoreCase(layer))
			return Layer.BACKGROUND;
		return Layer.TOKEN;
	}

	/**
	 * Find the map/zone for a given map name
	 * @param functionName String Name of the calling function.
	 * @param mapName      String Name of the searched for map.
	 * @return             ZoneRenderer The map/zone.
	 * @throws ParserException  if the map is not found
	 */
	private ZoneRenderer getNamedMap(String functionName, String mapName) throws ParserException {
		for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
			if (mapName.equals(zr.getZone().getName())) {
				return zr;
			}
		}
		throw new ParserException(I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, mapName));
	}

	/**
	 * Looks for a drawing on a specific map that matches a specific id.
	 * Throws a <code>ParserException</code> if the drawing is not found.
	 *
	 * @param    functionName    this is used in the exception message
	 * @param    map             the zone that should contain the drawing
	 * @param    guid            the id of the drawing.
	 * @throws   ParserException if the drawing is not found.
	 */
	private Pen getPen(String functionName, Zone map, GUID guid) throws ParserException {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				return de.getPen();
			}
		}
		throw new ParserException(I18N.getText("macro.function.drawingFunction.unknownDrawing", functionName, guid.toString()));
	}

	private DrawablePaint paintFromString(String paint) {
		if (paint.toLowerCase().startsWith("asset://")) {
			String id = paint.substring(8);
			return new DrawableTexturePaint(new MD5Key(id));
		} else {
			return new DrawableColorPaint(MapToolUtil.getColor(paint));
		}
	}

	private String paintToString(DrawablePaint drawablePaint) {
		if (drawablePaint instanceof DrawableColorPaint) {
			return "#" + Integer.toHexString(((DrawableColorPaint) drawablePaint).getColor()).substring(2);
		}
		if (drawablePaint instanceof DrawableTexturePaint) {
			return "asset://" + ((DrawableTexturePaint) drawablePaint).getAsset().getId();
		}
		return "";
	}

	public void sendToBack(Zone map, GUID guid) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				map.removeDrawable(de.getDrawable().getId());
				map.addDrawableRear(de);
			}
		}
		// horrid kludge needed to redraw zone :(
		for (DrawnElement de : map.getAllDrawnElements()) {
			MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
			MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
	}

	public void setDrawingOpacity(Zone map, GUID guid, float op) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				setOpacity(map, de, op);
			}
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
	}

	private void setOpacity(Zone map, DrawnElement d, float op) {
		if (d.getDrawable() instanceof DrawablesGroup) {
			DrawablesGroup dg = (DrawablesGroup) d.getDrawable();
			for (DrawnElement de : dg.getDrawableList()) {
				setOpacity(map, de, op);
			}
		} else {
			Pen pen = d.getPen();
			pen.setOpacity(op);
			d.setPen(pen);
			MapTool.serverCommand().updateDrawing(map.getId(), pen, d);
		}
	}

	/**
	 * Looks for a drawing on a specific map that matches a specific id.
	 * If found sets the Pen.
	 * Throws a <code>ParserException</code> if the drawing is not found.
	 *
	 * @param    functionName    this is used in the exception message
	 * @param    map             the zone that should contain the drawing
	 * @param    guid            the id of the drawing.
	 * @param    pen             the replacement pen.
	 * @throws   ParserException if the drawing is not found.
	 */
	private void setPen(String functionName, Zone map, GUID guid, Object pen) throws ParserException {
		if (!(pen instanceof Pen))
			throw new ParserException(I18N.getText("macro.function.drawingFunction.invalidPen", functionName));
		Pen p = new Pen((Pen) pen);
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				de.setPen(p);
				return;
			}
		}
		throw new ParserException(I18N.getText("macro.function.drawingFunction.unknownDrawing", functionName, guid.toString()));
	}
}
