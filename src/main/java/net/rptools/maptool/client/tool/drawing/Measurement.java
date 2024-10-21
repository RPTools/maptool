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
package net.rptools.maptool.client.tool.drawing;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public sealed interface Measurement {
  record Rectangular(Rectangle2D bounds) implements Measurement {}

  record LineSegment(Point2D p1, Point2D p2) implements Measurement {}

  record IsoRectangular(Point2D north, Point2D west, Point2D east) implements Measurement {}
}
