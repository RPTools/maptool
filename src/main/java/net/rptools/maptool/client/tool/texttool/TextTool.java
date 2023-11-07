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

/** */
public class TextTool extends DefaultTool implements ZoneOverlay {
  private static final long serialVersionUID = -8944323545051996907L;

  private Label selectedLabel;

  private int dragOffsetX;
  private int dragOffsetY;
  private boolean isDragging;
  private boolean selectedNewLabel;

  public TextTool() {}

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

  public class EditLabelDialog extends JDialog {
    private static final long serialVersionUID = 7621373725343873527L;

    private boolean accepted;

    public EditLabelDialog(Label label) {
      super(MapTool.getFrame(), I18N.getText("tool.label.dialogtitle"), true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      EditLabelPanel panel = new EditLabelPanel(this);
      panel.bind(label);

      add(panel);
      getRootPane().setDefaultButton(panel.getOKButton());
      pack();
    }

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
      getColorWell().setColor(model.getForegroundColor());
      super.bind(model);
    }

    @Override
    public boolean commit() {
      getModel().setForegroundColor(getColorWell().getColor());
      return super.commit();
    }

    public ColorWell getColorWell() {
      return (ColorWell) getComponent("foregroundColor");
    }

    public JTextField getLabelTextField() {
      return (JTextField) getComponent("@label");
    }

    public JButton getOKButton() {
      return (JButton) getComponent("okButton");
    }

    public void initOKButton() {
      getOKButton()
          .addActionListener(
              e -> {
                dialog.accepted = true;
                commit();
                close();
              });
    }

    public void initCancelButton() {
      ((JButton) getComponent("cancelButton")).addActionListener(e -> close());
    }

    private void close() {
      dialog.setVisible(false);
    }
  }
}
