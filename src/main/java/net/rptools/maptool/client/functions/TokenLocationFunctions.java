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

import java.awt.Rectangle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.SquareGrid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
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
        5,
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
      Token token = getTokenFromParam(res, functionName, parameters, 0);
      return BigDecimal.valueOf(token.getZOrder());
    }
    if (functionName.equals("setTokenDrawOrder")) {
      Token token = getTokenFromParam(res, functionName, parameters, 1);
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
      token.setZOrder(((BigDecimal) parameters.get(0)).intValue());
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      Zone zone = renderer.getZone();
      zone.putToken(token);
      MapTool.serverCommand().putToken(zone.getId(), token);
      renderer.flushLight();
      return BigDecimal.valueOf(token.getZOrder());
    }
    if (functionName.equals("getDistance")) {
      return getDistance(res, parameters);
    }
    if (functionName.equals("getDistanceToXY")) {
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
    Token token = getTokenFromParam(res, functionName, args, 1);
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
   * Gets the distance between two tokens.
   *
   * @param source
   * @param target
   * @param gridUnits
   * @return
   * @throws ParserException
   */
  public double getDistance(Token source, Token target, boolean units, String metric)
      throws ParserException {
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    Grid grid = renderer.getZone().getGrid();

    if (grid.getCapabilities().isPathingSupported() && !NO_GRID.equals(metric)) {

      // Get which cells our tokens occupy
      Set<CellPoint> sourceCells =
          source
              .getFootprint(grid)
              .getOccupiedCells(grid.convert(new ZonePoint(source.getX(), source.getY())));
      Set<CellPoint> targetCells =
          target
              .getFootprint(grid)
              .getOccupiedCells(grid.convert(new ZonePoint(target.getX(), target.getY())));

      ZoneWalker walker;
      if (metric != null && grid instanceof SquareGrid) {
        try {
          WalkerMetric wmetric = WalkerMetric.valueOf(metric);
          walker = new AStarSquareEuclideanWalker(renderer.getZone(), wmetric);

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
        for (CellPoint tcell : targetCells) {
          walker.setWaypoints(scell, tcell);
          distance = Math.min(distance, walker.getDistance());
        }
      }

      if (units) {
        return distance;
      } else {
        return distance / getDistancePerCell();
      }
    } else {

      double d = source.getFootprint(grid).getScale();
      double sourceCenterX = source.getX() + (d * grid.getSize()) / 2;
      double sourceCenterY = source.getY() + (d * grid.getSize()) / 2;
      d = target.getFootprint(grid).getScale();
      double targetCenterX = target.getX() + (d * grid.getSize()) / 2;
      double targetCenterY = target.getY() + (d * grid.getSize()) / 2;
      double a = sourceCenterX - targetCenterX;
      double b = sourceCenterY - targetCenterY;
      double h = Math.sqrt(a * a + b * b);
      h /= renderer.getZone().getGrid().getSize();
      if (units) {
        h *= renderer.getZone().getUnitsPerCell();
      }
      return h;
    }
  }

  /**
   * Gets the distance to a target x,y co-ordinate following map movement rules.
   *
   * @param source The token to get the distance from.
   * @param x the x co-ordinate to get the distance to.
   * @param y the y co-ordinate to get the distance to.
   * @param units get the distance in the units specified for the map.
   * @param metric The metric used.
   * @return
   * @throws ParserException when an error occurs
   */
  public double getDistance(Token source, int x, int y, boolean units, String metric)
      throws ParserException {
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    Grid grid = renderer.getZone().getGrid();

    if (grid.getCapabilities().isPathingSupported() && !NO_GRID.equals(metric)) {

      // Get which cells our tokens occupy
      Set<CellPoint> sourceCells =
          source
              .getFootprint(grid)
              .getOccupiedCells(grid.convert(new ZonePoint(source.getX(), source.getY())));

      ZoneWalker walker;
      if (metric != null && grid instanceof SquareGrid) {
        try {
          WalkerMetric wmetric = WalkerMetric.valueOf(metric);
          walker = new AStarSquareEuclideanWalker(renderer.getZone(), wmetric);

        } catch (IllegalArgumentException e) {
          throw new ParserException(
              I18N.getText("macro.function.getDistance.invalidMetric", metric));
        }
      } else {
        walker = grid.createZoneWalker();
      }

      // Get the distances from each source to target cell and keep the minimum one
      double distance = Double.MAX_VALUE;
      CellPoint targetCell = new CellPoint(x, y);
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

      double d = source.getFootprint(grid).getScale();
      double sourceCenterX = source.getX() + (d * grid.getSize()) / 2;
      double sourceCenterY = source.getY() + (d * grid.getSize()) / 2;
      double a = sourceCenterX - x;
      double b = sourceCenterY - y;
      double h = Math.sqrt(a * a + b * b);
      h /= renderer.getZone().getGrid().getSize();
      if (units) {
        h *= renderer.getZone().getUnitsPerCell();
      }
      return h;
    }
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

    Token target = getTokenFromParam(res, "getDistance", args, 0);
    Token source = getTokenFromParam(res, "getDistance", args, 2);

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
    if (args.size() < 2) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", "getDistance", 2, args.size()));
    }

    Token source = getTokenFromParam(res, "getDistanceToXY", args, 3);

    if (!(args.get(0) instanceof BigDecimal)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN",
              "getDistanceToXY",
              1,
              args.get(0).toString()));
    }
    if (!(args.get(1) instanceof BigDecimal)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN",
              "getDistanceToXY",
              2,
              args.get(1).toString()));
    }

    int x = ((BigDecimal) args.get(0)).intValue();
    int y = ((BigDecimal) args.get(1)).intValue();

    boolean useDistancePerCell = true;
    if (args.size() > 2) {
      if (!(args.get(2) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN",
                "getDistanceXY",
                3,
                args.get(2).toString()));
      }
      BigDecimal val = (BigDecimal) args.get(2);
      useDistancePerCell = val.equals(BigDecimal.ZERO) ? false : true;
    }

    String metric = null;
    if (args.size() > 4) {
      metric = (String) args.get(4);
    }

    double dist = getDistance(source, x, y, useDistancePerCell, metric);

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

    if (units) {
      ZonePoint zp = new ZonePoint(x, y);
      token.setX(zp.x);
      token.setY(zp.y);
    } else {
      CellPoint cp = new CellPoint(x, y);
      ZonePoint zp = grid.convert(cp);
      token.setX(zp.x);
      token.setY(zp.y);
    }
  }

  /**
   * Moves a token to the specified location.
   *
   * @param token The token to move.
   * @param args the arguments to the function.
   */
  private String moveToken(MapToolVariableResolver res, List<Object> args) throws ParserException {
    Token token = getTokenFromParam(res, "moveToken", args, 3);
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
    zone.putToken(token);
    MapTool.serverCommand().putToken(zone.getId(), token);
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
      token = getTokenFromParam(res, "goto", args, 0);
      x = token.getX();
      y = token.getY();
      MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(x, y));
    } else {

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

  /**
   * Gets the token from the specified index or returns the token in context. This method will check
   * the list size before trying to retrieve the token so it is safe to use for functions that have
   * the token as an optional argument.
   *
   * @param res The variable resolver.
   * @param functionName The function name (used for generating exception messages).
   * @param param The parameters for the function.
   * @param index The index to find the token at.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  private Token getTokenFromParam(
      MapToolVariableResolver res, String functionName, List<Object> param, int index)
      throws ParserException {
    Token token;
    if (param.size() > index) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", functionName));
      }
      token = FindTokenFunctions.findToken(param.get(index).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", functionName, param.get(index).toString()));
      }
    } else {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
      }
    }
    return token;
  }
}
