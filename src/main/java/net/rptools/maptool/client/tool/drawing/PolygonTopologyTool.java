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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/** Tool for drawing freehand lines. */
public class PolygonTopologyTool extends LineTool implements MouseMotionListener {

  private static final long serialVersionUID = 3258132466219627316L;

  public PolygonTopologyTool() {}

  @Override
  // Override abstracttool to prevent color palette from
  // showing up
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    // Hide the drawable color palette
    MapTool.getFrame().removeControlPanel();
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected boolean drawMeasurement() {
    return false;
  }

  @Override
  public String getTooltip() {
    return "tool.polytopo.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.poly.instructions";
  }

  @Override
  protected boolean isBackgroundFill(MouseEvent e) {
    return true;
  }

  @Override
  protected void completeDrawable(GUID zoneGUID, Pen pen, Drawable drawable) {
    Area area = new Area();

    if (drawable instanceof LineSegment) {
      LineSegment line = (LineSegment) drawable;
      BasicStroke stroke =
          new BasicStroke(pen.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

      Path2D path = new Path2D.Double();
      Point lastPoint = null;

      for (Point point : line.getPoints()) {
        if (path.getCurrentPoint() == null) {
          path.moveTo(point.x, point.y);
        } else if (!point.equals(lastPoint)) {
          path.lineTo(point.x, point.y);
          lastPoint = point;
        }
      }

      area.add(new Area(stroke.createStrokedShape(path)));
    } else {
      area = new Area(((ShapeDrawable) drawable).getShape());
    }
    if (pen.isEraser()) {
      getZone().removeTopology(area);
      MapTool.serverCommand().removeTopology(getZone().getId(), area, getZone().getTopologyTypes());
    } else {
      getZone().addTopology(area);
      MapTool.serverCommand().addTopology(getZone().getId(), area, getZone().getTopologyTypes());
    }
    renderer.repaint();
  }

  @Override
  protected Pen getPen() {
    Pen pen = new Pen(MapTool.getFrame().getPen());
    pen.setEraser(isEraser());
    pen.setForegroundMode(Pen.MODE_TRANSPARENT);
    pen.setBackgroundMode(Pen.MODE_SOLID);
    pen.setThickness(1.0f);
    pen.setOpacity(AppStyle.topologyRemoveColor.getAlpha() / 255.0f);
    pen.setPaint(
        new DrawableColorPaint(
            isEraser() ? AppStyle.topologyRemoveColor : AppStyle.topologyAddColor));
    return pen;
  }

  @Override
  protected Polygon getPolygon(LineSegment line) {
    Polygon polygon = new Polygon();
    for (Point point : line.getPoints()) {
      polygon.addPoint(point.x, point.y);
    }
    return polygon;
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    paintTopologyOverlay(g);
    super.paintOverlay(renderer, g);
  }
}
