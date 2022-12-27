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
package net.rptools.maptool.client.swing;

import com.jeta.forms.components.label.JETALabel;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;
import com.jeta.forms.store.properties.ListItemProperty;
import java.awt.*;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import net.rptools.maptool.language.I18N;

/** A FormPanel that supports I18N text. */
public class FormPanelI18N extends FormPanel {
  public FormPanelI18N(String s) {
    super(s);
    FormAccessor form_accessor = getFormAccessor();

    Iterator<?> iter = form_accessor.beanIterator();
    while (iter.hasNext()) {
      translateComponent((Component) iter.next());
    }
  }

  /**
   * Recursively translate a component and its subcomponents.
   *
   * @param comp the component to be translated
   */
  protected static void translateComponent(Component comp) {
    if (comp instanceof JLabel jLabel) {
      jLabel.setText(I18N.getText(jLabel.getText()));
      String tooltip = jLabel.getToolTipText();
      if (tooltip != null) {
        jLabel.setToolTipText(I18N.getText(tooltip));
      }
    }
    if (comp instanceof JETALabel) {
      JETALabel label = (JETALabel) comp;
      label.setText(I18N.getText(label.getText()));
    } else if (comp instanceof AbstractButton) {
      // Includes JToggleButton, JCheckBox, JButton
      AbstractButton jButton = (AbstractButton) comp;
      jButton.setText(I18N.getText(jButton.getText()));
    } else if (comp instanceof JComboBox jComboBox) {
      for (int i = 0; i < jComboBox.getItemCount(); ++i) {
        var comboBoxItem = jComboBox.getItemAt(i);
        if (comboBoxItem instanceof ListItemProperty itemProperty) {
          itemProperty.setLabel(I18N.getText(itemProperty.getLabel()));
        } else if (comboBoxItem instanceof String string) {
          jComboBox.removeItemAt(i);
          jComboBox.insertItemAt(I18N.getText(string), i);
        } else {
          throw new RuntimeException(
              "Untranslated type of JComboBox item: " + comboBoxItem.getClass().getName());
        }
      }
    } else if (comp instanceof JTabbedPane) {
      JTabbedPane jTabbedPane = (JTabbedPane) comp;
      for (int i = 0; i < jTabbedPane.getTabRunCount(); i += 1) {
        jTabbedPane.setTitleAt(i, I18N.getText(jTabbedPane.getTitleAt(i)));
      }
    }
    if (comp instanceof JComponent jComponent) {
      var border = jComponent.getBorder();
      if (border instanceof TitledBorder) {
        TitledBorder titledBorder = (TitledBorder) border;
        titledBorder.setTitle(I18N.getText(titledBorder.getTitle()));
      }
      String tooltip = jComponent.getToolTipText();
      if (tooltip != null) {
        jComponent.setToolTipText(I18N.getText(tooltip));
      }
      for (Component c : jComponent.getComponents()) {
        translateComponent(c);
      }
    }
  }
}
