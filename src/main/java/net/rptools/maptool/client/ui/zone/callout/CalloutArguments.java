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
package net.rptools.maptool.client.ui.zone.callout;

import java.awt.Color;
import java.util.List;

/** Class representing tha arguments used to build the callout. */
public class CalloutArguments {
  /** The color used to render the text. */
  private final Color textColor;

  /* The color used to render the background. */
  private final Color backgroundColor;

  /** The color used to render the outline. */
  private final Color outlineColor;

  /** The anchor location of the callout. */
  private final CalloutPopupLocation popupLocation;

  /** The text to be displayed in the callout. */
  private final List<String> text;

  /**
   * Creates a new {@code CalloutArguments} object.
   *
   * @param textColor the color used to render the text of the callout.
   * @param backgroundColor the color used to render the background of the callout.
   * @param outlineColor the color used to render the outline of the callout.
   * @param popupLocation the anchor location for the callout.
   * @param text the text to be displayed in the callout.
   */
  CalloutArguments(
      Color textColor,
      Color backgroundColor,
      Color outlineColor,
      CalloutPopupLocation popupLocation,
      List<String> text) {
    this.textColor = textColor;
    this.backgroundColor = backgroundColor;
    this.outlineColor = outlineColor;
    this.popupLocation = popupLocation;
    this.text = List.copyOf(text);
  }

  /**
   * Returns the {@link Color} used to render the text of the callout.
   *
   * @return the {@link Color} used to render the text of the callout.
   */
  public Color getTextColor() {
    return textColor;
  }

  /**
   * Returns the {@link Color} used to render the text of the callout or {@code defaultColor} if it
   * is not set.
   *
   * @param defaultColor the {@link Color} to return if the text color has not been set.
   * @return the {@link Color} used to render the text of the callout.
   */
  public Color getTextColorOr(Color defaultColor) {
    return textColor != null ? textColor : defaultColor;
  }

  /**
   * Returns the {@link Color} used to render the background of the callout.
   *
   * @return the {@link Color} used to render the background of the callout.
   */
  public Color getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Returns the {@link Color} used to render the background of the callout or {@code defaultColor}
   * if it is not set.
   *
   * @param defaultColor the {@link Color} to return if the background color has not been set.
   * @return the {@link Color} used to render the background of the callout.
   */
  public Color getBackgroundColorOr(Color defaultColor) {
    return backgroundColor != null ? backgroundColor : defaultColor;
  }

  /**
   * Returns the anchor location of the callout.
   *
   * @return the anchor location of the callout.
   */
  public CalloutPopupLocation getPopupLocation() {
    return popupLocation;
  }

  /**
   * Returns the anchor location of the callout or {@code defaultPopupLocation} if it is not set.
   *
   * @param defaultPopupLocation the anchor location to return if one has not been set.
   * @return the anchor location of the callout.
   */
  public CalloutPopupLocation getPopupLocationOr(CalloutPopupLocation defaultPopupLocation) {
    return popupLocation != null ? popupLocation : defaultPopupLocation;
  }

  /**
   * Returns the text to render in the callout.
   *
   * @return the text to render in the callout.
   */
  public List<String> getText() {
    return text;
  }

  /**
   * Returns the {@link Color} used to render the outline of the callout or {@code defaultColor} if
   * it is not set.
   *
   * @param defaultColor the {@link Color} to return if the outline color has not been set.
   * @return the {@link Color} used to render the outline of the callout.
   */
  public Color getOutlineColorOr(Color defaultColor) {
    return outlineColor != null ? outlineColor : defaultColor;
  }

  /**
   * Returns the {@link Color} used to render the outline of the callout.
   *
   * @return the {@link Color} used to render the outline of the callout.
   */
  public Color getOutlineColor() {
    return outlineColor;
  }
}
