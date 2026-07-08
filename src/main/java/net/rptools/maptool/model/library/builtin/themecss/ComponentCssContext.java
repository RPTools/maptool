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

public class ComponentCssContext {

  /** The foreground color to use for links. */
  private final String linkColor;

  /** The focused border color to use for the theme. */
  private final String focusedBorderColor;

  /** The border for errors. */
  private final String errorBorderColor;

  /** The focused border color for errors. */
  private final String errorFocusedBorderColor;

  /** The border color for success. */
  private final String successBorderColor;

  /** The focused border for success. */
  private final String successFocusedBorderColor;

  /** The border color for warnings. */
  private final String warningBorderColor;

  /** The focused border color for warnings. */
  private final String warningFocusedBorderColor;

  /**
   * Creates a new instance of the combo box css context.
   *
   * @param uiDef The UI defaults to use to extract the values.
   * @param getColorOrBlank The function to use to convert the color key into a string color format.
   */
  public ComponentCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {
    linkColor = getColorOrBlank.apply("Component.linkColor");
    focusedBorderColor = getColorOrBlank.apply("Component.focusedBorderColor");
    errorBorderColor = getColorOrBlank.apply("Component.error.borderColor");
    errorFocusedBorderColor = getColorOrBlank.apply("Component.error.focusedBorderColor");
    successBorderColor = getColorOrBlank.apply("Component.success.borderColor");
    successFocusedBorderColor = getColorOrBlank.apply("Component.success.focusedBorderColor");
    warningBorderColor = getColorOrBlank.apply("Component.warning.borderColor");
    warningFocusedBorderColor = getColorOrBlank.apply("Component.warning.focusedBorderColor");
  }

  /**
   * Gets the foreground color to use for links.
   *
   * @return the foreground color to use for links.
   */
  public String getLinkColor() {
    return linkColor;
  }

  /**
   * Gets the focused border color.
   *
   * @return the focused border color.
   */
  public String getFocusedBorderColor() {
    return focusedBorderColor;
  }

  /**
   * Gets the error border color.
   *
   * @return the error border color.
   */
  public String getErrorBorderColor() {
    return errorBorderColor;
  }

  /**
   * Gets the error focused border color.
   *
   * @return the error focused border color.
   */
  public String getErrorFocusedBorderColor() {
    return errorFocusedBorderColor;
  }

  /**
   * Gets the success border color.
   *
   * @return the success border color.
   */
  public String getSuccessBorderColor() {
    return successBorderColor;
  }

  /**
   * Gets the success focused border color.
   *
   * @return the success focused border color.
   */
  public String getSuccessFocusedBorderColor() {
    return successFocusedBorderColor;
  }

  /**
   * Gets the warning border color.
   *
   * @return the warning border color.
   */
  public String getWarningBorderColor() {
    return warningBorderColor;
  }

  /**
   * Gets the warning focused border color.
   *
   * @return the warning focused border color.
   */
  public String getWarningFocusedBorderColor() {
    return warningFocusedBorderColor;
  }
}
