/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.walker.astar;

import java.util.List;

import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

public abstract class AbstractAStarHexEuclideanWalker extends AbstractAStarWalker {
	protected int[][] oddNeighborMap;
	protected int[][] evenNeighborMap;

	public AbstractAStarHexEuclideanWalker(Zone zone) {
		super(zone);
	}

	protected abstract void initNeighborMaps();

	@Override
	protected int[][] getNeighborMap(int x, int y) {
		return x % 2 == 0 ? evenNeighborMap : oddNeighborMap;
	}

	@Override
	protected double gScore(CellPoint p1, CellPoint p2) {
		return euclideanDistance(p1, p2);
	}

	@Override
	protected double hScore(CellPoint p1, CellPoint p2) {
		return euclideanDistance(p1, p2);
	}

	private double euclideanDistance(CellPoint p1, CellPoint p2) {
		ZonePoint zp1 = getZone().getGrid().convert(p1);
		ZonePoint zp2 = getZone().getGrid().convert(p2);

		int a = zp2.x - zp1.x;
		int b = zp2.y - zp1.y;

		return Math.sqrt(a * a + b * b);
	}

	@Override
	protected int calculateDistance(List<CellPoint> path, int feetPerCell) {
		int cellsMoved = path != null && path.size() > 1 ? path.size() - 1 : 0;
		return cellsMoved * feetPerCell;
	}
}
