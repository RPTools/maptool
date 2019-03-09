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
package net.rptools.maptool.client.walker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.walker.astar.AStarCellPoint;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone;

public class NaiveWalker extends AbstractZoneWalker {
  public NaiveWalker(Zone zone) {
    super(zone);
  }

  private int distance;

  @Override
  public List<CellPoint> calculatePath(CellPoint start, CellPoint end) {
    List<CellPoint> list = new ArrayList<CellPoint>();

    int x = start.x;
    int y = start.y;

    int count = 0;
    while (true && count < 100) {
      list.add(new CellPoint(x, y));

      if (x == end.x && y == end.y) {
        break;
      }
      if (x < end.x) x++;
      if (x > end.x) x--;
      if (y < end.y) y++;
      if (y > end.y) y--;

      count++;
    }
    distance = (list.size() - 1) * 5;
    return list;
  }

  public int getDistance() {
    return distance;
  }

  @Override
  public void setFootprint(TokenFootprint footprint) {
    // Not needed/used here
    System.out.println("Should not see this ever!");
  }

  @Override
  public Set<AStarCellPoint> getCheckedPoints() {
    return null;
  }
}
