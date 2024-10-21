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

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;

public abstract class AbstractDrawingLikeTool extends DefaultTool implements ZoneOverlay {
  private boolean isEraser;

  protected void setIsEraser(boolean eraser) {
    isEraser = eraser;
  }

  protected boolean isEraser() {
    return isEraser;
  }

  protected boolean isEraser(MouseEvent e) {
    return SwingUtil.isShiftDown(e);
  }

  protected boolean isSnapToGrid(MouseEvent e) {
    return SwingUtil.isControlDown(e);
  }

  protected boolean isSnapToCenter(MouseEvent e) {
    return e.isAltDown();
  }

  protected boolean isLinearTool() {
    return false;
  }

  protected ZonePoint getPoint(MouseEvent e) {
    ScreenPoint sp = new ScreenPoint(e.getX(), e.getY());
    ZonePoint zp = sp.convertToZoneRnd(renderer);
    if (isSnapToCenter(e) && isLinearTool()) {
      // Only line tools will snap to center as the Alt key for rectangle, diamond and oval
      // is used for expand from center.
      zp = renderer.getCellCenterAt(sp);
    } else if (isSnapToGrid(e)) {
      zp = renderer.getZone().getNearestVertex(zp);
    }
    return zp;
  }

  /** Draws the shape measurement as part of the overlay. */
  protected void drawMeasurementOverlay(
      ZoneRenderer renderer, Graphics2D g, Measurement measurement) {
    switch (measurement) {
      case null -> {}
      case Measurement.Rectangular rectangular -> {
        var rectangle = rectangular.bounds();
        ToolHelper.drawBoxedMeasurement(
            renderer,
            g,
            ScreenPoint.fromZonePoint(renderer, rectangle.getX(), rectangle.getY()),
            ScreenPoint.fromZonePoint(renderer, rectangle.getMaxX(), rectangle.getMaxY()));
      }
      case Measurement.LineSegment lineSegment -> {
        var p1 =
            ScreenPoint.fromZonePoint(renderer, lineSegment.p1().getX(), lineSegment.p1().getY());
        var p2 =
            ScreenPoint.fromZonePoint(renderer, lineSegment.p2().getX(), lineSegment.p2().getY());
        ToolHelper.drawMeasurement(renderer, g, p1, p2);
      }
      case Measurement.IsoRectangular isoRectangular -> {
        var north =
            ScreenPoint.fromZonePoint(
                renderer, isoRectangular.north().getX(), isoRectangular.north().getY());
        var west =
            ScreenPoint.fromZonePoint(
                renderer, isoRectangular.west().getX(), isoRectangular.west().getY());
        var east =
            ScreenPoint.fromZonePoint(
                renderer, isoRectangular.east().getX(), isoRectangular.east().getY());
        ToolHelper.drawIsoRectangleMeasurement(renderer, g, north, west, east);
      }
    }
  }
}
