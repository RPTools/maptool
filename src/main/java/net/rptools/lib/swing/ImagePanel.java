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
package net.rptools.lib.swing;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.*;
import net.rptools.lib.CodeTimer;

@SuppressWarnings("serial")
public class ImagePanel extends JComponent
    implements Scrollable,
        DragGestureListener,
        DragSourceListener,
        MouseListener,
        DragSourceMotionListener {

  public enum SelectionMode {
    SINGLE,
    MULTIPLE,
    NONE
  };

  private ImagePanelModel model;

  private int gridSize = 50;
  private final Dimension gridPadding = new Dimension(9, 11);
  private final int captionPadding = 5;

  private final Map<Rectangle, Integer> imageBoundsMap = new HashMap<Rectangle, Integer>();

  private boolean isDraggingEnabled = true;
  private boolean showCaptions = true;
  private boolean showImageBorder = false;

  private SelectionMode selectionMode = SelectionMode.NONE;

  private final List<Object> selectedIDList = new ArrayList<Object>();
  private final List<SelectionListener> selectionListenerList = new ArrayList<SelectionListener>();

  private int fontHeight = -1;

  public ImagePanel() {
    DragSource.getDefaultDragSource()
        .createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    addMouseListener(this);

    // Register with the ToolTipManager so that tooltips from the
    // renderer show through.
    ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    toolTipManager.registerComponent(this);
  }

  /**
   * Ensures that the font height has been set. Needed for methods that do measuring outside the
   * paint loop.
   *
   * @param g The graphics context from which to obtain font metrics. If this is null, the method
   *     will create and dispose its own graphics context.
   */
  private void ensureFontHeight(Graphics2D g) {
    if (fontHeight == -1) {
      boolean mustDisposeGraphics = false;
      if (g == null) {
        g =
            Objects.requireNonNullElseGet(
                (Graphics2D) getGraphics(),
                () -> {
                  var img =
                      GraphicsEnvironment.getLocalGraphicsEnvironment()
                          .getDefaultScreenDevice()
                          .getDefaultConfiguration()
                          .createCompatibleImage(1, 1, Transparency.OPAQUE);
                  return (Graphics2D) img.getGraphics();
                });
        mustDisposeGraphics = true;
      }
      fontHeight = g.getFontMetrics(UIManager.getFont("Label.font")).getHeight();
      if (mustDisposeGraphics) {
        g.dispose();
      }
    }
  }

  public void setGridSize(int size) {
    // Min
    size = Math.max(25, size);
    if (size != gridSize) {
      gridSize = size;
      revalidate();
      repaint();
    }
  }

  public int getGridSize() {
    return gridSize;
  }

  public void setDraggingEnabled(boolean enabled) {
    isDraggingEnabled = enabled;
  }

  public void setSelectionMode(SelectionMode mode) {
    if (selectionMode != mode) {
      selectionMode = mode;
      selectedIDList.clear();
      repaint();
    }
  }

  public void setShowCaptions(boolean enabled) {
    if (showCaptions != enabled) {
      showCaptions = enabled;
      repaint();
    }
  }

  public void setShowImageBorders(boolean enabled) {
    if (showImageBorder != enabled) {
      showImageBorder = enabled;
      repaint();
    }
  }

  public ImagePanelModel getModel() {
    return model;
  }

  public void setModel(ImagePanelModel model) {
    if (this.model != model) {
      this.model = model;
      revalidate();

      final JScrollPane scrollPane =
          (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);

      if (scrollPane != null) {
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.revalidate();
        scrollPane.repaint();
      }
    }
  }

  public List<Object> getSelectedIds() {
    List<Object> list = new ArrayList<Object>();
    list.addAll(selectedIDList);
    return list;
  }

  public void clearSelection() {
    selectedIDList.clear();
    fireSelectionEvent();
  }

  public void addSelectionListener(SelectionListener listener) {
    selectionListenerList.add(listener);
  }

  public void removeSelectionListener(SelectionListener listener) {
    selectionListenerList.remove(listener);
  }

  @Override
  public boolean isOpaque() {
    return true;
  }

  @Override
  protected void paintComponent(Graphics gfx) {
    var g = (Graphics2D) gfx;
    CodeTimer timer = new CodeTimer("ImagePanel.paintComponent");
    timer.setEnabled(false); // Change this to turn on perf data to System.out

    Rectangle clipBounds = g.getClipBounds();
    Dimension size = getSize();
    var savedFont = g.getFont();
    g.setFont(UIManager.getFont("Label.font"));
    FontMetrics fm = g.getFontMetrics();
    fontHeight = fm.getHeight();

    g.setColor(getBackground());
    g.fillRect(0, 0, size.width, size.height);

    if (model == null) {
      return;
    }
    imageBoundsMap.clear();

    int x = gridPadding.width;
    int y = gridPadding.height;
    int numToProcess = model.getImageCount();
    String timerField = null;
    if (timer.isEnabled()) {
      timerField = "time to process " + numToProcess + " images";
      timer.start(timerField);
    }
    for (int i = 0; i < numToProcess; i++) {
      Image image;

      Rectangle bounds = new Rectangle(x, y, gridSize, gridSize);
      imageBoundsMap.put(
          new Rectangle(
              x, y, gridSize, gridSize + (showCaptions ? captionPadding + fontHeight : 0)),
          i);

      // Background
      Paint paint = model.getBackground(i);
      if (paint != null) {
        g.setPaint(paint);
        g.fillRect(x - 2, y - 2, gridSize + 4, gridSize + 4); // bleed out a little
      }
      if (bounds.intersects(clipBounds)) {
        image = model.getImage(i);
        if (image != null) {
          Dimension dim = constrainSize(image, gridSize);
          var savedRenderingHints = g.getRenderingHints();
          if (dim.width < image.getWidth(null) || dim.height < image.getHeight(null)) {
            g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          } else if (dim.width > image.getWidth(null) || dim.height > image.getHeight(null)) {
            g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          }
          g.drawImage(
              image,
              x + (gridSize - dim.width) / 2,
              y + (gridSize - dim.height) / 2,
              dim.width,
              dim.height,
              this);

          // Image border
          g.setRenderingHints(savedRenderingHints);
          if (showImageBorder) {
            g.setColor(Color.black);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
          }
        }
      }
      // Selected
      if (selectedIDList.contains(model.getID(i))) {
        // TODO: Let the user pick the border
        ImageBorder.RED.paintAround(
            (Graphics2D) g, bounds.x, bounds.y, bounds.width, bounds.height);
      }
      // Decorations
      Image[] decorations = model.getDecorations(i);
      if (decorations != null) {
        int offx = x;
        int offy = y + gridSize;
        int rowHeight = 0;
        for (Image decoration : decorations) {
          g.drawImage(decoration, offx, offy - decoration.getHeight(null), this);

          rowHeight = Math.max(rowHeight, decoration.getHeight(null));
          offx += decoration.getWidth(null);
          if (offx > gridSize) {
            offx = x;
            offy -= rowHeight + 2;
            rowHeight = 0;
          }
        }
      }
      // Caption
      if (showCaptions) {
        String caption = model.getCaption(i);
        if (caption != null) {
          boolean nameTooLong = false;
          int strWidth = fm.stringWidth(caption);
          if (strWidth > bounds.width) {
            var avgCharWidth = (double) strWidth / caption.length();
            var fittableChars = (int) (bounds.width / avgCharWidth);
            caption = String.format("%s...", caption.substring(0, fittableChars - 2));
            strWidth = fm.stringWidth(caption);
          }
          int cx = x + (gridSize - strWidth) / 2;
          int cy = y + gridSize + fm.getHeight();

          g.setColor(getForeground());
          var savedRenderingHints = g.getRenderingHints();
          g.setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          g.drawString(caption, cx, cy);
          g.setRenderingHints(savedRenderingHints);
        }
      }
      // Line wrap
      x += gridSize + gridPadding.width;
      if ((x + gridSize) > (size.width - gridPadding.width)) {
        x = gridPadding.width;
        y += gridSize + gridPadding.height;
        if (showCaptions) {
          y += fontHeight;
        }
      }
    }
    g.setFont(savedFont);
    if (timer.isEnabled()) {
      timer.stop(timerField);
      System.out.println(timer);
    }
  }

  /**
   * Go through the image bounds map to see if any of the entries encompass the passed in X,Y values
   * and return the index.
   *
   * @param x
   * @param y
   * @return the index or -1 if not found
   */
  protected int getIndex(int x, int y) {
    for (Entry<Rectangle, Integer> entry : imageBoundsMap.entrySet()) {
      if (entry.getKey().contains(x, y)) {
        return entry.getValue();
      }
    }
    return -1;
  }

  /**
   * Get the ID for the image currently displayed at X,Y
   *
   * @param x
   * @param y
   * @return Asset ID or null if no selection
   */
  protected Object getImageIDAt(int x, int y) {
    int index = getIndex(x, y);
    if (index == -1 || model == null) {
      return null;
    }
    return model.getID(index);
  }

  protected void fireSelectionEvent() {
    List<Object> selectionList = Collections.unmodifiableList(selectedIDList);
    for (int i = 0; i < selectionListenerList.size(); i++) {
      selectionListenerList.get(i).selectionPerformed(selectionList);
    }
  }

  private Dimension constrainSize(Image image, int size) {
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);

    if (imageWidth == imageHeight) {
      return new Dimension(size, size);
    }
    int width = 0;
    int height = 0;
    if (imageWidth > imageHeight) {
      width = size;
      height = (int) (imageHeight * ((float) size) / imageWidth);
    } else {
      height = size;
      width = (int) (imageWidth * ((float) size) / imageHeight);
    }
    return new Dimension(width, height);
  }

  @Override
  public Dimension getPreferredSize() {
    if (model == null || model.getImageCount() == 0) {
      return new Dimension();
    }

    ensureFontHeight(null);
    int width = getWidth();

    int itemWidth = gridSize + gridPadding.width;
    int itemHeight =
        gridSize + gridPadding.height + (showCaptions ? fontHeight + captionPadding : 0);
    int rowCount;
    if (width < gridSize + gridPadding.width * 2) {
      rowCount = model.getImageCount();
    } else {
      rowCount = model.getImageCount() / (width / itemWidth);
    }
    int height = rowCount * itemHeight;
    return new Dimension(width, height);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  protected int getImageIndexAt(int x, int y) {
    for (Rectangle rect : imageBoundsMap.keySet()) {
      if (rect.contains(x, y)) {
        return imageBoundsMap.get(rect);
      }
    }
    return -1;
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    if (getModel() == null || getIndex(event.getX(), event.getY()) == -1) {
      return null;
    }

    // Jamz: Updated tooltip to include image Dimensions...
    return getModel().getCaption(getIndex(event.getX(), event.getY()), true);
  }

  // SCROLLABLE
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    ensureFontHeight(null);
    return ((gridSize + gridPadding.height * 2) + (showCaptions ? fontHeight + captionPadding : 0));
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    Dimension parentSize = SwingUtilities.getAncestorOfClass(JScrollPane.class, this).getSize();
    return getPreferredSize().height < parentSize.height;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return gridSize / 4;
  }

  // DRAG GESTURE LISTENER
  @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    if (model == null || !isDraggingEnabled) {
      return;
    }
    int index = getImageIndexAt(dge.getDragOrigin().x, dge.getDragOrigin().y);
    if (index < 0) {
      return;
    }
    Transferable transferable = model.getTransferable(index);
    if (transferable == null) {
      return;
    }
    // dge.startDrag(Toolkit.getDefaultToolkit().createCustomCursor(model.getImage(index), new
    // Point(0, 0), "Thumbnail"), transferable, this);
    dge.startDrag(getDragCursor(), transferable, this);
    DragSource.getDefaultDragSource().addDragSourceMotionListener(this);
  }

  protected Cursor getDragCursor() {
    return null;
  }

  // DRAG SOURCE LISTENER
  @Override
  public void dragDropEnd(DragSourceDropEvent dsde) {
    DragSource.getDefaultDragSource().removeDragSourceMotionListener(this);
  }

  @Override
  public void dragEnter(DragSourceDragEvent dsde) {}

  @Override
  public void dragExit(DragSourceEvent dse) {}

  @Override
  public void dragOver(DragSourceDragEvent dsde) {}

  @Override
  public void dropActionChanged(DragSourceDragEvent dsde) {}

  // DRAG SOURCE MOTION LISTENER
  @Override
  public void dragMouseMoved(DragSourceDragEvent dsde) {}

  // MOUSE LISTENER
  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {
    if (selectionMode == SelectionMode.NONE) {
      return;
    }
    Object imageID = getImageIDAt(e.getX(), e.getY());

    // TODO: Handle shift too
    if (!SwingUtil.isControlDown(e) || selectionMode == SelectionMode.SINGLE) {
      selectedIDList.clear();
    }
    if (imageID != null && !selectedIDList.contains(imageID)) {
      selectedIDList.add(imageID);
      repaint();
    }
    fireSelectionEvent();
  }

  @Override
  public void mouseReleased(MouseEvent e) {}
}
