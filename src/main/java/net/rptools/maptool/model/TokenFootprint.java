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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the set of cells a token occupies based on its size. Each token is assumed
 * to take up at least one cell, additional cells are indicated by cell offsets assuming the
 * occupied cell is at 0, 0
 */
public class TokenFootprint {
  private final Set<Point> cellSet = new HashSet<Point>();

  private String name;
  private GUID id;
  private boolean isDefault;
  private double scale = 1;

  private transient List<OffsetTranslator> translatorList = new LinkedList<OffsetTranslator>();

  public TokenFootprint() {
    // for serialization
  }

  public TokenFootprint(String name, boolean isDefault, double scale, Point... points) {
    this.name = name;
    id = new GUID();
    this.isDefault = isDefault;
    this.scale = scale;
    for (Point p : points) {
      cellSet.add(p);
    }
  }

  @Override
  public String toString() {
    return name;
  }

  public void addOffsetTranslator(OffsetTranslator translator) {
    translatorList.add(translator);
  }

  public Set<CellPoint> getOccupiedCells(CellPoint centerPoint) {
    Set<CellPoint> occupiedSet = new HashSet<CellPoint>();

    // Implied
    occupiedSet.add(centerPoint);

    // Relative
    for (Point offset : cellSet) {
      CellPoint cp = new CellPoint(centerPoint.x + offset.x, centerPoint.y + offset.y);
      for (OffsetTranslator translator : translatorList) {
        translator.translate(centerPoint, cp);
      }
      occupiedSet.add(cp);
    }
    return occupiedSet;
  }

  public TokenFootprint(String name, Point... points) {
    this(name, false, 1, points);
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public GUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Rectangle getBounds(Grid grid) {
    return getBounds(grid, null);
  }

  public double getScale() {
    return scale;
  }

  /**
   * Return a rectangle that exactly bounds the footprint, values are in {@link ZonePoint} space.
   *
   * @param grid the {@link Grid} that the footprint corresponds to
   * @param cell origin cell of this footprint; <code>null</code> means that <code>(0,0)</code> will
   *     be used
   */
  public Rectangle getBounds(Grid grid, CellPoint cell) {
    cell = cell != null ? cell : new CellPoint(0, 0);
    Rectangle bounds = new Rectangle(grid.getBounds(cell));

    for (CellPoint cp : getOccupiedCells(cell)) {
      bounds.add(grid.getBounds(cp));
    }
    bounds.x += grid.getOffsetX();
    bounds.y += grid.getOffsetY();
    return bounds;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TokenFootprint)) {
      return false;
    }
    return ((TokenFootprint) obj).id.equals(id);
  }

  private Object readResolve() {
    translatorList = new LinkedList<OffsetTranslator>();
    return this;
  }

  public static interface OffsetTranslator {
    public void translate(CellPoint originPoint, CellPoint offsetPoint);
  }
}
