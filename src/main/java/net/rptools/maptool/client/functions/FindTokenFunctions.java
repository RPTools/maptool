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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Includes currentToken(), findToken(), and functions to get lists of tokens through filters. */
public class FindTokenFunctions extends AbstractFunction {
  // @formatter:off
  private enum FindType {
    SELECTED,
    IMPERSONATED,
    IMPERSONATED_GLOBAL,
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

  private enum Ownership {
    BYALL, // tokens owned by all players
    NOTBYALL, // tokens not owned by all players
    SELF, // tokens owned by the current player
    NOTSELF, // tokens not owned by the current player
    OTHERS, // tokens owned by other players, but not yourself
    ANY, // tokens owned by any player
    NONE, // tokens owned by no players
    SINGLE, // tokens owned by a single player
    MULTIPLE, // tokens owned by more than one player
    ARRAY // tokens owned by one or more of the players listed in the array
  }

  private static Ownership getOwnership(String strOwnership) {
    strOwnership = strOwnership.toLowerCase().trim().replace("-", "");
    if (strOwnership.equals("byall")) return Ownership.BYALL;
    if (strOwnership.equals("notbyall")) return Ownership.NOTBYALL;
    if (strOwnership.equals("1") || strOwnership.equals("self")) return Ownership.SELF;
    if (strOwnership.equals("0") || strOwnership.equals("notself")) return Ownership.NOTSELF;
    if (strOwnership.equals("others")) return Ownership.OTHERS;
    if (strOwnership.equals("any")) return Ownership.ANY;
    if (strOwnership.equals("none")) return Ownership.NONE;
    if (strOwnership.equals("single")) return Ownership.SINGLE;
    if (strOwnership.equals("multiple")) return Ownership.MULTIPLE;
    if (strOwnership.equals("array")) return Ownership.ARRAY;
    return null;
  }

  private static final FindTokenFunctions instance = new FindTokenFunctions();

  /** Filter for all non image / non lib tokens. */
  private static class AllFilter implements Zone.Filter {
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
  private static class NPCFilter implements Zone.Filter {
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
  private static class PCFilter implements Zone.Filter {
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
  private static class LightFilter implements Zone.Filter {
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
  private static class ExposedFilter implements Zone.Filter {
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
  private static class StateFilter implements Zone.Filter {
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
  private static class OwnedFilter implements Zone.Filter {
    private final String playerName;
    private final Ownership ownership;
    private final Set<String> ownerList;

    OwnedFilter(Ownership ownership) {
      this.ownership = ownership;
      this.ownerList = Collections.emptySet();
      this.playerName = MapTool.getPlayer().getName();
    }

    OwnedFilter(Ownership ownership, Set<String> ownerList) {
      this.ownership = ownership;
      this.ownerList = ownerList;
      this.playerName = MapTool.getPlayer().getName();
    }

    OwnedFilter(String playerName, boolean match) {
      this.ownership =
          match ? FindTokenFunctions.Ownership.SELF : FindTokenFunctions.Ownership.NOTSELF;
      this.ownerList = Collections.emptySet();
      this.playerName = playerName;
    }

    public boolean matchToken(Token t) {
      if (ownership == Ownership.BYALL) return (t.isOwnedByAll());
      if (ownership == Ownership.NOTBYALL) return (!t.isOwnedByAll());

      if (ownership == Ownership.ANY) return (t.hasOwners());
      if (ownership == Ownership.NONE) return (!t.hasOwners());
      if (ownership == Ownership.MULTIPLE) return (t.isOwnedByAll() || t.getOwners().size() > 1);
      if (ownership == Ownership.SINGLE) return (!t.isOwnedByAll() && t.getOwners().size() == 1);
      if (ownership == Ownership.ARRAY) return (t.isOwnedByAny(ownerList));

      boolean isOwner = t.isOwner(playerName);
      if (ownership == Ownership.SELF) return (isOwner);
      if (ownership == Ownership.NOTSELF) return (!isOwner);
      if (ownership == Ownership.OTHERS) return (!isOwner && t.hasOwners());
      return false;
    }
  }

  /**
   * Filter by the layer the token is on (allows selecting tokens on the Object and Background
   * layers).
   */
  private static class LayerFilter implements Zone.Filter {
    private final List<Zone.Layer> filterLayers;

    public LayerFilter(JsonArray layers) {
      filterLayers = new ArrayList<>();
      for (JsonElement s : layers) {
        // Can't use .toString() as it wraps in extra quotes per JSON syntax.
        String name = s.getAsString().toUpperCase();
        final var layer = Zone.Layer.getByName(name);
        filterLayers.add(layer);
      }
    }

    public boolean matchToken(Token t) {
      // Filter out the utility lib: and image: tokens
      return filterLayers.contains(t.getLayer()) && !t.isImgOrLib();
    }
  }

  private static class PropertyTypeFilter implements Zone.Filter {
    private final JsonArray types;

    public PropertyTypeFilter(JsonArray types) {
      this.types = types;
    }

    public boolean matchToken(Token t) {
      // Don't filter out lib and image
      boolean isType = types.contains(new JsonPrimitive(t.getPropertyType()));
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

  /**
   * @return the instance.
   */
  public static FindTokenFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    boolean nameOnly = false;

    if (!functionName.equalsIgnoreCase("currentToken")
        && !functionName.startsWith("getImpersonated")
        && !functionName.startsWith("getVisible")
        && !functionName.startsWith("getSelected")
        && !MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
    if (functionName.equalsIgnoreCase("findToken")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String mapName = parameters.size() > 1 ? parameters.get(1).toString() : null;
      return findTokenId(parameters.get(0).toString(), mapName);
    }
    int psize = parameters.size();
    String delim = ",";
    FindType findType;
    String findArgs = null;
    ZoneRenderer zoneRenderer = null;
    if (functionName.equalsIgnoreCase("currentToken")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      findType = FindType.CURRENT;
    } else if (functionName.startsWith("getSelected")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      findType = FindType.SELECTED;
      delim = !parameters.isEmpty() ? parameters.get(0).toString() : delim;
    } else if (functionName.startsWith("getImpersonated")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      boolean global =
          psize > 0 ? FunctionUtil.paramAsBoolean(functionName, parameters, 0, false) : false;
      findType = global ? FindType.IMPERSONATED_GLOBAL : FindType.IMPERSONATED;
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
      return getTokenList(
          (MapToolVariableResolver) resolver, nameOnly, delim, parameters.get(1).toString());
    }
    return getTokens(
        (MapToolVariableResolver) resolver, findType, nameOnly, delim, findArgs, zoneRenderer);
  }

  /**
   * Called when the MTscript function is <code>getToken</code>, <code>getTokens</code>, <code>
   * getTokenName</code>, or <code>getTokenNames</code>.
   *
   * @param resolver parser context object
   * @param nameOnly whether to return only token names (<code>false</code> = token GUIDs)
   * @param delim either <code>json</code> or a string delimiter between output entries
   * @param jsonString incoming JSON data structure to filter results
   * @return list of filtered tokens
   * @throws ParserException if a condition is incorrect
   */
  private static Object getTokenList(
      MapToolVariableResolver resolver, boolean nameOnly, String delim, String jsonString)
      throws ParserException {
    JsonObject jobj = JsonParser.parseString(jsonString).getAsJsonObject();

    // First get a list of all our tokens. By default this is limited to the TOKEN and GM layers.
    List<Token> allTokens = null;
    JsonArray layers = null;
    if (!jobj.has("layer")) {
      layers = new JsonArray();
      layers.add(Zone.Layer.TOKEN.name());
      layers.add(Zone.Layer.GM.name());
    } else {
      Object o = jobj.get("layer");
      if (o instanceof JsonArray) {
        layers = (JsonArray) o;
      } else {
        layers = new JsonArray();
        if (o instanceof JsonPrimitive) {
          layers.add(((JsonPrimitive) o).getAsString());
        } else {
          layers.add(o.toString());
        }
      }
    }
    ZoneRenderer zoneRenderer;
    String mapName;
    if (!jobj.has("mapName")) {
      mapName = null; // set to null so findToken searches the current map
      zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    } else {
      mapName = jobj.get("mapName").getAsString();
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
    JsonObject range = null;
    JsonObject area = null;

    boolean match;
    // Now loop through conditions and filter out tokens that don't match conditions
    for (Object key : jobj.keySet()) {
      String searchType = key.toString();
      if ("setStates".equalsIgnoreCase(searchType) || "unsetStates".equalsIgnoreCase(searchType)) {
        JsonArray states;
        JsonElement json = jobj.get(searchType);
        if (json.isJsonArray()) states = json.getAsJsonArray();
        else {
          states = new JsonArray();
          states.add(json.getAsString());
        }
        match = "setStates".equalsIgnoreCase(searchType);
        // Looking for tokens that either match or don't match the states
        for (JsonElement item : states) {
          tokenList =
              getTokenList(
                  resolver, FindType.STATE, item.getAsString(), match, tokenList, zoneRenderer);
        }
      } else if ("range".equalsIgnoreCase(searchType)) {
        // We will do this as one of the last steps as it's one of the most expensive so we want to
        // do it on as few tokens as possible
        range = jobj.get(searchType).getAsJsonObject();
      } else if ("area".equalsIgnoreCase(searchType)) {
        // We will do this as one of the last steps as it's one of the most expensive so we want to
        // do it on as few tokens as possible
        area = jobj.get(searchType).getAsJsonObject();
        // } else if ("unsetStates".equalsIgnoreCase(searchType)) {
        // // ignore
      } else if ("propertyType".equalsIgnoreCase(searchType)) {
        JsonArray types;
        JsonElement json = jobj.get(searchType);
        if (json.isJsonArray()) types = json.getAsJsonArray();
        else {
          types = new JsonArray();
          types.add(json.getAsString());
        }
        tokenList = getTokensFiltered(new PropertyTypeFilter(types), tokenList);
      } else if ("light".equalsIgnoreCase(searchType)) {
        String type, name;
        JsonElement json = jobj.get(searchType);
        if (json.isJsonObject()) {
          JsonObject jobjLight = json.getAsJsonObject();
          match =
              !jobjLight.has("value")
                  || FunctionUtil.getBooleanValue(jobjLight.get("value").getAsString());
          type = jobjLight.has("category") ? jobjLight.get("category").getAsString() : "*";
          name = jobjLight.has("name") ? jobjLight.get("name").getAsString() : "*";

          Map<String, Map<GUID, LightSource>> lightSourcesMap =
              MapTool.getCampaign().getLightSourcesMap();

          if (!"*".equals(type) && !lightSourcesMap.containsKey(type)) {
            throw new ParserException(
                I18N.getText("macro.function.tokenLight.unknownLightType", "light", type));
          }
        } else if (json.isJsonArray()) {
          throw new ParserException(
              I18N.getText("macro.function.json.onlyObject", json.toString(), "light"));
        } else {
          String value = json.getAsString();
          if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            match = true;
            type = name = "*";
          } else if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            match = false;
            type = name = "*";
          } else {
            throw new ParserException(
                I18N.getText("macro.function.json.onlyObject", value, "light"));
          }
        }
        tokenList = getTokensFiltered(new LightFilter(type, name, match), tokenList);
      } else if ("owned".equalsIgnoreCase(searchType)) {
        JsonElement json = jobj.get(searchType);
        if (json.isJsonArray()) {
          Ownership ownership = Ownership.ARRAY;
          Set<String> setOwners = new HashSet<>();
          for (JsonElement ele : json.getAsJsonArray()) {
            setOwners.add(ele.getAsString());
          }
          tokenList = getTokensFiltered(new OwnedFilter(ownership, setOwners), tokenList);
        } else if (json.isJsonObject()) {
          throw new ParserException(
              I18N.getText("macro.function.json.onlyArray", json.toString(), "owned"));
        } else {
          Ownership ownership = getOwnership(json.getAsString());
          tokenList = getTokensFiltered(new OwnedFilter(ownership), tokenList);
        }
      } else {
        match = booleanCheck(jobj, searchType);
        if ("npc".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(resolver, FindType.NPC, "", match, tokenList, zoneRenderer);
        } else if ("pc".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(resolver, FindType.PC, "", match, tokenList, zoneRenderer);
        } else if ("selected".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(resolver, FindType.SELECTED, "", match, tokenList, zoneRenderer);
        } else if ("visible".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(resolver, FindType.VISIBLE, "", match, tokenList, zoneRenderer);
        } else if ("current".equalsIgnoreCase(searchType)) {
          tokenList = getTokenList(resolver, FindType.CURRENT, "", match, tokenList, zoneRenderer);
        } else if ("impersonated".equalsIgnoreCase(searchType)) {
          tokenList =
              getTokenList(resolver, FindType.IMPERSONATED, "", match, tokenList, zoneRenderer);
        }
      }
    }

    // Loop through and compare ranges if we have them
    if (range != null) {
      TokenLocationFunctions instance = TokenLocationFunctions.getInstance();
      Token token;
      if (range.has("token")) {
        token = findToken(range.get("token").getAsString(), mapName);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTokens",
                  range.get("token").getAsString()));
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

      if (range.has("from")) {
        from = range.get("from").getAsInt();
      }
      if (range.has("upto")) {
        upto = range.get("upto").getAsInt();
      }
      boolean useDistancePerCell = true;
      if (range.has("distancePerCell")) {
        useDistancePerCell = booleanCheck(range, "distancePerCell");
      }

      String metric = null;
      if (range.has("metric")) {
        metric = range.get("metric").getAsString();
      }
      List<Token> inrange = new LinkedList<Token>();
      for (Token targetToken : tokenList) {
        double distance = instance.getDistance(token, targetToken, useDistancePerCell, metric);
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
      if (area.has("token")) {
        token = findToken(area.get("token").getAsString(), mapName);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTokens",
                  area.get("token").getAsString()));
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
      JsonArray offsets = area.get("offsets").getAsJsonArray();
      if (offsets == null) {
        throw new ParserException(
            I18N.getText("macro.function.findTokenFunctions.offsetArray", "getTokens"));
      }
      String metric = null;
      if (area.has("metric")) {
        metric = area.get("metric").getAsString();
      }
      CellPoint cp = instance.getTokenCell(token);

      Point[] points = new Point[offsets.size()];
      int ip = 0; // create an array of points for each cell
      for (Object o : offsets) {
        if (!(o instanceof JsonObject)) {
          throw new ParserException(
              I18N.getText("macro.function.findTokenFunctions.offsetArray", "getTokens"));
        }
        JsonObject joff = (JsonObject) o;
        if (!joff.has("x") || !joff.has("y")) {
          throw new ParserException(
              I18N.getText("macro.function.findTokenFunctions.offsetArray", "getTokens"));
        }
        // note: cp.x and cp.y returns the top left cell (pixel for gridless
        points[ip] = new Point(joff.get("x").getAsInt() + cp.x, joff.get("y").getAsInt() + cp.y);
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
      JsonArray jsonArray = new JsonArray();
      for (String val : values) {
        jsonArray.add(val);
      }
      return jsonArray;
    } else {
      return StringFunctions.getInstance().join(values, delim);
    }
  }

  private static boolean booleanCheck(JsonObject jobj, String searchType) {
    JsonElement jel = jobj.get(searchType);
    if (jel.isJsonPrimitive()) {
      JsonPrimitive jprim = jel.getAsJsonPrimitive();
      if (jprim.isBoolean()) {
        return jprim.getAsBoolean();
      } else if (jprim.isNumber()) {
        return jprim.getAsInt() != 0;
      } else {
        // What's the rationale for returning true for other types?
        // Should we be looking at strings for true/false?
        return true;
      }
    }
    return false;
  }

  /**
   * Take a list of tokens and return a new sublist where each token satisfies the specified
   * condition
   *
   * @param resolver The parser, to get variables in context
   * @param findType the type of search to do
   * @param findArgs additional argument for the search
   * @param match should the property match? true: only include matches, false: exclude matches
   * @param originalList the list of tokens to search from
   * @param zoneRenderer the zone render of the map where the tokens are
   * @return tokenList satisfying the requirement
   */
  private static List<Token> getTokenList(
      MapToolVariableResolver resolver,
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
        Token token = resolver.getTokenInContext();
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
      case IMPERSONATED_GLOBAL:
        t = null;
        guid = MapTool.getFrame().getImpersonatePanel().getTokenId();
        if (guid != null) {
          // Searches all maps to find impersonated token
          t = findToken(guid.toString());
        }
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
      case OWNED: // for "getOwned" and "getOwnedNames" only. getTokens uses different code
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
   * @param resolver The parser that called the function.
   * @param findType The type of tokens to find.
   * @param nameOnly If a list of names is wanted.
   * @param delim The delimiter to use for lists, or "json" for a json array.
   * @param findArgs Any arguments for the find function
   * @param zoneRenderer the zone renderer, or null if using the current one
   * @return a string list that contains the ids or names of the tokens.
   * @throws ParserException if this code adds a new enum but doesn't properly handle it
   */
  private static String getTokens(
      MapToolVariableResolver resolver,
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
        getTokenList(resolver, findType, findArgs, true, zone.getAllTokens(), zoneRenderer);

    if (!tokens.isEmpty()) {
      for (Token token : tokens) {
        if (nameOnly) {
          values.add(token.getName());
        } else {
          values.add(token.getId().toString());
        }
      }
    }
    if ("json".equals(delim)) {
      JsonArray jsonArray = new JsonArray();
      for (String val : values) {
        jsonArray.add(val);
      }
      return jsonArray.toString();
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
   * @param identifier the identifier of the token (name, GM name, or GUID).
   * @param zoneNameOrId the name or ID of the zone. If null, check current zone.
   * @return the token, or null if none found.
   */
  public static Token findToken(String identifier, String zoneNameOrId) {
    if (identifier == null) {
      return null;
    }
    if (zoneNameOrId == null || zoneNameOrId.length() == 0) {
      ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
      return zr == null ? null : zr.getZone().resolveToken(identifier);
    } else {
      if (!GUID.isNotGUID(zoneNameOrId)) {
        try {
          final var zr = MapTool.getFrame().getZoneRenderer(GUID.valueOf(zoneNameOrId));
          if (zr != null) {
            Token token = zr.getZone().resolveToken(identifier);
            if (token != null) {
              return token;
            }
          }
        } catch (InvalidGUIDException ignored) {
          // Wasn't a GUID after all. Fall back to looking up by name.
        }
      }

      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        Zone zone = zr.getZone();
        if (zone.getName().equalsIgnoreCase(zoneNameOrId)) {
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
   * Finds the specified token by ID.
   *
   * @param guid the id of the token.
   * @param zoneName the name of the zone. If null, check current zone.
   * @return the token, or null if none found.
   */
  public static Token findToken(GUID guid, String zoneName) {
    if (guid == null) {
      return null;
    }
    if (zoneName == null || zoneName.length() == 0) {
      ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
      return zr == null ? null : zr.getZone().getToken(guid);
    } else {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        Zone zone = zr.getZone();
        if (zone.getName().equalsIgnoreCase(zoneName)) {
          Token token = zone.getToken(guid);
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
