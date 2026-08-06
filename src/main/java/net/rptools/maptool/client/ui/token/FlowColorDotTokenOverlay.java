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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Paint a dot so that it doesn't overlay any other states being displayed in the same grid.
 *
 * @author Jay
 */
public final class FlowColorDotTokenOverlay extends AbstractFlowShapeTokenOverlay {

  /**
   * Create a new dot token overlay
   *
   * @param name Name of the token overlay
   * @param color Color of the dot
   * @param gridSize Size of the overlay grid for this state. All states with the same grid size
   *     share the same overlay.
   */
  public FlowColorDotTokenOverlay(String name, Color color, int gridSize) {
    super(name, color, gridSize);
  }

  public FlowColorDotTokenOverlay(FlowColorDotTokenOverlay other) {
    super(other);
  }

  @Override
  public FlowColorDotTokenOverlay clone() {
    return new FlowColorDotTokenOverlay(this);
  }

  @Override
  public Shape getShape(Rectangle2D bounds) {
    return new Ellipse2D.Double(
        bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
  }
}
