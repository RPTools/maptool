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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.swing.Action;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;
import net.rptools.maptool.server.proto.GridDto;
import net.rptools.maptool.server.proto.IsometricGridDto;

public class IsometricGrid extends Grid {
  private static List<TokenFootprint> footprintList;
  private static final BufferedImage pathHighlight =
      RessourceManager.getImage(Images.GRID_BORDER_ISOMETRIC);

  public boolean isIsometric() {
    return true;
  }

  /**
   * Cell Dimensions
   *
   * <p>I decided to use cell size provided by Map Properties (getSize()) for the cell height. It
   * might appear more logical for getSize() to be the edge length. However, using it for height
   * means there is a correlation between square grid points and isometric grid points. This will
   * make the creation of maps and tokens easier.
   *
   * <p>*
   */
  @Override
  public double getCellWidth() {
    return getSize() * 2;
  }

  @Override
  public double getCellHeight() {
    return getSize();
  }

  @Override
  public Point2D.Double getCenterOffset() {
    return new Point2D.Double(0, getCellHeight() / 2);
  }

  public double getCellWidthHalf() {
    return getSize();
  }

  public double getCellHeightHalf() {
    return getSize() / 2;
  }

  @Override
  public BufferedImage getCellHighlight() {
    return pathHighlight;
  }

  @Override
  public Dimension getCellOffset() {
    return new Dimension((int) -getCellWidthHalf(), 0);
  }

  @Override
  protected int snapFacingInternal(
      int facing, boolean faceEdges, boolean faceVertices, int addedSteps) {
    if (!faceEdges && !faceVertices) {
      // Facing not support. Return a default answer.
      return 90;
    }

    // Work in range (0, 360], it's easier. Will convert back to (-180,180] at the end.
    facing = Math.floorMod(facing - 1, 360) + 1;

    /* The number of degrees between each standard facing. */
    int step = (faceEdges && faceVertices) ? 45 : 90;
    /* The position of the first standard facing CCW from zero. */
    int base = (faceEdges && !faceVertices) ? 45 : 0;
    /* A modification applied to facing to get the nearest answer, not a modulo/int div answer. */
    int diff = (step - 1) / 2;

    int stepsFromBase = Math.floorDiv(facing + diff - base, step) + addedSteps;
    return stepsFromBase * step + base;
  }

  @Override
  public Point2D.Double getCellCenter(CellPoint cell) {
    // iso grids have their x at their center;
    ZonePoint zonePoint = convert(cell);
    double x = zonePoint.x;
    double y = zonePoint.y + getCellHeight() / 2.0;
    return new Point2D.Double(x, y);
  }

  private static final GridCapabilities GRID_CAPABILITIES =
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
  public CellPoint convert(ZonePoint zp) {
    double isoX =
        ((zp.x - getOffsetX()) / getCellWidthHalf() + (zp.y - getOffsetY()) / getCellHeightHalf())
            / 2;
    double isoY =
        ((zp.y - getOffsetY()) / getCellHeightHalf() - (zp.x - getOffsetX()) / getCellWidthHalf())
            / 2;
    int newX = (int) Math.floor(isoX);
    int newY = (int) Math.floor(isoY);
    return new CellPoint(newX, newY);
  }

  @Override
  public ZonePoint convert(CellPoint cp) {
    double mapX = (cp.x - cp.y) * getCellWidthHalf() + getOffsetX();
    double mapY = (cp.x + cp.y) * getCellHeightHalf() + getOffsetY();
    return new ZonePoint((int) (mapX), (int) (mapY));
  }

  @Override
  public ZonePoint getNearestVertex(ZonePoint point) {
    double px = point.x;
    double py = point.y + getCellHeightHalf();
    ZonePoint zp = new ZonePoint((int) px, (int) py);
    return convert(convert(zp));
  }

  @Override
  public Rectangle getBounds(CellPoint cp) {
    ZonePoint zp = convert(cp);
    return new Rectangle(zp.x - getSize(), zp.y, getSize() * 2, getSize());
  }

  @Override
  public boolean useMetric() {
    return true;
  }

  @Override
  public ZoneWalker createZoneWalker() {
    WalkerMetric metric =
        MapTool.isPersonalServer()
            ? AppPreferences.movementMetric.get()
            : MapTool.getServerPolicy().getMovementMetric();
    return new AStarSquareEuclideanWalker(getZone(), metric);
  }

  @Override
  public GridCapabilities getCapabilities() {
    return GRID_CAPABILITIES;
  }

  @Override
  protected Area createCellShape() {
    return createCellShape(getSize());
  }

  private Area createCellShape(int size) {
    int[] x = {size, size * 2, size, 0};
    int[] y = {0, size / 2, size, size / 2};
    return new Area(new Polygon(x, y, 4));
  }

  @Override
  public void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap) {
    System.out.println("install iso movement keys");
    if (movementKeys == null) {
      movementKeys = new HashMap<KeyStroke, Action>(18); // This is 13/0.75, rounded up
      int size = getSize();
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new MovementKey(callback, -size, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), new MovementKey(callback, 0, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), new MovementKey(callback, size, -size));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), new MovementKey(callback, -size, 0));
      movementKeys.put(
          KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), new MovementKey(callback, 0, 0));
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
    System.out.println("uninstall iso movement keys");
    if (movementKeys != null) {
      for (KeyStroke key : movementKeys.keySet()) {
        actionMap.remove(key);
      }
    }
  }

  @Override
  protected int getTokenFacingAngleRelativeToGridAxis(Token token) {
    return -(token.getFacing() + 45);
  }

  @Override
  protected @Nonnull Area getShapedAreaWithoutFootprint(
      ShapeType shape,
      int tokenFacingAngle,
      double visionRange,
      double width,
      double arcAngle,
      int offsetAngle) {
    var orthoShape =
        super.getShapedAreaWithoutFootprint(
            shape, tokenFacingAngle, visionRange, width, arcAngle, offsetAngle);

    AffineTransform at = new AffineTransform();
    var sqrt2 = Math.sqrt(2);
    at.scale(sqrt2, sqrt2 / 2);
    at.rotate(Math.PI / 4);
    orthoShape.transform(at);

    return orthoShape;
  }

  @Override
  protected @Nonnull Area getFootprintShapedAreaForCone(Rectangle footprint) {
    // convert the cell footprint to an area
    Area cellShape = createCellShape(footprint.height);
    // convert the area to isometric view
    AffineTransform mtx = new AffineTransform();
    mtx.translate(-footprint.width / 2, -footprint.height / 2);
    cellShape.transform(mtx);
    return cellShape;
  }

  @Override
  public Area getTokenCellArea(Rectangle bounds) {
    return getTokenCellArea(new Area(bounds));
  }

  @Override
  public Area getTokenCellArea(Area bounds) {
    // Get the cell footprint
    Rectangle footprint = bounds.getBounds();
    footprint.x = -footprint.width / 2;
    footprint.y = -footprint.height / 2;
    // convert the cell footprint to an area
    Area cellShape = createCellShape(footprint.height);
    // convert the area to isometric view
    AffineTransform mtx = new AffineTransform();
    mtx.translate(bounds.getBounds().getX(), bounds.getBounds().getY());
    cellShape.transform(mtx);
    return cellShape;
  }

  @Override
  protected void fillDto(GridDto.Builder dto) {
    dto.setIsometricGrid(IsometricGridDto.newBuilder());
  }

  @Override
  public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
    double scale = renderer.getScale();
    double gridSize = getSize() * scale;
    double isoHeight = getSize() * scale;
    double isoWidth = getSize() * 2 * scale;

    g.setColor(new Color(getZone().getGridColor()));

    int offX = (int) (renderer.getViewOffsetX() % isoWidth + getOffsetX() * scale);
    int offY = (int) (renderer.getViewOffsetY() % gridSize + getOffsetY() * scale);

    int startCol = (int) ((int) (bounds.x / isoWidth) * isoWidth);
    int startRow = (int) ((int) (bounds.y / gridSize) * gridSize);

    for (double row = startRow; row < bounds.y + bounds.height + gridSize; row += gridSize) {
      for (double col = startCol; col < bounds.x + bounds.width + isoWidth; col += isoWidth) {
        drawHatch(renderer, g, (int) (col + offX), (int) (row + offY));
      }
    }

    for (double row = startRow - (isoHeight / 2);
        row < bounds.y + bounds.height + gridSize;
        row += gridSize) {
      for (double col = startCol - (isoWidth / 2);
          col < bounds.x + bounds.width + isoWidth;
          col += isoWidth) {
        drawHatch(renderer, g, (int) (col + offX), (int) (row + offY));
      }
    }
  }

  private void drawHatch(ZoneRenderer renderer, Graphics2D g, int x, int y) {
    double isoWidth = getSize() * renderer.getScale();
    int hatchSize = isoWidth > 10 ? (int) isoWidth / 8 : 2;
    g.setStroke(new BasicStroke(AppState.getGridSize()));
    g.drawLine(x - (hatchSize * 2), y - hatchSize, x + (hatchSize * 2), y + hatchSize);
    g.drawLine(x - (hatchSize * 2), y + hatchSize, x + (hatchSize * 2), y - hatchSize);
  }

  /**
   * Take a rectangular image, rotate it 45 degrees then reduce its resulting height by half.
   *
   * @param planImage the image to rotate and scale
   * @return image in isometric format
   */
  public static BufferedImage isoImage(BufferedImage planImage) {
    int nSize = (planImage.getWidth() + planImage.getHeight());
    return resize(rotate(planImage), nSize, nSize / 2);
  }

  private static BufferedImage rotate(BufferedImage planImage) {
    double sin = Math.abs(Math.sin(Math.toRadians(45)));
    double cos = Math.abs(Math.cos(Math.toRadians(45)));

    int w = planImage.getWidth(null), h = planImage.getHeight(null);

    int neww = (int) Math.floor(w * cos + h * sin);
    int newh = (int) Math.floor(h * cos + w * sin);

    // Rotate image 45 degrees
    BufferedImage rotateImage = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = rotateImage.createGraphics();
    g.translate((neww - w) / 2, (newh - h) / 2);
    g.rotate(Math.toRadians(45), w / 2, h / 2);
    g.drawRenderedImage(planImage, null);
    g.dispose();
    // scale image to half height
    return rotateImage;
  }

  private static BufferedImage resize(BufferedImage image, int newWidth, int newHeight) {
    // Resize into a BufferedImage
    BufferedImage bimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D bGr = bimg.createGraphics();
    bGr.drawImage(image, 0, 0, newWidth, newHeight, null);
    bGr.dispose();
    return bimg;
  }

  /**
   * Take a rectangular Area, rotate it 45 degrees then reduce its resulting height by half.
   *
   * @param planArea the area to rotate and scale
   * @return Area in isometric format
   */
  public static Area isoArea(Area planArea) {
    int nSize = (planArea.getBounds().width + planArea.getBounds().height);

    return resize(rotate(planArea), nSize, nSize / 2);
    // return rotate(planArea);
  }

  private static Area rotate(Area planArea) {
    double sin = Math.abs(Math.sin(Math.toRadians(45)));
    double cos = Math.abs(Math.cos(Math.toRadians(45)));

    int w = planArea.getBounds().width, h = planArea.getBounds().height;

    int neww = (int) Math.floor(w * cos + h * sin);
    int newh = (int) Math.floor(h * cos + w * sin);

    double scaleX = neww / w;
    double scaleY = newh / h;

    int tx = (neww - w) / 2;
    int ty = (newh - h) / 2;

    // Rotate Area 45 degrees
    AffineTransform atArea = AffineTransform.getScaleInstance(scaleX, scaleY);
    atArea.concatenate(AffineTransform.getTranslateInstance(tx, ty));
    atArea.concatenate(AffineTransform.getRotateInstance(Math.toRadians(45), w / 2, h / 2));

    return new Area(atArea.createTransformedShape(planArea));
  }

  private static Area resize(Area planArea, int newWidth, int newHeight) {
    // Resize into a Area
    double w = planArea.getBounds().width, h = planArea.getBounds().height;
    double scaleX = newWidth / w;
    double scaleY = newHeight / h;

    AffineTransform atArea = AffineTransform.getScaleInstance(scaleX, scaleY);

    return new Area(atArea.createTransformedShape(planArea));
  }
}
