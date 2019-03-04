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

import java.io.File;

public class AppUtil {

  private static final String USER_HOME;

  private static String appName;

  static {
    USER_HOME = System.getProperty("user.home");
  }

  public static void init(String appName) {
    AppUtil.appName = appName;
  }

  public static File getUserHome() {
    checkInit();
    return USER_HOME != null ? new File(USER_HOME) : null;
  }

  public static File getAppHome() {
    checkInit();
    if (USER_HOME == null) {
      return null;
    }

    File home = new File(USER_HOME + "/." + appName);
    home.mkdirs();

    return home;
  }

  public static File getAppHome(String subdir) {
    checkInit();
    if (USER_HOME == null) {
      return null;
    }

    File home = new File(getAppHome().getPath() + "/" + subdir);
    home.mkdirs();

    return home;
  }

  private static void checkInit() {
    if (appName == null) {
      throw new IllegalStateException("Must call init() on AppUtil");
    }
  }

  public static String getAppName() {
    return appName;
  }
}
