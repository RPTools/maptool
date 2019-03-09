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
package net.rptools.maptool.client.ui.zone;

import javax.swing.SwingWorker;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.model.CellPoint;

public class RenderPathWorker extends SwingWorker<Void, Void> {
  // private static final Logger log = LogManager.getLogger(RenderPathWorker.class);

  ZoneRenderer zoneRenderer;
  ZoneWalker walker;
  CellPoint startPoint, endPoint;
  private boolean restrictMovement = false;

  public RenderPathWorker(
      ZoneWalker walker, CellPoint endPoint, boolean restrictMovement, ZoneRenderer zoneRenderer) {
    this.walker = walker;
    this.endPoint = endPoint;
    this.restrictMovement = restrictMovement;
    this.zoneRenderer = zoneRenderer;
  }

  @Override
  protected Void doInBackground() throws Exception {
    walker.replaceLastWaypoint(endPoint, restrictMovement);
    return null;
  }

  @Override
  protected void done() {
    zoneRenderer.repaint();
  }
}
