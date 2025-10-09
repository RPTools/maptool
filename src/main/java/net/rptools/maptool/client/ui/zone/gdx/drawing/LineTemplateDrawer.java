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
import java.util.ListIterator;
import net.rptools.maptool.client.ui.zone.gdx.AreaRenderer;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.LineTemplate;
import net.rptools.maptool.model.drawing.Pen;

public class LineTemplateDrawer extends AbstractTemplateDrawer {
  public LineTemplateDrawer(AreaRenderer renderer) {
    super(renderer);
  }

  @Override
  protected void paint(
      PolygonSpriteBatch batch,
      Pen pen,
      Zone zone,
      AbstractTemplate template,
      boolean border,
      boolean area) {
    if (zone == null) {
      return;
    }
    var lineTemplate = (LineTemplate) template;

    // Need to paint? We need a line and to translate the painting
    if (lineTemplate.getPathVertex() == null) return;
    if (template.getRadius() == 0) return;
    if (lineTemplate.getPath() == null && lineTemplate.calcPath() == null) return;

    // Paint each element in the path
    int gridSize = zone.getGrid().getSize();
    ListIterator<CellPoint> i = lineTemplate.getPath().listIterator();
    while (i.hasNext()) {
      CellPoint p = i.next();
      int xOff = p.x * gridSize;
      int yOff = p.y * gridSize;
      int distance = template.getDistance(p.x, p.y);

      // Paint what is needed.
      if (area) {
        paintArea(batch, pen, template, p.x, p.y, xOff, yOff, gridSize, distance);
      } // endif
      if (border) {
        paintBorder(batch, pen, template, p.x, p.y, xOff, yOff, gridSize, i.previousIndex());
      } // endif
    } // endfor
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
    var lineTemplate = (LineTemplate) template;

    paintArea(batch, pen, template, xOff, yOff, gridSize, lineTemplate.getQuadrant());
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
      int pElement) {
    var lineTemplate = (LineTemplate) template;

    // Have to scan 3 points behind and ahead, since that is the maximum number of points
    // that can be added to the path from any single intersection.
    boolean[] noPaint = new boolean[4];
    var path = lineTemplate.getPath();
    for (int i = pElement - 3; i < pElement + 3; i++) {
      if (i < 0 || i >= path.size() || i == pElement) continue;
      CellPoint p = path.get(i);

      // Ignore diagonal cells and cells that are not adjacent
      int dx = p.x - x;
      int dy = p.y - y;
      if (Math.abs(dx) == Math.abs(dy) || Math.abs(dx) > 1 || Math.abs(dy) > 1) continue;

      // Remove the border between the 2 points
      noPaint[dx != 0 ? (dx < 0 ? 0 : 2) : (dy < 0 ? 3 : 1)] = true;
    } // endif

    var quadrant = lineTemplate.getQuadrant();
    // Paint the borders as needed
    if (!noPaint[0]) paintCloseVerticalBorder(batch, pen, template, xOff, yOff, gridSize, quadrant);
    if (!noPaint[1]) paintFarHorizontalBorder(batch, pen, template, xOff, yOff, gridSize, quadrant);
    if (!noPaint[2]) paintFarVerticalBorder(batch, pen, template, xOff, yOff, gridSize, quadrant);
    if (!noPaint[3])
      paintCloseHorizontalBorder(batch, pen, template, xOff, yOff, gridSize, quadrant);
  }
}
