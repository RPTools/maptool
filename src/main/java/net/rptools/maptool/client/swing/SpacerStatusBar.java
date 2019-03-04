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
package net.rptools.maptool.client.swing;

import java.awt.Dimension;
import javax.swing.JLabel;

/** */
public class SpacerStatusBar extends JLabel {

  private Dimension minSize = new Dimension(0, 10);

  public SpacerStatusBar(int size) {
    minSize = new Dimension(size, 10);
  }

  public Dimension getMinimumSize() {
    return minSize;
  }

  public Dimension getPreferredSize() {
    return getMinimumSize();
  }
}
