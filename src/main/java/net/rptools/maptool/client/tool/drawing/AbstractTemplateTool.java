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

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.swing.colorpicker.ColorPicker;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;

/** Base class for tools that draw templates. */
public abstract class AbstractTemplateTool extends AbstractDrawingLikeTool implements ZoneOverlay {

  private static final long serialVersionUID = 9121558405484986225L;

  private boolean isSnapToGridSelected;
  private boolean isEraseSelected;

  protected AffineTransform getPaintTransform(ZoneRenderer renderer) {
    AffineTransform transform = new AffineTransform();
    transform.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    transform.scale(renderer.getScale(), renderer.getScale());
    return transform;
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

  protected Pen getPen() {
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
    return pen.getForegroundMode() != Pen.MODE_TRANSPARENT
        || pen.getBackgroundMode() != Pen.MODE_TRANSPARENT;
  }
}
