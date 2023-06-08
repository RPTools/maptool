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

/** Context for the theme CSS. This is used to provide the CSS with the values it needs to render */
public class ThemeCssContext {

  /** The font size to use for the theme. */
  private final String fontSize;

  /** The font family to use for the theme. */
  private final String fontFamily;

  /** The background color to use for the theme. */
  private final String backgroundColor;

  /** The foreground color to use for the theme. */
  private final String foregroundColor;

  /** The disabled foreground color to use for the theme. */
  private final String foregroundColorDisabled;

  /** The button CSS context. */
  private final ButtonCssContext button;

  /** The text input CSS context. */
  private final TextInputCssContext textInput;

  /** The theme color CSS context. */
  private final ColorCssContext themeColor;

  /** Creates a new instance of the theme CSS context. */
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

  /**
   * Formats the specified color into a CSS color string.
   *
   * @param color The color to format.
   * @return The formatted color.
   */
  static String formatColor(Color color) {
    return String.format(
        "rgba(%d, %d, %d, %.02f)",
        color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255.0);
  }

  /**
   * Gets the font size.
   *
   * @return The font size.
   */
  public String getFontSize() {
    return fontSize;
  }

  /**
   * Gets the font family.
   *
   * @return The font family.
   */
  public String getFontFamily() {
    return fontFamily;
  }

  /**
   * Gets the background color.
   *
   * @return The background color.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Gets the foreground color.
   *
   * @return The foreground color.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Gets the disabled foreground color.
   *
   * @return The disabled foreground color.
   */
  public ButtonCssContext getButton() {
    return button;
  }

  /**
   * Gets the text input CSS context.
   *
   * @return The text input CSS context.
   */
  public TextInputCssContext getTextInput() {
    return textInput;
  }

  /**
   * Gets the theme color CSS context.
   *
   * @return The theme color CSS context.
   */
  public ColorCssContext getThemeColor() {
    return themeColor;
  }
}
