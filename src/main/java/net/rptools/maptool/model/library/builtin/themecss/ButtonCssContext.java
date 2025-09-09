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
 * Class that extracts and represents the information passed to handlebars for building the themed
 * CSS.
 */
public class ButtonCssContext {

  /** The button foreground color. */
  private final String foregroundColor;

  /** Starting background color for the button background. */
  private final String startBackgroundColor;

  /** Ending background color for the button background. */
  private final String endBackgroundColor;

  /** The background color of the button when it is pressed. */
  private final String pressedBackgroundColor;

  /** The background color of the button when it is disabled. */
  private final String disabledBackgroundColor;

  /** The foreground color of the button when it is disabled. */
  private final String disabledForegroundColor;

  /** The size of the button border when it is disabled. */
  private final String disabledBorderSize;

  /** The color of the border when the button is disabled. */
  private final String disabledBorderColor;

  /** The background color of the button when the mouse pointer is hovering over it. */
  private final String hoverBackgroundColor;

  /**
   * Creates a new <code>ButtonCssContext</code>
   *
   * @param uiDef the {@link UIDefaults} to extract information from.
   * @param formatColor the function to use to format the color.
   */
  public ButtonCssContext(UIDefaults uiDef, Function<Color, String> formatColor) {
    foregroundColor = formatColor.apply(uiDef.getColor("Button.foreground"));
    startBackgroundColor = formatColor.apply(uiDef.getColor("Button.startBackground"));
    endBackgroundColor = formatColor.apply(uiDef.getColor("Button.endBackground"));
    pressedBackgroundColor = formatColor.apply(uiDef.getColor("Button.pressedBackground"));
    disabledBackgroundColor = formatColor.apply(uiDef.getColor("Button.disabledBackground"));
    disabledForegroundColor = formatColor.apply(uiDef.getColor("Button.disabledForeground"));
    disabledBorderSize = uiDef.getInt("Button.disabledBorderSize") + "px";
    disabledBorderColor = formatColor.apply(uiDef.getColor("Button.disabledBorderColor"));
    hoverBackgroundColor = formatColor.apply(uiDef.getColor("Button.hoverBackground"));
  }

  /**
   * Returns the foreground color of the button.
   *
   * @return the foreground color of the button.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Returns the starting background color of the button.
   *
   * @return the starting background color of the button.
   */
  public String getStartBackgroundColor() {
    return startBackgroundColor;
  }

  /**
   * Returns the ending background color of the button.
   *
   * @return the ending background color of the button.
   */
  public String getEndBackgroundColor() {
    return endBackgroundColor;
  }

  /**
   * Returns the background color of the button when it is pressed.
   *
   * @return the background color of the button when it is pressed.
   */
  public String getPressedBackgroundColor() {
    return pressedBackgroundColor;
  }

  /**
   * Returns the background color of the button when it is disabled.
   *
   * @return the background color of the button when it is disabled.
   */
  public String getDisabledBackgroundColor() {
    return disabledBackgroundColor;
  }

  /**
   * Returns the foreground color of the button when it is disabled.
   *
   * @return the foreground color of the button when it is disabled.
   */
  public String getDisabledForegroundColor() {
    return disabledForegroundColor;
  }

  /**
   * Returns the size of the border when the button is disabled.
   *
   * @return the size of the border when the button is disabled.
   */
  public String getDisabledBorderSize() {
    return disabledBorderSize;
  }

  /**
   * Returns the color of the border when the button is disabled.
   *
   * @return the color of the border when the button is disabled.
   */
  public String getDisabledBorderColor() {
    return disabledBorderColor;
  }

  /**
   * Returns the background color of the button when the mouse pointer is hovering over it.
   *
   * @return the background color of the button when the mouse pointer is hovering over it.
   */
  public String getHoverBackgroundColor() {
    return hoverBackgroundColor;
  }
}
