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
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public abstract class AbstractAStarWalker extends AbstractZoneWalker {
  private static final Logger log = LogManager.getLogger(AbstractAStarWalker.class);

  private boolean debugCosts = false; // Manually set this to view H, G & F costs as rendered labels
  private List<GUID> debugLabels;

  private Area vbl = new Area();
  private double normal_cost = 1; // zone.getUnitsPerCell();
  private double distance = -1;

  private final GeometryFactory geometryFactory = new GeometryFactory();
  private ShapeReader shapeReader = new ShapeReader(geometryFactory);
  private Geometry vblGeometry = null;
  private TokenFootprint footprint = new TokenFootprint();

  private Map<AStarCellPoint, AStarCellPoint> checkedList =
      new ConcurrentHashMap<AStarCellPoint, AStarCellPoint>();
  private long avgRetrieveTime;
  private long avgTestTime;
  private long retrievalCount;
  private long testCount;

  protected int crossX = 0;
  protected int crossY = 0;

  private List<AStarCellPoint> terrainCells = new ArrayList<AStarCellPoint>();

  public AbstractAStarWalker(Zone zone) {
    super(zone);

    // Get tokens on map that may affect movement
    for (Token token : zone.getTokensWithTerrainModifiers()) {
      // log.info("Token: " + token.getName() + ", " + token.getTerrainModifier());
      Set<CellPoint> cells = token.getOccupiedCells(zone.getGrid());
      for (CellPoint cell : cells)
        terrainCells.add(new AStarCellPoint(cell, token.getTerrainModifier()));
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
   */
  protected abstract int[][] getNeighborMap(int x, int y);

  protected abstract double hScore(CellPoint p1, CellPoint p2);

  protected abstract double getDiagonalMultiplier(int[] neighborArray);

  public double getDistance() {
    if (distance < 0) return 0;
    else return distance;
  }

  public Collection<AStarCellPoint> getCheckedPoints() {
    return checkedList.values();
  }

  @Override
  public void setFootprint(TokenFootprint footprint) {
    this.footprint = footprint;
  }

  @Override
  protected List<CellPoint> calculatePath(CellPoint start, CellPoint goal) {
    crossX = start.x - goal.x;
    crossY = start.y - goal.y;

    List<AStarCellPoint> openList = new ArrayList<AStarCellPoint>();
    Map<AStarCellPoint, AStarCellPoint> openSet =
        new HashMap<AStarCellPoint, AStarCellPoint>(); // For faster lookups
    Set<AStarCellPoint> closedSet = new HashSet<AStarCellPoint>();

    // Current fail safe... bail out after 10 seconds of searching just in case, shouldn't hang UI
    // as this is off the AWT thread
    long timeOut = System.currentTimeMillis();
    double estimatedTimeoutNeeded = 10000;

    // if (start.equals(end))
    // log.info("NO WORK!");

    openList.add(new AStarCellPoint(start));
    openSet.put(openList.get(0), openList.get(0));

    AStarCellPoint currentNode = null;

    // Get current VBL for map...
    // Using JTS because AWT Area can only intersect with Area and we want to use simple lines here.
    // Render VBL to Geometry class once and store.
    // Note: zoneRenderer will be null if map is not visible to players.
    if (MapTool.getFrame().getCurrentZoneRenderer() != null)
      vbl = MapTool.getFrame().getCurrentZoneRenderer().getZoneView().getTopologyTree().getArea();

    if (!vbl.isEmpty()) {
      try {
        vblGeometry =
            shapeReader
                .read(vbl.getPathIterator(null))
                .buffer(1); // .buffer helps creating valid geometry and prevent self-intersecting
        // polygons
        if (!vblGeometry.isValid())
          log.info(
              "vblGeometry is invalid! May cause issues. Check for self-intersecting polygons.");
      } catch (Exception e) {
        log.info("vblGeometry oh oh: ", e);
      }

      // log.info("vblGeometry bounds: " + vblGeometry.toString());
    }

    // Erase previous debug labels, this actually erases ALL labels! Use only when debugging!
    if (!zone.getLabels().isEmpty() && debugCosts) {
      for (Label label : zone.getLabels()) {
        zone.removeLabel(label.getId());
      }
    }

    // Timeout quicker for GM cause reasons
    if (MapTool.getPlayer().isGM()) estimatedTimeoutNeeded = estimatedTimeoutNeeded / 2;

    // log.info("A* Path timeout estimate: " + estimatedTimeoutNeeded);

    while (!openList.isEmpty()) {
      if (System.currentTimeMillis() > timeOut + estimatedTimeoutNeeded) {
        log.info("Timing out after " + estimatedTimeoutNeeded);
        break;
      }

      currentNode = openList.remove(0);
      openSet.remove(currentNode);
      if (currentNode.equals(goal)) {
        break;
      }

      for (AStarCellPoint currentNeighbor : getNeighbors(currentNode, closedSet)) {
        currentNeighbor.h = hScore(currentNeighbor, goal);
        showDebugInfo(currentNeighbor);

        if (openSet.containsKey(currentNeighbor)) {
          // check if it is cheaper to get here the way that we just came, versus the previous path
          AStarCellPoint oldNode = openSet.get(currentNeighbor);
          if (currentNeighbor.gCost() < oldNode.gCost()) {
            oldNode.replaceG(currentNeighbor);
            currentNeighbor = oldNode;
            currentNeighbor.parent = currentNode;
          }
          continue;
        }

        pushNode(openList, currentNeighbor);
        openSet.put(currentNeighbor, currentNeighbor);
      }

      closedSet.add(currentNode);
      currentNode = null;

      // We now calculate paths off the main UI thread but only one at a time. If the token moves we
      // cancel the thread
      // and restart so we're only caclulating the most recent path request. Clearing the list
      // effectively finishes
      // this thread gracefully.
      if (Thread.interrupted()) {
        // log.info("Thread interrupted!");
        openList.clear();
      }
    }

    List<CellPoint> ret = new LinkedList<CellPoint>();
    while (currentNode != null) {
      ret.add(currentNode);
      currentNode = currentNode.parent;
    }

    // Jamz We don't need to "calculate" distance after the fact as it's already stored as the G
    // cost...
    if (!ret.isEmpty()) distance = ret.get(0).getDistanceTraveled(zone);
    else distance = 0;

    Collections.reverse(ret);
    timeOut = (System.currentTimeMillis() - timeOut);
    if (timeOut > 500) log.debug("Time to calculate A* path warning: " + timeOut + "ms");

    // if (retrievalCount > 0)
    // log.info("avgRetrieveTime: " + Math.floor(avgRetrieveTime / retrievalCount)/1000 + " micro");
    // if (testCount > 0)
    // log.info("avgTestTime: " + Math.floor(avgTestTime / testCount)/1000 + " micro");

    return ret;
  }

  void pushNode(List<AStarCellPoint> list, AStarCellPoint node) {
    if (list.isEmpty()) {
      list.add(node);
      return;
    }
    if (node.fCost() < list.get(0).fCost()) {
      list.add(0, node);
      return;
    }
    if (node.fCost() > list.get(list.size() - 1).fCost()) {
      list.add(node);
      return;
    }
    for (ListIterator<AStarCellPoint> iter = list.listIterator(); iter.hasNext(); ) {
      AStarCellPoint listNode = iter.next();
      if (listNode.fCost() >= node.fCost()) {
        iter.previous();
        iter.add(node);
        return;
      }
    }
  }

  protected List<AStarCellPoint> getNeighbors(AStarCellPoint node, Set<AStarCellPoint> closedSet) {
    List<AStarCellPoint> neighbors = new ArrayList<AStarCellPoint>();
    int[][] neighborMap = getNeighborMap(node.x, node.y);

    // Find all the neighbors.
    for (int[] neighborArray : neighborMap) {
      double terrainModifier = 0;
      boolean blockNode = false;

      AStarCellPoint neighbor =
          new AStarCellPoint(node.x + neighborArray[0], node.y + neighborArray[1]);
      Set<CellPoint> occupiedCells = footprint.getOccupiedCells(node);

      if (closedSet.contains(neighbor)) continue;

      // Add the cell we're coming from
      neighbor.parent = node;

      // Don't count VBL or Terrain Modifiers
      if (restrictMovement) {
        for (CellPoint cellPoint : occupiedCells) {
          AStarCellPoint occupiedNode = new AStarCellPoint(cellPoint);

          // VBL Check FIXME: Add to closed set?
          if (vblBlocksMovement(occupiedNode, neighbor)) {
            closedSet.add(occupiedNode);
            blockNode = true;
            break;
          }
        }

        if (blockNode) continue;

        // Check for terrain modifiers
        for (AStarCellPoint cell : terrainCells) {
          if (cell.equals(neighbor)) {
            terrainModifier += cell.terrainModifier;
            // log.info("terrainModifier for " + cell + " = " + cell.terrainModifier);
          }
        }
      }

      // Tokens with no terrainModifier set would be a zero so multiplier is set to 1 in that case
      if (terrainModifier == 0) terrainModifier = 1;

      // Get diagonal cost multiplier, if any...
      double diagonalMultiplier = getDiagonalMultiplier(neighborArray);
      neighbor.distanceTraveled =
          node.distanceTraveled + (normal_cost * terrainModifier * diagonalMultiplier);
      neighbor.g = node.g + (normal_cost * terrainModifier * diagonalMultiplier);

      neighbors.add(neighbor);
      // log.info("neighbor.g: " + neighbor.getG());
    }

    return neighbors;
  }

  private boolean vblBlocksMovement(AStarCellPoint start, AStarCellPoint goal) {
    if (vblGeometry == null) return false;

    // Stopwatch stopwatch = Stopwatch.createStarted();
    AStarCellPoint checkNode = checkedList.get(goal);
    if (checkNode != null) {
      Boolean test = checkNode.isValidMove(start);

      // if it's null then the test for that direction hasn't been set yet otherwise just return the
      // previous result
      if (test != null) {
        // log.info("Time to retrieve: " + stopwatch.elapsed(TimeUnit.NANOSECONDS));
        // avgRetrieveTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
        // retrievalCount++;
        return test;
      } else {
        // Copies all previous checks to save later...
        goal = checkNode;
      }
    }

    Rectangle startBounds = zone.getGrid().getBounds(start);
    Rectangle goalBounds = zone.getGrid().getBounds(goal);

    if (goalBounds.isEmpty() || startBounds.isEmpty()) return false;

    // If there is no vbl within the footprints, we're good!
    if (!vbl.intersects(startBounds) && !vbl.intersects(goalBounds)) return false;

    // If the goal center point is in vbl, allow to maintain path through vbl (should be GM only?)
    if (vbl.contains(goal.toPoint())) {
      // Allow GM to move through VBL
      // return !MapTool.getPlayer().isGM();
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
      blocksMovement = vblGeometry.intersects(centerRay);
    } catch (Exception e) {
      log.info("clipped.intersects oh oh: ", e);
      return true;
    }

    // avgTestTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
    // testCount++;

    goal.setValidMove(start, blocksMovement);
    checkedList.put(goal, goal);

    return blocksMovement;
  }

  protected void showDebugInfo(AStarCellPoint node) {
    if (!log.isDebugEnabled() && !debugCosts) return;

    if (debugLabels == null) debugLabels = new ArrayList<GUID>();

    Rectangle cellBounds = zone.getGrid().getBounds(node);
    DecimalFormat f = new DecimalFormat("##.00");

    Label gScore = new Label();
    Label hScore = new Label();
    Label fScore = new Label();

    gScore.setLabel(f.format(node.gCost()));
    gScore.setX(cellBounds.x + 10);
    gScore.setY(cellBounds.y + 10);

    hScore.setLabel(f.format(node.h));
    hScore.setX(cellBounds.x + 35);
    hScore.setY(cellBounds.y + 10);

    fScore.setLabel(f.format(node.fCost()));
    fScore.setX(cellBounds.x + 25);
    fScore.setY(cellBounds.y + 25);
    fScore.setForegroundColor(Color.RED);

    zone.putLabel(gScore);
    zone.putLabel(hScore);
    zone.putLabel(fScore);

    // Track labels to delete later
    debugLabels.add(gScore.getId());
    debugLabels.add(hScore.getId());
    debugLabels.add(fScore.getId());
  }
}
