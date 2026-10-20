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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
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
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/*
 * User Preferences are stored next to jar when installed.
 * A copy will also be kept in users home directory to persist across installs/upgrades.
 */
public class UserJvmOptions {

  private static final Logger log = LogManager.getLogger(UserJvmOptions.class);

  private static final String CURRENT_DATA_DIR = AppUtil.getAppHome().getName();
  private static final Pattern UNIT_PATTERN = Pattern.compile("^([0-9]+)[gGmMkK]$");
  private static final List<Locale> KNOWN_LOCALES = getKnownLocales();

  private static FileBasedConfigurationBuilder<INIConfiguration> configurationBuilder;
  private static SubnodeConfiguration jvmNodeConfiguration;

  public static void resetJvmOptions() {
    log.info("Resetting all startup options to defaults!");

    setJvmOption(JVM_OPTION.MAX_MEM, "");
    setJvmOption(JVM_OPTION.MIN_MEM, "");
    setJvmOption(JVM_OPTION.STACK_SIZE, "");
    setJvmOption(JVM_OPTION.ASSERTIONS, "");
    setJvmOption(JVM_OPTION.DATA_DIR, "");
    setJvmOption(JVM_OPTION.JAVA2D_D3D, "");
    setJvmOption(JVM_OPTION.JAVA2D_OPENGL_OPTION, "");
    setJvmOption(JVM_OPTION.MACOSX_EMBEDDED_OPTION, "");
  }

  public static boolean loadAppCfg() {
    Configurations configurations = new Configurations();
    INIConfiguration iniConfiguration;

    File cfgFile = AppUtil.getAppCfgFile();

    if (cfgFile == null) {
      return false;
    }

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
                String newKey = key + ":" + jvmNodeConfiguration.getString(key).replace("=", "");
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
    return jvmNodeConfiguration.getString(option.parameterName, option.defaultValue);
  }

  public static boolean hasJvmOption(JVM_OPTION option) {
    return jvmNodeConfiguration.getBoolean(
        option.parameterName, Boolean.parseBoolean(option.defaultValue));
  }

  public static void setJvmOption(JVM_OPTION option, String value) {
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

  private static List<Locale> getKnownLocales() {
    var i18nResourcePattern = Pattern.compile("i18n(?:_([^/]*))?\\.properties$");
    var i18nPackage = "net.rptools.maptool.language";

    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .forPackage(i18nPackage)
                .setInputsFilter(
                    new FilterBuilder()
                        .includePackage(i18nPackage)
                        .excludePackage(i18nPackage + ".macro_descriptions"))
                .setScanners(Scanners.Resources));
    Set<String> resourcePathSet = reflections.getResources(i18nResourcePattern);
    log.debug("getResourceBundles() resourcePathSet: {}", resourcePathSet);

    List<Locale> locales = new ArrayList<>();
    for (String resourcePath : resourcePathSet) {
      var matcher = i18nResourcePattern.matcher(resourcePath);
      if (matcher.find()) {
        var languageTag = matcher.group(1);

        // Special case: the root i18n.properties is en-US despite not being called that.
        locales.add(
            languageTag == null
                ? Locale.of("en", "US")
                : Locale.forLanguageTag(languageTag.replace("_", "-")));
      }
    }

    log.debug("languages: {}", locales);
    return locales;
  }

  public static List<Locale> getLocales() {
    return Collections.unmodifiableList(KNOWN_LOCALES);
  }

  public enum JVM_OPTION {
    MAX_MEM("-Xmx", ""),
    MIN_MEM("-Xms", ""),
    STACK_SIZE("-Xss", ""),
    ASSERTIONS("-ea", ""),
    DATA_DIR("-DMAPTOOL_DATADIR", CURRENT_DATA_DIR),
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
