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
package net.rptools.maptool.client.ui.zone.gdx.drawing;

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import net.rptools.maptool.client.ui.zone.gdx.AreaRenderer;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.ConeTemplate;
import net.rptools.maptool.model.drawing.Pen;

public class ConeTemplateDrawer extends RadiusTemplateDrawer {

  public ConeTemplateDrawer(AreaRenderer renderer) {
    super(renderer);
  }

  @Override
  protected void paintArea(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance) {
    var coneTemplate = (ConeTemplate) template;

    var direction = coneTemplate.getDirection();

    // Drawing along the spines only?
    if ((direction == AbstractTemplate.Direction.EAST
            || direction == AbstractTemplate.Direction.WEST)
        && y > x) return;
    if ((direction == AbstractTemplate.Direction.NORTH
            || direction == AbstractTemplate.Direction.SOUTH)
        && x > y) return;

    // Only squares w/in the radius
    if (distance > coneTemplate.getRadius()) {
      return;
    }
    for (AbstractTemplate.Quadrant q : AbstractTemplate.Quadrant.values()) {
      if (coneTemplate.withinQuadrant(q)) {
        paintArea(batch, pen, template, xOff, yOff, gridSize, q);
      }
    }
  }

  @Override
  protected void paintBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance) {
    var coneTemplate = (ConeTemplate) template;
    paintBorderAtRadius(
        batch, pen, coneTemplate, x, y, xOff, yOff, gridSize, distance, coneTemplate.getRadius());
    paintEdges(batch, pen, coneTemplate, x, y, xOff, yOff, gridSize, distance);
  }

  protected void paintEdges(
      PolygonSpriteBatch batch,
      Pen pen,
      ConeTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance) {

    // Handle the edges
    int radius = template.getRadius();
    var direction = template.getDirection();
    if (direction.ordinal() % 2 == 0) {
      if (x == 0) {
        if (direction == AbstractTemplate.Direction.SOUTH_EAST
            || direction == AbstractTemplate.Direction.SOUTH_WEST)
          paintCloseVerticalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        if (direction == AbstractTemplate.Direction.NORTH_EAST
            || direction == AbstractTemplate.Direction.NORTH_WEST)
          paintCloseVerticalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
      } // endif
      if (y == 0) {
        if (direction == AbstractTemplate.Direction.SOUTH_EAST
            || direction == AbstractTemplate.Direction.NORTH_EAST)
          paintCloseHorizontalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
        if (direction == AbstractTemplate.Direction.SOUTH_WEST
            || direction == AbstractTemplate.Direction.NORTH_WEST)
          paintCloseHorizontalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
      } // endif
    } else if (direction.ordinal() % 2 == 1 && x == y && distance <= radius) {
      if (direction == AbstractTemplate.Direction.SOUTH) {
        paintFarVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        paintFarVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
        paintCloseHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        paintCloseHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
      } // endif
      if (direction == AbstractTemplate.Direction.NORTH) {
        paintFarVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
        paintFarVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
        paintCloseHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
        paintCloseHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
      } // endif
      if (direction == AbstractTemplate.Direction.EAST) {
        paintCloseVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        paintCloseVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
        paintFarHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        paintFarHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
      } // endif
      if (direction == AbstractTemplate.Direction.WEST) {
        paintCloseVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
        paintCloseVerticalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
        paintFarHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
        paintFarHorizontalBorder(
            batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
      } // endif
    } // endif
  }

  protected void paintBorderAtRadius(
      PolygonSpriteBatch batch,
      Pen pen,
      ConeTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance,
      int radius) {
    // At the border?
    if (distance == radius) {
      var direction = template.getDirection();
      // Paint lines between vertical boundaries if needed
      if (template.getDistance(x + 1, y) > radius) {
        if (direction == AbstractTemplate.Direction.SOUTH_EAST
            || (direction == AbstractTemplate.Direction.SOUTH && y >= x)
            || (direction == AbstractTemplate.Direction.EAST && x >= y))
          paintFarVerticalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        if (direction == AbstractTemplate.Direction.NORTH_EAST
            || (direction == AbstractTemplate.Direction.NORTH && y >= x)
            || (direction == AbstractTemplate.Direction.EAST && x >= y))
          paintFarVerticalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
        if (direction == AbstractTemplate.Direction.SOUTH_WEST
            || (direction == AbstractTemplate.Direction.SOUTH && y >= x)
            || (direction == AbstractTemplate.Direction.WEST && x >= y))
          paintFarVerticalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
        if (direction == AbstractTemplate.Direction.NORTH_WEST
            || (direction == AbstractTemplate.Direction.NORTH && y >= x)
            || (direction == AbstractTemplate.Direction.WEST && x >= y))
          paintFarVerticalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
      } // endif

      // Paint lines between horizontal boundaries if needed
      if (template.getDistance(x, y + 1) > radius) {
        if (direction == AbstractTemplate.Direction.SOUTH_EAST
            || (direction == AbstractTemplate.Direction.SOUTH && y >= x)
            || (direction == AbstractTemplate.Direction.EAST && x >= y))
          paintFarHorizontalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
        if (direction == AbstractTemplate.Direction.SOUTH_WEST
            || (direction == AbstractTemplate.Direction.SOUTH && y >= x)
            || (direction == AbstractTemplate.Direction.WEST && x >= y))
          paintFarHorizontalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
        if (direction == AbstractTemplate.Direction.NORTH_EAST
            || (direction == AbstractTemplate.Direction.NORTH && y >= x)
            || (direction == AbstractTemplate.Direction.EAST && x >= y))
          paintFarHorizontalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
        if (direction == AbstractTemplate.Direction.NORTH_WEST
            || (direction == AbstractTemplate.Direction.NORTH && y >= x)
            || (direction == AbstractTemplate.Direction.WEST && x >= y))
          paintFarHorizontalBorder(
              batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
      } // endif
    } // endif
  }
}
