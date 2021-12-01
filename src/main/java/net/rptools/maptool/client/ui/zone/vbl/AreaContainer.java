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
package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.List;
import javax.annotation.Nullable;
import org.locationtech.jts.geom.GeometryFactory;

public interface AreaContainer {
  public Area getBounds();

  /**
   * Get the smallest child container containing `point`.
   *
   * @param point The point to check for containment.
   * @return If `point` is not within `this`, then `null`. Otherwise, the most nested descendant
   *     container of `this` that contains `point` (possibly `this` itself).
   */
  public @Nullable AreaContainer getDeepestContainerAt(Point2D point);

  /**
   * Find sections of the container's boundary that block vision.
   *
   * <p>Each returned segment is a sequence of connected faces which constitute an unbroken section
   * of the container's boundary that would block vision. Each segment is aware of `origin` so that
   * it can construct areas that cannot be seen from `origin`.
   *
   * <p>The segments that are returned depend on `origin`, and `frontSegments`.
   *
   * @param geometryFactory The strategy for creating geometries, which is used by the
   *     `VisibleAreaSegment` in creating areas of blocked vision.
   * @param origin The point from which visibility is calculated.
   * @param frontSegments If `true`, only front-facing boundary segments are returned. Otherwise,
   *     only back-facing boundary segments are returned. Due to winding order, back-facing and
   *     front-facing are interpretted differently for oceans and islands.
   * @return A list of segments, which together represent the complete set of boundary faces that
   *     block vision.
   */
  public List<VisibleAreaSegment> getVisionBlockingBoundarySegements(
      GeometryFactory geometryFactory, Point origin, boolean frontSegments);
}
