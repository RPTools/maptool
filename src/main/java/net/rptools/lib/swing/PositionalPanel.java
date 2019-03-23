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
package net.rptools.lib.swing;

import java.awt.Component;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PositionalPanel extends JPanel {

  public PositionalPanel() {
    setLayout(new PositionalLayout());
  }

  public void addImpl(Component comp, Object constraints, int index) {

    if (!(constraints instanceof PositionalLayout.Position)) {
      throw new IllegalArgumentException("Use add(Component, PositionalLayout.Position)");
    }

    super.addImpl(comp, constraints, index);

    if (((PositionalLayout.Position) constraints) == PositionalLayout.Position.CENTER) {

      setComponentZOrder(comp, getComponents().length - 1);
    } else {
      setComponentZOrder(comp, 0);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
   */
  public boolean isOptimizedDrawingEnabled() {
    return false;
  }
}
