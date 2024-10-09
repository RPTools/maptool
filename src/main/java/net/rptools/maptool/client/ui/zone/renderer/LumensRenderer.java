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
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.model.Zone;

/**
 * Draws a solid black overlay wherever a non-GM player should see darkness.
 *
 * <p>If the current view is a GM view, this renders nothing since darkness is rendered as a light
 * source for GMs.
 */
public class LumensRenderer {
  private final RenderHelper renderHelper;
  private final Zone zone;
  private final ZoneView zoneView;

  public LumensRenderer(RenderHelper renderHelper, Zone zone, ZoneView zoneView) {
    this.renderHelper = renderHelper;
    this.zone = zone;
    this.zoneView = zoneView;
  }

  public void render(Graphics2D g2d, PlayerView view) {
    var timer = CodeTimer.get();
    timer.start("renderLumensOverlay");
    try {
      if (!AppState.isShowLumensOverlay()) {
        return;
      }

      renderHelper.bufferedRender(g2d, AlphaComposite.SrcOver, worldG -> renderWorld(worldG, view));
    } finally {
      timer.stop("renderLumensOverlay");
    }
  }

  private void renderWorld(Graphics2D worldG, PlayerView view) {
    var timer = CodeTimer.get();
    var overlayOpacity = AppPreferences.lumensOverlayOpacity.get() / 255.0f;

    var visibleArea = zoneView.getVisibleArea(view);
    final var disjointLumensLevels = zoneView.getDisjointObscuredLumensLevels(view);

    var originalClip = worldG.getClip();
    var bounds = originalClip.getBounds();
    worldG.setComposite(AlphaComposite.Src.derive(overlayOpacity));
    // At night, show any uncovered areas as dark. In daylight, show them as light (clear).
    var backgroundFill =
        zone.getVisionType() == Zone.VisionType.NIGHT
            ? new Color(0.f, 0.f, 0.f, 1.f)
            : new Color(0.f, 0.f, 0.f, 0.f);
    worldG.setPaint(backgroundFill);
    worldG.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

    if (!view.isGMView()) {
      timer.start("renderLumensOverlay:setClip");
      Area clip = new Area(originalClip);
      clip.intersect(visibleArea);
      worldG.setClip(clip);
      timer.stop("renderLumensOverlay:setClip");
    }

    worldG.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));

    timer.start("renderLumensOverlay:drawLumens");
    for (final var lumensLevel : disjointLumensLevels) {
      final var lumensStrength = lumensLevel.lumensStrength();

      // Light is weaker than darkness, so do it first.
      float lightOpacity;
      float lightShade;
      if (lumensStrength == 0) {
        // This area represents daylight, so draw it as clear despite the low value.
        lightShade = 1.f;
        lightOpacity = 0;
      } else if (lumensStrength >= 100) {
        // Bright light, render mostly clear.
        lightShade = 1.f;
        lightOpacity = 1.f / 10.f;
      } else {
        lightShade = Math.max(0.f, Math.min(lumensStrength / 100.f, 1.f));
        lightShade *= lightShade;
        lightOpacity = 1.f;
      }

      timer.start("renderLumensOverlay:drawLights:fillArea");
      worldG.setPaint(new Color(lightShade, lightShade, lightShade, lightOpacity));
      worldG.fill(lumensLevel.lightArea());

      worldG.setPaint(new Color(0.f, 0.f, 0.f, 1.f));
      worldG.fill(lumensLevel.darknessArea());
      timer.stop("renderLumensOverlay:drawLights:fillArea");
    }

    // Now draw borders around each region if configured.
    final var borderThickness = AppPreferences.lumensOverlayBorderThickness.get();
    if (borderThickness > 0) {
      worldG.setStroke(
          new BasicStroke((float) borderThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      worldG.setComposite(AlphaComposite.SrcOver);
      worldG.setPaint(new Color(0.f, 0.f, 0.f, 1.f));
      for (final var lumensLevel : disjointLumensLevels) {
        timer.start("renderLumensOverlay:drawLights:drawArea");
        worldG.draw(lumensLevel.lightArea());
        worldG.draw(lumensLevel.darknessArea());
        timer.stop("renderLumensOverlay:drawLights:drawArea");
      }
    }

    timer.stop("renderLumensOverlay:drawLumens");
  }
}
