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
public final class FlowYieldTokenOverlay extends AbstractFlowShapeTokenOverlay {

  /**
   * Create a new dot token overlay
   *
   * @param name Name of the token overlay
   * @param color Color of the dot
   * @param gridSize Size of the overlay grid for this state. All states with the same grid size
   *     share the same overlay.
   */
  public FlowYieldTokenOverlay(String name, Color color, int gridSize) {
    super(name, color, gridSize);
  }

  public FlowYieldTokenOverlay(FlowYieldTokenOverlay other) {
    super(other);
  }

  @Override
  public FlowYieldTokenOverlay clone() {
    return new FlowYieldTokenOverlay(this);
  }

  @Override
  public Shape getShape(Rectangle2D r) {
    var path = new Path2D.Double();
    path.moveTo((float) r.getX(), (float) r.getY());
    path.lineTo((float) r.getCenterX(), (float) r.getMaxY());
    path.lineTo((float) r.getMaxX(), (float) r.getY());
    path.lineTo((float) r.getX(), (float) r.getY());
    path.closePath();
    return path;
  }
}
