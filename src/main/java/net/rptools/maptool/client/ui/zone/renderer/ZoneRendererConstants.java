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
package net.rptools.maptool.client.ui.zone.renderer;

import java.awt.*;
import net.rptools.maptool.model.Grid;

public class ZoneRendererConstants {
  public static final int MIN_GRID_SIZE = Grid.MIN_GRID_SIZE;
  public static final int MAX_GRID_SIZE = Grid.MAX_GRID_SIZE;

  public static final Color TRANSLUCENT_YELLOW =
      new Color(Color.yellow.getRed(), Color.yellow.getGreen(), Color.yellow.getBlue(), 50);

  public enum TokenMoveCompletion {
    TRUE,
    FALSE,
    OTHER
  }
}
