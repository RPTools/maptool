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

import com.google.common.collect.Lists;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.rptools.maptool.client.ui.zone.Illumination.LumensLevel;

/**
 * A data structure for storing {@link LitArea} objects and finding the difference between light and
 * darkness.
 *
 * <p>The illuminator is not responsible for building or transforming the areas themselves, and has
 * no knowledge of the lighting system. All it understands is areas and associated lumens.
 */
public class Illuminator {
  /**
   * Represents an area lit by a light.
   *
   * @param lumens The lumens associated with the area. Negative lumens indicates darkness.
   * @param area The area lit by the light.
   */
  public record LitArea(int lumens, Area area) {}

  /** Nodes are ordered from low lumens strength to high lumens strength. */
  private final ArrayList<Node> nodes = new ArrayList<>();

  public void add(LitArea litArea) {
    final var lumens = litArea.lumens();
    final var lumensStrength = Math.abs(lumens);
    final var isDarkness = lumens < 0;

    var index =
        Collections.binarySearch(
            Lists.transform(nodes, node -> node.lumensStrength), lumensStrength, Integer::compare);
    // If index is negative, it corresponds to the insertion point, so use then when creating
    // a new node.
    final Node node;
    if (index >= 0) {
      node = nodes.get(index);
    } else {
      index = -index - 1;
      node = new Node(lumensStrength);
      nodes.add(index, node);
    }

    node.contributingLitAreas.add(litArea);

    if (node.isValidated) {
      // We can easily keep it validated, so do so.
      final var path = isDarkness ? node.totalDarknessArea : node.totalLightArea;
      path.append(litArea.area().getPathIterator(null, 1), false);
    }
  }

  public void remove(LitArea litArea) {
    final var lumens = litArea.lumens();
    final var lumensStrength = Math.abs(lumens);

    var index =
        Collections.binarySearch(
            Lists.transform(nodes, node -> node.lumensStrength), lumensStrength, Integer::compare);
    if (index >= 0) {
      // Node exists, so modify it as needed.
      final var node = nodes.get(index);
      node.contributingLitAreas.remove(litArea);

      node.isValidated = false;
    }
  }

  private void revalidateNode(Node node) {
    if (node.isValidated) {
      return;
    }

    node.totalLightArea = new Path2D.Double();
    node.totalDarknessArea = new Path2D.Double();
    for (final var litArea : node.contributingLitAreas) {
      final var isDarkness = litArea.lumens() < 0;
      final var path = isDarkness ? node.totalDarknessArea : node.totalLightArea;
      path.append(litArea.area().getPathIterator(null, 1), false);
    }

    node.isValidated = true;
  }

  private void revalidateAggregations() {
    for (final var node : nodes) {
      revalidateNode(node);
    }
  }

  public Illumination getIllumination() {
    // Lumens will be sorted from weak to strong, with darkness coming after equal lights. Note
    // that the order is the same as {@link #nodes}.
    final var lumensLevels = new ArrayList<LumensLevel>();

    revalidateAggregations();

    for (final var node : nodes) {
      // Darkness and light have been completely aggregated, so we can work off the final
      // result for the most part.

      final var currentLitArea = new Area(node.totalLightArea);
      final var currentDarknessArea = new Area(node.totalDarknessArea);

      // Note: lumens levels are not guaranteed to be disjoint. Otherwise, we would also have
      // to punch out strong light from weak light and strong darkness from weak darkness.
      // We also don't care about subtracting strong darkness from weak light, or weak light
      // from strong darkness. That is an operation that callers can do if desired.
      lumensLevels.add(new LumensLevel(node.lumensStrength, currentLitArea, currentDarknessArea));
    }

    return new Illumination(lumensLevels);
  }

  private static final class Node {
    /** The absolute value of the lumens of all lights represented by this node. */
    public final int lumensStrength;

    /**
     * The list of all lights of levels {@link #lumensStrength} that have been added to the
     * structure.
     */
    public final List<LitArea> contributingLitAreas = new ArrayList<>();

    // region The following are cached to quickly rebuild results after a change, but can themselves
    //        be recalculated at any time from {@link #contributingLitAreas}.
    /**
     * true if {@link #totalLightArea} and {@link #totalDarknessArea} agree with {@link
     * #contributingLitAreas}.
     */
    public boolean isValidated = false;

    /** The union of all light areas in this node. */
    public Path2D totalLightArea = new Path2D.Double();

    /** The union of all darkness areas in this node. */
    public Path2D totalDarknessArea = new Path2D.Double();
    // endregion

    public Node(int lumensStrength) {
      this.lumensStrength = lumensStrength;
    }
  }
}
