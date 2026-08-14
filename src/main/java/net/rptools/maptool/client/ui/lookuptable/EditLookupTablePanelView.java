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
package net.rptools.maptool.client.ui.lookuptable;

import javax.swing.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class EditLookupTablePanelView {

  private JPanel mainPanel;
  private JTextField tableName;
  private JComboBox<String> tableGroup;
  private JTextField defaultTableRoll;
  private JCheckBox isVisible;
  private JCheckBox allowLookup;
  private JTable definitionTable;
  private JLabel tableImagePlaceholder;
  private JCheckBox pickOnce;
  private JButton resetPicks;
  private JLabel isVisibleIcon;
  private JLabel allowLookupIcon;
  private JLabel pickOnceIcon;
  private RSyntaxTextArea tableMetadata;
  private RTextScrollPane tableMetadataScrollPane;

  public JComponent getRootComponent() {
    return mainPanel;
  }

  public JTextField getTableName() {
    return tableName;
  }

  public JComboBox<String> getTableGroup() {
    return tableGroup;
  }

  public JTextField getDefaultTableRoll() {
    return defaultTableRoll;
  }

  public JCheckBox getIsVisible() {
    return isVisible;
  }

  public JCheckBox getAllowLookup() {
    return allowLookup;
  }

  public JTable getDefinitionTable() {
    return definitionTable;
  }

  public JLabel getTableImagePlaceholder() {
    return tableImagePlaceholder;
  }

  public JCheckBox getPickOnce() {
    return pickOnce;
  }

  public JButton getResetPicks() {
    return resetPicks;
  }

  public JLabel getIsVisibleIcon() {
    return isVisibleIcon;
  }

  public JLabel getAllowLookupIcon() {
    return allowLookupIcon;
  }

  public JLabel getPickOnceIcon() {
    return pickOnceIcon;
  }

  public RSyntaxTextArea getTableMetadata() {
    return tableMetadata;
  }

  public RTextScrollPane getTableMetadataScrollPane() {
    return tableMetadataScrollPane;
  }
}
