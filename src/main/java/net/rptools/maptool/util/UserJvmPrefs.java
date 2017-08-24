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

import javax.swing.table.DefaultTableModel;

import jdk.packager.services.UserJvmOptionsService;

public class UserJvmPrefs {
	public static DefaultTableModel getJvmOptionsTableModel() {
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		System.out.println("JVM Args :: " + arguments);

		UserJvmOptionsService ujo = UserJvmOptionsService.getUserJVMDefaults();
		Map<String, String> userOptions = ujo.getUserJVMOptions();

		// print out all the options currently set
		for (Map.Entry<String, String> entry : userOptions.entrySet()) {
			System.out.println("getUserJVMDefaults key:" + entry.getKey() + " value:" + entry.getValue());
		}

		// if we haven't marked the first run, do so now
		if (!userOptions.containsKey("-DfirstRunMs=")) {
			userOptions.put("-DfirstRunMs=", Long.toString(System.currentTimeMillis()));
		}

		// add the last run
		userOptions.put("-DlastRunMs=", Long.toString(System.currentTimeMillis()));

		// save the changes
		ujo.setUserJVMOptions(userOptions);

		// create a table row with Key, Current Value, and Default Value
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Key");
		model.addColumn("Effective");
		model.addColumn("Default");

		Map<String, String> defaults = ujo.getUserJVMOptionDefaults();
		for (Map.Entry<String, String> entry : userOptions.entrySet()) {
			// get the default, it may be null
			String def = defaults.get(entry.getKey());

			model.addRow(new Object[] { entry.getKey(), entry.getValue(), def == null ? "<no default>" : def });
			System.out.println("getUserJVMOptionDefaults key:" + entry.getKey() + " value:" + entry.getValue());
		}

		return model;
	}
}
