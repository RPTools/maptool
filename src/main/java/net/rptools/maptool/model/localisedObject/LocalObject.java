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

/** Stores the localized display name and preference value for list, combo and menu items */
public interface LocalObject {

  /**
   * @return the actual value used by the application.
   */
  Object getValue();

  /**
   * @return the key to look up the localised display value
   */
  String getI18nKey();

  /**
   * @return the localised display value
   */
  String getDisplayName();
}
