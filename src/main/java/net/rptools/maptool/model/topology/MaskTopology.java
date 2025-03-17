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

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.ui.zone.vbl.Facing;
import net.rptools.maptool.model.Zone.TopologyType;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;

public final class MaskTopology implements Topology {
  private final TopologyType type;
  private final Polygon polygon;

  private MaskTopology(TopologyType type, Polygon polygon) {
    this.type = type;
    this.polygon = polygon;
  }

  public static MaskTopology create(TopologyType type, Polygon polygon) {
    // Build a new polygon with the same rings, but with orientation enforced.
    // Exterior much be counterclockwise, while holes must be clockwise.
    var boundary = polygon.getExteriorRing();
    if (Orientation.isCCW(boundary.getCoordinateSequence())) {
      boundary = boundary.reverse();
    }
    final var holeCount = polygon.getNumInteriorRing();
    var holes = new LinearRing[holeCount];
    for (int i = 0; i < holeCount; ++i) {
      var hole = polygon.getInteriorRingN(i);
      if (!Orientation.isCCW(hole.getCoordinates())) {
        hole = hole.reverse();
      }
      holes[i] = hole;
    }
    polygon = GeometryUtil.getGeometryFactory().createPolygon(boundary, holes);

    return new MaskTopology(type, polygon);
  }

  public static List<MaskTopology> createFromLegacy(TopologyType type, Area area) {
    var masks = new ArrayList<MaskTopology>();
    if (area == null || area.isEmpty()) {
      return masks;
    }

    var polys = GeometryUtil.toJtsPolygons(area);
    for (var poly : polys) {
      masks.add(MaskTopology.create(type, poly));
    }
    return masks;
  }

  public TopologyType getType() {
    return type;
  }

  public Polygon getPolygon() {
    return polygon;
  }

  @Override
  public VisionResult addSegments(
      VisibilityType visibilityType,
      Coordinate origin,
      Envelope bounds,
      Consumer<Coordinate[]> sink) {
    // Convenience for adding rings to the result.
    BiConsumer<LinearRing, Facing> add =
        (ring, facing) ->
            getFacingSegments(ring.getCoordinateSequence(), facing, origin, bounds, sink);

    // This might look weird to have separate switches for inside and out, but it's much
    // cleaner.
    boolean isInside =
        Location.EXTERIOR
            != RayCrossingCounter.locatePointInRing(
                origin, polygon.getExteriorRing().getCoordinateSequence());

    if (!isInside) {
      switch (this.type) {
        case WALL_VBL -> {
          // Just the frontside of the exterior needs to be added.
          add.accept(polygon.getExteriorRing(), Facing.OCEAN_SIDE_FACES_ORIGIN);
        }
        case HILL_VBL -> {
          // Frontside of holes and backside of interior will block. All handled the same.
          add.accept(polygon.getExteriorRing(), Facing.ISLAND_SIDE_FACES_ORIGIN);
          for (var i = 0; i < polygon.getNumInteriorRing(); ++i) {
            add.accept(polygon.getInteriorRingN(i), Facing.ISLAND_SIDE_FACES_ORIGIN);
          }
        }
        case PIT_VBL -> {
          // Pit VBL does nothing when looked at from the outside.
        }
        case COVER_VBL -> {
          // Works just like Wall VBL when viewed from outside.
          add.accept(polygon.getExteriorRing(), Facing.OCEAN_SIDE_FACES_ORIGIN);
        }
        case MBL -> {
          // MBL does not take part in vision.
        }
      }
    } else {
      // Now check if we're in a hole. If not, we're inside the mask proper.
      int holeIndex = -1;
      for (var i = 0; i < polygon.getNumInteriorRing(); ++i) {
        // Holes are open, i.e., do not contain their boundary.
        var location =
            RayCrossingCounter.locatePointInRing(
                origin, polygon.getInteriorRingN(i).getCoordinateSequence());
        if (Location.INTERIOR == location) {
          holeIndex = i;
          break;
        }
      }

      switch (this.type) {
        case WALL_VBL -> {
          if (holeIndex < 0) {
            // Point is not in any hole, so vision is completely blocked.
            return VisionResult.CompletelyObscured;
          } else {
            // Point is in a hole, so vision is blocked by the boundary of that hole.
            add.accept(polygon.getInteriorRingN(holeIndex), Facing.OCEAN_SIDE_FACES_ORIGIN);
          }
        }
        case HILL_VBL -> {
          // If not in a hole, no blocking. If in a hole, blocks by the frontside of all other holes
          // as well as the exterior.
          if (holeIndex >= 0) {
            add.accept(polygon.getExteriorRing(), Facing.ISLAND_SIDE_FACES_ORIGIN);
            for (int i = 0; i < polygon.getNumInteriorRing(); ++i) {
              if (i != holeIndex) {
                add.accept(polygon.getInteriorRingN(i), Facing.ISLAND_SIDE_FACES_ORIGIN);
              }
            }
          }
        }
        case PIT_VBL -> {
          // Not blocking if in a hole. Otherwise, block by the boundary and each hole.
          if (holeIndex < 0) {
            add.accept(polygon.getExteriorRing(), Facing.ISLAND_SIDE_FACES_ORIGIN);
            for (int i = 0; i < polygon.getNumInteriorRing(); ++i) {
              add.accept(polygon.getInteriorRingN(i), Facing.ISLAND_SIDE_FACES_ORIGIN);
            }
          }
        }
        case COVER_VBL -> {
          // If in a hole, block by the boundary of that hole. If not in any hole, only block by the
          // exterior in cases of peninsulas and block by the backside of any hole.
          if (holeIndex < 0) {
            add.accept(polygon.getExteriorRing(), Facing.OCEAN_SIDE_FACES_ORIGIN);
            for (int i = 0; i < polygon.getNumInteriorRing(); ++i) {
              add.accept(polygon.getInteriorRingN(i), Facing.OCEAN_SIDE_FACES_ORIGIN);
            }
          } else {
            add.accept(polygon.getInteriorRingN(holeIndex), Facing.OCEAN_SIDE_FACES_ORIGIN);
          }
        }
        case MBL -> {
          // MBL does not take part in vision.
        }
      }
    }

    return VisionResult.Possible;
  }

  private static void getFacingSegments(
      CoordinateSequence vertices,
      Facing facing,
      Coordinate origin,
      Envelope bounds,
      Consumer<Coordinate[]> sink) {
    if (vertices.size() == 0) {
      return;
    }

    final var requiredOrientation =
        facing == Facing.ISLAND_SIDE_FACES_ORIGIN
            ? Orientation.CLOCKWISE
            : Orientation.COUNTERCLOCKWISE;

    final Coordinate previous = new Coordinate();
    final Coordinate current = new Coordinate();

    List<Coordinate> currentSegmentPoints = new ArrayList<>();
    for (int i = 1; i < vertices.size(); ++i) {
      assert currentSegmentPoints.size() == 0 || currentSegmentPoints.size() >= 2;

      vertices.getCoordinate(i - 1, previous);
      vertices.getCoordinate(i, current);

      final var shouldIncludeFace =
          // Don't need to be especially precise with the vision check.
          bounds.intersects(previous, current)
              && requiredOrientation == Orientation.index(origin, previous, current);

      if (shouldIncludeFace) {
        // Since we're including this face, the existing segment can be extended.
        if (currentSegmentPoints.isEmpty()) {
          // Also need the first point.
          currentSegmentPoints.add(new Coordinate(previous));
        }
        currentSegmentPoints.add(new Coordinate(current));
      } else if (!currentSegmentPoints.isEmpty()) {
        // Since we're skipping this face, the segment is broken and we must start a new one.
        var string = currentSegmentPoints;
        if (requiredOrientation != Orientation.COUNTERCLOCKWISE) {
          string = string.reversed();
        }
        sink.accept(string.toArray(Coordinate[]::new));
        currentSegmentPoints.clear();
      }
    }

    assert currentSegmentPoints.size() == 0 || currentSegmentPoints.size() >= 2;
    if (!currentSegmentPoints.isEmpty()) {
      var string = currentSegmentPoints;
      if (requiredOrientation != Orientation.COUNTERCLOCKWISE) {
        string = string.reversed();
      }
      sink.accept(string.toArray(Coordinate[]::new));
    }
  }
}
