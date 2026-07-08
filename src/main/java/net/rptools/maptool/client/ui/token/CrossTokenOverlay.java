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
 * Place a cross over a token.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
public final class CrossTokenOverlay extends AbstractShapeTokenOverlay {

  /**
   * Create a Cross token overlay with the given name.
   *
   * @param name Name of this token overlay.
   * @param color The color of this token overlay.
   * @param strokeWidth The width of the lines in this token overlay.
   */
  public CrossTokenOverlay(String name, Color color, int strokeWidth) {
    super(name, color, strokeWidth);
  }

  public CrossTokenOverlay(CrossTokenOverlay other) {
    super(other);
  }

  @Override
  public CrossTokenOverlay clone() {
    return new CrossTokenOverlay(this);
  }

  @Override
  public Shape getShape(Rectangle2D bounds) {
    var path = new Path2D.Double();
    path.moveTo(bounds.getMinX(), bounds.getCenterY());
    path.lineTo(bounds.getMaxX(), bounds.getCenterY());
    path.moveTo(bounds.getCenterX(), bounds.getMinY());
    path.lineTo(bounds.getCenterX(), bounds.getMaxY());
    path.closePath();
    return path;
  }
}
