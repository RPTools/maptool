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

import com.google.protobuf.StringValue;
import java.io.Serializable;
import net.rptools.maptool.server.proto.TokenPropertyDto;

public class TokenProperty implements Serializable {
  private String name;
  private String shortName;
  private boolean highPriority; // showOnStatSheet; so that 1.3b28 files load in 1.3b29
  private boolean ownerOnly;
  private boolean gmOnly;
  private String defaultValue;

  public TokenProperty() {
    // For serialization
  }

  public TokenProperty(String name) {
    this(name, null, false, false, false);
  }

  public TokenProperty(String name, String shortName) {
    this(name, shortName, false, false, false);
  }

  public TokenProperty(String name, boolean highPriority, boolean isOwnerOnly, boolean isGMOnly) {
    this(name, null, highPriority, isOwnerOnly, isGMOnly);
  }

  public TokenProperty(
      String name, String shortName, boolean highPriority, boolean isOwnerOnly, boolean isGMOnly) {
    this.name = name;
    this.shortName = shortName;
    this.highPriority = highPriority;
    this.ownerOnly = isOwnerOnly;
    this.gmOnly = isGMOnly;
  }

  public TokenProperty(
      String name,
      String shortName,
      boolean highPriority,
      boolean isOwnerOnly,
      boolean isGMOnly,
      String defaultValue) {
    this.name = name;
    this.shortName = shortName;
    this.highPriority = highPriority;
    this.ownerOnly = isOwnerOnly;
    this.gmOnly = isGMOnly;
    this.defaultValue = defaultValue;
  }

  /**
   * Creates a new <code>TokenProperty</code> that's a copy of another.
   *
   * @param prop the property to copy the values from.
   */
  public TokenProperty(TokenProperty prop) {
    this.name = prop.name;
    this.shortName = prop.shortName;
    this.highPriority = prop.highPriority;
    this.ownerOnly = prop.ownerOnly;
    this.gmOnly = prop.gmOnly;
    this.defaultValue = prop.defaultValue;
  }

  public boolean isOwnerOnly() {
    return ownerOnly;
  }

  public void setOwnerOnly(boolean ownerOnly) {
    this.ownerOnly = ownerOnly;
  }

  public boolean isShowOnStatSheet() {
    return highPriority;
  }

  public void setShowOnStatSheet(boolean showOnStatSheet) {
    this.highPriority = showOnStatSheet;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public boolean isGMOnly() {
    return gmOnly;
  }

  public void setGMOnly(boolean gmOnly) {
    this.gmOnly = gmOnly;
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public static TokenProperty fromDto(TokenPropertyDto dto) {
    var prop = new TokenProperty();
    prop.name = dto.getName();
    prop.shortName = dto.hasShortName() ? dto.getShortName().getValue() : null;
    prop.highPriority = dto.getHighPriority();
    prop.ownerOnly = dto.getOwnerOnly();
    prop.gmOnly = dto.getGmOnly();
    prop.defaultValue = dto.hasDefaultValue() ? dto.getDefaultValue().getValue() : null;
    return prop;
  }

  public TokenPropertyDto toDto() {
    var dto = TokenPropertyDto.newBuilder();
    dto.setName(name);
    if (shortName != null) dto.setShortName(StringValue.of(shortName));
    dto.setHighPriority(highPriority);
    dto.setOwnerOnly(ownerOnly);
    dto.setGmOnly(gmOnly);
    if (defaultValue != null) dto.setDefaultValue(StringValue.of(defaultValue));
    return dto.build();
  }
}
