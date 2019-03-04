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
package net.rptools.maptool.model.vision;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Vision;
import net.rptools.maptool.model.Zone;

public class BlockyRoundVision extends Vision {
  public BlockyRoundVision() {}

  public BlockyRoundVision(int distance) {
    setDistance(distance);
  }

  @Override
  protected Area createArea(Zone zone, Token token) {
    int size = getDistance() * getZonePointsPerCell(zone);
    Area area = drawCells(size, zone);
    return area;
  }

  @Override
  public Anchor getAnchor() {
    return Vision.Anchor.CORNER;
  }

  @Override
  public String toString() {
    return "Blocky Round";
  }

  private enum Quadrant {
    NE,
    NW,
    SE,
    SW
  }

  // TODO: Move this to a more generic location
  private static Area drawCells(int distance, Zone zone) {
    Area area = new Area();
    Area cellShape = new Area(zone.getGrid().getCellShape());
    Grid grid = zone.getGrid();

    int y = 1;
    int x = 1;

    while (true) {
      int cells = x + y;
      int mod = x < y ? x : y;
      int diag = (mod / 2) + (mod % 2);
      int totalDistance = grid.getSize() * (cells - diag);

      if (totalDistance <= distance) {
        drawCell(area, cellShape, grid, x, y, Quadrant.NE);
        drawCell(area, cellShape, grid, x, y, Quadrant.NW);
        drawCell(area, cellShape, grid, x, y, Quadrant.SE);
        drawCell(area, cellShape, grid, x, y, Quadrant.SW);
        x++;
      } else {
        if (x == 1) {
          break;
        }
        y++;
        x = 1;
      }
    }
    return area;
  }

  private static void drawCell(
      Area area, Area cellShape, Grid grid, int x, int y, Quadrant quadrant) {
    // Adjust the location of the cell based on the quadrant
    // these are based on the symmetry of the cells calculated in the SE quadrant
    switch (quadrant) {
      case NE:
        y = -y;
        x -= 1;
        break;
      case NW:
        x = -x;
        y = -y;
        break;
      case SE:
        x -= 1;
        y -= 1;
        break;
      case SW:
        x = -x;
        y -= 1;
        break;
    }
    AffineTransform af = new AffineTransform();
    af.translate(x * grid.getSize(), y * grid.getSize());
    cellShape.transform(af);
    area.add(cellShape);

    // Symmetry
    try {
      cellShape.transform(af.createInverse());
    } catch (NoninvertibleTransformException e) {
      e.printStackTrace();
    }
  }
}
