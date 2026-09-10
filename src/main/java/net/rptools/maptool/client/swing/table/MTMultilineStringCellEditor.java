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
package net.rptools.maptool.client.swing.table;

import com.jidesoft.combobox.MultilineStringExComboBox;
import com.jidesoft.combobox.PopupPanel;
import com.jidesoft.grid.MultilineStringCellEditor;
import com.jidesoft.plaf.basic.BasicExComboBoxUI;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

/* Pop-up cell editor using RSyntaxTextArea. Used in Token Property editor. */
public class MTMultilineStringCellEditor extends MultilineStringCellEditor {
  private static final Logger log = LogManager.getLogger(MTMultilineStringCellEditor.class);

  public MTMultilineStringExComboBox createMultilineStringComboBox() {
    MTMultilineStringExComboBox localMultilineStringExComboBox = new MTMultilineStringExComboBox();
    localMultilineStringExComboBox.setEditable(true);
    localMultilineStringExComboBox.setUI(new BasicExComboBoxUI());
    return localMultilineStringExComboBox;
  }

  /* needed to change the popup for properties */
  public static class MTMultilineStringExComboBox extends MultilineStringExComboBox {
    final ResourceBundle a = ResourceBundle.getBundle("com.jidesoft.combobox.combobox");

    public ResourceBundle getResourceBundle(Locale paramLocale) {
      return ResourceBundle.getBundle("com.jidesoft.combobox.combobox", paramLocale);
    }

    public PopupPanel createPopupComponent() {
      return new MTMultilineStringPopupPanel(
          getResourceBundle(Locale.getDefault()).getString("ComboBox.multilineStringTitle"));
    }
  }

  /* the property popup table */
  private static class MTMultilineStringPopupPanel extends PopupPanel {
    private final RSyntaxTextArea j = createTextArea();

    public MTMultilineStringPopupPanel() {
      this("");
    }

    public MTMultilineStringPopupPanel(String paramString) {
      this.setResizable(true);
      /* Set the color style via Theme */
      try {
        File themeFile =
            new File(
                AppConstants.THEMES_DIR, AppPreferences.defaultMacroEditorTheme.get() + ".xml");
        Theme theme = Theme.load(new FileInputStream(themeFile));
        theme.apply(j);

        j.revalidate();
      } catch (IOException e) {
        log.error("Error while loading multiline property editor theme", e);
      }
      JScrollPane localJScrollPane = new RTextScrollPane(j);
      localJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      localJScrollPane.setAutoscrolls(true);
      localJScrollPane.setPreferredSize(new Dimension(300, 200));
      setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
      setLayout(new BorderLayout());
      setTitle(paramString);
      add(localJScrollPane, "Center");
      setDefaultFocusComponent(j);
      j.setLineWrap(false);
      JCheckBox wrapToggle = new JCheckBox(I18N.getString("EditTokenDialog.msg.wrap"));
      wrapToggle.addActionListener(e -> j.setLineWrap(!j.getLineWrap()));

      DefaultComboBoxModel<String> syntaxListModel = new DefaultComboBoxModel<>();
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_NONE);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_JSON);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_HTML);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_XML);
      JComboBox<String> syntaxComboBox = new JComboBox<>(syntaxListModel);
      syntaxComboBox.addActionListener(
          e -> j.setSyntaxEditingStyle((String) syntaxComboBox.getSelectedItem()));

      add(syntaxComboBox, BorderLayout.BEFORE_FIRST_LINE);
      add(wrapToggle, BorderLayout.AFTER_LAST_LINE);
    }

    public Object getSelectedObject() {
      return j.getText();
    }

    public void setSelectedObject(Object paramObject) {
      if (paramObject != null) {
        j.setText(paramObject.toString());
      } else {
        j.setText("");
      }
    }

    protected RSyntaxTextArea createTextArea() {
      RSyntaxTextArea textArea = new RSyntaxTextArea();
      textArea.setUseFocusableTips(false);
      textArea.setAnimateBracketMatching(true);
      textArea.setBracketMatchingEnabled(true);
      textArea.setLineWrap(false);
      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
      return textArea;
    }
  }
}
