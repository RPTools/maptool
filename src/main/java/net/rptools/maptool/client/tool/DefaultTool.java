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
package net.rptools.maptool.client.tool;

import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.layerselectiondialog.LayerSelectionDialog;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.ViewMovementKey;
import net.rptools.maptool.model.Zone;

/** */
public abstract class DefaultTool extends Tool
    implements MouseListener, MouseMotionListener, MouseWheelListener {
  private static final long serialVersionUID = 3258411729238372921L;

  private final LayerSelectionDialog layerSelectionDialog =
      new LayerSelectionDialog(Zone.Layer.values(), this::selectedLayerChanged);

  private Zone.Layer selectedLayer;
  private boolean isDraggingMap;

  /**
   * The origin point for a map drag, or {@code null} if there is no drag possible.
   *
   * <p>Will be non-null when a drag is possible (right button pressed). To check whether the drag
   * is actually happening, use {@link #isDraggingMap()}. This field will be non-{@code null}
   * whenever {@link #isDraggingMap} is {@code true}, but it is also possible to be non-{@code null}
   * even if {@link #isDraggingMap} is {@code false}.
   */
  private @Nullable Point mapDragStart;

  private int dragThreshold = DragSource.getDragThreshold();

  protected int mouseX;
  protected int mouseY;

  // TBD
  private boolean isTouchScreen = false;

  protected ZoneRenderer renderer;

  protected Zone.Layer getSelectedLayer() {
    return selectedLayer;
  }

  protected LayerSelectionDialog getLayerSelectionDialog() {
    return layerSelectionDialog;
  }

  protected void selectedLayerChanged(Zone.Layer layer) {
    selectedLayer = layer;
    if (renderer != null) {
      renderer.setActiveLayer(layer);
    }
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    this.renderer = renderer;
    selectedLayer = renderer.getActiveLayer();
    layerSelectionDialog.setSelectedLayer(selectedLayer);
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    this.renderer = null;
    super.detachFrom(renderer);
  }

  protected boolean isDraggingMap() {
    return isDraggingMap;
  }

  /**
   * Stop dragging the map.
   *
   * <p>Useful if the default behaviour is interfering with a tool.
   */
  protected void cancelMapDrag() {
    mapDragStart = null;
    isDraggingMap = false;
  }

  protected void repaintZone() {
    renderer.repaint();
  }

  protected Zone getZone() {
    return renderer.getZone();
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, 1, 0));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, -1, 0));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, 0, 1));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, 0, -1));

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, 1, 0));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, -1, 0));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, 0, 1));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK),
        new ViewMovementKey(this, 0, -1));

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), new FlipTokenHorizontalActionListener());
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_DOWN_MASK),
        new FlipTokenVerticalActionListener());
  }

  ////
  // Mouse
  @Override
  public void mousePressed(MouseEvent e) {
    // Potential map dragging
    if (SwingUtilities.isRightMouseButton(e) && mapDragStart == null) {
      setDragStart(e.getX(), e.getY());
    }
  }

  /**
   * Set the location of the start of the drag
   *
   * @param x the x coordinate of the drag start
   * @param y the y coordinate of the drag start
   */
  public void setDragStart(int x, int y) {
    mapDragStart = new Point(x, y);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (isRightMouseButton(e) && mapDragStart != null) {
      if (isDraggingMap) {
        renderer.maybeForcePlayersView();
      }

      cancelMapDrag();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {}

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {}

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {}

  ////
  // MouseMotion
  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    if (renderer == null) {
      return;
    }
    mouseX = e.getX();
    mouseY = e.getY();

    CellPoint cp =
        getZone().getGrid().convert(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
    if (cp != null) {
      MapTool.getFrame().getCoordinateStatusBar().update(cp.x, cp.y);
    } else {
      MapTool.getFrame().getCoordinateStatusBar().clear();
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    int mX = e.getX();
    int mY = e.getY();
    CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(mX, mY));
    if (cellUnderMouse != null) {
      MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
    } else {
      MapTool.getFrame().getCoordinateStatusBar().clear();
    }
    // MAP MOVEMENT
    // Sometimes the mousePressed() event can come after the first mouseDragged() event when the
    // right button is pressed. So check that we are actually intending to drag the map.
    if (isRightMouseButton(e) && mapDragStart != null) {
      var mapDX = mX - mapDragStart.x;
      var mapDY = mY - mapDragStart.y;

      if (mapDX * mapDX + mapDY * mapDY > dragThreshold * dragThreshold) {
        isDraggingMap = true;
      }

      setDragStart(mX, mY);
      renderer.moveViewBy(mapDX, mapDY);
    }
  }

  public void moveViewByCells(int dx, int dy) {
    renderer.moveViewByCells(dx, dy);
  }

  ////
  // Mouse Wheel
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    // Fix for High Resolution Mouse Wheels
    if (e.getWheelRotation() == 0) {
      return;
    }

    // QUICK ROTATE
    if (SwingUtil.isShiftDown(e)) {
      Set<GUID> tokenGUIDSet = renderer.getSelectedTokenSet();
      if (tokenGUIDSet.isEmpty()) {
        return;
      }
      for (GUID tokenGUID : tokenGUIDSet) {
        Token token = getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        if (!AppUtil.playerOwns(token)) {
          continue;
        }

        int facing = token.getFacing();
        if (SwingUtil.isControlDown(e)) {
          facing += e.getWheelRotation() > 0 ? 5 : -5;
        } else {
          facing =
              renderer
                  .getZone()
                  .getGrid()
                  .nextFacing(
                      facing,
                      AppPreferences.faceEdge.get(),
                      AppPreferences.faceVertex.get(),
                      e.getWheelRotation() <= 0);
        }

        token.setFacing(facing);

        MapTool.serverCommand().putToken(getZone().getId(), token);
      }

      repaintZone();
      return;
    }
    // ZOOM
    if (!AppState.isZoomLocked()) {
      boolean direction = e.getWheelRotation() < 0;
      direction = isKeyDown('z') == direction;
      if (direction) {
        renderer.zoomOut(e.getX(), e.getY());
      } else {
        renderer.zoomIn(e.getX(), e.getY());
      }
      renderer.maybeForcePlayersView();
    }
  }

  @Override
  protected void resetTool() {
    MapTool.getFrame().getToolbox().setSelectedTool(PointerTool.class);
  }

  /*
   * For touch screens, swap the events, easier to move map/draw by default
   */
  public boolean isLeftMouseButton(MouseEvent event) {
    if (isTouchScreen) return SwingUtilities.isRightMouseButton(event);
    else return SwingUtilities.isLeftMouseButton(event);
  }

  /*
   * For touch screens, swap the events, easier to move map/draw by default
   */
  public boolean isRightMouseButton(MouseEvent event) {
    if (isTouchScreen) return SwingUtilities.isLeftMouseButton(event);
    else return SwingUtilities.isRightMouseButton(event);
  }

  /*
   * Nothing do here for now...
   */
  public boolean isMiddleMouseButton(MouseEvent event) {
    return SwingUtilities.isMiddleMouseButton(event);
  }

  private class FlipTokenHorizontalActionListener extends AbstractAction {
    private static final long serialVersionUID = -6286351028470892136L;

    @Override
    public void actionPerformed(ActionEvent e) {
      renderer
          .getSelectedTokensList()
          .forEach(
              token -> {
                if (token != null && AppUtil.playerOwns(token)) {
                  MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipX);
                }
              });
      MapTool.getFrame().refresh();
    }
  }

  private class FlipTokenVerticalActionListener extends AbstractAction {
    private static final long serialVersionUID = -6286351028470892137L;

    @Override
    public void actionPerformed(ActionEvent e) {
      renderer
          .getSelectedTokensList()
          .forEach(
              token -> {
                if (token != null && AppUtil.playerOwns(token)) {
                  MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipY);
                }
              });
      MapTool.getFrame().refresh();
    }
  }
}
