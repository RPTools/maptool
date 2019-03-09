/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
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
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * New class extending AbstractFunction to create new "Macro Functions" drawVBL, eraseVBL, getVBL
 *
 * <p>drawVBL(jsonArray) :: Takes an array of JSON Objects containing information to draw a Shape in
 * VBL
 *
 * <p>eraseVBL(jsonArray) :: Takes an array of JSON Objects containing information to erase a Shape
 * in VBL
 *
 * <p>getVBL(jsonArray) :: Get the VBL for a given area and return as array of points
 */
public class VBL_Functions extends AbstractFunction {
  private static final VBL_Functions instance = new VBL_Functions();
  private static final String[] paramTranslate = new String[] {"tx", "ty"};
  private static final String[] paramScale = new String[] {"sx", "sy"};

  private VBL_Functions() {
    super(0, 2, "drawVBL", "eraseVBL", "getVBL", "getTokenVBL", "setTokenVBL", "transferVBL");
  }

  public static VBL_Functions getInstance() {
    return instance;
  }

  private static enum Shape {
    RECTANGLE,
    POLYGON,
    CROSS,
    CIRCLE,
    AUTO,
    NONE
  };

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();

    if (functionName.equals("drawVBL") || functionName.equals("eraseVBL")) {
      boolean erase = false;

      if (parameters.size() != 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));
      }

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (functionName.equals("eraseVBL")) erase = true;

      Object j = JSONMacroFunctions.asJSON(parameters.get(0).toString().toLowerCase());
      if (!(j instanceof JSONObject || j instanceof JSONArray)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.unknownType",
                j == null ? parameters.get(0).toString() : j.toString(),
                functionName));
      }

      JSONArray vblArray = JSONArray.fromObject(j);

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
          case NONE:
            break;
          default:
            break;
        }
      }
    }

    if (functionName.equals("getVBL")) {
      boolean simpleJSON = false; // If true, send only array of x,y

      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));

      if (parameters.isEmpty())
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 2 && !parameters.get(1).equals(BigDecimal.ZERO)) simpleJSON = true;

      Object j = JSONMacroFunctions.asJSON(parameters.get(0).toString().toLowerCase());
      if (!(j instanceof JSONObject || j instanceof JSONArray)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.unknownType",
                j == null ? parameters.get(0).toString() : j.toString(),
                functionName));
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

    if (functionName.equals("getTokenVBL")) {
      Token token;

      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTokenVBL",
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "getTokenVBL"));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", "getTokenVBL", 1, parameters.size()));
      }

      Area vblArea = token.getVBL();
      if (vblArea != null) return getAreaPoints(vblArea, false);
      else return "";
    }

    if (functionName.equals("setTokenVBL")) {
      Token token = null;

      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));

      if (parameters.isEmpty())
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      Object jsonArea = JSONMacroFunctions.asJSON(parameters.get(0).toString().toLowerCase());

      if (parameters.size() == 2) {
        token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTokenVBL",
                  parameters.get(0).toString()));
        }

        if (!(jsonArea instanceof JSONObject || jsonArea instanceof JSONArray)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.unknownType",
                  jsonArea == null ? parameters.get(0).toString() : jsonArea.toString(),
                  functionName));
        }
      } else if (parameters.size() == 1) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "getTokenVBL"));
        }
        if (!(jsonArea instanceof JSONObject || jsonArea instanceof JSONArray)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.unknownType",
                  jsonArea == null ? parameters.get(0).toString() : jsonArea.toString(),
                  functionName));
        }
      }

      Area tokenVBL = new Area();
      JSONArray vblArray = JSONArray.fromObject(jsonArea);
      for (int i = 0; i < vblArray.size(); i++) {
        JSONObject vblObject = vblArray.getJSONObject(i);

        Shape vblShape = Shape.valueOf(vblObject.getString("shape").toUpperCase());
        switch (vblShape) {
          case RECTANGLE:
            tokenVBL.add(drawRectangleVBL(null, vblObject, false));
            break;
          case POLYGON:
            tokenVBL.add(drawPolygonVBL(null, vblObject, false));
            break;
          case CROSS:
            tokenVBL.add(drawCrossVBL(null, vblObject, false));
            break;
          case CIRCLE:
            tokenVBL.add(drawCircleVBL(null, vblObject, false));
            break;
          case AUTO:
            int alpha = getJSONint(vblObject, "alpha", "setTokenVBL[Auto]");
            if (alpha < 0 || alpha > 255)
              throw new ParserException(
                  I18N.getText("macro.function.input.illegalArgumentType", alpha, "0-255"));
            System.out.println("Alpha: " + alpha);
            tokenVBL = TokenVBL.createVblArea(token, alpha);
            break;
          case NONE:
            tokenVBL = null;
            break;
        }
      }

      token.setVBL(tokenVBL);
    }

    if (functionName.equals("transferVBL")) {
      Token token = null;

      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));

      if (parameters.isEmpty())
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      if (parameters.size() == 2) {
        token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTokenVBL",
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 1) {
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "getTokenVBL"));
        }
      }

      Object val = parameters.get(0);
      boolean vblFromToken;

      if (val instanceof Integer) {
        vblFromToken = ((Integer) val).intValue() != 0;
      } else if (val instanceof Boolean) {
        vblFromToken = ((Boolean) val).booleanValue();
      } else {
        try {
          vblFromToken = Integer.parseInt(val.toString()) != 0;
        } catch (NumberFormatException e) {
          vblFromToken = Boolean.parseBoolean(val.toString());
        }
      }

      if (vblFromToken) {
        TokenVBL.renderVBL(renderer, token.getTransformedVBL(), false);
      } else {
        token.setVBL(TokenVBL.getMapVBL_transformed(renderer, token));
      }
    }

    return "";
  }

  /**
   * Get the required parameters needed from the JSON to draw a rectangle and render as VBL.
   *
   * @param renderer Reference to the ZoneRenderer
   * @param vblObject The JSONObject containing all the coordinates and values to needed to draw a
   *     rectangle.
   * @param erase Set to true to erase the rectangle in VBL, otherwise draw it
   * @return
   * @throws ParserException If the minimum required parameters are not present in the JSON, throw
   *     ParserException
   */
  private Area drawRectangleVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase)
      throws ParserException {
    String funcname = "drawVBL[Rectangle]";
    // Required Parameters
    String requiredParms[] = {"x", "y", "w", "h"};
    if (!jsonKeysExist(vblObject, requiredParms, funcname))
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y,w,h}"));

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
    if (fill != 0) area.add(new Area(new java.awt.Rectangle(x, y, w, h)));

    AffineTransform atArea = new AffineTransform();
    applyTranslate(funcname, atArea, vblObject, paramTranslate);

    // Rotate the Polygon if requested
    if (useFacing || r != 0) {
      // Find the center x,y coords of the rectangle
      int rx = area.getBounds().x + (area.getBounds().width / 2);
      int ry = area.getBounds().y + (area.getBounds().height / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(vblObject, rParms, funcname)) {
        rx = getJSONint(vblObject, "rx", funcname);
        ry = getJSONint(vblObject, "ry", funcname);
      }
      if (useFacing) r = -(facing + 90);
      atArea.rotate(Math.toRadians(r), rx, ry);
    }
    applyScale(funcname, atArea, vblObject, paramScale);

    if (!atArea.isIdentity()) area.transform(atArea);

    return TokenVBL.renderVBL(renderer, area, erase);
  }

  private void applyTranslate(
      String funcname, AffineTransform at, JSONObject vblObject, String[] params)
      throws ParserException {
    if (jsonKeysExist(vblObject, params, funcname)) {
      double tx = getJSONdouble(vblObject, "tx", funcname);
      double ty = getJSONdouble(vblObject, "ty", funcname);
      at.translate(tx, ty);
    }
  }

  private void applyScale(
      String funcname, AffineTransform at, JSONObject vblObject, String[] params)
      throws ParserException {
    if (jsonKeysExist(vblObject, params, funcname)) {
      double sx = getJSONdouble(vblObject, "sx", funcname);
      double sy = getJSONdouble(vblObject, "sy", funcname);
      at.scale(sx, sy);
    }
  }

  /**
   * Get the required parameters needed from the JSON to draw a Polygon and render as VBL.
   *
   * @param renderer Reference to the ZoneRenderer
   * @param vblObject The JSONObject containing all the coordinates and values to needed to draw a
   *     rectangle.
   * @param erase Set to true to erase the rectangle in VBL, otherwise draw it
   * @return
   * @throws ParserException If the minimum required parameters are not present in the JSON, throw
   *     ParserException
   */
  private Area drawPolygonVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase)
      throws ParserException {
    String funcname = "drawVBL[Polygon]";
    String requiredParms[] = {"points"};
    if (!jsonKeysExist(vblObject, requiredParms, funcname))
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeA", "points", funcname));

    // Get all the x,y coords for the Polygon, must have at least 2
    JSONArray points = vblObject.getJSONArray("points");
    if (points.size() < 2) {
      throw new ParserException(
          I18N.getText("macro.function.json.getInvalidEndIndex", funcname, 2, points.size()));
    }
    // Optional Parameters
    int fill = getJSONint(vblObject, "fill", funcname);
    int close = getJSONint(vblObject, "close", funcname);
    double r = getJSONdouble(vblObject, "r", funcname);
    double facing = getJSONdouble(vblObject, "facing", funcname);
    float t = (float) getJSONdouble(vblObject, "thickness", funcname);
    boolean useFacing = vblObject.containsKey("facing");

    if (!vblObject.containsKey("thickness")) t = 2; // Set default thickness if no value is passed.

    Area area = null;

    if (close == 0) {
      // User requests for polygon to not be closed, so a Path is used
      Path2D path = new Path2D.Double();
      double lastX = 0;
      double lastY = 0;

      for (int i = 0; i < points.size(); i++) {
        JSONObject point = points.getJSONObject(i);

        String requiredPointParms[] = {"x", "y"};
        if (!jsonKeysExist(point, requiredPointParms, funcname))
          throw new ParserException(
              I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y}", funcname));

        double x = getJSONdouble(point, "x", funcname);
        double y = getJSONdouble(point, "y", funcname);

        if (path.getCurrentPoint() == null) {
          path.moveTo(x, y);
        } else if (!(lastX == x && lastY == y)) {
          path.lineTo(x, y);
          lastX = x;
          lastY = y;
        }
      }
      BasicStroke stroke =
          new BasicStroke(t > 0f ? t : 0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
      area = new Area(stroke.createStrokedShape(path));
    } else {
      // User requests for polygon to be closed, so a Polygon is used which is automatically closed
      Polygon poly = new Polygon();

      for (int i = 0; i < points.size(); i++) {
        JSONObject point = points.getJSONObject(i);

        String requiredPointParms[] = {"x", "y"};
        if (!jsonKeysExist(point, requiredPointParms, funcname))
          throw new ParserException(
              I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y}", funcname));

        int x = getJSONint(point, "x", funcname);
        int y = getJSONint(point, "y", funcname);

        poly.addPoint(x, y);
      }
      // A strokedShape will not be filled in and have a defined thickness.
      if (fill == 0) {
        BasicStroke stroke =
            new BasicStroke(t > 0f ? t : 0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
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
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(vblObject, rParms, funcname)) {
        rx = getJSONint(vblObject, "rx", funcname);
        ry = getJSONint(vblObject, "ry", funcname);
      }
      if (useFacing) r = -(facing + 90);

      atArea.rotate(Math.toRadians(r), rx, ry);
    }
    applyScale(funcname, atArea, vblObject, paramScale);

    if (!atArea.isIdentity()) area.transform(atArea);

    return TokenVBL.renderVBL(renderer, area, erase);
  }

  /**
   * Get the required parameters needed from the JSON to draw two Polygon 'lines' and render as VBL.
   * This is a convenience function to draw two lines perpendicular to each other to form a "cross"
   * commonly used to block LOS for objects like Trees but still show most of the image.
   *
   * @param renderer Reference to the ZoneRenderer
   * @param vblObject The JSONObject containing all the coordinates and values to needed to draw a
   *     rectangle.
   * @param erase Set to true to erase the rectangle in VBL, otherwise draw it
   * @return the token.
   * @throws ParserException If the minimum required parameters are not present in the JSON, throw
   *     ParserException
   */
  private Area drawCrossVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase)
      throws ParserException {
    String funcname = "drawVBL[Cross]";
    // Required Parameters
    String requiredParms[] = {"x", "y", "w", "h"};
    if (!jsonKeysExist(vblObject, requiredParms, funcname))
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,w,h}", funcname));

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
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(vblObject, rParms, funcname)) {
        rx = getJSONint(vblObject, "rx", funcname);
        ry = getJSONint(vblObject, "ry", funcname);
      }
      if (useFacing) r = -(facing + 90);

      atArea.rotate(Math.toRadians(r), rx, ry);
    }
    applyScale(funcname, atArea, vblObject, paramScale);

    if (!atArea.isIdentity()) area.transform(atArea);

    return TokenVBL.renderVBL(renderer, area, erase);
  }

  /**
   * Get the required parameters needed from the JSON to draw an approximate circle and render as
   * VBL.
   *
   * @param renderer Reference to the ZoneRenderer
   * @param vblObject The JSONObject containing all the coordinates and values to needed to draw a
   *     rectangle.
   * @param erase Set to true to erase the rectangle in VBL, otherwise draw it
   * @return
   * @throws ParserException If the minimum required parameters are not present in the JSON, throw
   *     ParserException
   */
  private Area drawCircleVBL(ZoneRenderer renderer, JSONObject vblObject, boolean erase)
      throws ParserException {
    String funcname = "drawVBL[Circle]";
    // Required Parameters
    String requiredParms[] = {"x", "y", "radius", "sides"};
    if (!jsonKeysExist(vblObject, requiredParms, funcname))
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,radius,sides}", funcname));

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
    if (sides < 3) sides = 3;
    if (sides > 100) sides = 100;

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
    if (fill != 0) area.add(new Area(fakeCircle));

    AffineTransform atArea = new AffineTransform();
    applyTranslate(funcname, atArea, vblObject, paramTranslate);

    // Rotate the Polygon if requested
    if (useFacing || rotation != 0) {
      // Find the center x,y coords of the rectangle
      int rx = area.getBounds().x + (area.getBounds().width / 2);
      int ry = area.getBounds().y + (area.getBounds().height / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(vblObject, rParms, funcname)) {
        rx = getJSONint(vblObject, "rx", funcname);
        ry = getJSONint(vblObject, "ry", funcname);
      }
      if (useFacing) rotation = -(facing + 90);

      atArea.rotate(Math.toRadians(rotation), rx, ry);
    }
    applyScale(funcname, atArea, vblObject, paramScale);

    if (!atArea.isIdentity()) area.transform(atArea);

    return TokenVBL.renderVBL(renderer, area, erase);
  }

  /**
   * Get the required parameters needed from the JSON to get/set VBL within a defined rectangle.
   *
   * @param renderer Reference to the ZoneRenderer
   * @param vblObject The JSONObject containing all the coordinates and values to needed to draw a
   *     rectangle.
   * @throws ParserException If the minimum required parameters are not present in the JSON, throw
   *     ParserException
   */
  private Area getVBL(ZoneRenderer renderer, JSONObject vblObject) throws ParserException {
    String funcname = "getVBL[Rectangle]";
    // Required Parameters
    String requiredParms[] = {"x", "y", "w", "h"};
    if (!jsonKeysExist(vblObject, requiredParms, funcname))
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,w,h}", funcname));

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
    if (t < 0) t = 0; // Set default thickness to 0 if null or negative
    if (w < 4) w = 4; // Set width to min of 4, as a 2 pixel thick rectangle as to
    // be at least 4 pixels wide
    if (h < 4) h = 4; // Set height to min of 4, as a 2 pixel thick rectangle as to
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
    if (fill != 0) area.add(new Area(new java.awt.Rectangle(x, y, w, h)));

    // Rotate the rectangle if requested
    if (useFacing || r != 0) {
      // Find the center x,y coords of the rectangle
      int rx = x + (w / 2);
      int ry = y + (h / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(vblObject, rParms, funcname)) {
        rx = getJSONint(vblObject, "rx", funcname);
        ry = getJSONint(vblObject, "ry", funcname);
      }
      if (useFacing) r = -(facing + 90);

      AffineTransform atArea = new AffineTransform();
      atArea.rotate(Math.toRadians(r), rx, ry);
      area.transform(atArea);
    }
    area.intersect(renderer.getZone().getTopology());
    return area;
  }

  /**
   * Get the required parameters needed from the JSON to get/set VBL within a defined rectangle.
   *
   * @param area Area passed in to convert to path of points
   * @param simpleJSON Boolean to set output to array of points or key/value pairs
   */
  private String getAreaPoints(Area area, boolean simpleJSON) {
    ArrayList<double[]> areaPoints = new ArrayList<double[]>();
    double[] coords = new double[6];

    for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
      // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
      // Because the Area is composed of straight lines
      int type = pi.currentSegment(coords);

      // We record a double array of {segment type, x coord, y coord}
      double[] pathIteratorCoords = {type, coords[0], coords[1]};
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
   * @param jsonObject The JSONObject to validate.
   * @param parmList A String array of keys to look up.
   * @return boolean Return true only if all keys exist, otherwise return false if any key is
   *     missing.
   */
  private boolean jsonKeysExist(JSONObject jsonObject, String[] parmList, String funcname) {
    for (String parm : parmList) {
      if (!jsonObject.containsKey(parm)) return false;
    }
    return true;
  }

  /**
   * This is a convenience method to fetch and return an int value from the JSON if key exists,
   * otherwise return 0.
   *
   * @param jsonObject The JSONObject to get key from.
   * @param key The string value to look for in the JSON.
   * @return An int
   */
  private int getJSONint(JSONObject jsonObject, String key, String funcname)
      throws ParserException {
    int value = 0;

    try {
      if (jsonObject.containsKey(key)) {
        Object v = jsonObject.get(key);
        if (v instanceof String) value = StringUtil.parseInteger((String) v);
        else if (v instanceof Number) value = ((Number) v).intValue();
        else {
          // Is this even possible?
          throw new ParserException(
              I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
        }
      }
    } catch (net.sf.json.JSONException e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", funcname, key));
    } catch (ParseException e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", funcname, key));
    }
    return value;
  }

  /**
   * This is a convenience method to fetch and return a double value from the JSON if key exists,
   * otherwise return 0.
   *
   * @param jsonObject The JSON object to get key from.
   * @param key The string value to look for in the JSON.
   * @return A double
   */
  private double getJSONdouble(JSONObject jsonObject, String key, String funcname)
      throws ParserException {
    double value = key.equals("facing") ? -90 : 0;
    try {
      if (jsonObject.containsKey(key)) {
        Object v = jsonObject.get(key);
        if (v instanceof String) value = StringUtil.parseDecimal((String) v);
        else if (v instanceof Number) value = ((Number) v).doubleValue();
        else {
          // Is this even possible?
          throw new ParserException(
              I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
        }
      }
    } catch (net.sf.json.JSONException e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
    } catch (ParseException e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
    }
    return value;
  }
}
