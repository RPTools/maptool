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
package net.rptools.maptool.client.ui.adjustgrid;

import com.jeta.forms.components.colors.JETAColorWell;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import net.rptools.lib.swing.SwingUtil;

public class AdjustGridDialog extends JDialog {

  private AdjustGridPanel adjustGridPanel = null;

  private boolean isOK;
  private JTextField gridSizeTextField;
  private JTextField offsetXTextField;
  private JTextField offsetYTextField;
  private JSlider zoomSlider;
  private JETAColorWell colorWell;

  /**
   * This is the default constructor
   *
   * @param owner the JFrame
   * @param image the image of the zone
   */
  public AdjustGridDialog(JFrame owner, BufferedImage image) {
    super(owner, "Adjust Grid", true);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            isOK = false;
            setVisible(false);
            dispose();
          }
        });

    initialize();
    getAdjustGridPanel().setZoneImage(image);
  }

  /** This method initializes this */
  private void initialize() {
    this.setSize(500, 500);

    FormPanel panel = new FormPanel("net/rptools/maptool/client/ui/forms/adjustGridDialog.xml");

    panel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    panel.getActionMap().put("cancel", new CancelAction());

    AbstractButton okButton = panel.getButton("okButton");
    okButton.setAction(new OKAction());

    AbstractButton cancelButton = panel.getButton("cancelButton");
    cancelButton.setAction(new CancelAction());

    AdjustGridPanel adjustGridPanel = getAdjustGridPanel();

    gridSizeTextField = panel.getTextField("gridSize");
    gridSizeTextField.addActionListener(new UpdateAdjustGridPanelHandler());
    gridSizeTextField.setText(Integer.toString(adjustGridPanel.getGridSize()));
    gridSizeTextField.addFocusListener(new SelectTextListener(gridSizeTextField));

    offsetXTextField = panel.getTextField("xOffset");
    offsetXTextField.addActionListener(new UpdateAdjustGridPanelHandler());
    offsetXTextField.setText(Integer.toString(adjustGridPanel.getGridOffsetX()));
    offsetXTextField.addFocusListener(new SelectTextListener(offsetXTextField));

    offsetYTextField = panel.getTextField("yOffset");
    offsetYTextField.addActionListener(new UpdateAdjustGridPanelHandler());
    offsetYTextField.setText(Integer.toString(adjustGridPanel.getGridOffsetY()));
    offsetYTextField.addFocusListener(new SelectTextListener(offsetYTextField));

    colorWell = (JETAColorWell) panel.getComponentByName("color");
    colorWell.addActionListener(new ColorChangeAction());

    zoomSlider = (JSlider) panel.getComponentByName("zoom");
    zoomSlider.setMinimum(0);
    zoomSlider.setMaximum(500);
    zoomSlider.addChangeListener(new ZoomChangeListener());

    FormAccessor accessor = panel.getFormAccessor();
    accessor.replaceBean("adjustGridPanel", adjustGridPanel);

    setLayout(new GridLayout());
    add(panel);

    getRootPane().setDefaultButton((JButton) okButton);
  }

  public void initialize(
      final int gridSize, final int gridOffsetX, final int gridOffsetY, final Color gridColor) {
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            gridSizeTextField.setText(Integer.toString(gridSize));
            offsetXTextField.setText(Integer.toString(gridOffsetX));
            offsetYTextField.setText(Integer.toString(gridOffsetY));
            colorWell.setColor(gridColor);

            getAdjustGridPanel().setGridColor(gridColor);
            getAdjustGridPanel().setGridOffset(gridOffsetX, gridOffsetY);
            getAdjustGridPanel().setGridSize(gridSize);
          }
        });
  }

  @Override
  public void setVisible(boolean b) {
    if (getOwner() != null) {
      SwingUtil.centerOver(this, getOwner());
    }
    super.setVisible(b);
  }

  public boolean isOK() {
    return isOK;
  }

  /**
   * This method initializes adjustGridPanel
   *
   * @return net.rptools.maptool.client.ui.adjustgrid.AdjustGridPanel
   */
  private AdjustGridPanel getAdjustGridPanel() {
    if (adjustGridPanel == null) {
      adjustGridPanel = new AdjustGridPanel();
      adjustGridPanel.setBorder(BorderFactory.createLineBorder(Color.black));
      adjustGridPanel.setBackground(Color.white);
      adjustGridPanel.addPropertyChangeListener(new AdjustGridPanelChangeListener());
    }
    return adjustGridPanel;
  }

  public int getGridSize() {
    return getAdjustGridPanel().getGridSize();
  }

  public int getGridOffsetX() {
    return getAdjustGridPanel().getGridOffsetX();
  }

  public int getGridOffsetY() {
    return getAdjustGridPanel().getGridOffsetY();
  }

  private class AdjustGridPanelChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();
      Object value = evt.getNewValue();

      if (AdjustGridPanel.PROPERTY_GRID_OFFSET_X.equals(name)) {
        offsetXTextField.setText(value.toString());
      }
      if (AdjustGridPanel.PROPERTY_GRID_OFFSET_Y.equals(name)) {
        offsetYTextField.setText(value.toString());
      }
      if (AdjustGridPanel.PROPERTY_GRID_SIZE.equals(name)) {
        gridSizeTextField.setText(value.toString());
      }
      if (AdjustGridPanel.PROPERTY_ZOOM.equals(name)) {
        zoomSlider.setValue((Integer) value);
      }
    }
  }

  public void setGridSize(int gridSize) {
    getAdjustGridPanel().setGridSize(gridSize);
    gridSizeTextField.setText(Integer.toString(gridSize));
  }

  public void setGridColor(Color color) {
    getAdjustGridPanel().setGridColor(color);
    colorWell.setColor(color);
  }

  public Color getGridColor() {
    return colorWell.getColor();
  }

  public void setGridOffset(int offsetX, int offsetY) {
    getAdjustGridPanel().setGridOffset(offsetX, offsetY);
    offsetXTextField.setText(Integer.toString(offsetX));
    offsetYTextField.setText(Integer.toString(offsetY));
  }

  private void updateAdjustGridPanel() {
    setGridSize(Integer.parseInt(gridSizeTextField.getText()));
    setGridOffset(
        Integer.parseInt(offsetXTextField.getText()), Integer.parseInt(offsetYTextField.getText()));
  }

  private class UpdateAdjustGridPanelHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      updateAdjustGridPanel();
    }
  }

  private class SelectTextListener implements FocusListener {
    private final JTextComponent textComponent;

    public SelectTextListener(JTextComponent component) {
      textComponent = component;
    }

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
      updateAdjustGridPanel();
    }
  }

  ////
  // ACTIONS
  private class OKAction extends AbstractAction {
    public OKAction() {
      putValue(Action.NAME, "OK");
    }

    public void actionPerformed(ActionEvent e) {
      isOK = true;
      setVisible(false);
      dispose();
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      putValue(Action.NAME, "Cancel");
    }

    public void actionPerformed(ActionEvent e) {
      isOK = false;
      setVisible(false);
      dispose();
    }
  }

  private class ColorChangeAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      Color color = colorWell.getColor();
      setGridColor(color);
    }
  }

  private class ZoomChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      adjustGridPanel.setZoomIndex(zoomSlider.getValue());
    }
  }
}
