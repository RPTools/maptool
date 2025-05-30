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
package net.rptools.maptool.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.ThreadContext;

public class LaunchInstructions {
  private static final String USAGE =
      "<html><body width=\"400\">You are running MapTool with insufficient memory allocated (%dMB).<br><br>"
          + "You may experience odd behavior, especially when connecting to or hosting a server.<br><br>  "
          + "MapTool will launch anyway, but it is recommended that you increase the maximum memory allocated or don't set a limit.</body></html>";

  static {
    // This will inject additional data tags in log4j2 which will be picked up by Sentry.io
    System.setProperty("log4j2.isThreadContextMapInheritable", "true");
    // This sets up log4j to capture logging from Java logging manager
    if (System.getProperty("java.util.logging.manager") == null) {
      System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }
    ThreadContext.put("OS", System.getProperty("os.name"));
  }

  public static void main(String[] args) {
    try {
      // This is called just to make sure there's a valid data dir
      AppUtil.getAppHome();
      // Initialize the first logger with the correct log path
      AppUtil.initLogging();

      long mem = Runtime.getRuntime().maxMemory();
      String msg = String.format(USAGE, mem / (1024 * 1024));

      /*
       * Asking for 256MB via the -Xmx256M switch doesn't guarantee that the amount maxMemory() reports will be 256MB. The actual amount seems to vary from PC to PC. 200MB seems to be a safe value
       * for now. <Phergus>
       */
      if (mem < 200 * 1024 * 1024) {
        JOptionPane.showMessageDialog(new JFrame(), msg, "Usage", JOptionPane.INFORMATION_MESSAGE);
      }

      MapTool.main(args);

      AppUpdate.gitHubReleases();
    } catch (Exception e) {
      // Shows a proper error message if MapTool can't initialize. Fix #1678.
      JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
      System.exit(1);
    }
  }
}
