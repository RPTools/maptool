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

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
import net.rptools.maptool.model.AttachedLightSource;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.Direction;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
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
  private final Map<GUID, Map<String, Map<Double, Area>>> lightSourceCache = new HashMap<>();
  /** Map light source type to all tokens with that type. */
  private final Map<LightSource.Type, Set<GUID>> lightSourceMap = new HashMap<>();
  /** Map each token to their map between sightType and set of lights. */
  private final Map<GUID, Map<String, Set<DrawableLight>>> drawableLightCache = new HashMap<>();
  /** Map each token to their map between sightType and set of bright lights. */
  private final Map<GUID, Map<String, Set<Area>>> brightLightCache = new Hashtable<>();
  /** Map the PlayerView to its visible area. */
  private final Map<PlayerView, VisibleAreaMeta> visibleAreaMap = new HashMap<>();
  /** Hold all of our lights combined by lumens. Used for hard FoW reveal. */
  private final SortedMap<Double, Area> allLightAreaMap = new ConcurrentSkipListMap<>();
  /** Map each token to their personal bright light source area. */
  private final Map<GUID, Set<Area>> personalBrightLightCache = new HashMap<>();
  /** Map each token to their personal drawable lights. */
  private final Map<GUID, Set<DrawableLight>> personalDrawableLightCache = new HashMap<>();

  /** The digested topology of the map VBL, and possibly tokens VBL. */
  private AreaTree topologyTree;
  /** The VBL area of the zone VBL and the tokens VBL. */
  private Area tokenTopology;

  /** Lumen for personal vision (darkvision). */
  private static final double LUMEN_VISION = 100;

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

  /** @return the current combined VBL (base VBL + TokenVBL) */
  public synchronized AreaTree getTopologyTree() {
    return getTopologyTree(true);
  }

  /**
   * Get the topologyTree. The topologyTree is "cached" and should only regenerate when topologyTree
   * is null which should happen on flush calls.
   *
   * @param useTokenVBL using token VBL? If so and topology null, create one from VBL tokens.
   * @return the AreaTree (topologyTree).
   */
  public synchronized AreaTree getTopologyTree(boolean useTokenVBL) {
    if (tokenTopology == null && useTokenVBL) {
      log.debug("ZoneView topologyTree is null, generating...");

      tokenTopology = new Area(zone.getTopology());
      List<Token> vblTokens =
          MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokensWithVBL();

      for (Token vblToken : vblTokens) {
        tokenTopology.add(vblToken.getTransformedVBL());
      }

      topologyTree = new AreaTree(tokenTopology);
    } else if (topologyTree == null) {
      topologyTree = new AreaTree(zone.getTopology());
    }

    return topologyTree;
  }

  // Jamz: This function and such "AreaData" never seems to get used...either old or future code?
  // public AreaData getTopologyAreaData() {
  // if (topologyAreaData == null) {
  // topologyAreaData = new AreaData(zone.getTopology());
  // topologyAreaData.digest();
  // }
  // return topologyAreaData;
  // }

  /**
   * Return the lightSourceArea of a lightSourceToken for a given sight type. Fill the
   * lightSourceCache entry if null.
   *
   * @param sightName the name of the sight type for which to get the light source area
   * @param lightSourceToken the token holding the light sources.
   * @return the lightSourceArea.
   */
  private Map<Double, Area> getLightSourceArea(String sightName, Token lightSourceToken) {
    GUID tokenId = lightSourceToken.getId();
    Map<String, Map<Double, Area>> areaBySightMap = lightSourceCache.get(tokenId);
    if (areaBySightMap != null) {
      Map<Double, Area> lightSourceArea = areaBySightMap.get(sightName);
      if (lightSourceArea != null) {
        return lightSourceArea;
      }
    } else {
      areaBySightMap = new HashMap<>();
      lightSourceCache.put(lightSourceToken.getId(), areaBySightMap);
    }

    // Calculate
    TreeMap<Double, Area> lightSourceAreaMap = new TreeMap<Double, Area>();

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
        double lumens = lightSource.getLumens();
        if (lumens < 0) lumens = Math.abs(lumens) + .5;

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
   * drawableLightCache and brightLightCache.
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
   * drawableLightCache and brightLightCache.
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
   * drawableLightCache and brightLightCache.
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
    Area visibleArea = FogUtil.calculateVisibility(p.x, p.y, lightSourceArea, getTopologyTree());

    if (visibleArea != null && lightSource.getType() == LightSource.Type.NORMAL) {
      addLightSourceToCache(
          visibleArea, p, lightSource, lightSourceToken, sight, direction, isPersonalLight);
    }
    return visibleArea;
  }

  /**
   * Adds the light source as seen by a given sight to the corresponding cache. Lights with a color
   * CSS value are stored in the drawableLightCache, while lights without are stored in
   * brightLightCache.
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
    Set<Area> brightLightSet = new HashSet<Area>();
    for (Light light : lightSource.getLightList()) {
      Area lightArea = lightSource.getArea(lightSourceToken, zone, direction, light);
      if (sight.getMultiplier() != 1) {
        lightArea.transform(
            AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
      }
      lightArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
      lightArea.intersect(visibleArea);

      if (light.getPaint() != null) {
        lightSet.add(new DrawableLight(lightSource.getType(), light.getPaint(), lightArea));
      } else {
        brightLightSet.add(lightArea);
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
    if (isPersonalLight) {
      personalBrightLightCache.put(lightSourceToken.getId(), brightLightSet);
    } else {
      Map<String, Set<Area>> brightLightMap =
          brightLightCache.computeIfAbsent(lightSourceToken.getId(), k -> new HashMap<>());
      if (brightLightMap.get(sight.getName()) != null) {
        brightLightMap.get(sight.getName()).addAll(brightLightSet);
      } else {
        brightLightMap.put(sight.getName(), brightLightSet);
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
      tokenVisibleArea = FogUtil.calculateVisibility(p.x, p.y, visibleArea, getTopologyTree());

      tokenVisibleAreaCache.put(token.getId(), tokenVisibleArea);
    }

    // Stopwatch stopwatch = Stopwatch.createStarted();

    // Combine in the visible light areas
    // Jamz TODO: add condition for daylight and darkness! Currently no darkness in daylight
    if (tokenVisibleArea != null && zone.getVisionType() == Zone.VisionType.NIGHT) {
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
      CombineLightsSwingWorker workerThread =
          new CombineLightsSwingWorker(token.getSightType(), lightSourceTokens);
      workerThread.execute();
      try {
        workerThread
            .get(); // Jamz: We need to wait for this thread (which spawns more threads) to finish
        // before we go on
      } catch (InterruptedException | ExecutionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // log.info("CombineLightsSwingWorker: \t" + stopwatch);

      // Check for personal vision and add to overall light map
      if (sight.hasPersonalLightSource()) {
        Area lightArea =
            calculatePersonalLightSourceArea(
                sight.getPersonalLightSource(), token, sight, Direction.CENTER);
        if (lightArea != null) {
          double lumens = sight.getPersonalLightSource().getLumens();
          lumens = (lumens == 0) ? LUMEN_VISION : lumens;
          // maybe some kind of imposed blindness?  Anyway, make sure to handle personal darkness..
          if (lumens < 0) lumens = Math.abs(lumens) + .5;
          if (allLightAreaMap.containsKey(lumens)) {
            allLightAreaMap.get(lumens).add(lightArea);
          } else {
            allLightAreaMap.put(lumens, lightArea);
          }
        }
      }

      // Jamz: OK, we should have ALL light areas in one map sorted by lumens. Lets apply it to the
      // map
      Area allLightArea = new Area();
      for (Entry<Double, Area> light : allLightAreaMap.entrySet()) {
        boolean isDarkness = false;
        // Jamz: negative lumens were converted to absolute value + .5 to sort lights
        // in tree map, so non-integers == darkness and lights are draw/removed in order
        // of lumens and darkness with equal lumens are drawn second due to the added .5
        if (light.getKey().intValue() != light.getKey()) isDarkness = true;

        if (origBounds.intersects(light.getValue().getBounds2D())) {
          Area intersection = new Area(tokenVisibleArea);
          intersection.intersect(light.getValue());
          if (isDarkness) {
            allLightArea.subtract(intersection);
          } else {
            allLightArea.add(intersection);
          }
        }
      }

      tokenVisibleArea = allLightArea;
    }

    allLightAreaMap.clear(); // Dispose of object, only needed for the scope of this method
    tokenVisionCache.put(token.getId(), tokenVisibleArea);

    // log.info("getVisibleArea: \t\t" + stopwatch);

    return tokenVisibleArea;
  }

  private class CombineLightsSwingWorker extends SwingWorker<Void, List<Token>> {
    private final String sightName;
    private final List<Token> lightSourceTokens;
    private final ExecutorService lightsThreadPool;
    private final long startTime = System.currentTimeMillis();

    private CombineLightsSwingWorker(String sightName, List<Token> lightSourceTokens) {
      this.sightName = sightName;
      this.lightSourceTokens = lightSourceTokens;
      lightsThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    protected Void doInBackground() throws Exception {
      for (Token lightSourceToken : lightSourceTokens) {
        CombineLightsTask task = new CombineLightsTask(sightName, lightSourceToken);
        lightsThreadPool.submit(task);
      }

      lightsThreadPool.shutdown();
      lightsThreadPool.awaitTermination(3, TimeUnit.MINUTES);

      return null;
    }

    @Override
    public void done() {
      lightsThreadPool.shutdown(); // always reclaim resources just in case?
      // System.out.println("Time to calculated lights for token: " + baseToken.getName() + ", " +
      // (System.currentTimeMillis() - startTime) + "ms");

      return;
    }
  }

  /**
   * @author Jamz
   *     <p>A Callable task add to the ExecutorCompletionService to combine lights as a threaded
   *     task
   */
  private final class CombineLightsTask implements Callable<Map<Double, Area>> {
    private final String sightName;
    private final Token lightSourceToken;

    private CombineLightsTask(String sightName, Token lightSourceToken) {
      this.sightName = sightName;
      this.lightSourceToken = lightSourceToken;
    }

    @Override
    public Map<Double, Area> call() {
      Map<Double, Area> lightArea = getLightSourceArea(sightName, lightSourceToken);

      for (Entry<Double, Area> light : lightArea.entrySet()) {
        // Area tempArea = light.getValue();
        Path2D path = new Path2D.Double();
        path.append(light.getValue().getPathIterator(null, 1), false);

        synchronized (allLightAreaMap) {
          if (allLightAreaMap.containsKey(light.getKey())) {
            // Area allLight = allLightAreaMap.get(light.getKey());
            // tempArea.add(allLight);

            // Path2D is faster than Area it looks like
            path.append(allLightAreaMap.get(light.getKey()).getPathIterator(null, 1), false);
          }

          // allLightAreaMap.put(light.getKey(), tempArea);
          allLightAreaMap.put(light.getKey(), new Area(path));
        }
      }

      return lightArea;
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
                FogUtil.calculateVisibility(p.x, p.y, lightSourceArea, getTopologyTree());
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
              lightList.add(new DrawableLight(type, light.getPaint(), visibleArea));
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
   * @param view the player view for which to get the personal bright lights.
   * @return the set of drawable lights.
   */
  public Set<DrawableLight> getDrawableLights(PlayerView view) {
    Set<DrawableLight> lightSet = new HashSet<DrawableLight>();

    for (Map<String, Set<DrawableLight>> map : drawableLightCache.values()) {
      for (Set<DrawableLight> set : map.values()) {
        lightSet.addAll(set);
      }
    }
    if (view != null && view.getTokens() != null) {
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
   * Get the drawable lights from the brightLightCache and from personalBrightLightCache.
   *
   * @param view the player view for which to get the personal bright lights.
   * @return the set of bright lights.
   */
  public Set<Area> getBrightLights(PlayerView view) {
    Set<Area> lightSet = new HashSet<Area>();
    // MJ: There seems to be contention for this cache, but that looks inconspicuous enough to
    // try this easy way out. Better: solve the synchronization issues.
    List<Map<String, Set<Area>>> copy = new ArrayList<>(brightLightCache.values());
    for (Map<String, Set<Area>> map : copy) {
      for (Set<Area> set : map.values()) {
        lightSet.addAll(set);
      }
    }
    if (view != null && view.getTokens() != null) {
      // Get the personal bright lights of the tokens of the player view
      for (Token token : view.getTokens()) {
        Set<Area> areas = personalBrightLightCache.get(token.getId());
        if (areas != null) {
          lightSet.addAll(areas);
        }
      }
    }
    return lightSet;
  }

  /**
   * Clear the tokenVisibleAreaCache, tokenVisionCache, lightSourceCache, visibleAreaMap,
   * drawableLightCache, brightLightCache, and personal drawable/bright light caches.
   */
  public void flush() {
    tokenVisibleAreaCache.clear();
    tokenVisionCache.clear();
    lightSourceCache.clear();
    visibleAreaMap.clear();
    drawableLightCache.clear();
    brightLightCache.clear();
    personalDrawableLightCache.clear();
    personalBrightLightCache.clear();
  }

  /**
   * Flush the ZoneView cache of the token. Remove token from tokenVisibleAreaCache,
   * tokenVisionCache, lightSourceCache, drawableLightCache, brightLightCache, and personal light
   * caches. Can clear tokenVisionCache and visibleAreaMap depending on the token.
   *
   * @param token the token to flush.
   */
  public void flush(Token token) {
    boolean hadLightSource = lightSourceCache.get(token.getId()) != null;

    tokenVisionCache.remove(token.getId());
    tokenVisibleAreaCache.remove(token.getId());
    lightSourceCache.remove(token.getId());
    drawableLightCache.remove(token.getId());
    brightLightCache.remove(token.getId());
    personalDrawableLightCache.remove(token.getId());
    personalBrightLightCache.remove(token.getId());

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
  @SuppressWarnings("unchecked")
  public void modelChanged(ModelChangeEvent event) {
    Object evt = event.getEvent();
    if (event.getModel() instanceof Zone) {
      boolean tokenChangedVBL = false;

      if (evt == Zone.Event.TOKEN_CHANGED || evt == Zone.Event.TOKEN_REMOVED) {
        if (event.getArg() instanceof List<?>) {
          @SuppressWarnings("unchecked")
          List<Token> list = (List<Token>) (event.getArg());
          for (Token token : list) {
            if (token.hasVBL()) tokenChangedVBL = true;
            flush(token);
          }
        } else {
          final Token token = (Token) event.getArg();
          if (token.hasVBL()) tokenChangedVBL = true;
          flush(token);
        }
        // Ug, stupid hack here, can't find a bug where if a NPC token is moved before lights are
        // cleared on another token, changes aren't pushed to client?
        // tokenVisionCache.clear();
      }

      if (evt == Zone.Event.TOKEN_ADDED || evt == Zone.Event.TOKEN_CHANGED) {
        Object o = event.getArg();
        List<Token> tokens = null;
        if (o instanceof Token) {
          tokens = new ArrayList<Token>(1);
          tokens.add((Token) o);
        } else {
          tokens = (List<Token>) o;
        }

        tokenChangedVBL = processTokenAddChangeEvent(tokens);
      }

      if (evt == Zone.Event.TOKEN_REMOVED) {
        Object o = event.getArg();
        List<Token> tokens;
        if (o instanceof Token) {
          tokens = new ArrayList<>(1);
          tokens.add((Token) o);
        } else {
          tokens = (List<Token>) o;
        }

        for (Token token : tokens) {
          if (token.hasVBL()) tokenChangedVBL = true;
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
      // since if a token that has VBL is added/removed/edited (rotated/moved/etc)
      // it should also trip a Topology change
      if (evt == Zone.Event.TOPOLOGY_CHANGED || tokenChangedVBL) {
        tokenVisionCache.clear();
        lightSourceCache.clear();
        brightLightCache.clear();
        drawableLightCache.clear();
        personalBrightLightCache.clear();
        personalDrawableLightCache.clear();
        visibleAreaMap.clear();
        topologyTree = null;
        tokenTopology = null;
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
   * @return if one of the token has VBL or not
   */
  private boolean processTokenAddChangeEvent(List<Token> tokens) {
    boolean hasSight = false;
    boolean hasVBL = false;
    Campaign c = MapTool.getCampaign();

    for (Token token : tokens) {
      boolean hasLightSource =
          token.hasLightSources() && (token.isVisible() || MapTool.getPlayer().isEffectiveGM());
      if (token.hasVBL()) hasVBL = true;
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

    return hasVBL;
  }

  /** Has a single field: the visibleArea area */
  private static class VisibleAreaMeta {
    Area visibleArea;
  }
}
