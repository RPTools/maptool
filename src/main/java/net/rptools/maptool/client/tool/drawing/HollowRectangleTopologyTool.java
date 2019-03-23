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
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.Rectangle;

/** @author drice */
public class HollowRectangleTopologyTool extends AbstractDrawingTool
    implements MouseMotionListener {
  private static final long serialVersionUID = 3258413928311830323L;

  protected Rectangle rectangle;

  public HollowRectangleTopologyTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/top-blue-hrect.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
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
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  public String getInstructions() {
    return "tool.recttopology.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.recttopologyhollow.tooltip";
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    // Color oldColor = g.getColor();

    if (MapTool.getPlayer().isGM()) {
      Zone zone = renderer.getZone();
      Area topology = zone.getTopology();

      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
      g2.scale(renderer.getScale(), renderer.getScale());

      g2.setColor(AppStyle.tokenTopologyColor);
      g2.fill(getTokenTopology());

      g2.setColor(AppStyle.topologyColor);
      g2.fill(topology);

      g2.dispose();
    }
    if (rectangle != null) {
      Pen pen = new Pen();
      pen.setEraser(getPen().isEraser());
      pen.setOpacity(AppStyle.topologyRemoveColor.getAlpha() / 255.0f);
      pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
      pen.setThickness(3.0f);

      if (pen.isEraser()) {
        pen.setEraser(false);
      }
      if (isEraser()) {
        pen.setPaint(new DrawableColorPaint(AppStyle.topologyRemoveColor));
      } else {
        pen.setPaint(new DrawableColorPaint(AppStyle.topologyAddColor));
      }
      paintTransformed(g, renderer, rectangle, pen);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    ZonePoint zp = getPoint(e);

    if (SwingUtilities.isLeftMouseButton(e)) {
      if (rectangle == null) {
        rectangle = new Rectangle(zp.x, zp.y, zp.x, zp.y);
      } else {
        rectangle.getEndPoint().x = zp.x;
        rectangle.getEndPoint().y = zp.y;

        int x1 = Math.min(rectangle.getStartPoint().x, rectangle.getEndPoint().x);
        int x2 = Math.max(rectangle.getStartPoint().x, rectangle.getEndPoint().x);
        int y1 = Math.min(rectangle.getStartPoint().y, rectangle.getEndPoint().y);
        int y2 = Math.max(rectangle.getStartPoint().y, rectangle.getEndPoint().y);

        Area area = new Area(new java.awt.Rectangle(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2));

        // Still use the whole area if it's an erase action
        if (!isEraser(e)) {
          Area innerArea =
              new Area(new java.awt.Rectangle(x1 + 1, y1 + 1, x2 - x1 - 2, y2 - y1 - 2));
          area.subtract(innerArea);
        }
        if (isEraser(e)) {
          renderer.getZone().removeTopology(area);
          MapTool.serverCommand().removeTopology(renderer.getZone().getId(), area);
        } else {
          renderer.getZone().addTopology(area);
          MapTool.serverCommand().addTopology(renderer.getZone().getId(), area);
        }
        renderer.repaint();
        // TODO: send this to the server
        rectangle = null;
      }
      setIsEraser(isEraser(e));
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (rectangle == null) {
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    setIsEraser(isEraser(e));

    ZonePoint p = getPoint(e);
    if (rectangle != null) {
      if (rectangle != null) {
        rectangle.getEndPoint().x = p.x;
        rectangle.getEndPoint().y = p.y;
      }
      renderer.repaint();
    }
  }

  /** Stop drawing a rectangle and repaint the zone. */
  @Override
  public void resetTool() {
    if (rectangle != null) {
      rectangle = null;
      renderer.repaint();
    } else {
      super.resetTool();
    }
  }
}
