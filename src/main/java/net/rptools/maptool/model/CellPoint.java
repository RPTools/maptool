/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

/**
 * This class represents a location based on the grid coordinates of a zone.
 * <p>
 * They can be converted to screen coordinates by calling {@link #convertToScreen(ZoneRenderer)}.
 * <p>
 * They can be converted to ZonePoints by calling {@link Grid#convert(CellPoint)}.
 * 
 * @author trevor
 */
public class CellPoint extends AbstractPoint {
	public CellPoint(int x, int y) {
		super(x, y);
	}

	@Override
	public String toString() {
		return "CellPoint" + super.toString();
	}

	/**
	 * Find the screen coordinates of the upper left hand corner of a cell taking into account scaling and translation. <b>This code does not call {@link Grid#getCellOffset()}, which might be
	 * appropriate in some circumstances.</b>
	 * 
	 * @param renderer
	 *            This renderer provides scaling
	 * @return The screen coordinates of the upper left hand corner in the passed point or in a new point.
	 */
	public ScreenPoint convertToScreen(ZoneRenderer renderer) {
		double scale = renderer.getScale();
		Zone zone = renderer.getZone();

		Grid grid = zone.getGrid();
		ZonePoint zp = grid.convert(this);

		int sx = renderer.getViewOffsetX() + (int) (zp.x * scale);
		int sy = renderer.getViewOffsetY() + (int) (zp.y * scale);

		return new ScreenPoint(sx, sy);
	}
}
