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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class UvttLineOfSightPromptView {
  private JComboBox<UvttLineOfSightPromptDialog.Choices> selectionComboBox;
  private JPanel mainPanel;
  private JButton okButton;
  private JCheckBox rememberChoice;
  private JButton cancelButton;

  public JPanel getRootComponent() {
    return mainPanel;
  }

  public JComboBox<UvttLineOfSightPromptDialog.Choices> getSelectionComboBox() {
    return selectionComboBox;
  }

  public JButton getOkButton() {
    return okButton;
  }

  public JButton getCancelButton() {
    return cancelButton;
  }

  public JCheckBox getRememberChoice() {
    return rememberChoice;
  }
}
