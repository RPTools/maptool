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
import net.rptools.maptool.model.drawing.Oval;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.util.GraphicsUtil;

public class HollowOvalTopologyTool extends AbstractDrawingTool implements MouseMotionListener {

  private static final long serialVersionUID = 3258413928311830325L;

  protected Oval oval;
  private ZonePoint originPoint;

  public HollowOvalTopologyTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/top-blue-hoval.png"))));
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
    return "tool.ovaltopology.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.ovaltopologyhollow.tooltip";
  }

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

    if (oval != null) {

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

      paintTransformed(g, renderer, oval, pen);
    }
  }

  public void mousePressed(MouseEvent e) {

    if (SwingUtilities.isLeftMouseButton(e)) {
      ZonePoint zp = getPoint(e);

      if (oval == null) {
        oval = new Oval(zp.x, zp.y, zp.x, zp.y);
        originPoint = zp;
      } else {
        oval.getEndPoint().x = zp.x;
        oval.getEndPoint().y = zp.y;

        Area area =
            GraphicsUtil.createLineSegmentEllipse(
                oval.getStartPoint().x,
                oval.getStartPoint().y,
                oval.getEndPoint().x,
                oval.getEndPoint().y,
                10);

        // Still use the whole area if it's an erase action
        if (!isEraser(e)) {
          int x1 = Math.min(oval.getStartPoint().x, oval.getEndPoint().x) + 2;
          int y1 = Math.min(oval.getStartPoint().y, oval.getEndPoint().y) + 2;

          int x2 = Math.max(oval.getStartPoint().x, oval.getEndPoint().x) - 2;
          int y2 = Math.max(oval.getStartPoint().y, oval.getEndPoint().y) - 2;

          Area innerArea = GraphicsUtil.createLineSegmentEllipse(x1, y1, x2, y2, 10);
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

        oval = null;
      }

      setIsEraser(isEraser(e));
    }

    super.mousePressed(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {

    if (oval == null) {
      super.mouseDragged(e);
    }
  }

  public void mouseMoved(MouseEvent e) {

    setIsEraser(isEraser(e));

    if (oval != null) {
      ZonePoint sp = getPoint(e);

      oval.getEndPoint().x = sp.x;
      oval.getEndPoint().y = sp.y;
      oval.getStartPoint().x = originPoint.x - (sp.x - originPoint.x);
      oval.getStartPoint().y = originPoint.y - (sp.y - originPoint.y);

      renderer.repaint();
    }
  }

  /** Stop drawing a rectangle and repaint the zone. */
  public void resetTool() {
    if (oval != null) {
      oval = null;
      renderer.repaint();
    } else {
      super.resetTool();
    }
  }
}
