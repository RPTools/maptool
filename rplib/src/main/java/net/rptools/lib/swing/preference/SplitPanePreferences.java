/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib.swing.preference;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JSplitPane;

public class SplitPanePreferences implements PropertyChangeListener {

	private JSplitPane splitPane;
	private Preferences prefs;

	private static final String PREF_LOCATION_KEY = "location";

	public SplitPanePreferences(String appName, String controlName, JSplitPane splitPane) {
		this.splitPane = splitPane;

		prefs = Preferences.userRoot().node(appName + "/control/" + controlName);

		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

		restorePreferences();
	}

	private void restorePreferences() {

		int position = prefs.getInt(PREF_LOCATION_KEY, -1);
		if (position == -1) {
			// First time usage, don't change the position of the split pane
			return;
		}

		splitPane.setDividerLocation(position);
	}

	private void savePreferences() {

		prefs.putInt(PREF_LOCATION_KEY, splitPane.getDividerLocation());
	}

	////
	// PROPERTY CHANGE LISTENER
	public void propertyChange(PropertyChangeEvent evt) {
		savePreferences();
	}
}
