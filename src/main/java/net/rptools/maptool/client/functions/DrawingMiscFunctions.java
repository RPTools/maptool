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
import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.tool.drawing.DrawingPointerTool;
import net.rptools.maptool.client.tool.drawing.TemplatePointerTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrawingMiscFunctions extends DrawingFunctions {
  private static final DrawingMiscFunctions instance = new DrawingMiscFunctions();

  public static DrawingMiscFunctions getInstance() {
    return instance;
  }

  private static final String FUNC_DUPLICATE_DRAWABLE = "duplicateDrawable";
  private static final String FUNC_GET_SELECTED_DRAWABLES = "getSelectedDrawables";
  private static final String FUNC_GET_DRAWABLES_UNDER_TOKEN = "getDrawablesUnderToken";
  private static final String FUNC_GET_TOKENS_OVER_DRAWABLE = "getTokensOverDrawable";
  private static final String FUNC_MOVE_DRAWABLE = "moveDrawable";

  private final Logger log = LogManager.getLogger(DrawingMiscFunctions.class);

  private DrawingMiscFunctions() {
    super(
        0,
        5,
        "findDrawings",
        "refreshDrawing",
        "bringDrawingToFront",
        "sendDrawingToBack",
        "movedOverDrawing",
        "removeDrawing",
        FUNC_DUPLICATE_DRAWABLE,
        FUNC_GET_SELECTED_DRAWABLES,
        FUNC_GET_DRAWABLES_UNDER_TOKEN,
        FUNC_GET_TOKENS_OVER_DRAWABLE,
        FUNC_MOVE_DRAWABLE);
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    checkTrusted(functionName);

    if (FUNC_DUPLICATE_DRAWABLE.equalsIgnoreCase(functionName)) {
      // [drawingRef [, mapRef]]
      FunctionUtil.checkNumberParam(FUNC_DUPLICATE_DRAWABLE, parameters, 0, 2);

      ZoneRenderer renderer =
          FunctionUtil.getZoneRendererFromParam(FUNC_DUPLICATE_DRAWABLE, parameters, 1);
      Zone zone = renderer.getZone();
      DrawnElement de =
          !parameters.isEmpty()
              ? findDrawnElement(FUNC_DUPLICATE_DRAWABLE, zone, parameters.getFirst().toString())
              : getSingleSelectedDrawnElement(FUNC_DUPLICATE_DRAWABLE, zone);
      return duplicateDrawnElement(renderer, de);

    } else if (FUNC_GET_SELECTED_DRAWABLES.equalsIgnoreCase(functionName)) {
      // [delim]
      FunctionUtil.checkNumberParam(FUNC_GET_SELECTED_DRAWABLES, parameters, 0, 1);

      String delim = !parameters.isEmpty() ? parameters.getFirst().toString() : ",";
      return getSelectedDrawables(delim);

    } else if (FUNC_GET_DRAWABLES_UNDER_TOKEN.equalsIgnoreCase(functionName)
        || FUNC_GET_TOKENS_OVER_DRAWABLE.equalsIgnoreCase(functionName)) {
      // get drawables -> [verbose [, threshold [, drawablesOnLayer [,tokenRef [, mapRef]]]]]
      // get tokens -> [verbose [, threshold [, tokensOnLayer [ ,drawingRef [, mapRef ]]]]]
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 5);

      // (optional) verbose - defaults to false, i.e. return just an array of ids
      boolean verbose =
          !parameters.isEmpty()
              ? FunctionUtil.paramAsBoolean(functionName, parameters, 0, false)
              : false;

      // (optional) threshold - defaults to 0, i.e. any overlap
      double threshold =
          parameters.size() > 1
              ? FunctionUtil.paramAsDouble(functionName, parameters, 1, false)
              : 0;

      // (optional) layer, defaults to null, i.e. all layers
      Zone.Layer layer = null;
      if (parameters.size() > 2) {
        String argLayer = parameters.get(2).toString().toUpperCase();
        if (!argLayer.equals("*")) {
          // getByName also handles turning HIDDEN -> GM
          layer = Zone.Layer.getByName(argLayer);
        }
      }

      // (optional) mapRef - defaults to current zone/map
      Zone zone = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 4).getZone();

      if (FUNC_GET_DRAWABLES_UNDER_TOKEN.equalsIgnoreCase(functionName)) {
        // (optional) tokenRef - defaults to current token
        Token token =
            FunctionUtil.getTokenFromParam(
                resolver, FUNC_GET_DRAWABLES_UNDER_TOKEN, parameters, 3, 4);
        return getDrawablesUnderToken(zone, verbose, threshold, layer, token);

      } else if (FUNC_GET_TOKENS_OVER_DRAWABLE.equalsIgnoreCase(functionName)) {
        // (optional) drawableRef - defaults to the drawable selected by the respective pointer tool
        DrawnElement de =
            parameters.size() > 3
                ? findDrawnElement(
                    FUNC_GET_TOKENS_OVER_DRAWABLE, zone, parameters.get(3).toString())
                : getSingleSelectedDrawnElement(FUNC_GET_TOKENS_OVER_DRAWABLE, zone);
        return getTokensOverDrawable(zone, verbose, threshold, layer, de);

      } else {
        // should not get here but this branch is needed otherwise IDE moans
        return null;
      }

    } else if (FUNC_MOVE_DRAWABLE.equalsIgnoreCase(functionName)) {
      // x, y, [, units [, drawingRef [, mapRef]]]
      FunctionUtil.checkNumberParam(FUNC_MOVE_DRAWABLE, parameters, 2, 5);

      // x & y
      int x = FunctionUtil.paramAsInteger(FUNC_MOVE_DRAWABLE, parameters, 0, false);
      int y = FunctionUtil.paramAsInteger(FUNC_MOVE_DRAWABLE, parameters, 1, false);

      // (optional) units - defaults to PIXELS
      int units =
          parameters.size() > 2
              ? FunctionUtil.paramAsInteger(functionName, parameters, 2, false)
              : DrawableUnit.getPixelsIndex();
      DrawableUnit drawableUnit = DrawableUnit.valueOfIndex(units);
      if (drawableUnit == null && parameters.size() > 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.drawable.unknownDrawableUnits", functionName, parameters.get(2)));
      }

      // (optional) renderer & zone/map - defaults to the current
      ZoneRenderer renderer =
          FunctionUtil.getZoneRendererFromParam(FUNC_MOVE_DRAWABLE, parameters, 4);
      Zone zone = renderer.getZone();

      // (optional) drawn element - defaults to the drawable selected by the respective pointer tool
      DrawnElement de =
          parameters.size() > 3
              ? findDrawnElement(FUNC_MOVE_DRAWABLE, zone, parameters.get(3).toString())
              : getSingleSelectedDrawnElement(FUNC_MOVE_DRAWABLE, zone);

      moveDrawable(renderer, de, x, y, drawableUnit);
      return "";

    } else {
      String mapName = parameters.get(0).toString();
      String drawing = parameters.get(1).toString();
      Zone map = FunctionUtil.getZoneRenderer(functionName, mapName).getZone();
      if ("movedOverDrawing".equalsIgnoreCase(functionName)) {
        FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
        String jsonPath = parameters.get(2).toString();
        GUID guid = getGUID(functionName, drawing);
        DrawnElement de = getDrawnElement(functionName, map, guid);
        return getCrossedPoints(map, de, jsonPath);
      } else if ("findDrawings".equalsIgnoreCase(functionName)) {
        FunctionUtil.checkNumberParam(functionName, parameters, 2, 3);
        List<DrawnElement> drawableList = map.getAllDrawnElements();
        List<String> drawingList = findDrawings(drawableList, drawing);
        String delim = parameters.size() > 2 ? parameters.get(2).toString() : ",";
        if ("json".equalsIgnoreCase(delim)) {
          JsonArray json = new JsonArray();
          for (String val : drawingList) {
            json.add(val);
          }
          return json;
        } else return StringFunctions.getInstance().join(drawingList, delim);
      } else {
        FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
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
        } else if ("removeDrawing".equalsIgnoreCase(functionName)) {
          MapTool.serverCommand().undoDraw(map.getId(), guid);
          return "";
        }
      }
      return null;
    }
  }

  /**
   * Check if a given path has crossed through a specified drawing or template.
   *
   * @param map the map
   * @param de the drawn element
   * @param pathStr the path as a string
   * @return a json array or all points crossed
   */
  private JsonArray getCrossedPoints(final Zone map, final DrawnElement de, final String pathStr) {
    List<Map<String, Integer>> pathPoints = convertJSONStringToList(pathStr);
    JsonArray returnPoints = new JsonArray();
    Area a = de.getDrawable().getArea(map);
    int cnt = 0;
    Point previousPoint = new Point();
    for (Map<String, Integer> entry : pathPoints) {
      Point currentPoint = new Point(entry.get("x"), entry.get("y"));
      if (cnt > 0) {
        Line2D l2d = new Line2D.Double(previousPoint, currentPoint);
        BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Area lineArea = new Area(stroke.createStrokedShape(l2d));
        lineArea.intersect(a);
        if (!lineArea.isEmpty()) {
          JsonObject firstPoint = new JsonObject();
          JsonObject secondPoint = new JsonObject();
          firstPoint.addProperty("x1", (int) previousPoint.getX());
          firstPoint.addProperty("y1", (int) previousPoint.getY());
          secondPoint.addProperty("x2", (int) currentPoint.getX());
          secondPoint.addProperty("y2", (int) currentPoint.getY());
          returnPoints.add(firstPoint);
          returnPoints.add(secondPoint);
        }
      }
      previousPoint = new Point(entry.get("x"), entry.get("y"));
      cnt++;
    }
    return returnPoints;
  }

  /**
   * Create a list of path points
   *
   * @param pointsString a string of points
   * @return a list of the path points
   */
  private List<Map<String, Integer>> convertJSONStringToList(final String pointsString) {

    JsonElement json = null;

    json = JSONMacroFunctions.getInstance().asJsonElement(pointsString);

    ArrayList<Map<String, Integer>> pathPoints = new ArrayList<>();
    if (json != null && json.isJsonArray()) {
      JsonArray jarr = json.getAsJsonArray();
      for (JsonElement ele : jarr) {
        if (ele.isJsonObject()) {
          JsonObject jobj = ele.getAsJsonObject();
          var point = new HashMap<String, Integer>();
          point.put("x", jobj.get("x").getAsInt());
          point.put("y", jobj.get("y").getAsInt());
          pathPoints.add(point);
        }
      }
    }
    return pathPoints;
  }

  /**
   * Searches the list of drawn elements for items with a matching name
   *
   * @param drawableList List of drawables.
   * @param name String name to match or * to match all
   * @return List<String> of ids
   */
  private List<String> findDrawings(List<DrawnElement> drawableList, String name) {
    List<String> drawingList = new LinkedList<String>();
    for (DrawnElement de : drawableList) {
      if (de.getDrawable() instanceof AbstractDrawing) {
        if (name.equals("*") || name.equals(((AbstractDrawing) de.getDrawable()).getName())) {
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

  /**
   * Duplicates a drawing or template in place.
   *
   * @param renderer where to draw
   * @param de the drawn element to duplicate
   * @return the id of the new duplicate drawn element
   */
  public GUID duplicateDrawnElement(ZoneRenderer renderer, DrawnElement de) {
    Drawable d = de.getDrawable();
    AbstractDrawing ad = (AbstractDrawing) d.copy();
    ad.setId(new GUID());
    // Draw it
    MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), ad);
    // Allow it to be undone
    renderer.getZone().addDrawable(de.getPen(), ad);
    renderer.repaint();
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();
    return ad.getId();
  }

  /**
   * Move a drawable to position x, y on a map
   *
   * @param renderer where to draw
   * @param de the drawn element to move
   * @param x where to move to on the x-axis
   * @param y where to move to on the y-axis
   * @param drawableUnit CELLS, DISTANCE, or PIXELS
   */
  private void moveDrawable(
      ZoneRenderer renderer, DrawnElement de, int x, int y, DrawableUnit drawableUnit) {

    Zone zone = renderer.getZone();

    // determine the drawable type and hand off moving it
    switch (de.getDrawable()) {
      case DrawablesGroup dg2 -> {
        moveDrawable(zone, dg2, x, y, drawableUnit);
        MapTool.serverCommand().updateDrawing(zone.getId(), de.getPen(), de);
        zone.updateDrawable(de, de.getPen());
      }
      case LineSegment ls -> {
        moveDrawable(zone, ls, x, y, drawableUnit);
        MapTool.serverCommand().updateDrawing(zone.getId(), de.getPen(), de);
        zone.updateDrawable(de, de.getPen());
      }
      case ShapeDrawable sd -> {
        moveDrawable(zone, sd, x, y, drawableUnit);
        MapTool.serverCommand().updateDrawing(zone.getId(), de.getPen(), de);
        zone.updateDrawable(de, de.getPen());
      }
      case AbstractTemplate at -> moveDrawable(zone, at, de, x, y, drawableUnit);
      case null, default -> {
        // drawn element is not of a type currently moveable by this method
      }
    }

    MapTool.serverCommand().updateDrawing(zone.getId(), de.getPen(), de);
    zone.updateDrawable(de, de.getPen());
    renderer.repaint();
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();
  }

  /**
   * Move a {@link DrawablesGroup} and its nested contents to position x, y on a map. Note that
   * <code>DrawablesGroup</code>s cannot currently contain templates.
   *
   * @param zone where to draw
   * @param dg the drawables group to move
   * @param x where to move to on the x-axis
   * @param y where to move to on the y-axis
   * @param drawableUnit whether x & y are given in CELLS, DISTANCE, or PIXELS
   */
  private void moveDrawable(Zone zone, DrawablesGroup dg, int x, int y, DrawableUnit drawableUnit) {
    for (DrawnElement de : dg.getDrawableList()) {
      if (de.getDrawable() instanceof DrawablesGroup dg2) {
        moveDrawable(zone, dg2, x, y, drawableUnit);
      } else if (de.getDrawable() instanceof LineSegment ls) {
        moveDrawable(zone, ls, x, y, drawableUnit);
      } else if (de.getDrawable() instanceof ShapeDrawable sd) {
        moveDrawable(zone, sd, x, y, drawableUnit);
      }
      MapTool.serverCommand().updateDrawing(zone.getId(), de.getPen(), de);
      zone.updateDrawable(de, de.getPen());
    }
  }

  /**
   * Move a {@link LineSegment} to position x, y on a map
   *
   * @param zone where to draw
   * @param ls the line segment to move
   * @param x where to move on the x-axis
   * @param y where to move on the y-axis
   * @param drawableUnit whether x & y are given in CELLS, DISTANCE, or PIXELS
   */
  private void moveDrawable(Zone zone, LineSegment ls, int x, int y, DrawableUnit drawableUnit) {

    ZonePoint zp = null;
    switch (drawableUnit) {
      case CELLS -> zp = zone.getGrid().convert(new CellPoint(x, y));
      case DISTANCE ->
          zp =
              zone.getGrid()
                  .convert(
                      new CellPoint(
                          DrawableUnit.convert(zone, x, drawableUnit, DrawableUnit.CELLS),
                          DrawableUnit.convert(zone, y, drawableUnit, DrawableUnit.CELLS)));
      case PIXELS -> zp = new ZonePoint(x, y);
      case null, default -> {
        // as PIXELS
        zp = new ZonePoint(x, y);
      }
    }

    ls.translate(zp.x - ls.getBounds(zone).x, zp.y - ls.getBounds(zone).y);
  }

  /**
   * Move a {@link ShapeDrawable} to position x, y on a map
   *
   * @param zone where to draw
   * @param sd the shape drawable to move
   * @param x where to move to on the x-axis
   * @param y where to move to on the y-axis
   * @param drawableUnit whether x & y are given in CELLS, DISTANCE, or PIXELS
   */
  private void moveDrawable(Zone zone, ShapeDrawable sd, int x, int y, DrawableUnit drawableUnit) {

    ZonePoint zp = null;
    switch (drawableUnit) {
      case CELLS -> zp = zone.getGrid().convert(new CellPoint(x, y));
      case DISTANCE ->
          zp =
              zone.getGrid()
                  .convert(
                      new CellPoint(
                          DrawableUnit.convert(zone, x, drawableUnit, DrawableUnit.CELLS),
                          DrawableUnit.convert(zone, y, drawableUnit, DrawableUnit.CELLS)));
      case PIXELS -> zp = new ZonePoint(x, y);
      case null, default -> {
        // as PIXELS
        zp = new ZonePoint(x, y);
      }
    }

    if (sd.getShape() instanceof RectangularShape rs) {
      rs.setFrame(zp.x, zp.y, rs.getWidth(), rs.getHeight());
    } else if (sd.getShape() instanceof Polygon p) {
      p.translate(zp.x - p.getBounds().x, zp.y - p.getBounds().y);
    }
  }

  /**
   * Moves an {@link AbstractTemplate}'s bounds to position x, y on a map, and if applicable the
   * pathVertex to a related position.
   *
   * @param zone where to draw
   * @param at the abstract template to move
   * @param de the drawn element
   * @param x where to move to on the x-axis
   * @param y where to move to on the y-axis
   * @param drawableUnit whether x & y are given in CELLS, DISTANCE, or PIXELS
   */
  private void moveDrawable(
      Zone zone, AbstractTemplate at, DrawnElement de, int x, int y, DrawableUnit drawableUnit) {

    Rectangle bounds = at.getArea(zone).getBounds();
    ZonePoint vertex = at.getVertex();
    ZonePoint diff = new ZonePoint(vertex.x - bounds.x, vertex.y - bounds.y);

    CellPoint cp;
    switch (drawableUnit) {
      case CELLS -> cp = new CellPoint(x, y);
      case DISTANCE ->
          cp =
              new CellPoint(
                  DrawableUnit.convert(zone, x, drawableUnit, DrawableUnit.CELLS),
                  DrawableUnit.convert(zone, y, drawableUnit, DrawableUnit.CELLS));
      case PIXELS -> cp = zone.getGrid().convert(new ZonePoint(x, y));
      case null, default -> {
        // as CELLS
        cp = new CellPoint(x, y);
      }
    }
    ZonePoint zp0 = zone.getGrid().convert(cp);
    ZonePoint zp = new ZonePoint(zp0.x + diff.x, zp0.y + diff.y);

    if (at instanceof LineTemplate lt && !(at instanceof WallTemplate)) {
      // While WallTemplate extends LineTemplate, changing a WallTemplate's path vertex breaks
      // getBounds(), so don't change it for WallTemplates!
      ZonePoint pathVertexMove =
          new ZonePoint(
              zp.x + lt.getPathVertex().x - lt.getVertex().x,
              zp.y + lt.getPathVertex().y - lt.getVertex().y);
      lt.setPathVertex(pathVertexMove);
    }
    if (at instanceof LineCellTemplate lct) {
      ZonePoint pathVertexMove =
          new ZonePoint(
              zp.x + lct.getPathVertex().x - lct.getVertex().x,
              zp.y + lct.getPathVertex().y - lct.getVertex().y);
      lct.setPathVertex(pathVertexMove);
    }
    at.setVertex(zp);
    de.setDrawable(at);
  }

  /**
   * Get selected drawings or templates relevant to the respective {@link DrawingPointerTool} or
   * {@link TemplatePointerTool}.
   *
   * @param delim the delimiter to use for the selected drawings output
   * @return the selected drawables
   */
  public String getSelectedDrawables(String delim) {

    List<GUID> selectedDrawables = new ArrayList<>();

    // get a list of selected drawings depending on the selected tool
    Object selectedTool = MapTool.getFrame().getToolbox().getSelectedTool().getClass();
    if (selectedTool.equals(TemplatePointerTool.class)) {
      selectedDrawables = TemplatePointerTool.getSelectedDrawables();
    } else if (selectedTool.equals(DrawingPointerTool.class)) {
      selectedDrawables = DrawingPointerTool.getSelectedDrawables();
    }

    // output the list using the desired delimiter
    if (delim.equalsIgnoreCase("json")) {
      JsonArray selectedDrawablesJsonArray = new JsonArray();
      for (GUID selectedDrawing : selectedDrawables) {
        selectedDrawablesJsonArray.add(selectedDrawing.toString());
      }
      return selectedDrawablesJsonArray.toString();
    } else {
      return selectedDrawables.stream().map(GUID::toString).collect(Collectors.joining(delim));
    }
  }

  /**
   * For a specified drawing/template, get any tokens that overlap by an amount.
   *
   * @param zone the map
   * @param verbose whether to show the amount of the overlap in the results
   * @param threshold the minimum amount of overlap between the tokens and the drawing
   * @param layer if supplied, only check tokens on this layer otherwise all layers
   * @param drawnElement the drawing/template
   * @return drawings under the token with their % overlap
   */
  private String getTokensOverDrawable(
      Zone zone, boolean verbose, double threshold, Zone.Layer layer, DrawnElement drawnElement) {

    JsonArray tokensOverDrawable = new JsonArray();

    List<Token> tokens;
    if (layer != null) {
      tokens = zone.getTokensOnLayer(layer);
    } else {
      tokens = zone.getAllTokens();
    }

    for (Token token : tokens) {
      double overlap = calculateOverlap(zone, token, drawnElement);
      if (overlap > 0 && overlap >= threshold) {
        String id = token.getId().toString();
        if (verbose) {
          JsonObject tokenWithOverlap = new JsonObject();
          tokenWithOverlap.addProperty("tokenId", id);
          tokenWithOverlap.addProperty("overlap", overlap);
          tokensOverDrawable.add(tokenWithOverlap);
        } else {
          tokensOverDrawable.add(id);
        }
      }
    }

    return tokensOverDrawable.toString();
  }

  /**
   * For a specified token, get any drawings/templates that are under it by an amount.
   *
   * @param zone the map
   * @param verbose whether to show the amount of the overlap in the results
   * @param threshold the minimum amount of overlap between the token and the drawings
   * @param layer if supplied, only check drawings/templates on this layer otherwise all layers
   * @param token the token
   * @return drawings under the token with their % overlap
   */
  private String getDrawablesUnderToken(
      Zone zone, boolean verbose, double threshold, Zone.Layer layer, Token token) {

    JsonArray drawablesUnderToken = new JsonArray();

    List<DrawnElement> drawnElements;
    if (layer != null) {
      drawnElements = zone.getDrawnElements(layer);
    } else {
      drawnElements = zone.getAllDrawnElements();
    }

    for (DrawnElement drawnElement : drawnElements) {
      double overlap = calculateOverlap(zone, token, drawnElement);
      if (overlap > 0 && overlap >= threshold) {
        String id = drawnElement.getDrawable().getId().toString();
        if (verbose) {
          JsonObject drawableWithOverlap = new JsonObject();
          drawableWithOverlap.addProperty("drawableId", id);
          drawableWithOverlap.addProperty("overlap", overlap);
          drawablesUnderToken.add(drawableWithOverlap);
        } else {
          drawablesUnderToken.add(id);
        }
      }
    }

    return drawablesUnderToken.toString();
  }

  /**
   * For a given token, break down its rectangle bounds into a mini-grid of smaller rectangles and
   * count the number of these rectangle centers that are within the {@link Drawable}'s {@link
   * Area}.
   *
   * <p>Limitations/areas for improvement:
   *
   * <ul>
   *   <li>the number of mini-rectangles checked is the same regardless of token size
   *   <li>does not account for different grid type shapes (so iso and hex grid threshold may be
   *       incorrect)
   *   <li>is not based on a token's occupied cells
   *
   * @param zone the map
   * @param token the token
   * @param drawnElement the drawn element
   * @return % of token bounds that overlaps with drawing
   */
  private double calculateOverlap(Zone zone, Token token, DrawnElement drawnElement) {

    Grid grid = zone.getGrid();

    Rectangle tokenFootprintBounds =
        token
            .getFootprint(grid)
            .getBounds(grid, grid.convert(new ZonePoint(token.getX(), token.getY())));
    Rectangle tokenImageBounds = token.getImageBounds(zone);
    Rectangle tokenBounds = token.isSnapToGrid() ? tokenFootprintBounds : tokenImageBounds;

    Area drawingArea = drawnElement.getDrawable().getArea(zone);

    int countOverlap = 0;

    // first do inexpensive check if bounds overlap, we can ignore those which don't overlap
    if (tokenBounds.intersects(drawingArea.getBounds())) {

      // set x & y dimensions to increment by
      double incX = tokenBounds.width / grid.getCellWidth();
      double incY = tokenBounds.height / grid.getCellHeight();

      // starting at token left iterate to token right
      for (double x = tokenBounds.x; x < (tokenBounds.x + tokenBounds.width); x = x + incX) {
        // starting at token top iterate to token bottom
        for (double y = tokenBounds.y; y < (tokenBounds.y + tokenBounds.height); y = y + incY) {
          // check if the middle of each square is within the drawing area
          if (drawingArea.contains((int) (x + incX / 2), (int) (y + incY / 2))) {
            countOverlap++;
          }
        }
      }
    }
    return countOverlap / (grid.getCellWidth() * grid.getCellHeight());
  }
}
