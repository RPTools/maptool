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

import java.awt.geom.Path2D;
import javax.annotation.Nullable;
import net.rptools.maptool.model.ZonePoint;

public class CrossStrategy implements Strategy<ZonePoint> {
  @Override
  public ZonePoint startNewAtPoint(ZonePoint point) {
    return point;
  }

  @Override
  public @Nullable DrawingResult getShape(
      ZonePoint state, ZonePoint currentPoint, boolean centerOnOrigin, boolean isFilled) {
    var bounds = Strategy.normalizedRectangle(state, currentPoint, false);
    if (bounds.width == 0 && bounds.height == 0) {
      return null;
    }

    var path = new Path2D.Double();
    path.moveTo(bounds.x, bounds.y);
    path.lineTo(bounds.x + bounds.width, bounds.y + bounds.height);
    path.moveTo(bounds.x, bounds.y + bounds.height);
    path.lineTo(bounds.x + bounds.width, bounds.y);
    return new DrawingResult(path, null);
  }
}
