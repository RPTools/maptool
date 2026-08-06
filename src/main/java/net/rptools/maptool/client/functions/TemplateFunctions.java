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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TemplateFunctions extends DrawingFunctions {
  private static final TemplateFunctions instance = new TemplateFunctions();

  public static TemplateFunctions getInstance() {
    return instance;
  }

  private static final String FUNC_CREATE_TEMPLATE = "createTemplate";
  private static final String FUNC_GET_TEMPLATE_RADIUS = "getTemplateRadius";
  private static final String FUNC_GET_TEMPLATE_X = "getTemplateX";
  private static final String FUNC_GET_TEMPLATE_Y = "getTemplateY";
  private static final String FUNC_MOVE_TEMPLATE = "moveTemplate";

  private final Logger log = LogManager.getLogger(TemplateFunctions.class);

  /** The minimum radius value allowed. */
  private final int MIN_RADIUS = AbstractTemplate.MIN_RADIUS;

  private TemplateFunctions() {
    super(
        0,
        5,
        FUNC_CREATE_TEMPLATE,
        FUNC_GET_TEMPLATE_RADIUS,
        FUNC_GET_TEMPLATE_X,
        FUNC_GET_TEMPLATE_Y,
        FUNC_MOVE_TEMPLATE);
  }

  /**
   * TemplateType Enumeration, including <code>template</code> method to return the appropriate
   * template class.
   */
  private enum TemplateType {
    BLAST() {
      @Override
      public AbstractTemplate template() {
        return new BlastTemplate();
      }
    },
    BURST() {
      @Override
      public AbstractTemplate template() {
        return new BurstTemplate();
      }
    },
    CONE() {
      @Override
      public AbstractTemplate template() {
        return new ConeTemplate();
      }
    },
    LINE() {
      @Override
      public AbstractTemplate template() {
        return new LineTemplate();
      }
    },
    LINECELL() {
      @Override
      public AbstractTemplate template() {
        return new LineCellTemplate();
      }
    },
    RADIUS() {
      @Override
      public AbstractTemplate template() {
        return new RadiusTemplate();
      }
    },
    RADIUSCELL() {
      @Override
      public AbstractTemplate template() {
        return new RadiusCellTemplate();
      }
    },
    WALL() {
      @Override
      public AbstractTemplate template() {
        return new WallTemplate();
      }
    };

    TemplateType() {}

    public abstract AbstractTemplate template();
  }

  /** Evaluate the function(s) */
  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    FunctionUtil.blockUntrustedMacro(functionName);

    if (FUNC_CREATE_TEMPLATE.equalsIgnoreCase(functionName)) {
      // templateType, radius/wallPath [, units, [, options [, delim [, mapRef ]]]]
      FunctionUtil.checkNumberParam(FUNC_CREATE_TEMPLATE, parameters, 2, 6);

      // template type, determine by name (i.e. enum value)
      String templateType = parameters.getFirst().toString().toUpperCase().trim();
      TemplateType tt;
      try {
        tt = TemplateType.valueOf(templateType);
      } catch (IllegalArgumentException iae) {
        throw new ParserException(
            I18N.getText(
                "macro.function.template.unknownTemplateType", FUNC_CREATE_TEMPLATE, templateType));
      }
      AbstractTemplate abstractTemplate = tt.template();

      // radius, for non wall templates
      // or wall path, for wall templates
      int radius =
          !(abstractTemplate instanceof WallTemplate)
              ? FunctionUtil.paramAsInteger(FUNC_CREATE_TEMPLATE, parameters, 1, false)
              : 0;
      String wallPath =
          (abstractTemplate instanceof WallTemplate) ? parameters.get(1).toString().trim() : "";

      // (optional) units - defaults to DISTANCE
      int units =
          parameters.size() > 2
              ? FunctionUtil.paramAsInteger(functionName, parameters, 2, false)
              : DrawableUnit.getDistanceIndex();
      DrawableUnit drawableUnit = DrawableUnit.valueOfIndex(units);
      if (drawableUnit == null && parameters.size() > 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.drawable.unknownDrawableUnits", functionName, parameters.get(2)));
      }

      // (optional) template/pen options
      // (optional) delimiter for template/pen options, defaulting to a comma
      String delimiter =
          parameters.size() > 4
              ? FunctionUtil.paramAsString(FUNC_CREATE_TEMPLATE, parameters, 4, false)
              : ",";
      JsonObject options =
          parameters.size() > 3
              ? FunctionUtil.jsonWithLowerCaseKeys(
                  FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                      FUNC_CREATE_TEMPLATE, parameters, 3, delimiter))
              : new JsonObject();

      // (optional) mapRef, defaulting to the current map
      ZoneRenderer zoneRenderer =
          FunctionUtil.getZoneRendererFromParam(FUNC_CREATE_TEMPLATE, parameters, 5);

      return createTemplate(
          FUNC_CREATE_TEMPLATE,
          abstractTemplate,
          radius,
          wallPath,
          drawableUnit,
          options,
          zoneRenderer);

    } else if (FUNC_GET_TEMPLATE_RADIUS.equalsIgnoreCase(functionName)) {
      // [units, [templateRef [, mapRef]]]
      FunctionUtil.checkNumberParam(FUNC_GET_TEMPLATE_RADIUS, parameters, 0, 3);

      // (optional) units - defaults to DISTANCE
      int units =
          !parameters.isEmpty()
              ? FunctionUtil.paramAsInteger(FUNC_GET_TEMPLATE_RADIUS, parameters, 0, false)
              : DrawableUnit.getDistanceIndex();
      DrawableUnit drawableUnit = DrawableUnit.valueOfIndex(units);
      if (drawableUnit == null && !parameters.isEmpty()) {
        throw new ParserException(
            I18N.getText(
                "macro.function.drawable.unknownDrawableUnits",
                functionName,
                parameters.getFirst()));
      }

      // (optional) mapRef, defaulting to the current map
      Zone zone =
          FunctionUtil.getZoneRendererFromParam(FUNC_GET_TEMPLATE_RADIUS, parameters, 2).getZone();

      // (optional) templateRef - defaults to the drawable selected by the respective pointer tool
      DrawnElement de =
          parameters.size() < 2
              ? getSingleSelectedDrawnElement(FUNC_GET_TEMPLATE_RADIUS, zone)
              : findDrawnElement(FUNC_GET_TEMPLATE_RADIUS, zone, parameters.get(1).toString());

      if (!(de.getDrawable() instanceof AbstractTemplate)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.template.unknownTemplate", functionName, de.getDrawable().getId()));
      }

      if (de.getDrawable() instanceof WallTemplate) {
        throw new ParserException(
            I18N.getText(
                "macro.function.template.invalidRadius",
                functionName,
                de.getDrawable().getClass().getSimpleName()));
      }

      return BigDecimal.valueOf(
          getTemplateRadius(FUNC_GET_TEMPLATE_RADIUS, zone, de, drawableUnit));

    } else if (FUNC_GET_TEMPLATE_X.equalsIgnoreCase(functionName)
        || FUNC_GET_TEMPLATE_Y.equalsIgnoreCase(functionName)) {
      // [units [,templateRef [, mapRef]]]
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);

      // (optional) units - defaults to PIXELS
      int units =
          !parameters.isEmpty()
              ? FunctionUtil.paramAsInteger(functionName, parameters, 0, false)
              : DrawableUnit.getPixelsIndex();
      DrawableUnit drawableUnit = DrawableUnit.valueOfIndex(units);
      if (drawableUnit == null && !parameters.isEmpty()) {
        throw new ParserException(
            I18N.getText(
                "macro.function.drawable.unknownDrawableUnits",
                functionName,
                parameters.getFirst()));
      }

      // zone/map
      Zone zone = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 2).getZone();

      // (optional) templateRef - defaults to the drawable selected by the respective pointer tool
      DrawnElement de =
          parameters.size() < 2
              ? getSingleSelectedDrawnElement(functionName, zone)
              : findDrawnElement(functionName, zone, parameters.get(1).toString());
      if (!(de.getDrawable() instanceof AbstractTemplate)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.template.unknownTemplate", functionName, de.getDrawable().getId()));
      }

      ZonePoint zp = getTemplateVertex(functionName, de);
      CellPoint cp = zone.getGrid().convert(zp);

      double dimension = 0;
      if (zp != null) {
        if (functionName.equalsIgnoreCase(FUNC_GET_TEMPLATE_X)) {
          switch (drawableUnit) {
            case CELLS -> dimension = cp.x;
            case DISTANCE ->
                dimension = DrawableUnit.convert(zone, cp.x, DrawableUnit.CELLS, drawableUnit);
            case PIXELS -> dimension = zp.x;
            default -> dimension = 0;
          }
        } else if (functionName.equalsIgnoreCase(FUNC_GET_TEMPLATE_Y)) {
          switch (drawableUnit) {
            case CELLS -> dimension = cp.y;
            case DISTANCE ->
                dimension = DrawableUnit.convert(zone, cp.y, DrawableUnit.CELLS, drawableUnit);
            case PIXELS -> dimension = zp.y;
            default -> dimension = 0;
          }
        }
        return BigDecimal.valueOf((int) dimension);
      }
      return null;

    } else if (FUNC_MOVE_TEMPLATE.equalsIgnoreCase(functionName)) {
      // x, y, [, units [, templateRef [, mapRef]]]
      FunctionUtil.checkNumberParam(FUNC_MOVE_TEMPLATE, parameters, 2, 5);

      // x & y
      int x = FunctionUtil.paramAsInteger(FUNC_MOVE_TEMPLATE, parameters, 0, false);
      int y = FunctionUtil.paramAsInteger(FUNC_MOVE_TEMPLATE, parameters, 1, false);

      // (optional) units - defaults to PIXELS
      int units =
          parameters.size() > 2
              ? FunctionUtil.paramAsInteger(functionName, parameters, 2, false)
              : DrawableUnit.getPixelsIndex();
      DrawableUnit drawableUnit = DrawableUnit.valueOfIndex(units);
      if (drawableUnit == null && parameters.size() > 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.drawable.unknownDrawableUnits", functionName, parameters.get(2)));
      }

      // (optional) renderer & zone/map - defaults to the current
      ZoneRenderer renderer =
          FunctionUtil.getZoneRendererFromParam(FUNC_MOVE_TEMPLATE, parameters, 4);
      Zone zone = renderer.getZone();

      // (optional) drawn element - defaults to the drawable selected by the respective pointer tool
      DrawnElement de =
          parameters.size() > 3
              ? findDrawnElement(FUNC_MOVE_TEMPLATE, zone, parameters.get(3).toString())
              : getSingleSelectedDrawnElement(FUNC_MOVE_TEMPLATE, zone);
      if (!(de.getDrawable() instanceof AbstractTemplate)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.template.unknownTemplate", functionName, de.getDrawable().getId()));
      }

      moveTemplate(renderer, de, x, y, drawableUnit);
      return "";
    }

    return null;
  }

  /*
   * METHODS
   */

  /**
   * Create a template.
   *
   * @param functionName this is used in exception messages
   * @param abstractTemplate a typed template
   * @param radius n radius/size for non-wall templates
   * @param wallPath XnYnZnNnEnSnWn path notation for wall templates
   * @param drawableUnit which drawableUnits to use
   * @param options template and pen options as a JsonObject
   * @param renderer the zone renderer
   * @return the drawingId of the created template
   * @throws ParserException parser exception
   */
  private GUID createTemplate(
      String functionName,
      AbstractTemplate abstractTemplate,
      int radius,
      String wallPath,
      DrawableUnit drawableUnit,
      JsonObject options,
      ZoneRenderer renderer)
      throws ParserException {

    Zone zone = renderer.getZone();

    // template radius to be in CELLS
    int convertedRadius =
        drawableUnit == DrawableUnit.CELLS || radius == 0
            ? radius
            : DrawableUnit.convert(zone, radius, drawableUnit, DrawableUnit.CELLS);
    int templateRadius = Math.max(MIN_RADIUS, convertedRadius);
    abstractTemplate.setRadius(templateRadius);

    // name of the template
    String name = options.has("name") ? options.get("name").getAsString() : "";
    abstractTemplate.setName(name);

    // layer to draw the template on, defaults to token layer for a player or the active layer for
    // the GM
    String layer = options.has("layer") ? options.get("layer").getAsString().toUpperCase() : "";
    Zone.Layer zoneLayer;
    if (layer.isEmpty()) {
      if (MapTool.getPlayer().isGM()) {
        zoneLayer = renderer.getActiveLayer();
      } else {
        zoneLayer = Zone.Layer.getDefaultPlayerLayer();
      }
    } else {
      zoneLayer = Zone.Layer.getByName(layer);
    }
    abstractTemplate.setLayer(zoneLayer);

    // x and y are for the where the position the template vertex in CELLS
    int x =
        options.has("x")
            ? DrawableUnit.convert(
                zone, options.get("x").getAsInt(), drawableUnit, DrawableUnit.CELLS)
            : 0;
    int y =
        options.has("y")
            ? DrawableUnit.convert(
                zone, options.get("y").getAsInt(), drawableUnit, DrawableUnit.CELLS)
            : 0;

    // pathx and path are used to set the pathVertex for Line and LineCell templates
    int pathX =
        options.has("pathx")
            ? DrawableUnit.convert(
                zone, options.get("pathx").getAsInt(), drawableUnit, DrawableUnit.CELLS)
            : 0;
    int pathY =
        options.has("pathy")
            ? DrawableUnit.convert(
                zone, options.get("pathy").getAsInt(), drawableUnit, DrawableUnit.CELLS)
            : 0;

    // prevent creating a pathVertex at the same position as the vertex
    if (x == x + pathX && y == y + pathY) {
      pathX = pathX + 1;
    }

    // initial placement of templates on the map, which may be adjusted by supplied options
    // note that not all shapes have a path vertex, but easier to set here along with the vertex
    ZonePoint zpVertex = zone.getGrid().convert(new CellPoint(x, y));
    ZonePoint zpPathVertex = zone.getGrid().convert(new CellPoint(x + pathX, y + pathY));

    // region set special properties for specific template types
    // due to template classes extending others, do the most extended first
    switch (abstractTemplate) {
      case BlastTemplate bt -> {
        // blast direction & placement around vertex
        String optionDirection =
            options.has("direction") ? options.get("direction").getAsString().toUpperCase() : "";
        if (!Objects.equals(optionDirection, "")) {
          try {
            BlastTemplate.Direction direction = BlastTemplate.Direction.valueOf(optionDirection);
            bt.setDirectionControlCellOffset(direction);
          } catch (IllegalArgumentException iae) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.template.unknownDirection", functionName, optionDirection));
          }
        } else {
          bt.setDirectionControlCellOffset(AbstractTemplate.Direction.SOUTH_EAST);
        }
      }

      case ConeTemplate ct -> {
        // direction & placement around vertex
        String optionDirection =
            options.has("direction") ? options.get("direction").getAsString().toUpperCase() : "";
        ConeTemplate.Direction direction;
        if (!Objects.equals(optionDirection, "")) {
          try {
            direction = ConeTemplate.Direction.valueOf(optionDirection);
          } catch (IllegalArgumentException iae) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.template.unknownDirection", functionName, optionDirection));
          }
        } else {
          direction =
              ConeTemplate.Direction.findDirection(
                  zpPathVertex.x, zpPathVertex.y, zpVertex.x, zpVertex.y);
        }
        ct.setDirection(direction);
      }
      case WallTemplate wt -> {
        // a path vertex is required by wt's super for rendering correctly
        zpPathVertex = new ZonePoint(0, 0);
        wt.setPathVertex(zpPathVertex);

        // walls are constructed from a path of individual cells
        wt.setPath(generateWallPath(functionName, zone, wt, wallPath, drawableUnit));
      }
      case LineTemplate lt -> lt.setPathVertex(zpPathVertex);
      case LineCellTemplate lct -> lct.setPathVertex(zpPathVertex);
      default -> {}
    }
    // endregion

    // region set other common template properties and set any pen options
    abstractTemplate.setVertex(zpVertex);
    boolean isEraser = options.has("eraser") && options.get("eraser").getAsBoolean();
    Pen colorPickerPen = MapTool.getFrame().getPen(isEraser);
    Pen templatePen = generatePenFromOptions(colorPickerPen, options);
    // endregion

    // draw the template
    MapTool.serverCommand().draw(zone.getId(), templatePen, abstractTemplate);
    // allow it to be undone
    zone.addDrawable(templatePen, abstractTemplate);
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();

    return abstractTemplate.getId();
  }

  /**
   * Apply pen options to a copy of another pen
   *
   * @param basePen the pen which to apply options onto
   * @param options a json object containing pen options
   * @return pen
   */
  private Pen generatePenFromOptions(Pen basePen, JsonObject options) {

    Pen pen = new Pen(basePen);

    // foreground (color or asset)
    if (options.has("foreground")) {
      String fg = options.get("foreground").getAsString();
      if (fg.equalsIgnoreCase("transparent") || fg.isEmpty()) {
        pen.setPaint(null);
      } else {
        pen.setPaint(FunctionUtil.getPaintFromString(fg));
      }
    }

    // background (color or asset)
    if (options.has("background")) {
      String bg = options.get("background").getAsString();
      if (bg.equalsIgnoreCase("transparent") || bg.isEmpty()) {
        pen.setBackgroundPaint(null);
      } else {
        pen.setBackgroundPaint(FunctionUtil.getPaintFromString(bg));
      }
    }

    // line thickness
    if (options.has("width")) {
      pen.setThickness(options.get("width").getAsFloat());
    }

    // square cap
    if (options.has("squarecap")) {
      pen.setSquareCap(options.get("squarecap").getAsBoolean());
    }

    // opacity
    if (options.has("opacity")) {
      float opacity = options.get("opacity").getAsFloat();
      if (opacity <= 1f) {
        pen.setOpacity(opacity);
      } else if (opacity <= 255f) {
        pen.setOpacity(opacity / 255f);
      }
    }

    // cut/eraser
    if (options.has("eraser")) {
      pen.setEraser(options.get("eraser").getAsBoolean());
    }

    return pen;
  }

  /**
   * From the given wall path notation, generate a list of cell points for the wall path.
   *
   * @param functionName this is used in the exception message
   * @param zone the map
   * @param wallTemplate the wall template
   * @param wallPath the wall path notation
   * @param drawableUnit the drawableUnits to use
   * @return a list of cell points for the wall path
   * @throws ParserException parser exception
   */
  private List<CellPoint> generateWallPath(
      String functionName,
      Zone zone,
      WallTemplate wallTemplate,
      String wallPath,
      DrawableUnit drawableUnit)
      throws ParserException {

    // a wall path *must* start off at the pathVertex (otherwise getPath() == null in
    // WallTemplate.toDto())
    int wallCellX = zone.getGrid().convert(wallTemplate.getPathVertex()).x;
    int wallCellY = zone.getGrid().convert(wallTemplate.getPathVertex()).y;

    List<CellPoint> wallPathList = new ArrayList<>();
    CellPoint wallCell = new CellPoint(wallCellX, wallCellY);
    wallPathList.add(wallCell);

    // e.g. for CELL based drawableUnits (& unitConversion == 1)
    //   y4x3 or s4e3 will create an 'L' shape to the south-east of the starting point
    //   x-3y-4 or w3n4 will create an 'L' shape to the north-west of the starting point
    //   z5 will create a wall stacked '5' high
    Pattern p = Pattern.compile("([xyznsew])(-?\\d+)", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(wallPath);

    while (m.find()) {
      String wallDirection = m.group(1).toLowerCase();
      int wallDistance =
          DrawableUnit.convert(
              zone, Integer.parseInt(m.group(2)), drawableUnit, DrawableUnit.CELLS);

      if (wallDistance != 0) {
        // loop through and construct path from individual cell points
        for (int i = 1; i <= Math.abs(wallDistance); i++) {
          switch (wallDirection) {
            case "x", "e":
              {
                wallCellX = wallDistance > 0 ? ++wallCellX : --wallCellX;
                break;
              }
            case "w":
              {
                wallCellX = wallDistance > 0 ? --wallCellX : ++wallCellX;
                break;
              }
            case "y", "s":
              {
                wallCellY = wallDistance > 0 ? ++wallCellY : --wallCellY;
                break;
              }
            case "n":
              {
                wallCellY = wallDistance > 0 ? --wallCellY : ++wallCellY;
                break;
              }
            case "z":
              {
                // just 'stack' the wall at the previous wallCellX or wallCellY
                break;
              }
            default:
              {
              }
          }
          wallCell = new CellPoint(wallCellX, wallCellY);
          wallPathList.add(wallCell);
        }
      }
    }

    if (wallPathList.isEmpty()) {
      throw new ParserException(
          I18N.getText("macro.function.template.unknownWallPath", functionName, wallPath));
    }

    // arbitrary wall length limit
    int wallPathLengthLimit = 9999;

    if (wallPathList.size() > wallPathLengthLimit) {
      throw new ParserException(
          I18N.getText(
              "macro.function.template.exceededWallLengthLimit",
              functionName,
              wallPath,
              wallPathLengthLimit));
    }

    return wallPathList;
  }

  /**
   * Get the template's radius.
   *
   * @param functionName this is used in the exception message
   * @param drawnElement the guid of the drawn element
   * @param drawableUnit the units
   * @return the radius in the units
   * @throws ParserException parser exception
   */
  private int getTemplateRadius(
      String functionName, Zone zone, DrawnElement drawnElement, DrawableUnit drawableUnit)
      throws ParserException {

    if (drawnElement.getDrawable() instanceof AbstractTemplate at) {
      return DrawableUnit.convert(zone, at.getRadius(), DrawableUnit.CELLS, drawableUnit);
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.template.unknownTemplate",
              functionName,
              drawnElement.getDrawable().getId()));
    }
  }

  /**
   * Get the template's vertex.
   *
   * @param functionName this is used in the exception message
   * @param drawnElement the guid of the drawn element
   * @return the zone co-ordinates of the template vertex
   * @throws ParserException parser exception
   */
  private ZonePoint getTemplateVertex(String functionName, DrawnElement drawnElement)
      throws ParserException {

    if (drawnElement.getDrawable() instanceof AbstractTemplate at) {
      return at.getVertex();
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.template.unknownTemplate",
              functionName,
              drawnElement.getDrawable().getId()));
    }
  }

  /**
   * Moves an {@link AbstractTemplate}'s vertex to position x, y on a map, and if applicable the
   * pathVertex to a related position.
   *
   * @param renderer where to draw
   * @param de the drawn element
   * @param x where to move to on the x-axis
   * @param y where to move to on the y-axis
   * @param drawableUnit whether x & y are given in CELLS, DISTANCE, or PIXELS
   */
  private void moveTemplate(
      ZoneRenderer renderer, DrawnElement de, int x, int y, DrawableUnit drawableUnit) {

    Zone zone = renderer.getZone();
    AbstractTemplate at = (AbstractTemplate) de.getDrawable();

    CellPoint cp;
    switch (drawableUnit) {
      case CELLS -> cp = new CellPoint(x, y);
      case DISTANCE ->
          cp =
              new CellPoint(
                  DrawableUnit.convert(zone, x, drawableUnit, DrawableUnit.CELLS),
                  DrawableUnit.convert(zone, y, drawableUnit, DrawableUnit.CELLS));
      case PIXELS -> cp = zone.getGrid().convert(new ZonePoint(x, y));
      case null, default -> {
        // as CELLS
        cp = new CellPoint(x, y);
      }
    }
    ZonePoint zp = zone.getGrid().convert(cp);

    if (at instanceof LineTemplate lt && !(at instanceof WallTemplate)) {
      // While WallTemplate extends LineTemplate, changing a WallTemplate's path vertex breaks
      // getBounds(), so don't change it for WallTemplates!
      ZonePoint pathVertexMove =
          new ZonePoint(
              zp.x + lt.getPathVertex().x - lt.getVertex().x,
              zp.y + lt.getPathVertex().y - lt.getVertex().y);
      lt.setPathVertex(pathVertexMove);
    }
    if (at instanceof LineCellTemplate lct) {
      ZonePoint pathVertexMove =
          new ZonePoint(
              zp.x + lct.getPathVertex().x - lct.getVertex().x,
              zp.y + lct.getPathVertex().y - lct.getVertex().y);
      lct.setPathVertex(pathVertexMove);
    }
    at.setVertex(zp);
    de.setDrawable(at);

    MapTool.serverCommand().updateDrawing(zone.getId(), de.getPen(), de);
    zone.updateDrawable(de, de.getPen());
    renderer.repaint();
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();
  }
}
