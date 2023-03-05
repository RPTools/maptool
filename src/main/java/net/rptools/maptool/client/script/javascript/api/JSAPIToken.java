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

import java.util.Iterator;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.script.javascript.*;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.graalvm.polyglot.HostAccess;

public class JSAPIToken implements MapToolJSAPIInterface {
  @Override
  public String serializeToString() {
    return token.getId().toString();
  }

  private final Token token;
  private Set<String> names;
  private Iterator<String> names_iter;

  public JSAPIToken(Token token) {
    this.token = token;
  }

  public JSAPIToken(String tid) {
    this(MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(new GUID(tid)));
  }

  @HostAccess.Export
  public String getNotes() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getNotes();
    }
    return "";
  }

  @HostAccess.Export
  public void setNotes(String notes) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      token.setNotes(notes);
    }
  }

  @HostAccess.Export
  public String getName() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getName();
    }
    return "";
  }

  @HostAccess.Export
  public void setName(String name) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      token.setName(name);
    }
  }

  @HostAccess.Export
  public boolean hasSight() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return token.getHasSight();
    }
    return false;
  }

  @HostAccess.Export
  public void setSight(boolean sight) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      token.setHasSight(sight);
    }
  }

  @HostAccess.Export
  public String toString() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return "Token(id=" + token.getId() + ")";
    }
    return "Token(id=accessdenied)";
  }

  @HostAccess.Export
  public String getId() {
    return "" + token.getId();
  }

  @HostAccess.Export
  public String getProperty(String name) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return "" + this.token.getProperty(name);
    }
    return "";
  }

  @HostAccess.Export
  public void setProperty(String name, Object value) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setProperty(name, value.toString());
    }
  }

  @HostAccess.Export
  public int getX() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return this.token.getX();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getX"));
  }

  @HostAccess.Export
  public int getY() throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      return this.token.getY();
    }
    throw new ParserException(I18N.getText("macro.function.initiative.gmOrOwner", "getY"));
  }

  @HostAccess.Export
  public void setX(int x) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setX(x);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setXY, x, token.getY());
    }
  }

  @HostAccess.Export
  public void setY(int y) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setY(y);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setXY, token.getX(), y);
    }
  }

  @HostAccess.Export
  public boolean isOwner(String playerID) {
    return this.token.isOwner(playerID);
  }
}
