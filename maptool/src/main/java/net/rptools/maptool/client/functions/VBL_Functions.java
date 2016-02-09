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

import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * New class extending AbstractFunction to create new "Macro Functions" drawVBL,
 * eraseVBL, getVBL
 * 
 * drawVBL(jsonArray) :: Takes an array of JSON Objects containing information
 * to draw a Shape in VBL
 * 
 * eraseVBL(jsonArray) :: Takes an array of JSON Objects containing information
 * to erase a Shape in VBL
 * 
 * getVBL(jsonArray) :: Get the VBL for a given area and return as array of
 * points
 */
public class VBL_Functions extends AbstractFunction {
	private static final VBL_Functions instance = new VBL_Functions();
	private static final String[] paramTranslate = new String[] { "tx", "ty" };
	private static final String[] paramScale = new String[] { "sx", "sy" };

	private VBL_Functions() {
		super(0, 2, "drawVBL", "eraseVBL", "getVBL");
	}

	public static VBL_Functions getInstance() {
		return instance;
	}

	private static enum Shape {
		RECTANGLE, POLYGON, CROSS, CIRCLE
	};

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();

		if (functionName.equals("drawVBL") || functionName.equals("eraseVBL")) {
			boolean erase = false;

			if (parameters.size() != 1) {
				throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 1, parameters.size()));
			}

			if (!MapTool.getParser().isMacroPathTrusted())
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

			if (functionName.equals("eraseVBL"))
				erase = true;

			Object j = JSONMacroFunctions.asJSON(parameters.get(0).toString().toLowerCase());
			if (!(j instanceof JSONObject || j instanceof JSONArray)) {
				throw new ParserException(I18N.getText("macro.function.json.unknownType", j == null ? parameters.get(0).toString() : j.toString(), functionName));
			}

			JSONArray vblArray = JSONArray.fromObject(j);
			// System.out.println("# of Objects: " + vblArray.size());
			// System.out.println("JSONArray: " + vblArray.toString(3));

			for (int i = 0; i < vblArray.size(); i++) {
				JSONObject vblObject = vblArray.getJSONObject(i);

				Shape vblShape = Shape.valueOf(vblObject.getString("shape").toUpperCase());
				switch (vblShape) {
				case RECTANGLE:
					drawRectangleVBL(renderer, vblObject, erase);
					break;
				case POLYGON:
					drawPolygonVBL(renderer, vblObject, erase);
					break;
				case CROSS:
					drawCrossVBL(renderer, vblObject, erase);
					break;
				case CIRCLE:
					drawCircleVBL(renderer, vblObject, erase);
					break;
				}
			}
		}

		if (functionName.equals("getVBL")) {
			boolean simpleJSON = false; // If true, send only array of x,y

			if (parameters.size() > 2)
				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));

			if (parameters.isEmpty())
				throw new ParserException(I18N.getText("macro.function.general.notenoughparms", functionName, 1, parameters.size()));

			if (!MapTool.getParser().isMacroPathTrusted())
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

			if (parameters.size() == 2 && !parameters.get(1).equals(BigDecimal.ZERO))
				simpleJSON = true;

			Object j = JSONMacroFunctions.asJSON(parameters.get(0).toString().toLowerCase());
			if (!(j instanceof JSONObject || j instanceof JSONArray)) {
				throw new ParserException(I18N.getText("macro.function.json.unknownType", j == null ? parameters.get(0).toString() : j.toString(), functionName));
			}
			JSONArray vblArray = JSONArray.fromObject(j);
			Area vblArea = null;
			for (int i = 0; i < vblArray.size(); i++) {
				JSONObject vblObject = vblArray.getJSONObject(i);
				if (vblArea == null) {
					vblArea = getVBL(renderer, vblObject);
				} else {
					vblArea.add(getVBL(renderer, vblObject));
				}
			}
			return getAreaPoints(vblArea, simpleJSON);
		}
		return "";
	}

	/**
	 * Get the required parameters needed from the JSON to draw a rectangle and
	 * render as VBL.
	 * 
	 * @param renderer
	 *            Reference to the ZoneRenderer
	 * @param vblObject
	 *            The JSONObject containing all the coordinates and values to
	 *            needed to draw a rectangle.
	 * @param erase
	 *            Set to true to erase the rectangle in VBL, otherwise draw it
	 * @throws ParserException
	 *             If the minimum required parameters are not present in the
	 *             JSON, throw ParserException
	 */
	private void drawRectangleVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase) throws ParserException {
		String funcname = "drawVBL[Rectangle]";
		// Required Parameters
		String requiredParms[] = { "x", "y", "w", "h" };
		if (!jsonKeysExist(vblObject, requiredParms, funcname))
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y,w,h}"));

		int x = getJSONint(vblObject, "x", funcname);
		int y = getJSONint(vblObject, "y", funcname);
		int w = getJSONint(vblObject, "w", funcname);
		int h = getJSONint(vblObject, "h", funcname);

		// Optional Parameters
		int fill = getJSONint(vblObject, "fill", funcname);
		double s = getJSONdouble(vblObject, "scale", funcname);
		double r = getJSONdouble(vblObject, "r", funcname);
		double facing = getJSONdouble(vblObject, "facing", funcname);
		float t = (float) getJSONdouble(vblObject, "thickness", funcname);
		boolean useFacing = vblObject.containsKey("facing");

		if (t < 2) {
			t = 2;
		} // Set default thickness to 2 if null or negative
		if (t % 2 != 0) {
			t -= 1;
		} // Set thickness an even number so we don't split .5 pixels on BasicStroke
		if (t > w - 2) {
			t = w - 2;
		} // Set thickness to width - 2 pixels if thicker
		if (t > h - 2) {
			t = h - 2;
		} // Set thickness to height -2 pixels if thicker
		if (w < 4) {
			w = 4;
		} // Set width to min of 4, as a 2 pixel thick rectangle as to be at least 4 pixels wide
		if (h < 4) {
			h = 4;
		} // Set height to min of 4, as a 2 pixel thick rectangle as to be at least 4 pixels high

		// Apply Scaling if requested
		if (s != 0) {
			// Subtracting "thickness" so drawing stays within "bounds"
			double w2 = (w * s) - t;
			double h2 = (h * s) - t;
			x = (int) (x + (t / 2));
			y = (int) (y + (t / 2));
			w = (int) w2;
			h = (int) h2;
		} else {
			// Subtracting "thickness" so drawing stays within "bounds"
			double w2 = w - t;
			double h2 = h - t;
			x = (int) (x + (t / 2));
			y = (int) (y + (t / 2));
			w = (int) w2;
			h = (int) h2;
		}
		// Apply Thickness, defaults to 2f
		BasicStroke stroke = new BasicStroke(t != 0f ? t : 2f);

		// Create the rectangle, unfilled
		Area area = new Area(stroke.createStrokedShape(new java.awt.Rectangle(x, y, w, h)));

		// Fill in the rectangle if requested
		if (fill != 0)
			area.add(new Area(new java.awt.Rectangle(x, y, w, h)));

		AffineTransform atArea = new AffineTransform();
		applyTranslate(funcname, atArea, vblObject, paramTranslate);

		// Rotate the Polygon if requested
		if (useFacing || r != 0) {
			// Find the center x,y coords of the rectangle
			int rx = area.getBounds().x + (area.getBounds().width / 2);
			int ry = area.getBounds().y + (area.getBounds().height / 2);

			// Override rx,ry coords if supplied
			String rParms[] = { "rx", "ry" };
			if (jsonKeysExist(vblObject, rParms, funcname)) {
				rx = getJSONint(vblObject, "rx", funcname);
				ry = getJSONint(vblObject, "ry", funcname);
			}
			if (useFacing)
				r = -(facing + 90);
			atArea.rotate(Math.toRadians(r), rx, ry);
		}
		applyScale(funcname, atArea, vblObject, paramScale);

		if (!atArea.isIdentity())
			area.transform(atArea);

		// Send to the engine to render
		renderVBL(renderer, area, erase);
	}

	private void applyTranslate(String funcname, AffineTransform at, JSONObject vblObject, String[] params) throws ParserException {
		if (jsonKeysExist(vblObject, params, funcname)) {
			double tx = getJSONdouble(vblObject, "tx", funcname);
			double ty = getJSONdouble(vblObject, "ty", funcname);
			at.translate(tx, ty);
		}
	}

	private void applyScale(String funcname, AffineTransform at, JSONObject vblObject, String[] params) throws ParserException {
		if (jsonKeysExist(vblObject, params, funcname)) {
			double sx = getJSONdouble(vblObject, "sx", funcname);
			double sy = getJSONdouble(vblObject, "sy", funcname);
			at.scale(sx, sy);
		}
	}

	/**
	 * Get the required parameters needed from the JSON to draw a Polygon and
	 * render as VBL.
	 * 
	 * @param renderer
	 *            Reference to the ZoneRenderer
	 * @param vblObject
	 *            The JSONObject containing all the coordinates and values to
	 *            needed to draw a rectangle.
	 * @param erase
	 *            Set to true to erase the rectangle in VBL, otherwise draw it
	 * @throws ParserException
	 *             If the minimum required parameters are not present in the
	 *             JSON, throw ParserException
	 */
	private void drawPolygonVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase) throws ParserException {
		String funcname = "drawVBL[Polygon]";
		String requiredParms[] = { "points" };
		if (!jsonKeysExist(vblObject, requiredParms, funcname))
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeA", "points", funcname));

		// Get all the x,y coords for the Polygon, must have at least 2
		JSONArray points = vblObject.getJSONArray("points");
		if (points.size() < 2) {
			throw new ParserException(I18N.getText("macro.function.json.getInvalidEndIndex", funcname, 2, points.size()));
		}
		// Optional Parameters
		int fill = getJSONint(vblObject, "fill", funcname);
		int close = getJSONint(vblObject, "close", funcname);
		double r = getJSONdouble(vblObject, "r", funcname);
		double facing = getJSONdouble(vblObject, "facing", funcname);
		float t = (float) getJSONdouble(vblObject, "thickness", funcname);
		boolean useFacing = vblObject.containsKey("facing");

		if (!vblObject.containsKey("thickness"))
			t = 2; // Set default thickness if no value is passed.

		Area area = null;

		if (close == 0) {
			// User requests for polygon to not be closed, so a Path is used
			Path2D path = new Path2D.Double();
			double lastX = 0;
			double lastY = 0;

			for (int i = 0; i < points.size(); i++) {
				JSONObject point = points.getJSONObject(i);

				String requiredPointParms[] = { "x", "y" };
				if (!jsonKeysExist(point, requiredPointParms, funcname))
					throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y}", funcname));

				double x = getJSONdouble(point, "x", funcname);
				double y = getJSONdouble(point, "y", funcname);

				if (path.getCurrentPoint() == null) {
					path.moveTo(x, y);
				} else if (lastX != x && lastY != y) {
					path.lineTo(x, y);
					lastX = x;
					lastY = y;
				}
			}
			BasicStroke stroke = new BasicStroke(t > 0f ? t : 0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			area = new Area(stroke.createStrokedShape(path));
		} else {
			// User requests for polygon to be closed, so a Polygon is used which is automatically closed
			Polygon poly = new Polygon();

			for (int i = 0; i < points.size(); i++) {
				JSONObject point = points.getJSONObject(i);

				String requiredPointParms[] = { "x", "y" };
				if (!jsonKeysExist(point, requiredPointParms, funcname))
					throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y}", funcname));

				int x = getJSONint(point, "x", funcname);
				int y = getJSONint(point, "y", funcname);

				poly.addPoint(x, y);
			}
			// A strokedShape will not be filled in and have a defined thickness.
			if (fill == 0) {
				BasicStroke stroke = new BasicStroke(t > 0f ? t : 0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
				area = new Area(stroke.createStrokedShape(poly));
			} else {
				area = new Area(poly);
			}
		}
		AffineTransform atArea = new AffineTransform();
		applyTranslate(funcname, atArea, vblObject, paramTranslate);

		// Rotate the Polygon if requested
		if (useFacing || r != 0) {
			// Find the center x,y coords of the rectangle
			int rx = area.getBounds().x + (area.getBounds().width / 2);
			int ry = area.getBounds().y + (area.getBounds().height / 2);

			// Override rx,ry coords if supplied
			String rParms[] = { "rx", "ry" };
			if (jsonKeysExist(vblObject, rParms, funcname)) {
				rx = getJSONint(vblObject, "rx", funcname);
				ry = getJSONint(vblObject, "ry", funcname);
			}
			if (useFacing)
				r = -(facing + 90);

			atArea.rotate(Math.toRadians(r), rx, ry);
		}
		applyScale(funcname, atArea, vblObject, paramScale);

		if (!atArea.isIdentity())
			area.transform(atArea);

		// Send to the engine to render
		renderVBL(renderer, area, erase);
	}

	/**
	 * Get the required parameters needed from the JSON to draw two Polygon
	 * 'lines' and render as VBL. This is a convenience function to draw two
	 * lines perpendicular to each other to form a "cross" commonly used to
	 * block LOS for objects like Trees but still show most of the image.
	 * 
	 * @param renderer
	 *            Reference to the ZoneRenderer
	 * @param vblObject
	 *            The JSONObject containing all the coordinates and values to
	 *            needed to draw a rectangle.
	 * @param erase
	 *            Set to true to erase the rectangle in VBL, otherwise draw it
	 * @return the token.
	 * @throws ParserException
	 *             If the minimum required parameters are not present in the
	 *             JSON, throw ParserException
	 */
	private void drawCrossVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase) throws ParserException {
		String funcname = "drawVBL[Cross]";
		// Required Parameters
		String requiredParms[] = { "x", "y", "w", "h" };
		if (!jsonKeysExist(vblObject, requiredParms, funcname))
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,w,h}", funcname));

		int x = getJSONint(vblObject, "x", funcname);
		int y = getJSONint(vblObject, "y", funcname);
		int w = getJSONint(vblObject, "w", funcname);
		int h = getJSONint(vblObject, "h", funcname);

		// Optional Parameters
		double s = getJSONdouble(vblObject, "scale", funcname);
		double r = getJSONdouble(vblObject, "r", funcname);
		double facing = getJSONdouble(vblObject, "facing", funcname);
		float t = (float) getJSONdouble(vblObject, "thickness", funcname);
		boolean useFacing = vblObject.containsKey("facing");

		// Apply Scaling if requested
		if (s != 0) {
			double w2 = w * s;
			double h2 = h * s;
			x = (int) (x - ((w2 - w) / 2));
			y = (int) (y - ((h2 - h) / 2));
			w = (int) w2;
			h = (int) h2;
		}
		// Apply Thickness, defaults to 2f
		BasicStroke stroke = new BasicStroke(t != 0f ? t : 2f);

		// Create the first line
		Polygon line = new Polygon();
		line.addPoint(x, y);
		line.addPoint(x + w, y + h);
		Area area = new Area(stroke.createStrokedShape(line));

		// Create the second line
		line.reset();
		line.addPoint(x, y + h);
		line.addPoint(x + w, y);
		area.add(new Area(stroke.createStrokedShape(line)));

		AffineTransform atArea = new AffineTransform();
		applyTranslate(funcname, atArea, vblObject, paramTranslate);

		// Rotate the Polygon if requested
		if (useFacing || r != 0) {
			// Find the center x,y coords of the rectangle
			int rx = area.getBounds().x + (area.getBounds().width / 2);
			int ry = area.getBounds().y + (area.getBounds().height / 2);

			// Override rx,ry coords if supplied
			String rParms[] = { "rx", "ry" };
			if (jsonKeysExist(vblObject, rParms, funcname)) {
				rx = getJSONint(vblObject, "rx", funcname);
				ry = getJSONint(vblObject, "ry", funcname);
			}
			if (useFacing)
				r = -(facing + 90);

			atArea.rotate(Math.toRadians(r), rx, ry);
		}
		applyScale(funcname, atArea, vblObject, paramScale);

		if (!atArea.isIdentity())
			area.transform(atArea);

		// Send to the engine to render
		renderVBL(renderer, area, erase);
	}

	/**
	 * Get the required parameters needed from the JSON to draw an approximate
	 * circle and render as VBL.
	 * 
	 * @param renderer
	 *            Reference to the ZoneRenderer
	 * @param vblObject
	 *            The JSONObject containing all the coordinates and values to
	 *            needed to draw a rectangle.
	 * @param erase
	 *            Set to true to erase the rectangle in VBL, otherwise draw it
	 * @throws ParserException
	 *             If the minimum required parameters are not present in the
	 *             JSON, throw ParserException
	 */
	private void drawCircleVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase) throws ParserException {
		String funcname = "drawVBL[Circle]";
		// Required Parameters
		String requiredParms[] = { "x", "y", "radius", "sides" };
		if (!jsonKeysExist(vblObject, requiredParms, funcname))
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,radius,sides}", funcname));

		int x = getJSONint(vblObject, "x", funcname);
		int y = getJSONint(vblObject, "y", funcname);
		double radius = getJSONdouble(vblObject, "radius", funcname);
		double sides = getJSONdouble(vblObject, "sides", funcname);

		// Optional Parameters
		int fill = getJSONint(vblObject, "fill", funcname);
		double rotation = getJSONdouble(vblObject, "r", funcname);
		double facing = getJSONdouble(vblObject, "facing", funcname);
		double scale = getJSONdouble(vblObject, "scale", funcname);
		float t = (float) getJSONdouble(vblObject, "thickness", funcname);
		boolean useFacing = vblObject.containsKey("facing");

		// Lets set some sanity limits
		if (sides < 3)
			sides = 3;
		if (sides > 100)
			sides = 100;

		// Apply Scaling if requested
		if (scale != 0) {
			radius = radius * scale;
		}

		// Subtracting "thickness" so drawing stays within "bounds"
		radius -= ((t / 2));
		x -= 1;
		y -= 1;

		// Apply Thickness, defaults to 2f
		BasicStroke stroke = new BasicStroke(t != 0f ? t : 2f);
		Polygon fakeCircle = new Polygon();

		double PI = Math.PI;

		for (int i = 0; i < sides; i++) {
			int Xi = (int) (x + radius * Math.cos(2.0 * PI * i / sides));
			int Yi = (int) (y + radius * Math.sin(2.0 * PI * i / sides));
			fakeCircle.addPoint(Xi, Yi);
		}
		// Create the circle, unfilled
		Area area = new Area(stroke.createStrokedShape(fakeCircle));

		// Fill in the circle if requested
		if (fill != 0)
			area.add(new Area(fakeCircle));

		AffineTransform atArea = new AffineTransform();
		applyTranslate(funcname, atArea, vblObject, paramTranslate);

		// Rotate the Polygon if requested
		if (useFacing || rotation != 0) {
			// Find the center x,y coords of the rectangle
			int rx = area.getBounds().x + (area.getBounds().width / 2);
			int ry = area.getBounds().y + (area.getBounds().height / 2);

			// Override rx,ry coords if supplied
			String rParms[] = { "rx", "ry" };
			if (jsonKeysExist(vblObject, rParms, funcname)) {
				rx = getJSONint(vblObject, "rx", funcname);
				ry = getJSONint(vblObject, "ry", funcname);
			}
			if (useFacing)
				rotation = -(facing + 90);

			atArea.rotate(Math.toRadians(rotation), rx, ry);
		}
		applyScale(funcname, atArea, vblObject, paramScale);

		if (!atArea.isIdentity())
			area.transform(atArea);

		// Send to the engine to render
		renderVBL(renderer, area, erase);
	}

	/**
	 * Get the required parameters needed from the JSON to get/set VBL within a
	 * defined rectangle.
	 * 
	 * @param renderer
	 *            Reference to the ZoneRenderer
	 * @param vblObject
	 *            The JSONObject containing all the coordinates and values to
	 *            needed to draw a rectangle.
	 * @throws ParserException
	 *             If the minimum required parameters are not present in the
	 *             JSON, throw ParserException
	 */
	private Area getVBL(ZoneRenderer renderer, JSONObject vblObject) throws ParserException {
		String funcname = "getVBL[Rectangle]";
		// Required Parameters
		String requiredParms[] = { "x", "y", "w", "h" };
		if (!jsonKeysExist(vblObject, requiredParms, funcname))
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,w,h}", funcname));

		int x = getJSONint(vblObject, "x", funcname);
		int y = getJSONint(vblObject, "y", funcname);
		int w = getJSONint(vblObject, "w", funcname);
		int h = getJSONint(vblObject, "h", funcname);

		// Optional Parameters
		int fill = getJSONint(vblObject, "fill", funcname);
		double s = getJSONdouble(vblObject, "scale", funcname);
		double r = getJSONdouble(vblObject, "r", funcname);
		double facing = getJSONdouble(vblObject, "facing", funcname);
		float t = (float) getJSONdouble(vblObject, "thickness", funcname);
		boolean useFacing = vblObject.containsKey("facing");

		// Allow thickness of 0 and default to 0 to allow complete capture of VBL under a token.
		if (t < 0)
			t = 0; // Set default thickness to 0 if null or negative
		if (w < 4)
			w = 4; // Set width to min of 4, as a 2 pixel thick rectangle as to
					// be at least 4 pixels wide
		if (h < 4)
			h = 4; // Set height to min of 4, as a 2 pixel thick rectangle as to
					// be at least 4 pixels high

		// Apply Scaling if requested
		if (s != 0) {
			// Subtracting "thickness" so drawing stays within "bounds"
			double w2 = (w * s) - t;
			double h2 = (h * s) - t;
			x = (int) (x + (t / 2));
			y = (int) (y + (t / 2));
			w = (int) w2;
			h = (int) h2;
		} else {
			// Subtracting "thickness" so drawing stays within "bounds"
			double w2 = w - t;
			double h2 = h - t;
			x = (int) (x + (t / 2));
			y = (int) (y + (t / 2));
			w = (int) w2;
			h = (int) h2;
		}
		// Apply Thickness, defaults handled above
		BasicStroke stroke = new BasicStroke(t);

		// Create the rectangle, unfilled
		Area area = new Area(stroke.createStrokedShape(new java.awt.Rectangle(x, y, w, h)));

		// Fill in the rectangle if requested
		if (fill != 0)
			area.add(new Area(new java.awt.Rectangle(x, y, w, h)));

		// Rotate the rectangle if requested
		if (useFacing || r != 0) {
			// Find the center x,y coords of the rectangle
			int rx = x + (w / 2);
			int ry = y + (h / 2);

			// Override rx,ry coords if supplied
			String rParms[] = { "rx", "ry" };
			if (jsonKeysExist(vblObject, rParms, funcname)) {
				rx = getJSONint(vblObject, "rx", funcname);
				ry = getJSONint(vblObject, "ry", funcname);
			}
			if (useFacing)
				r = -(facing + 90);

			AffineTransform atArea = new AffineTransform();
			atArea.rotate(Math.toRadians(r), rx, ry);
			area.transform(atArea);
		}
		area.intersect(renderer.getZone().getTopology());
		return area;
	}

	/**
	 * Get the required parameters needed from the JSON to get/set VBL within a
	 * defined rectangle.
	 * 
	 * @param area
	 *            Area passed in to convert to path of points
	 * @param simpleJSON
	 *            Boolean to set output to array of points or key/value pairs
	 */
	private String getAreaPoints(Area area, boolean simpleJSON) {
		ArrayList<double[]> areaPoints = new ArrayList<double[]>();
		double[] coords = new double[6];

		for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
			// The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
			// Because the Area is composed of straight lines
			int type = pi.currentSegment(coords);

			// We record a double array of {segment type, x coord, y coord}
			double[] pathIteratorCoords = { type, coords[0], coords[1] };
			areaPoints.add(pathIteratorCoords);
		}
		// Now that we have the Area defined as commands, lets record the points
		// into a json array of json objects.
		// Each shape will be it's own json object which each object contains an
		// array of x,y coords
		JSONObject polygon = new JSONObject();
		JSONArray linePoints = new JSONArray();
		JSONArray allPolygons = new JSONArray();

		polygon.put("generated", 1);
		polygon.put("shape", "polygon");

		double[] defaultPos = null;
		double[] moveTo = null;

		for (double[] currentElement : areaPoints) {
			// Create a json object to hold the x,y key/value pairs
			JSONObject line = new JSONObject();

			// 2 decimals is precise enough, we will deal in .5 pixels mostly.
			currentElement[1] = Math.floor(currentElement[1] * 100) / 100;
			currentElement[2] = Math.floor(currentElement[2] * 100) / 100;

			// Make the lines
			if (currentElement[0] == PathIterator.SEG_MOVETO) {
				if (defaultPos == null) {
					defaultPos = currentElement;
				} else {
					line.put("x", defaultPos[1]);
					line.put("y", defaultPos[2]);
					linePoints.add(line);
					line = new JSONObject();
				}
				moveTo = currentElement;

				line.put("x", currentElement[1]);
				line.put("y", currentElement[2]);
				linePoints.add(line);
			} else if (currentElement[0] == PathIterator.SEG_LINETO) {
				line.put("x", currentElement[1]);
				line.put("y", currentElement[2]);
				linePoints.add(line);
			} else if (currentElement[0] == PathIterator.SEG_CLOSE) {
				line.put("x", moveTo[1]);
				line.put("y", moveTo[2]);
				linePoints.add(line);
			} else {
				// System.out.println("in getAreaPoints(): found a curve, ignoring");
			}
		}
		if (simpleJSON) {
			int count = 0;
			for (int i = 0; i < linePoints.size(); i++) {
				JSONObject points = (JSONObject) linePoints.get(i);
				allPolygons.add(count, points.get("x"));
				count++;
				allPolygons.add(count, points.get("y"));
				count++;
			}
		} else {
			polygon.put("fill", 1);
			polygon.put("close", 1);
			polygon.put("thickness", 0);
			polygon.put("points", linePoints);
			allPolygons.add(polygon);
		}

		return allPolygons.toString();
	}

	/**
	 * Check to see if all needed parameters/keys in the JSON exist.
	 * 
	 * @param jsonObject
	 *            The JSONObject to validate.
	 * @param parmList
	 *            A String array of keys to look up.
	 * @return boolean Return true only if all keys exist, otherwise return
	 *         false if any key is missing.
	 */
	private boolean jsonKeysExist(JSONObject jsonObject, String[] parmList, String funcname) {
		for (String parm : parmList) {
			if (!jsonObject.containsKey(parm))
				return false;
		}
		return true;
	}

	/**
	 * This is a convenience method to fetch and return an int value from the
	 * JSON if key exists, otherwise return 0.
	 * 
	 * @param jsonObject
	 *            The JSONObject to get key from.
	 * @param key
	 *            The string value to look for in the JSON.
	 * @return An int
	 */
	private int getJSONint(JSONObject jsonObject, String key, String funcname) throws ParserException {
		int value = 0;

		try {
			if (jsonObject.containsKey(key)) {
				Object v = jsonObject.get(key);
				if (v instanceof String)
					value = StringUtil.parseInteger((String) v);
				else if (v instanceof Number)
					value = ((Number) v).intValue();
				else {
					// Is this even possible?
					throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
				}
			}
		} catch (net.sf.json.JSONException e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", funcname, key));
		} catch (ParseException e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeI", funcname, key));
		}
		return value;
	}

	/**
	 * This is a convenience method to fetch and return a double value from the
	 * JSON if key exists, otherwise return 0.
	 * 
	 * @param jsonObject
	 *            The JSON object to get key from.
	 * @param key
	 *            The string value to look for in the JSON.
	 * @return A double
	 */
	private double getJSONdouble(JSONObject jsonObject, String key, String funcname) throws ParserException {
		double value = key.equals("facing") ? -90 : 0;
		try {
			if (jsonObject.containsKey(key)) {
				Object v = jsonObject.get(key);
				if (v instanceof String)
					value = StringUtil.parseDecimal((String) v);
				else if (v instanceof Number)
					value = ((Number) v).doubleValue();
				else {
					// Is this even possible?
					throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
				}
			}
		} catch (net.sf.json.JSONException e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
		} catch (ParseException e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
		}
		return value;
	}

	/**
	 * This is a convenience method to send the VBL Area to be rendered to the
	 * server
	 * 
	 * @param renderer
	 *            Reference to the ZoneRenderer
	 * @param area
	 *            A valid Area containing VBL polygons
	 * @param erase
	 *            Set to true to erase the VBL, otherwise draw it
	 */
	private void renderVBL(ZoneRenderer renderer, Area area, boolean erase) {
		if (erase) {
			renderer.getZone().removeTopology(area);
			MapTool.serverCommand().removeTopology(renderer.getZone().getId(), area);
		} else {
			renderer.getZone().addTopology(area);
			MapTool.serverCommand().addTopology(renderer.getZone().getId(), area);
		}
		renderer.repaint();
	}
}
