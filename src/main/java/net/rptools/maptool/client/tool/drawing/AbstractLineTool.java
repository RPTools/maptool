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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppPreferences.RenderQuality;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/** Tool for drawing freehand lines. */
public abstract class AbstractLineTool extends AbstractDrawingTool {
  private int currentX;
  private int currentY;

  private LineSegment line;
  protected boolean drawMeasurementDisabled;

  protected int getCurrentX() {
    return currentX;
  }

  protected int getCurrentY() {
    return currentY;
  }

  protected LineSegment getLine() {
    return this.line;
  }

  protected void startLine(MouseEvent e) {
    line = new LineSegment(getPen().getThickness(), getPen().getSquareCap());
    addPoint(e);
  }

  protected Point addPoint(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      return null;
    }
    ZonePoint zp = getPoint(e);

    if (line == null) return null; // Escape has been pressed
    Point ret = new Point(zp.x, zp.y);

    line.getPoints().add(ret);
    currentX = zp.x;
    currentY = zp.y;

    renderer.repaint();
    return ret;
  }

  protected void removePoint(Point p) {
    if (line == null) return; // Escape has been pressed

    // Remove most recently added
    // TODO: optimize this
    Collections.reverse(line.getPoints());
    line.getPoints().remove(p);
    Collections.reverse(line.getPoints());
  }

  protected void stopLine(MouseEvent e) {
    if (line == null) return; // Escape has been pressed
    addPoint(e);

    LineSegment trimLine = getTrimLine(line);
    Drawable drawable = trimLine;
    if (isBackgroundFill(e) && line.getPoints().size() > 2) {
      drawable = new ShapeDrawable(getPolygon(trimLine));
    }
    completeDrawable(renderer.getZone().getId(), getPen(), drawable);

    line = null;
    currentX = -1;
    currentY = -1;
  }

  protected Polygon getPolygon(LineSegment line) {
    Polygon polygon = new Polygon();
    for (Point point : line.getPoints()) {
      polygon.addPoint(point.x, point.y);
    }
    return polygon;
  }

  /**
   * Due to mouse movement, a user drawn line often has duplicated points, especially at the end. To
   * draw a clean line with miter joints these duplicates should be removed.
   *
   * @param line the {@link LineSegment} to trim.
   * @return the trimmed {@link LineSegment}.
   */
  protected LineSegment getTrimLine(LineSegment line) {
    LineSegment newLine = new LineSegment(line.getWidth(), line.isSquareCap());
    Point lastPoint = null;
    for (Point point : line.getPoints()) {
      if (!point.equals(lastPoint)) newLine.getPoints().add(point);
      lastPoint = point;
    }
    return newLine;
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (line != null) {
      // For the line currently being drawn we are more concerned with speed of prettiness
      var og = (Graphics2D) g.create();
      RenderQuality.LOW_SCALING.setRenderingHints(og);
      Pen pen = getPen();
      pen.setForegroundMode(Pen.MODE_SOLID);

      if (pen.isEraser()) {
        pen = new Pen(pen);
        pen.setEraser(false);
        pen.setPaint(new DrawableColorPaint(Color.white));
      }
      paintTransformed(og, renderer, line, pen);

      List<Point> pointList = line.getPoints();
      if (!drawMeasurementDisabled && pointList.size() > 1 && drawMeasurement()) {

        Point start = pointList.get(pointList.size() - 2);
        Point end = pointList.get(pointList.size() - 1);

        ScreenPoint sp = ScreenPoint.fromZonePoint(renderer, start.x, start.y);
        ScreenPoint ep = ScreenPoint.fromZonePoint(renderer, end.x, end.y);

        // ep.y -= 15;

        ToolHelper.drawMeasurement(renderer, og, sp, ep);
      }
      og.dispose();
    }
  }

  protected boolean drawMeasurement() {
    return true;
  }

  /**
   * @see Tool#resetTool()
   */
  @Override
  protected void resetTool() {
    if (line != null) {
      line = null;
      currentX = -1;
      currentY = -1;
      renderer.repaint();
    } else {
      super.resetTool();
    }
  }
}
