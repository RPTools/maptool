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
import com.google.gson.JsonObject;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.drawing.DrawingPointerTool;
import net.rptools.maptool.client.tool.drawing.TemplatePointerTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class DrawingFunctions extends AbstractFunction {

  public DrawingFunctions(int minParameters, int maxParameters, String... aliases) {
    super(minParameters, maxParameters, aliases);
  }

  /**
   * DrawableUnit enumeration, including an <code>index</code> property based on the "units" boolean
   * argument found in certain MTScript functions, but as a tri-state with <code>CELLS</code>,
   * <code>DISTANCE</code>, and <code>PIXELS</code> options. For template radii the units used
   * typically will be <code>DISTANCE</code>.
   */
  protected enum DrawableUnit {
    CELLS(0),
    DISTANCE(1),
    PIXELS(2);

    DrawableUnit(int index) {
      this.index = index;
    }

    private final int index;

    public static DrawableUnit valueOfIndex(int index) {
      for (DrawableUnit drawableUnit : values()) {
        if (drawableUnit.index == index) {
          return drawableUnit;
        }
      }
      return null;
    }

    public static int convert(Zone zone, int value, DrawableUnit fromUnit, DrawableUnit toUnit) {
      double fromConversion = getConversionFactor(zone, fromUnit);
      double toConversion = getConversionFactor(zone, toUnit);
      return (int) (value * toConversion / fromConversion);
    }

    public static int convertToCells(Zone zone, int value, DrawableUnit fromUnit) {
      return convert(zone, value, fromUnit, DrawableUnit.CELLS);
    }

    public static double getConversionFactor(Zone zone, DrawableUnit drawableUnit) {
      return switch (drawableUnit) {
        case CELLS -> 1d;
        case DISTANCE -> zone.getUnitsPerCell();
        case PIXELS -> zone.getGrid().getSize();
      };
    }

    public static int getIndex(DrawableUnit drawableUnit) {
      return drawableUnit.index;
    }

    public static int getCellsIndex() {
      return DrawableUnit.CELLS.index;
    }

    public static int getDistanceIndex() {
      return DrawableUnit.DISTANCE.index;
    }

    public static int getPixelsIndex() {
      return DrawableUnit.PIXELS.index;
    }
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    // This class should never be called.
    return null;
  }

  /**
   * Moves a specified drawing on a specified map to the top of the drawing stack.
   *
   * @param map the zone that should contain the drawing
   * @param guid the id of the drawing.
   */
  protected void bringToFront(Zone map, GUID guid) {
    List<DrawnElement> drawableList = map.getAllDrawnElements();
    for (DrawnElement de : drawableList) {
      if (de.getDrawable().getId().equals(guid)) {
        map.removeDrawable(de.getDrawable().getId());
        MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
        map.addDrawable(new DrawnElement(de.getDrawable(), de.getPen()));
        MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
      }
    }
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();
  }

  protected Layer changeLayer(Zone map, Layer layer, GUID guid) {
    List<DrawnElement> drawableList = map.getAllDrawnElements();
    for (DrawnElement de : drawableList) {
      if (de.getDrawable().getLayer() != layer && de.getDrawable().getId().equals(guid)) {
        map.removeDrawable(de.getDrawable().getId());
        MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
        de.getDrawable().setLayer(layer);
        map.addDrawable(de);
        MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
      }
    }
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();
    return layer;
  }

  /**
   * Checks whether or not the function is trusted
   *
   * @param functionName Name of the macro function
   * @throws ParserException Returns trust error message and function name
   */
  protected void checkTrusted(String functionName) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
  }

  /**
   * Looks for a drawing on a specific map that matches a specific id. Throws a <code>
   * ParserException</code> if the drawing is not found.
   *
   * @param functionName this is used in the exception message
   * @param map the zone that should contain the drawing
   * @param guid the id of the drawing.
   * @return the {@link Drawable} requested.
   * @throws ParserException if the drawing is not found.
   */
  protected Drawable getDrawable(String functionName, Zone map, GUID guid) throws ParserException {
    return getDrawnElement(functionName, map, guid).getDrawable();
  }

  protected DrawnElement getDrawnElement(String functionName, Zone map, GUID guid)
      throws ParserException {
    DrawnElement drawnElement = findDrawnElement(map.getAllDrawnElements(), guid);
    if (drawnElement != null) return drawnElement;
    throw new ParserException(
        I18N.getText("macro.function.drawable.unknownDrawable", functionName, guid.toString()));
  }

  /**
   * Try and find a drawn element, using its {@link Drawable}'s {@link GUID} or case-insensitive
   * <code>name</code>.
   *
   * <p>Warning: If name is used, and there are multiple drawables with the same name on the same
   * map, only the first one encountered will be returned
   *
   * @param functionName this is used in the exception message
   * @param zone the map to search for the template
   * @param drawnElementRef either the name, or the id
   * @return the template's drawn element, or null if not found
   */
  public static DrawnElement findDrawnElement(
      String functionName, Zone zone, String drawnElementRef) throws ParserException {

    DrawnElement drawnElement = null;

    GUID guid = null;
    try {
      guid = GUID.valueOf(drawnElementRef);
    } catch (Exception e) {
      // wasn't a GUID after all, OK to ignore
    }

    for (DrawnElement de : zone.getAllDrawnElements()) {
      AbstractDrawing d = (AbstractDrawing) de.getDrawable();
      if (d.getId().equals(guid)) {
        drawnElement = de;
        break;
      } else if (drawnElementRef.equalsIgnoreCase(d.getName())) {
        drawnElement = de;
        break;
      }
    }

    if (drawnElement != null) return drawnElement;
    throw new ParserException(
        I18N.getText("macro.function.drawable.unknownDrawable", functionName, drawnElementRef));
  }

  private DrawnElement findDrawnElement(List<DrawnElement> drawableList, GUID guid) {
    for (DrawnElement de : drawableList) {
      if (de.getDrawable().getId().equals(guid)) {
        return de;
      }
      if (de.getDrawable() instanceof DrawablesGroup) {
        DrawnElement result =
            findDrawnElement(((DrawablesGroup) de.getDrawable()).getDrawableList(), guid);
        if (result != null) return result;
      }
    }
    return null;
  }

  /**
   * Validates the float
   *
   * @param functionName String Name of the calling function. Used for error messages.
   * @param f String value of percentage
   * @return float
   * @throws ParserException thrown on invalid float
   */
  protected float getFloatPercent(String functionName, String f) throws ParserException {
    try {
      float per = Float.parseFloat(f);
      while (per > 1) per = per / 100;
      return per;
    } catch (Exception e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentTypeN", functionName, f));
    }
  }

  /**
   * Validates the float
   *
   * @param functionName String Name of the calling function. Used for error messages.
   * @param f String value of float
   * @return float
   * @throws ParserException thrown on invalid float
   */
  protected float getFloat(String functionName, String f) throws ParserException {
    try {
      float per = Float.parseFloat(f);
      return per;
    } catch (Exception e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentTypeN", functionName, f));
    }
  }

  /**
   * Validates the GUID
   *
   * @param functionName String Name of the calling function. Used for error messages.
   * @param id String value of GUID
   * @return GUID
   * @throws ParserException thrown on invalid GUID
   */
  protected GUID getGUID(String functionName, String id) throws ParserException {
    try {
      return GUID.valueOf(id);
    } catch (Exception e) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentKeyTypeG", functionName, id));
    }
  }

  /**
   * Take a string and return a layer
   *
   * @param layer String naming the layer
   * @return Layer
   */
  protected Layer getLayer(String layer) {
    try {
      return Layer.valueOf(layer.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Layer.getDefaultPlayerLayer();
    }
  }

  /**
   * Looks for a drawing on a specific map that matches a specific id and returns its pen. Throws a
   * <code>ParserException</code> if the drawing is not found.
   *
   * @param functionName this is used in the exception message
   * @param map the zone that should contain the drawing
   * @param guid the id of the drawing.
   * @return Pen Pen of the drawing.
   * @throws ParserException if the drawing is not found.
   */
  protected Pen getPen(String functionName, Zone map, GUID guid) throws ParserException {
    return getDrawnElement(functionName, map, guid).getPen();
  }

  protected String paintToString(DrawablePaint drawablePaint) {
    if (drawablePaint instanceof DrawableColorPaint) {
      return "#"
          + Integer.toHexString(((DrawableColorPaint) drawablePaint).getColor()).substring(2);
    }
    if (drawablePaint instanceof DrawableTexturePaint) {
      return "asset://" + ((DrawableTexturePaint) drawablePaint).getAsset().getMD5Key();
    }
    return "";
  }

  /**
   * Get the name of the drawing (if it has one).
   *
   * @param functionName this is used in the exception message
   * @param zone the zone that should contain the drawing
   * @param guid the id of the drawing
   * @return the drawing name
   * @throws ParserException the exception
   */
  protected String getDrawingName(String functionName, Zone zone, GUID guid)
      throws ParserException {
    DrawnElement de = getDrawnElement(functionName, zone, guid);
    AbstractDrawing ad = (AbstractDrawing) de.getDrawable();
    return ad.getName() == null ? "" : ad.getName();
  }

  /**
   * Converts the selected drawing element into a JSON string.
   *
   * @param functionName this is used in the exception message
   * @param map the zone that should contain the drawing
   * @param guid the id of the drawing.
   * @return JSON in String form.
   * @throws ParserException if the drawable is not found.
   */
  protected JsonObject getDrawingJSONInfo(String functionName, Zone map, GUID guid)
      throws ParserException {
    DrawnElement el = getDrawnElement(functionName, map, guid);
    AbstractDrawing d = (AbstractDrawing) el.getDrawable();
    JsonObject dinfo = new JsonObject();
    dinfo.addProperty("id", el.getDrawable().getId().toString());
    dinfo.addProperty("name", d.getName());
    dinfo.addProperty("layer", el.getDrawable().getLayer().name());
    dinfo.addProperty("type", getDrawableType(el));
    dinfo.add("bounds", boundsToJSON(map, d));
    dinfo.addProperty("penColor", paintToString(el.getPen().getPaint()));
    dinfo.addProperty("fillColor", paintToString(el.getPen().getBackgroundPaint()));
    dinfo.addProperty("opacity", el.getPen().getOpacity());
    dinfo.addProperty("isEraser", el.getPen().isEraser() ? BigDecimal.ONE : BigDecimal.ZERO);
    dinfo.addProperty("penWidth", el.getPen().getThickness());
    dinfo.add("path", pathToJSON(d));
    dinfo.addProperty(
        "isTemplate",
        el.getDrawable() instanceof AbstractTemplate ? BigDecimal.ONE : BigDecimal.ZERO);

    if (el.getDrawable() instanceof AbstractTemplate at) {
      dinfo.addProperty("templateSize", at.getRadius());
      JsonObject template = new JsonObject();
      if (!(el.getDrawable() instanceof WallTemplate)) {
        template.addProperty("radius", at.getRadius());
      }
      JsonObject vertex = new JsonObject();
      vertex.addProperty("x", at.getVertex().x);
      vertex.addProperty("y", at.getVertex().y);
      template.add("vertex", vertex);

      // order is important here as some templates extend others, so do them first.
      switch (at) {
        case BlastTemplate bt -> {
          template.addProperty("direction", bt.getDirection().toString());
          JsonObject controlOffset = new JsonObject();
          controlOffset.addProperty("x", bt.getOffsetX());
          controlOffset.addProperty("y", bt.getOffsetY());
          template.add("controlOffset", controlOffset);
        }
        case ConeTemplate ct -> template.addProperty("direction", ct.getDirection().toString());
        case WallTemplate wt -> {
          JsonArray pathInfo = new JsonArray();
          for (CellPoint cp : wt.getPath()) {
            JsonObject pointInfo = new JsonObject();
            pointInfo.addProperty("x", cp.x);
            pointInfo.addProperty("y", cp.y);
            pathInfo.add(pointInfo);
          }
          template.add("wallPath", pathInfo);
        }
        case LineTemplate lt -> {
          JsonObject pathVertex = new JsonObject();
          pathVertex.addProperty("x", lt.getPathVertex().x);
          pathVertex.addProperty("y", lt.getPathVertex().y);
          template.add("pathVertex", pathVertex);
        }
        case LineCellTemplate lct -> {
          JsonObject pathVertex = new JsonObject();
          pathVertex.addProperty("x", lct.getPathVertex().x);
          pathVertex.addProperty("y", lct.getPathVertex().y);
          template.add("pathVertex", pathVertex);
        }
        default -> {}
      }
      if (!template.isEmpty()) {
        dinfo.add("template", template);
      }
    }

    return dinfo;
  }

  private JsonObject boundsToJSON(Zone map, AbstractDrawing d) {
    JsonObject binfo = new JsonObject();
    // templates have a BOUNDS_PADDING so use the area bounds instead for those
    if (d instanceof AbstractTemplate) {
      binfo.addProperty("x", d.getArea(map).getBounds().x);
      binfo.addProperty("y", d.getArea(map).getBounds().y);
      binfo.addProperty("width", d.getArea(map).getBounds().width);
      binfo.addProperty("height", d.getArea(map).getBounds().height);
    } else {
      binfo.addProperty("x", d.getBounds(map).x);
      binfo.addProperty("y", d.getBounds(map).y);
      binfo.addProperty("width", d.getBounds(map).width);
      binfo.addProperty("height", d.getBounds(map).height);
    }
    return binfo;
  }

  private String getDrawableType(DrawnElement el) {
    if (el.getDrawable() instanceof LineSegment) {
      return "Line";
    } else if (el.getDrawable() instanceof ShapeDrawable sd) {
      return sd.getShapeTypeName();
    } else if (el.getDrawable() instanceof DrawablesGroup) {
      return "Group";
    } else if (el.getDrawable() instanceof AbstractTemplate t) {
      return t.getClass().getSimpleName();
    } else {
      return "unknown";
    }
  }

  private JsonArray pathToJSON(AbstractDrawing d) {
    if (d instanceof LineSegment) {
      JsonArray pinfo = new JsonArray();
      LineSegment line = (LineSegment) d;
      for (Point point : line.getPoints()) {
        JsonObject info = new JsonObject();
        info.addProperty("x", point.x);
        info.addProperty("y", point.y);
        pinfo.add(info);
      }
      return pinfo;
    } else if (d instanceof ShapeDrawable sd) {
      var shape = sd.getShape();
      if (shape instanceof Ellipse2D) {
        // We don't support converting ellipses to path.
        return new JsonArray();
      } else {
        // Convert shape into path
        JsonArray pinfo = new JsonArray();
        final PathIterator pathIter = shape.getPathIterator(null);
        float[] coords = new float[6];
        JsonObject lastinfo = new JsonObject();
        while (!pathIter.isDone()) {
          pathIter.currentSegment(coords);
          JsonObject info = new JsonObject();
          info.addProperty("x", coords[0]);
          info.addProperty("y", coords[1]);
          if (!info.equals(lastinfo)) {
            pinfo.add(info);
          }
          lastinfo = info;
          pathIter.next();
        }
        return pinfo;
      }
    }
    return new JsonArray();
  }

  /**
   * Parses a string to a boolean. Throws a <code>ParserException</code> if the drawing is not
   * found.
   *
   * @param functionName this is used in the exception message.
   * @param args List of parameters passed to the function.
   * @param param int reference to the boolean parameter.
   * @return the boolean equivalent to the passed in argument.
   * @throws ParserException if the parameter fails to parse.
   */
  protected boolean parseBoolean(String functionName, List<Object> args, int param)
      throws ParserException {
    try {
      return FunctionUtil.getBooleanValue(args.get(param));
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeInvalid",
              functionName,
              param,
              args.get(param).toString()));
    }
  }

  protected void sendToBack(Zone map, GUID guid) {
    List<DrawnElement> drawableList = map.getAllDrawnElements();
    for (DrawnElement de : drawableList) {
      if (de.getDrawable().getId().equals(guid)) {
        map.removeDrawable(de.getDrawable().getId());
        map.addDrawableRear(de);
      }
    }
    // horrid kludge needed to redraw zone :(
    for (DrawnElement de : map.getAllDrawnElements()) {
      MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
      MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
    }
    MapTool.getFrame().updateDrawTree();
    MapTool.getFrame().refresh();
  }

  protected void setDrawingOpacity(String functionName, Zone map, GUID guid, float op)
      throws ParserException {
    DrawnElement de = getDrawnElement(functionName, map, guid);
    if (de != null) {
      setOpacity(map, de, op);
      MapTool.getFrame().updateDrawTree();
      MapTool.getFrame().refresh();
    }
  }

  private void setOpacity(Zone map, DrawnElement d, float op) {
    if (d.getDrawable() instanceof DrawablesGroup) {
      DrawablesGroup dg = (DrawablesGroup) d.getDrawable();
      for (DrawnElement de : dg.getDrawableList()) {
        setOpacity(map, de, op);
      }
    } else {
      Pen pen = d.getPen();
      pen.setOpacity(op);
      d.setPen(pen);
      MapTool.serverCommand().updateDrawing(map.getId(), pen, d);
    }
  }

  protected void setDrawingName(String functionName, Zone map, GUID guid, String name)
      throws ParserException {
    DrawnElement de = getDrawnElement(functionName, map, guid);
    AbstractDrawing ad = (AbstractDrawing) de.getDrawable();

    if (name != null) {
      ad.setName(name);
      MapTool.getFrame().updateDrawTree();
      MapTool.getFrame().refresh();
    }
  }

  /**
   * Looks for a drawing on a specific map that matches a specific id. If found sets the Pen. Throws
   * a <code>ParserException</code> if the drawing is not found.
   *
   * @param functionName this is used in the exception message
   * @param map the zone that should contain the drawing
   * @param guid the id of the drawing.
   * @param pen the replacement pen.
   * @throws ParserException if the drawing is not found.
   */
  protected void setPen(String functionName, Zone map, GUID guid, Pen pen) throws ParserException {
    List<DrawnElement> drawableList = map.getAllDrawnElements();
    DrawnElement de = findDrawnElement(drawableList, guid);
    if (de != null) {
      de.setPen(new Pen(pen));
      return;
    }
    throw new ParserException(
        I18N.getText("macro.function.drawable.unknownDrawable", functionName, guid.toString()));
  }

  /**
   * Get the bounds of the drawing / template.
   *
   * <p>See {@link AbstractTemplate} for details about <code>BOUNDS_PADDING</code> and why it should
   * be ignored here.
   *
   * @param functionName this is used in the exception message
   * @param guid the guid of the drawn element
   * @return the bounds
   * @see AbstractTemplate
   */
  protected java.awt.Rectangle getDrawingBounds(String functionName, Zone zone, GUID guid)
      throws ParserException {

    DrawnElement de = getDrawnElement(functionName, zone, guid);

    Drawable d = de.getDrawable();
    if (d instanceof AbstractTemplate) {
      // AbstractTemplates have BOUNDS_PADDING applied to the drawable so use area bounds instead
      return d.getArea(zone).getBounds();
    } else {
      return d.getBounds(zone);
    }
  }

  /**
   * Get a single selected drawing or template relevant to the respective {@link DrawingPointerTool}
   * or {@link TemplatePointerTool}.
   *
   * @param functionName this is used in the exception message
   * @param zone the map
   * @return the selected drawn element
   */
  public DrawnElement getSingleSelectedDrawnElement(String functionName, Zone zone)
      throws ParserException {

    List<GUID> selectedDrawables = new ArrayList<>();

    // get a list of selected drawings depending on the selected tool
    Object selectedTool = MapTool.getFrame().getToolbox().getSelectedTool().getClass();
    if (selectedTool.equals(TemplatePointerTool.class)) {
      selectedDrawables = TemplatePointerTool.getSelectedDrawables();
    } else if (selectedTool.equals(DrawingPointerTool.class)) {
      selectedDrawables = DrawingPointerTool.getSelectedDrawables();
    }

    // if only one is selected, return that
    if (selectedDrawables.size() == 1) {
      return zone.getDrawnElement(selectedDrawables.getFirst());
    } else if (selectedDrawables.size() > 1) {
      throw new ParserException(
          I18N.getText("macro.function.drawable.noSingleSelectedDrawable", functionName));
    } else {
      throw new ParserException(
          I18N.getText("macro.function.drawable.noSelectedDrawable", functionName));
    }
  }
}
