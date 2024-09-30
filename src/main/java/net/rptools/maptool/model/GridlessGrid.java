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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.server.proto.GridDto;
import net.rptools.maptool.server.proto.GridlessGridDto;
import net.rptools.maptool.util.GraphicsUtil;

public class GridlessGrid extends Grid {
  private static List<TokenFootprint> footprintList;

  // @formatter:off
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

  // @formatter:on

  @Override
  public List<TokenFootprint> getFootprints() {
    if (footprintList == null) {
      try {
        footprintList = loadFootprints("net/rptools/maptool/model/gridlessGridFootprints.xml");
      } catch (IOException ioe) {
        MapTool.showError("GridlessGrid.error.notLoaded", ioe);
      }
    }
    return footprintList;
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
      // movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), new MovementKey(callback,
      // 0, 0));
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
    return GraphicsUtil.createLineSegmentEllipse(
        -visionRange, -visionRange, visionRange, visionRange, CIRCLE_SEGMENTS);
  }

  @Override
  protected void fillDto(GridDto.Builder dto) {
    dto.setGridlessGrid(GridlessGridDto.newBuilder());
  }
}
