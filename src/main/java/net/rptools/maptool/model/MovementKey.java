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
package net.rptools.maptool.model;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import net.rptools.maptool.client.tool.PointerTool;

public class MovementKey extends AbstractAction {
  private static final long serialVersionUID = -4103031698708914986L;
  private final double dx, dy;
  private final PointerTool
      tool; // I'd like to store this in the Grid, but then it has to be final :(

  public MovementKey(PointerTool callback, double x, double y) {
    tool = callback;
    dx = x;
    dy = y;
  }

  @Override
  public String toString() {
    return "[" + dx + "," + dy + "]";
  }

  public void actionPerformed(ActionEvent e) {
    tool.handleKeyMove(dx, dy);
  }
}
