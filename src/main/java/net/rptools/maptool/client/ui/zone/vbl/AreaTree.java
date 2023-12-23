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

import com.google.common.collect.Iterables;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.lib.GeometryUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a topology area as a tree of nested polygons.
 *
 * <p>Solid areas of topology are called <emph>islands</emph>, while areas not covered by topology
 * are called <emph>oceans</emph>. Islands and oceans can be arranged in a tree structure where each
 * island is a parent to the oceans contained within it, and each ocean is a parent to the islands
 * contained within it. At the root of the tree is an infinite ocean with no parent island.
 */
public class AreaTree {
  private static final Logger log = LogManager.getLogger(AreaTree.class);

  /** The original area digested. */
  private final @Nonnull Node theOcean;

  /** Create an empty tree. */
  public AreaTree() {
    theOcean = new Node(new AreaMeta());
  }

  /**
   * Digests a flat {@link java.awt.geom.Area} into a hierarchical {@code AreaTree}.
   *
   * <p>The new {@code AreaTree} will represent that same topology as {@code area}, but represented
   * as polygonal regions. Holes in the topology will be represented by oceans contained in islands
   *
   * @param area The area to digest.
   */
  public AreaTree(@Nonnull Area area) {
    this();

    final var islands = new ArrayList<Node>();
    // Each polygon is an association of a parent polygon with polygonal holes. So we can easily map
    // each polygon to a parent island node with child ocean nodes for each hole.
    for (final var polygon : GeometryUtil.toJtsPolygons(area)) {
      final var island = new Node(new AreaMeta(polygon.getExteriorRing()));
      for (int i = 0; i < polygon.getNumInteriorRing(); ++i) {
        final var hole = polygon.getInteriorRingN(i);
        island.children.add(new Node(new AreaMeta(hole)));
      }
      islands.add(island);
    }

    // Now we need to hook up islands to the hierarchy. By sorting them from large to small, then
    // consuming front-to-back, we know that parents will have been added to the hierarchy.
    islands.sort(Comparator.comparingDouble(l -> -l.getMeta().getBoundingBoxArea()));
    for (var island : islands) {
      // This interior point check is only valid because we sorted the islands, ensuring parents are
      // added to the tree before any possible children.
      final var location = this.locate(island.getMeta().getInteriorPoint());
      if (location.island() != null) {
        // This shouldn't happen unless we messed up somewhere. Can't add islands to other islands.
        log.warn("Unable to find a parent container for an island. Returning an empty tree");
        this.theOcean.children.clear();
        return;
      }

      location.nearestOcean().children.add(island);
    }
  }

  /**
   * Find a point within the topology tree.
   *
   * <p>The result contains the nodes most directly associated with {@code point}, namely:
   *
   * <ul>
   *   <li>The deepest ocean containing {@code point}. There will always such an ocean.
   *   <li>The parent island of the deepest ocean. There will always be such an island unless the
   *       deepest ocean is the root ocean.
   *   <li>The child island of the deepest ocean that contains {@code point}. This only exists if
   *       the point is located directly in an island.
   * </ul>
   *
   * @param point The point to look up.
   * @return The location of {@code point} within the tree.
   */
  public @Nonnull TreeLocation locate(Coordinate point) {
    @Nullable Node parentIsland = null;
    @Nonnull Node nearestOcean = theOcean;
    @Nullable Node containingIsland = null;

    @Nullable Node nextNodeToCheck = theOcean;
    while (nextNodeToCheck != null) {
      final var nodeToCheck = nextNodeToCheck;
      nextNodeToCheck = null;

      for (final var child : nodeToCheck.getChildren()) {
        if (child.getMeta().contains(point)) {
          if (!child.getMeta().isOcean()) {
            containingIsland = child;
          } else {
            parentIsland = containingIsland;
            nearestOcean = child;
            containingIsland = null;
          }
          nextNodeToCheck = child;
          break;
        }
      }
    }

    // No containing child found.
    return new TreeLocation(parentIsland, nearestOcean, containingIsland);
  }

  /**
   * The results of locating a point in the {@code AreaTree}.
   *
   * @param parentIsland The parent of {@code nearestOcean} if one exists.
   * @param nearestOcean The deepest ancestor ocean. If the point is in an ocean, {@code
   *     nearestOcean} will be that ocean. Otherwise, it will be the parent of {@code island}.
   * @param island If the point is in an island, this will be that island. Otherwise {@code null}.
   */
  public record TreeLocation(
      @Nullable Node parentIsland, @Nonnull Node nearestOcean, @Nullable Node island) {}

  /**
   * A node of an {@code AreaTree}.
   *
   * <p>A node in the tree references its children and has a boundary ({@code AreaMeta}) as a value.
   */
  public static final class Node {
    private final @Nonnull AreaMeta meta;
    private final List<Node> children = new ArrayList<>();

    private Node(@Nonnull AreaMeta meta) {
      this.meta = meta;
    }

    public @Nonnull AreaMeta getMeta() {
      return meta;
    }

    public Iterable<Node> getChildren() {
      return Iterables.unmodifiableIterable(this.children);
    }
  }
}
