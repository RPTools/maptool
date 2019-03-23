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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.packager.services.UserJvmOptionsService;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/*
 * User Preferences are stored here:
 *
 * Mac: ~/Library/Application Support/[app.preferences.id]/packager/jvmuserargs.cfg
 *
 * Windows: C:\Users[username]\AppData\Roaming\[app.preferences.id]\packager\jvmuserargs.cfg
 *
 * Linux: ~/.local/[app.preferences.id]/packager/jvmuserargs.cfg
 */
public class UserJvmPrefs {
  private static final Logger log = LogManager.getLogger(UserJvmPrefs.class);

  private static final Pattern UNIT_PATTERN =
      Pattern.compile("([0-9]+)[g|G|m|M|k|K]"); // Valid JVM memory units

  private static final Map<String, String> LANGUAGE_MAP = getResourceBundles();

  private static final String I18N_RESOURCE_PREFIX = "i18n_";
  private static final String I18N_RESOURCE_PATH = "net/rptools/maptool/language";

  private static final String CURRENT_DATA_DIR = AppUtil.getAppHome().getName();

  public enum JVM_OPTION {
    // @formatter:off
    MAX_MEM("-Xmx", ""),
    MIN_MEM("-Xms", ""),
    STACK_SIZE("-Xss", ""),
    ASSERTIONS("-ea", ""),
    DATA_DIR("-DMAPTOOL_DATADIR=", CURRENT_DATA_DIR),
    LOCALE_LANGUAGE("-Duser.language=", ""),
    LOCALE_COUNTRY("-Duser.country=", ""),
    JAVA2D_D3D("-Dsun.java2d.d3d=", "false"),
    JAVA2D_OPENGL_OPTION("-Dsun.java2d.opengl=", "True"),
    MACOSX_EMBEDDED_OPTION("-Djavafx.macosx.embedded=", "true");
    // @formatter:on

    private final String command, defaultValue;

    JVM_OPTION(String command, String defaultValue) {
      this.command = command;
      this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
      return defaultValue;
    }
  }

  public static void resetJvmOptions() {
    log.info("Reseting all startup options to defaults!");

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

  public static String getJvmOption(JVM_OPTION option) {
    // For testing only
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = runtimeMxBean.getInputArguments();
    log.debug("get JVM Args :: " + arguments);

    UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
    Map<String, String> userOptions = ujo.getUserJVMOptions();

    // If user option is set, return it
    if (userOptions.containsKey(option.command)) {
      if (option.equals(JVM_OPTION.LOCALE_LANGUAGE)) {
        // Translate 2 letter language code to display language
        String languageCode = userOptions.get(option.command);
        for (Entry<String, String> entry : LANGUAGE_MAP.entrySet()) {
          if (Objects.equals(languageCode, entry.getValue())) {
            return entry.getKey();
          }
        }

        // Language Code not found, default option to English
        return "English";
      }

      return userOptions.get(option.command);
    }

    // Else, look for default value
    Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
    if (defaults.containsKey(option.command)) return defaults.get(option.command);

    // No user option of default found..
    return "";
  }

  public static boolean hasJvmOption(JVM_OPTION option) {
    // For testing only
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = runtimeMxBean.getInputArguments();
    log.debug("Has JVM Args :: " + arguments);

    UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
    Map<String, String> userOptions = ujo.getUserJVMOptions();

    // If user option is set, return it
    if (userOptions.containsKey(option.command)) return true;

    // Else, look for default value
    Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
    if (defaults.containsKey(option.command)) return true;

    // No user option of default found..
    return false;
  }

  public static void setJvmOption(JVM_OPTION option, String value) {
    UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
    Map<String, String> userOptions = ujo.getUserJVMOptions();

    if (value.isEmpty()) {
      userOptions.remove(option.command);
    } else {
      // Translate display language to 2 letter language code
      if (option.equals(JVM_OPTION.LOCALE_LANGUAGE)) value = LANGUAGE_MAP.get(value);

      userOptions.put(option.command, value);
    }

    ujo.setUserJVMOptions(userOptions);
  }

  public static void setJvmOption(JVM_OPTION option, boolean value) {
    UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
    Map<String, String> userOptions = ujo.getUserJVMOptions();

    if (value) userOptions.put(option.command, option.defaultValue);
    else userOptions.remove(option.command);

    ujo.setUserJVMOptions(userOptions);
  }

  public static boolean verifyJvmOptions(String s) {
    // Allow empty/null values as NO memory settings are technically allowed although not
    // recommended...
    if (s.isEmpty()) return true;

    Matcher m = UNIT_PATTERN.matcher(s);

    if (!m.find()) {
      // If we don't find a valid memory setting return false
      MapTool.showError(I18N.getText("msg.error.jvm.options", s));
      return false;
    } else {
      // Don't allow values less than 0
      if (Integer.parseInt(m.group(1)) <= 0) return false;
      else return true;
    }
  }

  private static Map<String, String> getResourceBundles() {
    Map<String, String> languages = new HashMap<String, String>();

    Locale loc = new Locale("en");
    languages.put(loc.getDisplayLanguage(), "");

    Reflections reflections = new Reflections(I18N_RESOURCE_PATH, new ResourcesScanner());
    Set<String> resourcePathSet =
        reflections.getResources(Pattern.compile(I18N_RESOURCE_PREFIX + ".*\\.properties"));
    log.info("resourcePathSet: " + resourcePathSet.toString());

    for (String resourcePath : resourcePathSet) {
      int index = I18N_RESOURCE_PATH.length() + I18N_RESOURCE_PREFIX.length() + 1;
      String languageCode = resourcePath.substring(index, index + 2);
      Locale locale = new Locale(languageCode);
      languages.put(locale.getDisplayLanguage(), languageCode);
    }

    log.debug("languages: " + languages.toString());
    return languages;
  }

  public static Set<Entry<String, String>> getLanguages() {
    return LANGUAGE_MAP.entrySet();
  }
}
