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
package net.rptools.maptool.client.script.javascript.api;

import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import org.graalvm.polyglot.HostAccess;

public class JSAPIChat {
  @HostAccess.Export
  public void broadcast(String message) {
    MapTool.addGlobalMessage(message);
  }

  @HostAccess.Export
  public void broadcastTo(List<String> who, String message) {
    MapTool.addGlobalMessage(message, who);
  }

  @HostAccess.Export
  public void broadcastToGM(String message) {
    List<String> who = new ArrayList<>();
    who.add("gm");
    MapTool.addGlobalMessage(message, who);
  }
}
