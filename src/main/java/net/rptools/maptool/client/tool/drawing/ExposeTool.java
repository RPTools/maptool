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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.Set;
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

public final class ExposeTool<StateT> extends AbstractDrawingLikeTool {
  private final String instructionKey;
  private final String tooltipKey;
  private final Strategy<StateT> strategy;

  /** The current state of the tool. If {@code null}, nothing is being drawn right now. */
  private @Nullable StateT state;

  private ZonePoint currentPoint = new ZonePoint(0, 0);
  private boolean centerOnOrigin = false;

  public ExposeTool(String instructionKey, String tooltipKey, Strategy<StateT> strategy) {
    this.instructionKey = instructionKey;
    this.tooltipKey = tooltipKey;
    this.strategy = strategy;
  }

  @Override
  public String getInstructions() {
    return instructionKey;
  }

  @Override
  public String getTooltip() {
    return tooltipKey;
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected boolean isLinearTool() {
    return strategy.isLinear();
  }

  /** If currently drawing, stop and clear it. */
  @Override
  protected void resetTool() {
    if (state != null) {
      state = null;
      renderer.repaint();
    } else {
      super.resetTool();
    }
  }

  private void submit(Shape shape) {
    if (!MapTool.getPlayer().isGM()) {
      MapTool.showError("msg.error.fogexpose");
      MapTool.getFrame().refresh();
      return;
    }

    Area area;
    if (shape instanceof Area tmpArea) {
      area = tmpArea;
    } else {
      // Fill the shape.
      area = new Area(shape);
    }

    Zone zone = getZone();
    Set<GUID> selectedToks = renderer.getSelectedTokenSet();

    if (isEraser()) {
      zone.hideArea(area, selectedToks);
      MapTool.serverCommand().hideFoW(zone.getId(), area, selectedToks);
    } else {
      MapTool.serverCommand().exposeFoW(zone.getId(), area, selectedToks);
    }
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    g2.scale(renderer.getScale(), renderer.getScale());

    if (state != null) {
      var result = strategy.getShape(state, currentPoint, centerOnOrigin, false);
      if (result != null) {
        var color = isEraser() ? Color.white : Color.black;

        if (!isLinearTool()) {
          // Render the interior for better user feedback.
          g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
          g2.fill(result.shape());
        }

        // Render the line.
        g2.setColor(color);
        g2.setStroke(
            new BasicStroke(
                1 / (float) renderer.getScale(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.draw(result.shape());

        // Measurements
        drawMeasurementOverlay(renderer, g, result.measurement());
      }
    }

    g2.dispose();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (state == null) {
      // We're not doing anything, so delegate to default behaviour.
      super.mouseDragged(e);
    } else if (strategy.isFreehand()) {
      // Extend the line.
      setIsEraser(isEraser(e));
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown(); // Pointless, but it doesn't hurt for consistency.
      strategy.pushPoint(state, currentPoint);
      renderer.repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    setIsEraser(isEraser(e));
    if (state != null) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();
      renderer.repaint();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    setIsEraser(isEraser(e));

    if (SwingUtilities.isLeftMouseButton(e)) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();

      if (state == null) {
        state = strategy.startNewAtPoint(currentPoint);
      } else if (!strategy.isFreehand()) {
        var result = strategy.getShape(state, currentPoint, centerOnOrigin, true);
        state = null;
        if (result != null) {
          submit(result.shape());
        }
      }
      renderer.repaint();
    } else if (state != null && !strategy.isFreehand()) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();
      strategy.pushPoint(state, currentPoint);
      renderer.repaint();
    }

    super.mousePressed(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (strategy.isFreehand() && SwingUtilities.isLeftMouseButton(e)) {
      currentPoint = getPoint(e);
      centerOnOrigin = e.isAltDown();
      var result = strategy.getShape(state, currentPoint, centerOnOrigin, true);
      state = null;
      if (result != null) {
        submit(result.shape());
      }
    }
  }
}
