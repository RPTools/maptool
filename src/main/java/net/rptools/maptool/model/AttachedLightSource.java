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

public class AttachedLightSource {

  private GUID lightSourceId;
  private String direction;

  public AttachedLightSource() {
    // for serialization
  }

  public AttachedLightSource(LightSource source, Direction direction) {
    lightSourceId = source.getId();
    this.direction = direction.name();
  }

  public Direction getDirection() {
    return direction != null ? Direction.valueOf(direction) : Direction.CENTER;
  }

  public GUID getLightSourceId() {
    return lightSourceId;
  }
}
