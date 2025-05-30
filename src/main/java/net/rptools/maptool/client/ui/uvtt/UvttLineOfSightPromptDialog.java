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
package net.rptools.maptool.client.ui.uvtt;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppPreferences.UvttLosImportType;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.language.I18N;

public class UvttLineOfSightPromptDialog extends JDialog {
  private final UvttLineOfSightPromptView view;
  private Choices result = null;

  public UvttLineOfSightPromptDialog(JFrame owner) {
    super(owner, I18N.getString("uvttLosPromptDialog.title"), true);
    view = new UvttLineOfSightPromptView();

    setLayout(new GridLayout());

    var root = view.getRootComponent();

    // Escape key
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    root.getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                cancel();
              }
            });
    add(root);

    final var uvttLosImportType = view.getSelectionComboBox();
    uvttLosImportType.setModel(new DefaultComboBoxModel<>(Choices.values()));
    uvttLosImportType.setSelectedItem(UvttLosImportType.Walls);

    final var okButton = view.getOkButton();
    okButton.addActionListener(e -> accept());
    getRootPane().setDefaultButton(okButton);

    final var cancelButton = view.getCancelButton();
    cancelButton.addActionListener(e -> cancel());

    pack();
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      result = null;
      SwingUtil.centerOver(this, getOwner());
    }
    super.setVisible(b);
  }

  public Optional<UvttLosImportType> getResult() {
    return result == null ? Optional.empty() : Optional.of(result.getWrappedType());
  }

  private void cancel() {
    setVisible(false);
    result = null;
  }

  private void accept() {
    setVisible(false);
    result = view.getSelectionComboBox().getItemAt(view.getSelectionComboBox().getSelectedIndex());
    if (view.getRememberChoice().isSelected()) {
      AppPreferences.uvttLosImportType.set(result.getWrappedType());
    }
  }

  /** Wrapper around {@link UvttLosImportType} for better text and only two options. */
  public enum Choices {
    Walls(UvttLosImportType.Walls, "uvttLosPromptDialog.choice.walls"),
    Masks(UvttLosImportType.Masks, "uvttLosPromptDialog.choice.masks");

    private final UvttLosImportType wrappedType;
    private final String description;

    Choices(UvttLosImportType wrappedType, String key) {
      this.wrappedType = wrappedType;
      this.description = I18N.getString(key);
    }

    public UvttLosImportType getWrappedType() {
      return wrappedType;
    }

    @Override
    public String toString() {
      return description;
    }
  }
}
