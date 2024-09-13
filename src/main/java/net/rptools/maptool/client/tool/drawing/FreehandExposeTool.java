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
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/** Tool for drawing freehand lines. */
public class FreehandExposeTool extends FreehandTool implements MouseMotionListener {
  private static final long serialVersionUID = 3258132466219627316L;

  public FreehandExposeTool() {}

  @Override
  public String getTooltip() {
    return "tool.freehandexpose.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.freehandexpose.instructions";
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected Pen getPen() {
    Pen pen = super.getPen();
    pen.setThickness(1);
    return pen;
  }

  @Override
  // Override abstracttool to prevent color palette from
  // showing up
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    // Hide the drawable color palette
    MapTool.getFrame().removeControlPanel();
  }

  @Override
  protected boolean isBackgroundFill(MouseEvent e) {
    // Expose tools are implied to be filled
    return false;
  }

  @Override
  protected void stopLine(MouseEvent e) {
    LineSegment line = getLine();

    if (line == null) return; // Escape has been pressed
    addPoint(e);
    completeDrawable(renderer.getZone().getId(), getPen(), line);
    resetTool();
  }

  @Override
  protected void completeDrawable(GUID zoneId, Pen pen, Drawable drawable) {
    if (!MapTool.getPlayer().isGM()) {
      MapTool.showError("msg.error.fogexpose");
      MapTool.getFrame().refresh();
      return;
    }
    Zone zone = MapTool.getCampaign().getZone(zoneId);

    Area area = null;
    if (drawable instanceof LineSegment) {
      area = new Area(getPolygon((LineSegment) drawable));
    }
    if (drawable instanceof ShapeDrawable) {
      area = new Area(((ShapeDrawable) drawable).getShape());
    }
    Set<GUID> selectedToks = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
    if (pen.isEraser()) {
      zone.hideArea(area, selectedToks);
      MapTool.serverCommand().hideFoW(zone.getId(), area, selectedToks);
    } else {
      MapTool.serverCommand().exposeFoW(zone.getId(), area, selectedToks);
    }
    MapTool.getFrame().refresh();
  }
}
