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
package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.prep.PreparedGeometry;

public final class VisionBlockingAccumulator {
  private final GeometryFactory geometryFactory;
  private final Point origin;
  private final Coordinate originCoordinate;
  private final PreparedGeometry vision;
  private final List<LineString> visionBlockingSegments;

  public VisionBlockingAccumulator(
      GeometryFactory geometryFactory, Point origin, PreparedGeometry vision) {
    this.geometryFactory = geometryFactory;
    this.origin = origin;
    this.originCoordinate = new Coordinate(origin.getX(), origin.getY());

    this.vision = vision;

    this.visionBlockingSegments = new ArrayList<>();
  }

  public Point getOrigin() {
    return origin;
  }

  public List<LineString> getVisionBlockingSegments() {
    return visionBlockingSegments;
  }

  private void addVisionBlockingSegments(AreaContainer areaContainer, Facing facing) {
    var segments =
        areaContainer.getVisionBlockingBoundarySegments(
            geometryFactory, originCoordinate, facing, vision);
    visionBlockingSegments.addAll(segments);
  }

  private void addIslandForHillBlocking(
      AreaIsland blockingIsland, @Nullable AreaOcean excludeChildOcean) {
    // The back side of the island blocks.
    addVisionBlockingSegments(blockingIsland, Facing.ISLAND_SIDE_FACES_ORIGIN);
    // The front side of each contained ocean also acts as a back side boundary of the island.
    for (var blockingOcean : blockingIsland.getOceans()) {
      if (blockingOcean == excludeChildOcean) {
        continue;
      }

      addVisionBlockingSegments(blockingOcean, Facing.ISLAND_SIDE_FACES_ORIGIN);
    }
  }

  /**
   * Finds all wall topology segments that can take part in blocking vision.
   *
   * @param topology The topology to treat as Wall VBL.
   * @return false if the vision has been completely blocked by topology, or true if vision may be
   *     blocked by particular segments.
   */
  public boolean addWallBlocking(AreaTree topology) {
    final AreaContainer container = topology.getContainerAt(origin);
    if (container == null) {
      // Should never happen since the global ocean should catch everything.
      return false;
    }

    if (container instanceof AreaIsland) {
      // Since we're contained in a wall island, there can be no vision through it.
      return false;
    } else if (container instanceof AreaOcean ocean) {
      final var parentIsland = ocean.getParentIsland();
      if (parentIsland != null) {
        // The near edge of the island blocks vision, which is the same as the boundary of this
        // ocean, which we're inside.
        addVisionBlockingSegments(ocean, Facing.OCEAN_SIDE_FACES_ORIGIN);
      }

      // Check each contained island.
      for (var containedIsland : ocean.getIslands()) {
        // The front side of wall VBL blocks vision.
        addVisionBlockingSegments(containedIsland, Facing.OCEAN_SIDE_FACES_ORIGIN);
      }
    }

    return true;
  }

  /**
   * Finds all hill topology segments that can take part in blocking vision.
   *
   * @param topology The topology to treat as Hill VBL.
   * @return false if the vision has been completely blocked by topology, or true if vision can be
   *     blocked by particular segments.
   */
  public boolean addHillBlocking(AreaTree topology) {
    final AreaContainer container = topology.getContainerAt(origin);
    if (container == null) {
      // Should never happen since the global ocean should catch everything.
      return false;
    }

    /*
     * There are two cases for Hill VBL:
     * 1. A token inside hill VBL can see into adjacent oceans, and therefore into other areas of
     *    Hill VBL in those oceans.
     * 2. A token outside hill VBL can see into hill VBL, but not into any oceans adjacent to it.
     */

    if (container instanceof final AreaIsland island) {
      /*
       * Since we're in an island, vision is blocked by:
       * 1. The back side of the parent ocean's parent island.
       * 2. The back side of any sibling islands (other children of the parent ocean).
       * 3. The back side of any child ocean's islands.
       * 4. For each island in the above, any child ocean provided it's not the parent of the
       *    current island.
       */
      final var parentOcean = island.getParentOcean();

      final var grandparentIsland = parentOcean.getParentIsland();
      if (grandparentIsland != null) {
        addIslandForHillBlocking(grandparentIsland, parentOcean);
      }

      for (final var siblingIsland : parentOcean.getIslands()) {
        if (siblingIsland == island) {
          // We don't want to block vision for the hill we're currently in.
          // TODO Ideally we could block the second occurence of the current island, but we need
          //  a way to do that reliably.
          continue;
        }

        addIslandForHillBlocking(siblingIsland, null);
      }

      for (final var childOcean : island.getOceans()) {
        for (final var grandchildIsland : childOcean.getIslands()) {
          addIslandForHillBlocking(grandchildIsland, null);
        }
      }
    } else if (container instanceof final AreaOcean ocean) {
      /*
       * Since we're in an ocean, vision is blocked by:
       * 1. The back side of the parent island.
       * 2. The back side of any child island
       * 3. For each island in the above, any child ocean provided it's not the current ocean.
       */
      final var parentIsland = ocean.getParentIsland();
      if (parentIsland != null) {
        addIslandForHillBlocking(parentIsland, null);
      }
      // Check each contained island.
      for (var containedIsland : ocean.getIslands()) {
        addIslandForHillBlocking(containedIsland, null);
      }
    }

    return true;
  }

  /**
   * Finds all pit topology segments that can take part in blocking vision.
   *
   * @param topology The topology to treat as Pit VBL.
   * @return false if the vision has been completely blocked by topology, or true if vision can be
   *     blocked by particular segments.
   */
  public boolean addPitBlocking(AreaTree topology) {
    final AreaContainer container = topology.getContainerAt(origin);
    if (container == null) {
      // Should never happen since the global ocean should catch everything.
      return false;
    }

    /*
     * There are two cases for Pit VBL:
     * 1. A token inside Pit VBL can see only see within the current island, not into any adjacent
     *    oceans.
     * 2. A token outside Pit VBL is unobstructed by the Pit VBL (nothing special to do).
     */

    if (container instanceof final AreaIsland island) {
      addVisionBlockingSegments(island, Facing.ISLAND_SIDE_FACES_ORIGIN);
      for (var childOcean : island.getOceans()) {
        addVisionBlockingSegments(childOcean, Facing.ISLAND_SIDE_FACES_ORIGIN);
      }
    }

    return true;
  }

  /**
   * Finds all cover topology segments that can take part in blocking vision.
   *
   * @param topology The topology to treat as Cover VBL.
   * @return false if the vision has been completely blocked by topology, or true if vision can be
   *     blocked by particular segments.
   */
  public boolean addCoverBlocking(AreaTree topology) {
    final AreaContainer container = topology.getContainerAt(origin);
    if (container == null) {
      // Should never happen since the global ocean should catch everything.
      return false;
    }

    /*
     * There are two cases for Cover VBL:
     * 1. A token inside Cover VBL can see everything, unobstructed by the cover.
     * 2. A token outside Cover VBL can see nothing, as if it were wall.
     */

    if (container instanceof final AreaIsland island) {

      final var parentOcean = island.getParentOcean();

      for (final var siblingIsland : parentOcean.getIslands()) {
        if (siblingIsland == island) {
          continue;
        }
        addVisionBlockingSegments(siblingIsland, Facing.OCEAN_SIDE_FACES_ORIGIN);
      }
      for (final var childOcean : island.getOceans()) {
        for (final var grandchildIsland : childOcean.getIslands()) {
          addVisionBlockingSegments(grandchildIsland, Facing.OCEAN_SIDE_FACES_ORIGIN);
        }
        addVisionBlockingSegments(childOcean, Facing.OCEAN_SIDE_FACES_ORIGIN);
      }
      addVisionBlockingSegments(parentOcean, Facing.OCEAN_SIDE_FACES_ORIGIN);
      addVisionBlockingSegments(island, Facing.OCEAN_SIDE_FACES_ORIGIN);
    } else if (container instanceof AreaOcean ocean) {
      final var parentIsland = ocean.getParentIsland();
      if (parentIsland != null) {
        addVisionBlockingSegments(ocean, Facing.ISLAND_SIDE_FACES_ORIGIN);
      }

      for (var containedIsland : ocean.getIslands()) {
        addVisionBlockingSegments(containedIsland, Facing.OCEAN_SIDE_FACES_ORIGIN);
      }
    }

    return true;
  }
}
