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
package net.rptools.maptool.client.ui.adjustgrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.ui.Scale;

public class AdjustGridPanel extends JComponent
    implements MouseListener, MouseMotionListener, MouseWheelListener {

  private static final int MINIMUM_GRID_SIZE = 5;

  private static enum Direction {
    Increase,
    Decrease
  };

  public static final String PROPERTY_GRID_OFFSET_X = "gridOffsetX";
  public static final String PROPERTY_GRID_OFFSET_Y = "gridOffsetY";
  public static final String PROPERTY_GRID_SIZE = "gridSize";
  public static final String PROPERTY_ZOOM = "zoom";

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private int gridOffsetX = 0;
  private int gridOffsetY = 0;
  private int gridSize = 40;
  private boolean showGrid = true;

  private int mouseX;
  private int mouseY;

  private int dragStartX;
  private int dragStartY;
  private int dragOffsetX;
  private int dragOffsetY;

  private Color gridColor = Color.darkGray;

  private BufferedImage image;

  private Scale scale;

  public AdjustGridPanel() {
    setOpaque(false);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(AppActions.ZOOM_OUT.getKeyStroke(), "zoomOut");
    getActionMap()
        .put(
            "zoomOut",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                zoomOut();
              }
            });

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(AppActions.ZOOM_IN.getKeyStroke(), "zoomIn");
    getActionMap()
        .put(
            "zoomIn",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                zoomIn();
              }
            });

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(AppActions.ZOOM_RESET.getKeyStroke(), "zoomReset");
    getActionMap()
        .put(
            "zoomReset",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                zoomReset();
              }
            });
  }

  public void setZoneImage(BufferedImage image) {
    this.image = image;

    scale = new Scale(image.getWidth(), image.getHeight());
  }

  public void setZoomIndex(int index) {
    scale.setScale(index);
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {

    if (image == null) {
      return;
    }

    Dimension size = getSize();

    if (scale.initialize(size.width, size.height)) {
      propertyChangeSupport.firePropertyChange(PROPERTY_ZOOM, 0, (int) scale.getScale());
    }

    // CALCULATIONS
    Dimension imageSize = getScaledImageSize();
    Point imagePosition = getScaledImagePosition();

    double imgRatio = scale.getScale();

    // SETUP
    Graphics2D g2d = (Graphics2D) g;

    // BG FILL
    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, size.width, size.height);

    // IMAGE
    g2d.drawImage(image, imagePosition.x, imagePosition.y, imageSize.width, imageSize.height, null);
    g2d.setColor(Color.black);
    g2d.drawRect(imagePosition.x, imagePosition.y, imageSize.width, imageSize.height);

    // GRID
    g2d.setColor(gridColor);
    double gridSize = this.gridSize * imgRatio;

    // across
    int x = imagePosition.x + (int) (gridOffsetX * imgRatio);
    for (double i = gridSize; i <= imageSize.width; i += gridSize) {
      g2d.drawLine(
          x + (int) i, imagePosition.y, x + (int) i, imagePosition.y + imageSize.height - 1);
    }

    // down
    int y = imagePosition.y + (int) (gridOffsetY * imgRatio);
    for (double i = gridSize; i <= imageSize.height; i += gridSize) {
      g2d.drawLine(
          imagePosition.x, y + (int) i, imagePosition.x + imageSize.width - 1, y + (int) i);
    }
  }

  public void setGridColor(Color color) {
    gridColor = color;
  }

  @Override
  public boolean isFocusable() {
    return true;
  }

  public void setGridOffset(int offsetX, int offsetY) {
    gridOffsetX = offsetX;
    gridOffsetY = offsetY;

    repaint();
  }

  public int getGridSize() {
    return gridSize;
  }

  public int getGridOffsetX() {
    return gridOffsetX;
  }

  public int getGridOffsetY() {
    return gridOffsetY;
  }

  public void setGridSize(int size) {
    gridSize = Math.max(MINIMUM_GRID_SIZE, size);
    repaint();
  }

  private Dimension getScaledImageSize() {

    Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
    imageSize.width *= scale.getScale();
    imageSize.height *= scale.getScale();

    return imageSize;
  }

  private Point getScaledImagePosition() {

    int imgX = scale.getOffsetX();
    int imgY = scale.getOffsetY();

    return new Point(imgX, imgY);
  }

  public void zoomIn() {
    scale.zoomIn(mouseX, mouseY);
    repaint();
  }

  public void zoomOut() {
    scale.zoomOut(mouseX, mouseY);
    repaint();
  }

  public void zoomReset() {
    scale.reset();
    repaint();
  }

  public void moveGridBy(int dx, int dy) {

    int oldOffsetX = gridOffsetX;
    int oldOffsetY = gridOffsetY;

    gridOffsetX += dx;
    gridOffsetY += dy;

    gridOffsetX %= gridSize;
    gridOffsetY %= gridSize;

    if (gridOffsetY > 0) {
      gridOffsetY = gridOffsetY - gridSize;
    }

    if (gridOffsetX > 0) {
      gridOffsetX = gridOffsetX - gridSize;
    }

    repaint();

    propertyChangeSupport.firePropertyChange(PROPERTY_GRID_OFFSET_X, oldOffsetX, gridOffsetX);
    propertyChangeSupport.firePropertyChange(PROPERTY_GRID_OFFSET_Y, oldOffsetY, gridOffsetY);
  }

  public void adjustGridSize(int delta) {

    int oldSize = gridSize;
    gridSize = Math.max(MINIMUM_GRID_SIZE, gridSize + delta);

    repaint();
    propertyChangeSupport.firePropertyChange(PROPERTY_GRID_SIZE, oldSize, gridSize);
  }

  private void adjustGridSize(Direction direction) {

    Point imagePosition = getScaledImagePosition();

    double gridSize = this.gridSize * scale.getScale();

    int cellX = (int) ((mouseX - imagePosition.x - gridOffsetX) / gridSize);
    int cellY = (int) ((mouseY - imagePosition.y - gridOffsetY) / gridSize);

    switch (direction) {
      case Increase:
        adjustGridSize(1);

        if (this.gridSize != gridSize) {
          moveGridBy(-cellX, -cellY);
        }
        break;
      case Decrease:
        adjustGridSize(-1);

        if (this.gridSize != gridSize) {
          moveGridBy(cellX, cellY);
        }
        break;
    }
  }

  ////
  // MOUSE LISTENER
  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {

    mouseX = e.getX();
    mouseY = e.getY();

    dragStartX = e.getX();
    dragStartY = e.getY();

    Point imagePosition = getScaledImagePosition();

    int x = (int) ((e.getX() - imagePosition.x) / scale.getScale() - gridOffsetX);
    int y = (int) ((e.getY() - imagePosition.y) / scale.getScale() - gridOffsetY);

    dragOffsetX = x % gridSize;
    dragOffsetY = y % gridSize;
  }

  public void mouseReleased(MouseEvent e) {}

  ////
  // MOUSE MOTION LISTENER
  public void mouseDragged(MouseEvent e) {

    if (SwingUtilities.isLeftMouseButton(e)) {

      Point imagePosition = getScaledImagePosition();

      int x = (int) ((e.getX() - imagePosition.x) / scale.getScale() - dragOffsetX);
      int y = (int) ((e.getY() - imagePosition.y) / scale.getScale() - dragOffsetY);

      int oldOffsetX = gridOffsetX;
      int oldOffsetY = gridOffsetY;

      gridOffsetX = x % gridSize;
      gridOffsetY = y % gridSize;

      if (gridOffsetY > 0) {
        gridOffsetY = gridOffsetY - gridSize;
      }

      if (gridOffsetX > 0) {
        gridOffsetX = gridOffsetX - gridSize;
      }

      repaint();
      propertyChangeSupport.firePropertyChange(PROPERTY_GRID_OFFSET_X, oldOffsetX, gridOffsetX);
      propertyChangeSupport.firePropertyChange(PROPERTY_GRID_OFFSET_Y, oldOffsetY, gridOffsetY);
    } else {
      int offsetX = scale.getOffsetX() + e.getX() - dragStartX;
      int offsetY = scale.getOffsetY() + e.getY() - dragStartY;

      scale.setOffset(offsetX, offsetY);

      dragStartX = e.getX();
      dragStartY = e.getY();

      repaint();
    }
  }

  public void mouseMoved(MouseEvent e) {

    Dimension imgSize = getScaledImageSize();
    Point imgPos = getScaledImagePosition();

    boolean insideMap =
        e.getX() > imgPos.x
            && e.getX() < imgPos.x + imgSize.width
            && e.getY() > imgPos.y
            && e.getY() < imgPos.y + imgSize.height;
    if ((insideMap && showGrid) || (!insideMap && !showGrid)) {
      showGrid = !insideMap;
      repaint();
    }

    mouseX = e.getX();
    mouseY = e.getY();
  }

  ////
  // MOUSE WHEEL LISTENER
  public void mouseWheelMoved(MouseWheelEvent e) {

    if (SwingUtil.isControlDown(e)) {

      double oldScale = scale.getScale();
      if (e.getWheelRotation() > 0) {
        scale.zoomOut(e.getX(), e.getY());
      } else {
        scale.zoomIn(e.getX(), e.getY());
      }
      propertyChangeSupport.firePropertyChange(PROPERTY_ZOOM, oldScale, scale.getScale());
    } else {

      if (e.getWheelRotation() > 0) {

        adjustGridSize(Direction.Increase);
      } else {

        adjustGridSize(Direction.Decrease);
      }
    }
    repaint();
  }

  ////
  // PROPERTY CHANGE SUPPORT
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(name, listener);
  }

  @Override
  public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(name, listener);
  }
  /*
   * private final Map<KeyStroke, Action> KEYSTROKES = new HashMap<KeyStroke, Action>() { { put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), new GridSizeAction(Size.Decrease));
   * put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), new GridSizeAction(Size.Decrease)); put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), new
   * GridSizeAction(Size.Increase)); put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), new GridSizeAction(Size.Increase)); put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new
   * GridOffsetAction(GridOffsetAction.Direction.Up)); put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new GridOffsetAction(GridOffsetAction.Direction.Left));
   * put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new GridOffsetAction(GridOffsetAction.Direction.Down)); put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new
   * GridOffsetAction(GridOffsetAction.Direction.Right)); } }; protected Map<KeyStroke, Action> getKeyActionMap() { return KEYSTROKES; }
   *
   * private final class GridSizeAction extends AbstractAction { private final Size size; public GridSizeAction(Size size) { this.size = size; }
   *
   * public void actionPerformed(ActionEvent e) { ZoneRenderer renderer = (ZoneRenderer) e.getSource(); adjustGridSize(renderer, size); } }
   *
   * private static final class GridOffsetAction extends AbstractAction { private static enum Direction { Left, Right, Up, Down }; private final Direction direction;
   *
   * public GridOffsetAction(Direction direction) { this.direction = direction; }
   *
   * public void actionPerformed(ActionEvent e) { ZoneRenderer renderer = (ZoneRenderer) e.getSource(); switch (direction) { case Left: renderer.moveGridBy(-1, 0); break; case Right:
   * renderer.moveGridBy(1, 0); break; case Up: renderer.moveGridBy(0, -1); break; case Down: renderer.moveGridBy(0, 1); break; } } }
   */
}
