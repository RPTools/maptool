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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.colorpicker.ColorPicker;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public final class DrawingTool<StateT> extends AbstractDrawingLikeTool {
  private final String instructionKey;
  private final String tooltipKey;
  private final Strategy<StateT> strategy;

  /** The current state of the tool. If {@code null}, nothing is being drawn right now. */
  private @Nullable StateT state;

  private ZonePoint currentPoint = new ZonePoint(0, 0);
  private boolean centerOnOrigin = false;

  public DrawingTool(String instructionKey, String tooltipKey, Strategy<StateT> strategy) {
    this.instructionKey = instructionKey;
    this.tooltipKey = tooltipKey;
    this.strategy = strategy;
  }

  @Override
  public String getInstructions() {
    return instructionKey;
  }

  @Override
  public String getTooltip() {
    return tooltipKey;
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected boolean isLinearTool() {
    return strategy.isLinear();
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
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    MapTool.getFrame().removeControlPanel();
    renderer.setCursor(Cursor.getDefaultCursor());

    super.detachFrom(renderer);
  }

  /** If currently drawing, stop and clear it. */
  @Override
  protected void resetTool() {
    if (state != null) {
      state = null;
      renderer.repaint();
    } else {
      super.resetTool();
    }
  }

  @Override
  protected boolean isEraser(MouseEvent e) {
    // Use the color picker as the default, but invert based on key state.
    var inverted = super.isEraser(e);
    boolean defaultValue = MapTool.getFrame().getColorPicker().isEraseSelected();
    if (inverted) {
      defaultValue = !defaultValue;
    }
    return defaultValue;
  }

  @Override
  protected boolean isSnapToGrid(MouseEvent e) {
    // Use the color picker as the default, but invert based on key state.
    var inverted = super.isSnapToGrid(e);
    boolean defaultValue = MapTool.getFrame().getColorPicker().isSnapSelected();
    if (inverted) {
      // Invert from the color panel
      defaultValue = !defaultValue;
    }
    return defaultValue;
  }

  private boolean isBackgroundFill() {
    return MapTool.getFrame().getColorPicker().isFillBackgroundSelected();
  }

  private boolean hasPaint(Pen pen) {
    return pen.getForegroundMode() != Pen.MODE_TRANSPARENT
        || pen.getBackgroundMode() != Pen.MODE_TRANSPARENT;
  }

  private Pen getPen() {
    Pen pen = new Pen(MapTool.getFrame().getPen());
    pen.setEraser(isEraser());

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

  private Drawable toDrawable(Shape shape) {
    if (shape instanceof Path2D path) {
      if (isLinearTool()) {
        // Preserve the path. Will be an unbroken path of straight segments. In the future we can
        // look at handling more general paths.
        var pen = getPen();
        return new LineSegment(pen.getThickness(), pen.getSquareCap(), path);
      } else {
        // The path describes an area.
        return new ShapeDrawable(new Area(path), true);
      }
    }

    // All other shapes are solid and represented by ShapeDrawable.
    return new ShapeDrawable(shape, true);
  }

  private void submit(Shape shape) {
    var pen = getPen();
    if (!hasPaint(pen)) {
      return;
    }

    Zone zone = getZone();
    var drawable = toDrawable(shape);
    if (drawable.getBounds(zone) == null) {
      return;
    }

    if (MapTool.getPlayer().isGM()) {
      drawable.setLayer(getSelectedLayer());
    } else {
      drawable.setLayer(Zone.Layer.getDefaultPlayerLayer());
    }

    // Send new textures
    MapToolUtil.uploadTexture(pen.getPaint());
    MapToolUtil.uploadTexture(pen.getBackgroundPaint());

    // Tell the local/server to render the drawable.
    MapTool.serverCommand().draw(zone.getId(), pen, drawable);

    // Allow it to be undone
    zone.addDrawable(pen, drawable);
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    g2.scale(renderer.getScale(), renderer.getScale());

    if (state != null) {
      // Linear tools are not filled until completed.
      var result = strategy.getShape(state, currentPoint, centerOnOrigin, false);
      if (result != null) {
        var drawable = toDrawable(result.shape());

        Pen pen = getPen();
        if (isEraser()) {
          pen = new Pen(pen);
          pen.setEraser(false);
          pen.setPaint(new DrawableColorPaint(Color.white));
          pen.setBackgroundPaint(new DrawableColorPaint(Color.white));
        }

        drawable.draw(renderer.getZone(), g2, pen);

        // Measurements
        drawMeasurementOverlay(renderer, g, result.measurement());
      }
    }

    g2.dispose();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (state == null) {
      // We're not doing anything, so delegate to default behaviour.
      super.mouseDragged(e);
    } else if (strategy.isFreehand()) {
      // Extend the line.
      setIsEraser(isEraser(e));
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown(); // Pointless, but it doesn't hurt for consistency.
      strategy.pushPoint(state, currentPoint);
      renderer.repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    setIsEraser(isEraser(e));
    if (state != null) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();
      renderer.repaint();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    setIsEraser(isEraser(e));

    if (SwingUtilities.isLeftMouseButton(e)) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();

      if (state == null) {
        state = strategy.startNewAtPoint(currentPoint);
      } else if (!strategy.isFreehand()) {
        var result = strategy.getShape(state, currentPoint, centerOnOrigin, isBackgroundFill());
        state = null;
        if (result != null) {
          submit(result.shape());
        }
      }
      renderer.repaint();
    } else if (state != null && !strategy.isFreehand()) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();
      strategy.pushPoint(state, currentPoint);
      renderer.repaint();
    }

    super.mousePressed(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (strategy.isFreehand() && SwingUtilities.isLeftMouseButton(e)) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();
      var result = strategy.getShape(state, currentPoint, centerOnOrigin, isBackgroundFill());
      state = null;
      if (result != null) {
        submit(result.shape());
      }
    }
  }
}
