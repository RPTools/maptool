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
package net.rptools.maptool.client.ui.zone;

import java.awt.*;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.Tool;

/** Transparent JPanel that displays the PointTool overlay. */
public class PointerToolOverlay extends JPanel {

  public PointerToolOverlay() {
    super();
    this.setOpaque(false); // transparent overlay
  }

  /**
   * Paints the PointerTool overlay. Does nothing if the PointerTool is not selected.
   *
   * @param g – the Graphics object
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Tool currentTool = MapTool.getFrame().getToolbox().getSelectedTool();
    if (currentTool instanceof PointerTool) {
      ((PointerTool) currentTool).paintOverlay((Graphics2D) g);
    }
  }

  /**
   * Returns whether the point is contained by a subcomponent of the overlay. This allows the cursor
   * to reflect the components underneath the overlay.
   *
   * @param x – the x coordinate of the point
   * @param y – the y coordinate of the point
   * @return true if the point is within a subcomponent; otherwise false
   */
  @Override
  public boolean contains(int x, int y) {
    for (Component component : getComponents()) {
      Point containerPoint = SwingUtilities.convertPoint(this, x, y, component);
      if (component.contains(containerPoint)) {
        return true;
      }
    }
    return false;
  }
}
