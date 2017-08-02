/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.launcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;

/**
 * Represents a single XML file used to configure logging for MapTool. It implements Comparable so that sorting is based on the description field (or filename, if no description is available).
 * 
 * @author frank
 */
public class LoggingConfig implements Comparable<LoggingConfig> {
	File fname;
	Map<String, String> properties = new HashMap<String, String>(2);
	JCheckBox chkbox;

	LoggingConfig(File f, JCheckBox c) {
		fname = f;
		chkbox = c;
	}

	public String getProperty(String key) {
		if (key == null) {
			return null;
		}
		return properties.get(key.toLowerCase());
	}

	public void addProperty(String key, String value) {
		if (key != null) {
			properties.put(key.toLowerCase(), value);
		}
	}

	@Override
	public int compareTo(LoggingConfig arg) {
		if (this.equals(arg)) {
			return 0;
		}
		final String desc = properties.get("desc"); //$NON-NLS-1$
		if (desc != null) {
			return desc.compareTo(arg.properties.get("desc")); //$NON-NLS-1$
		}
		return fname.getName().compareTo(arg.fname.getName());
	}
}