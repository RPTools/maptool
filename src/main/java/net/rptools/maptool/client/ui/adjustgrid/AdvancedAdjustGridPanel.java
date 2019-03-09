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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.ui.Scale;

public class AdvancedAdjustGridPanel extends JComponent
    implements MouseListener, MouseMotionListener, MouseWheelListener {

  private static final int MINIMUM_GRID_SIZE = 5;

  private int gridCountX = 10;
  private int gridCountY = 10;
  private boolean showGrid = true;

  private int mouseX;
  private int mouseY;

  private BufferedImage image;

  private Scale scale;

  private enum Handle {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
  }

  private int topGrid;
  private int bottomGrid;
  private int leftGrid;
  private int rightGrid;

  private boolean showRows = true;
  private boolean showCols = true;

  private Handle draggingHandle;

  public AdvancedAdjustGridPanel() {
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

  public Rectangle getGridBounds() {
    return new Rectangle(topGrid, leftGrid, rightGrid - leftGrid, bottomGrid - topGrid);
  }

  public void setZoneImage(BufferedImage image) {
    this.image = image;

    topGrid = 0;
    bottomGrid = image.getHeight();
    leftGrid = 0;
    rightGrid = image.getWidth();

    scale = new Scale(image.getWidth(), image.getHeight());
  }

  public void setShowRows(boolean show) {
    showRows = show;
  }

  public void setShowCols(boolean show) {
    showCols = show;
  }

  @Override
  protected void paintComponent(Graphics g) {

    if (image == null) {
      return;
    }

    Dimension size = getSize();

    scale.initialize(size.width, size.height);

    // CALCULATIONS
    Dimension imageSize = getScaledImageSize();
    Point imagePosition = getScaledImagePosition();

    double imgRatio = scale.getScale();

    // handles
    int top = (int) (topGrid * imgRatio);
    int bottom = (int) (bottomGrid * imgRatio);
    int left = (int) (leftGrid * imgRatio);
    int right = (int) (rightGrid * imgRatio);

    // SETUP
    Graphics2D g2d = (Graphics2D) g;

    // BG FILL
    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, size.width, size.height);

    // IMAGE
    g2d.drawImage(image, imagePosition.x, imagePosition.y, imageSize.width, imageSize.height, null);

    // GRID
    g2d.setColor(Color.blue);
    double dx = ((rightGrid - leftGrid) / (float) gridCountX) * imgRatio;
    double dy = ((bottomGrid - topGrid) / (float) gridCountY) * imgRatio;

    // across
    if (showCols) {
      int x = imagePosition.x + left;
      for (int i = 0; i < gridCountX; i++) {
        g2d.drawLine(
            x + (int) (i * dx),
            imagePosition.y + top,
            x + (int) (i * dx),
            imagePosition.y + bottom);
      }
    }

    // down
    if (showRows) {
      int y = imagePosition.y + top;
      for (int i = 0; i < gridCountY; i++) {
        g2d.drawLine(
            imagePosition.x + left,
            y + (int) (i * dy),
            imagePosition.x + right,
            y + (int) (i * dy));
      }
    }

    // HANDLES
    int handleSize = 10;

    Object oldValue = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.setColor(Color.red);

    // Top
    int halfHandleX = imagePosition.x + imageSize.width / 2;
    g2d.fillRect(halfHandleX - handleSize, top + imagePosition.y - 3, handleSize * 2, 3);
    g2d.drawLine(
        imagePosition.x - handleSize,
        top + imagePosition.y,
        imagePosition.x + imageSize.width + handleSize,
        top + imagePosition.y);

    // Bottom
    g2d.fillRect(halfHandleX - handleSize, bottom + imagePosition.y + 1, handleSize * 2, 3);
    g2d.drawLine(
        imagePosition.x - handleSize,
        bottom + imagePosition.y,
        imagePosition.x + imageSize.width + handleSize,
        bottom + imagePosition.y);

    // Left
    int halfHandleY = imagePosition.y + imageSize.height / 2;
    g2d.fillRect(left + imagePosition.x - 3, halfHandleY - handleSize, 3, handleSize * 2);
    g2d.drawLine(
        left + imagePosition.x,
        imagePosition.y - handleSize,
        left + imagePosition.x,
        imagePosition.y + imageSize.height + handleSize);

    // Right
    g2d.fillRect(right + 1 + imagePosition.x, halfHandleY - handleSize, 3, handleSize * 2);
    g2d.drawLine(
        right + imagePosition.x,
        imagePosition.y - handleSize,
        right + imagePosition.x,
        imagePosition.y + imageSize.height + handleSize);

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValue);
  }

  @Override
  public boolean isFocusable() {
    return true;
  }

  public void setGridCountX(int count) {
    gridCountX = count;
    repaint();
  }

  public void setGridCountY(int count) {
    gridCountY = count;
    repaint();
  }

  private float getScaledImageRatio() {
    return getScaledImageSize().width / (float) image.getWidth();
  }

  private Dimension getScaledImageSize() {

    Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
    imageSize.width *= scale.getScale();
    imageSize.height *= scale.getScale();

    return imageSize;
  }

  private Point getScaledImagePosition() {
    Dimension size = getSize();
    Dimension imageSize = getScaledImageSize();

    int imgX = scale.getOffsetX();
    int imgY = scale.getOffsetY();

    return new Point(imgX, imgY);
  }

  private void updateHandles(MouseEvent e) {
    // Convert
    float imgRatio = getScaledImageRatio();
    Point imgPosition = getScaledImagePosition();

    Point location = e.getPoint();
    location.translate(-imgPosition.x, -imgPosition.y);

    location = new Point((int) (location.x / imgRatio), (int) (location.y / imgRatio));

    switch (draggingHandle) {
      case TOP:
        {
          if (location.y < 0) {
            location.y = 0;
          }
          if (location.y > bottomGrid - MINIMUM_GRID_SIZE) {
            location.y = bottomGrid - MINIMUM_GRID_SIZE;
          }

          topGrid = location.y;
          break;
        }
      case BOTTOM:
        {
          if (location.y < topGrid + MINIMUM_GRID_SIZE) {
            location.y = topGrid + MINIMUM_GRID_SIZE;
          }
          if (location.y > image.getHeight()) {
            location.y = image.getHeight();
          }

          bottomGrid = location.y;
          break;
        }
      case LEFT:
        {
          if (location.x < 0) {
            location.x = 0;
          }
          if (location.x > rightGrid - MINIMUM_GRID_SIZE) {
            location.x = rightGrid - MINIMUM_GRID_SIZE;
          }

          leftGrid = location.x;
          break;
        }
      case RIGHT:
        {
          if (location.x < leftGrid + MINIMUM_GRID_SIZE) {
            location.x = leftGrid + MINIMUM_GRID_SIZE;
          }
          if (location.x > image.getWidth()) {
            location.x = image.getWidth();
          }

          rightGrid = location.x;
          break;
        }
    }
    repaint();
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

  ////
  // MOUSE LISTENER
  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {

    if (SwingUtilities.isLeftMouseButton(e)) {
      float imgRatio = getScaledImageRatio();
      Point imgPos = getScaledImagePosition();

      int top = (int) (topGrid * imgRatio) + imgPos.y;
      int bottom = (int) (bottomGrid * imgRatio) + imgPos.y;
      int left = (int) (leftGrid * imgRatio) + imgPos.x;
      int right = (int) (rightGrid * imgRatio) + imgPos.x;

      int distTop = Math.abs(e.getY() - top);
      int distBottom = Math.abs(e.getY() - bottom);
      int distLeft = Math.abs(e.getX() - left);
      int distRight = Math.abs(e.getX() - right);

      int dist = distTop;
      draggingHandle = Handle.TOP;

      if (distBottom < dist) {
        dist = distBottom;
        draggingHandle = Handle.BOTTOM;
      }
      if (distLeft < dist) {
        dist = distLeft;
        draggingHandle = Handle.LEFT;
      }
      if (distRight < dist) {
        dist = distRight;
        draggingHandle = Handle.RIGHT;
      }

      updateHandles(e);
    } else {
      mouseX = e.getX();
      mouseY = e.getY();
    }
  }

  public void mouseReleased(MouseEvent e) {
    draggingHandle = null;

    repaint();
  }

  ////
  // MOUSE MOTION LISTENER
  public void mouseDragged(MouseEvent e) {

    if (SwingUtilities.isLeftMouseButton(e)) {
      updateHandles(e);
    } else {
      int offsetX = scale.getOffsetX() + e.getX() - mouseX;
      int offsetY = scale.getOffsetY() + e.getY() - mouseY;

      scale.setOffset(offsetX, offsetY);

      mouseX = e.getX();
      mouseY = e.getY();
    }

    repaint();
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

    if (e.getWheelRotation() > 0) {
      scale.zoomOut(e.getX(), e.getY());
    } else {
      scale.zoomIn(e.getX(), e.getY());
    }

    repaint();
  }
}
