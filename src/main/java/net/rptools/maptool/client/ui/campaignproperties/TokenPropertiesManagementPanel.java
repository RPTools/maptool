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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.rptools.CaseInsensitiveHashMap;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.TokenProperty;

public class TokenPropertiesManagementPanel extends AbeillePanel<CampaignProperties> {

  private Map<String, List<TokenProperty>> tokenTypeMap;
  private String editingType;

  public TokenPropertiesManagementPanel() {
    super("net/rptools/maptool/client/ui/forms/tokenPropertiesManagementPanel.xml");

    panelInit();
  }

  public void copyCampaignToUI(CampaignProperties campaignProperties) {

    tokenTypeMap = new HashMap<String, List<TokenProperty>>(campaignProperties.getTokenTypeMap());

    updateTypeList();
  }

  public void copyUIToCampaign(Campaign campaign) {

    campaign.getTokenTypeMap().clear();
    campaign.getTokenTypeMap().putAll(tokenTypeMap);
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

  public JButton getNewButton() {
    return (JButton) getComponent("newButton");
  }

  public JButton getUpdateButton() {
    return (JButton) getComponent("updateButton");
  }

  public JButton getRevertButton() {
    return (JButton) getComponent("revertButton");
  }

  public JTextArea getTokenPropertiesArea() {
    return (JTextArea) getComponent("tokenProperties");
  }

  public void initUpdateButton() {
    getUpdateButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                update();
              }
            });
  }

  public void initNewButton() {
    getNewButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {

                EventQueue.invokeLater(
                    new Runnable() {
                      public void run() {
                        // This will force a reset
                        getTokenTypeList().getSelectionModel().clearSelection();
                        reset();
                      }
                    });
              }
            });
  }

  public void initRevertButton() {
    getRevertButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                bind(editingType);
              }
            });
  }

  public void initTypeList() {

    getTokenTypeList()
        .addListSelectionListener(
            new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                  return;
                }

                if (getTokenTypeList().getSelectedValue() == null) {
                  reset();
                } else {
                  bind((String) getTokenTypeList().getSelectedValue());
                }
              }
            });
    getTokenTypeList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  private void bind(String type) {

    editingType = type;

    getTokenTypeName().setText(type != null ? type : "");
    getTokenTypeName().setEditable(!CampaignProperties.DEFAULT_TOKEN_PROPERTY_TYPE.equals(type));
    getTokenPropertiesArea()
        .setText(type != null ? compileTokenProperties(tokenTypeMap.get(type)) : "");
  }

  private void update() {

    // Pull the old one out and put the new one in (rename)
    List<TokenProperty> current;
    try {
      // If an exception occurs here, the GUI goes back into editing of the text.
      current = parseTokenProperties(getTokenPropertiesArea().getText());

      tokenTypeMap.remove(editingType);
      tokenTypeMap.put(getTokenTypeName().getText().trim(), current);
      reset();
      updateTypeList();
    } catch (IllegalArgumentException e) {
      // Don't need to do anything here...
    }
  }

  private void reset() {

    bind((String) null);
  }

  private void updateTypeList() {

    getTokenTypeList().setModel(new TypeListModel());
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
        int indexDefault = line.indexOf(":");
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
        int index = line.indexOf("(");
        if (index > 0) {
          String shortName = line.substring(index + 1, line.lastIndexOf(")")).trim();
          if (shortName.length() > 0) {
            property.setShortName(shortName);
          }
          line = line.substring(0, index).trim();
        }
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
}
