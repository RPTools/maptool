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

@MapToolJSAPIDefinition(javaScriptVariableName = "MapTool")
public class JSAPIMapTool implements MapToolJSAPIInterface {
	private final JSAPIClientInfo clientInfo = new JSAPIClientInfo();

	private final JSAPIChat chat = new JSAPIChat();

	private final JSAPITokens tokens = new JSAPITokens();

	public JSAPIClientInfo getClientInfo() {
		return clientInfo;
	}

	public JSAPIChat getChat() {
		return chat;
	}

	public JSAPITokens getTokens() {
		return tokens;
	}
}
