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

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.rptools.maptool.client.ui.zone.RenderPathWorker;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.TerrainModifierOperation;
import net.rptools.maptool.model.TokenFootprint;

public interface ZoneWalker extends AutoCloseable {
  @Override
  void close();

  public void setWaypoints(CellPoint... points);

  public void addWaypoints(CellPoint... point);

  public void replaceLastWaypoint(CellPoint point);

  public void replaceLastWaypoint(
      CellPoint point,
      boolean restrictMovement,
      Set<TerrainModifierOperation> terrainModifiersIgnored,
      Token keyToken);

  public boolean isWaypoint(CellPoint point);

  public double getDistance();

  public @Nonnull Path<CellPoint> getPath();

  public Path<CellPoint> getPath(RenderPathWorker renderPathWorker);

  public CellPoint getLastPoint();

  /**
   * Remove an existing waypoint. Nothing is removed if the passed point is not a waypoint.
   *
   * @param point The point to be removed
   * @return The value <code>true</code> is returned if the point is removed.
   */
  boolean removeWaypoint(CellPoint point);

  /**
   * Toggle the existence of a way point. A waypoint is added if the passed point is not on an
   * existing waypoint or a waypoint is removed if it is on an existing point.
   *
   * @param point Point being toggled
   * @return The value <code>true</code> if a waypoint was added, <code>false</code> if one was
   *     removed.
   */
  boolean toggleWaypoint(CellPoint point);

  public void setFootprint(TokenFootprint footprint);

  public default Map<CellPoint, Set<CellPoint>> getBlockedMoves() {
    return null;
  }
}
