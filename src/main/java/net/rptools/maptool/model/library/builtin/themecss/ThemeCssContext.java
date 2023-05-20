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

import java.awt.Color;
import javax.swing.UIManager;

public class ThemeCssContext {

  private final String fontSize;

  private final String fontFamily;

  private final String backgroundColor;

  private final String foregroundColor;

  private final String foregroundColorDisabled;

  private final ButtonCssContext button;

  private final TextInputCssContext textInput;

  private final ColorCssContext themeColor;

  public ThemeCssContext() {
    var uiDef = UIManager.getDefaults();
    backgroundColor = formatColor(uiDef.getColor("Label.background"));
    foregroundColor = formatColor(uiDef.getColor("Label.foreground"));
    foregroundColorDisabled = formatColor(uiDef.getColor("Label.disabledForeground"));
    var font = uiDef.getFont("Label.font");
    fontFamily = font.getFamily();
    fontSize = font.getSize() + "px";
    button = new ButtonCssContext(uiDef);
    textInput = new TextInputCssContext(uiDef);
    themeColor = new ColorCssContext(uiDef);
  }

  static String formatColor(Color color) {
    return String.format(
        "rgba(%d, %d, %d, %.02f)",
        color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255.0);
  }

  public String getFontSize() {
    return fontSize;
  }

  public String getFontFamily() {
    return fontFamily;
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public String getForegroundColor() {
    return foregroundColor;
  }

  public ButtonCssContext getButton() {
    return button;
  }

  public TextInputCssContext getTextInput() {
    return textInput;
  }

  public ColorCssContext getThemeColor() {
    return themeColor;
  }
}
