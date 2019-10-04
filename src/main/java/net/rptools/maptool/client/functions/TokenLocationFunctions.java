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

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class TokenLocationFunctions extends AbstractFunction {

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
        "getDistance",
        "moveToken",
        "goto",
        "getDistanceToXY",
        "setTokenDrawOrder",
        "moveTokenToMap",
        "moveTokenFromMap");
  }

  /** Gets an instance of TokenLocationFunctions. */
  public static TokenLocationFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
    MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();

    if (functionName.equals("getTokenX")) {
      return BigDecimal.valueOf(getTokenLocation(res, functionName, parameters).x);
    }
    if (functionName.equals("getTokenY")) {
      return BigDecimal.valueOf(getTokenLocation(res, functionName, parameters).y);
    }
    if (functionName.equals("getTokenDrawOrder")) {
      Token token = FunctionUtil.getTokenFromParam(res, functionName, parameters, 0, -1);
      return BigDecimal.valueOf(token.getZOrder());
    }
    if (functionName.equals("setTokenDrawOrder")) {
      Token token = FunctionUtil.getTokenFromParam(res, functionName, parameters, 1, -1);
      if (parameters.isEmpty()) {
        throw new ParserException(
            I18N.getText("macro.function.general.notEnoughParam", functionName, 1, 0));
      }
      if (!(parameters.get(0) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN",
                functionName,
                1,
                parameters.get(0).toString()));
      }
      MapTool.serverCommand()
          .updateTokenProperty(token, "setZOrder", ((BigDecimal) parameters.get(0)).intValue());
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      renderer.flushLight();
      return BigDecimal.valueOf(token.getZOrder());
    }
    if (functionName.equals("getDistance")) {
      return getDistance(res, parameters);
    }
    if (functionName.equals("getDistanceToXY")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 6);
      return getDistanceToXY(res, parameters);
    }
    if (functionName.equals("goto")) {
      return gotoLoc(res, parameters);
    }
    if (functionName.equals("moveToken")) {
      return moveToken(res, parameters);
    }
    if (functionName.equals("moveTokenToMap")) {
      return tokenMoveMap(true, parameters);
    }
    if (functionName.equals("moveTokenFromMap")) {
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
   * @throws ParserException
   */
  @SuppressWarnings("unchecked")
  private String tokenMoveMap(boolean fromCurrentMap, List<Object> args) throws ParserException {
    String functionName = fromCurrentMap ? "moveTokenToMap" : "moveTokenFromMap";
    if (args.size() < 2) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", functionName, 2, args.size()));
    }
    Object tokenString = args.get(0);
    String map = (String) args.get(1);

    List<String> tokens = new ArrayList<String>();

    Object json = JSONMacroFunctions.asJSON(tokenString);
    if (json instanceof JSONArray) {
      tokens.addAll((JSONArray) json);
    } else {
      tokens.add((String) tokenString);
    }
    Zone zone = null;
    List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
    for (ZoneRenderer zr : zrenderers) {
      Zone z = zr.getZone();
      if (z.getName().equalsIgnoreCase(map)) {
        zone = z;
        break;
      }
    }
    if (zone == null) {
      throw new ParserException(
          I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, map));
    }
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
    int x = 0;
    int y = 0;
    int z = zone.getLargestZOrder() + 1;

    if (args.size() > 2) {
      if (!(args.get(2) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", functionName, 2, args.get(2).toString()));
      } else {
        x = ((BigDecimal) args.get(2)).intValue();
      }
    }
    if (args.size() > 3) {
      if (!(args.get(3) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", functionName, 3, args.get(3).toString()));
      } else {
        y = ((BigDecimal) args.get(3)).intValue();
      }
    }
    if (args.size() > 4) {
      if (!(args.get(4) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", functionName, 4, args.get(4).toString()));
      } else {
        z = ((BigDecimal) args.get(4)).intValue();
      }
    }
    StringBuilder sb = new StringBuilder();
    for (String id : tokens) {
      Token token = fromZone.resolveToken(id);
      if (token == null) {
        sb.append(I18N.getText("macro.function.moveTokenMap.unknownToken", functionName, id))
            .append("<br>");
      } else {
        Grid grid = toZone.getGrid();

        ZonePoint zp = grid.convert(new CellPoint(x, y));
        x = zp.x;
        y = zp.y;

        token.setX(x);
        token.setY(y);
        token.setZOrder(z);
        toZone.putToken(token);
        MapTool.serverCommand().putToken(toZone.getId(), token);
        MapTool.serverCommand().removeToken(fromZone.getId(), token.getId());
        sb.append(I18N.getText("macro.function.moveTokenMap.movedToken", token.getName(), map))
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
   * @param res The variable resolver.
   * @param args The arguments.
   * @return the location of the token.
   * @throws ParserException if an error occurs.
   */
  private TokenLocation getTokenLocation(
      MapToolVariableResolver res, String functionName, List<Object> args) throws ParserException {
    Token token = FunctionUtil.getTokenFromParam(res, functionName, args, 1, -1);
    boolean useDistancePerCell = true;

    if (args.size() > 0) {
      if (!(args.get(0) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", functionName, 1, args.get(0).toString()));
      }
      BigDecimal val = (BigDecimal) args.get(0);
      useDistancePerCell = val.equals(BigDecimal.ZERO) ? false : true;
    }

    TokenLocation loc = new TokenLocation();

    if (useDistancePerCell) {
      Rectangle tokenBounds =
          token.getBounds(MapTool.getFrame().getCurrentZoneRenderer().getZone());
      loc.x = tokenBounds.x;
      loc.y = tokenBounds.y;
      loc.z = token.getZOrder();
    } else {
      CellPoint cellPoint = getTokenCell(token);
      int x = cellPoint.x;
      int y = cellPoint.y;

      loc.x = x;
      loc.y = y;
      loc.z = token.getZOrder();
    }

    return loc;
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
        for (int i = 0; i < points.length; i++) {
          if (cellx == points[i].x && celly == points[i].y) return true;
        }
      }
    } else {
      Rectangle bounds = token.getBounds(zone);
      for (int i = 0; i < points.length; i++) {
        if (bounds.contains(points[i])) return true;
      }
    }
    return false;
  }

  /**
   * Gets the distance to another token.
   *
   * @param res The variable resolver.
   * @param args arguments to the function.
   * @return the distance between tokens.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal getDistance(MapToolVariableResolver res, List<Object> args)
      throws ParserException {
    if (args.size() < 1) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", "getDistance", 1, args.size()));
    }

    Token target = FunctionUtil.getTokenFromParam(res, "getDistance", args, 0, -1);
    Token source = FunctionUtil.getTokenFromParam(res, "getDistance", args, 2, -1);

    boolean useDistancePerCell = true;
    if (args.size() > 1) {
      if (!(args.get(1) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", "getDistance", 2, args.get(1).toString()));
      }
      BigDecimal val = (BigDecimal) args.get(1);
      useDistancePerCell = val.equals(BigDecimal.ZERO) ? false : true;
    }

    String metric = null;
    if (args.size() > 3) {
      metric = (String) args.get(3);
    }

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
   * @param res The variable resolver.
   * @param args arguments to the function.
   * @return the distance between tokens.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal getDistanceToXY(MapToolVariableResolver res, List<Object> args)
      throws ParserException {
    final String fName = "getDistanceToXY";

    int x = FunctionUtil.paramAsInteger(fName, args, 0, false);
    int y = FunctionUtil.paramAsInteger(fName, args, 1, false);

    boolean useDistancePerCell = true;
    if (args.size() > 2) useDistancePerCell = FunctionUtil.paramAsBoolean(fName, args, 2, true);

    Token source = FunctionUtil.getTokenFromParam(res, fName, args, 3, -1);
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
   * Moves a token to the specified x,y location. If <code>units</code> is true, the incoming (x,y)
   * is treated as a <code>ZonePoint</code>. If <code>units</code> is false, the incoming (x,y) is
   * treated as a <code>CellPoint</code> and is converted to a ZonePoint by calling {@link
   * Grid#convert(CellPoint)}.
   *
   * @param token The token to move.
   * @param x the x co-ordinate of the destination.
   * @param y the y co-ordinate of the destination.
   * @param units whether the (x,y) coordinate is a <code>ZonePoint</code> (true) or <code>CellPoint
   *     </code> (false)
   */
  public void moveToken(Token token, int x, int y, boolean units) {
    Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
    int newX;
    int newY;

    if (units) {
      ZonePoint zp = new ZonePoint(x, y);
      newX = zp.x;
      newY = zp.y;
    } else {
      CellPoint cp = new CellPoint(x, y);
      ZonePoint zp = grid.convert(cp);
      newX = zp.x;
      newY = zp.y;
    }
    MapTool.serverCommand().updateTokenProperty(token, "setXY", newX, newY);
  }

  /**
   * Moves a token to the specified location.
   *
   * @param res the variable resolver.
   * @param args the arguments to the function.
   */
  private String moveToken(MapToolVariableResolver res, List<Object> args) throws ParserException {
    Token token = FunctionUtil.getTokenFromParam(res, "moveToken", args, 3, -1);
    boolean useDistance = true;

    if (args.size() < 2) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", "moveToken", 2, args.size()));
    }

    int x, y;

    if (!(args.get(0) instanceof BigDecimal)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN", "moveToken", 1, args.get(0).toString()));
    }
    if (!(args.get(1) instanceof BigDecimal)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN", "moveToken", 2, args.get(1).toString()));
    }

    x = ((BigDecimal) args.get(0)).intValue();
    y = ((BigDecimal) args.get(1)).intValue();

    if (args.size() > 2) {
      if (!(args.get(2) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", "moveToken", 3, args.get(2).toString()));
      }
      BigDecimal val = (BigDecimal) args.get(2);
      useDistance = val.equals(BigDecimal.ZERO) ? false : true;
    }
    moveToken(token, x, y, useDistance);
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = renderer.getZone();
    renderer.flushLight();
    return "";
  }

  /**
   * Centers the map on a new location.
   *
   * @param res The variable resolver.
   * @param args The arguments to the function.
   * @return an empty string.
   * @throws ParserException if an error occurs.
   */
  private String gotoLoc(MapToolVariableResolver res, List<Object> args) throws ParserException {
    Token token = null;
    int x;
    int y;

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "goto"));
    }

    if (args.size() < 2) {
      token = FunctionUtil.getTokenFromParam(res, "goto", args, 0, -1);
      x = token.getX();
      y = token.getY();
      MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(x, y));
    } else {

      if (!(args.get(0) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", "goto", 1, args.get(0).toString()));
      }

      if (!(args.get(1) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN", "goto", 2, args.get(1).toString()));
      }

      x = ((BigDecimal) args.get(0)).intValue();
      y = ((BigDecimal) args.get(1)).intValue();
      MapTool.getFrame().getCurrentZoneRenderer().centerOn(new CellPoint(x, y));
    }

    return "";
  }

  /**
   * Gets the distance for each cell.
   *
   * @return the distance for each cell.
   */
  private double getDistancePerCell() {
    return MapTool.getFrame().getCurrentZoneRenderer().getZone().getUnitsPerCell();
  }

  /**
   * Gets the cell point that the token is at.
   *
   * @param token
   * @return
   */
  public CellPoint getTokenCell(Token token) {
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    return zone.getGrid().convert(new ZonePoint(token.getX(), token.getY()));
  }
}
