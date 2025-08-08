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
package net.rptools.maptool.client.ui.zone.renderer.tokenRender;

import java.awt.*;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.renderer.HaloRenderer;
import net.rptools.maptool.client.ui.zone.renderer.RenderHelper;
import net.rptools.maptool.model.Zone;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TokenDecorationRenderer {
  /* Render order by increasing z-index
          AURA,
          HALO,
          TOKEN,
          STATE,
          BAR,
          FACING,
          LABEL;
  */
  private static final Logger log = LogManager.getLogger(TokenDecorationRenderer.class);
  private final RenderHelper renderHelper;
  private final Zone zone;
  private final FacingArrowRenderer FACING_ARROW_RENDERER;
  private final HaloRenderer HALO_RENDERER;
  private final StateRenderer OVERLAY_RENDERER;

  public TokenDecorationRenderer(RenderHelper renderHelper, Zone zone) {
    this.renderHelper = renderHelper;
    this.zone = zone;
    FACING_ARROW_RENDERER = new FacingArrowRenderer(renderHelper, zone);
    HALO_RENDERER = new HaloRenderer(renderHelper, zone);
    OVERLAY_RENDERER = new StateRenderer(renderHelper, zone);
  }

  public void renderDecorations(
      boolean under,
      ZoneViewModel viewModel,
      ZoneViewModel.TokenPosition position,
      Graphics2D g2d,
      boolean selected,
      boolean moving,
      boolean hover) {
    var timer = CodeTimer.get();
    Composite oldComposite = g2d.getComposite();
    if (hover || selected && !moving) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    } else {
      g2d.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, position.token().getTokenOpacity()));
    }
    timer.increment("TokenDecorationRenderer-render");
    if (under) {
      timer.start("TokenDecorationRenderer-renderUnder");
      // paint Halo
      renderHelper.render(g2d, worldG -> HALO_RENDERER.renderHalo(worldG, position));
      // paint TOKEN
      timer.stop("TokenDecorationRenderer-renderUnder");
    } else {
      timer.start("TokenDecorationRenderer-renderOver");
      if (!moving) {
        // paint STATE
        OVERLAY_RENDERER.renderStates(viewModel, position, g2d, selected, hover);
        // paint BAR
        OVERLAY_RENDERER.renderBars(viewModel, position, g2d, selected, hover);
      }
      // paint FACING
      FACING_ARROW_RENDERER.paintArrow(g2d, position);
      // paint LABEL
      // Not yet implemented;
      timer.stop("TokenDecorationRenderer-renderOver");
    }
    g2d.setComposite(oldComposite);
  }
}
