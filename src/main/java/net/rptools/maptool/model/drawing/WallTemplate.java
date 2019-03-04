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
package net.rptools.maptool.model.drawing;

import java.util.List;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ZonePoint;

/**
 * A template that draws consecutive blocks
 *
 * @author Jay
 */
public class WallTemplate extends LineTemplate {
  /**
   * Set the path vertex, it isn't needed by the wall template but the superclass needs it to paint.
   */
  public WallTemplate() {
    setPathVertex(new ZonePoint(0, 0));
  }

  /** @see net.rptools.maptool.model.drawing.AbstractTemplate#getRadius() */
  @Override
  public int getRadius() {
    return getPath() == null ? 0 : getPath().size();
  }

  /** @see net.rptools.maptool.model.drawing.LineTemplate#setRadius(int) */
  @Override
  public void setRadius(int squares) {
    // Do nothing, calculated from path length
  }

  /**
   * @see
   *     net.rptools.maptool.model.drawing.LineTemplate#setVertex(net.rptools.maptool.model.ZonePoint)
   */
  @Override
  public void setVertex(ZonePoint vertex) {
    ZonePoint v = getVertex();
    v.x = vertex.x;
    v.y = vertex.y;
  }

  /** @see net.rptools.maptool.model.drawing.LineTemplate#calcPath() */
  @Override
  protected List<CellPoint> calcPath() {
    return getPath(); // Do nothing, path is set by tool.
  }
}
