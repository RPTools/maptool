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
 * Draw an empty circle over a token.
 *
 * @author jgorrell
 */
public final class OTokenOverlay extends AbstractShapeTokenOverlay {

  /**
   * Create an O token overlay with the given name.
   *
   * @param name Name of this token overlay.
   * @param color The color of this token overlay.
   * @param strokeWidth The width of the lines in this token overlay.
   */
  public OTokenOverlay(String name, Color color, int strokeWidth) {
    super(name, color, strokeWidth);
  }

  public OTokenOverlay(OTokenOverlay other) {
    super(other);
  }

  @Override
  public OTokenOverlay clone() {
    return new OTokenOverlay(this);
  }

  @Override
  public Shape getShape(Rectangle2D bounds) {
    double offset = getStroke().getLineWidth() / 2.0;
    var path =
        new Ellipse2D.Double(
            bounds.getMinX() + offset,
            bounds.getMinY() + offset,
            bounds.getWidth() - 2 * offset,
            bounds.getHeight() - 2 * offset);
    return path;
  }
}
