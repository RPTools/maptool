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
package net.rptools.lib;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

public final class OsDetection {

  /** Returns true if currently running on a Windows based operating system. */
  public static boolean WINDOWS =
      (System.getProperty("os.name").toLowerCase().startsWith("windows"));

  /** Returns true if currently running on a Mac OS X based operating system. */
  public static boolean MAC_OS_X =
      (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

  /** Returns true if currently running on Linux or other Unix/Unix like system. */
  public static boolean LINUX_OR_UNIX =
      (System.getProperty("os.name").indexOf("nix") >= 0
          || System.getProperty("os.name").indexOf("nux") >= 0
          || System.getProperty("os.name").indexOf("aix") >= 0
          || System.getProperty("os.name").indexOf("sunos") >= 0);

  public static int menuShortcut = getMenuShortcutKeyMask();

  private static int getMenuShortcutKeyMask() {
    int key = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    String prop = System.getProperty("os.name", "unknown");
    if ("darwin".equalsIgnoreCase(prop)) {
      if (key == InputEvent.CTRL_DOWN_MASK) {
        key = InputEvent.META_DOWN_MASK;
      }
    }
    return key;
  }

  public static KeyStroke withMenuShortcut(KeyStroke k) {
    int modifiers = k.getModifiers() | menuShortcut;
    if (k.getKeyCode() != KeyEvent.VK_UNDEFINED) {
      k = KeyStroke.getKeyStroke(k.getKeyCode(), modifiers);
    } else {
      k = KeyStroke.getKeyStroke(k.getKeyChar(), modifiers);
    }

    return k;
  }

  private OsDetection() {
    throw new RuntimeException("OsSupport is a static class");
  }
}
