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
import net.rptools.maptool.language.I18N;

public class TooltipView extends InlineView {

  private boolean mlToolTips;

  /**
   * Constructs a new view wrapped on an element.
   *
   * @param elem the element
   * @param macroLinkToolTips if to show macrolinks as tooltips
   */
  public TooltipView(Element elem, boolean macroLinkToolTips) {
    super(elem);
    mlToolTips = macroLinkToolTips;
  }

  @Override
  public String getToolTipText(float x, float y, Shape allocation) {
    AttributeSet attSet;

    attSet = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.A);
    if (attSet != null) {
      Object attribute = attSet.getAttribute(HTML.Attribute.HREF);
      String href;

      if (attribute != null) {
        href = attribute.toString();
      } else {
        href = I18N.getString("macroLink.error.tooltip.bad.href");
      }

      if (href.startsWith("macro:")) {
        boolean isInsideChat = mlToolTips;
        boolean allowToolTipToShow = !AppPreferences.suppressToolTipsForMacroLinks.get();
        if (isInsideChat && allowToolTipToShow) {
          return MacroLinkFunction.getInstance().macroLinkToolTip(href);
        }
        // if we are not displaying macro link tooltips let if fall through so that any span
        // tooltips will be displayed
      } else {
        return href;
      }
    }

    attSet = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.SPAN);
    if (attSet != null) return (String) attSet.getAttribute(HTML.Attribute.TITLE);

    return null;
  }
}
