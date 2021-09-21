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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL.JTS_SimplifyMethodType;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

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
 *
 * <p>drawMBL(jsonArray) :: Takes an array of JSON Objects containing information to draw a Shape in
 * MBL
 *
 * <p>eraseMBL(jsonArray) :: Takes an array of JSON Objects containing information to erase a Shape
 * in MBL
 *
 * <p>getMBL(jsonArray) :: Get the MBL for a given area and return as array of points
 *
 * <p>transferVBL(direction[, delete][, tokenId] :: move or copy VBL between token and VBL layer
 */
public class Topology_Functions extends AbstractFunction {

  private static final Topology_Functions instance = new Topology_Functions();
  private static final String[] paramTranslate = new String[] {"tx", "ty"};
  private static final String[] paramScale = new String[] {"sx", "sy"};

  private Topology_Functions() {
    super(
        0,
        3,
        "drawVBL",
        "eraseVBL",
        "getVBL",
        "drawMBL",
        "eraseMBL",
        "getMBL",
        "getTokenVBL",
        "setTokenVBL",
        "transferVBL");
  }

  public static Topology_Functions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    int results = -1;

    if (functionName.equals("drawVBL")
        || functionName.equals("eraseVBL")
        || functionName.equals("drawMBL")
        || functionName.equals("eraseMBL")) {
      boolean erase = false;
      if (parameters.size() != 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));
      }

      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }

      if (functionName.equals("eraseVBL") || functionName.equals("eraseMBL")) {
        erase = true;
      }

      JsonElement json =
          JSONMacroFunctions.getInstance().asJsonElement(parameters.get(0).toString());

      JsonArray topologyArray;
      if (json.isJsonArray()) {
        topologyArray = json.getAsJsonArray();
      } else if (json.isJsonObject()) {
        topologyArray = new JsonArray();
        topologyArray.add(json.getAsJsonObject());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.unknownType",
                json == null ? parameters.get(0).toString() : json.toString(),
                functionName));
      }

      for (int i = 0; i < topologyArray.size(); i++) {
        JsonObject topologyObject = topologyArray.get(i).getAsJsonObject();

        Shape topologyShape =
            Shape.valueOf(topologyObject.get("shape").getAsString().toUpperCase());
        boolean isTerrainVbl =
            topologyObject.has("terrain")
                && BigInteger.ONE.equals(topologyObject.get("terrain").getAsBigInteger());

        Zone.TopologyMode mode =
            (functionName.equals("drawVBL") || functionName.equals("eraseVBL"))
                ? (isTerrainVbl ? Zone.TopologyMode.TERRAIN_VBL : Zone.TopologyMode.VBL)
                : Zone.TopologyMode.MBL;
        Area newArea =
            switch (topologyShape) {
              case RECTANGLE -> makeRectangle(topologyObject, functionName);
              case POLYGON -> makePolygon(topologyObject, functionName);
              case CROSS -> makeCross(topologyObject, functionName);
              case CIRCLE -> makeCircle(topologyObject, functionName);
              case NONE -> null;
              default -> null;
            };
        if (newArea != null) {
          TokenVBL.renderTopology(renderer, newArea, erase, mode);
        }
      }
    } else if (functionName.equals("getVBL") || functionName.equals("getMBL")) {
      // TODO Wiki needs updating. `format` (parameter 2) is optional, defaulting to 0.

      Zone.TopologyMode mode =
          functionName.equals("getVBL") ? Zone.TopologyMode.VBL : Zone.TopologyMode.MBL;
      boolean simpleJSON = false; // If true, send only array of x,y

      if (parameters.size() > 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 2, parameters.size()));
      }

      if (parameters.isEmpty()) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));
      }

      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }

      if (parameters.size() >= 2 && !parameters.get(1).equals(BigDecimal.ZERO)) {
        simpleJSON = true;
      }

      JsonElement json =
          JSONMacroFunctions.getInstance().asJsonElement(parameters.get(0).toString());
      // TODO Update the wiki. The first parameter can be a single shape or a list thereof.
      JsonArray topologyArray;
      if (json.isJsonArray()) {
        topologyArray = json.getAsJsonArray();
      } else if (json.isJsonObject()) {
        topologyArray = new JsonArray();
        topologyArray.add(json.getAsJsonObject());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.unknownType",
                json == null ? parameters.get(0).toString() : json.toString(),
                functionName));
      }

      Area topologyArea = new Area();
      Area terrainVblArea = new Area();
      for (int i = 0; i < topologyArray.size(); i++) {
        JsonObject topologyObject = topologyArray.get(i).getAsJsonObject();
        boolean isTerrainVbl =
            (mode == Zone.TopologyMode.VBL)
                && topologyObject.has("terrain")
                && BigInteger.ONE.equals(topologyObject.get("terrain").getAsBigInteger());
        Area tempTopologyArea = getTopology(renderer, topologyObject, mode, functionName);

        (isTerrainVbl ? terrainVblArea : topologyArea).add(tempTopologyArea);
      }

      if (simpleJSON) {
        // Build a single list of points for all areas.
        // TODO Is this good behaviour? Not sure what the use case is for a raw list of coordinates.
        return getAreaPoints(List.of(topologyArea, terrainVblArea));
      } else {
        // Build separate objects for each area.
        JsonArray allShapes = new JsonArray();
        if (!topologyArea.isEmpty()) {
          allShapes.add(
              getAreaShapeObject(topologyArea, mode == Zone.TopologyMode.MBL ? null : false));
        }
        if (!terrainVblArea.isEmpty()) {
          allShapes.add(getAreaShapeObject(terrainVblArea, true));
        }
        return allShapes.toString();
      }
    } else if (functionName.equals("getTokenVBL")) {
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
        MapToolVariableResolver res = (MapToolVariableResolver) resolver;
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
      if (vblArea != null) {
        JsonArray allShapes = new JsonArray();
        allShapes.add(getAreaShapeObject(vblArea, token.getIsTerrainVbl()));
        return allShapes.toString();
      } else {
        return "";
      }
    } else if (functionName.equals("setTokenVBL")) {
      Token token = null;

      if (parameters.size() > 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (parameters.isEmpty()) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));
      }

      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }

      JsonElement jsonArea =
          JSONMacroFunctions.getInstance().asJsonElement(parameters.get(0).toString());
      JsonArray vblArray;
      if (jsonArea.isJsonArray()) {
        vblArray = jsonArea.getAsJsonArray();
      } else if (jsonArea.isJsonObject()) {
        vblArray = new JsonArray();
        vblArray.add(jsonArea.getAsJsonObject());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.unknownType",
                jsonArea == null ? parameters.get(0).toString() : jsonArea.toString(),
                functionName));
      }

      if (parameters.size() == 2) {
        token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);
      } else if (parameters.size() == 1) {
        MapToolVariableResolver res = (MapToolVariableResolver) resolver;
        token = res.getTokenInContext();
      }
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getTokenVBL"));
      }

      Area tokenVBL = new Area();
      for (int i = 0; i < vblArray.size(); i++) {
        JsonObject vblObject = vblArray.get(i).getAsJsonObject();
        if (vblObject.has("terrain")) {
          boolean isTerrainToken = token.getIsTerrainVbl();
          boolean isTerrainVbl = BigInteger.ONE.equals(vblObject.get("terrain").getAsBigInteger());
          if (isTerrainToken != isTerrainVbl) {
            continue;
          }
        }

        Shape vblShape = Shape.valueOf(vblObject.get("shape").getAsString().toUpperCase());
        switch (vblShape) {
          case RECTANGLE:
            tokenVBL.add(makeRectangle(vblObject, functionName));
            break;
          case POLYGON:
            tokenVBL.add(makePolygon(vblObject, functionName));
            break;
          case CROSS:
            tokenVBL.add(makeCross(vblObject, functionName));
            break;
          case CIRCLE:
            tokenVBL.add(makeCircle(vblObject, functionName));
            break;
          case AUTO:
            tokenVBL = autoGenerateVBL(token, vblObject);

            if (tokenVBL != null) {
              int tokenVblOptimizedPointCount = 0;
              for (PathIterator pi = tokenVBL.getPathIterator(null); !pi.isDone(); pi.next()) {
                tokenVblOptimizedPointCount++;
              }

              results = tokenVblOptimizedPointCount;
            }

            break;
          case NONE:
            // Setting to null causes various Token VBL updating to be skipped
            // during event handling. Leaving it as an empty Area fixed that.
            // tokenVBL = null;
            break;
        }
      }
      // Replace with new VBL
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setVBL, tokenVBL);
    } else if (functionName.equals("transferVBL")) {
      // TODO Wiki needs updating to not talk about yellow and blue VBL since there is now orange
      // and cyan.
      Token token = null;

      if (parameters.size() > 3) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (parameters.isEmpty()) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notenoughparms", functionName, 1, parameters.size()));
      }

      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }

      // make sure only to check the last parameter as token if it is not the BigDecimal for delete
      if (parameters.size() >= 2
          && (!(parameters.get(parameters.size() - 1) instanceof BigDecimal))) {
        token =
            FindTokenFunctions.findToken(parameters.get(parameters.size() - 1).toString(), null);

        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTokenVBL",
                  parameters.get(0).toString()));
        }
      } else {
        MapToolVariableResolver res = (MapToolVariableResolver) resolver;
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "transferVBL"));
        }
      }
      boolean isTerrainVbl = token.getIsTerrainVbl();

      boolean delete = false;
      if (parameters.size() >= 2 && BigDecimal.ONE.equals(parameters.get(1))) {
        delete = true;
      }

      Object val = parameters.get(0);
      boolean vblFromToken;

      if (val instanceof Integer) {
        vblFromToken = (Integer) val != 0;
      } else if (val instanceof Boolean) {
        vblFromToken = (Boolean) val;
      } else {
        try {
          vblFromToken = Integer.parseInt(val.toString()) != 0;
        } catch (NumberFormatException e) {
          vblFromToken = Boolean.parseBoolean(val.toString());
        }
      }

      if (vblFromToken) {
        TokenVBL.renderTopology(
            renderer,
            token.getTransformedVBL(),
            false,
            isTerrainVbl ? Zone.TopologyMode.TERRAIN_VBL : Zone.TopologyMode.VBL);
        if (delete) {
          token.setVBL(null);
        }
      } else {
        Area vbl = TokenVBL.getVBL_underToken(renderer, token, isTerrainVbl);
        token.setVBL(TokenVBL.getMapVBL_transformed(renderer, token, isTerrainVbl));
        if (delete) {
          TokenVBL.renderTopology(
              renderer,
              vbl,
              true,
              isTerrainVbl ? Zone.TopologyMode.TERRAIN_VBL : Zone.TopologyMode.VBL);
        }
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }

    if (results >= 0) {
      return results;
    } else {
      return "";
    }
  }

  /**
   * Auto generate VBL using new token vbl optimzation options
   *
   * @param token the token to generate VBL from
   * @param jsonParms the parameters json passed in
   * @return the generated token vbl as an Area
   * @throws ParserException
   */
  private Area autoGenerateVBL(Token token, JsonObject jsonParms) throws ParserException {
    final int sensitivity = getJSONint(jsonParms, "sensitivity", 10, 0, 255, "setTokenVBL[Auto]");
    final int inverse = getJSONint(jsonParms, "inverse", 0, 0, 1, "setTokenVBL[Auto]");
    final int r = getJSONint(jsonParms, "r", 0, 0, 255, "setTokenVBL[Auto]");
    final int g = getJSONint(jsonParms, "g", 0, 0, 255, "setTokenVBL[Auto]");
    final int b = getJSONint(jsonParms, "b", 0, 0, 255, "setTokenVBL[Auto]");
    final int a = getJSONint(jsonParms, "a", 0, 0, 255, "setTokenVBL[Auto]");
    final int level = getJSONint(jsonParms, "level", 2, 0, 100, "setTokenVBL[Auto]");
    final String method =
        getJSONasString(
            jsonParms, "method", JTS_SimplifyMethodType.getDefault().name(), "setTokenVBL[Auto]");

    Color color = new Color(r, g, b, a);
    final boolean inverseVbl = inverse == 1;

    return TokenVBL.createOptimizedVblArea(token, sensitivity, inverseVbl, color, level, method);
  }

  /**
   * Get the required parameters needed from the JSON to draw a rectangle and render as topology.
   *
   * @param topologyObject The JsonObject containing all the coordinates and values to needed to
   *     draw a rectangle.
   * @return the topology area
   * @throws ParserException If the minimum required parameters are not present in the JSON.
   */
  private Area makeRectangle(JsonObject topologyObject, String funcname) throws ParserException {
    funcname += "[Rectangle]";
    // Required Parameters
    String[] requiredParms = {"x", "y", "w", "h"};
    if (!jsonKeysExist(topologyObject, requiredParms, funcname)) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y,w,h}"));
    }

    int x = getJSONint(topologyObject, "x", funcname);
    int y = getJSONint(topologyObject, "y", funcname);
    int w = getJSONint(topologyObject, "w", funcname);
    int h = getJSONint(topologyObject, "h", funcname);

    // Optional Parameters
    int fill = getJSONint(topologyObject, "fill", funcname);
    double s = getJSONdouble(topologyObject, "scale", funcname);
    double r = getJSONdouble(topologyObject, "r", funcname);
    double facing = getJSONdouble(topologyObject, "facing", funcname);
    float t = (float) getJSONdouble(topologyObject, "thickness", funcname);
    boolean useFacing = topologyObject.has("facing");

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
    double w2;
    double h2;
    if (s != 0) {
      // Subtracting "thickness" so drawing stays within "bounds"
      w2 = (w * s) - t;
      h2 = (h * s) - t;
    } else {
      // Subtracting "thickness" so drawing stays within "bounds"
      w2 = w - t;
      h2 = h - t;
    }
    x = (int) (x + (t / 2));
    y = (int) (y + (t / 2));
    w = (int) w2;
    h = (int) h2;
    // Apply Thickness, defaults to 2f
    BasicStroke stroke = new BasicStroke(t != 0f ? t : 2f);

    // Create the rectangle, unfilled
    Area area = new Area(stroke.createStrokedShape(new java.awt.Rectangle(x, y, w, h)));

    // Fill in the rectangle if requested
    if (fill != 0) {
      area.add(new Area(new java.awt.Rectangle(x, y, w, h)));
    }

    AffineTransform atArea = new AffineTransform();
    applyTranslate(funcname, atArea, topologyObject, paramTranslate);

    // Rotate the Polygon if requested
    if (useFacing || r != 0) {
      // Find the center x,y coords of the rectangle
      int rx = area.getBounds().x + (area.getBounds().width / 2);
      int ry = area.getBounds().y + (area.getBounds().height / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(topologyObject, rParms, funcname)) {
        rx = getJSONint(topologyObject, "rx", funcname);
        ry = getJSONint(topologyObject, "ry", funcname);
      }
      if (useFacing) {
        r = -(facing + 90);
      }
      atArea.rotate(Math.toRadians(r), rx, ry);
    }
    applyScale(funcname, atArea, topologyObject, paramScale);

    if (!atArea.isIdentity()) {
      area.transform(atArea);
    }

    return area;
  }

  /**
   * Get the required parameters needed from the JSON to draw a Polygon and render as topology.
   *
   * @param topologyObject The JsonObject containing all the coordinates and values to needed to
   *     draw a polygon.
   * @return the topology area
   * @throws ParserException If the minimum required parameters are not present in the JSON.
   */
  private Area makePolygon(JsonObject topologyObject, String funcname) throws ParserException {
    funcname += "[Polygon]";
    String requiredParms[] = {"points"};
    if (!jsonKeysExist(topologyObject, requiredParms, funcname)) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeA", funcname, "points"));
    }

    // Get all the x,y coords for the Polygon, must have at least 2
    JsonArray points = topologyObject.get("points").getAsJsonArray();
    if (points.size() < 2) {
      throw new ParserException(
          I18N.getText("macro.function.json.getInvalidEndIndex", funcname, 2, points.size()));
    }
    // Optional Parameters
    int fill = getJSONint(topologyObject, "fill", funcname);
    int close = getJSONint(topologyObject, "close", funcname);
    double r = getJSONdouble(topologyObject, "r", funcname);
    double facing = getJSONdouble(topologyObject, "facing", funcname);
    float t = (float) getJSONdouble(topologyObject, "thickness", funcname);
    boolean useFacing = topologyObject.has("facing");

    if (!topologyObject.has("thickness")) {
      t = 2; // Set default thickness if no value is passed.
    }

    Area area = null;

    if (close == 0) {
      // User requests for polygon to not be closed, so a Path is used
      Path2D path = new Path2D.Double();
      double lastX = 0;
      double lastY = 0;

      for (int i = 0; i < points.size(); i++) {
        JsonObject point = points.get(i).getAsJsonObject();

        String requiredPointParms[] = {"x", "y"};
        if (!jsonKeysExist(point, requiredPointParms, funcname)) {
          throw new ParserException(
              I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y}"));
        }

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
          new BasicStroke(Math.max(t, 0f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
      area = new Area(stroke.createStrokedShape(path));
    } else {
      // User requests for polygon to be closed, so a Polygon is used which is automatically
      // closed
      Polygon poly = new Polygon();

      for (int i = 0; i < points.size(); i++) {
        JsonObject point = points.get(i).getAsJsonObject();

        String requiredPointParms[] = {"x", "y"};
        if (!jsonKeysExist(point, requiredPointParms, funcname)) {
          throw new ParserException(
              I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y}"));
        }

        int x = getJSONint(point, "x", funcname);
        int y = getJSONint(point, "y", funcname);

        poly.addPoint(x, y);
      }
      // A strokedShape will not be filled in and have a defined thickness.
      if (fill == 0) {
        BasicStroke stroke =
            new BasicStroke(Math.max(t, 0f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        area = new Area(stroke.createStrokedShape(poly));
      } else {
        area = new Area(poly);
      }
    }
    AffineTransform atArea = new AffineTransform();
    applyTranslate(funcname, atArea, topologyObject, paramTranslate);

    // Rotate the Polygon if requested
    if (useFacing || r != 0) {
      // Find the center x,y coords of the rectangle
      int rx = area.getBounds().x + (area.getBounds().width / 2);
      int ry = area.getBounds().y + (area.getBounds().height / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(topologyObject, rParms, funcname)) {
        rx = getJSONint(topologyObject, "rx", funcname);
        ry = getJSONint(topologyObject, "ry", funcname);
      }
      if (useFacing) {
        r = -(facing + 90);
      }

      atArea.rotate(Math.toRadians(r), rx, ry);
    }
    applyScale(funcname, atArea, topologyObject, paramScale);

    if (!atArea.isIdentity()) {
      area.transform(atArea);
    }

    return area;
  }

  /**
   * Get the required parameters needed from the JSON to draw two Polygon 'lines' and render as
   * topology. This is a convenience function to draw two lines perpendicular to each other to form
   * a "cross" commonly used to block LOS for objects like Trees but still show most of the image.
   *
   * @param topologyObject The JsonObject containing all the coordinates and values to needed to
   *     draw a cross.
   * @return the topology area
   * @throws ParserException If the minimum required parameters are not present in the JSON.
   */
  private Area makeCross(JsonObject topologyObject, String funcname) throws ParserException {
    funcname += "[Cross]";
    // Required Parameters
    String requiredParms[] = {"x", "y", "w", "h"};
    if (!jsonKeysExist(topologyObject, requiredParms, funcname)) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y,w,h}"));
    }

    int x = getJSONint(topologyObject, "x", funcname);
    int y = getJSONint(topologyObject, "y", funcname);
    int w = getJSONint(topologyObject, "w", funcname);
    int h = getJSONint(topologyObject, "h", funcname);

    // Optional Parameters
    double s = getJSONdouble(topologyObject, "scale", funcname);
    double r = getJSONdouble(topologyObject, "r", funcname);
    double facing = getJSONdouble(topologyObject, "facing", funcname);
    float t = (float) getJSONdouble(topologyObject, "thickness", funcname);
    boolean useFacing = topologyObject.has("facing");

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
    applyTranslate(funcname, atArea, topologyObject, paramTranslate);

    // Rotate the Polygon if requested
    if (useFacing || r != 0) {
      // Find the center x,y coords of the rectangle
      int rx = area.getBounds().x + (area.getBounds().width / 2);
      int ry = area.getBounds().y + (area.getBounds().height / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(topologyObject, rParms, funcname)) {
        rx = getJSONint(topologyObject, "rx", funcname);
        ry = getJSONint(topologyObject, "ry", funcname);
      }
      if (useFacing) {
        r = -(facing + 90);
      }

      atArea.rotate(Math.toRadians(r), rx, ry);
    }
    applyScale(funcname, atArea, topologyObject, paramScale);

    if (!atArea.isIdentity()) {
      area.transform(atArea);
    }

    return area;
  }

  /**
   * Get the required parameters needed from the JSON to draw an approximate circle and render as
   * topology.
   *
   * @param topologyObject The JsonObject containing all the coordinates and values to needed to
   *     draw a circle.
   * @return the topology area
   * @throws ParserException If the minimum required parameters are not present in the JSON.
   */
  private Area makeCircle(JsonObject topologyObject, String funcname) throws ParserException {
    funcname += "[Circle]";
    // Required Parameters
    String requiredParms[] = {"x", "y", "radius", "sides"};
    if (!jsonKeysExist(topologyObject, requiredParms, funcname)) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", funcname, "{x,y,radius,sides}"));
    }

    int x = getJSONint(topologyObject, "x", funcname);
    int y = getJSONint(topologyObject, "y", funcname);
    double radius = getJSONdouble(topologyObject, "radius", funcname);
    double sides = getJSONdouble(topologyObject, "sides", funcname);

    // Optional Parameters
    int fill = getJSONint(topologyObject, "fill", funcname);
    double rotation = getJSONdouble(topologyObject, "r", funcname);
    double facing = getJSONdouble(topologyObject, "facing", funcname);
    double scale = getJSONdouble(topologyObject, "scale", funcname);
    float t = (float) getJSONdouble(topologyObject, "thickness", funcname);
    boolean useFacing = topologyObject.has("facing");

    // Lets set some sanity limits
    if (sides < 3) {
      sides = 3;
    }
    if (sides > 100) {
      sides = 100;
    }

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
    if (fill != 0) {
      area.add(new Area(fakeCircle));
    }

    AffineTransform atArea = new AffineTransform();
    applyTranslate(funcname, atArea, topologyObject, paramTranslate);

    // Rotate the Polygon if requested
    if (useFacing || rotation != 0) {
      // Find the center x,y coords of the rectangle
      int rx = area.getBounds().x + (area.getBounds().width / 2);
      int ry = area.getBounds().y + (area.getBounds().height / 2);

      // Override rx,ry coords if supplied
      String rParms[] = {"rx", "ry"};
      if (jsonKeysExist(topologyObject, rParms, funcname)) {
        rx = getJSONint(topologyObject, "rx", funcname);
        ry = getJSONint(topologyObject, "ry", funcname);
      }
      if (useFacing) {
        rotation = -(facing + 90);
      }

      atArea.rotate(Math.toRadians(rotation), rx, ry);
    }
    applyScale(funcname, atArea, topologyObject, paramScale);

    if (!atArea.isIdentity()) {
      area.transform(atArea);
    }

    return area;
  }

  private void applyTranslate(
      String funcname, AffineTransform at, JsonObject topologyObject, String[] params)
      throws ParserException {
    if (jsonKeysExist(topologyObject, params, funcname)) {
      double tx = getJSONdouble(topologyObject, "tx", funcname);
      double ty = getJSONdouble(topologyObject, "ty", funcname);
      at.translate(tx, ty);
    }
  }

  private void applyScale(
      String funcname, AffineTransform at, JsonObject topologyObject, String[] params)
      throws ParserException {
    if (jsonKeysExist(topologyObject, params, funcname)) {
      double sx = getJSONdouble(topologyObject, "sx", funcname);
      double sy = getJSONdouble(topologyObject, "sy", funcname);
      at.scale(sx, sy);
    }
  }

  /**
   * Get the required parameters needed from the JSON to get/set topology within a defined
   * rectangle.
   *
   * @param renderer Reference to the ZoneRenderer
   * @param topologyObject JsonObject containing all the coordinates and values needed to draw a
   *     rectangle.
   * @param mode The topology mode to operate in.
   * @return the topology area.
   * @throws ParserException If the minimum required parameters are not present in the JSON.
   */
  private Area getTopology(
      ZoneRenderer renderer, JsonObject topologyObject, Zone.TopologyMode mode, String funcname)
      throws ParserException {
    // Required Parameters
    String requiredParms[] = {"x", "y", "w", "h"};
    if (!jsonKeysExist(topologyObject, requiredParms, funcname)) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeI", "{x,y,w,h}", funcname));
    }

    int x = getJSONint(topologyObject, "x", funcname);
    int y = getJSONint(topologyObject, "y", funcname);
    int w = getJSONint(topologyObject, "w", funcname);
    int h = getJSONint(topologyObject, "h", funcname);

    // Optional Parameters
    int fill = getJSONint(topologyObject, "fill", funcname);
    double s = getJSONdouble(topologyObject, "scale", funcname);
    double r = getJSONdouble(topologyObject, "r", funcname);
    double facing = getJSONdouble(topologyObject, "facing", funcname);
    float t = (float) getJSONdouble(topologyObject, "thickness", funcname);
    boolean useFacing = topologyObject.has("facing");

    // Allow thickness of 0 and default to 0 to allow complete capture of topology under a token.
    if (t < 0) {
      t = 0; // Set default thickness to 0 if null or negative
    }
    if (w < 4) {
      w = 4; // Set width to min of 4, as a 2 pixel thick rectangle as to
    }
    // be at least 4 pixels wide
    if (h < 4) {
      h = 4; // Set height to min of 4, as a 2 pixel thick rectangle as to
    }
    // be at least 4 pixels high

    // Apply Scaling if requested
    double w2;
    double h2;
    if (s != 0) {
      // Subtracting "thickness" so drawing stays within "bounds"
      w2 = (w * s) - t;
      h2 = (h * s) - t;
    } else {
      // Subtracting "thickness" so drawing stays within "bounds"
      w2 = w - t;
      h2 = h - t;
    }
    x = (int) (x + (t / 2));
    y = (int) (y + (t / 2));
    w = (int) w2;
    h = (int) h2;
    // Apply Thickness, defaults handled above
    BasicStroke stroke = new BasicStroke(t);

    // Create the rectangle, unfilled
    Area area = new Area(stroke.createStrokedShape(new java.awt.Rectangle(x, y, w, h)));

    // Fill in the rectangle if requested
    if (fill != 0) {
      area.add(new Area(new java.awt.Rectangle(x, y, w, h)));
    }

    // Rotate the rectangle if requested
    if (useFacing || r != 0) {
      // Find the center x,y coords of the rectangle
      int rx = x + (w / 2);
      int ry = y + (h / 2);

      // Override rx,ry coords if supplied
      String[] rParms = {"rx", "ry"};
      if (jsonKeysExist(topologyObject, rParms, funcname)) {
        rx = getJSONint(topologyObject, "rx", funcname);
        ry = getJSONint(topologyObject, "ry", funcname);
      }
      if (useFacing) {
        r = -(facing + 90);
      }

      AffineTransform atArea = new AffineTransform();
      atArea.rotate(Math.toRadians(r), rx, ry);
      area.transform(atArea);
    }

    // Note: when COMBINED, the overlap between VBL and MBL is returned.
    if (mode.isRegularVbl()) {
      area.intersect(renderer.getZone().getTopology());
    }
    if (mode.isTerrainVbl()) {
      area.intersect(renderer.getZone().getTerrainVbl());
    }
    if (mode.isMbl()) {
      area.intersect(renderer.getZone().getTopologyTerrain());
    }

    return area;
  }

  private JsonObject getAreaShapeObject(Area area, @Nullable Boolean isTerrain) {
    // Each shape will be it's own json object which each object contains an  array of x,y coords
    JsonObject polygon = new JsonObject();

    polygon.addProperty("generated", 1);
    polygon.addProperty("shape", "polygon");
    if (isTerrain != null) {
      polygon.addProperty("terrain", isTerrain ? 1 : 0);
    }

    polygon.addProperty("fill", 1);
    polygon.addProperty("close", 1);
    polygon.addProperty("thickness", 0);

    JsonArray points = new JsonArray();
    consumeAreaPoints(
        area,
        (x, y) -> {
          var point = new JsonObject();
          point.addProperty("x", x);
          point.addProperty("y", y);
          points.add(point);
        });
    polygon.add("points", points);

    return polygon;
  }

  private JsonArray getAreaPoints(List<Area> areas) {
    JsonArray allPoints = new JsonArray();
    for (var area : areas) {
      if (area.isEmpty()) {
        continue;
      }

      consumeAreaPoints(
          area,
          (x, y) -> {
            allPoints.add(x);
            allPoints.add(y);
          });
    }
    return allPoints;
  }

  private void consumeAreaPoints(Area area, BiConsumer<Double, Double> pointConsumer) {
    ArrayList<double[]> areaPoints = new ArrayList<>();
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

    double[] defaultPos = null;
    double[] moveTo = null;

    for (double[] currentElement : areaPoints) {
      // 2 decimals is precise enough, we will deal in .5 pixels mostly.
      currentElement[1] = Math.floor(currentElement[1] * 100) / 100;
      currentElement[2] = Math.floor(currentElement[2] * 100) / 100;

      // Make the lines
      if (currentElement[0] == PathIterator.SEG_MOVETO) {
        if (defaultPos == null) {
          defaultPos = currentElement;
        } else {
          pointConsumer.accept(defaultPos[1], defaultPos[2]);
        }
        moveTo = currentElement;

        pointConsumer.accept(currentElement[1], currentElement[2]);
      } else if (currentElement[0] == PathIterator.SEG_LINETO) {
        pointConsumer.accept(currentElement[1], currentElement[2]);
      } else if (currentElement[0] == PathIterator.SEG_CLOSE) {
        pointConsumer.accept(moveTo[1], moveTo[2]);
      } else {
        // System.out.println("in getAreaPoints(): found a curve, ignoring");
      }
    }
  }

  /**
   * Check to see if all needed parameters/keys in the JSON exist.
   *
   * @param jsonObject The JsonObject to validate.
   * @param parmList A String array of keys to look up.
   * @return boolean Return true only if all keys exist, otherwise return false if any key is
   *     missing.
   */
  private boolean jsonKeysExist(JsonObject jsonObject, String[] parmList, String funcname) {
    for (String parm : parmList) {
      if (!jsonObject.has(parm)) {
        return false;
      }
    }
    return true;
  }

  /**
   * This is a convenience method to fetch and return an int value from the JSON if key exists,
   * otherwise return 0.
   *
   * @param jsonObject The JsonObject to get key from.
   * @param key The string value to look for in the JSON.
   * @param defaultVal The default value to return if key not found
   * @return A String
   */
  private String getJSONasString(
      JsonObject jsonObject, String key, String defaultVal, String funcname)
      throws ParserException {
    String value = defaultVal;

    if (jsonObject.has(key)) {
      JsonElement v = jsonObject.get(key);
      if (v.getAsJsonPrimitive().isString()) {
        return v.getAsJsonPrimitive().getAsString();
      } else {
        // Is this even possible?
        throw new ParserException(
            I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
      }
    }
    return value;
  }

  /**
   * This is a convenience method to fetch and return an int value from the JSON if key exists,
   * otherwise return 0.
   *
   * @param jsonObject The JsonObject to get key from.
   * @param key The string value to look for in the JSON.
   * @return An int
   */
  private int getJSONint(JsonObject jsonObject, String key, String funcname)
      throws ParserException {

    int value = 0;

    if (jsonObject.has(key)) {
      JsonElement v = jsonObject.get(key);
      if (v.getAsJsonPrimitive().isNumber()) {
        return v.getAsJsonPrimitive().getAsInt();
      } else {
        // Is this even possible?
        throw new ParserException(
            I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
      }
    }
    return value;
  }

  /**
   * This is a convenience method to fetch and return an int value from the JSON if key exists,
   * otherwise return 0.
   *
   * @param jsonObject The JsonObject to get key from.
   * @param key The string value to look for in the JSON.
   * @param defaultVal The default value to return if key not found
   * @param min minimal acceptable value
   * @param max maximum acceptable value
   * @return An int
   */
  private int getJSONint(
      JsonObject jsonObject, String key, int defaultVal, int min, int max, String funcname)
      throws ParserException {
    int value = defaultVal;

    if (jsonObject.has(key)) {
      JsonElement v = jsonObject.get(key);
      if (v.getAsJsonPrimitive().isNumber()) {
        value = v.getAsJsonPrimitive().getAsInt();

        if (value < min || value > max) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.input.illegalArgumentType",
                  value,
                  min + "-" + max + " for " + key));
        }

        return value;
      } else {
        // Is this even possible?
        throw new ParserException(
            I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
      }
    }
    return value;
  }

  /**
   * This is a convenience method to fetch and return a double value from the JSON if key exists,
   * otherwise return 0.
   *
   * @param jsonObject The JsonObject to get key from.
   * @param key The string value to look for in the JSON.
   * @return A double
   */
  private double getJSONdouble(JsonObject jsonObject, String key, String funcname)
      throws ParserException {
    double value = key.equals("facing") ? -90 : 0;
    if (jsonObject.has(key)) {
      JsonElement v = jsonObject.get(key);
      if (v.getAsJsonPrimitive().isNumber()) {
        return v.getAsJsonPrimitive().getAsDouble();
      }
      // Is this even possible?
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeD", funcname, key));
    }
    return value;
  }

  private enum Shape {
    RECTANGLE,
    POLYGON,
    CROSS,
    CIRCLE,
    AUTO,
    NONE
  }
}
