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
package net.rptools.maptool.client.walker.astar;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.vbl.MovementBlockingTopology;
import net.rptools.maptool.client.walker.AbstractZoneWalker;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.zones.MaskTopologyChanged;
import net.rptools.maptool.model.zones.WallTopologyChanged;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

public abstract class AbstractAStarWalker extends AbstractZoneWalker {
  private record TerrainModifier(Token.TerrainModifierOperation operation, double value) {}

  private static boolean isInteger(double d) {
    return (int) d == d;
  }

  private static final Logger log = LogManager.getLogger(AbstractAStarWalker.class);

  private final GeometryFactory geometryFactory = new GeometryFactory();
  protected int crossX = 0;
  protected int crossY = 0;
  private Area fowExposedArea = new Area();
  private double cell_cost = zone.getUnitsPerCell();
  private double distance = -1;
  private final AtomicBoolean invalidatedTopology = new AtomicBoolean(true);
  private @Nonnull MovementBlockingTopology preparedTopology = new MovementBlockingTopology();
  private PreparedGeometry fowExposedAreaGeometry = null;
  private TokenFootprint footprint = new TokenFootprint();
  private Map<CellPoint, Map<CellPoint, Boolean>> vblBlockedMovesByGoal = new ConcurrentHashMap<>();
  private Map<CellPoint, Map<CellPoint, Boolean>> fowBlockedMovesByGoal = new ConcurrentHashMap<>();
  private final Map<CellPoint, List<TerrainModifier>> terrainCells = new HashMap<>();

  /**
   * The IDs of all debugging labels, so we can remove them again later. Only access this on the
   * Swing thread _or else_.
   */
  private static final List<GUID> debugLabels = new ArrayList<>();

  public AbstractAStarWalker(Zone zone) {
    super(zone);

    // Get tokens on map that may affect movement
    for (Token token : zone.getTokensWithTerrainModifiers()) {
      Set<CellPoint> cells = token.getOccupiedCells(zone.getGrid());
      for (CellPoint cell : cells) {
        terrainCells
            .computeIfAbsent(cell, ignored -> new ArrayList<>())
            .add(
                new TerrainModifier(
                    token.getTerrainModifierOperation(), token.getTerrainModifier()));
      }
    }

    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Override
  public void close() {
    new MapToolEventBus().getMainEventBus().unregister(this);
  }

  private void onTopologyChanged() {
    // This event is not called on the walker thread, so needs synchronization.
    invalidatedTopology.set(true);
  }

  @Subscribe
  private void onTopologyChanged(WallTopologyChanged event) {
    if (event.zone() != zone) {
      return;
    }
    onTopologyChanged();
  }

  @Subscribe
  private void onTopologyChanged(MaskTopologyChanged event) {
    if (event.zone() != zone) {
      return;
    }
    onTopologyChanged();
  }

  /**
   * Returns the list of neighbor cells that are valid for being movement-checked. This is an array
   * of (x,y) offsets (see the constants in this class) named as compass points.
   *
   * <p>It should be possible to query the current (x,y) CellPoint passed in to determine which
   * directions are feasible to move into. But it would require information about visibility (which
   * token is moving, does it have sight, and so on). Currently that information is not available
   * here, but perhaps an option Token parameter could be specified to the constructor? Or maybe as
   * the tree was scanned, since I believe all Grids share a common ZoneWalker.
   *
   * @param x the x of the CellPoint
   * @param y the y of the CellPoint
   * @return the array of (x,y) for the neighbor cells
   */
  protected abstract int[][] getNeighborMap(int x, int y);

  protected abstract double hScore(AStarCellPoint p1, CellPoint p2);

  protected abstract double getDiagonalMultiplier(int[] neighborArray);

  public double getDistance() {
    if (distance < 0) {
      return 0;
    } else {
      return distance;
    }
  }

  public Map<CellPoint, Set<CellPoint>> getBlockedMoves() {
    final Map<CellPoint, Set<CellPoint>> result = new HashMap<>();
    for (var entry : vblBlockedMovesByGoal.entrySet()) {
      result.put(
          entry.getKey(),
          entry.getValue().entrySet().stream()
              .filter(Map.Entry::getValue)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet()));
    }
    for (var entry : fowBlockedMovesByGoal.entrySet()) {
      result.put(
          entry.getKey(),
          entry.getValue().entrySet().stream()
              .filter(Map.Entry::getValue)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet()));
    }
    return result;
  }

  @Override
  public void setFootprint(TokenFootprint footprint) {
    this.footprint = footprint;
  }

  @Override
  protected List<CellPoint> calculatePath(CellPoint start, CellPoint goal) {
    crossX = start.x - goal.x;
    crossY = start.y - goal.y;

    Queue<AStarCellPoint> openList =
        new PriorityQueue<>(Comparator.comparingDouble(AStarCellPoint::fCost));
    Map<AStarCellPoint, AStarCellPoint> openSet = new HashMap<>(); // For faster lookups
    Set<AStarCellPoint> closedSet = new HashSet<>();

    // Current fail safe... bail out after 10 seconds of searching just in case, shouldn't hang UI
    // as this is off the AWT thread
    long timeOut = System.currentTimeMillis();
    double estimatedTimeoutNeeded = 10000;

    var startNode = new AStarCellPoint(start, !isInteger(start.distanceTraveledWithoutTerrain));
    openList.add(startNode);
    openSet.put(startNode, startNode);

    AStarCellPoint currentNode = null;

    // Get current VBL for map...
    // Using JTS because AWT Area can only intersect with Area and we want to use simple lines here.
    // Render VBL to Geometry class once and store.
    // Note: zoneRenderer will be null if map is not visible to players.
    Area newFowExposedArea = new Area();
    final var zoneRenderer = MapTool.getFrame().getZoneRenderer(zone);
    if (zoneRenderer != null) {
      final var zoneView = zoneRenderer.getZoneView();

      if (invalidatedTopology.compareAndSet(true, false)) {
        // The move cache may no longer accurately reflect the VBL limitations.
        this.vblBlockedMovesByGoal.clear();

        var topologyTypes =
            MapTool.getServerPolicy().getVblBlocksMove()
                ? EnumSet.allOf(Zone.TopologyType.class)
                : EnumSet.of(Zone.TopologyType.MBL);
        this.preparedTopology =
            new MovementBlockingTopology(
                zone.getWalls(),
                zone.getMasks(topologyTypes, keyToken == null ? null : keyToken.getId()));
      }

      var view = zoneRenderer.getPlayerView();
      newFowExposedArea =
          zone.hasFog() && !view.isGMView() ? zoneView.getExposedArea(view) : new Area();
    }

    if (!Objects.equals(newFowExposedArea, fowExposedArea)) {
      // The move cache may no longer accurately reflect the FOW limitations.
      this.fowBlockedMovesByGoal.clear();

      fowExposedArea = newFowExposedArea;
      // FoW has changed. Let's update the JTS geometry to match.
      if (fowExposedArea.isEmpty()) {
        this.fowExposedAreaGeometry = null;
      } else {
        try {
          this.fowExposedAreaGeometry =
              PreparedGeometryFactory.prepare(GeometryUtil.toJts(fowExposedArea));
        } catch (Exception e) {
          log.info("FoW Geometry oh oh: ", e);
        }
      }
    }

    // Erase previous debug labels.
    EventQueue.invokeLater(
        () -> {
          for (GUID labelId : debugLabels) {
            zone.removeLabel(labelId);
          }
          debugLabels.clear();
        });

    // Timeout quicker for GM cause reasons
    if (MapTool.getPlayer().isGM()) {
      estimatedTimeoutNeeded = estimatedTimeoutNeeded / 2;
    }

    Rectangle2D pathfindingBounds = this.getPathfindingBounds(start, goal);

    log.debug("Starting pathfinding");
    log.debug("Pathfinding bounds are {}", pathfindingBounds);
    while (!openList.isEmpty()) {
      log.debug("Open list has {} elements", openList.size());

      if (System.currentTimeMillis() > timeOut + estimatedTimeoutNeeded) {
        log.info("Timing out after " + estimatedTimeoutNeeded);
        break;
      }

      currentNode = openList.remove();
      log.debug("Current node is {}", currentNode.position);
      openSet.remove(currentNode);
      if (currentNode.position.equals(goal)) {
        log.debug("Achieved our goal at {}", goal);
        break;
      }

      for (AStarCellPoint currentNeighbor :
          getNeighbors(currentNode, closedSet, pathfindingBounds)) {
        currentNeighbor.h = hScore(currentNeighbor, goal);
        showDebugInfo(currentNeighbor);

        if (openSet.containsKey(currentNeighbor)) {
          // check if it is cheaper to get here the way that we just came, versus the previous path
          AStarCellPoint oldNode = openSet.get(currentNeighbor);
          if (currentNeighbor.g < oldNode.g) {
            // We're about to modify the node cost, so we have to reinsert the node.
            openList.remove(oldNode);

            oldNode.replaceG(currentNeighbor);
            oldNode.parent = currentNode;

            openList.add(oldNode);
          }
          continue;
        }

        openList.add(currentNeighbor);
        openSet.put(currentNeighbor, currentNeighbor);
        log.debug("Added neighbor to open set: {}", currentNeighbor.position);
      }

      closedSet.add(currentNode);
      currentNode = null;

      /*
        We now calculate paths off the main UI thread but only one at a time.
        If the token moves, we cancel the thread and restart so we're only calculating the most
        recent path request. Clearing the list effectively finishes this thread gracefully.
      */
      if (Thread.interrupted()) {
        log.debug("Pathfinding cancelled");
        openList.clear();
      }
    }

    if (currentNode == null) {
      log.debug("Failed pathfinding");
    } else {
      log.debug("Completed pathfinding at {}", goal);
    }

    List<CellPoint> returnedCellPointList = new LinkedList<>();
    while (currentNode != null) {
      returnedCellPointList.add(currentNode.position);
      currentNode = currentNode.parent;
    }

    // We don't need to "calculate" distance after the fact as it's already stored as the G cost...
    if (!returnedCellPointList.isEmpty()) {
      distance = returnedCellPointList.get(0).getDistanceTraveled(zone);
    } else { // if path finding was interrupted because of timeout
      distance = 0;
      goal.setAStarCanceled(true);

      returnedCellPointList.add(goal);
      returnedCellPointList.add(start);
    }

    Collections.reverse(returnedCellPointList);
    timeOut = (System.currentTimeMillis() - timeOut);
    if (timeOut > 500) {
      log.debug("Time to calculate A* path warning: " + timeOut + "ms");
    }

    return returnedCellPointList;
  }

  /**
   * Find a suitable bounding box in which A* can look for paths.
   *
   * <p>The bounding box will surround all of the following:
   *
   * <ul>
   *   <li>All MBL/VBL
   *   <li>All terrain modifiers
   *   <li>The start and goal cells
   * </ul>
   *
   * Additionally, some padding is provided around all this so that a token can navigate around the
   * outside if necessary.
   *
   * @param start
   * @param goal
   * @return A bounding box suitable for constraining the A* search space.
   */
  protected Rectangle2D getPathfindingBounds(CellPoint start, CellPoint goal) {
    // Bounding box must contain all VBL/MBL ...
    var vblEnvelope = preparedTopology.getEnvelope();

    Rectangle2D pathfindingBounds =
        new Rectangle2D.Double(
            vblEnvelope.getMinX(),
            vblEnvelope.getMinY(),
            vblEnvelope.getWidth(),
            vblEnvelope.getHeight());

    pathfindingBounds = pathfindingBounds.createUnion(fowExposedArea.getBounds());
    // ... and the footprints of all terrain tokens ...
    for (var cellPoint : terrainCells.keySet()) {
      pathfindingBounds = pathfindingBounds.createUnion(zone.getGrid().getBounds(cellPoint));
    }
    // ... and the original token position ...
    pathfindingBounds = pathfindingBounds.createUnion(zone.getGrid().getBounds(start));
    // ... and the target token position ...
    pathfindingBounds = pathfindingBounds.createUnion(zone.getGrid().getBounds(goal));
    // ... and have ample room for the token to go anywhere around the outside if necessary.
    var tokenBounds = footprint.getBounds(zone.getGrid());
    // Expand by twice the token size to ensure plenty of room.
    pathfindingBounds.setRect(
        pathfindingBounds.getMinX() - 2 * tokenBounds.width,
        pathfindingBounds.getMinY() - 2 * tokenBounds.height,
        pathfindingBounds.getWidth() + 4 * tokenBounds.width,
        pathfindingBounds.getHeight() + 4 * tokenBounds.height);

    return pathfindingBounds;
  }

  protected List<AStarCellPoint> getNeighbors(
      AStarCellPoint node, Set<AStarCellPoint> closedSet, Rectangle2D pathfindingBounds) {
    List<AStarCellPoint> neighbors = new ArrayList<>();
    int[][] neighborMap = getNeighborMap(node.position.x, node.position.y);

    // Find all the neighbors.
    for (int[] neighborArray : neighborMap) {
      double terrainMultiplier = 0;
      double terrainAdder = 0;
      boolean terrainIsFree = false;
      boolean blockNode = false;

      // Get diagonal cost multiplier, if any...
      double diagonalMultiplier = getDiagonalMultiplier(neighborArray);
      boolean invertEvenOddDiagonals = !isInteger(diagonalMultiplier);

      AStarCellPoint neighbor =
          new AStarCellPoint(
              node.position.x + neighborArray[0],
              node.position.y + neighborArray[1],
              node.isOddStepOfOneTwoOneMovement ^ invertEvenOddDiagonals);
      log.debug("Checking neighbor: {}", neighbor.position);
      if (closedSet.contains(neighbor)) {
        log.debug("Rejected neighbor for being in the closed set: {}", neighbor.position);
        continue;
      }

      if (!zone.getGrid().getBounds(node.position).intersects(pathfindingBounds)) {
        log.debug("Rejected neighbor for being out of bounds: {}", neighbor.position);
        // This position is too far out to possibly be part of the optimal path.
        closedSet.add(neighbor);
        continue;
      }

      // Add the cell we're coming from
      neighbor.parent = node;

      // Don't count VBL or Terrain Modifiers
      if (restrictMovement) {
        if (tokenFootprintIntersectsVBL(neighbor.position)) {
          // The token would overlap VBL if moved to this position, so it is not a valid position.
          closedSet.add(neighbor);
          log.debug("Rejected neighbor for being inside MBL: {}", neighbor.position);
          continue;
        }

        Set<CellPoint> occupiedCells = footprint.getOccupiedCells(node.position);
        for (CellPoint cellPoint : occupiedCells) {
          // Check whether moving the occupied cell to its new location would be prohibited by VBL.
          var cellNeighbor =
              new CellPoint(cellPoint.x + neighborArray[0], cellPoint.y + neighborArray[1]);
          if (vblBlocksMovement(cellPoint, cellNeighbor)) {
            blockNode = true;
            log.debug("MBL blocked movement to neighbor: {}", neighbor.position);
            break;
          }
          if (fowBlocksMovement(cellPoint, cellNeighbor)) {
            log.debug("FOW blocked movement to neighbor to {}", neighbor.position);
            blockNode = true;
            break;
          }
        }

        if (blockNode) {
          continue;
        }

        // Check for terrain modifiers
        for (TerrainModifier terrainModifier :
            terrainCells.getOrDefault(neighbor.position, Collections.emptyList())) {
          if (!terrainModifiersIgnored.contains(terrainModifier.operation)) {
            switch (terrainModifier.operation) {
              case MULTIPLY:
                terrainMultiplier += terrainModifier.value;
                break;
              case ADD:
                terrainAdder += terrainModifier.value;
                break;
              case BLOCK:
                // Terrain blocking applies equally regardless of even/odd diagonals.
                closedSet.add(new AStarCellPoint(neighbor.position, false));
                closedSet.add(new AStarCellPoint(neighbor.position, true));
                blockNode = true;
                continue;
              case FREE:
                terrainIsFree = true;
                break;
              case NONE:
                break;
            }
          }
        }
      }
      terrainAdder = terrainAdder / cell_cost;

      if (blockNode) {
        log.debug("Terrain blocked movement to neighbor to {}", neighbor.position);
        continue;
      }

      // If the total terrainMultiplier equals out to zero, or there were no multipliers,
      // set to 1 so we do math right...
      if (terrainMultiplier == 0) {
        terrainMultiplier = 1;
      }

      terrainMultiplier = Math.abs(terrainMultiplier); // net negative multipliers screw with the AI

      if (terrainIsFree) {
        neighbor.g = node.g;
        neighbor.position.distanceTraveled = node.position.distanceTraveled;
      } else {
        neighbor.position.distanceTraveledWithoutTerrain =
            node.position.distanceTraveledWithoutTerrain + diagonalMultiplier;

        if (neighbor.isOddStepOfOneTwoOneMovement()) {
          neighbor.g = node.g + terrainAdder + terrainMultiplier;

          neighbor.position.distanceTraveled =
              node.position.distanceTraveled + terrainAdder + terrainMultiplier;
        } else {
          neighbor.g = node.g + terrainAdder + terrainMultiplier * Math.ceil(diagonalMultiplier);

          neighbor.position.distanceTraveled =
              node.position.distanceTraveled
                  + terrainAdder
                  + terrainMultiplier * Math.ceil(diagonalMultiplier);
        }
      }

      log.debug("Accepted neighbor: {}", neighbor.position);
      neighbors.add(neighbor);
    }

    return neighbors;
  }

  private boolean tokenFootprintIntersectsVBL(CellPoint position) {
    var points =
        footprint.getOccupiedCells(position).stream()
            .map(
                cellPoint -> {
                  var bounds = zone.getGrid().getBounds(cellPoint);
                  return new Coordinate(bounds.getCenterX(), bounds.getCenterY());
                })
            .toArray(Coordinate[]::new);
    Geometry footprintGeometry = new ConvexHull(points, geometryFactory).getConvexHull();

    return preparedTopology.intersects(footprintGeometry);
  }

  private boolean vblBlocksMovement(CellPoint start, CellPoint goal) {
    Map<CellPoint, Boolean> blockedMoves =
        vblBlockedMovesByGoal.computeIfAbsent(goal, pos -> new HashMap<>());
    Boolean test = blockedMoves.get(start);
    // if it's null then the test for that direction hasn't been set yet otherwise just return the
    // previous result
    if (test != null) {
      return test;
    }

    Rectangle startBounds = zone.getGrid().getBounds(start);
    Rectangle goalBounds = zone.getGrid().getBounds(goal);

    if (goalBounds.isEmpty() || startBounds.isEmpty()) {
      return false;
    }

    // NEW WAY - use polygon test
    double x1 = startBounds.getCenterX();
    double y1 = startBounds.getCenterY();
    double x2 = goalBounds.getCenterX();
    double y2 = goalBounds.getCenterY();
    LineString centerRay =
        geometryFactory.createLineString(
            new Coordinate[] {new Coordinate(x1, y1), new Coordinate(x2, y2)});

    boolean blocksMovement;
    try {
      blocksMovement = preparedTopology.intersects(centerRay);
    } catch (Exception e) {
      log.info("clipped.intersects oh oh: ", e);
      return true;
    }

    blockedMoves.put(start, blocksMovement);

    return blocksMovement;
  }

  private boolean fowBlocksMovement(CellPoint start, CellPoint goal) {
    if (fowExposedAreaGeometry == null) {
      return false;
    }

    Map<CellPoint, Boolean> blockedMoves =
        fowBlockedMovesByGoal.computeIfAbsent(goal, pos -> new HashMap<>());
    Boolean test = blockedMoves.get(start);
    // if it's null then the test for that direction hasn't been set yet otherwise just return the
    // previous result
    if (test != null) {
      return test;
    }

    Rectangle startBounds = zone.getGrid().getBounds(start);
    Rectangle goalBounds = zone.getGrid().getBounds(goal);

    if (goalBounds.isEmpty() || startBounds.isEmpty()) {
      return false;
    }

    // Check whether a center-to-center line touches hard FoW.
    double x1 = startBounds.getCenterX();
    double y1 = startBounds.getCenterY();
    double x2 = goalBounds.getCenterX();
    double y2 = goalBounds.getCenterY();
    LineString centerRay =
        geometryFactory.createLineString(
            new Coordinate[] {new Coordinate(x1, y1), new Coordinate(x2, y2)});

    boolean blocksMovement;
    try {
      blocksMovement = !fowExposedAreaGeometry.covers(centerRay);
    } catch (Exception e) {
      log.info("clipped.intersects oh oh: ", e);
      return true;
    }

    blockedMoves.put(start, blocksMovement);

    return blocksMovement;
  }

  protected void showDebugInfo(AStarCellPoint node) {
    if (!DeveloperOptions.Toggle.ShowAiDebugging.get()) {
      return;
    }

    final int basis = zone.getGrid().getSize() / 10;
    final int xOffset = basis * (node.isOddStepOfOneTwoOneMovement ? 7 : 3);

    Rectangle cellBounds = zone.getGrid().getBounds(node.position);
    DecimalFormat f = new DecimalFormat("##.00");

    Label gScore = new Label();
    Label hScore = new Label();
    Label fScore = new Label();
    Label parent = new Label();

    gScore.setLabel(f.format(node.g));
    gScore.setX(cellBounds.x + xOffset);
    gScore.setY(cellBounds.y + 1 * basis);

    hScore.setLabel(f.format(node.h));
    hScore.setX(cellBounds.x + xOffset);
    hScore.setY(cellBounds.y + 3 * basis);

    fScore.setLabel(f.format(node.fCost()));
    fScore.setX(cellBounds.x + xOffset);
    fScore.setY(cellBounds.y + 5 * basis);
    fScore.setForegroundColor(Color.RED);

    if (node.parent != null) {
      parent.setLabel(
          String.format(
              "(%d, %d | %s)",
              node.parent.position.x,
              node.parent.position.y,
              node.parent.isOddStepOfOneTwoOneMovement() ? "O" : "E"));
    } else {
      parent.setLabel("(none)");
    }
    parent.setX(cellBounds.x + xOffset);
    parent.setY(cellBounds.y + 7 * basis);
    parent.setForegroundColor(Color.BLUE);

    EventQueue.invokeLater(
        () -> {
          // Track labels to delete later
          debugLabels.addAll(
              List.of(gScore.getId(), hScore.getId(), fScore.getId(), parent.getId()));
          zone.putLabel(gScore);
          zone.putLabel(hScore);
          zone.putLabel(fScore);
          zone.putLabel(parent);
        });
  }
}
