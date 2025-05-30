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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.ImageBorder;
import net.rptools.maptool.client.tool.drawing.ExposeTool;
import net.rptools.maptool.client.ui.theme.Borders;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.model.Token;

public class SelectionRenderer {
  private final RenderHelper renderHelper;
  private final ZoneViewModel viewModel;
  private final ZoneView zoneView;

  public SelectionRenderer(RenderHelper renderHelper, ZoneViewModel viewModel, ZoneView zoneView) {
    this.renderHelper = renderHelper;
    this.viewModel = viewModel;
    this.zoneView = zoneView;
  }

  public void drawSelectBorder(Graphics2D g, ZoneViewModel.TokenPosition position) {
    g = (Graphics2D) g.create();

    var token = position.token();

    var zoneScale = this.viewModel.getZoneScale();
    var scale = this.viewModel.getZoneScale().getScale();

    var footprint = position.footprintBounds();
    ScreenPoint sp =
        zoneScale.toScreenSpace(new Point2D.Double(footprint.getX(), footprint.getY()));

    final ImageBorder selectedBorder;

    if (MapTool.getServerPolicy().isUseIndividualFOW()
        && token.getLayer().supportsVision()
        && zoneView.isUsingVision()
        && MapTool.getFrame().getToolbox().getSelectedTool() instanceof ExposeTool<?>) {
      selectedBorder = RessourceManager.getBorder(Borders.FOW_TOOLS);
    } else if (!AppUtil.playerOwns(token)) {
      selectedBorder = AppStyle.selectedUnownedBorder;
    } else if (viewModel.getHighlightCommonMacros().contains(token.getId())) {
      selectedBorder = AppStyle.commonMacroBorder;
    } else if (token.getLayer().isStampLayer()) {
      selectedBorder = AppStyle.selectedStampBorder;
    } else {
      selectedBorder = AppStyle.selectedBorder;
    }

    g.translate(sp.x, sp.y);
    if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
      // Rotated
      // facing defaults to down, or -90  degrees
      g.rotate(
          Math.toRadians(token.getFacingInDegrees()),
          (footprint.getWidth() / 2. - token.getAnchorX()) * scale,
          (footprint.getHeight() / 2. - token.getAnchorY()) * scale);
    }
    selectedBorder.paintAround(
        g, 0, 0, (int) (footprint.getWidth() * scale), (int) (footprint.getHeight() * scale));
  }
}
