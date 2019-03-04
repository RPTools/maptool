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
package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class FindTokenFunctions extends AbstractFunction {
  // @formatter:off
  private enum FindType {
    SELECTED,
    IMPERSONATED,
    NPC,
    PC,
    ALL,
    CURRENT,
    EXPOSED,
    STATE,
    OWNED,
    VISIBLE,
    LAYER // FJE 1.3b77
  }
  // @formatter:on

  private static final FindTokenFunctions instance = new FindTokenFunctions();

  /** Filter for all tokens. */
  private class AllFilter implements Zone.Filter {
    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      return true;
    }
  }

  /** Filter for NPC tokens. */
  private class NPCFilter implements Zone.Filter {
    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      return t.getType() == Token.Type.NPC;
    }
  }

  /** Filter for PC tokens. */
  private class PCFilter implements Zone.Filter {
    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      return t.getType() == Token.Type.PC;
    }
  }

  /** Filter for player exposed tokens. */
  private class ExposedFilter implements Zone.Filter {
    private final Zone zone;

    public ExposedFilter(Zone zone) {
      this.zone = zone;
    }

    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      return zone.isTokenVisible(t);
    }
  }

  /** Filter for finding tokens by set state. */
  private class StateFilter implements Zone.Filter {
    private final String stateName;

    public StateFilter(String stateName) {
      this.stateName = stateName;
    }

    public boolean matchToken(Token t) {
      Object val = t.getState(stateName);
      // Filter out the utility lib: and image: tokens
      if (val == null) {
        return false;
      }
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      if (val instanceof Boolean) {
        return ((Boolean) val).booleanValue();
      }
      if (val instanceof BigDecimal) {
        if (val.equals(BigDecimal.ZERO)) {
          return false;
        } else {
          return true;
        }
      }
      return true;
    }
  }

  /** Filter for finding tokens by owner. */
  private class OwnedFilter implements Zone.Filter {
    private final String name;

    public OwnedFilter(String name) {
      this.name = name;
    }

    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      return t.isOwner(name);
    }
  }

  /**
   * Filter by the layer the token is on (allows selecting tokens on the Object and Background
   * layers).
   */
  private class LayerFilter implements Zone.Filter {
    private final JSONArray layers;

    public LayerFilter(JSONArray layers) {
      this.layers = new JSONArray();
      for (Object s : layers) {
        String name = s.toString().toUpperCase();
        this.layers.add(Zone.Layer.valueOf("HIDDEN".equals(name) ? "GM" : name));
      }
    }

    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      if (t.getName().toLowerCase().startsWith("image:")
          || t.getName().toLowerCase().startsWith("lib:")) {
        return false;
      }
      return layers.contains(t.getLayer());
    }
  }

  private FindTokenFunctions() {
    super(
        0,
        2,
        "findToken",
        "currentToken",
        "getTokenName",
        "getTokenNames",
        "getSelectedNames",
        "getTokens",
        "getSelected",
        "getImpersonated",
        "getImpersonatedName",
        "getExposedTokens",
        "getExposedTokenNames",
        "getPC",
        "getNPC",
        "getPCNames",
        "getNPCNames",
        "getWithState",
        "getWithStateNames",
        "getOwned",
        "getOwnedNames",
        "getVisibleTokens",
        "getVisibleTokenNames");
  }

  public static FindTokenFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    boolean nameOnly = false;

    if (!functionName.equals("currentToken")
        && !functionName.startsWith("getImpersonated")
        && !functionName.startsWith("getVisible")
        && !functionName.startsWith("getSelected")) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }
    }
    if (functionName.equals("findToken")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      String mapName = parameters.size() > 1 ? parameters.get(1).toString() : null;
      return findTokenId(parameters.get(0).toString(), mapName);
    }
    String delim = ",";
    FindType findType;
    String findArgs = null;
    if (functionName.equals("currentToken")) {
      findType = FindType.CURRENT;
    } else if (functionName.startsWith("getSelected")) {
      findType = FindType.SELECTED;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getImpersonated")) {
      findType = FindType.IMPERSONATED;
    } else if (functionName.startsWith("getPC")) {
      findType = FindType.PC;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getNPC")) {
      findType = FindType.NPC;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getToken")) {
      findType = FindType.ALL;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getExposedToken")) {
      findType = FindType.EXPOSED;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getWithState")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      findType = FindType.STATE;
      findArgs = parameters.get(0).toString();
      delim = parameters.size() > 1 ? parameters.get(1).toString() : delim;
    } else if (functionName.startsWith("getOwned")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      findType = FindType.OWNED;
      findArgs = parameters.get(0).toString();
      delim = parameters.size() > 1 ? parameters.get(1).toString() : delim;
    } else if (functionName.startsWith("getVisibleToken")) {
      findType = FindType.VISIBLE;
      delim = parameters.size() > 0 ? parameters.get(0).toString() : delim;
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }

    if (functionName.endsWith("Name") || functionName.endsWith("Names")) {
      nameOnly = true;
    }

    // Special case of getToken,getTokenNames where a JSON object supplies arguments
    if (findType == FindType.ALL && parameters.size() > 1) {
      return getTokenList(parser, nameOnly, delim, parameters.get(1).toString());
    }
    return getTokens(parser, findType, nameOnly, delim, findArgs);
  }

  /**
   * Called when the MTscript function is <code>getToken</code>, <code>getTokens</code>, <code>
   * getTokenName</code>, or <code>getTokenNames</code>.
   *
   * @param parser parser context object
   * @param nameOnly whether to return only token names (<code>false</code> = token GUIDs)
   * @param delim either <code>json</code> or a string delimiter between output entries
   * @param jsonString incoming JSON data structure to filter results
   * @return
   * @throws ParserException
   */
  private Object getTokenList(Parser parser, boolean nameOnly, String delim, String jsonString)
      throws ParserException {
    JSONObject jobj = JSONObject.fromObject(jsonString);

    // First get a list of all our tokens. By default this is limited to the TOKEN and GM layers.
    List<Token> allTokens = null;
    JSONArray layers = null;
    if (!jobj.containsKey("layer")) {
      layers = new JSONArray();
      layers.add(Zone.Layer.TOKEN.toString());
      layers.add(Zone.Layer.GM.toString());
    } else {
      Object o = jobj.get("layer");
      if (o instanceof JSONArray) {
        layers = (JSONArray) o;
      } else {
        layers = new JSONArray();
        layers.add(o.toString());
      }
    }
    allTokens =
        MapTool.getFrame()
            .getCurrentZoneRenderer()
            .getZone()
            .getTokensFiltered(new LayerFilter(layers));
    List<Token> tokenList = new ArrayList<Token>(allTokens.size());
    tokenList.addAll(allTokens);
    JSONObject range = null;
    JSONObject area = null;

    // Now loop through conditions that are true and only retain tokens returned.
    for (Object key : jobj.keySet()) {
      String searchType = key.toString();
      if ("setStates".equalsIgnoreCase(searchType)) {
        // setStates and layers work the same until you get to the filtering part...
        JSONArray ary;
        Object o = jobj.get(searchType);
        if (o instanceof JSONArray) {
          ary = (JSONArray) o;
        } else {
          ary = new JSONArray();
          ary.add(o.toString());
        }
        // Looking for tokens with all of these states set
        for (Object item : ary) {
          List<Token> lst = getTokenList(parser, FindType.STATE, item.toString());
          tokenList.retainAll(lst);
        }
      } else if ("range".equalsIgnoreCase(searchType)) {
        // We will do this as one of the last steps as it's one of the most expensive so we want to
        // do it on as few tokens as possible
        range = jobj.getJSONObject(searchType);
      } else if ("area".equalsIgnoreCase(searchType)) {
        // We will do this as one of the last steps as it's one of the most expensive so we want to
        // do it on as few tokens as possible
        area = jobj.getJSONObject(searchType);
        // } else if ("unsetStates".equalsIgnoreCase(searchType)) {
        // // ignore
      } else {
        if (booleanCheck(jobj, searchType)) {
          List<Token> lst = null;
          if ("npc".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.NPC, "");
          } else if ("pc".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.PC, "");
          } else if ("selected".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.SELECTED, "");
          } else if ("visible".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.VISIBLE, "");
          } else if ("owned".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.OWNED, MapTool.getPlayer().getName());
          } else if ("current".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.CURRENT, "");
          } else if ("impersonated".equalsIgnoreCase(searchType)) {
            lst = getTokenList(parser, FindType.IMPERSONATED, "");
          }
          if (lst != null) tokenList.retainAll(lst);
        }
      }
    }

    // After looping through all the true conditions it's time to loop through
    // the false conditions and remove any tokens that match from our list.
    // This is a little more painful as first we get the tokens that match
    // the criteria, remove those from a list of all tokens, and use that
    // resultant list to tell the tokenList which to retain.
    // FJE Huh? Why not just remove ones that match from 'tokenList'???
    List<Token> inverseList = new ArrayList<Token>();
    for (Object key : jobj.keySet()) {
      String searchType = key.toString();
      if ("unsetStates".equalsIgnoreCase(searchType)) {
        JSONArray states = (JSONArray) jobj.get(searchType);
        for (Object st : states) {
          inverseList.clear();
          inverseList.addAll(allTokens);
          inverseList.removeAll(getTokenList(parser, FindType.STATE, st.toString()));
          tokenList.retainAll(inverseList);
        }
        // } else if ("setStates".equalsIgnoreCase(searchType)) {
        // // ignore
        // } else if ("range".equalsIgnoreCase(searchType)) {
        // // ignore
        // } else if ("area".equalsIgnoreCase(searchType)) {
        // // ignore
      } else {
        if (!booleanCheck(jobj, searchType)) {
          inverseList.clear();
          inverseList.addAll(allTokens);
          if ("npc".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(getTokenList(parser, FindType.NPC, ""));
          } else if ("pc".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(getTokenList(parser, FindType.PC, ""));
          } else if ("selected".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(getTokenList(parser, FindType.SELECTED, ""));
          } else if ("visible".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(getTokenList(parser, FindType.VISIBLE, ""));
          } else if ("owned".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(
                getTokenList(parser, FindType.OWNED, MapTool.getPlayer().getName()));
          } else if ("current".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(getTokenList(parser, FindType.CURRENT, ""));
          } else if ("impersonated".equalsIgnoreCase(searchType)) {
            inverseList.removeAll(getTokenList(parser, FindType.IMPERSONATED, ""));
          }
          if (inverseList != null) tokenList.retainAll(inverseList);
        }
      }
    }

    // Loop through and compare ranges if we have them
    if (range != null) {
      TokenLocationFunctions instance = TokenLocationFunctions.getInstance();
      Token token;
      if (range.containsKey("token")) {
        token = findToken(range.getString("token"), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "getTokens", range.getString("token")));
        }
      } else {
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null)
          token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
        else token = findToken(MapTool.getFrame().getCommandPanel().getIdentity(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "getTokens"));
        }
      }
      int from = Integer.MIN_VALUE;
      int upto = Integer.MAX_VALUE;

      if (range.containsKey("from")) {
        from = range.getInt("from");
      }
      if (range.containsKey("upto")) {
        upto = range.getInt("upto");
      }
      boolean useDistancePerCell = true;
      if (range.containsKey("distancePerCell")) {
        useDistancePerCell = booleanCheck(range, "distancePerCell");
      }

      String metric = null;
      if (range.containsKey("metric")) {
        metric = range.getString("metric");
      }
      List<Token> inrange = new LinkedList<Token>();
      for (Token targetToken : tokenList) {
        Double distance = instance.getDistance(token, targetToken, useDistancePerCell, metric);
        if (distance <= upto && distance >= from && token != targetToken) {
          inrange.add(targetToken);
        }
      }
      tokenList.retainAll(inrange);
    }

    // Loop through and compare the area if we have it
    if (area != null) {
      TokenLocationFunctions instance = TokenLocationFunctions.getInstance();
      Token token;
      if (area.containsKey("token")) {
        token = findToken(area.getString("token"), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "getTokens", area.getString("token")));
        }
      } else {
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null)
          token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
        else token = findToken(MapTool.getFrame().getCommandPanel().getIdentity(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "getTokens"));
        }
      }
      JSONArray offsets = area.getJSONArray("offsets");
      if (offsets == null) {
        throw new ParserException(
            I18N.getText("macro.function.findTokenFunctions.offsetArray", "getTokens"));
      }
      String metric = null;
      if (area.containsKey("metric")) {
        metric = area.getString("metric");
      }
      CellPoint cp = instance.getTokenCell(token);

      Set<Token> matching = new HashSet<Token>();
      for (Object o : offsets) {
        if (!(o instanceof JSONObject)) {
          throw new ParserException(
              I18N.getText("macro.function.findTokenFunctions.offsetArray", "getTokens"));
        }
        JSONObject joff = (JSONObject) o;
        if (!joff.containsKey("x") || !joff.containsKey("y")) {
          throw new ParserException(
              I18N.getText("macro.function.findTokenFunctions.offsetArray", "getTokens"));
        }
        int x = joff.getInt("x");
        int y = joff.getInt("y");
        for (Token targetToken : tokenList) {
          if (!matching.contains(targetToken)) {
            Double distance = instance.getDistance(targetToken, cp.x + x, cp.y + y, false, metric);
            if (distance >= 0 && distance < 1) {
              matching.add(targetToken);
            }
          }
        }
      }
      tokenList.retainAll(matching);
    }

    ArrayList<String> values = new ArrayList<String>();
    for (Token token : tokenList) {
      if (nameOnly) {
        values.add(token.getName());
      } else {
        values.add(token.getId().toString());
      }
    }
    if ("json".equals(delim)) {
      return JSONArray.fromObject(values);
    } else {
      return StringFunctions.getInstance().join(values, delim);
    }
  }

  private boolean booleanCheck(JSONObject jobj, String searchType) {
    Object val = jobj.get(searchType);
    if (val instanceof Boolean) {
      if (Boolean.TRUE.equals(val)) {
        return true;
      } else {
        return false;
      }
    } else if (val instanceof Integer) {
      if (Integer.valueOf(0).equals(val)) {
        return false;
      } else {
        return true;
      }
    } else {
      return val == null ? true : false;
    }
  }

  private List<Token> getTokenList(Parser parser, FindType findType, String findArgs)
      throws ParserException {
    List<Token> tokenList = new LinkedList<Token>();
    ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zoneRenderer.getZone();
    switch (findType) {
      case ALL:
        tokenList = zone.getTokensFiltered(new AllFilter());
        break;
      case NPC:
        tokenList = zone.getTokensFiltered(new NPCFilter());
        break;
      case PC:
        tokenList = zone.getTokensFiltered(new PCFilter());
        break;
      case SELECTED:
        tokenList = zoneRenderer.getSelectedTokensList();
        break;
      case CURRENT:
        Token token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
        if (token != null) {
          tokenList.add(token);
        }
        break;
      case IMPERSONATED:
        Token t;
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null) t = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
        else t = zone.resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());
        if (t != null) tokenList.add(t);
        break;
      case EXPOSED:
        tokenList = zone.getTokensFiltered(new ExposedFilter(zone));
        break;
      case STATE:
        tokenList = zone.getTokensFiltered(new StateFilter(findArgs));
        break;
      case OWNED:
        tokenList = zone.getTokensFiltered(new OwnedFilter(findArgs));
        break;
      case VISIBLE:
        for (GUID id : zoneRenderer.getVisibleTokenSet()) {
          tokenList.add(zone.getToken(id));
        }
      case LAYER:
        // Layer check already performed and unneeded here
        break;
      default:
        // Should never get here, but if we do then another enum type was added and we didn't
        // account for it!
        throw new ParserException(
            I18N.getText(
                "macro.function.findTokenFunctions.unknownEnum", "getTokens", findType.toString()));
    }
    return tokenList;
  }

  /**
   * Gets the names or ids of the tokens on the current map.
   *
   * @param parser The parser that called the function.
   * @param findType The type of tokens to find.
   * @param nameOnly If a list of names is wanted.
   * @param delim The delimiter to use for lists, or "json" for a json array.
   * @param findArgs Any arguments for the find function
   * @return a string list that contains the ids or names of the tokens.
   * @throws ParserException if this code adds a new enum but doesn't properly handle it
   */
  private String getTokens(
      Parser parser, FindType findType, boolean nameOnly, String delim, String findArgs)
      throws ParserException {
    ArrayList<String> values = new ArrayList<String>();
    List<Token> tokens = getTokenList(parser, findType, findArgs);

    if (tokens != null && !tokens.isEmpty()) {
      for (Token token : tokens) {
        if (nameOnly) {
          values.add(token.getName());
        } else {
          values.add(token.getId().toString());
        }
      }
    }
    if ("json".equals(delim)) {
      return JSONArray.fromObject(values).toString();
    } else {
      return StringFunctions.getInstance().join(values, delim);
    }
  }

  /**
   * Finds the specified token.
   *
   * @param identifier the name of the token.
   * @return the token.
   */
  private String findTokenId(String identifier, String zoneName) {
    Token token = findToken(identifier, zoneName);
    return token == null ? "" : token.getId().toString();
  }

  /**
   * Finds the specified token.
   *
   * @param identifier the name of the token.
   * @return the token.
   */
  public static Token findToken(String identifier, String zoneName) {
    if (zoneName == null || zoneName.length() == 0) {
      Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      Token token = zone.resolveToken(identifier);
      return token;
    } else {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        Zone zone = zr.getZone();
        if (zone.getName().equalsIgnoreCase(zoneName)) {
          Token token = zone.resolveToken(identifier);
          if (token != null) {
            return token;
          }
        }
      }
    }
    return null;
  }
}
