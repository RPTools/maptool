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
import java.awt.EventQueue;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.functions.json.JsonArrayFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.Function;

/**
 * Represents a callable function within the MapTool client. This class provides the implementation
 * for executing specific functions as part of the client's functionality.
 */
public class CallFunction extends AbstractFunction {

  /** The {@link JSONMacroFunctions} instance used for conversions. */
  private final JSONMacroFunctions jsonMacroFunctions = JSONMacroFunctions.getInstance();

  /** The {@link JsonArrayFunctions} instance used for conversions. */
  private final JsonArrayFunctions jsonArrayFunctions = jsonMacroFunctions.getJsonArrayFunctions();

  /** Create the Object and register the paramater count and function name. */
  public CallFunction() {
    super(
        1,
        UNLIMITED_PARAMETERS,
        "call",
        "call.a",
        "call.deferred",
        "call.deferred.a",
        "call.foreach",
        "call.foreach.a");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    String fnname = functionName.toLowerCase();

    var varResolver = (MapToolVariableResolver) resolver;
    boolean defer = fnname.contains(".deferred");
    switch (fnname) {
      case "call", "call.deferred" -> {
        FunctionUtil.checkNumberParam(functionName, parameters, 1, Function.UNLIMITED_PARAMETERS);
        return doCall(
            varResolver,
            parameters.get(0).toString(),
            paramsToJsonArray(parameters.subList(1, parameters.size())),
            defer);
      }
      case "call.a", "call.deferred.a" -> {
        FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
        var jsonArray = jsonArrayFunctions.coerceToJsonArray(parameters.get(1));
        return doCall(varResolver, parameters.get(0).toString(), jsonArray, defer);
      }
      case "call.foreach" -> {
        FunctionUtil.checkNumberParam(functionName, parameters, 2, Function.UNLIMITED_PARAMETERS);
        var returnArray = new JsonArray();
        for (var param : parameters.subList(1, parameters.size())) {
          JsonArray jsonArray = jsonArrayFunctions.coerceToJsonArray(param);
          returnArray.add(
              jsonMacroFunctions.asJsonElement(
                  doCall(varResolver, parameters.get(0).toString(), jsonArray, defer)));
        }
        return returnArray;
      }
      case "call.foreach.a" -> {
        FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
        var returnArray = new JsonArray();
        var params = jsonArrayFunctions.coerceToJsonArray(parameters.get(1));
        for (var param : params) {
          var jsonParam = jsonArrayFunctions.coerceToJsonArray(param);
          returnArray.add(
              jsonMacroFunctions.asJsonElement(
                  doCall(varResolver, parameters.get(0).toString(), jsonParam, defer)));
        }
        return returnArray;
      }
      case null, default -> {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    }
  }

  /**
   * Call the macro specified.
   *
   * @param resolver The MapToolVariableResolver to use for resolving variables.
   * @param macroName The name of the macro to call.
   * @param args The arguments to pass to the macro as a JsonArray.
   * @param defer If true, the macro will be called later.
   * @return The result of the macro call, for deferred macros this will be an empty string.
   * @throws ParserException if an error occurs during the macro call.
   */
  private Object doCall(
      MapToolVariableResolver resolver, String macroName, JsonArray args, boolean defer)
      throws ParserException {
    Token tokenInContext = resolver.getTokenInContext();

    resolver.setVariable(MapToolLineParser.ARGS_PASSED_AS_VARIABLE_NAME, args);

    if (defer) {
      EventQueue.invokeLater(
          () -> {
            try {
              MapTool.getParser()
                  .runMacro(
                      resolver,
                      tokenInContext,
                      macroName,
                      MapToolLineParser.ARGS_PASSED_AS_VARIABLE_INDICATOR);
            } catch (ParserException e) {
              MapTool.addErrorMessage(e);
            }
          });
      return ""; // Defered macros will return nothing as the excution will be done later
    } else {
      return MapTool.getParser()
          .runMacro(
              resolver,
              tokenInContext,
              macroName,
              MapToolLineParser.ARGS_PASSED_AS_VARIABLE_INDICATOR);
    }
  }

  /**
   * Convert the parameters to a JsonArray.
   *
   * @param parameters the parameters to convert.
   * @return a JsonArray containing the converted parameters.
   */
  private JsonArray paramsToJsonArray(List<Object> parameters) {
    JsonArray jsonArray = new JsonArray();
    for (Object param : parameters) {
      if (param instanceof JsonElement) {
        jsonArray.add((JsonElement) param);
      } else {
        jsonArray.add(jsonMacroFunctions.asJsonElement(param));
      }
    }
    return jsonArray;
  }
}
