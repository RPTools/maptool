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
package net.rptools.maptool.model.sheet.stats;

import java.util.Objects;
import net.rptools.maptool.server.proto.StatSheetPropertiesDto;

public final class StatSheetProperties {

  private final String id;
  private final StatSheetLocation location;

  public StatSheetProperties(String id, StatSheetLocation location) {
    this.id = id;
    this.location = location;
  }

  public static StatSheetProperties fromDto(StatSheetPropertiesDto dto) {
    return new StatSheetProperties(dto.getId(), StatSheetLocation.valueOf(dto.getLocation()));
  }

  public static StatSheetPropertiesDto toDto(StatSheetProperties sheet) {
    var builder = StatSheetPropertiesDto.newBuilder();
    builder.setId(sheet.id());
    builder.setLocation(sheet.location().name());
    return builder.build();
  }

  public String id() {
    return id;
  }

  public StatSheetLocation location() {
    return location;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (StatSheetProperties) obj;
    return Objects.equals(this.id, that.id) && Objects.equals(this.location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, location);
  }

  @Override
  public String toString() {
    return "StatSheetProperties[" + "id=" + id + ", " + "location=" + location + ']';
  }
}
