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

import com.google.gson.JsonNull;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.Function;

public class IlluminationFunctions extends AbstractFunction {
  private static final IlluminationFunctions instance = new IlluminationFunctions();

  private IlluminationFunctions() {
    super(0, Function.UNLIMITED_PARAMETERS, "getIllumination");
  }

  public static IlluminationFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("getIllumination")) {
      return BigDecimal.valueOf(getIllumination(functionName, parameters));
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  private int getIllumination(String functionName, List<Object> parameters) throws ParserException {
    FunctionUtil.blockUntrustedMacro(functionName);
    FunctionUtil.checkNumberParam(functionName, parameters, 2, 4);

    final var x = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
    final var y = FunctionUtil.paramAsInteger(functionName, parameters, 1, false);
    final var point = new Point2D.Double(x, y);

    final var renderer = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 2);
    final var playerView = getPlayerView(functionName, renderer, parameters, 3);

    for (final var lumensLevel :
        renderer.getZoneView().getDisjointObscuredLumensLevels(playerView)) {
      if (lumensLevel.darknessArea().contains(point)) {
        return -lumensLevel.lumensStrength();
      } else if (lumensLevel.lightArea().contains(point)) {
        return lumensLevel.lumensStrength();
      }
    }

    return 0;
  }

  /**
   * Builds a player view for `getIllumination()`
   *
   * @param functionName
   * @param renderer
   * @param parameters
   * @param tokenListIndex
   * @return If the token list is not provided, the current view for the zone. If `json.null` is
   *     provided, a non-token view. If a token ID list is provided, a token view containing the
   *     identified tokens. is returned. If the token ID list is empty, a token view with no in it.
   * @throws ParserException
   */
  private PlayerView getPlayerView(
      String functionName, ZoneRenderer renderer, List<Object> parameters, int tokenListIndex)
      throws ParserException {
    if (parameters.size() <= tokenListIndex) {
      return renderer.getPlayerView();
    }

    final var parameter = parameters.get(tokenListIndex);
    if (parameter instanceof JsonNull) {
      // Explicitly requesting without token view.
      return new PlayerView(MapTool.getPlayer().getEffectiveRole());
    }

    // Tokens for the view passed as a JSON array.
    final var jsonList =
        JSONMacroFunctions.getInstance().asJsonElement(parameters.get(3).toString());
    if (!jsonList.isJsonArray()) {
      // Whoops, we need an array.
      throw new ParserException(I18N.getText("macro.function.general.argumentTypeA", functionName));
    }
    final var jsonArray = jsonList.getAsJsonArray();

    final var tokens = new ArrayList<Token>();
    for (final var element : jsonArray) {
      final var identifier = JSONMacroFunctions.getInstance().jsonToScriptString(element);
      final var token = renderer.getZone().resolveToken(identifier);
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", functionName, identifier));
      }

      tokens.add(token);
    }

    return new PlayerView(MapTool.getPlayer().getEffectiveRole(), tokens);
  }
}
