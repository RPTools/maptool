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

import java.awt.*;
import java.util.*;
import java.util.List;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WebTokenInfo {

  private static final WebTokenInfo instance = new WebTokenInfo();

  private final AppEventListener appEventListener;
  private final Map<Zone, ModelChangeListener> modelChangeListeners = new WeakHashMap<>();

  private WebTokenInfo() {
    // Add listener for new zones.
    appEventListener =
        new AppEventListener() {
          @Override
          public void handleAppEvent(AppEvent appEvent) {
            if (appEvent.getId().equals(MapTool.ZoneEvent.Added)) {
              addTokenChangeListeners();
            }
          }
        };

    addTokenChangeListeners();
  }

  // TODO: This could be a single listener for all zones
  private void addTokenChangeListeners() {
    for (Zone zone : MapTool.getCampaign().getZones()) {
      if (modelChangeListeners.containsKey(zone) == false) {
        modelChangeListeners.put(
            zone,
            new ModelChangeListener() {
              @Override
              public void modelChanged(ModelChangeEvent event) {
                System.out.println("DEBUG: Event " + event.eventType);
                if (event.eventType == Zone.Event.TOKEN_CHANGED) {
                  tokenChanged((Token) event.getArg());
                } else if (event.eventType == Zone.Event.TOKEN_ADDED) {
                  tokenAdded((Token) event.getArg());
                } else if (event.eventType == Zone.Event.TOKEN_REMOVED) {
                  tokenRemoved((Token) event.getArg());
                }
              }
            });
        zone.addModelChangeListener(modelChangeListeners.get(zone));
      }
    }
  }

  private void tokenChanged(Token token) {
    JSONObject jobj = new JSONObject();
    JSONArray tokenArray = new JSONArray();
    tokenArray.add(token.getId().toString());
    jobj.put("tokensChanged", tokenArray);

    MTWebClientManager.getInstance().sendToAllSessions("token-update", jobj);
  }

  private void tokenAdded(Token token) {
    JSONObject jobj = new JSONObject();
    JSONArray tokenArray = new JSONArray();
    tokenArray.add(token.getId().toString());
    jobj.put("tokensAdded", tokenArray);

    MTWebClientManager.getInstance().sendToAllSessions("token-update", jobj);
  }

  private void tokenRemoved(Token token) {
    JSONObject jobj = new JSONObject();
    JSONArray tokenArray = new JSONArray();
    tokenArray.add(token.getId().toString());
    jobj.put("tokensRemoved", tokenArray);

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
      tokenList.addAll(
          zr.getZone()
              .getTokensFiltered(
                  new Zone.Filter() {
                    public boolean matchToken(Token t) {
                      return t.getId().equals(id);
                    }
                  }));

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
      if (zr.getZone().getTokens().contains(token)) {
        return zr.getZone();
      }
    }

    return null;
  }

  void sendTokenInfo(MTWebSocket mtws, String inResponseTo, JSONObject data) {

    if (data.containsKey("propertyNames")) {
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

  void sendTokenProperties(MTWebSocket mtws, String inResponseTo, JSONObject data) {
    String tokenId = data.getString("tokenId");
    Token token = findTokenFromId(tokenId);

    if (token == null) {
      System.out.println("DEBUG: sendTokenInfo(): Unable to find token " + tokenId);
      return;
      // FIXME: log this error
    }

    JSONObject jobj = new JSONObject();
    jobj.put("tokenId", tokenId);

    JSONArray properties = new JSONArray();
    JSONObject propertiesMap = new JSONObject();

    JSONArray propToFetch = data.getJSONArray("propertyNames");
    for (int i = 0; i < propToFetch.size(); i++) {
      String pname = propToFetch.getString(i);
      String val;
      if (pname.startsWith(":")) {
        val = getTokenValue(token, pname);
      } else {

        val = token.getProperty(pname) == null ? null : token.getProperty(pname).toString();
      }
      JSONObject jprop = new JSONObject();
      jprop.put("name", pname);
      jprop.put("value", val);
      properties.add(jprop);
      propertiesMap.put(pname, val);
    }

    jobj.put("properties", properties);
    jobj.put("propertiesMap", propertiesMap);

    mtws.sendMessage("tokenProperties", inResponseTo, jobj);
  }

  void sendTokenRegisterdProperties(MTWebSocket mtws, String inResponseTo, JSONObject data) {
    String tokenId = data.getString("tokenId");
    Token token = findTokenFromId(tokenId);

    if (token == null) {
      System.out.println("DEBUG: sendTokenInfo(): Unable to find token " + tokenId);
      return;
      // FIXME: log this error
    }

    JSONObject jobj = new JSONObject();
    jobj.put("tokenId", tokenId);
    jobj.put("name", token.getName());
    jobj.put("label", token.getLabel());
    jobj.put("notes", token.getNotes());

    JSONObject jprop = new JSONObject();

    for (TokenProperty tp : MapTool.getCampaign().getTokenPropertyList(token.getPropertyType())) {
      JSONObject jp = new JSONObject();
      jp.put("name", tp.getName());
      if (tp.getShortName() != null) {
        jp.put("shortName", tp.getShortName());
      }
      if (tp.getDefaultValue() != null) {
        jp.put("defaultValue", tp.getDefaultValue());
      }
      jp.put("value", token.getProperty(tp.getName()));
      jp.put("showOnStatSheet", tp.isShowOnStatSheet());

      jprop.put(tp.getName(), jp);
    }

    jobj.put("properties", jprop);

    JSONArray jmacros = new JSONArray();

    for (MacroButtonProperties macro : token.getMacroList(false)) {
      JSONObject jmb = new JSONObject();
      jmb.put("label", macro.getLabel());
      jmb.put("tooltip", macro.getEvaluatedToolTip());
      jmb.put("index", macro.getIndex());
      jmb.put("fontColor", macro.getFontColorAsHtml());
      jmb.put("displayGroup", macro.getGroupForDisplay());
      jmb.put("group", macro.getGroup());
      jmb.put("index", macro.getIndex());
      jmb.put("autoExecute", macro.getAutoExecute());
      jmb.put("maxWidth", macro.getMaxWidth());
      jmb.put("minWidth", macro.getMinWidth());
      jmb.put("applyToTokens", macro.getApplyToTokens());

      jmacros.add(jmb);
    }

    jobj.put("macros", jmacros);

    mtws.sendMessage("tokenInfo", inResponseTo, jobj);
  }

  void processMacro(JSONObject data) {
    // FIXME: need to check parameters.
    // FIXME: need to check permissions.
    if ("callMacro".equalsIgnoreCase(data.getString("command"))) {
      Token token = findTokenFromId(data.getString("tokenId"));

      MacroButtonProperties macro = token.getMacro(data.getInt("macroIndex"), false);
      macro.executeMacro(token.getId());
    }
  }

  void processSetProperties(JSONObject data) {
    final String tokenId = data.getString("tokenId");
    final Token token = findTokenFromId(tokenId);
    final Zone zone = findZoneTokenIsOn(token);

    if (token == null) {
      System.out.println("DEBUG: sendTokenInfo(): Unable to find token " + tokenId);
      return;
      // FIXME: log this error
    }

    final JSONObject props = data.getJSONObject("properties");
    EventQueue.invokeLater(
        new Runnable() {
          @SuppressWarnings("unchecked")
          @Override
          public void run() {
            Set<String> pnames = props.keySet();
            for (String pname : pnames) {
              String val = props.getString(pname);
              token.setProperty(pname, val);
            }

            zone.putToken(token);
          }
        });
  }
}
