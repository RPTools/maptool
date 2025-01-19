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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.vbl.NodedTopology;
import net.rptools.maptool.client.ui.zone.vbl.VisibilityProblem;
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
import net.rptools.maptool.model.topology.VisibilityType;
import net.rptools.maptool.model.topology.VisionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

public class FogUtil {
  private static final Logger log = LogManager.getLogger(FogUtil.class);
  private static final GeometryFactory geometryFactory = GeometryUtil.getGeometryFactory();

  /**
   * Performs a vision sweep, producing a visibility polygon.
   *
   * @param origin The origin point for the vision.
   * @param visionBounds Some bounds on the vision.
   * @param topology The topology to account for.
   * @return The boundary of the visibility polygon. If no vision is possible - e.g., because {@code
   *     origin} is inside a Wall VBL mask - an empty ring is returned. If no topology was present,
   *     {@code null} is returned to indicate no restriction on vision.
   */
  public static @Nullable LinearRing doVisionSweep(
      VisibilityType visibilityType,
      Coordinate origin,
      Envelope visionBounds,
      NodedTopology topology) {
    var timer = CodeTimer.get();

    timer.start("create solver");
    final var problem = new VisibilityProblem(origin, visionBounds);
    timer.stop("create solver");

    timer.start("accumulate blocking walls");
    var visionResult = topology.getSegments(visibilityType, origin, visionBounds, problem::add);
    if (visionResult == VisionResult.CompletelyObscured) {
      // No vision possible.
      return geometryFactory.createLinearRing();
    }
    if (problem.isEmpty()) {
      // No topology.
      return null;
    }
    timer.stop("accumulate blocking walls");

    timer.start("calculate visible area");
    final Coordinate[] visibilityPolygon;
    try {
      visibilityPolygon = problem.solve();
    } catch (Exception e) {
      log.error("Unexpected error while calculating visible area.", e);
      // Play it safe and don't consider anything to be visible.
      return geometryFactory.createLinearRing();
    }
    timer.stop("calculate visible area");

    return geometryFactory.createLinearRing(visibilityPolygon);
  }

  /**
   * Figure out the visible area for a given topology, origin, and unblocked vision.
   *
   * @param origin The origin point of the vision.
   * @param vision The area of the vision before blocking is applied.
   * @param topology The topology to apply to the vision blocking.
   * @return A subset of {@code vision}, restricted to the visibility polygon. Can be empty if
   *     vision is not possible.
   */
  public static @Nonnull Area calculateVisibility(
      VisibilityType visibilityType, Point2D origin, Area vision, NodedTopology topology) {
    var timer = CodeTimer.get();
    timer.start("FogUtil::calculateVisibility");
    try {
      var cOrigin = new Coordinate(origin.getX(), origin.getY());

      Envelope visionBounds;
      try {
        timer.start("get vision bounds");
        var awtBounds = vision.getBounds2D();
        visionBounds =
            new Envelope(
                new Coordinate(awtBounds.getMinX(), awtBounds.getMinY()),
                new Coordinate(awtBounds.getMaxX(), awtBounds.getMaxY()));
      } finally {
        timer.stop("get vision bounds");
      }

      LinearRing visibilityPolygon;
      try {
        timer.start("doVisionSweep()");
        visibilityPolygon = doVisionSweep(visibilityType, cOrigin, visionBounds, topology);
      } finally {
        timer.stop("doVisionSweep()");
      }

      Area blockedVision;
      try {
        timer.start("combine visibility polygon with vision");
        if (visibilityPolygon == null) {
          // There was no topology, so we can just use the original area.
          blockedVision = new Area(vision);
        } else if (visibilityPolygon.isEmpty()) {
          // Vision is not possible.
          blockedVision = new Area();
        } else {
          // We intersect in AWT space because JTS can be finicky about intersection precision.
          var shapeWriter = new ShapeWriter();
          // The linear ring is just the boundary, but the resulting shape is the enclosed region.
          blockedVision = new Area(shapeWriter.toShape(visibilityPolygon));
          blockedVision.intersect(vision);
        }
      } finally {
        timer.stop("combine visibility polygon with vision");
      }
      return blockedVision;
    } finally {
      timer.stop("FogUtil::calculateVisibility");
    }
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
    CodeTimer.using(
        "exposeLastPath",
        timer -> {
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
            final var visibleAreas = new ArrayList<Area>();

            // Lee: get path according to zone's way point exposure toggle...
            List<? extends AbstractPoint> processPath =
                zone.getWaypointExposureToggle()
                    ? lastPath.getWayPointList()
                    : lastPath.getCellPath();

            int stepCount = processPath.size();
            log.debug("Path size = " + stepCount);

            timer.start("Get visible areas");
            Consumer<ZonePoint> revealAt =
                zp -> {
                  tokenClone.setX(zp.x);
                  tokenClone.setY(zp.y);

                  Area currVisionArea =
                      zoneView.getVisibleArea(tokenClone, renderer.getPlayerView());
                  if (currVisionArea != null) {
                    visibleAreas.add(currVisionArea);
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
            timer.stop("Get visible areas");

            timer.start("Union visible areas");
            Area visionArea = GeometryUtil.destructiveUnion(visibleAreas);
            timer.stop("Union visible areas");

            timer.start("Add to token exposed area");
            meta.addToExposedAreaHistory(visionArea);
            timer.stop("Add to token exposed area");

            renderer.flush(tokenClone);

            filteredToks.clear();
            filteredToks.add(token.getId());

            timer.start("Update zone");
            zone.putToken(token);
            timer.stop("Update zone");

            timer.start("Send results");
            MapTool.serverCommand().exposeFoW(zone.getId(), visionArea, filteredToks);
            MapTool.serverCommand().updateExposedAreaMeta(zone.getId(), exposedGUID, meta);
            timer.stop("Send results");

            timer.stop("exposeLastPath-" + token.getName());
          }
        });
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
