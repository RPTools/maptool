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
import java.util.Arrays;
import java.util.function.Function;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.theme.ThemeSupport;

/**
 * A context for the Preferences css context. This is used to extract and provide the color values
 * handlebars for the themed css.
 */
public class PreferencesCssContext {

  /** The name of the selected theme. */
  private final String themeName;

  /** The color scheme of the selected theme (i.e. light/dark). */
  private final String themeColorScheme;

  /** The foreground color of NPC map labels. */
  private final String npcForegroundColor;

  /** The background color of NPC map labels. */
  private final String npcBackgroundColor;

  /** The border color of NPC map labels. */
  private final String npcBorderColor;

  /** The foreground color of PC map labels. */
  private final String pcForegroundColor;

  /** The background color of PC map labels. */
  private final String pcBackgroundColor;

  /** The border color of PC map labels. */
  private final String pcBorderColor;

  /** The foreground color of non-visible token map labels. */
  private final String nonVisibleForegroundColor;

  /** The background color of non-visible token map labels. */
  private final String nonVisibleBackgroundColor;

  /** The border color of non-visible token map labels. */
  private final String nonVisibleBorderColor;

  /** The foreground color of drawing map labels. */
  private final String drawingForegroundColor;

  /** The background color of drawing map labels. */
  private final String drawingBackgroundColor;

  /** The border color of drawing map labels. */
  private final String drawingBorderColor;

  /** The foreground color of template map labels. */
  private final String templateForegroundColor;

  /** The background color of template map labels. */
  private final String templateBackgroundColor;

  /** The border color of template map labels. */
  private final String templateBorderColor;

  /** The font size of the map labels. */
  private final String mapLabelFontSize;

  /** The border width of the map labels. */
  private final String mapLabelBorderWidth;

  /** The border arc of the map labels. */
  private final int mapLabelBorderArc;

  /** Whether to show map label borders. */
  private final int mapLabelShowBorder;

  /** The background color of the facing arrow. */
  private final String facingArrowBackgroundColor;

  /** The border color of the facing arrow. */
  private final String facingArrowBorderColor;

  /** The foreground color of chat notifications. */
  private final String chatNotificationForegroundColor;

  /** The foreground color of trusted prefixes. */
  private final String trustedPrefixForegroundColor;

  /** The background color of trusted prefixes. */
  private final String trustedPrefixBackgroundColor;

  /**
   * Creates a new instance of the preferences css context.
   *
   * @param formatColor The function to use to format the color.
   */
  public PreferencesCssContext(Function<Color, String> formatColor) {

    themeName = ThemeSupport.getThemeName();
    themeColorScheme =
        Boolean.TRUE.equals(
                Arrays.stream(ThemeSupport.THEMES)
                    .filter(t -> t.name().equals(themeName))
                    .findFirst()
                    .map(ThemeSupport.ThemeDetails::dark)
                    .orElse(null))
            ? "dark"
            : "light";

    // map label color preferences
    npcBackgroundColor = formatColor.apply(AppPreferences.npcMapLabelBackground.get());
    npcForegroundColor = formatColor.apply(AppPreferences.npcMapLabelForeground.get());
    npcBorderColor = formatColor.apply(AppPreferences.npcMapLabelBorder.get());
    pcBackgroundColor = formatColor.apply(AppPreferences.pcMapLabelBackground.get());
    pcForegroundColor = formatColor.apply(AppPreferences.pcMapLabelForeground.get());
    pcBorderColor = formatColor.apply(AppPreferences.pcMapLabelBorder.get());
    nonVisibleBackgroundColor =
        formatColor.apply(AppPreferences.nonVisibleTokenMapLabelBackground.get());
    nonVisibleForegroundColor =
        formatColor.apply(AppPreferences.nonVisibleTokenMapLabelForeground.get());
    nonVisibleBorderColor = formatColor.apply(AppPreferences.nonVisibleTokenMapLabelBorder.get());
    drawingBackgroundColor = formatColor.apply(AppPreferences.drawingMapLabelBackgroundColor.get());
    drawingForegroundColor = formatColor.apply(AppPreferences.drawingMapLabelForegroundColor.get());
    drawingBorderColor = formatColor.apply(AppPreferences.drawingMapLabelBorderColor.get());
    templateBackgroundColor =
        formatColor.apply(AppPreferences.templateMapLabelBackgroundColor.get());
    templateForegroundColor =
        formatColor.apply(AppPreferences.templateMapLabelForegroundColor.get());
    templateBorderColor = formatColor.apply(AppPreferences.templateMapLabelBorderColor.get());

    // map label other preferences
    mapLabelFontSize = AppPreferences.mapLabelFontSize.get() + "px";
    mapLabelBorderWidth =
        AppPreferences.mapLabelShowBorder.get()
            ? AppPreferences.mapLabelBorderWidth.get() + "px"
            : "0px";
    mapLabelBorderArc = AppPreferences.mapLabelBorderArc.get();
    mapLabelShowBorder = AppPreferences.mapLabelShowBorder.get() ? 1 : 0;

    // other preferences
    facingArrowBackgroundColor = formatColor.apply(AppPreferences.facingArrowBGColour.get());
    facingArrowBorderColor = formatColor.apply(AppPreferences.facingArrowBorderColour.get());
    chatNotificationForegroundColor = formatColor.apply(AppPreferences.chatNotificationColor.get());
    trustedPrefixForegroundColor = formatColor.apply(AppPreferences.trustedPrefixForeground.get());
    trustedPrefixBackgroundColor = formatColor.apply(AppPreferences.trustedPrefixBackground.get());
  }

  /** Get the name of the selected theme. */
  public String getThemeName() {
    return themeName;
  }

  /** Get the color scheme of the selected theme. */
  public String getThemeColorScheme() {
    return themeColorScheme;
  }

  /** Get the foreground color of NPC map labels. */
  public String getNpcForegroundColor() {
    return npcForegroundColor;
  }

  /** Get the background color of NPC map labels. */
  public String getNpcBackgroundColor() {
    return npcBackgroundColor;
  }

  /** Get the border color of NPC map labels. */
  public String getNpcBorderColor() {
    return npcBorderColor;
  }

  /** Get the foreground color of PC map labels. */
  public String getPcForegroundColor() {
    return pcForegroundColor;
  }

  /** Get the background color of PC map labels. */
  public String getPcBackgroundColor() {
    return pcBackgroundColor;
  }

  /** Get the border color of PC map labels. */
  public String getPcBorderColor() {
    return pcBorderColor;
  }

  /** Get the foreground color of non-visible token map labels. */
  public String getNonVisibleForegroundColor() {
    return nonVisibleForegroundColor;
  }

  /** Get the background color of non-visible token map labels. */
  public String getNonVisibleBackgroundColor() {
    return nonVisibleBackgroundColor;
  }

  /** Get the border color of non-visible token map labels. */
  public String getNonVisibleBorderColor() {
    return nonVisibleBorderColor;
  }

  /** Get the foreground color of drawing map labels. */
  public String getDrawingForegroundColor() {
    return drawingForegroundColor;
  }

  /** Get the background color of drawing map labels. */
  public String getDrawingBackgroundColor() {
    return drawingBackgroundColor;
  }

  /** Get the border color of drawing map labels. */
  public String getDrawingBorderColor() {
    return drawingBorderColor;
  }

  /** Get the foreground color of template map labels. */
  public String getTemplateForegroundColor() {
    return templateForegroundColor;
  }

  /** Get the background color of template map labels. */
  public String getTemplateBackgroundColor() {
    return templateBackgroundColor;
  }

  /** Get the border color of template map labels. */
  public String getTemplateBorderColor() {
    return templateBorderColor;
  }

  /** Get whether to show the map label borders. */
  public int getMapLabelShowBorder() {
    return mapLabelShowBorder;
  }

  /** Get the font size of the map labels. */
  public String getMapLabelFontSize() {
    return mapLabelFontSize;
  }

  /** Get the border width of the map labels. */
  public String getMapLabelBorderWidth() {
    return mapLabelBorderWidth;
  }

  /** Get the border arc of the map labels. */
  public int getMapLabelBorderArc() {
    return mapLabelBorderArc;
  }

  /** Get the border color of the facing arrow. */
  public String getFacingArrowBackgroundColor() {
    return facingArrowBackgroundColor;
  }

  /** Get the color scheme of the selected theme (i.e. light/dark). */
  public String getFacingArrowBorderColor() {
    return facingArrowBorderColor;
  }

  /** Get the foreground color of chat notifications. */
  public String getChatNotificationForegroundColor() {
    return chatNotificationForegroundColor;
  }

  /** Get the foreground color of trusted prefixes. */
  public String getTrustedPrefixForegroundColor() {
    return trustedPrefixForegroundColor;
  }

  /** Get the background color of trusted prefixes. */
  public String getTrustedPrefixBackgroundColor() {
    return trustedPrefixBackgroundColor;
  }
}
