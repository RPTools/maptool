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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
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
import net.rptools.maptool.client.DeveloperOptions;
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
  private boolean isNewTokenSelected;
  private boolean isDrawingSelectionBox;
  private Rectangle selectionBoundBox;

  private Token tokenUnderMouse;

  private final TokenStackPanel tokenStackPanel = new TokenStackPanel();

  // private Map<Shape, Token> rotateBoundsMap = new HashMap<Shape, Token>();
  private final Map<Shape, Token> resizeBoundsMap = new HashMap<Shape, Token>();

  // Keeps track of the start of a token drag, in screen coordinates.
  // Useful for drawing selection boxes and resizing tokens.
  private int dragStartX;
  private int dragStartY;

  private @Nullable TokenDragOp tokenDragOp;
  private @Nullable TokenResizeOp tokenResizeOp;

  private BufferedImage resizeImg = RessourceManager.getImage(Images.RESIZE);

  public StampTool() {}

  @Override
  protected void selectedLayerChanged(Layer layer) {
    super.selectedLayerChanged(layer);
    if (!layer.isStampLayer() && MapTool.getFrame() != null) {
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
    startTokenDrag(
        keyToken, tokens, new ScreenPoint(dragStartX, dragStartY).convertToZone(renderer), false);
  }

  private void startTokenDrag(
      Token keyToken, Set<GUID> tokens, ZonePoint dragStart, boolean isMovingWithKeys) {
    if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
      // Not allowed
      return;
    }
    renderer.addMoveSelectionSet(MapTool.getPlayer().getName(), keyToken.getId(), tokens);
    MapTool.serverCommand()
        .startTokenMove(
            MapTool.getPlayer().getName(), renderer.getZone().getId(), keyToken.getId(), tokens);

    tokenDragOp = new TokenDragOp(renderer, keyToken, dragStart, isMovingWithKeys);
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
                    location.getToken(),
                    Collections.singleton(location.getToken().getId()),
                    // TODO is dragstart even correct in this case? I know it's not from the map
                    // explorer
                    new ScreenPoint(dragStartX, dragStartY).convertToZone(renderer),
                    false);
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

    if (tokenDragOp != null) {
      return;
    }

    dragStartX = e.getX();
    dragStartY = e.getY();

    // Check token resizing
    for (Entry<Shape, Token> entry : resizeBoundsMap.entrySet()) {
      Shape bounds = entry.getKey();
      if (bounds.contains(dragStartX, dragStartY)) {
        // The token being resized does not necessarily = tokenUnderMouse. If there is more then one
        // token under the mouse, the top token will be the tokenUnderMouse, but it is the selected
        // that is intended to be resized.
        tokenResizeOp =
            new TokenResizeOp(
                renderer,
                entry.getValue(),
                dragStartX,
                dragStartY,
                bounds.getBounds().x + bounds.getBounds().width - e.getX(),
                bounds.getBounds().y + bounds.getBounds().height - e.getY());

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
        && tokenDragOp == null
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

    if (tokenResizeOp != null) {
      tokenResizeOp.finish();
      tokenResizeOp = null;
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
        if (tokenDragOp != null) {
          tokenDragOp.finish();
          tokenDragOp = null;
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
        tokenDragOp = null;
        isDrawingSelectionBox = false;
      }
      return;
    }
    // POPUP MENU
    if (SwingUtilities.isRightMouseButton(e) && tokenDragOp == null && !isDraggingMap()) {
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
        if (tokenUnderMouse.getLayer().isStampLayer()) {
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
    super.mouseMoved(e);

    if (renderer == null) {
      return;
    }

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

    if (tokenDragOp != null) {
      tokenDragOp.dragTo(mouseX, mouseY);
      return;
    }
    tokenUnderMouse = getTokenAt(mouseX, mouseY);
    renderer.setMouseOver(tokenUnderMouse);
  }

  private Token getTokenAt(int x, int y) {
    Token token = renderer.getTokenAt(x, y);
    if (token == null) {
      for (var entry : resizeBoundsMap.entrySet()) {
        if (entry.getKey().contains(x, y)) {
          token = entry.getValue();
        }
      }
    }
    return token;
  }

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

    if (tokenResizeOp != null) {
      tokenResizeOp.dragTo(mouseX, mouseY, SwingUtil.isShiftDown(e), SwingUtil.isControlDown(e));
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

      if (tokenDragOp != null) {
        tokenDragOp.dragTo(mouseX, mouseY);
        return;
      }

      if (tokenUnderMouse == null
          || !renderer.getSelectedTokenSet().contains(tokenUnderMouse.getId())) {
        return;
      }

      if (tokenDragOp == null && renderer.isTokenMoving(tokenUnderMouse)) {
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
        startTokenDrag(
            tokenUnderMouse,
            selectedTokenSet,
            new ScreenPoint(dragStartX, dragStartY).convertToZone(renderer),
            false);
        SwingUtil.hidePointer(renderer);
      }

      return;
    }

    super.mouseDragged(e);
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
            if (tokenDragOp == null) {
              return;
            }

            tokenDragOp.finish();
            tokenDragOp = null;
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (tokenDragOp == null) {
              return;
            }

            tokenDragOp.finish();
            tokenDragOp = null;
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
    if (tokenDragOp == null) {
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
      startTokenDrag(token, selectedTokenSet, new ZonePoint(token.getX(), token.getY()), true);
    }

    if (tokenDragOp == null) {
      // Typically would be set in startTokenDrag() above, but not if server policy prevents it.
      return;
    }

    tokenDragOp.moveByKey(dx, dy, micro);
    if (tokenDragOp.tokenBeingDragged.getLayer().oneStepKeyDrag()) {
      tokenDragOp.finish();
      tokenDragOp = null;
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
        if (!token.getLayer().isStampLayer()) {
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
            double theta = Math.toRadians(token.getFacingInDegrees());
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
      }
    }
  }

  private void resizeStamp() {
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

  private static final class TokenDragOp {
    private final ZoneRenderer renderer;
    private final Token tokenBeingDragged;
    private boolean isMovingWithKeys;

    private final ZonePoint dragAnchor;
    // For snap-to-grid, the distance between the drag anchor and the snapped version of the drag
    // anchor.
    private final int snapOffsetX;
    private final int snapOffsetY;
    // Keeps track of the start and end of a token drag, in map coordinates.
    // Useful for smoothly dragging tokens.
    private final ZonePoint tokenDragStart;
    private ZonePoint tokenDragCurrent;

    public TokenDragOp(
        ZoneRenderer renderer,
        Token tokenBeingDragged,
        ZonePoint dragStart,
        boolean isMovingWithKeys) {
      this.renderer = renderer;
      this.tokenBeingDragged = tokenBeingDragged;
      this.isMovingWithKeys = isMovingWithKeys;

      // Drag offset is used to make the drag behave as if started at the token's drag point.
      this.dragAnchor = tokenBeingDragged.getDragAnchor(renderer.getZone());
      this.snapOffsetX = dragAnchor.x - tokenBeingDragged.getX();
      this.snapOffsetY = dragAnchor.y - tokenBeingDragged.getY();

      this.tokenDragStart = new ZonePoint(dragStart);
      this.tokenDragCurrent = new ZonePoint(this.tokenDragStart);
    }

    public void finish() {
      renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
    }

    public void dragTo(int mouseX, int mouseY) {
      if (isMovingWithKeys) {
        return;
      }

      final boolean debugEnabled = DeveloperOptions.Toggle.DebugTokenDragging.isEnabled();

      if (debugEnabled) {
        renderer.setShape3(
            new Rectangle2D.Double(tokenDragStart.x - 5, tokenDragStart.y - 5, 10, 10));
        renderer.setShape4(new Rectangle2D.Double(dragAnchor.x - 5, dragAnchor.y - 5, 10, 10));
      }

      ZonePoint zonePoint = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);

      zonePoint.x = this.dragAnchor.x + zonePoint.x - tokenDragStart.x;
      zonePoint.y = this.dragAnchor.y + zonePoint.y - tokenDragStart.y;

      var grid = renderer.getZone().getGrid();
      if (tokenBeingDragged.isSnapToGrid() && grid.getCapabilities().isSnapToGridSupported()) {
        // Snap to grid point.
        zonePoint = grid.convert(grid.convert(zonePoint));

        if (debugEnabled) {
          renderer.setShape(new Rectangle2D.Double(zonePoint.x - 5, zonePoint.y - 5, 10, 10));
        }

        // Adjust given offet from grid to anchor point.
        zonePoint.x += this.snapOffsetX;
        zonePoint.y += this.snapOffsetY;
      }

      if (debugEnabled) {
        renderer.setShape2(new Rectangle2D.Double(zonePoint.x - 5, zonePoint.y - 5, 10, 10));
      }

      doDragTo(zonePoint);
    }

    public void moveByKey(int dx, int dy, boolean micro) {
      isMovingWithKeys = true;

      ZonePoint zp;
      if (tokenBeingDragged.isSnapToGrid()) {
        var grid = renderer.getZone().getGrid();
        CellPoint cp = grid.convert(tokenDragCurrent);
        cp.x += dx;
        cp.y += dy;
        zp = grid.convert(cp);

        zp.x += snapOffsetX;
        zp.y += snapOffsetY;
      } else {
        Rectangle tokenSize = tokenBeingDragged.getBounds(renderer.getZone());
        int x = tokenDragCurrent.x + (micro ? dx : (tokenSize.width * dx));
        int y = tokenDragCurrent.y + (micro ? dy : (tokenSize.height * dy));
        zp = new ZonePoint(x, y);
      }

      doDragTo(zp);
    }

    private void doDragTo(ZonePoint newAnchorPoint) {
      tokenDragCurrent = new ZonePoint(newAnchorPoint);

      // Don't bother if there isn't any movement
      if (!renderer.hasMoveSelectionSetMoved(tokenBeingDragged.getId(), newAnchorPoint)) {
        return;
      }

      renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), newAnchorPoint);
      MapTool.serverCommand()
          .updateTokenMove(
              renderer.getZone().getId(),
              tokenBeingDragged.getId(),
              newAnchorPoint.x,
              newAnchorPoint.y);
    }
  }

  private record Vector2(double x, double y) {
    public static Vector2 sub(ZonePoint lhs, ZonePoint rhs) {
      return new Vector2(lhs.x - rhs.x, lhs.y - rhs.y);
    }

    public double dot(Vector2 other) {
      return x * other.x + y * other.y;
    }
  }

  private static final class TokenResizeOp {
    private final int dragOffsetX;
    private final int dragOffsetY;
    private final ZoneRenderer renderer;
    private final Token tokenBeingResized;
    private final Vector2 down;
    private final Vector2 right;

    // The position of the bottom-right corner of the token, assuming it is not rotated.
    private final double originalScaleX;
    private final double originalScaleY;
    private final ZonePoint startDragReference;

    private final BufferedImage tokenImage;

    public TokenResizeOp(
        ZoneRenderer renderer,
        Token tokenBeingResized,
        int dragStartX,
        int dragStartY,
        int dragOffsetX,
        int dragOffsetY) {
      this.dragOffsetX = dragOffsetX;
      this.dragOffsetY = dragOffsetY;

      this.renderer = renderer;
      this.tokenBeingResized = tokenBeingResized;

      // theta is the rotation angle clockwise from the positive x-axis to compensate for the +ve
      // y-axis pointing downwards in zone space and an unrotated token has facing of -90.
      // theta == 0 => token has default rotation.
      int theta = tokenBeingResized.getFacingInDegrees();
      double radians = Math.toRadians(theta);
      this.down = new Vector2(-Math.sin(radians), Math.cos(radians));
      this.right = new Vector2(Math.cos(radians), Math.sin(radians));

      this.originalScaleX = tokenBeingResized.getScaleX();
      this.originalScaleY = tokenBeingResized.getScaleY();
      this.startDragReference =
          new ScreenPoint(dragStartX + dragOffsetX, dragStartY + dragOffsetY)
              .convertToZone(renderer);

      this.tokenImage = ImageManager.getImage(tokenBeingResized.getImageAssetId());
    }

    public void finish() {
      renderer.flush(tokenBeingResized);
      MapTool.serverCommand().putToken(renderer.getZone().getId(), tokenBeingResized);
    }

    public void dragTo(int mouseX, int mouseY, boolean lockAspectRatio, boolean snapSizeToGrid) {
      var currentZp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
      if (snapSizeToGrid) { // snap size to grid
        currentZp = getNearestVertex(currentZp);
      } else {
        // Keep the cursor at the same conceptual position in the drag handle.
        currentZp.x += dragOffsetX;
        currentZp.y += dragOffsetY;
      }

      // Measured in map coordinates
      var displacement = Vector2.sub(currentZp, startDragReference);
      // Measured in the token's rotated frame, at map scale.
      var adjustment = new Vector2(right.dot(displacement), down.dot(displacement));

      if (lockAspectRatio) { // lock aspect ratio
        // In general it is not possible to satisfy both lockAspectRatio and snapSizeToGrid. So
        // instead we snap size to grid, then constrain the aspect ratio afterwards, which is this
        // logic.
        double ratio = tokenImage.getWidth() / (double) tokenImage.getHeight();
        adjustment = new Vector2(adjustment.x, adjustment.x / ratio);
      }

      // For snap-to-grid tokens (except background stamps) we anchor at the center of the token.
      final var isSnapToGridAndAnchoredAtCenter =
          tokenBeingResized.isSnapToGrid()
              && tokenBeingResized.getLayer().anchorSnapToGridAtCenter();
      final var snapToGridMultiplier = isSnapToGridAndAnchoredAtCenter ? 2 : 1;
      var widthIncrease = adjustment.x * snapToGridMultiplier;
      var heightIncrease = adjustment.y * snapToGridMultiplier;

      var originalWidth = tokenImage.getWidth() * originalScaleX;
      var originalHeight = tokenImage.getHeight() * originalScaleY;
      var updatedWidth = Math.max(1, originalWidth + widthIncrease);
      var updatedHeight = Math.max(1, originalHeight + heightIncrease);

      tokenBeingResized.setScaleX(updatedWidth / (double) tokenImage.getWidth());
      tokenBeingResized.setScaleY(updatedHeight / (double) tokenImage.getHeight());

      renderer.repaint();
    }

    private ZonePoint getNearestVertex(ZonePoint point) {
      return renderer.getZone().getNearestVertex(point);
    }
  }
}
