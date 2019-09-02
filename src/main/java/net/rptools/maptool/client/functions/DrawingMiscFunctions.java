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
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
        "movedOverDrawing");
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    checkTrusted(functionName);
    String mapName = parameters.get(0).toString();
    String drawing = parameters.get(1).toString();
    Zone map = getNamedMap(functionName, mapName).getZone();
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
      if ("json".equalsIgnoreCase(delim)) return JSONArray.fromObject(drawingList);
      else return StringFunctions.getInstance().join(drawingList, delim);
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
      }
    }
    return null;
  }

  private JSONArray getCrossedPoints(final Zone map, final DrawnElement de, final String pathStr) {
    List<Map<String, Integer>> pathPoints = convertJSONStringToList(pathStr);
    JSONArray returnPoints = new JSONArray();
    Area a = de.getDrawable().getArea();
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
          JSONObject firstPoint = new JSONObject();
          JSONObject secondPoint = new JSONObject();
          firstPoint.put("x1", (int) previousPoint.getX());
          firstPoint.put("y1", (int) previousPoint.getY());
          secondPoint.put("x2", (int) currentPoint.getX());
          secondPoint.put("y2", (int) currentPoint.getY());
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
    Object jsonObject = JSONMacroFunctions.asJSON(pointsString);

    ArrayList<Map<String, Integer>> pathPoints = new ArrayList<Map<String, Integer>>();
    if (jsonObject instanceof JSONArray) {
      ArrayList<?> tempPoints = (ArrayList<?>) JSONArray.toCollection((JSONArray) jsonObject);
      for (Object o : tempPoints) {
        MorphDynaBean bean = (MorphDynaBean) o;
        Map<String, Integer> point = new HashMap<String, Integer>();
        point.put("x", (Integer) bean.get("x"));
        point.put("y", (Integer) bean.get("y"));
        pathPoints.add(point);
      }
    }
    return pathPoints;
  }

  /**
   * Searches the list of drawn elements for items with a matching name
   *
   * @param drawableList List of drawables.
   * @param name String value for the search
   * @return List<String> of ids
   */
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
