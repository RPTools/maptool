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

import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
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
import net.rptools.maptool.util.TokenUtil;

/** */
public abstract class DefaultTool extends Tool
    implements MouseListener, MouseMotionListener, MouseWheelListener {
  private static final long serialVersionUID = 3258411729238372921L;

  private final LayerSelectionDialog layerSelectionDialog =
      new LayerSelectionDialog(
          new Zone.Layer[] {
            Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND
          },
          this::selectedLayerChanged);

  private Zone.Layer selectedLayer;
  private boolean isDraggingMap;
  private int dragStartX;
  private int dragStartY;
  private int dragThreshold = DragSource.getDragThreshold();

  protected int mouseX;
  protected int mouseY;

  // This is to manage overflowing of map move events (keep things snappy)
  private int mapDX, mapDY;

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

  public boolean isDraggingMap() {
    return isDraggingMap;
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

    // Disable until the conrete hotkeys are decided.
    /*
    actionMap.put(
         KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK),
         new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
             if (layerSelectionDialog.isVisible()) {
               layerSelectionDialog.setSelectedLayer(Zone.Layer.TOKEN);
             }
           }
         });
     actionMap.put(
         KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK),
         new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
             if (layerSelectionDialog.isVisible()) {
               layerSelectionDialog.setSelectedLayer(Zone.Layer.GM);
             }
           }
         });
     actionMap.put(
         KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK),
         new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
             if (layerSelectionDialog.isVisible()) {

               layerSelectionDialog.setSelectedLayer(Zone.Layer.OBJECT);
             }
           }
         });
     actionMap.put(
         KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK),
         new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
             if (layerSelectionDialog.isVisible()) {
               layerSelectionDialog.setSelectedLayer(Zone.Layer.BACKGROUND);
             }
           }
         });
         */
  }

  ////
  // Mouse
  @Override
  public void mousePressed(MouseEvent e) {
    // Potential map dragging
    if (SwingUtilities.isRightMouseButton(e)) {
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
    dragStartX = x;
    dragStartY = y;
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (isDraggingMap && isRightMouseButton(e)) {
      renderer.maybeForcePlayersView();
    }
    // Cleanup
    isDraggingMap = false;
  }

  /**
   * @param isDraggingMap whether the user drags the map
   */
  void setDraggingMap(boolean isDraggingMap) {
    this.isDraggingMap = isDraggingMap;
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
    if (isRightMouseButton(e)) {

      mapDX += mX - dragStartX;
      mapDY += mY - dragStartY;

      if (mapDX * mapDX + mapDY * mapDY > dragThreshold * dragThreshold) {
        isDraggingMap = true;
      }

      setDragStart(mX, mY);

      renderer.moveViewBy(mapDX, mapDY);
      mapDX = 0;
      mapDY = 0;
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
        Integer facing = token.getFacing();
        if (facing == null) {
          facing = -90; // natural alignment
        }
        if (SwingUtil.isControlDown(e)) {
          // Modify on the fly the rotation point
          if (e.isAltDown()) {
            int x = token.getX();
            int y = token.getY();
            int w = token.getWidth();
            int h = token.getHeight();

            double xc = x + w / 2;
            double yc = y + h / 2;

            facing += e.getWheelRotation() > 0 ? 5 : -5;
            token.setFacing(facing);
            int a = token.getFacingInDegrees();
            double r = Math.toRadians(a);

            System.out.println("Angle: " + a);
            System.out.println("Origin x,y: " + x + ", " + y);
            System.out.println("Origin bounds: " + token.getBounds(renderer.getZone()));
            // System.out.println("Anchor x,y: " + token.getAnchor().x + ", " +
            // token.getAnchor().y);

            // x = (int) ((x + w) - w * Math.cos(r));
            // y = (int) (y - w * Math.sin(r));

            // double x1 = (x - xc) * Math.cos(r) - (y - yc) * Math.sin(r) + xc;
            // double y1 = (y - yc) * Math.cos(r) + (x - xc) * Math.sin(r) + yc;

            // x = (int) (x * Math.cos(r) - y * Math.sin(r));
            // y = (int) (y * Math.cos(r) + x * Math.sin(r));

            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            at.rotate(r, x + w, y);

            x = (int) at.getTranslateX();
            y = (int) at.getTranslateY();

            // token.setX(x);
            // token.setY(y);
            // renderer.flush(token);
            // MapTool.serverCommand().putToken(getZone().getId(), token);

            // token.setX(0);
            // token.setY(0);

            System.out.println("New x,y: " + x + ", " + y);
            System.out.println("New bounds: " + token.getBounds(renderer.getZone()).toString());

          } else {
            facing += e.getWheelRotation() > 0 ? 5 : -5;
          }
        } else {
          int[] facingArray = getZone().getGrid().getFacingAngles();
          int facingIndex = TokenUtil.getIndexNearestTo(facingArray, facing);

          facingIndex += e.getWheelRotation() > 0 ? 1 : -1;
          if (facingIndex < 0) {
            facingIndex = facingArray.length - 1;
          }
          if (facingIndex == facingArray.length) {
            facingIndex = 0;
          }
          facing = facingArray[facingIndex];
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
      direction = isKeyDown('z') == direction; // XXX Why check for this?
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
