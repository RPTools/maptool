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
import net.rptools.maptool.language.I18N;

/** */
public class CoordinateStatusBar extends JLabel {

  private static final Dimension minSize = new Dimension(75, 10);

  public CoordinateStatusBar() {
    setToolTipText(I18N.getString("CoordinateStatusBar.mapCoordinates")); // $NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getMinimumSize()
   */
  public Dimension getMinimumSize() {
    return minSize;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  public void clear() {
    setText(""); // $NON-NLS-1$
  }

  public void update(int x, int y) {
    setText("  " + x + ", " + y); // $NON-NLS-1$ //$NON-NLS-2$
  }
}
