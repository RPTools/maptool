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
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DiamondTool extends AbstractDrawingTool implements MouseMotionListener {

  private static final long serialVersionUID = 8239333601131612106L;
  protected Shape diamond;
  protected ZonePoint originPoint;

  public DiamondTool() {}

  @Override
  public String getInstructions() {
    return "tool.rect.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.isorectangle.tooltip";
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (diamond != null) {
      Pen pen = getPen();
      if (pen.isEraser()) {
        pen = new Pen(pen);
        pen.setEraser(false);
        pen.setPaint(new DrawableColorPaint(Color.white));
        pen.setBackgroundPaint(new DrawableColorPaint(Color.white));
      }
      paintTransformed(g, renderer, new ShapeDrawable(diamond, false), pen);
      ToolHelper.drawDiamondMeasurement(renderer, g, diamond);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    ZonePoint zp = getPoint(e);
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (diamond == null) {
        originPoint = zp;
        diamond = createDiamond(originPoint, originPoint);
      } else {
        diamond = createDiamond(originPoint, zp);

        if (diamond.getBounds().width == 0 || diamond.getBounds().height == 0) {
          diamond = null;
          renderer.repaint();
          return;
        }
        // ToolHelper.drawDiamondMeasurement(renderer, null, diamond);
        completeDrawable(renderer.getZone().getId(), getPen(), new ShapeDrawable(diamond, false));
        diamond = null;
      }
      setIsEraser(isEraser(e));
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (diamond == null) {
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);

    if (diamond != null) {
      ZonePoint p = getPoint(e);
      diamond = createDiamond(originPoint, p);
      renderer.repaint();
    }
  }

  /** Stop drawing a rectangle and repaint the zone. */
  @Override
  public void resetTool() {
    if (diamond != null) {
      diamond = null;
      originPoint = null;
      renderer.repaint();
    } else {
      super.resetTool();
    }
  }
}
