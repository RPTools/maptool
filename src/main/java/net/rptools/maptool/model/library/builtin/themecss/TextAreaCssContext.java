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

import java.awt.*;
import java.util.function.Function;
import javax.swing.*;

/**
 * A context for the textarea. This is used to extract and provide the color values handlebars for
 * the themed css.
 */
public class TextAreaCssContext {

  /** The foreground color of the textarea. */
  private final String foregroundColor;

  /** The background color of the textarea. */
  private final String backgroundColor;

  /** The foreground color of the textarea when disabled. */
  private final String disabledForegroundColor;

  /** The background color of the textarea when disabled. */
  private final String disabledBackgroundColor;

  /** The border color of the textarea when disabled. */
  private final String disabledBorderColor;

  /** The border size of the textarea when disabled. */
  private final String disabledBorderSize;

  /**
   * Creates a new instance of the textarea css context.
   *
   * @param uiDef The UI defaults to use to extract the values.
   * @param getColorOrBlank The function to use to convert the color key into a string color format.
   */
  public TextAreaCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {
    foregroundColor = getColorOrBlank.apply("TextArea.foreground");
    backgroundColor = getColorOrBlank.apply("TextArea.background");
    disabledForegroundColor = getColorOrBlank.apply("TextArea.inactiveForeground");
    disabledBackgroundColor = getColorOrBlank.apply("TextArea.inactiveBackground");
    disabledBorderColor = getColorOrBlank.apply("TextArea.inactiveForeground");
    disabledBorderSize = "1px";
  }

  /**
   * Returns the foreground color of the textarea.
   *
   * @return The foreground color of the textarea.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Returns the background color of the textarea.
   *
   * @return The background color of the textarea.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Returns the foreground color of the textarea when disabled.
   *
   * @return The foreground color of the textarea when disabled.
   */
  public String getDisabledForegroundColor() {
    return disabledForegroundColor;
  }

  /**
   * Returns the background color of the textarea when disabled.
   *
   * @return The background color of the textarea when disabled.
   */
  public String getDisabledBackgroundColor() {
    return disabledBackgroundColor;
  }

  /**
   * Returns the border color of the textarea when disabled.
   *
   * @return The border color of the textarea when disabled.
   */
  public String getDisabledBorderColor() {
    return disabledBorderColor;
  }

  /**
   * Returns the border size of the textarea when disabled.
   *
   * @return The border size of the textarea when disabled.
   */
  public String getDisabledBorderSize() {
    return disabledBorderSize;
  }
}
