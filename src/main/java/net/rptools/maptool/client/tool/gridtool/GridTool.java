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
package net.rptools.maptool.client.tool.gridtool;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.zones.GridChanged;

/** */
public class GridTool extends DefaultTool {
  private static final long serialVersionUID = 3760846783148208951L;
  private static final int zoomSliderStopCount = 100;

  private enum Size {
    Increase,
    Decrease
  }

  private final JSpinner gridSizeSpinner;
  private final JTextField gridOffsetXTextField;
  private final JTextField gridOffsetYTextField;
  private final ColorWell colorWell;
  private final JSlider zoomSlider;
  private final JTextField gridSecondDimension;
  private final JLabel gridSecondDimensionLabel;
  private final AbeillePanel controlPanel;

  private int lastZoomIndex;
  private int dragOffsetX;
  private int dragOffsetY;

  private int mouseX;
  private int mouseY;

  private boolean oldShowGrid;

  public GridTool() {
    // Create the control panel
    controlPanel = new AbeillePanel(new AdjustGridControlPanelView().getRootComponent());

    gridSizeSpinner = (JSpinner) controlPanel.getComponent("gridSize");
    gridSizeSpinner.setModel(
        new SpinnerNumberModel(100, Grid.MIN_GRID_SIZE, Grid.MAX_GRID_SIZE, 1));
    gridSizeSpinner.addChangeListener(new UpdateGridListener());

    gridOffsetXTextField = (JTextField) controlPanel.getComponent("offsetX");
    gridOffsetXTextField.addKeyListener(new UpdateGridListener());

    gridOffsetYTextField = (JTextField) controlPanel.getComponent("offsetY");
    gridOffsetYTextField.addKeyListener(new UpdateGridListener());

    gridSecondDimensionLabel = (JLabel) controlPanel.getComponent("gridSecondDimensionLabel");
    gridSecondDimension = (JTextField) controlPanel.getComponent("gridSecondDimension");
    gridSecondDimension.addFocusListener(new UpdateGridListener());

    colorWell = (ColorWell) controlPanel.getComponent("colorWell");
    colorWell.addActionListener(e -> copyControlPanelToGrid());

    JButton closeButton = (JButton) controlPanel.getComponent("closeButton");
    closeButton.addActionListener(
        e -> {
          resetTool();
          // Lee: just to make the light sources snap to their owners after the tool is closed
          Zone z = MapTool.getFrame().getCurrentZoneRenderer().getZone();
          z.putTokens(z.getAllTokens());
        });
    zoomSlider = (JSlider) controlPanel.getComponent("zoomSlider");
    zoomSlider.setMinimum(0);
    zoomSlider.setMaximum(zoomSliderStopCount);
    ZoomChangeListener zoomListener = new ZoomChangeListener();
    zoomSlider.addChangeListener(zoomListener);
    zoomSlider.addMouseListener(zoomListener);
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK),
        new GridSizeAction(Size.Decrease));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK),
        new GridSizeAction(Size.Decrease));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK),
        new GridSizeAction(Size.Increase));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK),
        new GridSizeAction(Size.Increase));
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new GridOffsetAction(Direction.Up));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new GridOffsetAction(Direction.Left));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new GridOffsetAction(Direction.Down));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new GridOffsetAction(Direction.Right));
  }

  private void copyGridToControlPanel() {
    Zone zone = renderer.getZone();
    Grid grid = zone.getGrid();

    updateSecondDimension(grid, true);
    gridOffsetXTextField.setText(Integer.toString(grid.getOffsetX()));
    gridOffsetYTextField.setText(Integer.toString(grid.getOffsetY()));
    colorWell.setColor(new Color(zone.getGridColor()));
    // Setting the size must be done last as it triggers a ChangeEvent
    // which causes copyControlPanelToGrid() to be called.
    gridSizeSpinner.setValue(grid.getSize());

    resetZoomSlider();
  }

  /**
   * Updates the panel's text or the grids second settable dimension.
   *
   * @param toPanel true = from grid to panel, false = from panel to grid
   * @param grid
   */
  private void updateSecondDimension(Grid grid, boolean toPanel) {
    if (grid.getCapabilities().isSecondDimensionAdjustmentSupported()) {
      if (toPanel) {
        // truncate to 3 decimal places
        double secondDim = Math.round(grid.getSecondDimension() * 1000.0) / 1000.0;
        gridSecondDimension.setText(Double.toString(secondDim));
      } else { // toGrid
        double newMajDiameter = getDouble(gridSecondDimension.getText(), 0);
        grid.setSecondDimension(newMajDiameter);
      }
    }
  }

  private void copyControlPanelToGrid() {
    Zone zone = renderer.getZone();
    Grid grid = zone.getGrid();

    updateSecondDimension(grid, false);
    updateSecondDimension(grid, true);
    grid.setOffset(getInt(gridOffsetXTextField, 0), getInt(gridOffsetYTextField, 0));
    zone.setGridColor(colorWell.getColor().getRGB());
    grid.setSize(Math.max((Integer) gridSizeSpinner.getValue(), Grid.MIN_GRID_SIZE));

    new MapToolEventBus().getMainEventBus().post(new GridChanged(zone));
  }

  @Override
  public String getTooltip() {
    return "tool.gridtool.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.gridtool.instructions";
  }

  private int getInt(JTextComponent component, int defaultValue) {
    return getInt(component.getText(), defaultValue);
  }

  private int getInt(String value, int defaultValue) {
    try {
      return value.length() > 0 ? Integer.parseInt(value.trim()) : defaultValue;
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private double getDouble(String value, double defaultValue) {
    try {
      return value.length() > 0 ? Double.parseDouble(value.trim()) : defaultValue;
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see maptool.client.Tool#attachTo(maptool.client.ZoneRenderer)
   */
  @Override
  protected void attachTo(ZoneRenderer renderer) {
    oldShowGrid = AppState.isShowGrid();
    AppState.setShowGrid(true);

    Grid grid = renderer.getZone().getGrid();

    boolean showSecond = grid.getCapabilities().isSecondDimensionAdjustmentSupported();
    gridSecondDimension.setVisible(showSecond);
    gridSecondDimensionLabel.setVisible(showSecond);

    MapTool.getFrame().showControlPanel(controlPanel);
    renderer.repaint();

    super.attachTo(renderer);

    copyGridToControlPanel();
  }

  /*
   * (non-Javadoc)
   *
   * @see maptool.client.Tool#detachFrom(maptool.client.ZoneRenderer)
   */
  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    AppState.setShowGrid(oldShowGrid);
    MapTool.getFrame().removeControlPanel();
    renderer.repaint();

    // Commit the grid size change
    Zone zone = renderer.getZone();
    MapTool.serverCommand()
        .setZoneGridSize(
            zone.getId(),
            zone.getGrid().getOffsetX(),
            zone.getGrid().getOffsetY(),
            zone.getGrid().getSize(),
            zone.getGridColor());

    super.detachFrom(renderer);
  }

  ////
  // MOUSE LISTENER

  @Override
  public void mousePressed(java.awt.event.MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      ZonePoint zp = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
      int x = zp.x - renderer.getZone().getGrid().getOffsetX();
      int y = zp.y - renderer.getZone().getGrid().getOffsetY();

      dragOffsetX = x % renderer.getZone().getGrid().getSize();
      dragOffsetY = y % renderer.getZone().getGrid().getSize();
    } else {
      super.mousePressed(e);
    }
  }

  ////
  // MOUSE MOTION LISTENER
  @Override
  public void mouseDragged(java.awt.event.MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      ZonePoint zp = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
      int x = zp.x - dragOffsetX;
      int y = zp.y - dragOffsetY;

      int gridSize = renderer.getZone().getGrid().getSize();

      x %= gridSize;
      y %= gridSize;

      if (x > 0) x -= gridSize;
      if (y > 0) y -= gridSize;

      renderer.getZone().getGrid().setOffset(x, y);

      copyGridToControlPanel();
    } else {
      super.mouseDragged(e);
    }
  }

  ////
  // MOUSE WHEEL LISTENER
  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event. MouseWheelEvent)
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    // Fix for hi-res mice
    if (e.getWheelRotation() == 0) {
      return;
    }
    ZoneRenderer renderer = (ZoneRenderer) e.getSource();
    if (SwingUtil.isControlDown(e)) {
      if (e.getWheelRotation() > 0) {
        renderer.zoomOut(e.getX(), e.getY());
      } else {
        renderer.zoomIn(e.getX(), e.getY());
      }
    } else {
      if (e.getWheelRotation() > 0) {
        adjustGridSize(renderer, Size.Increase);
      } else {
        adjustGridSize(renderer, Size.Decrease);
      }
    }
  }

  private void resetZoomSlider() {
    EventQueue.invokeLater(
        () -> {
          lastZoomIndex = zoomSliderStopCount / 2;
          zoomSlider.setValue(lastZoomIndex);
        });
  }

  public void moveGridBy(int dx, int dy) {
    int gridOffsetX = renderer.getZone().getGrid().getOffsetX();
    int gridOffsetY = renderer.getZone().getGrid().getOffsetY();

    gridOffsetX += dx;
    gridOffsetY += dy;

    if (gridOffsetY > 0) {
      gridOffsetY = gridOffsetY - (int) renderer.getZone().getGrid().getCellHeight();
    }
    if (gridOffsetX > 0) {
      gridOffsetX = gridOffsetX - (int) renderer.getZone().getGrid().getCellWidth();
    }
    renderer.getZone().getGrid().setOffset(gridOffsetX, gridOffsetY);
  }

  public void adjustGridSize(int delta) {
    renderer
        .getZone()
        .getGrid()
        .setSize(Math.max(0, renderer.getZone().getGrid().getSize() + delta));
  }

  private void adjustGridSize(ZoneRenderer renderer, Size direction) {
    CellPoint cell = renderer.getCellAt(new ScreenPoint(mouseX, mouseY));
    if (cell == null) {
      return;
    }
    int oldGridSize = renderer.getZone().getGrid().getSize();

    switch (direction) {
      case Increase:
        adjustGridSize(1);
        updateSecondDimension(renderer.getZone().getGrid(), true);

        if (renderer.getZone().getGrid().getSize() != oldGridSize) {
          moveGridBy(-cell.x, -cell.y);
        }
        break;
      case Decrease:
        adjustGridSize(-1);
        updateSecondDimension(renderer.getZone().getGrid(), true);

        if (renderer.getZone().getGrid().getSize() != oldGridSize) {
          moveGridBy(cell.x, cell.y);
        }
        break;
    }
    copyGridToControlPanel();
  }

  private final class GridSizeAction extends AbstractAction {
    private static final long serialVersionUID = -3949586212099357034L;
    private final Size size;

    public GridSizeAction(Size size) {
      this.size = size;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = (ZoneRenderer) e.getSource();
      adjustGridSize(renderer, size);
      copyGridToControlPanel();
    }
  }

  private enum Direction {
    Left,
    Right,
    Up,
    Down
  }

  private class GridOffsetAction extends AbstractAction {
    private static final long serialVersionUID = 6664327737774374442L;
    private final Direction direction;

    public GridOffsetAction(Direction direction) {
      this.direction = direction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = (ZoneRenderer) e.getSource();
      switch (direction) {
        case Left:
          moveGridBy(-1, 0);
          break;
        case Right:
          moveGridBy(1, 0);
          break;
        case Up:
          moveGridBy(0, -1);
          break;
        case Down:
          moveGridBy(0, 1);
          break;
      }
      copyGridToControlPanel();
    }
  }

  private class ZoomChangeListener extends MouseAdapter implements ChangeListener {
    @Override
    public void stateChanged(ChangeEvent e) {
      int delta = zoomSlider.getValue() - lastZoomIndex;
      if (delta == 0) {
        return;
      }
      boolean direction = delta > 0;
      delta = Math.abs(delta);
      ZonePoint centerPoint = renderer.getCenterPoint();

      for (int i = 0; i < delta; i++) {
        if (direction) {
          renderer.getZoneScale().zoomOut(centerPoint.x, centerPoint.y);
        } else {
          renderer.getZoneScale().zoomIn(centerPoint.x, centerPoint.y);
        }
      }
      lastZoomIndex = zoomSlider.getValue();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      resetZoomSlider();
    }
  }

  ////
  // ACTIONS
  private class UpdateGridListener implements KeyListener, ChangeListener, FocusListener {
    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
      copyControlPanelToGrid();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void stateChanged(ChangeEvent e) {
      copyControlPanelToGrid();
    }

    @Override
    public void focusLost(FocusEvent e) {
      copyControlPanelToGrid();
    }

    @Override
    public void focusGained(FocusEvent e) {}
  }
}
