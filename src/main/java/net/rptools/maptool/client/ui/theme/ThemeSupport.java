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
package net.rptools.maptool.client.ui.theme;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.IntelliJTheme.ThemeLaf;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.rptools.maptool.client.AppConstants;

public class ThemeSupport {

  public record ThemeDetails(
      String name, Class<? extends IntelliJTheme.ThemeLaf> themeClass, String imagePath) {}

  public static final ThemeDetails[] THEMES =
      new ThemeDetails[] {
        new ThemeDetails("Arc", com.formdev.flatlaf.intellijthemes.FlatArcIJTheme.class, null),
        new ThemeDetails(
            "Ark - Orange", com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme.class, null),
        new ThemeDetails(
            "Ark Dark", com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme.class, null),
        new ThemeDetails(
            "Ark Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkIJTheme.class,
            null),
        new ThemeDetails(
            "Ark Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkContrastIJTheme.class,
            null),
        new ThemeDetails(
            "Ark Dark - Orange",
            com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme.class,
            null),
        new ThemeDetails(
            "Atom One Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme.class,
            null),
        new ThemeDetails(
            "Atom One Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Atom One Light (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme.class,
            null),
        new ThemeDetails(
            "Atom One Light Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Carbon", com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme.class, null),
        new ThemeDetails(
            "Cobalt 2", com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme.class, null),
        new ThemeDetails(
            "Cyan Light", com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme.class, null),
        new ThemeDetails(
            "Dark Flat", com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.class, null),
        new ThemeDetails(
            "Dark Purple", com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme.class, null),
        new ThemeDetails(
            "Darcula", com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme.class, null),
        new ThemeDetails(
            "Darcula (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaIJTheme.class,
            null),
        new ThemeDetails(
            "Darcula Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaContrastIJTheme.class,
            null),
        new ThemeDetails(
            "GitHub (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme.class,
            null),
        new ThemeDetails(
            "GitHub Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubContrastIJTheme.class,
            null),
        new ThemeDetails(
            "GitHub Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme.class,
            null),
        new ThemeDetails(
            "GitHub Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Gradianto Dark Fuchsia",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme.class,
            null),
        new ThemeDetails(
            "Gradianto Deep Ocean",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme.class,
            null),
        new ThemeDetails(
            "Gradianto Midnight Blue",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme.class,
            null),
        new ThemeDetails(
            "Gradianto Nature Green",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme.class,
            null),
        new ThemeDetails("Gray", com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme.class, null),
        new ThemeDetails(
            "Gruvbox Dark Hard",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme.class,
            null),
        new ThemeDetails(
            "Gruvbox Dark Medium",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkMediumIJTheme.class,
            null),
        new ThemeDetails(
            "Gruvbox Dark Soft",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkSoftIJTheme.class,
            null),
        new ThemeDetails(
            "Hiberbee", com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme.class, null),
        new ThemeDetails(
            "High Contrast",
            com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme.class,
            null),
        new ThemeDetails(
            "Light Flat", com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme.class, null),
        new ThemeDetails(
            "Light Owl (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlIJTheme.class,
            null),
        new ThemeDetails(
            "Light Owl Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Material Dark Design",
            com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme.class,
            null),
        new ThemeDetails(
            "Material Darker",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme.class,
            null),
        new ThemeDetails(
            "Material Darker Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Material Deep Ocean",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
                .class,
            null),
        new ThemeDetails(
            "Material Deep Ocean Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialDeepOceanContrastIJTheme.class,
            null),
        new ThemeDetails(
            "Material Lighter",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme.class,
            null),
        new ThemeDetails(
            "Material Lighter Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialLighterContrastIJTheme.class,
            null),
        new ThemeDetails(
            "Material Oceanic",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme.class,
            null),
        new ThemeDetails(
            "Material Oceanic Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialOceanicContrastIJTheme.class,
            null),
        new ThemeDetails(
            "Material Palenight",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightIJTheme
                .class,
            null),
        new ThemeDetails(
            "Material Palenight Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialPalenightContrastIJTheme.class,
            null),
        new ThemeDetails(
            "Monocai", com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme.class, null),
        new ThemeDetails(
            "Monocai Pro", com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme.class, null),
        new ThemeDetails(
            "Monokai Pro (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme.class,
            null),
        new ThemeDetails(
            "Monokai Pro Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Moonlight (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme.class,
            null),
        new ThemeDetails(
            "Moonlight Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightContrastIJTheme
                .class,
            null),
        new ThemeDetails("Nord", com.formdev.flatlaf.intellijthemes.FlatNordIJTheme.class, null),
        new ThemeDetails(
            "Night Owl (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlIJTheme.class,
            null),
        new ThemeDetails(
            "Night Owl Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "One Dark", com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme.class, null),
        new ThemeDetails(
            "Solarized Dark",
            com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme.class,
            null),
        new ThemeDetails(
            "Solarized Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkIJTheme.class,
            null),
        new ThemeDetails(
            "Solarized Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Solarized Light",
            com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme.class,
            null),
        new ThemeDetails(
            "Solarized Light (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme.class,
            null),
        new ThemeDetails(
            "Solarized Light Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightContrastIJTheme
                .class,
            null),
        new ThemeDetails(
            "Spacegray", com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme.class, null),
        new ThemeDetails(
            "Vuesion", com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme.class, null),
        new ThemeDetails(
            "Xcode Dark", com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme.class, null)
      };

  private static ThemeDetails currentThemeDetails = THEMES[0];

  private static IntelliJTheme.ThemeLaf currentLaf;

  public static void loadTheme()
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
          IllegalAccessException, UnsupportedLookAndFeelException {
    JsonObject theme = readTheme();

    String themeName = theme.getAsJsonPrimitive("theme").getAsString();

    ThemeDetails themeDetails =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(themeName))
            .findFirst()
            .orElse(currentThemeDetails);
    if (themeDetails != null) {
      var laf = themeDetails.themeClass.getDeclaredConstructor().newInstance();
      UIManager.setLookAndFeel(themeDetails.themeClass.getDeclaredConstructor().newInstance());
      setLaf(laf);
      currentThemeDetails = themeDetails;
    }
  }

  private static void setLaf(ThemeLaf laf) {
    currentLaf = laf;
  }

  private static JsonObject readTheme() {
    try (InputStreamReader reader =
        new InputStreamReader(new FileInputStream(AppConstants.THEME_CONFIG_FILE))) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IOException e) {
      return toJSon(currentThemeDetails);
    }
  }

  private static void writeTheme(ThemeDetails newTheme) {
    var json = toJSon(newTheme);
    try (FileWriter writer = new FileWriter(AppConstants.THEME_CONFIG_FILE)) {
      writer.write(json.toString());
    } catch (IOException e) {
      // TODO: CDW
      e.printStackTrace();
    }
  }

  private static JsonObject toJSon(ThemeDetails theme) {
    JsonObject json = new JsonObject();
    json.addProperty("theme", theme.name);
    return json;
  }

  public static void setTheme(String theme) {
    var newTheme =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(theme))
            .findFirst()
            .orElse(currentThemeDetails);
    writeTheme(newTheme);
  }

  public static String getTheme() {
    return currentThemeDetails.name;
  }

  public static boolean isDark() {
    return currentLaf.isDark();
  }
}
