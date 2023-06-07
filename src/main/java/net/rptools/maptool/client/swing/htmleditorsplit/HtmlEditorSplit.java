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
package net.rptools.maptool.client.swing.htmleditorsplit;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.*;
import java.beans.JavaBean;
import javax.swing.*;

@JavaBean(defaultProperty = "UI", description = "HTML-Editor that also displays sourcecode")
public class HtmlEditorSplit extends JPanel {
  private HtmlEditorSplitGui gui = new HtmlEditorSplitGui();

  public HtmlEditorSplit() {
    setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    add(
        gui.getRootComponent(),
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null,
            null,
            null,
            0,
            false));
  }

  public String getText() {
    return gui.getText();
  }

  public void setText(String text) {
    gui.setText(text);
  }

  public String getTextType() {
    return gui.getTextStyle();
  }

  public void setTextType(String type) {
    gui.setTextStyle(type);
  }

  public String getSelectedText() {
    return gui.getSelectedText();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    gui.setEnabled(enabled);
  }
}
