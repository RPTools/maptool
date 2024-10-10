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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.List;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

/**
 * This outlines the area visible to the token under the cursor. For player views, this is clipped
 * to the current fog-of-war, while for GMs they see everything.
 */
public class VisionOverlayRenderer {
  private final RenderHelper renderHelper;
  private final Zone zone;
  private final ZoneView zoneView;

  public VisionOverlayRenderer(RenderHelper renderHelper, Zone zone, ZoneView zoneView) {
    this.renderHelper = renderHelper;
    this.zone = zone;
    this.zoneView = zoneView;
  }

  public void render(Graphics2D g, PlayerView view, Token tokenUnderMouse) {
    var timer = CodeTimer.get();
    timer.start("renderVisionOverlay");
    try {
      if (tokenUnderMouse == null) {
        return;
      }

      boolean isOwner = AppUtil.playerOwns(tokenUnderMouse);
      boolean tokenIsPC = tokenUnderMouse.getType() == Token.Type.PC;
      boolean strictOwnership =
          MapTool.getServerPolicy() != null && MapTool.getServerPolicy().useStrictTokenManagement();
      boolean showVisionAndHalo = isOwner || view.isGMView() || (tokenIsPC && !strictOwnership);
      if (!showVisionAndHalo) {
        return;
      }

      this.renderHelper.render(g, worldG -> renderWorld(worldG, view, tokenUnderMouse));
    } finally {
      timer.stop("renderVisionOverlay");
    }
  }

  private void renderWorld(Graphics2D worldG, PlayerView view, Token token) {
    // The vision of the token is not necessarily related to the current view.
    final var tokenView = new PlayerView(view.getRole(), List.of(token));

    Area currentTokenVisionArea = zoneView.getVisibleArea(token, tokenView);
    // Nothing to show.
    if (currentTokenVisionArea.isEmpty()) {
      return;
    }
    if (zone.hasFog()) {
      currentTokenVisionArea = new Area(currentTokenVisionArea);
      currentTokenVisionArea.intersect(zoneView.getExposedArea(tokenView));
    }

    // Keep the line a consistent thickness
    worldG.setStroke(new BasicStroke(1 / (float) worldG.getTransform().getScaleX()));
    worldG.setColor(new Color(255, 255, 255)); // outline around visible area
    worldG.draw(currentTokenVisionArea);

    Color visionColor = token.getVisionOverlayColor();
    if (visionColor == null && AppPreferences.useHaloColorOnVisionOverlay.get()) {
      visionColor = token.getHaloColor();
    }
    if (visionColor != null) {
      worldG.setColor(
          new Color(
              visionColor.getRed(),
              visionColor.getGreen(),
              visionColor.getBlue(),
              AppPreferences.haloOverlayOpacity.get()));
      worldG.fill(currentTokenVisionArea);
    }
  }
}
