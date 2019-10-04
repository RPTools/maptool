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
import java.util.List;
import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Direction;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class TokenLightFunctions extends AbstractFunction {
  private static final TokenLightFunctions instance = new TokenLightFunctions();

  private TokenLightFunctions() {
    super(0, 5, "hasLightSource", "clearLights", "setLight", "getLights");
  }

  public static TokenLightFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    MapToolVariableResolver resolver = (MapToolVariableResolver) parser.getVariableResolver();

    if (functionName.equalsIgnoreCase("hasLightSource")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 4);

      String type = (parameters.size() > 0) ? parameters.get(0).toString() : "*";
      String name = (parameters.size() > 1) ? parameters.get(1).toString() : "*";
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return hasLightSource(token, type, name) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equalsIgnoreCase("clearLights")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, "clearLightSources");
      MapTool.getFrame().updateTokenTree();
      return "";
    }
    if (functionName.equalsIgnoreCase("setLight")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 3, 5);

      String type = parameters.get(0).toString();
      String name = parameters.get(1).toString();
      BigDecimal value = FunctionUtil.paramAsBigDecimal(functionName, parameters, 2, false);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 3, 4);
      return setLight(token, type, name, value);
    }
    if (functionName.equalsIgnoreCase("getLights")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 4);

      String type = parameters.size() > 0 ? parameters.get(0).toString() : "*";
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return getLights(token, type, delim);
    }
    return null;
  }

  /**
   * Gets the names of the light sources that are on.
   *
   * @param token The token to get the light sources for.
   * @param category The category to get the light sources for, if null then the light sources for
   *     all categories will be returned.
   * @param delim the delimiter for the list.
   * @return a string list containing the lights that are on.
   * @throws ParserException if the light type can't be found.
   */
  private String getLights(Token token, String category, String delim) throws ParserException {
    ArrayList<String> lightList = new ArrayList<String>();
    Map<String, Map<GUID, LightSource>> lightSourcesMap =
        MapTool.getCampaign().getLightSourcesMap();

    if (category == null || category.equals("*")) {
      for (String catName : lightSourcesMap.keySet()) {
        for (LightSource ls : lightSourcesMap.get(catName).values()) {
          if (token.hasLightSource(ls)) {
            lightList.add(ls.getName());
          }
        }
      }
    } else {
      if (lightSourcesMap.containsKey(category)) {
        for (LightSource ls : lightSourcesMap.get(category).values()) {
          if (token.hasLightSource(ls)) {
            lightList.add(ls.getName());
          }
        }
      } else {
        throw new ParserException(
            I18N.getText("macro.function.tokenLight.unknownLightType", "getLights", category));
      }
    }
    if ("json".equals(delim)) {
      return JSONArray.fromObject(lightList).toString();
    } else {
      return StringFunctions.getInstance().join(lightList, delim);
    }
  }

  /**
   * Sets the light value for a token.
   *
   * @param token the token to set the light for.
   * @param category the category of the light source.
   * @param name the name of the light source.
   * @param val the value to set for the light source, 0 for off non 0 for on.
   * @return 0 if the light was not found, otherwise 1;
   * @throws ParserException if the light type can't be found.
   */
  private BigDecimal setLight(Token token, String category, String name, BigDecimal val)
      throws ParserException {
    boolean found = false;
    Map<String, Map<GUID, LightSource>> lightSourcesMap =
        MapTool.getCampaign().getLightSourcesMap();

    if (lightSourcesMap.containsKey(category)) {
      for (LightSource ls : lightSourcesMap.get(category).values()) {
        if (ls.getName().equals(name)) {
          found = true;
          if (val.equals(BigDecimal.ZERO)) {
            MapTool.serverCommand().updateTokenProperty(token, "removeLightSource", ls);
          } else {
            MapTool.serverCommand()
                .updateTokenProperty(token, "addLightSource", ls, Direction.CENTER);
          }
        }
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.tokenLight.unknownLightType", "setLights", category));
    }
    MapTool.getFrame().updateTokenTree();
    token.getZoneRenderer().flushLight();

    return found ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Checks to see if the token has a light source. The token is checked to see if it has a light
   * source with the name in the second parameter from the category in the first parameter. A "*"
   * for category indicates all categories are checked; a "*" for name indicates all names are
   * checked.
   *
   * @param token the token to check.
   * @param category the type of light to check.
   * @param name the name of the light to check.
   * @return true if the token has the light source.
   * @throws ParserException if the light type can't be found.
   */
  private boolean hasLightSource(Token token, String category, String name) throws ParserException {
    if (category.equals("*") && name.equals("*")) {
      return token.hasLightSources();
    }

    Map<String, Map<GUID, LightSource>> lightSourcesMap =
        MapTool.getCampaign().getLightSourcesMap();

    if (category.equals("*")) {
      for (String catName : lightSourcesMap.keySet()) {
        for (LightSource ls : lightSourcesMap.get(catName).values()) {
          if (ls.getName().equals(name) || name.equals("*")) {
            if (token.hasLightSource(ls)) {
              return true;
            }
          }
        }
      }
    } else {
      if (lightSourcesMap.containsKey(category)) {
        for (LightSource ls : lightSourcesMap.get(category).values()) {
          if (ls.getName().equals(name) || name.equals("*")) {
            if (token.hasLightSource(ls)) {
              return true;
            }
          }
        }
      } else {
        throw new ParserException(
            I18N.getText("macro.function.tokenLight.unknownLightType", "hasLightSource", category));
      }
    }

    return false;
  }
}
