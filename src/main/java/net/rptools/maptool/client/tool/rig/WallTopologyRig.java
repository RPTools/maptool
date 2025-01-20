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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.model.topology.Vertex;
import net.rptools.maptool.model.topology.Wall;
import net.rptools.maptool.model.topology.WallTopology;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;

public final class WallTopologyRig implements Rig<WallTopologyRig.Element<?>> {
  public sealed interface Element<T> extends Movable<T>
      permits WallTopologyRig.MovableVertex, WallTopologyRig.MovableWall {}

  private WallTopology walls;
  private final Supplier<Double> vertexSelectDistanceSupplier;
  private final Supplier<Double> wallSelectDistanceSupplier;

  public WallTopologyRig(
      Supplier<Double> vertexSelectDistanceSupplier, Supplier<Double> wallSelectDistanceSupplier) {
    this.walls = new WallTopology();
    this.vertexSelectDistanceSupplier = vertexSelectDistanceSupplier;
    this.wallSelectDistanceSupplier = wallSelectDistanceSupplier;
    updateShape();
  }

  public void setWalls(WallTopology walls) {
    this.walls = new WallTopology(walls);
    updateShape();
  }

  public void bringToFront(Vertex vertex) {
    this.walls.bringToFront(vertex);
  }

  public void bringToFront(Wall wall) {
    this.walls.bringToFront(this.walls.getFrom(wall));
    this.walls.bringToFront(this.walls.getTo(wall));
  }

  @Override
  public List<WallTopologyRig.MovableVertex> getHandlesWithin(Rectangle2D bounds) {
    var envelope =
        new Envelope(
            bounds.getMinX(), bounds.getMaxX(),
            bounds.getMinY(), bounds.getMaxY());

    var results = new ArrayList<WallTopologyRig.MovableVertex>();
    walls
        .getVertices()
        .forEach(
            vertex -> {
              if (envelope.contains(GeometryUtil.point2DToCoordinate(vertex.position()))) {
                results.add(new WallTopologyRig.MovableVertex(this, walls, vertex));
              }
            });
    results.sort(Comparator.comparingInt(vertex -> vertex.getSource().zIndex()));
    return results;
  }

  public List<WallTopologyRig.MovableWall> getWallsWithin(Rectangle2D bounds) {
    var envelope =
        new Envelope(
            bounds.getMinX(), bounds.getMaxX(),
            bounds.getMinY(), bounds.getMaxY());

    var results = new ArrayList<WallTopologyRig.MovableWall>();
    walls
        .getWalls()
        .forEach(
            wall -> {
              var movableWall = new WallTopologyRig.MovableWall(this, walls, wall);
              var wallEnvelope =
                  new Envelope(
                      GeometryUtil.point2DToCoordinate(movableWall.getFrom().getPosition()),
                      GeometryUtil.point2DToCoordinate(movableWall.getTo().getPosition()));
              if (wallEnvelope.intersects(envelope)) {
                results.add(movableWall);
              }
            });
    results.sort(Comparator.comparingInt(wall -> walls.getZIndex(wall.getSource())));
    return results;
  }

  /**
   * Convenience method for calling {@link #getNearbyHandle(Point2D, double, Predicate)} followed by
   * {@link #getNearbyWall(Point2D, double, Predicate)}.
   *
   * @param point
   * @param filter
   * @return
   */
  public Optional<? extends Element<?>> getNearbyElement(
      Point2D point, double extraSpace, Predicate<Element<?>> filter) {
    var vertex = getNearbyHandle(point, extraSpace, filter);
    if (vertex.isPresent()) {
      return vertex;
    }
    return getNearbyWall(point, extraSpace, filter);
  }

  @Override
  public Optional<WallTopologyRig.MovableVertex> getNearbyHandle(
      Point2D point, double extraSpace, Predicate<Element<?>> filter) {
    var selectDistance = vertexSelectDistanceSupplier.get() + extraSpace;
    var candidatesVertices =
        getHandlesWithin(
            new Rectangle2D.Double(
                point.getX() - selectDistance,
                point.getY() - selectDistance,
                2 * selectDistance,
                2 * selectDistance));

    // Reverse so we pick the highest z-index candidate.
    for (var candidate : candidatesVertices.reversed()) {
      if (filter.test(candidate) && point.distance(candidate.getPosition()) < selectDistance) {
        return Optional.of(candidate);
      }
    }

    return Optional.empty();
  }

  public Optional<WallTopologyRig.MovableWall> getNearbyWall(
      Point2D point, double extraSpace, Predicate<Element<?>> filter) {
    var coordinate = GeometryUtil.point2DToCoordinate(point);

    var selectDistance = wallSelectDistanceSupplier.get() + extraSpace;
    var bounds =
        new Rectangle2D.Double(
            point.getX() - selectDistance,
            point.getY() - selectDistance,
            2 * selectDistance,
            2 * selectDistance);
    var candidateWalls = getWallsWithin(bounds);

    // Reverse so we pick the highest-z-index candidate.
    for (var wall : candidateWalls.reversed()) {
      if (!filter.test(wall)) {
        continue;
      }

      var segment = walls.asLineSegment(wall.getSource());
      var factor = segment.projectionFactor(coordinate);
      if (factor < 0 || factor > 1) {
        // Lies outside this segment.
        continue;
      }

      var pointAlong = segment.pointAlong(factor);
      var distance = coordinate.distance(pointAlong);
      if (distance <= selectDistance) {
        return Optional.of(wall);
      }
    }

    return Optional.empty();
  }

  public WallTopologyRig.MovableWall addDegenerateWall(Point2D point) {
    var newWall = new MovableWall(this, walls, walls.brandNewWall());

    newWall.getFrom().moveTo(point);
    newWall.getTo().moveTo(point);

    updateShape();

    return newWall;
  }

  /**
   * Creates a new wall connected to an existing vertex.
   *
   * @param connectTo The vertex to connect the new wall to.
   * @param point The position of the new vertex to create.
   * @return The newly created wall. The starting point of the wall will be {@code connectTo} and
   *     the ending point will be a new vertex with its position set to {@code point}.
   */
  public WallTopologyRig.MovableWall addConnectedWall(Vertex connectTo, Point2D point) {
    var newWall = new MovableWall(this, walls, walls.newWallStartingAt(connectTo));
    newWall.getTo().moveTo(point);
    updateShape();
    return newWall;
  }

  /**
   * Called whenever the source is changed in any way.
   *
   * <p>Although empty right now, if we ever add acceleration structures this would be the place to
   * update them.
   */
  private void updateShape() {}

  public WallTopology commit() {
    // Submit a copy so further edits are not accidentally directly reflected in the model.
    return new WallTopology(walls);
  }

  /**
   * Merge {@code one} with {@code two}.
   *
   * <p>The result will be a vertex that has all the walls of the originals, with data merged as
   * needed. Whether a new vertex is created or one of the originals is updated is implementation
   * defined, so make sure to use the return value as the resulting vertex.
   *
   * @param one The first of the vertices to merge.
   * @param two The second of the vertices to merge.
   * @return The surviving handle.
   */
  public WallTopologyRig.MovableVertex mergeVertices(Handle<Vertex> one, Handle<Vertex> two) {
    var survivor = walls.merge(one.getSource(), two.getSource());
    updateShape();
    return new WallTopologyRig.MovableVertex(this, walls, survivor);
  }

  /**
   * Get the point the wall would be split at if {@code splitAt(wall, reference)} were called.
   *
   * @param wall The wall to look for a split point on.
   * @param reference A point close to the wall.
   * @return The point on {@code wall} nearest to {@code reference}.
   */
  public Point2D getSplitPoint(Movable<Wall> wall, Point2D reference) {
    var coordinate = GeometryUtil.point2DToCoordinate(reference);
    var segment = walls.asLineSegment(wall.getSource());
    var fraction = segment.segmentFraction(coordinate);
    var projection = segment.pointAlong(fraction);
    return GeometryUtil.coordinateToPoint2D(projection);
  }

  /**
   * Splits {@code wall} at the point nearest to {@code reference}.
   *
   * @param wall The wall to split.
   * @param reference A point close to the wall.
   * @return The new handle created as part of the split.
   */
  public MovableVertex splitAt(Movable<Wall> wall, Point2D reference) {
    var projection = getSplitPoint(wall, reference);
    var newVertex = walls.splitWall(wall.getSource());
    newVertex.position(projection);
    return new WallTopologyRig.MovableVertex(this, walls, newVertex);
  }

  public static final class MovableWall implements WallTopologyRig.Element<Wall>, Movable<Wall> {
    private final WallTopologyRig parentRig;
    private final WallTopology walls;
    private final Wall source;
    private final LineSegment originalSegment;

    private MovableWall(WallTopologyRig parentRig, WallTopology walls, Wall source) {
      this.parentRig = parentRig;
      this.walls = walls;
      this.source = source;
      this.originalSegment = walls.asLineSegment(source);
    }

    @Override
    public WallTopologyRig getParentRig() {
      return parentRig;
    }

    @Override
    public Wall getSource() {
      return source;
    }

    public LineSegment asLineSegment() {
      return this.walls.asLineSegment(source);
    }

    public MovableVertex getFrom() {
      return new MovableVertex(parentRig, walls, walls.getFrom(source));
    }

    public MovableVertex getTo() {
      return new MovableVertex(parentRig, walls, walls.getTo(source));
    }

    @Override
    public void delete() {
      walls.removeWall(source);
      parentRig.updateShape();
    }

    @Override
    public boolean isForSameElement(Movable<?> other) {
      return other instanceof WallTopologyRig.MovableWall movableWall
          && this.getParentRig() == movableWall.getParentRig()
          && this.source.from().equals(movableWall.source.from())
          && this.source.to().equals(movableWall.source.to());
    }

    @Override
    public void displace(double displacementX, double displacementY, Snap snapMode) {
      var newFrom =
          new Point2D.Double(
              originalSegment.p0.x + displacementX, originalSegment.p0.y + displacementY);
      var newTo =
          new Point2D.Double(
              originalSegment.p1.x + displacementX, originalSegment.p1.y + displacementY);

      var snappedFrom = snapMode.snap(newFrom);
      var fromOffsetX = snappedFrom.getX() - newFrom.getX();
      var fromOffsetY = snappedFrom.getY() - newFrom.getY();

      var snappedTo = snapMode.snap(newTo);
      var toOffsetX = snappedTo.getX() - newTo.getX();
      var toOffsetY = snappedTo.getY() - newTo.getY();

      if ((fromOffsetX * fromOffsetX + fromOffsetY * fromOffsetY)
          < (toOffsetX * toOffsetX + toOffsetY * toOffsetY)) {
        getFrom().moveTo(new Point2D.Double(snappedFrom.getX(), snappedFrom.getY()));
        getTo()
            .moveTo(
                new Point2D.Double(
                    snappedFrom.getX() + (originalSegment.p1.x - originalSegment.p0.x),
                    snappedFrom.getY() + (originalSegment.p1.y - originalSegment.p0.y)));
      } else {
        getTo().moveTo(new Point2D.Double(snappedTo.getX(), snappedTo.getY()));
        getFrom()
            .moveTo(
                new Point2D.Double(
                    snappedTo.getX() + (originalSegment.p0.x - originalSegment.p1.x),
                    snappedTo.getY() + (originalSegment.p0.y - originalSegment.p1.y)));
      }

      parentRig.updateShape();
    }

    @Override
    public void applyMove() {
      originalSegment.setCoordinates(walls.asLineSegment(source));
    }
  }

  public static final class MovableVertex
      implements WallTopologyRig.Element<Vertex>, Handle<Vertex> {
    private final WallTopologyRig parentRig;
    private final WallTopology walls;
    private final Vertex source;
    private final Point2D originalPosition;

    private MovableVertex(WallTopologyRig parentRig, WallTopology walls, Vertex source) {
      this.parentRig = parentRig;
      this.walls = walls;
      this.source = source;
      this.originalPosition = this.source.position();
    }

    @Override
    public Vertex getSource() {
      return source;
    }

    @Override
    public WallTopologyRig getParentRig() {
      return parentRig;
    }

    @Override
    public Point2D getPosition() {
      return source.position();
    }

    @Override
    public void delete() {
      walls.removeVertex(source);
      parentRig.updateShape();
    }

    @Override
    public boolean isForSameElement(Movable<?> other) {
      return other instanceof WallTopologyRig.MovableVertex movableVertex
          && this.getParentRig() == movableVertex.getParentRig()
          && this.source.id().equals(movableVertex.source.id());
    }

    @Override
    public void moveTo(Point2D point) {
      source.position(point.getX(), point.getY());
      parentRig.updateShape();
    }

    @Override
    public void displace(double displacementX, double displacementY, Snap snapMode) {
      Point2D point =
          new Point2D.Double(
              originalPosition.getX() + displacementX, originalPosition.getY() + displacementY);
      point = snapMode.snap(point);

      moveTo(point);
    }

    @Override
    public void applyMove() {
      originalPosition.setLocation(source.position());
    }
  }
}
