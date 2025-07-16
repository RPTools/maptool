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

import com.formdev.flatlaf.FlatLaf;
import java.awt.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.FontChooser;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThemeFontTools {
  private static final Logger log = LogManager.getLogger(ThemeFontTools.class);
  public static final Properties MODEL_FLAT_PROPERTIES = new Properties();
  public static final List<String> MODEL_FLAT_PROPERTY_NAMES = new ArrayList<>();
  public static final Properties USER_FLAT_PROPERTIES = new Properties();
  public static final List<String> USER_FLAT_PROPERTY_NAMES = new ArrayList<>();

  private static final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  public static final List<Font> FONT_LIST =
      Arrays.stream(ge.getAllFonts()).sorted(Comparator.comparing(Font::getName)).toList();
  public static final Map<String, Integer> FONT_MAX_NAME_WIDTHS =
      new HashMap<>() {
        {
          put("name", 0);
          put("fontName", 0);
          put("familyName", 0);
        }
      };

  private static final int WIN_DEFAULT_FONT_SIZE =
      (Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font")) == null
          ? 12
          : ((Font) Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font"))
              .getSize();

  public static final int OS_DEFAULT_FONT_SIZE =
      SystemUtils.IS_OS_WINDOWS ? WIN_DEFAULT_FONT_SIZE : SystemUtils.IS_OS_MAC ? 13 : 12;
  public static final Map<String, Integer> FLAT_LAF_DEFAULT_FONT_SIZES = new HashMap<>();
  public static final Set<String> GENERAL_FONT_KEYS = FLAT_LAF_DEFAULT_FONT_SIZES.keySet();
  public static final List<String> FONT_STYLE_FAMILIES =
      new ArrayList<>(List.of("defaultFont", "light.font", "semibold.font", "monospaced.font"));

  private static final Path FLATLAF_PROPERTIES_FILE;

  static {
    FLATLAF_PROPERTIES_FILE =
        AppUtil.getAppHome("config").getAbsoluteFile().toPath().resolve("FlatLaf.properties");

    if (AppPreferences.useCustomThemeFontProperties.get()) {
      // create file if it does not exist
      try {
        Files.createFile(FLATLAF_PROPERTIES_FILE);
      } catch (FileAlreadyExistsException ignored) {
        // expected result
      } catch (IOException e) {
        log.warn("Unable to create user theme properties file.");
      }
      try (InputStream is = new FileInputStream(FLATLAF_PROPERTIES_FILE.toFile())) {
        USER_FLAT_PROPERTIES.load(is);
        USER_FLAT_PROPERTY_NAMES.addAll(USER_FLAT_PROPERTIES.stringPropertyNames());
      } catch (IOException ignored) {
      }
    }
    // load the model properties regardless - otherwise the preferences section fails
    try (InputStream is =
        ThemeFontTools.class.getResourceAsStream(
            "/net/rptools/maptool/client/ui/themes/modelUserTheme.properties")) {
      MODEL_FLAT_PROPERTIES.load(is);
      MODEL_FLAT_PROPERTY_NAMES.addAll(MODEL_FLAT_PROPERTIES.stringPropertyNames());
      for (String key : MODEL_FLAT_PROPERTY_NAMES) {
        FLAT_LAF_DEFAULT_FONT_SIZES.put(
            key, Integer.parseInt((String) MODEL_FLAT_PROPERTIES.get(key)));
      }
    } catch (IOException ignored) {
    }

    Graphics g =
        ge.getDefaultScreenDevice()
            .getDefaultConfiguration()
            .createCompatibleVolatileImage(300, 50)
            .getGraphics();
    FONT_LIST.forEach(
        font -> {
          font = font.deriveFont(OS_DEFAULT_FONT_SIZE * 1f);
          int nameWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(font), font.getName());
          int fontNameWidth =
              SwingUtilities.computeStringWidth(g.getFontMetrics(font), font.getFontName());
          int familyNameWidth =
              SwingUtilities.computeStringWidth(g.getFontMetrics(font), font.getFamily());
          if (nameWidth > FONT_MAX_NAME_WIDTHS.get("name")) {
            FONT_MAX_NAME_WIDTHS.put("name", nameWidth);
          }
          if (fontNameWidth > FONT_MAX_NAME_WIDTHS.get("fontName")) {
            FONT_MAX_NAME_WIDTHS.put("fontName", fontNameWidth);
          }
          if (familyNameWidth > FONT_MAX_NAME_WIDTHS.get("familyName")) {
            FONT_MAX_NAME_WIDTHS.put("familyName", familyNameWidth);
          }
        });
    g.dispose();
  }

  /*
   * Call before applying Flat look and feel to register additional/changed properties
   */
  public static void flatusInterruptus() {
    FlatLaf.registerCustomDefaultsSource(AppUtil.getAppHome("config"));
    if (USER_FLAT_PROPERTY_NAMES.contains("defaultFont")) {
      Font font = parseFlatPropertyString(USER_FLAT_PROPERTIES.getProperty("defaultFont"));
      if (font != null) {
        FlatLaf.setPreferredFontFamily(font.getFamily());
      }
    }
    if (USER_FLAT_PROPERTY_NAMES.contains("light.font")) {
      Font font = parseFlatPropertyString(USER_FLAT_PROPERTIES.getProperty("light.font"));
      if (font != null) {
        FlatLaf.setPreferredLightFontFamily(font.getFamily());
      }
    }
    if (USER_FLAT_PROPERTY_NAMES.contains("semibold.font")) {
      Font font = parseFlatPropertyString(USER_FLAT_PROPERTIES.getProperty("semibold.font"));
      if (font != null) {
        FlatLaf.setPreferredSemiboldFontFamily(font.getFamily());
      }
    }
    if (USER_FLAT_PROPERTY_NAMES.contains("monospaced.font")) {
      Font font = parseFlatPropertyString(USER_FLAT_PROPERTIES.getProperty("monospaced.font"));
      if (font != null) {
        FlatLaf.setPreferredMonospacedFontFamily(font.getFamily());
      }
    }
  }

  private static Font getFontByName(String name) {
    Font font = Font.getFont(name);
    if (font == null) {
      font =
          FONT_LIST.stream()
              .dropWhile(
                  f ->
                      !(f.getName().equalsIgnoreCase(name)
                          || f.getFontName().equalsIgnoreCase(name)
                          || f.getPSName().equalsIgnoreCase(name)))
              .findAny()
              .orElse(null);
    }
    if (font == null) {
      font =
          FONT_LIST.stream()
              .dropWhile(f -> !f.getFamily().equalsIgnoreCase(name))
              .findAny()
              .orElse(null);
    }
    return font;
  }

  protected static Font parseFlatPropertyString(String s) {
    if (s != null && !s.isBlank()) {
      s = s.replaceAll(" {2,}", " ");
      // split on spaces not contained within quotes
      String[] split = s.split(" (?=(([^\"]*\"){2})*[^\"]*$)");
      // remove th quotes
      for (int i = 0; i < split.length; i++) {
        split[i] = split[i].replaceAll("\"", "");
      }
      if (split.length > 0) {
        Font font = null;
        if (split.length == 4) {
          font = Font.getFont(split[3]);
          return font.deriveFont(Font.BOLD + Font.ITALIC, Float.parseFloat(split[0]));
        }
        boolean bold = false;
        boolean italic = false;
        float size = Float.NaN;
        String name = null;
        for (String part : split) {
          bold = bold ? bold : part.equalsIgnoreCase("bold");
          italic = italic ? italic : part.equalsIgnoreCase("italic");
          if (Float.isNaN(size)) {
            try {
              size = Float.parseFloat(part);
            } catch (NumberFormatException ignored) {
            }
          }
          if (name == null) {
            font = getFontByName(part);
            if (font != null) {
              name = font.getName();
            }
          }
        }

        if (font == null) {
          font = Font.decode(Font.SANS_SERIF);
        }

        font =
            font.deriveFont(
                (bold ? Font.BOLD : Font.PLAIN) + (italic ? Font.ITALIC : Font.PLAIN),
                Float.isNaN(size) ? 1f : size);
        return font;
      }
    }
    return null;
  }

  public static boolean writeCustomProperties(Map<String, FontChooser> properties) {
    for (String key : MODEL_FLAT_PROPERTY_NAMES) {
      if (properties.containsKey(key)) {
        String entry = properties.get(key).toFlatPropertyString();
        if (entry.isBlank()) {
          USER_FLAT_PROPERTIES.remove(key);
        } else {
          USER_FLAT_PROPERTIES.put(key, entry);
        }
      } else {
        USER_FLAT_PROPERTIES.remove(key);
      }
    }
    try {
      USER_FLAT_PROPERTIES.store(
          new FileOutputStream(FLATLAF_PROPERTIES_FILE.toFile()), "User properties");
      log.info("User font preferences written to config directory.");
      return true;
    } catch (IOException e) {
      // Could not write user font preferences to config directory.
      MapTool.showError("msg.error.cantSaveTheme", e);
      return false;
    }
  }
}
