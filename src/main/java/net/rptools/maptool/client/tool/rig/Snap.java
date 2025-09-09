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
package net.rptools.maptool.client.tool.rig;

import java.awt.geom.Point2D;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.ZonePoint;

@FunctionalInterface
public interface Snap {
  Point2D snap(Point2D point);

  static Snap none() {
    return point -> point;
  }

  static Snap vertex(Grid grid) {
    return point -> {
      var zonePoint = new ZonePoint((int) Math.floor(point.getX()), (int) Math.floor(point.getY()));
      var result = grid.getNearestVertex(zonePoint);
      return new Point2D.Double(result.x, result.y);
    };
  }

  static Snap center(Grid grid) {
    return point -> {
      var zonePoint = new ZonePoint((int) Math.floor(point.getX()), (int) Math.floor(point.getY()));
      var cellPoint = grid.convert(zonePoint);
      return grid.getCellCenter(cellPoint);
    };
  }

  static Snap fine(Grid grid) {
    return point -> {
      var zonePoint = new ZonePoint((int) Math.floor(point.getX()), (int) Math.floor(point.getY()));
      return grid.snapFine(zonePoint);
    };
  }
}
