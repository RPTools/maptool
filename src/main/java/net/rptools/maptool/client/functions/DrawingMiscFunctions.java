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
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

public class DrawingMiscFunctions extends DrawingFunctions {
  private static final DrawingMiscFunctions instance = new DrawingMiscFunctions();

  public static DrawingMiscFunctions getInstance() {
    return instance;
  }

  private DrawingMiscFunctions() {
    super(
        2,
        3,
        "findDrawings",
        "refreshDrawing",
        "bringDrawingToFront",
        "sendDrawingToBack",
        "movedOverDrawing",
        "removeDrawing");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    checkTrusted(functionName);
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
}
