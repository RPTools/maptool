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
package net.rptools.maptool.client.functions.json;

import com.google.gson.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.dicelib.expression.ExpressionParser;
import net.rptools.dicelib.expression.Result;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.EvalMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/** Class used to implement Json related functions in MT script. */
public class JSONMacroFunctions extends AbstractFunction {

  /** Object used to convert between json and MTS primitive types. */
  private final JsonMTSTypeConversion typeConversion;

  /** Class used for Json Array related functions */
  private final JsonArrayFunctions jsonArrayFunctions;

  /** Class used for Json Object related functions */
  private final JsonObjectFunctions jsonObjectFunctions;

  /** The default delimiter to use for MT String lists. */
  private static final String DEFAULT_STRING_LIST_DELIM = ",";

  /** The default delimiter to use for MT String Properties. */
  private static final String DEFAULT_STRING_PROP_DELIM = ";";

  /** Json Data Types */
  public enum JSONObjectType {
    OBJECT,
    ARRAY,
    UNKNOWN
  }

  /**
   * Return the singleton instance for this class.
   *
   * @return the singleton instance.
   */
  public static JSONMacroFunctions getInstance() {
    return instance;
  }

  /** Singleton instance. */
  private static final JSONMacroFunctions instance = new JSONMacroFunctions();

  /** Configuration object for JSONPath. */
  private static final Configuration jaywayConfig;

  static {
    jaywayConfig =
        Configuration.builder()
            .jsonProvider(new GsonJsonProvider())
            .mappingProvider(new GsonMappingProvider())
            .build();
  }

  /** Creates a new <code>JSONMacroFunctions</code> object. */
  private JSONMacroFunctions() {
    super(
        1,
        UNLIMITED_PARAMETERS,
        "json.get",
        "json.path.read",
        "json.path.add",
        "json.path.put",
        "json.path.set",
        "json.path.delete",
        "json.type",
        "json.fields",
        "json.length",
        "json.fromList",
        "json.set",
        "json.fromStrProp",
        "json.toStrProp",
        "json.toList",
        "json.toVars",
        "json.append",
        "json.remove",
        "json.indent",
        "json.contains",
        "json.sort",
        "json.shuffle",
        "json.reverse",
        "json.evaluate",
        "json.isEmpty",
        "json.equals",
        "json.count",
        "json.indexOf",
        "json.merge",
        "json.unique",
        "json.removeAll",
        "json.union",
        "json.intersection",
        "json.difference",
        "json.isSubset",
        "json.removeFirst",
        "json.rolls",
        "json.objrolls");

    typeConversion = new JsonMTSTypeConversion();
    jsonArrayFunctions = new JsonArrayFunctions(typeConversion);
    jsonObjectFunctions = new JsonObjectFunctions(typeConversion);
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {

    if (functionName.startsWith("json.path.")) {
      return handleJsonPathFunctions(functionName, args);
    }

    switch (functionName) {
      case "json.type":
        FunctionUtil.checkNumberParam(functionName, args, 1, 1);
        Object potJson = args.get(0);
        if (potJson instanceof JsonArray) {
          return JSONObjectType.ARRAY.name();
        } else if (potJson instanceof JsonObject) {
          return JSONObjectType.OBJECT.name();
        } else {
          String str = potJson.toString().trim();
          if (str.startsWith("{") || str.startsWith("[")) {
            JsonElement json = typeConversion.asJsonElement(str);
            if (json.isJsonArray()) {
              return JSONObjectType.ARRAY.name();
            } else if (json.isJsonObject()) {
              return JSONObjectType.OBJECT.name();
            }
          }
        }
        return JSONObjectType.UNKNOWN.name();
      case "json.length":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 1);
          JsonElement json = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          if (json.isJsonObject()) {
            return jsonObjectFunctions.length(json.getAsJsonObject());
          } else {
            return jsonArrayFunctions.length(json.getAsJsonArray());
          }
        }
      case "json.fields":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 2);
          JsonElement json = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_LIST_DELIM;

          if (json.isJsonObject()) {
            return jsonObjectFunctions.fields(json.getAsJsonObject(), delim);
          } else {
            return jsonArrayFunctions.fields(json.getAsJsonArray(), delim);
          }
        }
      case "json.toVars":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 3);
          JsonElement json = FunctionUtil.paramConvertedToJson(functionName, args, 0);

          if (json.isJsonObject()) {
            String prefix = args.size() > 1 ? args.get(1).toString() : "";
            String suffix = args.size() > 2 ? args.get(2).toString() : "";

            return jsonObjectFunctions.toVars(resolver, json.getAsJsonObject(), prefix, suffix);
          } else {
            FunctionUtil.checkNumberParam(functionName, args, 2, 2);
            String varName = args.get(1).toString();

            return jsonArrayFunctions.toVars(resolver, json.getAsJsonArray(), varName);
          }
        }
      case "json.toList":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 2);
          JsonElement json = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_LIST_DELIM;

          if (json.isJsonArray()) {
            return jsonArrayFunctions.toStringList(json.getAsJsonArray(), delim);
          } else {
            return jsonObjectFunctions.toStringList(json.getAsJsonObject(), delim);
          }
        }
      case "json.fromList":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 2);
          String stringList = args.get(0).toString();
          String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_LIST_DELIM;
          return jsonArrayFunctions.fromStringList(stringList, delim);
        }
      case "json.fromStrProp":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 2);
          String stringProp = args.get(0).toString();
          String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_PROP_DELIM;
          return jsonObjectFunctions.fromStrProp(stringProp, delim);
        }
      case "json.toStrProp":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 2);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_PROP_DELIM;
          if (jsonElement.isJsonArray()) {
            return jsonArrayFunctions.toStringProp(jsonElement.getAsJsonArray(), delim);
          } else {
            return jsonObjectFunctions.toStringProp(jsonElement.getAsJsonObject(), delim);
          }
        }
      case "json.set":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          JsonElement jsonElement;
          if (args.get(0).toString().trim().length() == 0) {
            jsonElement = new JsonObject();
          } else {
            jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
          }
          if (jsonElement.isJsonArray()) {
            return jsonArrayFunctions.set(
                jsonElement.getAsJsonArray(), args.subList(1, args.size()));
          } else {
            return jsonObjectFunctions.set(
                jsonElement.getAsJsonObject(), args.subList(1, args.size()));
          }
        }
      case "json.get":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          JsonElement jsonElement;
          try {
            jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
          } catch (
              ParserException
                  pe) { // If we cant convert it to a JsonArray/JsonObject then treat like array
            // with single value
            jsonElement = new JsonArray();
            jsonElement.getAsJsonArray().add(typeConversion.asJsonElement(args.get(0)));
          }
          if (jsonElement.isJsonArray()) {
            if (args.size() == 2) {
              return jsonArrayFunctions.get(
                  jsonElement.getAsJsonArray(),
                  FunctionUtil.paramAsInteger(functionName, args, 1, true));
            } else {
              return jsonArrayFunctions.get(
                  jsonElement.getAsJsonArray(),
                  FunctionUtil.paramAsInteger(functionName, args, 1, true),
                  FunctionUtil.paramAsInteger(functionName, args, 2, true));
            }
          } else {
            return jsonObjectFunctions.get(
                jsonElement.getAsJsonObject(), args.subList(1, args.size()));
          }
        }
      case "json.append":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          // Special case if first argument is empty string it represents an empty array
          JsonArray jsonArray;
          Object arg = args.get(0);
          if (arg instanceof String && StringUtils.isEmpty(((String) arg))) {
            jsonArray = new JsonArray();
          } else {
            jsonArray = jsonArrayFunctions.coerceToJsonArray(args.get(0));
          }
          return jsonArrayFunctions.concatenate(jsonArray, args.subList(1, args.size()));
        }
      case "json.remove":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, 2);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          if (jsonElement.isJsonArray()) {
            int index = FunctionUtil.paramAsInteger(functionName, args, 1, true);
            return jsonArrayFunctions.remove(jsonElement.getAsJsonArray(), index);
          } else {
            return jsonObjectFunctions.remove(
                jsonElement.getAsJsonObject(), args.get(1).toString());
          }
        }
      case "json.indent":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 2);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          int indentSize = 2;
          if (args.size() > 1) {
            indentSize = FunctionUtil.paramAsInteger(functionName, args, 1, true);
          }
          return jsonIndent(jsonElement, indentSize);
        }
      case "json.contains":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, 2);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          boolean contains;
          if (jsonElement.isJsonArray()) {
            contains = jsonArrayFunctions.contains(jsonElement.getAsJsonArray(), args.get(1));
          } else {
            contains =
                jsonObjectFunctions.contains(jsonElement.getAsJsonObject(), args.get(1).toString());
          }
          return contains ? BigDecimal.ONE : BigDecimal.ZERO;
        }
      case "json.sort":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, UNLIMITED_PARAMETERS);
          JsonArray jsonArray = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          boolean sortAscending = true;
          if (args.size() > 1 && args.get(1).toString().startsWith("d")) {
            sortAscending = false;
          }
          if (args.size() < 3) {
            if (sortAscending) {
              return jsonArrayFunctions.sortAscending(jsonArray);
            } else {
              return jsonArrayFunctions.sortDescending(jsonArray);
            }
          } else {
            var fields = new ArrayList<String>();
            for (int i = 2; i < args.size(); i++) {
              fields.add(args.get(i).toString());
            }
            if (sortAscending) {
              return jsonArrayFunctions.sortObjectsAscending(jsonArray, fields);
            } else {
              return jsonArrayFunctions.sortObjectsDescending(jsonArray, fields);
            }
          }
        }
      case "json.shuffle":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 1);
          JsonArray jsonArray = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          return jsonArrayFunctions.shuffle(jsonArray);
        }
      case "json.reverse":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 1);
          JsonArray jsonArray = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          return jsonArrayFunctions.reverse(jsonArray);
        }
      case "json.evaluate":
        {
          FunctionUtil.blockUntrustedMacro(functionName);
          FunctionUtil.checkNumberParam(functionName, args, 1, 1);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          return jsonEvaluate(jsonElement, (MapToolVariableResolver) resolver);
        }
      case "json.isEmpty":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 1);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);

          boolean empty;
          if (jsonElement.isJsonArray()) {
            empty = jsonArrayFunctions.isEmpty(jsonElement.getAsJsonArray());
          } else {
            empty = jsonObjectFunctions.isEmpty(jsonElement.getAsJsonObject());
          }
          return empty ? BigDecimal.ONE : BigDecimal.ZERO;
        }
      case "json.equals":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, 2);
          JsonElement jsonElement1 = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          JsonElement jsonElement2 = FunctionUtil.paramConvertedToJson(functionName, args, 1);
          return jsonElement1.equals(jsonElement2) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
      case "json.count":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, 3);
          int start = 0;
          if (args.size() > 2) {
            start = FunctionUtil.paramAsInteger(functionName, args, 2, true);
          }
          JsonArray jsonArray = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          JsonElement value = asJsonElement(args.get(1));

          return BigDecimal.valueOf(jsonArrayFunctions.count(jsonArray, value, start));
        }
      case "json.indexOf":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, 3);
          JsonArray jsonArray = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          JsonElement value = asJsonElement(args.get(1));
          int start = 0;
          if (args.size() > 2) {
            start = FunctionUtil.paramAsInteger(functionName, args, 2, true);
          }
          return BigDecimal.valueOf(jsonArrayFunctions.indexOf(jsonArray, value, start));
        }
      case "json.merge":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          JsonElement jsonElement = FunctionUtil.paramConvertedToJson(functionName, args, 0);
          if (jsonElement.isJsonArray()) {
            return jsonArrayFunctions.merge(paramsConvertedToJsonArrays(functionName, args));
          } else {
            return jsonObjectFunctions.merge(paramsAsJsonObjects(functionName, args));
          }
        }
      case "json.unique":
        {
          FunctionUtil.checkNumberParam(functionName, args, 1, 1);
          JsonArray jsonArray = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);

          return jsonArrayFunctions.unique(jsonArray);
        }
      case "json.removeAll":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
          if (jsonElement.isJsonArray()) {
            List<JsonArray> arrays = (paramsConvertedToJsonArrays(functionName, args));
            return jsonArrayFunctions.removeAll(arrays.get(0), arrays.subList(1, arrays.size()));
          } else {
            List<JsonObject> objects = paramsAsJsonObjects(functionName, args);
            return jsonObjectFunctions.removeAll(
                objects.get(0), objects.subList(1, objects.size()));
          }
        }
      case "json.union":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          List<JsonElement> elements = paramsConvertedToJsonElements(functionName, args);
          return jsonArrayFunctions.union(elements);
        }
      case "json.intersection":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          List<JsonElement> elements = paramsConvertedToJsonElements(functionName, args);
          return jsonArrayFunctions.intersection(elements);
        }
      case "json.difference":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          List<JsonElement> elements = paramsConvertedToJsonElements(functionName, args);
          return jsonArrayFunctions.difference(elements);
        }
      case "json.isSubset":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          List<JsonElement> elements = paramsConvertedToJsonElements(functionName, args);
          return jsonArrayFunctions.isSubset(elements) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
      case "json.removeFirst":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          JsonArray removeFrom = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          JsonArray toRemove = FunctionUtil.paramConvertedToJsonArray(functionName, args, 1);
          return jsonArrayFunctions.removeFirst(removeFrom, toRemove);
        }
      case "json.rolls":
        {
          FunctionUtil.checkNumberParam(functionName, args, 2, 3);
          String rollString = args.get(0).toString();
          int outerDim = FunctionUtil.paramAsInteger(functionName, args, 1, true);
          int innerDim;
          if (args.size() > 2) {
            innerDim = FunctionUtil.paramAsInteger(functionName, args, 2, true);
          } else {
            innerDim = 1;
          }

          return jsonRolls(rollString, outerDim, innerDim);
        }
      case "json.objrolls":
        {
          FunctionUtil.checkNumberParam(functionName, args, 3, 3);
          JsonArray names = FunctionUtil.paramConvertedToJsonArray(functionName, args, 0);
          JsonArray stats = FunctionUtil.paramConvertedToJsonArray(functionName, args, 1);
          JsonArray rollArray;
          String rollString;
          boolean isArrayOfRolls;

          try {
            rollArray = FunctionUtil.paramAsJsonArray(functionName, args, 2);
            rollString = "";
            isArrayOfRolls = true;
          } catch (ParserException e) {
            rollString = args.get(2).toString();
            rollArray = new JsonArray();
            isArrayOfRolls = false;
          }

          if (isArrayOfRolls) {
            return jsonObjRolls(names, stats, rollArray);
          } else {
            return jsonObjRolls(names, stats, rollString);
          }
        }
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Creates a {@link JsonObject} with the <code>rollString</code> evaluated for each of the <code>
   * stats</code> creating an inner object for each of the <code>names</code>.
   *
   * @param names The names to use as top level keys in the {@link JsonObject}.
   * @param stats The stats to use as keys in each of the inner {@link JsonObject}s.
   * @param rollString The roll expression.
   * @return The resulting {@link JsonObject}.
   * @throws ParserException if an error occurs parsing the roll expression.
   */
  private JsonObject jsonObjRolls(JsonArray names, JsonArray stats, String rollString)
      throws ParserException {
    JsonArray rollarr = new JsonArray();
    for (int i = 0; i < stats.size(); i++) {
      rollarr.add(rollString);
    }

    return jsonObjRolls(names, stats, rollarr);
  }

  private JsonObject jsonObjRolls(JsonArray names, JsonArray stats, JsonArray rolls)
      throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    if (stats.size() != rolls.size()) {
      throw new ParserException(I18N.getText("macro.function.json.matchingArrayOrRoll"));
    }

    JsonObject outerObj = new JsonObject();

    for (JsonElement name : names) {
      JsonObject innerObj = new JsonObject();
      for (int i = 0; i < stats.size(); i++) {
        JsonElement stat = stats.get(i);
        Result rollRes = parser.evaluate(rolls.get(i).getAsString());
        JsonElement val = asJsonElement(rollRes.getValue());
        innerObj.add(stat.getAsString(), val);
      }
      outerObj.add(name.getAsString(), innerObj);
    }

    return outerObj;
  }

  /**
   * Populates a {@link JsonArray} with the results of rolls performed in the <code>rollString
   * </code>/
   *
   * @param rollString The roll expression to perform.
   * @param amount The amount of times to perform the roll.
   * @param parser The {@link ExpressionParser} used to perform the rolls.
   * @return A {@link JsonArray} containing the rolls.
   * @throws ParserException when an error occurs while trying to perform the rolls.
   */
  private JsonArray jsonRolls(String rollString, int amount, ExpressionParser parser)
      throws ParserException {
    JsonArray array = new JsonArray();
    for (int i = 0; i < amount; i++) {
      array.add(asJsonElement(parser.evaluate(rollString).getValue()));
    }

    return array;
  }

  /**
   * Populates a {@link JsonArray} with the results of rolls performed in the <code>rollString
   * </code>/ If <code>innerDim</code> is <code>1</code> then a 1 dimensional array is returned,
   * otherwise a 2 dimensional array is returned.
   *
   * @param rollString The roll expression to perform.
   * @param outerDim The number of groups.
   * @param innerDim The number of rolls in each group.
   * @return a {@link JsonArray} of the evaluated rolls.
   * @throws ParserException when an error occurs evaluating the roll expression.
   */
  private JsonArray jsonRolls(String rollString, int outerDim, int innerDim)
      throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    if (innerDim == 1) {
      return jsonRolls(rollString, outerDim, parser);
    }

    JsonArray array = new JsonArray();
    for (int i = 0; i < outerDim; i++) {
      array.add(jsonRolls(rollString, innerDim, parser));
    }

    return array;
  }

  /**
   * Returns the parameter list as a list of {@link JsonArray}s.
   *
   * @param functionName The name of the MT Script function that was called.
   * @param params The parameters to extract as {@link JsonArray}s.
   * @return The list of {@link JsonArray}s.
   * @throws ParserException if the parameters can not be converted to {@link JsonArray}s.
   */
  private List<JsonArray> paramsAsJsonArrays(String functionName, List<Object> params)
      throws ParserException {
    List<JsonArray> arrays = new ArrayList<>();
    for (int i = 0; i < params.size(); i++) {
      arrays.add(FunctionUtil.paramAsJsonArray(functionName, params, i));
    }

    return arrays;
  }

  /**
   * Returns the parameter list as a list of {@link JsonArray}s. If the parameter is not a json
   * object/array and is an empty string it will result in a 0 sized JsonArray, otherwise the value
   * will result in a JsonArray containing that value.
   *
   * @param functionName The name of the MT Script function that was called.
   * @param params The parameters to extract as {@link JsonArray}s.
   * @return The list of {@link JsonArray}s.
   * @throws ParserException if the parameters can not be converted to {@link JsonArray}s.
   */
  private List<JsonArray> paramsConvertedToJsonArrays(String functionName, List<Object> params)
      throws ParserException {
    List<JsonArray> arrays = new ArrayList<>();
    for (int i = 0; i < params.size(); i++) {
      arrays.add(FunctionUtil.paramConvertedToJsonArray(functionName, params, i));
    }

    return arrays;
  }

  /**
   * Returns the parameter list as a list of {@link JsonObject}s.
   *
   * @param functionName The name of the MT Script function that was called.
   * @param params The parameters to extract as {@link JsonObject}s.
   * @return The list of {@link JsonObject}s.
   * @throws ParserException if the parameters can not be converted to {@link JsonObject}s.
   */
  private List<JsonObject> paramsAsJsonObjects(String functionName, List<Object> params)
      throws ParserException {
    List<JsonObject> objects = new ArrayList<>();
    for (int i = 0; i < params.size(); i++) {
      objects.add(FunctionUtil.paramAsJsonObject(functionName, params, i));
    }

    return objects;
  }

  /**
   * Returns the parameter list as a list of {@link JsonElement}s.
   *
   * @param functionName The name of the MT Script function that was called.
   * @param params The parameters to extract as {@link JsonElement}s.
   * @return The list of {@link JsonElement}s.
   * @throws ParserException if the parameters can not be converted to {@link JsonElement}s.
   */
  private List<JsonElement> paramsAsJsonElements(String functionName, List<Object> params)
      throws ParserException {
    List<JsonElement> elements = new ArrayList<>();
    for (int i = 0; i < params.size(); i++) {
      elements.add(FunctionUtil.paramAsJson(functionName, params, i));
    }

    return elements;
  }

  /**
   * Returns the parameter list as a list of {@link JsonElement}s. If the parameter is not a json
   * object/array and is an empty string it * will result in a 0 sized JsonArray, otherwise the
   * value will result * in a JsonArray containing that value. *
   *
   * @param functionName The name of the MT Script function that was called.
   * @param params The parameters to extract as {@link JsonElement}s.
   * @return The list of {@link JsonElement}s.
   */
  private List<JsonElement> paramsConvertedToJsonElements(
      String functionName, List<Object> params) {
    List<JsonElement> elements = new ArrayList<>();
    for (int i = 0; i < params.size(); i++) {
      elements.add(FunctionUtil.paramConvertedToJson(functionName, params, i));
    }

    return elements;
  }

  /**
   * Converts the argument passed to json and runs all the values contained within through the
   * parser to evaluate the contents.
   *
   * @param resolver the variable resolver used when evaluating the contents.
   * @param json the value to be converted into a json object.
   * @return a {@link JsonElement} with the values replaced by those evaluated by the parser.
   * @throws ParserException if any errors occur while parsing.
   */
  public JsonElement jsonEvaluateArg(MapToolVariableResolver resolver, Object json)
      throws ParserException {
    JsonElement jsonElement = asJsonElement(json);

    return jsonEvaluate(jsonElement, resolver);
  }

  /**
   * Converts the argument passed to json and runs all the values contained within through the
   * parser * to evaluate the contents.
   *
   * @param jsonElement the json to evaluate the contents of.
   * @param resolver the variable resolver used when evaluating the contents.
   * @return a {@link JsonElement} with the values replaced by those evaluated by the parser.
   * @throws ParserException if any errors occur while parsing.
   */
  public JsonElement jsonEvaluate(JsonElement jsonElement, MapToolVariableResolver resolver)
      throws ParserException {
    if (jsonElement.isJsonObject()) {
      JsonObject source = jsonElement.getAsJsonObject();
      JsonObject dest = new JsonObject();

      for (String key : source.keySet()) {
        JsonElement ele = source.get(key);
        if (ele.isJsonObject() || ele.isJsonArray()) {
          dest.add(key, jsonEvaluate(ele, resolver));
        } else {
          Object result =
              EvalMacroFunctions.evalMacro(
                  resolver, resolver.getTokenInContext(), ele.getAsString());
          dest.add(key, asJsonElement(result));
        }
      }

      return dest;
    } else {
      JsonArray dest = new JsonArray();
      for (JsonElement ele : jsonElement.getAsJsonArray()) {
        if (ele.isJsonObject() || ele.isJsonArray()) {
          dest.add(jsonEvaluate(ele, resolver));
        } else {
          Object result =
              EvalMacroFunctions.evalMacro(
                  resolver, resolver.getTokenInContext(), ele.getAsString());
          dest.add(asJsonElement(result));
        }
      }

      return dest;
    }
  }

  /**
   * Returns a {@link String} version of the json indented using the specified number of spaces.
   *
   * @param json The json to format.
   * @param indent The number of spaces to use for indentation.
   * @return The json as a formatted string.
   */
  public String jsonIndent(JsonElement json, int indent) {

    // This is a bit ugly but the GSON library offers no way to specify indentation.
    if (json.isJsonArray()) {
      return JSONArray.fromObject(json.toString()).toString(indent);
    } else if (json.isJsonObject()) {
      return JSONObject.fromObject(json.toString()).toString(indent);
    } else {
      return json.toString();
    }
  }

  /**
   * This method calls the required functions to perform the json.path.* MT Script functions.
   *
   * @param functionName The name of the MT Script.
   * @param parameters The parameters passed to the MT Script function.
   * @return The result of the execution of the function.
   * @throws ParserException if an error occurs during the execution of the function.
   */
  private Object handleJsonPathFunctions(String functionName, List<Object> parameters)
      throws ParserException {

    try {
      switch (functionName) {
        case "json.path.read":
          {
            FunctionUtil.checkNumberParam(functionName, parameters, 2, 3);
            JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, parameters, 0);
            String strPath = parameters.get(1).toString();
            String strConf = parameters.size() > 2 ? parameters.get(2).toString() : null;
            Configuration config = getConfig(strConf);
            return jsonPathRead(jsonElement, strPath, config);
          }
        case "json.path.add":
          {
            FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
            JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, parameters, 0);
            return jsonPathAdd(jsonElement, parameters.get(1).toString(), parameters.get(2));
          }
        case "json.path.set":
          {
            FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
            JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, parameters, 0);
            return jsonPathSet(jsonElement, parameters.get(1).toString(), parameters.get(2));
          }
        case "json.path.put":
          {
            FunctionUtil.checkNumberParam(functionName, parameters, 4, 4);
            JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, parameters, 0);
            return jsonPathPut(
                jsonElement,
                parameters.get(1).toString(),
                parameters.get(2).toString(),
                parameters.get(3));
          }
        case "json.path.delete":
          {
            FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
            JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, parameters, 0);
            return jsonPathDelete(jsonElement, parameters.get(1).toString());
          }
        default:
          throw new ParserException(
              I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    } catch (Exception ex) {
      throw new ParserException(
          I18N.getText("macro.function.json.path", functionName, ex.getLocalizedMessage()));
    }
  }

  /**
   * Returns a shallow copy of the passed in {@link JsonElement}. If the value is immutable then
   * this method may just return the value itself without making a copy.
   *
   * @param jsonElement The {@link JsonElement} to copy.
   * @return The resulting json data.
   */
  private JsonElement shallowCopy(JsonElement jsonElement) {
    if (jsonElement.isJsonObject()) {
      return jsonObjectFunctions.shallowCopy(jsonElement.getAsJsonObject());
    } else if (jsonElement.isJsonArray()) {
      return jsonArrayFunctions.shallowCopy(jsonElement.getAsJsonArray());
    } else {
      return jsonElement; // Is immutable so no need to return copy,
    }
  }

  /**
   * Returns a copy of the passed in json with the specified path removed.
   *
   * @param json The base {@link JsonElement}.
   * @param path The path to remove.
   * @return The resulting json data.
   */
  private JsonElement jsonPathDelete(JsonElement json, String path) {
    try {
      return JsonPath.using(jaywayConfig).parse(shallowCopy(json)).delete(path).json();
    } catch (PathNotFoundException ex) {
      // Return original json, this is to preserve backwards compatability pre library update
      return json;
    }
  }

  /**
   * Returns a copy of the passed in json with a new value added at the specified path.
   *
   * @param json The base {@link JsonElement}.
   * @param path The path to add the new information at.
   * @param key The new key to be added.
   * @param info The new information to add.
   * @return The resulting json data.
   */
  private JsonElement jsonPathPut(JsonElement json, String path, String key, Object info) {
    Object value = asJsonElement(info);

    try {
      return JsonPath.using(jaywayConfig).parse(shallowCopy(json)).put(path, key, value).json();
    } catch (PathNotFoundException ex) {
      // Return original json, this is to preserve backwards compatability pre library update
      return json;
    }
  }

  /**
   * Returns a copy of the passed in json with a new value set at the specified path.
   *
   * @param json The base {@link JsonElement}.
   * @param path The path to change the new information at.
   * @param info The new information to change to.
   * @return The resulting json data.
   */
  private JsonElement jsonPathSet(JsonElement json, String path, Object info) {
    Object value = asJsonElement(info);

    try {
      return JsonPath.using(jaywayConfig).parse(shallowCopy(json)).set(path, value).json();
    } catch (PathNotFoundException ex) {
      // Return original json, this is to preserve backwards compatability pre library update
      return json;
    }
  }

  /**
   * This method adds values to a copy of the json object at the specified path.
   *
   * @param json The json object to add the new information to.
   * @param path The path where the information is to be added.
   * @param info The information to be added.
   * @return a copy of the json with the new information added.
   */
  private JsonElement jsonPathAdd(JsonElement json, String path, Object info)
      throws ParserException {
    JsonElement jsonElement = typeConversion.asClonedJsonElement(json);
    Object value = asJsonElement(info);

    return JsonPath.using(jaywayConfig).parse(jsonElement).add(path, value).json();
  }

  /**
   * This method returns the value located at the specified path in the json object.
   *
   * @param json the json object to read the information from.
   * @param path the path to read in the object.
   * @return the return value as a MT Script type.
   */
  private Object jsonPathRead(JsonElement json, String path, Configuration config) {
    JsonElement jsonElement = asJsonElement(json);
    return typeConversion.asScriptType(JsonPath.using(config).parse(jsonElement).read(path));
  }

  /**
   * Returns the object used to perform manipulations on {@link JsonArray}s.
   *
   * @return the object used to perform manipulations on {@link JsonArray}s.
   */
  public JsonArrayFunctions getJsonArrayFunctions() {
    return jsonArrayFunctions;
  }

  /**
   * Returns the object used to perform manipulations on {@link JsonObject}s.
   *
   * @return the object used to perform manipulations on {@link JsonObject}s.
   */
  public JsonObjectFunctions getJsonObjectFunctions() {
    return jsonObjectFunctions;
  }

  /**
   * This method returns the object passed in as the appropriate json type.
   *
   * @param o the object to convert.
   * @return the json representation..
   */
  public JsonElement asJsonElement(Object o) {
    return typeConversion.asJsonElement(o);
  }

  /**
   * Converts a <code>String</code> to a {@link JsonPrimitive}.
   *
   * @param string the String to convert.
   * @return the converted value.
   */
  public JsonPrimitive convertPrimitiveFromString(String string) {
    return typeConversion.convertPrimitiveFromString(string);
  }

  /**
   * Converts a JsonElement to a String that is safe to be returned to MTScript
   *
   * @param element the JsonElement to convert.
   * @return The converted String.
   */
  public String jsonToScriptString(JsonElement element) {
    return typeConversion.jsonToScriptString(element);
  }

  /**
   * Create the Jayway configuration from a String
   *
   * @param strConf The String containing the configuration options
   * @return the Jayway Configuration
   */
  private static Configuration getConfig(String strConf) {
    Configuration config = jaywayConfig;
    if (strConf == null) {
      return config;
    }
    strConf = strConf.toUpperCase();
    if (strConf.contains("AS_PATH_LIST")) {
      config = config.addOptions(Option.AS_PATH_LIST);
    }
    if (strConf.contains("DEFAULT_PATH_LEAF_TO_NULL")) {
      config = config.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
    }
    if (strConf.contains("SUPPRESS_EXCEPTIONS")) {
      config = config.addOptions(Option.SUPPRESS_EXCEPTIONS);
    }
    if (strConf.contains("ALWAYS_RETURN_LIST")) {
      config = config.addOptions(Option.ALWAYS_RETURN_LIST);
    }
    if (strConf.contains("REQUIRE_PROPERTIES")) {
      config = config.addOptions(Option.REQUIRE_PROPERTIES);
    }
    return config;
  }

  /**
   * Converts a JsonElement into a MT Script type.
   *
   * @param jsonElement The json element to convert;
   * @return The MT Script value
   */
  public Object asScriptType(JsonElement jsonElement) {
    return typeConversion.asScriptType(jsonElement);
  }
}
