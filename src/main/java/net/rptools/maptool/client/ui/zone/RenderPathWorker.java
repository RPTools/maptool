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

import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.SwingWorker;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.TerrainModifierOperation;

public class RenderPathWorker extends SwingWorker<Void, Void> {

  ZoneRenderer zoneRenderer;
  @Nonnull ZoneWalker walker;
  CellPoint startPoint, endPoint;
  private final boolean restrictMovement;
  private final Set<TerrainModifierOperation> terrainModifiersIgnored;
  private final @Nonnull Token keyToken;

  public RenderPathWorker(
      @Nonnull ZoneWalker walker,
      CellPoint endPoint,
      boolean restrictMovement,
      Set<TerrainModifierOperation> terrainModifiersIgnored,
      @Nonnull Token keyToken,
      ZoneRenderer zoneRenderer) {
    this.walker = walker;
    this.endPoint = endPoint;
    this.restrictMovement = restrictMovement;
    this.zoneRenderer = zoneRenderer;
    this.terrainModifiersIgnored = terrainModifiersIgnored;
    this.keyToken = keyToken;
  }

  @Override
  protected Void doInBackground() {
    walker.replaceLastWaypoint(endPoint, restrictMovement, terrainModifiersIgnored, keyToken);
    return null;
  }

  @Override
  protected void done() {
    zoneRenderer.repaint();
  }
}
