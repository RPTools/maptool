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

import java.util.Objects;
import java.util.function.Function;
import javax.swing.UIDefaults;
import net.rptools.maptool.client.ui.theme.ThemeSupport;

/**
 * Class that extracts and represents the information passed to handlebars for building the themed
 * CSS.
 */
public class ButtonCssContext {

  /** The button foreground color. */
  private final String foregroundColor;

  /** The button background color. */
  private final String backgroundColor;

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

  /** The border color when the button is disabled. */
  private final String disabledBorderColor;

  /** The foreground color of the button when the mouse pointer is hovering over it. */
  private final String hoverForegroundColor;

  /** The background color of the button when the mouse pointer is hovering over it. */
  private final String hoverBackgroundColor;

  /** The border color of the button when the mouse pointer is hovering over it. */
  private final String hoverBorderColor;

  /** The border color of the button when it has focus. */
  private final String focusedForegroundColor;

  /** The border color of the button when it has focus. */
  private final String focusedBackgroundColor;

  /** The border color of the button when it has focus. */
  private final String focusedBorderColor;

  /** The border width of the button. */
  private final String borderWidth;

  /** The shadow width of the button. */
  private final String shadowWidth;

  /** The shadow color of the button. */
  private final String shadowColor;

  /** The shadow color of the button. */
  private final String startBorderColor;

  /** The shadow color of the button. */
  private final String endBorderColor;

  /** Whether to show a button shadow color. */
  private final int showShadow;

  /**
   * Creates a new <code>ButtonCssContext</code>
   *
   * @param uiDef The {@link UIDefaults} to extract information from.
   * @param getColorOrBlank The function to use to convert the color key into a string color format.
   */
  public ButtonCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {
    foregroundColor = getColorOrBlank.apply("Button.foreground");
    backgroundColor = getColorOrBlank.apply("Button.background");
    startBackgroundColor =
        ThemeCssContext.getColorOrDefault("Button.startBackground", backgroundColor);
    endBackgroundColor = ThemeCssContext.getColorOrDefault("Button.endBackground", backgroundColor);
    pressedBackgroundColor = getColorOrBlank.apply("Button.pressedBackground");
    disabledBackgroundColor = getColorOrBlank.apply("Button.disabledBackground");
    disabledForegroundColor = getColorOrBlank.apply("Button.disabledForeground");
    disabledBorderSize = uiDef.getInt("Button.disabledBorderSize") + "px";
    disabledBorderColor = getColorOrBlank.apply("Button.disabledBorderColor");
    hoverForegroundColor =
        ThemeCssContext.getColorOrDefault("Button.hoverForeground", foregroundColor);
    hoverBackgroundColor = getColorOrBlank.apply("Button.hoverBackground");
    hoverBorderColor = getColorOrBlank.apply("Button.hoverBorderColor");
    focusedForegroundColor =
        ThemeCssContext.getColorOrDefault("Button.focusedForeground", foregroundColor);
    focusedBackgroundColor =
        ThemeCssContext.getColorOrDefault("Button.focusedBackground", backgroundColor);
    focusedBorderColor = getColorOrBlank.apply("Button.focusedBorderColor");
    borderWidth = uiDef.getInt("Button.borderWidth") + "px";
    shadowColor = getColorOrBlank.apply("Button.shadowColor");
    shadowWidth = uiDef.getInt("Button.shadowWidth") + "px";
    startBorderColor = getColorOrBlank.apply("Button.startBorderColor");
    endBorderColor = getColorOrBlank.apply("Button.endBorderColor");
    showShadow =
        uiDef.getBoolean("button.showShadow") || Objects.equals(ThemeSupport.getThemeName(), "Aah")
            ? 1
            : 0;
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

  /**
   * Returns the foreground color of the button when the mouse pointer is hovering over it.
   *
   * @return the foreground color of the button when the mouse pointer is hovering over it.
   */
  public String getHoverForegroundColor() {
    return hoverForegroundColor;
  }

  /**
   * Returns the border color of the button when the mouse pointer is hovering over it.
   *
   * @return the border color of the button when the mouse pointer is hovering over it.
   */
  public String getHoverBorderColor() {
    return hoverBorderColor;
  }

  /**
   * Returns the foreground color of the button when it has focus.
   *
   * @return the foreground color of the button when it has focus.
   */
  public String getFocusedForegroundColor() {
    return focusedForegroundColor;
  }

  /**
   * Returns the background color of the button when it has focus.
   *
   * @return the background color of the button when it has focus.
   */
  public String getFocusedBackgroundColor() {
    return focusedBackgroundColor;
  }

  /**
   * Returns the border color of the button when it has focus.
   *
   * @return the border color of the button when it has focus.
   */
  public String getFocusedBorderColor() {
    return focusedBorderColor;
  }

  /**
   * Returns the border width of the button.
   *
   * @return the border width of the button.
   */
  public String getBorderWidth() {
    return borderWidth;
  }

  /**
   * Returns the shadow width of the button.
   *
   * @return the shadow width of the button.
   */
  public String getShadowWidth() {
    return shadowWidth;
  }

  /**
   * Returns the shadow color of the button.
   *
   * @return the shadow color of the button.
   */
  public String getShadowColor() {
    return shadowColor;
  }

  /**
   * Returns the start border color of the button.
   *
   * @return the start border color of the button.
   */
  public String getStartBorderColor() {
    return startBorderColor;
  }

  /**
   * Returns the end border color of the button.
   *
   * @return the end border color of the button.
   */
  public String getEndBorderColor() {
    return endBorderColor;
  }

  /**
   * Returns whether to show a button shadow.
   *
   * @return whether to show a button shadow.
   */
  public int getShowShadow() {
    return showShadow;
  }
}
