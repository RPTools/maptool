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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AbstractPoint;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.util.EventMacroUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Joe.Frazier
 */
public class TokenMoveFunctions extends AbstractFunction {

  /** macro name to call for the onTokenMove event */
  public static final String ON_TOKEN_MOVE_COMPLETE_CALLBACK = "onTokenMove";
  /** macro name to call for the onMultipleTokensMove event */
  public static final String ON_MULTIPLE_TOKENS_MOVED_COMPLETE_CALLBACK = "onMultipleTokensMove";
  /** variable to test for token move denial */
  public static final String ON_TOKEN_MOVE_DENY_VARIABLE = "tokens.denyMove";
  /** variable to contain number of tokens moved */
  public static final String ON_TOKEN_MOVE_COUNT_VARIABLE = "tokens.moveCount";

  private static final TokenMoveFunctions instance = new TokenMoveFunctions();
  private static final String NO_GRID = "NO_GRID";

  private static final Logger log = LogManager.getLogger(TokenMoveFunctions.class);

  private TokenMoveFunctions() {
    super(0, 2, "getLastPath", "movedOverToken", "movedOverPoints", "getMoveCount");
  }

  public static TokenMoveFunctions getInstance() {
    // log.setLevel(Level.INFO);
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    final Token tokenInContext = ((MapToolVariableResolver) resolver).getTokenInContext();
    if (tokenInContext == null) {
      throw new ParserException(
          I18N.getText("macro.function.general.noImpersonated", functionName));
    }
    boolean useDistancePerCell = true;
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

    if (functionName.equalsIgnoreCase("getLastPath")) {
      BigDecimal val = null;
      if (parameters.size() == 1) {
        if (!(parameters.get(0) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText("macro.function.general.argumentTypeN", functionName, 1));
        }
        val = (BigDecimal) parameters.get(0);
        useDistancePerCell = !val.equals(BigDecimal.ZERO);
      }
      Path<? extends AbstractPoint> path = tokenInContext.getLastPath();

      List<Map<String, Integer>> pathPoints = getLastPathList(path, useDistancePerCell);
      return pathPointsToJSONArray(pathPoints);
    }
    if (functionName.equalsIgnoreCase("movedOverPoints")) {
      // macro.function.general.noPerm
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }
      Path<?> path = tokenInContext.getLastPath();

      List<Map<String, Integer>> returnPoints = new ArrayList<Map<String, Integer>>();

      if ((parameters.size() == 1) || parameters.size() == 2) {
        String points = (String) parameters.get(0);
        String jsonPath = (String) (parameters.size() == 2 ? parameters.get(1) : "");

        List<Map<String, Integer>> pathPoints = null;
        if (jsonPath != null && !jsonPath.equals("")) {
          returnPoints = crossedPoints(zone, tokenInContext, points, jsonPath);
        } else {
          pathPoints = getLastPathList(path, true);
          returnPoints = crossedPoints(zone, tokenInContext, points, pathPoints);
        }
        JsonArray retVal = pathPointsToJSONArray(returnPoints);
        returnPoints = null;
        return retVal;
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 2, parameters.size()));
      }
    }
    if (functionName.equalsIgnoreCase("getMoveCount")) {
      boolean useFractionOnly = false;
      boolean useTerrainModifiers = false;

      if (parameters.size() == 1) {
        BigDecimal fractionOnly = (BigDecimal) parameters.get(0);
        useFractionOnly = fractionOnly == null || !fractionOnly.equals(BigDecimal.ZERO);
      }

      if (parameters.size() == 2) {
        BigDecimal fractionOnly = (BigDecimal) parameters.get(0);
        useFractionOnly = fractionOnly == null || !fractionOnly.equals(BigDecimal.ZERO);

        BigDecimal terrainModifiers = (BigDecimal) parameters.get(1);
        useTerrainModifiers = terrainModifiers == null || !terrainModifiers.equals(BigDecimal.ZERO);
      }

      if (useFractionOnly) {
        if (getMovement(tokenInContext, useFractionOnly, useTerrainModifiers).equals("0.5"))
          return BigDecimal.ONE;
        else return BigDecimal.ZERO;
      } else {
        return getMovement(tokenInContext, useFractionOnly, useTerrainModifiers);
      }
    }
    if (functionName.equalsIgnoreCase("movedOverToken")) {
      // macro.function.general.noPerm
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }
      Path<?> path = tokenInContext.getLastPath();
      List<Map<String, Integer>> returnPoints = new ArrayList<Map<String, Integer>>();
      Token target;

      if ((parameters.size() == 1) || parameters.size() == 2) {
        String targetToken = (String) parameters.get(0);
        String jsonPath = parameters.size() == 2 ? parameters.get(1).toString() : "";
        target = zone.resolveToken(targetToken);
        if (target == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.unknownToken", functionName, targetToken));
        }
        List<Map<String, Integer>> pathPoints = null;
        if (jsonPath != null && !jsonPath.equals("")) {
          returnPoints = crossedToken(zone, tokenInContext, target, jsonPath);
        } else {
          pathPoints = getLastPathList(path, true);
          returnPoints = crossedToken(zone, tokenInContext, target, pathPoints);
        }
        JsonArray retVal = pathPointsToJSONArray(returnPoints);
        returnPoints = null;
        return retVal;
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 2, parameters.size()));
      }
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  private List<Map<String, Integer>> crossedToken(
      final Zone zone, final Token tokenInContext, final Token target, final String pathString) {

    JsonElement json = null;
    json = JSONMacroFunctions.getInstance().asJsonElement(pathString);

    ArrayList<Map<String, Integer>> pathPoints = new ArrayList<Map<String, Integer>>();
    if (json != null && json.isJsonArray()) {
      for (JsonElement ele : json.getAsJsonArray()) {
        JsonObject jobj = ele.getAsJsonObject();
        Map<String, Integer> point = new HashMap<String, Integer>();
        point.put("x", jobj.get("x").getAsInt());
        point.put("y", jobj.get("y").getAsInt());
        pathPoints.add(point);
      }
      return getInstance().crossedToken(zone, tokenInContext, target, pathPoints);
    }
    return pathPoints;
  }

  private List<Map<String, Integer>> crossedPoints(
      final Zone zone,
      final Token tokenInContext,
      final String pointsString,
      final String pathString) {
    List<Map<String, Integer>> pathPoints = convertJSONStringToList(pathString);

    pathPoints = getInstance().crossedPoints(zone, tokenInContext, pointsString, pathPoints);
    return pathPoints;
  }

  /**
   * @param zone
   * @param tokenInContext
   * @param pointsString
   * @param pathPoints
   * @return
   */
  private List<Map<String, Integer>> crossedPoints(
      final Zone zone,
      final Token tokenInContext,
      final String pointsString,
      final List<Map<String, Integer>> pathPoints) {
    List<Map<String, Integer>> returnPoints = new ArrayList<Map<String, Integer>>();

    List<Map<String, Integer>> targetPoints = convertJSONStringToList(pointsString);
    if (pathPoints == null) {
      return returnPoints;
    }
    for (Map<String, Integer> entry : pathPoints) {
      Map<String, Integer> thePoint = new HashMap<String, Integer>();
      Grid grid = zone.getGrid();
      Rectangle originalArea = null;
      Polygon targetArea = new Polygon();
      for (Map<String, Integer> points : targetPoints) {
        int x = points.get("x");
        int y = points.get("y");
        targetArea.addPoint(x, y);
      }
      if (tokenInContext.isSnapToGrid()) {
        originalArea =
            tokenInContext
                .getFootprint(grid)
                .getBounds(grid, grid.convert(new ZonePoint(entry.get("x"), entry.get("y"))));
      } else {
        originalArea = tokenInContext.getBounds(zone);
      }
      Rectangle2D oa = originalArea.getBounds2D();
      if (targetArea.contains(oa) || targetArea.intersects(oa)) {
        thePoint.put("x", entry.get("x"));
        thePoint.put("y", entry.get("y"));
        returnPoints.add(thePoint);
      }
      thePoint = null;
    }
    return returnPoints;
  }

  /**
   * @param zone
   * @param target
   * @param pathPoints
   * @return
   */
  private List<Map<String, Integer>> crossedToken(
      final Zone zone,
      final Token tokenInContext,
      final Token target,
      final List<Map<String, Integer>> pathPoints) {
    List<Map<String, Integer>> returnPoints = new ArrayList<Map<String, Integer>>();

    /**
     * Lee: modifying code to match behavior on both grid-based and non-grid-based movement.
     *
     * <p>These lines below seem better outside the loop. Taking them out and inverting the loop +
     * if-else sequence...
     */
    Grid grid = zone.getGrid();
    Rectangle targetArea = target.getBounds(zone);

    if (pathPoints == null) {
      return returnPoints;
    }
    if (tokenInContext.isSnapToGrid()) {
      Map<String, Integer> thePoint = new HashMap<String, Integer>();
      for (Map<String, Integer> entry : pathPoints) {
        Rectangle originalArea =
            tokenInContext
                .getFootprint(grid)
                .getBounds(grid, grid.convert(new ZonePoint(entry.get("x"), entry.get("y"))));
        if (targetArea.intersects(originalArea) || originalArea.intersects(targetArea)) {
          thePoint.put("x", entry.get("x"));
          thePoint.put("y", entry.get("y"));
          returnPoints.add(thePoint);
          thePoint = new HashMap<String, Integer>();
        }
      }
    } else {
      // Lee: establish first point, then process line intersection when a line can be drawn.
      int ctr = 0;
      Point previousPoint = new Point();
      Map<String, Integer> firstPoint = new HashMap<String, Integer>(),
          secondPoint = new HashMap<String, Integer>();
      for (Map<String, Integer> entry : pathPoints) {
        Rectangle tokenArea = tokenInContext.getBounds(zone);
        Point currentPoint = new Point(entry.get("x"), entry.get("y"));
        if (ctr > 0) {
          if (targetArea.intersectsLine(new Line2D.Double(previousPoint, currentPoint))
              || targetArea.intersects(tokenArea)) {
            firstPoint.put("x1", (int) previousPoint.getX());
            firstPoint.put("y1", (int) previousPoint.getY());
            secondPoint.put("x2", entry.get("x"));
            secondPoint.put("y2", entry.get("y"));
            returnPoints.add(firstPoint);
            returnPoints.add(secondPoint);
            firstPoint = new HashMap<String, Integer>();
            secondPoint = new HashMap<String, Integer>();
          }
        }
        previousPoint = currentPoint;
        ctr += 1;
      }
      // Lee: commenting this out
      // originalArea = tokenInContext.getBounds(zone);
    }
    return returnPoints;
  }

  private JsonArray pathPointsToJSONArray(final List<Map<String, Integer>> pathPoints) {
    log.debug("...in pathPointsToJSONArrayt.  Converting list to JSONArray");

    JsonArray jsonArr = new JsonArray();
    if (pathPoints == null || pathPoints.isEmpty()) {
      return jsonArr;
    }
    JsonObject pointObj;
    // Lee: had to add handling for the line segment made by unsnapped movedOverToken()
    if (pathPoints.get(0).containsKey("x"))
      for (Map<String, Integer> entry : pathPoints) {
        pointObj = new JsonObject();
        pointObj.addProperty("x", entry.get("x"));
        pointObj.addProperty("y", entry.get("y"));
        if (entry.containsKey("fail")) {
          pointObj.addProperty("fail", entry.get("fail"));
        }
        jsonArr.add(pointObj);
      }
    else
      for (Map<String, Integer> entry : pathPoints) {
        pointObj = new JsonObject();
        pointObj.addProperty("x1", entry.get("x1"));
        pointObj.addProperty("y1", entry.get("y1"));
        pointObj.addProperty("x2", entry.get("x2"));
        pointObj.addProperty("y2", entry.get("y2"));
        jsonArr.add(pointObj);
      }

    log.debug("...in pathPointsToJSONArrayt.  return JSONArray");

    return jsonArr;
  }

  private List<Map<String, Integer>> getLastPathList(
      final Path<? extends AbstractPoint> path, final boolean useDistancePerCell) {
    List<Map<String, Integer>> points = new ArrayList<Map<String, Integer>>();
    if (path != null) {
      Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      AbstractPoint zp = null;

      log.debug("...in getLastPathList.  Loop over each path elements");

      for (AbstractPoint pathCell : path.getCellPath()) {
        log.debug(
            "...in getLastPathList.  Converting each path item to a cell point or zone point.");

        if (pathCell instanceof CellPoint) {
          CellPoint cp = (CellPoint) pathCell;
          if (useDistancePerCell) {
            zp = zone.getGrid().convert((CellPoint) pathCell);
          } else {
            zp = cp;
          }
        } else {
          zp = pathCell;
        }
        if (zp != null) {
          log.debug("...in getLastPathList.  Got a point, adding to list.");

          Map<String, Integer> tokenLocationPoint = new HashMap<>();
          if (pathCell.isAStarCanceled()) {
            tokenLocationPoint.put("fail", 1);
          }

          tokenLocationPoint.put("x", zp.x);
          tokenLocationPoint.put("y", zp.y);

          points.add(tokenLocationPoint);
        }
      }
    }
    return points;
  }

  /**
   * Handles the {@value #ON_TOKEN_MOVE_COMPLETE_CALLBACK} macro event for each individual token,
   * and returns a list of the tokens for which the movement was denied. Passes path information to
   * the handler, and checks the value of {@value #ON_TOKEN_MOVE_DENY_VARIABLE} for a veto.
   *
   * <p>Note: To maintain backward-compatibility this event only fires a single handler. If more
   * than one Lib:Token has a matching macro, the first one encountered is called - because this
   * order is unpredictable, this is very much not encouraged.
   *
   * @param path the path token
   * @param filteredTokens the tokens being moved (each one will be evaluated individually)
   * @return the list of tokens from the given list that have their movement rejected
   */
  public static List<Token> callForIndividualTokenMoveVetoes(
      final Path<?> path, final List<Token> filteredTokens) {
    List<Token> deniedTokens = new ArrayList<>();
    try {
      var libraries =
          new LibraryManager().getLegacyEventTargets(ON_TOKEN_MOVE_COMPLETE_CALLBACK).get();
      if (!libraries.isEmpty()) {
        String libraryNamespace = libraries.get(0).getNamespace().get();
        List<Map<String, Integer>> pathPoints = getInstance().getLastPathList(path, true);
        JsonArray pathArr = getInstance().pathPointsToJSONArray(pathPoints);
        String pathCoordinates = pathArr.toString();
        Map<String, Object> varsToSet = new HashMap<>();
        varsToSet.put(ON_TOKEN_MOVE_COUNT_VARIABLE, filteredTokens.size());

        for (Token token : filteredTokens) {
          boolean moveDenied =
              EventMacroUtil.pollEventHandlerForVeto(
                  ON_TOKEN_MOVE_COMPLETE_CALLBACK,
                  libraryNamespace,
                  pathCoordinates,
                  token,
                  ON_TOKEN_MOVE_DENY_VARIABLE,
                  varsToSet);
          if (moveDenied) deniedTokens.add(token);
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error(
          I18N.getText("library.error.retrievingEventHandler", ON_TOKEN_MOVE_COMPLETE_CALLBACK),
          e.getCause());
    }
    return deniedTokens;
  }

  private String getMovement(
      final Token source, boolean returnFractionOnly, boolean useTerrainModifiers) {
    ZoneWalker walker = null;

    WalkerMetric metric =
        MapTool.isPersonalServer()
            ? AppPreferences.getMovementMetric()
            : MapTool.getServerPolicy().getMovementMetric();

    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zr.getZone();
    Grid grid = zone.getGrid();

    Path<ZonePoint> gridlessPath;
    /*
     * Lee: causes an NPE when used on a newly dropped token. While a true solution would probably be to create a "path" based on the token's coords when it is dropped on the map, the easy out
     * here would be to just return a "0".
     *
     * Final Edit: attempting to create a default path for new drops had undesirable effects. Therefore, let's opt for the easy fix
     */
    int x = 0, y = 0;

    try {
      x = source.getLastPath().getCellPath().get(0).x;
      y = source.getLastPath().getCellPath().get(0).y;
    } catch (NullPointerException e) {
      return "0";
    }

    if (useTerrainModifiers && !returnFractionOnly) {
      if (source.getLastPath().getLastWaypoint() instanceof CellPoint) {
        CellPoint cp = (CellPoint) source.getLastPath().getLastWaypoint();
        double trueDistance = cp.getDistanceTraveled(zone);

        return new BigDecimal(trueDistance).stripTrailingZeros().toPlainString();
      }
    }

    if (source.isSnapToGrid() && grid.getCapabilities().isSnapToGridSupported()) {
      if (zone.getGrid().getCapabilities().isPathingSupported()) {
        List<CellPoint> cplist = new ArrayList<CellPoint>();
        walker = grid.createZoneWalker();
        walker.replaceLastWaypoint(new CellPoint(x, y));
        for (AbstractPoint point : source.getLastPath().getCellPath()) {
          CellPoint tokenPoint = new CellPoint(point.x, point.y);
          // walker.setWaypoints(tokenPoint);
          walker.replaceLastWaypoint(tokenPoint);
          cplist.add(tokenPoint);
        }

        double bar =
            calculateGridDistance(cplist, zone.getUnitsPerCell(), metric, returnFractionOnly);

        if (returnFractionOnly) {
          return Double.toString(bar);
        } else {
          return new BigDecimal(bar).stripTrailingZeros().toPlainString();
        }

        // return Integer.toString(walker.getDistance());
      }
    } else {
      gridlessPath = new Path<ZonePoint>();
      for (AbstractPoint point : source.getLastPath().getCellPath()) {
        gridlessPath.addPathCell(new ZonePoint(point.x, point.y));
      }
      double c = 0;
      ZonePoint lastPoint = null;
      for (ZonePoint zp : gridlessPath.getCellPath()) {
        if (lastPoint == null) {
          lastPoint = zp;
          continue;
        }
        int a = lastPoint.x - zp.x;
        int b = lastPoint.y - zp.y;
        c += Math.hypot(a, b);
        lastPoint = zp;
      }
      c /= zone.getGrid().getSize(); // Number of "cells"
      c *= zone.getUnitsPerCell(); // "actual" distance traveled
      return NumberFormat.getInstance().format(c);
    }
    return "";
  }

  /**
   * Handle the {@value #ON_MULTIPLE_TOKENS_MOVED_COMPLETE_CALLBACK} macro event, and determine
   * whether the movement was denied. Passes the list of tokens to the handler, and checks the value
   * of {@value #ON_TOKEN_MOVE_DENY_VARIABLE} for a veto.
   *
   * <p>Note: To maintain backward-compatibility, this event only fires a single handler. If more
   * than one Lib:Token has a matching macro, the first one encountered is called - because this
   * order is unpredictable, this is very much not encouraged.
   *
   * @param filteredTokens the tokens being moved
   * @return true if the move has been vetoed, false otherwise
   */
  public static boolean callForMultiTokenMoveVeto(List<GUID> filteredTokens) {
    boolean moveDenied = false;
    try {

      var libraries =
          new LibraryManager()
              .getLegacyEventTargets(ON_MULTIPLE_TOKENS_MOVED_COMPLETE_CALLBACK)
              .get();
      if (!libraries.isEmpty()) {
        String libraryNamespace = libraries.get(0).getNamespace().get();
        JsonArray json = new JsonArray();
        for (GUID tokenGuid : filteredTokens) {
          json.add(tokenGuid.toString());
        }
        Map<String, Object> varsToSet = new HashMap<>();
        varsToSet.put(ON_TOKEN_MOVE_COUNT_VARIABLE, filteredTokens.size());
        moveDenied =
            EventMacroUtil.pollEventHandlerForVeto(
                ON_MULTIPLE_TOKENS_MOVED_COMPLETE_CALLBACK,
                libraryNamespace,
                json.toString(),
                null,
                ON_TOKEN_MOVE_DENY_VARIABLE,
                varsToSet);
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error(
          I18N.getText(
              "library.error.retrievingEventHandler", ON_MULTIPLE_TOKENS_MOVED_COMPLETE_CALLBACK),
          e.getCause());
    }
    return moveDenied;
  }

  private List<Map<String, Integer>> convertJSONStringToList(final String pointsString) {
    JsonElement json = null;
    json = JSONMacroFunctions.getInstance().asJsonElement(pointsString);

    ArrayList<Map<String, Integer>> pathPoints = new ArrayList<Map<String, Integer>>();
    if (json != null && json.isJsonArray()) {
      for (JsonElement ele : json.getAsJsonArray()) {
        JsonObject jobj = ele.getAsJsonObject();
        Map<String, Integer> point = new HashMap<>();
        point.put("x", jobj.get("x").getAsInt());
        point.put("y", jobj.get("y").getAsInt());
        pathPoints.add(point);
      }
    }
    return pathPoints;
  }

  public double calculateGridDistance(
      List<CellPoint> path,
      double unitsPerCell,
      WalkerMetric metric,
      boolean oneTwoOneFractionOnly) {
    if (path == null || path.size() == 0) return 0;

    final double unitsDistance;

    {
      double numDiag = 0;
      double numStrt = 0;

      CellPoint previousPoint = null;
      for (CellPoint point : path) {
        if (previousPoint != null) {
          int change = Math.abs(previousPoint.x - point.x) + Math.abs(previousPoint.y - point.y);
          switch (change) {
            case 1:
              numStrt++;
              break;
            case 2:
              numDiag++;
              break;
            default:
              assert false
                  : String.format("Illegal path, cells are not contiguous change=%d", change);
              return -1;
          }
        }
        previousPoint = point;
      }
      final double cellDistance;
      switch (metric) {
        case MANHATTAN:
        case NO_DIAGONALS:
          cellDistance = (numStrt + numDiag * 2);
          break;
        case ONE_ONE_ONE:
          cellDistance = (numStrt + numDiag);
          break;
        default:
        case ONE_TWO_ONE:
          // Jamz: It's useful to know if there is a fractional remainder left in the ONE_TWO_ONE
          // movement
          // Allows macros to track full movement even if token is moved 1 cell at a time now...
          if (oneTwoOneFractionOnly) {
            return (numDiag + numDiag / 2) - (int) (numDiag + numDiag / 2);
          } else {
            cellDistance = (int) (numStrt + numDiag + numDiag / 2);
          }
          break;
      }

      unitsDistance = cellDistance * unitsPerCell;
    }

    return unitsDistance;
  }
}
