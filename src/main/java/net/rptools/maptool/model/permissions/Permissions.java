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
 *     sheet, and whether a property appears in the token property editor.
 */
public enum Permissions {
  // Order follows increasing visibility for accumulated permissions

  /** Nobody has permission */
  NONE("permission.displayName.none"),
  /** Only the GMs have permission */
  GM("permission.displayName.gm"),
  /** The GMs and the Owners have permission */
  OWNER("permission.displayName.owner"),
  /** The GMs, Owners, and the Owners' Allies have permission */
  ALLIED("permission.displayName.allies"),
  /** Everyone has permission */
  ALL("permission.displayName.all"),

  // Exclusive permissions, i.e. Can exclude the GM, etc.
  /** Only the Owners have permission */
  OWNER_ONLY("permission.displayName.owner.discrete"),
  /** Only the Owners' Allies have permission */
  ALLIED_ONLY("permission.displayName.allies.discrete"),
  /** Only the Owners and the Owners' Allies have permission */
  OWNER_ALLIED_ONLY("permission.displayName.owner.allies.discrete"),
  /** Only the Owners' Opponents have permission */
  OPPONENT_ONLY("permission.displayName.opponent");

  final String displayName;

  Permissions(String i18nKey) {
    this.displayName = I18N.getText(i18nKey);
  }

  public boolean hasPermission(@Nullable Object accessor, @Nullable Object target) {
    return PermissionTest.hasPermission(this, accessor, target);
  }

  public static Permissions fromString(String lookup) {
    try {
      return valueOf(lookup);
    } catch (IllegalArgumentException e) {
      for (Permissions p : values()) {
        if (p.name().equalsIgnoreCase(lookup) || p.displayName.equalsIgnoreCase(lookup)) {
          return p;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
