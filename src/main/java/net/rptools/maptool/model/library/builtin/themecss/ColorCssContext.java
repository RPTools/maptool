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

import com.formdev.flatlaf.FlatIconColors;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.UIDefaults;

/**
 * A context for the color CSS. This is used to extract and provide the color values handlebars for
 * the themed css.
 */
public class ColorCssContext {
  /** The "blue" action color from flat laf. */
  private final String actionsBlue;

  /** The "dark blue" action color from flat laf. */
  private final String actionsBlueDark;

  /** The "green" action color from flat laf. */
  private final String actionsGreen;

  /** The "dark green" action color from flat laf. */
  private final String actionsGreenDark;

  /** The "grey" action color from flat laf. */
  private final String actionsGrey;

  /** The "dark grey" action color from flat laf. */
  private final String actionsGreyDark;

  /** The "grey inline" action color from flat laf. */
  private final String actionsGreyInline;

  /** The "dark grey inline" action color from flat laf. */
  private final String actionsGreyInlineDark;

  /** The "red" action color from flat laf. */
  private final String actionsRed;

  /** The "dark red" action color from flat laf. */
  private final String actionsRedDark;

  /** The "yellow" action color from flat laf. */
  private final String actionsYellow;

  /** The "dark yellow" action color from flat laf. */
  private final String actionsYellowDark;

  /** The "black text" object color from flat laf. */
  private final String objectsBlackText;

  /** The "blue" object color from flat laf. */
  private final String objectsBlue;

  /** The "green" object color from flat laf. */
  private final String objectsGreen;

  /** The "green android" object color from flat laf. */
  private final String objectsGreenAndroid;

  /** The "grey" object color from flat laf. */
  private final String objectsGrey;

  /** The "pink" object color from flat laf. */
  private final String objectsPink;

  /** The "red" object color from flat laf. */
  private final String objectsRed;

  /** The "red status" object color from flat laf. */
  private final String objectsRedStatus;

  /** The "yellow" object color from flat laf. */
  private final String objectsYellow;

  /** The "dark yellow" object color from flat laf. */
  private final String objectsYellowDark;

  /**
   * Creates a new instance of {@link ColorCssContext}.
   *
   * @param uiDef The UI defaults to extract the colors from.
   */
  public ColorCssContext(UIDefaults uiDef, Function<Color, String> formatColor) {
    actionsBlue = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_BLUE.key));
    actionsBlueDark = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_BLUE_DARK.key));
    actionsGreen = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_GREEN.key));
    actionsGreenDark = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_GREEN_DARK.key));
    actionsGrey = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_GREY.key));
    actionsGreyDark = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_GREY_DARK.key));
    actionsGreyInline = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_GREYINLINE.key));
    actionsGreyInlineDark =
        formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_GREYINLINE_DARK.key));
    actionsRed = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_RED.key));
    actionsRedDark = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_RED_DARK.key));
    actionsYellow = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_YELLOW.key));
    actionsYellowDark = formatColor.apply(uiDef.getColor(FlatIconColors.ACTIONS_YELLOW_DARK.key));
    objectsBlackText = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_BLACK_TEXT.key));
    objectsBlue = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_BLUE.key));
    objectsGreen = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_GREEN.key));
    objectsGreenAndroid =
        formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_GREEN_ANDROID.key));
    objectsGrey = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_GREY.key));
    objectsPink = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_PINK.key));
    objectsRed = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_RED.key));
    objectsRedStatus = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_RED_STATUS.key));
    objectsYellow = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_YELLOW.key));
    objectsYellowDark = formatColor.apply(uiDef.getColor(FlatIconColors.OBJECTS_YELLOW_DARK.key));
  }

  /**
   * Returns the "blue" action color from flat laf.
   *
   * @return The "blue" action color from flat laf.
   */
  public String getActionsBlue() {
    return actionsBlue;
  }

  /**
   * Returns the "dark blue" action color from flat laf.
   *
   * @return The "dark blue" action color from flat laf.
   */
  public String getActionsBlueDark() {
    return actionsBlueDark;
  }

  /**
   * Returns the "green" action color from flat laf.
   *
   * @return The "green" action color from flat laf.
   */
  public String getActionsGreen() {
    return actionsGreen;
  }

  /**
   * Returns the "dark green" action color from flat laf.
   *
   * @return The "dark green" action color from flat laf.
   */
  public String getActionsGreenDark() {
    return actionsGreenDark;
  }

  /**
   * Returns the "grey" action color from flat laf.
   *
   * @return The "grey" action color from flat laf.
   */
  public String getActionsGrey() {
    return actionsGrey;
  }

  /**
   * Returns the "dark grey" action color from flat laf.
   *
   * @return The "dark grey" action color from flat laf.
   */
  public String getActionsGreyDark() {
    return actionsGreyDark;
  }

  /**
   * Returns the "grey inline" action color from flat laf.
   *
   * @return The "grey inline" action color from flat laf.
   */
  public String getActionsGreyInline() {
    return actionsGreyInline;
  }

  /**
   * Returns the "dark grey inline" action color from flat laf.
   *
   * @return The "dark grey inline" action color from flat laf.
   */
  public String getActionsGreyInlineDark() {
    return actionsGreyInlineDark;
  }

  /**
   * Returns the "red" action color from flat laf.
   *
   * @return The "red" action color from flat laf.
   */
  public String getActionsRed() {
    return actionsRed;
  }

  /**
   * Returns the "dark red" action color from flat laf.
   *
   * @return The "dark red" action color from flat laf.
   */
  public String getActionsRedDark() {
    return actionsRedDark;
  }

  /**
   * Returns the "yellow" action color from flat laf.
   *
   * @return The "yellow" action color from flat laf.
   */
  public String getActionsYellow() {
    return actionsYellow;
  }

  /**
   * Returns the "dark yellow" action color from flat laf.
   *
   * @return The "dark yellow" action color from flat laf.
   */
  public String getActionsYellowDark() {
    return actionsYellowDark;
  }

  /**
   * Returns the "black text" object color from flat laf.
   *
   * @return The "black text" object color from flat laf.
   */
  public String getObjectsBlackText() {
    return objectsBlackText;
  }

  /**
   * Returns the "blue" object color from flat laf.
   *
   * @return The "blue" object color from flat laf.
   */
  public String getObjectsBlue() {
    return objectsBlue;
  }

  /**
   * Returns the "green" object color from flat laf.
   *
   * @return The "green" object color from flat laf.
   */
  public String getObjectsGreen() {
    return objectsGreen;
  }

  /**
   * Returns the "green android" object color from flat laf.
   *
   * @return The "green android" object color from flat laf.
   */
  public String getObjectsGreenAndroid() {
    return objectsGreenAndroid;
  }

  /**
   * Returns the "grey" object color from flat laf.
   *
   * @return The "grey" object color from flat laf.
   */
  public String getObjectsGrey() {
    return objectsGrey;
  }

  /**
   * Returns the "pink" object color from flat laf.
   *
   * @return The "pink" object color from flat laf.
   */
  public String getObjectsPink() {
    return objectsPink;
  }

  /**
   * Returns the "red" object color from flat laf.
   *
   * @return The "red" object color from flat laf.
   */
  public String getObjectsRed() {
    return objectsRed;
  }

  /**
   * Returns the "red status" object color from flat laf.
   *
   * @return The "red status" object color from flat laf.
   */
  public String getObjectsRedStatus() {
    return objectsRedStatus;
  }

  /**
   * Returns the "yellow" object color from flat laf.
   *
   * @return The "yellow" object color from flat laf.
   */
  public String getObjectsYellow() {
    return objectsYellow;
  }

  /**
   * Returns the "dark yellow" object color from flat laf.
   *
   * @return The "dark yellow" object color from flat laf.
   */
  public String getObjectsYellowDark() {
    return objectsYellowDark;
  }
}
