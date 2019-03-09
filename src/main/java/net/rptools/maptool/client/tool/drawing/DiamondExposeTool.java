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
package net.rptools.maptool.client.tool.drawing;

import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;

public class DiamondExposeTool extends DiamondTool {
  private static final long serialVersionUID = 2577704127916955192L;

  public DiamondExposeTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/fog-blue-diamond.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  public String getInstructions() {
    return "tool.rectexpose.instructions";
  }

  @Override
  // Override abstracttool to prevent color palette from
  // showing up
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    // Hide the drawable color palette
    MapTool.getFrame().hideControlPanel();
  }

  @Override
  protected boolean isBackgroundFill(MouseEvent e) {
    // Expose tools are implied to be filled
    return false;
  }

  @Override
  protected Pen getPen() {
    Pen pen = super.getPen();
    pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
    pen.setThickness(1);
    return pen;
  }

  @Override
  protected void completeDrawable(GUID zoneId, Pen pen, Drawable drawable) {
    if (!MapTool.getPlayer().isGM()) {
      MapTool.showError("msg.error.fogexpose");
      MapTool.getFrame().refresh();
      return;
    }
    Zone zone = MapTool.getCampaign().getZone(zoneId);
    Area area = new Area(drawable.getArea());
    Set<GUID> selectedToks = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
    if (pen.isEraser()) {
      zone.hideArea(area, selectedToks);
      MapTool.serverCommand().hideFoW(zone.getId(), area, selectedToks);
    } else {
      zone.exposeArea(area, selectedToks);
      MapTool.serverCommand().exposeFoW(zone.getId(), area, selectedToks);
    }
    MapTool.getFrame().refresh();
  }

  @Override
  public String getTooltip() {
    return "tool.diamondexpose.tooltip";
  }
}
