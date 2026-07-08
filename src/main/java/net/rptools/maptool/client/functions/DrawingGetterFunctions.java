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
import java.math.BigDecimal;
import java.util.List;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

public class DrawingGetterFunctions extends DrawingFunctions {
  private static final DrawingGetterFunctions instance = new DrawingGetterFunctions();

  public static DrawingGetterFunctions getInstance() {
    return instance;
  }

  private static final String FUNC_GET_DRAWABLE_INFO = "getDrawableInfo";
  private static final String FUNC_GET_DRAWABLE_LAYER = "getDrawableLayer";
  private static final String FUNC_GET_DRAWABLE_NAME = "getDrawableName";
  private static final String FUNC_GET_DRAWABLE_X = "getDrawableX";
  private static final String FUNC_GET_DRAWABLE_Y = "getDrawableY";
  private static final String FUNC_GET_DRAWABLE_CENTER_X = "getDrawableCenterX";
  private static final String FUNC_GET_DRAWABLE_CENTER_Y = "getDrawableCenterY";
  private static final String FUNC_GET_DRAWABLE_MAX_X = "getDrawableMaxX";
  private static final String FUNC_GET_DRAWABLE_MAX_Y = "getDrawableMaxY";
  private static final String FUNC_GET_DRAWABLE_HEIGHT = "getDrawableHeight";
  private static final String FUNC_GET_DRAWABLE_WIDTH = "getDrawableWidth";

  private DrawingGetterFunctions() {
    super(
        0,
        3,
        "getDrawingLayer",
        "getDrawingOpacity",
        "getDrawingProperties",
        "getPenColor",
        "getFillColor",
        "getDrawingEraser",
        "getPenWidth",
        "getLineCap",
        "getDrawingInfo",
        FUNC_GET_DRAWABLE_INFO,
        FUNC_GET_DRAWABLE_LAYER,
        FUNC_GET_DRAWABLE_NAME,
        FUNC_GET_DRAWABLE_X,
        FUNC_GET_DRAWABLE_Y,
        FUNC_GET_DRAWABLE_CENTER_X,
        FUNC_GET_DRAWABLE_CENTER_Y,
        FUNC_GET_DRAWABLE_MAX_X,
        FUNC_GET_DRAWABLE_MAX_Y,
        FUNC_GET_DRAWABLE_HEIGHT,
        FUNC_GET_DRAWABLE_WIDTH);
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    checkTrusted(functionName);

    if (FUNC_GET_DRAWABLE_INFO.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_LAYER.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_NAME.equalsIgnoreCase(functionName)) {
      // [drawingRef [, mapRef]]
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      // mapRef - defaults to current zone/map
      Zone zone = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 1).getZone();

      // drawableRef - defaults to the drawable selected by the respective pointer tool
      DrawnElement de =
          !parameters.isEmpty()
              ? findDrawnElement(functionName, zone, parameters.getFirst().toString())
              : getSingleSelectedDrawnElement(functionName, zone);

      if (FUNC_GET_DRAWABLE_INFO.equalsIgnoreCase(functionName)) {
        return getDrawingJSONInfo(FUNC_GET_DRAWABLE_INFO, zone, de.getDrawable().getId());
      } else if (FUNC_GET_DRAWABLE_LAYER.equalsIgnoreCase(functionName)) {
        return getDrawable(FUNC_GET_DRAWABLE_LAYER, zone, de.getDrawable().getId())
            .getLayer()
            .name();
      } else if (FUNC_GET_DRAWABLE_NAME.equalsIgnoreCase(functionName)) {
        return getDrawingName(FUNC_GET_DRAWABLE_NAME, zone, de.getDrawable().getId());
      } else {
        return null;
      }

    } else if (FUNC_GET_DRAWABLE_X.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_Y.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_CENTER_X.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_CENTER_Y.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_MAX_X.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_MAX_Y.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_HEIGHT.equalsIgnoreCase(functionName)
        || FUNC_GET_DRAWABLE_WIDTH.equalsIgnoreCase(functionName)) {
      // [units [, drawingRef [, mapRef]]]
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);

      // units - defaults to PIXELS
      int units =
          !parameters.isEmpty()
              ? FunctionUtil.paramAsInteger(functionName, parameters, 0, false)
              : DrawableUnit.getPixelsIndex();
      DrawableUnit drawableUnit = DrawableUnit.valueOfIndex(units);
      if (drawableUnit == null) {
        throw new ParserException(
            I18N.getText("macro.function.drawable.unknownDrawableUnits", functionName, units));
      }

      // mapRef - defaults to current zone/map
      Zone zone = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 2).getZone();

      // drawableRef - defaults to the drawable selected by the respective pointer tool
      DrawnElement de =
          parameters.size() > 1
              ? findDrawnElement(functionName, zone, parameters.get(1).toString())
              : getSingleSelectedDrawnElement(functionName, zone);

      Rectangle bounds = getDrawingBounds(functionName, zone, de.getDrawable().getId());
      int dimension = 0;
      if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_X)) {
        dimension = bounds.x;
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_Y)) {
        dimension = bounds.y;
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_CENTER_X)) {
        dimension = (int) bounds.getCenterX();
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_CENTER_Y)) {
        dimension = (int) bounds.getCenterY();
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_MAX_X)) {
        dimension = (int) bounds.getMaxX();
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_MAX_Y)) {
        dimension = (int) bounds.getMaxY();
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_HEIGHT)) {
        dimension = bounds.height;
      } else if (functionName.equalsIgnoreCase(FUNC_GET_DRAWABLE_WIDTH)) {
        dimension = bounds.width;
      }
      return BigDecimal.valueOf(
          DrawableUnit.convert(zone, dimension, DrawableUnit.PIXELS, drawableUnit));

    } else {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String mapName = parameters.get(0).toString();
      String id = parameters.get(1).toString();
      Zone map = FunctionUtil.getZoneRenderer(functionName, mapName).getZone();
      GUID guid = getGUID(functionName, id);
      if ("getDrawingLayer".equalsIgnoreCase(functionName)) {
        return getDrawable(functionName, map, guid).getLayer().name();
      } else if ("getDrawingOpacity".equalsIgnoreCase(functionName)) {
        return getPen(functionName, map, guid).getOpacity();
      } else if ("getDrawingProperties".equalsIgnoreCase(functionName)) {
        return getPen(functionName, map, guid);
      } else if ("getPenColor".equalsIgnoreCase(functionName)) {
        return paintToString(getPen(functionName, map, guid).getPaint());
      } else if ("getFillColor".equalsIgnoreCase(functionName)) {
        return paintToString(getPen(functionName, map, guid).getBackgroundPaint());
      } else if ("getDrawingEraser".equalsIgnoreCase(functionName)) {
        return getPen(functionName, map, guid).isEraser() ? BigDecimal.ONE : BigDecimal.ZERO;
      } else if ("getPenWidth".equalsIgnoreCase(functionName)) {
        return getPen(functionName, map, guid).getThickness();
      } else if ("getLineCap".equalsIgnoreCase(functionName)) {
        return getPen(functionName, map, guid).getSquareCap() ? BigDecimal.ONE : BigDecimal.ZERO;
      } else if ("getDrawingInfo".equalsIgnoreCase(functionName)) {
        return getDrawingJSONInfo(functionName, map, guid);
      }
      return null;
    }
  }
}
