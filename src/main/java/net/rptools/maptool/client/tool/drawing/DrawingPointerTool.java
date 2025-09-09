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
package net.rptools.maptool.client.tool.drawing;

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.Serial;
import java.util.*;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.events.ZoneActivated;
import net.rptools.maptool.client.swing.colorpicker.ColorPicker;
import net.rptools.maptool.client.swing.label.FlatImageLabel;
import net.rptools.maptool.client.swing.label.FlatImageLabelFactory;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.drawpanel.DrawPanelPopupMenu;
import net.rptools.maptool.client.ui.drawpanel.DrawablesPanel;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tool for interacting with drawn elements on a map layer using the mouse. Handles either Drawings
 * or Templates, with full interactions listed below:
 *
 * <h6>If mouse over a drawing or template:</h6>
 *
 * <ul>
 *   <li><kbd>Hover</kbd> show the name label (if it has a name)
 *   <li><kbd>Left Click</kbd> select by {@link Area}
 *   <li><kbd>Left Click</kbd>+<kbd>SHIFT</kbd> multiselect
 *   <li><kbd>Left Click</kbd>+<kbd>CTRL</kbd> select other stack items
 *   <li><kbd>Right Click</kbd> shows the {@link DrawPanelPopupMenu}
 * </ul>
 *
 * <h6>If mouse not over a drawing or template:</h6>
 *
 * <ul>
 *   <li><kbd>Left Drag</kbd> draggable selection box selecting by {@link Rectangle} bounds
 *   <li><kbd>Left Drag</kbd>+<kbd>SHIFT</kbd> multiselect draggable selection box
 *   <li><kbd>Left Drag</kbd>+<kbd>CTRL</kbd> select matching the stroke color picker
 *   <li><kbd>Left Drag</kbd>+<kbd>ALT</kbd> select matching the fill color picker
 * </ul>
 *
 * <h6>Once selected:</h6>
 *
 * <ul>
 *   <li>Shows the name label (if it has a name)
 *   <li><kbd>CTRL</kbd>+<kbd>V</kbd> duplicate selected (templates only)
 *   <li><kbd>DELETE</kbd> delete selected
 * </ul>
 *
 * <h6>Once selected (templates only):</h6>
 *
 * <ul>
 *   <li><kbd>Left Press</kbd> Show the template(s) size/radius
 *   <li><kbd>Left Drag</kbd> if a GM or server movement policy permits, move the template(s), will
 *       then show the movement distance according using the movement metric setting.
 * </ul>
 *
 * <h6>Once selected (single template only):</h6>
 *
 * <ul>
 *   <li><kbd>Left Drag</kbd>+<kbd>CTRL</kbd> Change the template's Size/Radius
 *   <li><kbd>Left Drag</kbd>+<kbd>ALT</kbd> Change the template's Path Vertex (if applicable)
 * </ul>
 *
 * @see TemplatePointerTool
 */
public class DrawingPointerTool extends DefaultTool implements ZoneOverlay, MouseListener {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables & Enumerations
   *-------------------------------------------------------------------------------------------*/

  @Serial private static final long serialVersionUID = 8817168063984434244L;

  /**
   * Used to keep track of which pointer tool we are using, as using {@link DrawingPointerTool}
   * should not affect templates and conversely {@link TemplatePointerTool} should only affect
   * templates.
   */
  private static Object selectedTool = null;

  /** Stores the selected {@link DrawnElement}'s {@link Drawable} {@link GUID}s. */
  private static final Set<GUID> selectedDrawableIdSet = new HashSet<>();

  /**
   * Stores a copy of the dragged {@link DrawnElement}s to which we will apply temporary changes
   * whilst dragging, and will use to apply changes to the originals on drag completion.
   */
  private static final Set<DrawnElement> draggedDrawnElementSet = new HashSet<>();

  /**
   * Factory to generate {@link FlatImageLabel}s for drawing/template name labels. Will be assigned
   * prior to each use as user color preferences may change.
   */
  private static FlatImageLabelFactory flatImageLabelFactory;

  /**
   * Stores the factory generated {@link FlatImageLabel}s by each {@link Drawable}'s {@link GUID}
   * Currently, this cache is flushed when the layer or zone changes, or when this tool is
   * deselected.
   */
  private static final HashMap<GUID, FlatImageLabel> flatImageLabelCache = new HashMap<>();

  /** Reuse the delete action available from the {@link DrawPanelPopupMenu} */
  private static final DrawPanelPopupMenu.DeleteDrawingAction deleteAction =
      new DrawPanelPopupMenu.DeleteDrawingAction(selectedDrawableIdSet);

  /** Reuse the duplicate action available from the {@link DrawPanelPopupMenu} */
  private static final DrawPanelPopupMenu.DuplicateDrawingAction duplicateAction =
      new DrawPanelPopupMenu.DuplicateDrawingAction(selectedDrawableIdSet);

  private static final Logger LOGGER = LogManager.getLogger(DrawingPointerTool.class);

  /** Color picker related variables */
  private boolean isSnapToGridSelected;

  private boolean isEraseSelected;
  private boolean isEraser;

  /** Interaction variables */
  private boolean isDraggingDrawings = false;

  private boolean isDraggingSelectionBox = false;
  private boolean isMouseMoving = false;
  private Rectangle drawingSelectionBox = null;

  /** Interaction drawn element variables */
  private DrawnElement drawnElementAtMouse = null;

  private DrawnElement drawnElementAtMouseMove = null;
  private DrawnElement drawnElementAtMouseMovePrevious = null;

  /** Interaction positional variables */
  private Point dragStartPoint = null;

  private ZonePoint dragStartVertex = null;
  private ZonePoint dragWorkingCell = null;

  /** An enumeration of template cursor types. */
  private enum templateCursorType {
    CROSS,
    CELL
  }

  /**
   * The width of the cursor. Since the cursor is a cross, this is the width of the horizontal bar
   * and the height of the vertical bar. Always make it an odd number to keep it aligned on the grid
   * properly.
   */
  public static final int CURSOR_WIDTH = 25;

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  public DrawingPointerTool() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden Tool Class Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public String getTooltip() {
    return "tool.drawingpointer.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.drawingpointer.instructions";
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden DefaultTool Class Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    if (MapTool.getPlayer().isGM()) {
      MapTool.getFrame()
          .showControlPanel(MapTool.getFrame().getColorPicker(), getLayerSelectionDialog());
    } else {
      MapTool.getFrame().showControlPanel(MapTool.getFrame().getColorPicker());
    }
    renderer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    MapTool.getFrame().getColorPicker().setSnapSelected(isSnapToGridSelected);
    MapTool.getFrame().getColorPicker().setEraseSelected(isEraseSelected);
    selectedTool = MapTool.getFrame().getToolbox().getSelectedTool().getClass();
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    selectedDrawableIdSet.clear();
    draggedDrawnElementSet.clear();
    flatImageLabelCache.clear();
    MapTool.getFrame().removeControlPanel();
    renderer.setCursor(Cursor.getDefaultCursor());
    isSnapToGridSelected = MapTool.getFrame().getColorPicker().isSnapSelected();
    isEraseSelected = MapTool.getFrame().getColorPicker().isEraseSelected();
    selectedTool = null;

    super.detachFrom(renderer);
  }

  /**
   * Set <kbd>DELETE</kbd> to delete selected drawn elements. Set <kbd>CTRL</kbd>+<kbd>V</kbd> to
   * duplicate selected drawn elements.
   *
   * @param actionMap What keys do what action.
   */
  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction);
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), duplicateAction);
  }

  /**
   * Either drag drawings, or draw a draggable selection box.
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseDragged(MouseEvent e) {
    super.mouseDragged(e);

    if (isDraggingDrawings) {
      ZonePoint dragTargetCell = getCellAtMouse(e);
      if (!dragWorkingCell.equals(dragTargetCell)) {

        for (DrawnElement de : draggedDrawnElementSet) {
          // Currently drag templates only
          Drawable d = de.getDrawable();
          if (d instanceof AbstractTemplate at) {
            updateDraggedDrawnElements(e, at);
          }
        }
        dragWorkingCell = dragTargetCell;
        renderer.repaint();
      }
    } else if (isDraggingSelectionBox) {
      int x1 = dragStartPoint.x;
      int y1 = dragStartPoint.y;
      int x2 = e.getX();
      int y2 = e.getY();
      drawingSelectionBox.x = Math.min(x1, x2);
      drawingSelectionBox.y = Math.min(y1, y2);
      drawingSelectionBox.width = Math.abs(x1 - x2);
      drawingSelectionBox.height = Math.abs(y1 - y2);
      renderer.repaint();
    }
  }

  /**
   * Show the appropriate cursor and display a name label
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);

    // Do nothing if already dragging stuff
    if (isDraggingMap() || isDraggingSelectionBox || isDraggingDrawings) {
      return;
    }

    isMouseMoving = true;
    drawnElementAtMouseMove = getDrawnElementAtMouse(e);
    if (drawnElementAtMouseMovePrevious != drawnElementAtMouseMove) {
      drawnElementAtMouseMovePrevious = drawnElementAtMouseMove;
      if (drawnElementAtMouseMove != null) {
        renderer.setCursor(Cursor.getDefaultCursor());
        GUID id = drawnElementAtMouseMove.getDrawable().getId();
        if (!flatImageLabelCache.containsKey(id)) {
          flatImageLabelFactory = new FlatImageLabelFactory();
          flatImageLabelCache.put(
              id, flatImageLabelFactory.getMapImageLabel(drawnElementAtMouseMove));
        }
        renderer.repaint();
      } else {
        renderer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
    }
    isMouseMoving = false;
  }

  /**
   * Select a drawn element and/or prepare to start dragging.
   *
   * @param e the event to be processed
   */
  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);

    // Do not disrupt current dragging operations by pressing other mouse buttons
    if (isDraggingMap() || isDraggingSelectionBox || isDraggingDrawings) {
      return;
    }

    // So that keystrokes end up in the right place
    renderer.requestFocusInWindow();

    boolean multiSelect = e.isShiftDown();

    if (SwingUtilities.isLeftMouseButton(e)) {
      drawnElementAtMouse = getDrawnElementAtMouse(e);

      if (drawnElementAtMouse != null) {
        // Left mouse pressed over a drawing
        GUID id = drawnElementAtMouse.getDrawable().getId();
        if (selectedDrawableIdSet.contains(id)) {
          if (!multiSelect) {
            dragDrawnElementsStart(e);
          } else {
            selectedDrawableIdSet.remove(id);
          }
        } else {
          if (!multiSelect) {
            selectedDrawableIdSet.clear();
            draggedDrawnElementSet.clear();
          }
          selectedDrawableIdSet.add(id);
          dragDrawnElementsStart(e);
        }
        updateDrawablesPanel();
        isDraggingSelectionBox = false;
        drawingSelectionBox = null;
      } else {
        // Left mouse pressed over something which is not a drawing,
        // get ready to draw selection box
        isDraggingSelectionBox = true;
        isDraggingDrawings = false;
        dragStartPoint = new Point(e.getX(), e.getY());
        drawingSelectionBox = new Rectangle(dragStartPoint.x, dragStartPoint.y, 0, 0);
      }
    }
    renderer.repaint();
  }

  /**
   * When a mouse press is released, either complete the drag operations, show the popup menu, etc.
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseReleased(MouseEvent e) {

    // So that keystrokes end up in the right place
    renderer.requestFocusInWindow();

    Zone zone = getZone();
    boolean multiSelect = e.isShiftDown();

    // RIGHT MOUSE BUTTON - Select drawing if unselected and show popup menu
    if (SwingUtilities.isRightMouseButton(e)
        && !isDraggingDrawings
        && !isDraggingSelectionBox
        && !isDraggingMap()) {
      DrawnElement de = getDrawnElementAtMouse(e);
      if (de != null) {
        GUID id = de.getDrawable().getId();
        if (!selectedDrawableIdSet.contains(id)) {
          if (!multiSelect) {
            selectedDrawableIdSet.clear();
            draggedDrawnElementSet.clear();
          }
          selectedDrawableIdSet.add(id);
        }
        updateDrawablesPanel();

        // Select the drawing or template relevant to the current tool
        boolean isTemplate = isTemplate(de);
        if (selectedTool == TemplatePointerTool.class && isTemplate
            || selectedTool == DrawingPointerTool.class && !isTemplate) {
          new DrawPanelPopupMenu(selectedDrawableIdSet, e.getX(), e.getY(), renderer, de, true)
              .showPopup(renderer);
        }
      }
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      cancelMapDrag(); // We no longer drag the map. Fixes bug #616
      return;
    }

    // LEFT MOUSE BUTTON - Complete drag operations, either:
    // 1. update server with moved templates
    // 2. selecting templates within the bounds of the selection box
    if (SwingUtilities.isLeftMouseButton(e)) {
      // If we are moving templates, update each drawing on the server and tidy up
      if (isDraggingDrawings) {
        updateOriginalDrawnElements();
        isDraggingDrawings = false;
      }

      // If we have drawn a template selection box, select templates within
      if (isDraggingSelectionBox) {

        if (!multiSelect) {
          selectedDrawableIdSet.clear();
          draggedDrawnElementSet.clear();
        }

        // Derive a zone rectangle from the screen rectangle
        ZonePoint zpMin =
            new ScreenPoint(drawingSelectionBox.getMinX(), drawingSelectionBox.getMinY())
                .convertToZone(renderer);
        ZonePoint zpMax =
            new ScreenPoint(drawingSelectionBox.getMaxX(), drawingSelectionBox.getMaxY())
                .convertToZone(renderer);
        Rectangle zoneTemplateSelectionBox =
            new Rectangle(zpMin.x, zpMin.y, zpMax.x - zpMin.x, zpMax.y - zpMin.y);

        // GMs can select templates from any selected layer, whereas players are limited
        List<DrawnElement> drawableList = getDrawnElementsOnLayerList(false);

        for (DrawnElement de : drawableList) {
          Drawable d = de.getDrawable();
          // Only select drawing types relevant to the tool
          // Object selectedTool = MapTool.getFrame().getToolbox().getSelectedTool().getClass();
          boolean isTemplate = isTemplate(de);
          if (selectedTool == TemplatePointerTool.class && isTemplate
              || selectedTool == DrawingPointerTool.class && !isTemplate) {

            GUID id = d.getId();
            // Check if the template bounds is within the bounds of the selection box
            if (zoneTemplateSelectionBox.contains(d.getBounds(zone))) {

              boolean isControlCheck = true;
              boolean isAltCheck = true;
              // CTRL key - check if the template border color matches the color picker
              if (e.isControlDown()) {
                isControlCheck =
                    drawablePaintToString(de.getPen().getPaint())
                        .equals(drawablePaintToString(getPen().getPaint()));
              }
              // ALT key - check if the template fill color matches the color picker
              if (e.isAltDown()) {
                isAltCheck =
                    drawablePaintToString(de.getPen().getBackgroundPaint())
                        .equals(drawablePaintToString(getPen().getBackgroundPaint()));
              }
              if (isControlCheck && isAltCheck) {
                if (selectedDrawableIdSet.contains(id)) {
                  if (multiSelect) {
                    selectedDrawableIdSet.remove(id);
                  }
                } else {
                  selectedDrawableIdSet.add(id);
                }
              }
            }
          }
        } // end for
      }
    }
    isDraggingSelectionBox = false;
    drawingSelectionBox = null;
    dragStartPoint = null;
    updateDrawablesPanel();
    renderer.repaint();

    drawnElementAtMouse = getDrawnElementAtMouse(e);
    if (drawnElementAtMouse != null) {
      renderer.setCursor(Cursor.getDefaultCursor());
    } else {
      renderer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    super.mouseReleased(e);
  }

  /**
   * If a layer changes, clear the decks.
   *
   * @param layer the map layer.
   */
  @Override
  protected void selectedLayerChanged(Zone.Layer layer) {
    super.selectedLayerChanged(layer);
    selectedDrawableIdSet.clear();
    draggedDrawnElementSet.clear();
    flatImageLabelCache.clear();
    try {
      // Try to clear the drawables panel
      MapTool.getFrame().getDrawablesPanel().clearSelectedIds();
    } catch (Exception e) {
      // On MapTool start it will hang at splash screen with a InvocationTargetException
      // as draw panel not available yet, but is there a better way to handle this?
    }
  }

  /**
   * Pressing <kbd>ESC</kbd> resets the tool. There are two levels of reset:
   *
   * <ol>
   *   <li>If the user is dragging templates or dragging a selection box then revert.
   *   <li>Otherwise, if we have selected some drawn elements then unselect them.
   */
  @Override
  protected void resetTool() {
    if (isDraggingDrawings || isDraggingSelectionBox) {
      isDraggingDrawings = false;
      isDraggingSelectionBox = false;
      drawingSelectionBox = null;
      renderer.setCursor(Cursor.getDefaultCursor());
    } else if (!selectedDrawableIdSet.isEmpty()) {
      selectedDrawableIdSet.clear();
      draggedDrawnElementSet.clear();
      flatImageLabelCache.clear();
      updateDrawablesPanel();
    }
    renderer.repaint();
  }

  /*---------------------------------------------------------------------------------------------
   * ZoneOverlay Interface Overridden Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Paints:
   *
   * <ol>
   *   <li>drawing label names
   *   <li>if the selection box is being dragged
   *   <li>selected border box around selected templates
   */
  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {

    if (renderer != null) {

      // Paints a selected border box around the pre-dragged drawing position
      for (var id : selectedDrawableIdSet) {
        DrawnElement de = renderer.getZone().getDrawnElement(id);
        if (de == null) continue;
        paintSelectedBorder(g, de);
      }

      if (isDraggingDrawings) {
        // Paints the dragged drawings, movement line, and movement distance label
        paintDraggedDrawings(g);
      }

      if (drawingSelectionBox != null) {
        paintSelectionBox(g);
      }

      // Paints the select label name (if it has one)
      for (GUID id : selectedDrawableIdSet) {
        DrawnElement de = renderer.getZone().getDrawnElement(id);
        paintDrawingNameLabel(g, de);
      }

      // Paints the mouse move label name (if it has one)
      if (drawnElementAtMouseMove != null) {
        paintDrawingNameLabel(g, drawnElementAtMouseMove);
      }
    }
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get ready to drag drawn elements.
   *
   * <p>Currently dragging is limited to templates only so provide visual feedback via the cursor
   * what we can drag.
   *
   * @param e The mouse event.
   */
  private void dragDrawnElementsStart(MouseEvent e) {

    if (isTemplate(drawnElementAtMouse)) {
      renderer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    isDraggingDrawings = true;
    isDraggingSelectionBox = false;
    dragStartPoint = new Point(e.getX(), e.getY());
    dragWorkingCell = getCellAtMouse(e);
    if (drawnElementAtMouse.getDrawable() instanceof AbstractTemplate at) {
      dragStartVertex = new ZonePoint(at.getVertex());
    }
    setDraggedDrawnElementsSet();
  }

  /**
   * Convert a DrawablePaint to a string representation of a hex color or asset id. This is used for
   * matching pen stroke or fill during draggable selection with <kbd>CTRL</kbd> and/or
   * <kbd>ALT</kbd>
   *
   * @param drawablePaint the DrawablePaint
   * @return color as "#000000" or image as "asset://MD5Key"
   */
  private String drawablePaintToString(DrawablePaint drawablePaint) {
    if (drawablePaint instanceof DrawableColorPaint) {
      return "#"
          + Integer.toHexString(((DrawableColorPaint) drawablePaint).getColor()).substring(2);
    }
    if (drawablePaint instanceof DrawableTexturePaint) {
      return "asset://" + ((DrawableTexturePaint) drawablePaint).getAsset().getMD5Key();
    }
    return "";
  }

  /**
   * Calculate the cell closest to a mouse point. Cell coordinates are the upper left corner of the
   * cell.
   *
   * @param e The event to be checked.
   * @return The cell at the mouse point in screen coordinates.
   */
  private ZonePoint getCellAtMouse(MouseEvent e) {
    // Find the cell that the mouse is in.
    ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
    CellPoint cp = getZone().getGrid().convert(mouse);
    ZonePoint working = getZone().getGrid().convert(cp);

    // If the mouse is over halfway to the next vertex, move it there (both X & Y)
    int grid = (int) (getZone().getGrid().getSize() * renderer.getScale());
    if (mouse.x - working.x >= grid / 2) {
      working.x += getZone().getGrid().getSize();
    }
    if (mouse.y - working.y >= grid / 2) {
      working.y += getZone().getGrid().getSize();
    }
    return working;
  }

  /**
   * On the selected map layer, selects a drawn element at the mouse point, using the actual {@link
   * Area} rather than its {@link Rectangle} bounds to enable more accurate selection by mouse.
   *
   * <p>If there are a stack of drawn elements at the mouse point with the layer, frontmost element
   * will be returned. On subsequent calls and if the <kbd>CTRL</kbd> key is down it will return the
   * next element in the stack, cycling back to the frontmost.
   *
   * @param e The mouse event to be checked.
   * @return The drawn element at the mouse point, or <code>null</code> if none found
   * @see DrawnElement
   */
  private DrawnElement getDrawnElementAtMouse(MouseEvent e) {
    Zone zone = getZone();

    // Get list of drawables for the GM or Player, reversing the list so to select the frontmost
    // drawing first
    List<DrawnElement> drawableList = getDrawnElementsOnLayerList(true);
    List<DrawnElement> drawingsAtMouseList = new ArrayList<>();
    DrawnElement previousDrawnElementAtMouse = null;

    // CONSTRUCT A LIST OF RELEVANT DRAWINGS OR TEMPLATES UNDER THE MOUSE
    for (DrawnElement de : drawableList) {
      // Only select drawing types relevant to the selected tool
      boolean isTemplate = isTemplate(de);
      if (selectedTool == TemplatePointerTool.class && isTemplate
          || selectedTool == DrawingPointerTool.class && !isTemplate) {
        Area area = de.getDrawable().getArea(zone);
        ZonePoint zonePos = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
        if (area.contains(new Point(zonePos.x, zonePos.y))) {
          drawingsAtMouseList.add(de);
          if (de.equals(drawnElementAtMouse)) {
            previousDrawnElementAtMouse = de;
          }
        }
      }
    }

    // RETURN A DRAWING (OR NOT)
    if (!drawingsAtMouseList.isEmpty()) {
      // If drawing not found in stack or there is only one or the mouse moving just return the
      // frontmost
      if (previousDrawnElementAtMouse == null || drawingsAtMouseList.size() == 1 || isMouseMoving) {
        return drawingsAtMouseList.getFirst();
      } else {
        if (e.isControlDown()) {
          // Get the next template in the stack
          drawingsAtMouseList = drawingsAtMouseList.reversed();
          for (int i = 0; i < drawingsAtMouseList.size(); i++) {
            if (drawingsAtMouseList.get(i).equals(previousDrawnElementAtMouse)) {
              // Check if at the last template
              if (i == drawingsAtMouseList.size() - 1) {
                return drawingsAtMouseList.getFirst(); // Cycle back to the first
              } else {
                return drawingsAtMouseList.get(i + 1); // Get the next one
              }
            }
          } // end for
        } else {
          return drawingsAtMouseList.getFirst();
        }
      }
    } else {
      return null;
    }
    return null;
  }

  /**
   * Get a {@link List} of {@link DrawnElement}s on the selected {@link Zone.Layer}. A GM can get a
   * list from any layer, whereas Players are limited to the player (token) layer.
   *
   * @param reverse Whether to reverse the order, or not.
   * @return A list of drawn elements.
   */
  private List<DrawnElement> getDrawnElementsOnLayerList(boolean reverse) {
    Zone zone = getZone();
    List<DrawnElement> drawnElementList;
    if (MapTool.getPlayer().isGM()) {
      drawnElementList = zone.getDrawnElements(getSelectedLayer());
    } else {
      drawnElementList = zone.getDrawnElements(Zone.Layer.getDefaultPlayerLayer());
    }
    if (reverse) {
      drawnElementList = drawnElementList.reversed();
    }
    return drawnElementList;
  }

  private AffineTransform getPaintTransform(ZoneRenderer renderer) {
    AffineTransform transform = new AffineTransform();
    transform.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    transform.scale(renderer.getScale(), renderer.getScale());
    return transform;
  }

  /**
   * Get the Pen of the Color Picker
   *
   * @return Pen
   */
  protected Pen getPen() {
    Pen pen = new Pen(MapTool.getFrame().getPen());
    pen.setEraser(isEraser);

    ColorPicker picker = MapTool.getFrame().getColorPicker();
    if (picker.isFillForegroundSelected()) {
      pen.setForegroundMode(Pen.MODE_SOLID);
    } else {
      pen.setForegroundMode(Pen.MODE_TRANSPARENT);
    }
    if (picker.isFillBackgroundSelected()) {
      pen.setBackgroundMode(Pen.MODE_SOLID);
    } else {
      pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
    }
    pen.setSquareCap(picker.isSquareCapSelected());
    pen.setThickness(picker.getStrokeWidth());
    return pen;
  }

  /**
   * Determines the type of cursor used by a template.
   *
   * @param at The template.
   * @return Either a CROSS or CELL
   */
  private templateCursorType getTemplateCursorType(AbstractTemplate at) {
    String templateType = getTemplateType(at);
    if (templateType.equals("RadiusTemplate")
        || templateType.equals("ConeTemplate")
        || templateType.equals("LineTemplate")) {
      return templateCursorType.CROSS;
    } else {
      return templateCursorType.CELL;
    }
  }

  /**
   * Helper method to get the path vertex for the template (for known template types that have
   * them). *
   *
   * @param at The template.
   * @return The zone point for the path vertex, or <code>null</code>.
   */
  private ZonePoint getTemplatePathVertex(AbstractTemplate at) {
    ZonePoint templatePathVertex = null;
    String templateType = getTemplateType(at);
    if (templateType.equals("LineTemplate")) {
      LineTemplate lt = (LineTemplate) at;
      templatePathVertex = new ZonePoint(lt.getPathVertex());
    } else if (templateType.equals("LineCellTemplate")) {
      LineCellTemplate lct = (LineCellTemplate) at;
      templatePathVertex = new ZonePoint(lct.getPathVertex());
    }
    return templatePathVertex;
  }

  /**
   * Determines the type of template.
   *
   * @param at The template.
   * @return The simple class name (e.g. "WallTemplate")
   */
  private String getTemplateType(AbstractTemplate at) {
    return at.getClass().getSimpleName();
  }

  /**
   * Determines whether a given {@link DrawnElement} or {@link Drawable} is an {@link
   * AbstractTemplate} or not.
   *
   * @param object The object to assess whether it is a template or not.
   * @return A boolean true or false value.
   */
  private boolean isTemplate(Object object) {
    if (object instanceof DrawnElement de) {
      return de.getDrawable() instanceof AbstractTemplate;
    } else if (object instanceof Drawable d) {
      return d instanceof AbstractTemplate;
    } else {
      return false;
    }
  }

  /**
   * If the map is activated, clear the decks.
   *
   * @param event the event to process.
   */
  @Subscribe
  void onZoneActivated(ZoneActivated event) {
    if (!selectedDrawableIdSet.isEmpty()) {
      selectedDrawableIdSet.clear();
      draggedDrawnElementSet.clear();
      updateDrawablesPanel();
    }
  }

  /**
   * Paints the drawings which are being changed via dragging. This could be a change in position,
   * path, radius, direction, etc.
   *
   * @param g where to paint.
   */
  private void paintDraggedDrawings(Graphics2D g) {

    // Loop through the copies of the drawings being dragged
    for (DrawnElement de : draggedDrawnElementSet) {
      if (de != null) {

        // Templates only
        if (de.getDrawable() instanceof AbstractTemplate at) {
          Pen pen = de.getPen();
          AffineTransform oldTransform = g.getTransform();
          AffineTransform newTransform = g.getTransform();
          newTransform.concatenate(getPaintTransform(renderer));

          g.setTransform(newTransform);
          at.draw(getZone(), g, pen);
          Paint paint = pen.getPaint() != null ? pen.getPaint().getPaint() : null;

          // Only paint for the template at the mouse which instigated the drag
          if (drawnElementAtMouse.getDrawable().getId() == at.getId()) {
            paintTemplateCursor(g, paint, pen.getThickness(), at, dragStartVertex);
            paintTemplateMovementLine(g, pen, dragStartVertex, at);
            paintTemplateCursor(g, paint, pen.getThickness(), at, at.getVertex());
            ZonePoint pathVertex = getTemplatePathVertex(at);
            if (pathVertex != null) {
              paintTemplateCursor(g, paint, pen.getThickness(), at, pathVertex);
            }
          }

          // Do not scale labels with zoom level
          g.setTransform(oldTransform);
          paintTemplateRadiusLabel(g, at.getVertex(), at);
          if (drawnElementAtMouse.getDrawable().getId() == at.getId()) {
            paintTemplateMovementLabel(g, dragStartVertex, at);
          }
        }
      }
    } // end for
  }

  /**
   * Paints the drawn element's name label.
   *
   * @param g where to draw.
   * @param drawnElement which drawn element to base the label on.
   */
  private void paintDrawingNameLabel(Graphics2D g, DrawnElement drawnElement) {

    if (drawnElement != null) {
      String drawingName = null;
      Rectangle bounds = null;
      if (drawnElement.getDrawable() instanceof AbstractTemplate at) {
        drawingName = at.getName();
        bounds = at.getBounds(getZone());
      } else if (drawnElement.getDrawable() instanceof AbstractDrawing ad) {
        drawingName = ad.getName();
        bounds = ad.getBounds(getZone());
      }

      if (drawingName != null && !drawingName.trim().isEmpty()) {

        GUID id = drawnElement.getDrawable().getId();
        if (!flatImageLabelCache.containsKey(id)) {
          flatImageLabelFactory = new FlatImageLabelFactory();
          flatImageLabelCache.put(id, flatImageLabelFactory.getMapImageLabel(drawnElement));
        }

        Pen pen = drawnElement.getPen();
        int x = (int) (bounds.getMinX() + bounds.getMaxX()) / 2;
        int y = (int) (bounds.getMaxY() + pen.getThickness());
        ScreenPoint centerText = ScreenPoint.fromZonePoint(renderer, x, y);

        FlatImageLabel fil = flatImageLabelCache.get(id);
        Dimension nameDimension = fil.getDimensions(g, drawingName);
        fil.render(
            g,
            (int) (centerText.x - (nameDimension.width / 2f)),
            (int) (centerText.y),
            drawingName);
      }
    }
  }

  /**
   * Paints a selected border around a drawn element.
   *
   * @param g where to draw.
   * @param drawnElement which drawn element to draw the border around.
   */
  private void paintSelectedBorder(Graphics2D g, DrawnElement drawnElement) {
    var box = drawnElement.getDrawable().getBounds(getZone());
    var pen = drawnElement.getPen();

    var scale = renderer.getScale();

    var screenPoint = ScreenPoint.fromZonePoint(renderer, box.x, box.y);

    var x = (int) (screenPoint.x - pen.getThickness() * scale / 2);
    var y = (int) (screenPoint.y - pen.getThickness() * scale / 2);
    var w = (int) ((box.width + pen.getThickness()) * scale);
    var h = (int) ((box.height + pen.getThickness()) * scale);

    AppStyle.selectedBorder.paintAround(g, x, y, w, h);
  }

  /**
   * Paint the draggable selection box.
   *
   * @param g where tp paint.
   */
  private void paintSelectionBox(Graphics2D g) {
    // Paints the draggable selection box
    if (drawingSelectionBox != null) {
      Composite composite = g.getComposite();
      Stroke stroke = g.getStroke();
      g.setStroke(new BasicStroke(2));
      if (AppPreferences.fillSelectionBox.get()) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .25f));
        g.setPaint(AppStyle.drawingSelectionBoxFill);
        g.fillRoundRect(
            drawingSelectionBox.x,
            drawingSelectionBox.y,
            drawingSelectionBox.width,
            drawingSelectionBox.height,
            10,
            10);
        g.setComposite(composite);
      }
      g.setColor(AppStyle.selectionBoxOutline);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.drawRoundRect(
          drawingSelectionBox.x,
          drawingSelectionBox.y,
          drawingSelectionBox.width,
          drawingSelectionBox.height,
          10,
          10);
      g.setStroke(stroke);
    }
  }

  /**
   * Paint a cursor on the template, either a cross or cell cursor depending on the template type.
   * Can be used for both the vertex and (where applicable) the pathVertex.
   *
   * @param g where to draw.
   * @param paint how to paint.
   * @param thickness the line thickness.
   * @param at the template.
   * @param vertex the position to paint the cursor.
   */
  private void paintTemplateCursor(
      Graphics2D g, Paint paint, float thickness, AbstractTemplate at, ZonePoint vertex) {

    if (getTemplateCursorType(at) == templateCursorType.CROSS) {
      // Paint a Cross
      int halfCursor = CURSOR_WIDTH / 2;
      g.setPaint(paint);
      g.setStroke(new BasicStroke(thickness));
      g.drawLine(vertex.x - halfCursor, vertex.y, vertex.x + halfCursor, vertex.y);
      g.drawLine(vertex.x, vertex.y - halfCursor, vertex.x, vertex.y + halfCursor);
    } else if (getTemplateCursorType(at) == templateCursorType.CELL) {
      // Paint a Cell
      g.setPaint(paint);
      g.setStroke(new BasicStroke(thickness));
      int grid = getZone().getGrid().getSize();
      g.drawRect(vertex.x, vertex.y, grid, grid);
    }
  }

  /**
   * Paint the dragged movement distance in feet according to the Movement Metric setting.
   *
   * <p>Label is displayed below the template (i.e. similar to dragging a token)
   *
   * @param g where to draw.
   * @param startVertex the starting point.
   * @param at the template what is being dragged.
   */
  private void paintTemplateMovementLabel(
      Graphics2D g, ZonePoint startVertex, AbstractTemplate at) {

    // MOVEMENT LABEL
    double moveDistance;
    Zone zone = getZone();
    Grid grid = zone.getGrid();
    WalkerMetric wm =
        MapTool.isPersonalServer()
            ? AppPreferences.movementMetric.get()
            : MapTool.getServerPolicy().getMovementMetric();
    ZonePoint endVertex = at.getVertex();
    CellPoint dragStartCellPoint = grid.convert(startVertex);
    CellPoint dragVertexCellPoint = grid.convert(endVertex);
    double cellDistance = grid.cellDistance(dragStartCellPoint, dragVertexCellPoint, wm);
    moveDistance = cellDistance * zone.getUnitsPerCell();

    if (moveDistance != 0) {
      Rectangle bounds = at.getBounds(zone);
      int x = (int) (bounds.getMinX() + bounds.getMaxX()) / 2;
      int y = (int) (bounds.getMaxY());
      ScreenPoint centerText = ScreenPoint.fromZonePoint(renderer, x, y);

      ToolHelper.drawMeasurement(g, moveDistance, (int) centerText.x, (int) centerText.y);
    }
  }

  /**
   * Draw a straight line between a point on the map and a template.
   *
   * @param g where to draw.
   * @param pen the pen to use.
   * @param startVertex the zone starting position.
   * @param at the template what is being dragged.
   */
  private void paintTemplateMovementLine(
      Graphics2D g, Pen pen, ZonePoint startVertex, AbstractTemplate at) {

    ZonePoint endVertex = at.getVertex();

    // Only paint of the vertices are different
    if (!startVertex.equals(endVertex)) {

      // If the cursor type is a cell, draw from the middle of the cell
      int offsetXY = 0;
      if (getTemplateCursorType(at) == templateCursorType.CELL) {
        offsetXY = getZone().getGrid().getSize() / 2;
      }

      // MOVEMENT LINE
      int startX = startVertex.x + offsetXY;
      int startY = startVertex.y + offsetXY;
      int endX = endVertex.x + offsetXY;
      int endY = endVertex.y + offsetXY;
      float[] dashingPattern = {9f, 3f};

      Composite composite = g.getComposite();
      Paint paint;

      paint = pen.getBackgroundPaint() != null ? pen.getBackgroundPaint().getPaint() : null;
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
      g.setPaint(paint);
      g.setStroke(
          new BasicStroke(
              pen.getThickness() * 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f));
      g.drawLine(startX, startY, endX, endY);

      paint = pen.getPaint() != null ? pen.getPaint().getPaint() : null;
      g.setPaint(paint);
      g.setStroke(
          new BasicStroke(
              pen.getThickness(),
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER,
              1.0f,
              dashingPattern,
              2.0f));
      g.drawLine(startX, startY, endX, endY);
      g.setComposite(composite);
    }
  }

  /**
   * Paint the radius value in feet. To be displayed above the template vertex (i.e. same as when
   * drawing a template)
   *
   * @param g where to paint.
   * @param zp where on the map to paint the radius label.
   */
  private void paintTemplateRadiusLabel(Graphics2D g, ZonePoint zp, AbstractTemplate at) {
    if (at.getRadius() > 0) {
      ScreenPoint centerText = ScreenPoint.fromZonePoint(renderer, zp);
      centerText.translate(CURSOR_WIDTH, -CURSOR_WIDTH);
      ToolHelper.drawMeasurement(
          g, at.getRadius() * getZone().getUnitsPerCell(), (int) centerText.x, (int) centerText.y);
    } // endif
  }

  /**
   * Creates a copy of the {@link DrawnElement}s being dragged. This {@link Set} is used for
   * displaying the dragged drawings. e.g. for templates this could be either a change in position,
   * path, direction, and/or size.
   *
   * <p>In the event of the <kbd>Escape</kbd> key being pressed while dragging, any changes to
   * dragged drawings will not be applied to the original drawings.
   */
  private void setDraggedDrawnElementsSet() {
    List<DrawnElement> drawableList = getDrawnElementsOnLayerList(false);

    draggedDrawnElementSet.clear();
    if (!selectedDrawableIdSet.isEmpty()) {
      for (DrawnElement de : drawableList) {
        Drawable d = de.getDrawable();
        GUID id = d.getId();
        if (selectedDrawableIdSet.contains(id)) {
          if (d instanceof AbstractTemplate) {
            DrawnElement deCopy = new DrawnElement(de);
            draggedDrawnElementSet.add(deCopy);
          }
        }
      }
    }
  }

  /**
   * Helper method that sets the path vertex for the template (for known template types that have
   * them), but prevents setting it where it would equal the vertex.
   *
   * @param at the template.
   * @param zp the zone point for the path vertex.
   */
  private void setTemplatePathVertex(AbstractTemplate at, ZonePoint zp) {
    if (!at.getVertex().equals(zp)) {
      String templateType = getTemplateType(at);
      if (templateType.equals("LineTemplate")) {
        LineTemplate lt = (LineTemplate) at;
        lt.setPathVertex(zp);
      } else if (templateType.equals("LineCellTemplate")) {
        LineCellTemplate lct = (LineCellTemplate) at;
        lct.setPathVertex(zp);
      }
    }
  }

  /** Update the drawables panel with the selected drawings. */
  private void updateDrawablesPanel() {
    DrawablesPanel drawablesPanel = MapTool.getFrame().getDrawablesPanel();
    drawablesPanel.clearSelectedIds();

    if (!selectedDrawableIdSet.isEmpty()) {
      // Add the drawings by the order which they are on the layer in the zone.
      List<DrawnElement> drawableList = getDrawnElementsOnLayerList(true);
      for (DrawnElement de : drawableList) {
        GUID deId = de.getDrawable().getId();
        for (GUID id : selectedDrawableIdSet) {
          if (deId.equals(id)) {
            drawablesPanel.addSelectedId(id);
          }
        } // end for
      } // end for
    }
  }

  /**
   * Handles moving the template by moving the vertex and (if relevant) the pathVertex.
   *
   * <p>Dragging drawings is not possible if not the GM and prohibited by server policy.
   *
   * @param e the mouse event for the dragging.
   * @param at the template what is being dragged.
   */
  private void updateDraggedDrawnElements(MouseEvent e, AbstractTemplate at) {

    if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
      // i.e. not allowed
      return;
    }

    ZonePoint dragCellOffset = getCellAtMouse(e);
    dragCellOffset.x = dragWorkingCell.x - dragCellOffset.x;
    dragCellOffset.y = dragWorkingCell.y - dragCellOffset.y;
    ZonePoint vertex = new ZonePoint(at.getVertex());
    String templateType = getTemplateType(at);

    // CTRL -> Change the radius if only one drawing selected
    if (selectedDrawableIdSet.size() == 1 && e.isControlDown()) {

      // Resize the Template Radius
      CellPoint workingCell = getZone().getGrid().convert(getCellAtMouse(e));
      CellPoint vertexCell = getZone().getGrid().convert(vertex);
      int x = Math.abs(workingCell.x - vertexCell.x);
      int y = Math.abs(workingCell.y - vertexCell.y);
      int dragRadius = at.getDistance(x, y);
      at.setRadius(dragRadius);

      // Move the Template around the Vertex (if applicable)
      if (templateType.equals("BlastTemplate")) {
        ((BlastTemplate) at)
            .setControlCellRelative(workingCell.x - vertexCell.x, workingCell.y - vertexCell.y);
      }
      if (templateType.equals("ConeTemplate")) {
        ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
        ((ConeTemplate) at)
            .setDirection(
                RadiusTemplate.Direction.findDirection(mouse.x, mouse.y, vertex.x, vertex.y));
      }

      // ALT -> Change the Path Vertex if only one drawing selected
    } else if (selectedDrawableIdSet.size() == 1 && e.isAltDown()) {

      // Move just pathVertex (if applicable)
      ZonePoint pathVertex = getTemplatePathVertex(at);
      if (pathVertex != null) {
        pathVertex.x = pathVertex.x - dragCellOffset.x;
        pathVertex.y = pathVertex.y - dragCellOffset.y;
        setTemplatePathVertex(at, pathVertex);
      }

    } else {
      // MOVE THE ENTIRE TEMPLATE

      // First move the Vertex
      vertex.x = vertex.x - dragCellOffset.x;
      vertex.y = vertex.y - dragCellOffset.y;
      at.setVertex(vertex);

      // Then (if applicable) move the Path Vertex
      ZonePoint pathVertex = getTemplatePathVertex(at);
      if (pathVertex != null) {
        pathVertex.x = pathVertex.x - dragCellOffset.x;
        pathVertex.y = pathVertex.y - dragCellOffset.y;
        setTemplatePathVertex(at, pathVertex);
      }
    }
  }

  /** Once a drag has completed, apply any changes to the original drawings. */
  private void updateOriginalDrawnElements() {

    Zone zone = getZone();
    List<DrawnElement> drawableList;

    if (MapTool.getPlayer().isGM()) {
      drawableList = zone.getDrawnElements(getSelectedLayer());
    } else {
      drawableList = zone.getDrawnElements(Zone.Layer.getDefaultPlayerLayer());
    }

    for (DrawnElement deOriginal : drawableList) {
      for (DrawnElement deDragged : draggedDrawnElementSet) {
        GUID id = deDragged.getDrawable().getId();
        if (id == deOriginal.getDrawable().getId()) {
          if (deOriginal.getDrawable() instanceof AbstractTemplate atOriginal
              && deDragged.getDrawable() instanceof AbstractTemplate atDragged) {

            // Update the radius and vertex (all templates have these)
            atOriginal.setVertex(atDragged.getVertex());
            atOriginal.setRadius(atDragged.getRadius());

            // Update the path vertex (if applicable to the template type)
            setTemplatePathVertex(atOriginal, getTemplatePathVertex(atDragged));

            // Update other special things (applicable to specific template types)
            String templateType = getTemplateType(atOriginal);
            if (templateType.equals("BlastTemplate")) {
              int OffsetX = ((BlastTemplate) atDragged).getOffsetX();
              int OffsetY = ((BlastTemplate) atDragged).getOffsetY();
              ((BlastTemplate) atOriginal).setControlCellOffset(OffsetX, OffsetY);
            } else if (templateType.equals("ConeTemplate")) {
              ((ConeTemplate) atOriginal).setDirection(((ConeTemplate) atDragged).getDirection());
            }

            // Server drawing update
            MapTool.serverCommand().updateDrawing(zone.getId(), deOriginal.getPen(), deOriginal);
            renderer.getZone().updateDrawable(deOriginal, deOriginal.getPen());
          }
        }
      } // end for
    } // end for
    draggedDrawnElementSet.clear();
    renderer.repaint();
  }
}
