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

public class ButtonCssContext {

  private final String foregroundColor;
  private final String startBackgroundColor;
  private final String endBackgroundColor;

  private final String pressedBackgroundColor;

  private final String disabledBackgroundColor;

  private final String disabledForegroundColor;

  private final String disabledBorderSize;

  private final String disabledBorderColor;

  private final String hoverBackgroundColor;

  public ButtonCssContext(UIDefaults uiDef) {
    foregroundColor = ThemeCssContext.formatColor(uiDef.getColor("Button.foreground"));
    startBackgroundColor = ThemeCssContext.formatColor(uiDef.getColor("Button.startBackground"));
    endBackgroundColor = ThemeCssContext.formatColor(uiDef.getColor("Button.endBackground"));
    pressedBackgroundColor =
        ThemeCssContext.formatColor(uiDef.getColor("Button.pressedBackground"));
    disabledBackgroundColor =
        ThemeCssContext.formatColor(uiDef.getColor("Button.disabledBackground"));
    disabledForegroundColor =
        ThemeCssContext.formatColor(uiDef.getColor("Button.disabledForeground"));
    disabledBorderSize = uiDef.getInt("Button.disabledBorderSize") + "px";
    disabledBorderColor = ThemeCssContext.formatColor(uiDef.getColor("Button.disabledBorderColor"));
    hoverBackgroundColor = ThemeCssContext.formatColor(uiDef.getColor("Button.hoverBackground"));
  }

  public String getForegroundColor() {
    return foregroundColor;
  }

  public String getStartBackgroundColor() {
    return startBackgroundColor;
  }

  public String getEndBackgroundColor() {
    return endBackgroundColor;
  }

  public String getPressedBackgroundColor() {
    return pressedBackgroundColor;
  }

  public String getDisabledBackgroundColor() {
    return disabledBackgroundColor;
  }

  public String getDisabledForegroundColor() {
    return disabledForegroundColor;
  }

  public String getDisabledBorderSize() {
    return disabledBorderSize;
  }

  public String getDisabledBorderColor() {
    return disabledBorderColor;
  }

  public String getHoverBackgroundColor() {
    return hoverBackgroundColor;
  }
}
