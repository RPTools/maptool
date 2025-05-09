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

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.FlatLaf;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jidesoft.plaf.LookAndFeelFactory;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.*;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.themes.*;
import net.rptools.maptool.events.MapToolEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class used to implement Theme support for MapTool. */
public class ThemeSupport {

  private static final Logger log = LogManager.getLogger(ThemeSupport.class);

  public enum ThemeColor {
    RED(
        "ColorPalette.red",
        Color.decode("#DB5860"),
        Color.decode("#C75450"),
        FlatIconColors.ACTIONS_RED,
        FlatIconColors.ACTIONS_RED_DARK),
    YELLOW(
        "ColorPalette.yellow",
        Color.decode("#EDA200"),
        Color.decode("#F0A732"),
        FlatIconColors.ACTIONS_YELLOW,
        FlatIconColors.ACTIONS_YELLOW_DARK),
    GREEN(
        "ColorPalette.green",
        Color.decode("#59A869"),
        Color.decode("#499C54"),
        FlatIconColors.ACTIONS_GREEN,
        FlatIconColors.ACTIONS_GREEN_DARK),
    BLUE(
        "ColorPalette.blue",
        Color.decode("#389FD6"),
        Color.decode("#3592C4"),
        FlatIconColors.ACTIONS_BLUE,
        FlatIconColors.ACTIONS_BLUE_DARK),
    GREY(
        "ColorPalette.gray",
        Color.decode("#6E6E6E"),
        Color.decode("#AFB1B3"),
        FlatIconColors.ACTIONS_GREY,
        FlatIconColors.ACTIONS_GREY_DARK),
    PURPLE(
        "ColorPalette.purple",
        Color.decode("#B99BF8"),
        Color.decode("#B99BF8"),
        FlatIconColors.OBJECTS_PURPLE,
        FlatIconColors.OBJECTS_PURPLE);

    private final String propertyName;
    private final Color defaultLightColor;
    private final Color defaultDarkColor;

    private final FlatIconColors lightIconColor;
    private final FlatIconColors darkIconColor;

    ThemeColor(
        String propertyName,
        Color defaultLightColor,
        Color defaultDarkColor,
        FlatIconColors lightIconColor,
        FlatIconColors darkIconColor) {
      this.propertyName = propertyName;
      this.defaultLightColor = defaultLightColor;
      this.defaultDarkColor = defaultDarkColor;
      this.lightIconColor = lightIconColor;
      this.darkIconColor = darkIconColor;
    }

    String getPropertyName() {
      return propertyName;
    }
  }

  /** The path to the images detailing the theme. */
  private static final String IMAGE_PATH = "/net/rptools/maptool/client/ui/themes/image/";

  /**
   * Should the the chat window use the themes colors.
   *
   * @return true if the chat window should use the themes colors.
   */
  public static boolean shouldUseThemeColorsForChat() {
    return useThemeColorsForChat;
  }

  /**
   * Should the the chat window use the themes colors.
   *
   * @param useThemeColorsForChat true if the chat window should use the themes colors.
   */
  public static void setUseThemeColorsForChat(boolean useThemeColorsForChat) {
    if (ThemeSupport.useThemeColorsForChat != useThemeColorsForChat) {
      ThemeSupport.useThemeColorsForChat = useThemeColorsForChat;
      writeTheme();
    }
  }

  /**
   * Record that contains the details about a theme.
   *
   * @param name the name of the theme
   * @param themeClass the class that implements the theme
   * @param imagePath the path to an example image of the theme
   */
  public record ThemeDetails(
      String name, Class<? extends FlatLaf> themeClass, String imagePath, boolean dark) {}

  /** The list of themes that are available. */
  public static final ThemeDetails[] THEMES =
      new ThemeDetails[] {
        new ThemeDetails(
            "Arc", com.formdev.flatlaf.intellijthemes.FlatArcIJTheme.class, "Arc.png", false),
        new ThemeDetails(
            "Arc - Orange",
            com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme.class,
            "Arc-Orange.png",
            false),
        new ThemeDetails(
            "Arc Dark",
            com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme.class,
            "Arc-Dark.png",
            true),
        new ThemeDetails(
            "Arc Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkIJTheme.class,
            "Arc-Dark-Material.png",
            true),
        new ThemeDetails(
            "Arc Dark Contrast (Material)",
            FlatArcDarkContrastIJTheme.class,
            "Arc-Dark-Contrast-Material.png",
            true),
        new ThemeDetails(
            "Arc Dark - Orange",
            com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme.class,
            "Arc-Dark-Orange.png",
            true),
        new ThemeDetails(
            "Atom One Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme.class,
            "Atom-One-Dark-Material.png",
            true),
        new ThemeDetails(
            "Atom One Dark Contrast (Material)",
            FlatAtomOneDarkContrastIJTheme.class,
            "Atom-One-Dark-Contrast-Material.png",
            true),
        new ThemeDetails(
            "Atom One Light (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme.class,
            "Atom-One-Light-Material.png",
            false),
        new ThemeDetails(
            "Atom One Light Contrast (Material)",
            FlatAtomOneLightContrastIJTheme.class,
            "Atom-One-Light-Contrast-Material.png",
            false),
        new ThemeDetails(
            "Carbon",
            com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme.class,
            "Carbon.png",
            true),
        new ThemeDetails(
            "Cobalt 2",
            com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme.class,
            "Cobalt-2.png",
            true),
        new ThemeDetails(
            "Cyan Light",
            com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme.class,
            "Cyan-Light.png",
            false),
        new ThemeDetails(
            "Dark Flat",
            com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.class,
            "Dark-Flat.png",
            true),
        new ThemeDetails(
            "Dark Purple",
            com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme.class,
            "Dark-Purple.png",
            true),
        new ThemeDetails(
            "Darcula",
            com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme.class,
            "Darcula.png",
            true),
        new ThemeDetails(
            "Darcula (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaIJTheme.class,
            "Darcula-Material.png",
            true),
        new ThemeDetails(
            "Darcula Contrast (Material)",
            FlatDraculaContrastIJTheme.class,
            "Darcula-Contrast-Material.png",
            true),
        new ThemeDetails(
            "GitHub (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme.class,
            "GitHub-Material.png",
            false),
        new ThemeDetails(
            "GitHub Contrast (Material)",
            FlatGitHubContrastIJTheme.class,
            "GitHub-Contrast-Material.png",
            false),
        new ThemeDetails(
            "GitHub Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme.class,
            "GitHub-Dark-Material.png",
            true),
        new ThemeDetails(
            "GitHub Dark Contrast (Material)",
            FlatGitHubDarkContrastIJTheme.class,
            "GitHub-Dark-Contrast-Material.png",
            true),
        new ThemeDetails(
            "Gradianto Dark Fuchsia",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme.class,
            "Gradianto-Dark-Fuchsia.png",
            true),
        new ThemeDetails(
            "Gradianto Deep Ocean",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme.class,
            "Gradianto-Deep-Ocean.png",
            true),
        new ThemeDetails(
            "Gradianto Midnight Blue",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme.class,
            "Gradianto-Midnight-Blue.png",
            true),
        new ThemeDetails(
            "Gradianto Nature Green",
            com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme.class,
            "Gradianto-Nature-Green.png",
            true),
        new ThemeDetails(
            "Gray", com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme.class, "Gray.png", false),
        new ThemeDetails(
            "Gruvbox Dark Hard",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme.class,
            "Gruvbox-Dark-Hard.png",
            true),
        new ThemeDetails(
            "Gruvbox Dark Medium",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkMediumIJTheme.class,
            "Gruvbox-Dark-Medium.png",
            true),
        new ThemeDetails(
            "Gruvbox Dark Soft",
            com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkSoftIJTheme.class,
            "Gruvbox-Dark-Soft.png",
            true),
        new ThemeDetails(
            "Hiberbee",
            com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme.class,
            "Hiberbee.png",
            true),
        new ThemeDetails(
            "High Contrast",
            com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme.class,
            "High-Contrast.png",
            true),
        new ThemeDetails(
            "Light Flat",
            com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme.class,
            "Light-Flat.png",
            false),
        new ThemeDetails(
            "Light Owl (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlIJTheme.class,
            "Light-Owl-Material.png",
            false),
        new ThemeDetails(
            "Light Owl Contrast (Material)",
            FlatLightOwlContrastIJTheme.class,
            "Light-Owl-Contrast-Material.png",
            false),
        new ThemeDetails(
            "Material Design Dark",
            com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme.class,
            "Material-Design-Dark.png",
            true),
        new ThemeDetails(
            "Material Darker",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme.class,
            "Material-Darker.png",
            true),
        new ThemeDetails(
            "Material Darker Contrast",
            FlatMaterialDarkerContrastIJTheme.class,
            "Material-Darker-Contrast.png",
            true),
        new ThemeDetails(
            "Material Deep Ocean",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
                .class,
            "Material-Deep-Ocean.png",
            true),
        new ThemeDetails(
            "Material Deep Ocean Contrast",
            FlatMaterialDeepOceanContrastIJTheme.class,
            "Material-Deep-Ocean-Contrast.png",
            true),
        new ThemeDetails(
            "Material Lighter",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme.class,
            "Material-Lighter.png",
            false),
        new ThemeDetails(
            "Material Lighter Contrast",
            FlatMaterialLighterContrastIJTheme.class,
            "Material-Lighter-Contrast.png",
            false),
        new ThemeDetails(
            "Material Oceanic",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme.class,
            "Material-Oceanic.png",
            true),
        new ThemeDetails(
            "Material Oceanic Contrast",
            FlatMaterialOceanicContrastIJTheme.class,
            "Material-Oceanic-Contrast.png",
            true),
        new ThemeDetails(
            "Material Palenight",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightIJTheme
                .class,
            "Material-Palenight.png",
            true),
        new ThemeDetails(
            "Material Palenight Contrast",
            FlatMaterialPalenightContrastIJTheme.class,
            "Material-Palenight-Contrast.png",
            true),
        new ThemeDetails(
            "Monocai",
            com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme.class,
            "Monocai.png",
            true),
        new ThemeDetails(
            "Monokai Pro",
            com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme.class,
            "Monokai-Pro.png",
            true),
        new ThemeDetails(
            "Monokai Pro (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme.class,
            "Monokai-Pro-Material.png",
            true),
        new ThemeDetails(
            "Monokai Pro Contrast (Material)",
            FlatMonokaiProContrastIJTheme.class,
            "Monokai-Pro-Contrast-Material.png",
            true),
        new ThemeDetails(
            "Moonlight (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme.class,
            "Moonlight-Material.png",
            true),
        new ThemeDetails(
            "Moonlight Contrast (Material)",
            FlatMoonlightContrastIJTheme.class,
            "Moonlight-Contrast-Material.png",
            true),
        new ThemeDetails(
            "Nord", com.formdev.flatlaf.intellijthemes.FlatNordIJTheme.class, "Nord.png", true),
        new ThemeDetails(
            "Night Owl (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlIJTheme.class,
            "Night-Owl-Material.png",
            true),
        new ThemeDetails(
            "Night Owl Contrast (Material)",
            FlatNightOwlContrastIJTheme.class,
            "Night-Owl-Contrast-Material.png",
            true),
        new ThemeDetails(
            "One Dark",
            com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme.class,
            "One-Dark.png",
            true),
        new ThemeDetails(
            "Solarized Dark",
            com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme.class,
            "Solarized-Dark.png",
            true),
        new ThemeDetails(
            "Solarized Dark (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkIJTheme.class,
            "Solarized-Dark-Material.png",
            true),
        new ThemeDetails(
            "Solarized Dark Contrast (Material)",
            FlatSolarizedDarkContrastIJTheme.class,
            "Solarized-Dark-Contrast-Material.png",
            true),
        new ThemeDetails(
            "Solarized Light",
            com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme.class,
            "Solarized-Light.png",
            false),
        new ThemeDetails(
            "Solarized Light (Material)",
            com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme.class,
            "Solarized-Light-Material.png",
            false),
        new ThemeDetails(
            "Solarized Light Contrast (Material)",
            FlatSolarizedLightContrastIJTheme.class,
            "Solarized-Light-Contrast-Material.png",
            false),
        new ThemeDetails(
            "Spacegray",
            com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme.class,
            "Spacegray.png",
            true),
        new ThemeDetails(
            "Vuesion",
            com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme.class,
            "Vuesion.png",
            true),
        new ThemeDetails(
            "Xcode Dark",
            com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme.class,
            "Xcode-Dark.png",
            true),
        new ThemeDetails("Aah", AahLAF.class, "Aah.png", false),
        new ThemeDetails("Aah(Large Print)", AahLAF_LP.class, "Aah-LP.png", false),
        new ThemeDetails("Aah(Small Print)", AahLAF_SP.class, "Aah-SP.png", false),
        new ThemeDetails("Aah(Very Large Print)", AahLAF_VLP.class, "Aah-VLP.png", false),
        new ThemeDetails("Aark", AarkLaF.class, "Aark.png", true),
      };

  /** The current theme being used. */
  private static ThemeDetails currentThemeDetails = THEMES[0];

  /** The theme that will be applied after restart. */
  private static ThemeDetails pendingThemeDetails = currentThemeDetails;

  /** The current look and feel in use. */
  private static FlatLaf currentLaf;

  /** Should the chat window use the colors from the theme. */
  private static boolean useThemeColorsForChat = false;

  private static boolean startupUseThemeColorsForChat = false;

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
      throws NoSuchMethodException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException,
          UnsupportedLookAndFeelException {
    JsonObject theme = readTheme();

    String themeName = theme.getAsJsonPrimitive("theme").getAsString();
    if (theme.has("useThemeColorsForChat")) {
      useThemeColorsForChat = theme.getAsJsonPrimitive("useThemeColorsForChat").getAsBoolean();
      startupUseThemeColorsForChat = useThemeColorsForChat;
    }

    ThemeDetails themeDetails =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(themeName))
            .findFirst()
            .orElse(currentThemeDetails);
    if (themeDetails != null) {
      var laf = themeDetails.themeClass.getDeclaredConstructor().newInstance();
      UIManager.setLookAndFeel(themeDetails.themeClass.getDeclaredConstructor().newInstance());
      LookAndFeelFactory.installJideExtension();
      setLaf(laf);
      currentThemeDetails = themeDetails;
      pendingThemeDetails = themeDetails;
    }

    new MapToolEventBus().getMainEventBus().post(new ThemeLoadedEvent(currentThemeDetails));
  }

  /**
   * Sets the look and feel to use.
   *
   * @param laf the look and feel to use.
   */
  private static void setLaf(FlatLaf laf) {
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

  /** Writes the theme to the settings file. */
  private static void writeTheme() {
    var json = toJSon(pendingThemeDetails);
    json.addProperty("useThemeColorsForChat", useThemeColorsForChat);
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
    pendingThemeDetails =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(theme))
            .findFirst()
            .orElse(currentThemeDetails);
    writeTheme();
  }

  /**
   * Returns the current theme name.
   *
   * @return the current theme name.
   */
  public static String getThemeName() {
    return currentThemeDetails.name;
  }

  /**
   * Returns the current theme information.
   *
   * @return the current theme information.
   */
  public static ThemeDetails getCurrentThemeDetails() {
    return currentThemeDetails;
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
      var imageLocation = IMAGE_PATH + themeDetails.imagePath;
      log.info("Retrieving resource for theme name={} from location={}", themeName, imageLocation);
      var imageURL = ThemeSupport.class.getResource(imageLocation);
      if (imageURL == null) {
        log.warn(
            "Failed to retrieve resource for theme name={} from url={}, using empty ImageIcon");
        return new ImageIcon();
      }
      var imageIcon = new ImageIcon(imageURL, themeDetails.name);
      if (dimension != null && dimension.width > 0 && dimension.height > 0) {
        imageIcon.setImage(
            imageIcon
                .getImage()
                .getScaledInstance(dimension.width, dimension.height, Image.SCALE_AREA_AVERAGING));
      }
      return imageIcon;
    }
  }

  /**
   * Returns if there is a a new theme that will be applied after the restart.
   *
   * @return if there is a a new theme that will be applied after the restart.
   */
  public static boolean needsRestartForNewTheme() {
    return useThemeColorsForChat != startupUseThemeColorsForChat
        || !pendingThemeDetails.equals(currentThemeDetails);
  }

  /**
   * Returns what the theme will be after a restart of MapTool.
   *
   * @return what the theme will be after a restart of MapTool.
   */
  public static String getThemeAfterRestart() {
    return pendingThemeDetails.name;
  }

  /**
   * Returns one of the named theme colors.
   *
   * @param themeColor the color to return.
   * @return the color.
   */
  public static Color getThemeColor(ThemeColor themeColor) {
    Color color = null;
    if (currentThemeDetails.dark()) {
      color = UIManager.getColor(themeColor.darkIconColor.key);
    }

    if (color == null) {
      color = UIManager.getColor(themeColor.lightIconColor.key);
    }

    if (color == null) {
      if (currentThemeDetails.dark()) {
        color = themeColor.defaultDarkColor;
      } else {
        color = themeColor.defaultLightColor;
      }
    }
    return color;
  }

  public static String getThemeColorHexString(ThemeColor themeColor) {
    return String.format("#%06x", getThemeColor(themeColor).getRGB() & 0x00FFFFFF);
  }
}
