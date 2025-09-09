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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneView;

/**
 * Draws a solid black overlay wherever a non-GM player should see darkness.
 *
 * <p>If the current view is a GM view, this renders nothing since darkness is rendered as a light
 * source for GMs.
 */
public class DarknessRenderer {
  private final RenderHelper renderHelper;
  private final ZoneView zoneView;

  public DarknessRenderer(RenderHelper renderHelper, ZoneView zoneView) {
    this.renderHelper = renderHelper;
    this.zoneView = zoneView;
  }

  public void render(Graphics2D g2d, PlayerView view) {
    if (view.isGMView()) {
      return;
    }
    final Area darkness = zoneView.getIllumination(view).getDarkenedArea();
    if (darkness.isEmpty()) {
      // Skip the rendering work if it isn't necessary.
      return;
    }

    renderHelper.render(g2d, worldG -> renderWorld(worldG, darkness));
  }

  private void renderWorld(Graphics2D worldG, Area darkness) {
    worldG.setComposite(AlphaComposite.Src);
    worldG.setPaint(Color.black);
    worldG.fill(darkness);
  }
}
