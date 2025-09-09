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

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.server.proto.GridDto;
import net.rptools.maptool.server.proto.GridlessGridDto;

public class GridlessGrid extends Grid {

  private static final GridCapabilities GRID_CAPABILITIES =
      new GridCapabilities() {
        public boolean isPathingSupported() {
          return false;
        }

        public boolean isSnapToGridSupported() {
          return false;
        }

        public boolean isPathLineSupported() {
          return false;
        }

        public boolean isSecondDimensionAdjustmentSupported() {
          return false;
        }

        public boolean isCoordinatesSupported() {
          return false;
        }
      };

  @Override
  protected List<TokenFootprint> createFootprints() {
    return List.of(
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20100000080800E0E"), "-11", false, 0.086),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20200000080800E0E"), "-10", false, 0.107),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20300000080800E0E"), "-9", false, 0.134),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20400000080800E0E"), "-8", false, 0.168),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20500000080800E0E"), "-7", false, 0.210),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20600000080800E0E"), "-6", false, 0.262),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20700000080800E0E"), "-5", false, 0.328),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20800000080800E0E"), "-4", false, 0.410),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20900000080800E0E"), "-3", false, 0.512),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20A00000080800E0E"), "-2", false, 0.640),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20B00000080800E0E"), "-1", false, 0.800),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20C00000080800E0E"), "0", true, 1.000),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20D00000080800E0E"), "1", false, 1.200),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20E00000080800E0E"), "2", false, 1.440),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A20F00000080800E0E"), "3", false, 1.728),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21000000080800E0E"), "4", false, 2.074),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21100000080800E0E"), "5", false, 2.488),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21200000080800E0E"), "6", false, 2.986),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21300000080800E0E"), "7", false, 3.583),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21400000080800E0E"), "8", false, 4.300),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21500000080800E0E"), "9", false, 5.160),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21600000080800E0E"), "10", false, 6.192),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21700000080800E0E"), "11", false, 7.430),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21800000080800E0E"), "12", false, 8.916),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21900000080800E0E"), "13", false, 10.699),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21A00000080800E0E"), "14", false, 12.839),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21B00000080800E0E"), "15", false, 15.407),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21C00000080800E0E"), "16", false, 18.488),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21D00000080800E0E"), "17", false, 22.186),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21E00000080800E0E"), "18", false, 26.623),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A21F00000080800E0E"), "19", false, 31.948),
        new TokenFootprint(new GUID("C0A80F0EBEFED0A22000000080800E0E"), "20", false, 38.338));
  }

  @Override
  protected int snapFacingInternal(
      int facing, boolean faceEdges, boolean faceVertices, int addedSteps) {
    // Work in range (0, 360], it's easier. Will convert back to (-180,180] at the end.
    facing = Math.floorMod(facing - 1, 360) + 1;

    /* The number of degrees between each standard facing. */
    int step = 45;
    /* The position of the first standard facing CCW from zero. */
    int base = 0;
    /* A modification applied to facing to get the nearest answer, not a modulo/int div answer. */
    int diff = (step - 1) / 2;

    int stepsFromBase = Math.floorDiv(facing + diff - base, step) + addedSteps;
    return stepsFromBase * step + base;
  }

  @Override
  public Point2D.Double getCellCenter(CellPoint cell) {
    // For gridless grids, cell = pixel;
    return new Point2D.Double(cell.x, cell.y);
  }

  @Override
  public double cellDistance(CellPoint cellA, CellPoint cellB, WalkerMetric wmetric) {
    int dX = cellA.x - cellB.x;
    int dY = cellA.y - cellB.y;
    return Math.sqrt(dX * dX + dY * dY) / this.getSize(); // returns in cell units
  }

  /*
   * May as well use the same keys as for the square grid...
   */
  @Override
  public void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap) {
    if (movementKeys == null) {
      movementKeys = new HashMap<KeyStroke, Action>(18); // This is 13/0.75, rounded up
      Rectangle r = getFootprint(null).getBounds(this);
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0),
          new MovementKey(callback, -r.width, -r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new MovementKey(callback, 0, -r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0),
          new MovementKey(callback, r.width, -r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), new MovementKey(callback, -r.width, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), new MovementKey(callback, r.width, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0),
          new MovementKey(callback, -r.width, r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), new MovementKey(callback, 0, r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0),
          new MovementKey(callback, r.width, r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new MovementKey(callback, -r.width, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new MovementKey(callback, r.width, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new MovementKey(callback, 0, -r.height));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new MovementKey(callback, 0, r.height));
    }
    actionMap.putAll(movementKeys);
  }

  @Override
  public void uninstallMovementKeys(Map<KeyStroke, Action> actionMap) {
    if (movementKeys != null) {
      for (KeyStroke key : movementKeys.keySet()) {
        actionMap.remove(key);
      }
    }
  }

  @Override
  public Rectangle getBounds(CellPoint cp) {
    return new Rectangle(cp.x, cp.y, getSize(), getSize());
  }

  @Override
  public ZonePoint convert(CellPoint cp) {
    return new ZonePoint(cp.x, cp.y);
  }

  @Override
  public CellPoint convert(ZonePoint zp) {
    return new CellPoint(zp.x, zp.y);
  }

  @Override
  protected Area createCellShape() {
    // Doesn't do this
    return null;
  }

  @Override
  public GridCapabilities getCapabilities() {
    return GRID_CAPABILITIES;
  }

  @Override
  public double getCellWidth() {
    return getSize();
  }

  @Override
  public double getCellHeight() {
    return getSize();
  }

  @Override
  public Point2D.Double getCenterOffset() {
    return new Point2D.Double(0, 0);
  }

  @Override
  protected Area getGridArea(
      Token token, double range, boolean scaleWithToken, double visionRange) {
    // A grid area isn't well-defined when there is no grid, so fall back to a circle.
    return super.getGridArea(token, 0, scaleWithToken, visionRange);
  }

  @Override
  protected void fillDto(GridDto.Builder dto) {
    dto.setGridlessGrid(GridlessGridDto.newBuilder());
  }
}
