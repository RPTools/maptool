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

import net.rptools.maptool.model.Zone;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public final class VisionBlockingAccumulator {
  private final Coordinate origin;
  private final Envelope visionBounds;
  private final VisibilityProblem visibilityProblem;

  public VisionBlockingAccumulator(
      Coordinate origin, Envelope visionBounds, VisibilityProblem visibilityProblem) {
    this.origin = origin;
    this.visionBounds = new Envelope(visionBounds);
    this.visibilityProblem = visibilityProblem;
  }

  private void blockVisionBeyondContainer(AreaTree.Node container) {
    final var facing =
        container.getMeta().isOcean()
            ? Facing.OCEAN_SIDE_FACES_ORIGIN
            : Facing.ISLAND_SIDE_FACES_ORIGIN;

    container.getMeta().getFacingSegments(origin, facing, visionBounds, visibilityProblem::add);
    for (var child : container.getChildren()) {
      child.getMeta().getFacingSegments(origin, facing, visionBounds, visibilityProblem::add);
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
    final var location = topology.locate(origin);

    if (location.island() != null) {
      // Since we're contained in a wall island, there can be no vision through it.
      return false;
    }

    blockVisionBeyondContainer(location.nearestOcean());
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
    final var location = topology.locate(origin);

    /*
     * There are two cases for Hill VBL:
     * 1. A token inside hill VBL can see into adjacent oceans, and therefore into other areas of
     *    Hill VBL in those oceans.
     * 2. A token outside hill VBL can see into hill VBL, but not into any oceans adjacent to it.
     */

    if (location.parentIsland() != null) {
      blockVisionBeyondContainer(location.parentIsland());
    }

    // Check each contained island.
    for (var containedIsland : location.nearestOcean().getChildren()) {
      if (containedIsland == location.island()) {
        // We don't want to block vision for the hill we're currently in.
        // TODO Ideally we could block the second occurence of the current island, but we need
        //  a way to do that reliably.
        continue;
      }

      blockVisionBeyondContainer(containedIsland);
    }

    if (location.island() != null) {
      // Same basics as the nearestOcean logic above, but applied to children of this island
      // (grandchildren of nearestOcean).
      for (final var childOcean : location.island().getChildren()) {
        for (final var containedIsland : childOcean.getChildren()) {
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
    final var location = topology.locate(origin);

    /*
     * There are two cases for Pit VBL:
     * 1. A token inside Pit VBL can see only see within the current island, not into any adjacent
     *    oceans.
     * 2. A token outside Pit VBL is unobstructed by the Pit VBL (nothing special to do).
     */
    if (location.island() != null) {
      blockVisionBeyondContainer(location.island());
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
    final var location = topology.locate(origin);

    /*
     * There are two cases for Cover VBL:
     * 1. A token inside Cover VBL can see everything, unobstructed by the cover.
     * 2. A token outside Cover VBL can see nothing, as if it were wall.
     */

    blockVisionBeyondContainer(location.nearestOcean());

    if (location.island() != null) {
      for (var ocean : location.island().getChildren()) {
        blockVisionBeyondContainer(ocean);
      }
    }

    return true;
  }
}
