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

import java.util.ListIterator;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.LineCellTemplate;
import net.rptools.maptool.model.drawing.Pen;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class LineCellTemplateDrawer extends AbstractTemplateDrawer {

  public LineCellTemplateDrawer(ShapeDrawer drawer) {
    super(drawer);
  }

  @Override
  protected void paintArea(
      AbstractTemplate template, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    var lineCellTemplate = (LineCellTemplate) template;
    paintArea(template, xOff, yOff, gridSize, lineCellTemplate.getQuadrant());
  }

  @Override
  protected void paintBorder(
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int pElement) {
    var lineCellTemplate = (LineCellTemplate) template;
    // Have to scan 3 points behind and ahead, since that is the maximum number of points
    // that can be added to the path from any single intersection.
    boolean[] noPaint = new boolean[4];
    var path = lineCellTemplate.getPath();
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

    var quadrant = lineCellTemplate.getQuadrant();
    // Paint the borders as needed
    if (!noPaint[0]) paintCloseVerticalBorder(pen, template, xOff, yOff, gridSize, quadrant);
    if (!noPaint[1]) paintFarHorizontalBorder(pen, template, xOff, yOff, gridSize, quadrant);
    if (!noPaint[2]) paintFarVerticalBorder(pen, template, xOff, yOff, gridSize, quadrant);
    if (!noPaint[3]) paintCloseHorizontalBorder(pen, template, xOff, yOff, gridSize, quadrant);
  }

  @Override
  protected void paint(Pen pen, AbstractTemplate template, boolean border, boolean area) {
    if (MapTool.getCampaign().getZone(template.getZoneId()) == null) {
      return;
    }
    var lineCellTemplate = (LineCellTemplate) template;
    var path = lineCellTemplate.getPath();
    // Need to paint? We need a line and to translate the painting
    if (lineCellTemplate.getPathVertex() == null) return;
    if (lineCellTemplate.getRadius() == 0) return;
    if (path == null && lineCellTemplate.calcPath() == null) return;

    var quadrant = lineCellTemplate.getQuadrant();

    // Paint each element in the path
    int gridSize = MapTool.getCampaign().getZone(lineCellTemplate.getZoneId()).getGrid().getSize();
    ListIterator<CellPoint> i = path.listIterator();
    while (i.hasNext()) {
      CellPoint p = i.next();
      int xOff = p.x * gridSize;
      int yOff = p.y * gridSize;
      int distance = template.getDistance(p.x, p.y);

      if (quadrant.equals(AbstractTemplate.Quadrant.NORTH_EAST.name())) {
        yOff = yOff - gridSize;
      } else if (quadrant.equals(AbstractTemplate.Quadrant.SOUTH_WEST.name())) {
        xOff = xOff - gridSize;
      } else if (quadrant.equals(AbstractTemplate.Quadrant.NORTH_WEST.name())) {
        xOff = xOff - gridSize;
        yOff = yOff - gridSize;
      }

      // Paint what is needed.
      if (area) {
        paintArea(template, p.x, p.y, xOff, yOff, gridSize, distance);
      } // endif
      if (border) {
        paintBorder(pen, template, p.x, p.y, xOff, yOff, gridSize, i.previousIndex());
      } // endif
    } // endfor
  }
}
