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

    if (functionName.equals("hasLightSource")) {
      checkNumberOfParameters(functionName, parameters, 0, 4);
      Token token = getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return hasLightSource(token, parameters) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equals("clearLights")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, "clearLightSources");
      MapTool.getFrame().updateTokenTree();
      return "";
    }
    if (functionName.equals("setLight")) {
      checkNumberOfParameters(functionName, parameters, 3, 5);
      Token token = getTokenFromParam(resolver, functionName, parameters, 3, 4);

      if (!(parameters.get(2) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN",
                functionName,
                3,
                parameters.get(2).toString()));
      }
      return setLight(
          token,
          parameters.get(0).toString(),
          parameters.get(1).toString(),
          (BigDecimal) parameters.get(2));
    }
    if (functionName.equals("getLights")) {
      checkNumberOfParameters(functionName, parameters, 0, 4);
      Token token = getTokenFromParam(resolver, functionName, parameters, 2, 3);

      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      return getLights(token, parameters.size() > 0 ? parameters.get(0).toString() : null, delim);
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
   * @param token The token to set the light for.
   * @param category the category of the light source.
   * @param name The name of the light source.
   * @param val The value to set for the light source, 0 for off non 0 for on.
   * @return 0 If the light was not found, otherwise 1;
   * @throws ParserException
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
   * Checks to see if the token has a light source.
   *
   * @param token The token to check.
   * @param parameters The parameters for light sources to check.
   * @return true if the token has the light source. If there are no parameters then the token is
   *     checked for any light source. If there is one parameter then the token is checked to see if
   *     it contains a light source for that category. If there are two parameters the token is
   *     checked to see if it has a light source with the name in the second parameter from the
   *     category in the first parameter.
   */
  private boolean hasLightSource(Token token, List<Object> parameters) throws ParserException {
    String category = (parameters.size() > 0) ? parameters.get(0).toString() : "*";
    String name = (parameters.size() > 1) ? parameters.get(1).toString() : "*";

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

  /**
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  private void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
  }

  /**
   * Gets the token from the specified index or returns the token in context. This method will check
   * the list size before trying to retrieve the token so it is safe to use for functions that have
   * the token as a optional argument.
   *
   * @param res the variable resolver
   * @param functionName The function name (used for generating exception messages).
   * @param param The parameters for the function.
   * @param indexToken The index to find the token at.
   * @param indexMap The index to find the map name at. If -1, use current map instead.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  private Token getTokenFromParam(
      MapToolVariableResolver res,
      String functionName,
      List<Object> param,
      int indexToken,
      int indexMap)
      throws ParserException {

    String mapName =
        indexMap >= 0 && param.size() > indexMap ? param.get(indexMap).toString() : null;
    Token token;
    if (param.size() > indexToken) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", functionName));
      }
      token = FindTokenFunctions.findToken(param.get(indexToken).toString(), mapName);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken",
                functionName,
                param.get(indexToken).toString()));
      }
    } else {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
      }
    }
    return token;
  }
}
