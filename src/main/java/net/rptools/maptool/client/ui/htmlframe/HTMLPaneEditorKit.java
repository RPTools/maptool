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

import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import net.rptools.maptool.client.swing.MessagePanelEditorKit;

@SuppressWarnings("serial")
class HTMLPaneEditorKit extends MessagePanelEditorKit {
  private final HTMLPaneViewFactory viewFactory;

  HTMLPaneEditorKit(HTMLPane htmlPane) {
    setUseMacroLinkToolTips(false);
    viewFactory = new HTMLPaneViewFactory(super.getViewFactory(), htmlPane);
  }

  @Override
  public ViewFactory getViewFactory() {
    return viewFactory;
  }

  @Override
  public HTMLEditorKit.Parser getParser() {
    return super.getParser();
  }
}
