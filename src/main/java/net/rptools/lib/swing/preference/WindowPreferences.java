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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFrame;

/**
 * Automatically keeps track of and restores frame size when opening/closing the application.
 *
 * <p>To use, simply add a line like this to you frame's constructor:
 *
 * <p>new WindowPreferences(appName, identifier, this);
 */
public class WindowPreferences extends WindowAdapter {
  private final Preferences prefs;

  private static final String KEY_X = "x";
  private static final String KEY_Y = "y";
  private static final String KEY_WIDTH = "width";
  private static final String KEY_HEIGHT = "height";
  private static final String KEY_MAXIMIZED = "maximized";

  private static int DEFAULT_X;
  private static int DEFAULT_Y;
  private static int DEFAULT_WIDTH;
  private static int DEFAULT_HEIGHT;
  private static boolean DEFAULT_MAXIMIZED = false;

  /**
   * Creates an object that holds the window boundary information after storing it into {@link
   * Preferences#userRoot()}. This object also registers a <code>WindowListener</code> on the passed
   * in <code>Window</code> object so that it is notified when the window is closed, allowing this
   * object to save the final window boundary into <code>Preferences</code> again.
   *
   * @param appName top-level name to use in the Preferences
   * @param controlName bottom level name to use
   * @param window the window whose boundary information is being recorded
   */
  public WindowPreferences(String appName, String controlName, Window window) {
    prefs = Preferences.userRoot().node(appName + "/control/" + controlName);

    DEFAULT_X = window.getLocation().x;
    DEFAULT_Y = window.getLocation().y;
    DEFAULT_WIDTH = window.getSize().width;
    DEFAULT_HEIGHT = window.getSize().height;
    if (window instanceof Frame) {
      DEFAULT_MAXIMIZED = ((Frame) window).getExtendedState() == Frame.MAXIMIZED_BOTH;
    }

    restorePreferences(window);
    window.addWindowListener(this);
  }

  /** Clear out window preferences from the user's system */
  public void clear() {
    try {
      prefs.clear();
    } catch (BackingStoreException bse) {
      // This error shouldn't matter, really,
      // since it is an asthetic action
      bse.printStackTrace();
    }
  }

  ////
  // Preferences

  protected int getX() {
    return prefs.getInt(KEY_X, DEFAULT_X);
  }

  protected void setX(int x) {
    prefs.putInt(KEY_X, x);
  }

  protected int getY() {
    return prefs.getInt(KEY_Y, DEFAULT_Y);
  }

  protected void setY(int y) {
    prefs.putInt(KEY_Y, y);
  }

  protected int getWidth() {
    return prefs.getInt(KEY_WIDTH, DEFAULT_WIDTH);
  }

  protected void setWidth(int width) {
    prefs.putInt(KEY_WIDTH, width);
  }

  protected int getHeight() {
    return prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT);
  }

  protected void setHeight(int height) {
    prefs.putInt(KEY_HEIGHT, height);
  }

  protected boolean getMaximized() {
    return prefs.getBoolean(KEY_MAXIMIZED, DEFAULT_MAXIMIZED);
  }

  protected void setMaximized(boolean maximized) {
    prefs.putBoolean(KEY_MAXIMIZED, maximized);
  }

  protected void storePreferences(Window window) {

    JFrame frame = null;
    if (window instanceof JFrame) {
      frame = (JFrame) window;
    }
    if (frame != null && frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
      // support full screen when storing preferences
      setX(0);
      setY(0);
      setWidth(frame.getWidth());
      setHeight(frame.getHeight());
      setMaximized(true);
    } else {
      setX(window.getLocation().x);
      setY(window.getLocation().y);

      setWidth(window.getSize().width);
      setHeight(window.getSize().height);
      setMaximized(false);
    }
  }

  protected void restorePreferences(Window window) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    int x = Math.max(Math.min(getX(), screenSize.width - getWidth()), 0);
    int y = Math.max(Math.min(getY(), screenSize.height - getHeight()), 0);

    window.setSize(getWidth(), getHeight());
    window.setLocation(x, y);

    if (getMaximized() && window instanceof JFrame) {
      ((JFrame) window).setExtendedState(Frame.MAXIMIZED_BOTH);
    }
  }

  ////
  // WINDOW ADAPTER
  @Override
  public final void windowClosing(WindowEvent e) {
    storePreferences((Window) e.getSource());
  }

  @Override
  public void windowClosed(WindowEvent e) {
    storePreferences((Window) e.getSource());
  }
}
