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

// @formatter:off

import com.google.common.primitives.Floats;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;

// @formatter:on
public class ShapeFunctions extends AbstractFunction {
  public static final Map<String, ShapeDrawable> CACHED_SHAPES;
  private static final ShapeFunctions instance = new ShapeFunctions();
  private static final AWTPathProducer AWT_PATH_PRODUCER = new AWTPathProducer();
  private static final PathParser PATH_PARSER = new PathParser();
  private static final String UNKNOWN_LAYER = "macro.function.tokenProperty.unknownLayer";
  private static final String OBJECT_NOT_FOUND = "macro.function.general.objectNotFound";
  private static final String INDEX_OUT_OF_BOUNDS =
      "macro.function.general.indexOutOfBoundsVerbose";
  private static final String UNABLE_TO_PARSE = "macro.function.general.unableToParse";
  private static final String UNSUPPORTED_OPERATION = "macro.function.general.unsupportedOperation";
  private static final String WRONG_NUMBER_OF_ARGUMENTS_FOR_OPERATION =
      "macro.function.general.wrongNumberArgumentsForOperation";

  static {
    PATH_PARSER.setPathHandler(AWT_PATH_PRODUCER);
    CACHED_SHAPES = new TreeMap<>(String::compareToIgnoreCase);
  }

  private ShapeFunctions() {
    super(
        0,
        UNLIMITED_PARAMETERS,
        "shape.areaAdd",
        "shape.areaExclusiveOr",
        "shape.areaIntersect",
        "shape.areaSubtract",
        "shape.clearAll",
        "shape.combinePaths",
        "shape.copy",
        "shape.create",
        "shape.delete",
        "shape.draw",
        "shape.getProperties",
        "shape.list",
        "shape.transform");
  }

  public static ShapeFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("shape.areaAdd")) {
      return areaBoolean("add", parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.areaExclusiveOr")) {
      return areaBoolean("exclusiveOr", parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.areaIntersect")) {
      return areaBoolean("intersect", parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.areaSubtract")) {
      return areaBoolean("subtract", parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.clearAll")) {
      CACHED_SHAPES.clear();
      return true;
    } else if (functionName.equalsIgnoreCase("shape.combinePaths")) {
      return combinePaths(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.copy")) {
      return copyShape(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.create")) {
      return createShape(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.delete")) {
      return deleteShape(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.draw")) {
      return drawShape(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.getProperties")) {
      return getProperties(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.list")) {
      return shapeList(parser, resolver, functionName, parameters);
    } else if (functionName.equalsIgnoreCase("shape.transform")) {
      return transformShape(parser, resolver, functionName, parameters);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
  }

  /**
   * For use in planned Halo functions
   *
   * @param shapeName assigned key in cache
   * @return ShapeDrawable from cache
   */
  public static ShapeDrawable getDrawableFromCache(String shapeName) {
    if (CACHED_SHAPES.containsKey(shapeName)) {
      return CACHED_SHAPES.get(shapeName);
    }
    return null;
  }

  private Object areaBoolean(
      String operation,
      Parser parser,
      VariableResolver resolver,
      String functionName,
      List<Object> parameters)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, parameters, 5, -1);
    /*    name, layer, anti-aliasing, shape names...    */
    Object[] leadParams = getLeadParameters(functionName, parameters);
    GUID guid = (GUID) leadParams[0];
    String name = leadParams[1].toString();
    Zone.Layer layer = (Zone.Layer) leadParams[2];
    boolean aa = (boolean) leadParams[3];

    List<Area> areas = new ArrayList<>(parameters.size() - 3);
    for (int i = 3; i < parameters.size(); i++) {
      String shapeName = FunctionUtil.paramAsString(functionName, parameters, i, false);
      if (CACHED_SHAPES.containsKey(shapeName)) {
        areas.add(new Area(CACHED_SHAPES.get(shapeName).getShape()));
      } else {
        throw new ParserException(I18N.getText(OBJECT_NOT_FOUND, functionName, shapeName));
      }
    }

    Area area = areas.getFirst();
    for (int i = 1; i < areas.size(); i++) {
      switch (operation) {
        case "add" -> area.add(areas.get(i));
        case "subtract" -> area.subtract(areas.get(i));
        case "intersect" -> area.intersect(areas.get(i));
        case "exclusiveOr" -> area.exclusiveOr(areas.get(i));
        default -> throw new ParserException("#");
      }
    }

    ShapeDrawable sd = new ShapeDrawable(guid, area, aa);
    sd.setName(name);
    if (layer != null) {
      sd.setLayer(layer);
    }
    CACHED_SHAPES.put(name, sd);
    return name;
  }

  private Object combinePaths(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, parameters, 5, -1);
    /*    name, layer, antialiasing, connect, shape names...    */
    Object[] leadParams = getLeadParameters(functionName, parameters);
    GUID guid = (GUID) leadParams[0];
    String name = leadParams[1].toString();
    Zone.Layer layer = (Zone.Layer) leadParams[2];
    boolean aa = (boolean) leadParams[3];
    boolean connect = FunctionUtil.paramAsBoolean(functionName, parameters, 3, true);

    List<Shape> shapes = new ArrayList<>(parameters.size() - 4);
    for (int i = 4; i < parameters.size(); i++) {
      String shapeName = FunctionUtil.paramAsString(functionName, parameters, i, false);
      if (CACHED_SHAPES.containsKey(shapeName)) {
        shapes.add(CACHED_SHAPES.get(shapeName).getShape());
      } else {
        throw new ParserException(I18N.getText(OBJECT_NOT_FOUND, functionName, shapeName));
      }
    }

    Path2D path2D = new Path2D.Float(shapes.getFirst());
    for (int i = 1; i < shapes.size(); i++) {
      path2D.append(shapes.get(i), connect);
    }

    ShapeDrawable sd = new ShapeDrawable(guid, path2D, aa);
    sd.setName(name);
    if (layer != null) {
      sd.setLayer(layer);
    }
    CACHED_SHAPES.put(name, sd);
    return name;
  }

  private Object copyShape(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    /* copy name, shape name */

    FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
    String shapeName = FunctionUtil.paramAsString(functionName, parameters, 1, false);
    if (!CACHED_SHAPES.containsKey(shapeName)) {
      throw new ParserException(I18N.getText(OBJECT_NOT_FOUND, functionName, shapeName));
    }
    String copyName = FunctionUtil.paramAsString(functionName, parameters, 0, false);
    ShapeDrawable shapeDrawable = new ShapeDrawable(CACHED_SHAPES.get(shapeName));
    shapeDrawable.setId(new GUID());
    if (!copyName.equalsIgnoreCase("")) {
      shapeDrawable.setName(copyName);
    } else {
      shapeDrawable.setName(shapeDrawable.getId().toString());
    }
    CACHED_SHAPES.put(shapeDrawable.getName(), shapeDrawable);
    return shapeDrawable.getName();
  }

  private Object createShape(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, parameters, 5, 8);
    int numParam = parameters.size();
    int transformIndex = -1;

    if (numParam == 8) {
      transformIndex = 6;
    } else if (numParam == 7) {
      /* one arg is a delimiter */
      if (parameters.get(5) instanceof JsonElement) {
        transformIndex = 5;
      } else if (parameters.get(6) instanceof JsonElement) {
        transformIndex = 6;
      } else if (((String) parameters.get(5)).equalsIgnoreCase("json")) {
        transformIndex = 6;
      } else if (((String) parameters.get(6)).equalsIgnoreCase("json")) {
        transformIndex = 5;
      } else if (((String) parameters.get(5)).length() > 6) { // too long for a delimiter
        transformIndex = 5;
      } else if (((String) parameters.get(6)).length() > 6) { // too long for a delimiter
        transformIndex = 6;
      }
    } else if (numParam == 6) {
      /* arg index 5 MAY be a delimiter */
      if (parameters.get(5) instanceof JsonElement) {
        /* it is not */
        transformIndex = 5;
      } else if (((String) parameters.get(5)).length() > 6) {
        /* seems too long for a delimiter */
        transformIndex = 5;
      }
    }
    return createShape(parser, resolver, functionName, parameters, transformIndex);
  }

  private Object createShape(
      Parser parser,
      VariableResolver resolver,
      String functionName,
      List<Object> parameters,
      int transformIndex)
      throws ParserException {
    /* 0. name, 1. layer, 2. anti-aliasing, 3. type, 4. shape arguments, [5. delim], [6. transforms], [7. delim] */

    Object[] leadParams = getLeadParameters(functionName, parameters);
    GUID guid = (GUID) leadParams[0];
    String name = leadParams[1].toString();
    Zone.Layer layer = (Zone.Layer) leadParams[2];
    boolean aa = (boolean) leadParams[3];
    String shapeType = FunctionUtil.paramAsString(functionName, parameters, 3, true).toLowerCase();

    Shape shape;
    String delimiter =
        (transformIndex == -1 && parameters.size() == 6) || transformIndex == 6
            ? FunctionUtil.paramAsString(functionName, parameters, 5, false)
            : ";";
    if (shapeType.equalsIgnoreCase("svgpath")) {
      shape = svgPath(functionName, FunctionUtil.paramAsString(functionName, parameters, 4, true));
    } else if (shapeType.equalsIgnoreCase("path")) {
      JsonArray pathArray = new JsonArray();
      if (delimiter.equalsIgnoreCase("json")) {
        pathArray = FunctionUtil.paramConvertedToJson(functionName, parameters, 4).getAsJsonArray();
      } else {
        String pathString = FunctionUtil.paramAsString(functionName, parameters, 4, true);
        pathString = pathString.replaceAll("\\s", "");
        if (pathString.contains("[")) {
          pathArray = FunctionUtil.paramAsJsonArray(functionName, parameters, 4);
        } else {
          for (String s : pathString.split(delimiter)) {
            pathArray.add(s);
          }
        }
      }
      shape = path(functionName, pathArray);
    } else {
      JsonObject shapeParams =
          FunctionUtil.jsonWithLowerCaseKeys(
              FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                  functionName, parameters, 4, delimiter));
      shape =
          switch (shapeType) {
            case "arc" -> arc(functionName, shapeParams);
            case "cubiccurve" -> cubicCurve(functionName, shapeParams);
            case "ellipse" -> ellipse(functionName, shapeParams);
            case "line" -> line(functionName, shapeParams);
            case "polygon" -> polygon(functionName, shapeParams);
            case "quadcurve" -> quadCurve(functionName, shapeParams);
            case "rectangle" -> rectangle(functionName, shapeParams);
            case "roundrectangle" -> roundRectangle(functionName, shapeParams);
            default ->
                throw new ParserException(
                    I18N.getText(UNSUPPORTED_OPERATION, functionName, 3, shapeType));
          };
    }
    if (transformIndex != -1) {
      String delimiter2 = ",";
      if (transformIndex == parameters.size() - 2) {
        delimiter2 =
            FunctionUtil.paramAsString(functionName, parameters, transformIndex + 1, false);
      }
      shape = transform(shape, functionName, parameters, transformIndex, delimiter2);
    }
    ShapeDrawable sd = new ShapeDrawable(guid, shape, aa);
    sd.setName(name);
    if (layer != null) {
      sd.setLayer(layer);
    }
    CACHED_SHAPES.put(name, sd);

    return name;
  }

  private boolean deleteShape(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    String name = FunctionUtil.paramAsString(functionName, parameters, 0, false);
    if (CACHED_SHAPES.containsKey(name)) {
      CACHED_SHAPES.remove(name);
      return true;
    }
    return false;
  }

  private Object drawShape(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    /* name, map name, pen, delimiter */
    FunctionUtil.experimentalWarning(parser, resolver, functionName);
    FunctionUtil.checkNumberParam(functionName, parameters, 2, 4);
    String shapeName = FunctionUtil.paramAsString(functionName, parameters, 0, false);
    String mapName = FunctionUtil.paramAsString(functionName, parameters, 1, true);
    if (!CACHED_SHAPES.containsKey(shapeName)) {
      throw new ParserException(I18N.getText(OBJECT_NOT_FOUND, functionName, shapeName));
    }
    ShapeDrawable shapeDrawable = CACHED_SHAPES.get(shapeName);
    Rectangle bounds = shapeDrawable.getBounds();

    /* Sanity checks */
    if ((bounds.width == 0 && bounds.height == 0)
        || bounds.width > 50000
        || bounds.height > 50000) {
      return false;
    }

    Pen pen = new Pen(Pen.DEFAULT);
    if (parameters.size() > 2) {
      String delimiter = ";";
      if (parameters.size() > 3) {
        delimiter = FunctionUtil.paramAsString(functionName, parameters, 3, false);
      }
      JsonObject penObject =
          FunctionUtil.jsonWithLowerCaseKeys(
              FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                  functionName, parameters, 2, delimiter));

      if (penObject.keySet().contains("foreground")) {
        String fg = penObject.get("foreground").getAsString();
        if (fg.equalsIgnoreCase("transparent") || fg.equalsIgnoreCase("")) {
          pen.setForegroundMode(Pen.MODE_TRANSPARENT);
        } else {
          pen.setForegroundMode(Pen.MODE_SOLID);
          pen.setPaint(FunctionUtil.getPaintFromString(fg));
        }
      }
      if (penObject.keySet().contains("background")) {
        String bg = penObject.get("background").getAsString();
        if (bg.equalsIgnoreCase("transparent") || bg.equalsIgnoreCase("")) {
          pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
        } else {
          pen.setBackgroundMode(Pen.MODE_SOLID);
          pen.setBackgroundPaint(FunctionUtil.getPaintFromString(bg));
        }
      }
      if (penObject.keySet().contains("width")) {
        pen.setThickness(penObject.get("width").getAsFloat());
      }
      if (penObject.keySet().contains("squarecap")) {
        pen.setSquareCap(penObject.get("squarecap").getAsBoolean());
      }
      if (penObject.keySet().contains("opacity")) {
        float opacity = penObject.get("opacity").getAsFloat();
        if (opacity <= 1f) {
          pen.setOpacity(opacity);
        } else if (opacity <= 255f) {
          pen.setOpacity(opacity / 255f);
        }
      }
      if (penObject.keySet().contains("eraser")) {
        pen.setEraser(penObject.get("eraser").getAsBoolean());
      }
    }
    DrawnElement drawnElement = new DrawnElement(CACHED_SHAPES.get(shapeName), pen);
    drawnElement.setPen(pen);

    MapTool.serverCommand()
        .draw(
            FunctionUtil.getZoneRenderer(functionName, mapName).getZone().getId(),
            drawnElement.getPen(),
            drawnElement.getDrawable());
    MapTool.getFrame().refresh();

    return drawnElement.getDrawable().getId();
  }

  private Object getProperties(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    /* name, delimiter */
    String name = FunctionUtil.paramAsString(functionName, parameters, 0, false);
    String delimiter =
        parameters.size() < 2
            ? ";"
            : FunctionUtil.paramAsString(functionName, parameters, 1, false);
    if (!CACHED_SHAPES.containsKey(name)) {
      return false;
    }
    ShapeDrawable sd = CACHED_SHAPES.get(name);
    Shape shape = sd.getShape();
    List<String> segments = new ArrayList<>();
    PathIterator pi = shape.getPathIterator(null);
    double[] coords = new double[7];
    while (!pi.isDone()) {
      int seg = pi.currentSegment(coords);
      segments.add(
          String.format(
              "%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f",
              seg, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6]));
      pi.next();
    }
    StringBuilder stringBuilder = new StringBuilder(sd.toString());
    stringBuilder.append("segments=").append(String.join(",", segments)).append(";");

    if (delimiter.equalsIgnoreCase("json")) {
      JsonObject jsonObject =
          JSONMacroFunctions.getInstance()
              .getJsonObjectFunctions()
              .fromStrProp(stringBuilder.toString(), ";");
      jsonObject.add(
          "segments",
          JSONMacroFunctions.getInstance()
              .getJsonArrayFunctions()
              .fromStringList(String.join("##", segments), "##"));
      return jsonObject;
    } else {
      stringBuilder.append("segments=\"").append(String.join("\",\"", segments)).append("\";");
      return stringBuilder.toString();
    }
  }

  private Object shapeList(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    String delim =
        parameters.isEmpty() ? "," : FunctionUtil.paramAsString(functionName, parameters, 0, false);
    return FunctionUtil.delimitedResult(delim, new ArrayList<>(CACHED_SHAPES.keySet()));
  }

  private Object transformShape(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, parameters, 4, 6);
    /*  name, layer, antialiasing, shape name to transform, transforms, delimiter */
    Object[] leadParams = getLeadParameters(functionName, parameters);
    GUID guid = (GUID) leadParams[0];
    String name = leadParams[1].toString();
    Zone.Layer layer = (Zone.Layer) leadParams[2];
    boolean aa = (boolean) leadParams[3];

    String delimiter = ",";
    if (parameters.size() > 5) {
      delimiter = FunctionUtil.paramAsString(functionName, parameters, 5, false);
    }
    String shapeName = FunctionUtil.paramAsString(functionName, parameters, 3, false);
    if (!CACHED_SHAPES.containsKey(shapeName)) {
      throw new ParserException(I18N.getText(OBJECT_NOT_FOUND, functionName, shapeName));
    }
    Shape shape =
        transform(CACHED_SHAPES.get(shapeName).getShape(), functionName, parameters, 4, delimiter);
    ShapeDrawable sd = new ShapeDrawable(guid, shape, aa);
    sd.setName(name);
    if (layer != null) {
      sd.setLayer(layer);
    }
    CACHED_SHAPES.put(name, sd);
    return name;
  }

  private Object[] getLeadParameters(String functionName, List<Object> parameters)
      throws ParserException {

    Object[] results = new Object[4];
    results[0] = new GUID();

    if (parameters.getFirst().toString().isEmpty()) {
      results[1] = results[0].toString();
    } else {
      results[1] = FunctionUtil.paramAsString(functionName, parameters, 0, false);
    }
    String layerName = "";
    if (!parameters.get(1).toString().isEmpty()) {
      layerName = FunctionUtil.paramAsString(functionName, parameters, 1, false);
    }
    Zone.Layer layer = null;
    if (!layerName.isEmpty()) {
      try {
        layer = Zone.Layer.valueOf(layerName.toUpperCase());
      } catch (IllegalArgumentException iae) {
        throw new ParserException(I18N.getText(UNKNOWN_LAYER, layerName, functionName));
      }
    }

    if (!MapTool.getClient().getPlayer().isGM()) {
      results[2] = Zone.Layer.TOKEN;
    } else {
      results[2] = layer;
    }
    boolean aa = true;

    if (!parameters.get(2).toString().isEmpty()) {
      aa = FunctionUtil.paramAsString(functionName, parameters, 2, true).equals("1");
    }
    results[3] = aa;
    return results;
  }

  private Shape arc(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(
        shapeParameters,
        Set.of("x", "y", "w", "h", "start", "extent", "type"),
        4,
        functionName,
        "");
    return new Arc2D.Float(
        shapeParameters.get("x").getAsFloat(),
        shapeParameters.get("y").getAsFloat(),
        shapeParameters.get("w").getAsFloat(),
        shapeParameters.get("h").getAsFloat(),
        shapeParameters.get("start").getAsFloat(),
        shapeParameters.get("extent").getAsFloat(),
        switch (shapeParameters.get("type").getAsString().toLowerCase()) {
          case "chord", "1" -> Arc2D.CHORD;
          case "pie", "2" -> Arc2D.PIE;
          default -> Arc2D.OPEN;
        });
  }

  private Shape cubicCurve(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(
        shapeParameters,
        Set.of("x1", "y1", "ctrlx1", "ctrly1", "ctrlx2", "ctrly2", "x2", "y2"),
        4,
        functionName,
        "");

    return new CubicCurve2D.Float(
        shapeParameters.get("x1").getAsFloat(),
        shapeParameters.get("y1").getAsFloat(),
        shapeParameters.get("ctrlx1").getAsFloat(),
        shapeParameters.get("ctrly1").getAsFloat(),
        shapeParameters.get("ctrlx2").getAsFloat(),
        shapeParameters.get("ctrly2").getAsFloat(),
        shapeParameters.get("x2").getAsFloat(),
        shapeParameters.get("y2").getAsFloat());
  }

  private Shape ellipse(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(shapeParameters, Set.of("x", "y", "w", "h"), 4, functionName, "");

    return new Ellipse2D.Float(
        shapeParameters.get("x").getAsFloat(),
        shapeParameters.get("y").getAsFloat(),
        shapeParameters.get("w").getAsFloat(),
        shapeParameters.get("h").getAsFloat());
  }

  private Shape line(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(
        shapeParameters, Set.of("x1", "y1", "x2", "y2"), 4, functionName, "");

    return new Line2D.Float(
        shapeParameters.get("x1").getAsFloat(),
        shapeParameters.get("y1").getAsFloat(),
        shapeParameters.get("x2").getAsFloat(),
        shapeParameters.get("y2").getAsFloat());
  }

  private Shape path(String functionName, JsonArray pathArray) throws ParserException {
    ExtendedGeneralPath path = new ExtendedGeneralPath();
    for (JsonElement jsonElement : pathArray) {
      JsonArray segment = null;
      try {
        segment = jsonElement.getAsJsonArray();
        switch (segment.get(0).getAsString()) {
          case "wind", "w", "-1" -> path.setWindingRule(segment.get(1).getAsInt());
          case "close", "z", "4" -> path.closePath();
          case "moveto", "moveTo", "move", "m", "0" ->
              path.moveTo(segment.get(1).getAsFloat(), segment.get(2).getAsFloat());
          case "line", "lineto", "lineTo", "l", "1" ->
              path.lineTo(segment.get(1).getAsFloat(), segment.get(2).getAsFloat());
          case "quad", "quadto", "quadTo", "q", "2" ->
              path.quadTo(
                  segment.get(1).getAsFloat(),
                  segment.get(2).getAsFloat(),
                  segment.get(3).getAsFloat(),
                  segment.get(4).getAsFloat());
          case "cubic", "cubicto", "cubicTo", "c", "3" ->
              path.curveTo(
                  segment.get(1).getAsFloat(),
                  segment.get(2).getAsFloat(),
                  segment.get(3).getAsFloat(),
                  segment.get(4).getAsFloat(),
                  segment.get(5).getAsFloat(),
                  segment.get(6).getAsFloat());
          case "arc", "arcto", "arcTo", "a", "4321" ->
              path.arcTo(
                  segment.get(1).getAsFloat(),
                  segment.get(2).getAsFloat(),
                  segment.get(3).getAsFloat(),
                  segment.get(4).getAsBoolean(),
                  segment.get(5).getAsBoolean(),
                  segment.get(6).getAsFloat(),
                  segment.get(7).getAsFloat());
          default ->
              throw new ParserException(
                  I18N.getText("ILLEGAL_ARGUMENT", functionName, 5, segment.getAsString()));
        }
      } catch (IndexOutOfBoundsException iobe) {
        assert segment != null;
        throw new ParserException(
            I18N.getText(INDEX_OUT_OF_BOUNDS, functionName, 5, segment.getAsString()));
      }
    }
    return path;
  }

  private Shape polygon(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(
        shapeParameters, Set.of("xpoints", "ypoints", "numpoints"), 4, functionName, "");
    float[] xpts =
        Floats.toArray(
            shapeParameters.get("xpoints").getAsJsonArray().asList().stream()
                .map(JsonElement::getAsDouble)
                .toList());
    float[] ypts =
        Floats.toArray(
            shapeParameters.get("ypoints").getAsJsonArray().asList().stream()
                .map(JsonElement::getAsDouble)
                .toList());
    return new Polygon2D(xpts, ypts, shapeParameters.get("numpoints").getAsInt());
  }

  private Shape quadCurve(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(
        shapeParameters, Set.of("x1", "y1", "ctrlx", "ctrly", "x2", "y2"), 4, functionName, "");
    return new QuadCurve2D.Float(
        shapeParameters.get("x1").getAsFloat(),
        shapeParameters.get("y1").getAsFloat(),
        shapeParameters.get("ctrlx").getAsFloat(),
        shapeParameters.get("ctrly").getAsFloat(),
        shapeParameters.get("x2").getAsFloat(),
        shapeParameters.get("y2").getAsFloat());
  }

  private Shape rectangle(String functionName, JsonObject shapeParameters) throws ParserException {
    FunctionUtil.validateKeyNames(shapeParameters, Set.of("x", "y", "w", "h"), 4, functionName, "");

    return new Rectangle2D.Float(
        shapeParameters.get("x").getAsFloat(),
        shapeParameters.get("y").getAsFloat(),
        shapeParameters.get("w").getAsFloat(),
        shapeParameters.get("h").getAsFloat());
  }

  private Shape roundRectangle(String functionName, JsonObject shapeParameters)
      throws ParserException {
    FunctionUtil.validateKeyNames(
        shapeParameters, Set.of("x", "y", "w", "h", "arcw", "arch"), 4, functionName, "");

    return new RoundRectangle2D.Float(
        shapeParameters.get("x").getAsFloat(),
        shapeParameters.get("y").getAsFloat(),
        shapeParameters.get("w").getAsFloat(),
        shapeParameters.get("h").getAsFloat(),
        shapeParameters.get("arcw").getAsFloat(),
        shapeParameters.get("arch").getAsFloat());
  }

  private Shape svgPath(String functionName, String pathString) throws ParserException {
    try {
      PATH_PARSER.parse(pathString);
      final Path2D path = new Path2D.Double(AWT_PATH_PRODUCER.getShape());
      path.trimToSize();
      return path;
    } catch (ParseException pe) {
      throw new ParserException(I18N.getText(UNABLE_TO_PARSE, functionName, 5));
    }
  }

  private Shape transform(
      Shape shape,
      String functionName,
      List<Object> parameters,
      int transformIndex,
      String delimiter)
      throws ParserException {

    JsonArray transforms = new JsonArray();
    String transformsString =
        FunctionUtil.paramAsString(functionName, parameters, transformIndex, true);

    if (parameters.get(transformIndex) instanceof JsonArray
        || delimiter.equalsIgnoreCase("json")
        || transformsString.contains("[")) {
      transforms = FunctionUtil.paramAsJsonArray(functionName, parameters, transformIndex);
    } else {
      transformsString = transformsString.replaceAll("\\s", "");
      for (String s : transformsString.split(delimiter)) {
        transforms.add(s);
      }
    }

    BiFunction<JsonArray, Integer, Integer> nextStringIndex =
        ((jsonElements, integer) -> {
          int i;
          for (i = integer; i < jsonElements.size(); i++) {
            try {
              jsonElements.get(integer).getAsDouble();
            } catch (NumberFormatException nfe) {
              break;
            }
          }
          return i;
        });

    int i = 0;
    while (i < transforms.size()) {
      String type = transforms.get(i).getAsString().toLowerCase();
      List<Double> args = new ArrayList<>();
      i++;
      while (i < nextStringIndex.apply(transforms, i)) {
        args.add(transforms.get(i).getAsDouble());
        i++;
      }
      AffineTransform at = new AffineTransform();
      try {
        switch (type) {
          case "matrix" ->
              at.setTransform(
                  args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5));
          case "rotate" -> {
            if (args.size() == 1) {
              at.setToRotation(args.getFirst());
            } else {
              at.setToRotation(args.get(0), args.get(1), args.get(2));
            }
          }
          case "scale" -> at.setToScale(args.get(0), args.get(1));
          case "shear" -> at.setToShear(args.get(0), args.get(1));
          case "translate" -> at.setToTranslation(args.get(0), args.get(1));
          default ->
              throw new ParserException(I18N.getText(UNSUPPORTED_OPERATION, functionName, 5, type));
        }
      } catch (IndexOutOfBoundsException oob) {
        throw new ParserException(
            I18N.getText(
                WRONG_NUMBER_OF_ARGUMENTS_FOR_OPERATION,
                functionName,
                5,
                type,
                switch (type) {
                  case "matrix" -> 6;
                  case "rotate" -> "1 or 3";
                  case "scale", "shear", "translate" -> 2;
                  default ->
                      throw new ParserException(
                          I18N.getText(UNSUPPORTED_OPERATION, functionName, 5, type));
                }));
      }
      shape = at.createTransformedShape(shape);
    }
    return shape;
  }
}
