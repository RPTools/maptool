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
package net.rptools.maptool.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/*
 * User Preferences are stored next to jar when installed.
 * A copy will also be kept in users home directory to persist across installs/upgrades.
 */
public class UserJvmOptions {

  private static final Logger log = LogManager.getLogger(UserJvmOptions.class);

  private static final String I18N_RESOURCE_PREFIX = "i18n_";
  private static final String I18N_RESOURCE_PATH = "net/rptools/maptool/language";
  private static final String CURRENT_DATA_DIR = AppUtil.getAppHome().getName();
  private static final Pattern UNIT_PATTERN = Pattern.compile("^([0-9]+)[g|G|m|M|k|K]$");
  private static final Map<String, String> LANGUAGE_MAP = getResourceBundles();

  private static FileBasedConfigurationBuilder<INIConfiguration> configurationBuilder;
  private static SubnodeConfiguration jvmNodeConfiguration;

  public static void resetJvmOptions() {
    log.info("Resetting all startup options to defaults!");

    setJvmOption(JVM_OPTION.MAX_MEM, "");
    setJvmOption(JVM_OPTION.MIN_MEM, "");
    setJvmOption(JVM_OPTION.STACK_SIZE, "");
    setJvmOption(JVM_OPTION.ASSERTIONS, "");
    setJvmOption(JVM_OPTION.DATA_DIR, "");
    setJvmOption(JVM_OPTION.LOCALE_LANGUAGE, "");
    setJvmOption(JVM_OPTION.LOCALE_COUNTRY, "");
    setJvmOption(JVM_OPTION.JAVA2D_D3D, "");
    setJvmOption(JVM_OPTION.JAVA2D_OPENGL_OPTION, "");
    setJvmOption(JVM_OPTION.MACOSX_EMBEDDED_OPTION, "");
  }

  public static boolean loadAppCfg() {
    Configurations configurations = new Configurations();
    INIConfiguration iniConfiguration;

    File cfgFile = AppUtil.getDataDirAppCfgFile();

    configurationBuilder = configurations.iniBuilder(cfgFile);

    try {
      iniConfiguration = configurationBuilder.getConfiguration();
    } catch (ConfigurationException e) {
      log.error("Error loading JVM cfg file.", e);
      return false;
    }

    // Default is " = " which breaks launcher
    iniConfiguration.setSeparatorUsedInOutput("=");

    // For some reason this prevents the output from wrapping app.classpath in quotes /shrug
    iniConfiguration.setCommentLeadingCharsUsedInInput("#");

    Set<String> sections = iniConfiguration.getSections();
    log.debug("sections: {}", sections);

    // Get only JVM Options Section
    jvmNodeConfiguration = iniConfiguration.getSection("JavaOptions");

    // Special handling of -X memory parameters
    jvmNodeConfiguration
        .getKeys()
        .forEachRemaining(
            key -> {
              if (key.startsWith(JVM_OPTION.MAX_MEM.parameterName)
                  || key.startsWith(JVM_OPTION.MIN_MEM.parameterName)
                  || key.startsWith(JVM_OPTION.STACK_SIZE.parameterName)) {
                String value = key.substring(JVM_OPTION.MAX_MEM.parameterName.length());
                String newKey = key.substring(0, JVM_OPTION.MAX_MEM.parameterName.length());
                jvmNodeConfiguration.setProperty(newKey, value);
                log.debug("Updating on load {}:{}", key, jvmNodeConfiguration.getString(key));
              }
            });

    return true;
  }

  public static boolean saveAppCfg() {
    // Special handling of -X memory parameters and parms that don't follow key=value notation
    // e.g. -XSS and -XX:+ShowCodeDetailsInExceptionMessages
    jvmNodeConfiguration
        .getKeys()
        .forEachRemaining(
            key -> {
              if (key.startsWith(JVM_OPTION.MAX_MEM.parameterName)
                  || key.startsWith(JVM_OPTION.MIN_MEM.parameterName)
                  || key.startsWith(JVM_OPTION.STACK_SIZE.parameterName)) {
                String value = "";
                String newKey = key + jvmNodeConfiguration.getString(key);
                jvmNodeConfiguration.setProperty(newKey, value);
                jvmNodeConfiguration.clearProperty(key);
                log.debug(
                    "Updating {}={} to {}={}",
                    key,
                    jvmNodeConfiguration.getString(key),
                    newKey,
                    value);
              } else if (key.startsWith("-XX")) {
                String value = "";
                String newKey =
                    key + ":" + jvmNodeConfiguration.getString(key).replaceAll("\\=", "");
                jvmNodeConfiguration.setProperty(newKey, value);
                jvmNodeConfiguration.clearProperty(key);

                log.debug(
                    "Updating {}={} to {}={}",
                    key,
                    jvmNodeConfiguration.getString(key),
                    newKey,
                    value);
              }
            });

    log.debug("Attempting to save JVM configurations...");

    try {
      configurationBuilder.save();
    } catch (ConfigurationException e) {
      MapTool.showError("startup.config.unableToWrite", e);
      return false;
    }

    log.debug("JVM configurations saved!");

    copyConfigFile();
    return true;
  }

  private static void copyConfigFile() {
    File userDirAppConfig = AppUtil.getDataDirAppCfgFile();
    File appConfig = AppUtil.getAppCfgFile();

    if (appConfig == null || !appConfig.canWrite()) {
      return; // If not running from install or its not possible to write to install configuration
      // file then skip copy.
    }

    try {
      Files.copy(
          userDirAppConfig.toPath(), appConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      MapTool.showError("msg.error.copyingStartupConfig", e);
    }
  }

  public static String getJvmOption(JVM_OPTION option) {
    if (option.equals(JVM_OPTION.LOCALE_LANGUAGE)) {
      // Translate 2 letter language code to display language
      String languageCode =
          jvmNodeConfiguration.getString(option.parameterName, option.defaultValue);
      for (Entry<String, String> entry : LANGUAGE_MAP.entrySet()) {
        if (Objects.equals(languageCode, entry.getValue())) {
          return entry.getKey();
        }
      }

      // Language Code not found, default option to English
      return "English";
    }

    return jvmNodeConfiguration.getString(option.parameterName, option.defaultValue);
  }

  public static boolean hasJvmOption(JVM_OPTION option) {
    return jvmNodeConfiguration.getBoolean(
        option.parameterName, Boolean.parseBoolean(option.defaultValue));
  }

  public static void setJvmOption(JVM_OPTION option, String value) {
    // Translate display language to 2 letter language code
    // English will be a blank value so option is removed completely from cfg
    if (option.equals(JVM_OPTION.LOCALE_LANGUAGE)) {
      value = LANGUAGE_MAP.get(value);
    }

    if (value.isBlank()) {
      log.debug("Clearing property: {}, value is blank {}", option.parameterName, value);
      jvmNodeConfiguration.clearProperty(option.parameterName);
    } else {
      jvmNodeConfiguration.setProperty(option.parameterName, value);
      log.debug("Setting property: {} to {}", option.parameterName, value);
    }
  }

  public static void setJvmOption(JVM_OPTION option, boolean value) {
    jvmNodeConfiguration.setProperty(option.parameterName, value);
    log.debug("Setting boolean property: {} to {}", option.parameterName, value);
  }

  public static boolean verifyJvmOptions(String s) {
    // Allow empty/null values as NO memory settings are technically allowed
    // although not recommended...
    if (s.isEmpty()) {
      return true;
    }

    Matcher m = UNIT_PATTERN.matcher(s);

    if (!m.find()) {
      // If we don't find a valid memory setting return false
      MapTool.showError(I18N.getText("msg.error.jvm.options", s));
      return false;
    } else {
      // Don't allow values less than 0
      return Integer.parseInt(m.group(1)) > 0;
    }
  }

  private static Map<String, String> getResourceBundles() {
    Map<String, String> languages = new TreeMap<>();

    Locale loc = new Locale("en");
    languages.put(loc.getDisplayLanguage(), "");

    Reflections reflections = new Reflections(I18N_RESOURCE_PATH, new ResourcesScanner());
    Set<String> resourcePathSet =
        reflections.getResources(Pattern.compile(I18N_RESOURCE_PREFIX + ".*\\.properties"));
    log.debug("getResourceBundles() resourcePathSet: " + resourcePathSet.toString());

    for (String resourcePath : resourcePathSet) {
      int index = I18N_RESOURCE_PATH.length() + I18N_RESOURCE_PREFIX.length() + 1;
      String languageCode = resourcePath.substring(index, index + 2);

      // Crowdin file i18n_ach.properties isn't a language. Fix #1686.
      if (!languageCode.equals("ac")) {
        Locale locale = new Locale(languageCode);
        languages.put(locale.getDisplayLanguage(), languageCode);
      }
    }

    log.debug("languages: " + languages.toString());
    return languages;
  }

  public static Set<String> getLanguages() {
    return LANGUAGE_MAP.keySet();
  }

  public enum JVM_OPTION {
    MAX_MEM("-Xmx", ""),
    MIN_MEM("-Xms", ""),
    STACK_SIZE("-Xss", ""),
    ASSERTIONS("-ea", ""),
    DATA_DIR("-DMAPTOOL_DATADIR", CURRENT_DATA_DIR),
    LOCALE_LANGUAGE("-Duser..language", ""),
    LOCALE_COUNTRY("-Duser..country", ""),
    JAVA2D_D3D("-Dsun..java2d..d3d", "true"),
    JAVA2D_OPENGL_OPTION("-Dsun..java2d..opengl", "false"),
    MACOSX_EMBEDDED_OPTION("-Djavafx..macosx..embedded", "false");
    private final String parameterName, defaultValue;

    JVM_OPTION(String parameterName, String defaultValue) {
      this.parameterName = parameterName;
      this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
      return defaultValue;
    }
  }
}
