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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.model.Zone;
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

  private void blockVisionBeyondContainer(AreaOcean ocean) {
    final var facing = Facing.OCEAN_SIDE_FACES_ORIGIN;

    visionBlockingSegments.addAll(
        ocean.getVisionBlockingBoundarySegments(geometryFactory, originCoordinate, facing, vision));
    for (final var child : ocean.getIslands()) {
      visionBlockingSegments.addAll(
          child.getVisionBlockingBoundarySegments(
              geometryFactory, originCoordinate, facing, vision));
    }
  }

  private void blockVisionBeyondContainer(AreaIsland ocean) {
    final var facing = Facing.ISLAND_SIDE_FACES_ORIGIN;

    visionBlockingSegments.addAll(
        ocean.getVisionBlockingBoundarySegments(geometryFactory, originCoordinate, facing, vision));
    for (final var child : ocean.getOceans()) {
      visionBlockingSegments.addAll(
          child.getVisionBlockingBoundarySegments(
              geometryFactory, originCoordinate, facing, vision));
    }
  }

  /**
   * Finds all topology segments that can take part in blocking vision.
   *
   * <p>The exact selection of segments will different depending on the type of the topology. Some
   * topology (Wall VBL) is a mask and can completely block vision simply by the origin point being
   * located within it. The return value indicates whether this is the case.
   *
   * @param topology The VBL to apply.
   * @return {@code true} if vision is possible, i.e., is not completely blocked by the topology.
   */
  public boolean add(Zone.TopologyType type, AreaTree topology) {
    return switch (type) {
      case WALL_VBL -> addWallBlocking(topology);
      case HILL_VBL -> addHillBlocking(topology);
      case PIT_VBL -> addPitBlocking(topology);
      case COVER_VBL -> addCoverBlocking(topology);
      case MBL -> true;
    };
  }

  /**
   * Finds all wall topology segments that can take part in blocking vision.
   *
   * @param topology The topology to treat as Wall VBL.
   * @return false if the vision has been completely blocked by topology, or true if vision may be
   *     blocked by particular segments.
   */
  private boolean addWallBlocking(AreaTree topology) {
    final AreaContainer container = topology.getContainerAt(origin);
    if (container == null) {
      // Should never happen since the global ocean should catch everything.
      return false;
    }

    if (container instanceof AreaIsland) {
      // Since we're contained in a wall island, there can be no vision through it.
      return false;
    } else if (container instanceof AreaOcean ocean) {
      blockVisionBeyondContainer(ocean);
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
  private boolean addHillBlocking(AreaTree topology) {
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

    final @Nonnull AreaOcean nearestOcean;
    final @Nullable AreaIsland childIsland;
    if (container instanceof final AreaIsland island) {
      childIsland = island;
      nearestOcean = childIsland.getParentOcean();
    } else {
      final AreaOcean ocean = (AreaOcean) container;
      nearestOcean = ocean;
      childIsland = null;
    }
    final @Nullable AreaIsland parentIsland = nearestOcean.getParentIsland();

    if (parentIsland != null) {
      blockVisionBeyondContainer(parentIsland);
    }

    // Check each contained island.
    for (var containedIsland : nearestOcean.getIslands()) {
      if (containedIsland == childIsland) {
        // We don't want to block vision for the hill we're currently in.
        // TODO Ideally we could block the second occurence of the current island, but we need
        //  a way to do that reliably.
        continue;
      }

      blockVisionBeyondContainer(containedIsland);
    }

    if (childIsland != null) {
      // Same basics as the nearestOcean logic above, but applied to children of this island
      // (grandchildren of nearestOcean).
      for (final var childOcean : childIsland.getOceans()) {
        for (final var containedIsland : childOcean.getIslands()) {
          blockVisionBeyondContainer(containedIsland);
        }
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
  private boolean addPitBlocking(AreaTree topology) {
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
      blockVisionBeyondContainer(island);
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
  private boolean addCoverBlocking(AreaTree topology) {
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

    final AreaOcean nearestOcean =
        (container instanceof final AreaOcean ocean)
            ? ocean
            : ((AreaIsland) container).getParentOcean();
    blockVisionBeyondContainer(nearestOcean);

    if (container instanceof final AreaIsland island) {
      for (final var childOcean : island.getOceans()) {
        blockVisionBeyondContainer(childOcean);
      }
    }

    return true;
  }
}
