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
package net.rptools.maptool.model;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.server.proto.AttachedLightSourceDto;

public final class AttachedLightSource {

  private final @Nonnull GUID lightSourceId;

  public AttachedLightSource(@Nonnull GUID lightSourceId) {
    this.lightSourceId = lightSourceId;
  }

  /**
   * Obtain the attached {@code LightSource} from the token or campaign.
   *
   * @param token The token in which to look up light source IDs.
   * @param campaign The campaign in which to look up light source IDs.
   * @return The {@code LightSource} referenced by this {@code AttachedLightSource}, or {@code null}
   *     if no such light source exists.
   */
  public @Nullable LightSource resolve(Token token, Campaign campaign) {
    final var uniqueLightSource = token.getUniqueLightSource(lightSourceId);
    if (uniqueLightSource != null) {
      return uniqueLightSource;
    }

    for (Map<GUID, LightSource> map : campaign.getLightSourcesMap().values()) {
      if (map.containsKey(lightSourceId)) {
        return map.get(lightSourceId);
      }
    }

    return null;
  }

  /**
   * Check if this {@code AttachedLightSource} references a {@code LightSource} with a matching ID.
   *
   * @param lightSourceId The ID of the light source to match against.
   * @return {@code true} If {@code lightSourceId} is the same as the ID of the attached light
   *     source.
   */
  public boolean matches(@Nonnull GUID lightSourceId) {
    return lightSourceId.equals(this.lightSourceId);
  }

  public static AttachedLightSource fromDto(AttachedLightSourceDto dto) {
    return new AttachedLightSource(GUID.valueOf(dto.getLightSourceId()));
  }

  public AttachedLightSourceDto toDto() {
    var dto = AttachedLightSourceDto.newBuilder();
    dto.setLightSourceId(lightSourceId.toString());
    return dto.build();
  }
}
