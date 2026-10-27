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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import com.google.protobuf.StringValue;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.permissions.Permissions;
import net.rptools.maptool.server.proto.TokenPropertyDto;

public class TokenProperty implements DisplayNames, Serializable {
  // new permissions
  private static final Permissions EDITOR_VIEW_DEFAULT = Permissions.OWNER;
  private static final Permissions EDITOR_EDIT_DEFAULT = Permissions.OWNER;
  private static final Permissions VIEW_STATSHEET_DEFAULT = Permissions.NONE;
  private String name;
  private String shortName;
  private String displayName;
  private String defaultValue = "";
  private Permissions editorViewPermission = EDITOR_VIEW_DEFAULT;
  private Permissions editorEditPermission = EDITOR_EDIT_DEFAULT;
  private Permissions statSheetViewPermission = VIEW_STATSHEET_DEFAULT;

  // old stat-sheet permissions
  @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
  @Deprecated private boolean highPriority; // showOnStatSheet; so that 1.3b28 files load in 1.3b29
  @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
  @Deprecated private boolean ownerOnly;
  @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
  @Deprecated private boolean gmOnly;

  public TokenProperty() {
    // For serialization
  }

  public TokenProperty(String name) {
    this(name, null, null, null, null, null, null);
  }

  public TokenProperty(String name, String shortName, String displayName, Permissions permission) {
    this(name, shortName, displayName, true, true, permission, "");
  }

  public TokenProperty(
      String name,
      String shortName,
      String displayName,
      Boolean playerViewable,
      Boolean playerEditable,
      Permissions statSheetViewPermission,
      String defaultValue) {
    this.name = name;
    if (shortName != null) {
      this.shortName = shortName;
    }
    if (displayName != null) {
      this.displayName = displayName;
    }
    this.setEditorEditPermission(
        playerEditable == null
            ? EDITOR_EDIT_DEFAULT
            : playerEditable ? Permissions.OWNER : Permissions.GM);
    this.setEditorViewPermission(
        playerViewable == null
            ? EDITOR_VIEW_DEFAULT
            : playerViewable ? Permissions.OWNER : Permissions.GM);
    this.setStatSheetViewPermission(
        Objects.requireNonNullElse(statSheetViewPermission, VIEW_STATSHEET_DEFAULT));
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
    setName(prop.getName());
    setShortName(prop.getShortName());
    setDisplayName(prop.getDisplayName());
    setDefaultValue(prop.getDefaultValue());

    if (prop.highPriority) {
      if (prop.ownerOnly) {
        setStatSheetViewPermission(Permissions.OWNER);
      } else if (prop.gmOnly) {
        setStatSheetViewPermission(Permissions.GM);
      } else {
        setStatSheetViewPermission(Permissions.ALL);
      }
    } else {
      setStatSheetViewPermission(prop.getStatSheetViewPermission());
    }
    setEditorEditPermission(prop.getEditorEditPermission());
    setEditorViewPermission(prop.getEditorViewPermission());
  }

  public static TokenProperty fromDto(TokenPropertyDto dto) {
    var prop = new TokenProperty();
    prop.setName(dto.getName());
    prop.setDisplayName(dto.hasDisplayName() ? dto.getDisplayName().getValue() : null);
    prop.setShortName(dto.hasShortName() ? dto.getShortName().getValue() : null);
    prop.setEditorEditPermission(Permissions.fromString(dto.getEditorEditPermission()));
    prop.setEditorViewPermission(Permissions.fromString(dto.getEditorViewPermission()));
    prop.setStatSheetViewPermission(Permissions.fromString(dto.getStatSheetViewPermission()));
    prop.setDefaultValue(dto.hasDefaultValue() ? dto.getDefaultValue().getValue() : null);
    return prop;
  }

  public boolean isShowOnStatSheet() {
    return !statSheetViewPermission.equals(Permissions.NONE);
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
    return this.statSheetViewPermission;
  }

  public void setStatSheetViewPermission(Permissions statSheetViewPermission) {
    this.statSheetViewPermission =
        Objects.requireNonNullElse(statSheetViewPermission, Permissions.NONE);
  }

  public boolean isGMOnly() {
    return this.statSheetViewPermission.equals(Permissions.GM);
  }

  public boolean isOwnerOnly() {
    return this.statSheetViewPermission.equals(Permissions.OWNER);
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Permissions getEditorViewPermission() {
    return this.editorViewPermission;
  }

  public void setEditorViewPermission(Permissions editorViewPermission) {
    this.editorViewPermission =
        Objects.requireNonNullElse(editorViewPermission, EDITOR_VIEW_DEFAULT);
  }

  public void setEditorViewPermission(Boolean value) {
    if (value == null) {
      setEditorViewPermission(EDITOR_VIEW_DEFAULT);
    } else if (value) {
      setEditorViewPermission(Permissions.OWNER);
    } else {
      setEditorViewPermission(Permissions.GM);
      setEditorEditPermission(Permissions.GM);
    }
  }

  public Permissions getEditorEditPermission() {
    return this.editorEditPermission;
  }

  public void setEditorEditPermission(Boolean value) {
    if (value == null) {
      setEditorEditPermission(EDITOR_EDIT_DEFAULT);
    } else if (value) {
      setEditorEditPermission(Permissions.OWNER);
    } else {
      setEditorEditPermission(Permissions.GM);
    }
  }

  public void setEditorEditPermission(Permissions editorEditPermission) {
    this.editorEditPermission =
        Objects.requireNonNullElse(editorEditPermission, EDITOR_EDIT_DEFAULT);
  }

  public TokenPropertyDto toDto() {
    var dto = TokenPropertyDto.newBuilder();
    dto.setName(getName());
    if (displayName != null) {
      dto.setDisplayName(StringValue.of(getDisplayName()));
    }
    if (shortName != null) {
      dto.setShortName(StringValue.of(shortName));
    }
    if (defaultValue != null) {
      dto.setDefaultValue(StringValue.of(getDefaultValue()));
    }
    dto.setEditorEditPermission(getEditorEditPermission().name());
    dto.setEditorViewPermission(getEditorViewPermission().name());
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
      dto.setStatSheetViewPermission(dto.getStatSheetViewPermission());
    }
    return dto.build();
  }

  @Serial
  private @Nonnull Object readResolve() {
    return new TokenProperty(this);
  }
}
