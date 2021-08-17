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
import java.util.Map;

public class CalloutArguments {
  private final Color textColor;
  private final Color backgroundColor;
  private final Color outlineColor;
  private final CalloutPopupLocation popupLocation;
  private final List<String> text;
  private final Map<String, String> otherArguments;

  CalloutArguments(
      Color textColor,
      Color backgroundColor,
      Color outlineColor,
      CalloutPopupLocation popupLocation,
      List<String> text,
      Map<String, String> otherArguments) {
    this.textColor = textColor;
    this.backgroundColor = backgroundColor;
    this.outlineColor = outlineColor;
    this.popupLocation = popupLocation;
    this.text = List.copyOf(text);
    this.otherArguments = Map.copyOf(otherArguments);
  }

  public Color getTextColor() {
    return textColor;
  }

  public Color getTextColorOr(Color defaultColor) {
    return textColor != null ? textColor : defaultColor;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public Color getBackgroundColorOr(Color defaultColor) {
    return backgroundColor != null ? backgroundColor : defaultColor;
  }

  public CalloutPopupLocation getPopupLocation() {
    return popupLocation;
  }

  public CalloutPopupLocation getPopupLocationOr(CalloutPopupLocation defaultPopupLocation) {
    return popupLocation != null ? popupLocation : defaultPopupLocation;
  }

  public List<String> getText() {
    return text;
  }

  public Map<String, String> getOtherArguments() {
    return otherArguments;
  }

  public String getOtherArgument(String name) {
    return otherArguments.get(name);
  }

  public String getOtherArgumentOr(String name, String defaultValue) {
    String val = getOtherArgument(name);
    return val != null ? val : defaultValue;
  }

  public Color getOutlineColorOr(Color defaultOutlineColor) {
    return outlineColor != null ? outlineColor : defaultOutlineColor;
  }

  public Color getOutlineColor() {
    return outlineColor;
  }
}
