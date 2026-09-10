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
import java.util.Objects;
import net.rptools.maptool.server.proto.TokenPropertyDto;

public class TokenProperty implements DisplayNames, Serializable {
  private String name;
  private String shortName;
  private String displayName;
  private String defaultValue = "";

  // old stat-sheet permissions
  @Deprecated private boolean highPriority; // showOnStatSheet; so that 1.3b28 files load in 1.3b29
  @Deprecated private boolean ownerOnly;
  @Deprecated private boolean gmOnly;

  // new permissions
  private Permissions editorViewPermission = Permissions.OWNER;
  private Permissions editorEditPermission = Permissions.OWNER;
  private Permissions statSheetViewPermission = Permissions.NONE;

  public TokenProperty() {
    // For serialization
  }

  public TokenProperty(String name) {
    this(name, null, (String) null);
  }

  public TokenProperty(String name, String shortName) {
    this(name, shortName, (String) null);
  }

  public TokenProperty(String name, String shortName, String displayName) {
    this.name = name;
    this.shortName = shortName;
    this.displayName = displayName;
  }

  public TokenProperty(String name, boolean playerEditable) {
    this(name, null, null, playerEditable, Permissions.NONE, null);
  }

  public TokenProperty(String name, Permissions statSheetViewPermission) {
    this(name, null, null, true, statSheetViewPermission, null);
  }

  public TokenProperty(String name, String shortName, boolean playerEditable) {
    this(name, shortName, null, playerEditable, Permissions.NONE, null);
  }

  public TokenProperty(String name, String shortName, String displayName, boolean playerEditable) {
    this(name, shortName, displayName, playerEditable, Permissions.NONE, null);
  }

  public TokenProperty(String name, boolean playerEditable, Permissions statSheetViewPermission) {
    this(name, null, null, playerEditable, statSheetViewPermission, null);
  }

  public TokenProperty(String name, String shortName, Permissions statSheetViewPermission) {
    this(name, shortName, null, true, statSheetViewPermission, null);
  }

  public TokenProperty(
      String name, String shortName, boolean playerEditable, Permissions statSheetViewPermission) {
    this(name, shortName, null, playerEditable, statSheetViewPermission, null);
  }

  public TokenProperty(
      String name, String shortName, Permissions statSheetViewPermission, String defaultValue) {
    this(name, shortName, null, true, statSheetViewPermission, defaultValue);
  }

  public TokenProperty(
      String name,
      String shortName,
      boolean playerEditable,
      Permissions statSheetViewPermission,
      String defaultValue) {
    this(name, shortName, null, playerEditable, statSheetViewPermission, defaultValue);
  }

  public TokenProperty(
      String name, String shortName, String displayName, Permissions statSheetViewPermission) {
    this(name, shortName, displayName, true, statSheetViewPermission, null);
  }

  public TokenProperty(
      String name,
      String shortName,
      String displayName,
      boolean playerEditable,
      Permissions statSheetViewPermission) {
    this(name, shortName, displayName, playerEditable, statSheetViewPermission, null);
  }

  public TokenProperty(
      String name,
      String shortName,
      String displayName,
      boolean playerEditable,
      Permissions statSheetViewPermission,
      String defaultValue) {
    this(
        name,
        shortName,
        displayName,
        playerEditable,
        playerEditable,
        statSheetViewPermission,
        defaultValue);
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

  public TokenProperty(
      String name,
      String shortName,
      String displayName,
      boolean playerViewable,
      boolean playerEditable,
      Permissions statSheetViewPermission,
      String defaultValue) {
    this.name = name;
    this.shortName = shortName;
    this.displayName = displayName;
    this.editorEditPermission = playerEditable ? Permissions.OWNER : Permissions.GM;
    this.editorViewPermission = playerViewable ? Permissions.OWNER : Permissions.GM;
    if (statSheetViewPermission != null) {
      this.statSheetViewPermission = statSheetViewPermission;
    }
    this.defaultValue = defaultValue;
  }

  /**
   * Creates a new <code>TokenProperty</code> that's a copy of another.
   *
   * @param prop the property to copy the values from.
   */
  @SuppressWarnings("CopyConstructorMissesField")
  public TokenProperty(TokenProperty prop) {
    this.name = prop.getName();
    this.shortName = prop.getShortName();
    this.displayName = prop.getDisplayName();
    this.defaultValue = prop.getDefaultValue();

    if (prop.highPriority) {
      if (prop.ownerOnly) {
        setStatSheetViewPermission(Permissions.OWNER);
      } else if (prop.gmOnly) {
        setStatSheetViewPermission(Permissions.GM);
      } else {
        setStatSheetViewPermission(Permissions.ALL);
      }
    } else if (prop.hasStatSheetViewPermission()) {
      setStatSheetViewPermission(prop.getStatSheetViewPermission());
    }
    // because these getters substitute nulls for defaults
    setEditorEditPermission(prop.getEditorEditPermission());
    setEditorViewPermission(prop.getEditorViewPermission());
  }

  public boolean isShowOnStatSheet() {
    return statSheetViewPermission != null && !statSheetViewPermission.equals(Permissions.NONE);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasDisplayName() {
    return displayName != null && !displayName.isBlank();
  }

  public boolean hasShortName() {
    return shortName != null && !shortName.isBlank();
  }

  public boolean hasDefaultValue() {
    return defaultValue != null && !defaultValue.isBlank();
  }

  public boolean hasEditorViewPermission() {
    return editorViewPermission != null;
  }

  public boolean hasEditorEditPermission() {
    return editorEditPermission != null;
  }

  public boolean hasStatSheetViewPermission() {
    return statSheetViewPermission != null;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public Permissions getStatSheetViewPermission() {
    if (!hasStatSheetViewPermission()) {
      statSheetViewPermission = Permissions.NONE;
    }
    return statSheetViewPermission;
  }

  public void setStatSheetViewPermission(Permissions statSheetViewPermission) {
    this.statSheetViewPermission = statSheetViewPermission;
  }

  public boolean isGMOnly() {
    return statSheetViewPermission.equals(Permissions.GM);
  }

  public boolean isOwnerOnly() {
    return statSheetViewPermission.equals(Permissions.OWNER);
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Permissions getEditorViewPermission() {
    if (!hasEditorViewPermission()) {
      this.editorViewPermission = Permissions.OWNER;
    }
    return editorViewPermission;
  }

  public void setEditorViewPermission(Permissions editorViewPermission) {
    this.editorViewPermission = editorViewPermission;
  }

  public void setEditorViewPermission(boolean value) {
    if (value) {
      setEditorViewPermission(Permissions.OWNER);
    } else {
      setEditorViewPermission(Permissions.GM);
      setEditorEditPermission(Permissions.GM);
    }
  }

  public Permissions getEditorEditPermission() {
    if (!hasEditorEditPermission()) {
      this.editorEditPermission = Permissions.OWNER;
    }
    return editorEditPermission;
  }

  public void setEditorEditPermission(boolean value) {
    setEditorEditPermission(value ? Permissions.OWNER : Permissions.GM);
  }

  public void setEditorEditPermission(Permissions editorEditPermission) {
    this.editorEditPermission = editorEditPermission;
  }

  public static TokenProperty fromDto(TokenPropertyDto dto) {
    var prop = new TokenProperty();
    prop.name = dto.getName();
    prop.shortName = dto.hasShortName() ? dto.getShortName().getValue() : null;

    prop.editorEditPermission =
        dto.hasEditorEditPermission()
            ? Permissions.valueOf(dto.getEditorEditPermission())
            : Permissions.OWNER;
    prop.editorViewPermission =
        dto.hasEditorViewPermission()
            ? Permissions.valueOf(dto.getEditorViewPermission())
            : Permissions.OWNER;
    prop.statSheetViewPermission = Permissions.valueOf(dto.getStatSheetViewPermission());

    prop.defaultValue = dto.hasDefaultValue() ? dto.getDefaultValue().getValue() : null;
    prop.displayName = dto.hasDisplayName() ? dto.getDisplayName().getValue() : null;
    return prop;
  }

  public TokenPropertyDto toDto() {
    var dto = TokenPropertyDto.newBuilder();
    dto.setName(name);
    dto.setEditorEditPermission(
        Objects.requireNonNullElse(editorEditPermission, Permissions.OWNER).name());
    dto.setEditorViewPermission(
        Objects.requireNonNullElse(editorViewPermission, Permissions.OWNER).name());
    // for campaigns pre 1.19
    if (highPriority || ownerOnly || gmOnly) {
      if (ownerOnly) {
        dto.setStatSheetViewPermission(Permissions.OWNER.name());
      } else if (gmOnly) {
        dto.setStatSheetViewPermission(Permissions.GM.name());
      } else {
        dto.setStatSheetViewPermission(Permissions.ALL.name());
      }
    } else {
      dto.setStatSheetViewPermission(
          Objects.requireNonNullElse(statSheetViewPermission, Permissions.NONE).name());
    }
    if (hasShortName()) {
      dto.setShortName(StringValue.of(shortName));
    }
    if (hasDisplayName()) {
      dto.setDisplayName(StringValue.of(displayName));
    }
    if (hasDefaultValue()) {
      dto.setDefaultValue(StringValue.of(defaultValue));
    }
    return dto.build();
  }
}
