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
package net.rptools.maptool.client.ui.zone;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.rptools.lib.CollectionUtil;
import net.rptools.lib.MD5Key;
import net.rptools.lib.StringUtil;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.ZoneLoaded;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.AttachedLightSource;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.GraphicsUtil;
import net.rptools.maptool.util.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data used for rendering.
 *
 * <p>This is deliberately not much encapsulated.
 */
public class ZoneViewModel {
  private static final Logger log = LoggerFactory.getLogger(ZoneViewModel.class);

  /**
   * Represents the selectable bounds of a token.
   *
   * <p>Obsoletes various other TokenPosition types that are either screen-based or inconsistent.
   */
  public record TokenPosition(Token token, Rectangle2D footprintBounds, Area transformedBounds) {
    public static TokenPosition fromToken(Token token, Zone zone) {
      Rectangle2D footprintBounds = token.getFootprintBounds(zone);

      final Area transformedBounds = new Area(footprintBounds);
      if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
        // facing defaults to down or -90 degrees
        double centerX = footprintBounds.getCenterX() - token.getAnchorX();
        double centerY = footprintBounds.getCenterY() - token.getAnchorY();
        var at =
            AffineTransform.getRotateInstance(
                Math.toRadians(token.getFacingInDegrees()), centerX, centerY);

        transformedBounds.transform(at);
      }

      return new TokenPosition(token, footprintBounds, transformedBounds);
    }
  }

  public final Zone zone;

  // region These are updated externally.

  private final ZoneView zoneView;
  private final SelectionModel selectionModel;
  private final List<GUID> highlightCommonMacros = new ArrayList<>();

  // endregion

  // region These are updated at the start of each render via `update()`.

  private boolean isUsingGdxRenderer = false;

  /** The status message describing how loaded the zone is, or {@code null} if fully loaded. */
  private @Nullable String loadingProgress = "";

  private PlayerView playerView = new PlayerView(Player.Role.PLAYER);
  private Scale zoneScale = new Scale();
  private final Rectangle2D viewport = new Rectangle2D.Double();
  private Area visibleArea = new Area();

  private final List<Token> selectedTokenList = new ArrayList<>();
  private final Set<GUID> movingTokens = new HashSet<>();

  private final Map<GUID, TokenPosition> tokenPositions = new HashMap<>();
  private final Set<GUID> onScreenTokens = new HashSet<>();
  private final Map<Zone.Layer, List<TokenPosition>> tokenPositionsByLayer =
      CollectionUtil.newFilledEnumMap(Zone.Layer.class, l -> new LinkedList<>());

  private final List<TokenPosition> markerList = new ArrayList<>();
  private final Map<Token, Set<Token>> tokenStackMap = new HashMap<>();

  private final Map<Zone.Layer, Set<GUID>> visibleTokensByLayer =
      CollectionUtil.newFilledEnumMap(Zone.Layer.class, layer -> new HashSet<>());

  private final List<Point2D> lightPositions = new ArrayList<>();

  // endregion

  public ZoneViewModel(Zone zone, ZoneView zoneView, SelectionModel selectionModel) {
    this.zone = zone;
    this.zoneView = zoneView;
    this.selectionModel = selectionModel;
  }

  /** Marks the zone as not loaded, so that it ensures once again that all assets are loaded. */
  public void flush() {
    loadingProgress = "";
  }

  public boolean isUsingGdxRenderer() {
    return isUsingGdxRenderer;
  }

  /**
   * Gets a string describing how much of the zone is loaded.
   *
   * <p>If the zone is fully loaded, the result will be empty.
   *
   * @return An optional containing the loading status, or the empty optional if the zone is loaded.
   */
  public Optional<String> getLoadingStatus() {
    return Optional.ofNullable(loadingProgress);
  }

  public Scale getZoneScale() {
    return zoneScale;
  }

  public Rectangle2D getViewport() {
    return new Rectangle2D.Double(
        viewport.getMinX(), viewport.getMinY(), viewport.getWidth(), viewport.getHeight());
  }

  public Area getVisibleArea() {
    return visibleArea;
  }

  public PlayerView getPlayerView() {
    return playerView;
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
    List<Token> selectedTokens = null;
    if (selected && selectionModel.isAnyTokenSelected()) {
      selectedTokens = new ArrayList<>(getSelectedTokenList());
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

  public Map<GUID, TokenPosition> getTokenPositions() {
    return Collections.unmodifiableMap(tokenPositions);
  }

  public Set<GUID> getOnScreenTokens() {
    return Collections.unmodifiableSet(onScreenTokens);
  }

  public List<TokenPosition> getTokenPositionsForLayer(Zone.Layer layer) {
    return Collections.unmodifiableList(
        tokenPositionsByLayer.getOrDefault(layer, Collections.emptyList()));
  }

  public List<TokenPosition> getMarkerPositions() {
    return Collections.unmodifiableList(markerList);
  }

  public Map<Token, Set<Token>> getTokenStackMap() {
    return Collections.unmodifiableMap(tokenStackMap);
  }

  public List<Token> getSelectedTokenList() {
    return Collections.unmodifiableList(selectedTokenList);
  }

  public Set<GUID> getVisibleTokens(Zone.Layer layer) {
    return Collections.unmodifiableSet(visibleTokensByLayer.get(layer));
  }

  public boolean isTokenMoving(GUID tokenId) {
    return movingTokens.contains(tokenId);
  }

  public List<GUID> getHighlightCommonMacros() {
    return Collections.unmodifiableList(highlightCommonMacros);
  }

  public void setHighlightCommonMacros(List<Token> affectedTokens) {
    highlightCommonMacros.clear();
    affectedTokens.stream().map(Token::getId).forEach(highlightCommonMacros::add);
  }

  public List<Point2D> getLightPositions() {
    return Collections.unmodifiableList(lightPositions);
  }

  public void update() {
    updateIsUsingGdxRenderer();
    updateIsLoading();
    updateViewport();
    updatePlayerView();
    updateVisibleArea();
    updateSelectedTokensList();
    updateMovingTokens();
    updateTokenPositions();
    updateMarkerPositions();
    updateTokenStacks();
    updateVisibleTokens();
    updateLightPosition();
  }

  private void updateIsUsingGdxRenderer() {
    isUsingGdxRenderer = MapTool.getFrame().getGdxPanel().isVisible();
  }

  // What follows are "systems".

  /**
   * If the zone is not already loaded, updates the loading status and emits {@link ZoneLoaded} if
   * it becomes loaded.
   */
  private void updateIsLoading() {
    if (loadingProgress == null) {
      // We're done, until the cache is cleared
      return;
    }

    ImageObserver observer =
        Objects.requireNonNullElseGet(
            MapTool.getFrame().getZoneRenderer(this.zone),
            () -> (ImageObserver) (img, infoflags, x, y, width, height) -> false);

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
      Image image = ImageManager.getImage(asset.getMD5Key(), observer);
      if (image == null || image == ImageManager.TRANSFERING_IMAGE) {
        loaded = false;
        continue;
      }
      cacheCount++;
    }

    if (loaded) {
      // Indicate that loading is finished.
      loadingProgress = null;

      // Notify the token tree that it should update
      MapTool.getFrame().updateTokenTree();
      new MapToolEventBus().getMainEventBus().post(new ZoneLoaded(zone));
    } else {
      loadingProgress =
          String.format(
              " Loading Map '%s' - %d/%d Loaded %d/%d Cached",
              zone.getDisplayName(), downloadCount, assetSet.size(), cacheCount, assetSet.size());
    }
  }

  private void updateViewport() {
    var renderer = MapTool.getFrame().getZoneRenderer(this.zone);
    if (renderer == null) {
      // No viewport.
      zoneScale = new Scale();
      viewport.setFrame(0, 0, 0, 0);
      return;
    }

    zoneScale = new Scale(renderer.getZoneScale());
    var screenBounds = new Rectangle2D.Double(0, 0, renderer.getWidth(), renderer.getHeight());
    viewport.setFrame(zoneScale.toWorldSpace(screenBounds));
  }

  private void updateVisibleArea() {
    visibleArea = zoneView.getVisibleArea(playerView);
  }

  private void updateSelectedTokensList() {
    selectedTokenList.clear();

    for (GUID g : selectionModel.getSelectedTokenIds()) {
      final var token = zone.getToken(g);
      if (token != null) {
        selectedTokenList.add(token);
      }
    }
  }

  private void updatePlayerView() {
    playerView = makePlayerView(MapTool.getPlayer().getEffectiveRole(), true);
  }

  /** Clears and populates {@link #tokenPositions} and {@link #tokenPositionsByLayer}. */
  private void updateTokenPositions() {
    tokenPositions.clear();

    for (var layer : Zone.Layer.values()) {
      var layerList = tokenPositionsByLayer.get(layer);
      layerList.clear();

      // Note the order: the top most token is at the end of the list.
      var tokens = zone.getTokensOnLayer(layer);
      for (var token : tokens) {
        if ((!token.isVisible() || !token.getLayer().isVisibleToPlayers())
            && !this.playerView.isGMView()) {
          continue;
        }
        if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
          continue;
        }

        var tokenPosition = TokenPosition.fromToken(token, zone);

        tokenPositions.put(token.getId(), tokenPosition);
        layerList.add(tokenPosition);
      }
    }
  }

  private void updateMarkerPositions() {
    for (var list : tokenPositionsByLayer.values()) {
      for (var tokenPosition : list) {
        var token = tokenPosition.token();
        var playerCanSeeMarker =
            MapTool.getPlayer().isGM() || !StringUtil.isEmpty(token.getNotes());
        if (tokenPosition.token().isMarker() && playerCanSeeMarker) {
          markerList.add(tokenPosition);
        }
      }
    }
  }

  private void updateTokenStacks() {
    tokenStackMap.clear();
    var tokenPositions = tokenPositionsByLayer.get(Zone.Layer.TOKEN);
    for (var tokenPosition : tokenPositions) {
      var token = tokenPosition.token();
      Set<Token> tokenStackSet = new HashSet<>();

      for (var otherPosition : tokenPositions) {
        if (tokenPosition.token().getId().equals(otherPosition.token().getId())) {
          // Don't self-stack.
          continue;
        }

        // Are we covering anyone ?
        if (!tokenPosition.footprintBounds().contains(otherPosition.footprintBounds())) {
          continue;
        }

        tokenStackSet.add(otherPosition.token());

        var reverse = tokenStackMap.get(otherPosition.token());
        if (reverse != null) {
          // The other token also covers tokens, so incorporate them into this one.
          tokenStackSet.addAll(reverse);
          tokenStackMap.remove(otherPosition.token());
        }
      }

      if (!tokenStackSet.isEmpty()) {
        tokenStackSet.add(token);
        tokenStackMap.put(token, tokenStackSet);
      }
    }
  }

  private void updateVisibleTokens() {
    double scale = 1;
    var renderer = MapTool.getFrame().getZoneRenderer(this.zone);
    if (renderer != null) {
      scale = renderer.getZoneScale().getScale();
    }

    onScreenTokens.clear();

    for (var layer : Zone.Layer.values()) {
      var tokenPositions = tokenPositionsByLayer.get(layer);

      var visibleTokens = visibleTokensByLayer.get(layer);
      visibleTokens.clear();
      for (var tokenPosition : tokenPositions) {
        // First make sure it is on screen.
        if (!tokenPosition.transformedBounds().intersects(viewport)) {
          continue;
        }

        onScreenTokens.add(tokenPosition.token().getId());

        // Then make sure it is in revealed area (for players).
        if (!playerView.isGMView()
            && layer.supportsVision()
            && zoneView.isUsingVision()
            && !GraphicsUtil.intersects(tokenPosition.transformedBounds(), visibleArea)) {
          continue;
        }

        var bounds = tokenPosition.transformedBounds().getBounds2D();
        if (bounds.getWidth() * scale < 1 || bounds.getHeight() * scale < 1) {
          continue;
        }

        visibleTokens.add(tokenPosition.token().getId());
      }
    }
  }

  private void updateMovingTokens() {
    movingTokens.clear();

    var renderer = MapTool.getFrame().getZoneRenderer(this.zone);
    if (renderer == null) {
      return;
    }

    for (final var selectionSet : renderer.getSelectionSetMap().values()) {
      movingTokens.addAll(selectionSet.getTokens());
    }
  }

  private void updateLightPosition() {
    lightPositions.clear();

    if (!AppState.isShowLightSources() || !playerView.isGMView()) {
      return;
    }

    for (TokenPosition position : tokenPositions.values()) {
      var token = position.token();

      if (!token.hasLightSources()) {
        continue;
      }
      if (!onScreenTokens.contains(token.getId())) {
        continue;
      }

      boolean foundNormalLight = false;
      for (AttachedLightSource attachedLightSource : token.getLightSources()) {
        LightSource lightSource = attachedLightSource.resolve(token, MapTool.getCampaign());
        if (lightSource != null && lightSource.getType() == LightSource.Type.NORMAL) {
          foundNormalLight = true;
          break;
        }
      }
      if (foundNormalLight) {
        var bounds = position.transformedBounds().getBounds2D();
        lightPositions.add(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
      }
    }
  }
}
