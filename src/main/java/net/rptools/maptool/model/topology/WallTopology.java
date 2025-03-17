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
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.server.proto.VertexDto;
import net.rptools.maptool.server.proto.WallTopologyDto;
import net.rptools.maptool.server.proto.drawing.DoublePointDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;

/**
 * Topology based on thin connected walls.
 *
 * <p>Wall topology forms a graph where the walls themselves are the edges of the graph.
 */
public final class WallTopology implements Topology {
  private static final Logger log = LogManager.getLogger(WallTopology.class);

  public static final class GraphException extends Exception {
    public GraphException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Return type for {@link #string(Point2D, Consumer)} that ensures walls are built in a valid
   * fashion.
   */
  public final class StringBuilder {
    // These vertices and walls are not added to the graph until build() is called.
    private final List<Vertex> allVertices = new ArrayList<>();
    private final List<Wall> allWalls = new ArrayList<>();
    private Vertex lastVertex;

    private StringBuilder(Point2D startingPoint) {
      lastVertex = new Vertex();
      lastVertex.position(startingPoint);
      allVertices.add(lastVertex);
    }

    public void push(Point2D nextPoint) {
      push(nextPoint, new Wall.Data());
    }

    public void push(Point2D nextPoint, Wall.Data data) {
      var from = lastVertex;
      var to = new Vertex();
      to.position(nextPoint);
      var wall = new Wall(from.id(), to.id(), data);

      lastVertex = to;
      allVertices.add(lastVertex);
      allWalls.add(wall);
    }

    /** Finish the string and return the last vertex. */
    public void build() {
      if (allWalls.isEmpty()) {
        // Nothing to add to the graph.
        return;
      }

      for (var vertex : allVertices) {
        try {
          addVertexImpl(vertex);
        } catch (GraphException e) {
          throw new RuntimeException(
              "Unexpected scenario: vertex already exists despite vertex being new", e);
        }
      }
      for (var wall : allWalls) {
        try {
          addWallImpl(wall);
        } catch (GraphException e) {
          throw new RuntimeException(
              "Unexpected scenario: wall already exists despite wall being new", e);
        }
      }
    }
  }

  // Vertices are identified by GUID. Attached data is maintained in verticesById
  private final Graph<GUID, Wall> graph;
  private final Map<GUID, Vertex> verticesById = new LinkedHashMap<>();
  private transient int nextZIndex = 0;

  public WallTopology() {
    this.graph =
        GraphTypeBuilder.<GUID, Wall>undirected()
            .allowingMultipleEdges(false)
            .allowingSelfLoops(false)
            .weighted(false)
            .buildGraph();
  }

  public WallTopology(WallTopology other) {
    this();

    other
        .getVertices()
        .sorted(Comparator.comparingInt(Vertex::zIndex))
        .forEach(
            vertex -> {
              Vertex newVertex = new Vertex(vertex.id());
              try {
                addVertexImpl(newVertex);
              } catch (GraphException e) {
                log.error(
                    "Unexpected scenario: graph has duplicate vertex ID {}. Skipping.",
                    vertex.id(),
                    e);
              }
              newVertex.position(vertex.position());
            });

    other
        .getWalls()
        .sorted(Comparator.comparingInt(other::getZIndex))
        .forEach(
            wall -> {
              try {
                addWallImpl(new Wall(wall));
              } catch (GraphException e) {
                log.error(
                    "Unexpected scenario: topology has duplicate wall ({}, {}). Skipping.",
                    wall.from(),
                    wall.to(),
                    e);
              }
            });
  }

  private void addVertexImpl(Vertex vertex) throws GraphException {
    var added = graph.addVertex(vertex.id());
    if (!added) {
      throw new GraphException("Vertex with that GUID is already in the graph", null);
    }

    var previous = verticesById.put(vertex.id(), vertex);
    assert previous == null : "Invariant not held";

    // Because we store the vertices in a LinkedHashMap, they will always be iterated in insertion
    // order. We also include a z-index so that other components can put vertices in other data
    // structures and sort them again by z-order.
    vertex.zIndex(++nextZIndex);
  }

  private void addWallImpl(Wall wall) throws GraphException {
    boolean added;
    try {
      added = graph.addEdge(wall.from(), wall.to(), wall);
    } catch (IllegalArgumentException e) {
      throw new GraphException("One of the vertices does not exist in the graph", e);
    }

    if (!added) {
      throw new GraphException("Wall with those vertices is already in the graph", null);
    }
  }

  // region These exist to support serialization. They are little loose for general use.

  /**
   * Adds a new vertex to the graph.
   *
   * @param id The ID of the vertex
   * @throws GraphException If a vertex with ID {@code id} already exists.
   */
  public Vertex createVertex(GUID id) throws GraphException {
    var vertex = new Vertex(id);
    addVertexImpl(vertex);
    return vertex;
  }

  public void addWall(Wall wall) throws GraphException {
    addWallImpl(wall);
  }

  // endregion

  // region Other builder methods

  private Vertex addNewVertex() {
    Vertex vertex = new Vertex();
    try {
      addVertexImpl(vertex);
    } catch (GraphException e) {
      throw new RuntimeException("Impossible case: vertex already exists with new GUID", e);
    }
    return vertex;
  }

  public void string(Point2D startingPoint, Consumer<StringBuilder> action) {
    var builder = new StringBuilder(startingPoint);
    action.accept(builder);
    builder.build();
  }

  // endregion

  // region Main interface

  public Stream<Vertex> getVertices() {
    return this.graph.vertexSet().stream().map(verticesById::get);
  }

  public Stream<Wall> getWalls() {
    return this.graph.edgeSet().stream();
  }

  public Optional<Wall> getWall(GUID fromId, GUID toId) {
    var wall = this.graph.getEdge(fromId, toId);
    return Optional.ofNullable(wall);
  }

  /**
   * Get the source vertex of {@code wall}.
   *
   * @param wall
   * @return
   */
  public Vertex getFrom(Wall wall) {
    return verticesById.get(wall.from());
  }

  /**
   * Get the target vertex of {@code wall}.
   *
   * @param wall
   * @return
   */
  public Vertex getTo(Wall wall) {
    return verticesById.get(wall.to());
  }

  public int getZIndex(Wall wall) {
    return Math.max(getFrom(wall).zIndex(), getTo(wall).zIndex());
  }

  /**
   * Represent {@code wall} as a {@link LineSegment}.
   *
   * <p>This is a convenience method for getting the coordinates of the wall's vertices. Since
   * {@link LineSegment} cannot carry user data, the result does not include any attached wall data.
   *
   * @param wall
   * @return The {@link LineSegment} representation of {@code wall}.
   */
  public LineSegment asLineSegment(Wall wall) {
    return new LineSegment(
        GeometryUtil.point2DToCoordinate(getFrom(wall).position()),
        GeometryUtil.point2DToCoordinate(getTo(wall).position()));
  }

  public void removeVertex(Vertex vertex) {
    var removed = graph.removeVertex(vertex.id());
    if (!removed) {
      throw new RuntimeException("Foreign vertex");
    }

    verticesById.remove(vertex.id());
    removeDanglingVertices();
  }

  public void removeWall(Wall wall) {
    var removedWall = graph.removeEdge(wall.from(), wall.to());
    if (removedWall != null) {
      removeDanglingVertices();
    }
  }

  public Wall brandNewWall() {
    var from = addNewVertex();
    var to = addNewVertex();

    var newWall = new Wall(from.id(), to.id());
    try {
      addWallImpl(newWall);
    } catch (GraphException e) {
      throw new RuntimeException(
          "Unexpected scenario: wall already exists despite both vertices being new", e);
    }

    return newWall;
  }

  public Wall newWallStartingAt(Vertex from) {
    var isForeign = !this.graph.containsVertex(from.id());
    if (isForeign) {
      throw new RuntimeException("Vertex is not part of this topology");
    }

    var newVertex = addNewVertex();

    Wall newWall = new Wall(from.id(), newVertex.id());
    try {
      addWallImpl(newWall);
    } catch (GraphException e) {
      throw new RuntimeException(
          "Unexpected scenario: wall already exists despite the one vertex being new", e);
    }

    return newWall;
  }

  public Vertex splitWall(Wall wall) {
    // Remove wall and replace with two new walls connected through a new vertex.

    var removed = graph.removeEdge(wall.from(), wall.to());
    if (removed == null) {
      throw new RuntimeException("Wall does not exist");
    }
    // Don't trust the contents of `wall`, instead use `removed`.

    var newVertex = addNewVertex();
    var newWall1 = new Wall(removed.from(), newVertex.id());
    var newWall2 = new Wall(newVertex.id(), removed.to());
    newWall1.copyDataFrom(wall);
    newWall2.copyDataFrom(wall);

    try {
      addWallImpl(newWall1);
      addWallImpl(newWall2);
    } catch (GraphException e) {
      throw new RuntimeException("Unexpected scenario: wall already exists for new vertex", e);
    }

    return newVertex;
  }

  /**
   * Merge {@code first} with {@code second}.
   *
   * <p>How this merger is done is up to the implementation. Either {@code first} or {@code second}
   * may remaining in the graph while the other is removed, or both may be removed and replaced with
   * a new vertex. Callers should use the return value if the new or surviving vertex is required.
   *
   * @param first The vertex to remove by merging.
   * @param second The vertex to augment by merging {@code remove} into it.
   * @return The merged vertex.
   */
  public Vertex merge(Vertex first, Vertex second) {
    // Current implementation is to remove `first` and keep `second.

    // A copy is essential since graph.edgesOf() returns a live set.
    var incidentWalls = new ArrayList<>(graph.edgesOf(first.id()));

    var secondIsForeign = !graph.containsVertex(second.id());
    if (secondIsForeign) {
      throw new RuntimeException("Unable unable merge vertex that does not exist!");
    }

    var wasRemoved = graph.removeVertex(first.id());
    if (!wasRemoved) {
      throw new RuntimeException("Unable unable merge vertex that does not exist!");
    }

    // Anything that used to point to or from `first` now has to point to or from `second`.
    for (var oldWall : incidentWalls) {
      var firstIsSource = oldWall.from().equals(first.id());
      var neighbour = firstIsSource ? oldWall.to() : oldWall.from();

      // Three cases:
      // 1. `neighbour` is `second`. Drop such contracted walls.
      // 2. A wall already exists from `neighbour` to `second`. Merge into the existing wall.
      // 3. No wall exists from `neighbour` to `second`. Copy the wall over.

      if (neighbour.equals(second.id())) {
        // The wall was contracted. Drop it as redundant.
        continue;
      }

      var existingWall = graph.getEdge(second.id(), neighbour); // Order doesn't matter.
      if (existingWall == null) {
        // Simply duplicate the wall for `second` instead of `first`.
        var newWall =
            new Wall(
                firstIsSource ? second.id() : neighbour, firstIsSource ? neighbour : second.id());
        newWall.copyDataFrom(oldWall);
        try {
          addWallImpl(newWall);
        } catch (GraphException e) {
          throw new RuntimeException(
              "Unexpected scenario: wall already exists despite not existing", e);
        }
      } else {
        // Merge obsolete wall into the kept wall.
        var secondIsSource = existingWall.from().equals(second.id());

        // If the walls point in opposite directions, flip the one being merged so that they agree
        // on things like direction.
        var wallToMerge = (firstIsSource == secondIsSource) ? oldWall : oldWall.reversed();
        existingWall.mergeDataFrom(wallToMerge);
      }
    }

    removeDanglingVertices();

    return second;
  }

  public void bringToFront(Vertex vertex) {
    vertex.zIndex(++nextZIndex);
  }

  // endregion

  /**
   * Ensures that any vertices with no walls are removed from the graph.
   *
   * <p>This is usually called automatically when needed, so don't call this method without reason.
   * Deserializing is an exception where improper XML could result in vertices being added without
   * associated walls.
   */
  public void removeDanglingVertices() {
    var verticesToRemove = new ArrayList<GUID>();
    for (var vertex : graph.vertexSet()) {
      if (graph.edgesOf(vertex).isEmpty()) {
        verticesToRemove.add(vertex);
      }
    }
    graph.removeAllVertices(verticesToRemove);

    for (var vertexId : verticesToRemove) {
      verticesById.remove(vertexId);
    }
  }

  @Override
  public VisionResult addSegments(
      VisibilityType visibilityType,
      Coordinate origin,
      Envelope bounds,
      Consumer<Coordinate[]> sink) {
    getWalls()
        .forEach(
            wall -> {
              var segment = asLineSegment(wall);
              // Very rough bounds check so we don't include everything.
              if (!bounds.intersects(segment.p0, segment.p1)) {
                return;
              }

              // For directional walls, ensure the origin is on the correct side.
              var direction =
                  switch (wall.directionModifier(visibilityType)) {
                    case SameDirection -> wall.direction();
                    case ReverseDirection -> wall.direction().reversed();
                    case ForceBoth -> Wall.Direction.Both;
                    case Disabled -> null;
                  };
              if (direction == null) {
                // Segment is not active for this type.
                return;
              }

              boolean correctOrientation =
                  switch (direction) {
                    case Both -> true;
                    case Left -> Orientation.RIGHT == segment.orientationIndex(origin);
                    case Right -> Orientation.LEFT == segment.orientationIndex(origin);
                  };
              if (!correctOrientation) {
                return;
              }

              sink.accept(new Coordinate[] {segment.p0, segment.p1});
            });
    return VisionResult.Possible;
  }

  public WallTopologyDto toDto() {
    var builder = WallTopologyDto.newBuilder();
    for (var vertex : this.verticesById.values()) {
      builder.addVertices(
          VertexDto.newBuilder()
              .setId(vertex.id().toString())
              .setPosition(
                  DoublePointDto.newBuilder()
                      .setX(vertex.position().getX())
                      .setY(vertex.position().getY())));
    }
    for (var wall : this.graph.edgeSet()) {
      builder.addWalls(wall.toDto());
    }
    return builder.build();
  }

  public static WallTopology fromDto(WallTopologyDto dto) {
    var topology = new WallTopology();
    for (var vertexDto : dto.getVerticesList()) {
      Vertex vertex = new Vertex(new GUID(vertexDto.getId()));
      try {
        topology.addVertexImpl(vertex);
      } catch (GraphException e) {
        log.error("Unexpected error while adding vertex to graph", e);
        continue;
      }

      vertex.position(vertexDto.getPosition().getX(), vertexDto.getPosition().getY());
    }
    for (var wallDto : dto.getWallsList()) {
      try {
        var wall = Wall.fromDto(wallDto);
        topology.addWallImpl(wall);
      } catch (GraphException e) {
        log.error("Unexpected error while adding wall to topology", e);
      }
    }
    return topology;
  }
}
