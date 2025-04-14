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

import java.util.ArrayList;
import java.util.List;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.model.topology.MaskTopology;
import net.rptools.maptool.model.topology.Topology;
import net.rptools.maptool.model.topology.WallTopology;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.union.UnaryUnionOp;

/**
 * Collects a set of {@link Topology} into a form that is useful for pathfinding.
 *
 * @implNote The A* walker relies on the performance provided by {@link PreparedGeometry} in order
 *     to be effective on larger maps with complicated movement blocking. But {@code
 *     PreparedGeometry} sensitive to the types of geometries involved and does especially poorly
 *     with {@link GeometryCollection}. Since we know we have {@link Polygonal} masks and {@link
 *     Lineal} walls, we can prepare these separately so that the {@code PreparedGeometry} can work
 *     with a {@link MultiPolygon} and a {@link MultiLineString}.
 */
public class MovementBlockingTopology {
  private final List<PreparedGeometry> preparedGeometries = new ArrayList<>();

  public MovementBlockingTopology() {}

  public MovementBlockingTopology(WallTopology walls, List<MaskTopology> masks) {
    var factory = GeometryUtil.getGeometryFactory();

    var maskPolygons =
        masks.stream().map(MaskTopology::getPolygon).filter(p -> !p.isEmpty()).toList();
    if (!maskPolygons.isEmpty()) {
      var maskUnion = new UnaryUnionOp(maskPolygons, factory).union();
      // maskUnion should be a Polygon or MultiPolygon which fares very well with prepared geometry.
      this.preparedGeometries.add(PreparedGeometryFactory.prepare(maskUnion));
    }

    var wallGeometries =
        walls
            .getWalls()
            .filter(
                wall ->
                    switch (wall.movementModifier()) {
                      case ForceBoth -> true;
                      case Disabled -> false;
                    })
            .map(wall -> walls.asLineSegment(wall).toGeometry(factory))
            .toList();
    if (!wallGeometries.isEmpty()) {
      var wallUnion = new UnaryUnionOp(wallGeometries, factory).union();
      // wallUnion should be Lineal, which works well with prepared geometry.
      this.preparedGeometries.add(PreparedGeometryFactory.prepare(wallUnion));
    }
  }

  public Envelope getEnvelope() {
    var envelope = new Envelope();
    for (var geometry : preparedGeometries) {
      envelope.expandToInclude(geometry.getGeometry().getEnvelopeInternal());
    }
    return envelope;
  }

  public boolean intersects(Geometry other) {
    for (var prepared : preparedGeometries) {
      if (prepared.intersects(other)) {
        return true;
      }
    }
    return false;
  }
}
