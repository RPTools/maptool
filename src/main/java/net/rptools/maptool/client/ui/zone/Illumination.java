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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * The main result type returned by an {@link Illuminator}.
 *
 * <p>At its core, this is a list of {@link LumensLevel} objects that provides a mechanical
 * description of the strength of light at each point. The areas are not disjoint, i.e., multiple
 * {@link LumensLevel}s can overlap with each other. Also, light and darkness are not subtracted
 * from each other.
 *
 * <p>From the basic structure, we are able to calculate a few derivative results:
 *
 * <ol>
 *   <li>The obscured lumens levels. For each light area in the basic structure, subtract out any
 *       stronger darknesses, and for each darkness subtract out any stronger lights. The result is
 *       the obscured lit areas arranged by lumens level.
 *   <li>The complete lit area. This is the union of the light areas after the process in (1).
 *   <li>The disjoint obscured lumens levels. Starting from (1), we can additionally subtract strong
 *       light from weak light and strong darkness from weak darkness so that any given point is
 *       represented only in the strongest lumens level.
 * </ol>
 */
public final class Illumination {
  /**
   * Represents the area covered by a given lumens level.
   *
   * <p>A lumens level does not distinguish between different light sources, but contains unions of
   * various areas covered in light or darkness. Depending on context, the lumens level may or may
   * not incorporate obscurement.
   *
   * @param lumensStrength The lumens associated with the area.
   * @param lightArea The area covered by lights of this lumens value.
   * @param darknessArea The area covered by darkness of this lumens value.
   */
  public record LumensLevel(int lumensStrength, Area lightArea, Area darknessArea) {
    public LumensLevel(int lumensStrength) {
      this(lumensStrength, new Area(), new Area());
    }

    public LumensLevel copy() {
      return new LumensLevel(lumensStrength, new Area(lightArea), new Area(darknessArea));
    }
  }

  /**
   * The complete set of original lumens levels, without obscurement.
   *
   * <p>This list is ordered from strong lumens to weak lumens.
   */
  private final List<LumensLevel> lumensLevels;

  // region Cached fields. These are results that derive from {@link #lumensLevels}.

  /**
   * The obscured lumens levels.
   *
   * <p>An obscured lumens level has all stronger darkness subtracted from weaker light, and
   * stronger light subtracted from weaker darkness. If a darkness and light have the same lumens
   * strength, the darkness is considered stronger.
   *
   * <p>This list is ordered from strong lumens to weak lumens.
   */
  private List<LumensLevel> obscuredLumensLevels = null;

  /**
   * The disjoint obscured lumens levels.
   *
   * <p>These are the areas from {@link #obscuredLumensLevels}, modified so that there is no overlap
   * between any areas. Darkness and light are already disjoint from one another, but this list goes
   * further by subtracting strong light from weak light, and strong darkness from weak darkness.
   * The result is that any given point will only be associated with the strongest light or
   * darkness.
   *
   * <p>This list is ordered from strong lumens to weak lumens.
   */
  private List<LumensLevel> disjointObscuredLumensLevels = null;

  /**
   * The complete lit area.
   *
   * <p>This is derived from {@link #obscuredLumensLevels} by unioning all light areas and leaving
   * out all darkness areas.
   */
  private Area litArea = null;

  /**
   * The complete darkened area.
   *
   * <p>This is derived from {@link #obscuredLumensLevels} by unioning all darkness areas and
   * leaving out all light areas.
   */
  private Area darkenedArea = null;

  // endregion

  /**
   * Create a new {@code Illumination} from a set of base lumens levels.
   *
   * <p>The {@code lumensLevels} should contain the complete areas that <emp>could</emp> be covered
   * by each level of lumens. Obscurement (darkness competing with light) should not already be
   * calculated, as the {@code Illumination} will handle this.
   *
   * @param lumensLevels The base areas covered by each level of lumens.
   */
  public Illumination(List<LumensLevel> lumensLevels) {
    this.lumensLevels = new ArrayList<>(lumensLevels);
    this.lumensLevels.sort(
        Comparator.<LumensLevel>comparingInt(lhs -> lhs.lumensStrength).reversed());
  }

  /**
   * Look up an obscured lumens level based on the lumens strength.
   *
   * <p>This is useful for rendering individual lights, so that the light can be constrained to the
   * area that is actually illuminated by the light.
   *
   * @param lumensStrength The strength of lumens to find.
   * @return The {#link LumensLevel} of strength {@code lumensStrength}. If no level exists for it,
   *     an empty optional is returned.
   */
  public Optional<LumensLevel> getObscuredLumensLevel(int lumensStrength) {
    return getObscuredLumensLevels().stream()
        .filter(level -> level.lumensStrength() == lumensStrength)
        .findFirst();
  }

  private @Nonnull List<LumensLevel> getObscuredLumensLevels() {
    if (obscuredLumensLevels == null) {
      final var obscuredLumensLevels = new ArrayList<LumensLevel>();
      final var strongerDarkness = new Area();
      final var strongerLight = new Area();
      // lumensLevels is already sorted strong to weak, which is what we need.
      for (final var level : lumensLevels) {
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

  /**
   * Look up a disjoint obscured lumens level based on the lumens strength.
   *
   * <p>This is useful for rendering individual lights, so that the light can be constrained to the
   * area that is actually illuminated by the light and not by any stronger light.
   *
   * <p>See {@link #getDisjointObscuredLumensLevels()} for more information.
   *
   * @param lumensStrength The strength of lumens to find.
   * @return The {#link LumensLevel} of strength {@code lumensStrength}. If no level exists for it,
   *     an empty optional is returned.
   */
  public Optional<LumensLevel> getDisjointObscuredLumensLevel(int lumensStrength) {
    return getDisjointObscuredLumensLevels().stream()
        .filter(level -> level.lumensStrength() == lumensStrength)
        .findFirst();
  }

  /**
   * Get the disjoint obscured lumens levels.
   *
   * <p>Each lumens level in the result will have areas that do not intersect any other areas in any
   * lumens level. Stronger darkness will have been subtracted from weaker light and darkness, and
   * stronger light from weaker darkness and light.
   *
   * <p>This is useful for rendering the lumens levels, so that well-defined boundaries exist
   * between each lumens level.
   *
   * @return The obscured lumens levels, ordered from strong to weak lumens.
   */
  public @Nonnull List<LumensLevel> getDisjointObscuredLumensLevels() {
    if (disjointObscuredLumensLevels == null) {
      final var obscuredLumensLevels = this.getObscuredLumensLevels();
      final var disjointObscuredLumensLevels = new ArrayList<LumensLevel>();
      // The obscured levels already have strong darkness removed from weak light, and strong light
      // removed from weak darkness. Now we need to make sure strong light is also removed from weak
      // light, and strong darkness from weak darkness.
      final var strongerDarkness = new Area();
      final var strongerLight = new Area();
      // obscuredLumensLevels is sorted strong to weak, which works for us.
      for (final var level : obscuredLumensLevels) {
        final var obscurredDarknessArea = new Area(level.darknessArea());
        obscurredDarknessArea.subtract(strongerDarkness);
        strongerDarkness.add(level.darknessArea());

        final var obscurredLightArea = new Area(level.lightArea());
        obscurredLightArea.subtract(strongerLight);
        strongerLight.add(level.lightArea());

        disjointObscuredLumensLevels.add(
            new LumensLevel(level.lumensStrength(), obscurredLightArea, obscurredDarknessArea));
      }

      this.disjointObscuredLumensLevels = disjointObscuredLumensLevels;
    }

    return Collections.unmodifiableList(this.disjointObscuredLumensLevels);
  }

  /**
   * Get the total lit area from all lumens levels.
   *
   * <p>After subtracting stronger darkness from weaker lights, the resulting lights are unioned
   * into a single area.
   *
   * @return The lit area.
   */
  public @Nonnull Area getLitArea() {
    if (litArea == null) {
      final var result = new Area();
      getObscuredLumensLevels().forEach(level -> result.add(level.lightArea()));
      litArea = result;
    }

    return new Area(litArea);
  }

  /**
   * Get the total dark area from all lumens levels.
   *
   * <p>After subtracting stronger lights from weaker darkness, the resulting darknesses are unioned
   * into a single area.
   *
   * @return The darkened area.
   */
  public @Nonnull Area getDarkenedArea() {
    if (darkenedArea == null) {
      final var result = new Area();
      getObscuredLumensLevels().forEach(level -> result.add(level.darknessArea()));
      darkenedArea = result;
    }

    return new Area(darkenedArea);
  }

  /**
   * Creates a new {@code Illumination} with extra lighting.
   *
   * <p>This is useful for adding in temporary lights while keeping the original illumination
   * around.
   *
   * @param extraLights The lit areas to include in the new {@code Illumination}
   * @return An {@code Illumination} containing the lit areas of {@code this} and {@code
   *     extraLights}.
   */
  public @Nonnull Illumination withExtraLights(Collection<Illuminator.LitArea> extraLights) {
    final var newLevels = new ArrayList<>(Lists.transform(this.lumensLevels, LumensLevel::copy));

    for (final var extraLitArea : extraLights) {
      final var isDarkness = extraLitArea.lumens() < 0;
      final var lumensStrength = Math.abs(extraLitArea.lumens());
      final var area = extraLitArea.area();

      final var index =
          Collections.binarySearch(
              Lists.transform(newLevels, LumensLevel::lumensStrength),
              lumensStrength,
              Collections.reverseOrder(Integer::compare));
      final LumensLevel level;
      if (index >= 0) {
        // Already a lumens level. Add onto it.
        level = newLevels.get(index);
      } else {
        final var insertionPoint = -index - 1;
        level = new LumensLevel(lumensStrength);
        newLevels.add(insertionPoint, level);
      }
      (isDarkness ? level.darknessArea() : level.lightArea()).add(area);
    }

    return new Illumination(newLevels);
  }
}
