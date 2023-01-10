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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * The main result type returned by an {@link Illuminator}.
 *
 * <p>At its core, this is a list of {@link LumensLevel} objects that provides a mechanical
 * description of the strength of light at each point. The areas are not disjoint, i.e., multiple
 * {@code LumensLevel}s can overlap with each other. Also, light and darkness are not subtracted
 * from each other.
 *
 * <p>From the basic structure, we are able to calculate a few derivative results:
 *
 * <ol>
 *   <li>The obscured lumens levels. For each light area in the basic structure, subtract out any
 *       stronger darknesses, and for each darkness subtract out any stronger lights. The result is
 *       the obscured lit areas arranged by lumens level.
 *   <li>The complete visible area. This is the union of the light areas after the process in (1).
 * </ol>
 */
public final class Illumination {
  /**
   * Represents the entire area covered by a given lumens level.
   *
   * <p>The {@link #lightArea} is the union of all light at the lumens level of {@link
   * #lumensStrength}. It neither excludes stronger darkness nor stronger light. In a similar vein,
   * {@link #darknessArea} is the union of all darkness at the same lumens levels.
   *
   * @param lumensStrength The lumens associated with the area.
   * @param lightArea The area covered by lights of this lumens value.
   * @param darknessArea The area covered by darkness of this lumens value.
   */
  public record LumensLevel(int lumensStrength, Area lightArea, Area darknessArea) {}

  /** Ordered from weak to strong. */
  private final List<LumensLevel> lumensLevels;

  private List<LumensLevel> obscuredLumensLevels = null;
  private Area visibleArea = null;

  public Illumination(List<LumensLevel> lumensLevels) {
    this.lumensLevels = new ArrayList<>(lumensLevels);
  }

  public @Nonnull List<LumensLevel> getLumensLevels() {
    return Collections.unmodifiableList(lumensLevels);
  }

  public Optional<LumensLevel> getObscuredLumensLevel(int lumensStrength) {
    return getObscuredLumensLevels().stream()
        .filter(level -> level.lumensStrength() == lumensStrength)
        .findFirst();
  }

  public @Nonnull List<LumensLevel> getObscuredLumensLevels() {
    if (obscuredLumensLevels == null) {
      final var obscuredLumensLevels = new ArrayList<LumensLevel>();
      final var strongerDarkness = new Area();
      final var strongerLight = new Area();
      // lumensLevels is sorted weak to strong, we need the opposite.
      for (final var level : Lists.reverse(lumensLevels)) {
        final var obscurredDarknessArea = new Area(level.darknessArea());
        obscurredDarknessArea.subtract(strongerLight);
        strongerDarkness.add(level.darknessArea());

        final var obscurredLightArea = new Area(level.lightArea());
        obscurredLightArea.subtract(strongerDarkness);
        strongerLight.add(level.lightArea());

        obscuredLumensLevels.add(
            new LumensLevel(level.lumensStrength(), obscurredLightArea, obscurredDarknessArea));
      }

      this.obscuredLumensLevels = obscuredLumensLevels;
    }

    return Collections.unmodifiableList(this.obscuredLumensLevels);
  }

  public @Nonnull Area getVisibleArea() {
    if (visibleArea == null) {
      final var result = new Area();
      getObscuredLumensLevels().forEach(level -> result.add(level.lightArea()));
      visibleArea = result;
    }

    return new Area(visibleArea);
  }
}
