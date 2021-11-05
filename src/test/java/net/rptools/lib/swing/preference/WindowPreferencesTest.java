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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.Test;

class WindowPreferencesTest {

  private static int TEST_WIDTH = 100;
  private static int TEST_HEIGHT = 100;
  private static String TEST_APP_NAME = "testAppName";
  private static String TEST_CONTROL_NAME = "testControlName";

  private Window createSizedWindow(int width, int height) {
    Window w = new Window(null);
    w.setSize(width, height);
    return w;
  }

  private Window createAnonymousWindow() {
    return createSizedWindow(TEST_WIDTH, TEST_HEIGHT);
  }

  private WindowEvent createAnonymousEvent(Window w) {
    return new WindowEvent(w, 0);
  }

  private WindowPreferences createSamplePrefs(Window window) {
    cleanUpPrefs();
    return new WindowPreferences(TEST_APP_NAME, TEST_CONTROL_NAME, window);
  }

  private void cleanUpPrefs() {
    Preferences p = Preferences.userRoot().node(TEST_APP_NAME + "/control/" + TEST_CONTROL_NAME);
    try {
      p.removeNode();
    } catch (BackingStoreException e) {
      // we ignore it
    }
  }

  @Test
  void create() {
    Window w = createSizedWindow(TEST_WIDTH, TEST_HEIGHT);
    WindowPreferences prefs = createSamplePrefs(w);

    assertEquals(TEST_WIDTH, prefs.getWidth());
    assertEquals(TEST_HEIGHT, prefs.getHeight());
  }

  @Test
  void windowClosed() {
    int NEW_WIDTH = 300;
    int NEW_HEIGHT = 200;
    Window w = createAnonymousWindow();
    WindowPreferences prefs = createSamplePrefs(w);
    w.setSize(NEW_WIDTH, NEW_HEIGHT);
    WindowEvent ev = createAnonymousEvent(w);

    prefs.windowClosed(ev);

    assertEquals(NEW_WIDTH, prefs.getWidth());
    assertEquals(NEW_HEIGHT, prefs.getHeight());
  }
}
