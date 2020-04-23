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

import static java.util.stream.Collectors.toSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.TerrainModifierOperation;
import net.rptools.maptool.model.Token.Update;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenTerrainModifierFunctions extends AbstractFunction {

  /** The singleton instance. */
  private static final TokenTerrainModifierFunctions instance = new TokenTerrainModifierFunctions();

  private TokenTerrainModifierFunctions() {
    super(0, 3, "setTerrainModifier", "getTerrainModifier");
  }

  /**
   * Gets the instance of Terrain Modifier.
   *
   * @return the instance.
   */
  public static TokenTerrainModifierFunctions getInstance() {
    return instance;
  }

  /**
   * @param parser the MapTool parser.
   * @param functionName the name of the function.
   * @param param the list of parameters.
   * @return BigDecimal terrain modifier value.
   * @throws ParserException if unknown function name or incorrect function arguments.
   */
  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> param)
      throws ParserException {
    if (functionName.equals("getTerrainModifier")) {
      return getTerrainModifierInfo(parser, param);
    } else if (functionName.equals("setTerrainModifier")) {
      return setTerrainModifier(parser, param);
    } else {
      throw new ParserException(I18N.getText("macro.function.general.unknownFunction"));
    }
  }

  /**
   * Gets the Terrain Modifier
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the value of the terrain modifier
   * @throws ParserException if an error occurs.
   */
  private Object getTerrainModifierInfo(Parser parser, List<Object> args) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "getTerrainModifier"));
    }

    FunctionUtil.checkNumberParam("getTerrainModifier", args, 0, 3);

    String firstParameter = "";
    if (args.size() > 0) {
      firstParameter = FunctionUtil.paramAsString("getTerrainModifier", args, 0, false);
    }

    if ("json".equals(firstParameter.toLowerCase())) {
      Token token = FunctionUtil.getTokenFromParam(parser, "getTerrainModifier", args, 1, 2);
      return getTerrainModifierInfo(token);
    } else {
      Token token = FunctionUtil.getTokenFromParam(parser, "getTerrainModifier", args, 0, 1);
      return token.getTerrainModifier();
    }
  }

  /**
   * Gets the Terrain Modifier, Operation, and ignored terrain operations
   *
   * @param token The token to check.
   * @return the terrain modifier value and operations as a json object.
   */
  private Object getTerrainModifierInfo(Token token) {
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();

    for (TerrainModifierOperation terrainModifiersIgnored : token.getTerrainModifiersIgnored()) {
      jsonArray.add(terrainModifiersIgnored.toString());
    }

    jsonObject.addProperty("terrainModifier", token.getTerrainModifier());
    jsonObject.addProperty(
        "terrainModifierOperation", token.getTerrainModifierOperation().toString());
    jsonObject.add("terrainModifiersIgnored", jsonArray);

    return jsonObject;
  }

  /**
   * Sets the Terrain Modifier
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the value the terrain modifier will be set to
   * @throws ParserException if an error occurs.
   */
  private Object setTerrainModifier(Parser parser, List<Object> args) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "setTerrainModifier"));
    }

    FunctionUtil.checkNumberParam("setTerrainModifier", args, 1, 3);

    Token token = FunctionUtil.getTokenFromParam(parser, "setTerrainModifier", args, 1, 2);
    Double terrainModifier = token.getTerrainModifier();

    if (args.get(0) instanceof BigDecimal) {
      terrainModifier = FunctionUtil.paramAsDouble("setTerrainModifier", args, 0, false);
    } else {
      // Set Terrain Modifier if passed in...
      JsonObject json = FunctionUtil.paramAsJsonObject("setTerrainModifier", args, 0);

      JsonElement terrainModifierElement = json.get("terrainModifier");
      if (terrainModifierElement != null) {
        terrainModifier = terrainModifierElement.getAsDouble();
      }

      // Set Terrain Modifier Operation if passed in...
      JsonElement terrainModifierOperationPrimitive = json.get("terrainModifierOperation");

      try {
        if (terrainModifierOperationPrimitive != null) {
          TerrainModifierOperation terrainModifierOperation =
              TerrainModifierOperation.valueOf(terrainModifierOperationPrimitive.getAsString());

          MapTool.serverCommand()
              .updateTokenProperty(
                  token, Update.setTerrainModifierOperation, terrainModifierOperation);
        }
      } catch (java.lang.IllegalArgumentException iae) {
        throw new ParserException(
            I18N.getText(
                "macro.function.parse.enum.illegalArgumentType",
                "setTerrainModifier",
                terrainModifierOperationPrimitive.getAsString(),
                Arrays.asList(TerrainModifierOperation.values()).stream()
                    .map(value -> value.toString())
                    .collect(Collectors.joining(", "))));
      }

      // Set Terrain Modifiers Ignored if passed in...
      JsonArray jsonArray = json.getAsJsonArray("terrainModifiersIgnored");

      if (jsonArray != null) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<TerrainModifierOperation>>() {}.getType();

        List<TerrainModifierOperation> ignoredTerrainOperationsList =
            gson.fromJson(jsonArray, type);
        Set<TerrainModifierOperation> ignoredTerrainOperationsSet =
            ignoredTerrainOperationsList.stream()
                .filter(operation -> operation != null)
                .collect(toSet());

        MapTool.serverCommand()
            .updateTokenProperty(
                token, Update.setTerrainModifiersIgnored, ignoredTerrainOperationsSet);
      }
    }

    // Finally, set and return the terrainModifier
    MapTool.serverCommand().updateTokenProperty(token, Update.setTerrainModifier, terrainModifier);

    return terrainModifier;
  }
}
