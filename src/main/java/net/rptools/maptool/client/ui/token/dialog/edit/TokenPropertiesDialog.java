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
package net.rptools.maptool.client.ui.token.dialog.edit;

import javax.swing.*;
import net.rptools.maptool.client.swing.htmleditorsplit.HtmlEditorSplit;

public class TokenPropertiesDialog {

  private JPanel mainPanel;
  private JPanel headPanel;
  private JPanel buttonPanel;
  private JLabel tokenImage;
  private JTabbedPane tabPanel;
  private HtmlEditorSplit gmNotesEditor;
  private HtmlEditorSplit playerNotesEditor;
  private JComboBox comboBox1;
  private JComboBox comboBox2;
  private JLabel ownershipList;

  public JComponent getRootComponent() {
    return mainPanel;
  }

  public TokenPropertiesDialog() {}

  private void createUIComponents() {
    gmNotesEditor = new HtmlEditorSplit();
    gmNotesEditor.setName("gmNotesEditor");
    playerNotesEditor = new HtmlEditorSplit();
    playerNotesEditor.setName("playerNotesEditor");
  }
}
