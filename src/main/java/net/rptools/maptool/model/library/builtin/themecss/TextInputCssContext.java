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
package net.rptools.maptool.model.library.builtin.themecss;

import javax.swing.UIDefaults;

public class TextInputCssContext {

  private final String foregroundColor;
  private final String backgroundColor;

  private final String disabledBorderSize;

  private final String disabledBorderColor;

  public TextInputCssContext(UIDefaults uiDef) {
    foregroundColor = ThemeCssContext.formatColor(uiDef.getColor("TextField.foreground"));
    backgroundColor = ThemeCssContext.formatColor(uiDef.getColor("TextField.background"));
    disabledBorderSize = "1px";
    disabledBorderColor =
        ThemeCssContext.formatColor(uiDef.getColor("TextField.inactiveForeground"));
  }

  public String getForegroundColor() {
    return foregroundColor;
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public String getDisabledBorderSize() {
    return disabledBorderSize;
  }

  public String getDisabledBorderColor() {
    return disabledBorderColor;
  }
}
