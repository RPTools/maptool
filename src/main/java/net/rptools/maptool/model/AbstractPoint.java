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

import net.rptools.maptool.server.proto.drawing.IntPointDto;

public abstract sealed class AbstractPoint implements Cloneable permits ZonePoint, CellPoint {

  public int x;
  public int y;

  /**
   * Only populated by AStarWalker classes to be used upstream.
   *
   * @return whether A* couldn't find a path to the cell.
   */
  public boolean isAStarCanceled() {
    return false;
  }

  public AbstractPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void translate(int dx, int dy) {
    x += dx;
    y += dy;
  }

  public boolean equals(Object o) {
    if (!(o instanceof AbstractPoint)) return false;
    AbstractPoint p = (AbstractPoint) o;

    return p.x == x && p.y == y;
  }

  public int hashCode() {
    return (x + "-" + y).hashCode();
  }

  public String toString() {
    return "[" + x + "," + y + "]";
  }

  public AbstractPoint clone() {
    try {
      return (AbstractPoint) super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(e);
    }
  }

  public IntPointDto toDto() {
    return IntPointDto.newBuilder().setX(x).setY(y).build();
  }
}
