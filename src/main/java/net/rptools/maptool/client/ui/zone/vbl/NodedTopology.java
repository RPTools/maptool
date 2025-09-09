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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.topology.MaskTopology;
import net.rptools.maptool.model.topology.Topology;
import net.rptools.maptool.model.topology.VisibilityType;
import net.rptools.maptool.model.topology.VisionResult;
import net.rptools.maptool.model.topology.Wall;
import net.rptools.maptool.model.topology.WallTopology;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.snapround.SnapRoundingNoder;

/**
 * Utility for noding a set of {@link Topology}.
 *
 * <p>The result will contain a copy of every part of the original topologies, modified to add nodes
 * at any intersection points. This makes it acceptable for use with vision sweeps.
 */
public class NodedTopology {
  private final List<Topology> preparedParts;

  private NodedTopology(List<Topology> preparedParts) {
    this.preparedParts = preparedParts;
  }

  public VisionResult getSegments(
      VisibilityType visibilityType,
      Coordinate origin,
      Envelope bounds,
      Consumer<Coordinate[]> sink) {
    for (var preparedPart : preparedParts) {
      var maskResult = preparedPart.addSegments(visibilityType, origin, bounds, sink);
      if (maskResult == VisionResult.CompletelyObscured) {
        return maskResult;
      }
    }

    return VisionResult.Possible;
  }

  /**
   * Merge a set of topologies into a single noded collection.
   *
   * <p>Every part from each input topology will be copied into the result, but modified with
   * additional nodes at any intersection points. This makes it acceptable for use with vision
   * sweeps.
   *
   * <p>This process assumes the individual topologies are valid. E.g., the masks are made up only
   * of simple rings with the holes being completely within the boundary.
   *
   * @param walls The input walls.
   * @param legacyMasks The legacy masks.
   * @return The merged and noded topology.
   */
  public static NodedTopology prepare(WallTopology walls, List<MaskTopology> legacyMasks) {
    var preparedTopologies = new ArrayList<Topology>();

    CodeTimer.using(
        "NodedTopology#prepare()",
        timer -> {
          var tempMasks = new ArrayList<TempMask>();

          var strings = new ArrayList<NodedSegmentString>();

          timer.start("collect walls");
          var tempWalls = new TempWalls(walls);
          strings.addAll(tempWalls.walls);
          timer.stop("collect walls");

          timer.start("collect masks");
          for (var mask : legacyMasks) {
            var tempMask = new TempMask(mask);
            tempMasks.add(tempMask);
            strings.add(tempMask.boundary);
            strings.addAll(Arrays.asList(tempMask.holes));
          }
          timer.stop("collect masks");

          var noder = new SnapRoundingNoder(GeometryUtil.getPrecisionModel());

          timer.start("compute nodes");
          noder.computeNodes(strings);
          timer.stop("compute nodes");

          // At this point, each string in `strings` has extra nodes added. These aren't part of its
          // points, because that would make too much sense. Instead, we go through each and grab
          // the complete set of nodes to make new strings.

          var factory = GeometryUtil.getGeometryFactory();

          timer.start("prepare walls");
          {
            var preparedWalls = new WallTopology();
            for (var wallString : tempWalls.walls) {
              // String length will be at least 2.
              var originalWall = (Wall) wallString.getData();
              timer.start("get noded coordinates");
              var coordinates = wallString.getNodedCoordinates();
              timer.stop("get noded coordinates");
              if (coordinates.length < 2) {
                // This happens when we encounter a wall with vertices at the same location.
                continue;
              }

              timer.start("build noded string");
              preparedWalls.string(
                  GeometryUtil.coordinateToPoint2D(coordinates[0]),
                  builder -> {
                    for (var i = 1; i < coordinates.length; ++i) {
                      builder.push(
                          GeometryUtil.coordinateToPoint2D(coordinates[i]), originalWall.data());
                    }
                  });
              timer.stop("build noded string");
            }
            preparedTopologies.add(preparedWalls);
          }
          timer.stop("prepare walls");

          timer.start("prepare masks");
          for (var tempMask : tempMasks) {
            var newBoundary = factory.createLinearRing(tempMask.boundary.getNodedCoordinates());
            var newHoles = new LinearRing[tempMask.holes.length];
            for (var i = 0; i < newHoles.length; ++i) {
              newHoles[i] = factory.createLinearRing(tempMask.holes[i].getNodedCoordinates());
            }
            // Make a new GUID. Even though this is conceptually the same topology, it is distinct.
            preparedTopologies.add(
                MaskTopology.create(tempMask.type, factory.createPolygon(newBoundary, newHoles)));
          }
          timer.stop("prepare masks");
        });

    return new NodedTopology(preparedTopologies);
  }

  private static final class TempWalls {
    public final List<NodedSegmentString> walls;

    public TempWalls(WallTopology walls) {
      this.walls = new ArrayList<>();
      walls
          .getWalls()
          .forEach(
              wall -> {
                var segment = walls.asLineSegment(wall);
                var string =
                    new NodedSegmentString(new Coordinate[] {segment.p0, segment.p1}, wall);
                this.walls.add(string);
              });
    }
  }

  private static final class TempMask {
    public final Zone.TopologyType type;
    public final NodedSegmentString boundary;
    public final NodedSegmentString[] holes;

    public TempMask(MaskTopology mask) {
      this.type = mask.getType();
      this.boundary =
          new NodedSegmentString(mask.getPolygon().getExteriorRing().getCoordinates(), null);
      this.holes = new NodedSegmentString[mask.getPolygon().getNumInteriorRing()];
      for (var i = 0; i < holes.length; ++i) {
        this.holes[i] =
            new NodedSegmentString(mask.getPolygon().getInteriorRingN(i).getCoordinates(), null);
      }
    }
  }
}
