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

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

public class HTMLPaneViewFactory extends HTMLFactory {

  /** The view factory to delegate unknown tags to. */
  private final ViewFactory viewFactory;

  /** The HTML Pane that we belong to, required for processing form events. */
  private final HTMLPane htmlPane;

  /**
   * Creates a new HTMLPaneViewFactory.
   *
   * @param delegate The view factory to delegate unknown tags to.
   * @param formPane The HTMLPane that we are creating tags for.
   */
  public HTMLPaneViewFactory(ViewFactory delegate, HTMLPane formPane) {
    viewFactory = delegate;
    htmlPane = formPane;
  }

  /**
   * Creates a new HTMLPaneViewFactory.
   *
   * @param formPane The HTMLPane that we are creating tags for.
   */
  public HTMLPaneViewFactory(HTMLPane formPane) {
    this(null, formPane);
  }

  /**
   * Creates a view for the specified element.
   *
   * @param element The element to create the view for.
   * @return the view for the element.
   */
  @Override
  public View create(Element element) {
    HTML.Tag tagType =
        (HTML.Tag) element.getAttributes().getAttribute(StyleConstants.NameAttribute);
    View view; // For debugging purposes (no easy way to see a return value in Eclipse)

    if (tagType == HTML.Tag.INPUT || tagType == HTML.Tag.SELECT || tagType == HTML.Tag.TEXTAREA) {
      view = new HTMLPaneFormView(element, htmlPane);
    } else {
      if (viewFactory != null) {
        view = viewFactory.create(element);
      } else {
        view = super.create(element);
      }
    }
    return view;
  }
}
