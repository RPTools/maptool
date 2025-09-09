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
import net.rptools.maptool.client.script.javascript.*;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import org.graalvm.polyglot.HostAccess;

public class JSAPITokens implements MapToolJSAPIInterface {
  @Override
  public String serializeToString() {
    return "MapTool.tokens";
  }

  @HostAccess.Export
  public List<Object> getMapTokens() {
    return getMapTokens(MapTool.getFrame().getCurrentZoneRenderer());
  }

  @HostAccess.Export
  public List<Object> getMapTokens(String zoneName) {

    return getMapTokens(MapTool.getFrame().getZoneRenderer(zoneName));
  }

  public List<Object> getMapTokens(ZoneRenderer zr) {
    final List<Object> tokens = new ArrayList<>();
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    zr.getZone()
        .getAllTokens()
        .forEach(
            (t -> {
              if (trusted || t.isOwner(playerId)) {
                tokens.add(new JSAPIToken(t));
              }
            }));

    return tokens;
  }

  @HostAccess.Export
  public JSAPIToken getTokenByName(String tokenName) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    for (ZoneRenderer z : MapTool.getFrame().getZoneRenderers()) {
      if (trusted || z.getZone().isVisible()) {
        Token t = z.getZone().getTokenByName(tokenName);
        if (t != null && (trusted || t.isOwner(playerId))) {
          return new JSAPIToken(t);
        }
      }
    }
    return null;
  }

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
    JSAPIToken token = null;
    Token findToken =
        MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(new GUID(uuid));
    if (findToken != null) {
      token = new JSAPIToken(findToken);
      token.setMap(MapTool.getFrame().getCurrentZoneRenderer().getZone());
    } else {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        findToken = zr.getZone().resolveToken(uuid);
        if (findToken != null) {
          token = new JSAPIToken(findToken);
          token.setMap(zr.getZone());
          break;
        }
      }
    }
    if (token != null
        && (JSScriptEngine.inTrustedContext() || token.isOwner(MapTool.getPlayer().getName()))) {
      return token;
    }
    return null;
  }

  @HostAccess.Export
  public JSAPIToken getMapTokenByID(String uuid) {
    JSAPIToken token = null;
    Token findToken =
        MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(new GUID(uuid));
    if (findToken != null
        && (JSScriptEngine.inTrustedContext() || token.isOwner(MapTool.getPlayer().getName()))) {
      token = new JSAPIToken(findToken);
      token.setMap(MapTool.getFrame().getCurrentZoneRenderer().getZone());
    }
    return token;
  }
}
