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

import com.google.gson.JsonElement;
import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Functions to move tokens, get a token's location, or calculate the distance from a token. */
public class TokenLocationFunctions extends AbstractFunction {

  /** Holds a token's x, y and z coordinates. */
  private static class TokenLocation {
    int x;
    int y;
    int z;
  }

  /** Ignore grid for movement metric in distance methods. */
  private static final String NO_GRID = "NO_GRID";

  /** Singleton for class/ */
  private static final TokenLocationFunctions instance = new TokenLocationFunctions();

  private TokenLocationFunctions() {
    super(
        0,
        6,
        "getTokenX",
        "getTokenY",
        "getTokenDrawOrder",
        "getTokenMap",
        "getTokenMapIDs",
        "getDistance",
        "moveToken",
        "goto",
        "getDistanceToXY",
        "setTokenDrawOrder",
        "moveTokenToMap",
        "moveTokenFromMap");
  }

  /**
   * @return instance of TokenLocationFunctions.
   */
  public static TokenLocationFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    FunctionUtil.blockUntrustedMacro(functionName);

    if (functionName.equalsIgnoreCase("getTokenX") || functionName.equalsIgnoreCase("getTokenY")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      boolean useDistancePerCell =
          parameters.size() > 0
              ? FunctionUtil.paramAsBoolean(functionName, parameters, 0, false)
              : true;
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      TokenLocation location = getTokenLocation(useDistancePerCell, token);
      return BigDecimal.valueOf(
          functionName.equalsIgnoreCase("getTokenX") ? location.x : location.y);
    }
    if (functionName.equalsIgnoreCase("getTokenDrawOrder")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return BigDecimal.valueOf(token.getZOrder());
    }
    if (functionName.equalsIgnoreCase("setTokenDrawOrder")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      int newZOrder = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setZOrder, newZOrder);
      return BigDecimal.valueOf(token.getZOrder());
    }
    if (functionName.equalsIgnoreCase("getTokenMap")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String identifier = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      final var zoneNames = getTokenZones(identifier).map(Zone::getName).toList();
      return FunctionUtil.delimitedResult(delim, zoneNames);
    }
    if (functionName.equalsIgnoreCase("getTokenMapIDs")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String identifier = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      final var zoneNames = getTokenZones(identifier).map(Zone::getId).map(GUID::toString).toList();
      return FunctionUtil.delimitedResult(delim, zoneNames);
    }
    if (functionName.equalsIgnoreCase("getDistance")) {
      FunctionUtil.checkNumberParam("getDistance", parameters, 1, 4);
      return getDistance(resolver, parameters);
    }
    if (functionName.equalsIgnoreCase("getDistanceToXY")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 6);
      return getDistanceToXY(resolver, parameters);
    }
    if (functionName.equalsIgnoreCase("goto")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      return gotoLoc(resolver, parameters);
    }
    if (functionName.equalsIgnoreCase("moveToken")) {
      FunctionUtil.checkNumberParam("moveToken", parameters, 2, 4);
      return moveToken(resolver, parameters);
    }
    if (functionName.equalsIgnoreCase("moveTokenToMap")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 5);
      return tokenMoveMap(true, parameters);
    }
    if (functionName.equalsIgnoreCase("moveTokenFromMap")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 5);
      return tokenMoveMap(false, parameters);
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Moves tokens between maps.
   *
   * @param fromCurrentMap true if it is begin moved from the named map to this one.
   * @param args The parameters for the function.
   * @return a message detailing the number of tokens moved.
   * @throws ParserException if the parameters are invalid, or the token is already on the map.
   */
  private String tokenMoveMap(boolean fromCurrentMap, List<Object> args) throws ParserException {
    String functionName = fromCurrentMap ? "moveTokenToMap" : "moveTokenFromMap";
    Object tokenString = args.get(0);
    String map = (String) args.get(1);

    List<String> tokens = new ArrayList<String>();

    JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(tokenString);
    if (json.isJsonArray()) {
      for (JsonElement ele : json.getAsJsonArray()) {
        tokens.add(JSONMacroFunctions.getInstance().jsonToScriptString(ele));
      }
    } else {
      tokens.add((String) tokenString);
    }

    final var zone = FunctionUtil.getZoneRenderer(functionName, map).getZone();

    Zone toZone;
    Zone fromZone;

    if (fromCurrentMap) {
      fromZone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      toZone = zone;
    } else {
      toZone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      fromZone = zone;
    }
    if (fromZone.equals(toZone)) {
      throw new ParserException(
          I18N.getText("macro.function.moveTokenMap.alreadyThere", functionName));
    }
    int x = args.size() > 2 ? FunctionUtil.paramAsInteger(functionName, args, 2, false) : 0;
    int y = args.size() > 3 ? FunctionUtil.paramAsInteger(functionName, args, 3, false) : 0;
    int z =
        args.size() > 4
            ? FunctionUtil.paramAsInteger(functionName, args, 4, false)
            : zone.getLargestZOrder() + 1;

    StringBuilder sb = new StringBuilder();
    for (String id : tokens) {
      Token token = fromZone.resolveToken(id);
      if (token == null) {
        sb.append(I18N.getText("macro.function.moveTokenMap.unknownToken", functionName, id))
            .append("<br>");
      } else {
        Grid grid = toZone.getGrid();

        ZonePoint zp = grid.convert(new CellPoint(x, y));

        token.setX(zp.x);
        token.setY(zp.y);
        token.setZOrder(z);
        MapTool.serverCommand().putToken(toZone.getId(), token);
        MapTool.serverCommand().removeToken(fromZone.getId(), token.getId());
        sb.append(
                I18N.getText(
                    "macro.function.moveTokenMap.movedToken",
                    token.getName(),
                    toZone.getName(),
                    fromZone.getName()))
            .append("<br>");
      }
    }
    MapTool.getFrame().getCurrentZoneRenderer().flushLight();
    MapTool.getFrame().refresh();

    return sb.toString();
  }

  /**
   * Gets the location of the token on the map.
   *
   * @param useDistancePerCell should the cell coordinates per returned?
   * @param token the token to return the location of.
   * @return the location of the token.
   */
  private TokenLocation getTokenLocation(boolean useDistancePerCell, Token token) {
    TokenLocation loc = new TokenLocation();
    if (useDistancePerCell) {
      Rectangle tokenBounds = token.getBounds(token.getZoneRenderer().getZone());
      loc.x = tokenBounds.x;
      loc.y = tokenBounds.y;
    } else {
      CellPoint cellPoint = getTokenCell(token);
      int x = cellPoint.x;
      int y = cellPoint.y;

      loc.x = x;
      loc.y = y;
    }
    loc.z = token.getZOrder();

    return loc;
  }

  /**
   * Returns the token pixel offset between its reported pixel location and its true (x,y) pixel
   * coordinate.
   *
   * @param token the token to return the offset of
   * @return a point representing the (x,y) offset
   */
  private Point getTokenPixelLocationOffset(Token token) {
    TokenLocation loc = getTokenLocation(true, token);
    return new Point(loc.x - token.getX(), loc.y - token.getY());
  }

  /**
   * Gets the distance between two tokens following map movement rules. Always use closedForm
   * because VBL &amp; terrain are currently ignored. The other walker-based approach is kept as it
   * will be needed if we implement distance based on terrain &amp; VBL.
   *
   * @param source The token to get the distance from.
   * @param target The token to calculate the distance to.
   * @param units get the distance in the units specified for the map.
   * @param metric The metric used.
   * @return the distance.
   * @throws ParserException when an error occurs
   */
  public double getDistance(Token source, Token target, boolean units, String metric)
      throws ParserException {
    boolean closedForm = true; // VBL & terrain ignored, so closedForm always work
    Zone zone = source.getZoneRenderer().getZone();
    Grid grid = zone.getGrid();
    double distance;

    if (grid.getCapabilities().isPathingSupported() && !NO_GRID.equals(metric)) {
      Set<CellPoint> sourceCells = source.getOccupiedCells(grid);
      Set<CellPoint> targetCells = target.getOccupiedCells(grid);

      WalkerMetric wmetric = null;
      if (metric != null && grid.useMetric()) {
        try {
          wmetric = WalkerMetric.valueOf(metric);
        } catch (IllegalArgumentException e) {
          throw new ParserException(
              I18N.getText("macro.function.getDistance.invalidMetric", metric));
        }
      }

      distance = Double.MAX_VALUE;
      if (closedForm) {
        if (wmetric == null && grid.useMetric())
          wmetric =
              MapTool.isPersonalServer()
                  ? AppPreferences.getMovementMetric()
                  : MapTool.getServerPolicy().getMovementMetric();
        // explicitly find difference without walkers
        double curDist;
        for (CellPoint scell : sourceCells) {
          for (CellPoint tcell : targetCells) {
            curDist = grid.cellDistance(scell, tcell, wmetric);
            distance = Math.min(distance, curDist);
          }
        }
        if (units) distance *= zone.getUnitsPerCell();
      } else {
        // walker approach, slow but could eventually take into account VBL & terrain
        ZoneWalker walker =
            grid.useMetric() && wmetric != null
                ? new AStarSquareEuclideanWalker(zone, wmetric)
                : grid.createZoneWalker();

        for (CellPoint scell : sourceCells) {
          for (CellPoint tcell : targetCells) {
            walker.setWaypoints(scell, tcell);
            distance = Math.min(distance, walker.getDistance());
          }
        }
        if (!units) distance /= zone.getUnitsPerCell();
      }
    } else {
      // take distance between center of the two tokens
      Rectangle sourceBounds = source.getBounds(zone);
      double sourceCenterX = sourceBounds.x + sourceBounds.width / 2.0;
      double sourceCenterY = sourceBounds.y + sourceBounds.height / 2.0;
      Rectangle targetBounds = target.getBounds(zone);
      double targetCenterX = targetBounds.x + targetBounds.width / 2.0;
      double targetCenterY = targetBounds.y + targetBounds.height / 2.0;

      double a = (int) (sourceCenterX - targetCenterX);
      double b = (int) (sourceCenterY - targetCenterY);
      distance = Math.sqrt(a * a + b * b) / grid.getSize();
      if (units) distance *= zone.getUnitsPerCell();
    }
    return distance;
  }

  /**
   * Gets the distance to a target x,y co-ordinate following map movement rules.
   *
   * @param source The token to get the distance from.
   * @param x the x co-ordinate to get the distance to.
   * @param y the y co-ordinate to get the distance to.
   * @param units get the distance in the units specified for the map.
   * @param metric The metric used.
   * @param pixels Are {@code x & y} for pixels coordinates? false: cell coords
   * @return the distance between the token and the x,y coordinates
   * @throws ParserException when an error occurs
   */
  public double getDistance(
      Token source, int x, int y, boolean units, String metric, boolean pixels)
      throws ParserException {
    Zone zone = source.getZoneRenderer().getZone();
    Grid grid = zone.getGrid();

    if (grid.getCapabilities().isPathingSupported() && !NO_GRID.equals(metric)) {
      // Get which cells our tokens occupy
      Set<CellPoint> sourceCells = source.getOccupiedCells(grid);

      CellPoint targetCell;
      if (!pixels) targetCell = new CellPoint(x, y);
      else targetCell = grid.convert(new ZonePoint(x, y));

      ZoneWalker walker;
      if (metric != null && grid.useMetric()) {
        try {
          WalkerMetric wmetric = WalkerMetric.valueOf(metric);
          walker = new AStarSquareEuclideanWalker(zone, wmetric);

        } catch (IllegalArgumentException e) {
          throw new ParserException(
              I18N.getText("macro.function.getDistance.invalidMetric", metric));
        }
      } else {
        walker = grid.createZoneWalker();
      }

      // Get the distances from each source to target cell and keep the minimum one
      double distance = Double.MAX_VALUE;
      for (CellPoint scell : sourceCells) {
        walker.setWaypoints(scell, targetCell);
        distance = Math.min(distance, walker.getDistance());
      }

      if (units) {
        return distance;
      } else {
        return distance / getDistancePerCell();
      }
    } else {
      double targetX, targetY;
      if (!pixels) {
        // get center of target cell
        Point2D.Double targetPoint = grid.getCellCenter(new CellPoint(x, y));
        targetX = targetPoint.x;
        targetY = targetPoint.y;
      } else {
        targetX = x;
        targetY = y;
      }

      // get the pixel coords for the center of the token
      Rectangle sourceBounds = source.getBounds(zone);
      double sourceCenterX = sourceBounds.x + sourceBounds.width / 2.0;
      double sourceCenterY = sourceBounds.y + sourceBounds.height / 2.0;
      double a = (int) (sourceCenterX - targetX);
      double b = (int) (sourceCenterY - targetY);
      double h = Math.sqrt(a * a + b * b) / grid.getSize();
      if (units) h *= zone.getUnitsPerCell();
      return h;
    }
  }

  /**
   * Return true if token is at one of (x,y) cell coordinates, false otherwise. If using no-grid
   * map, check if part of the token overlaps each x-y pixel. Intended for getTokens() macro call to
   * get better performances.
   *
   * @param token The token to check the overlap status of
   * @param zone The map
   * @param points An array of points (cells coordinates)
   * @return true if overlap, false otherwise
   */
  public static boolean isTokenAtXY(Token token, Zone zone, Point[] points) {
    Grid grid = zone.getGrid();
    if (grid.getCapabilities().isPathingSupported()) {
      Set<CellPoint> tokenCells = token.getOccupiedCells(grid);
      int cellx, celly;
      for (CellPoint cell : tokenCells) {
        cellx = cell.x;
        celly = cell.y;
        for (Point point : points) {
          if (cellx == point.x && celly == point.y) return true;
        }
      }
    } else {
      Rectangle bounds = token.getBounds(zone);
      for (Point point : points) {
        if (bounds.contains(point)) return true;
      }
    }
    return false;
  }

  /**
   * Gets the distance to another token.
   *
   * @param args arguments to the function.
   * @return the distance between tokens.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal getDistance(VariableResolver resolver, List<Object> args)
      throws ParserException {
    Token target = FunctionUtil.getTokenFromParam(resolver, "getDistance", args, 0, -1);
    Token source = FunctionUtil.getTokenFromParam(resolver, "getDistance", args, 2, -1);

    boolean useDistancePerCell =
        args.size() > 1 ? FunctionUtil.paramAsBoolean("getDistance", args, 1, false) : true;
    String metric = args.size() > 3 ? args.get(3).toString() : null;

    double dist = getDistance(source, target, useDistancePerCell, metric);

    if (dist == Math.floor(dist)) {
      return BigDecimal.valueOf((int) dist);
    } else {
      return BigDecimal.valueOf(dist);
    }
  }

  /**
   * Gets the distance to an x,y location.
   *
   * @param args arguments to the function.
   * @return the distance between tokens.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal getDistanceToXY(VariableResolver resolver, List<Object> args)
      throws ParserException {
    final String fName = "getDistanceToXY";

    int x = FunctionUtil.paramAsInteger(fName, args, 0, false);
    int y = FunctionUtil.paramAsInteger(fName, args, 1, false);

    boolean useDistancePerCell = true;
    if (args.size() > 2) useDistancePerCell = FunctionUtil.paramAsBoolean(fName, args, 2, true);

    Token source = FunctionUtil.getTokenFromParam(resolver, fName, args, 3, -1);
    String metric = args.size() > 4 ? args.get(4).toString() : null;
    boolean pixel = args.size() > 5 ? FunctionUtil.paramAsBoolean(fName, args, 5, true) : false;

    double dist = getDistance(source, x, y, useDistancePerCell, metric, pixel);

    if (dist == Math.floor(dist)) {
      return BigDecimal.valueOf((int) dist);
    } else {
      return BigDecimal.valueOf(dist);
    }
  }

  /**
   * Get a ZonePoint of the specified x,y location for the current map. If <code>units</code> is
   * true, the incoming (x,y) is treated as a <code>ZonePoint</code>. If <code>units</code> is
   * false, the incoming (x,y) is treated as a <code>CellPoint</code> and is converted to a
   * ZonePoint by calling {@link Grid#convert(CellPoint)}.
   *
   * @param x the x co-ordinate of the destination.
   * @param y the y co-ordinate of the destination.
   * @param units whether the (x,y) coordinate is a <code>ZonePoint</code> (true) or <code>CellPoint
   *     </code> (false)
   * @return the ZonePoint of the coordinates.
   */
  public static ZonePoint getZonePoint(int x, int y, boolean units) {
    ZonePoint zp;
    if (units) {
      zp = new ZonePoint(x, y);
    } else {
      Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
      CellPoint cp = new CellPoint(x, y);
      zp = grid.convert(cp);
    }
    return zp;
  }

  /**
   * Moves a token to the specified location.
   *
   * @param args the arguments to the function.
   */
  private static String moveToken(VariableResolver resolver, List<Object> args)
      throws ParserException {
    int x = FunctionUtil.paramAsInteger("moveToken", args, 0, false);
    int y = FunctionUtil.paramAsInteger("moveToken", args, 1, false);
    boolean useDistance =
        args.size() > 2 ? FunctionUtil.paramAsBoolean("moveToken", args, 2, false) : true;
    Token token = FunctionUtil.getTokenFromParam(resolver, "moveToken", args, 3, -1);

    if (useDistance) {
      // Remove pixel offset, so that getTokenX / getTokenY and coordinates match. Fixes #1757.
      Point offset = getInstance().getTokenPixelLocationOffset(token);
      x -= offset.x;
      y -= offset.y;
    }
    ZonePoint zp = getZonePoint(x, y, useDistance);
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setXY, zp.x, zp.y);
    return "";
  }

  /**
   * Centers the map on a new location.
   *
   * @param args The arguments to the function.
   * @return an empty string.
   * @throws ParserException if an error occurs.
   */
  private String gotoLoc(VariableResolver resolver, List<Object> args) throws ParserException {
    int x;
    int y;

    if (args.size() < 2) {
      Token token = FunctionUtil.getTokenFromParam(resolver, "goto", args, 0, -1);
      x = token.getX();
      y = token.getY();
      MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(x, y));
    } else {
      x = FunctionUtil.paramAsInteger("goto", args, 0, false);
      y = FunctionUtil.paramAsInteger("goto", args, 1, false);
      MapTool.getFrame().getCurrentZoneRenderer().centerOn(new CellPoint(x, y));
    }

    return "";
  }

  /**
   * Gets the distance for one cell on the current map.
   *
   * @return the distance for each cell.
   */
  private double getDistancePerCell() {
    return MapTool.getFrame().getCurrentZoneRenderer().getZone().getUnitsPerCell();
  }

  /**
   * Gets the cell point that the token is at.
   *
   * @param token the token.
   * @return the CellPoint where the token is.
   */
  public CellPoint getTokenCell(Token token) {
    Zone zone = token.getZoneRenderer().getZone();
    return zone.getGrid().convert(new ZonePoint(token.getX(), token.getY()));
  }

  /**
   * Returns the zones containing the identified token.
   *
   * @param identifier the identifier of the token.
   * @return all zones containing the token.
   */
  private Stream<Zone> getTokenZones(String identifier) {
    return MapTool.getFrame().getZoneRenderers().stream()
        .map(ZoneRenderer::getZone)
        .filter(zone -> zone.resolveToken(identifier) != null);
  }
}
