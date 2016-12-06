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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class TreePreferences implements TreeSelectionListener {
	private final JTree tree;
	private final Preferences prefs;
	private static final String PREF_PATH_KEY = "path";
	private static final String PATH_DELIMITER = "|";

	public TreePreferences(String appName, String controlName, JTree tree) {
		this.tree = tree;

		prefs = Preferences.userRoot().node(appName + "/control/" + controlName);
		tree.getSelectionModel().addTreeSelectionListener(this);

		// Wait until the UI has been built to do this
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				restorePreferences();
			}
		});
	}

	private void restorePreferences() {
		String path = prefs.get(PREF_PATH_KEY, "");
		if (path == null || path.length() == 0) {
			// First time usage
			return;
		}
		StringTokenizer strtok = new StringTokenizer(path, PATH_DELIMITER);
		List<Object> pathList = new ArrayList<Object>();
		pathList.add(tree.getModel().getRoot());
		Object currentStep = tree.getModel().getRoot();

		while (strtok.hasMoreElements()) {
			boolean found = false;
			String nextStep = strtok.nextToken();
			for (int i = 0; i < tree.getModel().getChildCount(currentStep); i++) {
				Object element = tree.getModel().getChild(currentStep, i);
				if (element != null && element.toString().equals(nextStep)) {
					pathList.add(element);
					currentStep = element;
					found = true;
					break;
				}
			}
			if (!found) {
				break;
			}
		}
		if (pathList.size() > 0) {
			TreePath treePath = new TreePath(pathList.toArray());
			tree.expandPath(treePath);
			tree.setSelectionPath(treePath);
		}
	}

	private void savePreferences() {
		StringBuilder builder = new StringBuilder();
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			return;
		}
		for (int i = 0; i < path.getPathCount(); i++) {
			Object o = path.getPathComponent(i);

			builder.append(o).append(PATH_DELIMITER);
		}
		if (builder.length() > 0) {
			// Kill the last delimiter
			builder.setLength(builder.length() - 1);
		}
		String composedPath = builder.toString();
		if (composedPath.startsWith(PATH_DELIMITER)) {
			composedPath = composedPath.substring(1);
		}
		prefs.put(PREF_PATH_KEY, composedPath);
	}

	////
	// TREE SELECTION LISTENER
	public void valueChanged(TreeSelectionEvent e) {
		savePreferences();
	}
}
