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
package net.rptools.maptool.client.tool.boardtool;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;

/**
 * Allows user to re-position the background map (internally called the 'board'). This entire class
 * should be 'transient'... should this be in the var names, or in the reference to the BoardTool
 * instance?
 */
public class BoardTool extends DefaultTool {
  private static final long serialVersionUID = 98389912045059L;

  // Context variables
  private static Zone zone;
  private static boolean oldShowGrid;

  // Status variables
  private static Point boardPosition = new Point(0, 0);
  private static Dimension snap = new Dimension(1, 1);

  // Action control variables
  private Point dragStart;
  private Dimension dragOffset;
  private Point boardStart;

  // UI button fields
  private final JTextField boardPositionXTextField;
  private final JTextField boardPositionYTextField;
  private final AbeillePanel controlPanel;
  private final JRadioButton snapNoneButton;
  private final JRadioButton snapGridButton;
  private final JRadioButton snapTileButton;

  /** Initialize the panel and set up the actions. */
  public BoardTool() {
    // Create the control panel
    controlPanel = new AbeillePanel(new AdjustBoardControlPanelView().getRootComponent());

    boardPositionXTextField = (JTextField) controlPanel.getComponent("offsetX");
    boardPositionXTextField.addKeyListener(new UpdateBoardListener());

    boardPositionYTextField = (JTextField) controlPanel.getComponent("offsetY");
    boardPositionYTextField.addKeyListener(new UpdateBoardListener());

    ActionListener enforceRules = evt -> enforceButtonRules();
    snapNoneButton = (JRadioButton) controlPanel.getComponent("snapNone");
    snapNoneButton.addActionListener(enforceRules);

    snapGridButton = (JRadioButton) controlPanel.getComponent("snapGrid");
    snapGridButton.addActionListener(enforceRules);

    snapTileButton = (JRadioButton) controlPanel.getComponent("snapTile");
    snapTileButton.addActionListener(enforceRules);

    JButton closeButton = (JButton) controlPanel.getComponent("closeButton");
    closeButton.addActionListener(e -> resetTool());
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new boardPositionAction(Direction.Up));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new boardPositionAction(Direction.Left));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new boardPositionAction(Direction.Down));
    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new boardPositionAction(Direction.Right));
  }

  /** Figure out how big the repeating board tile image is. */
  private Dimension getTileSize() {
    Zone zone = renderer.getZone();
    Dimension tileSize = null;

    if (zone != null) {
      DrawablePaint paint = zone.getBackgroundPaint();
      DrawableTexturePaint dummy = new DrawableTexturePaint();
      if (paint.getClass() == dummy.getClass()) {
        Image bgTexture =
            ImageManager.getImage(((DrawableTexturePaint) paint).getAsset().getMD5Key());
        tileSize = new Dimension(bgTexture.getWidth(null), bgTexture.getHeight(null));
      }
    }
    return tileSize;
  }

  /** Moves the board to the nearest snap intersection. Modifies GUI. */
  private void snapBoard() {
    boardPosition.x = (Math.round(boardPosition.x / snap.width) * snap.width);
    boardPosition.y = (Math.round(boardPosition.y / snap.height) * snap.height);
    updateGUI();
  }

  /**
   * Sets the snap mode with independent x/y snaps and adjusts the board position appropriately.
   *
   * @param x the new x snap amount
   * @param y the new y snap amount
   */
  private void setSnap(int x, int y) {
    snap.width = x;
    snap.height = y;
    snapBoard();
  }

  private void updateGUI() {
    boardPositionXTextField.setText(Integer.toString(boardPosition.x));
    boardPositionYTextField.setText(Integer.toString(boardPosition.y));
  }

  /**
   * Copies the current board (map image as set in "New Map/Edit Map") info to the tool so we have
   * the appropriate starting info. Should be called each time the tool is un-hidden.
   */
  private void copyBoardToControlPanel() {
    boardPosition.x = zone.getBoardX();
    boardPosition.y = zone.getBoardY();
    snapBoard();
    updateGUI();
  }

  private void copyControlPanelToBoard() {
    boardPosition.x = getInt(boardPositionXTextField, 0);
    boardPosition.y = getInt(boardPositionYTextField, 0);
    zone.setBoard(boardPosition);
  }

  @Override
  public String getTooltip() {
    return "tool.boardtool.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.boardtool.instructions";
  }

  /**
   * Parses the text field of the component into a number, returning the default value if the text
   * field is _not_ a number.
   */
  private int getInt(JTextComponent component, int defaultValue) {
    // Get the string from the component, then
    // call the more-generic getInt-from-a-string
    return StringUtil.parseInteger(component.getText(), defaultValue);
  }

  /*
   * private double getDouble(String value, double defaultValue) { try { return value.length() > 0 ? Double.parseDouble(value.trim()) : defaultValue; } catch (NumberFormatException e) { return 0; }
   * }
   */

  /*
   * (non-Javadoc)
   *
   * @see maptool.client.Tool#attachTo(maptool.client.ZoneRenderer)
   */
  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    zone = renderer.getZone();
    copyBoardToControlPanel();
    oldShowGrid = AppState.isShowGrid();
    AppState.setShowGrid(true);

    // Find out if it is already aligned to grid or background tile, and
    // default to keeping that same alignment.
    final int offset = zone.getBoardX();
    final Dimension tileSize = getTileSize();
    final int gridSize = zone.getGrid().getSize();

    if ((tileSize != null) && ((offset % tileSize.width) == 0)) {
      setSnap(tileSize.width, tileSize.height);
      snapTileButton.setSelected(true);
    } else if ((offset % gridSize) == 0) {
      setSnap(gridSize, gridSize);
      snapGridButton.setSelected(true);
    } else {
      setSnap(1, 1);
      snapNoneButton.setSelected(true);
    }
    MapTool.getFrame().showControlPanel(controlPanel);
  }

  /*
   * (non-Javadoc)
   *
   * @see maptool.client.Tool#detachFrom(maptool.client.ZoneRenderer)
   */
  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    MapTool.getFrame().removeControlPanel();
    MapTool.serverCommand()
        .setBoard(zone.getId(), zone.getMapAssetId(), zone.getBoardX(), zone.getBoardY());
    AppState.setShowGrid(oldShowGrid);
    super.detachFrom(renderer);
  }

  ////
  // MOUSE LISTENER

  @Override
  public void mousePressed(java.awt.event.MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      ZonePoint zp = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
      Grid grid = renderer.getZone().getGrid();
      dragStart = new Point(zp.x - grid.getOffsetX(), zp.y - grid.getOffsetY());
      boardStart = new Point(boardPosition);
      dragOffset = new Dimension(0, 0);
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

      dragOffset.width = zp.x - dragStart.x;
      dragOffset.height = zp.y - dragStart.y;

      boardPosition.x = boardStart.x + dragOffset.width;
      boardPosition.y = boardStart.y + dragOffset.height;
      snapBoard();
      updateGUI();
      zone.setBoard(boardPosition);
    } else {
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseMoved(java.awt.event.MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
  }

  /** A simple enum for correlating keys with directions */
  private enum Direction {
    Left,
    Right,
    Up,
    Down
  };

  /** Constructs actions to attach to key-presses. */
  @SuppressWarnings("serial")
  private class boardPositionAction extends AbstractAction {
    private final Direction direction;

    public boardPositionAction(Direction direction) {
      this.direction = direction;
    }

    public void actionPerformed(ActionEvent e) {
      switch (direction) {
        case Left:
          boardPosition.x -= snap.width;
          break;
        case Right:
          boardPosition.x += snap.width;
          break;
        case Up:
          boardPosition.y -= snap.height;
          break;
        case Down:
          boardPosition.y += snap.height;
          break;
      }
      updateGUI();
      zone.setBoard(boardPosition);
    }
  }

  ////
  // ACTIONS
  private class UpdateBoardListener implements KeyListener, ChangeListener, FocusListener {
    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
      copyControlPanelToBoard();
    }

    public void keyTyped(KeyEvent e) {}

    public void stateChanged(ChangeEvent e) {
      copyControlPanelToBoard();
    }

    public void focusLost(FocusEvent e) {
      copyControlPanelToBoard();
    }

    public void focusGained(FocusEvent e) {}
  }

  private void enforceButtonRules() {
    if (snapGridButton.isSelected()) {
      final int gridSize = zone.getGrid().getSize();
      setSnap(gridSize, gridSize);
    } else if (snapTileButton.isSelected()) {
      final Dimension tileSize = getTileSize();
      if (tileSize != null) setSnap(tileSize.width, tileSize.height);
      else setSnap(1, 1);
    } else {
      setSnap(1, 1);
    }
    updateGUI();
    zone.setBoard(boardPosition);
  }
}
