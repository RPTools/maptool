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

import java.awt.event.MouseEvent;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.model.ZonePoint;

public abstract class AbstractDrawingLikeTool extends DefaultTool implements ZoneOverlay {
  private boolean isEraser;

  protected void setIsEraser(boolean eraser) {
    isEraser = eraser;
  }

  protected boolean isEraser() {
    return isEraser;
  }

  protected boolean isEraser(MouseEvent e) {
    return SwingUtil.isShiftDown(e);
  }

  protected boolean isSnapToGrid(MouseEvent e) {
    return SwingUtil.isControlDown(e);
  }

  protected boolean isSnapToCenter(MouseEvent e) {
    return e.isAltDown();
  }

  protected boolean isLinearTool() {
    return false;
  }

  protected ZonePoint getPoint(MouseEvent e) {
    ScreenPoint sp = new ScreenPoint(e.getX(), e.getY());
    ZonePoint zp = sp.convertToZoneRnd(renderer);
    if (isSnapToCenter(e) && isLinearTool()) {
      // Only line tools will snap to center as the Alt key for rectangle, diamond and oval
      // is used for expand from center.
      zp = renderer.getCellCenterAt(sp);
    } else if (isSnapToGrid(e)) {
      zp = renderer.getZone().getNearestVertex(zp);
    }
    return zp;
  }
}
