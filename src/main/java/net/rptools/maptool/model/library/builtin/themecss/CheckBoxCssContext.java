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
import java.util.Objects;
import java.util.function.Function;
import javax.swing.*;

/**
 * A context for the checkbox. This is used to extract and provide the color values handlebars for
 * the themed css.
 */
public class CheckBoxCssContext {

  /** The style of the checkbox. */
  private final boolean styleFilled;

  /** The foreground color of the checkbox. */
  private final String foregroundColor;

  /** The background color of the checkbox. */
  private final String backgroundColor;

  /** The border color of the checkbox. */
  private final String borderColor;

  /** The foreground color of the checkbox when disabled. */
  private final String disabledForegroundColor;

  /** The background color of the checkbox when disabled. */
  private final String disabledBackgroundColor;

  /** The border color of the checkbox when disabled. */
  private final String disabledBorderColor;

  /** The border color of the checkbox when focused. */
  private final String focusedBorderColor;

  /** The background color of the checkbox when hovered. */
  private final String hoverBackgroundColor;

  /** The border color of the checkbox when hovered. */
  private final String hoverBorderColor;

  /** The background color of the checkbox when hovered and selected. */
  private final String hoverSelectedBackgroundColor;

  /** The background color of the checkbox when selected. */
  private final String selectedBackgroundColor;

  /** The border color of the checkbox when selected. */
  private final String selectedBorderColor;

  /**
   * Creates a new instance of the checkbox css context.
   *
   * @param uiDef The UI defaults to use to extract the values.
   * @param getColorOrBlank The function to use to convert the color key into a string color format.
   */
  public CheckBoxCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {

    disabledForegroundColor = getColorOrBlank.apply("CheckBox.icon.disabledCheckmarkColor");
    disabledBorderColor = getColorOrBlank.apply("CheckBox.icon.disabledBorderColor");
    hoverBackgroundColor = getColorOrBlank.apply("CheckBox.icon.hoverBackground");

    styleFilled = Objects.equals(uiDef.getString("CheckBox.icon.style"), "filled");
    if (styleFilled) {
      backgroundColor = getColorOrBlank.apply("CheckBox.icon[filled].background");
      borderColor = getColorOrBlank.apply("CheckBox.icon[filled].borderColor");
      foregroundColor = getColorOrBlank.apply("CheckBox.icon[filled].checkmarkColor");
      disabledBackgroundColor = getColorOrBlank.apply("CheckBox.icon[filled].disabledBackground");
      hoverBorderColor = getColorOrBlank.apply("CheckBox.icon[filled].hoverBorderColor");
      hoverSelectedBackgroundColor =
          getColorOrBlank.apply("CheckBox.icon[filled].hoverSelectedBackground");
      focusedBorderColor = getColorOrBlank.apply("CheckBox.icon[filled].focusedBorderColor");
      selectedBackgroundColor = getColorOrBlank.apply("CheckBox.icon[filled].selectedBackground");
      selectedBorderColor = getColorOrBlank.apply("CheckBox.icon[filled].selectedBorderColor");
    } else {
      backgroundColor = getColorOrBlank.apply("CheckBox.icon.background");
      borderColor = getColorOrBlank.apply("CheckBox.icon.borderColor");
      foregroundColor = getColorOrBlank.apply("CheckBox.icon.checkmarkColor");
      disabledBackgroundColor = getColorOrBlank.apply("CheckBox.icon.disabledBackground");
      hoverBorderColor = getColorOrBlank.apply("CheckBox.icon.hoverBorderColor");
      hoverSelectedBackgroundColor = getColorOrBlank.apply("CheckBox.icon.hoverBackground");
      focusedBorderColor = getColorOrBlank.apply("CheckBox.icon.focusedBorderColor");
      selectedBackgroundColor = getColorOrBlank.apply("CheckBox.icon.selectedBackground");
      selectedBorderColor = getColorOrBlank.apply("CheckBox.icon.selectedBorderColor");
    }
  }

  /**
   * Returns the style of the checkbox.
   *
   * @return The style of the checkbox.
   */
  public boolean getStyleFilled() {
    return styleFilled;
  }

  /**
   * Returns the foreground color of the checkbox.
   *
   * @return The foreground color of the checkbox.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Returns the background color of the checkbox.
   *
   * @return The background color of the checkbox.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Returns the border color of the checkbox.
   *
   * @return The border color of the checkbox.
   */
  public String getBorderColor() {
    return borderColor;
  }

  /**
   * Returns the foreground color of the checkbox when disabled.
   *
   * @return The foreground color of the checkbox when disabled.
   */
  public String getDisabledForegroundColor() {
    return disabledForegroundColor;
  }

  /**
   * Returns the background color of the checkbox when disabled.
   *
   * @return The background color of the checkbox when disabled.
   */
  public String getDisabledBackgroundColor() {
    return disabledBackgroundColor;
  }

  /**
   * Returns the border color of the checkbox when disabled.
   *
   * @return The border color of the checkbox when disabled.
   */
  public String getDisabledBorderColor() {
    return disabledBorderColor;
  }

  /**
   * Returns the border color of the checkbox when focused.
   *
   * @return The border color of the checkbox when focused.
   */
  public String getFocusedBorderColor() {
    return focusedBorderColor;
  }

  /**
   * Returns the background color of the checkbox when hovered.
   *
   * @return The background color of the checkbox when hovered.
   */
  public String getHoverBackgroundColor() {
    return hoverBackgroundColor;
  }

  /**
   * Returns the border color of the checkbox when hovered.
   *
   * @return The border color of the checkbox when hovered.
   */
  public String getHoverBorderColor() {
    return hoverBorderColor;
  }

  /**
   * Returns the background color of the checkbox when hovered and selected.
   *
   * @return The background color of the checkbox when hovered and selected.
   */
  public String getHoverSelectedBackgroundColor() {
    return hoverSelectedBackgroundColor;
  }

  /**
   * Returns the background color of the checkbox when selected.
   *
   * @return The background color of the checkbox when selected.
   */
  public String getSelectedBackgroundColor() {
    return selectedBackgroundColor;
  }

  /**
   * Returns the border color of the checkbox when selected.
   *
   * @return The border color of the checkbox when selected.
   */
  public String getSelectedBorderColor() {
    return selectedBorderColor;
  }
}
