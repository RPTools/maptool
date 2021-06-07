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
import javax.swing.*;
import net.rptools.maptool.client.tool.DefaultTool;

public class ViewMovementKey extends AbstractAction {
  private static final long serialVersionUID = -3922295728913067353L;

  private final int dx, dy;
  private final DefaultTool tool;

  public ViewMovementKey(DefaultTool callback, int x, int y) {
    tool = callback;
    dx = x;
    dy = y;
  }

  @Override
  public String toString() {
    return "[" + dx + "," + dy + "]";
  }

  public void actionPerformed(ActionEvent e) {
    tool.moveViewByCells(dx, dy);
  }
}
