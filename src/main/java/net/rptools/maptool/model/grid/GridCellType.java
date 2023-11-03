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
package net.rptools.maptool.model.grid;

import static net.rptools.maptool.model.grid.GridUpdates.moderniseName;

import net.rptools.maptool.client.AppPreferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridCellType {
  protected static final Logger log = LogManager.getLogger();

  public GridCellType(double sizeInPixels, String gridTypeName) {
    String shortName = moderniseName(gridTypeName);
    setCellProperties(shortName);
    setPixelsPerCell(sizeInPixels);
    setPolygonProperties();
    log.info(polygonProperties.toString());
  }

  // A regular hexagon is one where all angles are 60 degrees.
  // the ratio = minor_radius / edge_length

  public CellProperties cellProperties = null;
  public PolygonProperties polygonProperties;
  public double pixelsPerCell = AppPreferences.getDefaultGridSize();

  public CellProperties getCellProperties() {
    return this.cellProperties;
  }

  public void setCellProperties(String typeName) {
    switch (GridUpdates.moderniseName(typeName)) {
      case "GRIDLESS" -> this.cellProperties = CellProperties.GRIDLESS;
      case "HEX_V" -> this.cellProperties = CellProperties.HEX_V;
      case "HEX_H" -> this.cellProperties = CellProperties.HEX_H;
      case "SQUARE" -> this.cellProperties = CellProperties.SQUARE;
      case "ISOMETRIC" -> this.cellProperties = CellProperties.ISOMETRIC_SQUARE;
      default -> throw new AssertionError("Switch failed: setCellProperties - " + typeName);
    }
  }

  public double getPixelsPerCell() {
    return pixelsPerCell;
  }

  public void setPixelsPerCell(double pixelsPerCell_) {
    if (pixelsPerCell != pixelsPerCell_) {
      pixelsPerCell = pixelsPerCell_;
      if (cellProperties != null) setPolygonProperties();
    }
  }

  public void setPolygonProperties() {
    switch (cellProperties) {
      case GRIDLESS, NONE, UNSET -> polygonProperties = new PolygonProperties(0);
      case HEX_H -> polygonProperties = new PolygonProperties(6, pixelsPerCell, 30);
      case HEX_V -> polygonProperties = new PolygonProperties(6, pixelsPerCell);
      case ISOMETRIC_HEX -> polygonProperties =
          new PolygonProperties(6, pixelsPerCell).isometricise(15.0);
      case ISOMETRIC_SQUARE -> polygonProperties =
          new PolygonProperties(4, pixelsPerCell).isometricise();
      case ISOMETRIC_TRIANGLE -> polygonProperties = new PolygonProperties(3).isometricise();
      case SQUARE -> polygonProperties =
          new PolygonProperties(4, pixelsPerCell, 45)
              .offsetCentre(new double[] {pixelsPerCell / 2.0, pixelsPerCell / 2.0});
      case TRIANGLE -> polygonProperties = new PolygonProperties(3, pixelsPerCell);
      default -> throw new IllegalStateException("Unexpected value: " + this.cellProperties);
    }
  }

  enum CellProperties {
    NONE(0, false, false, 0.0, "None", "NONE"),
    GRIDLESS(1, false, false, 0.0, "Gridless", "GRIDLESS"),
    UNSET(2, false, false, 0.0, "Not set", "TYPE_NOT_SET"),
    HEX_V(3, false, false, 1.0, "Vertical Hex", "HEX_VERT"),
    HEX_H(4, false, true, 1.0, "Horizontal Hex", "HEX_HORI"),
    SQUARE(5, false, false, 1.0, "Square", "SQUARE"),
    TRIANGLE(6, false, false, 1.0, "Triangle", "n/a"),
    ISOMETRIC_HEX(7, true, false, 1.0, "Isometric Hex", "n/a"),
    ISOMETRIC_SQUARE(8, true, false, 1.0, "Isometric Square", "ISOMETRIC"),
    ISOMETRIC_TRIANGLE(9, true, false, 1.0, "Isometric Triangle", "n/a");
    int index;
    boolean isometric;
    boolean horizontal;
    double aspectRatio;
    String friendlyKey;
    String oldName;

    CellProperties(
        int index,
        boolean isometric,
        boolean horizontal,
        Double aspectRatio,
        String friendlyKey,
        String oldName) {
      this.index = index;
      this.isometric = isometric;
      this.horizontal = horizontal;
      this.aspectRatio = aspectRatio;
      this.friendlyKey = friendlyKey;
      this.oldName = oldName;
    }

    CellProperties(int index_) {
      this.index = index_;
    }

    CellProperties(String oldName_) {
      this.oldName = oldName_;
    }

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public boolean isIsometric() {
      return isometric;
    }

    public void setIsometric(boolean isometric) {
      this.isometric = isometric;
    }

    public boolean isHorizontal() {
      return horizontal;
    }

    public void setHorizontal(boolean horizontal) {
      this.horizontal = horizontal;
    }

    public double getAspectRatio() {
      return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
      this.aspectRatio = aspectRatio;
    }

    public String getFriendlyKey() {
      return friendlyKey;
    }

    public void setFriendlyKey(String friendlyKey) {
      this.friendlyKey = friendlyKey;
    }

    public String getOldName() {
      return oldName;
    }

    public void setOldName(String oldName) {
      this.oldName = oldName;
    }

    @Override
    public String toString() {
      return this.name()
          + " -> "
          + "index: "
          + index
          + ", isometric: "
          + isometric
          + ", horizontal: "
          + horizontal
          + ", aspectRatio: "
          + aspectRatio
          + ", friendlyKey: "
          + friendlyKey
          + ", oldName: "
          + oldName;
    }
  }
}
