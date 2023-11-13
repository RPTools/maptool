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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.swing.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.TokenHoverEnter;
import net.rptools.maptool.client.events.TokenHoverExit;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.swing.HTMLPanelRenderer;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.*;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Pointer.Type;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.maptool.util.GraphicsUtil;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;
import net.rptools.maptool.util.TokenUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the pointer tool from the top-level of the toolbar. It allows tokens to be selected and
 * moved, it triggers the statsheet to be displayed, it handles keystroke movement of tokens using
 * the NumPad keys, and it handles positioning the Speech and Thought bubbles when the Spacebar is
 * held down (possibly in combination with Shift or Ctrl).
 */
public class PointerTool extends DefaultTool {
  private static final long serialVersionUID = 8606021718606275084L;
  private static final Logger log = LogManager.getLogger(PointerTool.class);
  private BufferedImage panelTexture = RessourceManager.getImage(Images.TEXTURE_PANEL);

  private boolean isShowingTokenStackPopup;
  private boolean isShowingPointer;
  private boolean isDraggingToken;
  private boolean isNewTokenSelected;
  private boolean isDrawingSelectionBox;
  private boolean isSpaceDown;
  private boolean isMovingWithKeys;
  private Rectangle selectionBoundBox;

  // Hovers
  private boolean isShowingHover;
  private Area hoverTokenBounds;
  private String hoverTokenNotes;

  // Track token interactions to hide statsheets when doing other stuff
  private boolean mouseButtonDown = false;

  private Token tokenBeingDragged;
  private Token tokenUnderMouse;
  private Token markerUnderMouse;
  private int keysDown; // used to record whether Shift/Ctrl/Meta keys are down

  private final TokenStackPanel tokenStackPanel = new TokenStackPanel();
  private final HTMLPanelRenderer htmlRenderer = new HTMLPanelRenderer();
  private final Font boldFont = AppStyle.labelFont.deriveFont(Font.BOLD);

  private BufferedImage statSheet;
  private Token tokenOnStatSheet;

  private static int PADDING = 7;
  private static int STATSHEET_EXTERIOR_PADDING = 5;

  // Offset from token's X,Y when dragging. Values are in zone coordinates.
  private int dragOffsetX = 0;
  private int dragOffsetY = 0;
  private int dragStartX = 0;
  private int dragStartY = 0;

  private String currentPointerName;

  public PointerTool() {
    htmlRenderer.setBackground(new Color(0, 0, 0, 200));
    htmlRenderer.setForeground(Color.black);
    htmlRenderer.setOpaque(false);
    htmlRenderer.addStyleSheetRule("body{color:black}");
    htmlRenderer.addStyleSheetRule(".title{font-size: 14pt}");
  }

  @Override
  protected void selectedLayerChanged(Zone.Layer layer) {
    super.selectedLayerChanged(layer);
    if (layer.isStampLayer()) {
      MapTool.getFrame().getToolbox().setSelectedTool(StampTool.class);
    }
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);

    if (MapTool.getPlayer().isGM()) {
      MapTool.getFrame().showControlPanel(getLayerSelectionDialog());
    }
    htmlRenderer.attach(renderer);

    if (getSelectedLayer().isStampLayer()) {
      MapTool.getFrame().getToolbox().setSelectedTool(StampTool.class);
    }
  }

  /**
   * When implementation is completed, this method will accept a ZoneRenderer parameter and
   * determine that zone's grid style, then query the grid for the keystroke movement it wants to
   * use. Those keystrokes are then added to the InputMap and ActionMap for the component by calling
   * the superclass's addListeners() method.
   *
   * @deprecated
   * @param comp the component to add as listener
   */
  @Deprecated
  protected void addListeners_NOT_USED(JComponent comp) {
    if (comp instanceof ZoneRenderer) {
      Grid grid = ((ZoneRenderer) comp).getZone().getGrid();
      addGridBasedKeys(grid, true);
    }
    super.addListeners(comp);
  }

  /**
   * Let the grid decide which keys perform which kind of movement. This allows hex grids to handle
   * the six-sided shapes intelligently depending on whether the grid is a vertical or horizontal
   * grid. This also moves us one step closer to defining the keys in an external file...
   *
   * <p>Boy, this is ugly. As I pin down fixes for code leading up to MT1.4 I find myself performing
   * criminal acts on the code base. :(
   */
  @Override
  protected void addGridBasedKeys(Grid grid, boolean enable) { // XXX Currently not called from
    // anywhere
    try {
      if (enable) {
        grid.installMovementKeys(this, keyActionMap);
      } else {
        grid.uninstallMovementKeys(keyActionMap);
      }
    } catch (Exception e) {
      // If there was an exception just ignore those keystrokes...
      MapTool.showError("exception adding grid-based keys; shouldn't get here!", e); // this
      // gives
      // me a
      // hook
      // to set
      // a
      // breakpoint
    }
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    super.detachFrom(renderer);
    MapTool.getFrame().removeControlPanel();
    htmlRenderer.detach(renderer);
  }

  @Override
  public String getInstructions() {
    return "tool.pointer.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.pointer.tooltip";
  }

  public void startTokenDrag(Token keyToken, Set<GUID> tokens) {
    tokenBeingDragged = keyToken;

    Player p = MapTool.getPlayer();
    if (!p.isGM()
        && (MapTool.getServerPolicy().isMovementLocked()
            || MapTool.getFrame().getInitiativePanel().isMovementLocked(keyToken))) {
      // Not allowed
      return;
    }

    renderer.addMoveSelectionSet(
        p.getName(), tokenBeingDragged.getId(), renderer.getOwnedTokens(tokens));
    MapTool.serverCommand()
        .startTokenMove(
            p.getName(),
            renderer.getZone().getId(),
            tokenBeingDragged.getId(),
            renderer.getOwnedTokens(tokens));

    isDraggingToken = true;
  }

  /** Complete the drag of the token, and expose FOW */
  public void stopTokenDrag() {
    renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
    isDraggingToken = false;
    isMovingWithKeys = false;

    dragOffsetX = 0;
    dragOffsetY = 0;

    exposeFoW(null);
  }

  /**
   * Expose the FoW at a ZonePoint, or at the visible area, for the selected token
   *
   * @param p the ZonePoint to expose, or a null if exposing visible area and last path
   */
  public void exposeFoW(ZonePoint p) {
    // if has fog(required)
    // and ((isGM with pref set) OR serverPolicy allows auto reveal by players)

    String name = MapTool.getPlayer().getName();
    boolean isGM = MapTool.getPlayer().isGM();
    boolean ownerReveal; // if true, reveal FoW if current player owns the token.
    boolean hasOwnerReveal; // if true, reveal FoW if token has an owner.
    boolean noOwnerReveal; // if true, reveal FoW if token has no owners.

    if (MapTool.isPersonalServer()) {
      ownerReveal =
          hasOwnerReveal = noOwnerReveal = AppPreferences.getAutoRevealVisionOnGMMovement();
    } else {
      ownerReveal = MapTool.getServerPolicy().isAutoRevealOnMovement();
      hasOwnerReveal = isGM && MapTool.getServerPolicy().isAutoRevealOnMovement();
      noOwnerReveal = isGM && MapTool.getServerPolicy().getGmRevealsVisionForUnownedTokens();
    }
    if (renderer.getZone().hasFog() && (ownerReveal || hasOwnerReveal || noOwnerReveal)) {
      Set<GUID> exposeSet = new HashSet<GUID>();
      Zone zone = renderer.getZone();
      for (GUID tokenGUID : renderer.getOwnedTokens(renderer.getSelectedTokenSet())) {
        Token token = zone.getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        if (ownerReveal && token.isOwner(name)) exposeSet.add(tokenGUID);
        else if (hasOwnerReveal && token.hasOwners()) exposeSet.add(tokenGUID);
        else if (noOwnerReveal && !token.hasOwners()) exposeSet.add(tokenGUID);
      }

      if (p != null) {
        FogUtil.exposeVisibleAreaAtWaypoint(renderer, exposeSet, p);
        return;
      }

      // Lee: fog exposure according to reveal type
      if (!zone.getWaypointExposureToggle()) {
        FogUtil.exposeLastPath(renderer, exposeSet);
      }
      FogUtil.exposeVisibleArea(renderer, exposeSet, false);
    }
  }

  private void showTokenStackPopup(List<Token> tokenList, int x, int y) {
    tokenStackPanel.show(tokenList, x, y);
    isShowingTokenStackPopup = true;
    repaint();
  }

  private class TokenStackPanel {
    private static final int PADDING = 4;

    private List<Token> tokenList;
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
      FontMetrics fm = getFontMetrics(getFont());
      return new Dimension(
          tokenList.size() * (gridSize + PADDING) + PADDING,
          gridSize + PADDING * 2 + fm.getHeight() + 10);
    }

    public void handleMouseReleased(MouseEvent event) {}

    /**
     * Handles right click (popup menu) and double left click (token editor).
     *
     * @param event the mouse event.
     */
    public void handleMousePressed(MouseEvent event) {
      if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)) {
        Token token = getTokenAt(event.getX(), event.getY());
        if (token == null || !AppUtil.playerOwns(token)) {
          return;
        }
        tokenUnderMouse = token;
        MapTool.getFrame().showTokenPropertiesDialog(tokenUnderMouse, renderer);
      }
      if (SwingUtilities.isRightMouseButton(event)) {
        Token token = getTokenAt(event.getX(), event.getY());
        if (token == null || !AppUtil.playerOwns(token)) {
          return;
        }
        tokenUnderMouse = token;
        Set<GUID> selectedSet = new HashSet<GUID>();
        selectedSet.add(token.getId());
        new TokenPopupMenu(selectedSet, event.getX(), event.getY(), renderer, tokenUnderMouse)
            .showPopup(renderer);
      }
    }

    public void handleMouseMotionEvent(MouseEvent event) {
      Token token = getTokenAt(event.getX(), event.getY());
      if (token == null || !AppUtil.playerOwns(token)) {
        return;
      }

      final var selectionModel = renderer.getSelectionModel();
      final var wasNotAlreadySelected = !selectionModel.isSelected(token.getId());
      // Only this token should be selected going forward.
      renderer.getSelectionModel().replaceSelection(Collections.singletonList(token.getId()));

      if (wasNotAlreadySelected) {
        Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
        if (!(tool instanceof PointerTool)) {
          return;
        }
        tokenUnderMouse = token;
        ((PointerTool) tool).startTokenDrag(token, Collections.singleton(token.getId()));
      }
    }

    public void paint(Graphics g) {
      Dimension size = getSize();
      int gridSize = (int) renderer.getScaledGridSize();

      FontMetrics fm = g.getFontMetrics();

      // Background
      ((Graphics2D) g)
          .setPaint(
              new GradientPaint(x, y, Color.white, x + size.width, y + size.height, Color.gray));
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

        GraphicsUtil.drawBoxedString(
            (Graphics2D) g,
            token.getName(),
            bounds.x + bounds.width / 2,
            bounds.y + bounds.height + fm.getAscent());

        tokenLocationList.add(new TokenLocation(bounds, token));
      }
    }

    public Token getTokenAt(int x, int y) {
      for (TokenLocation location : tokenLocationList) {
        if (location.getBounds().contains(x, y)) {
          return location.getToken();
        }
      }
      return null;
    }

    public boolean contains(int x, int y) {
      return new Rectangle(this.x, this.y, getSize().width, getSize().height).contains(x, y);
    }
  }

  private boolean handledByHover(Point p) {
    if (!isShowingHover) return false;

    if (htmlRenderer.contains(p)) {
      htmlRenderer.clickAt(p);
      return true;
    }
    return false;
  }

  // //
  // Mouse
  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);

    if (handledByHover(e.getPoint())) return;

    mouseButtonDown = true;

    if (isShowingHover) {
      isShowingHover = false;
      hoverTokenBounds = null;
      hoverTokenNotes = null;
      markerUnderMouse = renderer.getMarkerAt(e.getX(), e.getY());
      repaint();
    }
    if (isShowingTokenStackPopup) {
      if (tokenStackPanel.contains(e.getX(), e.getY())) {
        tokenStackPanel.handleMousePressed(e);
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
    dragStartX = e.getX(); // These same two lines are in super.mousePressed(). Why do them
    // here?
    dragStartY = e.getY();

    // Properties
    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
      mouseButtonDown = false;
      List<Token> tokenList = renderer.getTokenStackAt(mouseX, mouseY);
      if (tokenList != null) {
        // Stack
        renderer.getSelectionModel().replaceSelection(Collections.emptyList());
        showTokenStackPopup(tokenList, e.getX(), e.getY());
      } else {
        // Single
        Token token = renderer.getTokenAt(e.getX(), e.getY());
        if (token != null) {
          if (!AppUtil.playerOwns(token)) {
            return;
          }
          MapTool.getFrame().showTokenPropertiesDialog(token, renderer);
        }
      }
      return;
    }
    // SELECTION
    Token token = renderer.getTokenAt(e.getX(), e.getY());
    final var selectionModel = renderer.getSelectionModel();
    if (token != null && !isDraggingToken && SwingUtilities.isLeftMouseButton(e)) {
      // Don't select if it's already being moved by someone
      isNewTokenSelected = false;
      if (!renderer.isTokenMoving(token)) {
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
        // ZonePoint dragged to
        ZonePoint pos = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);

        // Offset specific to the token
        Point tokenOffset = token.getDragOffset(getZone());

        // Dragging offset for currently selected token
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
    mouseButtonDown = false;
    // System.out.println("mouseReleased " + e.toString());

    if (isShowingTokenStackPopup) {
      if (tokenStackPanel.contains(e.getX(), e.getY())) {
        tokenStackPanel.handleMouseReleased(e);
        return;
      } else {
        isShowingTokenStackPopup = false;
        repaint();
      }
    }

    // Jamz: We have to capture here as isLeftMouseButton is also true during drag
    // Jamz: Also, changed to right button which is easier to click during drag
    // WAYPOINT
    if (SwingUtilities.isRightMouseButton(e) && isDraggingToken) {
      setWaypoint();
      setDraggingMap(false); // We no longer drag the map. Fixes bug #616
      return;
    }

    if (SwingUtilities.isLeftMouseButton(e)) {
      try {
        // MARKER
        renderer.setCursor(
            Cursor.getPredefinedCursor(
                markerUnderMouse != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        if (tokenUnderMouse == null
            && markerUnderMouse != null
            && !isShowingHover
            && !isDraggingToken) {
          isShowingHover = true;
          hoverTokenBounds = renderer.getMarkerBounds(markerUnderMouse);
          hoverTokenNotes = createHoverNote(markerUnderMouse);
          if (hoverTokenBounds == null) {
            // Uhhhh, where's the token ?
            isShowingHover = false;
          }
          repaint();
        }
        // SELECTION BOUND BOX
        final var selectionModel = renderer.getSelectionModel();
        if (isDrawingSelectionBox) {
          isDrawingSelectionBox = false;

          final var tokens = renderer.getTokenIdsInBounds(selectionBoundBox);
          if (!SwingUtil.isShiftDown(e)) {
            selectionModel.replaceSelection(tokens);
          } else {
            selectionModel.addTokensToSelection(tokens);
          }
          selectionBoundBox = null;
          return;
        }
        // DRAG TOKEN COMPLETE
        if (isDraggingToken) {
          SwingUtil.showPointer(renderer);
          stopTokenDrag();
        } else {
          // IF SELECTING MULTIPLE, SELECT SINGLE TOKEN
          if (!SwingUtil.isShiftDown(e)) {
            Token token = renderer.getTokenAt(e.getX(), e.getY());
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

    // Jamz: This doesn't seem to work for me, looks like Mouse 1 is always returned along with
    // Mouse 3 so it's caught above...
    // And Middle button? That's a pain to click while dragging isn't it? How about Right click
    // during drag?
    // WAYPOINT
    if (SwingUtilities.isMiddleMouseButton(e) && isDraggingToken) {
      setWaypoint();
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
      final var selectedTokens = renderer.getSelectedTokenSet();
      if (tokenUnderMouse != null && !selectedTokens.isEmpty()) {
        if (tokenUnderMouse.getLayer().isStampLayer()) {
          new StampPopupMenu(selectedTokens, e.getX(), e.getY(), renderer, tokenUnderMouse)
              .showPopup(renderer);
        } else if (AppUtil.playerOwns(tokenUnderMouse)) {
          // FIXME Every once in awhile we get a report on the forum of the following exception:
          // java.awt.IllegalComponentStateException: component must be showing on the screen to
          // determine its location
          // It's thrown as a result of the showPopup() call on the next line. For the life of me, I
          // can't figure out why the "renderer" component might not be "showing on the screen"???
          // Maybe it has something to do with a dual-monitor configuration? Or a monitor added
          // after Java was started and then MT dragged to that monitor?
          new TokenPopupMenu(selectedTokens, e.getX(), e.getY(), renderer, tokenUnderMouse)
              .showPopup(renderer);
        }
        return;
      }
    }
    super.mouseReleased(e);
  }

  // //
  // MouseMotion
  @Override
  public void mouseMoved(MouseEvent e) {
    if (renderer == null) {
      return;
    }
    super.mouseMoved(e);

    // mouseX = e.getX(); // done by super.mouseMoved()
    // mouseY = e.getY();
    if (isShowingPointer) {
      ZonePoint zp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
      Pointer pointer =
          MapTool.getFrame().getPointerOverlay().getPointer(MapTool.getPlayer().getName());
      if (pointer != null) {
        pointer.setX(zp.x);
        pointer.setY(zp.y);
        renderer.repaint();
        MapTool.serverCommand().movePointer(MapTool.getPlayer().getName(), zp.x, zp.y);
      }
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

    if (isDraggingToken) {
      // FJE If we're dragging the token, wouldn't mouseDragged() be called instead? Can this
      // code
      // ever be executed?
      if (isMovingWithKeys) {
        return;
      }
      ZonePoint zp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
      ZonePoint last;
      if (tokenUnderMouse == null) last = zp;
      else {
        last = renderer.getLastWaypoint(tokenUnderMouse.getId());
        // XXX This shouldn't be possible, but it happens?!
        if (last == null) last = zp;
      }
      handleDragToken(zp, zp.x - last.x, zp.y - last.y);
      return;
    }
    var oldTokenUnderMouse = tokenUnderMouse;
    tokenUnderMouse = renderer.getTokenAt(mouseX, mouseY);
    keysDown = e.getModifiersEx();
    renderer.setMouseOver(tokenUnderMouse);

    if (tokenUnderMouse == null) {
      statSheet = null;
      if (oldTokenUnderMouse != null) {
        new MapToolEventBus()
            .getMainEventBus()
            .post(
                new TokenHoverExit(
                    oldTokenUnderMouse,
                    getZone(),
                    SwingUtil.isShiftDown(keysDown),
                    SwingUtil.isControlDown(keysDown)));
      }
    } else if (tokenUnderMouse != oldTokenUnderMouse) {
      statSheet = null;
      if (oldTokenUnderMouse != null) {
        new MapToolEventBus()
            .getMainEventBus()
            .post(
                new TokenHoverExit(
                    oldTokenUnderMouse,
                    getZone(),
                    SwingUtil.isShiftDown(keysDown),
                    SwingUtil.isControlDown(keysDown)));
      }
      new MapToolEventBus()
          .getMainEventBus()
          .post(
              new TokenHoverEnter(
                  tokenUnderMouse,
                  getZone(),
                  SwingUtil.isShiftDown(keysDown),
                  SwingUtil.isControlDown(keysDown)));
    }
    Token marker = renderer.getMarkerAt(mouseX, mouseY);
    if (!AppUtil.tokenIsVisible(renderer.getZone(), marker, renderer.getPlayerView())) {
      marker = null;
    }
    if (marker != markerUnderMouse && marker != null) {
      markerUnderMouse = marker;
      renderer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      MapTool.getFrame().setStatusMessage(markerUnderMouse.getName());
    } else if (marker == null && markerUnderMouse != null) {
      markerUnderMouse = null;
      renderer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      MapTool.getFrame().setStatusMessage("");
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();

    if (isShowingTokenStackPopup) {
      isShowingTokenStackPopup = false;
      if (tokenStackPanel.contains(mouseX, mouseY)) {
        tokenStackPanel.handleMouseMotionEvent(e);
        return;
      } else {
        renderer.repaint();
      }
    }
    // XXX Updating the status bar is done in super.mouseDragged() -- maybe just call that here?
    // But
    // it also causes repaint events...
    CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(mouseX, mouseY));
    if (cellUnderMouse != null) {
      MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
    }
    if (SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isRightMouseButton(e)) {
      if (isDrawingSelectionBox) {
        int x1 = dragStartX;
        int y1 = dragStartY;

        int x2 = mouseX;
        int y2 = mouseY;

        selectionBoundBox.x = Math.min(x1, x2);
        selectionBoundBox.y = Math.min(y1, y2);
        selectionBoundBox.width = Math.abs(x1 - x2);
        selectionBoundBox.height = Math.abs(y1 - y2);
        /*
         * NOTE: This is a weird one that has to do with the order of the mouseReleased event. If the selection box started the drag while hovering over a marker, we need to tell it to not
         * show the marker after the drag is complete.
         */
        markerUnderMouse = null;
        renderer.repaint();
        return;
      }
      if (tokenUnderMouse == null
          || !renderer.getSelectedTokenSet().contains(tokenUnderMouse.getId())) {
        return;
      }
      if (isDraggingToken) {
        if (isMovingWithKeys) {
          return;
        }
        ZonePoint last = renderer.getLastWaypoint(tokenUnderMouse.getId());
        if (last == null) {
          // This makes no sense to me. Why create a fake last point that is
          // half the token width away from the current point? (Phil)
          // last =  new ZonePoint(
          //        tokenUnderMouse.getX() + r.width / 2,
          //        tokenUnderMouse.getY() + r.height / 2);

          // Just make a last ZP that is the same.
          last = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
        }
        ZonePoint zp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
        // These lines were causing tokens to end up in the wrong grid cell in
        // relation to the the mouse location.
        // if (tokenUnderMouse.isSnapToGrid() && grid.getCapabilities().isSnapToGridSupported()) {
        //          zp.translate(-r.width / 2, -r.height / 2);
        //          last.translate(-r.width / 2, -r.height / 2);
        // }
        //        zp.translate(-dragOffsetX, -dragOffsetY);

        // Now the dx/dy are calculated on Zone Points that haven't been
        // translated for drag offset or for snapping. That is being done in
        // handleDragToken().
        int dx = zp.x - last.x;
        int dy = zp.y - last.y;
        handleDragToken(zp, dx, dy);
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

      // Make sure we're allowed
      if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
        return;
      }
      // Might be dragging a token
      String playerId = MapTool.getPlayer().getName();
      Set<GUID> selectedTokenSet = renderer.getOwnedTokens(renderer.getSelectedTokenSet());
      if (!selectedTokenSet.isEmpty()) {
        // Make sure we can do this
        // Possibly let unowned tokens be moved?
        if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
          for (GUID tokenGUID : selectedTokenSet) {
            Token token = renderer.getZone().getToken(tokenGUID);
            if (!token.isOwner(playerId)) {
              return;
            }
          }
        }
        startTokenDrag(tokenUnderMouse, selectedTokenSet);
        isDraggingToken = true;
        if (AppPreferences.getHideMousePointerWhileDragging()) SwingUtil.hidePointer(renderer);
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
   * @param zonePoint The new ZonePoint for the token.
   * @param dx The amount being moved in the X direction
   * @param dy The amount being moved in the Y direction
   * @return true if the move was successful
   */
  public boolean handleDragToken(ZonePoint zonePoint, int dx, int dy) {
    Grid grid = renderer.getZone().getGrid();
    // Always correct for offset. Fix #1589
    zonePoint.translate(-dragOffsetX, -dragOffsetY);
    // For snapped dragging
    if (tokenBeingDragged.isSnapToGrid()
        && grid.getCapabilities().isSnapToGridSupported()
        && AppPreferences.getTokensSnapWhileDragging()) {
      // Convert the zone point to a cell point and back to force the snap to grid on drag
      zonePoint = grid.convert(grid.convert(zonePoint));
    }
    CellPoint cellUnderMouse = grid.convert(zonePoint);
    MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
    // Don't bother if there isn't any movement
    if (!renderer.hasMoveSelectionSetMoved(tokenBeingDragged.getId(), zonePoint)) {
      return false;
    }
    // Make sure it's a valid move
    boolean isValid;
    if (grid.getSize() >= 9)
      isValid = validateMove(tokenBeingDragged, renderer.getSelectedTokenSet(), zonePoint, dx, dy);
    else
      isValid = validateMove_legacy(tokenBeingDragged, renderer.getSelectedTokenSet(), zonePoint);

    if (!isValid) {
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

  private boolean validateMove(
      Token leadToken, Set<GUID> tokenSet, ZonePoint point, int dirx, int diry) {
    if (MapTool.getPlayer().isGM()) {
      return true;
    }
    boolean isBlocked = false;
    Zone zone = renderer.getZone();
    if (zone.hasFog()) {
      // Check that the new position for each token is within the exposed area
      Area zoneFog = zone.getExposedArea();
      if (zoneFog == null) zoneFog = new Area();
      boolean useTokenExposedArea =
          MapTool.getServerPolicy().isUseIndividualFOW() && zone.getVisionType() != VisionType.OFF;
      int deltaX = point.x - leadToken.getX();
      int deltaY = point.y - leadToken.getY();
      Grid grid = zone.getGrid();
      // Loop through all tokens. As soon as one of them is blocked, stop processing and
      // return
      // false.
      // Jamz: Option this for lead token only? It's annoying dragging a group when one token
      // has
      // limited vision...
      // Or if ANY token in group can move, finish move?
      for (Iterator<GUID> iter = tokenSet.iterator(); !isBlocked && iter.hasNext(); ) {
        Area tokenFog = new Area(zoneFog);
        GUID tokenGUID = iter.next();
        Token token = zone.getToken(tokenGUID);
        if (token == null) {
          continue;
        }

        // Rolled back change from commit 3d5f619 because of reported bug by dorpond
        // https://github.com/JamzTheMan/maptool/commit/3d5f619dff6e61c605ee532ac3c86a3860e91864
        if (useTokenExposedArea) {
          ExposedAreaMetaData meta = zone.getExposedAreaMetaData(token.getExposedAreaGUID());
          tokenFog.add(meta.getExposedAreaHistory());

          // Jamz: Allow a token without site to move within the current PlayerView
          if (!token.getHasSight()) {
            tokenFog.add(renderer.getZoneView().getVisibleArea(new PlayerView(Role.PLAYER)));
          }
        }

        Rectangle tokenSize = token.getBounds(zone);
        Rectangle destination =
            new Rectangle(
                tokenSize.x + deltaX, tokenSize.y + deltaY, tokenSize.width, tokenSize.height);
        isBlocked = !grid.validateMove(token, destination, dirx, diry, tokenFog);
      }
    }
    return !isBlocked;
  }

  private boolean validateMove_legacy(Token leadToken, Set<GUID> tokenSet, ZonePoint point) {
    Zone zone = renderer.getZone();
    if (MapTool.getPlayer().isGM()) {
      return true;
    }
    boolean isVisible = true;
    if (zone.hasFog()) {
      // Check that the new position for each token is within the exposed area
      Area fow = zone.getExposedArea();
      if (fow == null) {
        return true;
      }
      isVisible = false;
      int fudgeSize = Math.max(Math.min((zone.getGrid().getSize() - 2) / 3 - 1, 8), 0);
      int deltaX = point.x - leadToken.getX();
      int deltaY = point.y - leadToken.getY();
      Rectangle bounds = new Rectangle();
      for (GUID tokenGUID : tokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        int x = token.getX() + deltaX;
        int y = token.getY() + deltaY;

        Rectangle tokenSize = token.getBounds(zone);
        /*
         * Perhaps create a counter and count the number of times that the contains() check returns true? There are currently 9 rectangular areas checked by this code (note the "/3" in the two
         * 'interval' variables) so checking for 5 or more would mean more than 55%+ of the destination was visible...
         */
        int intervalX = tokenSize.width - fudgeSize * 2;
        int intervalY = tokenSize.height - fudgeSize * 2;
        int counter = 0;
        for (int dy = 0; dy < 3; dy++) {
          for (int dx = 0; dx < 3; dx++) {
            int by = y + fudgeSize + (intervalY * dy / 3);
            int bx = x + fudgeSize + (intervalX * dx / 3);
            bounds.x = bx;
            bounds.y = by;
            bounds.width = intervalY * (dy + 1) / 3 - intervalY * dy / 3; // No, this
            // isn't the
            // same as
            // intervalY*1/3
            // because of
            // integer
            // arithmetic
            bounds.height = intervalX * (dx + 1) / 3 - intervalX * dx / 3;

            if (!MapTool.getServerPolicy().isUseIndividualFOW()
                || zone.getVisionType() == VisionType.OFF) {
              if (fow.contains(bounds)) {
                counter++;
              }
            } else {
              ExposedAreaMetaData meta = zone.getExposedAreaMetaData(token.getExposedAreaGUID());
              if (meta.getExposedAreaHistory().contains(bounds)) {
                counter++;
              }
            }
          }
        }
        isVisible = (counter >= 6);
      }
    }
    return isVisible;
  }

  /**
   * @note These keystrokes are currently hard-coded and should be exported to a property file in a
   *     perfect universe. :)
   *     <table>
   * <caption>Keystrokes</caption>
   * <tr>
   * <td>Meta R
   * <td>Select the FacingTool (to allow rotating with the left/right arrows)
   * <tr>
   * <td>DELETE
   * <td>Allow deletion of owned tokens
   * <tr>
   * <td>Space
   * <td>Show arrow pointer on map
   * <tr>
   * <td>Ctrl Space
   * <td>Show speech bubble on map
   * <tr>
   * <td>Shift Space
   * <td>Show thought bubble on map
   * <tr>
   * <td>D
   * <td>Stop dragging token
   * <tr>
   * <td>T
   * <td>Cycle forward through tokens
   * <tr>
   * <td>Shift T
   * <td>Cycle backward through tokens
   * <tr>
   * <td>Meta I
   * <td>Expose fog from visible area
   * <tr>
   * <td>Meta P
   * <td>Expose fog from last path
   * <tr>
   * <td>Meta Shift O
   * <td>Expose only PC area (reinsert other fog)
   * <tr>
   * <td>NumPad digits
   * <td>Move token (specifics based on the grid type are not implemented yet):<br>
   * <tr>
   * <td>7 (up/left)
   * <td>8 (up)
   * <td>9 (up/right)
   * <tr>
   * <td>4 (left)
   * <td>5 (stop)
   * <td>6(right)
   * <tr>
   * <td>1 (down/left)
   * <td>2 (down)
   * <td>3 (down/right)
   * <tr>
   * <td>Down
   * <td>Move token down
   * <tr>
   * <td>Up
   * <td>Move token up
   * <tr>
   * <td>Right
   * <td>Move token right
   * <tr>
   * <td>Shift Right
   * <td>Rotate token right by facing amount (depends on grid)
   * <tr>
   * <td>Ctrl Shift Right
   * <td>Rotate token right by 5 degree increments
   * <tr>
   * <td>Left
   * <td>Move token left
   * <tr>
   * <td>Shift Left
   * <td>Rotate token left by facing amount (depends on grid)
   * <tr>
   * <td>Ctrl Shift Left
   * <td>Rotate token left by 5 degree increments
   * </table>
   */
  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), AppActions.NEXT_TOKEN);
    actionMap.put(AppActions.CUT_TOKENS.getKeyStroke(), AppActions.CUT_TOKENS);
    actionMap.put(AppActions.COPY_TOKENS.getKeyStroke(), AppActions.COPY_TOKENS);
    actionMap.put(AppActions.PASTE_TOKENS.getKeyStroke(), AppActions.PASTE_TOKENS);
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_R, AppActions.menuShortcut),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            // TODO: Combine all this crap with the Stamp tool
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
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), new StopPointerActionListener());
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK, true),
        new StopPointerActionListener());
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK, true),
        new StopPointerActionListener());
    actionMap.put(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK, true),
        new StopPointerActionListener(true));

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false),
        new PointerActionListener(Pointer.Type.ARROW));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK, false),
        new PointerActionListener(Pointer.Type.SPEECH_BUBBLE));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK, false),
        new PointerActionListener(Pointer.Type.THOUGHT_BUBBLE));
    actionMap.put(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK, false),
        new PointerActionListener(Pointer.Type.LOOK_HERE));

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_D, 0),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            if (!isDraggingToken) {
              return;
            }
            // Stop
            stopTokenDrag();
          }
        });
    // Other NumPad keys are handled by individual grid types
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            if (!isDraggingToken) {
              return;
            }
            // Stop
            stopTokenDrag();
          }
        });
    int size = 1;
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), new MovementKey(this, -size, -size));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new MovementKey(this, 0, -size));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), new MovementKey(this, size, -size));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), new MovementKey(this, -size, 0));
    // actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), new MovementKey(this, 0,
    // 0));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), new MovementKey(this, size, 0));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), new MovementKey(this, -size, size));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), new MovementKey(this, 0, size));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), new MovementKey(this, size, size));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new MovementKey(this, -size, 0));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new MovementKey(this, size, 0));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new MovementKey(this, 0, -size));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new MovementKey(this, 0, size));

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            handleKeyRotate(-1, false); // clockwise
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            handleKeyRotate(-1, true); // clockwise
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            handleKeyRotate(1, false); // counter-clockwise
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            handleKeyRotate(1, true); // counter-clockwise
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_T, 0),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            renderer.cycleSelectedToken(1);
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            renderer.cycleSelectedToken(-1);
          }
        });

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_I, AppActions.menuShortcut),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            if (MapTool.getPlayer().isGM()
                || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
              FogUtil.exposeVisibleArea(
                  renderer, renderer.getOwnedTokens(renderer.getSelectedTokenSet()));
            }
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_O, AppActions.menuShortcut | InputEvent.SHIFT_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            // Only let the GM's do this
            if (MapTool.getPlayer().isGM()) {
              FogUtil.exposePCArea(renderer);
            }
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, AppActions.menuShortcut | InputEvent.SHIFT_DOWN_MASK),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            // Only let the GM's do this
            if (MapTool.getPlayer().isGM()) {
              FogUtil.exposeAllOwnedArea(renderer);
            }
          }
        });
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_P, AppActions.menuShortcut),
        new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(ActionEvent e) {
            if (MapTool.getPlayer().isGM()
                || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
              FogUtil.exposeLastPath(
                  renderer, renderer.getOwnedTokens(renderer.getSelectedTokenSet()));
            }
          }
        });
  }

  /**
   * Handle token rotations when using the arrow keys.
   *
   * @param direction -1 is cw & 1 is ccw
   */
  private void handleKeyRotate(int direction, boolean freeRotate) {
    Set<GUID> tokenGUIDSet = renderer.getSelectedTokenSet();
    if (tokenGUIDSet.isEmpty()) {
      return;
    }
    for (GUID tokenGUID : tokenGUIDSet) {
      Token token = renderer.getZone().getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      if (!AppUtil.playerOwns(token)) {
        continue;
      }
      Integer facing = token.getFacing();
      // TODO: this should really be a per grid setting
      if (facing == null) {
        facing = -90; // natural alignment
      }
      if (freeRotate) {
        facing += direction * 5;
      } else {
        int[] facingArray = renderer.getZone().getGrid().getFacingAngles();
        int facingIndex = TokenUtil.getIndexNearestTo(facingArray, facing);

        facingIndex += direction;

        if (facingIndex < 0) {
          facingIndex = facingArray.length - 1;
        }
        if (facingIndex == facingArray.length) {
          facingIndex = 0;
        }
        facing = facingArray[facingIndex];
      }
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setFacing, facing);
    }
    renderer.repaint();
  }

  /**
   * Handle the movement of tokens by keypresses.
   *
   * @param dx The X movement in Cell units
   * @param dy The Y movement in Cell units
   */
  public void handleKeyMove(double dx, double dy) {
    Token keyToken = null;
    if (!isDraggingToken) {
      // Start
      Set<GUID> selectedTokenSet = renderer.getOwnedTokens(renderer.getSelectedTokenSet());

      for (GUID tokenId : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenId);
        if (token == null) {
          return;
        }
        // Need a key token to orient the move from, just arbitrarily pick the first one
        if (keyToken == null) {
          keyToken = token;
        }

        // Only one person at a time
        if (renderer.isTokenMoving(token)) {
          return;
        }
      }
      if (keyToken == null) {
        return;
      }
      // Note these are zone space coordinates
      dragStartX = keyToken.getX();
      dragStartY = keyToken.getY();
      startTokenDrag(keyToken, selectedTokenSet);
    }
    if (!isMovingWithKeys) {
      dragOffsetX = 0;
      dragOffsetY = 0;
    }
    // The zone point the token will be moved to after adjusting for dx/dy
    ZonePoint zp = new ZonePoint(dragStartX, dragStartY);
    Grid grid = renderer.getZone().getGrid();
    if (tokenBeingDragged.isSnapToGrid() && grid.getCapabilities().isSnapToGridSupported()) {
      CellPoint cp = grid.convert(zp);
      cp.x += dx;
      cp.y += dy;
      zp = grid.convert(cp);
      dx = zp.x - tokenBeingDragged.getX();
      dy = zp.y - tokenBeingDragged.getY();
    } else {
      // Scalar for dx/dy in zone space. Defaulting to essentially 1 pixel.
      int moveFactor = 1;
      if (tokenBeingDragged.isSnapToGrid()) {
        // Move in grid size increments. Allows tokens set snap-to-grid on gridless maps
        // to move in whole cell size increments.
        moveFactor = grid.getSize();
      }
      int x = dragStartX + (int) (dx * moveFactor);
      int y = dragStartY + (int) (dy * moveFactor);
      zp = new ZonePoint(x, y);
    }
    isMovingWithKeys = true;
    handleDragToken(zp, (int) dx, (int) dy);
  }

  private void setWaypoint() {
    ZonePoint p = new ZonePoint(dragStartX, dragStartY);
    exposeFoW(p);

    renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), p);
    MapTool.serverCommand()
        .toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), p);
  }

  // //
  // POINTER KEY ACTION
  private class PointerActionListener extends AbstractAction {
    private static final long serialVersionUID = 8348513388262364724L;

    Pointer.Type type;

    public PointerActionListener(Pointer.Type type) {
      this.type = type;
    }

    public void actionPerformed(ActionEvent e) {
      if (isSpaceDown) {
        return;
      }
      if (isDraggingToken) {
        setWaypoint();
      } else {
        // Pointer
        isShowingPointer = true;

        ZonePoint zp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
        Pointer pointer = new Pointer(renderer.getZone(), zp.x, zp.y, 0, type);
        // Jamz test move clients to view when using point (for GM only)...
        // TODO: Snap player view back when done?
        if (MapTool.getPlayer().isGM() && type.equals(Pointer.Type.LOOK_HERE)) {
          MapTool.serverCommand()
              .enforceZoneView(
                  renderer.getZone().getId(),
                  zp.x,
                  zp.y,
                  renderer.getScale(),
                  renderer.getWidth(),
                  renderer.getHeight());
        }

        currentPointerName = getPointerName(type);

        MapTool.serverCommand().showPointer(currentPointerName, pointer);
      }
      isSpaceDown = true;
    }

    /**
     * Returns the name to be displayed in the pointer callout.
     *
     * <p>If the pointer type is not speech or thought bubble then it will be the player name. For
     * speech and thought bubble the following logic applies.
     *
     * <ul>
     *   <li>If there is an impersonated token with a speech bubble name
     *       <ul>
     *         <li>If there is no token under the mouse; Result = Impersonated Token Speech Name
     *         <li>If one of the tokens under mouse is the Impersonated token; Result = Impersonated
     *             Token Speech Name
     *         <li>If there is single token under the mouse with speech name; Result = Token under
     *             mouse Speech Name
     *         <li>If there is a token stack under the mouse and some have speech name; Result = one
     *             of the tokens in the stack (will be top one if it has speech name)
     *         <li>Otherwise player name
     *       </ul>
     *   <li>If there is no impersonated token, and there is a token under the mouse
     *       <ul>
     *         <li>If there is a single token under the mouse with speech name; Result = token under
     *             mouse speech name
     *         <li>If there is a token stack under the mouse and one has speech name; Result = one
     *             of the tokens in the stack (will be top one if it has speech name)
     *         <li>otherwise player name
     *       </ul>
     *   <li>Otherwise Player name
     * </ul>
     *
     * @param type the type of pointer
     * @return the name to be displayed.
     */
    private String getPointerName(Type type) {
      String playerName = MapTool.getPlayer().getName();

      if (type != Type.SPEECH_BUBBLE && type != Type.THOUGHT_BUBBLE) {
        return playerName;
      }
      boolean isGM = MapTool.getPlayer().isGM();
      List<Token> tokenStackAt = renderer.getTokenStackAt(mouseX, mouseY);
      if (tokenStackAt == null) {
        if (tokenUnderMouse != null) {
          tokenStackAt = List.of(tokenUnderMouse);
        } else {
          tokenStackAt = List.of();
        }
      }
      Set<Token> tokens =
          tokenStackAt.stream()
              .filter(t -> isGM || t.isOwner(playerName))
              .filter(t -> t.getSpeechName() != null && t.getSpeechName().length() > 0)
              .collect(Collectors.toSet());

      Token pointerToken = null;
      Token impersonatedToken = null;
      GUID guid = MapTool.getFrame().getImpersonatePanel().getTokenId();
      if (guid != null) {
        // Searches all maps to find impersonated token
        impersonatedToken = FindTokenFunctions.findToken(guid.toString());
      }
      if (impersonatedToken != null) {
        if (impersonatedToken.getSpeechName() == null
            || impersonatedToken.getSpeechName().length() == 0) {
          impersonatedToken = null;
        }
      }

      Token tUnder = null;
      if (tokenUnderMouse != null && (isGM || tokenUnderMouse.isOwner(playerName))) {
        tUnder = tokenUnderMouse;
      }

      if (impersonatedToken != null && tUnder == null) {
        pointerToken = impersonatedToken;
      } else if (impersonatedToken != null) {
        if (tokens.contains(impersonatedToken)) {
          pointerToken = impersonatedToken;
        } else if (tUnder.getSpeechName() != null && tUnder.getSpeechName().length() > 0) {
          pointerToken = tUnder;
        } else if (tokens.size() > 0) {
          pointerToken = tokens.iterator().next();
        } else {
          pointerToken = null;
        }
      } else if (tUnder != null) {
        if (tUnder.getSpeechName() != null && tUnder.getSpeechName().length() > 0) {
          pointerToken = tokenUnderMouse;
        } else if (tokens.size() > 0) {
          pointerToken = tokens.iterator().next();
        } else {
          pointerToken = null;
        }
      } else {
        pointerToken = null;
      }

      if (pointerToken != null) {
        return pointerToken.getSpeechName();
      } else {
        return playerName;
      }
    }
  }

  // //
  // STOP POINTER ACTION
  private class StopPointerActionListener extends AbstractAction {
    private static final long serialVersionUID = -8508019800264211345L;
    private boolean restoreZoneView = false;

    public StopPointerActionListener(boolean restore) {
      restoreZoneView = restore;
    }

    public StopPointerActionListener() {
      restoreZoneView = false;
    }

    public void actionPerformed(ActionEvent e) {
      if (isShowingPointer) {
        isShowingPointer = false;
        MapTool.serverCommand().hidePointer(currentPointerName);

        if (MapTool.getPlayer().isGM() & restoreZoneView) {
          MapTool.serverCommand().restoreZoneView(renderer.getZone().getId());
        }
      }
      isSpaceDown = false;
    }
  }

  // class WrappedText
  // {
  // int lineCount;
  // String[] lines;
  // stringWidth[] widths;
  // }

  // private WrappedText wrapText(string text)
  // {
  // StringBuilder currentLine;
  // WrappedText wrappedText = new WrappedText();
  // for(int I = 0;I<text.length();I++){
  // if(text.charAt(I) == '\n'){
  // wrappedText.lines
  // }
  //
  // currentLine.append(text.charAt(I));
  //
  // }
  // String[] firstPass = text.split('\n');
  // }

  /**
   * Draws the PointerTool overlay. Includes selection box, token stack popup, statsheet, and
   * notes/gm notes.
   *
   * @param g – the Graphics object
   */
  public void paintOverlay(Graphics2D g) {
    if (renderer == null) {
      return;
    }
    Dimension viewSize = renderer.getSize();
    FontRenderContext fontRenderContext = g.getFontRenderContext();

    Composite composite = g.getComposite();
    if (selectionBoundBox != null) {

      Stroke stroke = g.getStroke();
      g.setStroke(new BasicStroke(2));

      if (AppPreferences.getFillSelectionBox()) {
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
    if (isShowingTokenStackPopup) {
      tokenStackPanel.paint(g);
    }
    // Statsheet
    if (tokenUnderMouse != null
        && !isDraggingToken
        && AppUtil.tokenIsVisible(
            renderer.getZone(), tokenUnderMouse, new PlayerView(MapTool.getPlayer().getRole()))) {
      if (AppPreferences.getPortraitSize() > 0
          && (SwingUtil.isShiftDown(keysDown) == AppPreferences.getShowStatSheetModifier())
          && new StatSheetManager().isLegacyStatSheet(tokenUnderMouse.getStatSheet())
          && (tokenOnStatSheet == null
              || !tokenOnStatSheet.equals(tokenUnderMouse)
              || statSheet == null)) {
        tokenOnStatSheet = tokenUnderMouse;

        BufferedImage image = null;
        Dimension imgSize = new Dimension(0, 0);
        if (AppPreferences.getShowPortrait()) {
          // Portrait
          MD5Key portraitId =
              tokenUnderMouse.getPortraitImage() != null
                  ? tokenUnderMouse.getPortraitImage()
                  : tokenUnderMouse.getImageAssetId();
          image =
              ImageManager.getImage(
                  portraitId,
                  (img, infoflags, x, y, width, height) -> {
                    // The image was loading, so now rebuild the portrait panel with the
                    // real
                    // image
                    statSheet = null;
                    renderer.repaint();
                    return true;
                  });

          imgSize = new Dimension(image.getWidth(), image.getHeight());

          // Size
          SwingUtil.constrainTo(imgSize, AppPreferences.getPortraitSize());
        }

        Dimension statSize = null;
        int rm = AppStyle.miniMapBorder.getRightMargin();
        int lm = AppStyle.miniMapBorder.getLeftMargin();
        int tm = AppStyle.miniMapBorder.getTopMargin();
        int bm = AppStyle.miniMapBorder.getBottomMargin();

        // Stats
        int maxStatsWidth =
            viewSize.width
                - lm
                - rm * 2
                - imgSize.width
                - PADDING * 3
                - STATSHEET_EXTERIOR_PADDING * 2;
        Map<String, String> propertyMap = new LinkedHashMap<String, String>();
        Map<String, Integer> propertyLineCount = new LinkedHashMap<String, Integer>();
        LinkedList<TextLayout> lineLayouts = new LinkedList<TextLayout>();
        if (AppPreferences.getShowStatSheet()
            && new StatSheetManager().isLegacyStatSheet(tokenUnderMouse.getStatSheet())) {
          CodeTimer timer = new CodeTimer("statSheet");
          timer.setEnabled(AppState.isCollectProfilingData());
          timer.setThreshold(5);
          timer.start("allProps");
          for (TokenProperty property :
              MapTool.getCampaign().getTokenPropertyList(tokenUnderMouse.getPropertyType())) {
            if (property.isShowOnStatSheet()) {
              if (property.isGMOnly() && !MapTool.getPlayer().isGM()) {
                continue;
              }
              if (property.isOwnerOnly() && !AppUtil.playerOwns(tokenUnderMouse)) {
                continue;
              }
              timer.start(property.getName());
              MapToolVariableResolver resolver = new MapToolVariableResolver(tokenUnderMouse);
              resolver.initialize();
              resolver.setAutoPrompt(false);
              Object propertyValue =
                  tokenUnderMouse.getEvaluatedProperty(resolver, property.getName());
              resolver.flush();
              if (propertyValue != null && propertyValue.toString().length() > 0) {
                String propName = property.getShortName();
                if (StringUtils.isEmpty(propName)) propName = property.getName();
                propertyMap.put(propName, propertyValue.toString());
              }
              timer.stop(property.getName());
            }
          }
          timer.stop("allProps");
          if (timer.isEnabled()) {
            String results = timer.toString();
            MapTool.getProfilingNoteFrame().addText(results);
          }
        }
        if (tokenUnderMouse.getPortraitImage() != null || !propertyMap.isEmpty()) {
          Font font = AppStyle.labelFont;
          FontMetrics valueFM = g.getFontMetrics(font);
          FontMetrics keyFM = g.getFontMetrics(boldFont);
          int rowHeight = Math.max(valueFM.getHeight(), keyFM.getHeight());
          int keyWidth = -1;
          float valueWidth = -1;
          int layoutWidth = 1;
          if (!propertyMap.isEmpty()) {
            // Figure out size requirements
            // int height = propertyMap.size() * (rowHeight + PADDING);
            int height = 0;
            // Iterate over keys to reserve room for key column
            for (Entry<String, String> entry : propertyMap.entrySet()) {
              int tempKeyWidth = SwingUtilities.computeStringWidth(keyFM, entry.getKey());
              if (keyWidth < 0 || tempKeyWidth > keyWidth) {
                keyWidth = tempKeyWidth;
              }
            }
            layoutWidth = Math.max(1, maxStatsWidth - keyWidth);
            // Iterate over values, break them into lines as necessary. Figure out
            // longest value
            // length.
            for (Entry<String, String> entry : propertyMap.entrySet()) {
              int lineCount = 0;
              for (String line : entry.getValue().split("\n")) {
                if (line.length() > 0) {
                  // For each value, make the iterator need and stash data about
                  // it
                  AttributedString text = new AttributedString(line);
                  text.addAttribute(TextAttribute.FONT, font);
                  AttributedCharacterIterator paragraph = text.getIterator();
                  int paragraphStart = paragraph.getBeginIndex();
                  int paragraphEnd = paragraph.getEndIndex();
                  // Make and initialize LineBreakMeasurer
                  LineBreakMeasurer lineMeasurer =
                      new LineBreakMeasurer(
                          paragraph, BreakIterator.getLineInstance(), fontRenderContext);
                  lineMeasurer.setPosition(paragraphStart);
                  // Get each line from the measurer and find the widest one;
                  while (lineMeasurer.getPosition() < paragraphEnd) {
                    TextLayout layout = lineMeasurer.nextLayout(layoutWidth);
                    lineLayouts.add(layout);
                    height += rowHeight;
                    float tmpValueWidth = layout.getPixelBounds(null, 0, 0).width;
                    lineCount++;
                    if (valueWidth < 0 || tmpValueWidth > valueWidth) {
                      valueWidth = tmpValueWidth;
                    }
                  }
                } else {
                  height += rowHeight;
                  lineCount++;
                }
              }
              propertyLineCount.put(entry.getKey(), lineCount);
              height += PADDING;
            }
            statSize = new Dimension((int) (keyWidth + valueWidth + PADDING * 3), height);
          }
          // Create the space for the image
          int width = imgSize.width + (statSize != null ? statSize.width + rm : 0) + lm + rm;
          int height =
              Math.max(imgSize.height, (statSize != null ? statSize.height + bm : 0))
                  + tm
                  + bm
                  + PADDING * 2;
          statSheet = new BufferedImage(width, height, BufferedImage.BITMASK);
          Graphics2D statsG = statSheet.createGraphics();
          statsG.setClip(new Rectangle(0, 0, width, height));
          statsG.setFont(font);
          SwingUtil.useAntiAliasing(statsG);

          // Draw the stats first, right aligned
          if (statSize != null) {
            Rectangle bounds =
                new Rectangle(
                    width - statSize.width - rm,
                    statSize.height == height ? 0 : height - statSize.height - bm,
                    statSize.width,
                    statSize.height);
            statsG.setPaint(
                new TexturePaint(
                    panelTexture,
                    new Rectangle(0, 0, panelTexture.getWidth(), panelTexture.getHeight())));
            statsG.fill(bounds);
            AppStyle.miniMapBorder.paintAround(statsG, bounds);
            AppStyle.shadowBorder.paintWithin(statsG, bounds);

            // Stats
            int y = bounds.y + rowHeight;
            for (Entry<String, String> entry : propertyMap.entrySet()) {
              // Box
              statsG.setColor(new Color(249, 241, 230, 140));
              statsG.fillRect(
                  bounds.x,
                  y - keyFM.getAscent(),
                  bounds.width - PADDING / 2,
                  rowHeight * propertyLineCount.get(entry.getKey()));
              statsG.setColor(new Color(175, 163, 149));
              statsG.drawRect(
                  bounds.x,
                  y - keyFM.getAscent(),
                  bounds.width - PADDING / 2,
                  rowHeight * propertyLineCount.get(entry.getKey()));

              // Draw Key
              statsG.setColor(Color.black);
              statsG.setFont(boldFont);
              statsG.drawString(entry.getKey(), bounds.x + PADDING * 2, y);

              // Draw Value
              for (String line : entry.getValue().split("\n")) {
                if (line.length() > 0) {
                  // For each value, make the iterator need and stash data about
                  // it
                  AttributedString text = new AttributedString(line);
                  text.addAttribute(TextAttribute.FONT, font);
                  AttributedCharacterIterator paragraph = text.getIterator();
                  int paragraphStart = paragraph.getBeginIndex();
                  int paragraphEnd = paragraph.getEndIndex();
                  // Make and initialize LineBreakMeasurer
                  LineBreakMeasurer lineMeasurer =
                      new LineBreakMeasurer(
                          paragraph, BreakIterator.getLineInstance(), fontRenderContext);
                  lineMeasurer.setPosition(paragraphStart);
                  // Get each line from the measurer and find the widest one;
                  while (lineMeasurer.getPosition() < paragraphEnd) {
                    TextLayout layout = lineMeasurer.nextLayout(layoutWidth);
                    layout.draw(
                        statsG,
                        bounds.x + bounds.width - PADDING - layout.getPixelBounds(null, 0, 0).width,
                        y);
                    y += rowHeight;
                  }
                } else {
                  y += rowHeight;
                }
              }

              // statsG.setFont(font);
              // int strw = SwingUtilities.computeStringWidth(valueFM,
              // entry.getValue());
              // statsG.drawString(entry.getValue(), bounds.x + bounds.width - strw
              // -PADDING, y);

              y += PADDING;
            }
          }

          // Draw the portrait
          if (AppPreferences.getShowPortrait()) {
            Rectangle bounds =
                new Rectangle(lm, height - imgSize.height - bm, imgSize.width, imgSize.height);

            statsG.setPaint(
                new TexturePaint(
                    panelTexture,
                    new Rectangle(0, 0, panelTexture.getWidth(), panelTexture.getHeight())));
            statsG.fill(bounds);
            AppPreferences.getRenderQuality().setShrinkRenderingHints(g);
            statsG.drawImage(image, bounds.x, bounds.y, imgSize.width, imgSize.height, this);
            AppStyle.miniMapBorder.paintAround(statsG, bounds);
            AppStyle.shadowBorder.paintWithin(statsG, bounds);

            // Label
            GraphicsUtil.drawBoxedString(
                statsG, tokenUnderMouse.getName(), bounds.width / 2 + lm, height - 15);
          } else if (AppPreferences.getShowStatSheet() && statSize != null) {
            // Label
            Rectangle bounds =
                new Rectangle(
                    lm,
                    statSize.height,
                    statSize.width + keyFM.getAscent() / 2 + PADDING / 2,
                    statSize.height);
            GraphicsUtil.drawBoxedString(
                statsG,
                tokenUnderMouse.getName(),
                bounds.width / 2 + lm,
                height - statSize.height - PADDING * 3);
          }

          statsG.dispose();
        }
      }
    }

    // Jamz: Statsheet was still showing on drag, added other tests to hide statsheet as well
    if (statSheet != null && !isDraggingToken && !mouseButtonDown) {
      g.drawImage(
          statSheet,
          STATSHEET_EXTERIOR_PADDING,
          viewSize.height - statSheet.getHeight() - STATSHEET_EXTERIOR_PADDING,
          this);
    }

    // Hovers
    if (isShowingHover) {
      // Anchor next to the token
      Dimension size =
          htmlRenderer.setText(
              hoverTokenNotes,
              (int) (renderer.getWidth() * .75),
              (int) (renderer.getHeight() * .75));
      Point location =
          new Point(
              hoverTokenBounds.getBounds().x
                  + hoverTokenBounds.getBounds().width / 2
                  - size.width / 2,
              hoverTokenBounds.getBounds().y);

      // Anchor in the bottom left corner
      location.x = 4 + PADDING;
      location.y = viewSize.height - size.height - 4 - PADDING;

      // Keep it on screen
      if (location.x + size.width > viewSize.width) {
        location.x = viewSize.width - size.width;
      }
      if (location.x < 4) {
        location.x = 4;
      }
      if (location.y + size.height > viewSize.height - 4) {
        location.y = viewSize.height - size.height - 4;
      }
      if (location.y < 4) {
        location.y = 4;
      }

      // Background
      // g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .5f));
      // g.setColor(Color.black);
      // g.fillRect(location.x, location.y, size.width, size.height);
      // g.setComposite(composite);
      g.setPaint(
          new TexturePaint(
              panelTexture,
              new Rectangle(0, 0, panelTexture.getWidth(), panelTexture.getHeight())));
      g.fillRect(location.x, location.y, size.width, size.height);

      // Content
      htmlRenderer.render(g, location.x, location.y);
      // Bounds (for handling clicks)
      htmlRenderer.setBounds(location.x, location.y, size.width, size.height);

      // Border
      AppStyle.miniMapBorder.paintAround(g, location.x, location.y, size.width, size.height);
      AppStyle.shadowBorder.paintWithin(g, location.x, location.y, size.width, size.height);
      // AppStyle.border.paintAround(g, location.x, location.y,
      // size.width, size.height);
    }
  }

  private String createHoverNote(Token marker) {
    var notes = StringUtil.htmlize(marker.getNotes(), marker.getNotesType());
    var gmNotes = StringUtil.htmlize(marker.getGMNotes(), marker.getGmNotesType());

    boolean showGMNotes = MapTool.getPlayer().isGM() && !StringUtil.isEmpty(gmNotes);
    boolean showNotes = !StringUtil.isEmpty(notes);

    StringBuilder builder = new StringBuilder();

    if (marker.getPortraitImage() != null) {
      builder.append("<table><tr><td valign=top>");
    }
    if (showGMNotes || showNotes) {
      builder.append("<b><span class='title'>").append(marker.getName());
      if (MapTool.getPlayer().isGM()
          && !StringUtil.isEmpty(marker.getGMName())
          && !marker.getName().equals(marker.getGMName())) {
        builder.append(" (").append(marker.getGMName()).append(")");
      }
      builder.append("</span></b><br>");
    }
    if (showNotes) {
      builder.append(notes);
      // add a gap between player and gmNotes
      if (showGMNotes) {
        builder.append("<br><br>");
      }
    }
    if (showGMNotes) {
      if (showNotes) {
        builder.append("<b><span class='title'>GM Notes</span></b><br>");
      }
      builder.append(gmNotes);
    }
    if (marker.getPortraitImage() != null) {
      BufferedImage image = ImageManager.getImageAndWait(marker.getPortraitImage());
      Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
      if (imgSize.width > AppConstants.NOTE_PORTRAIT_SIZE
          || imgSize.height > AppConstants.NOTE_PORTRAIT_SIZE) {
        SwingUtil.constrainTo(imgSize, AppConstants.NOTE_PORTRAIT_SIZE);
      }
      builder.append("</td><td valign=top>");
      builder
          .append("<img src='asset://")
          .append(marker.getPortraitImage())
          .append("' width=")
          .append(imgSize.width)
          .append(" height=")
          .append(imgSize.height)
          .append("></tr></table>");
    }
    String hoverText = builder.toString();
    return hoverText;
  }
}
