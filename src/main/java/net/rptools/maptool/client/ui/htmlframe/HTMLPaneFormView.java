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
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import net.rptools.maptool.client.swing.SubmitFormView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Extends SubmitFormView to potentially provide a JScrollPane for TEXTAREA. */
public class HTMLPaneFormView extends SubmitFormView {

  private static final Logger log = LogManager.getLogger(HTMLPaneFormView.class);

  /**
   * Creates a new HTMLPaneFormView.
   *
   * @param elem The element this is a view for.
   * @param pane The HTMLPane this element resides on.
   */
  public HTMLPaneFormView(Element elem, HTMLPane pane) {
    super(elem, pane);
  }

  @Override
  public Logger getLog() {
    return log;
  }

  @Override
  protected Component createComponent() {
    Component c = null;

    AttributeSet attr = getElement().getAttributes();
    HTML.Tag t = (HTML.Tag) attr.getAttribute(StyleConstants.NameAttribute);

    if (t == HTML.Tag.TEXTAREA) {
      JScrollPane sp = (JScrollPane) super.createComponent();
      JTextArea area = (JTextArea) sp.getViewport().getView();
      area.setLineWrap(true);
      area.setWrapStyleWord(true);
      c =
          new JScrollPane(
              area,
              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    } else {
      c = super.createComponent();
    }

    return c;
  }
}
