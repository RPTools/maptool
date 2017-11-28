/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jdk.packager.services.UserJvmOptionsService;

/*
 * User Preferences are stored here:
 * 
 * Mac: ~/Library/Application Support/[app.preferences.id]/packager/jvmuserargs.cfg
 * 
 * Windows: C:\Users[username]\AppData\Roaming[app.preferences.id]\packager\jvmuserargs.cfg
 * 
 * Linux: ~/.local/[app.preferences.id]/packager/jvmuserargs.cfg
 */
public class UserJvmPrefs {
	private static final Logger log = LogManager.getLogger(UserJvmPrefs.class);

	private static final Pattern UNIT_PATTERN = Pattern.compile("([0-9]+)[g|G|m|M|k|K]"); // Valid JVM memory units

	public enum JVM_OPTION {
		MAX_MEM("-Xmx", ""), MIN_MEM("-Xms", ""), STACK_SIZE("-Xss", ""), ASSERTIONS("-ea", ""), DATADIR("-DMAPTOOL_DATADIR", ""), LOCALE_LANGUAGE("-Duser.language", ""), LOCALE_COUNTRY(
				"-Duser.country", ""), JAVA2D_D3D("-Dsun.java2d.d3d=", "false"), JAVA2D_OPENGL_OPTION("-Dsun.java2d.opengl=", "True"), MACOSX_EMBEDDED_OPTION("-Djavafx.macosx.embedded=", "true");

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
		setJvmOption(JVM_OPTION.DATADIR, "");
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
		log.info("get JVM Args :: " + arguments);

		UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
		Map<String, String> userOptions = ujo.getUserJVMOptions();

		// If user option is set, return it
		if (userOptions.containsKey(option.command))
			return userOptions.get(option.command);

		// Else, look for default value
		Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
		if (defaults.containsKey(option.command))
			return defaults.get(option.command);

		// No user option of default found..
		return "";
	}

	public static boolean hasJvmOption(JVM_OPTION option) {
		// For testing only
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		log.info("Has JVM Args :: " + arguments);

		UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
		Map<String, String> userOptions = ujo.getUserJVMOptions();

		// If user option is set, return it
		if (userOptions.containsKey(option.command))
			return true;

		// Else, look for default value
		Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
		if (defaults.containsKey(option.command))
			return true;

		// No user option of default found..
		return false;
	}

	public static void setJvmOption(JVM_OPTION option, String value) {
		UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
		Map<String, String> userOptions = ujo.getUserJVMOptions();

		if (value.isEmpty())
			userOptions.remove(option.command);
		else
			userOptions.put(option.command, value);

		ujo.setUserJVMOptions(userOptions);
	}

	public static void setJvmOption(JVM_OPTION option, boolean value) {
		UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
		Map<String, String> userOptions = ujo.getUserJVMOptions();

		if (value)
			userOptions.put(option.command, option.defaultValue);
		else
			userOptions.remove(option.command);

		ujo.setUserJVMOptions(userOptions);
	}

	public static boolean verifyJvmOptions(String s) {
		// Allow empty/null values as NO memory settings are technically allowed although not recommended...
		if (s.isEmpty())
			return true;

		Matcher m = UNIT_PATTERN.matcher(s);

		if (!m.find()) {
			// If we don't find a valid memory setting return false
			return false;
		} else {
			// Don't allow values less than 0
			if (Integer.parseInt(m.group(1)) <= 0)
				return false;
			else
				return true;
		}
	}
}
