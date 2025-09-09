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

/**
 * A class that holds the properties of a stat sheet for a token type or a token. Note this can't be
 * a record as current version of xstream does not support records.
 */
public final class StatSheetProperties {

  /** The id of the stat sheet. */
  private final String id;

  /** The location of the stat sheet. */
  private final StatSheetLocation location;

  /**
   * Creates a new instance of the class.
   *
   * @param id The id of the stat sheet.
   * @param location The location of the stat sheet.
   */
  public StatSheetProperties(String id, StatSheetLocation location) {
    this.id = id;
    this.location = location;
  }

  /**
   * Creates a new instance of the class from the DTO.
   *
   * @param dto The DTO to create the instance from.
   * @return The new instance.
   */
  public static StatSheetProperties fromDto(StatSheetPropertiesDto dto) {
    return new StatSheetProperties(dto.getId(), StatSheetLocation.valueOf(dto.getLocation()));
  }

  /**
   * Extracts the DTO from the stat sheet.
   *
   * @param sheet The stat sheet to extract the DTO from.
   * @return The DTO.
   */
  public static StatSheetPropertiesDto toDto(StatSheetProperties sheet) {
    var builder = StatSheetPropertiesDto.newBuilder();
    builder.setId(sheet.id());
    builder.setLocation(sheet.location().name());
    return builder.build();
  }

  /**
   * Returns the id of the stat sheet.
   *
   * @return The id of the stat sheet.
   */
  public String id() {
    return id;
  }

  /**
   * Returns the location of the stat sheet.
   *
   * @return The location of the stat sheet.
   */
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
