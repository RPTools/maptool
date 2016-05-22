/*
 * This software Copyright by the RPTools.net development team, and licensed
 * under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this source Code. If not, see <http://www.gnu.org/licenses/>
 */
package net.rptools.maptool.client.script.javascript.api;

import net.rptools.maptool.client.MapTool;

import java.util.ArrayList;
import java.util.List;

public class JSAPIChat {
	public void broadcast(String message) {
		MapTool.addGlobalMessage(message);
	}

	public void broadcastTo(List<String> who, String message) {
		MapTool.addGlobalMessage(message, who);
	}

	public void broadcastToGM(String message) {
		List<String> who = new ArrayList<>();
		who.add("gm");
		MapTool.addGlobalMessage(message, who);
	}
}
