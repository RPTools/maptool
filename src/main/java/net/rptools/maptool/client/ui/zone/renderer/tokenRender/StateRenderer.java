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
import java.awt.geom.Rectangle2D;
import java.util.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.token.*;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.renderer.RenderHelper;
import net.rptools.maptool.model.Zone;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class StateRenderer {
  private static final Logger log = LogManager.getLogger(StateRenderer.class);
  private final RenderHelper renderHelper;
  private final Zone zone;
  private final Map<String, BarTokenOverlay> barMap =
      Collections.synchronizedMap(MapTool.getCampaign().getTokenBarsMap());
  private final Map<String, BooleanTokenOverlay> stateMap =
      Collections.synchronizedMap(MapTool.getCampaign().getTokenStatesMap());
  private Set<String> overlayNames;
  private boolean isPaintBars = false;

  public StateRenderer(RenderHelper renderHelper, Zone zone) {
    this.renderHelper = renderHelper;
    this.zone = zone;
  }

  public void renderStates(
      ZoneViewModel viewModel,
      ZoneViewModel.TokenPosition position,
      Graphics2D g2d,
      boolean selected,
      boolean hover) {
    isPaintBars = false;
    synchronized (stateMap) {
      overlayNames = stateMap.keySet();
    }
    renderOverlay(viewModel, position, g2d, selected, hover);
  }

  public void renderBars(
      ZoneViewModel viewModel,
      ZoneViewModel.TokenPosition position,
      Graphics2D g2d,
      boolean selected,
      boolean hover) {
    isPaintBars = true;
    synchronized (barMap) {
      overlayNames = barMap.keySet();
    }
    renderOverlay(viewModel, position, g2d, selected, hover);
  }

  @SuppressWarnings("unused")
  public void renderOverlay(
      ZoneViewModel viewModel,
      ZoneViewModel.TokenPosition position,
      Graphics2D g2d,
      boolean selected,
      boolean hover) {
    Rectangle2D tokenBounds =
        viewModel.getZoneScale().toScreenSpace(position.footprintBounds().getBounds2D());
    Rectangle bounds =
        new Rectangle(0, 0, (int) tokenBounds.getWidth(), (int) tokenBounds.getHeight());
    Graphics2D overlayG =
        (Graphics2D)
            g2d.create(
                (int) tokenBounds.getX(),
                (int) tokenBounds.getY(),
                (int) tokenBounds.getWidth(),
                (int) tokenBounds.getHeight());
    overlayG.setClip(null);

    Composite oldComposite = g2d.getComposite();
    float alpha = 1f;
    if (oldComposite instanceof AlphaComposite alphaComposite) {
      alpha = alphaComposite.getAlpha();
    }

    // Check each of the set values
    for (String name : overlayNames) {
      AbstractTokenOverlay overlay = isPaintBars ? barMap.get(name) : stateMap.get(name);
      Object value = position.token().getState(name);
      if (overlay == null
          || overlay.isMouseover() && hover
          || !overlay.showPlayer(position.token(), MapTool.getPlayer())) {
        continue;
      }
      overlayG.setComposite(
          AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER, alpha * (float) overlay.getOpacity() / 100));

      overlay.paintOverlay(overlayG, position.token(), bounds, value);
    }
    overlayG.dispose();
  }
}
