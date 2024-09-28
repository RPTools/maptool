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

/**
 * Given a string describing the type of desired grid, this factory creates and returns an object of
 * the appropriate type.
 *
 * <p>(Ugh. This really should use an SPI-like factory interface.)
 */
public class GridFactory {
  public static final String HEX_VERT = "Vertical Hex";
  public static final String HEX_HORI = "Horizontal Hex";
  public static final String SQUARE = "Square";
  public static final String ISOMETRIC = "Isometric";
  public static final String ISOMETRIC_HEX = "Isometric Hex";
  public static final String NONE = "None";

  public static Grid createGrid(String type) {
    if (isHexVertical(type)) {
      return new HexGridVertical();
    }
    if (isHexHorizontal(type)) {
      return new HexGridHorizontal();
    }
    if (isSquare(type)) {
      return new SquareGrid();
    }
    if (isIsometric(type)) {
      return new IsometricGrid();
    }
    if (isNone(type)) {
      return new GridlessGrid();
    }
    throw new IllegalArgumentException("Unknown grid type: " + type);
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
