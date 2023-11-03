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

import net.rptools.maptool.client.AppPreferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Given a string describing the type of desired grid, this factory creates and returns an object of
 * the appropriate type.
 *
 * <p>(Ugh. This really should use an SPI-like factory interface.)
 */
public class GridFactory {
  protected static final Logger log = LogManager.getLogger();
  public static final String HEX_VERT = "Vertical Hex";
  public static final String HEX_HORI = "Horizontal Hex";
  public static final String SQUARE = "Square";
  public static final String ISOMETRIC = "Isometric";
  public static final String ISOMETRIC_HEX = "Isometric Hex";
  public static final String NONE = "None";

  public static Grid createGrid(String type) {
    return createGrid(type, true, false);
  }

  public static Grid createGrid(
      String type, int gridSize, boolean faceEdges, boolean faceVertices) {
    log.info(type);

    Grid tmpGrid = null;
    switch (GridUpdates.moderniseName(type)) {
      case "GRIDLESS" -> tmpGrid = new GridlessGrid();
      case "HEX_H" -> tmpGrid = new HexGridHorizontal(faceEdges, faceVertices);
      case "HEX_V" -> tmpGrid = new HexGridVertical(faceEdges, faceVertices);
      case "ISOMETRIC_HEX" -> tmpGrid = new GridlessGrid();
      case "ISOMETRIC_SQUARE" -> tmpGrid = new IsometricGrid(faceEdges, faceVertices);
      case "ISOMETRIC_TRIANGLE" -> new HexGridVertical(faceEdges, faceVertices);
      case "SQUARE" -> tmpGrid = new SquareGrid(faceEdges, faceVertices);
      case "TRIANGLE" -> tmpGrid = new HexGridVertical(faceEdges, faceVertices);
    }
    GridCellType cellType = new GridCellType(AppPreferences.getDefaultGridSize(), type);
    if (tmpGrid != null) tmpGrid.setCellType(cellType);
    return tmpGrid;
  }

  public static Grid createGrid(String type, boolean faceEdges, boolean faceVertices) {
    Grid tmpGrid;
    String tmpName;
    if (isHexVertical(type)) {
      tmpGrid = new HexGridVertical(faceEdges, faceVertices);
      tmpName = HEX_VERT;
    } else if (isHexHorizontal(type)) {
      tmpGrid = new HexGridHorizontal(faceEdges, faceVertices);
      tmpName = HEX_HORI;
    } else if (isSquare(type)) {
      tmpGrid = new SquareGrid(faceEdges, faceVertices);
      tmpName = SQUARE;
    } else if (isIsometric(type)) {
      tmpGrid = new IsometricGrid(faceEdges, faceVertices);
      tmpName = ISOMETRIC;
    } else if (isNone(type)) {
      tmpGrid = new GridlessGrid();
      tmpName = NONE;
    } else {
      throw new IllegalArgumentException("Unknown grid type: " + type);
    }
    tmpGrid.setGridTypeName(tmpName);
    tmpGrid.setCellType(new GridCellType(AppPreferences.getDefaultGridSize(), tmpName));
    return tmpGrid;
  }

  public static int getGridCellFaceCount(Grid grid) {
    switch (getGridType(grid)) {
      case ISOMETRIC_HEX, HEX_HORI, HEX_VERT -> {
        return 6;
      }
      case SQUARE, ISOMETRIC -> {
        return 4;
      }
      case NONE -> {
        return 0;
      }
      default -> throw new IllegalArgumentException(
          "Don't know type of grid: " + grid.getClass().getName());
    }
  }

  public static String getGridType(Grid grid) {
    if (grid instanceof HexGridVertical) {
      if (grid.isIsometric()) return ISOMETRIC_HEX;
      return HEX_VERT;
    }
    if (grid instanceof HexGridHorizontal) {
      return HEX_HORI;
    }
    if (grid instanceof SquareGrid) {
      return SQUARE;
    }
    if (grid instanceof IsometricGrid) {
      return ISOMETRIC;
    }
    if (grid instanceof GridlessGrid) {
      return NONE;
    }
    throw new IllegalArgumentException("Don't know type of grid: " + grid.getClass().getName());
  }

  public static boolean isSquare(String gridType) {
    return SQUARE.equals(gridType);
  }

  public static boolean isNone(String gridType) {
    return NONE.equals(gridType);
  }

  public static boolean isHexVertical(String gridType) {
    return HEX_VERT.equals(gridType);
  }

  public static boolean isHexHorizontal(String gridType) {
    return HEX_HORI.equals(gridType);
  }

  public static boolean isIsometric(String gridType) {
    return ISOMETRIC.equals(gridType);
  }

  public static boolean isIsometricHex(String gridType) {
    return ISOMETRIC_HEX.equals(gridType);
  }
}
