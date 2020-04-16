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
import com.jeta.forms.gui.form.FormComponent;
import com.jeta.forms.gui.form.GridView;
import java.awt.*;
import java.util.Iterator;
import javax.swing.*;
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
  private static void translateComponent(Component comp) {
    if (comp instanceof JETALabel) {
      JETALabel label = (JETALabel) comp;
      label.setText(I18N.getText(label.getText()));
      String tooltip = label.getToolTipText();
      if (tooltip != null) {
        label.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JCheckBox) {
      JCheckBox checkBox = (JCheckBox) comp;
      checkBox.setText(I18N.getText(checkBox.getText()));
      String tooltip = checkBox.getToolTipText();
      if (tooltip != null) {
        checkBox.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JButton) {
      JButton jButton = (JButton) comp;
      jButton.setText(I18N.getText(jButton.getText()));
    } else if (comp instanceof JTabbedPane) {
      JTabbedPane jTabbedPane = (JTabbedPane) comp;
      for (int i = 0; i < jTabbedPane.getTabRunCount(); i += 1) {
        // Translate the tab titles
        jTabbedPane.setTitleAt(i, I18N.getText(jTabbedPane.getTitleAt(i)));
      }
      for (Component subComp : jTabbedPane.getComponents()) {
        // Recursively translate the sub components
        translateComponent(subComp);
      }
    } else if (comp instanceof GridView) {
      Iterator<?> iter = ((GridView) comp).beanIterator();
      while (iter.hasNext()) {
        // Recursively translate the sub components
        translateComponent((Component) iter.next());
      }
    } else if (comp instanceof FormComponent) {
      // Translate the GridView inside the form
      FormComponent form = (FormComponent) comp;
      translateComponent(form.getChildView());
    } else if (comp instanceof JScrollPane) {
      JScrollPane jScrollPane = (JScrollPane) comp;
      // Translate the child of the scroll pane
      translateComponent(jScrollPane.getViewport().getView());
    }
  }
}
