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
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;

/** Base class for tools that draw templates. */
public abstract class AbstractTemplateTool extends DefaultTool implements ZoneOverlay {

  private static final long serialVersionUID = 9121558405484986225L;

  private boolean isSnapToGridSelected;
  private boolean isEraseSelected;
  private boolean isEraser;

  protected AffineTransform getPaintTransform(ZoneRenderer renderer) {
    return renderer.getViewModel().getZoneScale().toScreenTransform();
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

  protected boolean isEraser(MouseEvent e) {
    boolean defaultValue = MapTool.getFrame().getColorPicker().isEraseSelected();
    if (SwingUtil.isShiftDown(e)) {
      // Invert from the color panel
      defaultValue = !defaultValue;
    }
    return defaultValue;
  }

  /**
   * @return The pen to used for the finished template.
   */
  protected Pen getPen() {
    var pen = MapTool.getFrame().getPen(isEraser());
    if (pen.getBackgroundPaint() == null) {
      pen.setBackgroundPaint(new DrawableColorPaint(Color.black));
    }
    return pen;
  }

  /**
   * Get the pen set up to paint the overlay.
   *
   * @return The pen used to paint the overlay.
   */
  protected Pen getPenForOverlay() {
    // Get the pen and modify to only show a cursor and the boundary
    Pen pen = getPen(); // new copy of pen, OK to modify
    if (pen.getBackgroundPaint() == null) {
      pen.setBackgroundPaint(new DrawableColorPaint(Color.black));
    }
    if (pen.getPaint() == null) {
      pen.setPaint(new DrawableColorPaint(Color.black));
    }
    pen.setThickness(3);
    if (pen.isEraser()) {
      pen.setEraser(false);
      pen.setPaint(new DrawableColorPaint(Color.WHITE));
    }
    return pen;
  }

  @Override
  public abstract void paintOverlay(ZoneRenderer renderer, Graphics2D g);

  /**
   * Render a drawable on a zone. This method consolidates all of the calls to the server in one
   * place so that it is easier to keep them in sync.
   *
   * @param pen The pen used to draw.
   * @param drawable What is being drawn.
   */
  protected void completeDrawable(Pen pen, Drawable drawable) {
    var zone = getZone();

    if (!hasPaint(pen)) {
      return;
    }
    if (drawable.getBounds(zone) == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) {
      drawable.setLayer(getSelectedLayer());
    } else {
      drawable.setLayer(Layer.getDefaultPlayerLayer());
    }

    // Send new textures
    MapToolUtil.uploadTexture(pen.getPaint());
    MapToolUtil.uploadTexture(pen.getBackgroundPaint());

    // Tell the local/server to render the drawable.
    MapTool.serverCommand().draw(zone.getId(), pen, drawable);

    // Allow it to be undone
    zone.addDrawable(pen, drawable);
  }

  private boolean hasPaint(Pen pen) {
    return pen.getPaint() != null || pen.getBackgroundPaint() != null;
  }
}
