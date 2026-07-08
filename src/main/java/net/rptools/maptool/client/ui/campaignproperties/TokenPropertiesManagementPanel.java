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
package net.rptools.maptool.client.ui.campaignproperties;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import net.rptools.CaseInsensitiveHashMap;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.TableCellRendererDecorator;
import net.rptools.maptool.client.swing.TextFieldEditorButtonTableCellEditor;
import net.rptools.maptool.client.ui.campaignproperties.TokenPropertiesTableModel.LargeEditableText;
import net.rptools.maptool.client.ui.sheet.stats.StatSheetComboBoxRenderer;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.sheet.stats.StatSheet;
import net.rptools.maptool.model.sheet.stats.StatSheetLocation;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenPropertiesManagementPanel extends AbeillePanel<CampaignProperties> {
  private static final Logger log = LogManager.getLogger(TokenPropertiesManagementPanel.class);
  private Map<String, List<TokenProperty>> tokenTypeMap;
  private final Map<String, StatSheetProperties> tokenTypeStatSheetMap = new HashMap<>();
  private String editingType;

  private final SortedMap<String, String> renameTypes = new TreeMap<>();

  private String defaultPropertyType;

  CampaignProperties campaignProperties;

  public TokenPropertiesManagementPanel() {
    super(new TokenPropertiesManagementPanelView().getRootComponent());

    panelInit();
  }

  public void copyCampaignToUI(CampaignProperties cp) {
    campaignProperties = cp;
    defaultPropertyType = cp.getDefaultTokenPropertyType();

    tokenTypeMap = new HashMap<>();
    campaignProperties
        .getTokenTypeMap()
        .forEach(
            (k, v) ->
                tokenTypeMap.put(k, new ArrayList<>(v.stream().map(TokenProperty::new).toList())));
    var ssManager = new StatSheetManager();
    tokenTypeMap
        .keySet()
        .forEach(
            tt ->
                tokenTypeStatSheetMap.put(tt, campaignProperties.getTokenTypeDefaultStatSheet(tt)));
    updateTypeList();
  }

  public void copyUIToCampaign(Campaign campaign) {

    campaign.getTokenTypeMap().clear();
    campaign.getTokenTypeMap().putAll(tokenTypeMap);
    campaign
        .getTokenTypeMap()
        .keySet()
        .forEach(tt -> campaign.setTokenTypeDefaultSheetId(tt, tokenTypeStatSheetMap.get(tt)));
    campaign.setDefaultTokenPropertyType(defaultPropertyType);
  }

  public void finalizeCellEditing() {
    if (getTokenPropertiesTable().isEditing()) {
      getTokenPropertiesTable().getCellEditor().stopCellEditing();
    }
  }

  public JList getTokenTypeList() {
    JList list = (JList) getComponent("tokenTypeList");
    if (list == null) {
      list = new JList();
    }
    return list;
  }

  public JTextField getTokenTypeName() {
    return (JTextField) getComponent("tokenTypeName");
  }

  public JButton getTypeAddButton() {
    return (JButton) getComponent("typeAddButton");
  }

  public JButton getTypeDeleteButton() {
    return (JButton) getComponent("typeDeleteButton");
  }

  public JButton getTypeDuplicateButton() {
    return (JButton) getComponent("typeDuplicateButton");
  }

  public JButton getPropertyMoveUpButton() {
    return (JButton) getComponent("propertyMoveUpButton");
  }

  public JButton getPropertyMoveDownButton() {
    return (JButton) getComponent("propertyMoveDownButton");
  }

  public JButton getPropertyAddButton() {
    return (JButton) getComponent("propertyAddButton");
  }

  public JButton getPropertyDeleteButton() {
    return (JButton) getComponent("propertyDeleteButton");
  }

  public JTable getTokenPropertiesTable() {
    return (JTable) getComponent("propertiesTable");
  }

  public JComboBox getStatSheetLocationComboBox() {
    return (JComboBox) getComponent("statSheetLocationComboBox");
  }

  public JComboBox getStatSheetComboBox() {
    return (JComboBox) getComponent("statSheetComboBox");
  }

  public JButton getTypeSetAsDefault() {
    return (JButton) getComponent("typeDefaultButton");
  }

  public JButton getHelpButton() {
    return (JButton) getComponent("helpButton");
  }

  public JPanel getDescriptionContainer() {
    return (JPanel) getComponent("descriptionContainer");
  }

  public TokenPropertiesTableModel getTokenPropertiesTableModel() {
    return (TokenPropertiesTableModel) getTokenPropertiesTable().getModel();
  }

  public JScrollPane getTableScrollPane() {
    return (JScrollPane) getComponent("tokenPropertiesTableScrollPane");
  }

  public void initTypeAddButton() {
    var button = getTypeAddButton();
    button.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_NEW));
    button.addActionListener(
        e ->
            EventQueue.invokeLater(
                () -> {
                  // First find a unique name, there are so few entries we don't have to worry
                  // about being fancy
                  int seq = 1;
                  String name =
                      I18N.getText("campaignPropertiesDialog.newTokenTypeDefaultName", seq);
                  while (tokenTypeMap.containsKey(name)) {
                    seq++;
                    name = I18N.getText("campaignPropertiesDialog.newTokenTypeDefaultName", seq);
                  }

                  var newName =
                      (String)
                          JOptionPane.showInputDialog(
                              this,
                              I18N.getText("campaignPropertiesDialog.newTokenTypeName"),
                              I18N.getText("campaignPropertiesDialog.newTokenTypeTitle"),
                              JOptionPane.PLAIN_MESSAGE,
                              null,
                              null,
                              name);
                  if (newName != null) {
                    tokenTypeMap.put(newName, new LinkedList<>());
                    updateTypeList();
                    getTokenTypeList().setSelectedValue(newName, true);
                  }
                }));
  }

  public void initTypeDeleteButton() {
    var button = getTypeDeleteButton();
    button.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_DELETE));
    button.addActionListener(
        e -> {
          var type = (String) getTokenTypeList().getSelectedValue();
          if (type != null) {
            JPanel renameToPanel = new JPanel();
            JComboBox<String> types =
                new JComboBox<>(
                    tokenTypeMap.keySet().stream()
                        .filter(t -> !t.equals(type))
                        .map(String::toString)
                        .toArray(String[]::new));
            renameToPanel.add(
                new JLabel(I18N.getText("campaignPropertiesDialog.tokenTypeNameDeleteMessage")));
            renameToPanel.add(types);
            int option =
                JOptionPane.showConfirmDialog(
                    this,
                    renameToPanel,
                    I18N.getText("campaignPropertiesDialog.tokenTypeNameDeleteTitle", type),
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
              var newType = (String) types.getSelectedItem();
              if (newType != null) {
                renameTypes.put(type, newType);
                tokenTypeMap.remove(type);
                updateTypeList();
              }
            }
          }
        });
    button.setEnabled(false);
  }

  public void initTypeDefaultButton() {
    var button = getTypeSetAsDefault();
    button.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_ACCEPT));
    button.addActionListener(
        l -> {
          var propertyType = (String) getTokenTypeList().getSelectedValue();
          if (propertyType != null) {
            defaultPropertyType = propertyType;
            button.setEnabled(false);
            var delButton = getTypeDeleteButton();
            delButton.setEnabled(false);
          }
        });

    button.setEnabled(false);
  }

  public void initPropertyMoveUpButton() {
    var button = getPropertyMoveUpButton();
    button.addActionListener(
        l -> {
          finalizeCellEditing();
          JTable propertiesTable = getTokenPropertiesTable();
          var selectedRow = propertiesTable.getSelectedRow();
          if (selectedRow <= 0) {
            return;
          }

          var model = getTokenPropertiesTableModel();
          model.movePropertyUp(selectedRow);
          --selectedRow;
          propertiesTable.setRowSelectionInterval(selectedRow, selectedRow);
          propertiesTable.scrollRectToVisible(propertiesTable.getCellRect(selectedRow, 0, true));
        });
    button.setEnabled(false);
  }

  public void initPropertyMoveDownButton() {
    var button = getPropertyMoveDownButton();
    button.addActionListener(
        l -> {
          finalizeCellEditing();
          JTable propertiesTable = getTokenPropertiesTable();
          var selectedRow = propertiesTable.getSelectedRow();
          if (selectedRow < 0 || selectedRow >= propertiesTable.getRowCount() - 1) {
            return;
          }

          var model = getTokenPropertiesTableModel();
          model.movePropertyDown(selectedRow);
          ++selectedRow;
          propertiesTable.setRowSelectionInterval(selectedRow, selectedRow);
          propertiesTable.scrollRectToVisible(propertiesTable.getCellRect(selectedRow, 0, true));
        });
    button.setEnabled(false);
  }

  public void initPropertyAddButton() {
    var button = getPropertyAddButton();
    button.addActionListener(
        e ->
            EventQueue.invokeLater(
                () -> {
                  finalizeCellEditing();
                  JTable propertiesTable = getTokenPropertiesTable();
                  var model = getTokenPropertiesTableModel();
                  // selected row is -1 for no selection causing property to be appended to list
                  // instead of inserted
                  int selectedRow = propertiesTable.getSelectedRow();
                  model.addProperty(selectedRow);
                  int count = model.getRowCount();
                  propertiesTable.scrollRectToVisible(
                      propertiesTable.getCellRect(
                          selectedRow == -1 ? count - 1 : selectedRow, 0, true));
                  propertiesTable.repaint();
                }));
    button.setEnabled(false);
  }

  public void initPropertyDeleteButton() {
    var button = getPropertyDeleteButton();
    button.addActionListener(
        e ->
            EventQueue.invokeLater(
                () -> {
                  finalizeCellEditing();
                  var model = getTokenPropertiesTableModel();
                  model.deleteProperty(getTokenPropertiesTable().getSelectedRow());
                }));
    button.setEnabled(false);
  }

  public void initTypeDuplicateButton() {
    var button = getTypeDuplicateButton();
    button.setIcon(RessourceManager.getSmallIcon(Icons.ACTION_COPY));
    button.addActionListener(
        e ->
            EventQueue.invokeLater(
                () -> {
                  log.info("Type Duplicate - button action");
                  var propertyType = (String) getTokenTypeList().getSelectedValue();
                  if (propertyType != null) {
                    String newName = propertyType + "@";
                    tokenTypeMap.put(newName, tokenTypeMap.get(propertyType));
                    updateTypeList();
                    getTokenTypeList().setSelectedValue(newName, true);
                    button.setEnabled(true);
                  }
                }));
    button.setEnabled(false);
  }

  public void initDescriptionContainer() {
    getDescriptionContainer().setVisible(false);
  }

  public void initHelpButton() {
    var button = getHelpButton();
    button.addActionListener(
        e ->
            EventQueue.invokeLater(
                () -> {
                  JPanel helpText = getDescriptionContainer();
                  helpText.setVisible(!helpText.isVisible());
                  button.setSelected(helpText.isVisible());
                }));
    button.setEnabled(true);
  }

  public void initPropertyTable() {
    var propertyTable = getTokenPropertiesTable();
    propertyTable.setModel(new TokenPropertiesTableModel());
    propertyTable.setDefaultEditor(
        LargeEditableText.class, new TextFieldEditorButtonTableCellEditor());
    propertyTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (e.getValueIsAdjusting()) {
                return;
              }

              var deleteButton = getPropertyDeleteButton();
              deleteButton.setEnabled(getTokenPropertiesTable().getSelectedRow() >= 0);

              var moveUpButton = getPropertyMoveUpButton();
              moveUpButton.setEnabled(getTokenPropertiesTable().getSelectedRow() > 0);

              var moveDownButton = getPropertyMoveDownButton();
              // Note: this works even if selection is empty (getSelectedRow() == -1).
              moveDownButton.setEnabled(
                  getTokenPropertiesTable().getSelectedRow()
                      < getTokenPropertiesTable().getRowCount() - 1);
            });
  }

  public void initTokenTypeName() {
    var field = getTokenTypeName();
    field.setEditable(false);
    field.addActionListener(
        event -> {
          int option =
              JOptionPane.showConfirmDialog(
                  this,
                  I18N.getText("campaignPropertiesDialog.tokenTypeNameChangeWarning"),
                  I18N.getText("campaignPropertiesDialog.tokenTypeNameChangeTitle"),
                  JOptionPane.OK_CANCEL_OPTION,
                  JOptionPane.WARNING_MESSAGE);
          if (option == JOptionPane.OK_OPTION) {
            var ttList = getTokenTypeList();
            var oldName = (String) ttList.getSelectedValue();
            var newName = field.getText();
            tokenTypeMap.put(newName, tokenTypeMap.remove(oldName));
            tokenTypeStatSheetMap.put(newName, tokenTypeStatSheetMap.remove(oldName));
            ttList.setSelectedValue(newName, true);
            updateExistingTokenTypes(oldName, newName);
          }
        });
  }

  private void updateExistingTokenTypes(String oldName, String newName) {
    if (oldName == null || newName == null || oldName.equals(newName)) {
      return;
    }
    if (defaultPropertyType.equals(oldName)) {
      defaultPropertyType = newName;
    }
    renameTypes.put(oldName, newName);
  }

  public void initTypeList() {

    getTokenTypeList()
        .addListSelectionListener(
            e -> {
              if (e.getValueIsAdjusting()) {
                return;
              }

              var propertyType =
                  getTokenTypeList().getSelectedValue() == null
                      ? null
                      : getTokenTypeList().getSelectedValue().toString();

              finalizeCellEditing();

              if (propertyType == null) {
                reset();
                getPropertyAddButton().setEnabled(false);
                getTypeDeleteButton().setEnabled(false);
                getTypeDuplicateButton().setEnabled(false);
                getTokenTypeName().setEditable(false);
                getStatSheetComboBox().setEnabled(false);
                getTypeSetAsDefault().setEnabled(false);
              } else {
                bind((String) getTokenTypeList().getSelectedValue());
                getPropertyAddButton().setEnabled(true);
                getTypeDuplicateButton().setEnabled(true);
                getTokenTypeName().setEditable(true);
                // Can't delete the default property
                if (propertyType.equals(defaultPropertyType)) {
                  getTypeDeleteButton().setEnabled(false);
                } else {
                  getTypeDeleteButton().setEnabled(true);
                }
                getStatSheetComboBox().setEnabled(true);
                populateStatSheetComboBoxes(propertyType);
                if (!propertyType.equals(defaultPropertyType)) {
                  getTypeSetAsDefault().setEnabled(true);
                } else {
                  getTypeSetAsDefault().setEnabled(false);
                }
              }
            });
    getTokenTypeList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getTokenTypeList().setCellRenderer(new TokenTypeCellRenderer());
  }

  private void populateStatSheetComboBoxes(String propertyType) {
    var combo = getStatSheetComboBox();
    combo.removeAllItems();
    var ssManager = new StatSheetManager();
    ssManager.getOrderedStatSheets(propertyType).forEach(ss -> combo.addItem(ss));
    var statSheetProperty = tokenTypeStatSheetMap.get(propertyType);
    String id;
    if (statSheetProperty == null) {
      id = StatSheetManager.LEGACY_STATSHEET.id();
      tokenTypeStatSheetMap.put(propertyType, new StatSheetProperties(id, null));
    } else {
      id = statSheetProperty.id();
    }
    combo.setSelectedItem(ssManager.getStatSheet(id));
  }

  public void initStatSheetDetails() {
    var locationCombo = getStatSheetLocationComboBox();
    locationCombo.setEnabled(false);
    Arrays.stream(StatSheetLocation.values()).forEach(locationCombo::addItem);
    locationCombo.addActionListener(
        l -> {
          if (getStatSheetLocationComboBox().hasFocus()) { // only if user has made change
            var location = (StatSheetLocation) locationCombo.getSelectedItem();
            var tokenType = (String) getTokenTypeList().getSelectedValue();
            if (location != null && tokenType != null) {
              var id = tokenTypeStatSheetMap.get(tokenType).id();
              tokenTypeStatSheetMap.put(tokenType, new StatSheetProperties(id, location));
            }
          }
        });

    var combo = getStatSheetComboBox();
    combo.setEnabled(false);
    combo.setRenderer(new StatSheetComboBoxRenderer());
    final var ssManager = new StatSheetManager();
    combo.addActionListener(
        l -> {
          var ss = (StatSheet) combo.getSelectedItem();
          if (getStatSheetComboBox().hasFocus()) { // Only if user has made change
            var tokenType = (String) getTokenTypeList().getSelectedValue();
            if (ss != null && tokenType != null) {
              var id = ss.id();
              var location = tokenTypeStatSheetMap.get(tokenType).location();
              tokenTypeStatSheetMap.put(tokenType, new StatSheetProperties(id, location));
              getStatSheetLocationComboBox().setSelectedItem(location);
            }
          }
          if (ssManager.isLocationUserSettable(ss)) {
            getStatSheetLocationComboBox().setEnabled(true);
          } else {
            getStatSheetLocationComboBox().setEnabled(false);
            getStatSheetLocationComboBox().setSelectedItem(null);
          }
        });
  }

  private void bind(String type) {

    editingType = type;

    getTokenTypeName().setText(type != null ? type : "");
    var model = getTokenPropertiesTableModel();
    model.setPropertyType(type);
  }

  private void reset() {

    bind((String) null);
  }

  private void updateTypeList() {
    getTokenTypeList().setModel(new TypeListModel());
    getTokenPropertiesTableModel().setPropertyTypeMap(tokenTypeMap);
  }

  private String compileTokenProperties(List<TokenProperty> propertyList) {

    // Sanity
    if (propertyList == null) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    for (TokenProperty property : propertyList) {
      if (property.isShowOnStatSheet()) {
        builder.append("*");
      }
      if (property.isOwnerOnly()) {
        builder.append("@");
      }
      if (property.isGMOnly()) {
        builder.append("#");
      }
      builder.append(property.getName());
      if (property.getShortName() != null) {
        builder.append(" (").append(property.getShortName()).append(")");
      }
      if (property.getDefaultValue() != null) {
        builder.append(":").append(property.getDefaultValue());
      }
      builder.append("\n");
    }

    return builder.toString();
  }

  /**
   * Given a string (normally from the JTextArea which holds the properties for a Property Type)
   * this method converts those lines into a List of EditTokenProperty objects. It checks for
   * duplicates along the way, ignoring any it finds. (Should produce a list of warnings to indicate
   * which ones are duplicates. See the Light/Sight code for examples.)
   *
   * @param propertyText
   * @return
   */
  private List<TokenProperty> parseTokenProperties(String propertyText)
      throws IllegalArgumentException {
    List<TokenProperty> propertyList = new ArrayList<TokenProperty>();
    BufferedReader reader = new BufferedReader(new StringReader(propertyText));
    CaseInsensitiveHashMap<String> caseCheck = new CaseInsensitiveHashMap<String>();
    List<String> errlog = new LinkedList<String>();
    try {
      String original, line;
      while ((original = reader.readLine()) != null) {
        line = original = original.trim();
        if (line.length() == 0) {
          continue;
        }

        TokenProperty property = new TokenProperty();

        // Prefix
        while (true) {
          if (line.startsWith("*")) {
            property.setShowOnStatSheet(true);
            line = line.substring(1);
            continue;
          }
          if (line.startsWith("@")) {
            property.setOwnerOnly(true);
            line = line.substring(1);
            continue;
          }
          if (line.startsWith("#")) {
            property.setGMOnly(true);
            line = line.substring(1);
            continue;
          }

          // Ran out of special characters
          break;
        }

        // default value
        // had to do this here since the short name is not built
        // to take advantage of multiple opening/closing parenthesis
        // in a single property line
        int indexDefault = line.indexOf(':');
        if (indexDefault > 0) {
          String defaultVal = line.substring(indexDefault + 1).trim();
          if (defaultVal.length() > 0) {
            property.setDefaultValue(defaultVal);
          }

          // remove the default value from the end of the string...
          line = line.substring(0, indexDefault);
        }
        // Suffix
        // (Really should handle nested parens here)
        int index = line.indexOf('(');
        if (index > 0) {
          int indexClose = line.lastIndexOf(')');
          // Check for unenclosed parentheses. Fix #1575.
          if (indexClose < index) {
            MapTool.showError(I18N.getText("CampaignPropertyDialog.error.parenthesis", line));
            throw new IllegalArgumentException("Missing parenthesis");
          }
          String shortName = line.substring(index + 1, indexClose).trim();
          if (shortName.length() > 0) {
            property.setShortName(shortName);
          }
          line = line.substring(0, index);
        }
        line = line.trim();
        property.setName(line);
        // Since property names are not case-sensitive, let's make sure that we don't
        // already have this name represented somewhere in the list.
        String old = caseCheck.get(line);
        if (old != null) {
          // Perhaps these properties should produce warnings at all, but what it someone
          // is actually <b>using them as property names!</b>
          if (old.startsWith("---"))
            errlog.add(
                I18N.getText("msg.error.mtprops.properties.duplicateComment", original, old));
          else errlog.add(I18N.getText("msg.error.mtprops.properties.duplicate", original, old));
        } else {
          propertyList.add(property);
          caseCheck.put(line, original);
        }
      }

    } catch (IOException ioe) {
      // If this happens, I'll check into the nearest insane asylum
      MapTool.showError("IOException during parsing of properties?!", ioe);
    }
    caseCheck.clear();
    if (!errlog.isEmpty()) {
      errlog.add(0, I18N.getText("msg.error.mtprops.properties.title", editingType));
      errlog.add(I18N.getText("msg.error.mtprops.properties.ending"));
      MapTool.showFeedback(errlog.toArray());
      errlog.clear();
      throw new IllegalArgumentException(); // Don't save the properties...
    }
    return propertyList;
  }

  public void prettify() {
    /* fix text areas to look like labels
     * dig down to the appropriate container level
     * then set the backgrounds to transparent
     */
    JPanel jPanel = getDescriptionContainer();
    Arrays.stream(jPanel.getComponents())
        .flatMap(
            c -> c instanceof JPanel panel ? Arrays.stream(panel.getComponents()) : Stream.empty())
        .forEach(
            c -> {
              if (c instanceof JTextField) {
                c.setBackground(c.getParent().getBackground());
              }
            });

    JTable propertyTable = getTokenPropertiesTable();
    TokenPropertiesTableModel model = (TokenPropertiesTableModel) propertyTable.getModel();

    propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    propertyTable.getTableHeader().setResizingAllowed(true);

    // Custom header that uses the column model to decide tooltips.
    var header =
        new JTableHeader(propertyTable.getColumnModel()) {
          @Override
          public String getToolTipText(MouseEvent event) {
            int columnIndex = columnAtPoint(event.getPoint());
            return model.getColumnTooltipText(columnIndex);
          }
        };
    propertyTable.setTableHeader(header);

    // The custom renderer delegates to the default one.
    var customHeaderRenderer = new TableCellRendererDecorator(header.getDefaultRenderer());
    customHeaderRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    customHeaderRenderer.setVerticalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < propertyTable.getColumnCount(); i++) {
      var column = propertyTable.getColumnModel().getColumn(i);

      column.setHeaderRenderer(customHeaderRenderer);
    }
  }

  private class TypeListModel extends AbstractListModel {
    public Object getElementAt(int index) {
      List<String> names = new ArrayList<String>(tokenTypeMap.keySet());
      Collections.sort(names);
      return names.get(index);
    }

    public int getSize() {
      return tokenTypeMap.size();
    }
  }

  /**
   * Gets the Token Property Type rename operations that have occurred.
   *
   * @return a {@link Map} of renames.
   */
  public SortedMap<String, String> getRenameTypes() {
    return renameTypes;
  }

  /** A List cell renderer that calls out default property type. */
  private class TokenTypeCellRenderer extends JLabel implements ListCellRenderer {

    public TokenTypeCellRenderer() {
      setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      var val = value.toString();
      if (val.equals(defaultPropertyType)) {
        setText(
            "<html>"
                + val
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>("
                + I18N.getString("TokenPropertiesPanel.defaultPropertyType")
                + ")</i></html>");
      } else {
        setText("<html>" + val + "</html>");
      }

      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      return this;
    }
  }
}
