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
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Rectangle;

/**
 * @author drice
 */
public class RectangleTopologyTool extends AbstractDrawingTool implements MouseMotionListener {
  private static final long serialVersionUID = 3258413928311830323L;

  protected Rectangle rectangle;

  public RectangleTopologyTool() {}

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
  public String getInstructions() {
    return "tool.recttopology.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.recttopology.tooltip";
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    paintTopologyOverlay(g, rectangle);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      ZonePoint sp = getPoint(e);

      if (rectangle == null) {
        rectangle = new Rectangle(sp.x, sp.y, sp.x, sp.y);
      } else {
        rectangle.getEndPoint().x = sp.x;
        rectangle.getEndPoint().y = sp.y;

        int x1 = Math.min(rectangle.getStartPoint().x, rectangle.getEndPoint().x);
        int x2 = Math.max(rectangle.getStartPoint().x, rectangle.getEndPoint().x);
        int y1 = Math.min(rectangle.getStartPoint().y, rectangle.getEndPoint().y);
        int y2 = Math.max(rectangle.getStartPoint().y, rectangle.getEndPoint().y);

        Area area = new Area(new java.awt.Rectangle(x1, y1, x2 - x1, y2 - y1));
        if (isEraser(e)) {
          getZone().removeTopology(area);
          MapTool.serverCommand()
              .removeTopology(getZone().getId(), area, getZone().getTopologyTypes());
        } else {
          getZone().addTopology(area);
          MapTool.serverCommand()
              .addTopology(getZone().getId(), area, getZone().getTopologyTypes());
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
      rectangle.getEndPoint().x = p.x;
      rectangle.getEndPoint().y = p.y;
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
