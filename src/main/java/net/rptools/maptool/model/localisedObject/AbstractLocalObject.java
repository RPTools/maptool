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
package net.rptools.maptool.model.localisedObject;

import java.util.Arrays;
import net.rptools.maptool.language.I18N;
import org.jetbrains.annotations.NotNull;

/** Stores the localized display name and preference value for list, combo and menu items */
public abstract class AbstractLocalObject implements LocalObject {
  /** The actual value used by the preference. */
  Object value;

  /** The key used to look up the localised display name. */
  String i18nKey = "";

  /** The localised display name of a menu item or combo box option visible to the user. */
  String displayName;

  /**
   * Create a local object without performing the i18n lookup
   *
   * @param value the value held by the object
   * @param displayName the display name presented to users
   * @param dummy just here to change the signature so no lookup is performed.
   */
  AbstractLocalObject(@NotNull Object value, String displayName, Object dummy) {
    this.value = value;
    this.displayName = displayName;
  }

  /**
   * Create a local object and fetches the localised display name
   *
   * @param value the value held by the object
   * @param i18nKeyAndArgs varArgs for the i18n key and any optional arguments to pass with it.
   */
  AbstractLocalObject(@NotNull Object value, String... i18nKeyAndArgs) {
    this.value = value;
    this.i18nKey = i18nKeyAndArgs[0];
    if (i18nKeyAndArgs.length == 1) {
      this.displayName = I18N.getText(this.i18nKey);
    } else {
      this.displayName =
          I18N.getText(
              this.i18nKey,
              (Object) Arrays.copyOfRange(i18nKeyAndArgs, 1, i18nKeyAndArgs.length - 1));
    }
  }

  /**
   * @return {@link #value} {@inheritDoc}
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * @return {@link #i18nKey} {@inheritDoc}
   */
  public String getI18nKey() {
    return i18nKey;
  }

  /**
   * @return {@link #displayName} {@inheritDoc}
   */
  public @NotNull String getDisplayName() {
    return displayName;
  }
}
