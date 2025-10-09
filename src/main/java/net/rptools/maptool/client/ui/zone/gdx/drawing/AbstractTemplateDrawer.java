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
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;

public abstract class AbstractTemplateDrawer extends AbstractDrawingDrawer {

  public AbstractTemplateDrawer(AreaRenderer renderer) {
    super(renderer);
  }

  @Override
  protected void drawBackground(PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen) {
    alpha = AbstractTemplate.DEFAULT_BG_ALPHA;
    paint(batch, pen, zone, (AbstractTemplate) element, false, true);
  }

  @Override
  protected void drawBorder(PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen) {
    paint(batch, pen, zone, (AbstractTemplate) element, true, false);
  }

  protected void paint(
      PolygonSpriteBatch batch,
      Pen pen,
      Zone zone,
      AbstractTemplate template,
      boolean border,
      boolean area) {
    var radius = template.getRadius();

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

  protected void paintArea(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
    int y = vertex.y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
    var floats =
        new float[] {x, -y - gridSize, x, -y, x + gridSize, -y, x + gridSize, -y - gridSize};
    applyColor(pen.getBackgroundPaint(), true);
    areaRenderer.paintVertices(batch, floats, null);
  }

  protected int getXMult(AbstractTemplate.Quadrant q) {
    return ((q == AbstractTemplate.Quadrant.NORTH_WEST || q == AbstractTemplate.Quadrant.SOUTH_WEST)
        ? -1
        : +1);
  }

  protected int getYMult(AbstractTemplate.Quadrant q) {
    return ((q == AbstractTemplate.Quadrant.NORTH_WEST || q == AbstractTemplate.Quadrant.NORTH_EAST)
        ? -1
        : +1);
  }

  protected void paintCloseVerticalBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff;
    line(batch, pen, x, y, x, y + getYMult(q) * gridSize);
  }

  protected void paintFarHorizontalBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff + getYMult(q) * gridSize;
    line(batch, pen, x, y, x + getXMult(q) * gridSize, y);
  }

  protected void paintFarVerticalBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff + getXMult(q) * gridSize;
    int y = vertex.y + getYMult(q) * yOff;
    line(batch, pen, x, y, x, y + getYMult(q) * gridSize);
  }

  protected void paintCloseHorizontalBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff;
    line(batch, pen, x, y, x + getXMult(q) * gridSize, y);
  }

  protected abstract void paintArea(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance);

  protected abstract void paintBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance);
}
