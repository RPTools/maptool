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

import java.awt.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.functions.MacroLinkFunction;

public class TooltipView extends InlineView {

  private boolean mlToolTips;

  /**
   * Constructs a new view wrapped on an element.
   *
   * @param elem the element
   */
  public TooltipView(Element elem, boolean macroLinkToolTips) {
    super(elem);
    mlToolTips = macroLinkToolTips;
  }

  @Override
  public String getToolTipText(float x, float y, Shape allocation) {
    AttributeSet att;

    att = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.A);
    if (att != null) {
      String href = att.getAttribute(HTML.Attribute.HREF).toString();
      if (href.startsWith("macro:")) {
        boolean isInsideChat = mlToolTips;
        boolean allowToolTipToShow = !AppPreferences.getSuppressToolTipsForMacroLinks();
        if (isInsideChat && allowToolTipToShow) {
          return MacroLinkFunction.getInstance().macroLinkToolTip(href);
        }
        // if we are not displaying macro link tooltips let if fall through so that any span
        // tooltips will be displayed
      } else {
        return href;
      }
    }

    att = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.SPAN);
    if (att != null) return (String) att.getAttribute(HTML.Attribute.TITLE);

    return null;
  }
}
