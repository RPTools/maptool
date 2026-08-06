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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.model.Zone;

public class FogRenderer {
  private final RenderHelper renderHelper;
  private final Zone zone;
  private final ZoneView zoneView;

  public FogRenderer(RenderHelper renderHelper, Zone zone, ZoneView zoneView) {
    this.renderHelper = renderHelper.withTimerPrefix("FogRenderer");
    this.zone = zone;
    this.zoneView = zoneView;
  }

  public void render(Graphics2D g, PlayerView view) {
    var timer = CodeTimer.get();
    timer.start("FogRenderer-renderFog");
    try {
      if (!zone.hasFog()) {
        return;
      }

      this.renderHelper.bufferedRender(
          g, AlphaComposite.SrcOver, worldG -> renderWorld(worldG, view));
    } finally {
      timer.stop("FogRenderer-renderFog");
    }
  }

  private void renderWorld(Graphics2D worldG, PlayerView view) {
    /*
     * The tricky thing in this method is that the areas we have (exposed, visible) are the areas
     * where we should _not_ render. So we have to do clipped fills and clears instead of directly
     * rendering the areas.
     */

    var timer = CodeTimer.get();

    var visibility = zoneView.getVisibility(view);
    Area softFogArea = visibility.softFogArea();
    Area clearArea = visibility.clearArea();

    var originalClip = worldG.getClip();

    timer.start("FogRenderer-renderFog:hardFow");
    // Fill. This will be cleared out later to produce soft fog and clear visible area.
    worldG.setPaint(zone.getFogPaint().getPaint());
    // JFJ this fixes the GM exposed area view.
    worldG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, view.isGMView() ? .6f : 1f));
    var bounds = originalClip.getBounds();
    worldG.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    timer.stop("FogRenderer-renderFog:hardFow");

    timer.start("FogRenderer-renderFog:softFow");
    if (!softFogArea.isEmpty()) {
      worldG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
      worldG.setColor(new Color(0, 0, 0, AppPreferences.fogOverlayOpacity.get()));
      worldG.fill(softFogArea);
    }
    timer.stop("FogRenderer-renderFog:softFow");

    timer.start("FogRenderer-renderFog:exposedArea");
    if (!clearArea.isEmpty()) {
      // Now fill in the visible area.
      worldG.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
      worldG.fill(clearArea);
    }
    timer.stop("FogRenderer-renderFog:exposedArea");

    timer.start("FogRenderer-renderFog:outline");
    // If there is no boundary between soft fog and visible area, there is no need for an outline.
    if (!softFogArea.isEmpty() && !clearArea.isEmpty()) {
      worldG.setComposite(AlphaComposite.Src);
      // Keep the line a consistent thickness
      worldG.setStroke(new BasicStroke(1 / (float) worldG.getTransform().getScaleX()));
      worldG.setColor(Color.BLACK);
      worldG.draw(clearArea);
    }
    timer.stop("FogRenderer-renderFog:outline");
  }
}
