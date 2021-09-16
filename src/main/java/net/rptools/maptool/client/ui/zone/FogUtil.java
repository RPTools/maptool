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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.vbl.AreaContainer;
import net.rptools.maptool.client.ui.zone.vbl.AreaIsland;
import net.rptools.maptool.client.ui.zone.vbl.AreaOcean;
import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
import net.rptools.maptool.client.ui.zone.vbl.VisibleAreaSegment;
import net.rptools.maptool.model.AbstractPoint;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ExposedAreaMetaData;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.GridCapabilities;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.player.Player.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.union.UnaryUnionOp;

public class FogUtil {
  private static final Logger log = LogManager.getLogger(FogUtil.class);
  private static final PrecisionModel precisionModel = new PrecisionModel(100000);
  private static final GeometryFactory geometryFactory = new GeometryFactory(precisionModel);

  /**
   * Return the visible area for an origin, a lightSourceArea and a VBL.
   *
   * @param x the x vision origin.
   * @param y the y vision origin.
   * @param vision the lightSourceArea.
   * @param topology the VBL topology.
   * @return the visible area.
   */
  public static Area calculateVisibility(
      int x, int y, Area vision, AreaTree topology, AreaTree terrainVbl) {
    vision = new Area(vision);
    vision.transform(AffineTransform.getTranslateInstance(x, y));

    final Point origin = new Point(x, y);

    List<VisibleAreaSegment> visionBlockingSegments = new ArrayList<>();
    var vblBlockingSegments = findVisibleAreaSegments(topology, origin);
    var terrainVblBlockingSegments = findVisibleAreaSegments(terrainVbl, origin);

    if (vblBlockingSegments == null || terrainVblBlockingSegments == null) {
      // Vision has been completely blocked by topology.
      return null;
    }

    visionBlockingSegments.addAll(vblBlockingSegments);
    visionBlockingSegments.addAll(terrainVblBlockingSegments);

    Geometry totalClearedArea = calculateShadows(visionBlockingSegments);

    if (totalClearedArea != null) {
      // Convert back to AWT area to modify vision.
      var shapeWriter = new ShapeWriter();
      var area = new Area(shapeWriter.toShape(totalClearedArea));
      vision.subtract(area);
    }

    // For simplicity, this catches some of the edge cases
    return vision;
  }

  private static @Nullable List<VisibleAreaSegment> findVisibleAreaSegments(
      AreaTree topology, Point origin) {
    AreaOcean ocean = topology.getOceanAt(origin);
    if (ocean == null) {
      // Should never happen since the global ocean should catch everything.
      return null;
    }
    final AreaIsland island =
        ocean.getIslands().stream()
            .filter(i -> i.getBounds().contains(origin))
            .findFirst()
            .orElse(null);
    final List<VisibleAreaSegment> visionBlockingSegments = new ArrayList<>();
    final BiConsumer<AreaContainer, Boolean> addVisionBlockingSegments =
        (container, frontSide) ->
            visionBlockingSegments.addAll(
                container.getVisibleBoundarySegements(geometryFactory, origin, frontSide));

    if (island != null) {
      // We're in an island. For normal VBL, vision is entirely blocked. But for terrain VBL,
      // vision can continue until we hit the front of any contained ocean or the boundary of
      // the island.

      if (!island.isTerrain()) {
        // Since we're contained in a non-terrain island, there can be no vision through it.
        return null;
      } else {
        // Since we're inside this island, the facing edges are like the back side.
        addVisionBlockingSegments.accept(island, false);

        for (var nestedOcean : island.getOceans()) {
          // Shadow cast the front side of the ocean.
          addVisionBlockingSegments.accept(nestedOcean, true);
        }
      }
    } else {
      // We're in an ocean. Vision is blocked in one way or another by the parent island and
      // containing island.
      // For the parent island, if it is regular VBL, vision is blocked by the near side of the
      // island, which is defined as the boundary of this ocean. If it is terrain VBL, vision
      // is blocked by the boundary of the parent island, as well as the front side of the
      // boundary of any other ocean contained in the same island.
      // For any contained island, if it is regular VBL, vision is blocked by the front side of
      // the boundary of the island. If it is terrain VBL, vision is blocked by the back side of
      // the boundary of the island, as well as the front side of any oceans contained within.

      // Check the parent island.
      final var parentIsland = ocean.getParentIsland();
      if (parentIsland != null) {
        if (!parentIsland.isTerrain()) {
          // Shadow cast the near edge of the island, aka the boundary of this ocean.
          // Since we're inside this ocean, the facing edges are like the back side.
          addVisionBlockingSegments.accept(ocean, false);
        } else {
          // Since we're inside this island, the boundary is like the back side.
          addVisionBlockingSegments.accept(parentIsland, false);

          for (var siblingOcean : parentIsland.getOceans()) {
            // Note that it is important to shadow cast even when `siblingOcean == ocean`. This is
            // because a single ocean can have concave sections with intervening VBL from the
            // containing island.
            // Shadow cast the front side of the ocean.
            addVisionBlockingSegments.accept(siblingOcean, true);
          }
        }
      }

      // Check each contained island.
      for (var containedIsland : ocean.getIslands()) {
        if (!containedIsland.isTerrain()) {
          // Shadow cast the front side of the island.
          addVisionBlockingSegments.accept(containedIsland, true);
        } else {
          // Shadow cast the backside of the island.
          addVisionBlockingSegments.accept(containedIsland, false);

          for (var nestedOcean : containedIsland.getOceans()) {
            // Shadow cast the front side of the ocean.
            addVisionBlockingSegments.accept(nestedOcean, true);
          }
        }
      }
    }

    return visionBlockingSegments;
  }

  private static @Nullable Geometry calculateShadows(
      List<VisibleAreaSegment> visionBlockingSegments) {
    int skippedAreas = 0;
    Collections.sort(visionBlockingSegments);
    List<PreparedGeometry> clearedAreaList = new ArrayList<>();
    nextSegment:
    for (var segment : visionBlockingSegments) {
      Geometry boundingBox = segment.getBoundingBox();
      for (var clearedArea : clearedAreaList) {
        if (clearedArea.contains(boundingBox)) {
          skippedAreas++;
          continue nextSegment;
        }
      }

      Geometry shadow = segment.castShadow(Integer.MAX_VALUE / 2);

      PreparedGeometry prepared = PreparedGeometryFactory.prepare(shadow);
      clearedAreaList.add(prepared);
    }

    if (clearedAreaList.isEmpty()) {
      return null;
    }

    List<Geometry> plainGeometries =
        clearedAreaList.stream().map(PreparedGeometry::getGeometry).collect(Collectors.toList());

    Geometry geometryCollection =
        geometryFactory.createGeometryCollection(plainGeometries.toArray(Geometry[]::new)).buffer(0);

    return new UnaryUnionOp(geometryCollection).union();
  }

  /**
   * Expose visible area and previous path of all tokens in the token set. Server and clients are
   * updated.
   *
   * @param renderer the ZoneRenderer of the map
   * @param tokenSet the set of GUID of the tokens
   */
  public static void exposeVisibleArea(final ZoneRenderer renderer, Set<GUID> tokenSet) {
    exposeVisibleArea(renderer, tokenSet, false);
  }

  /**
   * Expose the visible area of all tokens in the token set. Server and clients are updated.
   *
   * @param renderer the ZoneRenderer of the map
   * @param tokenSet the set of GUID of the tokens
   * @param exposeCurrentOnly show only the current vision be exposed, or the last path too?
   */
  @SuppressWarnings("unchecked")
  public static void exposeVisibleArea(
      final ZoneRenderer renderer, Set<GUID> tokenSet, boolean exposeCurrentOnly) {
    final Zone zone = renderer.getZone();

    for (GUID tokenGUID : tokenSet) {
      Token token = zone.getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      if (!token.getHasSight()) {
        continue;
      }
      if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
        continue;
      }

      if (zone.getWaypointExposureToggle() && !exposeCurrentOnly) {
        if (token.getLastPath() == null) return;

        List<CellPoint> wayPointList = (List<CellPoint>) token.getLastPath().getWayPointList();

        final Token tokenClone = token.clone();

        for (final Object cell : wayPointList) {
          ZonePoint zp = null;
          if (cell instanceof CellPoint) {
            zp = zone.getGrid().convert((CellPoint) cell);
          } else {
            zp = (ZonePoint) cell;
          }

          tokenClone.setX(zp.x);
          tokenClone.setY(zp.y);

          renderer.flush(tokenClone);
          Area tokenVision = renderer.getZoneView().getVisibleArea(tokenClone);
          if (tokenVision != null) {
            Set<GUID> filteredToks = new HashSet<GUID>();
            filteredToks.add(tokenClone.getId());
            MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision, filteredToks);
          }
        }
        // System.out.println("2. Token: " + token.getGMName() + " - ID: " + token.getId());
        renderer.flush(token);
      } else {
        renderer.flush(token);
        Area tokenVision = renderer.getVisibleArea(token);
        if (tokenVision != null) {
          Set<GUID> filteredToks = new HashSet<GUID>();
          filteredToks.add(token.getId());
          MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision, filteredToks);
        }
      }
    }
  }

  public static void exposeVisibleAreaAtWaypoint(
      final ZoneRenderer renderer, Set<GUID> tokenSet, ZonePoint zp) {
    final Zone zone = renderer.getZone();

    for (GUID tokenGUID : tokenSet) {
      Token token = zone.getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      if (!token.getHasSight()) {
        continue;
      }
      if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
        continue;
      }

      ZonePoint zpStart = new ZonePoint(token.getX(), token.getY());
      token.setX(zp.x);
      token.setY(zp.y);
      renderer.flush(token);

      Area tokenVision = renderer.getZoneView().getVisibleArea(token);
      if (tokenVision != null) {
        Set<GUID> filteredToks = new HashSet<GUID>();
        filteredToks.add(token.getId());
        MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision, filteredToks);
      }

      token.setX(zpStart.x);
      token.setY(zpStart.y);
      renderer.flush(token);
    }
  }

  /**
   * This function is called by Meta-Shift-O, the token right-click, Expose {@code ->} only
   * Currently visible menu, from the Client/Server methods calls from
   * net.rptools.maptool.server.ServerMethodHandler.exposePCArea(GUID), and the macro
   * exposePCOnlyArea(). It takes the list of all PC tokens with sight and clear their exposed area,
   * clear the general exposed area, and expose the currently visible area. The server and other
   * clients are also updated.
   *
   * @author updated Jamz, Merudo
   * @since updated 1.5.8
   * @param renderer the ZoneRenderer
   */
  public static void exposePCArea(ZoneRenderer renderer) {
    Set<GUID> tokenSet = new HashSet<GUID>();
    List<Token> tokList = renderer.getZone().getPlayerTokensWithSight();

    String playerName = MapTool.getPlayer().getName();
    boolean isGM = MapTool.getPlayer().getRole() == Role.GM;

    for (Token token : tokList) {
      // why check ownership? Only GM can run this.
      boolean owner = token.isOwner(playerName) || isGM;

      if ((!MapTool.isPersonalServer() || MapTool.getServerPolicy().isUseIndividualViews())
          && !owner) {
        continue;
      }

      tokenSet.add(token.getId());
    }

    clearExposedArea(renderer.getZone(), true);
    renderer.getZone().clearExposedArea(tokenSet);
    exposeVisibleArea(renderer, tokenSet, true);
  }

  /**
   * Clear the FoW on one map. Updates server and clients.
   *
   * @param zone the Zone of the map.
   * @param globalOnly should only common area be cleared, or all token exposed areas?
   */
  private static void clearExposedArea(Zone zone, boolean globalOnly) {
    zone.clearExposedArea(globalOnly);
    MapTool.serverCommand().clearExposedArea(zone.getId(), globalOnly);
  }

  // Jamz: Expose not just PC tokens but also any NPC tokens the player owns
  /**
   * This function is called by Meta-Shift-F and the macro exposeAllOwnedArea()
   *
   * <p>Changed base function to select tokens now on ownership and based on TokenSelection menu
   * buttons.
   *
   * @author Jamz
   * @since 1.4.0.1
   * @param renderer the ZoneRenderer
   */
  public static void exposeAllOwnedArea(ZoneRenderer renderer) {
    Set<GUID> tokenSet = new HashSet<GUID>();

    // Jamz: Possibly pass a variable to override buttons? Also, maybe add a return a list of ID's
    List<Token> tokList = renderer.getZone().getOwnedTokensWithSight(MapTool.getPlayer());

    for (Token token : tokList) tokenSet.add(token.getId());

    // System.out.println("tokList: " + tokList.toString());

    /*
     * TODO: Jamz: May need to add back the isUseIndividualViews() logic later after testing... String playerName = MapTool.getPlayer().getName(); boolean isGM = MapTool.getPlayer().getRole() ==
     * Role.GM;
     *
     * for (Token token : tokList) { boolean owner = token.isOwner(playerName) || isGM;
     *
     * //System.out.println("token: " + token.getName() + ", owner: " + owner);
     *
     * if ((!MapTool.isPersonalServer() || MapTool.getServerPolicy().isUseIndividualViews()) && !owner) { continue; } tokenSet.add(token.getId()); }
     */

    renderer.getZone().clearExposedArea(tokenSet);
    exposeVisibleArea(renderer, tokenSet, true);
  }

  /**
   * Restore the FoW on one map. Updates server and clients.
   *
   * @param renderer the ZoneRenderer of the map.
   */
  public static void restoreFoW(final ZoneRenderer renderer) {
    // System.out.println("Zone ID: " + renderer.getZone().getId());
    clearExposedArea(renderer.getZone(), false);
  }

  public static void exposeLastPath(final ZoneRenderer renderer, final Set<GUID> tokenSet) {
    CodeTimer timer = new CodeTimer("exposeLastPath");

    final Zone zone = renderer.getZone();
    final Grid grid = zone.getGrid();
    GridCapabilities caps = grid.getCapabilities();

    if (!caps.isPathingSupported() || !caps.isSnapToGridSupported()) {
      return;
    }

    final Set<GUID> filteredToks = new HashSet<GUID>(2);

    for (final GUID tokenGUID : tokenSet) {
      final Token token = zone.getToken(tokenGUID);
      timer.start("exposeLastPath-" + token.getName());

      Path<? extends AbstractPoint> lastPath = token.getLastPath();

      if (lastPath == null) return;

      Map<GUID, ExposedAreaMetaData> fullMeta = zone.getExposedAreaMetaData();
      GUID exposedGUID = token.getExposedAreaGUID();
      final ExposedAreaMetaData meta =
          fullMeta.computeIfAbsent(exposedGUID, guid -> new ExposedAreaMetaData());

      final Token tokenClone = new Token(token);
      final ZoneView zoneView = renderer.getZoneView();
      Area visionArea = new Area();

      // Lee: get path according to zone's way point exposure toggle...
      List<? extends AbstractPoint> processPath =
          zone.getWaypointExposureToggle() ? lastPath.getWayPointList() : lastPath.getCellPath();

      int stepCount = processPath.size();
      log.debug("Path size = " + stepCount);

      Consumer<ZonePoint> revealAt =
          zp -> {
            tokenClone.setX(zp.x);
            tokenClone.setY(zp.y);

            Area currVisionArea = zoneView.getVisibleArea(tokenClone);
            if (currVisionArea != null) {
              visionArea.add(currVisionArea);
              meta.addToExposedAreaHistory(currVisionArea);
            }

            zoneView.flush(tokenClone);
          };
      if (token.isSnapToGrid()) {
        // For each cell point along the path, reveal FoW.
        for (final AbstractPoint cell : processPath) {
          assert cell instanceof CellPoint;
          revealAt.accept(grid.convert((CellPoint) cell));
        }
      } else {
        // Only reveal the final position.
        final AbstractPoint finalCell = processPath.get(processPath.size() - 1);
        assert finalCell instanceof ZonePoint;
        revealAt.accept((ZonePoint) finalCell);
      }

      timer.stop("exposeLastPath-" + token.getName());
      renderer.flush(tokenClone);

      filteredToks.clear();
      filteredToks.add(token.getId());
      zone.putToken(token);
      MapTool.serverCommand().exposeFoW(zone.getId(), visionArea, filteredToks);
      MapTool.serverCommand().updateExposedAreaMeta(zone.getId(), exposedGUID, meta);
    }

    String results = timer.toString();
    MapTool.getProfilingNoteFrame().addText(results);
    // System.out.println(results);
    timer.clear();
  }

  /**
   * Find the center point of a vision TODO: This is a horrible horrible method. the API is just
   * plain disgusting. But it'll work to consolidate all the places this has to be done until we can
   * encapsulate it into the vision itself.
   *
   * @param token the token to get the vision center of.
   * @param zone the Zone where the token is.
   * @return the center point
   */
  public static Point calculateVisionCenter(Token token, Zone zone) {
    Grid grid = zone.getGrid();
    int x = 0, y = 0;

    Rectangle bounds = null;
    if (token.isSnapToGrid()) {
      bounds =
          token
              .getFootprint(grid)
              .getBounds(grid, grid.convert(new ZonePoint(token.getX(), token.getY())));
    } else {
      bounds = token.getBounds(zone);
    }

    x = bounds.x + bounds.width / 2;
    y = bounds.y + bounds.height / 2;

    return new Point(x, y);
  }
}
