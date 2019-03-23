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

import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;

/** Tool for drawing freehand lines. */
public class PolyLineTopologyTool extends PolygonTopologyTool implements MouseMotionListener {
  private static final long serialVersionUID = 3258132466219627316L;

  public PolyLineTopologyTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/top-blue-free.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public String getTooltip() {
    return "tool.polylinetopo.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.poly.instructions";
  }

  protected boolean isBackgroundFill(MouseEvent e) {
    return false;
  }

  protected Pen getPen() {

    Pen pen = new Pen(MapTool.getFrame().getPen());
    pen.setEraser(isEraser());
    pen.setForegroundMode(Pen.MODE_SOLID);
    pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
    pen.setThickness(2.0f);
    pen.setOpacity(AppStyle.topologyRemoveColor.getAlpha() / 255.0f);
    pen.setPaint(
        new DrawableColorPaint(
            isEraser() ? AppStyle.topologyRemoveColor : AppStyle.topologyAddColor));

    return pen;
  }

  protected Polygon getPolygon(LineSegment line) {
    Polygon polygon = new Polygon();
    for (Point point : line.getPoints()) {
      polygon.addPoint(point.x, point.y);
    }

    return polygon;
  }
}
