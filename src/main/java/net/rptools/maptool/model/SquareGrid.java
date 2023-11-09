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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;
import net.rptools.maptool.server.proto.GridDto;
import net.rptools.maptool.server.proto.SquareGridDto;

public class SquareGrid extends Grid {
  private static final String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // $NON-NLS-1$
  private static final Dimension CELL_OFFSET = new Dimension(0, 0);
  private static BufferedImage pathHighlight = RessourceManager.getImage(Images.GRID_BORDER_SQUARE);
  private static BufferedImage pathHighlightAlt =
      RessourceManager.getImage(Images.GRID_BORDER_SQUARE_RED);

  private static List<TokenFootprint> footprintList;

  // @formatter:off
  private static final GridCapabilities CAPABILITIES =
      new GridCapabilities() {
        public boolean isPathingSupported() {
          return true;
        }

        public boolean isSnapToGridSupported() {
          return true;
        }

        public boolean isPathLineSupported() {
          return true;
        }

        public boolean isSecondDimensionAdjustmentSupported() {
          return false;
        }

        public boolean isCoordinatesSupported() {
          return true;
        }
      };
  // @formatter:on

  private static final int[] ALL_ANGLES = new int[] {-135, -90, -45, 0, 45, 90, 135, 180};
  private static int[] FACING_ANGLES;

  public SquareGrid() {
    super();
    if (FACING_ANGLES == null) {
      boolean faceEdges = AppPreferences.getFaceEdge();
      boolean faceVertices = AppPreferences.getFaceVertex();
      setFacings(faceEdges, faceVertices);
    }
  }

  @Override
  public Point2D.Double getCenterOffset() {
    return new Point2D.Double(getCellWidth() / 2, getCellHeight() / 2);
  }

  public SquareGrid(boolean faceEdges, boolean faceVertices) {
    setFacings(faceEdges, faceVertices);
  }

  @Override
  public void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap) {
    if (movementKeys == null) {
      movementKeys = new HashMap<KeyStroke, Action>(18); // This is 13/0.75, rounded up
      int size = getSize();
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), new MovementKey(callback, -size, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new MovementKey(callback, 0, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), new MovementKey(callback, size, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), new MovementKey(callback, -size, 0));
      // movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), new MovementKey(callback,
      // 0, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), new MovementKey(callback, size, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), new MovementKey(callback, -size, size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), new MovementKey(callback, 0, size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), new MovementKey(callback, size, size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new MovementKey(callback, -size, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new MovementKey(callback, size, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new MovementKey(callback, 0, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new MovementKey(callback, 0, size));
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
  protected void fillDto(GridDto.Builder dto) {
    dto.setSquareGrid(SquareGridDto.newBuilder());
  }

  @Override
  public void setFacings(boolean faceEdges, boolean faceVertices) {
    if (faceEdges && faceVertices) {
      FACING_ANGLES = ALL_ANGLES;
    } else if (!faceEdges && faceVertices) {
      FACING_ANGLES = new int[] {-135, -45, 45, 135};
    } else if (faceEdges && !faceVertices) {
      FACING_ANGLES = new int[] {-90, 0, 90, 180};
    } else {
      FACING_ANGLES = new int[] {90};
    }
  }

  @Override
  public void drawCoordinatesOverlay(Graphics2D g, ZoneRenderer renderer) {
    Object oldAA = SwingUtil.useAntiAliasing(g);

    Font oldFont = g.getFont();
    g.setFont(g.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    FontMetrics fm = g.getFontMetrics();

    double cellSize = renderer.getScaledGridSize();
    CellPoint topLeft = convert(new ScreenPoint(0, 0).convertToZone(renderer));
    ScreenPoint sp = ScreenPoint.fromZonePoint(renderer, convert(topLeft));

    Dimension size = renderer.getSize();

    int startX = SwingUtilities.computeStringWidth(fm, "MMM") + 10;

    double x = sp.x + cellSize / 2; // Start at middle of the cell that's on screen
    int nextAvailableSpace = -1;
    while (x < size.width) {
      String coord = Integer.toString(topLeft.x);

      int strWidth = SwingUtilities.computeStringWidth(fm, coord);
      int strX = (int) x - strWidth / 2;

      if (x > startX && strX > nextAvailableSpace) {
        g.setColor(Color.black);
        g.drawString(coord, strX, fm.getHeight());
        g.setColor(Color.orange);
        g.drawString(coord, strX - 1, fm.getHeight() - 1);

        nextAvailableSpace = strX + strWidth + 10;
      }
      x += cellSize;
      topLeft.x++;
    }
    double y = sp.y + cellSize / 2; // Start at middle of the cell that's on screen
    nextAvailableSpace = -1;
    while (y < size.height) {
      String coord = decimalToAlphaCoord(topLeft.y);

      int strY = (int) y + fm.getAscent() / 2;

      if (y > fm.getHeight() && strY > nextAvailableSpace) {
        g.setColor(Color.black);
        g.drawString(coord, 10, strY);
        g.setColor(Color.yellow);
        g.drawString(coord, 10 - 1, strY - 1);

        nextAvailableSpace = strY + fm.getAscent() / 2 + 10;
      }
      y += cellSize;
      topLeft.y++;
    }
    g.setFont(oldFont);
    SwingUtil.restoreAntiAliasing(g, oldAA);
  }

  @Override
  public List<TokenFootprint> getFootprints() {
    if (footprintList == null) {
      try {
        footprintList = loadFootprints("net/rptools/maptool/model/squareGridFootprints.xml");
      } catch (IOException ioe) {
        MapTool.showError("SquareGrid.error.squareGridNotLoaded", ioe);
      }
    }
    return footprintList;
  }

  @Override
  public boolean useMetric() {
    return true;
  }

  @Override
  public Rectangle getBounds(CellPoint cp) {
    return new Rectangle(
        cp.x * getSize() + getOffsetX(), cp.y * getSize() + getOffsetY(), getSize(), getSize());
  }

  @Override
  public BufferedImage getCellHighlight() {
    return pathHighlight;
  }

  @Override
  protected Area createCellShape(int size) {
    return new Area(new Rectangle(0, 0, size, size));
  }

  @Override
  public Dimension getCellOffset() {
    return CELL_OFFSET;
  }

  @Override
  public double getCellHeight() {
    return getSize();
  }

  @Override
  public double getCellWidth() {
    return getSize();
  }

  @Override
  public int[] getFacingAngles() {
    return FACING_ANGLES;
  }

  @Override
  public Point2D.Double getCellCenter(CellPoint cell) {
    // square have their xy at their top left
    ZonePoint zonePoint = convert(cell);
    double x = zonePoint.x + getCellWidth() / 2.0;
    double y = zonePoint.y + getCellHeight() / 2.0;
    return new Point2D.Double(x, y);
  }

  @Override
  public CellPoint convert(ZonePoint zp) {
    double calcX = (zp.x - getOffsetX()) / (float) getSize();
    double calcY = (zp.y - getOffsetY()) / (float) getSize();

    boolean exactCalcX = (zp.x - getOffsetX()) % getSize() == 0;
    boolean exactCalcY = (zp.y - getOffsetY()) % getSize() == 0;

    int newX = (int) (calcX < 0 && !exactCalcX ? calcX - 1 : calcX);
    int newY = (int) (calcY < 0 && !exactCalcY ? calcY - 1 : calcY);

    // System.out.format("%d / %d => %f, %f => %d, %d\n", zp.x, getSize(), calcX, calcY, newX,
    // newY);
    return new CellPoint(newX, newY);
  }

  @Override
  public ZoneWalker createZoneWalker() {
    WalkerMetric metric =
        MapTool.isPersonalServer()
            ? AppPreferences.getMovementMetric()
            : MapTool.getServerPolicy().getMovementMetric();
    return new AStarSquareEuclideanWalker(getZone(), metric);
  }

  @Override
  public ZonePoint convert(CellPoint cp) {
    return new ZonePoint((cp.x * getSize() + getOffsetX()), (cp.y * getSize() + getOffsetY()));
  }

  @Override
  public GridCapabilities getCapabilities() {
    return CAPABILITIES;
  }

  @Override
  public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
    double scale = renderer.getScale();
    double gridSize = getSize() * scale;

    g.setColor(new Color(getZone().getGridColor()));

    int offX = (int) (renderer.getViewOffsetX() % gridSize + getOffsetX() * scale);
    int offY = (int) (renderer.getViewOffsetY() % gridSize + getOffsetY() * scale);

    int startCol = (int) ((int) (bounds.x / gridSize) * gridSize);
    int startRow = (int) ((int) (bounds.y / gridSize) * gridSize);

    for (double row = startRow; row < bounds.y + bounds.height + gridSize; row += gridSize) {
      if (AppState.getGridSize() == 1) {
        g.drawLine(bounds.x, (int) (row + offY), bounds.x + bounds.width, (int) (row + offY));
      } else {
        g.fillRect(
            bounds.x,
            (int) (row + offY - (AppState.getGridSize() / 2)),
            bounds.width,
            AppState.getGridSize());
      }
    }
    for (double col = startCol; col < bounds.x + bounds.width + gridSize; col += gridSize) {
      if (AppState.getGridSize() == 1) {
        g.drawLine((int) (col + offX), bounds.y, (int) (col + offX), bounds.y + bounds.height);
      } else {
        g.fillRect(
            (int) (col + offX - (AppState.getGridSize() / 2)),
            bounds.y,
            AppState.getGridSize(),
            bounds.height);
      }
    }
  }

  public ZonePoint getCenterPoint(CellPoint cellPoint) {
    ZonePoint zp = convert(cellPoint);
    zp.x += getCellWidth() / 2;
    zp.y += getCellHeight() / 2;
    return zp;
  }

  public static String decimalToAlphaCoord(int value) {
    String result = "";
    int temp;
    boolean isNegative = false;

    if (value < 0) {
      value *= -1;
      value--; // Shift down so -1 is -A instead of -B
      isNegative = true;
    }
    while (value >= 26) {
      temp = value % 26;
      value = (value - temp) / 26 - 1;
      result = alpha.charAt(temp) + result;
    }
    result = alpha.charAt(value) + result;

    if (isNegative) {
      result = "-" + result;
    }
    return result;
  }
}
