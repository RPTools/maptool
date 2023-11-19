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
import java.util.ArrayList;
import java.util.List;

/** Builder class for {@link CalloutArguments}. */
public class CalloutArgumentBuilder {
  /** The {@link Color} used to render the text if applicable. */
  private Color textColor;

  /** The {@link Color} used to render the background of the callout. */
  private Color backgroundColor;

  /** The {@link Color} used to render the outline of the callout if applicable. */
  private Color outlineColor;

  /** The anchor location of the callout. */
  private CalloutPopupLocation popupLocation;

  /** The text lines to be rendered in the callout. */
  private final List<String> text = new ArrayList<>();

  /**
   * Builds a {@link CalloutArguments} object based on the parameters set using this builder.
   *
   * @return the {@link CalloutArguments}.
   */
  public CalloutArguments build() {
    return new CalloutArguments(textColor, backgroundColor, outlineColor, popupLocation, text);
  }

  /**
   * Returns the {@link Color} used to render the text.
   *
   * @return the {@link Color} used to render the text.
   */
  public Color getTextColor() {
    return textColor;
  }

  /**
   * Sets the {@link Color} used to render text.
   *
   * @param textColor the {@link Color} used to render the text.
   * @return {@code this} so that methods can be chained.
   */
  public CalloutArgumentBuilder setTextColor(Color textColor) {
    this.textColor = textColor;
    return this;
  }

  /**
   * Sets the {@link Color} used to render text from a {@link String}.
   *
   * @param textColor the {@link Color} used to render the text from a {@link String}.
   * @return {@code this} so that methods can be chained.
   * @see Color#decode(String)
   */
  public CalloutArgumentBuilder setTextColor(String textColor) {
    this.textColor = Color.decode(textColor);
    return this;
  }

  /**
   * Returns the background {@link Color} for the callout.
   *
   * @return the background {@link Color} for the callout.
   */
  public Color getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Sets the {@link Color} used to render the background.
   *
   * @param backgroundColor the {@link Color} used to render the background.
   * @return {@code this} so that methods can be chained.
   */
  public CalloutArgumentBuilder setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
    return this;
  }

  /**
   * Sets the {@link Color} used to render the background from a {@link String}.
   *
   * @param backgroundColor the {@link Color} used to render the background from a {@link String}.
   * @return {@code this} so that methods can be chained.
   * @see Color#decode(String)
   */
  public CalloutArgumentBuilder setBackgroundColor(String backgroundColor) {
    this.backgroundColor = Color.decode(backgroundColor);
    return this;
  }

  /**
   * Returns the {@link Color} used to render the outline of the callout.
   *
   * @return the {@link Color} used to render the outline of the callout.
   */
  public Color getOutlineColor() {
    return outlineColor;
  }

  /**
   * Sets the {@link Color} used to render the outline.
   *
   * @param outlineColor the {@link Color} used to render the outline.
   * @return {@code this} so that methods can be chained.
   */
  public CalloutArgumentBuilder setOutlineColor(Color outlineColor) {
    this.outlineColor = outlineColor;
    return this;
  }

  /**
   * Sets the {@link Color} used to render the outline from a {@link String}.
   *
   * @param outlineColor the {@link Color} used to render the outline from a {@link String}.
   * @return {@code this} so that methods can be chained.
   * @see Color#decode(String)
   */
  public CalloutArgumentBuilder setOutlineColor(String outlineColor) {
    this.outlineColor = Color.decode(outlineColor);
    return this;
  }

  /**
   * Returns the anchor location for the callout.
   *
   * @return the anchor location for the callout.
   */
  public CalloutPopupLocation getPopupLocation() {
    return popupLocation;
  }

  /**
   * Sets the anchor location for the callout.
   *
   * @param popupLocation the anchor location for the callout.
   * @return {@code this} so that the methods can be chained.
   */
  public CalloutArgumentBuilder setPopupLocation(CalloutPopupLocation popupLocation) {
    this.popupLocation = popupLocation;
    return this;
  }

  /**
   * Returns the text to be rendered in the callout.
   *
   * @return the text to be rednered in the callout.
   */
  public List<String> getText() {
    return text;
  }

  /**
   * Adds a text line to the text to be rendered in the callout.
   *
   * @param text the line of text to be added.
   * @return {@code this} so that the methods can be chained.
   */
  public CalloutArgumentBuilder addText(String text) {
    this.text.add(text);
    return this;
  }

  /**
   * Removes all the lines of text to be rendered in the callout.
   *
   * @return {@code this} so that the methods can be chained.
   */
  public CalloutArgumentBuilder clearText() {
    this.text.clear();
    return this;
  }
}
