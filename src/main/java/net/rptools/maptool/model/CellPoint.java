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
package net.rptools.maptool.model;

import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;

/**
 * This class represents a location based on the grid coordinates of a zone.
 *
 * <p>They can be converted to screen coordinates by calling {@link #convertToScreen(ZoneRenderer)}.
 *
 * <p>They can be converted to ZonePoints by calling {@link Grid#convert(CellPoint)}.
 *
 * @author trevor
 */
public final class CellPoint extends AbstractPoint {

  public double distanceTraveled; // Only populated by AStarWalker classes to be used upstream
  public double
      distanceTraveledWithoutTerrain; // Only populated by AStarWalker classes to be used upstream
  private boolean isAStarCanceled = false;

  public CellPoint(int x, int y) {
    super(x, y);
  }

  public CellPoint(int x, int y, double distanceTraveled, double distanceTraveledWithoutTerrain) {
    super(x, y);
    this.distanceTraveled = distanceTraveled;
    this.distanceTraveledWithoutTerrain = distanceTraveledWithoutTerrain;
  }

  public CellPoint(CellPoint other) {
    this(other.x, other.y);
  }

  @Override
  public String toString() {
    return "CellPoint" + super.toString();
  }

  public boolean isAStarCanceled() {
    return isAStarCanceled;
  }

  public void setAStarCanceled(boolean aStarCanceled) {
    this.isAStarCanceled = aStarCanceled;
  }

  /**
   * Find the screen coordinates of the upper left hand corner of a cell taking into account scaling
   * and translation. <b>This code does not call {@link Grid#getCellOffset()}, which might be
   * appropriate in some circumstances.</b>
   *
   * @param renderer This renderer provides scaling
   * @return The screen coordinates of the upper left hand corner in the passed point or in a new
   *     point.
   */
  public ScreenPoint convertToScreen(ZoneRenderer renderer) {
    double scale = renderer.getScale();
    Zone zone = renderer.getZone();

    Grid grid = zone.getGrid();
    ZonePoint zp = grid.convert(this);

    int sx = renderer.getViewOffsetX() + (int) (zp.x * scale);
    int sy = renderer.getViewOffsetY() + (int) (zp.y * scale);

    return new ScreenPoint(sx, sy);
  }

  public ZonePoint convertToZonePoint(Grid grid) {
    return grid.convert(this);
  }

  public ZonePoint offsetZonePoint(Grid grid, double offsetX, double offsetY) {
    ZonePoint zp = convertToZonePoint(grid);
    offsetX += 1;
    offsetY += 1;

    zp.x = (int) (zp.x + (grid.getCellWidth() / 2) * offsetX);
    zp.y = (int) (zp.y + (grid.getCellWidth() / 2) * offsetY);

    return zp;
  }

  public ZonePoint midZonePoint(Grid grid, CellPoint other) {
    return this.offsetZonePoint(grid, other.x - this.x, other.y - this.y);
  }

  /**
   * Return distance in grid units for current map.
   *
   * @param zone the zone to calculate the distance on
   * @return the distance traveled, rounded and taking into account the Units per Cell
   */
  public double getDistanceTraveled(Zone zone) {
    switch (zone.getAStarRounding()) {
      case CELL_UNIT:
        return roundToCellCost(distanceTraveled * zone.getUnitsPerCell(), zone.getUnitsPerCell());
      case INTEGER:
        return (int) distanceTraveled * zone.getUnitsPerCell();
      case NONE:
      default:
        return distanceTraveled * zone.getUnitsPerCell();
    }
  }

  public double getDistanceTraveledWithoutTerrain() {
    return distanceTraveledWithoutTerrain;
  }

  private double roundToCellCost(double num, double unitsPerCell) {
    if (num == 0) {
      return 0;
    } else {
      double result = Math.floor((num + unitsPerCell / 2) / unitsPerCell) * unitsPerCell;
      return Math.max(result, unitsPerCell);
    }
  }
}
