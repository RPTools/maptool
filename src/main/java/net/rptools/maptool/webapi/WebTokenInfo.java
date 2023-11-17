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
package net.rptools.maptool.webapi;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.*;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.zones.TokensAdded;
import net.rptools.maptool.model.zones.TokensChanged;
import net.rptools.maptool.model.zones.TokensRemoved;

public class WebTokenInfo {

  private static final WebTokenInfo instance = new WebTokenInfo();

  private WebTokenInfo() {
    // Add listener for new zones.
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  private void onTokensAdded(TokensAdded event) {
    for (final var token : event.tokens()) {
      tokenAdded(token);
    }
  }

  @Subscribe
  private void onTokensRemoved(TokensRemoved event) {
    for (final var token : event.tokens()) {
      tokenRemoved(token);
    }
  }

  @Subscribe
  private void onTokensChanged(TokensChanged event) {
    for (final var token : event.tokens()) {
      tokenChanged(token);
    }
  }

  private void tokenChanged(Token token) {
    JsonObject jobj = new JsonObject();
    JsonArray tokenArray = new JsonArray();
    tokenArray.add(token.getId().toString());
    jobj.add("tokensChanged", tokenArray);

    MTWebClientManager.getInstance().sendToAllSessions("token-update", jobj);
  }

  private void tokenAdded(Token token) {
    JsonObject jobj = new JsonObject();
    JsonArray tokenArray = new JsonArray();
    tokenArray.add(token.getId().toString());
    jobj.add("tokensAdded", tokenArray);

    MTWebClientManager.getInstance().sendToAllSessions("token-update", jobj);
  }

  private void tokenRemoved(Token token) {
    JsonObject jobj = new JsonObject();
    JsonArray tokenArray = new JsonArray();
    tokenArray.add(token.getId().toString());
    jobj.add("tokensRemoved", tokenArray);

    MTWebClientManager.getInstance().sendToAllSessions("token-update", jobj);
  }

  public static WebTokenInfo getInstance() {
    return instance;
  }

  public Token findTokenFromId(String tokenId) {
    System.out.println("DEBUG: tokenId = " + tokenId);
    final GUID id = new GUID(tokenId);

    final List<Token> tokenList = new ArrayList<>();

    List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
    for (ZoneRenderer zr : zrenderers) {
      tokenList.addAll(zr.getZone().getTokensFiltered(t -> t.getId().equals(id)));

      if (tokenList.size() > 0) {
        break;
      }
    }

    if (tokenList.size() > 0) {
      return tokenList.get(0);
    } else {
      return null;
    }
  }

  private Zone findZoneTokenIsOn(Token token) {
    List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
    for (ZoneRenderer zr : zrenderers) {
      if (zr.getZone().getAllTokens().contains(token)) {
        return zr.getZone();
      }
    }

    return null;
  }

  void sendTokenInfo(MTWebSocket mtws, String inResponseTo, JsonObject data) {

    if (data.has("propertyNames")) {
      sendTokenProperties(mtws, inResponseTo, data);
    } else {
      sendTokenRegisterdProperties(mtws, inResponseTo, data);
    }
  }

  String getTokenValue(Token token, String name) {
    if (":name".equalsIgnoreCase(name)) {
      return token.getName();
    } else if (":notes".equalsIgnoreCase(name)) {
      return token.getNotes();
    } else if (":label".equalsIgnoreCase(name)) {
      return token.getLabel();
    }

    return "";
  }

  void sendTokenProperties(MTWebSocket mtws, String inResponseTo, JsonObject data) {
    String tokenId = data.get("tokenId").getAsString();
    Token token = findTokenFromId(tokenId);

    if (token == null) {
      System.out.println("DEBUG: sendTokenInfo(): Unable to find token " + tokenId);
      return;
      // FIXME: log this error
    }

    JsonObject jobj = new JsonObject();
    jobj.addProperty("tokenId", tokenId);

    JsonArray properties = new JsonArray();
    JsonObject propertiesMap = new JsonObject();

    JsonArray propToFetch = data.get("propertyNames").getAsJsonArray();
    for (int i = 0; i < propToFetch.size(); i++) {
      String pname = propToFetch.get(i).getAsString();
      String val;
      if (pname.startsWith(":")) {
        val = getTokenValue(token, pname);
      } else {

        val = token.getProperty(pname) == null ? null : token.getProperty(pname).toString();
      }
      JsonObject jprop = new JsonObject();
      jprop.addProperty("name", pname);
      jprop.addProperty("value", val);
      properties.add(jprop);
      propertiesMap.addProperty(pname, val);
    }

    jobj.add("properties", properties);
    jobj.add("propertiesMap", propertiesMap);

    mtws.sendMessage("tokenProperties", inResponseTo, jobj);
  }

  void sendTokenRegisterdProperties(MTWebSocket mtws, String inResponseTo, JsonObject data) {
    String tokenId = data.get("tokenId").getAsString();
    Token token = findTokenFromId(tokenId);

    if (token == null) {
      System.out.println("DEBUG: sendTokenInfo(): Unable to find token " + tokenId);
      return;
      // FIXME: log this error
    }

    JsonObject jobj = new JsonObject();
    jobj.addProperty("tokenId", tokenId);
    jobj.addProperty("name", token.getName());
    if (token.getLabel() != null) {
      jobj.addProperty("label", token.getLabel());
    }
    if (token.getNotes() != null) {
      jobj.addProperty("notes", token.getNotes());
    }

    JsonObject jprop = new JsonObject();

    for (TokenProperty tp : MapTool.getCampaign().getTokenPropertyList(token.getPropertyType())) {
      JsonObject jp = new JsonObject();
      jp.addProperty("name", tp.getName());
      if (tp.getShortName() != null) {
        jp.addProperty("shortName", tp.getShortName());
      }
      if (tp.getDefaultValue() != null) {
        jp.addProperty("defaultValue", tp.getDefaultValue());
      }
      Object property = token.getProperty(tp.getName());
      if (property != null) {
        jp.add("value", JSONMacroFunctions.getInstance().asJsonElement(property));
      }
      jp.addProperty("showOnStatSheet", tp.isShowOnStatSheet());

      jprop.add(tp.getName(), jp);
    }

    jobj.add("properties", jprop);

    JsonArray jmacros = new JsonArray();

    for (MacroButtonProperties macro : token.getMacroList(false)) {
      JsonObject jmb = new JsonObject();
      jmb.addProperty("label", macro.getLabel());
      jmb.addProperty("tooltip", macro.getEvaluatedToolTip());
      jmb.addProperty("index", macro.getIndex());
      jmb.addProperty("fontColor", macro.getFontColorAsHtml());
      jmb.addProperty("displayGroup", macro.getGroupForDisplay());
      jmb.addProperty("group", macro.getGroup());
      jmb.addProperty("autoExecute", macro.getAutoExecute());
      jmb.addProperty("maxWidth", macro.getMaxWidth());
      jmb.addProperty("minWidth", macro.getMinWidth());
      jmb.addProperty("applyToTokens", macro.getApplyToTokens());

      jmacros.add(jmb);
    }

    jobj.add("macros", jmacros);

    mtws.sendMessage("tokenInfo", inResponseTo, jobj);
  }

  void processMacro(JsonObject data) {
    // FIXME: need to check parameters.
    // FIXME: need to check permissions.
    if ("callMacro".equalsIgnoreCase(data.get("command").getAsString())) {
      Token token = findTokenFromId(data.get("tokenId").getAsString());

      MacroButtonProperties macro = token.getMacro(data.get("macroIndex").getAsInt(), false);
      macro.executeMacro(token.getId());
    }
  }

  void processSetProperties(JsonObject data) {
    final String tokenId = data.get("tokenId").getAsString();
    final Token token = findTokenFromId(tokenId);
    final Zone zone = findZoneTokenIsOn(token);

    if (token == null) {
      System.out.println("DEBUG: sendTokenInfo(): Unable to find token " + tokenId);
      return;
      // FIXME: log this error
    }

    final JsonObject props = data.get("properties").getAsJsonObject();
    EventQueue.invokeLater(
        () -> {
          Set<String> pnames = props.keySet();
          for (String pname : pnames) {
            String val = props.get(pname).getAsString();
            token.setProperty(pname, val);
          }

          zone.putToken(token);
        });
  }
}
