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

import java.io.PrintStream;
import java.text.MessageFormat;

public class DebugStream extends PrintStream {
  private static final DebugStream INSTANCE = new DebugStream();
  private static boolean debugOn = true;

  public static void activate() {
    System.setOut(INSTANCE);
    debugOn = true;
  }

  public static void deactivate() {
    System.setOut(INSTANCE);
    debugOn = false;
  }

  private DebugStream() {
    super(System.out);
  }

  @Override
  public void println(Object x) {
    if (debugOn) {
      showLocation();
    }

    super.println(x);
  }

  @Override
  public void println(String x) {
    if (debugOn) {
      showLocation();
    }

    super.println(x);
  }

  private void showLocation() {
    StackTraceElement element = Thread.currentThread().getStackTrace()[3];
    super.print(
        MessageFormat.format(
            "({0}:{1, number,#}) : ", element.getFileName(), element.getLineNumber()));
  }
}
