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

import net.rptools.maptool.language.I18N;

/** The location of a stat sheet on a map view. */
public enum StatSheetLocation {
  TOP_LEFT("position.corner.topLeft"),
  TOP("position.side.top"),
  TOP_RIGHT("position.corner.topRight"),
  RIGHT("position.side.right"),
  BOTTOM_RIGHT("position.corner.bottomRight"),
  BOTTOM("position.side.bottom"),
  BOTTOM_LEFT("position.corner.bottomLeft"),
  LEFT("position.side.left");

  /** The i18n key for the location. */
  private final String i18nKey;

  /**
   * Creates a new instance of <code>StatSheetLocation</code>.
   *
   * @param i18nKey The i18n key for the location.
   */
  StatSheetLocation(String i18nKey) {
    this.i18nKey = i18nKey;
  }

  /**
   * Returns the i18n key for the location.
   *
   * @return The i18n key for the location.
   */
  public String getI18nKey() {
    return i18nKey;
  }

  @Override
  public String toString() {
    return I18N.getString(i18nKey);
  }
}
