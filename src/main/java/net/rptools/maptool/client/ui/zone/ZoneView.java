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

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.Illumination.LumensLevel;
import net.rptools.maptool.client.ui.zone.Illuminator.LitArea;
import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.zones.TokensAdded;
import net.rptools.maptool.model.zones.TokensChanged;
import net.rptools.maptool.model.zones.TokensRemoved;
import net.rptools.maptool.model.zones.TopologyChanged;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Responsible for calculating lights and vision. */
public class ZoneView {
  /**
   * Associates a LitArea stored in an Illuminator with the Light that it was created for.
   *
   * <p>This is used to represent normal lights, personal lights, and day light. In the case of
   * daylight, lightInfo will be null since it doesn't originate from an actual light, and should
   * never be rendered.
   *
   * @param litArea
   * @param lightInfo
   */
  private record ContributedLight(LitArea litArea, LightInfo lightInfo) {

    public static ContributedLight forDaylight(Area visibleArea) {
      return new ContributedLight(new LitArea(1, visibleArea), null);
    }
  }

  /**
   * Combines a LightSource and a Light for easy referencing in ContributedLight.
   *
   * @param lightSource
   * @param light
   */
  private record LightInfo(LightSource lightSource, Light light) {}

  /**
   * Represents the important aspects of a sight for the purposes of calculating illumination.
   *
   * <p>Obviously only one field for now, but in the future it may do more (e.g., lumens boosting),
   * so let's keep it in an easily identifiable type.
   */
  public record IlluminationKey(double multiplier) {}

  private static final Logger log = LogManager.getLogger(ZoneView.class);

  /** The zone of the ZoneView. */
  private final Zone zone;

  // VISION

  // region These fields track light sources and their illuminated areas.

  /** Map light source type to all tokens with that type. */
  private final Map<LightSource.Type, Set<GUID>> lightSourceMap = new HashMap<>();

  // endregion

  // region The fields cache information about tokens themselves. They do incorporate illumination
  // parameters, but only the token's sight determines them, so we only need to cache per-token and
  // not also per-IlluminationKey.

  /**
   * Personal lights and daylight for each token and illumination.
   *
   * <p>These are calculated for a given IlluminationKey derived from the token's sight, and are
   * incorporated into per-PlayerView results.
   */
  private final Map<GUID, List<ContributedLight>> contributedPersonalLightsByToken =
      new HashMap<>();

  /**
   * Map each token to the area they can see by themselves.
   *
   * <p>This vision only accounts for topology, and does not include and other limits on the token's
   * vision. The results can be intersected with lighting results to produce the area that can
   * actually be seen by a token in a given view.
   */
  private final Map<GUID, Area> tokenVisibleAreaCache = new HashMap<>();

  // endregion

  // region These fields cache information that is specific to certain illumination parameters. They
  //        only need to be flushed when something globally changes, such as light definitions.

  /**
   * The list of all non-personal lights contributed to an illumination for each token.
   *
   * <p>This is used to generate DrawableLights and to remove light sources from any existing
   * Illuminators.
   */
  private final Map<IlluminationKey, Map<GUID, List<ContributedLight>>> contributedLightsByToken =
      new HashMap<>();

  /** The illuminator structures that can be used for each set of illumination parameters. */
  private final Map<IlluminationKey, Illuminator> illuminators = new HashMap<>();

  /**
   * A cache of results from the {@link #illuminators} that do includes all normal lights but no
   * personal lights or daylight.
   */
  private final Map<IlluminationKey, Illumination> illuminationCache = new HashMap<>();

  // endregion

  // region These fields cache information that is specific to the current PlayerView. They need to
  //        be flushed when something external changes that can affect vision (e.g., VBL, map vision
  //        settings, light definitions).

  /** Map each token to their current vision, depending on other lights. */
  private final Map<PlayerView, Map<GUID, Area>> tokenVisionCachePerView = new HashMap<>();

  /**
   * The illumination calculated for a view.
   *
   * <p>Unlike {@link #illuminationCache}, this field includes personal lights and daylight.
   */
  private final Map<PlayerView, Illumination> illuminationsPerView = new HashMap<>();

  /** Map the PlayerView to its exposed area. */
  private final Map<PlayerView, Area> exposedAreaMap = new HashMap<>();

  /** Map the PlayerView to its visible area. */
  private final Map<PlayerView, VisibleAreaMeta> visibleAreaMap = new HashMap<>();

  // endregion

  private final Map<Zone.TopologyType, Area> topologyAreas = new EnumMap<>(Zone.TopologyType.class);

  private final Map<Zone.TopologyType, AreaTree> topologyTrees =
      new EnumMap<>(Zone.TopologyType.class);

  /** Lumen for personal vision (darkvision). */
  private static final int LUMEN_VISION = 100;

  /**
   * Construct ZoneView from zone. Build lightSourceMap, and add ZoneView to Zone as listener.
   *
   * @param zone the Zone to add.
   */
  public ZoneView(Zone zone) {
    this.zone = zone;
    findLightSources();

    new MapToolEventBus().getMainEventBus().register(this);
  }

  public Area getExposedArea(PlayerView view) {
    Area exposed = exposedAreaMap.get(view);

    if (exposed == null) {
      boolean combinedView =
          !isUsingVision()
              || MapTool.isPersonalServer()
              || !MapTool.getServerPolicy().isUseIndividualFOW()
              || view.isGMView();

      if (view.isUsingTokenView() || combinedView) {
        exposed = zone.getExposedArea(view);
      } else {
        // Not a token-specific view, but we are using Individual FoW. So we build up all the owned
        // tokens' exposed areas to build the soft FoW. Note that not all owned tokens may still
        // have sight (so weren't included in the PlayerView), but could still have previously
        // exposed areas.
        exposed = new Area();
        for (Token tok : zone.getTokens()) {
          if (!AppUtil.playerOwns(tok)) {
            continue;
          }
          ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tok.getExposedAreaGUID());
          Area exposedArea = meta.getExposedAreaHistory();
          exposed.add(new Area(exposedArea));
        }
      }

      exposedAreaMap.put(view, exposed);
    }
    return exposed;
  }

  /**
   * Calculate the visible area of the view, cache it in visibleAreaMap, and return it
   *
   * @param view the PlayerView
   * @return the visible area
   */
  public Area getVisibleArea(PlayerView view) {
    calculateVisibleArea(view);
    ZoneView.VisibleAreaMeta visible = visibleAreaMap.get(view);

    return visible != null ? visible.visibleArea : new Area();
  }

  /**
   * Get the vision status of the zone.
   *
   * @return true if the vision of the zone is not of type VisionType.OFF
   */
  public boolean isUsingVision() {
    return zone.getVisionType() != Zone.VisionType.OFF;
  }

  /**
   * Get the map and token topology of the requested type.
   *
   * <p>The topology is cached and should only regenerate when not yet present, which should happen
   * on flush calls.
   *
   * @param topologyType The type of topology tree to get.
   * @return the area of the topology.
   */
  public synchronized Area getTopology(Zone.TopologyType topologyType) {
    var topology = topologyAreas.get(topologyType);

    if (topology == null) {
      log.debug("ZoneView topology area for {} is null, generating...", topologyType.name());

      topology = new Area(zone.getTopology(topologyType));
      List<Token> topologyTokens =
          MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokensWithTopology(topologyType);
      for (Token topologyToken : topologyTokens) {
        topology.add(topologyToken.getTransformedTopology(topologyType));
      }

      topologyAreas.put(topologyType, topology);
    }

    return topology;
  }

  /**
   * Get the topology tree of the requested type.
   *
   * <p>The topology tree is cached and should only regenerate when the tree is not present, which
   * should happen on flush calls.
   *
   * <p>This method is equivalent to building an AreaTree from the results of getTopology(), but the
   * results are cached.
   *
   * @param topologyType The type of topology tree to get.
   * @return the AreaTree (topology tree).
   */
  private synchronized AreaTree getTopologyTree(Zone.TopologyType topologyType) {
    var topologyTree = topologyTrees.get(topologyType);

    if (topologyTree == null) {
      log.debug("ZoneView topology tree for {} is null, generating...", topologyType.name());

      var topology = getTopology(topologyType);

      topologyTree = new AreaTree(topology);
      topologyTrees.put(topologyType, topologyTree);
    }

    return topologyTree;
  }

  private Illuminator getUpToDateIlluminator(IlluminationKey illuminationKey) {
    final var illuminator = illuminators.computeIfAbsent(illuminationKey, key -> new Illuminator());
    final var contributingTokens =
        contributedLightsByToken.computeIfAbsent(illuminationKey, key -> new HashMap<>());

    // We need to get all lights ordered by lumens. From there, we can do darkness subtraction and
    // build drawable lights for each, and then build a lumens map.
    final var lightSourceTokenGuids =
        new ArrayList<>(
            lightSourceMap.getOrDefault(LightSource.Type.NORMAL, Collections.emptySet()));
    // No need to recalculate for tokens already contributing.
    lightSourceTokenGuids.removeAll(contributingTokens.keySet());
    final var lightSourceTokens =
        lightSourceTokenGuids.stream()
            .map(zone::getToken)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

    // For each light source, extract all normal and darkness lights.
    for (final var lightSourceToken : lightSourceTokens) {
      final var litAreas = calculateLitAreas(lightSourceToken, illuminationKey.multiplier());
      for (final var litArea : litAreas) {
        illuminator.add(litArea.litArea());
        contributingTokens
            .computeIfAbsent(lightSourceToken.getId(), id -> new ArrayList<>())
            .add(litArea);
      }
    }

    return illuminator;
  }

  private List<ContributedLight> calculateLitAreas(Token lightSourceToken, double multiplier) {
    final var result = new ArrayList<ContributedLight>();

    for (final var attachedLightSource : lightSourceToken.getLightSources()) {
      LightSource lightSource =
          MapTool.getCampaign().getLightSource(attachedLightSource.getLightSourceId());
      if (lightSource == null) {
        continue;
      }

      final var perLightSourceResult =
          calculateLitAreaForLightSource(lightSourceToken, multiplier, lightSource);
      result.addAll(perLightSourceResult);
    }

    return result;
  }

  private List<ContributedLight> calculateLitAreaForLightSource(
      @Nonnull Token lightSourceToken, double multiplier, @Nonnull LightSource lightSource) {
    if (lightSource.getType() != LightSource.Type.NORMAL) {
      return Collections.emptyList();
    }

    final var p = FogUtil.calculateVisionCenter(lightSourceToken, zone);
    final var translateTransform = AffineTransform.getTranslateInstance(p.x, p.y);
    final var magnifyTransform = AffineTransform.getScaleInstance(multiplier, multiplier);

    final var lightSourceArea = lightSource.getArea(lightSourceToken, zone);
    // Calculate exposed area
    // Jamz: OK, let not have lowlight vision type multiply darkness radius
    if (multiplier != 1 && lightSource.getType() == LightSource.Type.NORMAL) {
      lightSourceArea.transform(magnifyTransform);
    }
    lightSourceArea.transform(translateTransform);

    final var lightSourceVisibleArea =
        FogUtil.calculateVisibility(
            p,
            lightSourceArea,
            getTopologyTree(Zone.TopologyType.WALL_VBL),
            getTopologyTree(Zone.TopologyType.HILL_VBL),
            getTopologyTree(Zone.TopologyType.PIT_VBL));
    if (lightSourceVisibleArea == null) {
      // Nothing illuminated for this source.
      return Collections.emptyList();
    }

    final var litAreas = new ArrayList<ContributedLight>();
    var lumens = lightSource.getLumens();
    if (lumens == 0) {
      lumens = LUMEN_VISION;
    }

    // Tracks the cummulative inner ranges of light sources so that we can cut them out of the
    // outer ranges and end up with disjoint sets, even when magnifying.
    final var cummulativeNotTransformedArea = new Area();
    for (final var light : lightSource.getLightList()) {
      final var notScaledLightArea =
          light.getArea(lightSourceToken, zone, lightSource.isScaleWithToken());
      if (notScaledLightArea == null) {
        continue;
      }
      final var lightArea = new Area(notScaledLightArea);
      lightArea.subtract(cummulativeNotTransformedArea);

      if (lightSource.getType() == LightSource.Type.NORMAL && multiplier != 1) {
        lightArea.transform(magnifyTransform);
      }
      lightArea.transform(translateTransform);
      lightArea.intersect(lightSourceVisibleArea);

      litAreas.add(
          new ContributedLight(new LitArea(lumens, lightArea), new LightInfo(lightSource, light)));

      cummulativeNotTransformedArea.add(notScaledLightArea);
    }

    return litAreas;
  }

  private Stream<Token> getTokensForView(PlayerView view) {
    final boolean isGMview = view.isGMView();
    final boolean checkOwnership =
        MapTool.getServerPolicy().isUseIndividualViews() || MapTool.isPersonalServer();
    List<Token> tokenList =
        view.isUsingTokenView()
            ? view.getTokens()
            : zone.getTokensFiltered(
                t -> t.isToken() && t.getHasSight() && (isGMview || t.isVisible()));

    return tokenList.stream()
        .filter(
            token -> {
              boolean weOwnIt = AppUtil.playerOwns(token);
              // Permission
              if (checkOwnership) {
                if (!weOwnIt) {
                  return false;
                }
              } else {
                // If we're viewing the map as a player and the token is not a PC, then skip it.
                if (!isGMview
                    && token.getType() != Token.Type.PC
                    && !AppUtil.ownedByOnePlayer(token)) {
                  return false;
                }
              }

              // player ownership permission
              if (token.isVisibleOnlyToOwner() && !weOwnIt) {
                return false;
              }

              return true;
            });
  }

  private ZoneView.IlluminationKey illuminationKeyFromView(PlayerView view) {
    // The maximum range is generally a good option for rendering.
    return new ZoneView.IlluminationKey(
        view.isUsingTokenView()
            ? view.getTokens().stream()
                .map(Token::getSightType)
                .map(sightName -> MapTool.getCampaign().getSightType(sightName))
                .map(SightType::getMultiplier)
                .max(Double::compare)
                .orElse(1.0)
            : 1.0);
  }

  private Illumination getIllumination(IlluminationKey illuminationKey) {
    return illuminationCache.computeIfAbsent(
        illuminationKey, key -> getUpToDateIlluminator(key).getIllumination());
  }

  private @Nonnull List<ContributedLight> getPersonalTokenContributions(Token token) {
    if (!token.getHasSight()) {
      return Collections.emptyList();
    }
    final var sight = MapTool.getCampaign().getSightType(token.getSightType());
    if (sight == null) {
      return Collections.emptyList();
    }

    var personalLights = contributedPersonalLightsByToken.get(token.getId());
    if (personalLights == null) {
      personalLights = new ArrayList<>();

      // Get the token's sight.
      final var tokenVisibleArea = getTokenVisibleArea(token);

      if (zone.getVisionType() != Zone.VisionType.NIGHT) {
        final var contributedLight = ContributedLight.forDaylight(tokenVisibleArea);
        // Treat the entire visible area like a light source of minimal lumens.
        personalLights.add(contributedLight);
      }

      if (sight.hasPersonalLightSource()) {
        // Calculate the personal light area here.
        // Note that a personal light is affected by its own sight's magnification, but that's it.
        // Since each token is only aware of its own personal light, of course we don't want a
        // token's sight magnification to affect other personal lights.
        personalLights.addAll(
            calculateLitAreaForLightSource(
                token, sight.getMultiplier(), sight.getPersonalLightSource()));
      }

      contributedPersonalLightsByToken.put(
          token.getId(), Collections.unmodifiableList(personalLights));
    }

    return personalLights;
  }

  private Illumination getIllumination(PlayerView view) {
    var illumination = illuminationsPerView.get(view);
    if (illumination == null) {
      // Not yet calculated. Do so now.
      final var illuminationKey = illuminationKeyFromView(view);
      final var baseIllumination = getIllumination(illuminationKey);

      final var extraLights = new ArrayList<ContributedLight>();
      getTokensForView(view)
          .forEach(
              token -> {
                final var personalLights = getPersonalTokenContributions(token);
                extraLights.addAll(personalLights);
              });

      // Now fold the extra lights into the lumens levels.
      final var lumensLevels =
          new ArrayList<>(
              baseIllumination.getLumensLevels().stream()
                  .map(
                      ll ->
                          new LumensLevel(
                              ll.lumensStrength(),
                              new Area(ll.lightArea()),
                              new Area(ll.darknessArea())))
                  .toList());
      for (final var extraLitArea : extraLights) {
        final var isDarkness = extraLitArea.litArea().lumens() < 0;
        final var lumensStrength = Math.abs(extraLitArea.litArea().lumens());
        final var area = extraLitArea.litArea().area();

        final var index =
            Collections.binarySearch(
                Lists.transform(lumensLevels, LumensLevel::lumensStrength), lumensStrength);
        final LumensLevel level;
        if (index >= 0) {
          // Already a lumens level. Add onto it.
          level = lumensLevels.get(index);
        } else {
          final var insertionPoint = -index - 1;
          level = new LumensLevel(lumensStrength, new Area(), new Area());
          lumensLevels.add(insertionPoint, level);
        }
        (isDarkness ? level.darknessArea() : level.lightArea()).add(area);
      }

      illumination = new Illumination(lumensLevels);
      illuminationsPerView.put(view, illumination);
    }

    return illumination;
  }

  /**
   * Gets the various areas associated with each level of lumens.
   *
   * @return The areas associated with each lumens value, arranged from weak to strong.
   */
  public List<LumensLevel> getLumensLevels(PlayerView view) {
    final var illumination = getIllumination(view);
    return illumination.getLumensLevels();
  }

  /**
   * Get the area that is visible to a token itself, accounting only for topology.
   *
   * <p>The results are cached in {@link #tokenVisibleAreaCache}
   *
   * @param token The token to find the visible area for.
   * @return The visible area for the token.
   */
  private Area getTokenVisibleArea(@Nonnull Token token) {
    // Sanity
    if (!token.getHasSight()) {
      return new Area();
    }

    SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
    // More sanity checks; maybe sight type removed from campaign after token set?
    if (sight == null) {
      return new Area();
    }

    // Combine the player visible area with the available light sources
    var tokenVisibleArea = tokenVisibleAreaCache.get(token.getId());

    if (tokenVisibleArea == null) {
      // Not cached yet.
      Point p = FogUtil.calculateVisionCenter(token, zone);
      Area visibleArea = sight.getVisionShape(token, zone);
      visibleArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
      tokenVisibleArea =
          FogUtil.calculateVisibility(
              p,
              visibleArea,
              getTopologyTree(Zone.TopologyType.WALL_VBL),
              getTopologyTree(Zone.TopologyType.HILL_VBL),
              getTopologyTree(Zone.TopologyType.PIT_VBL));
      // Can be null if no visibility.
      tokenVisibleArea = Objects.requireNonNullElse(tokenVisibleArea, new Area());
      tokenVisibleAreaCache.put(token.getId(), tokenVisibleArea);
    }

    // TODO Instead of a defensive copy, we could include a very stern warning to not modify.
    return new Area(tokenVisibleArea);
  }

  /**
   * Return the token visible area from tokenVisionCache. If null, create it.
   *
   * @param token the token to get the visible area of.
   * @return the visible area of a token, including the effect of other lights.
   */
  public Area getVisibleArea(Token token, PlayerView view) {
    // Sanity
    if (token == null) {
      return null;
    }

    // Cache ?
    Map<GUID, Area> tokenVisionCache =
        tokenVisionCachePerView.computeIfAbsent(view, v -> new HashMap<>());
    Area tokenVisibleArea = tokenVisionCache.get(token.getId());
    if (tokenVisibleArea != null) {
      return tokenVisibleArea;
    }

    // Not cached, so need to calculate the area.
    tokenVisibleArea = getTokenVisibleArea(token);

    // Very important that we don't use the general view, but only the view from the token's
    // perspective.
    final var singleTokenView = new PlayerView(view.getRole(), Collections.singletonList(token));
    final var illumination = getIllumination(singleTokenView);
    final var visibleArea = illumination.getVisibleArea();
    visibleArea.intersect(tokenVisibleArea);

    tokenVisionCache.put(token.getId(), visibleArea);

    // log.info("getVisibleArea: \t\t" + stopwatch);

    return visibleArea;
  }

  /**
   * Get the lists of drawable auras.
   *
   * @return the list of drawable auras.
   */
  public List<DrawableLight> getDrawableAuras() {
    List<DrawableLight> lightList = new LinkedList<DrawableLight>();
    final var auraTokenGUIDs = lightSourceMap.get(LightSource.Type.AURA);
    if (auraTokenGUIDs != null) {
      for (GUID lightSourceToken : auraTokenGUIDs) {
        Token token = zone.getToken(lightSourceToken);
        if (token == null) {
          continue;
        }
        Point p = FogUtil.calculateVisionCenter(token, zone);

        for (AttachedLightSource als : token.getLightSources()) {
          LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
          if (lightSource == null) {
            continue;
          }
          // Token can also have non-auras lights, we don't want those.
          if (lightSource.getType() != LightSource.Type.AURA) {
            continue;
          }

          Area lightSourceArea = lightSource.getArea(token, zone);
          lightSourceArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
          Area visibleArea =
              FogUtil.calculateVisibility(
                  p,
                  lightSourceArea,
                  getTopologyTree(Zone.TopologyType.WALL_VBL),
                  getTopologyTree(Zone.TopologyType.HILL_VBL),
                  getTopologyTree(Zone.TopologyType.PIT_VBL));

          // This needs to be cached somehow
          for (Light light : lightSource.getLightList()) {
            // If there is no paint, it's a "bright aura" that just shows whatever is beneath it and
            //  doesn't need to be rendered.
            if (light.getPaint() == null) {
              continue;
            }
            boolean isOwner = token.getOwners().contains(MapTool.getPlayer().getName());
            if ((light.isGM() && !MapTool.getPlayer().isEffectiveGM())) {
              continue;
            }
            if ((!token.isVisible()) && !MapTool.getPlayer().isEffectiveGM()) {
              continue;
            }
            if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
              continue;
            }
            if (light.isOwnerOnly() && !isOwner && !MapTool.getPlayer().isEffectiveGM()) {
              continue;
            }

            // Calculate the area covered by this particular range.
            Area lightArea = lightSource.getArea(token, zone, light);
            if (lightArea == null) {
              continue;
            }
            lightArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
            lightArea.intersect(visibleArea);
            lightList.add(
                new DrawableLight(
                    LightSource.Type.AURA, light.getPaint(), visibleArea, lightSource.getLumens()));
          }
        }
      }
    }
    return lightList;
  }

  /**
   * Find the light sources from all appropriate tokens, and store them in {@link #lightSourceMap}.
   */
  private void findLightSources() {
    lightSourceMap.clear();

    for (Token token : zone.getAllTokens()) {
      if (token.hasLightSources() && token.isVisible()) {
        if (!token.isVisibleOnlyToOwner() || AppUtil.playerOwns(token)) {
          for (AttachedLightSource als : token.getLightSources()) {
            LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
            if (lightSource == null) {
              continue;
            }
            Set<GUID> lightSet =
                lightSourceMap.computeIfAbsent(lightSource.getType(), k -> new HashSet<>());
            lightSet.add(token.getId());
          }
        }
      }
    }
  }

  public Collection<DrawableLight> getDrawableLights(PlayerView view) {
    final var illuminationKey = illuminationKeyFromView(view);
    final var contributions =
        contributedLightsByToken.getOrDefault(illuminationKey, Collections.emptyMap());

    final var personalContributions =
        getTokensForView(view)
            .filter(token -> contributedPersonalLightsByToken.containsKey(token.getId()))
            .collect(
                Collectors.toMap(
                    Token::getId, token -> contributedPersonalLightsByToken.get(token.getId())));

    if (contributions.isEmpty() && personalContributions.isEmpty()) {
      // Optimization to not build an illumination if not needed.
      return Collections.emptyList();
    }

    final var illumination = getIllumination(view);
    return Stream.of(contributions.values().stream(), personalContributions.values().stream())
        .flatMap(Function.identity())
        .flatMap(Collection::stream)
        .filter(laud -> laud.lightInfo() != null)
        .map(
            laud -> {
              // Make sure each drawable light is restricted to the area it covers, accounting for
              // darkness effects.
              final var obscuredArea = new Area(laud.litArea().area());
              final var lumensLevel =
                  illumination.getObscuredLumensLevel(Math.abs(laud.litArea().lumens()));
              // Should always be present based on construction, but just in case.
              if (lumensLevel.isEmpty()) {
                return null;
              }

              obscuredArea.intersect(
                  laud.litArea().lumens() < 0
                      ? lumensLevel.get().darknessArea()
                      : lumensLevel.get().lightArea());
              return new DrawableLight(
                  laud.lightInfo().lightSource().getType(),
                  laud.lightInfo().light().getPaint(),
                  obscuredArea,
                  laud.litArea.lumens());
            })
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Clear the vision caches (@link #tokenVisionCachePerView}, {@link #visibleAreaMap}), fog cache
   * ({@link #exposedAreaMap}), and illumination caches ({@link #contributedLightsByToken}, {@link
   * #illuminators}).
   *
   * <p>Needs to be called whenever topology changes, fog is edited, or map vision settings are
   * changed. These are all external factors that directly affect vision and illumination. In the
   * future moves on, these situations should be handled by events instead, and the need for an
   * explicit flush should go away or at least be reduced.
   */
  public void flush() {
    contributedLightsByToken.clear();
    contributedPersonalLightsByToken.clear();
    tokenVisibleAreaCache.clear();
    illuminators.clear();

    tokenVisionCachePerView.clear();
    illuminationsPerView.clear();
    exposedAreaMap.clear();
    visibleAreaMap.clear();
    illuminationCache.clear();
  }

  public void flushFog() {
    exposedAreaMap.clear();
  }

  /**
   * Flush the ZoneView cache of the token. Remove token from {@link #tokenVisionCachePerView},
   * {@link #contributedLightsByToken}, and {@link #illuminators}. Can clear {@link
   * #tokenVisionCachePerView}, {@link #visibleAreaMap}, and {@link #exposedAreaMap} depending on
   * the token.
   *
   * @param token the token to flush.
   */
  public void flush(Token token) {
    for (final var cache : tokenVisionCachePerView.values()) {
      cache.remove(token.getId());
    }
    tokenVisibleAreaCache.remove(token.getId());

    // TODO Split logic for light and sight, since the sight portion is entirely duplicated.
    var hadLightSource =
        contributedLightsByToken.values().stream().anyMatch(map -> map.containsKey(token.getId()));
    if (hadLightSource || token.hasLightSources()) {
      // Have to recalculate all token vision
      contributedPersonalLightsByToken.remove(token.getId());
      for (final var entry : contributedLightsByToken.entrySet()) {
        final var illuminator = illuminators.get(entry.getKey());
        if (illuminator == null) {
          continue;
        }
        for (final var litArea :
            entry.getValue().getOrDefault(token.getId(), Collections.emptyList())) {
          illuminator.remove(litArea.litArea());
        }
        entry.getValue().remove(token.getId());
      }

      tokenVisionCachePerView.clear();
      illuminationsPerView.clear();
      exposedAreaMap.clear();
      visibleAreaMap.clear();
      illuminationCache.clear();
    } else if (token.getHasSight()) {
      contributedPersonalLightsByToken.remove(token.getId());
      // TODO Could we instead only clear those views that include the token?
      illuminationsPerView.clear();
      exposedAreaMap.clear();
      visibleAreaMap.clear();
      illuminationCache.clear();
    }
  }

  /**
   * Construct the {@link #visibleAreaMap} entry for a player view.
   *
   * @param view the player view.
   */
  private void calculateVisibleArea(PlayerView view) {
    if (visibleAreaMap.get(view) != null && !visibleAreaMap.get(view).visibleArea.isEmpty()) {
      return;
    }
    // Cache it
    final var illumination = getIllumination(view);
    // We _could_ instead union up all the individual token's areas, but we already have the same
    // result via the view's illumination.
    VisibleAreaMeta meta = new VisibleAreaMeta();
    meta.visibleArea = illumination.getVisibleArea();
    visibleAreaMap.put(view, meta);
  }

  @Subscribe
  private void onTopologyChanged(TopologyChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    flush();
    topologyAreas.clear();
    topologyTrees.clear();
  }

  private boolean flushExistingTokens(List<Token> tokens) {
    boolean tokenChangedTopology = false;
    for (Token token : tokens) {
      if (token.hasAnyTopology()) tokenChangedTopology = true;
      flush(token);
    }
    // Ug, stupid hack here, can't find a bug where if a NPC token is moved before lights are
    // cleared on another token, changes aren't pushed to client?
    // tokenVisionCache.clear();
    return tokenChangedTopology;
  }

  @Subscribe
  private void onTokensAdded(TokensAdded event) {
    if (event.zone() != zone) {
      return;
    }

    boolean tokenChangedTopology = processTokenAddChangeEvent(event.tokens());

    // Moved this event to the bottom so we can check the other events
    // since if a token that has topology is added/removed/edited (rotated/moved/etc)
    // it should also trip a Topology change
    if (tokenChangedTopology) {
      flush();
      topologyAreas.clear();
      topologyTrees.clear();
    }
  }

  @Subscribe
  private void onTokensRemoved(TokensRemoved event) {
    if (event.zone() != zone) {
      return;
    }

    boolean tokenChangedTopology = flushExistingTokens(event.tokens());

    for (Token token : event.tokens()) {
      if (token.hasAnyTopology()) tokenChangedTopology = true;
      for (AttachedLightSource als : token.getLightSources()) {
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource == null) {
          continue;
        }
        Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
        if (lightSet != null) {
          lightSet.remove(token.getId());
        }
      }
    }

    // Moved this event to the bottom so we can check the other events
    // since if a token that has topology is added/removed/edited (rotated/moved/etc)
    // it should also trip a Topology change
    if (tokenChangedTopology) {
      flush();
      topologyAreas.clear();
      topologyTrees.clear();
    }
  }

  @Subscribe
  private void onTokensChanged(TokensChanged event) {
    if (event.zone() != zone) {
      return;
    }

    flushExistingTokens(event.tokens());

    boolean tokenChangedTopology = processTokenAddChangeEvent(event.tokens());

    // Moved this event to the bottom so we can check the other events
    // since if a token that has topology is added/removed/edited (rotated/moved/etc)
    // it should also trip a Topology change
    if (tokenChangedTopology) {
      flush();
      topologyAreas.clear();
      topologyTrees.clear();
    }
  }

  /**
   * Update {@link #lightSourceMap} with the light sources of the tokens, and clear {@link
   * #visibleAreaMap} and {@link #exposedAreaMap} if one of the tokens has sight.
   *
   * @param tokens the list of tokens
   * @return if one of the token has topology or not
   */
  private boolean processTokenAddChangeEvent(List<Token> tokens) {
    boolean hasSight = false;
    boolean hasTopology = false;
    Campaign c = MapTool.getCampaign();

    for (Token token : tokens) {
      boolean hasLightSource =
          token.hasLightSources() && (token.isVisible() || MapTool.getPlayer().isEffectiveGM());
      if (token.hasAnyTopology()) hasTopology = true;
      for (AttachedLightSource als : token.getLightSources()) {
        LightSource lightSource = c.getLightSource(als.getLightSourceId());
        if (lightSource != null) {
          Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
          if (hasLightSource) {
            if (lightSet == null) {
              lightSet = new HashSet<GUID>();
              lightSourceMap.put(lightSource.getType(), lightSet);
            }
            lightSet.add(token.getId());
          } else if (lightSet != null) lightSet.remove(token.getId());
        }
      }
      hasSight |= token.getHasSight();
    }

    if (hasSight) {
      exposedAreaMap.clear();
      visibleAreaMap.clear();
      // Not sure, let's do it for now.
      illuminationCache.clear();
    }

    return hasTopology;
  }

  /** Has a single field: the visibleArea area */
  private static class VisibleAreaMeta {
    Area visibleArea;
  }
}
