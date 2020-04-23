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
package net.rptools.maptool.client.ui;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitHandler;
import java.io.IOException;
import net.rptools.lib.image.ImageUtil;
import org.apache.log4j.Logger;

/**
 * Apple's new platform API has deprecated the <code>ApplicationListener</code> class. The preferred
 * approach going forward is to use the various set*() methods of <code>Application</code> instead.
 * They are:
 *
 * <ul>
 *   <li>requestToggleFullScreen({@link java.awt.Window})
 *   <li>setAboutHandler({@link java.awt.desktop.AboutHandler})
 *   <li>setDefaultMenuBar({@link javax.swing.JMenuBar})
 *   <li>setDockIconBadge({@link java.lang.String})
 *   <li>setDockIconImage({@link java.awt.Image})
 *   <li>setDockIconProgress(int)
 *   <li>setDockMenu({@link java.awt.PopupMenu})
 *   <li>setOpenFileHandler({@link java.awt.desktop.OpenFilesHandler})
 *   <li>setOpenURIHandler({@link java.awt.desktop.OpenURIHandler})
 *   <li>setPreferencesHandler({@link java.awt.desktop.PreferencesHandler})
 *   <li>setPrintFileHandler({@link java.awt.desktop.PrintFilesHandler})
 *   <li>setQuitHandler({@link java.awt.desktop.QuitHandler})
 *   <li>setQuitStrategy({@link java.awt.desktop.QuitStrategy})
 *   <li>addAppEventListener({@link java.awt.desktop.SystemEventListener})
 *   <li>removeAppEventListener({@link java.awt.desktop.SystemEventListener})
 * </ul>
 */
public class OSXAdapter {

  private static final Logger log = Logger.getLogger(OSXAdapter.class);
  private static Desktop dt = Desktop.getDesktop();
  private static final String MAPTOOL_DOCK_ICON_PNG =
      "net/rptools/maptool/client/image/maptool-dock-icon.png";

  /**
   * Sets the quit handler for the main menu on macOS so that it invokes the proper method of the
   * {@link java.awt.desktop.QuitHandler} object.
   *
   * @param h the object to delegate the event to
   */
  public static void setQuitHandler(QuitHandler h) {
    dt.setQuitHandler(h);
    dt.disableSuddenTermination();
  }

  /**
   * Sets the quit handler for the main menu on macOS so that it invokes the proper method of the
   * {@link java.awt.desktop.AboutHandler} object.
   *
   * @param h the object to delegate the event to
   */
  public static void setAboutHandler(AboutHandler h) {
    dt.setAboutHandler(h);
  }

  /**
   * Sets the quit handler for the main menu on macOS so that it invokes the proper method of the
   * {@link java.awt.desktop.PreferencesHandler} object.
   *
   * @param h the object to delegate the event to
   */
  public static void setPreferencesHandler(PreferencesHandler h) {
    dt.setPreferencesHandler(h);
  }

  /**
   * Sets the quit handler for the main menu on macOS so that it invokes the proper method of the
   * {@link java.awt.desktop.OpenFilesHandler} object.
   *
   * @param h the object to delegate the event to
   */
  public static void setFileHandler(OpenFilesHandler h) {
    dt.setOpenFileHandler(h);
  }

  /**
   * If we're running on macOS, we call this method to download and install the MapTool logo from
   * the main web site. We cache this image so that it appears correctly if the application is later
   * executed in "offline" mode, so to speak.
   */
  public static void macOSXicon() {
    // If we're running on OSX, add the dock icon image
    // -- and change our application name to just "MapTool" (not currently)
    // We wait until after we call initialize() so that the asset and image managers
    // are configured.

    Image img = null;
    try {
      img = ImageUtil.getImage(MAPTOOL_DOCK_ICON_PNG);
    } catch (IOException e) {
      log.warn("Cannot read '" + MAPTOOL_DOCK_ICON_PNG + "'; no dock icon", e);
    }

    if (Taskbar.isTaskbarSupported()) {
      try {
        Taskbar tb = Taskbar.getTaskbar();
        if (img != null) {
          tb.setIconImage(img);
        }
        // We could also modify the popup menu that displays when the user right-clicks the dock
        // image...
        // And we could use the tb.setProgressValue() call to represent campaign loading/saving...

        // if (MapToolUtil.isDebugEnabled()) {
        // String vers = MapTool.getVersion();
        // vers = vers.substring(vers.length() - 2);
        // vers = vers.replaceAll("[^0-9]", "0"); // Convert all non-digits to zeroes
        // tb.setIconBadge(vers);
        // }
      } catch (Exception e) {
        log.info("Error accessing the Taskbar API?!", e);
      }
    }
  }
}
