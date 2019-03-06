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
