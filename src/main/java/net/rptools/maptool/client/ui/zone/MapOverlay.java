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

import java.awt.Component;
import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Transparent JPanel that displays the UI elements over the map. */
public class MapOverlay extends JPanel {

  public MapOverlay() {
    super();
    this.setOpaque(false); // transparent overlay
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
