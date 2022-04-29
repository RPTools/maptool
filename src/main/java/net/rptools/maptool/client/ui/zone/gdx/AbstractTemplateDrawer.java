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
package net.rptools.maptool.client.ui.zone.gdx;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class AbstractTemplateDrawer extends AbstractDrawingDrawer {

  public AbstractTemplateDrawer(ShapeDrawer drawer) {
    super(drawer);
  }

  @Override
  protected void drawBackground(Drawable element, Pen pen) {
    tmpColor.set(tmpColor.r, tmpColor.g, tmpColor.b, AbstractTemplate.DEFAULT_BG_ALPHA);
    drawer.setColor(tmpColor);
    paint(pen, (AbstractTemplate) element, false, true);
  }

  @Override
  protected void drawBorder(Drawable element, Pen pen) {
    paint(pen, (AbstractTemplate) element, true, false);
  }

  protected void paint(Pen pen, AbstractTemplate template, boolean border, boolean area) {
    var radius = template.getRadius();

    if (radius == 0) return;
    Zone zone = MapTool.getCampaign().getZone(template.getZoneId());
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
          paintBorder(pen, template, x, y, xOff, yOff, gridSize, template.getDistance(x, y));
        if (area) paintArea(template, x, y, xOff, yOff, gridSize, template.getDistance(x, y));
      } // endfor
    } // endfor
  }

  protected void paintArea(
      AbstractTemplate template, int xOff, int yOff, int gridSize, AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
    int y = vertex.y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
    drawer.filledRectangle(x, -y - gridSize, gridSize, gridSize);
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
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff;
    line(pen, x, y, x, y + getYMult(q) * gridSize);
  }

  protected void paintFarHorizontalBorder(
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff + getYMult(q) * gridSize;
    line(pen, x, y, x + getXMult(q) * gridSize, y);
  }

  protected void paintFarVerticalBorder(
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff + getXMult(q) * gridSize;
    int y = vertex.y + getYMult(q) * yOff;
    line(pen, x, y, x, y + getYMult(q) * gridSize);
  }

  protected void paintCloseHorizontalBorder(
      Pen pen,
      AbstractTemplate template,
      int xOff,
      int yOff,
      int gridSize,
      AbstractTemplate.Quadrant q) {
    var vertex = template.getVertex();
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff;
    line(pen, x, y, x + getXMult(q) * gridSize, y);
  }

  protected abstract void paintArea(
      AbstractTemplate template, int x, int y, int xOff, int yOff, int gridSize, int distance);

  protected abstract void paintBorder(
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance);
}
