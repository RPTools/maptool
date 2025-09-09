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
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.ui.zone.DrawableLight;
import net.rptools.maptool.client.ui.zone.LightingComposite;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.model.Zone;

/**
 * Draws a solid black overlay wherever a non-GM player should see darkness.
 *
 * <p>If the current view is a GM view, this renders nothing since darkness is rendered as a light
 * source for GMs.
 */
public class LightsRenderer {
  private final RenderHelper renderHelper;
  private final Zone zone;
  private final ZoneView zoneView;

  public LightsRenderer(RenderHelper renderHelper, Zone zone, ZoneView zoneView) {
    this.renderHelper = renderHelper;
    this.zone = zone;
    this.zoneView = zoneView;
  }

  public void renderAuras(Graphics2D g2d, PlayerView view) {
    var timer = CodeTimer.get();
    timer.start("renderAuras");
    try {
      final var drawableAuras = zoneView.getDrawableAuras(view);
      if (drawableAuras.isEmpty()) {
        return;
      }

      final var lightBlending =
          AlphaComposite.SrcOver.derive(AppPreferences.auraOverlayOpacity.get() / 255.0f);
      final var overlayFillColor = new Color(0, 0, 0, 0);

      renderHelper.bufferedRender(
          g2d,
          AlphaComposite.SrcOver,
          worldG -> renderWorld(worldG, view, drawableAuras, lightBlending, overlayFillColor));
    } finally {
      timer.stop("renderAuras");
    }
  }

  public void renderLights(Graphics2D g2d, PlayerView view) {
    var timer = CodeTimer.get();
    timer.start("renderLights");
    try {
      if (!AppState.isShowLights()) {
        return;
      }

      final var drawableLights = zoneView.getDrawableLights(view);
      if (drawableLights.isEmpty()) {
        return;
      }

      final var overlayBlending =
          switch (zone.getLightingStyle()) {
            case OVERTOP ->
                AlphaComposite.SrcOver.derive(AppPreferences.lightOverlayOpacity.get() / 255.f);
            case ENVIRONMENTAL -> LightingComposite.OverlaidLights;
          };
      final var overlayFillColor =
          switch (zone.getLightingStyle()) {
            case OVERTOP -> new Color(0, 0, 0, 0);
            case ENVIRONMENTAL -> Color.black;
          };

      renderHelper.bufferedRender(
          g2d,
          overlayBlending,
          worldG ->
              renderWorld(
                  worldG, view, drawableLights, LightingComposite.BlendedLights, overlayFillColor));
    } finally {
      timer.stop("renderLights");
    }
  }

  private void renderWorld(
      Graphics2D worldG,
      PlayerView view,
      Iterable<DrawableLight> lights,
      Composite lightBlending,
      Color backgroundFill) {
    var timer = CodeTimer.get();

    var visibleArea = zoneView.getVisibleArea(view);

    var originalClip = worldG.getClip();
    var bounds = originalClip.getBounds();
    worldG.setComposite(AlphaComposite.Src);
    worldG.setPaint(backgroundFill);
    worldG.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

    if (!view.isGMView()) {
      timer.start("renderLightOverlay:setClip");
      var clip = new Area(originalClip);
      clip.intersect(visibleArea);
      worldG.setClip(clip);
      timer.stop("renderLightOverlay:setClip");
    }

    worldG.setComposite(lightBlending);

    // Draw lights onto the buffer image so the map doesn't affect how they blend
    timer.start("renderLightOverlay:drawLights");
    for (var light : lights) {
      worldG.setPaint(light.getPaint().getPaint());
      timer.start("renderLightOverlay:fillLight");
      worldG.fill(light.getArea());
      timer.stop("renderLightOverlay:fillLight");
    }
    timer.stop("renderLightOverlay:drawLights");
  }
}
