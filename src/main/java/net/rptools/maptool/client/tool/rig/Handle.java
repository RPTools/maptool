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

import java.awt.geom.Point2D;

/** A draggable node with a definite position in a rig. */
public interface Handle<T> extends Movable<T> {
  /**
   * @return The position of the handle.
   */
  Point2D getPosition();

  /**
   * Moves the handle to {@code point}.
   *
   * <p>Immmediately after this call, {@link #getPosition()} will be the same location as {@code
   * point}.
   *
   * @param point The location to move the handle to.
   */
  void moveTo(Point2D point);
}
