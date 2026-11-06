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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import static net.rptools.lib.MathUtil.isEven;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents the set of cells a token occupies based on its size. Each token is assumed
 * to take up at least one cell, additional cells are indicated by cell offsets assuming the
 * occupied cell is at 0, 0
 */
public class TokenFootprint {
  private final GUID id;
  private final String name;
  private final double scale;
  private final String localizedName;
  private final Set<Point> cellSet = new HashSet<>();

  public TokenFootprint() {
    this(new GUID(), "", "", 1.0);
  }

  /**
   * Create a footprint with a list of constituent points, relative to {@code (0, 0)}.
   *
   * <p>The point {@code (0, 0)} is implicitly included.
   *
   * @param id The unique ID of the footprint.
   * @param name The unique name of the footprint.
   * @param localizedName A human-readable, translated name for displaying the footprint.
   * @param scale For footprints less than one grid cell, how much to scale the footprint relative
   *     to the cell size.
   * @param points The non-zero points making up the footprint.
   */
  public TokenFootprint(GUID id, String name, String localizedName, double scale, Point... points) {
    this.id = id;
    this.name = name;
    this.localizedName = Objects.requireNonNullElse(localizedName, name);
    this.scale = scale;
    this.cellSet.addAll(Arrays.asList(points));
  }

  @Override
  public String toString() {
    return getLocalizedName();
  }

  public Set<CellPoint> getOccupiedCells(CellPoint centerPoint, OffsetTranslator offsetTranslator) {
    var occupiedSet = new HashSet<CellPoint>();

    // Implied
    occupiedSet.add(centerPoint);

    // Relative
    for (Point offset : cellSet) {
      CellPoint cp = new CellPoint(centerPoint.x + offset.x, centerPoint.y + offset.y);
      cp = offsetTranslator.translate(centerPoint, cp);
      occupiedSet.add(cp);
    }
    return occupiedSet;
  }

  public GUID getId() {
    return id;
  }

  /** Returns the English name of the footprint */
  public String getName() {
    return name;
  }

  /** Returns the localized name of the footprint */
  public String getLocalizedName() {
    return localizedName;
  }

  public Rectangle getBounds(Grid grid) {
    return getBounds(grid, null);
  }

  /**
   * A scale factor to convert {@link #getBounds(Grid, CellPoint)} to actual footprint bounds.
   *
   * @return The footprint's scale factor.
   */
  public double getScale() {
    return scale;
  }

  /**
   * Return a rectangle that exactly bounds the footprint, values are in {@link ZonePoint} space.
   *
   * @param grid the {@link Grid} that the footprint corresponds to
   * @param cell origin cell of this footprint; <code>null</code> means that <code>(0,0)</code> will
   *     be used
   * @return the bounding rectangle that bounds the footprint
   */
  public Rectangle getBounds(Grid grid, CellPoint cell) {
    cell = cell != null ? cell : new CellPoint(0, 0);
    Rectangle bounds = new Rectangle(grid.getBounds(cell));

    for (CellPoint cp : getOccupiedCells(cell, grid.getType().getOffsetTranslator())) {
      bounds.add(grid.getBounds(cp));
    }

    return bounds;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TokenFootprint other)) {
      return false;
    }

    return name.equals(other.name) && scale == other.scale && cellSet.equals(other.cellSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, scale, cellSet);
  }

  public interface OffsetTranslator {
    CellPoint translate(CellPoint originPoint, CellPoint offsetPoint);

    OffsetTranslator IDENTITY = (originPoint, offsetPoint) -> offsetPoint;

    OffsetTranslator HEX_VERTICAL =
        (originPoint, offsetPoint) ->
            (isEven(originPoint.x) || !isEven(offsetPoint.x))
                ? offsetPoint
                : new CellPoint(offsetPoint.x, offsetPoint.y + 1);

    OffsetTranslator HEX_HORIZONTAL =
        (originPoint, offsetPoint) ->
            (isEven(originPoint.y) || !isEven(offsetPoint.y))
                ? offsetPoint
                : new CellPoint(offsetPoint.x + 1, offsetPoint.y);
  }
}
