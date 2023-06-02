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
package net.rptools.maptool.client.swing.colorpicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.swing.PaintChooser;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;

public class ColorPicker extends JPanel {
  private final JFrame owner;

  private final ColorWell foregroundColor;
  private final ColorWell backgroundColor;
  private final List<ColorWell> recentColors = new ArrayList<>(16);
  private final JToggleButton snapToggle;
  private final JToggleButton eraseToggle;
  private final JToggleButton squareCapToggle;
  private final PaintChooser paintChooser;
  private final JSpinner penWidthSpinner;
  private final JSpinner transparencySpinner;

  private static final int RECENT_COLOR_LIST_SIZE = 16;
  private static final int maxPenWidth = 300;

  private static final Color[] DEFAULT_COLORS =
      new Color[] {
        null,
        Color.black,
        Color.darkGray,
        Color.lightGray,
        Color.white,
        Color.pink,
        new Color(127, 0, 0),
        Color.red,
        Color.orange,
        Color.yellow,
        new Color(0, 127, 0),
        Color.green,
        Color.blue,
        Color.cyan,
        new Color(127, 0, 127),
        Color.magenta,
        new Color(127 + 32, 127, 61),
      };

  public ColorPicker(JFrame owner) {
    this.owner = owner;

    paintChooser = new PaintChooser();
    paintChooser.setPreferredSize(new Dimension(450, 400));

    AbeillePanel panel = new AbeillePanel(new ColorPanelView().getRootComponent());

    ColorWellListener listener = new ColorWellListener(1);

    foregroundColor = (ColorWell) panel.getComponent("foregroundColor");
    backgroundColor = (ColorWell) panel.getComponent("backgroundColor");

    foregroundColor.setMouseAdapter(listener);

    backgroundColor.setMouseAdapter(listener);

    listener = new ColorWellListener(2);
    for (int i = 0; i < RECENT_COLOR_LIST_SIZE; i++) {
      ColorWell colorWell = (ColorWell) panel.getComponent("recentColor" + i);
      colorWell.setMouseAdapter(listener);
      recentColors.add(colorWell);
    }
    snapToggle = (JToggleButton) panel.getButton("toggleSnapToGrid");
    snapToggle.setIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_SNAP_OFF));
    snapToggle.setSelectedIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_SNAP_ON));

    squareCapToggle = (JToggleButton) panel.getButton("toggleSquareCap");
    squareCapToggle.setIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_CAP_ROUND));
    squareCapToggle.setSelectedIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_CAP_SQUARE));

    eraseToggle = (JToggleButton) panel.getButton("toggleErase");
    eraseToggle.setIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_PENCIL));
    eraseToggle.setSelectedIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_ERASER));

    var opacityLabel = (JLabel) panel.getComponent("opacityLabel");
    opacityLabel.setIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_OPACITY));
    var penWidthLabel = (JLabel) panel.getComponent("penWidthLabel");
    penWidthLabel.setIcon(RessourceManager.getSmallIcon(Icons.COLORPICKER_PEN_WIDTH));

    penWidthSpinner = panel.getSpinner("penWidth");
    penWidthSpinner.setModel(new SpinnerNumberModel(3, 1, maxPenWidth, 1));
    penWidthSpinner.addChangeListener(
        e -> {
          try {
            penWidthSpinner.commitEdit();
          } catch (ParseException pe) {
            pe.printStackTrace();
          }
        });

    transparencySpinner = panel.getSpinner("opacity");
    transparencySpinner.setModel(new SpinnerNumberModel(100, 1, 100, 1));
    transparencySpinner.addChangeListener(
        e -> {
          try {
            transparencySpinner.commitEdit();
          } catch (ParseException pe) {
            pe.printStackTrace();
          }
        });
    initialize();
    add(panel);
  }

  public PaintChooser getPaintChooser() {
    return paintChooser;
  }

  public void initialize() {
    foregroundColor.setPaint(Color.BLACK);
    backgroundColor.setPaint(Color.WHITE);

    for (int i = 0; i < DEFAULT_COLORS.length && i < RECENT_COLOR_LIST_SIZE; i++) {
      recentColors.get(i).setPaint(DEFAULT_COLORS[i]);
    }
  }

  public void setForegroundPaint(Paint paint) {
    foregroundColor.setPaint(paint);
  }

  public void setBackgroundPaint(Paint paint) {
    backgroundColor.setPaint(paint);
  }

  public boolean isFillForegroundSelected() {
    return foregroundColor.getPaint() != null;
  }

  public boolean isFillBackgroundSelected() {
    return backgroundColor.getPaint() != null;
  }

  public void setEraseSelected(boolean selected) {
    eraseToggle.setSelected(selected);
  }

  public boolean isEraseSelected() {
    return eraseToggle.isSelected();
  }

  public void setSnapSelected(boolean selected) {
    snapToggle.setSelected(selected);
  }

  public boolean isSnapSelected() {
    return snapToggle.isSelected();
  }

  public boolean isSquareCapSelected() {
    return squareCapToggle.isSelected();
  }

  public void setSquareCapSelected(boolean selected) {
    squareCapToggle.setSelected(selected);
  }

  public void setTranslucency(int percent) {
    percent = Math.max(0, percent);
    percent = Math.min(100, percent);

    transparencySpinner.setValue(percent);
  }

  public void setPenWidth(int width) {
    width = Math.max(0, width);
    width = Math.min(maxPenWidth, width);

    penWidthSpinner.setValue(width);
  }

  public Paint getForegroundPaint() {
    return foregroundColor.getPaint();
  }

  public Paint getBackgroundPaint() {
    return backgroundColor.getPaint();
  }

  public int getStrokeWidth() {
    return (Integer) penWidthSpinner.getValue();
  }

  public float getOpacity() {
    return ((Integer) transparencySpinner.getValue()) / 100.0f;
  }

  public class ColorWellListener extends MouseAdapter {
    private final int clickCount;

    /**
     * Pass a 1 or 2 to determine whether this listener is looking for single or double click mouse
     * events.
     *
     * @param clickCount number of clicks to listen for
     */
    public ColorWellListener(int clickCount) {
      this.clickCount = clickCount;
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
      ColorWell comp = (ColorWell) evt.getSource();

      if (evt.getClickCount() == clickCount) {
        Paint result = paintChooser.choosePaint(owner, comp.getPaint());
        comp.setPaint(result);
        return;
      }
      switch (evt.getButton()) {
        case MouseEvent.BUTTON1:
          foregroundColor.setPaint(comp.getPaint());
          break;
        case MouseEvent.BUTTON3:
          backgroundColor.setPaint(comp.getPaint());
          break;
      }
    }
  }
}
