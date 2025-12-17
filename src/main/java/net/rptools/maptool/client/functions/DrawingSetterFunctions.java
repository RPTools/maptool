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

import java.util.List;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

public class DrawingSetterFunctions extends DrawingFunctions {
  private static final DrawingSetterFunctions instance = new DrawingSetterFunctions();

  public static DrawingSetterFunctions getInstance() {
    return instance;
  }

  private static final String FUNC_SET_DRAWABLE_LAYER = "setDrawableLayer";
  private static final String FUNC_SET_DRAWABLE_NAME = "setDrawableName";

  private DrawingSetterFunctions() {
    super(
        1,
        3,
        "setDrawingLayer",
        "setDrawingOpacity",
        "setDrawingProperties",
        "setPenColor",
        "setFillColor",
        "setDrawingEraser",
        "setPenWidth",
        "setLineCap",
        "setDrawingName",
        FUNC_SET_DRAWABLE_LAYER,
        FUNC_SET_DRAWABLE_NAME);
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    checkTrusted(functionName);

    if (FUNC_SET_DRAWABLE_LAYER.equalsIgnoreCase(functionName)) {
      // layer, [drawingRef [, mapRef]]
      FunctionUtil.checkNumberParam(FUNC_SET_DRAWABLE_LAYER, parameters, 1, 3);
      Layer layer = getLayer(parameters.getFirst().toString());
      ZoneRenderer renderer =
          FunctionUtil.getZoneRendererFromParam(FUNC_SET_DRAWABLE_LAYER, parameters, 2);
      Zone zone = renderer.getZone();
      DrawnElement de =
          parameters.size() < 2
              ? getSingleSelectedDrawnElement(FUNC_SET_DRAWABLE_LAYER, zone)
              : findDrawnElement(FUNC_SET_DRAWABLE_LAYER, zone, parameters.get(1).toString());
      return changeLayer(zone, layer, de.getDrawable().getId()).name();

    } else if (FUNC_SET_DRAWABLE_NAME.equalsIgnoreCase(functionName)) {
      // name, [drawingRef [, mapRef]]
      FunctionUtil.checkNumberParam(FUNC_SET_DRAWABLE_NAME, parameters, 1, 3);
      String name = parameters.getFirst().toString();
      ZoneRenderer renderer =
          FunctionUtil.getZoneRendererFromParam(FUNC_SET_DRAWABLE_NAME, parameters, 2);
      Zone zone = renderer.getZone();
      DrawnElement de =
          parameters.size() < 2
              ? getSingleSelectedDrawnElement(FUNC_SET_DRAWABLE_NAME, zone)
              : findDrawnElement(FUNC_SET_DRAWABLE_NAME, zone, parameters.get(1).toString());
      setDrawingName(FUNC_SET_DRAWABLE_NAME, zone, de.getDrawable().getId(), name);
      return "";

    } else {
      FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
      String mapName = parameters.get(0).toString();
      String id = parameters.get(1).toString();
      Zone map = FunctionUtil.getZoneRenderer(functionName, mapName).getZone();
      GUID guid = getGUID(functionName, id);
      if ("setDrawingLayer".equalsIgnoreCase(functionName)) {
        Layer layer = getLayer(parameters.get(2).toString());
        return changeLayer(map, layer, guid).name();
      } else if ("setDrawingOpacity".equalsIgnoreCase(functionName)) {
        String opacity = parameters.get(2).toString();
        float op = getFloatPercent(functionName, opacity);
        setDrawingOpacity(functionName, map, guid, op);
        return "";
      } else if ("setDrawingProperties".equalsIgnoreCase(functionName)) {
        Pen pen = (Pen) parameters.get(2);
        setPen(functionName, map, guid, pen);
        return "";
      } else if ("setPenColor".equalsIgnoreCase(functionName)) {
        String paint = parameters.get(2).toString();
        if ("".equalsIgnoreCase(paint)) {
          getPen(functionName, map, guid).setPaint(null);
        } else {
          getPen(functionName, map, guid).setPaint(FunctionUtil.getPaintFromString(paint));
        }
        return "";
      } else if ("setFillColor".equalsIgnoreCase(functionName)) {
        String paint = parameters.get(2).toString();
        if ("".equalsIgnoreCase(paint)) {
          getPen(functionName, map, guid).setBackgroundPaint(null);
        } else {
          getPen(functionName, map, guid)
              .setBackgroundPaint(FunctionUtil.getPaintFromString(paint));
        }
        return "";
      } else if ("setDrawingEraser".equalsIgnoreCase(functionName)) {
        boolean eraser = parseBoolean(functionName, parameters, 2);
        Pen p = getPen(functionName, map, guid);
        p.setEraser(eraser);
        return "";
      } else if ("setPenWidth".equalsIgnoreCase(functionName)) {
        String penWidth = parameters.get(2).toString();
        float pw = getFloat(functionName, penWidth);
        getPen(functionName, map, guid).setThickness(pw);
        return "";
      } else if ("setLineCap".equalsIgnoreCase(functionName)) {
        boolean squareCap = parseBoolean(functionName, parameters, 2);
        Pen p = getPen(functionName, map, guid);
        p.setSquareCap(squareCap);
        return "";
      } else if ("setDrawingName".equalsIgnoreCase(functionName)) {
        String name = parameters.get(2).toString();
        setDrawingName(functionName, map, guid, name);
        return "";
      }
      return null;
    }
  }
}
