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
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

public final class TopologyTool<StateT> extends AbstractDrawingLikeTool {
  private final String instructionKey;
  private final String tooltipKey;
  private final boolean isFilled;
  private final Strategy<StateT> strategy;

  /** The current state of the tool. If {@code null}, nothing is being drawn right now. */
  private @Nullable StateT state;

  private ZonePoint currentPoint = new ZonePoint(0, 0);
  // Topology never supports center on origin right now, but it should in the future.
  private boolean centerOnOrigin;

  public TopologyTool(
      String instructionKey, String tooltipKey, boolean isFilled, Strategy<StateT> strategy) {
    this.instructionKey = instructionKey;
    this.tooltipKey = tooltipKey;
    this.isFilled = isFilled;
    this.strategy = strategy;
    // Consistency with topology tools before refactoring. Can be updated as part of #5002.
    this.centerOnOrigin = this.strategy instanceof OvalStrategy;
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

  private BasicStroke getLineStroke() {
    return new BasicStroke(2.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
  }

  private void submit(Shape shape) {
    Area area;
    if (shape instanceof Area tmpArea) {
      area = tmpArea;
    } else if (isFilled) {
      // Fill the shape without stroking.
      area = new Area(shape);
    } else {
      // Stroke the shape into an area.
      var stroke = getLineStroke();
      area = new Area(stroke.createStrokedShape(shape));
    }

    if (isEraser()) {
      getZone().removeTopology(area);
      MapTool.serverCommand().removeTopology(getZone().getId(), area, getZone().getTopologyTypes());
    } else {
      getZone().addTopology(area);
      MapTool.serverCommand().addTopology(getZone().getId(), area, getZone().getTopologyTypes());
    }
  }

  private Area getTokenTopology(Zone.TopologyType topologyType) {
    List<Token> topologyTokens = getZone().getTokensWithTopology(topologyType);

    Area tokenTopology = new Area();
    for (Token topologyToken : topologyTokens) {
      tokenTopology.add(topologyToken.getTransformedTopology(topologyType));
    }

    return tokenTopology;
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (!MapTool.getPlayer().isGM()) {
      // Redundant check since the tool should not be available otherwise.
      return;
    }

    Zone zone = renderer.getZone();

    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    g2.scale(renderer.getScale(), renderer.getScale());

    g2.setColor(AppStyle.tokenMblColor);
    g2.fill(getTokenTopology(Zone.TopologyType.MBL));
    g2.setColor(AppStyle.tokenTopologyColor);
    g2.fill(getTokenTopology(Zone.TopologyType.WALL_VBL));
    g2.setColor(AppStyle.tokenHillVblColor);
    g2.fill(getTokenTopology(Zone.TopologyType.HILL_VBL));
    g2.setColor(AppStyle.tokenPitVblColor);
    g2.fill(getTokenTopology(Zone.TopologyType.PIT_VBL));
    g2.setColor(AppStyle.tokenCoverVblColor);
    g2.fill(getTokenTopology(Zone.TopologyType.COVER_VBL));

    g2.setColor(AppStyle.topologyTerrainColor);
    g2.fill(zone.getTopology(Zone.TopologyType.MBL));

    g2.setColor(AppStyle.topologyColor);
    g2.fill(zone.getTopology(Zone.TopologyType.WALL_VBL));

    g2.setColor(AppStyle.hillVblColor);
    g2.fill(zone.getTopology(Zone.TopologyType.HILL_VBL));

    g2.setColor(AppStyle.pitVblColor);
    g2.fill(zone.getTopology(Zone.TopologyType.PIT_VBL));

    g2.setColor(AppStyle.coverVblColor);
    g2.fill(zone.getTopology(Zone.TopologyType.COVER_VBL));

    if (state != null) {
      var result = strategy.getShape(state, currentPoint, centerOnOrigin, false);
      if (result != null) {
        var stroke = getLineStroke();
        var color = isEraser() ? AppStyle.topologyRemoveColor : AppStyle.topologyAddColor;

        if (!isFilled || isLinearTool()) {
          // Render as a thick line.
          g2.setColor(color);
          g2.setStroke(stroke);
          g2.draw(result.shape());
        } else {
          // Render as an area with a thin border.
          g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
          g2.setStroke(
              new BasicStroke(
                  1 / (float) renderer.getScale(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
          g2.draw(result.shape());

          g2.setColor(color);
          g2.fill(result.shape());
        }
      }
    }

    g2.dispose();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (state == null) {
      // We're not doing anything, so delegate to default behaviour.
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    setIsEraser(isEraser(e));
    if (state != null) {
      currentPoint = getPoint(e);
      renderer.repaint();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    setIsEraser(isEraser(e));

    if (SwingUtilities.isLeftMouseButton(e)) {
      currentPoint = getPoint(e);

      if (state == null) {
        state = strategy.startNewAtPoint(currentPoint);
      } else {
        var result = strategy.getShape(state, currentPoint, centerOnOrigin, isFilled);
        state = null;
        if (result != null) {
          submit(result.shape());
        }
      }
      renderer.repaint();
    } else if (state != null) {
      currentPoint = getPoint(e);
      strategy.pushPoint(state, currentPoint);
      renderer.repaint();
    }

    super.mousePressed(e);
  }
}
