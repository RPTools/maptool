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
package net.rptools.maptool.client.ui.zone.renderer;

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.CollectionUtil;
import net.rptools.lib.MD5Key;
import net.rptools.lib.StringUtil;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.functions.TokenMoveFunctions;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.ImageLabel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.swing.label.FlatImageLabelFactory;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.tool.StampTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.dialog.create.NewTokenDialog;
import net.rptools.maptool.client.ui.zone.*;
import net.rptools.maptool.client.ui.zone.gdx.GdxRenderer;
import net.rptools.maptool.client.ui.zone.renderer.tokenRender.FacingArrowRenderer;
import net.rptools.maptool.client.ui.zone.renderer.tokenRender.TokenRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.zones.*;
import net.rptools.maptool.util.GraphicsUtil;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.ImageSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** */
public class ZoneRenderer extends JComponent implements DropTargetListener {

  private static final long serialVersionUID = 3832897780066104884L;
  private static final Logger log = LogManager.getLogger(ZoneRenderer.class);

  /** DebounceExecutor for throttling repaint() requests. */
  private final DebounceExecutor repaintDebouncer;

  private final ZoneViewModel viewModel;

  /** Noise for mask on repeating tiles. */
  private DrawableNoise noise = null;

  /** Is the noise filter on for disrupting pattens in background tiled textures. */
  private boolean bgTextureNoiseFilterOn = false;

  private static LightSourceIconOverlay lightSourceIconOverlay = new LightSourceIconOverlay();

  /** The zone the ZoneRenderer was built from. */
  protected final Zone zone;

  /** The ZoneView constructed from the zone. */
  private final ZoneView zoneView;

  /** Manages the selected tokens on the zone. */
  private final SelectionModel selectionModel;

  private Scale zoneScale;
  private final Map<Zone.Layer, DrawableRenderer> drawableRenderers;
  private final List<ZoneOverlay> overlayList = new ArrayList<>();
  private final List<LabelLocation> labelLocationList = new LinkedList<>();
  private final Map<GUID, SelectionSet> selectionSetMap = new HashMap<>();
  private final List<Token> showPathList = new ArrayList<>();

  // Optimizations
  final Map<GUID, BufferedImage> labelRenderingCache = new HashMap<>();
  private Token tokenUnderMouse;

  private ScreenPoint pointUnderMouse;
  private @Nonnull Zone.Layer activeLayer = Layer.getDefaultPlayerLayer();

  private BufferedImage miniImage;
  private BufferedImage backBuffer;
  private boolean drawBackground = true;
  private int lastX;
  private int lastY;
  private double lastScale;
  private Area visibleScreenArea;
  private final List<ItemRenderer> itemRenderList = new LinkedList<>();
  private PlayerView lastView;

  private boolean autoResizeStamp = false;

  /** Store previous view to restore to, e.g. after GM shows ctrl+shift+space pointer */
  private double previousScale;

  private ZonePoint previousZonePoint;

  private final EnumSet<Layer> disabledLayers = EnumSet.noneOf(Layer.class);
  private final GridRenderer gridRenderer;
  private final HaloRenderer haloRenderer;
  private final TokenRenderer tokenRenderer;
  private final FacingArrowRenderer facingArrowRenderer;
  private final SelectionRenderer selectionRenderer;
  private final LightsRenderer lightsRenderer;
  private final DarknessRenderer darknessRenderer;
  private final LumensRenderer lumensRenderer;
  private final FogRenderer fogRenderer;
  private final VisionOverlayRenderer visionOverlayRenderer;
  private final DebugRenderer debugRenderer;

  public Token getTokenUnderMouse() {
    return tokenUnderMouse;
  }

  /**
   * Constructor for the ZoneRenderer from a zone.
   *
   * @param zone the zone of the ZoneRenderer
   */
  public ZoneRenderer(Zone zone) {
    if (zone == null) {
      throw new IllegalArgumentException("Zone cannot be null");
    }
    this.zone = zone;
    selectionModel = new SelectionModel(zone);
    zoneView = new ZoneView(zone);
    this.viewModel = new ZoneViewModel(zone, zoneView, selectionModel);
    setZoneScale(new Scale());

    drawableRenderers =
        CollectionUtil.newFilledEnumMap(
            Zone.Layer.class, layer -> new PartitionedDrawableRenderer(zone));

    var renderHelper = new RenderHelper(this, tempBufferPool);
    this.gridRenderer = new GridRenderer(this);
    this.haloRenderer = new HaloRenderer(renderHelper, zone);
    this.tokenRenderer = new TokenRenderer(renderHelper, zone);
    this.facingArrowRenderer = new FacingArrowRenderer(renderHelper, zone);
    this.selectionRenderer = new SelectionRenderer(renderHelper, viewModel, zoneView);
    this.lightsRenderer = new LightsRenderer(renderHelper, zone, zoneView);
    this.darknessRenderer = new DarknessRenderer(renderHelper, zoneView);
    this.lumensRenderer = new LumensRenderer(renderHelper, zone, zoneView);
    this.fogRenderer = new FogRenderer(renderHelper, zone, zoneView);
    this.visionOverlayRenderer = new VisionOverlayRenderer(renderHelper, zone, zoneView);
    this.debugRenderer = new DebugRenderer(renderHelper);
    repaintDebouncer =
        new DebounceExecutor(1000 / AppPreferences.frameRateCap.get(), this::repaint);

    setFocusable(true);

    // DnD
    setTransferHandler(new TransferableHelper());
    try {
      getDropTarget().addDropTargetListener(this);
    } catch (TooManyListenersException e1) {
      // Should never happen because the transfer handler fixes this problem.
    }

    // Focus
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
          }

          @Override
          public void mouseExited(MouseEvent e) {
            pointUnderMouse = null;
          }
        });
    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            pointUnderMouse = new ScreenPoint(e.getX(), e.getY());
          }
        });

    new MapToolEventBus().getMainEventBus().register(this);
  }

  public void setFrameRateCap(int cap) {
    this.repaintDebouncer.setDelay(1000 / cap);
  }

  public void setAutoResizeStamp(boolean value) {
    this.autoResizeStamp = value;
  }

  public boolean isAutoResizeStamp() {
    return autoResizeStamp;
  }

  public void showPath(Token token, boolean show) {
    if (show) {
      showPathList.add(token);
    } else {
      showPathList.remove(token);
    }
  }

  public List<Token> getShowPathList() {
    return showPathList;
  }

  /**
   * If token is not null, center on it, set the active layer to it, select it, and request focus.
   *
   * @param token the token to center on
   */
  public void centerOnAndSetSelected(Token token) {
    if (token == null) {
      return;
    }

    centerOn(new ZonePoint(token.getX(), token.getY()));
    setActiveLayer(token.getLayer());
    MapTool.getFrame()
        .getToolbox()
        .setSelectedTool(!token.getLayer().isStampLayer() ? PointerTool.class : StampTool.class);

    selectionModel.replaceSelection(Collections.singletonList(token.getId()));
    requestFocusInWindow();
  }

  public ZonePoint getCenterPoint() {
    return new ScreenPoint(getSize().width / 2d, getSize().height / 2d).convertToZone(this);
  }

  public boolean isPathShowing(Token token) {
    return showPathList.contains(token);
  }

  public Scale getZoneScale() {
    return zoneScale;
  }

  public void setZoneScale(Scale scale) {
    zoneScale = scale;
    invalidateCurrentViewCache();

    scale.addPropertyChangeListener(
        evt -> {
          if (Scale.PROPERTY_SCALE.equals(evt.getPropertyName())) {
            clearZoomDependantCaches();
          }
          visibleScreenArea = null;
          repaintDebouncer.dispatch();
        });
  }

  public void flushDrawableRenderer() {
    for (final var renderer : drawableRenderers.values()) {
      renderer.flush();
    }
  }

  public ScreenPoint getPointUnderMouse() {
    return pointUnderMouse;
  }

  public void setMouseOver(Token token) {
    if (tokenUnderMouse == token) {
      return;
    }
    tokenUnderMouse = token;
    repaintDebouncer.dispatch();
  }

  @Override
  public boolean isOpaque() {
    return false;
  }

  public void addMoveSelectionSet(String playerId, GUID keyToken, Set<GUID> tokenList) {
    // I'm not supposed to be moving a token when someone else is already moving it
    selectionSetMap.put(keyToken, new SelectionSet(this, playerId, keyToken, tokenList));
    repaintDebouncer.dispatch(); // Jamz: Seems to have no affect?
  }

  public @Nullable ZonePoint getKeyTokenDragAnchorPosition(GUID keyToken) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return null;
    }
    return set.getKeyTokenDragAnchorPosition();
  }

  public boolean hasMoveSelectionSetMoved(GUID keyToken, ZonePoint dragAnchorPosition) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return false;
    }

    return !set.getKeyTokenDragAnchorPosition().equals(dragAnchorPosition);
  }

  public void updateMoveSelectionSet(GUID keyToken, ZonePoint latestPoint) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return;
    }
    set.update(latestPoint);
    repaintDebouncer.dispatch(); // Jamz: may cause flicker when using AI
  }

  public Map<GUID, SelectionSet> getSelectionSetMap() {
    return selectionSetMap;
  }

  public void toggleMoveSelectionSetWaypoint(GUID keyToken, ZonePoint location) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return;
    }
    set.toggleWaypoint(location);
    repaintDebouncer.dispatch();
  }

  public void removeMoveSelectionSet(GUID keyToken) {
    SelectionSet set = selectionSetMap.remove(keyToken);
    if (set == null) {
      return;
    }
    set.cancel();
    repaintDebouncer.dispatch();
  }

  /**
   * Commit the move of the token selected
   *
   * @param keyTokenId the token ID of the key token
   */
  public void commitMoveSelectionSet(GUID keyTokenId) {
    SelectionSet set = selectionSetMap.remove(keyTokenId);
    if (set == null) {
      return;
    }
    // Let the last thread finish rendering the path if A* Pathfinding is on
    set.renderFinalPath();

    MapTool.serverCommand().stopTokenMove(getZone().getId(), keyTokenId);
    Token keyToken = new Token(zone.getToken(keyTokenId), true);

    /*
     * Lee: if the lead token is snapped-to-grid and has not moved, every follower should return to where they were. Flag set at PointerTool and StampTool's stopTokenDrag() Handling the rest here.
     */
    Set<GUID> selectionSet = set.getTokens();

    boolean stg = false;
    if (set.getWalker() != null) {
      if (set.getWalker().getDistance() >= 0) {
        stg = true;
      }
    } else {
      stg = true;
    }

    // Lee: check only matters for snap-to-grid
    if (stg) {
      CodeTimer.using(
          "ZoneRenderer.commitMoveSelectionSet",
          moveTimer -> {
            moveTimer.setThreshold(1);

            moveTimer.start("setup");

            var changedMaskTopologyTypes = EnumSet.noneOf(Zone.TopologyType.class);

            Path<? extends AbstractPoint> path =
                set.getWalker() != null ? set.getWalker().getPath() : set.getGridlessPath();
            // Jamz: add final path render here?

            List<GUID> filteredTokens = new ArrayList<>();
            moveTimer.stop("setup");

            moveTimer.start("each-token");
            for (GUID tokenGUID : selectionSet) {
              Token token = zone.getToken(tokenGUID);
              // If the token has been deleted, the GUID will still be in the
              // set but getToken() will return null.
              if (token == null) {
                continue;
              }

              var tokenPath = path.derive(zone.getGrid(), keyToken, token);
              token.setLastPath(tokenPath);

              // This is the last *anchor* point.
              var lastPoint = tokenPath.getWayPointList().getLast();
              var endPoint =
                  switch (lastPoint) {
                    case CellPoint cp -> {
                      // Anchor at the cell center.
                      var grid = zone.getGrid();
                      var zp = grid.convert(cp);
                      var centerOffset = grid.getCenterOffset();
                      zp.x += (int) centerOffset.x;
                      zp.y += (int) centerOffset.y;
                      yield zp;
                    }
                    case ZonePoint zp -> zp;
                  };
              token.moveDragAnchorTo(zone, endPoint);
              log.debug("Token end pos: {}, {}", token.getX(), token.getY());

              flush(token);
              MapTool.serverCommand().putToken(zone.getId(), token);

              // Only add certain tokens to the list to process in the move Macro function(s).
              if (token.getLayer().supportsWalker() && token.isVisible()) {
                filteredTokens.add(tokenGUID);
              }

              changedMaskTopologyTypes.addAll(token.getMaskTopologyTypes());
            }
            moveTimer.stop("each-token");

            moveTimer.start("onTokenMove");
            if (!filteredTokens.isEmpty()) {
              // give onTokenMove a chance to reject each token's movement.
              // to avoid re-scanning for handlers on each token we pass in all the tokens at once
              List<Token> tokensToCheck =
                  filteredTokens.stream().map(zone::getToken).collect(Collectors.toList());
              List<Token> tokensDenied =
                  TokenMoveFunctions.callForIndividualTokenMoveVetoes(path, tokensToCheck);
              for (Token token : tokensDenied) {
                denyMovement(token);
              }
            }
            moveTimer.stop("onTokenMove");

            moveTimer.start("onMultipleTokensMove");
            // Multiple tokens, the list of tokens, and call onMultipleTokensMove() macro function.
            if (filteredTokens.size() > 1) {
              // now determine if the macro returned false and if so, revert each token's move to
              // the last path.
              boolean moveDenied = TokenMoveFunctions.callForMultiTokenMoveVeto(filteredTokens);
              if (moveDenied) {
                for (GUID tokenGUID : filteredTokens) {
                  Token token = zone.getToken(tokenGUID);
                  denyMovement(token);
                }
              }
            }
            moveTimer.stop("onMultipleTokensMove");

            moveTimer.start("updateTokenTree");
            MapTool.getFrame().updateTokenTree();
            moveTimer.stop("updateTokenTree");

            if (!changedMaskTopologyTypes.isEmpty()) {
              zone.tokenMaskTopologyChanged(changedMaskTopologyTypes);
            }
          });
    } else {
      for (GUID tokenGUID : selectionSet) {
        denyMovement(zone.getToken(tokenGUID));
      }
    }
  }

  /**
   * Undo the last movement.
   *
   * @param token the token for which we undo the movement
   */
  private void denyMovement(final Token token) {
    Path<?> path = token.getLastPath();
    if (path != null) {
      ZonePoint zp;
      if (path.getCellPath().getFirst() instanceof CellPoint) {
        zp = zone.getGrid().convert((CellPoint) path.getCellPath().getFirst());
      } else {
        zp = (ZonePoint) path.getCellPath().getFirst();
      }
      // Relocate
      token.setX(zp.x);
      token.setY(zp.y);

      // Do it again to cancel out the last move position
      token.setX(zp.x);
      token.setY(zp.y);

      // No more last path
      token.setLastPath(null);
      MapTool.serverCommand().putToken(zone.getId(), token);

      // Cache clearing
      flush(token);
    }
  }

  public boolean isTokenMoving(Token token) {
    return viewModel.isTokenMoving(token.getId());
  }

  protected void setViewOffset(int x, int y) {
    zoneScale.setOffset(x, y);
    GdxRenderer.getInstance().setScale(zoneScale);
  }

  public void centerOn(ZonePoint point) {
    int x = point.x;
    int y = point.y;

    x = getSize().width / 2 - (int) (x * getScale()) - 1;
    y = getSize().height / 2 - (int) (y * getScale()) - 1;

    setViewOffset(x, y);
    repaintDebouncer.dispatch();
  }

  public void centerOn(CellPoint point) {
    centerOn(zone.getGrid().convert(point));
  }

  /**
   * Remove the token from: {@link #labelRenderingCache}. Set the {@link #visibleScreenArea} to
   * null. Flush the token from {@link #zoneView}.
   *
   * @param token the token to flush
   */
  public void flush(Token token) {
    // This method can be called from a non-EDT thread so if that happens, make sure we synchronize
    // with the EDT.
    labelRenderingCache.remove(token.getId());

    // This should be smarter, but whatever
    visibleScreenArea = null;

    zoneView.flush(token);
    GdxRenderer.getInstance().flushFog();
  }

  /**
   * @return the ZoneView
   */
  public ZoneView getZoneView() {
    return zoneView;
  }

  public ZoneViewModel getViewModel() {
    return viewModel;
  }

  public SelectionModel getSelectionModel() {
    return selectionModel;
  }

  /** Clear internal caches and back-buffers */
  public void flush() {
    viewModel.flush();

    if (zone.getBackgroundPaint() instanceof DrawableTexturePaint) {
      ImageManager.flushImage(((DrawableTexturePaint) zone.getBackgroundPaint()).getAssetId());
    }
    ImageManager.flushImage(zone.getMapAssetId());

    flushDrawableRenderer();
    zoneView.flushFog();
  }

  /** Flush the {@link #zoneView} and repaint. */
  public void flushLight() {
    zoneView.flush();
    repaintDebouncer.dispatch();
  }

  /** Set flushFog to true, visibleScreenArea to null, and repaints */
  public void flushFog() {
    visibleScreenArea = null;
    repaintDebouncer.dispatch();
    GdxRenderer.getInstance().flushFog();
  }

  /**
   * @return the Zone
   */
  public Zone getZone() {
    return zone;
  }

  public void addOverlay(ZoneOverlay overlay) {
    overlayList.add(overlay);
    repaintDebouncer.dispatch();
  }

  public void removeOverlay(ZoneOverlay overlay) {
    overlayList.remove(overlay);
    repaintDebouncer.dispatch();
  }

  public void moveViewBy(int dx, int dy) {
    setViewOffset(getViewOffsetX() + dx, getViewOffsetY() + dy);
  }

  public void moveViewByCells(int dx, int dy) {
    int gridSize = (int) (zone.getGrid().getSize() * getScale());

    int rawXOffset = getViewOffsetX() + dx * gridSize;
    int rawYOffset = getViewOffsetY() + dy * gridSize;

    int snappedXOffset = rawXOffset - rawXOffset % gridSize;
    int snappedYOffset = rawYOffset - rawYOffset % gridSize;

    setViewOffset(snappedXOffset, snappedYOffset);
  }

  public void zoomReset(int x, int y) {
    zoneScale.zoomReset(x, y);
    MapTool.getFrame().getZoomStatusBar().update();
    GdxRenderer.getInstance().setScale(zoneScale);
  }

  public void zoomIn(int x, int y) {
    zoneScale.zoomIn(x, y);
    MapTool.getFrame().getZoomStatusBar().update();
    GdxRenderer.getInstance().setScale(zoneScale);
  }

  public void zoomOut(int x, int y) {
    zoneScale.zoomOut(x, y);
    MapTool.getFrame().getZoomStatusBar().update();
    GdxRenderer.getInstance().setScale(zoneScale);
  }

  public void enforceView(int x, int y, double scale, int gmWidth, int gmHeight) {
    int width = getWidth();
    int height = getHeight();

    if ((width * gmHeight) < (height * gmWidth)) {
      // Our aspect ratio is narrower than server's, so fit to width
      scale = scale * width / gmWidth;
    } else {
      // Our aspect ratio is shorter than server's, so fit to height
      scale = scale * height / gmHeight;
    }

    previousScale = getScale();
    previousZonePoint = getCenterPoint();

    setScale(scale);
    centerOn(new ZonePoint(x, y));
    GdxRenderer.getInstance().setScale(zoneScale);
  }

  public void restoreView() {
    log.info("Restoring view: " + previousZonePoint);
    log.info("previousScale: " + previousScale);

    centerOn(previousZonePoint);
    setScale(previousScale);
    GdxRenderer.getInstance().setScale(zoneScale);
  }

  public void forcePlayersView() {
    ZonePoint zp = new ScreenPoint(getWidth() / 2d, getHeight() / 2d).convertToZone(this);
    MapTool.serverCommand()
        .enforceZoneView(getZone().getId(), zp.x, zp.y, getScale(), getWidth(), getHeight());
  }

  public void maybeForcePlayersView() {
    if (AppState.isPlayerViewLinked() && MapTool.getPlayer().isGM()) {
      forcePlayersView();
    }
  }

  public BufferedImage getMiniImage(int size) {
    return miniImage;
  }

  @Override
  public void paintComponent(Graphics g) {
    CodeTimer.using(
        "ZoneRenderer.renderZone",
        timer -> {
          timer.setThreshold(10);

          if (!MapTool.getFrame().getGdxPanel().isVisible()) {
            timer.start("paintComponent");
            Graphics2D g2d = (Graphics2D) g;

            timer.start("paintComponent:allocateBuffer");
            tempBufferPool.setWidth(getSize().width);
            tempBufferPool.setHeight(getSize().height);
            tempBufferPool.setConfiguration(g2d.getDeviceConfiguration());
            timer.stop("paintComponent:allocateBuffer");

            try (final var bufferHandle = tempBufferPool.acquire()) {
              final var buffer = bufferHandle.get();

              final var bufferG2d = buffer.createGraphics();
              // Keep the clip to avoid rendering more than we have to.
              bufferG2d.setClip(g2d.getClip());

              renderZone(bufferG2d, null);

              int noteVPos = 20;
              bufferG2d.setFont(AppStyle.labelFont);
              if (MapTool.getFrame().areFullScreenToolsShown()) {
                noteVPos += 40;
              }
              if (!AppPreferences.mapVisibilityWarning.get()
                  && (!zone.isVisible() && getPlayerView().isGMView())) {
                GraphicsUtil.drawBoxedString(
                    bufferG2d, I18N.getText("zone.map_not_visible"), getSize().width / 2, noteVPos);
                noteVPos += 20;
              }
              if (AppState.isShowAsPlayer()) {
                GraphicsUtil.drawBoxedString(
                    bufferG2d, I18N.getText("zone.player_view"), getSize().width / 2, noteVPos);
              }

              timer.start("paintComponent:renderBuffer");
              bufferG2d.dispose();
              g2d.drawImage(buffer, null, 0, 0);
              timer.stop("paintComponent:renderBuffer");
            }
          }

          timer.stop("paintComponent");
        });
  }

  public PlayerView getPlayerView() {
    return viewModel.getPlayerView();
  }

  /**
   * The returned {@link PlayerView} contains a list of tokens that includes either all selected
   * tokens that this player owns and that have their <code>HasSight</code> checkbox enabled, or all
   * owned tokens that have <code>HasSight</code> enabled.
   *
   * @param role the player role
   * @param selected whether to get the view of selected tokens, or all owned
   * @return the player view
   */
  public PlayerView makePlayerView(Player.Role role, boolean selected) {
    return viewModel.makePlayerView(role, selected);
  }

  /**
   * This method clears {@link #visibleScreenArea} and {@link #lastView}. It also flushes the {@link
   * #zoneView}.
   */
  public void invalidateCurrentViewCache() {
    visibleScreenArea = null;
    lastView = null;
  }

  public void restoreLayers() {
    disabledLayers.clear();
  }

  public void disableLayer(Layer layer) {
    disabledLayers.add(layer);
  }

  public boolean shouldRenderLayer(Layer layer, PlayerView view) {
    return !disabledLayers.contains(layer) && (layer.isVisibleToPlayers() || view.isGMView());
  }

  /**
   * This is the top-level method of the rendering pipeline that coordinates all other calls. {@link
   * #paintComponent(Graphics)} calls this method, then adds the two optional strings, "Map not
   * visible to players" and "Player View" as appropriate.
   *
   * @param g2d Graphics2D object normally passed in by {@link #paintComponent(Graphics)}
   * @param view PlayerView object that describes whether the view is a Player or GM view. Pass
   *     {@code null} to use the current view.
   */
  public void renderZone(Graphics2D g2d, @Nullable PlayerView view) {
    final var timer = CodeTimer.get();

    timer.start("update");
    viewModel.update();
    timer.stop("update");

    timer.start("setup");
    // Clear internal state
    itemRenderList.clear();

    if (view == null) {
      view = getPlayerView();
    }

    g2d = (Graphics2D) g2d.create();

    Rectangle viewRect = new Rectangle(getSize().width, getSize().height);

    g2d.setFont(AppStyle.labelFont);
    SwingUtil.useAntiAliasing(g2d);

    // much of the raster code assumes the user clip is set
    if (g2d.getClipBounds() == null) {
      g2d.setClip(0, 0, viewRect.width, viewRect.height);
    }

    // Are we still waiting to show the zone ?
    var loadingProgress = viewModel.getLoadingStatus();
    if (loadingProgress.isPresent()) {
      g2d.setColor(Color.black);
      g2d.fillRect(0, 0, viewRect.width, viewRect.height);
      GraphicsUtil.drawBoxedString(
          g2d, loadingProgress.get(), viewRect.width / 2, viewRect.height / 2);
      // Make sure we keep checking for completion.
      repaintDebouncer.dispatch();
      return;
    }
    if (MapTool.getCampaign().isBeingSerialized()) {
      g2d.setColor(Color.black);
      g2d.fillRect(0, 0, viewRect.width, viewRect.height);
      GraphicsUtil.drawBoxedString(
          g2d, "    Please Wait    ", viewRect.width / 2, viewRect.height / 2);
      return;
    }
    if (zone == null) {
      return;
    }
    if (lastView != null && !lastView.equals(view)) {
      invalidateCurrentViewCache();
    }
    lastView = view;

    timer.stop("setup");

    // Calculations
    timer.start("calcs-1");
    if (visibleScreenArea == null) {
      timer.start("ZoneRenderer-getVisibleArea");
      Area a = zoneView.getVisibleArea(view);
      timer.stop("ZoneRenderer-getVisibleArea");

      timer.start("createTransformedArea");
      if (!a.isEmpty()) {
        AffineTransform af = new AffineTransform();
        af.translate(zoneScale.getOffsetX(), zoneScale.getOffsetY());
        af.scale(getScale(), getScale());
        visibleScreenArea = a.createTransformedArea(af);
      }
      timer.stop("createTransformedArea");
    }

    timer.stop("calcs-1");

    // Rendering pipeline
    if (zone.drawBoard()) {
      timer.start("board");
      renderBoard(g2d, view);
      timer.stop("board");
    }
    if (shouldRenderLayer(Zone.Layer.BACKGROUND, view)) {
      List<DrawnElement> drawables = zone.getDrawnElements(Layer.BACKGROUND);

      timer.start("drawableBackground");
      renderDrawableOverlay(g2d, drawableRenderers.get(Layer.BACKGROUND), view, drawables);
      timer.stop("drawableBackground");

      List<Token> background = zone.getTokensOnLayer(Layer.BACKGROUND, false);
      if (!background.isEmpty()) {
        timer.start("tokensBackground");
        renderTokens(g2d, background, view);
        timer.stop("tokensBackground");
      }
    }
    if (shouldRenderLayer(Zone.Layer.OBJECT, view)) {
      // Drawables on the object layer are always below the grid, and...
      List<DrawnElement> drawables = zone.getDrawnElements(Layer.OBJECT);

      timer.start("drawableObjects");
      renderDrawableOverlay(g2d, drawableRenderers.get(Layer.OBJECT), view, drawables);
      timer.stop("drawableObjects");
    }
    timer.start("grid");

    gridRenderer.renderGrid(g2d, view);
    timer.stop("grid");

    if (shouldRenderLayer(Zone.Layer.OBJECT, view)) {
      // ... Images on the object layer are always ABOVE the grid.
      List<Token> stamps = zone.getTokensOnLayer(Layer.OBJECT, false);
      if (!stamps.isEmpty()) {
        timer.start("tokensStamp");
        renderTokens(g2d, stamps, view);
        timer.stop("tokensStamp");
      }
    }
    if (shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      this.lightsRenderer.renderLights(g2d, view);
      this.lumensRenderer.render(g2d, view);
      this.lightsRenderer.renderAuras(g2d, view);
    }

    darknessRenderer.render(g2d, view);

    /*
     * The following sections used to handle rendering of the Hidden (i.e. "GM") layer followed by
     * the Token layer. The problem was that we want all drawables to appear below all tokens, and
     * the old configuration performed the rendering in the following order:
     *
     * <ol>
     *   <li>Render Hidden-layer tokens
     *   <li>Render Hidden-layer drawables
     *   <li>Render Token-layer drawables
     *   <li>Render Token-layer tokens
     * </ol>
     *
     * That's fine for players, but clearly wrong if the view is for the GM. We now use:
     *
     * <ol>
     *   <li>Render Token-layer drawables // Player-drawn images shouldn't obscure GM's images?
     *   <li>Render Hidden-layer drawables // GM could always use "View As Player" if needed?
     *   <li>Render Hidden-layer tokens
     *   <li>Render Token-layer tokens
     * </ol>
     */
    if (shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      List<DrawnElement> drawables = zone.getDrawnElements(Layer.TOKEN);

      timer.start("drawableTokens");
      renderDrawableOverlay(g2d, drawableRenderers.get(Layer.TOKEN), view, drawables);
      timer.stop("drawableTokens");

      if (shouldRenderLayer(Zone.Layer.GM, view)) {
        drawables = zone.getDrawnElements(Layer.GM);

        timer.start("drawableGM");
        renderDrawableOverlay(g2d, drawableRenderers.get(Layer.GM), view, drawables);
        timer.stop("drawableGM");

        List<Token> stamps = zone.getTokensOnLayer(Layer.GM, false);
        if (!stamps.isEmpty()) {
          timer.start("tokensGM");
          renderTokens(g2d, stamps, view);
          timer.stop("tokensGM");
        }
      }
      List<Token> tokens = zone.getTokensOnLayer(Layer.TOKEN, false);
      if (!tokens.isEmpty()) {
        timer.start("tokens");
        renderTokens(g2d, tokens, view);
        timer.stop("tokens");
      }
      timer.start("unowned movement");
      showBlockedMoves(g2d, view, getUnOwnedMovementSet(view));
      timer.stop("unowned movement");
    }

    // (This method has its own 'timer' calls)
    if (AppState.getShowTextLabels()) {
      renderLabels(g2d, view);
    }

    this.fogRenderer.render(g2d, view);

    if (shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      // Jamz: If there is fog or vision we may need to re-render vision-blocking type tokens
      // For example. this allows a "door" stamp to block vision but still allow you to see the
      // door.
      List<Token> vblTokens = zone.getTokensAlwaysVisible();
      if (!vblTokens.isEmpty()) {
        timer.start("tokens - always visible");
        renderTokens(g2d, vblTokens, view, true);
        timer.stop("tokens - always visible");
      }

      // if there is fog or vision we may need to re-render figure type tokens
      // and figure tokens need sorting via alternative logic.
      List<Token> tokens = zone.getFigureTokens();
      List<Token> sortedTokens = new ArrayList<>(tokens);
      sortedTokens.sort(zone.getFigureZOrderComparator());
      if (!tokens.isEmpty()) {
        timer.start("tokens - figures");
        renderTokens(g2d, sortedTokens, view, true);
        timer.stop("tokens - figures");
      }

      timer.start("owned movement");
      showBlockedMoves(g2d, view, getOwnedMovementSet(view));
      timer.stop("owned movement");

      // Text associated with tokens being moved is added to a list to be drawn after, i.e. on top
      // of, the tokens themselves.
      // So if one moving token is on top of another moving token, at least the textual identifiers
      // will be visible.
      timer.start("token name/labels");
      renderRenderables(g2d);
      timer.stop("token name/labels");
    }

    this.visionOverlayRenderer.render(g2d, view, tokenUnderMouse);

    timer.start("overlays");
    for (ZoneOverlay overlay : overlayList) {
      timer.start("overlays: %s", overlay.getClass().getSimpleName());
      overlay.paintOverlay(this, g2d);
      timer.stop("overlays: %s", overlay.getClass().getSimpleName());
    }
    timer.stop("overlays");

    timer.start("renderCoordinates");
    gridRenderer.renderCoordinates(g2d, view);
    timer.stop("renderCoordinates");

    timer.start("lightSourceIconOverlay.paintOverlay");
    if (shouldRenderLayer(Zone.Layer.TOKEN, view)
        && view.isGMView()
        && AppState.isShowLightSources()) {
      lightSourceIconOverlay.paintOverlay(this, g2d);
    }
    timer.stop("lightSourceIconOverlay.paintOverlay");

    debugRenderer.renderShapes(g2d, Arrays.asList(shape, shape2, shape3, shape4));
  }

  private void delayRendering(ItemRenderer renderer) {
    itemRenderList.add(renderer);
  }

  private void renderRenderables(Graphics2D g) {
    for (ItemRenderer renderer : itemRenderList) {
      renderer.render(g);
    }
  }

  /**
   * Cache of images for rendering overlays.
   *
   * <p>Size is set to two: one for the buffer to draw the entire zone, and one for drawing each
   * overlay in turn.
   */
  private final BufferedImagePool tempBufferPool = new BufferedImagePool(2);

  private void renderLabels(Graphics2D g, PlayerView view) {
    final var timer = CodeTimer.get();

    timer.start("labels-1");
    var labelRenderFactory = new FlatImageLabelFactory();
    labelLocationList.clear();
    for (Label label : zone.getLabels()) {
      var fLabel = labelRenderFactory.getMapImageLabel(label);
      ZonePoint zp = new ZonePoint(label.getX(), label.getY());
      if (!zone.isPointVisible(zp, view)) {
        continue;
      }
      timer.start("labels-1.1");
      ScreenPoint sp = ScreenPoint.fromZonePointRnd(this, zp.x, zp.y);
      var dim = fLabel.getDimensions(g, label.getLabel());
      Rectangle bounds =
          fLabel.render(
              g, (int) (sp.x - dim.width / 2), (int) (sp.y - dim.height / 2), label.getLabel());
      labelLocationList.add(new LabelLocation(bounds, label));
      timer.stop("labels-1.1");
    }
    timer.stop("labels-1");
  }

  protected void renderDrawableOverlay(
      Graphics g, DrawableRenderer renderer, PlayerView view, List<DrawnElement> drawnElements) {
    Rectangle viewport =
        new Rectangle(
            zoneScale.getOffsetX(), zoneScale.getOffsetY(), getSize().width, getSize().height);

    renderer.renderDrawables(g, drawnElements, viewport, getScale());
  }

  protected void renderBoard(Graphics2D g, PlayerView view) {
    Dimension size = getSize();
    if (backBuffer == null
        || backBuffer.getWidth() != size.width
        || backBuffer.getHeight() != size.height) {
      backBuffer = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
      drawBackground = true;
    }
    Scale scale = getZoneScale();
    if (scale.getOffsetX() != lastX
        || scale.getOffsetY() != lastY
        || scale.getScale() != lastScale) {
      drawBackground = true;
    }
    if (zone.isBoardChanged()) {
      drawBackground = true;
      zone.setBoardChanged(false);
    }
    if (drawBackground) {
      Graphics2D bbg = backBuffer.createGraphics();
      AppPreferences.renderQuality.get().setRenderingHints(bbg);

      // Background texture
      Paint paint =
          zone.getBackgroundPaint().getPaint(getViewOffsetX(), getViewOffsetY(), getScale(), this);
      bbg.setPaint(paint);
      bbg.fillRect(0, 0, size.width, size.height);

      // Only apply the noise if the feature is on and the background a textured paint
      if (bgTextureNoiseFilterOn && paint instanceof TexturePaint) {
        bbg.setPaint(noise.getPaint(getViewOffsetX(), getViewOffsetY(), getScale()));
        bbg.fillRect(0, 0, size.width, size.height);
      }

      // Map
      if (zone.getMapAssetId() != null) {
        BufferedImage mapImage = ImageManager.getImage(zone.getMapAssetId(), this);
        double scaleFactor = getScale();
        bbg.drawImage(
            mapImage,
            getViewOffsetX() + (int) (zone.getBoardX() * scaleFactor),
            getViewOffsetY() + (int) (zone.getBoardY() * scaleFactor),
            (int) (mapImage.getWidth() * scaleFactor * zone.getImageScaleX()),
            (int) (mapImage.getHeight() * scaleFactor * zone.getImageScaleY()),
            null);
      }
      bbg.dispose();
      drawBackground = false;
    }
    lastX = scale.getOffsetX();
    lastY = scale.getOffsetY();
    lastScale = scale.getScale();

    g.drawImage(backBuffer, 0, 0, this);
  }

  public Set<SelectionSet> getOwnedMovementSet(PlayerView view) {
    Set<SelectionSet> movementSet = new HashSet<>();
    for (SelectionSet selection : selectionSetMap.values()) {
      if (selection.getPlayerId().equals(MapTool.getPlayer().getName())) {
        movementSet.add(selection);
      }
    }
    return movementSet;
  }

  public Set<SelectionSet> getUnOwnedMovementSet(PlayerView view) {
    Set<SelectionSet> movementSet = new HashSet<>();
    for (SelectionSet selection : selectionSetMap.values()) {
      if (!selection.getPlayerId().equals(MapTool.getPlayer().getName())) {
        movementSet.add(selection);
      }
    }
    return movementSet;
  }

  protected void showBlockedMoves(Graphics2D g, PlayerView view, Set<SelectionSet> movementSet) {
    if (selectionSetMap.isEmpty()) {
      return;
    }
    g = (Graphics2D) g.create();

    // Regardless of vision settings, no need to render beyond the fog.
    Area clearArea = null;
    if (!view.isGMView()) {
      if (zone.hasFog() && zoneView.isUsingVision()) {
        clearArea = new Area(zoneView.getExposedArea(view));
        clearArea.intersect(zoneView.getVisibleArea(view));
      } else if (zone.hasFog()) {
        clearArea = zoneView.getExposedArea(view);
      } else if (zoneView.isUsingVision()) {
        clearArea = zoneView.getVisibleArea(view);
      }

      if (clearArea != null) {
        AffineTransform af = new AffineTransform();
        af.translate(zoneScale.getOffsetX(), zoneScale.getOffsetY());
        af.scale(getScale(), getScale());
        var clip = clearArea.createTransformedArea(af);

        g.clip(clip);
      }
    }

    for (SelectionSet set : movementSet) {
      Token keyToken = zone.getToken(set.getKeyToken());
      if (keyToken == null) {
        // It was removed ?
        selectionSetMap.remove(set.getKeyToken());
        continue;
      }
      // Hide the hidden layer
      if (!keyToken.getLayer().isVisibleToPlayers() && !view.isGMView()) {
        continue;
      }
      ZoneWalker walker = set.getWalker();

      for (GUID tokenGUID : set.getTokens()) {
        Token token = zone.getToken(tokenGUID);

        // Perhaps deleted?
        if (token == null) {
          continue;
        }

        var position = viewModel.getTokenPositions().get(token.getId());
        if (position == null) {
          // Token not visible to this player.
          continue;
        }

        // Show path only on the key token on token layer that are visible to the owner or gm while
        // fow and vision is on
        if (token == keyToken && token.getLayer().supportsWalker()) {
          renderPath(
              g,
              walker != null ? walker.getPath() : set.getGridlessPath(),
              token.getFootprint(zone.getGrid()));
        }

        // Show current Blocked Movement directions for A*
        if (walker != null && DeveloperOptions.Toggle.ShowAiDebugging.get()) {
          Map<CellPoint, Set<CellPoint>> blockedMovesByTarget = walker.getBlockedMoves();
          for (var entry : blockedMovesByTarget.entrySet()) {
            var targetPoint = entry.getKey();
            var blockedMoves = entry.getValue();

            for (CellPoint point : blockedMoves) {
              ZonePoint zp = getZone().getGrid().midZonePoint(point, targetPoint);
              showBlockedMoves(
                  g, zp, RessourceManager.getImage(Images.ZONE_RENDERER_BLOCK_MOVE), 1.0f);
            }
          }
        }

        // We need a shifted version of the position, to wherever the token is being dragged.
        var newBounds =
            new Rectangle2D.Double(
                position.footprintBounds().getX() + set.getOffsetX(),
                position.footprintBounds().getY() + set.getOffsetY(),
                position.footprintBounds().getWidth(),
                position.footprintBounds().getHeight());
        var newArea = new Area(position.transformedBounds());
        newArea.transform(AffineTransform.getTranslateInstance(set.getOffsetX(), set.getOffsetY()));
        var newPosition = new ZoneViewModel.TokenPosition(token, newBounds, newArea);

        tokenRenderer.renderToken(token, newPosition, g, 1);

        // Other details.
        // Only draw these if the token is visible on screen where it is dragged to.
        if (token == keyToken
            && (AppUtil.playerOwns(token) || shouldShowMovementLabels(token, set, clearArea))
            && viewModel.getViewport().intersects(newPosition.footprintBounds())) {
          var screenBounds = zoneScale.toScreenSpace(newPosition.footprintBounds());

          var labelY = (int) screenBounds.getMaxY() + 10;
          var labelX = (int) screenBounds.getCenterX();

          if (token.getLayer().supportsWalker() && AppState.getShowMovementMeasurements()) {
            double distanceTraveled = calculateTraveledDistance(set);
            if (distanceTraveled >= 0) {
              String distance = NumberFormat.getInstance().format(distanceTraveled);
              delayRendering(new LabelRenderer(this, distance, labelX, labelY));
              labelY += 20;
            }
          }
          if (set.getPlayerId() != null && !set.getPlayerId().isEmpty()) {
            delayRendering(new LabelRenderer(this, set.getPlayerId(), labelX, labelY));
          }
        }
      }
    }
  }

  private boolean shouldShowMovementLabels(Token token, SelectionSet set, Area clearArea) {
    Rectangle tokenRectangle;
    if (set.getWalker() != null) {
      final var path = set.getWalker().getPath();
      if (path.getCellPath().isEmpty()) {
        return false;
      }
      final var lastPoint = path.getCellPath().getLast();
      final var grid = zone.getGrid();
      tokenRectangle = token.getFootprint(grid).getBounds(grid, lastPoint);
    } else {
      final var path = set.getGridlessPath();
      if (path.getCellPath().isEmpty()) {
        return false;
      }
      final var lastPoint = path.getCellPath().getLast();
      Rectangle tokBounds = token.getFootprintBounds(zone);
      tokenRectangle = new Rectangle();
      tokenRectangle.setBounds(
          lastPoint.x, lastPoint.y, (int) tokBounds.getWidth(), (int) tokBounds.getHeight());
    }

    return clearArea == null || clearArea.intersects(tokenRectangle);
  }

  private double calculateTraveledDistance(SelectionSet set) {
    ZoneWalker walker = set.getWalker();
    if (walker != null) {
      // This wouldn't be true unless token.isSnapToGrid() && grid.isPathingSupported()
      return walker.getDistance();
    }

    double distanceTraveled = 0;
    ZonePoint lastPoint = null;
    for (ZonePoint zp : set.getGridlessPath().getCellPath()) {
      if (lastPoint == null) {
        lastPoint = zp;
        continue;
      }
      int a = lastPoint.x - zp.x;
      int b = lastPoint.y - zp.y;
      distanceTraveled += Math.hypot(a, b);
      lastPoint = zp;
    }
    distanceTraveled /= zone.getGrid().getSize(); // Number of "cells"
    distanceTraveled *= zone.getUnitsPerCell(); // "actual" distance traveled
    return distanceTraveled;
  }

  /**
   * Render the path of a token. Highlight the cells and draw the waypoints, distance numbers, and
   * line path.
   *
   * @param g The Graphics2D renderer.
   * @param path The path of the token.
   * @param footprint The footprint of the token.
   */
  @SuppressWarnings("unchecked")
  public void renderPath(
      Graphics2D g, Path<? extends AbstractPoint> path, TokenFootprint footprint) {
    final var timer = CodeTimer.get();

    if (path == null) {
      return;
    }
    if (path.getCellPath().isEmpty()) {
      return;
    }

    Object oldRendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Grid grid = zone.getGrid();
    double scale = getScale();

    Rectangle footprintBounds = footprint.getBounds(grid);
    if (path.getCellPath().getFirst() instanceof CellPoint) {
      timer.start("renderPath-1");
      CellPoint previousPoint = null;
      Point previousHalfPoint = null;

      Path<CellPoint> pathCP = (Path<CellPoint>) path;
      List<CellPoint> cellPath = pathCP.getCellPath();

      Set<CellPoint> pathSet = new HashSet<>();
      List<ZonePoint> waypointList = new LinkedList<>();
      for (CellPoint p : cellPath) {
        pathSet.addAll(footprint.getOccupiedCells(p));

        if (pathCP.isWaypoint(p) && previousPoint != null) {
          ZonePoint zp = grid.convert(p);
          zp.x += footprintBounds.width / 2;
          zp.y += footprintBounds.height / 2;
          waypointList.add(zp);
        }
        previousPoint = p;
      }

      // Don't show the final path point as a waypoint, it's redundant, and ugly
      if (!waypointList.isEmpty()) {
        waypointList.removeLast();
      }
      timer.stop("renderPath-1");

      timer.start("renderPath-2");
      Dimension cellOffset = zone.getGrid().getCellOffset();
      for (CellPoint p : pathSet) {
        ZonePoint zp = grid.convert(p);
        zp.x += (int) (grid.getCellWidth() / 2 + cellOffset.width);
        zp.y += (int) (grid.getCellHeight() / 2 + cellOffset.height);
        highlightCell(g, zp, grid.getCellHighlight(), 1.0f);
      }
      if (AppState.getShowMovementMeasurements()) {
        double cellAdj = grid.isHex() ? 2.5 : 2;
        for (CellPoint p : cellPath) {
          ZonePoint zp = grid.convert(p);
          zp.x += (int) (grid.getCellWidth() / cellAdj + cellOffset.width);
          zp.y += (int) (grid.getCellHeight() / cellAdj + cellOffset.height);
          addDistanceText(
              g, zp, 1.0f, p.getDistanceTraveled(zone), p.getDistanceTraveledWithoutTerrain());
        }
      }
      int w = 0;
      for (ZonePoint p : waypointList) {
        ZonePoint zp = new ZonePoint(p.x + cellOffset.width, p.y + cellOffset.height);
        highlightCell(g, zp, RessourceManager.getImage(Images.ZONE_RENDERER_CELL_WAYPOINT), .333f);
      }

      // Line path
      if (grid.getCapabilities().isPathLineSupported()) {
        ZonePoint lineOffset;
        if (grid.isHex()) {
          lineOffset = new ZonePoint(0, 0);
        } else {
          lineOffset =
              new ZonePoint(
                  footprintBounds.x + footprintBounds.width / 2 - grid.getOffsetX(),
                  footprintBounds.y + footprintBounds.height / 2 - grid.getOffsetY());
        }

        int xOffset = (int) (lineOffset.x * scale);
        int yOffset = (int) (lineOffset.y * scale);

        g.setColor(Color.blue);

        previousPoint = null;
        for (CellPoint p : cellPath) {
          if (previousPoint != null) {
            ZonePoint ozp = grid.convert(previousPoint);
            int ox = ozp.x;
            int oy = ozp.y;

            ZonePoint dzp = grid.convert(p);
            int dx = dzp.x;
            int dy = dzp.y;

            ScreenPoint origin = ScreenPoint.fromZonePoint(this, ox, oy);
            ScreenPoint destination = ScreenPoint.fromZonePoint(this, dx, dy);

            int halfX = (int) ((origin.x + destination.x) / 2);
            int halfY = (int) ((origin.y + destination.y) / 2);
            Point halfPoint = new Point(halfX, halfY);

            if (previousHalfPoint != null) {
              int x1 = previousHalfPoint.x + xOffset;
              int y1 = previousHalfPoint.y + yOffset;

              int x2 = (int) origin.x + xOffset;
              int y2 = (int) origin.y + yOffset;

              int xh = halfPoint.x + xOffset;
              int yh = halfPoint.y + yOffset;

              QuadCurve2D curve = new QuadCurve2D.Float(x1, y1, x2, y2, xh, yh);
              g.draw(curve);
            }
            previousHalfPoint = halfPoint;
          }
          previousPoint = p;
        }
      }
      timer.stop("renderPath-2");
    } else {
      timer.start("renderPath-3");
      // Zone point/grid-less path

      // Line
      Color highlight = new Color(255, 255, 255, 80);
      Stroke highlightStroke = new BasicStroke(9);
      Stroke oldStroke = g.getStroke();
      Object oldAA = SwingUtil.useAntiAliasing(g);
      ScreenPoint lastPoint = null;

      Path<ZonePoint> pathZP = (Path<ZonePoint>) path;
      List<ZonePoint> pathList = pathZP.getCellPath();
      for (ZonePoint zp : pathList) {
        if (lastPoint == null) {
          lastPoint =
              ScreenPoint.fromZonePointRnd(
                  this,
                  zp.x + (footprintBounds.width / 2d) * footprint.getScale(),
                  zp.y + (footprintBounds.height / 2d) * footprint.getScale());
          continue;
        }
        ScreenPoint nextPoint =
            ScreenPoint.fromZonePoint(
                this,
                zp.x + (footprintBounds.width / 2d) * footprint.getScale(),
                zp.y + (footprintBounds.height / 2d) * footprint.getScale());

        g.setColor(highlight);
        g.setStroke(highlightStroke);
        g.drawLine((int) lastPoint.x, (int) lastPoint.y, (int) nextPoint.x, (int) nextPoint.y);

        g.setStroke(oldStroke);
        g.setColor(Color.blue);
        g.drawLine((int) lastPoint.x, (int) lastPoint.y, (int) nextPoint.x, (int) nextPoint.y);
        lastPoint = nextPoint;
      }
      SwingUtil.restoreAntiAliasing(g, oldAA);

      // Waypoints
      boolean originPoint = true;
      for (ZonePoint p : pathList) {
        // Skip the first point (it's the path origin)
        if (originPoint) {
          originPoint = false;
          continue;
        }

        // Skip the final point
        if (p == pathList.getLast()) {
          continue;
        }
        p =
            new ZonePoint(
                (int) (p.x + (footprintBounds.width / 2) * footprint.getScale()),
                (int) (p.y + (footprintBounds.height / 2) * footprint.getScale()));
        highlightCell(g, p, RessourceManager.getImage(Images.ZONE_RENDERER_CELL_WAYPOINT), .333f);
      }
      timer.stop("renderPath-3");
    }

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldRendering);
  }

  private Shape shape;
  private Shape shape2;
  private Shape shape3;
  private Shape shape4;

  public void setShape(Shape shape) {
    if (shape == null) {
      return;
    }

    this.shape = shape;
    this.repaintDebouncer.dispatch();
  }

  public void setShape2(Shape shape) {
    if (shape == null) {
      return;
    }

    this.shape2 = shape;
    this.repaintDebouncer.dispatch();
  }

  public void setShape3(Shape shape) {
    if (shape == null) {
      return;
    }

    this.shape3 = shape;
    this.repaintDebouncer.dispatch();
  }

  public void setShape4(Shape shape) {
    if (shape == null) {
      return;
    }

    this.shape4 = shape;
    this.repaintDebouncer.dispatch();
  }

  public void showBlockedMoves(Graphics2D g, ZonePoint point, BufferedImage image, float size) {
    // Resize image to size of 1/4 size of grid
    double resizeWidth = zone.getGrid().getCellWidth() / image.getWidth() * .25;
    double resizeHeight = zone.getGrid().getCellHeight() / image.getHeight() * .25;

    double cWidth = image.getWidth() * getScale() * resizeWidth;
    double cHeight = image.getHeight() * getScale() * resizeHeight;

    double iWidth = cWidth * size;
    double iHeight = cHeight * size;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, point);

    AffineTransform backup = g.getTransform();

    g.drawImage(
        image,
        (int) (sp.x - iWidth / 2),
        (int) (sp.y - iHeight / 2),
        (int) iWidth,
        (int) iHeight,
        this);
    g.setTransform(backup);
  }

  public void highlightCell(Graphics2D g, ZonePoint point, BufferedImage image, float size) {
    Grid grid = zone.getGrid();
    double cWidth = grid.getCellWidth() * getScale();
    double cHeight = grid.getCellHeight() * getScale();

    double iWidth = cWidth * size;
    double iHeight = cHeight * size;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, point);

    g.drawImage(
        image,
        (int) (sp.x - iWidth / 2),
        (int) (sp.y - iHeight / 2),
        (int) iWidth,
        (int) iHeight,
        this);
  }

  public void addDistanceText(
      Graphics2D g, ZonePoint point, float size, double distance, double distanceWithoutTerrain) {
    if (distance == 0) {
      return;
    }

    Grid grid = zone.getGrid();
    double cWidth = grid.getCellWidth() * getScale();
    double cHeight = grid.getCellHeight() * getScale();

    double iWidth = cWidth * size;
    double iHeight = cHeight * size;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, point);

    int cellX = (int) (sp.x - iWidth / 2);
    int cellY = (int) (sp.y - iHeight / 2);

    // Draw distance for each cell
    double fontScale = (double) grid.getSize() / 50; // Font size of 12 at grid size 50 is default
    int fontSize = (int) (getScale() * 12 * fontScale);
    int textOffset = (int) (getScale() * 7 * fontScale); // 7 pixels at 100% zoom & grid size of 50

    String distanceText = NumberFormat.getInstance().format(distance);
    if (DeveloperOptions.Toggle.ShowAiDebugging.get()) {
      distanceText += " (" + NumberFormat.getInstance().format(distanceWithoutTerrain) + ")";
      fontSize = (int) (fontSize * 0.75);
    }

    Font font = new Font(Font.DIALOG, Font.BOLD, fontSize);
    Font originalFont = g.getFont();

    FontMetrics fm = g.getFontMetrics(font);
    int textWidth = fm.stringWidth(distanceText);

    g.setFont(font);
    g.setColor(Color.BLACK);

    g.drawString(
        distanceText,
        (int) (cellX + cWidth - textWidth - textOffset),
        (int) (cellY + cHeight - textOffset));
    g.setFont(originalFont);
  }

  /**
   * Get a list of tokens currently visible on the screen. The list is ordered by location starting
   * in the top left and going to the bottom right.
   *
   * @return the token list
   */
  public List<Token> getTokensOnScreen() {
    List<Token> list = new ArrayList<>();

    // Always assume tokens, for now
    for (ZoneViewModel.TokenPosition location : getTokenPositions(getActiveLayer())) {
      list.add(location.token());
    }

    // Sort by location on screen, top left to bottom right
    list.sort(
        (o1, o2) -> {
          if (o1.getY() < o2.getY()) {
            return -1;
          }
          if (o1.getY() > o2.getY()) {
            return 1;
          }
          return Integer.compare(o1.getX(), o2.getX());
        });
    return list;
  }

  public @Nonnull Zone.Layer getActiveLayer() {
    return activeLayer;
  }

  /**
   * Sets the active layer.
   *
   * @param layer the layer to set active
   */
  public void setActiveLayer(Zone.Layer layer) {
    activeLayer = Objects.requireNonNullElse(layer, Layer.getDefaultPlayerLayer());
    selectionModel.replaceSelection(Collections.emptyList());
    repaintDebouncer.dispatch();
  }

  /**
   * Get the token locations for the given layer, creates an empty list if there are no locations
   * for the given layer
   */
  private List<ZoneViewModel.TokenPosition> getTokenPositions(Zone.Layer layer) {
    return viewModel.getTokenPositionsForLayer(layer);
  }

  protected void renderTokens(Graphics2D g, List<Token> tokenList, PlayerView view) {
    renderTokens(g, tokenList, view, false);
  }

  protected void renderTokens(
      Graphics2D g, List<Token> tokenList, PlayerView view, boolean figuresOnly) {
    final var timer = CodeTimer.get();

    Graphics2D clippedG = g;
    var imageLabelFactory = new FlatImageLabelFactory();

    boolean isGMView = view.isGMView(); // speed things up

    timer.start("createClip");
    if (!isGMView
        && visibleScreenArea != null
        && !tokenList.isEmpty()
        && tokenList.getFirst().getLayer().supportsVision()) {
      clippedG = (Graphics2D) g.create();

      Area visibleArea = new Area(g.getClipBounds());
      visibleArea.intersect(visibleScreenArea);
      clippedG.setClip(new GeneralPath(visibleArea));
      AppPreferences.renderQuality.get().setRenderingHints(clippedG);
    }
    timer.stop("createClip");

    List<ZoneViewModel.TokenPosition> tokenPostProcessing = new ArrayList<>(tokenList.size());
    for (Token token : tokenList) {
      if (token.getShape() != Token.TokenShape.FIGURE && figuresOnly && !token.isAlwaysVisible()) {
        continue;
      }

      timer.start("token-list-1");
      ZoneViewModel.TokenPosition position;
      try {
        if (token.getLayer().isStampLayer() && viewModel.isTokenMoving(token.getId())) {
          continue;
        }

        position = viewModel.getTokenPositions().get(token.getId());
        if (position == null) {
          // Unknown token?
          continue;
        }
        if (!viewModel.getVisibleTokens(token.getLayer()).contains(token.getId())) {
          // Token not on screen or otherwise not visible.
          continue;
        }
      } finally {
        timer.stop("token-list-1");
      }

      Graphics2D tokenG;
      if (isTokenInNeedOfClipping(token, position.transformedBounds(), isGMView)) {
        tokenG = (Graphics2D) clippedG.create();
        if (token.getShape() == Token.TokenShape.FIGURE || token.isAlwaysVisible()) {
          Area cb =
              zone.getGrid()
                  .getTokenCellArea(zoneScale.toScreenSpace(position.transformedBounds()));
          tokenG.clip(cb);
        }
      } else {
        tokenG = (Graphics2D) g.create();
        AppPreferences.renderQuality.get().setRenderingHints(tokenG);
      }

      // Previous path
      timer.start("renderTokens:ShowPath");
      if (showPathList.contains(token) && token.getLastPath() != null) {
        renderPath(g, token.getLastPath(), token.getFootprint(zone.getGrid()));
      }
      timer.stop("renderTokens:ShowPath");

      timer.start("token-list-1b");
      // get token image, using image table if present
      BufferedImage image = ImageSupport.getTokenImage(token, this);
      timer.stop("token-list-1b");

      timer.start("token-list-5a");
      if (token.getIsFlippedIso() && getZone().getGrid().isIsometric()) {
        int newSize = (image.getWidth() + image.getHeight());
        token.setWidth(newSize);
        token.setHeight(newSize / 2);
      }
      timer.stop("token-list-5a");

      // Render Halo
      haloRenderer.renderHalo(tokenG, token, position);

      // Calculate alpha Transparency from token and use opacity to indicate that token is moving
      float opacity = token.getTokenOpacity();
      if (viewModel.isTokenMoving(token.getId())) {
        opacity = opacity / 2.0f;
      }
      // Finally render the token image
      timer.start("token-list-7");
      // Clipping is handled in the isTokenInNeedOfClipping() call far above.
      tokenRenderer.renderToken(token, position, tokenG, opacity);
      timer.stop("token-list-7");

      timer.start("token-list-9");
      // Set up the graphics so that the overlay can just be painted.
      Rectangle2D tokenBounds = zoneScale.toScreenSpace(position.transformedBounds().getBounds2D());
      Graphics2D locG =
          (Graphics2D)
              tokenG.create(
                  (int) tokenBounds.getX(),
                  (int) tokenBounds.getY(),
                  (int) tokenBounds.getWidth(),
                  (int) tokenBounds.getHeight());
      Rectangle bounds =
          new Rectangle(0, 0, (int) tokenBounds.getWidth(), (int) tokenBounds.getHeight());

      // Check each of the set values
      for (String state : MapTool.getCampaign().getTokenStatesMap().keySet()) {
        Object stateValue = token.getState(state);
        AbstractTokenOverlay overlay = MapTool.getCampaign().getTokenStatesMap().get(state);
        if (stateValue instanceof AbstractTokenOverlay) {
          overlay = (AbstractTokenOverlay) stateValue;
        }
        if (overlay == null
            || overlay.isMouseover() && token != tokenUnderMouse
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }
        overlay.paintOverlay(locG, token, bounds, stateValue);
      }
      timer.stop("token-list-9");

      timer.start("token-list-10");

      for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
        Object barValue = token.getState(bar);
        BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
        if (overlay == null
            || overlay.isMouseover() && token != tokenUnderMouse
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }

        overlay.paintOverlay(locG, token, bounds, barValue);
      }
      locG.dispose();
      timer.stop("token-list-10");

      timer.start("token-list-8");
      // Facing
      facingArrowRenderer.paintArrow(tokenG, position);
      timer.stop("token-list-8");

      timer.start("token-list-11");
      // Keep track of which tokens have been drawn for post-processing on them later
      // (such as selection borders and names/labels)
      if (getActiveLayer().equals(token.getLayer())) {
        tokenPostProcessing.add(position);
      }
      timer.stop("token-list-11");
    }

    // Selection and labels
    timer.start("token-list-12");
    for (ZoneViewModel.TokenPosition position : tokenPostProcessing) {
      var token = position.token();

      // Count moving tokens as "selected" so that a border is drawn around them.
      boolean isSelected =
          selectionModel.isSelected(token.getId()) || viewModel.isTokenMoving(token.getId());
      if (isSelected) {
        selectionRenderer.drawSelectBorder(clippedG, position);
        // Remove labels from the cache if the corresponding tokens are deselected
      } else if (!AppState.isShowTokenNames()) {
        labelRenderingCache.remove(token.getId());
      }

      // Token names and labels
      boolean showCurrentTokenLabel = AppState.isShowTokenNames() || token == tokenUnderMouse;

      // if policy does not auto-reveal FoW, check if fog covers the token (slow)
      if (showCurrentTokenLabel
          && !isGMView
          && (!zoneView.isUsingVision() || !MapTool.getServerPolicy().isAutoRevealOnMovement())
          && !zone.isTokenVisible(token)) {
        showCurrentTokenLabel = false;
      }
      if (showCurrentTokenLabel) {
        GUID tokId = token.getId();
        int offset = 3; // Keep it from tramping on the token border.
        ImageLabel background;
        Color foreground;

        if (token.isVisible()) {
          if (token.getType() == Token.Type.NPC) {
            background = GraphicsUtil.BLUE_LABEL;
            foreground = Color.WHITE;
          } else {
            background = GraphicsUtil.GREY_LABEL;
            foreground = Color.BLACK;
          }
        } else {
          background = GraphicsUtil.DARK_GREY_LABEL;
          foreground = Color.WHITE;
        }
        String name = token.getName();
        if (isGMView && token.getGMName() != null && !StringUtil.isEmpty(token.getGMName())) {
          name += " (" + token.getGMName() + ")";
        }
        if (!view.equals(lastView) || !labelRenderingCache.containsKey(tokId)) {
          boolean hasLabel = false;

          var flatImgLabel = imageLabelFactory.getMapImageLabel(token);

          var nameDimension = flatImgLabel.getDimensions(g, name);
          var labelDimension = new Dimension(0, 0);
          // If token has a label (in addition to name).
          if (token.getLabel() != null && !token.getLabel().trim().isEmpty()) {
            hasLabel = true;
            labelDimension = flatImgLabel.getDimensions(g, token.getLabel());
          }
          int width = (int) Math.max(nameDimension.getWidth(), labelDimension.getWidth());
          int height = (int) (nameDimension.getHeight() + labelDimension.getHeight()) + 4;
          var labelRender = new BufferedImage(width, height, Transparency.TRANSLUCENT);
          Graphics2D gLabelRender = labelRender.createGraphics();
          gLabelRender.setRenderingHints(g.getRenderingHints());

          // Draw name and label to image
          if (hasLabel) {
            flatImgLabel.render(
                gLabelRender,
                (width - labelDimension.width) / 2,
                nameDimension.height + 4,
                token.getLabel());
          }
          flatImgLabel.render(gLabelRender, (width - nameDimension.width) / 2, 0, name);

          // Add image to cache
          labelRenderingCache.put(tokId, labelRender);
        }
        // Create LabelRenderer using cached label.
        Rectangle r =
            zoneScale.toScreenSpace(position.transformedBounds().getBounds2D()).getBounds();
        delayRendering(
            new LabelRenderer(
                this,
                name,
                r.x + r.width / 2,
                r.y + r.height + offset,
                SwingUtilities.CENTER,
                background,
                foreground,
                tokId));
      }
    }
    timer.stop("token-list-12");

    // Stacks
    timer.start("token-list-13");
    if (!tokenList.isEmpty() && tokenList.getFirst().getLayer().isTokenLayer()) {
      boolean hideTSI = AppPreferences.hideTokenStackIndicator.get();
      if (!hideTSI) {
        for (Token token : viewModel.getTokenStackMap().keySet()) {
          var position = viewModel.getTokenPositions().get(token.getId());
          if (position == null) {
            // Shouldn't happen, but should handle the case anyway.
            continue;
          }
          if (!viewModel.getOnScreenTokens().contains(token.getId())) {
            // Don't draw indicator for offscreen tokens.
            continue;
          }

          var bounds = zoneScale.toScreenSpace(position.transformedBounds().getBounds2D());

          BufferedImage stackImage = RessourceManager.getImage(Images.ZONE_RENDERER_STACK_IMAGE);
          clippedG.drawImage(
              stackImage,
              AffineTransform.getTranslateInstance(
                  bounds.getMaxX() - stackImage.getWidth() + 2, bounds.getMinY() - 2),
              null);
        }
      }
    }

    if (clippedG != g) {
      clippedG.dispose();
    }
    timer.stop("token-list-13");
  }

  /**
   * Returns whether the token should be clipped, depending on its bounds, the view, and the visible
   * screen area.
   *
   * @param token the token that could be clipped
   * @param tokenCellArea the cell area corresponding to the bounds of the token
   * @param isGMView whether it is the view of a GM
   * @return true if the token is need of clipping, false otherwise
   */
  public boolean isTokenInNeedOfClipping(Token token, Area tokenCellArea, boolean isGMView) {
    // can view everything or zone is not using vision = no clipping needed
    if (isGMView || !zoneView.isUsingVision()) {
      return false;
    }

    var visibleArea = viewModel.getVisibleArea();
    if (visibleArea.isEmpty()) {
      // No clipping if there is no visible screen area.
      return false;
    }

    // If the token is a figure and its center is visible then no clipping
    if (token.getShape() == Token.TokenShape.FIGURE
        && zone.getGrid().checkCenterRegion(tokenCellArea.getBounds(), visibleArea)) {
      return false;
    }

    // Jamz: Always Visible tokens will get rendered fully to place on top of FoW
    // if we can see a portion of the stamp/token, defaults to 2/9ths, don't clip at all
    if (token.isAlwaysVisible()
        && zone.getGrid()
            .checkRegion(
                tokenCellArea.getBounds(), visibleArea, token.getAlwaysVisibleTolerance())) {
      return false;
    }

    // clipping needed
    return true;
  }

  public Set<GUID> getSelectedTokenSet() {
    return selectionModel.getSelectedTokenIds();
  }

  /**
   * Convenience method to return a set of tokens filtered by ownership.
   *
   * @param tokenSet the set of GUIDs to filter
   * @return the set of GUIDs
   */
  public Set<GUID> getOwnedTokens(Set<GUID> tokenSet) {
    Set<GUID> ownedTokens = new LinkedHashSet<>();
    if (tokenSet != null) {
      for (GUID guid : tokenSet) {
        Token token = zone.getToken(guid);
        if (token == null || !AppUtil.playerOwns(token)) {
          continue;
        }
        ownedTokens.add(guid);
      }
    }
    return ownedTokens;
  }

  /**
   * A convenience method to get selected tokens that actually exist.
   *
   * @return List of tokens
   */
  public List<Token> getSelectedTokensList() {
    return new ArrayList<>(viewModel.getSelectedTokenList());
  }

  /**
   * Verifies if a token is selectable based on existence, visibility and ownership.
   *
   * @param tokenGUID the token
   * @return whether the token is selectable
   */
  public boolean isTokenSelectable(GUID tokenGUID) {
    if (tokenGUID == null) {
      return false; // doesn't exist
    }
    Token token = zone.getToken(tokenGUID);
    if (token == null) {
      return false; // doesn't exist
    }
    if (!zone.isTokenVisible(token)) {
      return AppUtil.playerOwns(token); // can't own or see
    }
    return true;
  }

  /**
   * Gets the tokens inside the viewport.
   *
   * <p>This is a convenience method for {@link #getTokenIdsInBounds(Rectangle)} that supplies the
   * viewport as the rectangle.
   *
   * @return A list of token IDs for tokens whose footprint intersects with the current viewport.
   */
  public List<GUID> getTokenIdsOnScreen() {
    return getTokenIdsInBounds(getBounds());
  }

  /**
   * Gets the tokens inside a rectangle.
   *
   * @param screenRect the bounds in which to look for tokens.
   * @return A list of token IDs for tokens whose footprint intersects with {@code screenRect}.
   */
  public List<GUID> getTokenIdsInBounds(Rectangle screenRect) {
    var rect = zoneScale.toWorldSpace(screenRect);

    final var tokens = new ArrayList<GUID>();
    for (ZoneViewModel.TokenPosition position : getTokenPositions(getActiveLayer())) {
      if (rect.intersects(position.transformedBounds().getBounds())) {
        tokens.add(position.token().getId());
      }
    }
    return tokens;
  }

  public void cycleSelectedToken(int direction) {
    List<Token> visibleTokens = getTokensOnScreen();
    int newSelection = 0;

    if (visibleTokens.isEmpty()) {
      return;
    }
    if (selectionModel.isAnyTokenSelected()) {
      // Find the first selected token on the screen
      for (int i = 0; i < visibleTokens.size(); i++) {
        Token token = visibleTokens.get(i);
        if (!isTokenSelectable(token.getId())) {
          continue;
        }
        if (selectionModel.isSelected(token.getId())) {
          newSelection = i;
          break;
        }
      }
      // Pick the next
      newSelection += direction;
    }
    if (newSelection < 0) {
      newSelection = visibleTokens.size() - 1;
    }
    if (newSelection >= visibleTokens.size()) {
      newSelection = 0;
    }

    // Make the selection
    selectionModel.replaceSelection(
        Collections.singletonList(visibleTokens.get(newSelection).getId()));
  }

  public Rectangle getLabelBounds(Label label) {
    for (LabelLocation location : labelLocationList) {
      if (location.label == label) {
        return location.bounds;
      }
    }
    return null;
  }

  /**
   * Returns the token at screen location x, y (not cell location).
   *
   * @param x screen location x
   * @param y screen location y
   * @return the token
   */
  public @Nullable Token getTokenAt(int x, int y) {
    var zonePoint = ScreenPoint.convertToZone2d(this, x, y);

    List<ZoneViewModel.TokenPosition> positionList =
        new ArrayList<>(getTokenPositions(getActiveLayer()));
    Collections.reverse(positionList);
    for (ZoneViewModel.TokenPosition location : positionList) {
      if (location.transformedBounds().contains(zonePoint)) {
        return location.token();
      }
    }
    return null;
  }

  public @Nullable Token getMarkerAt(int x, int y) {
    var zonePoint = ScreenPoint.convertToZone2d(this, x, y);

    List<ZoneViewModel.TokenPosition> positionList =
        new ArrayList<>(viewModel.getMarkerPositions());
    Collections.reverse(positionList);
    for (ZoneViewModel.TokenPosition position : positionList) {
      if (position.transformedBounds().contains(zonePoint)) {
        return position.token();
      }
    }
    return null;
  }

  public List<Token> getTokenStackAt(int x, int y) {
    var tokenStackMap = viewModel.getTokenStackMap();

    Token token = getTokenAt(x, y);
    if (token == null || !tokenStackMap.containsKey(token)) {
      return null;
    }
    List<Token> tokenList = new ArrayList<>(tokenStackMap.get(token));
    tokenList.sort(Token.COMPARE_BY_NAME);
    return tokenList;
  }

  /**
   * Returns the label at screen location x, y (not cell location). To get the token at a cell
   * location, use getGameMap() and use that.
   *
   * @param x the screen location x
   * @param y the screen location y
   * @return the Label
   */
  public Label getLabelAt(int x, int y) {
    List<LabelLocation> labelList = new ArrayList<>(labelLocationList);
    Collections.reverse(labelList);
    for (LabelLocation location : labelList) {
      if (location.bounds.contains(x, y)) {
        return location.label;
      }
    }
    return null;
  }

  public int getViewOffsetX() {
    return zoneScale.getOffsetX();
  }

  public int getViewOffsetY() {
    return zoneScale.getOffsetY();
  }

  /**
   * Since the map can be scaled, this is a convenience method to find out what cell is at this
   * location.
   *
   * @param screenPoint Find the cell for this point.
   * @return The cell coordinates of the passed screen point.
   */
  public CellPoint getCellAt(ScreenPoint screenPoint) {
    ZonePoint zp = screenPoint.convertToZone(this);
    return zone.getGrid().convert(zp);
  }

  /**
   * Converts a screen point to the center point of the corresponding grid cell.
   *
   * @param sp the screen point
   * @return ZonePoint with the coordinates of the center of the grid cell.
   */
  public ZonePoint getCellCenterAt(ScreenPoint sp) {
    Grid grid = getZone().getGrid();
    CellPoint cp = getCellAt(sp);
    Point2D.Double p2d = grid.getCellCenter(cp);
    return new ZonePoint((int) p2d.getX(), (int) p2d.getY());
  }

  public void setScale(double scale) {
    if (zoneScale.getScale() != scale) {
      /*
       * MCL: I think it is correct to clear these caches (if not more).
       */
      clearZoomDependantCaches();
      zoneScale.zoomScale(getWidth() / 2, getHeight() / 2, scale);
      MapTool.getFrame().getZoomStatusBar().update();
    }
  }

  private void clearZoomDependantCaches() {
    invalidateCurrentViewCache();
  }

  public double getScale() {
    return zoneScale.getScale();
  }

  public double getScaledGridSize() {
    // Optimize: only need to calc this when grid size or scale changes
    return getScale() * zone.getGrid().getSize();
  }

  /** This makes sure that any image updates get refreshed. This could be a little smarter. */
  @Override
  public boolean imageUpdate(Image img, int infoFlags, int x, int y, int w, int h) {
    repaintDebouncer.dispatch();
    return super.imageUpdate(img, infoFlags, x, y, w, h);
  }

  // DROP TARGET LISTENER
  /*
   * (non-Javadoc)
   *
   * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd. DropTargetDragEvent )
   */
  @Override
  public void dragEnter(DropTargetDragEvent dtde) {}

  /*
   * (non-Javadoc)
   *
   * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
   */
  @Override
  public void dragExit(DropTargetEvent dte) {}

  /*
   * (non-Javadoc)
   *
   * @see java.awt.dnd.DropTargetListener#dragOver (java.awt.dnd.DropTargetDragEvent)
   */
  @Override
  public void dragOver(DropTargetDragEvent dtde) {}

  /**
   * Adds tokens at a given zone point coordinates.
   *
   * @param tokens the list of tokens to add
   * @param zp the zone point where to add the tokens
   * @param configureTokens the list indicating if each token is to be configured
   * @param showDialog whether to display a token edit dialog
   */
  public void addTokens(
      List<Token> tokens, ZonePoint zp, List<Boolean> configureTokens, boolean showDialog) {
    GridCapabilities gridCaps = zone.getGrid().getCapabilities();
    boolean isGM = MapTool.getPlayer().isGM();
    List<String> failedPaste = new ArrayList<>(tokens.size());
    List<GUID> selectThese = new ArrayList<>(tokens.size());

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, zp);
    Point dropPoint = new Point((int) sp.x, (int) sp.y);
    SwingUtilities.convertPointToScreen(dropPoint, this);
    int tokenIndex = 0;
    for (Token token : tokens) {
      boolean configureToken = configureTokens.get(tokenIndex++);

      // Get the snap to grid value for the current preferences and abilities
      token.setSnapToGrid(
          gridCaps.isSnapToGridSupported() && AppPreferences.tokensStartSnapToGrid.get());
      if (token.isSnapToGrid()) {
        zp = zone.getGrid().convert(zone.getGrid().convert(zp));
      }
      token.setX(zp.x);
      token.setY(zp.y);

      // Set the image properties
      if (configureToken) {
        BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());
        token.setWidth(image.getWidth(null));
        token.setHeight(image.getHeight(null));
        token.setFootprint(zone.getGrid(), zone.getGrid().getDefaultFootprint());
        token.guessAndSetShape();
      }

      // Always set the layer
      token.setLayer(getActiveLayer());

      // He who drops, owns, if there are no players already set
      // and if there are already players set, add the current one to the list.
      // (Cannot use AppUtil.playerOwns() since that checks 'isStrictTokenManagement' and we want
      // real ownership here.)
      if (!isGM && (!token.hasOwners() || !token.isOwner(MapTool.getPlayer().getName()))) {
        token.addOwner(MapTool.getPlayer().getName());
      }

      // Token type
      Rectangle size = token.getFootprintBounds(zone);
      switch (getActiveLayer()) {
        case TOKEN:
          // Players can't drop invisible tokens
          token.setVisible(!isGM || AppPreferences.newTokensVisible.get());
          if (AppPreferences.tokensStartFreesize.get()) {
            token.setSnapToScale(false);
          }
          break;
        case BACKGROUND:
          token.setShape(Token.TokenShape.TOP_DOWN);

          token.setSnapToScale(!AppPreferences.backgroundsStartFreesize.get());
          token.setSnapToGrid(AppPreferences.backgroundsStartSnapToGrid.get());
          token.setVisible(AppPreferences.newBackgroundsVisible.get());

          // Center on drop point
          if (!token.isSnapToScale() && !token.isSnapToGrid()) {
            token.setX(token.getX() - size.width / 2);
            token.setY(token.getY() - size.height / 2);
          }
          break;
        case OBJECT:
          token.setShape(Token.TokenShape.TOP_DOWN);

          token.setSnapToScale(!AppPreferences.objectsStartFreesize.get());
          token.setSnapToGrid(AppPreferences.objectsStartSnapToGrid.get());
          token.setVisible(AppPreferences.newObjectsVisible.get());

          // Center on drop point
          if (!token.isSnapToScale() && !token.isSnapToGrid()) {
            token.setX(token.getX() - size.width / 2);
            token.setY(token.getY() - size.height / 2);
          }
          break;
      }

      // This looks redundant. But calling getType() retrieves the type of
      // the Token and returns NPC if the type can't be determined (raw image,
      // corrupted token file, etc.). So retrieving it and then turning around and
      // setting it ensures it has a valid value without necessarily changing what
      // it was. :)
      Token.Type type = token.getType();
      token.setType(type);

      // Token type
      if (isGM) {
        // Check the name (after Token layer is set as name relies on layer)
        Token tokenNameUsed = zone.getTokenByName(token.getName());
        token.setName(MapToolUtil.nextTokenId(zone, token, tokenNameUsed != null));

        if (getActiveLayer() == Zone.Layer.TOKEN) {
          if (AppPreferences.showDialogOnNewToken.get() || showDialog) {
            NewTokenDialog dialog = new NewTokenDialog(token, dropPoint.x, dropPoint.y);
            if (dialog.showDialog().equals(GenericDialog.DENY)) {
              continue;
            }
          }
        }
      } else {
        /* Player dropped, ensure it's a PC token
        (Why? Couldn't a Player drop an RPTOK that represents an NPC, such as for a summoned monster?
        Unfortunately, we can't know at this point whether the original input was an RPTOK or not.)
        */
        token.setType(Token.Type.PC);

        /* For Players, check to see if the name is already in use. If it is already in use, make
        sure the current Player owns the token being duplicated (to avoid subtle ways of manipulating someone else's
        token!).
         */
        Token tokenNameUsed = zone.getTokenByName(token.getName());
        if (tokenNameUsed != null) {
          if (!AppUtil.playerOwns(tokenNameUsed)) {
            failedPaste.add(token.getName());
            continue;
          }
          String newName = MapToolUtil.nextTokenId(zone, token, tokenNameUsed != null);
          token.setName(newName);
        }
      }
      // Make sure all the assets are transferred
      for (MD5Key id : token.getAllImageAssets()) {
        Asset asset = AssetManager.getAsset(id);
        if (asset == null) {
          log.error("Could not find image for asset: " + id);
          continue;
        }
        MapToolUtil.uploadAsset(asset);
      }
      // Set all macros to "Allow players to edit macro", because the macros are not trusted
      if (!isGM) {
        Map<Integer, MacroButtonProperties> mbpMap = token.getMacroPropertiesMap(false);
        for (MacroButtonProperties mbp : mbpMap.values()) {
          if (!mbp.getAllowPlayerEdits()) {
            mbp.setAllowPlayerEdits(true);
          }
        }
      }

      // Save the token and tell everybody about it
      MapTool.serverCommand().putToken(zone.getId(), token);
      selectThese.add(token.getId());
    }
    // For convenience, select them
    selectionModel.replaceSelection(selectThese);

    if (!isGM) {
      String msg = I18N.getText("Token.dropped.byPlayer", zone.getName(), MapTool.getPlayer());
      MapTool.addMessage(TextMessage.gm(null, msg));
    }
    if (!failedPaste.isEmpty()) {
      String message = I18N.getText("Token.error.unableToPaste", failedPaste);
      TextMessage msg = TextMessage.gmMe(null, message);
      MapTool.addMessage(msg);
    }
    // Copy them to the clipboard so that we can quickly copy them onto the map
    AppActions.copyTokens(tokens);
    AppActions.updateActions();
    requestFocusInWindow();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.dnd.DropTargetListener#drop (java.awt.dnd.DropTargetDropEvent)
   */
  @Override
  public void drop(DropTargetDropEvent dtde) {
    if (MapTool.getPlayer().isGM() || !MapTool.getServerPolicy().getDisablePlayerAssetPanel()) {
      ZonePoint zp =
          new ScreenPoint((int) dtde.getLocation().getX(), (int) dtde.getLocation().getY())
              .convertToZone(this);
      TransferableHelper th = (TransferableHelper) getTransferHandler();
      List<Token> tokens = th.getTokens();
      if (tokens != null && !tokens.isEmpty()) {
        addTokens(tokens, zp, th.getConfigureTokens(), false);
      }
    }
  }

  public List<Token> getVisibleTokens() {
    var visibleTokenSet = viewModel.getVisibleTokens(Layer.TOKEN);

    List<Token> tokenList = new ArrayList<>(visibleTokenSet.size());
    for (GUID id : visibleTokenSet) {
      tokenList.add(zone.getToken(id));
    }
    return tokenList;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.dnd.DropTargetListener#dropActionChanged (java.awt.dnd.DropTargetDragEvent)
   */
  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {}

  @Subscribe
  private void onSelectionChanged(SelectionModel.SelectionChanged event) {
    if (event.zone() != zone) {
      return;
    }

    showPathList.clear();
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onTokensAdded(TokensAdded event) {
    if (event.zone() != this.zone) {
      return;
    }

    for (Token token : event.tokens()) {
      flush(token);
    }
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onTokensRemoved(TokensRemoved event) {
    if (event.zone() != this.zone) {
      return;
    }

    for (Token token : event.tokens()) {
      flush(token);
    }
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onTokensChanged(TokensChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    for (Token token : event.tokens()) {
      flush(token);
    }
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onFogChanged(FogChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    zoneView.flushFog();
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  private void onTopologyChanged() {
    flushFog();
    flushLight();
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onTopologyChanged(WallTopologyChanged event) {
    if (event.zone() != this.zone) {
      return;
    }
    onTopologyChanged();
  }

  @Subscribe
  private void onTopologyChanged(MaskTopologyChanged event) {
    if (event.zone() != this.zone) {
      return;
    }
    onTopologyChanged();
  }

  private void markDrawableLayerDirty(Layer layer) {
    drawableRenderers.get(layer).setDirty();
  }

  @Subscribe
  private void onDrawableAdded(DrawableAdded event) {
    if (event.zone() != this.zone) {
      return;
    }
    markDrawableLayerDirty(event.drawnElement().getDrawable().getLayer());
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onDrawableRemoved(DrawableRemoved event) {
    if (event.zone() != this.zone) {
      return;
    }
    markDrawableLayerDirty(event.drawnElement().getDrawable().getLayer());
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
  }

  @Subscribe
  private void onBoardChanged(BoardChanged event) {
    if (event.zone() != this.zone) {
      return;
    }
    repaintDebouncer.dispatch();
  }

  // Should this be moved to GridRenderer? No. Lots of things depend on the grid.
  @Subscribe
  private void onGridChanged(GridChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    // A change in grid can change the size of templates.
    flushDrawableRenderer();
    repaintDebouncer.dispatch();
  }

  /**
   * Our goal with this method (which overrides the parent's method) is to create a custom mouse
   * pointer that represents a group of tokens selected on the map. The idea is to provide some
   * feedback to the user that they have more than one token selected at the current time.
   *
   * <p>Unfortunately, while our custom cursor appears to be created correctly, it is never properly
   * applied as the mouse pointer so there is no visual effect, hence it is currently commented out
   * by using an "if (false)" around the code block.
   *
   * @param cursor the cursor to set.
   * @see java.awt.Component#setCursor(java.awt.Cursor)
   */
  @SuppressWarnings("unused")
  @Override
  public void setCursor(Cursor cursor) {
    if (false && cursor == Cursor.getDefaultCursor()) {
      custom = createCustomCursor("image/cursor.png", "Group");
      cursor = custom;
    }
    // Overlay and ZoneRenderer should have same cursor
    super.setCursor(cursor);
    MapTool.getFrame().getOverlayPanel().setOverlayCursor(cursor);
  }

  private Cursor custom = null;

  /**
   * Create a custom cursor.
   *
   * @param resource the String corresponding to the buffered image.
   * @param tokenName the name of the token, to be displayed by the cursor.
   * @return the created cursor.
   */
  public Cursor createCustomCursor(String resource, String tokenName) {
    Cursor c = null;
    try {
      BufferedImage img = ImageIO.read(MapTool.class.getResourceAsStream(resource));
      Font font = AppStyle.labelFont;
      Graphics2D z = (Graphics2D) this.getGraphics();
      z.setFont(font);
      FontRenderContext frc = z.getFontRenderContext();
      TextLayout tl = new TextLayout(tokenName, font, frc);
      Rectangle textBox = tl.getPixelBounds(null, 0, 0);

      // Create a larger BufferedImage to hold both the existing cursor and a token name.

      // Use the largest of the image width or string width, and the height of the image + the
      // height
      // of the string to represent the bounding box of the 'arrow+tokenName'
      Rectangle bounds =
          new Rectangle(Math.max(img.getWidth(), textBox.width), img.getHeight() + textBox.height);
      BufferedImage cursor =
          new BufferedImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
      Graphics2D g2d = cursor.createGraphics();
      g2d.setFont(font);
      g2d.setComposite(z.getComposite());
      g2d.setStroke(z.getStroke());
      g2d.setPaintMode();
      z.dispose();

      Object oldAA = SwingUtil.useAntiAliasing(g2d);
      g2d.drawImage(
          img, new AffineTransform(1f, 0f, 0f, 1f, 0, 0), null); // Draw the arrow at 1:1 resolution
      g2d.translate(0, img.getHeight() + textBox.height / 2);
      g2d.setColor(Color.BLACK);
      GraphicsUtil.drawBoxedString(
          g2d, tokenName, 0, 0, SwingUtilities.LEFT); // The text draw here is not nearly
      // as nice looking as normal
      g2d.dispose();
      c = Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(0, 0), tokenName);
      SwingUtil.restoreAntiAliasing(g2d, oldAA);

      img.flush(); // Try to be friendly about memory usage. ;-)
      cursor.flush();
    } catch (Exception ignored) {
    }
    return c;
  }

  /**
   * Returns the alpha level used to apply the noise to background repeating textures.
   *
   * @return the alpha level used to apply the noise.
   */
  public float getNoiseAlpha() {
    return noise.getNoiseAlpha();
  }

  /**
   * Returns the seed value used to generate the noise that is applied to the background repeating
   * images.
   *
   * @return the seed value used to generate the noise.
   */
  public long getNoiseSeed() {
    return noise.getNoiseSeed();
  }

  /**
   * Sets the seed value and alpha level used for the noise applied to repeating background
   * textures.
   *
   * @param seed The seed value used to generate the noise to be applied.
   * @param alpha The alpha level to apply the noise.
   */
  public void setNoiseValues(long seed, float alpha) {
    noise.setNoiseValues(seed, alpha);
    drawBackground = true;
  }

  /**
   * Returns if the setting for applying background noise to textures is on or off.
   *
   * @return <code>true</code> if noise will be applied to repeating background textures, otherwise
   *     <code>false</code>
   */
  public boolean isBgTextureNoiseFilterOn() {
    return bgTextureNoiseFilterOn;
  }

  /**
   * Turn on / off application of noise to repeated background textures.
   *
   * @param on <code>true</code> to turn on, <code>false</code> to turn off.
   */
  public void setBgTextureNoiseFilterOn(boolean on) {
    bgTextureNoiseFilterOn = on;
    drawBackground = true;
    if (on) {
      noise = new DrawableNoise();
    } else {
      noise = null;
    }
  }
}
