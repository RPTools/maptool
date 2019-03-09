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
package net.rptools.maptool.model.drawing;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;

/** An rectangle */
public class ShapeDrawable extends AbstractDrawing {
  private final Shape shape;
  private final boolean useAntiAliasing;

  public ShapeDrawable(Shape shape, boolean useAntiAliasing) {
    this.shape = shape;
    this.useAntiAliasing = useAntiAliasing;
  }

  public ShapeDrawable(Shape shape) {
    this(shape, true);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
   */
  public java.awt.Rectangle getBounds() {
    return shape.getBounds();
  }

  public Area getArea() {
    return new Area(shape);
  }

  @Override
  protected void draw(Graphics2D g) {
    Object oldAA = applyAA(g);
    g.draw(shape);
    restoreAA(g, oldAA);
  }

  @Override
  protected void drawBackground(Graphics2D g) {
    Object oldAA = applyAA(g);
    g.fill(shape);
    restoreAA(g, oldAA);
  }

  public Shape getShape() {
    return shape;
  }

  private Object applyAA(Graphics2D g) {
    Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        useAntiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    return oldAA;
  }

  private void restoreAA(Graphics2D g, Object oldAA) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
  }
}
