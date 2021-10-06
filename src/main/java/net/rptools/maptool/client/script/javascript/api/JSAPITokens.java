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
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.graalvm.polyglot.HostAccess;

public class JSAPITokens implements MapToolJSAPIInterface {
  @Override
  public String serializeToString() {
    return "MapTool.tokens";
  }

  @HostAccess.Export
  public List<Object> getAllTokens() {
    final List<Object> tokens = new ArrayList<>();
    boolean trusted = JSScriptEngine.inTrustedContext();
    if (!trusted) {
      MapTool.getFrame()
          .getZoneRenderers()
          .forEach(
              (z -> {
                if (z.getZone().isVisible()) {
                  z.getVisibleTokens()
                      .forEach(
                          (t -> {
                            tokens.add(new JSAPIToken(t));
                          }));
                }
              }));

    } else {
      MapTool.getFrame()
          .getZoneRenderers()
          .forEach(
              (z -> {
                z.getZone()
                    .getTokens()
                    .forEach(
                        (t -> {
                          tokens.add(new JSAPIToken(t));
                        }));
              }));
    }
    return tokens;
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
        .getTokens()
        .forEach(
            (t -> {
              if (trusted || t.isOwner(playerId)) {
                tokens.add(new JSAPIToken(t));
              }
            }));

    return tokens;
  }

  @HostAccess.Export
  public List<Object> getOwnedTokens() throws ParserException {
    String playerId = MapTool.getPlayer().getName();
    return getOwnedTokens(playerId);
  }

  @HostAccess.Export
  public List<Object> getOwnedTokens(String playerId) throws ParserException {
    final List<Object> tokens = new ArrayList<>();
    boolean trusted = JSScriptEngine.inTrustedContext();
    if (!trusted && !(MapTool.getPlayer().getName().equals(playerId))) {
      throw new ParserException(
          I18N.getText(
              "macro.function.initiative.gmOrOwner",
              "getOwnedTokens with a different player name"));
    }
    MapTool.getFrame()
        .getZoneRenderers()
        .forEach(
            (z -> {
              if (trusted || z.getZone().isVisible()) {
                z.getZone()
                    .getTokens()
                    .forEach(
                        (t -> {
                          if (t.isOwner(playerId)) {
                            tokens.add(new JSAPIToken(t));
                          }
                        }));
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
  public JSAPIToken getLibToken(String tokenName) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    if (!trusted) {
      throw new ParserException(I18N.getText("macro.function.general.onlyGM", "getLibToken"));
    }
    for (ZoneRenderer z : MapTool.getFrame().getZoneRenderers()) {
      Token t = z.getZone().getTokenByName(tokenName);
      if (t != null) {
        if (t.hasOwners()) {
          continue;
        }
        return new JSAPIToken(t);
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
    JSAPIToken token = new JSAPIToken(uuid);
    if (JSScriptEngine.inTrustedContext() || token.isOwner(MapTool.getPlayer().getName())) {
      return token;
    }
    return null;
  }
}
