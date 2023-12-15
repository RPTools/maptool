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
import java.util.function.Function;
import javax.swing.UIDefaults;

/**
 * A context for the text input. This is used to extract and provide the color values handlebars for
 * the themed css.
 */
public class TextInputCssContext {

  /** The foreground color of the text input. */
  private final String foregroundColor;

  /** The background color of the text input. */
  private final String backgroundColor;

  /** The border size of the text input when disabled. */
  private final String disabledBorderSize;

  /** The border color of the text input when disabled. */
  private final String disabledBorderColor;

  /**
   * Creates a new instance of the text input css context.
   *
   * @param uiDef The UI defaults to use to extract the values.
   * @param formatColor The function to use to format the color.
   */
  public TextInputCssContext(UIDefaults uiDef, Function<Color, String> formatColor) {
    foregroundColor = formatColor.apply(uiDef.getColor("TextField.foreground"));
    backgroundColor = formatColor.apply(uiDef.getColor("TextField.background"));
    disabledBorderSize = "1px";
    disabledBorderColor = formatColor.apply(uiDef.getColor("TextField.inactiveForeground"));
  }

  /**
   * Returns the foreground color of the text input.
   *
   * @return The foreground color of the text input.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Returns the background color of the text input.
   *
   * @return The background color of the text input.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Returns the border size of the text input when disabled.
   *
   * @return The border size of the text input when disabled.
   */
  public String getDisabledBorderSize() {
    return disabledBorderSize;
  }

  /**
   * Returns the border color of the text input when disabled.
   *
   * @return The border color of the text input when disabled.
   */
  public String getDisabledBorderColor() {
    return disabledBorderColor;
  }
}
