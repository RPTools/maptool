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

import static net.rptools.maptool.client.functions.JSONMacroFunctions.convertToJSON;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.FunctionUtil;
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
    PROPERTYTYPE, // 1.5.5
    LAYER // FJE 1.3b77
  }
  // @formatter:on

  private static final FindTokenFunctions instance = new FindTokenFunctions();

  /** Filter for all non image / non lib tokens. */
  private class AllFilter implements Zone.Filter {
    private final boolean match;

    private AllFilter(boolean match) {
      this.match = match;
    }

    public boolean matchToken(Token t) {
      // Match=true: filter out utility lib: and image: tokens
      return match == !t.isImgOrLib();
    }
  }

  /** Filter for NPC tokens. */
  private class NPCFilter implements Zone.Filter {
    private final boolean match; // true: NPC, false: Non-NPC

    private NPCFilter(boolean match) {
      this.match = match;
    }

    public boolean matchToken(Token t) {
      boolean isNPC = t.getType() == Token.Type.NPC && !t.isImgOrLib();
      return match == isNPC;
    }
  }

  /** Filter for PC tokens. */
  private class PCFilter implements Zone.Filter {
    private final boolean match; // true: PC, false: Non-PC

    private PCFilter(boolean match) {
      this.match = match;
    }

    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      boolean isPC = t.getType() == Token.Type.PC && !t.isImgOrLib();
      return match == isPC;
    }
  }

  /** Filter for Light */
  private class LightFilter implements Zone.Filter {
    private final String type;
    private final String name;
    private final boolean match;

    public LightFilter(String type, String name, boolean match) {
      this.type = type;
      this.name = name;
      this.match = match;
    }

    @Override
    public boolean matchToken(Token t) {
      try {
        return match == TokenLightFunctions.hasLightSource(t, type, name);
      } catch (ParserException e) {
        // Should not happen: a test was done already
        MapTool.showError(e.getLocalizedMessage());
        return false;
      }
    }
  }

  /** Filter for player exposed tokens. */
  private class ExposedFilter implements Zone.Filter {
    private final Zone zone;
    private final boolean match;

    public ExposedFilter(Zone zone, boolean match) {
      this.zone = zone;
      this.match = match;
    }

    public boolean matchToken(Token t) {
      boolean isExposed = zone.isTokenVisible(t) && !t.isImgOrLib();
      return match == isExposed;
    }
  }

  /** Filter for finding tokens by set state. */
  private class StateFilter implements Zone.Filter {
    private final String stateName;
    private final boolean match;

    public StateFilter(String stateName, boolean match) {
      this.stateName = stateName;
      this.match = match;
    }

    public boolean matchToken(Token t) {
      Object val = t.getState(stateName);
      boolean hasState = true;
      // Filter out the utility lib: and image: tokens
      if (val == null || t.isImgOrLib()) {
        hasState = false;
      } else if (val instanceof Boolean) {
        hasState = (Boolean) val;
      } else if (val instanceof BigDecimal) {
        hasState = !val.equals(BigDecimal.ZERO);
      }
      return match == hasState;
    }
  }

  /** Filter for finding tokens by owner. */
  private class OwnedFilter implements Zone.Filter {
    private final String name;
    private final boolean match;

    public OwnedFilter(String name, boolean match) {
      this.name = name;
      this.match = match;
    }

    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      boolean isOwner = t.isOwner(name) && !t.isImgOrLib();
      return match == isOwner;
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
      return layers.contains(t.getLayer()) && !t.isImgOrLib();
    }
  }

  private class PropertyTypeFilter implements Zone.Filter {
    private final JSONArray types;

    public PropertyTypeFilter(JSONArray types) {
      this.types = types;
    }

    public boolean matchToken(Token t) {
      // Don't filter out lib and image
      boolean isType = types.contains(t.getPropertyType());
      return isType;
    }
  }

  private FindTokenFunctions() {
    super(
        0,
        3,
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
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String mapName = parameters.size() > 1 ? parameters.get(1).toString() : null;
      return findTokenId(parameters.get(0).toString(), mapName);
    }
    int psize = parameters.size();
    String delim = ",";
    FindType findType;
    String findArgs = null;
    ZoneRenderer zoneRenderer = null;
    if (functionName.equals("currentToken")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      findType = FindType.CURRENT;
    } else if (functionName.startsWith("getSelected")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      findType = FindType.SELECTED;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getImpersonated")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      findType = FindType.IMPERSONATED;
    } else if (functionName.startsWith("getPC")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      findType = FindType.PC;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getNPC")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      findType = FindType.NPC;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getToken")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      findType = FindType.ALL;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getExposedToken")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      findType = FindType.EXPOSED;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getWithState")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      findType = FindType.STATE;
      findArgs = parameters.get(0).toString();
      delim = psize > 1 ? parameters.get(1).toString() : delim;
    } else if (functionName.startsWith("getOwned")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      findType = FindType.OWNED;
      findArgs = psize > 0 ? parameters.get(0).toString() : MapTool.getPlayer().getName();
      delim = psize > 1 ? parameters.get(1).toString() : delim;
      zoneRenderer = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 2);
    } else if (functionName.startsWith("getVisibleToken")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      findType = FindType.VISIBLE;
      delim = psize > 0 ? parameters.get(0).toString() : delim;
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
    return getTokens(parser, findType, nameOnly, delim, findArgs, zoneRenderer);
  }

  /**
   * Called when the MTscript function is <code>getToken</code>, <code>getTokens</code>, <code>
   * getTokenName</code>, or <code>getTokenNames</code>.
   *
   * @param parser parser context object
   * @param nameOnly whether to return only token names (<code>false</code> = token GUIDs)
   * @param delim either <code>json</code> or a string delimiter between output entries
   * @param jsonString incoming JSON data structure to filter results
   * @return list of filtered tokens
   * @throws ParserException if a condition is incorrect
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
    ZoneRenderer zoneRenderer;
    String mapName;
    if (!jobj.containsKey("mapName")) {
      mapName = null; // set to null so findToken searches the current map
      zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    } else {
      mapName = jobj.get("mapName").toString();
      zoneRenderer = MapTool.getFrame().getZoneRenderer(mapName);
      if (zoneRenderer == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.moveTokenMap.unknownMap",
                nameOnly ? "getTokenNames" : "getTokens",
                mapName));
      }
    }
    Zone zone = zoneRenderer.getZone();
    allTokens = zone.getTokensFiltered(new LayerFilter(layers));
    List<Token> tokenList = new ArrayList<Token>(allTokens.size());
    tokenList.addAll(allTokens);
    JSONObject range = null;
    JSONObject area = null;

    Boolean match;
    // Now loop through conditions and filter out tokens that don't match conditions
    for (Object key : jobj.keySet()) {
      String searchType = key.toString();
      if ("setStates".equalsIgnoreCase(searchType) || "unsetStates".equalsIgnoreCase(searchType)) {
        JSONArray states;
        Object o = jobj.get(searchType);
        if (o instanceof JSONArray) states = (JSONArray) o;
        else {
          states = new JSONArray();
          states.add(o.toString());
        }
        match = "setStates".equalsIgnoreCase(searchType);
        // Looking for tokens that either match or don't match the states
        for (Object item : states) {
          tokenList =
              getTokenList(parser, FindType.STATE, item.toString(), match, tokenList, zoneRenderer);
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
      } else if ("propertyType".equalsIgnoreCase(searchType)) {
        JSONArray types;
        Object o = jobj.get(searchType);
        if (o instanceof JSONArray) types = (JSONArray) o;
        else {
          types = new JSONArray();
          types.add(o.toString());
        }
        tokenList = getTokensFiltered(new PropertyTypeFilter(types), tokenList);
      } else if ("light".equalsIgnoreCase(searchType)) {
        String value = jobj.get(searchType).toString();
        String type, name;
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
          match = true;
          type = name = "*";
        } else if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
          match = false;
          type = name = "*";
        } else {
          Object jsonLight = convertToJSON(value);
          if (jsonLight instanceof JSONObject) {
            JSONObject jobjLight = (JSONObject) jsonLight;
            match = !jobjLight.has("value") || FunctionUtil.getBooleanValue(jobjLight.get("value"));
            type = jobjLight.has("category") ? jobjLight.get("category").toString() : "*";
            name = jobjLight.has("name") ? jobjLight.get("name").toString() : "*";

            Map<String, Map<GUID, LightSource>> lightSourcesMap =
                MapTool.getCampaign().getLightSourcesMap();

            if (!"*".equals(type) && !lightSourcesMap.containsKey(type))
              throw new ParserException(
                  I18N.getText("macro.function.tokenLight.unknownLightType", "light", type));

          } else {
            throw new ParserException(
                I18N.getText("macro.function.json.onlyObject", value.toString(), "light"));
          }
        }
        tokenList = getTokensFiltered(new LightFilter(type, name, match), tokenList);
      } else {
        match = booleanCheck(jobj, searchType);
        if ("npc".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(parser, FindType.NPC, "", match, tokenList, zoneRenderer);
        } else if ("pc".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(parser, FindType.PC, "", match, tokenList, zoneRenderer);
        } else if ("selected".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(parser, FindType.SELECTED, "", match, tokenList, zoneRenderer);
        } else if ("visible".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(parser, FindType.VISIBLE, "", match, tokenList, zoneRenderer);
        } else if ("owned".equalsIgnoreCase(searchType)) {
          tokenList =
              getTokenList(
                  parser,
                  FindType.OWNED,
                  MapTool.getPlayer().getName(),
                  match,
                  tokenList,
                  zoneRenderer);
        } else if ("current".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(parser, FindType.CURRENT, "", match, tokenList, zoneRenderer);
        } else if ("impersonated".equalsIgnoreCase(searchType)) {
          tokenList =
              getTokenList(parser, FindType.IMPERSONATED, "", match, tokenList, zoneRenderer);
        }
      }
    }

    // Loop through and compare ranges if we have them
    if (range != null) {
      TokenLocationFunctions instance = TokenLocationFunctions.getInstance();
      Token token;
      if (range.containsKey("token")) {
        token = findToken(range.getString("token"), mapName);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "getTokens", range.getString("token")));
        }
      } else {
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null) token = zone.getToken(guid);
        else token = findToken(MapTool.getFrame().getCommandPanel().getIdentity(), mapName);
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
        token = findToken(area.getString("token"), mapName);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "getTokens", area.getString("token")));
        }
      } else {
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null) token = zone.getToken(guid);
        else token = findToken(MapTool.getFrame().getCommandPanel().getIdentity(), mapName);
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

      Point[] points = new Point[offsets.size()];
      int ip = 0; // create an array of points for each cell
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
        // note: cp.x and cp.y returns the top left cell (pixel for gridless
        points[ip] = new Point(joff.getInt("x") + cp.x, joff.getInt("y") + cp.y);
        ip += 1;
      }
      Set<Token> matching = new HashSet<Token>();
      for (Token targetToken : tokenList) {
        if (TokenLocationFunctions.isTokenAtXY(targetToken, zone, points))
          matching.add(targetToken);
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

  /**
   * Take a list of tokens and return a new sublist where each token satisfies the specified
   * condition
   *
   * @param parser The parser, to get variables in context
   * @param findType the type of search to do
   * @param findArgs additional argument for the search
   * @param match should the property match? true: only include matches, false: exclude matches
   * @param originalList the list of tokens to search from
   * @param zoneRenderer the zone render of the map where the tokens are
   * @return tokenList satisfying the requirement
   */
  private List<Token> getTokenList(
      Parser parser,
      FindType findType,
      String findArgs,
      boolean match,
      List<Token> originalList,
      ZoneRenderer zoneRenderer)
      throws ParserException {
    List<Token> tokenList = new LinkedList<Token>();
    if (originalList.size() == 0) return tokenList;

    Zone zone = zoneRenderer.getZone();
    switch (findType) {
      case ALL:
        tokenList = getTokensFiltered(new AllFilter(match), originalList);
        break;
      case NPC:
        tokenList = getTokensFiltered(new NPCFilter(match), originalList);
        break;
      case PC:
        tokenList = getTokensFiltered(new PCFilter(match), originalList);
        break;
      case SELECTED:
        tokenList = getTokensFiltered(zoneRenderer.getSelectedTokensList(), originalList, match);
        break;
      case CURRENT:
        Token token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
        if (token != null) {
          tokenList = getTokensFiltered(Collections.singletonList(token), originalList, match);
        } else if (!match) tokenList = originalList;
        break;
      case IMPERSONATED:
        Token t;
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null) t = zone.getToken(guid);
        else t = zone.resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());
        if (t != null) {
          tokenList = getTokensFiltered(Collections.singletonList(t), originalList, match);
        } else if (!match) tokenList = originalList;
        break;
      case EXPOSED:
        tokenList = getTokensFiltered(new ExposedFilter(zone, match), originalList);
        break;
      case STATE:
        tokenList = getTokensFiltered(new StateFilter(findArgs, match), originalList);
        break;
      case OWNED:
        tokenList = getTokensFiltered(new OwnedFilter(findArgs, match), originalList);
        break;
      case VISIBLE:
        tokenList = getTokensFiltered(zoneRenderer.getVisibleTokens(), originalList, match);
        break;
      case LAYER:
        // Layer check already performed and unneeded here
        tokenList = originalList;
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

  private static List<Token> getTokensFiltered(Zone.Filter filter, List<Token> originalList) {
    List<Token> tokenList = new ArrayList<Token>(originalList.size());

    for (Token token : originalList) {
      if (filter.matchToken(token)) tokenList.add(token);
    }
    return tokenList;
  }

  private static List<Token> getTokensFiltered(
      List<Token> editList, List<Token> originalList, boolean match) {
    List<Token> tokenList = new ArrayList<Token>(originalList);

    if (match) tokenList.retainAll(editList); // keep tokens in both lists
    else tokenList.removeAll(editList); // remove edit list from original list
    return tokenList;
  }

  /**
   * Gets the names or ids of the tokens on a map.
   *
   * @param parser The parser that called the function.
   * @param findType The type of tokens to find.
   * @param nameOnly If a list of names is wanted.
   * @param delim The delimiter to use for lists, or "json" for a json array.
   * @param findArgs Any arguments for the find function
   * @param zoneRenderer the zone renderer, or null if using the current one
   * @return a string list that contains the ids or names of the tokens.
   * @throws ParserException if this code adds a new enum but doesn't properly handle it
   */
  private String getTokens(
      Parser parser,
      FindType findType,
      boolean nameOnly,
      String delim,
      String findArgs,
      ZoneRenderer zoneRenderer)
      throws ParserException {
    ArrayList<String> values = new ArrayList<String>();
    if (zoneRenderer == null) {
      zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    }
    Zone zone = zoneRenderer.getZone();
    List<Token> tokens =
        getTokenList(parser, findType, findArgs, true, zone.getAllTokens(), zoneRenderer);

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
   * Finds the specified token id.
   *
   * @param identifier the name of the token.
   * @param zoneName the name of the zone.
   * @return the token Id, or a blank string if none found.
   */
  private String findTokenId(String identifier, String zoneName) {
    Token token = findToken(identifier, zoneName);
    return token == null ? "" : token.getId().toString();
  }

  /**
   * Finds the specified token.
   *
   * @param identifier the name of the token.
   * @param zoneName the name of the zone.
   * @return the token, or null if none found.
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

  /**
   * find a token on all maps (first matching is returned)
   *
   * @param identifier to check for
   * @return the token
   */
  public static Token findToken(final String identifier) {
    final Zone currentZone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    Token token = currentZone.resolveToken(identifier);
    if (token == null) {
      final List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (final ZoneRenderer zr : zrenderers) {
        final Zone zone = zr.getZone();
        token = zone.resolveToken(identifier);
        if (token != null) {
          return token;
        }
      }
    }
    return token;
  }
}
