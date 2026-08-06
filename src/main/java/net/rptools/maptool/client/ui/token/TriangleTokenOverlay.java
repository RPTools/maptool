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
package net.rptools.maptool.client.ui.token;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Place a Triangle (triangle point down) over a token.
 *
 * @author pwright
 * @version $Revision$ $Date$ $Author$
 */
public final class TriangleTokenOverlay extends AbstractShapeTokenOverlay {

  /**
   * Create a Triangle token overlay with the given name.
   *
   * @param name Name of this token overlay.
   * @param color The color of this token overlay.
   * @param strokeWidth The width of the lines in this token overlay.
   */
  public TriangleTokenOverlay(String name, Color color, int strokeWidth) {
    super(name, color, strokeWidth);
  }

  public TriangleTokenOverlay(TriangleTokenOverlay other) {
    super(other);
  }

  @Override
  public TriangleTokenOverlay clone() {
    return new TriangleTokenOverlay(this);
  }

  @Override
  public Shape getShape(Rectangle2D bounds) {
    var adjustY = bounds.getHeight() * 0.134;
    var path = new Path2D.Double();
    path.moveTo(bounds.getMinX(), bounds.getMaxY() - adjustY);
    path.lineTo(bounds.getCenterX(), bounds.getMinY());
    path.lineTo(bounds.getMaxX(), bounds.getMaxY() - adjustY);
    path.closePath();
    return path;
  }
}
