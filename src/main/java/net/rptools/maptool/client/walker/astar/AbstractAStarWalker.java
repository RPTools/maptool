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

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import java.util.stream.Collectors;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.walker.AbstractZoneWalker;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.valid.IsValidOp;

public abstract class AbstractAStarWalker extends AbstractZoneWalker {
  private record TerrainModifier(Token.TerrainModifierOperation operation, double value) {}

  private static boolean isInteger(double d) {
    return (int) d == d;
  }

  private static final Logger log = LogManager.getLogger(AbstractAStarWalker.class);
  // Manually set this in order to view H, G & F costs as rendered labels

  private final GeometryFactory geometryFactory = new GeometryFactory();
  protected int crossX = 0;
  protected int crossY = 0;
  private Area vbl = new Area();
  private Area fowExposedArea = new Area();
  private double cell_cost = zone.getUnitsPerCell();
  private double distance = -1;
  private PreparedGeometry vblGeometry = null;
  private PreparedGeometry fowExposedAreaGeometry = null;
  // private long avgRetrieveTime;
  // private long avgTestTime;
  // private long retrievalCount;
  // private long testCount;
  private TokenFootprint footprint = new TokenFootprint();
  private Map<CellPoint, Map<CellPoint, Boolean>> vblBlockedMovesByGoal = new ConcurrentHashMap<>();
  private Map<CellPoint, Map<CellPoint, Boolean>> fowBlockedMovesByGoal = new ConcurrentHashMap<>();
  private final Map<CellPoint, List<TerrainModifier>> terrainCells = new HashMap<>();

  /**
   * The IDs of all debugging labels, so we can remove them again later. Only access this on the
   * Swing thread _or else_. TODO Make this per-walker. Unfortunately we create new walkers all the
   * time for each operation, so that isn't feasible right now.
   */
  private static final List<GUID> debugLabels = new ArrayList<>();

  public AbstractAStarWalker(Zone zone) {
    super(zone);

    // Get tokens on map that may affect movement
    for (Token token : zone.getTokensWithTerrainModifiers()) {
      // log.info("Token: " + token.getName() + ", " + token.getTerrainModifier());
      Set<CellPoint> cells = token.getOccupiedCells(zone.getGrid());
      for (CellPoint cell : cells) {
        terrainCells
            .computeIfAbsent(cell, ignored -> new ArrayList<>())
            .add(
                new TerrainModifier(
                    token.getTerrainModifierOperation(), token.getTerrainModifier()));
      }
    }
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

    // if (start.equals(end))
    // log.info("NO WORK!");

    var startNode = new AStarCellPoint(start, !isInteger(start.distanceTraveledWithoutTerrain));
    openList.add(startNode);
    openSet.put(startNode, startNode);

    AStarCellPoint currentNode = null;

    // Get current VBL for map...
    // Using JTS because AWT Area can only intersect with Area and we want to use simple lines here.
    // Render VBL to Geometry class once and store.
    // Note: zoneRenderer will be null if map is not visible to players.
    Area newVbl = new Area();
    Area newFowExposedArea = new Area();
    final var zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    if (zoneRenderer != null) {
      final var zoneView = zoneRenderer.getZoneView();

      var mbl = zoneView.getTopology(Zone.TopologyType.MBL);
      if (tokenMbl != null) {
        mbl = new Area(mbl);
        mbl.subtract(tokenMbl);
      }

      if (MapTool.getServerPolicy().getVblBlocksMove()) {
        var wallVbl = zoneView.getTopology(Zone.TopologyType.WALL_VBL);
        var hillVbl = zoneView.getTopology(Zone.TopologyType.HILL_VBL);
        var pitVbl = zoneView.getTopology(Zone.TopologyType.PIT_VBL);

        // A token's topology should not be used to block itself!
        if (tokenWallVbl != null) {
          wallVbl = new Area(wallVbl);
          wallVbl.subtract(tokenWallVbl);
        }
        if (tokenHillVbl != null) {
          hillVbl = new Area(hillVbl);
          hillVbl.subtract(tokenHillVbl);
        }
        if (tokenPitVbl != null) {
          pitVbl = new Area(pitVbl);
          pitVbl.subtract(tokenPitVbl);
        }

        newVbl.add(wallVbl);
        newVbl.add(hillVbl);
        newVbl.add(pitVbl);

        // Finally, add the Move Blocking Layer!
        newVbl.add(mbl);
      } else {
        newVbl = mbl;
      }

      newFowExposedArea =
          zoneRenderer.getZone().hasFog()
              ? zoneView.getExposedArea(zoneRenderer.getPlayerView())
              : null;
    }

    if (!newVbl.equals(vbl)) {
      // The move cache may no longer accurately reflect the VBL limitations.
      this.vblBlockedMovesByGoal.clear();

      vbl = newVbl;
      // VBL has changed. Let's update the JTS geometry to match.
      if (vbl.isEmpty()) {
        this.vblGeometry = null;
      } else {
        try {
          var vblGeometry = GeometryUtil.toJts(vbl);

          // polygons
          if (!vblGeometry.isValid()) {
            log.info(
                "vblGeometry is invalid! May cause issues. Check for self-intersecting polygons.");
            log.debug("Invalid vblGeometry: " + new IsValidOp(vblGeometry).getValidationError());
          }

          vblGeometry = vblGeometry.buffer(1); // .buffer always creates valid geometry.
          this.vblGeometry = PreparedGeometryFactory.prepare(vblGeometry);
        } catch (Exception e) {
          log.info("vblGeometry oh oh: ", e);
        }
      }

      // log.info("vblGeometry bounds: " + vblGeometry.toString());
    }
    if (!Objects.equals(newFowExposedArea, fowExposedArea)) {
      // The move cache may no longer accurately reflect the FOW limitations.
      this.fowBlockedMovesByGoal.clear();

      fowExposedArea = newFowExposedArea;
      // FoW has changed. Let's update the JTS geometry to match.
      if (fowExposedArea == null || fowExposedArea.isEmpty()) {
        this.fowExposedAreaGeometry = null;
      } else {
        try {
          var fowExposedAreaGeometry = GeometryUtil.toJts(fowExposedArea);

          // polygons
          if (!fowExposedAreaGeometry.isValid()) {
            log.info(
                "FoW Geometry is invalid! May cause issues. Check for self-intersecting polygons.");
            log.debug(
                "Invalid FoW Geometry: "
                    + new IsValidOp(fowExposedAreaGeometry).getValidationError());
          }

          fowExposedAreaGeometry =
              fowExposedAreaGeometry.buffer(1); // .buffer always creates valid geometry.
          this.fowExposedAreaGeometry = PreparedGeometryFactory.prepare(fowExposedAreaGeometry);
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

    // log.info("A* Path timeout estimate: " + estimatedTimeoutNeeded);

    Rectangle pathfindingBounds = this.getPathfindingBounds(start, goal);

    while (!openList.isEmpty()) {
      if (System.currentTimeMillis() > timeOut + estimatedTimeoutNeeded) {
        log.info("Timing out after " + estimatedTimeoutNeeded);
        break;
      }

      currentNode = openList.remove();
      openSet.remove(currentNode);
      if (currentNode.position.equals(goal)) {
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
      }

      closedSet.add(currentNode);
      currentNode = null;

      /*
        We now calculate paths off the main UI thread but only one at a time.
        If the token moves, we cancel the thread and restart so we're only calculating the most
        recent path request. Clearing the list effectively finishes this thread gracefully.
      */
      if (Thread.interrupted()) {
        // log.info("Thread interrupted!");
        openList.clear();
      }
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

    // if (retrievalCount > 0)
    // log.info("avgRetrieveTime: " + Math.floor(avgRetrieveTime / retrievalCount)/1000 + " micro");
    // if (testCount > 0)
    // log.info("avgTestTime: " + Math.floor(avgTestTime / testCount)/1000 + " micro");

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
  protected Rectangle getPathfindingBounds(CellPoint start, CellPoint goal) {
    // Bounding box must contain all VBL/MBL ...
    Rectangle pathfindingBounds = vbl.getBounds();
    pathfindingBounds = pathfindingBounds.union(fowExposedArea.getBounds());
    // ... and the footprints of all terrain tokens ...
    for (var cellPoint : terrainCells.keySet()) {
      pathfindingBounds = pathfindingBounds.union(zone.getGrid().getBounds(cellPoint));
    }
    // ... and the original token position ...
    pathfindingBounds = pathfindingBounds.union(zone.getGrid().getBounds(start));
    // ... and the target token position ...
    pathfindingBounds = pathfindingBounds.union(zone.getGrid().getBounds(goal));
    // ... and have ample room for the token to go anywhere around the outside if necessary.
    var tokenBounds = footprint.getBounds(zone.getGrid());
    pathfindingBounds.grow(2 * tokenBounds.width, 2 * tokenBounds.height);

    return pathfindingBounds;
  }

  protected List<AStarCellPoint> getNeighbors(
      AStarCellPoint node, Set<AStarCellPoint> closedSet, Rectangle pathfindingBounds) {
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
      if (closedSet.contains(neighbor)) {
        continue;
      }

      if (!zone.getGrid().getBounds(node.position).intersects(pathfindingBounds)) {
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
          blockNode = true;
          continue;
        }

        Set<CellPoint> occupiedCells = footprint.getOccupiedCells(node.position);
        for (CellPoint cellPoint : occupiedCells) {
          // Check whether moving the occupied cell to its new location would be prohibited by VBL.
          var cellNeighbor =
              new CellPoint(cellPoint.x + neighborArray[0], cellPoint.y + neighborArray[1]);
          if (vblBlocksMovement(cellPoint, cellNeighbor)) {
            blockNode = true;
            break;
          }
          if (fowBlocksMovement(cellPoint, cellNeighbor)) {
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

      neighbors.add(neighbor);
    }

    return neighbors;
  }

  private boolean tokenFootprintIntersectsVBL(CellPoint position) {
    if (vblGeometry == null) {
      return false;
    }

    var points =
        footprint.getOccupiedCells(position).stream()
            .map(
                cellPoint -> {
                  var bounds = zone.getGrid().getBounds(cellPoint);
                  return new Coordinate(bounds.getCenterX(), bounds.getCenterY());
                })
            .toArray(Coordinate[]::new);
    Geometry footprintGeometry = new ConvexHull(points, geometryFactory).getConvexHull();

    return vblGeometry.intersects(footprintGeometry);
  }

  private boolean vblBlocksMovement(CellPoint start, CellPoint goal) {
    if (vblGeometry == null) {
      return false;
    }

    // Stopwatch stopwatch = Stopwatch.createStarted();
    Map<CellPoint, Boolean> blockedMoves =
        vblBlockedMovesByGoal.computeIfAbsent(goal, pos -> new HashMap<>());
    Boolean test = blockedMoves.get(start);
    // if it's null then the test for that direction hasn't been set yet otherwise just return the
    // previous result
    if (test != null) {
      // log.info("Time to retrieve: " + stopwatch.elapsed(TimeUnit.NANOSECONDS));
      // avgRetrieveTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
      // retrievalCount++;
      return test;
    }

    Rectangle startBounds = zone.getGrid().getBounds(start);
    Rectangle goalBounds = zone.getGrid().getBounds(goal);

    if (goalBounds.isEmpty() || startBounds.isEmpty()) {
      return false;
    }

    // If the goal center point is in vbl, allow to maintain path through vbl (should be GM only?)
    /*
    if (vbl.contains(goal.toPoint())) {
      // Allow GM to move through VBL
       return !MapTool.getPlayer().isGM();
    }
    */

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
      blocksMovement = vblGeometry.intersects(centerRay);
    } catch (Exception e) {
      log.info("clipped.intersects oh oh: ", e);
      return true;
    }

    // avgTestTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
    // testCount++;

    blockedMoves.put(start, blocksMovement);

    return blocksMovement;
  }

  private boolean fowBlocksMovement(CellPoint start, CellPoint goal) {
    if (MapTool.getPlayer().isEffectiveGM()) {
      return false;
    }

    if (fowExposedAreaGeometry == null) {
      return false;
    }

    // Stopwatch stopwatch = Stopwatch.createStarted();
    Map<CellPoint, Boolean> blockedMoves =
        fowBlockedMovesByGoal.computeIfAbsent(goal, pos -> new HashMap<>());
    Boolean test = blockedMoves.get(start);
    // if it's null then the test for that direction hasn't been set yet otherwise just return the
    // previous result
    if (test != null) {
      // log.info("Time to retrieve: " + stopwatch.elapsed(TimeUnit.NANOSECONDS));
      // avgRetrieveTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
      // retrievalCount++;
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

    // avgTestTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
    // testCount++;

    blockedMoves.put(start, blocksMovement);

    return blocksMovement;
  }

  protected void showDebugInfo(AStarCellPoint node) {
    if (!DeveloperOptions.Toggle.ShowAiDebugging.isEnabled()) {
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
