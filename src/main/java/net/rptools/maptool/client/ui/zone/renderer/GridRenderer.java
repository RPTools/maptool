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
package net.rptools.maptool.client.ui.zone.renderer;

import java.awt.*;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.model.Zone;

public class GridRenderer {
  private Zone zone;
  private ZoneRenderer renderer;

  private boolean initialised = false;

  public boolean isInitialised() {
    return initialised;
  }

  GridRenderer() {}

  public void setRenderer(ZoneRenderer zoneRenderer) {
    renderer = zoneRenderer;
    zone = renderer.getZone();
    initialised = true;
  }

  protected void renderGrid(Graphics2D g, PlayerView view) {
    int gridSize = (int) (zone.getGrid().getSize() * renderer.getScale());
    if (!AppState.isShowGrid() || gridSize < ZoneRendererConstants.MIN_GRID_SIZE) {
      return;
    }
    zone.getGrid().draw(renderer, g, g.getClipBounds());
  }

  protected void renderCoordinates(Graphics2D g, PlayerView view) {
    if (AppState.isShowCoordinates()) {
      zone.getGrid().drawCoordinatesOverlay(g, renderer);
    }
  }
}
