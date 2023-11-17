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

import com.google.gson.JsonObject;
import java.awt.Rectangle;
import java.util.List;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class ZoomFunctions extends AbstractFunction {
  /** Singleton for class * */
  private static final ZoomFunctions instance = new ZoomFunctions();

  private static final String EQUALS = "=";

  private ZoomFunctions() {
    super(0, 6, "getZoom", "setZoom", "getViewArea", "setViewArea", "getViewCenter", "setZoomLock");
  }

  public static ZoomFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    if ("getZoom".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 0);
      return Double.valueOf(MapTool.getFrame().getCurrentZoneRenderer().getScale()).toString();
    }
    if ("setZoom".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 1);
      Double zoom = FunctionUtil.paramAsDouble(functionName, args, 0, true);
      MapTool.getFrame().getCurrentZoneRenderer().setScale(zoom);
      return "";
    }
    if ("getViewArea".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      boolean pixels =
          args.size() > 0 ? FunctionUtil.paramAsBoolean(functionName, args, 0, true) : true;
      String delim = args.size() > 1 ? args.get(1).toString() : ",";
      return getViewArea(pixels, delim);
    }
    if ("setViewArea".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 4, 6);
      Integer x1 = FunctionUtil.paramAsInteger(functionName, args, 0, true);
      Integer y1 = FunctionUtil.paramAsInteger(functionName, args, 1, true);
      Integer x2 = FunctionUtil.paramAsInteger(functionName, args, 2, true);
      Integer y2 = FunctionUtil.paramAsInteger(functionName, args, 3, true);
      boolean pixels =
          args.size() > 4 ? FunctionUtil.paramAsBoolean(functionName, args, 4, true) : true;
      boolean allPlayers =
          args.size() > 5 ? FunctionUtil.paramAsBoolean(functionName, args, 5, true) : false;
      return setViewArea(x1, y1, x2, y2, pixels, allPlayers);
    }
    if ("getViewCenter".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      boolean pixels =
          args.size() > 0 ? FunctionUtil.paramAsBoolean(functionName, args, 0, true) : true;
      String delim = args.size() > 1 ? args.get(1).toString() : ",";
      return getViewCenter(pixels, delim);
    }
    if ("setZoomLock".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 1);
      Boolean zoom = FunctionUtil.paramAsBoolean(functionName, args, 0, true);
      AppState.setZoomLocked(zoom);
      return "";
    }
    return null;
  }

  /**
   * Given a grid pixels or cell coordinates of top left (x1, y1) and bottom right (x2, y2) this
   * function returns a json of rectangular coordinates of the current view
   *
   * @param pixels boolean pixels|grid
   * @param delim the delimiter for the return string
   * @return JSON of coordinates or String props with delim
   */
  private static Object getViewArea(boolean pixels, String delim) {
    ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();

    int offsetX = zoneRenderer.getViewOffsetX() * -1;
    int offsetY = zoneRenderer.getViewOffsetY() * -1;
    int width = zoneRenderer.getWidth();
    int height = zoneRenderer.getHeight();

    // convert zoomed pixels to true pixels
    ZonePoint topLeft = convertToZone(zoneRenderer, offsetX, offsetY);
    ZonePoint bottomRight = convertToZone(zoneRenderer, offsetX + width, offsetY + height);

    if (pixels) {
      if ("json".equalsIgnoreCase(delim)) {
        return createBoundsAsJSON(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
      } else {
        return createBoundsAsStringProps(delim, topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
      }
    } else {
      CellPoint z1 = zoneRenderer.getZone().getGrid().convert(topLeft);
      CellPoint z2 = zoneRenderer.getZone().getGrid().convert(bottomRight);

      if ("json".equalsIgnoreCase(delim)) {
        return createBoundsAsJSON(z1.x, z1.y, z2.x, z2.y);
      } else {
        return createBoundsAsStringProps(delim, z1.x, z1.y, z2.x, z2.y);
      }
    }
  }

  /**
   * Returns zonePoint from x and y pixel coordinates, and from the scale of the renderer
   *
   * @param renderer the renderer of the zone
   * @param x the x coordinate
   * @param y the y coordinate
   * @return ZonePoint of the coordinates
   */
  public static ZonePoint convertToZone(ZoneRenderer renderer, double x, double y) {
    double scale = renderer.getScale();
    double zX = (int) Math.floor(x / scale);
    double zY = (int) Math.floor(y / scale);
    return new ZonePoint((int) zX, (int) zY);
  }

  private static Object createBoundsAsStringProps(
      String delim, int offsetX, int offsetY, int endX, int endY) {
    return "startX" + EQUALS + offsetX + delim + "startY" + EQUALS + offsetY + delim + "endX"
        + EQUALS + endX + delim + "endY" + EQUALS + endY;
  }

  private static JsonObject createBoundsAsJSON(int offsetX, int offsetY, int endX, int endY) {
    JsonObject bounds = new JsonObject();
    bounds.addProperty("startX", offsetX);
    bounds.addProperty("startY", offsetY);
    bounds.addProperty("endX", endX);
    bounds.addProperty("endY", endY);
    return bounds;
  }

  /**
   * Given a grid pixels or cell coordinates of top left (x1, y1) and bottom right (x2, y2) this
   * function centres the screen over this area.
   *
   * @param x1 x coordinate of the cell that will mark the upper left corner of the displayed area.
   * @param y1 y coordinate of the cell that will mark the upper left corner of the displayed area.
   * @param x2 x coordinate of the cell that will mark the lower right corner of the displayed area.
   * @param y2 y coordinate of the cell that will mark the lower right corner of the displayed area
   * @param pixels if 1 the coordinates are measured in pixels, otherwise measured in cells
   * @param allPlayers if 1 and called from a trusted macro, all players views will be set
   * @return empty string
   */
  private static String setViewArea(
      Integer x1, Integer y1, Integer x2, Integer y2, Boolean pixels, Boolean allPlayers) {
    ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    // If x & y not in pixels, use grid cell coordinates and convert to pixels
    if (!pixels) {
      Grid mapGrid = zoneRenderer.getZone().getGrid();
      Rectangle fromBounds = mapGrid.getBounds(new CellPoint(x1, y1));
      x1 = fromBounds.x;
      y1 = fromBounds.y;
      Rectangle toBounds = mapGrid.getBounds(new CellPoint(x2, y2));
      x2 = toBounds.x + toBounds.width;
      y2 = toBounds.y + toBounds.height;
    }
    // enforceView command uses point at centre of screen
    int width = x2 - x1;
    int height = y2 - y1;
    int centreX = x1 + (width / 2);
    int centreY = y1 + (height / 2);
    zoneRenderer.enforceView(centreX, centreY, 1, width, height);
    // if requested, set all players to map and match view
    if (allPlayers && MapTool.getParser().isMacroTrusted()) {
      MapTool.serverCommand().enforceZone(zoneRenderer.getZone().getId());
      zoneRenderer.forcePlayersView();
    }
    return "";
  }

  /**
   * This function returns a json or String props of coordinates of the center of the current view
   *
   * @param pixels boolean pixels|grid
   * @param delim the delimiter for the return string
   * @return JSON of coordinates or String props with delim
   */
  private static Object getViewCenter(boolean pixels, String delim) {
    ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();

    int offsetX = zoneRenderer.getViewOffsetX() * -1;
    int width = zoneRenderer.getWidth();

    int offsetY = zoneRenderer.getViewOffsetY() * -1;
    int height = zoneRenderer.getHeight();

    ZonePoint topLeft = convertToZone(zoneRenderer, offsetX, offsetY);
    ZonePoint bottomRight = convertToZone(zoneRenderer, offsetX + width, offsetY + height);

    int centerX = (topLeft.x + bottomRight.x) / 2;
    int centerY = (topLeft.y + bottomRight.y) / 2;

    if (!pixels) {
      CellPoint centerPoint =
          zoneRenderer.getZone().getGrid().convert(new ZonePoint(centerX, centerY));
      centerX = centerPoint.x;
      centerY = centerPoint.y;
    }

    if ("json".equalsIgnoreCase(delim)) {
      JsonObject center = new JsonObject();
      center.addProperty("centerX", centerX);
      center.addProperty("centerY", centerY);
      return center;
    } else {
      return "centerX" + EQUALS + centerX + delim + "centerY" + EQUALS + centerY;
    }
  }
}
