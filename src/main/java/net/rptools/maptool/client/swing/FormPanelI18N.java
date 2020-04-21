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

import com.jeta.forms.components.colors.JETAColorWell;
import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.label.JETALabel;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;
import com.jeta.forms.gui.form.FormComponent;
import com.jeta.forms.gui.form.GridView;
import java.awt.*;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.Border;
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
  private static void translateComponent(Component comp) {
    if (comp instanceof JETALabel) {
      JETALabel label = (JETALabel) comp;
      label.setText(I18N.getText(label.getText()));
      String tooltip = label.getToolTipText();
      if (tooltip != null) {
        label.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof AbstractButton) {
      // Includes JToggleButton, JCheckBox, JButton
      AbstractButton jButton = (AbstractButton) comp;
      jButton.setText(I18N.getText(jButton.getText()));
      String tooltip = jButton.getToolTipText();
      if (tooltip != null) {
        jButton.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JSpinner) {
      JSpinner jSpin = (JSpinner) comp;
      String tooltip = jSpin.getToolTipText();
      if (tooltip != null) {
        jSpin.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JComboBox) {
      JComboBox<?> jComboBox = (JComboBox<?>) comp;
      String tooltip = jComboBox.getToolTipText();
      if (tooltip != null) {
        jComboBox.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JTextField) {
      JTextField jTextField = (JTextField) comp;
      String tooltip = jTextField.getToolTipText();
      if (tooltip != null) {
        jTextField.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JList) {
      JList<?> jList = (JList<?>) comp;
      String tooltip = jList.getToolTipText();
      if (tooltip != null) {
        jList.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof JETAColorWell) {
      JETAColorWell jetaColorWell = (JETAColorWell) comp;
      String tooltip = jetaColorWell.getToolTipText();
      if (tooltip != null) {
        jetaColorWell.setToolTipText(I18N.getText(tooltip));
      }
    } else if (comp instanceof ImageComponent) {
      ImageComponent jImage = (ImageComponent) comp;
      String tooltip = jImage.getToolTipText();
      if (tooltip != null) {
        jImage.setToolTipText(I18N.getText(tooltip));
      }
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
      GridView gridView = (GridView) comp;
      Iterator<?> iter = gridView.beanIterator();
      while (iter.hasNext()) {
        // Recursively translate the sub components
        translateComponent((Component) iter.next());
      }
      Border border = gridView.getBorder();
      // Translate the titled border, if any
      if (border instanceof TitledBorder) {
        TitledBorder titledBorder = (TitledBorder) border;
        titledBorder.setTitle(I18N.getText(titledBorder.getTitle()));
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
