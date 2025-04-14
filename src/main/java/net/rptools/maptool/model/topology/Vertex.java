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
package net.rptools.maptool.model.topology;

import java.awt.geom.Point2D;
import java.util.Objects;
import net.rptools.maptool.model.GUID;

/**
 * Represents the end of one or more walls.
 *
 * <p>The position is intentionally mutable as vertices are free to move about the plane without
 * affecting the graph structure.
 */
public final class Vertex {
  private final GUID id;
  private final Point2D position;
  private int zIndex;

  public Vertex() {
    this(new GUID());
  }

  public Vertex(GUID id) {
    this.id = id;
    this.position = new Point2D.Double(0, 0);
    this.zIndex = -1;
  }

  public boolean equals(Object obj) {
    return obj instanceof Vertex vertex && Objects.equals(id, vertex.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public GUID id() {
    return id;
  }

  public Point2D position() {
    return new Point2D.Double(position.getX(), position.getY());
  }

  public void position(double x, double y) {
    this.position.setLocation(x, y);
  }

  public void position(Point2D position) {
    this.position.setLocation(position);
  }

  public int zIndex() {
    return zIndex;
  }

  public void zIndex(int zIndex) {
    this.zIndex = zIndex;
  }
}
