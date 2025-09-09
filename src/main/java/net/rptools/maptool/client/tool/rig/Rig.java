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
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** A rig is a representation of a model in terms of handles and other movable elements. */
public interface Rig<E> {
  default Optional<? extends Handle<?>> getNearbyHandle(Point2D point) {
    return getNearbyHandle(point, 0., element -> true);
  }

  Optional<? extends Handle<?>> getNearbyHandle(
      Point2D point, double extraSpace, Predicate<E> filter);

  List<? extends Handle<?>> getHandlesWithin(Rectangle2D bounds);

  default Optional<? extends E> getNearbyElement(Point2D point) {
    return getNearbyElement(point, 0., element -> true);
  }

  Optional<? extends E> getNearbyElement(Point2D point, double extraSpace, Predicate<E> filter);
}
