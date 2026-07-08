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
 * Paint a square so that it doesn't overlay any other states being displayed in the same grid.
 *
 * @author Jay
 */
public final class FlowDiamondTokenOverlay extends AbstractFlowShapeTokenOverlay {

  /**
   * Create a new dot token overlay
   *
   * @param name Name of the token overlay
   * @param color Color of the dot
   * @param grid Size of the overlay grid for this state. All states with the same grid size share
   *     the same overlay.
   */
  public FlowDiamondTokenOverlay(String name, Color color, int grid) {
    super(name, color, grid);
  }

  public FlowDiamondTokenOverlay(FlowDiamondTokenOverlay other) {
    super(other);
  }

  @Override
  public FlowDiamondTokenOverlay clone() {
    return new FlowDiamondTokenOverlay(this);
  }

  @Override
  public Shape getShape(Rectangle2D bounds) {
    var path = new Path2D.Double();
    path.moveTo((float) bounds.getCenterX(), (float) bounds.getY());
    path.lineTo((float) bounds.getX(), (float) bounds.getCenterY());
    path.lineTo((float) bounds.getCenterX(), (float) bounds.getMaxY());
    path.lineTo((float) bounds.getMaxX(), (float) bounds.getCenterY());
    path.closePath();
    return path;
  }
}
