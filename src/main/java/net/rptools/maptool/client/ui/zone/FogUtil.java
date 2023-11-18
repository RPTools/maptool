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
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
import net.rptools.maptool.client.ui.zone.vbl.VisibilitySweepEndpoint;
import net.rptools.maptool.client.ui.zone.vbl.VisionBlockingAccumulator;
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
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.union.UnaryUnionOp;

public class FogUtil {
  private static final Logger log = LogManager.getLogger(FogUtil.class);
  private static final GeometryFactory geometryFactory = GeometryUtil.getGeometryFactory();

  /**
   * Return the visible area for an origin, a lightSourceArea and a VBL.
   *
   * @param origin the vision origin.
   * @param vision the lightSourceArea.
   * @param topology the VBL topology.
   * @return the visible area.
   */
  public static @Nonnull Area calculateVisibility(
      Point origin,
      Area vision,
      AreaTree topology,
      AreaTree hillVbl,
      AreaTree pitVbl,
      AreaTree coverVbl) {
    // We could use the vision envelope instead, but vision geometry tends to be pretty simple.
    final var visionGeometry = PreparedGeometryFactory.prepare(GeometryUtil.toJts(vision));

    /*
     * Find the visible area for each topology type independently.
     *
     * In principle, we could also combine all the vision blocking segments for all topology types
     * and run the sweep algorithm once. But this is subject to some pathological cases that JTS
     * cannot handle. These cases do not exist within a single type of topology, but can arise when
     * we combine them.
     */
    List<Geometry> visibleAreas = new ArrayList<>();
    final List<Function<VisionBlockingAccumulator, Boolean>> topologyConsumers = new ArrayList<>();
    topologyConsumers.add(acc -> acc.addWallBlocking(topology));
    topologyConsumers.add(acc -> acc.addHillBlocking(hillVbl));
    topologyConsumers.add(acc -> acc.addPitBlocking(pitVbl));
    topologyConsumers.add(acc -> acc.addCoverBlocking(coverVbl));
    for (final var consumer : topologyConsumers) {
      final var accumulator =
          new VisionBlockingAccumulator(geometryFactory, origin, visionGeometry);
      final var isVisionCompletelyBlocked = consumer.apply(accumulator);
      if (!isVisionCompletelyBlocked) {
        // Vision has been completely blocked by this topology. Short circuit.
        return new Area();
      }

      final var visibleArea =
          calculateVisibleArea(
              new Coordinate(origin.getX(), origin.getY()),
              accumulator.getVisionBlockingSegments(),
              visionGeometry);
      if (visibleArea != null) {
        visibleAreas.add(visibleArea);
      }
    }

    // We have to intersect all the results in order to find the true remaining visible area.
    vision = new Area(vision);
    if (!visibleAreas.isEmpty()) {
      // We intersect in AWT space because JTS can be really finicky about intersection precision.
      var shapeWriter = new ShapeWriter();
      for (final var visibleArea : visibleAreas) {
        var area = new Area(shapeWriter.toShape(visibleArea));
        vision.intersect(area);
      }
    }

    // For simplicity, this catches some of the edge cases
    return vision;
  }

  private record NearestWallResult(LineSegment wall, Coordinate point, double distance) {}

  private static NearestWallResult findNearestOpenWall(
      Set<LineSegment> openWalls, LineSegment ray) {
    assert !openWalls.isEmpty();

    @Nullable LineSegment currentNearest = null;
    @Nullable Coordinate currentNearestPoint = null;
    double nearestDistance = Double.MAX_VALUE;
    for (final var openWall : openWalls) {
      final var intersection = ray.lineIntersection(openWall);
      if (intersection == null) {
        continue;
      }

      final var distance = ray.p0.distance(intersection);
      if (distance < nearestDistance) {
        currentNearest = openWall;
        currentNearestPoint = intersection;
        nearestDistance = distance;
      }
    }

    assert currentNearest != null;
    return new NearestWallResult(currentNearest, currentNearestPoint, nearestDistance);
  }

  /**
   * Builds a list of endpoints for the sweep algorithm to consume.
   *
   * <p>The endpoints will be unique (i.e., no coordinate is represented more than once) and in a
   * consistent orientation (i.e., counterclockwise around the origin). In addition, all endpoints
   * will have their starting and ending walls filled according to which walls are incident to the
   * corresponding point.
   *
   * @param origin The center of vision, by which orientation can be determined.
   * @param visionBlockingSegments The "walls" that are able to block vision. All points in these
   *     walls will be present in the returned list.
   * @return A list of all endpoints in counterclockwise order.
   */
  private static List<VisibilitySweepEndpoint> getSweepEndpoints(
      Coordinate origin, List<LineString> visionBlockingSegments) {
    final Map<Coordinate, VisibilitySweepEndpoint> endpointsByPosition = new HashMap<>();
    for (final var segment : visionBlockingSegments) {
      VisibilitySweepEndpoint current = null;
      for (final var coordinate : segment.getCoordinates()) {
        final var previous = current;
        current =
            endpointsByPosition.computeIfAbsent(
                coordinate, c -> new VisibilitySweepEndpoint(c, origin));
        if (previous == null) {
          // We just started this segment; still need a second point.
          continue;
        }

        final var isForwards =
            Orientation.COUNTERCLOCKWISE
                == Orientation.index(origin, previous.getPoint(), current.getPoint());
        // Make sure the wall always goes in the counterclockwise direction.
        final LineSegment wall =
            isForwards
                ? new LineSegment(previous.getPoint(), coordinate)
                : new LineSegment(coordinate, previous.getPoint());
        if (isForwards) {
          previous.startsWall(wall);
          current.endsWall(wall);
        } else {
          previous.endsWall(wall);
          current.startsWall(wall);
        }
      }
    }
    final List<VisibilitySweepEndpoint> endpoints = new ArrayList<>(endpointsByPosition.values());

    endpoints.sort(
        Comparator.comparingDouble(VisibilitySweepEndpoint::getPseudoangle)
            .thenComparing(VisibilitySweepEndpoint::getDistance));

    return endpoints;
  }

  private static @Nullable Geometry calculateVisibleArea(
      Coordinate origin, List<LineString> visionBlockingSegments, PreparedGeometry visionGeometry) {
    if (visionBlockingSegments.isEmpty()) {
      // No topology, apparently.
      return null;
    }

    /*
     * Unioning all the line segments has the nice effect of noding any intersections between line
     * segments. Without this, it may not be valid.
     * Note: if the geometry were only composed of one topology, it would certainly be valid due to
     * its "flat" nature. But even in that case, it is more robust to due the union in case this
     * flatness assumption ever changes.
     */
    final var allWallGeometry = new UnaryUnionOp(visionBlockingSegments).union();
    // Replace the original geometry with the well-defined geometry.
    visionBlockingSegments = new ArrayList<>();
    LineStringExtracter.getLines(allWallGeometry, visionBlockingSegments);

    /*
     * The algorithm requires walls in every direction. The easiest way to accomplish this is to add
     * the boundary of the bounding box.
     */
    final var envelope = allWallGeometry.getEnvelopeInternal();
    envelope.expandToInclude(visionGeometry.getGeometry().getEnvelopeInternal());
    // Exact expansion distance doesn't matter, we just don't want the boundary walls to overlap
    // endpoints from real walls.
    envelope.expandBy(1.0);
    // Because we definitely have geometry, the envelope will always be a non-trivial rectangle.
    visionBlockingSegments.add(((Polygon) geometryFactory.toGeometry(envelope)).getExteriorRing());

    // Now that we have valid geometry and a bounding box, we can continue with the sweep.

    final var endpoints = getSweepEndpoints(origin, visionBlockingSegments);
    Set<LineSegment> openWalls = Collections.newSetFromMap(new IdentityHashMap<>());

    // This initial sweep just makes sure we have the correct open set to start.
    for (final var endpoint : endpoints) {
      openWalls.addAll(endpoint.getStartsWalls());
      openWalls.removeAll(endpoint.getEndsWalls());
    }

    // Now for the real sweep. Make sure to process the first point once more at the end to ensure
    // the sweep covers the full 360 degrees.
    endpoints.add(endpoints.get(0));
    List<Coordinate> visionPoints = new ArrayList<>();
    for (final var endpoint : endpoints) {
      assert !openWalls.isEmpty();

      final var ray = new LineSegment(origin, endpoint.getPoint());
      final var nearestWallResult = findNearestOpenWall(openWalls, ray);

      openWalls.addAll(endpoint.getStartsWalls());
      openWalls.removeAll(endpoint.getEndsWalls());

      // Find a new nearest wall.
      final var newNearestWallResult = findNearestOpenWall(openWalls, ray);

      if (newNearestWallResult.wall != nearestWallResult.wall) {
        // Implies we have changed which wall we are at. Need to figure out projections.

        if (openWalls.contains(nearestWallResult.wall())) {
          // The previous nearest wall is still open. I.e., we didn't fall of its end but
          // encountered a new closer wall. So we project the current point to the previous
          // nearest wall, then step to the current point.
          visionPoints.add(nearestWallResult.point());
          visionPoints.add(endpoint.getPoint());
        } else {
          // The previous nearest wall is now closed. I.e., we "fell off" it and therefore have
          // encountered a different wall. So we step from the current point (which is on the
          // previous wall) to the projection on the new wall.
          visionPoints.add(endpoint.getPoint());
          // Special case: if the two walls are adjacent, they share the current point. We don't
          // need to add the point twice, so just skip in that case.
          if (!endpoint.getStartsWalls().contains(newNearestWallResult.wall())) {
            visionPoints.add(newNearestWallResult.point());
          }
        }
      }
    }
    if (visionPoints.size() < 3) {
      // This shouldn't happen, but just in case.
      log.warn("Sweep produced too few points: {}", visionPoints);
      return null;
    }
    visionPoints.add(visionPoints.get(0)); // Ensure a closed loop.

    return geometryFactory.createPolygon(visionPoints.toArray(Coordinate[]::new));
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
          Area tokenVision =
              renderer.getZoneView().getVisibleArea(tokenClone, renderer.getPlayerView());
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
        Area tokenVision = renderer.getZoneView().getVisibleArea(token, renderer.getPlayerView());
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

      Area tokenVision = renderer.getZoneView().getVisibleArea(token, renderer.getPlayerView());
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

            Area currVisionArea = zoneView.getVisibleArea(tokenClone, renderer.getPlayerView());
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
