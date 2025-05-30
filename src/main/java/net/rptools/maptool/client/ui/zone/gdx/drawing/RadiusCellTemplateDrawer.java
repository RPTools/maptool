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
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.Pen;

public class RadiusCellTemplateDrawer extends AbstractTemplateDrawer {

  public RadiusCellTemplateDrawer(AreaRenderer renderer) {
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
    // Only squares w/in the radius
    int radius = template.getRadius();
    if (distance <= radius) {
      paintArea(batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
    }

    if (template.getDistance(x, y + 1) <= radius) {
      paintArea(batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
    }

    if (template.getDistance(x + 1, y) <= radius) {
      paintArea(batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
    }

    if (template.getDistance(x + 1, y + 1) <= radius) {
      paintArea(batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
    }
  }

  @Override
  protected void paintArea(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    ZonePoint vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
    int y = vertex.y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
    var floats =
        new float[] {
          x, -y - gridSize, x, -y, x + gridSize, -y, x + gridSize, -y - gridSize,
        };
    applyColor(pen.getBackgroundPaint(), true);
    areaRenderer.paintVertices(batch, floats, null);
  }

  @Override
  protected int getXMult(AbstractTemplate.Quadrant q) {
    return ((q == AbstractTemplate.Quadrant.NORTH_WEST || q == AbstractTemplate.Quadrant.SOUTH_WEST)
        ? -1
        : +1);
  }

  @Override
  protected int getYMult(AbstractTemplate.Quadrant q) {
    return ((q == AbstractTemplate.Quadrant.NORTH_WEST || q == AbstractTemplate.Quadrant.NORTH_EAST)
        ? -1
        : +1);
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
    paintBorderAtRadius(
        batch, pen, template, x, y, xOff, yOff, gridSize, distance, template.getRadius());
  }

  protected void paintBorderAtRadius(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance,
      int radius) {
    // At the border?
    // Paint lines between vertical boundaries if needed

    if (template.getDistance(x, y + 1) == radius && template.getDistance(x + 1, y + 1) > radius) {
      paintFarVerticalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
    }
    if (distance == radius && template.getDistance(x + 1, y) > radius) {
      paintFarVerticalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
    }
    if (template.getDistance(x + 1, y + 1) == radius
        && template.getDistance(x + 2, y + 1) > radius) {
      paintFarVerticalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
    }
    if (template.getDistance(x + 1, y) == radius && template.getDistance(x + 2, y) > radius) {
      paintFarVerticalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
    } // endif
    if (x == 0 && y + 1 == radius) {
      paintFarVerticalBorder(
          batch,
          pen,
          template,
          xOff - gridSize,
          yOff,
          gridSize,
          AbstractTemplate.Quadrant.SOUTH_EAST);
    }
    if (x == 0 && y + 2 == radius) {
      paintFarVerticalBorder(
          batch,
          pen,
          template,
          xOff - gridSize,
          yOff,
          gridSize,
          AbstractTemplate.Quadrant.NORTH_WEST);
    }

    // Paint lines between horizontal boundaries if needed
    if (template.getDistance(x, y + 1) == radius && template.getDistance(x, y + 2) > radius) {
      paintFarHorizontalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_EAST);
    }
    if (template.getDistance(x, y) == radius && template.getDistance(x, y + 1) > radius) {
      paintFarHorizontalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_EAST);
    }
    if (y == 0 && x + 1 == radius) {
      paintFarHorizontalBorder(
          batch,
          pen,
          template,
          xOff,
          yOff - gridSize,
          gridSize,
          AbstractTemplate.Quadrant.SOUTH_EAST);
    }
    if (y == 0 && x + 2 == radius) {
      paintFarHorizontalBorder(
          batch,
          pen,
          template,
          xOff,
          yOff - gridSize,
          gridSize,
          AbstractTemplate.Quadrant.NORTH_WEST);
    }
    if (template.getDistance(x + 1, y + 1) == radius
        && template.getDistance(x + 1, y + 2) > radius) {
      paintFarHorizontalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.NORTH_WEST);
    }
    if (template.getDistance(x + 1, y) == radius && template.getDistance(x + 1, y + 1) > radius) {
      paintFarHorizontalBorder(
          batch, pen, template, xOff, yOff, gridSize, AbstractTemplate.Quadrant.SOUTH_WEST);
    } // endif
  }

  @Override
  protected void paint(
      PolygonSpriteBatch batch,
      Pen pen,
      Zone zone,
      AbstractTemplate template,
      boolean border,
      boolean area) {
    int radius = template.getRadius();

    if (radius == 0) return;
    if (zone == null) return;

    // Find the proper distance
    int gridSize = zone.getGrid().getSize();
    for (int y = 0; y < radius; y++) {
      for (int x = 0; x < radius; x++) {

        // Get the offset to the corner of the square
        int xOff = x * gridSize;
        int yOff = y * gridSize;

        // Template specific painting
        if (border)
          paintBorder(batch, pen, template, x, y, xOff, yOff, gridSize, template.getDistance(x, y));
        if (area)
          paintArea(batch, pen, template, x, y, xOff, yOff, gridSize, template.getDistance(x, y));
      } // endfor
    } // endfor
  }
}
