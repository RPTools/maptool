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

public abstract class AbstractPoint implements Cloneable {

  public int x;
  public int y;

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
    return new String(x + "-" + y).hashCode();
  }

  public String toString() {
    return "[" + x + "," + y + "]";
  }

  public AbstractPoint clone() {
    try {
      return (AbstractPoint) super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }
}
