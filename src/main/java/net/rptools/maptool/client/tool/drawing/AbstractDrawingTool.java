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
package net.rptools.maptool.client.tool.drawing;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.List;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.swing.colorpicker.ColorPicker;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/** Tool for drawing freehand lines. */
public abstract class AbstractDrawingTool extends DefaultTool implements ZoneOverlay {

  private static final long serialVersionUID = 9121558405484986225L;

  private boolean isEraser;
  private boolean isSnapToGridSelected;
  private boolean isEraseSelected;

  protected Rectangle createRect(ZonePoint originPoint, ZonePoint newPoint) {
    int x = Math.min(originPoint.x, newPoint.x);
    int y = Math.min(originPoint.y, newPoint.y);

    int w = Math.max(originPoint.x, newPoint.x) - x;
    int h = Math.max(originPoint.y, newPoint.y) - y;

    return new Rectangle(x, y, w, h);
  }

  protected Shape createDiamond(ZonePoint originPoint, ZonePoint newPoint) {
    int ox = originPoint.x;
    int oy = originPoint.y;
    int nx = newPoint.x;
    int ny = newPoint.y;
    int x1 = ox - (ny - oy) + ((nx - ox) / 2);
    int y1 = ((oy + ny) / 2) - ((nx - ox) / 4);
    int x2 = ox + (ny - oy) + ((nx - ox) / 2);
    int y2 = ((oy + ny) / 2) + ((nx - ox) / 4);
    int x[] = {originPoint.x, x1, nx, x2};
    int y[] = {originPoint.y, y1, ny, y2};
    return new Polygon(x, y, 4);
  }

  protected Shape createHollowDiamond(ZonePoint originPoint, ZonePoint newPoint, Pen pen) {
    int ox = originPoint.x;
    int oy = originPoint.y;
    int nx = newPoint.x;
    int ny = newPoint.y;
    int x1 = ox - (ny - oy) + ((nx - ox) / 2);
    int y1 = ((oy + ny) / 2) - ((nx - ox) / 4);
    int x2 = ox + (ny - oy) + ((nx - ox) / 2);
    int y2 = ((oy + ny) / 2) + ((nx - ox) / 4);
    int x[] = {originPoint.x, x1, nx, x2, originPoint.x};
    int y[] = {originPoint.y, y1, ny, y2, originPoint.y};

    BasicStroke stroke =
        new BasicStroke(pen.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    Path2D path = new Path2D.Double();

    for (int l = 0; l < 5; l++) {
      if (path.getCurrentPoint() == null) {
        path.moveTo(x[l], y[l]);
      } else {
        path.lineTo(x[l], y[l]);
      }
    }

    Area area = new Area(stroke.createStrokedShape(path));
    return area;
  }

  protected AffineTransform getPaintTransform(ZoneRenderer renderer) {
    AffineTransform transform = new AffineTransform();
    transform.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    transform.scale(renderer.getScale(), renderer.getScale());
    return transform;
  }

  protected void paintTransformed(Graphics2D g, ZoneRenderer renderer, Drawable drawing, Pen pen) {
    AffineTransform transform = getPaintTransform(renderer);
    AffineTransform oldTransform = g.getTransform();
    g.transform(transform);
    drawing.draw(g, pen);
    g.setTransform(oldTransform);
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    if (MapTool.getPlayer().isGM()) {
      MapTool.getFrame()
          .showControlPanel(MapTool.getFrame().getColorPicker(), getLayerSelectionDialog());
    } else {
      MapTool.getFrame().showControlPanel(MapTool.getFrame().getColorPicker());
    }
    renderer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    MapTool.getFrame().getColorPicker().setSnapSelected(isSnapToGridSelected);
    MapTool.getFrame().getColorPicker().setEraseSelected(isEraseSelected);
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    MapTool.getFrame().removeControlPanel();
    renderer.setCursor(Cursor.getDefaultCursor());

    isSnapToGridSelected = MapTool.getFrame().getColorPicker().isSnapSelected();
    isEraseSelected = MapTool.getFrame().getColorPicker().isEraseSelected();

    super.detachFrom(renderer);
  }

  protected void setIsEraser(boolean eraser) {
    isEraser = eraser;
  }

  protected boolean isEraser() {
    return isEraser;
  }

  protected boolean isBackgroundFill(MouseEvent e) {
    boolean defaultValue = MapTool.getFrame().getColorPicker().isFillBackgroundSelected();
    return defaultValue;
  }

  protected boolean isEraser(MouseEvent e) {
    boolean defaultValue = MapTool.getFrame().getColorPicker().isEraseSelected();
    if (SwingUtil.isShiftDown(e)) {
      // Invert from the color panel
      defaultValue = !defaultValue;
    }
    return defaultValue;
  }

  protected boolean isSnapToGrid(MouseEvent e) {
    boolean defaultValue = MapTool.getFrame().getColorPicker().isSnapSelected();
    if (SwingUtil.isControlDown(e)) {
      // Invert from the color panel
      defaultValue = !defaultValue;
    }
    return defaultValue;
  }

  protected boolean isSnapToCenter(MouseEvent e) {
    boolean defaultValue = false;
    if (e.isAltDown()) {
      defaultValue = true;
    }
    return defaultValue;
  }

  protected Pen getPen() {
    Pen pen = new Pen(MapTool.getFrame().getPen());
    pen.setEraser(isEraser);

    ColorPicker picker = MapTool.getFrame().getColorPicker();
    if (picker.isFillForegroundSelected()) {
      pen.setForegroundMode(Pen.MODE_SOLID);
    } else {
      pen.setForegroundMode(Pen.MODE_TRANSPARENT);
    }
    if (picker.isFillBackgroundSelected()) {
      pen.setBackgroundMode(Pen.MODE_SOLID);
    } else {
      pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
    }
    pen.setSquareCap(picker.isSquareCapSelected());
    pen.setThickness(picker.getStrokeWidth());
    return pen;
  }

  protected ZonePoint getPoint(MouseEvent e) {
    ScreenPoint sp = new ScreenPoint(e.getX(), e.getY());
    ZonePoint zp = sp.convertToZoneRnd(renderer);
    if (isSnapToCenter(e) && this instanceof AbstractLineTool) {
      // Only line tools will snap to center as the Alt key for rectangle, diamond and oval
      // is used for expand from center.
      zp = renderer.getCellCenterAt(sp);
    } else if (isSnapToGrid(e)) {
      zp = renderer.getZone().getNearestVertex(zp);
    }
    return zp;
  }

  protected Area getTokenTopology(Zone.TopologyType topologyType) {
    List<Token> topologyTokens =
        MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokensWithTopology(topologyType);

    Area tokenTopology = new Area();
    for (Token topologyToken : topologyTokens) {
      tokenTopology.add(topologyToken.getTransformedTopology(topologyType));
    }

    return tokenTopology;
  }

  @Override
  public abstract void paintOverlay(ZoneRenderer renderer, Graphics2D g);

  protected void paintTopologyOverlay(Graphics2D g, Drawable drawable) {
    paintTopologyOverlay(g, drawable, Pen.MODE_SOLID);
  }

  protected void paintTopologyOverlay(Graphics2D g, Shape shape) {
    ShapeDrawable drawable = null;

    if (shape != null) {
      drawable = new ShapeDrawable(shape, false);
    }

    paintTopologyOverlay(g, drawable, Pen.MODE_SOLID);
  }

  protected void paintTopologyOverlay(Graphics2D g, Shape shape, int penMode) {
    ShapeDrawable drawable = null;

    if (shape != null) {
      drawable = new ShapeDrawable(shape, false);
    }

    paintTopologyOverlay(g, drawable, penMode);
  }

  protected void paintTopologyOverlay(Graphics2D g) {
    Rectangle rectangle = null;
    paintTopologyOverlay(g, rectangle, Pen.MODE_SOLID);
  }

  protected void paintTopologyOverlay(Graphics2D g, Drawable drawable, int penMode) {
    if (MapTool.getPlayer().isGM()) {
      Zone zone = renderer.getZone();

      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
      g2.scale(renderer.getScale(), renderer.getScale());

      g2.setColor(AppStyle.tokenMblColor);
      g2.fill(getTokenTopology(Zone.TopologyType.MBL));
      g2.setColor(AppStyle.tokenTopologyColor);
      g2.fill(getTokenTopology(Zone.TopologyType.WALL_VBL));
      g2.setColor(AppStyle.tokenHillVblColor);
      g2.fill(getTokenTopology(Zone.TopologyType.HILL_VBL));
      g2.setColor(AppStyle.tokenPitVblColor);
      g2.fill(getTokenTopology(Zone.TopologyType.PIT_VBL));
      g2.setColor(AppStyle.tokenCoverVblColor);
      g2.fill(getTokenTopology(Zone.TopologyType.COVER_VBL));

      g2.setColor(AppStyle.topologyTerrainColor);
      g2.fill(zone.getTopology(Zone.TopologyType.MBL));

      g2.setColor(AppStyle.topologyColor);
      g2.fill(zone.getTopology(Zone.TopologyType.WALL_VBL));

      g2.setColor(AppStyle.hillVblColor);
      g2.fill(zone.getTopology(Zone.TopologyType.HILL_VBL));

      g2.setColor(AppStyle.pitVblColor);
      g2.fill(zone.getTopology(Zone.TopologyType.PIT_VBL));

      g2.setColor(AppStyle.coverVblColor);
      g2.fill(zone.getTopology(Zone.TopologyType.COVER_VBL));

      g2.dispose();
    }

    if (drawable != null) {
      Pen pen = new Pen();
      pen.setEraser(getPen().isEraser());
      pen.setOpacity(AppStyle.topologyRemoveColor.getAlpha() / 255.0f);
      pen.setBackgroundMode(penMode);

      if (penMode == Pen.MODE_TRANSPARENT) {
        pen.setThickness(3.0f);
      }

      if (pen.isEraser()) {
        pen.setEraser(false);
      }
      if (isEraser()) {
        pen.setBackgroundPaint(new DrawableColorPaint(AppStyle.topologyRemoveColor));
      } else {
        pen.setBackgroundPaint(new DrawableColorPaint(AppStyle.topologyAddColor));
      }
      paintTransformed(g, renderer, drawable, pen);
    }
  }

  /**
   * Render a drawable on a zone. This method consolidates all of the calls to the server in one
   * place so that it is easier to keep them in sync.
   *
   * @param zoneId Id of the zone where the <code>drawable</code> is being drawn.
   * @param pen The pen used to draw.
   * @param drawable What is being drawn.
   */
  protected void completeDrawable(GUID zoneId, Pen pen, Drawable drawable) {
    if (!hasPaint(pen)) {
      return;
    }
    if (drawable.getBounds() == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) {
      drawable.setLayer(getSelectedLayer());
    } else {
      drawable.setLayer(Layer.TOKEN);
    }

    // Send new textures
    MapToolUtil.uploadTexture(pen.getPaint());
    MapToolUtil.uploadTexture(pen.getBackgroundPaint());

    // Tell the local/server to render the drawable.
    MapTool.serverCommand().draw(zoneId, pen, drawable);

    // Allow it to be undone
    Zone z = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    z.addDrawable(pen, drawable);
  }

  private boolean hasPaint(Pen pen) {
    return pen.getForegroundMode() != Pen.MODE_TRANSPARENT
        || pen.getBackgroundMode() != Pen.MODE_TRANSPARENT;
  }
}
