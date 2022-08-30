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
import java.awt.Dimension;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;

/** Class used to implement Theme support for MapTool. */
public class ThemeSupport {

  /** The path to the images detailing the theme. */
  private static final String IMAGE_PATH = "/net/rptools/maptool/client/ui/themes/image/";

  /**
   * Record that contains the details about a theme.
   *
   * @param name the name of the theme
   * @param themeClass the class that implements the theme
   * @param imagePath the path to an example image of the theme
   */
  public record ThemeDetails(
      String name, Class<? extends IntelliJTheme.ThemeLaf> themeClass, String imagePath) {}

  /** The list of themes that are available. */
  public static final ThemeDetails[] THEMES =
      new ThemeDetails[] {
        new ThemeDetails("Arc", com.formdev.flatlaf.intellijthemes.FlatArcIJTheme.class, "Arc.png"),
        new ThemeDetails(
            "Arc - Orange",
            com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme.class,
            "Arc-Orange.png"),
        new ThemeDetails(
            "Arc Dark",
            com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme.class,
            "Arc-Dark.png"),
        new ThemeDetails(
            "Arc Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkIJTheme.class,
            "Arc-Dark-Material.png"),
        new ThemeDetails(
            "Arc Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkContrastIJTheme.class,
            "Arc-Dark-Contrast-Material.png"),
        new ThemeDetails(
            "Arc Dark - Orange",
            com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme.class,
            "Arc-Dark-Orange.png"),
        new ThemeDetails(
            "Atom One Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme.class,
            "Atom-One-Dark-Material.png"),
        new ThemeDetails(
            "Atom One Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme
                .class,
            "Atom-One-Dark-Contrast-Material.png"),
        new ThemeDetails(
            "Atom One Light (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme.class,
            "Atom-One-Light-Material.png"),
        new ThemeDetails(
            "Atom One Light Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightContrastIJTheme
                .class,
            "Atom-One-Light-Contrast-Material.png"),
        new ThemeDetails(
            "Carbon", com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme.class, "Carbon.png"),
        new ThemeDetails(
            "Cobalt 2",
            com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme.class,
            "Cobalt-2.png"),
        new ThemeDetails(
            "Cyan Light",
            com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme.class,
            "Cyan-Light.png"),
        new ThemeDetails(
            "Dark Flat",
            com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.class,
            "Dark-Flat.png"),
        new ThemeDetails(
            "Dark Purple",
            com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme.class,
            "Dark-Purple.png"),
        new ThemeDetails(
            "Darcula", com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme.class, "Darcula.png"),
        new ThemeDetails(
            "Darcula (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaIJTheme.class,
            "Darcula-Material.png"),
        new ThemeDetails(
            "Darcula Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaContrastIJTheme.class,
            "Darcula-Contrast-Material.png"),
        new ThemeDetails(
            "GitHub (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme.class,
            "GitHub-Material.png"),
        new ThemeDetails(
            "GitHub Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubContrastIJTheme.class,
            "GitHub-Contrast-Material.png"),
        new ThemeDetails(
            "GitHub Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme.class,
            "GitHub-Dark-Material.png"),
        new ThemeDetails(
            "GitHub Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkContrastIJTheme
                .class,
            "GitHub-Dark-Contrast-Material.png"),
        new ThemeDetails(
            "Gradianto Dark Fuchsia",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme.class,
            "Gradianto-Dark-Fuchsia.png"),
        new ThemeDetails(
            "Gradianto Deep Ocean",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme.class,
            "Gradianto-Deep-Ocean.png"),
        new ThemeDetails(
            "Gradianto Midnight Blue",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme.class,
            "Gradianto-Midnight-Blue.png"),
        new ThemeDetails(
            "Gradianto Nature Green",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme.class,
            "Gradianto-Nature-Green.png"),
        new ThemeDetails(
            "Gray", com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme.class, "Gray.png"),
        new ThemeDetails(
            "Gruvbox Dark Hard",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme.class,
            "Gruvbox-Dark-Hard.png"),
        new ThemeDetails(
            "Gruvbox Dark Medium",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkMediumIJTheme.class,
            "Gruvbox-Dark-Medium.png"),
        new ThemeDetails(
            "Gruvbox Dark Soft",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkSoftIJTheme.class,
            "Gruvbox-Dark-Soft.png"),
        new ThemeDetails(
            "Hiberbee",
            com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme.class,
            "Hiberbee.png"),
        new ThemeDetails(
            "High Contrast",
            com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme.class,
            "High-Contrast.png"),
        new ThemeDetails(
            "Light Flat",
            com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme.class,
            "Light-Flat.png"),
        new ThemeDetails(
            "Light Owl (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlIJTheme.class,
            "Light-Owl-Material.png"),
        new ThemeDetails(
            "Light Owl Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlContrastIJTheme
                .class,
            "Light-Owl-Contrast-Material.png"),
        new ThemeDetails(
            "Material Design Dark",
            com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme.class,
            "Material-Design-Dark.png"),
        new ThemeDetails(
            "Material Darker",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme.class,
            "Material-Darker.png"),
        new ThemeDetails(
            "Material Darker Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerContrastIJTheme
                .class,
            "Material-Darker-Contrast.png"),
        new ThemeDetails(
            "Material Deep Ocean",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
                .class,
            "Material-Deep-Ocean.png"),
        new ThemeDetails(
            "Material Deep Ocean Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialDeepOceanContrastIJTheme.class,
            "Material-Deep-Ocean-Contrast.png"),
        new ThemeDetails(
            "Material Lighter",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme.class,
            "Material-Lighter.png"),
        new ThemeDetails(
            "Material Lighter Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialLighterContrastIJTheme.class,
            "Material-Lighter-Contrast.png"),
        new ThemeDetails(
            "Material Oceanic",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme.class,
            "Material-Oceanic.png"),
        new ThemeDetails(
            "Material Oceanic Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialOceanicContrastIJTheme.class,
            "Material-Oceanic-Contrast.png"),
        new ThemeDetails(
            "Material Palenight",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightIJTheme
                .class,
            "Material-Palenight.png"),
        new ThemeDetails(
            "Material Palenight Contrast",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite
                .FlatMaterialPalenightContrastIJTheme.class,
            "Material-Palenight-Contrast.png"),
        new ThemeDetails(
            "Monocai", com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme.class, "Monocai.png"),
        new ThemeDetails(
            "Monokai Pro",
            com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme.class,
            "Monokai-Pro.png"),
        new ThemeDetails(
            "Monokai Pro (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme.class,
            "Monokai-Pro-Material.png"),
        new ThemeDetails(
            "Monokai Pro Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProContrastIJTheme
                .class,
            "Monokai-Pro-Contrast-Material.png"),
        new ThemeDetails(
            "Moonlight (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme.class,
            "Moonlight-Material.png"),
        new ThemeDetails(
            "Moonlight Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightContrastIJTheme
                .class,
            "Moonlight-Contrast-Material.png"),
        new ThemeDetails(
            "Nord", com.formdev.flatlaf.intellijthemes.FlatNordIJTheme.class, "Nord.png"),
        new ThemeDetails(
            "Night Owl (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlIJTheme.class,
            "Night-Owl-Material.png"),
        new ThemeDetails(
            "Night Owl Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlContrastIJTheme
                .class,
            "Night-Owl-Contrast-Material.png"),
        new ThemeDetails(
            "One Dark",
            com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme.class,
            "One-Dark.png"),
        new ThemeDetails(
            "Solarized Dark",
            com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme.class,
            "Solarized-Dark.png"),
        new ThemeDetails(
            "Solarized Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkIJTheme.class,
            "Solarized-Dark-Material.png"),
        new ThemeDetails(
            "Solarized Dark Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkContrastIJTheme
                .class,
            "Solarized-Dark-Contrast-Material.png"),
        new ThemeDetails(
            "Solarized Light",
            com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme.class,
            "Solarized-Light.png"),
        new ThemeDetails(
            "Solarized Light (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme.class,
            "Solarized-Light-Material.png"),
        new ThemeDetails(
            "Solarized Light Contrast (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightContrastIJTheme
                .class,
            "Solarized-Light-Contrast-Material.png"),
        new ThemeDetails(
            "Spacegray",
            com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme.class,
            "Spacegray.png"),
        new ThemeDetails(
            "Vuesion", com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme.class, "Vuesion.png"),
        new ThemeDetails(
            "Xcode Dark",
            com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme.class,
            "Xcode-Dark.png"),
      };

  /** The current theme being used. */
  private static ThemeDetails currentThemeDetails = THEMES[0];

  /** The current look and feel in use. */
  private static IntelliJTheme.ThemeLaf currentLaf;

  /**
   * Loads the details of the theme to use.
   *
   * @throws NoSuchMethodException if there is an error finding the theme class.
   * @throws InvocationTargetException if there is an error invoking the theme class.
   * @throws InstantiationException if there is an error instantiating the theme class.
   * @throws IllegalAccessException if there is an error accessing the theme class.
   * @throws UnsupportedLookAndFeelException if the look and feel is not supported.
   */
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

  /**
   * Sets the look and feel to use.
   *
   * @param laf the look and feel to use.
   */
  private static void setLaf(ThemeLaf laf) {
    currentLaf = laf;
  }

  /**
   * Reads the theme from the settings file.
   *
   * @return the theme from the settings file.
   */
  private static JsonObject readTheme() {
    try (InputStreamReader reader =
        new InputStreamReader(new FileInputStream(AppConstants.THEME_CONFIG_FILE))) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IOException e) {
      return toJSon(currentThemeDetails);
    }
  }

  /**
   * Writes the theme to the settings file.
   *
   * @param newTheme the theme to write.
   */
  private static void writeTheme(ThemeDetails newTheme) {
    var json = toJSon(newTheme);
    try (FileWriter writer = new FileWriter(AppConstants.THEME_CONFIG_FILE)) {
      writer.write(json.toString());
    } catch (IOException e) {
      MapTool.showError("msg.error.cantSaveTheme", e);
    }
  }

  /**
   * Converts the theme details to a JSON object.
   *
   * @param theme the theme details to convert.
   * @return the JSON object.
   */
  private static JsonObject toJSon(ThemeDetails theme) {
    JsonObject json = new JsonObject();
    json.addProperty("theme", theme.name);
    return json;
  }

  /**
   * Sets the theme to use from the name.
   *
   * @param theme the name of the theme to use.
   */
  public static void setTheme(String theme) {
    var newTheme =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(theme))
            .findFirst()
            .orElse(currentThemeDetails);
    writeTheme(newTheme);
  }

  /**
   * Returns the current theme name.
   *
   * @return the current theme name.
   */
  public static String getTheme() {
    return currentThemeDetails.name;
  }

  /**
   * Returns if the theme is a dark theme or not.
   *
   * @return if the theme is a dark theme or not.
   */
  public static boolean isDark() {
    return currentLaf.isDark();
  }

  /**
   * Returns an {@link ImageIcon} for an example of the current theme.
   *
   * @param dimension the size of the image.
   * @return an {@link ImageIcon} for an example of the current theme.
   */
  public static ImageIcon getExampleImageIcon(Dimension dimension) {
    return getExampleImageIcon(currentThemeDetails.name, dimension);
  }

  /**
   * Returns an {@link ImageIcon} for an example of the specified theme.
   *
   * @param themeName the name of the theme to get the image for.
   * @param dimension the size of the image.
   * @return an {@link ImageIcon} for an example of the specified theme.
   */
  public static ImageIcon getExampleImageIcon(String themeName, Dimension dimension) {
    ThemeDetails themeDetails =
        Arrays.stream(THEMES).filter(t -> t.name.equals(themeName)).findFirst().orElse(null);
    if (themeDetails == null
        || themeDetails.imagePath == null
        || themeDetails.imagePath.isEmpty()) {
      return new ImageIcon();
    } else {
      var imageIcon =
          new ImageIcon(
              ThemeSupport.class.getResource(IMAGE_PATH + themeDetails.imagePath),
              themeDetails.name);
      if (dimension != null && dimension.width > 0 && dimension.height > 0) {
        imageIcon.setImage(
            imageIcon
                .getImage()
                .getScaledInstance(dimension.width, dimension.height, Image.SCALE_AREA_AVERAGING));
      }
      return imageIcon;
    }
  }
}
