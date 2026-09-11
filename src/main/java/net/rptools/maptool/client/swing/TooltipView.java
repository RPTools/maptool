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

  private final boolean mlToolTips;

  /**
   * Constructs a new view wrapped on an element.
   *
   * @param elem the element
   * @param macroLinkToolTips if to show macroLinks as tooltips
   */
  public TooltipView(Element elem, boolean macroLinkToolTips) {
    super(elem);
    mlToolTips = macroLinkToolTips;
  }

  @Override
  public String getToolTipText(float x, float y, Shape allocation) {
    boolean isInsideChat = mlToolTips;
    boolean showTitleAsTooltip = AppPreferences.suppressToolTipsForMacroLinks.get();
    boolean isMacroLink = false;
    AttributeSet attSet = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.A);
    String href;
    String title = null;

    if (attSet != null) {
      Object attribute = attSet.getAttribute(HTML.Attribute.HREF);

      if (attribute != null) {
        href = attribute.toString();
        isMacroLink =
            href.toLowerCase().startsWith("macro:")
                || (href.toLowerCase().startsWith("lib:")
                    && href.toLowerCase().contains("/macro/"));
      } else {
        href = I18N.getString("macroLink.error.tooltip.bad.href");
      }

      attribute = attSet.getAttribute(HTML.Attribute.TITLE);
      if (attribute != null) {
        title = attribute.toString();
      }

      if (isInsideChat) {
        if (!isMacroLink || showTitleAsTooltip) {
          // not using anti-cheat tooltip, or not a macroLink, i.e. not suppress tooltip
          if (title != null) {
            return title;
          }
        } else if (href.toLowerCase().startsWith("macro:")) {
          // use anti-cheat tooltip, i.e. suppress normal tooltip
          return MacroLinkFunction.getInstance().macroLinkToolTip(href);
        } else if (href.toLowerCase().startsWith("lib:")) {
          // no tooltip creation function available yet, just show the URL
          return href;
        }
      }
    }
    // first fallback - use title
    if (title != null) {
      return title;
    }
    // second fallback - span tag
    attSet = (AttributeSet) getElement().getAttributes().getAttribute(HTML.Tag.SPAN);
    if (attSet != null) {
      return (String) attSet.getAttribute(HTML.Attribute.TITLE);
    }
    // nothing to show
    return null;
  }
}
