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
package net.rptools.maptool.client.tool.texttool;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.ZonePoint;

/**
 * The TextTool class represents a tool that allows users to add and edit labels in a graphical
 * editor. It extends the DefaultTool class and implements the ZoneOverlay interface.
 */
public class TextTool extends DefaultTool implements ZoneOverlay {
  /** The serial version UID. */
  private static final long serialVersionUID = -8944323545051996907L;

  /**
   * Represents the currently selected Label object.
   *
   * @see Label
   */
  private Label selectedLabel;

  /** The horizontal offset for dragging the element. */
  private int dragOffsetX;

  /** The vertical offset of the drag operation. */
  private int dragOffsetY;

  /** Is the Label being dragged. */
  private boolean isDragging;

  private boolean selectedNewLabel;

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    renderer.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    super.attachTo(renderer);
    selectedLabel = null;
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    renderer.setCursor(Cursor.getDefaultCursor());
    super.detachFrom(renderer);
  }

  @Override
  public String getTooltip() {
    return "tool.label.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.label.instructions";
  }

  /**
   * Paints the overlay for the given ZoneRenderer using the provided Graphics2D object.
   *
   * @param renderer the ZoneRenderer object used to render the zone
   * @param g the Graphics2D object used for rendering
   */
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (selectedLabel != null && renderer.getLabelBounds(selectedLabel) != null) {
      AppStyle.selectedBorder.paintWithin(g, renderer.getLabelBounds(selectedLabel));
    }
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            if (selectedLabel != null) {
              renderer.getZone().removeLabel(selectedLabel.getId());
              MapTool.serverCommand()
                  .removeLabel(renderer.getZone().getId(), selectedLabel.getId());
              selectedLabel = null;
              repaint();
            }
          }
        });
  }

  ////
  // MOUSE
  @Override
  public void mousePressed(MouseEvent e) {
    Label label = renderer.getLabelAt(e.getX(), e.getY());
    if (label != selectedLabel) {
      selectedNewLabel = true;
      renderer.repaint();
    } else {
      selectedNewLabel = false;
    }
    if (label != null) {
      ScreenPoint sp = ScreenPoint.fromZonePoint(renderer, label.getX(), label.getY());
      dragOffsetX = (int) (e.getX() - sp.x);
      dragOffsetY = (int) (e.getY() - sp.y);
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (!isDragging) {
        Label label = renderer.getLabelAt(e.getX(), e.getY());
        if (label == null) {
          if (selectedLabel == null) {
            ZonePoint zp = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
            label = new Label("", zp.x, zp.y);
            selectedLabel = label;
          } else {
            selectedLabel = null;
            renderer.repaint();
            return;
          }
        } else {
          if (selectedNewLabel) {
            selectedLabel = label;
            renderer.repaint();
            return;
          }
        }
        EditLabelDialog dialog = new EditLabelDialog(label);
        dialog.setVisible(true);

        if (!dialog.isAccepted()) {
          return;
        }
        renderer.getZone().putLabel(label);
      }
      if (selectedLabel != null) {
        MapTool.serverCommand().putLabel(renderer.getZone().getId(), selectedLabel);
        renderer.repaint();
      }
    }
    isDragging = false;
    super.mouseReleased(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    super.mouseDragged(e);
    if (!isDragging) {
      // Setup
      Label label = renderer.getLabelAt(e.getX(), e.getY());
      if (selectedLabel == null || selectedLabel != label) {
        selectedLabel = label;
      }
      if (selectedLabel == null || SwingUtilities.isRightMouseButton(e)) {
        return;
      }
    }
    isDragging = true;

    ZonePoint zp =
        new ScreenPoint(e.getX() - dragOffsetX, e.getY() - dragOffsetY).convertToZone(renderer);

    selectedLabel.setX(zp.x);
    selectedLabel.setY(zp.y);
    renderer.repaint();
  }

  /**
   * The EditLabelDialog class is a dialog box that allows the user to edit a label. It extends the
   * JDialog class and provides functionality for displaying and interacting with the label editing
   * panel.
   */
  public class EditLabelDialog extends JDialog {
    /** The serial version UID. */
    private static final long serialVersionUID = 7621373725343873527L;

    /** Indicates whether the changes have been accepted. */
    private boolean accepted;

    /**
     * Constructs a new EditLabelDialog.
     *
     * @param label the label to be edited
     * @since [version number or first version]
     */
    public EditLabelDialog(Label label) {
      super(MapTool.getFrame(), I18N.getText("tool.label.dialogtitle"), true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      EditLabelPanel panel = new EditLabelPanel(this);
      panel.bind(label);

      add(panel);
      getRootPane().setDefaultButton(panel.getOKButton());
      pack();
    }

    /**
     * Checks if the changes made in the dialog have been accepted.
     *
     * @return true if the changes have been accepted, false otherwise
     */
    public boolean isAccepted() {
      return accepted;
    }

    @Override
    public void setVisible(boolean b) {
      if (b) {
        SwingUtil.centerOver(this, getOwner());
      }
      super.setVisible(b);
    }
  }

  /**
   * EditLabelPanel is a GUI panel used for editing label properties. It extends the AbeillePanel
   * class and provides functionality for binding a Label model, committing changes to the model,
   * and initializing the user interface components.
   */
  public class EditLabelPanel extends AbeillePanel<Label> {
    private static final long serialVersionUID = 3307411310513003924L;

    private final EditLabelDialog dialog;

    public EditLabelPanel(EditLabelDialog dialog) {
      super(new EditLabelDialogView().getRootComponent());

      this.dialog = dialog;
      panelInit();

      getLabelTextField().setSelectionStart(0);
      getLabelTextField().setSelectionEnd(getLabelTextField().getText().length());
      getLabelTextField().setCaretPosition(getLabelTextField().getText().length());
    }

    @Override
    public void bind(Label model) {
      getForegroundColorWell().setColor(model.getForegroundColor());
      getBackgroundColorWell().setColor(model.getBackgroundColor());
      getFontSizeSpinner().setValue(model.getFontSize());
      getBorderColorWell().setColor(model.getBorderColor());
      getBorderWidthSpinner().setValue(model.getBorderWidth());
      getBorderArcSpinner().setValue(model.getBorderArc());
      super.bind(model);
    }

    @Override
    public boolean commit() {
      getModel().setForegroundColor(getForegroundColorWell().getColor());
      getModel().setBackgroundColor(getBackgroundColorWell().getColor());
      getModel().setFontSize(Math.max((Integer) getFontSizeSpinner().getValue(), 6));
      getModel().setBorderColor(getBorderColorWell().getColor());
      getModel().setBorderWidth(Math.max((Integer) getBorderWidthSpinner().getValue(), 0));
      getModel().setBorderArc(Math.max((Integer) getBorderArcSpinner().getValue(), 0));
      return super.commit();
    }

    /**
     * Retrieves the foreground color well component.
     *
     * @return The foreground color well component.
     */
    public ColorWell getForegroundColorWell() {
      return (ColorWell) getComponent("foregroundColor");
    }

    /**
     * Retrieves the background color well component.
     *
     * @return The background color well component.
     */
    public ColorWell getBackgroundColorWell() {
      return (ColorWell) getComponent("backgroundColor");
    }

    /**
     * Retrieves the border color well component.
     *
     * @return The border color well component.
     */
    public ColorWell getBorderColorWell() {
      return (ColorWell) getComponent("borderColor");
    }

    /**
     * Retrieves the border width spinner component from EditLabelPanel.
     *
     * @return The border width spinner component.
     */
    public JSpinner getBorderWidthSpinner() {
      return (JSpinner) getComponent("borderWidth");
    }

    /**
     * Retrieves the border arc spinner component from EditLabelPanel.
     *
     * @return The border arc spinner component.
     */
    public JSpinner getBorderArcSpinner() {
      return (JSpinner) getComponent("borderArc");
    }

    /**
     * Retrieves the font size spinner component from EditLabelPanel.
     *
     * @return The font size spinner component.
     */
    public JSpinner getFontSizeSpinner() {
      return (JSpinner) getComponent("fontSize");
    }

    /**
     * Retrieves the label text field component from EditLabelPanel.
     *
     * @return The label text field component.
     */
    public JTextField getLabelTextField() {
      return (JTextField) getComponent("@label");
    }

    /**
     * Retrieves the OK button component from EditLabelPanel.
     *
     * @return The OK button component.
     */
    public JButton getOKButton() {
      return (JButton) getComponent("okButton");
    }

    /**
     * Initializes the OK button by adding an ActionListener that handles the button click event.
     * Upon clicking the OK button, the dialog's 'accepted' flag is set to true, the commit() method
     * is called, and the dialog is closed.
     */
    public void initOKButton() {
      getOKButton()
          .addActionListener(
              e -> {
                dialog.accepted = true;
                commit();
                close();
              });
    }

    /**
     * Initializes the cancel button by adding an ActionListener that handles the button click
     * event. Upon clicking the cancel button, the dialog is closed.
     */
    public void initCancelButton() {
      ((JButton) getComponent("cancelButton")).addActionListener(e -> close());
    }

    /** Closes the dialog. */
    private void close() {
      dialog.setVisible(false);
    }
  }
}
