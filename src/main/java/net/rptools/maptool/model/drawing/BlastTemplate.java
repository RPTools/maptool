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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.ZonePoint;

/**
 * The blast template draws a square for D&D 4e
 *
 * @author jgorrell
 * @version $Revision: $ $Date: $ $Author: $
 */
public class BlastTemplate extends ConeTemplate {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Renderer for the blast. The {@link Shape} is just a rectangle. */
  private ShapeDrawable renderer = new ShapeDrawable(new Rectangle());

  private int offsetX;
  private int offsetY;

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * This methods adjusts the rectangle in the renderer to match the new radius, vertex, or
   * location.
   */
  private void adjustRectangle() {
    if (getZoneId() == null) return;
    int gridSize = MapTool.getCampaign().getZone(getZoneId()).getGrid().getSize();
    int size = getRadius() * gridSize;

    Rectangle r = (Rectangle) renderer.getShape();
    r.setBounds(getVertex().x, getVertex().y, size, size);

    r.x += offsetX * gridSize;
    r.y += offsetY * gridSize;
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden *Template Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Rectangle getBounds() {
    Rectangle r = new Rectangle(renderer.getShape().getBounds());
    // We don't know pen width, so add some padding to account for it
    r.x -= 5;
    r.y -= 5;
    r.width += 10;
    r.height += 10;
    return r;
  }

  /**
   * Defines the blast based on the specified square
   *
   * @param relX The X coordinate of the control square relative to the origin square
   * @param relY The Y coordinate of the control square relative to the origin square
   */
  public void setControlCellRelative(int relX, int relY) {

    relX = Math.max(Math.min(relX, MAX_RADIUS), -MAX_RADIUS);
    relY = Math.max(Math.min(relY, MAX_RADIUS), -MAX_RADIUS);

    int radius = Math.max(Math.abs(relX), Math.abs(relY));
    // Number of cells along axis of smaller offset we need to shift the square in order to "center"
    // the blast
    int centerOffset = -(radius / 2);
    // Smallest delta we can apply to centerOffset and still have valid placement
    int lowerBound = -((radius + 1) / 2);
    // Largest delta we can apply to centerOffset and still have valid placement
    int upperBound = (radius / 2) + 1;

    setRadius(radius);
    // The larger magnitude offset determines size and gross positioning, the smaller determines
    // fine positioning
    if (Math.abs(relX) > Math.abs(relY)) {
      if (relX > 0) {
        offsetX = 1;
      } else {
        offsetX = -radius;
      }
      offsetY = centerOffset + Math.min(Math.max(lowerBound, relY), upperBound);
    } else {
      if (relY > 0) {
        offsetY = 1;
      } else {
        offsetY = -radius;
      }
      offsetX = centerOffset + Math.min(Math.max(lowerBound, relX), upperBound);
    }
    adjustRectangle();
  }

  /**
   * @see
   *     net.rptools.maptool.model.drawing.AbstractTemplate#setVertex(net.rptools.maptool.model.ZonePoint)
   */
  @Override
  public void setVertex(ZonePoint vertex) {
    super.setVertex(vertex);
    adjustRectangle();
  }

  /** @see net.rptools.maptool.model.drawing.AbstractTemplate#getDistance(int, int) */
  @Override
  public int getDistance(int x, int y) {
    return Math.max(x, y);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractDrawing Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.model.drawing.AbstractDrawing#draw(java.awt.Graphics2D) */
  @Override
  protected void draw(Graphics2D g) {
    renderer.draw(g);
  }

  /** @see net.rptools.maptool.model.drawing.AbstractDrawing#drawBackground(java.awt.Graphics2D) */
  @Override
  protected void drawBackground(Graphics2D g) {
    Composite old = g.getComposite();
    if (old != AlphaComposite.Clear)
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DEFAULT_BG_ALPHA));
    renderer.drawBackground(g);
    g.setComposite(old);
  }
}
