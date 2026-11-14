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
package net.rptools.maptool.model.permissions;

import javax.annotation.Nullable;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.DisplayName;
import net.rptools.maptool.model.TokenProperty;

/**
 *
 *
 * <h4>Defined Player permissions</h4>
 *
 * <li>Accommodates ordinary RPG use for GM vs Players
 * <li>Adds a level {@link #ALLIED} where friendlies have permission as a step before {@link #ALL}
 * <li>This opens the door for Teams of Players
 * <li>Introduces exclusive permissions beyond GM Only
 *
 *     <p>Used in {@link TokenProperty} for determining who can see a property on a pop-up attribute
 *
 *     <p>sheet, and whether a property appears in the token property editor.
 */
public enum Permissions implements DisplayName.WithTooltip {
  // Order follows increasing visibility for accumulated permissions

  /** Nobody has permission */
  NONE("permission.none.name"),
  /** Only the GMs have permission */
  GM("permission.gm.name"),
  /** The GMs and the Owners have permission */
  OWNER("permission.owner.name"),
  /** The GMs, Owners, and the Owners' Allies have permission */
  ALLIED("permission.allies.name"),
  /** Everyone has permission */
  ALL("permission.all.name"),

  // Exclusive permissions, i.e. Can exclude the GM, etc.
  /** Only the Owners have permission */
  OWNER_ONLY("permission.owner.discrete.name"),
  /** Only the Owners' Allies have permission */
  ALLIED_ONLY("permission.allies.discrete.name"),
  /** Only the Owners and the Owners' Allies have permission */
  OWNER_ALLIED_ONLY("permission.owner.allies.discrete.name"),
  /** Only the Owners' Opponents have permission */
  OPPONENT_ONLY("permission.opponent.name");

  private static PermissionsTests gateKeeper = new RPGGateKeeper();
  final String i18nNameKey;
  final String i18nDescriptionKey;

  Permissions(String i18nKey) {
    this.i18nNameKey = i18nKey;
    this.i18nDescriptionKey = i18nKey + ".description";
    I18N.getText(i18nKey);
  }

  public static Permissions fromString(String lookup) {
    try {
      return valueOf(lookup);
    } catch (IllegalArgumentException e) {
      for (Permissions p : values()) {
        if (p.name().equalsIgnoreCase(lookup) || p.i18nNameKey.equalsIgnoreCase(lookup)) {
          return p;
        }
      }
    }
    return null;
  }

  public static void setGateKeeper(PermissionsTests gateKeeper) {
    Permissions.gateKeeper = gateKeeper;
  }

  public boolean hasPermission(@Nullable Object accessor) {
    return gateKeeper.getAccessorTest().apply(this, accessor);
  }

  public boolean hasPermission(@Nullable Object accessor, @Nullable Object object) {
    return gateKeeper.getObjectAccessTest().apply(this, accessor, object);
  }

  public boolean isAllied(@Nullable Object accessor, @Nullable Object other) {
    return gateKeeper.getObjectAllyTest().apply(accessor, other);
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  @Override
  public String getDescription() {
    return I18N.getMessage(i18nDescriptionKey);
  }

  @Override
  public void setDescription(String value) {
    // unused
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public void setName(String value) {
    // unused
  }

  @Override
  public String getDisplayName() {
    return I18N.getMessage(i18nNameKey);
  }

  @Override
  public void setDisplayName(String value) {
    // unused
  }
}
