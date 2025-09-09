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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.annotation.Nullable;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.GraphicsUtil;

public class OvalStrategy implements Strategy<ZonePoint> {
  private final int steps;

  public OvalStrategy() {
    this(0);
  }

  public OvalStrategy(int steps) {
    this.steps = steps;
  }

  @Override
  public ZonePoint startNewAtPoint(ZonePoint point) {
    return point;
  }

  @Override
  public @Nullable DrawingResult getShape(
      ZonePoint state, ZonePoint currentPoint, boolean centerOnOrigin, boolean isFilled) {
    var bounds = Strategy.normalizedRectangle(state, currentPoint, centerOnOrigin);
    if (bounds.width == 0 && bounds.height == 0) {
      return null;
    }

    Shape shape;
    if (steps <= 0) {
      shape = new Ellipse2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
    } else {
      shape =
          GraphicsUtil.createLineSegmentEllipsePath(
              bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, steps);
    }

    return new DrawingResult(shape, new Measurement.Rectangular(bounds));
  }
}
