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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.AutoResizeStampDialog;
import net.rptools.maptool.client.ui.StampPopupMenu;
import net.rptools.maptool.client.ui.TokenLocation;
import net.rptools.maptool.client.ui.TokenPopupMenu;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.util.ImageManager;

/** Tool used for background and object tokens, and to resize a token in free size mode. */
public class StampTool extends DefaultTool implements ZoneOverlay {

  private boolean isShowingTokenStackPopup;
  private boolean isDraggingToken;
  private boolean isNewTokenSelected;
  private boolean isDrawingSelectionBox;
  private boolean isMovingWithKeys;
  private boolean isResizingToken;
  private boolean isResizingRotatedToken;
  private Rectangle selectionBoundBox;

  // The position with greater than integer accuracy of a rotated stamp that is being resized.
  private Point2D.Double preciseStampZonePoint;
  private ZonePoint lastResizeZonePoint;

  private Token tokenBeingDragged;
  private Token tokenUnderMouse;
  private Token tokenBeingResized;

  private final TokenStackPanel tokenStackPanel = new TokenStackPanel();

  // private Map<Shape, Token> rotateBoundsMap = new HashMap<Shape, Token>();
  private final Map<Shape, Token> resizeBoundsMap = new HashMap<Shape, Token>();

  // Offset from token's X,Y when dragging. Values are in cell coordinates.
  private int dragOffsetX;
  private int dragOffsetY;
  private int dragStartX;
  private int dragStartY;

  private BufferedImage resizeImg = RessourceManager.getImage(Images.RESIZE);

  public StampTool() {}

  @Override
  protected void selectedLayerChanged(Zone.Layer layer) {
    super.selectedLayerChanged(layer);
    if (layer == Layer.TOKEN && MapTool.getFrame() != null) {
      MapTool.getFrame().getToolbox().setSelectedTool(PointerTool.class);
    }
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    MapTool.getFrame().removeControlPanel();
    super.detachFrom(renderer);
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    MapTool.getFrame().showControlPanel(getLayerSelectionDialog());
    super.attachTo(renderer);
  }

  @Override
  public String getInstructions() {
    return "tool.pointer.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.stamp.tooltip";
  }

  public void startTokenDrag(Token keyToken, Set<GUID> tokens) {
    tokenBeingDragged = keyToken;

    if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
      // Not allowed
      return;
    }
    renderer.addMoveSelectionSet(MapTool.getPlayer().getName(), tokenBeingDragged.getId(), tokens);
    MapTool.serverCommand()
        .startTokenMove(
            MapTool.getPlayer().getName(),
            renderer.getZone().getId(),
            tokenBeingDragged.getId(),
            tokens);
    isDraggingToken = true;
  }

  public void stopTokenDrag() {
    renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
    isDraggingToken = false;
    isMovingWithKeys = false;

    dragOffsetX = 0;
    dragOffsetY = 0;
  }

  /**
   * Set the tokenList, x, y, in the StackPanel, and isShowingTokenStackPupup to true
   *
   * @param tokenList to set
   * @param x the x to set
   * @param y the y to set
   */
  private void showTokenStackPopup(List<Token> tokenList, int x, int y) {
    tokenStackPanel.show(tokenList, x, y);
    isShowingTokenStackPopup = true;
  }

  private class TokenStackPanel {
    private static final int PADDING = 4;

    private List<Token> tokenList;

    /** List of token locations, each containing a token and its bounds. */
    private final List<TokenLocation> tokenLocationList = new ArrayList<TokenLocation>();

    private int x;
    private int y;

    public void show(List<Token> tokenList, int x, int y) {
      this.tokenList = tokenList;
      this.x = x - TokenStackPanel.PADDING - getSize().width / 2;
      this.y = y - TokenStackPanel.PADDING - getSize().height / 2;
    }

    public Dimension getSize() {
      int gridSize = (int) renderer.getScaledGridSize();
      return new Dimension(
          tokenList.size() * (gridSize + PADDING) + PADDING, gridSize + PADDING * 2);
    }

    /**
     * Does nothing
     *
     * @param event the mousevent
     */
    public void handleMouseEvent(MouseEvent event) {
      // Nothing to do right now
    }

    public void handleMouseMotionEvent(MouseEvent event) {
      Point p = event.getPoint();
      for (TokenLocation location : tokenLocationList) {
        if (location.getBounds().contains(p.x, p.y)) {
          if (!AppUtil.playerOwns(location.getToken())) {
            return; //  drag not allowed
          }

          final var selectionModel = renderer.getSelectionModel();
          final var wasNotAlreadySelected = !selectionModel.isSelected(location.getToken().getId());
          // Only this token should be selected going forward.
          renderer
              .getSelectionModel()
              .replaceSelection(Collections.singletonList(location.getToken().getId()));

          if (wasNotAlreadySelected) {
            Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
            if (!(tool instanceof StampTool)) {
              return;
            }
            tokenUnderMouse = location.getToken();
            ((StampTool) tool)
                .startTokenDrag(
                    location.getToken(), Collections.singleton(location.getToken().getId()));
          }
          return;
        }
      }
    }

    public void paint(Graphics g) {
      Dimension size = getSize();
      int gridSize = (int) renderer.getScaledGridSize();

      // Background
      g.setColor(getBackground());
      g.fillRect(x, y, size.width, size.height);

      // Border
      AppStyle.border.paintAround((Graphics2D) g, x, y, size.width - 1, size.height - 1);

      // Images
      tokenLocationList.clear();
      for (int i = 0; i < tokenList.size(); i++) {
        Token token = tokenList.get(i);
        BufferedImage image = ImageManager.getImage(token.getImageAssetId(), renderer);

        Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
        SwingUtil.constrainTo(imgSize, gridSize);

        Rectangle bounds =
            new Rectangle(
                x + PADDING + i * (gridSize + PADDING), y + PADDING, imgSize.width, imgSize.height);
        g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, renderer);

        tokenLocationList.add(new TokenLocation(bounds, token));
      }
    }

    public boolean contains(int x, int y) {
      return new Rectangle(this.x, this.y, getSize().width, getSize().height).contains(x, y);
    }
  }

  // //
  // Mouse
  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);

    if (isShowingTokenStackPopup) {
      if (tokenStackPanel.contains(e.getX(), e.getY())) {
        tokenStackPanel.handleMouseEvent(e);
        return;
      } else {
        isShowingTokenStackPopup = false;
        repaint();
      }
    }

    // So that keystrokes end up in the right place
    renderer.requestFocusInWindow();
    if (isDraggingMap()) {
      return;
    }

    if (isDraggingToken) {
      return;
    }

    dragStartX = e.getX();
    dragStartY = e.getY();

    // Check token resizing
    for (Entry<Shape, Token> entry : resizeBoundsMap.entrySet()) {
      Shape bounds = entry.getKey();
      if (bounds.contains(dragStartX, dragStartY)) {
        dragOffsetX = bounds.getBounds().x + bounds.getBounds().width - e.getX();
        dragOffsetY = bounds.getBounds().y + bounds.getBounds().height - e.getY();

        isResizingToken = true;
        // The token being resized does not necessarily = tokenUnderMouse. If there is more then one
        // token under the mouse, the top token will be the tokenUnderMouse, but it is the selected
        // that is intended to be resized.
        tokenBeingResized = entry.getValue();
        return;
      }
    }

    // Check token rotation
    // for (Entry<Shape, Token> entry : rotateBoundsMap.entrySet()) {
    // if (entry.getKey().contains(dragStartX, dragStartY)) {
    // isRotatingToken = true;
    // return;
    // }
    // }

    // Properties
    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
      List<Token> tokenList = renderer.getTokenStackAt(mouseX, mouseY);
      if (tokenList != null) {
        // Stack
        renderer.getSelectionModel().replaceSelection(Collections.emptyList());
        showTokenStackPopup(tokenList, e.getX(), e.getY());
      } else {
        // Single
        Token token = getTokenAt(e.getX(), e.getY());
        MapTool.getFrame().showTokenPropertiesDialog(token, renderer);
      }
      return;
    }
    // SELECTION
    Token token = getTokenAt(e.getX(), e.getY());
    if (token != null
        && !isDraggingToken
        && SwingUtilities.isLeftMouseButton(e)
        && !renderer.isAutoResizeStamp()) {
      // Permission
      if (!AppUtil.playerOwns(token)) {
        if (!SwingUtil.isShiftDown(e)) {
          renderer.getSelectionModel().replaceSelection(Collections.emptyList());
        }
        return;
      }
      // Don't select if it's already being moved by someone
      isNewTokenSelected = false;
      if (!renderer.isTokenMoving(token)) {
        final var selectionModel = renderer.getSelectionModel();
        final var isSelected = selectionModel.isSelected(token.getId());
        if (SwingUtil.isShiftDown(e)) {
          // if shift, we invert the selection of the token
          if (isSelected) {
            selectionModel.removeTokensFromSelection(Collections.singletonList(token.getId()));
          } else {
            selectionModel.addTokensToSelection(Collections.singletonList(token.getId()));
          }
        } else if (!isSelected) {
          // if not shift and click on non-selected token, switch selection to the token
          isNewTokenSelected = true;
          selectionModel.replaceSelection(Collections.singletonList(token.getId()));
        }
        // Position on the zone of the click
        ZonePoint pos = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);

        // Offset specific to the token
        Point tokenOffset = token.getDragOffset(getZone());

        dragOffsetX = pos.x - tokenOffset.x;
        dragOffsetY = pos.y - tokenOffset.y;
      }
    } else {
      if (SwingUtilities.isLeftMouseButton(e)) {
        // Starting a bound box selection
        isDrawingSelectionBox = true;
        selectionBoundBox = new Rectangle(e.getX(), e.getY(), 0, 0);
      } else {
        if (tokenUnderMouse != null) {
          isNewTokenSelected = true;
        }
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (isShowingTokenStackPopup) {
      if (tokenStackPanel.contains(e.getX(), e.getY())) {
        tokenStackPanel.handleMouseEvent(e);
        return;
      } else {
        isShowingTokenStackPopup = false;
        repaint();
      }
    }

    if (isResizingToken) {
      renderer.flush(tokenBeingResized);
      MapTool.serverCommand().putToken(renderer.getZone().getId(), tokenBeingResized);
      isResizingToken = false;
      isResizingRotatedToken = false;
      tokenBeingResized = null;
      return;
    }

    if (SwingUtilities.isLeftMouseButton(e)) {
      try {
        SwingUtil.showPointer(renderer);

        // SELECTION BOUND BOX
        final var selectionModel = renderer.getSelectionModel();
        if (isDrawingSelectionBox) {
          isDrawingSelectionBox = false;

          if (renderer.isAutoResizeStamp()) {
            resizeStamp();
            renderer.setAutoResizeStamp(false);
            renderer.setCursor(Cursor.getDefaultCursor());
          } else {
            final var tokens = renderer.getTokenIdsInBounds(selectionBoundBox);
            if (!SwingUtil.isShiftDown(e)) {
              selectionModel.replaceSelection(tokens);
            } else {
              selectionModel.addTokensToSelection(tokens);
            }
          }

          selectionBoundBox = null;
          renderer.repaint();
          return;
        }

        // DRAG TOKEN COMPLETE
        if (isDraggingToken) {
          stopTokenDrag();
        } else {
          // IF SELECTING MULTIPLE, SELECT SINGLE TOKEN
          if (!SwingUtil.isShiftDown(e)) {
            Token token = getTokenAt(e.getX(), e.getY());
            // Mouse down already selected the token. Now let's enforce it being the only one.
            // ... but only if it isn't being moved at the same time.
            if (token != null
                && selectionModel.isSelected(token.getId())
                && !renderer.isTokenMoving(token)) {
              selectionModel.replaceSelection(Collections.singletonList(token.getId()));
            }
          }
        }
      } finally {
        isDraggingToken = false;
        isDrawingSelectionBox = false;
      }
      return;
    }
    // POPUP MENU
    if (SwingUtilities.isRightMouseButton(e) && !isDraggingToken && !isDraggingMap()) {
      final var selectionModel = renderer.getSelectionModel();
      if (tokenUnderMouse != null && !selectionModel.isSelected(tokenUnderMouse.getId())) {
        if (!SwingUtil.isShiftDown(e)) {
          selectionModel.replaceSelection(Collections.singletonList(tokenUnderMouse.getId()));
        } else {
          selectionModel.addTokensToSelection(Collections.singletonList(tokenUnderMouse.getId()));
        }
        isNewTokenSelected = false;
      }
      if (tokenUnderMouse != null && renderer.getSelectedTokenSet().size() > 0) {
        if (tokenUnderMouse.getLayer() != Layer.TOKEN) {
          new StampPopupMenu(
                  renderer.getSelectedTokenSet(), e.getX(), e.getY(), renderer, tokenUnderMouse)
              .showPopup(renderer);
        } else {
          new TokenPopupMenu(
                  renderer.getSelectedTokenSet(), e.getX(), e.getY(), renderer, tokenUnderMouse)
              .showPopup(renderer);
        }
        return;
      }
    }
    super.mouseReleased(e);
  }

  // //
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
    super.mouseMoved(e);

    if (isShowingTokenStackPopup) {
      if (tokenStackPanel.contains(e.getX(), e.getY())) {
        return;
      }
      // Turn it off
      isShowingTokenStackPopup = false;
      repaint();
      return;
    }
    mouseX = e.getX();
    mouseY = e.getY();

    if (isDraggingToken) {
      if (isMovingWithKeys) {
        return;
      }
      ZonePoint zonePoint = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
      handleDragToken(zonePoint);
      return;
    }
    tokenUnderMouse = getTokenAt(mouseX, mouseY);
    renderer.setMouseOver(tokenUnderMouse);
  }

  private Token getTokenAt(int x, int y) {
    Token token = renderer.getTokenAt(mouseX, mouseY);
    if (token == null) {
      for (var entry : resizeBoundsMap.entrySet()) {
        if (entry.getKey().contains(mouseX, mouseY)) {
          token = entry.getValue();
        }
      }
    }
    return token;
  }

  private ScreenPoint getNearestVertex(ScreenPoint point) {
    ZonePoint zp = point.convertToZone(renderer);
    zp = renderer.getZone().getNearestVertex(zp);
    return ScreenPoint.fromZonePoint(renderer, zp);
  }

  ScreenPoint p = new ScreenPoint(0, 0);

  @Override
  public void mouseDragged(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();

    if (renderer.isAutoResizeStamp() && SwingUtilities.isLeftMouseButton(e)) {
      int x1 = dragStartX;
      int y1 = dragStartY;

      int x2 = mouseX;
      int y2 = mouseY;

      selectionBoundBox.x = Math.min(x1, x2);
      selectionBoundBox.y = Math.min(y1, y2);
      selectionBoundBox.width = Math.abs(x1 - x2);
      selectionBoundBox.height = Math.abs(y1 - y2);

      renderer.repaint();
      return;
    }

    if (isShowingTokenStackPopup) {
      isShowingTokenStackPopup = false;
      if (tokenStackPanel.contains(e.getX(), e.getY())) {
        tokenStackPanel.handleMouseMotionEvent(e);
        return;
      } else {
        renderer.repaint();
      }
    }

    if (isResizingToken) {
      // Fixing a bug here. Need to adjust for Anchor points - Jamz
      int anchorX = (int) (tokenBeingResized.getAnchor().x * renderer.getScale());
      int anchorY = (int) (tokenBeingResized.getAnchor().y * renderer.getScale());
      ScreenPoint sp =
          new ScreenPoint(mouseX + dragOffsetX - anchorX, mouseY + dragOffsetY - anchorY);

      BufferedImage image = ImageManager.getImage(tokenBeingResized.getImageAssetId());

      if (SwingUtil.isControlDown(e)) { // snap size to grid
        sp = getNearestVertex(sp);
      }
      boolean isRotated =
          tokenBeingResized.hasFacing()
              && tokenBeingResized.getShape() == Token.TokenShape.TOP_DOWN
              && tokenBeingResized.getFacing() != -90;
      if (!isRotated
          && SwingUtil.isShiftDown(e)) { // lock aspect ratio -- broken for rotated images
        ScreenPoint tokenPoint =
            ScreenPoint.fromZonePoint(renderer, tokenBeingResized.getX(), tokenBeingResized.getY());

        double ratio = image.getWidth() / (double) image.getHeight();
        int dx = (int) (sp.x - tokenPoint.x);

        sp.y = (int) (tokenPoint.y + (dx / ratio));
      }
      ZonePoint zp = sp.convertToZone(renderer);
      p = ScreenPoint.fromZonePoint(renderer, zp);

      // For snap-to-grid tokens (except background stamps) we anchor at the center of the token.
      // TODO Named for good diff. Rename to isSnappedAndCenterAnchored
      final var isSnappedNonBackground =
          tokenBeingResized.isSnapToGrid() && tokenBeingResized.getLayer() != Layer.BACKGROUND;
      final var snapToGridMultiplier = isSnappedNonBackground ? 2 : 1;

      int newWidth = Math.max(1, (zp.x - tokenBeingResized.getX()) * snapToGridMultiplier);
      int newHeight = Math.max(1, (zp.y - tokenBeingResized.getY()) * snapToGridMultiplier);

      if (SwingUtil.isControlDown(e) && isSnappedNonBackground) {
        // Account for the 1/2 cell on each side of the stamp (since it's anchored in the center)
        newWidth += renderer.getZone().getGrid().getSize();
        newHeight += renderer.getZone().getGrid().getSize();
      }
      // take into account rotated stamps
      if (isRotated) {
        // if we are beginning a new resize, reset the resizing variables.
        if (!isResizingRotatedToken) {
          isResizingRotatedToken = true;
          preciseStampZonePoint =
              new Point2D.Double(tokenBeingResized.getX(), tokenBeingResized.getY());
          lastResizeZonePoint = new ZonePoint(zp.x, zp.y);
        }
        // theta is the rotation angle clockwise from the positive x-axis to compensate for the +ve
        // y-axis
        // pointing downwards in zone space and an unrotated token has facing of -90.
        int theta = -tokenBeingResized.getFacing() - 90;

        // can't handle snap to grid with rotated token when resizing because they have to be able
        // to nudge.
        if (tokenBeingResized.isSnapToGrid()) {
          tokenBeingResized.setSnapToGrid(false);
        }
        Rectangle footprintBounds = tokenBeingResized.getBounds(renderer.getZone());

        // zp = mouse location
        int changeX = (zp.x - lastResizeZonePoint.x) * snapToGridMultiplier;
        int changeY = (zp.y - lastResizeZonePoint.y) * snapToGridMultiplier;

        double sinTheta = Math.sin(Math.toRadians(theta));
        double cosTheta = Math.cos(Math.toRadians(theta));

        // Calculate change in the stamp's height and width.
        // Sine terms are negated from the standard rotation transform because the direction of
        // theta
        // is reversed (theta rotates clockwise)
        double dw = changeX * cosTheta + changeY * sinTheta;
        double dh = -changeX * sinTheta + changeY * cosTheta;

        newWidth = (int) Math.max(1, footprintBounds.width + dw);
        newHeight = (int) Math.max(1, footprintBounds.height + dh);

        // Move the stamp to compensate for a change in the stamp's rotation anchor
        // so that the stamp stays fixed in place while being resized

        // change in stamp's rotation anchor due to resize
        double dx = dw / 2;
        double dy = dh / 2;

        // change in rotated stamp's anchor due to resize. currently only works perfectly for
        // clockwise 0-90
        // needs fine tuning for the three other quadrants to prevent the stamp from creeping
        double dxRot = dx * cosTheta - dy * sinTheta;
        double dyRot = dx * sinTheta + dy * cosTheta;

        // Resizing a stamp automatically adjusts its rotation anchor point, so only consider the
        // adjustment required due to the rotation.
        double stampAdjustX = dxRot - dx;
        double stampAdjustY = dyRot - dy;

        // prevent the stamp from moving around if a limit has been reached.
        if (newWidth == 1 || newHeight == 1) {
          newWidth = newWidth == 1 ? 1 : footprintBounds.width;
          newHeight = newHeight == 1 ? 1 : footprintBounds.height;
        } else {
          // remembering the precise location prevents the stamp from drifting due to rounding to
          // int
          preciseStampZonePoint.x += stampAdjustX;
          preciseStampZonePoint.y += stampAdjustY;

          lastResizeZonePoint = (ZonePoint) zp.clone();
        }
        tokenBeingResized.setX((int) (preciseStampZonePoint.x));
        tokenBeingResized.setY((int) (preciseStampZonePoint.y));
      }
      tokenBeingResized.setScaleX(newWidth / (double) image.getWidth());
      tokenBeingResized.setScaleY(newHeight / (double) image.getHeight());

      renderer.repaint();
      return;
    }

    CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
    if (cellUnderMouse != null) {
      MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
    }

    if (SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isRightMouseButton(e)) {
      if (isDrawingSelectionBox) {
        int x1 = dragStartX;
        int y1 = dragStartY;

        int x2 = e.getX();
        int y2 = e.getY();

        selectionBoundBox.x = Math.min(x1, x2);
        selectionBoundBox.y = Math.min(y1, y2);
        selectionBoundBox.width = Math.abs(x1 - x2);
        selectionBoundBox.height = Math.abs(y1 - y2);

        renderer.repaint();
        return;
      }

      if (isDraggingToken) {
        if (isMovingWithKeys) {
          return;
        }
        ZonePoint zonePoint = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
        handleDragToken(zonePoint);
        return;
      }

      if (tokenUnderMouse == null
          || !renderer.getSelectedTokenSet().contains(tokenUnderMouse.getId())) {
        return;
      }

      if (!isDraggingToken && renderer.isTokenMoving(tokenUnderMouse)) {
        return;
      }

      if (isNewTokenSelected) {
        renderer
            .getSelectionModel()
            .replaceSelection(Collections.singletonList(tokenUnderMouse.getId()));
      }

      isNewTokenSelected = false;

      // Make user we're allowed
      if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
        return;
      }

      // Might be dragging a token
      String playerId = MapTool.getPlayer().getName();
      Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
      if (selectedTokenSet.size() > 0) {
        // Make sure we can do this
        if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
          for (GUID tokenGUID : selectedTokenSet) {
            Token token = renderer.getZone().getToken(tokenGUID);
            if (!token.isOwner(playerId)) {
              return;
            }
          }
        }
        Point origin = new Point(tokenUnderMouse.getX(), tokenUnderMouse.getY());

        origin.translate(dragOffsetX, dragOffsetY);

        startTokenDrag(tokenUnderMouse, selectedTokenSet);
        isDraggingToken = true;
        SwingUtil.hidePointer(renderer);
      }

      return;
    }

    super.mouseDragged(e);
  }

  public boolean isDraggingToken() {
    return isDraggingToken;
  }

  /**
   * Move the keytoken being dragged to this zone point
   *
   * @param zonePoint the zone point to move to
   * @return true if the move was successful
   */
  public boolean handleDragToken(ZonePoint zonePoint) {
    // TODO: Optimize this (combine with calling code)
    if (tokenBeingDragged.isSnapToGrid()
        && getZone().getGrid().getCapabilities().isSnapToGridSupported()) {
      zonePoint.translate(-dragOffsetX, -dragOffsetY);
      CellPoint cellUnderMouse = renderer.getZone().getGrid().convert(zonePoint);
      zonePoint = renderer.getZone().getGrid().convert(cellUnderMouse);
      MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
    } else {
      zonePoint.translate(-dragOffsetX, -dragOffsetY);
    }
    // Don't bother if there isn't any movement
    if (!renderer.hasMoveSelectionSetMoved(tokenBeingDragged.getId(), zonePoint)) {
      return false;
    }
    dragStartX = zonePoint.x;
    dragStartY = zonePoint.y;

    renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), zonePoint);
    MapTool.serverCommand()
        .updateTokenMove(
            renderer.getZone().getId(), tokenBeingDragged.getId(), zonePoint.x, zonePoint.y);
    return true;
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(AppActions.CUT_TOKENS.getKeyStroke(), AppActions.CUT_TOKENS);
    actionMap.put(AppActions.COPY_TOKENS.getKeyStroke(), AppActions.COPY_TOKENS);
    actionMap.put(AppActions.PASTE_TOKENS.getKeyStroke(), AppActions.PASTE_TOKENS);
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_R, AppActions.menuShortcut),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (renderer.getSelectedTokenSet().isEmpty()) {
              return;
            }
            Toolbox toolbox = MapTool.getFrame().getToolbox();
            FacingTool tool = (FacingTool) toolbox.getTool(FacingTool.class);
            tool.init(
                renderer.getZone().getToken(renderer.getSelectedTokenSet().iterator().next()),
                renderer.getSelectedTokenSet());
            toolbox.setSelectedTool(FacingTool.class);
          }
        });
    // TODO: Optimize this by making it non anonymous
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ToolHelper.getDeleteTokenAction());
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_D, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (!isDraggingToken) {
              return;
            }
            // Stop
            stopTokenDrag();
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (!isDraggingToken) {
              return;
            }
            // Stop
            stopTokenDrag();
          }
        });

    // TODO Should these keystrokes be based on the grid type, like they are in PointerTool?
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, 0, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, 0, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, -1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, 1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, -1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, -1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, 1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, 1, false);
          }
        });

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, 0, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, 0, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, -1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, 1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, -1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, -1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, 1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, 1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_T, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            renderer.cycleSelectedToken(1);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            renderer.cycleSelectedToken(-1);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, 1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, 0, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, 0, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, -1, false);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, 1, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(1, 0, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(-1, 0, true);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            handleKeyMove(0, -1, true);
          }
        });
  }

  private void handleKeyMove(int dx, int dy, boolean micro) {
    if (!isDraggingToken) {
      // Start
      Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
      if (selectedTokenSet.size() != 1) {
        // only allow one at a time
        return;
      }
      Token token = renderer.getZone().getToken(selectedTokenSet.iterator().next());
      if (token == null) {
        return;
      }
      // Only one person at a time
      if (renderer.isTokenMoving(token)) {
        return;
      }
      dragStartX = token.getX();
      dragStartY = token.getY();
      startTokenDrag(token, selectedTokenSet);
    }
    if (!isMovingWithKeys) {
      dragOffsetX = 0;
      dragOffsetY = 0;
    }
    ZonePoint zp = null;
    if (tokenBeingDragged.isSnapToGrid()) {
      CellPoint cp = renderer.getZone().getGrid().convert(new ZonePoint(dragStartX, dragStartY));

      cp.x += dx;
      cp.y += dy;

      zp = renderer.getZone().getGrid().convert(cp);
    } else {
      Rectangle tokenSize = tokenBeingDragged.getBounds(renderer.getZone());

      int x = dragStartX + (micro ? dx : (tokenSize.width * dx));
      int y = dragStartY + (micro ? dy : (tokenSize.height * dy));

      zp = new ZonePoint(x, y);
    }
    isMovingWithKeys = true;
    handleDragToken(zp);
    if (tokenBeingDragged.getLayer() == Layer.BACKGROUND) {
      stopTokenDrag();
    }
  }

  // //
  // ZoneOverlay
  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.client.ZoneOverlay#paintOverlay(net.rptools.maptool .client.ZoneRenderer, java.awt.Graphics2D)
   */
  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (selectionBoundBox != null) {
      if (renderer.isAutoResizeStamp()) {
        Stroke stroke = g.getStroke();
        final float dash1[] = {10.0f, 5.0f};
        final BasicStroke dashed =
            new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
        g.setStroke(dashed);

        Composite composite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .15f));
        g.setPaint(AppStyle.resizeBoxFill);
        g.fillRect(
            selectionBoundBox.x,
            selectionBoundBox.y,
            selectionBoundBox.width,
            selectionBoundBox.height);
        g.setComposite(composite);

        g.setColor(AppStyle.resizeBoxOutline);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawRect(
            selectionBoundBox.x,
            selectionBoundBox.y,
            selectionBoundBox.width,
            selectionBoundBox.height);

        g.setStroke(stroke);
      } else {
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(2));

        if (AppPreferences.getFillSelectionBox()) {
          Composite composite = g.getComposite();
          g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .25f));
          g.setPaint(AppStyle.selectionBoxFill);
          g.fillRoundRect(
              selectionBoundBox.x,
              selectionBoundBox.y,
              selectionBoundBox.width,
              selectionBoundBox.height,
              10,
              10);
          g.setComposite(composite);
        }
        g.setColor(AppStyle.selectionBoxOutline);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawRoundRect(
            selectionBoundBox.x,
            selectionBoundBox.y,
            selectionBoundBox.width,
            selectionBoundBox.height,
            10,
            10);

        g.setStroke(stroke);
      }
    }

    if (isShowingTokenStackPopup) {
      tokenStackPanel.paint(g);
    } else {
      resizeBoundsMap.clear();
      // rotateBoundsMap.clear();
      for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        if (token.getLayer() == Layer.TOKEN) {
          return;
        }
        // Show sizing controls
        // getTokenBounds() pulls the data from the tokenLocationCache in ZoneRenderer. That cache
        // is populated inside renderer.renderTokens(). As long as the cache is created first, we
        // should
        // be good, right? This code relies on the order of operations in another class! Ugh!
        // Double-ugh! :)
        Area bounds = renderer.getTokenBounds(token);
        if (bounds == null || renderer.isTokenMoving(token)) {
          continue;
        }
        // Resize
        if (!token.isSnapToScale()) {
          double scale = renderer.getScale();
          Rectangle footprintBounds = token.getBounds(renderer.getZone());

          double scaledWidth = (footprintBounds.width * scale);
          double scaledHeight = (footprintBounds.height * scale);

          ScreenPoint stampLocation =
              ScreenPoint.fromZonePoint(renderer, footprintBounds.x, footprintBounds.y);

          // distance to place the resize image in the lower left corner of an unrotated stamp
          double tx = stampLocation.x + scaledWidth - resizeImg.getWidth();
          double ty = stampLocation.y + scaledHeight - resizeImg.getHeight();

          Rectangle resizeBounds = new Rectangle(0, 0, resizeImg.getHeight(), resizeImg.getWidth());
          Area resizeBoundsArea = new Area(resizeBounds);

          AffineTransform at = new AffineTransform();
          at.translate(tx, ty);

          // Rotated
          if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
            // untested when anchor != (0,0)
            // rotate the resize image with the stamp.
            double theta = Math.toRadians(-token.getFacing() - 90);
            double anchorX =
                -scaledWidth / 2 + resizeImg.getWidth() - (token.getAnchor().x * scale);
            double anchorY =
                -scaledHeight / 2 + resizeImg.getHeight() - (token.getAnchor().y * scale);
            at.rotate(theta, anchorX, anchorY);
          }
          // place the map over the image.
          resizeBoundsArea.transform(at);
          resizeBoundsMap.put(resizeBoundsArea, token);

          g.drawImage(resizeImg, at, renderer);
        }

        // g.setColor(Color.red);
        // g.fillRect((int)(p.x-2), (int)(p.y-2), 4, 4);
        //
        // // Rotate
        // int length = 35;
        // int cx = bounds.x + bounds.width/2;
        // int cy = bounds.y + bounds.height/2;
        // int facing = token.getFacing() != null ? token.getFacing() : 0;
        //
        // int x = (int)(cx + Math.cos(Math.toRadians(facing)) * length);
        // int y = (int)(cy - Math.sin(Math.toRadians(facing)) * length);
        //
        // Ellipse2D rotateBounds = new Ellipse2D.Float(x-5, y-5, 10, 10);
        // rotateBoundsMap.put(rotateBounds, token);
        //
        // g.setColor(Color.black);
        // g.drawLine(cx, cy, x, y);
        // g.fill(rotateBounds);
        //
        // g.setColor(Color.gray);
        // g.draw(rotateBounds);
      }
    }
  }

  public void resizeStamp() {
    if (tokenUnderMouse == null) {
      // Cancel action, didn't start/end the selection over the stamp
      JOptionPane.showMessageDialog(
          null,
          I18N.getText("dialog.stampTool.error.noSelection"),
          I18N.getText("msg.title.messageDialogError"),
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (selectionBoundBox.width <= 1 || selectionBoundBox.height <= 1) {
      // Cancel action, didn't select enough pixels
      JOptionPane.showMessageDialog(
          null,
          I18N.getText("dialog.stampTool.error.badSelection"),
          I18N.getText("msg.title.messageDialogError"),
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    double selectedWidth = selectionBoundBox.width;
    double selectedHeight = selectionBoundBox.height;
    double currentScaleX = tokenUnderMouse.getScaleX();
    double currentScaleY = tokenUnderMouse.getScaleY();
    boolean adjustAnchor = true;

    AutoResizeStampDialog dialog =
        new AutoResizeStampDialog(
            selectionBoundBox.width,
            selectionBoundBox.height,
            tokenUnderMouse.getWidth(),
            tokenUnderMouse.getHeight(),
            tokenUnderMouse.getAnchor().x,
            tokenUnderMouse.getAnchor().y);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(MapTool.getFrame());
    dialog.setVisible(true);

    double cellWidthSelected = dialog.getCellWidthSelected();
    double cellHeightSelected = dialog.getCellHeightSelected();
    selectedWidth = dialog.getPixelWidthSelected();
    selectedHeight = dialog.getPixelHeightSelected();

    if (selectedWidth > 0
        && selectedHeight > 0
        && cellWidthSelected > 0
        && cellHeightSelected > 0) {
      double gridSize = renderer.getZone().getGrid().getSize();
      double zoneScale = renderer.getScale();
      double newScaleX =
          ((gridSize * cellWidthSelected) / (selectedWidth / zoneScale)) * currentScaleX;
      double newScaleY =
          ((gridSize * cellHeightSelected) / (selectedHeight / zoneScale)) * currentScaleY;

      tokenUnderMouse.setScaleX(newScaleX);
      tokenUnderMouse.setScaleY(newScaleY);

      MapTool.getFrame().refresh();
      if (adjustAnchor)
        adjustAnchor(1 + (newScaleX - currentScaleX), 1 + (newScaleY - currentScaleY));
    }

    // AppState.setShowGrid(showGrid);
    MapTool.getFrame().refresh();
  }

  public void adjustAnchor(double scaleX, double scaleY) {
    ZonePoint selectionTr =
        ScreenPoint.convertToZone(renderer, selectionBoundBox.getX(), selectionBoundBox.getY());
    int gridSize = renderer.getZone().getGrid().getSize();
    int tokenX = tokenUnderMouse.getX() + tokenUnderMouse.getAnchor().x;
    int tokenY = tokenUnderMouse.getY() + tokenUnderMouse.getAnchor().y;

    int x = (int) ((selectionTr.x - tokenX) * scaleX);
    x = x - (((x / gridSize) + 1) * gridSize);

    int y = (int) ((selectionTr.y - tokenY) * scaleY);
    y = y - (((y / gridSize) + 1) * gridSize);

    tokenUnderMouse.setAnchor(-x, -y);
  }

  public Point getAdjustedAnchor(double scaleX, double scaleY) {
    ZonePoint selectionTr =
        ScreenPoint.convertToZone(renderer, selectionBoundBox.getX(), selectionBoundBox.getY());
    int gridSize = renderer.getZone().getGrid().getSize();
    int tokenX = tokenUnderMouse.getX() + tokenUnderMouse.getAnchor().x;
    int tokenY = tokenUnderMouse.getY() + tokenUnderMouse.getAnchor().y;

    int x = (int) ((selectionTr.x - tokenX) * scaleX);
    x = x - (((x / gridSize) + 1) * gridSize);

    int y = (int) ((selectionTr.y - tokenY) * scaleY);
    y = y - (((y / gridSize) + 1) * gridSize);

    return new Point(-x, -y);
  }
}
