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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
import net.rptools.maptool.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Responsible for calculating lights and vision. */
public class ZoneView implements ModelChangeListener {
  private static final Logger log = LogManager.getLogger(ZoneView.class);

  /** The zone of the ZoneView. */
  private final Zone zone;

  // VISION
  /** Map each token to the area they can see by themselves. */
  private final Map<GUID, Area> tokenVisibleAreaCache = new HashMap<>();
  /** Map each token to their current vision, depending on other lights. */
  private final Map<GUID, Area> tokenVisionCache = new HashMap<>();
  /** Map lightSourceToken to the areaBySightMap. */
  private final Map<GUID, Map<String, Map<Integer, Area>>> lightSourceCache = new HashMap<>();
  /** Map light source type to all tokens with that type. */
  private final Map<LightSource.Type, Set<GUID>> lightSourceMap = new HashMap<>();
  /** Map each token to their map between sightType and set of lights. */
  private final Map<GUID, Map<String, Set<DrawableLight>>> drawableLightCache = new HashMap<>();
  /** Map the PlayerView to its visible area. */
  private final Map<PlayerView, VisibleAreaMeta> visibleAreaMap = new HashMap<>();
  /** Map each token to their personal drawable lights. */
  private final Map<GUID, Set<DrawableLight>> personalDrawableLightCache = new HashMap<>();

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
    zone.addModelChangeListener(this);
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

  /**
   * Return the lightSourceArea of a lightSourceToken for a given sight type. Fill the
   * lightSourceCache entry if null.
   *
   * @param sightName the name of the sight type for which to get the light source area
   * @param lightSourceToken the token holding the light sources.
   * @return the lightSourceArea.
   */
  private Map<Integer, Area> getLightSourceArea(String sightName, Token lightSourceToken) {
    GUID tokenId = lightSourceToken.getId();
    Map<String, Map<Integer, Area>> areaBySightMap = lightSourceCache.get(tokenId);
    if (areaBySightMap != null) {
      Map<Integer, Area> lightSourceArea = areaBySightMap.get(sightName);
      if (lightSourceArea != null) {
        return lightSourceArea;
      }
    } else {
      areaBySightMap = new HashMap<>();
      lightSourceCache.put(lightSourceToken.getId(), areaBySightMap);
    }

    Map<Integer, Area> lightSourceAreaMap = new HashMap<>();

    for (AttachedLightSource attachedLightSource : lightSourceToken.getLightSources()) {
      LightSource lightSource =
          MapTool.getCampaign().getLightSource(attachedLightSource.getLightSourceId());
      if (lightSource == null) {
        continue;
      }
      SightType sight = MapTool.getCampaign().getSightType(sightName);
      Area visibleArea =
          calculateLightSourceArea(
              lightSource, lightSourceToken, sight, attachedLightSource.getDirection());

      if (visibleArea != null && lightSource.getType() == LightSource.Type.NORMAL) {
        var lumens = lightSource.getLumens();
        // Group all the light area's by lumens so there is only one area per lumen value
        if (lightSourceAreaMap.containsKey(lumens)) {
          visibleArea.add(lightSourceAreaMap.get(lumens));
        }
        lightSourceAreaMap.put(lumens, visibleArea);
      }
    }

    // Cache
    areaBySightMap.put(sightName, lightSourceAreaMap);
    return lightSourceAreaMap;
  }

  /**
   * Calculate the area visible by a sight type for a given light, and put the lights in
   * drawableLightCache.
   *
   * @param lightSource the personal light source.
   * @param lightSourceToken the token holding the light source.
   * @param sight the sight type.
   * @param direction the direction of the light source.
   * @return the area visible.
   */
  private Area calculatePersonalLightSourceArea(
      LightSource lightSource, Token lightSourceToken, SightType sight, Direction direction) {
    return calculateLightSourceArea(lightSource, lightSourceToken, sight, direction, true);
  }

  /**
   * Calculate the area visible by a sight type for a given light, and put the lights in
   * drawableLightCache.
   *
   * @param lightSource the light source. Not a personal light.
   * @param lightSourceToken the token holding the light source.
   * @param sight the sight type.
   * @param direction the direction of the light source.
   * @return the area visible.
   */
  private Area calculateLightSourceArea(
      LightSource lightSource, Token lightSourceToken, SightType sight, Direction direction) {
    return calculateLightSourceArea(lightSource, lightSourceToken, sight, direction, false);
  }

  /**
   * Calculate the area visible by a sight type for a given lightSource, and put the lights in
   * drawableLightCache.
   *
   * @param lightSource the light source. Not a personal light.
   * @param lightSourceToken the token holding the light source.
   * @param sight the sight type.
   * @param direction the direction of the light source.
   * @param isPersonalLight is the light a personal light?
   * @return the area visible.
   */
  private Area calculateLightSourceArea(
      LightSource lightSource,
      Token lightSourceToken,
      SightType sight,
      Direction direction,
      boolean isPersonalLight) {
    if (sight == null) {
      return null;
    }
    Point p = FogUtil.calculateVisionCenter(lightSourceToken, zone);
    Area lightSourceArea = lightSource.getArea(lightSourceToken, zone, direction);

    // Calculate exposed area
    // Jamz: OK, let not have lowlight vision type multiply darkness radius
    if (sight.getMultiplier() != 1 && lightSource.getLumens() >= 0) {
      lightSourceArea.transform(
          AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
    }
    Area visibleArea =
        FogUtil.calculateVisibility(
            p.x,
            p.y,
            lightSourceArea,
            getTopologyTree(Zone.TopologyType.WALL_VBL),
            getTopologyTree(Zone.TopologyType.HILL_VBL),
            getTopologyTree(Zone.TopologyType.PIT_VBL));

    if (visibleArea != null && lightSource.getType() == LightSource.Type.NORMAL) {
      addLightSourceToCache(
          visibleArea, p, lightSource, lightSourceToken, sight, direction, isPersonalLight);
    }
    return visibleArea;
  }

  /**
   * Adds the light source as seen by a given sight to the corresponding cache. Lights (but not
   * darkness) with a color CSS value are stored in the drawableLightCache.
   *
   * @param visibleArea the area visible from the light source token
   * @param p the vision center of the light source token
   * @param lightSource the light source
   * @param lightSourceToken the light source token
   * @param sight the sight
   * @param direction the direction of the light source
   */
  private void addLightSourceToCache(
      Area visibleArea,
      Point p,
      LightSource lightSource,
      Token lightSourceToken,
      SightType sight,
      Direction direction,
      boolean isPersonalLight) {
    // Keep track of colored light
    Set<DrawableLight> lightSet = new HashSet<DrawableLight>();
    for (Light light : lightSource.getLightList()) {
      Area lightArea = lightSource.getArea(lightSourceToken, zone, direction, light);
      if (sight.getMultiplier() != 1) {
        lightArea.transform(
            AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
      }
      lightArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
      lightArea.intersect(visibleArea);

      // If a light has no paint, it's a "bright light" that just reveal FoW but doesn't need to be
      // rendered.
      if (light.getPaint() != null || lightSource.getLumens() < 0) {
        lightSet.add(
            new DrawableLight(
                lightSource.getType(), light.getPaint(), lightArea, lightSource.getLumens()));
      }
    }
    // FIXME There was a bug report of a ConcurrentModificationException regarding
    // drawableLightCache.
    // I don't see how, but perhaps this code -- and the ones in flush() and flush(Token) -- should
    // be
    // wrapped in a synchronization block? This method is probably called only on the same thread as
    // getDrawableLights() but the two flush() methods may be called from different threads. How to
    // verify this with Eclipse? Maybe the flush() methods should defer modifications to the
    // EventDispatchingThread?
    if (isPersonalLight) {
      personalDrawableLightCache.put(lightSourceToken.getId(), lightSet);
    } else {
      Map<String, Set<DrawableLight>> lightMap =
          drawableLightCache.computeIfAbsent(lightSourceToken.getId(), k -> new HashMap<>());
      if (lightMap.get(sight.getName()) != null) {
        lightMap.get(sight.getName()).addAll(lightSet);
      } else {
        lightMap.put(sight.getName(), lightSet);
      }
    }
  }

  /**
   * Return the token visible area from tokenVisionCache. If null, create it.
   *
   * @param token the token to get the visible area of.
   * @return the visible area of a token, including the effect of other lights.
   */
  public Area getVisibleArea(Token token) {
    // Sanity
    if (token == null || !token.getHasSight()) {
      return null;
    }

    // Cache ?
    Area tokenVisibleArea = tokenVisionCache.get(token.getId());
    // System.out.println("tokenVisionCache size? " + tokenVisionCache.size());

    if (tokenVisibleArea != null) return tokenVisibleArea;

    SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
    // More sanity checks; maybe sight type removed from campaign after token set?
    if (sight == null) {
      // TODO Should we turn off the token's HasSight flag? Would speed things up for later...
      return null;
    }

    // Combine the player visible area with the available light sources
    tokenVisibleArea = tokenVisibleAreaCache.get(token.getId());
    if (tokenVisibleArea == null) {
      Point p = FogUtil.calculateVisionCenter(token, zone);
      Area visibleArea = sight.getVisionShape(token, zone);
      tokenVisibleArea =
          FogUtil.calculateVisibility(
              p.x,
              p.y,
              visibleArea,
              getTopologyTree(Zone.TopologyType.WALL_VBL),
              getTopologyTree(Zone.TopologyType.HILL_VBL),
              getTopologyTree(Zone.TopologyType.PIT_VBL));

      tokenVisibleAreaCache.put(token.getId(), tokenVisibleArea);
    }

    // Stopwatch stopwatch = Stopwatch.createStarted();

    // Combine in the visible light areas
    if (tokenVisibleArea != null) {
      Rectangle2D origBounds = tokenVisibleArea.getBounds();
      List<Token> lightSourceTokens = new ArrayList<Token>();

      // Add the tokens from the lightSourceMap with normal (not aura) lights
      if (lightSourceMap.get(LightSource.Type.NORMAL) != null) {
        for (GUID lightSourceTokenId : lightSourceMap.get(LightSource.Type.NORMAL)) {
          Token lightSourceToken = zone.getToken(lightSourceTokenId);
          // Verify if the token still exists
          if (lightSourceToken != null) {
            lightSourceTokens.add(lightSourceToken);
          }
        }
      }

      if (token.hasLightSources() && !lightSourceTokens.contains(token)) {
        // This accounts for temporary tokens (such as during an Expose Last Path)
        lightSourceTokens.add(token);
      }

      // stopwatch.reset();
      // stopwatch.start();
      // Jamz: Iterate through all tokens and combine light areas by lumens
      /* Hold all of our lights combined by lumens. Used for hard FoW reveal. */
      final SortedMap<Integer, Path2D> allLightAreaMap =
          new TreeMap<>(
              (lhsLumens, rhsLumens) -> {
                int comparison = Integer.compare(lhsLumens, rhsLumens);
                if (comparison == 0) {
                  // Values are equal. Not much else to do.
                  return 0;
                }

                // Primarily order lumens by magnitude.
                int absComparison = Integer.compare(Math.abs(lhsLumens), Math.abs(rhsLumens));
                if (absComparison != 0) {
                  return absComparison;
                }

                // At this point we know have different values with the same magnitude. I.e., one
                // value is
                // positive and the other negative. We want negative values to come after positive
                // values,
                // which is simply the opposite of the natural order.
                return -comparison;
              });

      getLightAreasByLumens(allLightAreaMap, token.getSightType(), lightSourceTokens);

      // Check for daylight and add it to the overall light map.
      if (zone.getVisionType() != Zone.VisionType.NIGHT) {
        // Treat the entire visible area like a light source of minimal lumens.
        addLightAreaByLumens(allLightAreaMap, 1, tokenVisibleArea);
      }

      // Check for personal vision and add to overall light map
      if (sight.hasPersonalLightSource()) {
        Area lightArea =
            calculatePersonalLightSourceArea(
                sight.getPersonalLightSource(), token, sight, Direction.CENTER);
        if (lightArea != null) {
          var lumens = sight.getPersonalLightSource().getLumens();
          lumens = (lumens == 0) ? LUMEN_VISION : lumens;
          // maybe some kind of imposed blindness?  Anyway, make sure to handle personal darkness..
          addLightAreaByLumens(allLightAreaMap, lumens, lightArea);
        }
      }

      // Jamz: OK, we should have ALL light areas in one map sorted by lumens. Lets apply it to the
      // map
      Area allLightArea = new Area();
      for (Entry<Integer, Path2D> light : allLightAreaMap.entrySet()) {
        final var lightPath = light.getValue();
        boolean isDarkness = false;
        if (light.getKey() < 0) isDarkness = true;

        if (origBounds.intersects(lightPath.getBounds2D())) {
          Area intersection = new Area(tokenVisibleArea);
          intersection.intersect(new Area(lightPath));
          if (isDarkness) {
            allLightArea.subtract(intersection);
          } else {
            allLightArea.add(intersection);
          }
        }
      }
      allLightAreaMap.clear(); // Dispose of object, only needed for the scope of this method

      tokenVisibleArea = allLightArea;
    }

    tokenVisionCache.put(token.getId(), tokenVisibleArea);

    // log.info("getVisibleArea: \t\t" + stopwatch);

    return tokenVisibleArea;
  }

  private static void addLightAreaByLumens(
      Map<Integer, Path2D> lightAreasByLumens, int lumens, Shape area) {
    var totalPath = lightAreasByLumens.computeIfAbsent(lumens, key -> new Path2D.Double());
    totalPath.append(area.getPathIterator(null, 1), false);
  }

  private void getLightAreasByLumens(
      Map<Integer, Path2D> allLightPathMap, String sightName, List<Token> lightSourceTokens) {
    for (Token lightSourceToken : lightSourceTokens) {
      Map<Integer, Area> lightArea = getLightSourceArea(sightName, lightSourceToken);

      for (Entry<Integer, Area> light : lightArea.entrySet()) {
        // Add the token's light area to the global area in `allLightPathMap`.
        addLightAreaByLumens(allLightPathMap, light.getKey(), light.getValue());
      }
    }
  }

  /**
   * Get the lists of drawable light from lightSourceMap.
   *
   * @param type the type of lights to get.
   * @return the list of drawable lights of the given type.
   */
  public List<DrawableLight> getLights(LightSource.Type type) {
    List<DrawableLight> lightList = new LinkedList<DrawableLight>();
    if (lightSourceMap.get(type) != null) {
      for (GUID lightSourceToken : lightSourceMap.get(type)) {
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
          if (lightSource.getType() == type) {
            // This needs to be cached somehow
            Area lightSourceArea = lightSource.getArea(token, zone, Direction.CENTER);
            Area visibleArea =
                FogUtil.calculateVisibility(
                    p.x,
                    p.y,
                    lightSourceArea,
                    getTopologyTree(Zone.TopologyType.WALL_VBL),
                    getTopologyTree(Zone.TopologyType.HILL_VBL),
                    getTopologyTree(Zone.TopologyType.PIT_VBL));
            if (visibleArea == null) {
              continue;
            }
            for (Light light : lightSource.getLightList()) {
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
              if (light.isOwnerOnly()
                  && lightSource.getType() == LightSource.Type.AURA
                  && !isOwner
                  && !MapTool.getPlayer().isEffectiveGM()) {
                continue;
              }
              lightList.add(
                  new DrawableLight(type, light.getPaint(), visibleArea, lightSource.getLumens()));
            }
          }
        }
      }
    }
    return lightList;
  }

  /** Find the light sources from all appropriate tokens, and store them in lightSourceMap. */
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

  /**
   * Get the drawable lights from the drawableLightCache and from personalDrawableLightCache.
   *
   * @param view the player view for which to get the personal lights.
   * @return the set of drawable lights.
   */
  public Set<DrawableLight> getDrawableLights(PlayerView view) {
    Set<DrawableLight> lightSet = new HashSet<DrawableLight>();

    for (Map<String, Set<DrawableLight>> map : drawableLightCache.values()) {
      for (Set<DrawableLight> set : map.values()) {
        lightSet.addAll(set);
      }
    }
    if (view != null && view.isUsingTokenView()) {
      // Get the personal drawable lights of the tokens of the player view
      for (Token token : view.getTokens()) {
        Set<DrawableLight> lights = personalDrawableLightCache.get(token.getId());
        if (lights != null) {
          lightSet.addAll(lights);
        }
      }
    }
    return lightSet;
  }

  /**
   * Clear the tokenVisibleAreaCache, tokenVisionCache, lightSourceCache, visibleAreaMap,
   * drawableLightCache, and personal drawable light caches.
   */
  public void flush() {
    tokenVisibleAreaCache.clear();
    tokenVisionCache.clear();
    lightSourceCache.clear();
    visibleAreaMap.clear();
    drawableLightCache.clear();
    personalDrawableLightCache.clear();
  }

  /**
   * Flush the ZoneView cache of the token. Remove token from tokenVisibleAreaCache,
   * tokenVisionCache, lightSourceCache, drawableLightCache, and personal light caches. Can clear
   * tokenVisionCache and visibleAreaMap depending on the token.
   *
   * @param token the token to flush.
   */
  public void flush(Token token) {
    boolean hadLightSource = lightSourceCache.get(token.getId()) != null;

    tokenVisionCache.remove(token.getId());
    tokenVisibleAreaCache.remove(token.getId());
    lightSourceCache.remove(token.getId());
    drawableLightCache.remove(token.getId());
    personalDrawableLightCache.remove(token.getId());

    if (hadLightSource || token.hasLightSources()) {
      // Have to recalculate all token vision
      tokenVisionCache.clear();
      visibleAreaMap.clear();
    } else if (token.getHasSight()) {
      visibleAreaMap.clear();
    }
  }

  /**
   * Construct the visibleAreaMap entry for a player view.
   *
   * @param view the player view.
   */
  private void calculateVisibleArea(PlayerView view) {
    if (visibleAreaMap.get(view) != null
        && visibleAreaMap.get(view).visibleArea.getBounds().getCenterX() != 0.0d) {
      return;
    }
    // Cache it
    VisibleAreaMeta meta = new VisibleAreaMeta();
    meta.visibleArea = new Area();

    visibleAreaMap.put(view, meta);

    // Calculate it
    final boolean isGMview = view.isGMView();
    final boolean checkOwnership =
        MapTool.getServerPolicy().isUseIndividualViews() || MapTool.isPersonalServer();
    List<Token> tokenList =
        view.isUsingTokenView()
            ? view.getTokens()
            : zone.getTokensFiltered(
                t -> t.isToken() && t.getHasSight() && (isGMview || t.isVisible()));

    for (Token token : tokenList) {
      boolean weOwnIt = AppUtil.playerOwns(token);
      // Permission
      if (checkOwnership) {
        if (!weOwnIt) {
          continue;
        }
      } else {
        // If we're viewing the map as a player and the token is not a PC, then skip it.
        if (!isGMview && token.getType() != Token.Type.PC && !AppUtil.ownedByOnePlayer(token)) {
          continue;
        }
      }
      // player ownership permission
      if (token.isVisibleOnlyToOwner() && !weOwnIt) {
        continue;
      }
      Area tokenVision = getVisibleArea(token);
      if (tokenVision != null) {
        meta.visibleArea.add(tokenVision);
      }
    }

    // System.out.println("calculateVisibleArea: " + (System.currentTimeMillis() - startTime) +
    // "ms");
  }

  /**
   * MODEL CHANGE LISTENER for events TOKEN_CHANGED, TOKEN_REMOVED, TOKEN_ADDED.
   *
   * @param event the event.
   */
  public void modelChanged(ModelChangeEvent event) {
    Object evt = event.getEvent();
    if (event.getModel() instanceof Zone) {
      boolean tokenChangedTopology = false;

      if (evt == Zone.Event.TOKEN_CHANGED || evt == Zone.Event.TOKEN_REMOVED) {
        for (Token token : event.getTokensAsList()) {
          if (token.hasAnyTopology()) tokenChangedTopology = true;
          flush(token);
        }
        // Ug, stupid hack here, can't find a bug where if a NPC token is moved before lights are
        // cleared on another token, changes aren't pushed to client?
        // tokenVisionCache.clear();
      }

      if (evt == Zone.Event.TOKEN_ADDED || evt == Zone.Event.TOKEN_CHANGED) {
        tokenChangedTopology = processTokenAddChangeEvent(event.getTokensAsList());
      }

      if (evt == Zone.Event.TOKEN_REMOVED) {
        for (Token token : event.getTokensAsList()) {
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
      }

      // Moved this event to the bottom so we can check the other events
      // since if a token that has topology is added/removed/edited (rotated/moved/etc)
      // it should also trip a Topology change
      if (evt == Zone.Event.TOPOLOGY_CHANGED || tokenChangedTopology) {
        tokenVisionCache.clear();
        lightSourceCache.clear();
        drawableLightCache.clear();
        personalDrawableLightCache.clear();
        visibleAreaMap.clear();
        topologyAreas.clear();
        topologyTrees.clear();
        tokenVisibleAreaCache.clear();

        // topologyAreaData = null; // Jamz: This isn't used, probably never completed code.
      }
    }
  }

  /**
   * Update lightSourceMap with the light sources of the tokens, and clear visibleAreaMap if one of
   * the tokens has sight.
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

    if (hasSight) visibleAreaMap.clear();

    return hasTopology;
  }

  /** Has a single field: the visibleArea area */
  private static class VisibleAreaMeta {
    Area visibleArea;
  }
}
