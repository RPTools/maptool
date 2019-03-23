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

import java.util.Iterator;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class DrawingFunctions extends AbstractFunction {

  public DrawingFunctions(int minParameters, int maxParameters, String... aliases) {
    super(minParameters, maxParameters, aliases);
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    // This class should never be called.
    return null;
  }

  /**
   * Moves a specified drawing on a specified map to the top of the drawing stack.
   *
   * @param map the zone that should contain the drawing
   * @param guid the id of the drawing.
   * @throws ParserException if there were more or less parameters than allowed
   */
  protected void bringToFront(Zone map, GUID guid) {
    List<DrawnElement> drawableList = map.getAllDrawnElements();
    Iterator<DrawnElement> iter = drawableList.iterator();
    while (iter.hasNext()) {
      DrawnElement de = iter.next();
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
    Iterator<DrawnElement> iter = drawableList.iterator();
    while (iter.hasNext()) {
      DrawnElement de = iter.next();
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
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  protected void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
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
        I18N.getText(
            "macro.function.drawingFunction.unknownDrawing", functionName, guid.toString()));
  }

  private DrawnElement findDrawnElement(List<DrawnElement> drawableList, GUID guid) {
    Iterator<DrawnElement> iter = drawableList.iterator();
    while (iter.hasNext()) {
      DrawnElement de = iter.next();
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
      Float per = Float.parseFloat(f);
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
      Float per = Float.parseFloat(f);
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
    if ("GM".equalsIgnoreCase(layer)) return Layer.GM;
    else if ("OBJECT".equalsIgnoreCase(layer)) return Layer.OBJECT;
    else if ("BACKGROUND".equalsIgnoreCase(layer)) return Layer.BACKGROUND;
    return Layer.TOKEN;
  }

  /**
   * Find the map/zone for a given map name
   *
   * @param functionName String Name of the calling function.
   * @param mapName String Name of the searched for map.
   * @return ZoneRenderer The map/zone.
   * @throws ParserException if the map is not found
   */
  protected ZoneRenderer getNamedMap(String functionName, String mapName) throws ParserException {
    for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
      if (mapName.equals(zr.getZone().getName())) {
        return zr;
      }
    }
    throw new ParserException(
        I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, mapName));
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

  /**
   * Parses a string into either a Color Paint or Texture Paint.
   *
   * @param paint String containing the paint description.
   * @return Pen DrawableTexturePaint or DrawableColorPaint.
   */
  protected DrawablePaint paintFromString(String paint) {
    if (paint.toLowerCase().startsWith("asset://")) {
      String id = paint.substring(8);
      return new DrawableTexturePaint(new MD5Key(id));
    } else if (paint.length() == 32) {
      return new DrawableTexturePaint(new MD5Key(paint));
    } else {
      return new DrawableColorPaint(MapToolUtil.getColor(paint));
    }
  }

  protected String paintToString(DrawablePaint drawablePaint) {
    if (drawablePaint instanceof DrawableColorPaint) {
      return "#"
          + Integer.toHexString(((DrawableColorPaint) drawablePaint).getColor()).substring(2);
    }
    if (drawablePaint instanceof DrawableTexturePaint) {
      return "asset://" + ((DrawableTexturePaint) drawablePaint).getAsset().getId();
    }
    return "";
  }

  /**
   * Parses a string to a boolean. Throws a <code>ParserException</code> if the drawing is not
   * found.
   *
   * @param functionName this is used in the exception message.
   * @param args List of parameters passed to the function.
   * @param param int reference to the boolean parameter.
   * @throws ParserException if the parameter fails to parse.
   */
  protected boolean parseBoolean(String functionName, List<Object> args, int param)
      throws ParserException {
    try {
      return AbstractTokenAccessorFunction.getBooleanValue(args.get(param));
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
    Iterator<DrawnElement> iter = drawableList.iterator();
    while (iter.hasNext()) {
      DrawnElement de = iter.next();
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
  protected void setPen(String functionName, Zone map, GUID guid, Object pen)
      throws ParserException {
    if (!(pen instanceof Pen))
      throw new ParserException(
          I18N.getText("macro.function.drawingFunction.invalidPen", functionName));
    Pen p = new Pen((Pen) pen);
    List<DrawnElement> drawableList = map.getAllDrawnElements();
    DrawnElement de = findDrawnElement(drawableList, guid);
    if (de != null) {
      de.setPen(p);
      return;
    }
    throw new ParserException(
        I18N.getText(
            "macro.function.drawingFunction.unknownDrawing", functionName, guid.toString()));
  }
}
