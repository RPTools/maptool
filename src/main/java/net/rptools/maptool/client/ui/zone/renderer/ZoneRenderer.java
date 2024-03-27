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
import javax.imageio.ImageIO;
import javax.swing.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.ZoneLoaded;
import net.rptools.maptool.client.functions.TokenMoveFunctions;
import net.rptools.maptool.client.swing.ImageBorder;
import net.rptools.maptool.client.swing.ImageLabel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.swing.label.FlatImageLabelFactory;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.tool.StampTool;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.tool.drawing.FreehandExposeTool;
import net.rptools.maptool.client.tool.drawing.OvalExposeTool;
import net.rptools.maptool.client.tool.drawing.PolygonExposeTool;
import net.rptools.maptool.client.tool.drawing.RectangleExposeTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.theme.Borders;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.dialog.create.NewTokenDialog;
import net.rptools.maptool.client.ui.zone.*;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.LookupTable.LookupEntry;
import net.rptools.maptool.model.Token.TokenShape;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.zones.*;
import net.rptools.maptool.util.CollectionUtil;
import net.rptools.maptool.util.GraphicsUtil;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/** */
public class ZoneRenderer extends JComponent
    implements DropTargetListener, Comparable<ZoneRenderer> {

  private static final long serialVersionUID = 3832897780066104884L;
  private static final Logger log = LogManager.getLogger(ZoneRenderer.class);

  /** DebounceExecutor for throttling repaint() requests. */
  private final DebounceExecutor repaintDebouncer;

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
  private final Map<Zone.Layer, DrawableRenderer> drawableRenderers =
      CollectionUtil.newFilledEnumMap(Zone.Layer.class, layer -> new PartitionedDrawableRenderer());
  private final List<ZoneOverlay> overlayList = new ArrayList<ZoneOverlay>();
  private final Map<Zone.Layer, List<TokenLocation>> tokenLocationMap =
      new HashMap<Zone.Layer, List<TokenLocation>>();
  private final List<LabelLocation> labelLocationList = new LinkedList<LabelLocation>();
  private Map<Token, Set<Token>> tokenStackMap;
  private final Map<GUID, SelectionSet> selectionSetMap = new HashMap<GUID, SelectionSet>();
  private final Map<Token, TokenLocation> tokenLocationCache = new HashMap<Token, TokenLocation>();
  private final List<TokenLocation> markerLocationList = new ArrayList<TokenLocation>();
  private GeneralPath facingArrow;
  private final List<Token> showPathList = new ArrayList<Token>();
  private boolean showAllPaths = true; // Jamz: new option to show path
  // Optimizations
  final Map<GUID, BufferedImage> labelRenderingCache = new HashMap<GUID, BufferedImage>();
  private final Map<Token, BufferedImage> flipImageMap = new HashMap<Token, BufferedImage>();
  private final Map<Token, BufferedImage> flipIsoImageMap = new HashMap<Token, BufferedImage>();
  private Token tokenUnderMouse;

  private ScreenPoint pointUnderMouse;
  private @Nonnull Zone.Layer activeLayer = Layer.getDefaultPlayerLayer();
  private String loadingProgress;
  private boolean isLoaded;

  private BufferedImage miniImage;
  private BufferedImage backbuffer;
  private boolean drawBackground = true;
  private int lastX;
  private int lastY;
  private double lastScale;
  private Area visibleScreenArea;
  private final List<ItemRenderer> itemRenderList = new LinkedList<ItemRenderer>();
  private PlayerView lastView;
  private Set<GUID> visibleTokenSet = new HashSet<>();

  private boolean autoResizeStamp = false;

  /** Store previous view to restore to, eg after GM shows ctrl+shift+space pointer */
  private double previousScale;

  private ZonePoint previousZonePoint;

  private final EnumSet<Layer> disabledLayers = EnumSet.noneOf(Layer.class);
  private final ZoneCompositor compositor;
  private final GridRenderer gridRenderer;
  private final HaloRenderer haloRenderer;
  private final LightsRenderer lightsRenderer;
  private final DarknessRenderer darknessRenderer;
  private final LumensRenderer lumensRenderer;
  private final FogRenderer fogRenderer;
  private final VisionOverlayRenderer visionOverlayRenderer;
  private final DebugRenderer debugRenderer;

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
    zoneView = new ZoneView(zone);
    setZoneScale(new Scale());

    var renderHelper = new RenderHelper(this, tempBufferPool);
    this.compositor = new ZoneCompositor();
    this.gridRenderer = new GridRenderer();
    this.haloRenderer = new HaloRenderer();
    this.lightsRenderer = new LightsRenderer(renderHelper, zone, zoneView);
    this.darknessRenderer = new DarknessRenderer(renderHelper, zoneView);
    this.lumensRenderer = new LumensRenderer(renderHelper, zone, zoneView);
    this.fogRenderer = new FogRenderer(renderHelper, zone, zoneView);
    this.visionOverlayRenderer = new VisionOverlayRenderer(renderHelper, zone, zoneView);
    this.debugRenderer = new DebugRenderer(renderHelper);
    repaintDebouncer = new DebounceExecutor(1000 / AppPreferences.getFrameRateCap(), this::repaint);

    setFocusable(true);
    selectionModel = new SelectionModel(zone);

    // add(MapTool.getFrame().getFxPanel(), PositionalLayout.Position.NW);

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

          @Override
          public void mouseEntered(MouseEvent e) {}
        });
    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            pointUnderMouse = new ScreenPoint(e.getX(), e.getY());
          }
        });
    // fps.start();

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
    return new ScreenPoint(getSize().width / 2, getSize().height / 2).convertToZone(this);
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
            tokenLocationCache.clear();
          }
          if (Scale.PROPERTY_OFFSET.equals(evt.getPropertyName())) {
            // flushFog = true;
          }
          visibleScreenArea = null;
          repaintDebouncer.dispatch();
        });
  }

  /**
   * I _hate_ this method. But couldn't think of a better way to tell the drawable renderer that a
   * new image had arrived TODO: FIX THIS ! Perhaps add a new app listener for when new images show
   * up, add the drawable renderer as a listener
   */
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

  public boolean hasMoveSelectionSetMoved(GUID keyToken, ZonePoint point) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return false;
    }
    Token token = zone.getToken(keyToken);
    int x = point.x - token.getX();
    int y = point.y - token.getY();

    return set.offsetX != x || set.offsetY != y;
  }

  public void updateMoveSelectionSet(GUID keyToken, ZonePoint offset) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return;
    }
    Token token = zone.getToken(keyToken);
    set.setOffset(offset.x - token.getX(), offset.y - token.getY());
    repaintDebouncer.dispatch(); // Jamz: may cause flicker when using AI
  }

  public void toggleMoveSelectionSetWaypoint(GUID keyToken, ZonePoint location) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return;
    }
    set.toggleWaypoint(location);
    repaintDebouncer.dispatch();
  }

  public ZonePoint getLastWaypoint(GUID keyToken) {
    SelectionSet set = selectionSetMap.get(keyToken);
    if (set == null) {
      return null;
    }
    return set.getLastWaypoint();
  }

  public void removeMoveSelectionSet(GUID keyToken) {
    SelectionSet set = selectionSetMap.remove(keyToken);
    if (set == null) {
      return;
    }
    repaintDebouncer.dispatch();
  }

  /**
   * Commit the move of the token selected
   *
   * @param keyTokenId the token ID of the key token
   */
  public void commitMoveSelectionSet(GUID keyTokenId) {
    // TODO: Quick hack to handle updating server state
    SelectionSet set = selectionSetMap.get(keyTokenId);

    if (set == null) {
      return;
    }

    // Let the last thread finish rendering the path if A* Pathfinding is on
    set.renderFinalPath();

    removeMoveSelectionSet(keyTokenId);
    MapTool.serverCommand().stopTokenMove(getZone().getId(), keyTokenId);
    Token keyToken = zone.getToken(keyTokenId);

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

            boolean topologyTokenMoved = false; // If any token has topology we need to reset FoW

            // Lee: the 1st of evils. changing it to handle proper computation
            // for a key token's snapped state
            AbstractPoint originPoint, tokenCell;
            if (keyToken.isSnapToGrid()) {
              originPoint = zone.getGrid().convert(new ZonePoint(keyToken.getX(), keyToken.getY()));
            } else {
              originPoint = new ZonePoint(keyToken.getX(), keyToken.getY());
            }

            Path<? extends AbstractPoint> path =
                set.getWalker() != null ? set.getWalker().getPath() : set.gridlessPath;
            // Jamz: add final path render here?

            List<GUID> filteredTokens = new ArrayList<GUID>();
            moveTimer.stop("setup");

            int offsetX, offsetY;

            moveTimer.start("eachtoken");
            for (GUID tokenGUID : selectionSet) {
              Token token = zone.getToken(tokenGUID);
              // If the token has been deleted, the GUID will still be in the
              // set but getToken() will return null.
              if (token == null) {
                continue;
              }

              // Lee: get offsets based on key token's snapped state
              if (token.isSnapToGrid()) {
                tokenCell = zone.getGrid().convert(new ZonePoint(token.getX(), token.getY()));
              } else {
                tokenCell = new ZonePoint(token.getX(), token.getY());
              }

              int cellOffX, cellOffY;
              if (token.isSnapToGrid() == keyToken.isSnapToGrid()) {
                cellOffX = originPoint.x - tokenCell.x;
                cellOffY = originPoint.y - tokenCell.y;
              } else {
                cellOffX = cellOffY = 0; // not used unless both are of same SnapToGrid
              }

              if (token.isSnapToGrid()
                  && (!AppPreferences.getTokensSnapWhileDragging() || !keyToken.isSnapToGrid())) {
                // convert to Cellpoint and back to ensure token ends up at correct X and Y
                CellPoint cellEnd =
                    zone.getGrid()
                        .convert(
                            new ZonePoint(
                                token.getX() + set.getOffsetX(), token.getY() + set.getOffsetY()));
                ZonePoint pointEnd = cellEnd.convertToZonePoint(zone.getGrid());
                offsetX = pointEnd.x - token.getX();
                offsetY = pointEnd.y - token.getY();
              } else {
                offsetX = set.getOffsetX();
                offsetY = set.getOffsetY();
              }

              /*
               * Lee: the problem now is to keep the precise coordinate computations for unsnapped tokens following a snapped key token. The derived path in the following section contains rounded
               * down values because the integer cell values were passed. If these were double in nature, the precision would be kept, but that would be too difficult to change at this stage...
               */

              token.applyMove(set, path, offsetX, offsetY, keyToken, cellOffX, cellOffY);

              // Lee: setting originPoint to landing point
              token.setOriginPoint(new ZonePoint(token.getX(), token.getY()));

              flush(token);
              MapTool.serverCommand().putToken(zone.getId(), token);

              // No longer need this version
              // Lee: redundant flush() already did this above
              // replacementImageMap.remove(token);

              // Only add certain tokens to the list to process in the move
              // Macro function(s).
              if (token.getLayer().supportsWalker() && token.isVisible()) {
                filteredTokens.add(tokenGUID);
              }

              if (token.hasAnyTopology()) {
                topologyTokenMoved = true;
              }
            }
            moveTimer.stop("eachtoken");

            moveTimer.start("onTokenMove");
            if (!filteredTokens.isEmpty()) {
              // give onTokenMove a chance to reject each token's movement.
              // pass in all the tokens at once so it doesn't have to re-scan for handlers for each
              // token
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
            // Multiple tokens, the list of tokens and call
            // onMultipleTokensMove() macro function.
            if (filteredTokens.size() > 1) {
              // now determine if the macro returned false and if so
              // revert each token's move to the last path.
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

            if (topologyTokenMoved) {
              zone.tokenTopologyChanged();
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
      ZonePoint zp = null;
      if (path.getCellPath().get(0) instanceof CellPoint) {
        zp = zone.getGrid().convert((CellPoint) path.getCellPath().get(0));
      } else {
        zp = (ZonePoint) path.getCellPath().get(0);
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
    for (SelectionSet set : selectionSetMap.values()) {
      if (set.contains(token)) {
        return true;
      }
    }
    return false;
  }

  protected void setViewOffset(int x, int y) {
    zoneScale.setOffset(x, y);
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
   * Remove the token from: {@link #tokenLocationCache}, {@link #flipImageMap}, {@link
   * #flipIsoImageMap}, {@link #labelRenderingCache}. Set the {@link #visibleScreenArea}, {@link
   * #tokenStackMap} to null. Flush the token from {@link #zoneView}.
   *
   * @param token the token to flush
   */
  public void flush(Token token) {
    // This method can be called from a non-EDT thread so if that happens, make sure
    // we synchronize with the EDT.
    synchronized (tokenLocationCache) {
      tokenLocationCache.remove(token);
    }
    flipImageMap.remove(token);
    flipIsoImageMap.remove(token);
    labelRenderingCache.remove(token.getId());

    // This should be smarter, but whatever
    visibleScreenArea = null;

    // This could also be smarter
    tokenStackMap = null;

    zoneView.flush(token);
  }

  /**
   * @return the ZoneView
   */
  public ZoneView getZoneView() {
    return zoneView;
  }

  public SelectionModel getSelectionModel() {
    return selectionModel;
  }

  /** Clear internal caches and backbuffers */
  public void flush() {
    if (zone.getBackgroundPaint() instanceof DrawableTexturePaint) {
      ImageManager.flushImage(((DrawableTexturePaint) zone.getBackgroundPaint()).getAssetId());
    }
    ImageManager.flushImage(zone.getMapAssetId());

    // MCL: I think these should be added, but I'm not sure so I'm not doing it.
    // tokenLocationMap.clear();
    // tokenLocationCache.clear();

    flushDrawableRenderer();
    flipImageMap.clear();
    flipIsoImageMap.clear();
    zoneView.flushFog();

    isLoaded = false;
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
  }

  /**
   * @return the Zone
   */
  public Zone getZone() {
    return zone;
  }

  public void addOverlay(ZoneOverlay overlay) {
    overlayList.add(overlay);
  }

  public void removeOverlay(ZoneOverlay overlay) {
    overlayList.remove(overlay);
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
  }

  public void zoomIn(int x, int y) {
    zoneScale.zoomIn(x, y);
    MapTool.getFrame().getZoomStatusBar().update();
  }

  public void zoomOut(int x, int y) {
    zoneScale.zoomOut(x, y);
    MapTool.getFrame().getZoomStatusBar().update();
  }

  public void setView(int x, int y, double scale) {

    setViewOffset(x, y);

    zoneScale.setScale(scale);
    MapTool.getFrame().getZoomStatusBar().update();
  }

  public void enforceView(int x, int y, double scale, int gmWidth, int gmHeight) {
    int width = getWidth();
    int height = getHeight();

    // if (((double) width / height) < ((double) gmWidth / gmHeight))
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
  }

  public void restoreView() {
    log.info("Restoring view: " + previousZonePoint);
    log.info("previousScale: " + previousScale);

    centerOn(previousZonePoint);
    setScale(previousScale);
  }

  public void forcePlayersView() {
    ZonePoint zp = new ScreenPoint(getWidth() / 2, getHeight() / 2).convertToZone(this);
    MapTool.serverCommand()
        .enforceZoneView(getZone().getId(), zp.x, zp.y, getScale(), getWidth(), getHeight());
  }

  public void maybeForcePlayersView() {
    if (AppState.isPlayerViewLinked() && MapTool.getPlayer().isGM()) {
      forcePlayersView();
    }
  }

  public BufferedImage getMiniImage(int size) {
    // if (miniImage == null && getTileImage() !=
    // ImageManager.UNKNOWN_IMAGE) {
    // miniImage = new BufferedImage(size, size, Transparency.OPAQUE);
    // Graphics2D g = miniImage.createGraphics();
    // g.setPaint(new TexturePaint(getTileImage(), new Rectangle(0, 0,
    // miniImage.getWidth(), miniImage.getHeight())));
    // g.fillRect(0, 0, size, size);
    // g.dispose();
    // }
    return miniImage;
  }

  @Override
  public void paintComponent(Graphics g) {
    CodeTimer.using(
        "ZoneRenderer.renderZone",
        timer -> {
          timer.setThreshold(10);

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
            // Keep the clip so we don't render more than we have to.
            bufferG2d.setClip(g2d.getClip());

            timer.start("paintComponent:createView");
            PlayerView pl = getPlayerView();
            timer.stop("paintComponent:createView");

            renderZone(bufferG2d, pl);

            int noteVPos = 20;
            bufferG2d.setFont(AppStyle.labelFont);
            if (MapTool.getFrame().areFullScreenToolsShown()) {
              noteVPos += 40;
            }
            if (!AppPreferences.getMapVisibilityWarning() && (!zone.isVisible() && pl.isGMView())) {
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

          timer.stop("paintComponent");
        });
  }

  public PlayerView getPlayerView() {
    return getPlayerView(MapTool.getPlayer().getEffectiveRole());
  }

  /**
   * The returned {@link PlayerView} contains a list of tokens that includes all selected tokens
   * that this player owns and that have their <code>HasSight</code> checkbox enabled.
   *
   * @param role the player role
   * @return the player view
   */
  public PlayerView getPlayerView(Player.Role role) {
    return getPlayerView(role, true);
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
  public PlayerView getPlayerView(Player.Role role, boolean selected) {
    List<Token> selectedTokens = null;
    if (selected && selectionModel.isAnyTokenSelected()) {
      selectedTokens = getSelectedTokensList();
      selectedTokens.removeIf(token -> !token.getHasSight() || !AppUtil.playerOwns(token));
    }
    if (selectedTokens == null || selectedTokens.isEmpty()) {
      // if no selected token qualifying for view, use owned tokens or player tokens with sight
      final boolean checkOwnership =
          MapTool.getServerPolicy().isUseIndividualViews() || MapTool.isPersonalServer();
      selectedTokens =
          checkOwnership
              ? zone.getOwnedTokensWithSight(MapTool.getPlayer())
              : zone.getPlayerTokensWithSight();
    }
    if (selectedTokens == null || selectedTokens.isEmpty()) {
      return new PlayerView(role);
    }

    return new PlayerView(role, selectedTokens);
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

  private boolean shouldRenderLayer(Layer layer, PlayerView view) {
    return !disabledLayers.contains(layer) && (layer.isVisibleToPlayers() || view.isGMView());
  }

  /**
   * This is the top-level method of the rendering pipeline that coordinates all other calls. {@link
   * #paintComponent(Graphics)} calls this method, then adds the two optional strings, "Map not
   * visible to players" and "Player View" as appropriate.
   *
   * @param g2d Graphics2D object normally passed in by {@link #paintComponent(Graphics)}
   * @param view PlayerView object that describes whether the view is a Player or GM view
   */
  public void renderZone(Graphics2D g2d, PlayerView view) {
    final var timer = CodeTimer.get();

    timer.start("setup");
    g2d = (Graphics2D) g2d.create();

    // Clear internal state
    tokenLocationMap.clear();
    markerLocationList.clear();
    itemRenderList.clear();

    if (!compositor.isInitialised()) compositor.setRenderer(this);
    if (!gridRenderer.isInitialised()) gridRenderer.setRenderer(this);
    if (!haloRenderer.isInitialised()) haloRenderer.setRenderer(this);

    Rectangle viewRect = new Rectangle(getSize().width, getSize().height);

    g2d.setFont(AppStyle.labelFont);
    SwingUtil.useAntiAliasing(g2d);

    Area viewArea = new Area(viewRect);
    // much of the raster code assumes the user clip is set
    if (g2d.getClipBounds() == null) {
      g2d.setClip(0, 0, viewRect.width, viewRect.height);
    }
    // Are we still waiting to show the zone ?
    if (isLoading()) {
      g2d.setColor(Color.black);
      g2d.fillRect(0, 0, viewRect.width, viewRect.height);
      GraphicsUtil.drawBoxedString(g2d, loadingProgress, viewRect.width / 2, viewRect.height / 2);
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

    Map<Token, Set<Token>> drawThese = compositor.drawWhat(viewRect);

    timer.stop("setup");

    // Calculations
    timer.start("calcs-1");
    AffineTransform af = new AffineTransform();
    af.translate(zoneScale.getOffsetX(), zoneScale.getOffsetY());
    af.scale(getScale(), getScale());

    if (visibleScreenArea == null) {
      timer.start("ZoneRenderer-getVisibleArea");
      Area a = zoneView.getVisibleArea(view);
      timer.stop("ZoneRenderer-getVisibleArea");

      timer.start("createTransformedArea");
      if (!a.isEmpty()) {
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
      // if (!drawables.isEmpty()) {
      timer.start("drawableBackground");
      renderDrawableOverlay(g2d, drawableRenderers.get(Layer.BACKGROUND), view, drawables);
      timer.stop("drawableBackground");
      // }
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
      // if (!drawables.isEmpty()) {
      timer.start("drawableObjects");
      renderDrawableOverlay(g2d, drawableRenderers.get(Layer.OBJECT), view, drawables);
      timer.stop("drawableObjects");
      // }
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

    /**
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
      // if (!drawables.isEmpty()) {
      timer.start("drawableTokens");
      renderDrawableOverlay(g2d, drawableRenderers.get(Layer.TOKEN), view, drawables);
      timer.stop("drawableTokens");
      // }

      if (shouldRenderLayer(Zone.Layer.GM, view)) {
        drawables = zone.getDrawnElements(Layer.GM);
        // if (!drawables.isEmpty()) {
        timer.start("drawableGM");
        renderDrawableOverlay(g2d, drawableRenderers.get(Layer.GM), view, drawables);
        timer.stop("drawableGM");
        // }
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

      // Moved below, after the renderFog() call...
      // timer.start("owned movement");
      // renderMoveSelectionSets(g2d, view, getOwnedMovementSet(view));
      // timer.stop("owned movement");

      // Text associated with tokens being moved is added to a list to be drawn after, i.e. on top
      // of, the tokens
      // themselves.
      // So if one moving token is on top of another moving token, at least the textual identifiers
      // will be
      // visible.
      // timer.start("token name/labels");
      // renderRenderables(g2d);
      // timer.stop("token name/labels");
    }

    /**
     * FJE It's probably not appropriate for labels to be above everything, including tokens. Above
     * drawables, yes. Above tokens, no. (Although in that case labels could be completely obscured.
     * Hm.)
     */
    // Drawing labels is slooooow. :(
    // Perhaps we should draw the fog first and use hard fog to determine whether labels need to be
    // drawn?
    // (This method has it's own 'timer' calls)
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
      List<Token> sortedTokens = new ArrayList<Token>(tokens);
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
      // of, the tokens
      // themselves.
      // So if one moving token is on top of another moving token, at least the textual identifiers
      // will be
      // visible.
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
    if (!gridRenderer.isInitialised()) gridRenderer.setRenderer(this);
    gridRenderer.renderCoordinates(g2d, view);
    timer.stop("renderCoordinates");

    timer.start("lightSourceIconOverlay.paintOverlay");
    if (shouldRenderLayer(Zone.Layer.TOKEN, view)
        && view.isGMView()
        && AppState.isShowLightSources()) {
      lightSourceIconOverlay.paintOverlay(this, g2d);
    }
    timer.stop("lightSourceIconOverlay.paintOverlay");

    debugRenderer.renderShapes(g2d, Arrays.asList(shape, shape2));
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
      var flabel = labelRenderFactory.getMapImageLabel(label);
      ZonePoint zp = new ZonePoint(label.getX(), label.getY());
      if (!zone.isPointVisible(zp, view)) {
        continue;
      }
      timer.start("labels-1.1");
      ScreenPoint sp = ScreenPoint.fromZonePointRnd(this, zp.x, zp.y);
      var dim = flabel.getDimensions(g, label.getLabel());
      Rectangle bounds =
          flabel.render(
              g, (int) (sp.x - dim.width / 2), (int) (sp.y - dim.height / 2), label.getLabel());
      labelLocationList.add(new LabelLocation(bounds, label));
      timer.stop("labels-1.1");
    }
    timer.stop("labels-1");
  }

  public boolean isLoading() {
    if (isLoaded) {
      // We're done, until the cache is cleared
      return false;
    }
    // Get a list of all the assets in the zone
    Set<MD5Key> assetSet = zone.getAllAssetIds();
    assetSet.remove(null); // remove bad data

    // Make sure they are loaded
    int downloadCount = 0;
    int cacheCount = 0;
    boolean loaded = true;
    for (MD5Key id : assetSet) {
      // Have we gotten the actual data yet ?
      Asset asset = AssetManager.getAsset(id);
      if (asset == null) {
        AssetManager.getAssetAsynchronously(id);
        loaded = false;
        continue;
      }
      downloadCount++;

      // Have we loaded the image into memory yet ?
      Image image = ImageManager.getImage(asset.getMD5Key(), this);
      if (image == null || image == ImageManager.TRANSFERING_IMAGE) {
        loaded = false;
        continue;
      }
      cacheCount++;
    }
    loadingProgress =
        String.format(
            " Loading Map '%s' - %d/%d Loaded %d/%d Cached",
            zone.getDisplayName(), downloadCount, assetSet.size(), cacheCount, assetSet.size());
    isLoaded = loaded;
    if (isLoaded) {
      // Notify the token tree that it should update
      MapTool.getFrame().updateTokenTree();

      new MapToolEventBus().getMainEventBus().post(new ZoneLoaded(zone));
    }
    return !isLoaded;
  }

  protected void renderDrawableOverlay(
      Graphics g, DrawableRenderer renderer, PlayerView view, List<DrawnElement> drawnElements) {
    Rectangle viewport =
        new Rectangle(
            zoneScale.getOffsetX(), zoneScale.getOffsetY(), getSize().width, getSize().height);
    // List<DrawnElement> list = new ArrayList<DrawnElement>();
    // list.addAll(drawnElements);

    renderer.renderDrawables(g, drawnElements, viewport, getScale());
  }

  protected void renderBoard(Graphics2D g, PlayerView view) {
    Dimension size = getSize();
    if (backbuffer == null
        || backbuffer.getWidth() != size.width
        || backbuffer.getHeight() != size.height) {
      backbuffer = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
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
      Graphics2D bbg = backbuffer.createGraphics();
      AppPreferences.getRenderQuality().setRenderingHints(bbg);

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
            (int) (mapImage.getWidth() * scaleFactor),
            (int) (mapImage.getHeight() * scaleFactor),
            null);
      }
      bbg.dispose();
      drawBackground = false;
    }
    lastX = scale.getOffsetX();
    lastY = scale.getOffsetY();
    lastScale = scale.getScale();

    g.drawImage(backbuffer, 0, 0, this);
  }

  private Set<SelectionSet> getOwnedMovementSet(PlayerView view) {
    Set<SelectionSet> movementSet = new HashSet<SelectionSet>();
    for (SelectionSet selection : selectionSetMap.values()) {
      if (selection.getPlayerId().equals(MapTool.getPlayer().getName())) {
        movementSet.add(selection);
      }
    }
    return movementSet;
  }

  private Set<SelectionSet> getUnOwnedMovementSet(PlayerView view) {
    Set<SelectionSet> movementSet = new HashSet<SelectionSet>();
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

    double scale = zoneScale.getScale();
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

        // Don't bother if it's not visible
        if (!token.isVisible() && !view.isGMView()) {
          continue;
        }

        // ... or if it's visible only to the owner and that's not us!
        final boolean isOwner = view.isGMView() || AppUtil.playerOwns(token);
        if (token.isVisibleOnlyToOwner() && !isOwner) {
          continue;
        }

        // ... or if it doesn't have an image to display. (Hm, should still show *something*?)
        Asset asset = AssetManager.getAsset(token.getImageAssetId());
        if (asset == null) {
          continue;
        }

        // OPTIMIZE: combine this with the code in renderTokens()
        Rectangle footprintBounds = token.getBounds(zone);
        ScreenPoint newScreenPoint =
            ScreenPoint.fromZonePoint(
                this, footprintBounds.x + set.getOffsetX(), footprintBounds.y + set.getOffsetY());

        // get token image, using image table if present
        BufferedImage image = getTokenImage(token);

        int scaledWidth = (int) (footprintBounds.width * scale);
        int scaledHeight = (int) (footprintBounds.height * scale);

        // Tokens are centered on the image center point
        int x = (int) (newScreenPoint.x);
        int y = (int) (newScreenPoint.y);

        // Show path only on the key token on token layer that are visible to the owner or gm while
        // fow and vision is on
        if (token == keyToken && token.getLayer().supportsWalker()) {
          renderPath(
              g,
              walker != null ? walker.getPath() : set.gridlessPath,
              token.getFootprint(zone.getGrid()));
        }

        // Show current Blocked Movement directions for A*
        if (walker != null && DeveloperOptions.Toggle.ShowAiDebugging.isEnabled()) {
          Map<CellPoint, Set<CellPoint>> blockedMovesByTarget = walker.getBlockedMoves();
          // Color currentColor = g.getColor();
          for (var entry : blockedMovesByTarget.entrySet()) {
            var position = entry.getKey();
            var blockedMoves = entry.getValue();

            for (CellPoint point : blockedMoves) {
              ZonePoint zp = point.midZonePoint(getZone().getGrid(), position);
              double r = (zp.x - 1) * 45;
              showBlockedMoves(
                  g, zp, r, RessourceManager.getImage(Images.ZONE_RENDERER_BLOCK_MOVE), 1.0f);
            }
          }
        }
        // handle flipping
        BufferedImage workImage = image;
        if (token.isFlippedX() || token.isFlippedY()) {
          workImage =
              new BufferedImage(image.getWidth(), image.getHeight(), image.getTransparency());

          int workW = image.getWidth() * (token.isFlippedX() ? -1 : 1);
          int workH = image.getHeight() * (token.isFlippedY() ? -1 : 1);
          int workX = token.isFlippedX() ? image.getWidth() : 0;
          int workY = token.isFlippedY() ? image.getHeight() : 0;

          Graphics2D wig = workImage.createGraphics();
          wig.drawImage(image, workX, workY, workW, workH, null);
          wig.dispose();
        }
        // on the iso plane
        if (token.isFlippedIso()) {
          if (flipIsoImageMap.get(token) == null) {
            workImage = IsometricGrid.isoImage(workImage);
          } else {
            workImage = flipIsoImageMap.get(token);
          }
          token.setHeight(workImage.getHeight());
          token.setWidth(workImage.getWidth());
          footprintBounds = token.getBounds(zone);
        }
        // Draw token
        double iso_ho = 0;
        Dimension imgSize = new Dimension(workImage.getWidth(), workImage.getHeight());
        if (token.getShape() == TokenShape.FIGURE) {
          double th = token.getHeight() * (double) footprintBounds.width / token.getWidth();
          iso_ho = footprintBounds.height - th;
          footprintBounds =
              new Rectangle(
                  footprintBounds.x,
                  footprintBounds.y - (int) iso_ho,
                  footprintBounds.width,
                  (int) th);
          iso_ho = iso_ho * getScale();
        }
        SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);

        int offsetx = 0;
        int offsety = 0;
        if (token.isSnapToScale()) {
          offsetx =
              (int)
                  (imgSize.width < footprintBounds.width
                      ? (footprintBounds.width - imgSize.width) / 2 * getScale()
                      : 0);
          offsety =
              (int)
                  (imgSize.height < footprintBounds.height
                      ? (footprintBounds.height - imgSize.height) / 2 * getScale()
                      : 0);
        }
        int tx = x + offsetx;
        int ty = y + offsety + (int) iso_ho;

        AffineTransform at = new AffineTransform();
        at.translate(tx, ty);

        if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
          at.rotate(
              Math.toRadians(-token.getFacing() - 90),
              scaledWidth / 2 - token.getAnchor().x * scale - offsetx,
              scaledHeight / 2
                  - token.getAnchor().y * scale
                  - offsety); // facing defaults to down, or -90 degrees
        }
        if (token.isSnapToScale()) {
          at.scale(
              (double) imgSize.width / workImage.getWidth(),
              (double) imgSize.height / workImage.getHeight());
          at.scale(getScale(), getScale());
        } else {
          if (token.getShape() == TokenShape.FIGURE) {
            at.scale(
                (double) scaledWidth / workImage.getWidth(),
                (double) scaledWidth / workImage.getWidth());
          } else {
            at.scale(
                (double) scaledWidth / workImage.getWidth(),
                (double) scaledHeight / workImage.getHeight());
          }
        }

        g.drawImage(workImage, at, this);

        // Other details.
        // If the token is visible on the screen it will be in the location cache
        if (token == keyToken
            && (isOwner || shouldShowMovementLabels(token, set, clearArea))
            && tokenLocationCache.containsKey(token)) {
          var labelY = y + 10 + scaledHeight;
          var labelX = x + scaledWidth / 2;

          if (token.getLayer().supportsWalker() && AppState.getShowMovementMeasurements()) {
            double distanceTraveled = calculateTraveledDistance(set);
            if (distanceTraveled >= 0) {
              String distance = NumberFormat.getInstance().format(distanceTraveled);
              delayRendering(new LabelRenderer(this, distance, labelX, labelY));
              labelY += 20;
            }
          }
          if (set.getPlayerId() != null && set.getPlayerId().length() >= 1) {
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
      final var path = set.gridlessPath;
      if (path.getCellPath().isEmpty()) {
        return false;
      }
      final var lastPoint = path.getCellPath().getLast();
      Rectangle tokBounds = token.getBounds(zone);
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
    for (ZonePoint zp : set.gridlessPath.getCellPath()) {
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

    // log.info("Rendering path..." + System.currentTimeMillis());

    Rectangle footprintBounds = footprint.getBounds(grid);
    if (path.getCellPath().get(0) instanceof CellPoint) {
      timer.start("renderPath-1");
      CellPoint previousPoint = null;
      Point previousHalfPoint = null;

      Path<CellPoint> pathCP = (Path<CellPoint>) path;
      List<CellPoint> cellPath = pathCP.getCellPath();

      Set<CellPoint> pathSet = new HashSet<CellPoint>();
      List<ZonePoint> waypointList = new LinkedList<ZonePoint>();
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
      if (waypointList.size() > 0) {
        waypointList.remove(waypointList.size() - 1);
      }
      timer.stop("renderPath-1");
      // log.info("pathSet size: " + pathSet.size());

      timer.start("renderPath-2");
      Dimension cellOffset = zone.getGrid().getCellOffset();
      for (CellPoint p : pathSet) {
        ZonePoint zp = grid.convert(p);
        zp.x += grid.getCellWidth() / 2 + cellOffset.width;
        zp.y += grid.getCellHeight() / 2 + cellOffset.height;
        highlightCell(g, zp, grid.getCellHighlight(), 1.0f);
      }
      if (AppState.getShowMovementMeasurements()) {
        double cellAdj = grid.isHex() ? 2.5 : 2;
        for (CellPoint p : cellPath) {
          ZonePoint zp = grid.convert(p);
          zp.x += grid.getCellWidth() / cellAdj + cellOffset.width;
          zp.y += grid.getCellHeight() / cellAdj + cellOffset.height;
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

            int halfx = (int) ((origin.x + destination.x) / 2);
            int halfy = (int) ((origin.y + destination.y) / 2);
            Point halfPoint = new Point(halfx, halfy);

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
      // Zone point/gridless path

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
                  zp.x + (footprintBounds.width / 2) * footprint.getScale(),
                  zp.y + (footprintBounds.height / 2) * footprint.getScale());
          continue;
        }
        ScreenPoint nextPoint =
            ScreenPoint.fromZonePoint(
                this,
                zp.x + (footprintBounds.width / 2) * footprint.getScale(),
                zp.y + (footprintBounds.height / 2) * footprint.getScale());

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
        if (p == pathList.get(pathList.size() - 1)) {
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

  public void drawText(String text, int x, int y) {
    Graphics g = this.getGraphics();

    Grid grid = zone.getGrid();
    double cwidth = grid.getCellWidth() * getScale();
    double cheight = grid.getCellHeight() * getScale();

    double iwidth = cwidth;
    double iheight = cheight;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, x, y);

    int cellX = (int) (sp.x - iwidth / 2);
    int cellY = (int) (sp.y - iheight / 2);

    // Draw distance for each cell
    int fontSize = (int) (getScale() * 12);
    int textOffset = (int) (getScale() * 7); // 7 pixels at 100% zoom

    Font font = new Font(Font.DIALOG, Font.BOLD, fontSize);
    Font originalFont = g.getFont();

    FontMetrics fm = g.getFontMetrics(font);
    int textWidth = fm.stringWidth(text);

    g.setFont(font);
    g.setColor(Color.BLACK);

    // log.info("Text: [" + text + "], width: " + textWidth + ", font size: " + fontSize + ",
    // offset: " + textOffset);

    g.drawString(
        text,
        (int) (cellX + cwidth - textWidth - textOffset),
        (int) (cellY + cheight - textOffset));
    g.setFont(originalFont);
  }

  private Shape shape;
  private Shape shape2;

  public void setShape(Shape shape) {
    if (shape == null) {
      return;
    }

    this.shape = shape;
  }

  public void setShape2(Shape shape) {
    if (shape == null) {
      return;
    }

    this.shape2 = shape;
  }

  public void showBlockedMoves(
      Graphics2D g, ZonePoint point, double angle, BufferedImage image, float size) {
    // Resize image to size of 1/4 size of grid
    double resizeWidth = zone.getGrid().getCellWidth() / image.getWidth() * .25;
    double resizeHeight = zone.getGrid().getCellHeight() / image.getHeight() * .25;

    double cwidth = image.getWidth() * getScale() * resizeWidth;
    double cheight = image.getHeight() * getScale() * resizeHeight;

    double iwidth = cwidth * size;
    double iheight = cheight * size;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, point);

    AffineTransform backup = g.getTransform();

    g.drawImage(
        image,
        (int) (sp.x - iwidth / 2),
        (int) (sp.y - iheight / 2),
        (int) iwidth,
        (int) iheight,
        this);
    g.setTransform(backup);
  }

  public void highlightCell(Graphics2D g, ZonePoint point, BufferedImage image, float size) {
    Grid grid = zone.getGrid();
    double cwidth = grid.getCellWidth() * getScale();
    double cheight = grid.getCellHeight() * getScale();

    double iwidth = cwidth * size;
    double iheight = cheight * size;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, point);

    g.drawImage(
        image,
        (int) (sp.x - iwidth / 2),
        (int) (sp.y - iheight / 2),
        (int) iwidth,
        (int) iheight,
        this);
  }

  public void addDistanceText(
      Graphics2D g, ZonePoint point, float size, double distance, double distanceWithoutTerrain) {
    if (distance == 0) {
      return;
    }

    Grid grid = zone.getGrid();
    double cwidth = grid.getCellWidth() * getScale();
    double cheight = grid.getCellHeight() * getScale();

    double iwidth = cwidth * size;
    double iheight = cheight * size;

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, point);

    int cellX = (int) (sp.x - iwidth / 2);
    int cellY = (int) (sp.y - iheight / 2);

    // Draw distance for each cell
    double fontScale = (double) grid.getSize() / 50; // Font size of 12 at grid size 50 is default
    int fontSize = (int) (getScale() * 12 * fontScale);
    int textOffset = (int) (getScale() * 7 * fontScale); // 7 pixels at 100% zoom & grid size of 50

    String distanceText = NumberFormat.getInstance().format(distance);
    if (DeveloperOptions.Toggle.ShowAiDebugging.isEnabled()) {
      distanceText += " (" + NumberFormat.getInstance().format(distanceWithoutTerrain) + ")";
      fontSize = (int) (fontSize * 0.75);
    }

    Font font = new Font(Font.DIALOG, Font.BOLD, fontSize);
    Font originalFont = g.getFont();

    FontMetrics fm = g.getFontMetrics(font);
    int textWidth = fm.stringWidth(distanceText);

    g.setFont(font);
    g.setColor(Color.BLACK);

    // log.info("Text: [" + distanceText + "], width: " + textWidth + ", font size: " + fontSize +
    // ", offset: " + textOffset + ", fontScale: " + fontScale+ ", getScale(): " + getScale());

    g.drawString(
        distanceText,
        (int) (cellX + cwidth - textWidth - textOffset),
        (int) (cellY + cheight - textOffset));
    g.setFont(originalFont);
  }

  /**
   * Get a list of tokens currently visible on the screen. The list is ordered by location starting
   * in the top left and going to the bottom right.
   *
   * @return the token list
   */
  public List<Token> getTokensOnScreen() {
    List<Token> list = new ArrayList<Token>();

    // Always assume tokens, for now
    List<TokenLocation> tokenLocationListCopy =
        new ArrayList<TokenLocation>(getTokenLocations(getActiveLayer()));
    for (TokenLocation location : tokenLocationListCopy) {
      list.add(location.token);
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
   * Get the token locations for the given layer, creates an empty list if there are not locations
   * for the given layer
   */
  private List<TokenLocation> getTokenLocations(Zone.Layer layer) {
    return tokenLocationMap.computeIfAbsent(layer, k -> new LinkedList<>());
  }

  // TODO: I don't like this hardwiring
  protected Shape getFigureFacingArrow(int angle, int size) {
    int base = (int) (size * .75);
    int width = (int) (size * .35);

    facingArrow = new GeneralPath();
    facingArrow.moveTo(base, -width);
    facingArrow.lineTo(size, 0);
    facingArrow.lineTo(base, width);
    facingArrow.lineTo(base, -width);

    GeneralPath gp =
        (GeneralPath)
            facingArrow.createTransformedShape(
                AffineTransform.getRotateInstance(-Math.toRadians(angle)));
    return gp.createTransformedShape(AffineTransform.getScaleInstance(getScale(), getScale() / 2));
  }

  // TODO: I don't like this hardwiring
  protected Shape getCircleFacingArrow(int angle, int size) {
    int base = (int) (size * .75);
    int width = (int) (size * .35);

    facingArrow = new GeneralPath();
    facingArrow.moveTo(base, -width);
    facingArrow.lineTo(size, 0);
    facingArrow.lineTo(base, width);
    facingArrow.lineTo(base, -width);

    GeneralPath gp =
        (GeneralPath)
            facingArrow.createTransformedShape(
                AffineTransform.getRotateInstance(-Math.toRadians(angle)));
    return gp.createTransformedShape(AffineTransform.getScaleInstance(getScale(), getScale()));
  }

  // TODO: I don't like this hardwiring
  protected Shape getSquareFacingArrow(int angle, int size) {
    int base = (int) (size * .75);
    int width = (int) (size * .35);

    facingArrow = new GeneralPath();
    facingArrow.moveTo(0, 0);
    facingArrow.lineTo(-(size - base), -width);
    facingArrow.lineTo(-(size - base), width);
    facingArrow.lineTo(0, 0);

    GeneralPath gp =
        (GeneralPath)
            facingArrow.createTransformedShape(
                AffineTransform.getRotateInstance(-Math.toRadians(angle)));
    return gp.createTransformedShape(AffineTransform.getScaleInstance(getScale(), getScale()));
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
        // TODO Should we actually check zoneView.isUsingVision() for parity with later checks?
        && visibleScreenArea != null
        && !tokenList.isEmpty()
        && tokenList.get(0).getLayer().supportsVision()) {
      clippedG = (Graphics2D) g.create();

      Area visibleArea = new Area(g.getClipBounds());
      visibleArea.intersect(visibleScreenArea);
      clippedG.setClip(new GeneralPath(visibleArea));
      AppPreferences.getRenderQuality().setRenderingHints(clippedG);
    }
    timer.stop("createClip");

    // This is in screen coordinates
    Rectangle viewport = new Rectangle(0, 0, getSize().width, getSize().height);

    Rectangle clipBounds = g.getClipBounds();
    double scale = zoneScale.getScale();
    Set<GUID> tempVisTokens = new HashSet<GUID>();

    // calculations
    boolean calculateStacks =
        !tokenList.isEmpty() && tokenList.get(0).getLayer().isTokenLayer() && tokenStackMap == null;
    if (calculateStacks) {
      tokenStackMap = new HashMap<Token, Set<Token>>();
    }

    // TODO: I (Craig) have commented out the clearing of the tokenLocationCache.clear() for now as
    // it introduced a more serious bug with resizing.

    // Clearing the cache here removes a bug in which campaigns are not initially drawn. Why?
    // Is that because the rendering pipeline thinks they've already been drawn so isn't forced to
    // re-render them? So how does this cache get filled then? It's not part of the campaign
    // state...
    // tokenLocationCache.clear();

    List<Token> tokenPostProcessing = new ArrayList<Token>(tokenList.size());
    for (Token token : tokenList) {
      if (token.getShape() != Token.TokenShape.FIGURE && figuresOnly && !token.isAlwaysVisible()) {
        continue;
      }

      timer.start("tokenlist-1");
      try {
        if (token.getLayer().isStampLayer() && isTokenMoving(token)) {
          continue;
        }
        // Don't bother if it's not visible
        // NOTE: Not going to use zone.isTokenVisible as it is very slow. In fact, it's faster
        // to just draw the tokens and let them be clipped
        if ((!token.isVisible() || !token.getLayer().isVisibleToPlayers()) && !isGMView) {
          continue;
        }
        if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
          continue;
        }
      } finally {
        // This ensures that the timer is always stopped
        timer.stop("tokenlist-1");
      }
      timer.start("tokenlist-1.1");
      TokenLocation location = tokenLocationCache.get(token);
      if (location != null && !location.maybeOnscreen(viewport)) {
        timer.stop("tokenlist-1.1");
        continue;
      }
      timer.stop("tokenlist-1.1");

      timer.start("tokenlist-1a");
      Rectangle footprintBounds = token.getBounds(zone);
      timer.stop("tokenlist-1a");

      timer.start("tokenlist-1b");
      // get token image, using image table if present
      BufferedImage image = getTokenImage(token);
      timer.stop("tokenlist-1b");

      timer.start("tokenlist-1c");
      double scaledWidth = (footprintBounds.width * scale);
      double scaledHeight = (footprintBounds.height * scale);

      // if (!token.isStamp()) {
      // // Fit inside the grid
      // scaledWidth --;
      // scaledHeight --;
      // }

      ScreenPoint tokenScreenLocation =
          ScreenPoint.fromZonePoint(this, footprintBounds.x, footprintBounds.y);
      timer.stop("tokenlist-1c");

      timer.start("tokenlist-1d");
      // Tokens are centered on the image center point
      double x = tokenScreenLocation.x;
      double y = tokenScreenLocation.y;

      Rectangle2D origBounds = new Rectangle2D.Double(x, y, scaledWidth, scaledHeight);
      Area tokenBounds = new Area(origBounds);
      if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
        double sx = scaledWidth / 2 + x - (token.getAnchor().x * scale);
        double sy = scaledHeight / 2 + y - (token.getAnchor().y * scale);
        tokenBounds.transform(
            AffineTransform.getRotateInstance(
                Math.toRadians(-token.getFacing() - 90), sx, sy)); // facing
        // defaults
        // to
        // down,
        // or
        // -90
        // degrees
      }
      timer.stop("tokenlist-1d");

      timer.start("tokenlist-1e");
      try {
        location =
            new TokenLocation(
                this,
                tokenBounds,
                origBounds,
                token,
                x,
                y,
                footprintBounds.width,
                footprintBounds.height,
                scaledWidth,
                scaledHeight);
        tokenLocationCache.put(token, location);
        // Too small ?
        if (location.scaledHeight < 1 || location.scaledWidth < 1) {
          continue;
        }
        // Vision visibility
        if (!isGMView && token.getLayer().supportsVision() && zoneView.isUsingVision()) {
          if (!GraphicsUtil.intersects(visibleScreenArea, location.bounds)) {
            continue;
          }
        }
      } finally {
        // This ensures that the timer is always stopped
        timer.stop("tokenlist-1e");
      }
      // Markers
      timer.start("renderTokens:Markers");
      // System.out.println("Token " + token.getName() + " is a marker? " + token.isMarker());
      if (token.isMarker() && canSeeMarker(token)) {
        markerLocationList.add(location);
      }
      timer.stop("renderTokens:Markers");

      // Stacking check
      if (calculateStacks) {
        timer.start("tokenStack");
        // System.out.println(token.getName() + " - " + location.boundsCache);

        Set<Token> tokenStackSet = null;
        for (TokenLocation currLocation : getTokenLocations(Zone.Layer.TOKEN)) {
          // Are we covering anyone ?
          // System.out.println("\t" + currLocation.token.getName() + " - " +
          // location.boundsCache.contains(currLocation.boundsCache));
          if (location.boundsCache.contains(currLocation.boundsCache)) {
            if (tokenStackSet == null) {
              tokenStackSet = new HashSet<Token>();
              // Sometimes got NPE here
              if (tokenStackMap == null) {
                tokenStackMap = new HashMap<Token, Set<Token>>();
              }
              tokenStackMap.put(token, tokenStackSet);
              tokenStackSet.add(token);
            }
            tokenStackSet.add(currLocation.token);

            if (tokenStackMap.get(currLocation.token) != null) {
              tokenStackSet.addAll(tokenStackMap.get(currLocation.token));
              tokenStackMap.remove(currLocation.token);
            }
          }
        }
        timer.stop("tokenStack");
      }

      // Keep track of the location on the screen
      // Note the order -- the top most token is at the end of the list
      timer.start("renderTokens:Locations");
      Zone.Layer layer = token.getLayer();
      List<TokenLocation> locationList = getTokenLocations(layer);
      if (locationList != null) {
        locationList.add(location);
      }
      timer.stop("renderTokens:Locations");

      // Add the token to our visible set.
      tempVisTokens.add(token.getId());

      // Only draw if we're visible
      // NOTE: this takes place AFTER resizing the image, that's so that the user
      // suffers a pause only once while scaling, and not as new tokens are
      // scrolled onto the screen
      timer.start("renderTokens:OnscreenCheck");
      if (!location.bounds.intersects(clipBounds)) {
        timer.stop("renderTokens:OnscreenCheck");
        continue;
      }
      timer.stop("renderTokens:OnscreenCheck");

      // create a per token Graphics object - normally clipped, unless always visible
      Area tokenCellArea = zone.getGrid().getTokenCellArea(tokenBounds);
      Graphics2D tokenG;
      if (isTokenInNeedOfClipping(token, tokenCellArea, isGMView)) {
        tokenG = (Graphics2D) clippedG.create();
      } else {
        tokenG = (Graphics2D) g.create();
        AppPreferences.getRenderQuality().setRenderingHints(tokenG);
      }

      // Previous path
      timer.start("renderTokens:ShowPath");
      if (showPathList.contains(token) && token.getLastPath() != null) {
        renderPath(g, token.getLastPath(), token.getFootprint(zone.getGrid()));
      }
      timer.stop("renderTokens:ShowPath");

      timer.start("tokenlist-5");
      // handle flipping
      BufferedImage workImage = image;
      if (token.isFlippedX() || token.isFlippedY()) {
        workImage = flipImageMap.get(token);
        if (workImage == null) {
          workImage =
              new BufferedImage(image.getWidth(), image.getHeight(), image.getTransparency());

          int workW = image.getWidth() * (token.isFlippedX() ? -1 : 1);
          int workH = image.getHeight() * (token.isFlippedY() ? -1 : 1);
          int workX = token.isFlippedX() ? image.getWidth() : 0;
          int workY = token.isFlippedY() ? image.getHeight() : 0;

          Graphics2D wig = workImage.createGraphics();
          wig.drawImage(image, workX, workY, workW, workH, null);
          wig.dispose();

          flipImageMap.put(token, workImage);
        }
      }
      timer.stop("tokenlist-5");

      timer.start("tokenlist-5a");
      if (token.isFlippedIso()) {
        if (flipIsoImageMap.get(token) == null) {
          workImage = IsometricGrid.isoImage(workImage);
          flipIsoImageMap.put(token, workImage);
        } else {
          workImage = flipIsoImageMap.get(token);
        }
        token.setHeight(workImage.getHeight());
        token.setWidth(workImage.getWidth());
        footprintBounds = token.getBounds(zone);
      }
      timer.stop("tokenlist-5a");

      timer.start("tokenlist-6");
      // Position
      // For Isometric Grid we alter the height offset
      double iso_ho = 0;
      Dimension imgSize = new Dimension(workImage.getWidth(), workImage.getHeight());
      if (token.getShape() == TokenShape.FIGURE) {
        double th = token.getHeight() * (double) footprintBounds.width / token.getWidth();
        iso_ho = footprintBounds.height - th;
        footprintBounds =
            new Rectangle(
                footprintBounds.x,
                footprintBounds.y - (int) iso_ho,
                footprintBounds.width,
                (int) th);
        iso_ho = iso_ho * getScale();
      }
      SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);

      int offsetx = 0;
      int offsety = 0;

      if (token.isSnapToScale()) {
        offsetx =
            (int)
                (imgSize.width < footprintBounds.width
                    ? (footprintBounds.width - imgSize.width) / 2 * getScale()
                    : 0);
        offsety =
            (int)
                (imgSize.height < footprintBounds.height
                    ? (footprintBounds.height - imgSize.height) / 2 * getScale()
                    : 0);
      }
      double tx = location.x + offsetx;
      double ty = location.y + offsety + iso_ho;

      AffineTransform at = new AffineTransform();
      at.translate(tx, ty);

      // Rotated
      if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
        // Jamz: Test, rotate on NW corner
        // at.rotate(Math.toRadians(token.getFacingInDegrees()), (token.getAnchor().x * scale) -
        // offsetx,
        // (token.getAnchor().y * scale) - offsety);

        at.rotate(
            Math.toRadians(-token.getFacing() - 90),
            location.scaledWidth / 2 - (token.getAnchor().x * scale) - offsetx,
            location.scaledHeight / 2 - (token.getAnchor().y * scale) - offsety);
        // facing defaults to down, or -90 degrees
      }
      // Snap
      if (token.isSnapToScale()) {
        at.scale(
            ((double) imgSize.width) / workImage.getWidth(),
            ((double) imgSize.height) / workImage.getHeight());
        at.scale(getScale(), getScale());
      } else {
        if (token.getShape() == TokenShape.FIGURE) {
          at.scale(scaledWidth / workImage.getWidth(), scaledWidth / workImage.getWidth());
        } else {
          at.scale(scaledWidth / workImage.getWidth(), scaledHeight / workImage.getHeight());
        }
      }
      timer.stop("tokenlist-6");

      // Render Halo
      haloRenderer.renderHalo(tokenG, token, location);

      // Calculate alpha Transparency from token and use opacity for indicating that token is moving
      float opacity = token.getTokenOpacity();
      if (isTokenMoving(token)) opacity = opacity / 2.0f;

      // Finally render the token image
      timer.start("tokenlist-7");
      if (!isGMView && zoneView.isUsingVision() && (token.getShape() == Token.TokenShape.FIGURE)) {
        Area cb = zone.getGrid().getTokenCellArea(tokenBounds);
        if (GraphicsUtil.intersects(visibleScreenArea, cb)) {
          // the cell intersects visible area so
          if (zone.getGrid().checkCenterRegion(cb.getBounds(), visibleScreenArea)) {
            // if we can see the centre, draw the whole token
            Composite oldComposite = tokenG.getComposite();
            if (opacity < 1.0f) {
              tokenG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            }
            tokenG.drawImage(workImage, at, this);
            tokenG.setComposite(oldComposite);
            // g.draw(cb); // debugging
          } else {
            // else draw the clipped token
            Area cellArea = new Area(visibleScreenArea);
            cellArea.intersect(cb);
            tokenG.setClip(cellArea);
            Composite oldComposite = tokenG.getComposite();
            if (opacity < 1.0f) {
              tokenG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            }
            tokenG.drawImage(workImage, at, this);
            tokenG.setComposite(oldComposite);
          }
        }
      } else if (!isGMView && zoneView.isUsingVision() && token.isAlwaysVisible()) {
        // Jamz: Always Visible tokens will get rendered again here to place on top of FoW
        Area cb = zone.getGrid().getTokenCellArea(tokenBounds);
        if (GraphicsUtil.intersects(visibleScreenArea, cb)) {
          // if we can see a portion of the stamp/token, draw the whole thing, defaults to 2/9ths
          if (zone.getGrid()
              .checkRegion(cb.getBounds(), visibleScreenArea, token.getAlwaysVisibleTolerance())) {
            Composite oldComposite = tokenG.getComposite();
            if (opacity < 1.0f) {
              tokenG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            }
            tokenG.drawImage(workImage, at, this);
            tokenG.setComposite(oldComposite);
          } else {
            // else draw the clipped stamp/token
            // This will only show the part of the token that does not have VBL on it
            // as any VBL on the token will block LOS, affecting the clipping.
            Area cellArea = new Area(visibleScreenArea);
            cellArea.intersect(cb);
            tokenG.setClip(cellArea);
            Composite oldComposite = tokenG.getComposite();
            if (opacity < 1.0f) {
              tokenG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            }
            tokenG.drawImage(workImage, at, this);
            tokenG.setComposite(oldComposite);
          }
        }
      } else {
        // fallthrough normal token rendered against visible area
        Composite oldComposite = tokenG.getComposite();
        if (opacity < 1.0f) {
          tokenG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        }
        tokenG.drawImage(workImage, at, this);
        tokenG.setComposite(oldComposite);
      }
      timer.stop("tokenlist-7");

      timer.start("tokenlist-8");
      // Halo (SQUARE)
      // XXX Why are square halos drawn separately?!
      /*
       * if (token.hasHalo() && token.getShape() == Token.TokenShape.SQUARE) { Stroke oldStroke = g.getStroke(); clippedG.setStroke(new BasicStroke(AppPreferences.getHaloLineWidth()));
       * clippedG.setColor(token.getHaloColor()); clippedG.draw(new Rectangle2D.Double(location.x, location.y, location.scaledWidth, location.scaledHeight)); clippedG.setStroke(oldStroke); }
       */

      // Facing ?
      // TODO: Optimize this by doing it once per token per facing
      if (token.hasFacing()) {
        Token.TokenShape tokenType = token.getShape();
        switch (tokenType) {
          case FIGURE:
            if (token.getHasImageTable()
                && token.hasFacing()
                && AppPreferences.getForceFacingArrow() == false) {
              break;
            }
            Shape arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);

            if (!zone.getGrid().isIsometric()) {
              arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
            }

            double fx = location.x + location.scaledWidth / 2;
            double fy = location.y + location.scaledHeight / 2;

            tokenG.translate(fx, fy);
            if (token.getFacing() < 0) {
              tokenG.setColor(Color.yellow);
            } else {
              tokenG.setColor(ZoneRendererConstants.TRANSLUCENT_YELLOW);
            }
            tokenG.fill(arrow);
            tokenG.setColor(Color.darkGray);
            tokenG.draw(arrow);
            tokenG.translate(-fx, -fy);
            break;
          case TOP_DOWN:
            if (AppPreferences.getForceFacingArrow() == false) {
              break;
            }
          case CIRCLE:
            arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
            if (zone.getGrid().isIsometric()) {
              arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);
            }

            double cx = location.x + location.scaledWidth / 2;
            double cy = location.y + location.scaledHeight / 2;

            tokenG.translate(cx, cy);
            tokenG.setColor(Color.yellow);
            tokenG.fill(arrow);
            tokenG.setColor(Color.darkGray);
            tokenG.draw(arrow);
            tokenG.translate(-cx, -cy);
            break;
          case SQUARE:
            if (zone.getGrid().isIsometric()) {
              arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);
              cx = location.x + location.scaledWidth / 2;
              cy = location.y + location.scaledHeight / 2;
            } else {
              int facing = token.getFacing();
              while (facing < 0) {
                facing += 360;
              } // TODO: this should really be done in Token.setFacing() but I didn't want to take
              // the chance
              // of breaking something, so change this when it's safe to break stuff
              facing %= 360;
              arrow = getSquareFacingArrow(facing, footprintBounds.width / 2);

              cx = location.x + location.scaledWidth / 2;
              cy = location.y + location.scaledHeight / 2;

              // Find the edge of the image
              // TODO: Man, this is horrible, there's gotta be a better way to do this
              double xp = location.scaledWidth / 2;
              double yp = location.scaledHeight / 2;
              if (facing >= 45 && facing <= 135 || facing >= 225 && facing <= 315) {
                xp = (int) (yp / Math.tan(Math.toRadians(facing)));
                if (facing > 180) {
                  xp = -xp;
                  yp = -yp;
                }
              } else {
                yp = (int) (xp * Math.tan(Math.toRadians(facing)));
                if (facing > 90 && facing < 270) {
                  xp = -xp;
                  yp = -yp;
                }
              }
              cx += xp;
              cy -= yp;
            }

            tokenG.translate(cx, cy);
            tokenG.setColor(Color.yellow);
            tokenG.fill(arrow);
            tokenG.setColor(Color.darkGray);
            tokenG.draw(arrow);
            tokenG.translate(-cx, -cy);
            break;
        }
      }
      timer.stop("tokenlist-8");

      timer.start("tokenlist-9");
      // Set up the graphics so that the overlay can just be painted.
      Graphics2D locg =
          (Graphics2D)
              tokenG.create(
                  (int) tokenBounds.getBounds().getX(),
                  (int) tokenBounds.getBounds().getY(),
                  (int) tokenBounds.getBounds().getWidth(),
                  (int) tokenBounds.getBounds().getHeight());
      Rectangle bounds =
          new Rectangle(
              0,
              0,
              (int) tokenBounds.getBounds().getWidth(),
              (int) tokenBounds.getBounds().getHeight());

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
        overlay.paintOverlay(locg, token, bounds, stateValue);
      }
      timer.stop("tokenlist-9");

      timer.start("tokenlist-10");

      for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
        Object barValue = token.getState(bar);
        BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
        if (overlay == null
            || overlay.isMouseover() && token != tokenUnderMouse
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }

        overlay.paintOverlay(locg, token, bounds, barValue);
      } // endfor
      locg.dispose();
      timer.stop("tokenlist-10");

      timer.start("tokenlist-11");
      // Keep track of which tokens have been drawn so we can perform post-processing on them later
      // (such as selection borders and names/labels)
      if (getActiveLayer().equals(token.getLayer())) {
        tokenPostProcessing.add(token);
      }
      timer.stop("tokenlist-11");

      // DEBUGGING
      // ScreenPoint tmpsp = ScreenPoint.fromZonePoint(this, new ZonePoint(token.getX(),
      // token.getY()));
      // g.setColor(Color.red);
      // g.drawLine(tmpsp.x, 0, tmpsp.x, getSize().height);
      // g.drawLine(0, tmpsp.y, getSize().width, tmpsp.y);
    }
    timer.start("tokenlist-12");
    boolean useIF = MapTool.getServerPolicy().isUseIndividualFOW();

    final var movingTokens = new HashSet<GUID>();
    for (final var selectionSet : selectionSetMap.values()) {
      movingTokens.addAll(selectionSet.getTokens());
    }

    // Selection and labels
    for (Token token : tokenPostProcessing) {
      TokenLocation location = tokenLocationCache.get(token);
      if (location == null) {
        continue;
      }
      Area bounds = location.bounds;

      // TODO: This isn't entirely accurate as it doesn't account for the actual text
      // to be in the clipping bounds, but I'll fix that later
      if (!bounds.getBounds().intersects(clipBounds)) {
        continue;
      }
      Rectangle footprintBounds = token.getBounds(zone);

      // Count moving tokens as "selected" so that a border is drawn around them.
      boolean isSelected =
          selectionModel.isSelected(token.getId()) || movingTokens.contains(token.getId());
      if (isSelected) {
        ScreenPoint sp = ScreenPoint.fromZonePoint(this, footprintBounds.x, footprintBounds.y);
        double width = footprintBounds.width * getScale();
        double height = footprintBounds.height * getScale();

        ImageBorder selectedBorder =
            token.getLayer().isStampLayer()
                ? AppStyle.selectedStampBorder
                : AppStyle.selectedBorder;
        if (highlightCommonMacros.contains(token)) {
          selectedBorder = AppStyle.commonMacroBorder;
        }
        if (!AppUtil.playerOwns(token)) {
          selectedBorder = AppStyle.selectedUnownedBorder;
        }
        if (useIF && token.getLayer().supportsVision() && zoneView.isUsingVision()) {
          Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
          if (tool
                  instanceof
                  RectangleExposeTool // XXX Change to use marker interface such as ExposeTool?
              || tool instanceof OvalExposeTool
              || tool instanceof FreehandExposeTool
              || tool instanceof PolygonExposeTool) {
            selectedBorder = RessourceManager.getBorder(Borders.FOW_TOOLS);
          }
        }
        if (token.hasFacing()
            && (token.getShape() == Token.TokenShape.TOP_DOWN || token.getLayer().isStampLayer())) {
          AffineTransform oldTransform = clippedG.getTransform();

          // Rotated
          clippedG.translate(sp.x, sp.y);
          clippedG.rotate(
              Math.toRadians(-token.getFacing() - 90),
              width / 2 - (token.getAnchor().x * scale),
              height / 2 - (token.getAnchor().y * scale)); // facing defaults to down, or -90
          // degrees
          selectedBorder.paintAround(clippedG, 0, 0, (int) width, (int) height);

          clippedG.setTransform(oldTransform);
        } else {
          selectedBorder.paintAround(clippedG, (int) sp.x, (int) sp.y, (int) width, (int) height);
        }
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
        Rectangle r = bounds.getBounds();
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
    timer.stop("tokenlist-12");

    timer.start("tokenlist-13");
    // Stacks
    // TODO: find a cleaner way to indicate token layer
    if (!tokenList.isEmpty() && tokenList.get(0).getLayer().isTokenLayer()) {
      boolean hideTSI = AppPreferences.getHideTokenStackIndicator();
      if (tokenStackMap != null
          && !hideTSI) { // FIXME Needed to prevent NPE but how can it be null?
        for (Token token : tokenStackMap.keySet()) {
          Area bounds = getTokenBounds(token);
          if (bounds == null) {
            // token is offscreen
            continue;
          }
          BufferedImage stackImage = RessourceManager.getImage(Images.ZONE_RENDERER_STACK_IMAGE);
          clippedG.drawImage(
              stackImage,
              bounds.getBounds().x + bounds.getBounds().width - stackImage.getWidth() + 2,
              bounds.getBounds().y - 2,
              null);
        }
      }
    }

    // Markers
    // for (TokenLocation location : getMarkerLocations()) {
    // BufferedImage stackImage = AppStyle.markerImage;
    // g.drawImage(stackImage, location.bounds.getBounds().x, location.bounds.getBounds().y, null);
    // }

    if (clippedG != g) {
      clippedG.dispose();
    }
    timer.stop("tokenlist-13");

    if (figuresOnly) {
      tempVisTokens.addAll(visibleTokenSet);
    }

    visibleTokenSet = Collections.unmodifiableSet(tempVisTokens);
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
  private boolean isTokenInNeedOfClipping(Token token, Area tokenCellArea, boolean isGMView) {

    // can view everything or zone is not using vision = no clipping needed
    if (isGMView || !zoneView.isUsingVision()) return false;

    // no clipping if there is no visible screen area
    if (visibleScreenArea == null) {
      return false;
    }

    // If the token is a figure and its center is visible then no clipping
    if (token.getShape() == Token.TokenShape.FIGURE
        && zone.getGrid().checkCenterRegion(tokenCellArea.getBounds(), visibleScreenArea)) {
      return false;
    }

    // Jamz: Always Visible tokens will get rendered fully to place on top of FoW
    // if we can see a portion of the stamp/token, defaults to 2/9ths, don't clip at all
    if (token.isAlwaysVisible()
        && zone.getGrid()
            .checkRegion(
                tokenCellArea.getBounds(), visibleScreenArea, token.getAlwaysVisibleTolerance())) {
      return false;
    }

    // clipping needed
    return true;
  }

  private boolean canSeeMarker(Token token) {
    return MapTool.getPlayer().isGM() || !StringUtil.isEmpty(token.getNotes());
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
    Set<GUID> ownedTokens = new LinkedHashSet<GUID>();
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
    List<Token> tokenList = new ArrayList<Token>();

    for (GUID g : selectionModel.getSelectedTokenIds()) {
      final var token = zone.getToken(g);
      if (token != null) {
        tokenList.add(token);
      }
    }
    // Commented out to preserve selection order
    // Collections.sort(tokenList, Token.NAME_COMPARATOR);

    return tokenList;
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
   * Selects the tokens inside a selection rectangle.
   *
   * @param rect the selection rectangle
   */
  public List<GUID> getTokenIdsInBounds(Rectangle rect) {
    final var tokens = new ArrayList<GUID>();
    for (TokenLocation location : getTokenLocations(getActiveLayer())) {
      if (rect.intersects(location.bounds.getBounds())) {
        tokens.add(location.token.getId());
      }
    }
    return tokens;
  }

  public void cycleSelectedToken(int direction) {
    List<Token> visibleTokens = getTokensOnScreen();
    int newSelection = 0;

    if (visibleTokens.size() == 0) {
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

  public Area getTokenBounds(Token token) {
    TokenLocation location = tokenLocationCache.get(token);
    if (location != null
        && !location.maybeOnscreen(new Rectangle(0, 0, getSize().width, getSize().height))) {
      location = null;
    }
    return location != null ? location.bounds : null;
  }

  public Area getMarkerBounds(Token token) {
    for (TokenLocation location : markerLocationList) {
      if (location.token == token) {
        return location.bounds;
      }
    }
    return null;
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
   * <p>TODO: Add a check so that tokens owned by the current player are given priority.
   *
   * @param x screen location x
   * @param y screen location y
   * @return the token
   */
  public Token getTokenAt(int x, int y) {
    List<TokenLocation> locationList =
        new ArrayList<TokenLocation>(getTokenLocations(getActiveLayer()));
    Collections.reverse(locationList);
    for (TokenLocation location : locationList) {
      if (location.bounds.contains(x, y)) {
        return location.token;
      }
    }
    return null;
  }

  public Token getMarkerAt(int x, int y) {
    List<TokenLocation> locationList = new ArrayList<TokenLocation>(markerLocationList);
    Collections.reverse(locationList);
    for (TokenLocation location : locationList) {
      if (location.bounds.contains(x, y)) {
        return location.token;
      }
    }
    return null;
  }

  public List<Token> getTokenStackAt(int x, int y) {
    Token token = getTokenAt(x, y);
    if (token == null || tokenStackMap == null || !tokenStackMap.containsKey(token)) {
      return null;
    }
    List<Token> tokenList = new ArrayList<Token>(tokenStackMap.get(token));
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
    List<LabelLocation> labelList = new ArrayList<LabelLocation>(labelLocationList);
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
      tokenLocationCache.clear();
      invalidateCurrentViewCache();
      zoneScale.zoomScale(getWidth() / 2, getHeight() / 2, scale);
      MapTool.getFrame().getZoomStatusBar().update();
    }
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
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
    repaintDebouncer.dispatch();
    return super.imageUpdate(img, infoflags, x, y, w, h);
  }

  //
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
    List<String> failedPaste = new ArrayList<String>(tokens.size());
    List<GUID> selectThese = new ArrayList<GUID>(tokens.size());

    ScreenPoint sp = ScreenPoint.fromZonePoint(this, zp);
    Point dropPoint = new Point((int) sp.x, (int) sp.y);
    SwingUtilities.convertPointToScreen(dropPoint, this);
    int tokenIndex = 0;
    for (Token token : tokens) {
      boolean configureToken = configureTokens.get(tokenIndex++);

      // Get the snap to grid value for the current prefs and abilities
      token.setSnapToGrid(
          gridCaps.isSnapToGridSupported() && AppPreferences.getTokensStartSnapToGrid());
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

      // He who drops, owns, if there are not players already set
      // and if there are already players set, add the current one to the list.
      // (Cannot use AppUtil.playerOwns() since that checks 'isStrictTokenManagement' and we want
      // real ownership
      // here.
      if (!isGM && (!token.hasOwners() || !token.isOwner(MapTool.getPlayer().getName()))) {
        token.addOwner(MapTool.getPlayer().getName());
      }

      // Token type
      Rectangle size = token.getBounds(zone);
      switch (getActiveLayer()) {
        case TOKEN:
          // Players can't drop invisible tokens
          token.setVisible(!isGM || AppPreferences.getNewTokensVisible());
          if (AppPreferences.getTokensStartFreesize()) {
            token.setSnapToScale(false);
          }
          break;
        case BACKGROUND:
          token.setShape(Token.TokenShape.TOP_DOWN);

          token.setSnapToScale(!AppPreferences.getBackgroundsStartFreesize());
          token.setSnapToGrid(AppPreferences.getBackgroundsStartSnapToGrid());
          token.setVisible(AppPreferences.getNewBackgroundsVisible());

          // Center on drop point
          if (!token.isSnapToScale() && !token.isSnapToGrid()) {
            token.setX(token.getX() - size.width / 2);
            token.setY(token.getY() - size.height / 2);
          }
          break;
        case OBJECT:
          token.setShape(Token.TokenShape.TOP_DOWN);

          token.setSnapToScale(!AppPreferences.getObjectsStartFreesize());
          token.setSnapToGrid(AppPreferences.getObjectsStartSnapToGrid());
          token.setVisible(AppPreferences.getNewObjectsVisible());

          // Center on drop point
          if (!token.isSnapToScale() && !token.isSnapToGrid()) {
            token.setX(token.getX() - size.width / 2);
            token.setY(token.getY() - size.height / 2);
          }
          break;
      }

      // FJE Yes, this looks redundant. But calling getType() retrieves the type of
      // the Token and returns NPC if the type can't be determined (raw image,
      // corrupted token file, etc). So retrieving it and then turning around and
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
          if (AppPreferences.getShowDialogOnNewToken() || showDialog) {
            NewTokenDialog dialog = new NewTokenDialog(token, dropPoint.x, dropPoint.y);
            dialog.showDialog();
            if (!dialog.isSuccess()) {
              continue;
            }
          }
        }
      } else {
        // Player dropped, ensure it's a PC token
        // (Why? Couldn't a Player drop an RPTOK that represents an NPC, such as for a summoned
        // monster?
        // Unfortunately, we can't know at this point whether the original input was an RPTOK or
        // not.)
        token.setType(Token.Type.PC);

        // For Players, check to see if the name is already in use. If it is already in use, make
        // sure the
        // current Player
        // owns the token being duplicated (to avoid subtle ways of manipulating someone else's
        // token!).
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
      // Make sure all the assets are transfered
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
      String mesg = I18N.getText("Token.error.unableToPaste", failedPaste);
      TextMessage msg = TextMessage.gmMe(null, mesg);
      MapTool.addMessage(msg);
    }
    // Copy them to the clipboard so that we can quickly copy them onto the map
    AppActions.copyTokens(tokens);
    AppActions.updateActions();
    requestFocusInWindow();
  }

  /**
   * Checks to see if token has an image table and references that if the token has a facing
   * otherwise uses basic image
   *
   * @param token the token to get the image from.
   * @return BufferedImage
   */
  private BufferedImage getTokenImage(Token token) {
    BufferedImage image = null;
    // Get the basic image
    if (token.getHasImageTable() && token.hasFacing() && token.getImageTableName() != null) {
      LookupTable lookupTable =
          MapTool.getCampaign().getLookupTableMap().get(token.getImageTableName());
      if (lookupTable != null) {
        try {
          LookupEntry result = lookupTable.getLookup(token.getFacing().toString());
          if (result != null) {
            image = ImageManager.getImage(result.getImageId(), this);
          }
        } catch (ParserException p) {
          // do nothing
        }
      }
    }

    if (image == null) {
      // Adds this as observer so we can repaint once the image is ready. Fixes #1700.
      image = ImageManager.getImage(token.getImageAssetId(), this);
    }
    return image;
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

  public Set<GUID> getVisibleTokenSet() {
    return visibleTokenSet;
  }

  public List<Token> getVisibleTokens() {
    List<Token> tokenList = new ArrayList<Token>(visibleTokenSet.size());
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

  @Subscribe
  private void onTopologyChanged(TopologyChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    flushFog();
    flushLight();
    MapTool.getFrame().updateTokenTree(); // for any event
    repaintDebouncer.dispatch();
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

  // Should this be moved to GridRenderer?
  @Subscribe
  private void onGridChanged(GridChanged event) {
    if (event.zone() != this.zone) {
      return;
    }
    repaintDebouncer.dispatch();
  }

  //
  // COMPARABLE
  public int compareTo(@NotNull ZoneRenderer o) {
    if (o != this) {
      return (int) (zone.getCreationTime() - o.zone.getCreationTime());
    }
    return 0;
  }

  // Begin token common macro identification
  private List<Token> highlightCommonMacros = new ArrayList<Token>();

  public List<Token> getHighlightCommonMacros() {
    return highlightCommonMacros;
  }

  public void setHighlightCommonMacros(List<Token> affectedTokens) {
    highlightCommonMacros = affectedTokens;
    repaintDebouncer.dispatch();
  }

  // End token common macro identification

  //
  // IMAGE OBSERVER
  // private final ImageObserver drawableObserver = new ImageObserver() {
  // public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
  // ZoneRenderer.this.flushDrawableRenderer();
  // MapTool.getFrame().refresh();
  // return true;
  // }
  // };

  /**
   * Our goal with this method (which overrides the parent's method) is to create a custom mouse
   * pointer that represents a group of tokens selected on the map. The idea is to provide some
   * feedback to the user that they have more than one token selected at the current time.
   *
   * <p>Unfortunately, while our custom cursor appears to be created correctly, it is never properly
   * applied as the mouse pointer so there is no visual effect. Hence it's currently commented out
   * by using an "if (false)" around the code block.
   *
   * <p>Merudo: applied correctly now? TODO: replace false by proper condition.
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
      // Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16); // On OSX returns any
      // size up to 1/2
      // of (screen width, screen height)
      // System.out.println("Best cursor size: " + d);

      BufferedImage img = ImageIO.read(MapTool.class.getResourceAsStream(resource));
      Font font = AppStyle.labelFont;
      Graphics2D z = (Graphics2D) this.getGraphics();
      z.setFont(font);
      FontRenderContext frc = z.getFontRenderContext();
      TextLayout tl = new TextLayout(tokenName, font, frc);
      Rectangle textbox = tl.getPixelBounds(null, 0, 0);

      // Now create a larger BufferedImage that will hold both the existing cursor and a token name

      // Use the larger of the image width or string width, and the height of the image + the height
      // of the string
      // to represent the bounding box of the 'arrow+tokenName'
      Rectangle bounds =
          new Rectangle(Math.max(img.getWidth(), textbox.width), img.getHeight() + textbox.height);
      BufferedImage cursor =
          new BufferedImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
      Graphics2D g2d = cursor.createGraphics();
      g2d.setFont(font);
      g2d.setComposite(z.getComposite());
      g2d.setStroke(z.getStroke());
      g2d.setPaintMode();
      z.dispose();

      Object oldAA = SwingUtil.useAntiAliasing(g2d);
      // g2d.setTransform( ((Graphics2D)this.getGraphics()).getTransform() );
      // g2d.drawImage(img, null, 0, 0);
      g2d.drawImage(
          img, new AffineTransform(1f, 0f, 0f, 1f, 0, 0), null); // Draw the arrow at 1:1 resolution
      g2d.translate(0, img.getHeight() + textbox.height / 2);
      // g2d.transform(new AffineTransform(0.5f, 0f, 0f, 0.5f, 0, 0)); // Why do I need this to
      // scale down the
      // text??
      g2d.setColor(Color.BLACK);
      GraphicsUtil.drawBoxedString(
          g2d, tokenName, 0, 0, SwingUtilities.LEFT); // The text draw here is not nearly
      // as nice looking as normal
      // g2d.setBackground(Color.BLACK);
      // g2d.setColor(Color.WHITE);
      // g2d.fillRect(0, bounds.height-textbox.height, textbox.width, textbox.height);
      // g2d.drawString(tokenName, 0F, bounds.height - descent);
      g2d.dispose();
      c = Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(0, 0), tokenName);
      SwingUtil.restoreAntiAliasing(g2d, oldAA);

      img.flush(); // Try to be friendly about memory usage. ;-)
      cursor.flush();
    } catch (Exception e) {
    }
    return c;
  }

  /**
   * Returns the alpha level used to apply the noise to back ground repeating textures.
   *
   * @return the alpha level used to apply the noise.
   */
  public float getNoiseAlpha() {
    return noise.getNoiseAlpha();
  }

  /**
   * Returns the seed value used to generate the noise that is applied to tback ground repeating
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
