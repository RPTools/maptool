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

public class Association<E, T> {

  private E lhs;
  private T rhs;

  public Association(E lhs, T rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public void setLeft(E value) {
    lhs = value;
  }

  public void setRight(T value) {
    rhs = value;
  }

  public E getLeft() {
    return lhs;
  }

  public T getRight() {
    return rhs;
  }
}
