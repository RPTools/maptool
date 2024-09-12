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

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.script.javascript.*;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
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
  private Zone map;

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
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setNotes, notes);
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
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setName, name);
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
  public String getRawProperty(String name) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Object val = this.token.getProperty(name);
      // Short-circuit to returning null so we don't return "null"
      if (val == null) {
        return null;
      }
      return "" + val;
    }
    return null;
  }

  @HostAccess.Export
  public String getProperty(String name) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Object val = this.token.getProperty(name);
      // Fall back to the property type's default value
      // since it's not useful to return nulls and require
      // javascript to have to handle defaults when getInfo isn't even bound,
      // especially when the value gets unset if it matches the default.
      // Evaluation is not performed automatically, use getEvaluatedProperty for that.
      if (val == null) {
        List<TokenProperty> propertyList =
            MapTool.getCampaign()
                .getCampaignProperties()
                .getTokenPropertyList(this.token.getPropertyType());
        if (propertyList != null) {
          for (TokenProperty property : propertyList) {
            if (name.equalsIgnoreCase(property.getName())) {
              val = property.getDefaultValue();
              break;
            }
          }
        }
      }
      if (val == null) {
        return null;
      }
      return "" + val;
    }
    return null;
  }

  @HostAccess.Export
  public String getEvaluatedProperty(String name) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      Object val = this.token.getEvaluatedProperty(name);
      return "" + val;
    }
    return "";
  }

  @HostAccess.Export
  public void setProperty(String name, Object value) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setProperty(name, value.toString());
      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setProperty, name, value.toString());
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

  @HostAccess.Export
  public boolean isOnCurrentMap() {
    Token findToken =
        MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(new GUID(this.getId()));
    return this.token == findToken;
  }

  public void setMap(Zone m) {
    this.map = m;
  }

  @HostAccess.Export
  public String getMapName() {
    return this.map.getDisplayName();
  }

  @HostAccess.Export
  public boolean getState(String stateName) {
    Object currentState = this.token.getState(stateName);
    return (currentState instanceof Boolean && (Boolean) currentState);
  }

  @HostAccess.Export
  public void setState(String stateName, boolean aValue) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setState(stateName, aValue);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, stateName, aValue);
    }
  }

  @HostAccess.Export
  public void setAllStates(boolean aValue) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setAllStates(aValue);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setAllStates, aValue);
    }
  }

  @HostAccess.Export
  public List<String> getActiveStates() {
    return this.token.getSetStates();
  }

  @HostAccess.Export
  public BigDecimal getBar(String barName) {
    Object currentBar = this.token.getState(barName);
    return currentBar == null ? BigDecimal.ZERO : (BigDecimal) currentBar;
  }

  @HostAccess.Export
  public void setBar(String barName, double aValue) {
    BigDecimal value = BigDecimal.valueOf(aValue);
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setState(barName, value);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, barName, value);
    }
  }

  @HostAccess.Export
  public boolean isBarVisible(String barName) {
    Object currentBar = this.token.getState(barName);
    return currentBar != null;
  }

  @HostAccess.Export
  public void setBarVisible(String barName, boolean show) {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setState(barName, show);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, barName, show);
    }
  }

  @HostAccess.Export
  public boolean isPC() {
    return this.token.getType() == Token.Type.PC;
  }

  @HostAccess.Export
  public void setPC() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setType(Token.Type.PC);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setPC);
    }
  }

  @HostAccess.Export
  public boolean isNPC() {
    return this.token.getType() == Token.Type.NPC;
  }

  @HostAccess.Export
  public void setNPC() {
    boolean trusted = JSScriptEngine.inTrustedContext();
    String playerId = MapTool.getPlayer().getName();
    if (trusted || token.isOwner(playerId)) {
      this.token.setType(Token.Type.NPC);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setNPC);
    }
  }

  @HostAccess.Export
  public String getType() {
    return this.token.getType().name();
  }
}
