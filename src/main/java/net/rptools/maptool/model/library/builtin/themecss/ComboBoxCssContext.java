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

public class ComboBoxCssContext {

  /** The combo box background color. */
  private final String backgroundColor;

  /** The combo box foreground color. */
  private final String foregroundColor;

  /** The combo box button arrow color. */
  private final String buttonArrowColor;

  /** The combo box button background color. */
  private final String buttonBackgroundColor;

  /** The combo box button arrow color when disabled. */
  private final String buttonDisabledArrowColor;

  /** The combo box button arrow color when hovered. */
  private final String buttonHoverArrowColor;

  /** The combo box button arrow color when pressed. */
  private final String buttonPressedArrowColor;

  /** The combo box background color when disabled. */
  private final String disabledBackgroundColor;

  /** The combo box foreground color when disabled. */
  private final String disabledForegroundColor;

  /**
   * Creates a new instance of the combo box css context.
   *
   * @param uiDef The UI defaults to use to extract the values.
   * @param getColorOrBlank The function to use to convert the color key into a string color format.
   */
  public ComboBoxCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {
    backgroundColor = getColorOrBlank.apply("ComboBox.background");
    foregroundColor = getColorOrBlank.apply("ComboBox.foreground");
    buttonArrowColor = getColorOrBlank.apply("ComboBox.buttonArrowColor");
    buttonBackgroundColor = getColorOrBlank.apply("ComboBox.buttonBackground");
    buttonDisabledArrowColor = getColorOrBlank.apply("ComboBox.buttonDisabledArrowColor");
    buttonHoverArrowColor = getColorOrBlank.apply("ComboBox.buttonHoverArrowColor");
    buttonPressedArrowColor = getColorOrBlank.apply("ComboBox.buttonPressedArrowColor");
    disabledBackgroundColor = getColorOrBlank.apply("ComboBox.disabledBackground");
    disabledForegroundColor = getColorOrBlank.apply("ComboBox.disabledForeground");
  }

  /**
   * Gets the combo box background color.
   *
   * @return The combo box background color.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Gets the combo box foreground color.
   *
   * @return The combo box foreground color.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Gets the combo box arrow button color.
   *
   * @return The combo box arrow button color.
   */
  public String getButtonArrowColor() {
    return buttonArrowColor;
  }

  /**
   * Gets the combo box button background color.
   *
   * @return The combo box button background color.
   */
  public String getButtonBackgroundColor() {
    return buttonBackgroundColor;
  }

  /**
   * Gets the combo box button arrow color when disabled.
   *
   * @return The combo box button arrow color when disabled.
   */
  public String getButtonDisabledArrowColor() {
    return buttonDisabledArrowColor;
  }

  /**
   * Gets the combo box button arrow color when hovered.
   *
   * @return The combo box button arrow color when hovered.
   */
  public String getButtonHoverArrowColor() {
    return buttonHoverArrowColor;
  }

  /**
   * Gets the combo box button arrow color when pressed.
   *
   * @return The combo box button arrow color when pressed.
   */
  public String getButtonPressedArrowColor() {
    return buttonPressedArrowColor;
  }

  /**
   * Gets the combo box background color when disabled.
   *
   * @return The combo box background color when disabled.
   */
  public String getDisabledBackgroundColor() {
    return disabledBackgroundColor;
  }

  /**
   * Gets the combo box foreground color when disabled.
   *
   * @return The combo box foreground color when disabled.
   */
  public String getDisabledForegroundColor() {
    return disabledForegroundColor;
  }
}
