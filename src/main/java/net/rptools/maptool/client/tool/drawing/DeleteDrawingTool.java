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

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.ZoneActivated;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.drawpanel.DrawPanelPopupMenu;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawnElement;

/** Tool for deleting drawings. */
public class DeleteDrawingTool extends DefaultTool implements ZoneOverlay, MouseListener {

  @Serial private static final long serialVersionUID = -8846217296437736953L;

  private static final Set<GUID> selectedDrawings = new HashSet<>();
  private static final DrawPanelPopupMenu.DeleteDrawingAction deleteAction =
      new DrawPanelPopupMenu.DeleteDrawingAction(selectedDrawings);

  public DeleteDrawingTool() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction);
  }

  @Override
  public String getTooltip() {
    return "tool.deletedrawing.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.deletedrawing.instructions";
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    if (MapTool.getPlayer().isGM()) {
      MapTool.getFrame().showControlPanel(getLayerSelectionDialog());
    }
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    MapTool.getFrame().removeControlPanel();
    super.detachFrom(renderer);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    super.mouseClicked(e);
    var zone = renderer.getZone();

    var multiSelect = e.isShiftDown();

    if (!multiSelect) selectedDrawings.clear();

    var drawableList = zone.getDrawnElements(getSelectedLayer());
    for (var element : drawableList) {
      var drawable = element.getDrawable();
      var id = drawable.getId();
      ZonePoint pos = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
      if (drawable.getBounds().contains(pos.x, pos.y)) {
        if (!selectedDrawings.contains(id)) selectedDrawings.add(id);
        else selectedDrawings.remove(id);
        break;
      }
    }
    if (e.getClickCount() == 2) deleteAction.actionPerformed(new ActionEvent(this, 0, ""));

    renderer.repaint();
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {

    for (var id : selectedDrawings) {
      var drawnElement = renderer.getZone().getDrawnElement(id);
      if (drawnElement == null) continue;

      drawBox(g, drawnElement);
    }
  }

  private void drawBox(Graphics2D g, DrawnElement element) {
    var box = element.getDrawable().getBounds();
    var pen = element.getPen();

    var scale = renderer.getScale();

    var screenPoint = ScreenPoint.fromZonePoint(renderer, box.x, box.y);

    var x = (int) (screenPoint.x - pen.getThickness() * scale / 2);
    var y = (int) (screenPoint.y - pen.getThickness() * scale / 2);
    var w = (int) ((box.width + pen.getThickness()) * scale);
    var h = (int) ((box.height + pen.getThickness()) * scale);

    AppStyle.selectedBorder.paintAround(g, x, y, w, h);
  }

  @Subscribe
  void onZoneActivated(ZoneActivated event) {
    selectedDrawings.clear();
  }
}
