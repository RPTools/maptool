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
import java.util.Collection;
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
import net.rptools.maptool.client.AppState;
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
import net.rptools.maptool.model.Zone.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZoneView implements ModelChangeListener {
  private static final Logger log = LogManager.getLogger(ZoneView.class);

  private final Zone zone;

  // VISION
  private final Map<GUID, Area> tokenVisibleAreaCache = new HashMap<GUID, Area>();
  private final Map<GUID, Area> tokenVisionCache = new HashMap<GUID, Area>();
  private final Map<GUID, Map<String, TreeMap<Double, Area>>> lightSourceCache =
      new HashMap<GUID, Map<String, TreeMap<Double, Area>>>();
  private final Map<LightSource.Type, Set<GUID>> lightSourceMap =
      new HashMap<LightSource.Type, Set<GUID>>();
  private final Map<GUID, Map<String, Set<DrawableLight>>> drawableLightCache =
      new HashMap<GUID, Map<String, Set<DrawableLight>>>();
  private final Map<GUID, Map<String, Set<Area>>> brightLightCache =
      new Hashtable<GUID, Map<String, Set<Area>>>();
  private final Map<PlayerView, VisibleAreaMeta> visibleAreaMap =
      new HashMap<PlayerView, VisibleAreaMeta>();
  private final SortedMap<Double, Area> allLightAreaMap =
      new ConcurrentSkipListMap<Double, Area>(); // Hold all of our lights combined by lumens

  // private AreaData topologyAreaData;
  private AreaTree topologyTree;
  private Area tokenTopolgy;

  public ZoneView(Zone zone) {
    this.zone = zone;
    findLightSources();
    zone.addModelChangeListener(this);
  }

  public Area getVisibleArea(PlayerView view) {
    calculateVisibleArea(view);
    ZoneView.VisibleAreaMeta visible = visibleAreaMap.get(view);

    // if (visible == null)
    // System.out.println("ZoneView: visible == null. Please report this on our forum @
    // forum.rptools.net. Thank you!");
    return visible != null ? visible.visibleArea : new Area();
  }

  public boolean isUsingVision() {
    return zone.getVisionType() != Zone.VisionType.OFF;
  }

  // Returns the current combined VBL (base VBL + TokenVBL)
  public synchronized AreaTree getTopologyTree() {
    return getTopologyTree(true);
  }

  // topologyTree is "cached" and should only regenerate when topologyTree is null which should
  // happen on flush calls
  public synchronized AreaTree getTopologyTree(boolean useTokenVBL) {
    if (tokenTopolgy == null && useTokenVBL) {
      log.debug("ZoneView topologyTree is null, generating...");

      tokenTopolgy = new Area(zone.getTopology());
      List<Token> vblTokens =
          MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokensWithVBL();

      for (Token vblToken : vblTokens) {
        tokenTopolgy.add(vblToken.getTransformedVBL());
      }

      topologyTree = new AreaTree(tokenTopolgy);
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

  private TreeMap<Double, Area> getLightSourceArea(Token baseToken, Token lightSourceToken) {
    Map<String, TreeMap<Double, Area>> areaBySightMap =
        lightSourceCache.get(lightSourceToken.getId());
    if (areaBySightMap != null) {
      TreeMap<Double, Area> lightSourceArea = areaBySightMap.get(baseToken.getSightType());
      if (lightSourceArea != null) {
        return lightSourceArea;
      }
    } else {
      areaBySightMap = new HashMap<String, TreeMap<Double, Area>>();
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
      SightType sight = MapTool.getCampaign().getSightType(baseToken.getSightType());
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
    areaBySightMap.put(baseToken.getSightType(), lightSourceAreaMap);
    return lightSourceAreaMap;
  }

  private Area calculatePersonalLightSourceArea(
      LightSource lightSource, Token lightSourceToken, SightType sight, Direction direction) {
    return calculateLightSourceArea(lightSource, lightSourceToken, sight, direction, true);
  }

  private Area calculateLightSourceArea(
      LightSource lightSource, Token lightSourceToken, SightType sight, Direction direction) {
    return calculateLightSourceArea(lightSource, lightSourceToken, sight, direction, false);
  }

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

    if (visibleArea == null) {
      return null;
    }

    if (lightSource.getType() != LightSource.Type.NORMAL) {
      return visibleArea;
    }
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

      if (light.getPaint() != null || isPersonalLight) {
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
    Map<String, Set<DrawableLight>> lightMap = drawableLightCache.get(lightSourceToken.getId());
    if (lightMap == null) {
      lightMap = new HashMap<String, Set<DrawableLight>>();
      drawableLightCache.put(lightSourceToken.getId(), lightMap);
    }
    if (lightMap.get(sight.getName()) != null) {
      lightMap.get(sight.getName()).addAll(lightSet);
    } else {
      lightMap.put(sight.getName(), lightSet);
    }
    Map<String, Set<Area>> brightLightMap = brightLightCache.get(lightSourceToken.getId());
    if (brightLightMap == null) {
      brightLightMap = new HashMap<String, Set<Area>>();
      brightLightCache.put(lightSourceToken.getId(), brightLightMap);
    }
    if (brightLightMap.get(sight.getName()) != null) {
      brightLightMap.get(sight.getName()).addAll(brightLightSet);
    } else {
      brightLightMap.put(sight.getName(), brightLightSet);
    }
    return visibleArea;
  }

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
      Area peronalLightArea = new Area();
      List<Token> lightSourceTokens = new ArrayList<Token>();

      if (lightSourceMap.get(LightSource.Type.NORMAL) != null) {
        for (GUID lightSourceTokenId : lightSourceMap.get(LightSource.Type.NORMAL)) {
          Token lightSourceToken = zone.getToken(lightSourceTokenId);
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
          new CombineLightsSwingWorker(token, lightSourceTokens);
      workerThread.execute();
      try {
        workerThread
            .get(); // Jamz: We need to wait for this thread (which spawns more threads) to finish
        // before we go on
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ExecutionException e) {
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
          peronalLightArea = new Area(tokenVisibleArea);
          peronalLightArea.intersect(lightArea);

          allLightAreaMap.put((double) 100, lightArea);
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
    private final Token baseToken;
    private final List<Token> lightSourceTokens;
    private final ExecutorService lightsThreadPool;
    private final long startTime = System.currentTimeMillis();

    private CombineLightsSwingWorker(Token baseToken, List<Token> lightSourceTokens) {
      this.baseToken = baseToken;
      this.lightSourceTokens = lightSourceTokens;
      lightsThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    protected Void doInBackground() throws Exception {
      for (Token lightSourceToken : lightSourceTokens) {
        CombineLightsTask task = new CombineLightsTask(baseToken, lightSourceToken);
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
  private final class CombineLightsTask implements Callable<TreeMap<Double, Area>> {
    private final Token baseToken;
    private final Token lightSourceToken;

    private CombineLightsTask(Token baseToken, Token lightSourceToken) {
      this.baseToken = baseToken;
      this.lightSourceToken = lightSourceToken;
    }

    @Override
    public TreeMap<Double, Area> call() throws Exception {
      TreeMap<Double, Area> lightArea = getLightSourceArea(baseToken, lightSourceToken);

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
              if ((light.isGM() && !MapTool.getPlayer().isGM())) {
                continue;
              }
              if ((light.isGM() || !token.isVisible())
                  && MapTool.getPlayer().isGM()
                  && AppState.isShowAsPlayer()) {
                continue;
              }
              if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
                continue;
              }
              if (light.isOwnerOnly() && lightSource.getType() == LightSource.Type.AURA) {
                if (!isOwner && !MapTool.getPlayer().isGM()) {
                  continue;
                }
              }
              lightList.add(new DrawableLight(type, light.getPaint(), visibleArea));
            }
          }
        }
      }
    }
    return lightList;
  }

  private void findLightSources() {
    lightSourceMap.clear();

    for (Token token : zone.getAllTokens()) {
      if (token.hasLightSources() && token.isVisible())
        if (!token.isVisibleOnlyToOwner()
            || (token.isVisibleOnlyToOwner() && AppUtil.playerOwns(token))) {
          for (AttachedLightSource als : token.getLightSources()) {
            LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
            if (lightSource == null) {
              continue;
            }
            Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
            if (lightSet == null) {
              lightSet = new HashSet<GUID>();
              lightSourceMap.put(lightSource.getType(), lightSet);
            }
            lightSet.add(token.getId());
          }
        }
    }
  }

  public Set<DrawableLight> getDrawableLights() {
    Set<DrawableLight> lightSet = new HashSet<DrawableLight>();

    for (Map<String, Set<DrawableLight>> map : drawableLightCache.values()) {
      for (Set<DrawableLight> set : map.values()) {
        lightSet.addAll(set);
      }
    }
    return lightSet;
  }

  public Set<Area> getBrightLights() {
    Set<Area> lightSet = new HashSet<Area>();
    // MJ: There seems to be contention for this cache, but that looks inconspicuous enough to
    // try this easy way out. Better: solve the synchronization issues.
    Collection<Map<String, Set<Area>>> copy =
        new ArrayList<Map<String, Set<Area>>>(brightLightCache.values());
    for (Map<String, Set<Area>> map : copy) {
      for (Set<Area> set : map.values()) {
        lightSet.addAll(set);
      }
    }
    return lightSet;
  }

  public void flush() {
    tokenVisibleAreaCache.clear();
    tokenVisionCache.clear();
    lightSourceCache.clear();
    visibleAreaMap.clear();
    drawableLightCache.clear();
    brightLightCache.clear();
  }

  public void flush(Token token) {
    boolean hadLightSource = lightSourceCache.get(token.getId()) != null;

    tokenVisionCache.remove(token.getId());
    tokenVisibleAreaCache.remove(token.getId());
    lightSourceCache.remove(token.getId());
    drawableLightCache.remove(token.getId());
    brightLightCache.remove(token.getId());
    visibleAreaMap.clear();

    if (hadLightSource || token.hasLightSources()) {
      // Have to recalculate all token vision
      tokenVisionCache.clear();
    }
    if (token.getHasSight()) {
      visibleAreaMap.clear();
    }
    // TODO: This fixes a bug with changing vision type, I don't like it though, it needs to be
    // optimized back out
    lightSourceCache.clear();
    // TODO: This fixes a similar bug with turning lights off after moving a different npc token, I
    // don't like it either...
    tokenVisionCache.clear();
  }

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
                new Filter() {
                  public boolean matchToken(Token t) {
                    return t.isToken() && t.getHasSight() && (isGMview || t.isVisible());
                  }
                });

    for (Token token : tokenList) {
      boolean weOwnIt = AppUtil.playerOwns(token);
      // Permission
      if (checkOwnership) {
        if (!weOwnIt) {
          continue;
        }
      } else {
        // If we're viewing the map as a player and the token is not a PC or we're not the GM, then
        // skip it.
        // This used to be the code:
        // if ((token.getType() != Token.Type.PC && !view.isGMView() || (!view.isGMView() &&
        // MapTool.getPlayer().getRole() == Role.GM))) {
        if (!isGMview && (token.getType() != Token.Type.PC || MapTool.getPlayer().isGM())) {
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

  ////
  // MODEL CHANGE LISTENER
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
        Token token = (Token) event.getArg();
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

      // Moved this event to the bottom so we can check the other events
      // since if a token that has VBL is added/removed/edited (rotated/moved/etc)
      // it should also trip a Topology change
      if (evt == Zone.Event.TOPOLOGY_CHANGED || tokenChangedVBL) {
        tokenVisionCache.clear();
        lightSourceCache.clear();
        visibleAreaMap.clear();
        topologyTree = null;
        tokenTopolgy = null;
        tokenVisibleAreaCache.clear();
        // topologyAreaData = null; // Jamz: This isn't used, probably never completed code.
      }
    }
  }

  /** @return */
  private boolean processTokenAddChangeEvent(List<Token> tokens) {
    boolean hasSight = false;
    boolean hasVBL = false;
    Campaign c = MapTool.getCampaign();

    for (Token token : tokens) {
      boolean hasLightSource =
          token.hasLightSources()
              && (token.isVisible() || (MapTool.getPlayer().isGM() && !AppState.isShowAsPlayer()));
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

  private static class VisibleAreaMeta {
    Area visibleArea;
  }
}
