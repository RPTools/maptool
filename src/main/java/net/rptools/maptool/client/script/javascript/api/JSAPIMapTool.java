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
import net.rptools.maptool.model.Token;
import org.graalvm.polyglot.*;

@MapToolJSAPIDefinition(javaScriptVariableName = "MapTool")
public class JSAPIMapTool implements MapToolJSAPIInterface {

  @HostAccess.Export public final JSAPIClientInfo clientInfo = new JSAPIClientInfo();

  @HostAccess.Export public final JSAPIChat chat = new JSAPIChat();

  @HostAccess.Export public final JSAPITokens tokens = new JSAPITokens();

  @HostAccess.Export
  public List<JSAPIToken> getSelectedTokens() {
    List<Token> tokens = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList();
    List<JSAPIToken> out_tokens = new ArrayList<JSAPIToken>();
    for (Token token : tokens) {
      out_tokens.add(new JSAPIToken(token));
    }
    return out_tokens;
  }

  @HostAccess.Export
  public JSAPIToken getSelected() {
    List<Token> tokens = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList();
    if (tokens.size() > 0) {
      return new JSAPIToken(tokens.get(0));
    }
    return null;
  }

  @HostAccess.Export
  public JSAPIToken getTokenByID(String uuid) {
    return new JSAPIToken(uuid);
  }
}
