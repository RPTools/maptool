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
package net.rptools.maptool.client.tool.rig;

public interface Movable<T> {
  Rig getParentRig();

  T getSource();

  /**
   * Check whether {@code this} and {@code other} represent the same element in the same parent rig.
   *
   * @param other
   * @return {@code true} if {@code this} and {@code other} represent the same vertex in the same
   *     rig.
   */
  boolean isForSameElement(Movable<?> other);

  /**
   * Moves the handle by applying a displacement relative to its original position.
   *
   * @param displacementX The amount to displace the handle along in the x-direction.
   * @param displacementY The amount to displace the handle along in the y-direction.
   * @param snapMode The snap behaviour to apply.
   */
  void displace(double displacementX, double displacementY, Snap snapMode);

  /** Commits the latest displacement from {@link #displace(double, double, Snap)} to the source. */
  void applyMove();

  /** Delete the element from the parent rig. */
  void delete();
}
