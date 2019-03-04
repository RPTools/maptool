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
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Direction;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class TokenLightFunctions extends AbstractFunction {
  private static final TokenLightFunctions instance = new TokenLightFunctions();

  private TokenLightFunctions() {
    super(0, 3, "hasLightSource", "clearLights", "setLight", "getLights");
  }

  public static TokenLightFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    final Token tokenInContext =
        ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
    if (tokenInContext == null) {
      throw new ParserException(
          I18N.getText("macro.function.general.noImpersonated", functionName));
    }
    if (functionName.equals("hasLightSource")) {
      return hasLightSource(tokenInContext, parameters) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equals("clearLights")) {
      tokenInContext.clearLightSources();
      MapTool.serverCommand()
          .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), tokenInContext);
      MapTool.getFrame().updateTokenTree();
      return "";
    }
    if (functionName.equals("setLight")) {
      if (parameters.size() < 3) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 3, parameters.size()));
      }
      if (!(parameters.get(2) instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN",
                functionName,
                3,
                parameters.get(2).toString()));
      }
      return setLight(
          tokenInContext,
          parameters.get(0).toString(),
          parameters.get(1).toString(),
          (BigDecimal) parameters.get(2));
    }
    if (functionName.equals("getLights")) {
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      return getLights(
          tokenInContext, parameters.size() > 0 ? parameters.get(0).toString() : null, delim);
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
  private String getLights(Token token, String category, String delim) {
    ArrayList<String> lightList = new ArrayList<String>();

    if (category == null || category.equals("*")) {
      for (String catName : MapTool.getCampaign().getLightSourcesMap().keySet()) {
        for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(catName).values()) {
          if (token.hasLightSource(ls)) {
            lightList.add(ls.getName());
          }
        }
      }
    } else {
      for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(category).values()) {
        if (token.hasLightSource(ls)) {
          lightList.add(ls.getName());
        }
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

    for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(category).values()) {
      if (ls.getName().equals(name)) {
        found = true;
        if (val.equals(BigDecimal.ZERO)) {
          token.removeLightSource(ls);
        } else {
          token.addLightSource(ls, Direction.CENTER);
        }
      }
    }
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = renderer.getZone();
    zone.putToken(token);
    MapTool.serverCommand().putToken(zone.getId(), token);
    MapTool.getFrame().updateTokenTree();
    renderer.flushLight();

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
  private boolean hasLightSource(Token token, List<Object> parameters) {
    if (parameters.size() < 1) {
      return token.hasLightSources();
    }
    String category = parameters.get(0).toString();

    if (parameters.size() < 2) {
      for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(category).values()) {
        if (token.hasLightSource(ls)) {
          return true;
        }
      }
      return false;
    }
    String name = parameters.get(1).toString();

    for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(category).values()) {
      if (ls.getName().equals(name)) {
        if (token.hasLightSource(ls)) {
          return true;
        }
      }
    }
    return false;
  }
}
