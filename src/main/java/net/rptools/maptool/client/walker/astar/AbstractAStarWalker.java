/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.walker.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.rptools.maptool.client.walker.AbstractZoneWalker;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;

public abstract class AbstractAStarWalker extends AbstractZoneWalker {
	public AbstractAStarWalker(Zone zone) {
		super(zone);
	}

	private int distance = -1;

	/**
	 * Returns the list of neighbor cells that are valid for being movement-checked. This is an array of (x,y) offsets (see the constants in this class) named as compass points.
	 * <p>
	 * It should be possible to query the current (x,y) CellPoint passed in to determine which directions are feasible to move into. But it would require information about visibility (which token is
	 * moving, does it have sight, and so on). Currently that information is not available here, but perhaps an option Token parameter could be specified to the constructor? Or maybe as the tree was
	 * scanned, since I believe all Grids share a common ZoneWalker.
	 */
	protected abstract int[][] getNeighborMap(int x, int y);

	@Override
	protected List<CellPoint> calculatePath(CellPoint start, CellPoint end) {
		List<AStarCellPoint> openList = new ArrayList<AStarCellPoint>();
		Map<AStarCellPoint, AStarCellPoint> openSet = new HashMap<AStarCellPoint, AStarCellPoint>(); // For faster lookups
		Set<AStarCellPoint> closedSet = new HashSet<AStarCellPoint>();

		openList.add(new AStarCellPoint(start));
		openSet.put(openList.get(0), openList.get(0));

		AStarCellPoint node = null;

		while (!openList.isEmpty()) {
			node = openList.remove(0);
			openSet.remove(node);
			if (node.equals(end)) {
				break;
			}
			int[][] neighborMap = getNeighborMap(node.x, node.y);
			for (int i = 0; i < neighborMap.length; i++) {
				int x = node.x + neighborMap[i][0];
				int y = node.y + neighborMap[i][1];
				AStarCellPoint neighborNode = new AStarCellPoint(x, y);
				if (closedSet.contains(neighborNode)) {
					continue;
				}
				neighborNode.parent = node;
				neighborNode.gScore = gScore(start, neighborNode);
				neighborNode.hScore = hScore(neighborNode, end);

				if (openSet.containsKey(neighborNode)) {
					AStarCellPoint oldNode = openSet.get(neighborNode);
					// check if it is cheaper to get here the way that we just
					// came, versus the previous path
					if (neighborNode.gScore < oldNode.gScore) {
						oldNode.gScore = neighborNode.gScore;
						neighborNode = oldNode;
						neighborNode.parent = node;
					}
					continue;
				}
				pushNode(openList, neighborNode);
				openSet.put(neighborNode, neighborNode);
			}
			closedSet.add(node);
			node = null;
		}
		List<CellPoint> ret = new LinkedList<CellPoint>();
		while (node != null) {
			ret.add(node);
			node = node.parent;
		}
		distance = -1;
		Collections.reverse(ret);
		return ret;
	}

	private void pushNode(List<AStarCellPoint> list, AStarCellPoint node) {
		if (list.isEmpty()) {
			list.add(node);
			return;
		}
		if (node.cost() < list.get(0).cost()) {
			list.add(0, node);
			return;
		}
		if (node.cost() > list.get(list.size() - 1).cost()) {
			list.add(node);
			return;
		}
		for (ListIterator<AStarCellPoint> iter = list.listIterator(); iter.hasNext();) {
			AStarCellPoint listNode = iter.next();
			if (listNode.cost() > node.cost()) {
				iter.previous();
				iter.add(node);
				return;
			}
		}
	}

	protected abstract int calculateDistance(List<CellPoint> path, int feetPerCell);

	protected abstract double gScore(CellPoint p1, CellPoint p2);

	protected abstract double hScore(CellPoint p1, CellPoint p2);

	public int getDistance() {
		if (distance == -1) {
			distance = calculateDistance(getPath().getCellPath(), getZone().getUnitsPerCell());
		}
		return distance;
	}
}
