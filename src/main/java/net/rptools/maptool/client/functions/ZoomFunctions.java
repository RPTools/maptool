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
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONObject;

public class ZoomFunctions extends AbstractFunction {
  /** Singleton for class * */
  private static final ZoomFunctions instance = new ZoomFunctions();

  private static final String EQUALS = "=";

  private ZoomFunctions() {
    super(0, 6, "getZoom", "setZoom", "getViewArea", "setViewArea", "getViewCenter");
  }

  public static ZoomFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    if ("getZoom".equals(functionName)) {
      return getZ();
    }
    if ("setZoom".equals(functionName)) {
      return setZ(args);
    }
    if ("getViewArea".equals(functionName)) {
      return getViewArea(args);
    }
    if ("setViewArea".equals(functionName)) {
      return setViewArea(args);
    }
    if ("getViewCenter".equals(functionName)) {
      return getViewCenter(args);
    }
    return null;
  }

  /**
   * Sets the scale or zoom distance on the current zone where a value of 0.5 = 50%, 1 = 100% and 2
   * = 200%
   *
   * @param args
   * @return String
   * @throws ParserException
   */
  private String setZ(List<Object> args) throws ParserException {
    if (args.size() != 1) {
      throw new ParserException(
          I18N.getText("macro.function.general.wrongNumParam", "setZoom", 1, args.size()));
    }
    double zoom = 1;
    try {
      zoom = Double.valueOf(args.get(0).toString());
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN", "moveToken", 1, args.get(0).toString()));
    }
    MapTool.getFrame().getCurrentZoneRenderer().setScale(zoom);

    return "";
  }

  /**
   * Returns the scale or zoom distance on the current zone where a value of 0.5 = 50%, 1 = 100% and
   * 2 = 200%
   *
   * @return String
   * @throws ParserException
   */
  private String getZ() throws ParserException {
    return Double.valueOf(MapTool.getFrame().getCurrentZoneRenderer().getScale()).toString();
  }

  /**
   * Given a grid pixels or cell coordinates of top left (x1, y1) and bottom right (x2, y2) this
   * function returns a json of rectangular coordinates of the current view
   *
   * @param arg should be optional boolean pixels|grid, optional String delim
   * @return JSON of coordinates or String props with delim
   * @throws ParserException
   */
  private Object getViewArea(List<Object> args) throws ParserException {
    if (args.size() > 2) {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getViewArea", 2, args.size()));
    }

    boolean pixels = true;
    if (args.size() >= 1) {
      pixels = parseBoolean(args, 0);
    }

    String delim = ";";
    if (args.size() == 2) {
      delim = args.get(1).toString();
    }

    int offsetX = MapTool.getFrame().getCurrentZoneRenderer().getViewOffsetX() * -1;
    int offsetY = MapTool.getFrame().getCurrentZoneRenderer().getViewOffsetY() * -1;
    int width = MapTool.getFrame().getCurrentZoneRenderer().getWidth();
    int height = MapTool.getFrame().getCurrentZoneRenderer().getHeight();

    if (pixels) {
      if ("json".equalsIgnoreCase(delim)) {
        return createBoundsAsJSON(offsetX, offsetY, offsetX + width, offsetY + height);
      } else {
        return createBoundsAsStringProps(
            delim, offsetX, offsetY, offsetX + width, offsetY + height);
      }
    } else {
      ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
      CellPoint z1 =
          zoneRenderer.getZone().getGrid().convert(convertToZone(zoneRenderer, offsetX, offsetY));
      CellPoint z2 =
          zoneRenderer
              .getZone()
              .getGrid()
              .convert(convertToZone(zoneRenderer, offsetX + width, offsetY + height));

      if ("json".equalsIgnoreCase(delim)) {
        return createBoundsAsJSON(z1.x, z1.y, z2.x, z2.y);
      } else {
        return createBoundsAsStringProps(delim, z1.x, z1.y, z2.x, z2.y);
      }
    }
  }

  public static ZonePoint convertToZone(ZoneRenderer renderer, double x, double y) {
    double scale = renderer.getScale();
    double zX = (int) Math.floor(x / scale);
    double zY = (int) Math.floor(y / scale);
    return new ZonePoint((int) zX, (int) zY);
  }

  private Object createBoundsAsStringProps(
      String delim, int offsetX, int offsetY, int width, int height) {
    StringBuffer sb = new StringBuffer();
    sb.append("startX").append(EQUALS).append(offsetX).append(delim);
    sb.append("startY").append(EQUALS).append(offsetY).append(delim);
    sb.append("endX").append(EQUALS).append(width).append(delim);
    sb.append("endY").append(EQUALS).append(height);
    return sb.toString();
  }

  private JSONObject createBoundsAsJSON(int offsetX, int offsetY, int width, int height) {
    JSONObject bounds = new JSONObject();
    bounds.put("startX", offsetX);
    bounds.put("startY", offsetY);
    bounds.put("endX", width);
    bounds.put("endY", height);
    return bounds;
  }

  /**
   * Given a grid pixels or cell coordinates of top left (x1, y1) and bottom right (x2, y2) this
   * function centres the screen over this area.
   *
   * @param args should contain int x1, int y1, int x2, int y2, boolean pixels, boolean enforceView
   * @return
   * @throws ParserException
   */
  private String setViewArea(List<Object> args) throws ParserException {
    if (args.size() < 4) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", "setViewArea", 4, args.size()));
    }
    int x1 = 0;
    int y1 = 0;
    int x2 = 0;
    int y2 = 0;
    boolean pixels = true;
    boolean enforce = false;
    x1 = parseInteger(args, 0);
    y1 = parseInteger(args, 1);
    x2 = parseInteger(args, 2);
    y2 = parseInteger(args, 3);
    if (args.size() >= 5) pixels = parseBoolean(args, 4);
    if (args.size() >= 6) enforce = parseBoolean(args, 5);
    // If x & y not in pixels, use grid cell coordinates and convert to pixels
    if (!pixels) {
      Grid mapGrid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
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
    MapTool.getFrame().getCurrentZoneRenderer().enforceView(centreX, centreY, 1, width, height);
    // if requested, set all players to map and match view
    if (enforce && MapTool.getParser().isMacroTrusted()) {
      MapTool.serverCommand()
          .enforceZone(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId());
      MapTool.getFrame().getCurrentZoneRenderer().forcePlayersView();
    }
    return "";
  }

  private int parseInteger(List<Object> args, int param) throws ParserException {
    try {
      return Integer.valueOf(args.get(param).toString());
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentKeyType",
              "setViewArea",
              param,
              args.get(param).toString()));
    }
  }

  private boolean parseBoolean(List<Object> args, int param) throws ParserException {
    try {
      return AbstractTokenAccessorFunction.getBooleanValue(args.get(param));
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeInvalid",
              "setViewArea",
              param,
              args.get(param).toString()));
    }
  }

  /**
   * This function returns a json or String props of coordinates of the center of the current view
   *
   * @param arg should be optional boolean pixels|grid, optional String delim
   * @return JSON of coordinates or String props with delim
   * @throws ParserException
   */
  private Object getViewCenter(List<Object> args) throws ParserException {
    boolean pixels = true;
    if (args.size() > 0) pixels = parseBoolean(args, 0);

    String delim = ";";
    if (args.size() > 1) {
      delim = args.get(1).toString();
    }

    ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();

    int offsetX = zoneRenderer.getViewOffsetX() * -1;
    int width = zoneRenderer.getWidth();
    int centerX = (int) offsetX + (width / 2);

    int offsetY = zoneRenderer.getViewOffsetY() * -1;
    int height = zoneRenderer.getHeight();
    int centerY = (int) offsetY + (height / 2);

    if (!pixels) {
      CellPoint centerPoint =
          zoneRenderer.getZone().getGrid().convert(convertToZone(zoneRenderer, centerX, centerY));
      centerX = centerPoint.x;
      centerY = centerPoint.y;
    }

    if ("json".equalsIgnoreCase(delim)) {
      JSONObject center = new JSONObject();
      center.put("centerX", centerX);
      center.put("centerY", centerY);
      return center;
    } else {
      StringBuffer center = new StringBuffer();
      center.append("centerX").append(EQUALS).append(centerX).append(delim);
      center.append("centerY").append(EQUALS).append(centerY).append(delim);
      return center.toString();
    }
  }
}
