package net.rptools.maptool.client.functions.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.EvalMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Class used to implement Json related functions in MT script.
 */
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

  /**
   * Return the singleton instance for this class.
   * @return the singleton instance.
   */
  public static JSONMacroFunctions getInstance() {
    return instance;
  }



  /** Singleton instance. */
  private static final JSONMacroFunctions instance = new JSONMacroFunctions();

  /** Configuration object for JSONPath. */
  private static final Configuration jaywayConfig =
      Configuration.builder().jsonProvider(new GsonJsonProvider()).build();


  /** The parser used to parse Json strings into an internal representation. */
  private static final JsonParser jsonParser = new JsonParser();

  /**
   * Creates a new <code>JSONMacroFunctions</code> object.
   */
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

    typeConversion = new JsonMTSTypeConversion(jsonParser);
    jsonArrayFunctions = new JsonArrayFunctions(typeConversion);
    jsonObjectFunctions = new JsonObjectFunctions(typeConversion);
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {

    if (functionName.startsWith("json.path.")) {
      return handleJsonPathFunctions(functionName, args);
    }

    switch (functionName) {
      case "json.fromList": {
        FunctionUtil.checkNumberParam(functionName, args, 1, 2);
        String stringList = args.get(0).toString();
        String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_LIST_DELIM;
        return jsonArrayFunctions.fromStringList(stringList, delim);
      }
      case "json.fromStrProp": {
        FunctionUtil.checkNumberParam(functionName, args, 1, 2);
        String stringProp = args.get(0).toString();
        String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_PROP_DELIM;
        return jsonObjectFunctions.fromStrProp(stringProp, delim);
      }
      case "json.toStrProp": {
        FunctionUtil.checkNumberParam(functionName, args, 1, 2);
        JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
        String delim = args.size() > 1 ? args.get(1).toString() : DEFAULT_STRING_PROP_DELIM;
        if (jsonElement.isJsonArray()) {
          return jsonArrayFunctions.toStringProp(jsonElement.getAsJsonArray(), delim);
        } else {
          return jsonObjectFunctions.toStringProp(jsonElement.getAsJsonObject(), delim);
        }
      }
      case "json.append": {
          FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);
          JsonArray jsonArray = jsonArrayFunctions.coerceToJsonArray(args.get(0));
          return jsonArrayFunctions.concatenate(jsonArray, args.subList(1, args.size()));
      }
      case "json.remove": {
        FunctionUtil.checkNumberParam(functionName, args, 2, 2);
        JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
        if (jsonElement.isJsonArray()) {
          int index = FunctionUtil.paramAsInteger(functionName, args, 1, true);
          return jsonArrayFunctions.remove(jsonElement.getAsJsonArray(), index);
        } else {
          return jsonObjectFunctions.remove(jsonElement.getAsJsonObject(), args.get(1).toString());
        }
      }
      case "json.indent":
        FunctionUtil.checkNumberParam(functionName, args, 1, 2);
        return jsonIndent(functionName, args.get(0), args.size() > 1 ? args.get(1) : null);
      case "json.contains": {
        FunctionUtil.checkNumberParam(functionName, args, 2, 2);
        JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
        boolean contains;
        if (jsonElement.isJsonArray()) {
          contains = jsonArrayFunctions.contains(jsonElement.getAsJsonArray(), args.get(1));
        } else {
          contains = jsonObjectFunctions.contains(jsonElement.getAsJsonObject(), args.get(1).toString());
        }
        return contains ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      case "json.sort": {
        FunctionUtil.checkNumberParam(functionName, args, 1, UNLIMITED_PARAMETERS);
        JsonArray jsonArray = FunctionUtil.paramAsJsonArray(functionName, args, 0);
        boolean sortAscending = true;
        if (args.size() > 1) {
          if (args.get(1).toString().startsWith("d")) {
            sortAscending = false;
          }
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
            return jsonArrayFunctions.sorObjectsDescending(jsonArray, fields);
          }
        }
      }
      case "json.shuffle": {
        FunctionUtil.checkNumberParam(functionName, args, 1, 1);
        JsonArray jsonArray = FunctionUtil.paramAsJsonArray(functionName, args, 0);
        return jsonArrayFunctions.shuffle(jsonArray);
      }
      case "json.reverse": {
        FunctionUtil.checkNumberParam(functionName, args, 1, 1);
        JsonArray jsonArray = FunctionUtil.paramAsJsonArray(functionName, args, 0);
        return jsonArrayFunctions.reverse(jsonArray);
      }
      case "json.evaluate": {
        FunctionUtil.enforceTrusted(functionName);
        FunctionUtil.checkNumberParam(functionName, args, 1, 1);
        MapToolVariableResolver resolver = (MapToolVariableResolver) parser.getVariableResolver();
        return jsonEvaluateArg(resolver, args.get(0));
      }
      case "json.isEmpty": {
        FunctionUtil.checkNumberParam(functionName, args, 1, 1);
        JsonElement jsonElement = FunctionUtil.paramAsJson(functionName, args, 0);
        boolean empty;
        if (jsonElement.isJsonArray()) {
          empty = jsonArrayFunctions.isEmpty(jsonElement.getAsJsonArray());
        } else {
          empty = jsonObjectFunctions.isEmpty(jsonElement.getAsJsonObject());
        }
        return empty ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      case "json.equals": {
        FunctionUtil.checkNumberParam(functionName, args, 2, 2);
        JsonElement jsonElement1 = FunctionUtil.paramAsJson(functionName, args, 0);
        JsonElement jsonElement2 = FunctionUtil.paramAsJson(functionName, args, 1);
        return jsonElement1.equals(jsonElement2) ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      case "json.count": {
        FunctionUtil.checkNumberParam(functionName, args, 2, 3);
        int start = 0;
        if (args.size() > 2) {
          start = FunctionUtil.paramAsInteger(functionName, args, 2, true);
        }
        JsonArray jsonArray = FunctionUtil.paramAsJsonArray(functionName, args, 0);

        return BigDecimal.valueOf(jsonArrayFunctions.count(jsonArray, args.get(1), start));
      }
      case "json.indexOf":
        FunctionUtil.checkNumberParam(functionName, args, 2, 3);
        return jsonIndexOf(functionName, args.get(0), args.get(1), args.size() > 2 ? args.get(2) : null);
      case "json.merge":
      case "json.unique":
      case "json.removeAll":
      case "json.union":
      case "json.intersection":
      case "json.difference":
      case "json.isSubset":
      case "json.removeFirst":
      case "json.rolls":
      case "json.objrolls":
    }

    return null; // TODO CDW
  }



  private Object jsonIndexOf(String functionName, Object json, Object val, Object ind)
      throws ParserException {
    int start = 0;

    if (ind != null) {
      if (!(ind instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeN", functionName, ind.toString())
        );
      }
      start = ((BigDecimal) ind).intValue();
    }

    JsonElement jsonElement = asJsonElement(json);
    if (!jsonElement.isJsonArray()) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              json == null ? "NULL" : json.toString(),
              functionName));
    }

    JsonArray jsonArray = jsonElement.getAsJsonArray();
    JsonElement lookFor = asJsonElement(val);
    for (int i = start; i < jsonArray.size(); i++) {
      if (lookFor.equals(jsonArray.get(i))) {
        return BigDecimal.valueOf(i);
      }
    }

    return BigDecimal.ZERO;
  }

  private BigDecimal jsonCount(String functionName, Object json, Object val, Object ind)
      throws ParserException {
    int start = 0;
    int count = 0;

    if (ind != null) {
      if (!(ind instanceof BigDecimal)) {
        throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeN", functionName, ind.toString())
        );
      }
      start = ((BigDecimal) ind).intValue();
    }

    JsonElement jsonElement = asJsonElement(json);
    if (!jsonElement.isJsonArray()) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              json == null ? "NULL" : json.toString(),
              functionName));
    }

    JsonArray jsonArray = jsonElement.getAsJsonArray();
    JsonElement lookFor = asJsonElement(val);
    for (int i = start; i < jsonArray.size(); i++) {
      if (lookFor.equals(jsonArray.get(i))) {
        count++;
      }
    }

    return BigDecimal.valueOf(count);
  }

  private BigDecimal jsonEquals(Object left, Object right) {
    JsonElement leftJson = asJsonElement(left);
    JsonElement rightJson = asJsonElement(right);

    return leftJson.equals(rightJson) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private BigDecimal jsonIsEmpty(Object json) {
    JsonElement jsonElement = asJsonElement(json);
    if (jsonElement.isJsonObject()) {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      return jsonObject.size() > 0 ? BigDecimal.ZERO : BigDecimal.ONE;
    } else if (jsonElement.isJsonArray()) {
      JsonArray jsonArray = jsonElement.getAsJsonArray();
      return jsonArray.size() > 0 ? BigDecimal.ZERO : BigDecimal.ONE;
    } else {
      return jsonElement.getAsString().length() > 0 ? BigDecimal.ZERO : BigDecimal.ONE;
    }
  }

  /**
   * Converts the argument passed to json and runs all the values contained within through the parser
   * to evaluate the contents.
   *
   * @param resolver the variable resolver used when evaluating the contents.
   * @param json the value to be converted into a json object.
   *
   * @return a {@link JsonElement} with the values replaced by those evaluated by the parser.
   *
   * @throws ParserException if any errors occur while parsing.
   */
  private JsonElement jsonEvaluateArg(MapToolVariableResolver resolver,  Object json)
      throws ParserException {
    JsonElement jsonElement = typeConversion.asJsonElement(json);

    return jsonEvaluate(jsonElement, resolver);
  }

  /**
   * Converts the argument passed to json and runs all the values contained within through the
   * parser * to evaluate the contents.
   *
   * @param jsonElement the json to evaluate the contents of.
   * @param resolver the variable resolver used when evaluating the contents.
   *
   * @return a {@link JsonElement} with the values replaced by those evaluated by the parser.
   *
   * @throws ParserException if any errors occur while parsing.
   */
  private JsonElement jsonEvaluate(JsonElement jsonElement, MapToolVariableResolver resolver)
      throws ParserException {
    if (jsonElement.isJsonObject()) {
      JsonObject source = jsonElement.getAsJsonObject();
      JsonObject dest = new JsonObject();

      for (String key : source.keySet()) {
        JsonElement ele = source.get(key);
        if (ele.isJsonObject() || ele.isJsonArray()) {
          dest.add(key, jsonEvaluate(ele, resolver));
        } else {
          Object result = EvalMacroFunctions.evalMacro(resolver, resolver.getTokenInContext(), ele.getAsString());
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
          Object result = EvalMacroFunctions.evalMacro(resolver, resolver.getTokenInContext(), ele.getAsString());
          dest.add(asJsonElement(result));
        }
      }

      return dest;
    }
  }

  private String jsonIndent(String functionName, Object json, Object indent) throws ParserException {
    int ind;
    if (indent instanceof Number) {
      ind = ((Number) indent).intValue();
    } else {
      try {
        ind = Integer.parseInt(indent.toString());
      } catch (NumberFormatException nfe) {
        throw new ParserException(
            I18N.getText("macro.function.json.indentMustBeNumeric", functionName));
      }
    }

    // This is a bit ugly but the GSON library offers no way to specify indentation.
    JsonElement jsonElement = asJsonElement(json);
    if (jsonElement instanceof JsonArray) {
      return JSONArray.fromObject(jsonElement.getAsString()).toString(ind);
    } else if (jsonElement instanceof JsonObject){
      return JSONObject.fromObject(jsonElement.getAsString()).toString(ind);
    } else {
      return jsonElement.getAsString();
    }
  }

  private Object handleJsonPathFunctions(String functionName, List<Object> parameters)
      throws ParserException {

    try {
      switch (functionName) {
        case "json.path.read":
          FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
          return jsonPathRead(parameters.get(0), parameters.get(1).toString());
        case "json.path.add":
          FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
          return jsonPathAdd(parameters.get(0), parameters.get(1).toString(), parameters.get(2));
        case "json.path.set":
          FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
          return jsonPathSet(parameters.get(0), parameters.get(1).toString(), parameters.get(2));
        case "json.path.put":
          FunctionUtil.checkNumberParam(functionName, parameters, 4, 4);
          return jsonPathPut(
              parameters.get(0),
              parameters.get(1).toString(),
              parameters.get(2).toString(),
              parameters.get(3));
        case "json.path.delete":
          FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
          return jsonPathDelete(parameters.get(0), parameters.get(1).toString());
        default:
          throw new ParserException(
              I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    } catch (Exception ex) {
      throw new ParserException(
          I18N.getText("macro.function.json.path", functionName, ex.getLocalizedMessage()));
    }
  }

  private Object jsonPathDelete(Object json, String path) {
    JsonElement jsonElement = asClonedJsonElement(json);

    return JsonPath.parse(jsonElement).delete(path).json();
  }

  private JsonElement jsonPathPut(Object json, String path, String key, Object info) {
    JsonElement jsonElement = asClonedJsonElement(json);
    Object value = asJsonElement(info);

    return JsonPath.parse(jsonElement).put(path, key, value).json();
  }

  private Object jsonPathSet(Object json, String path, Object info) {
    JsonElement jsonElement = asClonedJsonElement(json);
    Object value = asJsonElement(info);

    return JsonPath.parse(jsonElement).set(path, value).json();
  }

  /**
   * This method adds values to a copy of the json object at the specified path.
   *
   * @param json The json object to add the new information to.
   * @param path The path where the information is to be added.
   * @param info The information to be added.
   * @return a copy of the json with the new information added.
   */
  private JsonElement jsonPathAdd(Object json, String path, Object info) {
    JsonElement jsonElement = asClonedJsonElement(json);
    Object value = asJsonElement(info);

    return JsonPath.parse(jsonElement).add(path, value).json();
  }


  /**
   * This method returns the value located at the specified path in the json object.
   *
   * @param json the json object to read the information from.
   * @param path the path to read in the object.
   * @return the return value as a MT Script type.
   */
  private Object jsonPathRead(Object json, String path) {
    JsonElement jsonElement = asJsonElement(json);
    return asScriptType(JsonPath.using(jaywayConfig).parse(jsonElement).read(path));
  }

  private JsonElement jsonParse(String json) {
    return jsonParser.parse(json);
  }

  private List<JsonElement> jsonArrayToList(JsonArray jsonArray) {
    List<JsonElement> list = new ArrayList<>(jsonArray.size());
    for (JsonElement ele : jsonArray) {
      list.add(ele);
    }

    return list;
  }

  private JsonArray listToJsonArray(List<JsonElement> list) {
    JsonArray jsonArray = new JsonArray();
    for (JsonElement ele : list) {
      jsonArray.add(ele);
    }

    return jsonArray;
  }


}
