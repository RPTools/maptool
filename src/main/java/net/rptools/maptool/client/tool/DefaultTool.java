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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.Set;
import javax.swing.SwingUtilities;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.Tool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.TokenUtil;

/** */
public abstract class DefaultTool extends Tool
    implements MouseListener, MouseMotionListener, MouseWheelListener {
  private static final long serialVersionUID = 3258411729238372921L;

  private boolean isDraggingMap;
  private int dragStartX;
  private int dragStartY;

  protected int mouseX;
  protected int mouseY;

  // This is to manage overflowing of map move events (keep things snappy)
  private long lastMoveRedraw;
  private int mapDX, mapDY;
  private static final int REDRAW_DELAY = 25; // millis

  // TBD
  private boolean isTouchScreen = false;

  protected ZoneRenderer renderer;

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    this.renderer = renderer;
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

  ////
  // Mouse
  public void mousePressed(MouseEvent e) {
    // Potential map dragging
    if (SwingUtilities.isRightMouseButton(e)) {
      dragStartX = e.getX();
      dragStartY = e.getY();
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (isDraggingMap && isRightMouseButton(e)) {
      renderer.maybeForcePlayersView();
    }
    // Cleanup
    isDraggingMap = false;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {}

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {}

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {}

  ////
  // MouseMotion
  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
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
      isDraggingMap = true;

      mapDX += mX - dragStartX;
      mapDY += mY - dragStartY;

      dragStartX = mX;
      dragStartY = mY;

      long now = System.currentTimeMillis();
      if (now - lastMoveRedraw > REDRAW_DELAY) {
        // TODO: does it matter to capture the last map move in the series ?
        // TODO: This should probably be genericized and put into ZoneRenderer to prevent over
        // zealous repainting
        renderer.moveViewBy(mapDX, mapDY);
        mapDX = 0;
        mapDY = 0;
        lastMoveRedraw = now;
      }
    }
  }

  ////
  // Mouse Wheel
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

        renderer.flush(token);
        MapTool.serverCommand().putToken(getZone().getId(), token);
      }

      repaintZone();
      return;
    }
    // ZOOM
    if (!AppState.isZoomLocked()) {
      boolean direction = e.getWheelRotation() > 0;
      direction = isKeyDown('z') ? !direction : direction;
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
}
