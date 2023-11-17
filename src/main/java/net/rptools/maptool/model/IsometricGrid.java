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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.rptools.maptool.util.GraphicsUtil;

public class IsometricGrid extends Grid {
  /**
   * An attempt at an isometric style map grid where each cell is a diamond with the sides angled at
   * approx 30 degrees. However rather than being true isometric, each cell is twice as wide as
   * high. This makes converting images significantly easier for end-users.
   */
  private static final int ISO_ANGLE = 27;

  private static final int[] ALL_ANGLES = new int[] {-135, -90, -45, 0, 45, 90, 135, 180};
  private static int[] FACING_ANGLES;
  private static List<TokenFootprint> footprintList;
  private static BufferedImage pathHighlight =
      RessourceManager.getImage(Images.GRID_BORDER_ISOMETRIC);

  public IsometricGrid() {
    super();
    if (FACING_ANGLES == null) {
      boolean faceEdges = AppPreferences.getFaceEdge();
      boolean faceVertices = AppPreferences.getFaceVertex();
      setFacings(faceEdges, faceVertices);
    }
  }

  public IsometricGrid(boolean faceEdges, boolean faceVertices) {
    setFacings(faceEdges, faceVertices);
  }

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

  public static double degreesFromIso(double facing) {
    /**
     * Given a facing from an isometric map turn it into plan map equivalent i.e. 27 degree converts
     * to 45 degree
     */
    double newFacing = facing;
    if (Math.cos(facing) != 0) {
      double v1 = Math.sin(Math.toRadians(newFacing)) * 2;
      double v2 = Math.cos(Math.toRadians(newFacing));
      double v3 = Math.toDegrees(Math.atan(v1 / v2));
      if (facing > 90 || facing < -90) v3 = 180 + v3;
      newFacing = Math.floor(v3);
    }
    return newFacing;
  }

  public static double degreesToIso(double facing) {
    /**
     * Given a facing from a plan map turn it into isometric map equivalent i.e 45 degree converts
     * to 30 degree
     */
    double iso = Math.asin((Math.sin(facing) / 2) / Math.cos(facing));
    System.out.println("in=" + facing + " out=" + iso);
    return iso;
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
  public int[] getFacingAngles() {
    return FACING_ANGLES;
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
            ? AppPreferences.getMovementMetric()
            : MapTool.getServerPolicy().getMovementMetric();
    return new AStarSquareEuclideanWalker(getZone(), metric);
  }

  @Override
  public GridCapabilities getCapabilities() {
    return GRID_CAPABILITIES;
  }

  @Override
  protected Area createCellShape(int size) {
    int x[] = {size, size * 2, size, 0};
    int y[] = {0, size / 2, size, size / 2};
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
  public void setFacings(boolean faceEdges, boolean faceVertices) {
    if (faceEdges && faceVertices) {
      FACING_ANGLES = ALL_ANGLES;
    } else if (!faceEdges && faceVertices) {
      FACING_ANGLES = new int[] {-90, 0, 90, 180};
    } else if (faceEdges && !faceVertices) {
      FACING_ANGLES = new int[] {-135, -45, 45, 135};
    } else {
      FACING_ANGLES = new int[] {90};
    }
  }

  @Override
  public Area getShapedArea(
      ShapeType shape,
      Token token,
      double range,
      double arcAngle,
      int offsetAngle,
      boolean scaleWithToken) {
    if (shape == null) {
      shape = ShapeType.CIRCLE;
    }
    int visionDistance = getZone().getTokenVisionInPixels();
    double visionRange =
        (range == 0) ? visionDistance : range * getSize() / getZone().getUnitsPerCell();

    if (scaleWithToken) {
      Rectangle footprint = token.getFootprint(this).getBounds(this);
      visionRange += footprint.getHeight() / 2;
      System.out.println(token.getName() + " footprint.getWidth() " + footprint.getWidth());
      System.out.println(token.getName() + " footprint.getHeight() " + footprint.getHeight());
    }
    // System.out.println("this.getDefaultFootprint() " + this.getDefaultFootprint());
    // System.out.println("token.getWidth() " + token.getWidth());

    Area visibleArea = new Area();
    switch (shape) {
      case CIRCLE:
        visionRange = (float) Math.sin(Math.toRadians(45)) * visionRange;
        // visibleArea = new Area(new Ellipse2D.Double(-visionRange * 2, -visionRange, visionRange *
        // 4, visionRange * 2));
        visibleArea =
            GraphicsUtil.createLineSegmentEllipse(
                -visionRange * 2, -visionRange, visionRange * 2, visionRange, CIRCLE_SEGMENTS);
        break;
      case SQUARE:
        int x[] = {0, (int) visionRange * 2, 0, (int) -visionRange * 2};
        int y[] = {(int) -visionRange, 0, (int) visionRange, 0};
        visibleArea = new Area(new Polygon(x, y, 4));
        break;
      case CONE:
        if (token.getFacing() == null) {
          token.setFacing(0);
        }
        // Rotate the vision range by 45 degrees for isometric view
        visionRange = (float) Math.sin(Math.toRadians(45)) * visionRange;
        // Get the cone, use degreesFromIso to convert the facing from isometric to plan

        // Area tempvisibleArea = new Area(new Arc2D.Double(-visionRange * 2, -visionRange,
        // visionRange * 4, visionRange * 2, token.getFacing() - (arcAngle / 2.0) + (offsetAngle *
        // 1.0), arcAngle,
        // Arc2D.PIE));
        Arc2D cone =
            new Arc2D.Double(
                -visionRange * 2,
                -visionRange,
                visionRange * 4,
                visionRange * 2,
                token.getFacing() - (arcAngle / 2.0) + (offsetAngle * 1.0),
                arcAngle,
                Arc2D.PIE);
        GeneralPath path = new GeneralPath();
        path.append(cone.getPathIterator(null, 1), false); // Flatten the cone to remove 'curves'
        Area tempvisibleArea = new Area(path);

        // Get the cell footprint
        Rectangle footprint =
            token.getFootprint(getZone().getGrid()).getBounds(getZone().getGrid());
        footprint.x = -footprint.width / 2;
        footprint.y = -footprint.height / 2;
        // convert the cell footprint to an area
        Area cellShape = getZone().getGrid().createCellShape(footprint.height);
        // convert the area to isometric view
        AffineTransform mtx = new AffineTransform();
        mtx.translate(-footprint.width / 2, -footprint.height / 2);
        cellShape.transform(mtx);
        // join cell footprint and cone to create viewable area
        visibleArea.add(cellShape);
        visibleArea.add(tempvisibleArea);
        break;
      default:
        // visibleArea = new Area(new Ellipse2D.Double(-visionRange, -visionRange, visionRange * 2,
        // visionRange * 2));
        visibleArea =
            GraphicsUtil.createLineSegmentEllipse(
                -visionRange, -visionRange, visionRange, visionRange, CIRCLE_SEGMENTS);
        break;
    }
    return visibleArea;
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
    Area cellShape = getZone().getGrid().createCellShape(footprint.height);
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
