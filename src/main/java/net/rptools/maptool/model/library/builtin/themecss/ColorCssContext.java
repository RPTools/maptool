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
import javax.swing.UIDefaults;

public class ColorCssContext {
  private final String actionsBlue;
  private final String actionsBlueDark;

  private final String actionsGreen;

  private final String actionsGreenDark;
  private final String actionsGrey;
  private final String actionsGreyDark;
  private final String actionsGreyInline;
  private final String actionsGreyInlineDark;
  private final String actionsRed;
  private final String actionsRedDark;
  private final String actionsYellow;
  private final String actionsYellowDark;
  private final String objectsBlackText;
  private final String objectsBlue;
  private final String objectsGreen;
  private final String objectsGreenAndroid;
  private final String objectsGrey;
  private final String objectsPink;
  private final String objectsRed;
  private final String objectsRedStatus;
  private final String objectsYellow;
  private final String objectsYellowDark;

  public ColorCssContext(UIDefaults uiDef) {
    actionsBlue = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_BLUE.key));
    actionsBlueDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_BLUE_DARK.key));
    actionsGreen = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_GREEN.key));
    actionsGreenDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_GREEN_DARK.key));
    actionsGrey = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_GREY.key));
    actionsGreyDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_GREY_DARK.key));
    actionsGreyInline =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_GREYINLINE.key));
    actionsGreyInlineDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_GREYINLINE_DARK.key));
    actionsRed = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_RED.key));
    actionsRedDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_RED_DARK.key));
    actionsYellow = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_YELLOW.key));
    actionsYellowDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.ACTIONS_YELLOW_DARK.key));
    objectsBlackText =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_BLACK_TEXT.key));
    objectsBlue = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_BLUE.key));
    objectsGreen = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_GREEN.key));
    objectsGreenAndroid =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_GREEN_ANDROID.key));
    objectsGrey = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_GREY.key));
    objectsPink = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_PINK.key));
    objectsRed = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_RED.key));
    objectsRedStatus =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_RED_STATUS.key));
    objectsYellow = ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_YELLOW.key));
    objectsYellowDark =
        ThemeCssContext.formatColor(uiDef.getColor(FlatIconColors.OBJECTS_YELLOW_DARK.key));
  }

  public String getActionsBlue() {
    return actionsBlue;
  }

  public String getActionsBlueDark() {
    return actionsBlueDark;
  }

  public String getActionsGreen() {
    return actionsGreen;
  }

  public String getActionsGreenDark() {
    return actionsGreenDark;
  }

  public String getActionsGrey() {
    return actionsGrey;
  }

  public String getActionsGreyDark() {
    return actionsGreyDark;
  }

  public String getActionsGreyInline() {
    return actionsGreyInline;
  }

  public String getActionsGreyInlineDark() {
    return actionsGreyInlineDark;
  }

  public String getActionsRed() {
    return actionsRed;
  }

  public String getActionsRedDark() {
    return actionsRedDark;
  }

  public String getActionsYellow() {
    return actionsYellow;
  }

  public String getActionsYellowDark() {
    return actionsYellowDark;
  }

  public String getObjectsBlackText() {
    return objectsBlackText;
  }

  public String getObjectsBlue() {
    return objectsBlue;
  }

  public String getObjectsGreen() {
    return objectsGreen;
  }

  public String getObjectsGreenAndroid() {
    return objectsGreenAndroid;
  }

  public String getObjectsGrey() {
    return objectsGrey;
  }

  public String getObjectsPink() {
    return objectsPink;
  }

  public String getObjectsRed() {
    return objectsRed;
  }

  public String getObjectsRedStatus() {
    return objectsRedStatus;
  }

  public String getObjectsYellow() {
    return objectsYellow;
  }

  public String getObjectsYellowDark() {
    return objectsYellowDark;
  }
}
