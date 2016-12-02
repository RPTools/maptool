/*
 * This software Copyright by the RPTools.net development team, and licensed
 * under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License *
 * along with this source Code. If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license text at
 * <http://www.gnu.org/licenses/agpl.html>.
 */

package net.rptools.maptool.launcher;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

/*
 * This class inserts/updates the registry entry to allow HiDPI screens to scale
 * ie MapTool fonts/menu/icons are larger on high resolution screens. The
 * manifest file should already be baked into the JRE we supply. See:
 * https://bugs.openjdk.java.net/browse/JDK-6829055
 */
public class SetDPIAware {
	private final static boolean WINDOWS = (System.getProperty("os.name").toLowerCase().startsWith("windows"));

	private static final String REGISTRY_KEY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\SideBySide";
	private static final String REGISTRY_NAME = "PreferExternalManifest";
	public static final int REGISTRY_VALUE_ON = 1;
	public static final int REGISTRY_VALUE_OFF = 0;

	public static boolean getKeyValue() {
		if (!WINDOWS)
			return false;

		try {
			if (Advapi32Util.registryValueExists(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH, REGISTRY_NAME)) {
				if (Advapi32Util.registryGetIntValue(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH, REGISTRY_NAME) == REGISTRY_VALUE_ON)
					return true;
			}
		} catch (Win32Exception e) {
			System.out.println("Windows Registry read access failed.");
		}

		return false;
	}

	public static boolean setKeyValue(boolean isSelected) {
		if (!WINDOWS)
			return false;

		if (!Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH)) {
			Advapi32Util.registryCreateKey(HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion", "SideBySide");
		}

		if (isSelected)
			Advapi32Util.registrySetIntValue(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH, REGISTRY_NAME, REGISTRY_VALUE_ON);
		else
			Advapi32Util.registrySetIntValue(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH, REGISTRY_NAME, REGISTRY_VALUE_OFF);

		return getKeyValue();
	}

	public static boolean checkRegistryAccess() {
		if (!WINDOWS)
			return false;

		// If we can't create/delete keys it will throw an exception which in turns disables the checkbox
		try {
			Advapi32Util.registryCreateKey(HKEY_LOCAL_MACHINE, "Software", "MapToolTestJNA");
			Advapi32Util.registryDeleteKey(HKEY_LOCAL_MACHINE, "Software", "MapToolTestJNA");

			return true;
		} catch (Win32Exception e) {
			System.out.println("Windows Registry write access is denied.");
		}

		return false;
	}

	public static void main(String[] args) {
		System.out.println("Key Path Exists: " + Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH));
		System.out.println("Key Value Exists: " + Advapi32Util.registryValueExists(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH, REGISTRY_NAME));
		System.out.println("Value is: " + Advapi32Util.registryGetIntValue(HKEY_LOCAL_MACHINE, REGISTRY_KEY_PATH, REGISTRY_NAME));
		System.out.println("Registry Create Access: " + checkRegistryAccess());
	}
}