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
package net.rptools.maptool.client.script_deprecated;

import org.mozilla.javascript.ClassShutter;

public class SecurityClassShutter implements ClassShutter {

  public boolean visibleToScripts(String cname) {
    // Everything in java.lang excluding the system class.
    if (cname.startsWith("java.lang")) {
      if (cname.equals("java.lang.System")) {
        return false;
      }
      return true;
    }

    // Everything in java.util
    if (cname.startsWith("java.util")) {
      return true;
    }

    // Everything in java.math
    if (cname.startsWith("java.math")) {
      return true;
    }

    // Maptool JavaScript macro api classes.
    if (cname.startsWith("net.rptools.maptool.client.script.api")) {
      return true;
    }

    // Allow the mozilla javascript classes
    if (cname.startsWith("org.mozilla.javascript")) {
      return true;
    }

    return false;
  }
}
