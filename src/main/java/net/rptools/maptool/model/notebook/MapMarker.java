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
package net.rptools.maptool.model.notebook;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;

/** Interface that describes a general marker on the map. */
public interface MapMarker {

  /**
   * Returns the MD5Key of the {@link Asset} used to display the marker on the map.
   *
   * @return the MD5Key of the {@link Asset} used to display the marker on the map.
   */
  MD5Key getAsset();

  /**
   * Returns the x co-ordinate of the center of the marker on the map.
   *
   * @return the x co-ordinate of the center of the marker on the map.
   */
  double getIconCenterX();

  /**
   * Returns the y co-ordinate of the center of the marker on the map.
   *
   * @return the y co-ordinate of the center of the marker on the map.
   */
  double getIconCenterY();
}
