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
import java.awt.Rectangle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.ZonePoint;

/**
 * The cone template draws a highlight over all the squares effected from a specific spine. There
 * are 8 different directions from each spine.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class ConeTemplate extends RadiusTemplate {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /**
   * The dirction to paint. The ne,se,nw,sw paint a quadrant and the n,w,e,w paint along the spine
   * of the selected vertex. Saved as a string as a hack to get around the hessian library's problem
   * w/ serializing enumerations.
   */
  private String direction = Direction.SOUTH_EAST.name();

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the direction for this ConeTemplate.
   *
   * @return Returns the current value of direction.
   */
  public Direction getDirection() {
    if (direction == null) return null;
    return Direction.valueOf(direction);
  }

  /**
   * Set the value of direction for this ConeTemplate.
   *
   * @param direction The direction to draw the cone from the center vertex.
   */
  public void setDirection(Direction direction) {
    if (direction != null) this.direction = direction.name();
    else direction = null;
  }

  /**
   * Paint the border at a specific radius.
   *
   * @param g Where to paint
   * @param x Distance from vertex along X axis in cell coordinates.
   * @param y Distance from vertex along Y axis in cell coordinates.
   * @param xOff Distance from vertex along X axis in screen coordinates.
   * @param yOff Distance from vertex along Y axis in screen coordinates.
   * @param gridSize The size of one side of the grid in screen coordinates.
   * @param distance The distance in cells from the vertex to the cell which is offset from the
   *     vertex by <code>x</code> & <code>y</code>.
   * @param radius The radius where the border is painted.
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  protected void paintBorderAtRadius(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance, int radius) {
    // At the border?
    if (distance == radius) {

      // Paint lines between vertical boundaries if needed
      if (getDistance(x + 1, y) > radius) {
        if (getDirection() == Direction.SOUTH_EAST
            || (getDirection() == Direction.SOUTH && y >= x)
            || (getDirection() == Direction.EAST && x >= y))
          paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        if (getDirection() == Direction.NORTH_EAST
            || (getDirection() == Direction.NORTH && y >= x)
            || (getDirection() == Direction.EAST && x >= y))
          paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
        if (getDirection() == Direction.SOUTH_WEST
            || (getDirection() == Direction.SOUTH && y >= x)
            || (getDirection() == Direction.WEST && x >= y))
          paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
        if (getDirection() == Direction.NORTH_WEST
            || (getDirection() == Direction.NORTH && y >= x)
            || (getDirection() == Direction.WEST && x >= y))
          paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
      } // endif

      // Paint lines between horizontal boundaries if needed
      if (getDistance(x, y + 1) > radius) {
        if (getDirection() == Direction.SOUTH_EAST
            || (getDirection() == Direction.SOUTH && y >= x)
            || (getDirection() == Direction.EAST && x >= y))
          paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        if (getDirection() == Direction.SOUTH_WEST
            || (getDirection() == Direction.SOUTH && y >= x)
            || (getDirection() == Direction.WEST && x >= y))
          paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
        if (getDirection() == Direction.NORTH_EAST
            || (getDirection() == Direction.NORTH && y >= x)
            || (getDirection() == Direction.EAST && x >= y))
          paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
        if (getDirection() == Direction.NORTH_WEST
            || (getDirection() == Direction.NORTH && y >= x)
            || (getDirection() == Direction.WEST && x >= y))
          paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
      } // endif
    } // endif
  }

  /**
   * Paint the border at a specific radius.
   *
   * @param g Where to paint
   * @param x Distance from vertex along X axis in cell coordinates.
   * @param y Distance from vertex along Y axis in cell coordinates.
   * @param xOff Distance from vertex along X axis in screen coordinates.
   * @param yOff Distance from vertex along Y axis in screen coordinates.
   * @param gridSize The size of one side of the grid in screen coordinates.
   * @param distance The distance in cells from the vertex to the cell which is offset from the
   *     vertex by <code>x</code> & <code>y</code>.
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  protected void paintEdges(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {

    // Handle the edges
    int radius = getRadius();
    if (getDirection().ordinal() % 2 == 0) {
      if (x == 0) {
        if (getDirection() == Direction.SOUTH_EAST || getDirection() == Direction.SOUTH_WEST)
          paintCloseVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        if (getDirection() == Direction.NORTH_EAST || getDirection() == Direction.NORTH_WEST)
          paintCloseVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
      } // endif
      if (y == 0) {
        if (getDirection() == Direction.SOUTH_EAST || getDirection() == Direction.NORTH_EAST)
          paintCloseHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
        if (getDirection() == Direction.SOUTH_WEST || getDirection() == Direction.NORTH_WEST)
          paintCloseHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
      } // endif
    } else if (getDirection().ordinal() % 2 == 1 && x == y && distance <= radius) {
      if (getDirection() == Direction.SOUTH) {
        paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
        paintCloseHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        paintCloseHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
      } // endif
      if (getDirection() == Direction.NORTH) {
        paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
        paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
        paintCloseHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
        paintCloseHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
      } // endif
      if (getDirection() == Direction.EAST) {
        paintCloseVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        paintCloseVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
        paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
        paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
      } // endif
      if (getDirection() == Direction.WEST) {
        paintCloseVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
        paintCloseVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
        paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
        paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
      } // endif
    } // endif
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractTemplate Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  @Override
  protected void paintBorder(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    paintBorderAtRadius(g, x, y, xOff, yOff, gridSize, distance, getRadius());
    paintEdges(g, x, y, xOff, yOff, gridSize, distance);
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintArea(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  @Override
  protected void paintArea(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {

    // Drawing along the spines only?
    if ((getDirection() == Direction.EAST || getDirection() == Direction.WEST) && y > x) return;
    if ((getDirection() == Direction.NORTH || getDirection() == Direction.SOUTH) && x > y) return;

    // Only squares w/in the radius
    if (distance <= getRadius()) {

      // Paint the squares
      if (getDirection() == Direction.SOUTH_EAST
          || getDirection() == Direction.SOUTH
          || getDirection() == Direction.EAST)
        paintArea(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
      if (getDirection() == Direction.NORTH_EAST
          || getDirection() == Direction.NORTH
          || getDirection() == Direction.EAST)
        paintArea(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
      if (getDirection() == Direction.SOUTH_WEST
          || getDirection() == Direction.SOUTH
          || getDirection() == Direction.WEST)
        paintArea(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
      if (getDirection() == Direction.NORTH_WEST
          || getDirection() == Direction.NORTH
          || getDirection() == Direction.WEST)
        paintArea(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
    } // endif
  }

  /*---------------------------------------------------------------------------------------------
   * Drawable Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.model.drawing.Drawable#getBounds() */
  public Rectangle getBounds() {

    if (MapTool.getCampaign().getZone(getZoneId()) == null) {
      // How does this happen ?! Anyway, try to use the current zone (since that's what we're
      // drawing anyway, seems reasonable
      if (MapTool.getFrame().getCurrentZoneRenderer() == null) {
        // Wha?!
        return new Rectangle();
      }
      setZoneId(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId());
    }

    int gridSize = MapTool.getCampaign().getZone(getZoneId()).getGrid().getSize();
    int quadrantSize = getRadius() * gridSize + BOUNDS_PADDING;

    // Find the x,y loc
    ZonePoint vertex = getVertex();
    int x = vertex.x;
    if (getDirection() == Direction.NORTH_WEST
        || getDirection() == Direction.WEST
        || getDirection() == Direction.SOUTH_WEST
        || getDirection() == Direction.NORTH
        || getDirection() == Direction.SOUTH) {

      x -= quadrantSize;
    }

    int y = vertex.y;
    if (getDirection() == Direction.NORTH_WEST
        || getDirection() == Direction.NORTH
        || getDirection() == Direction.NORTH_EAST
        || getDirection() == Direction.EAST
        || getDirection() == Direction.WEST) {

      y -= quadrantSize;
    }

    // Find the width,height
    int width = quadrantSize + BOUNDS_PADDING;
    if (getDirection() == Direction.NORTH || getDirection() == Direction.SOUTH)
      width += quadrantSize;
    int height = quadrantSize + BOUNDS_PADDING;
    if (getDirection() == Direction.EAST || getDirection() == Direction.WEST)
      height += quadrantSize;
    return new Rectangle(x, y, width, height);
  }
}
