/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.util;

import net.rptools.maptool.client.AppConstants;
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
 * 
 * User Preferences are stored here: Mac ~/Library/Application Support/[app.preferences.id]/packager/jvmuserargs.cfg Windows
 * C:\Users[username]\AppData\Roaming[app.preferences.id]\packager\jvmuserargs.cfg Linux ~/.local/[app.preferences.id]/packager/jvmuserargs.cfg
 * 
 */
public class UserJvmPrefs {
	private static final Logger log = LogManager.getLogger(UserJvmPrefs.class);

	private static final Pattern UNIT_PATTERN = Pattern.compile("([0-9]+)[g|G|m|M|k|K]"); // Valid JVM memory units

	public enum JVM_OPTION {
		MAX_MEM("-Xmx", ""), MIN_MEM("-Xms", ""), STACK_SIZE("-Xss", ""), ASSERTIONS("-ea", ""), DATADIR("-DMAPTOOL_DATADIR", ""), LOCALE_LANGUAGE("-Duser.language", ""), LOCALE_COUNTRY(
				"-Duser.country", ""), JAVA2D_D3D("-Dsun.java2d.d3d", "=false"), JAVA2D_OPENGL_OPTION("-Dsun.java2d.opengl", "=True"), MACOSX_EMBEDDED_OPTION("-Djavafx.macosx.embedded", "=true");

		private final String command, defaultValue;

		JVM_OPTION(String command, String defaultValue) {
			this.command = command;
			this.defaultValue = defaultValue;
		}

		// public String getCommand() {
		// return command;
		// }
		//
		public String getDefaultValue() {
			return defaultValue;
		}
	}

	public static String getJvmOption(JVM_OPTION option) {
		// For testing only
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		log.info("TEST - JVM Args :: " + arguments);

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
		log.info("TEST - JVM Args :: " + arguments);

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

	// public static String getJvmOption(String key) {
	// // For testing only
	// RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	// List<String> arguments = runtimeMxBean.getInputArguments();
	// log.info("TEST - JVM Args :: " + arguments);
	//
	// UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
	// Map<String, String> userOptions = ujo.getUserJVMOptions();
	//
	// // If user option is set, return it
	// if (userOptions.containsKey(key))
	// return userOptions.get(key);
	//
	// // Else, look for default value
	// Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
	// if (defaults.containsKey(key))
	// return defaults.get(key);
	//
	// // No user option of default found..
	// return "";
	// }

	// public static void setJvmOption(String key, String value) {
	// UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
	// Map<String, String> userOptions = ujo.getUserJVMOptions();
	//
	// if (value.isEmpty())
	// userOptions.remove(key);
	// else
	// userOptions.put(key, value);
	//
	// ujo.setUserJVMOptions(userOptions);
	// }

	// public static Map<String, String> getJvmOptionsMap() {
	// // For testing only
	// RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	// List<String> arguments = runtimeMxBean.getInputArguments();
	// log.info("TEST - JVM Args :: " + arguments);
	//
	// UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
	// Map<String, String> userOptions = ujo.getUserJVMOptions();
	//
	// // print out all the options currently set
	// for (Map.Entry<String, String> entry : userOptions.entrySet()) {
	// log.debug("current defaults getUserJVMDefaults() key: " + entry.getKey() + ", value: " + entry.getValue());
	// }
	//
	// // if we haven't marked the first run, do so now
	// if (!userOptions.containsKey("-DfirstRunMs=")) {
	// userOptions.put("-DfirstRunMs=", Long.toString(System.currentTimeMillis()));
	// }
	//
	// // add the last run
	// userOptions.put("-DlastRunMs=", Long.toString(System.currentTimeMillis()));
	//
	// // Set default Xss for testing
	// // userOptions.putIfAbsent("-Xss", "6M");
	//
	// // save the changes
	// ujo.setUserJVMOptions(userOptions);
	//
	// // create a table row with Key, Current Value, and Default Value
	// DefaultTableModel model = new DefaultTableModel();
	// model.addColumn("Key");
	// model.addColumn("Effective");
	// model.addColumn("Default");
	//
	// Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
	// for (Map.Entry<String, String> entry : userOptions.entrySet()) {
	// // get the default, it may be null
	// String def = defaults.get(entry.getKey());
	//
	// model.addRow(new Object[] { entry.getKey(), entry.getValue(), def == null ? "<no default>" : def });
	//
	// log.info("getJvmOptionsTableModel() key: " + entry.getKey() + ", value: " + entry.getValue() + ", default: " + (def == null ? "<no default>" : def));
	// }
	//
	// return defaults;
	// }
}
