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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;

/**
 * Manages the light sources and illuminations of a zone, for a given set of illuminator parameters.
 *
 * <p>This needs to be kept in sync with the associated {@code Zone} in order for the results to
 * make sense
 *
 * <p>No caching is done here, this is purely a data structure.
 */
public class IlluminationModel {
  /**
   * Associates a LitArea stored in an Illuminator with the Light that it was created for.
   *
   * <p>This is used to represent normal lights, personal lights, and day light. In the case of
   * daylight, lightInfo will be null since it doesn't originate from an actual light, and should
   * never be rendered.
   *
   * @param litArea
   * @param lightInfo
   */
  public record ContributedLight(Illuminator.LitArea litArea, LightInfo lightInfo) {}

  /**
   * Combines a LightSource and a Light for easy referencing in ContributedLight.
   *
   * @param lightSource
   * @param light
   */
  public record LightInfo(LightSource lightSource, Light light) {}

  /**
   * The data structure for calculating lit areas according to lumens. Lit areas can be added and
   * removed from this structure.
   */
  private final Illuminator illuminator = new Illuminator();

  /**
   * The list of all non-personal lights contributing to the the illuminator.
   *
   * <p>This is used to associate the LitAreas added to the Illuminator with the original lighting
   * parameters that created it. We can use this information to generate DrawableLights for
   * rendering.
   */
  private final Map<GUID, List<ContributedLight>> contributedLightsByToken = new HashMap<>();

  public void removeToken(GUID tokenId) {
    final var contributions =
        Objects.requireNonNullElse(
            contributedLightsByToken.remove(tokenId), Collections.<ContributedLight>emptyList());
    // Remove each contribution from the illuminator as well.
    for (final var contributedLight : contributions) {
      illuminator.remove(contributedLight.litArea());
    }
  }

  public boolean hasToken(GUID tokenId) {
    return contributedLightsByToken.containsKey(tokenId);
  }

  public void addToken(GUID tokenId, List<ContributedLight> contributions) {
    for (final var contribution : contributions) {
      illuminator.add(contribution.litArea());
      contributedLightsByToken.computeIfAbsent(tokenId, id -> new ArrayList<>()).add(contribution);
    }
  }

  public Stream<ContributedLight> getContributions() {
    return contributedLightsByToken.values().stream().flatMap(Collection::stream);
  }

  public Illumination getIllumination() {
    return illuminator.getIllumination();
  }
}
