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
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import net.rptools.maptool.client.AppStatePersisted;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.TopologyModeSelectionPanel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.ZonePoint;

/**
 * A freehand drawing tool for Vision Blocking Layer (VBL) and other topology types.
 *
 * <p><b>Design:</b> Applies changes incrementally during {@code mouseDragged} by submitting small
 * {@link Area} segments (circles connected by thick lines) to the server. This provides real-time
 * responsiveness without accumulating large paths.
 *
 * <p><b>Integration:</b> Uses a custom {@link RadiusPanel} for thickness and integrates with {@link
 * TopologyModeSelectionPanel} for layer selection.
 *
 * <p><b>Interactions:</b>
 * <ul>
 *   <li>LClick+Drag: Incremental draw/erase.
 *   <li>Shift: Toggle Eraser mode (real-time indicator update).
 *   <li>Ctrl+Wheel: Dynamic radius adjustment.
 * </ul>
 */
public final class VBLPenTool extends AbstractDrawingLikeTool {
  private final TopologyModeSelectionPanel topologyModeSelectionPanel;
  private final TopologyTool.MaskOverlay maskOverlay;
  private static final RadiusPanel RADIUS_PANEL = new RadiusPanel();

  private ZonePoint lastPoint;
  private boolean shiftDown;

  public VBLPenTool(TopologyModeSelectionPanel modePanel) {
    super("tool.vblpen.instructions", "tool.vblpen.tooltip");
    topologyModeSelectionPanel = modePanel;
    maskOverlay = new TopologyTool.MaskOverlay();
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    topologyModeSelectionPanel.setEnabled(true);
    MapTool.getFrame().showControlPanel(RADIUS_PANEL);
    super.attachTo(renderer);
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    topologyModeSelectionPanel.setEnabled(false);
    MapTool.getFrame().removeControlPanel();
    super.detachFrom(renderer);
  }

  @Override
  protected void resetTool() {
    lastPoint = null;
    shiftDown = false;
    renderer.repaint();
    super.resetTool();
  }

  @Override
  protected boolean isEraser() {
    return shiftDown;
  }

  @Override
  protected boolean isSnapToGrid(MouseEvent e) {
    return false;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      shiftDown = true;
      renderer.repaint();
    }
    super.keyPressed(e);
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      shiftDown = false;
      renderer.repaint();
    }
    super.keyReleased(e);
  }

  private void submit(Shape shape) {
    if (shape == null) {
      return;
    }
    Area area = new Area(shape);
    for (var type : AppStatePersisted.getTopologyTypes()) {
      MapTool.serverCommand().updateMaskTopology(getZone(), area, isEraser(), type);
    }
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    maskOverlay.paintOverlay(renderer, g);

    var zoneScale = renderer.getViewModel().getZoneScale();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.transform(zoneScale.toScreenTransform());

    int thickness = AppStatePersisted.getVblPenRadius();
    double radius = thickness / 2.0;

    // Show a preview of the pen at the current mouse position.
    var mousePoint = renderer.getMousePosition();
    if (mousePoint != null) {
      ZonePoint zp =
          getPoint(new MouseEvent(renderer, 0, 0, 0, mousePoint.x, mousePoint.y, 0, false));
      Ellipse2D circle = new Ellipse2D.Double(zp.x - radius, zp.y - radius, thickness, thickness);
      Color color = isEraser() ? AppStyle.topologyRemoveColor : AppStyle.topologyAddColor;
      g2.setColor(color);
      g2.fill(circle);
      g2.setStroke(new BasicStroke(1 / (float) zoneScale.getScale()));
      g2.draw(circle);
    }

    g2.dispose();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      shiftDown = e.isShiftDown();
      lastPoint = getPoint(e);
      addPointToTopology(lastPoint);
      renderer.repaint();
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (lastPoint != null && SwingUtilities.isLeftMouseButton(e)) {
      cancelMapDrag();
      shiftDown = e.isShiftDown();
      ZonePoint point = getPoint(e);
      if (!point.equals(lastPoint)) {
        addPointToTopology(point);
        lastPoint = point;
        renderer.repaint();
      }
    } else {
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (lastPoint != null && SwingUtilities.isLeftMouseButton(e)) {
      shiftDown = e.isShiftDown();
      lastPoint = null;
      renderer.repaint();
    }
    super.mouseReleased(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    shiftDown = e.isShiftDown();
    renderer.repaint(); // Repaint to update the pen preview.
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (net.rptools.maptool.client.swing.SwingUtil.isControlDown(e)) {
      int thickness = AppStatePersisted.getVblPenRadius();
      // Increase/decrease by 1 or more depending on rotation.
      // Scrolling down (positive) decreases, up (negative) increases.
      thickness -= e.getWheelRotation();
      thickness = Math.max(1, Math.min(300, thickness));

      AppStatePersisted.setVblPenRadius(thickness);
      RADIUS_PANEL.updateSpinner(thickness);
      renderer.repaint();
      return;
    }
    super.mouseWheelMoved(e);
  }

  private void addPointToTopology(ZonePoint point) {
    int thickness = AppStatePersisted.getVblPenRadius();
    double radius = thickness / 2.0;

    Path2D segmentPath = new Path2D.Double();
    Ellipse2D circle =
        new Ellipse2D.Double(point.x - radius, point.y - radius, thickness, thickness);
    segmentPath.append(circle, false);

    if (lastPoint != null) {
      BasicStroke stroke =
          new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      Path2D line = new Path2D.Double();
      line.moveTo(lastPoint.x, lastPoint.y);
      line.lineTo(point.x, point.y);
      segmentPath.append(stroke.createStrokedShape(line), false);
    }
    submit(segmentPath);
  }

  private static class RadiusPanel extends JPanel {
    private final JSpinner radiusSpinner;

    public RadiusPanel() {
      setLayout(new FlowLayout(FlowLayout.CENTER));
      setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

      add(new JLabel(I18N.getText("label.radius") + ":"));

      radiusSpinner =
          new JSpinner(new SpinnerNumberModel(AppStatePersisted.getVblPenRadius(), 1, 300, 1));
      radiusSpinner.addChangeListener(
          e -> {
            AppStatePersisted.setVblPenRadius((Integer) radiusSpinner.getValue());
            if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
              MapTool.getFrame().getCurrentZoneRenderer().repaint();
            }
          });
      add(radiusSpinner);
    }

    public void updateSpinner(int value) {
      radiusSpinner.setValue(value);
    }
  }
}
