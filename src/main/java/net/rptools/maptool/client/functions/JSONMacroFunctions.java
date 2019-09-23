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

import com.jayway.jsonpath.*;
import java.math.BigDecimal;
import java.util.*;
import net.rptools.common.expression.ExpressionParser;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.*;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

@SuppressWarnings("unchecked")
public class JSONMacroFunctions extends AbstractFunction {
  public enum JSONObjectType {
    OBJECT,
    ARRAY,
    UNKNOWN
  }

  private static final JSONMacroFunctions instance = new JSONMacroFunctions();

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
  }

  public static JSONMacroFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    if (functionName.equals("json.fromList")) {
      String delim = ",";
      if (parameters.size() > 1) {
        delim = parameters.get(1).toString();
      }
      return fromStrList(parameters.get(0).toString(), delim);
    }

    if (functionName.equals("json.path.read")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String jsonStr = parameters.get(0).toString();
      String path = parameters.get(1).toString();

      try {
        return JsonPath.parse(jsonStr).read(path);
      } catch (Exception e) {
        throw new ParserException(
            I18N.getText("macro.function.json.path", functionName, e.getLocalizedMessage()));
      }
    }

    if (functionName.equals("json.path.add")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
      String jsonStr = parameters.get(0).toString();
      String path = parameters.get(1).toString();
      Object value = parameters.get(2);
      Object json = convertToJSON(value.toString());
      if (json != null) value = json; // to prevent quotes getting turned into \" and \"

      try {
        return JsonPath.parse(jsonStr).add(path, value).jsonString(); // add element to array
      } catch (Exception e) {
        throw new ParserException(
            I18N.getText("macro.function.json.path", functionName, e.getLocalizedMessage()));
      }
    }

    if (functionName.equals("json.path.set")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
      String jsonStr = parameters.get(0).toString();
      String path = parameters.get(1).toString();
      Object value = parameters.get(2);
      Object json = convertToJSON(value.toString());
      if (json != null) value = json;

      try {
        return JsonPath.parse(jsonStr).set(path, value).jsonString(); // set element in array/object
      } catch (Exception e) {
        throw new ParserException(
            I18N.getText("macro.function.json.path", functionName, e.getLocalizedMessage()));
      }
    }

    if (functionName.equals("json.path.put")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 4, 4);
      String jsonStr = parameters.get(0).toString();
      String path = parameters.get(1).toString();
      String key = parameters.get(2).toString();
      Object value = parameters.get(3);
      Object json = convertToJSON(value.toString());
      if (json != null) value = json;

      try {
        return JsonPath.parse(jsonStr).put(path, key, value).jsonString(); // add value in object
      } catch (Exception e) {
        throw new ParserException(
            I18N.getText("macro.function.json.path", functionName, e.getLocalizedMessage()));
      }
    }

    if (functionName.equals("json.path.delete")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String jsonStr = parameters.get(0).toString();
      String path = parameters.get(1).toString();

      try {
        return JsonPath.parse(jsonStr).delete(path).jsonString(); // delete path
      } catch (Exception e) {
        throw new ParserException(
            I18N.getText("macro.function.json.path", functionName, e.getLocalizedMessage()));
      }
    }

    if (functionName.equals("json.fromStrProp")) {
      String delim = ";";
      if (parameters.size() > 1) {
        delim = parameters.get(1).toString();
      }
      return fromStrProp(parameters.get(0).toString(), delim);
    }

    if (functionName.equalsIgnoreCase("json.toVars")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      JSONObject jsonObject = FunctionUtil.paramAsJsonObject(functionName, parameters, 0);
      String prefix = parameters.size() > 1 ? parameters.get(1).toString() : "";
      String suffix = parameters.size() > 2 ? parameters.get(2).toString() : "";

      JSONArray jsonNames = new JSONArray();
      for (Object keyStr : jsonObject.keySet()) {
        // add prefix and suffix
        String varName = prefix + keyStr.toString().trim() + suffix;
        // replace spaces by underscores
        varName = varName.replaceAll("\\s", "_");
        // delete special characters other than "." & "_" in var name
        varName = varName.replaceAll("[^a-zA-Z0-9._]", "");

        if (!varName.equals("")) {
          Object value = jsonObject.get(keyStr);
          parser.setVariable(varName, value);
          jsonNames.add(varName);
        }
      }
      return jsonNames;
    }

    if (functionName.equals("json.set")) {
      if (parameters.size() < 3) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 3, parameters.size()));
      }
      return JSONSet(asJSON(parameters.get(0)), parameters);
    }

    if (functionName.equals("json.length")) {
      return JSONLength(asJSON(parameters.get(0)));
    }

    if (functionName.equals("json.fields")) {
      String delim = ",";
      if (parameters.size() > 1) {
        delim = parameters.get(1).toString();
      }
      return JSONFields(asJSON(parameters.get(0)), delim);
    }

    if (functionName.equals("json.type")) {
      return getJSONObjectType(parameters.get(0)).toString();
    }

    if (functionName.equals("json.toList")) {
      String delim = ",";
      if (parameters.size() > 1) {
        delim = parameters.get(1).toString();
      }
      return JSONToList(asJSON(parameters.get(0)), delim);
    }

    if (functionName.equals("json.toStrProp")) {
      String delim = ";";
      if (parameters.size() > 1) {
        delim = parameters.get(1).toString();
      }
      return JSONToStrProp(asJSON(parameters.get(0)), delim);
    }

    if (functionName.equals("json.get")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONGet(asJSON(parameters.get(0)), parameters.subList(1, parameters.size()));
    }

    if (functionName.equals("json.append")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONAppend(asJSON(parameters.get(0)), parameters);
    }

    if (functionName.equals("json.remove")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONDelete(asJSON(parameters.get(0)), parameters.get(1).toString());
    }

    if (functionName.equals("json.indent")) {
      int indent = 4;
      if (parameters.size() > 1) {
        try {
          indent = Integer.parseInt(parameters.get(1).toString());
        } catch (Exception e) {
          // Do nothing as we will just use the default.
        }
      }
      return JSONIndent(asJSON(parameters.get(0)), indent);
    }

    if (functionName.equals("json.contains")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONContains(asJSON(parameters.get(0)), parameters.get(1).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }

    if (functionName.equals("json.sort")) {
      if (parameters.size() > 2) {
        List<String> fields = new ArrayList<String>(parameters.size() - 2);
        for (Object o : parameters.subList(2, parameters.size())) {
          if (o instanceof String) fields.add((String) o);
          else
            throw new ParserException(
                I18N.getText(
                    "macro.function.json.unknownType",
                    o == null ? "NULL" : o.toString(),
                    functionName));
        }
        return JSONSort(
            asJSON(parameters.get(0)),
            parameters.size() > 1 ? parameters.get(1).toString() : "ascending",
            fields);
      } else {
        return JSONSort(
            asJSON(parameters.get(0)),
            parameters.size() > 1 ? parameters.get(1).toString() : "ascending",
            null);
      }
    }

    if (functionName.equals("json.shuffle")) {
      return JSONShuffle(asJSON(parameters.get(0)));
    }

    if (functionName.equals("json.reverse")) {
      return JSONReverse(asJSON(parameters.get(0)));
    }

    if (functionName.equals("json.evaluate")) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }
      Object j = asJSON(parameters.get(0));
      if (!(j instanceof JSONObject) && !(j instanceof JSONArray)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.unknownType",
                j == null ? "NULL" : j.toString(),
                "json.evaluate"));
      }
      // Create a new object or array so that we preserve immutability for macros.
      Object json;
      if (j instanceof JSONObject) {
        json = JSONObject.fromObject(j);
      } else {
        json = JSONArray.fromObject(j);
      }
      return JSONEvaluate((MapToolVariableResolver) parser.getVariableResolver(), json);
    }

    if (functionName.equals("json.isEmpty")) {
      Object j = asJSON(parameters.get(0));
      if (j instanceof JSONObject) {
        return ((JSONObject) j).isEmpty() ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      if (j instanceof JSONArray) {
        return ((JSONArray) j).isEmpty() ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      if (j instanceof String) {
        return j.toString().length() == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      return BigDecimal.ZERO;
    }

    if (functionName.equals("json.equals")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      Object left = asJSON(parameters.get(0));
      Object right = asJSON(parameters.get(1));

      if (left instanceof JSONArray) {
        if (right instanceof JSONArray) {
          JSONArray la = (JSONArray) left;
          JSONArray ra = (JSONArray) right;
          if (la.size() != ra.size()) {
            return BigDecimal.ZERO;
          }
          for (int i = 0; i < la.size(); i++) {
            if (!la.get(i).equals(ra.get(i))) {
              return BigDecimal.ZERO;
            }
          }
          return BigDecimal.ONE;
        }
        return BigDecimal.ZERO;
      }

      if (left instanceof JSONObject) {
        if (right instanceof JSONObject) {
          JSONObject lo = (JSONObject) left;
          JSONObject ro = (JSONObject) right;

          if (lo.size() != ro.size()) {
            return BigDecimal.ZERO;
          }

          for (Object key : lo.keySet()) {
            if (!lo.get(key).equals(ro.get(key))) {
              return BigDecimal.ZERO;
            }
          }
          return BigDecimal.ONE;
        }
      }

      if (left instanceof String) {
        if (right instanceof String) {
          if (left.toString().length() == 0 || right.toString().length() == 0) {
            return BigDecimal.ONE;
          }
        }
        return BigDecimal.ZERO;
      }
      return BigDecimal.ZERO;
    }

    if (functionName.equals("json.count")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      int start = 0;
      if (parameters.size() > 2) {
        if (!(parameters.get(2) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  3,
                  parameters.get(2).toString()));
        }
        start = ((BigDecimal) parameters.get(2)).intValue();
      }
      return JSONCount(asJSON(parameters.get(0).toString()), parameters.get(1), start);
    }

    if (functionName.equals("json.indexOf")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      int start = 0;
      if (parameters.size() > 2) {
        if (!(parameters.get(2) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  3,
                  parameters.get(2).toString()));
        }
        start = ((BigDecimal) parameters.get(2)).intValue();
      }
      return JSONIndexOf(asJSON(parameters.get(0).toString()), parameters.get(1), start);
    }

    if (functionName.equals("json.merge")) {
      return JSONMerge(parameters);
    }

    if (functionName.equals("json.unique")) {
      return JSONUnique(asJSON(parameters.get(0).toString()));
    }

    if (functionName.equals("json.removeAll")) {
      return JSONRemoveAll(parameters);
    }

    if (functionName.equals("json.removeFirst")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONRemoveFirst(parameters);
    }

    if (functionName.equals("json.union")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONUnion(parameters);
    }

    if (functionName.equals("json.difference")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONDifference(parameters);
    }

    if (functionName.equals("json.intersection")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONIntersection(parameters);
    }

    if (functionName.equals("json.isSubset")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return JSONIsSubset(parameters);
    }

    if (functionName.equals("json.rolls")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      } else if (parameters.size() > 3) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 3, parameters.size()));
      }
      return JSONRolls(parameters);
    }

    if (functionName.equals("json.objrolls")) {
      if (parameters.size() != 3) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 3, parameters.size()));
      }

      return JSONObjRolls(parameters);
    }
    throw new ParserException(functionName + "(): Unknown function");
  }

  /**
   * Determines if JSON arrays are a subset of another array.
   *
   * @param parameters The arguments to the function.
   * @return 1 if all the sets are a subset of the first set, otherwise 0.
   * @throws ParserException If an error occurs.
   */
  private BigDecimal JSONIsSubset(List<Object> parameters) throws ParserException {
    Set<Object> set = new HashSet<Object>();
    Set<Object> subset = new HashSet<Object>();

    Object o1 = asJSON(parameters.get(0));
    if (o1 instanceof JSONArray) {
      set.addAll((JSONArray) o1);
    } else if (o1 instanceof JSONObject) {
      set.addAll(((JSONObject) o1).keySet());
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyJSON",
              o1 == null ? "NULL" : o1.toString(),
              "json.isSubset"));
    }

    for (int i = 1; i < parameters.size(); i++) {
      Object o2 = asJSON(parameters.get(i));
      if (o2 instanceof JSONArray) {
        subset.addAll((JSONArray) o2);
      } else if (o2 instanceof JSONObject) {
        subset.addAll(((JSONObject) o2).keySet());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.onlyJSON",
                o1 == null ? "NULL" : o1.toString(),
                "json.isSubset"));
      }

      if (!set.containsAll(subset)) {
        return BigDecimal.ZERO;
      }
    }

    return BigDecimal.ONE;
  }

  /**
   * Remove the first occurrence of each element in the second array from the first array.
   *
   * @param parameters The arguments to the function.
   * @return a JSON array containing the difference of all the arguments.
   * @throws ParserException
   */
  private Object JSONRemoveFirst(List<Object> parameters) throws ParserException {

    List<Object> result = new LinkedList<>();

    Object o = asJSON(parameters.get(0).toString());
    if (o instanceof JSONArray) {
      result.addAll((JSONArray) o);
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              o == null ? "NULL" : o.toString(),
              "json.removeFirst"));
    }

    o = asJSON(parameters.get(1).toString());
    List<Object> toRemove = new ArrayList<>();
    if (o instanceof JSONArray) {
      toRemove.addAll((JSONArray) o);
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              o == null ? "NULL" : o.toString(),
              "json.removeFirst"));
    }

    for (Object val : toRemove) {
      Iterator iter = result.iterator();
      while (iter.hasNext()) {
        Object obj = iter.next();
        if (obj.equals(val)) {
          iter.remove();
          break;
        }
      }
    }

    return JSONArray.fromObject(result);
  }

  /**
   * Perform a difference of all of the JSON objects or arrays.
   *
   * @param parameters The arguments to the function.
   * @return a JSON array containing the difference of all the arguments.
   * @throws ParserException
   */
  private Object JSONDifference(List<Object> parameters) throws ParserException {
    Set<Object> s = new HashSet<Object>();

    Object o = asJSON(parameters.get(0).toString());
    if (o instanceof JSONArray) {
      s.addAll((JSONArray) o);
    } else if (o instanceof JSONObject) {
      s.addAll(((JSONObject) o).keySet());
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyJSON",
              o == null ? "NULL" : o.toString(),
              "json.difference"));
    }

    for (int i = 1; i < parameters.size(); i++) {
      o = asJSON(parameters.get(i).toString());
      if (o instanceof JSONArray) {
        s.removeAll((JSONArray) o);
      } else if (o instanceof JSONObject) {
        s.removeAll(((JSONObject) o).keySet());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.onlyJSON",
                o == null ? "NULL" : o.toString(),
                "json.difference"));
      }
    }

    return JSONArray.fromObject(s);
  }

  /**
   * Perform a union of all of the JSON objects or arrays.
   *
   * @param parameters The arguments to the function.
   * @return a JSON array containing the union of all the arguments.
   * @throws ParserException
   */
  private Object JSONUnion(List<Object> parameters) throws ParserException {
    Set<Object> s = new HashSet<Object>();

    Object o = asJSON(parameters.get(0).toString());
    if (o instanceof JSONArray) {
      s.addAll((JSONArray) o);
    } else if (o instanceof JSONObject) {
      s.addAll(((JSONObject) o).keySet());
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyJSON", o == null ? "NULL" : o.toString(), "json.union"));
    }

    for (int i = 1; i < parameters.size(); i++) {
      o = asJSON(parameters.get(i).toString());
      if (o instanceof JSONArray) {
        s.addAll((JSONArray) o);
      } else if (o instanceof JSONObject) {
        s.addAll(((JSONObject) o).keySet());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.onlyJSON", o == null ? "NULL" : o.toString(), "json.union"));
      }
    }

    return JSONArray.fromObject(s);
  }

  /**
   * Perform a intersection of all of the JSON objects or arrays.
   *
   * @param parameters The arguments to the function.
   * @return a JSON array containing the intersection of all the arguments.
   * @throws ParserException
   */
  private Object JSONIntersection(List<Object> parameters) throws ParserException {
    Set<Object> s = new HashSet<Object>();

    Object o = asJSON(parameters.get(0).toString());
    if (o instanceof JSONArray) {
      s.addAll((JSONArray) o);
    } else if (o instanceof JSONObject) {
      s.addAll(((JSONObject) o).keySet());
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyJSON",
              o == null ? "NULL" : o.toString(),
              "json.intersection"));
    }

    for (int i = 1; i < parameters.size(); i++) {
      o = asJSON(parameters.get(i).toString());
      if (o instanceof JSONArray) {
        s.retainAll((JSONArray) o);
      } else if (o instanceof JSONObject) {
        s.retainAll(((JSONObject) o).keySet());
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.json.onlyJSON",
                o == null ? "NULL" : o.toString(),
                "json.intersection"));
      }
    }

    return JSONArray.fromObject(s);
  }

  /**
   * Removes all the values in the second and any subsequent objects/arrays from the first JSON
   * object or array.
   *
   * @param parameters The parameters to the function.
   * @return the new object or array.
   * @throws ParserException if an error occurs.
   */
  private Object JSONRemoveAll(List<Object> parameters) throws ParserException {
    Object json = asJSON(parameters.get(0).toString());

    if (json instanceof JSONArray) {
      // Create a new JSON Array to preserve immutability for the macro script.
      JSONArray jarr = JSONArray.fromObject(json);

      for (int i = 1; i < parameters.size(); i++) {
        Object o2 = asJSON(parameters.get(i).toString());
        if (o2 instanceof JSONArray) {
          jarr.removeAll((JSONArray) o2);
        } else if (o2 instanceof JSONObject) {
          jarr.removeAll(((JSONObject) o2).keySet());
        } else {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.onlyJSON",
                  o2 == null ? "NULL" : o2.toString(),
                  "json.removeAll"));
        }
      }
      return jarr;
    } else if (json instanceof JSONObject) {
      // Create a new JSON Array to preserve immutability for the macro script.
      JSONObject jobj = JSONObject.fromObject(json);

      for (int i = 1; i < parameters.size(); i++) {
        Object o2 = asJSON(parameters.get(i).toString());
        if (o2 instanceof JSONArray) {
          for (Object o3 : (JSONArray) o2) {
            jobj.remove(o3);
          }
        } else if (o2 instanceof JSONObject) {
          for (Object o3 : ((JSONObject) o2).keySet()) {
            jobj.remove(o3);
          }
        } else {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.onlyJSON",
                  o2 == null ? "NULL" : o2.toString(),
                  "json.removeAll"));
        }
      }
      return jobj;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyJSON",
              json == null ? "NULL" : json.toString(),
              "json.removeAll"));
    }
  }

  /**
   * Returns a JSON array with no duplicates. There is order of elements may not be preserved.
   *
   * @param obj The JSON array to remove the duplicates from.
   * @return a JSON array.
   * @throws ParserException if an exception occurs.
   */
  private JSONArray JSONUnique(Object obj) throws ParserException {
    if (!(obj instanceof JSONArray)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              obj == null ? "NULL" : obj.toString(),
              "json.unique"));
    }
    JSONArray jarr = (JSONArray) obj;
    Set s = new HashSet();
    s.addAll(jarr);
    return JSONArray.fromObject(s);
  }

  /**
   * Merges multiple JSON objects or arrays.
   *
   * @param parameters The parameters to the function.
   * @return the merged object or array.
   * @throws ParserException if an error occurs.
   */
  private Object JSONMerge(List<Object> parameters) throws ParserException {
    Object json = asJSON(parameters.get(0).toString());

    if (json instanceof JSONArray) {
      // Create a new JSON Array to preserve immutability for the macro script.
      JSONArray jarr = JSONArray.fromObject(json);

      for (int i = 1; i < parameters.size(); i++) {
        Object o2 = asJSON(parameters.get(i).toString());
        if (!(o2 instanceof JSONArray)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.onlyJSON",
                  o2 == null ? "NULL" : o2.toString(),
                  "json.merge"));
        }
        jarr.addAll((JSONArray) o2);
      }
      return jarr;
    } else if (json instanceof JSONObject) {
      // Create a new JSON Array to preserve immutability for the macro script.
      JSONObject jobj = JSONObject.fromObject(json);

      for (int i = 1; i < parameters.size(); i++) {
        Object o2 = asJSON(parameters.get(i).toString());
        if (!(o2 instanceof JSONObject)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.onlyJSON",
                  o2 == null ? "NULL" : o2.toString(),
                  "json.merge"));
        }
        jobj.putAll((JSONObject) o2);
      }
      return jobj;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyJSON",
              json == null ? "NULL" : json.toString(),
              "json.merge"));
    }
  }

  /**
   * Gets the number of times a value appears in the array.
   *
   * @param json The JSON array to check.
   * @param searchFor the value to search for.
   * @param start the index to start searching at.
   * @return the number of times the value occurs.
   * @throws ParserException
   */
  private BigDecimal JSONCount(Object json, Object searchFor, int start) throws ParserException {
    if (!(json instanceof JSONArray)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              json == null ? "NULL" : json.toString(),
              "json.count"));
    }
    JSONArray jarr = (JSONArray) json;

    if (searchFor instanceof String && convertToJSON((String) searchFor) != null) {
      searchFor = convertToJSON((String) searchFor);
    }

    int count = 0;
    for (int i = start, max = jarr.size(); i < max; i++) {
      Object ob = jarr.get(i);
      if (ob instanceof Float) {
        if (searchFor.equals(BigDecimal.valueOf((Float) ob))) {
          count++;
        }
      } else if (ob instanceof Integer) {
        if (searchFor.equals(BigDecimal.valueOf((Integer) ob))) {
          count++;
        }
      } else if (ob instanceof Double) {
        if (searchFor.equals(BigDecimal.valueOf((Double) ob))) {
          count++;
        }
      } else if (ob instanceof Long) {
        if (searchFor.equals(BigDecimal.valueOf((Long) ob))) {
          count++;
        }
      } else {
        if (searchFor.equals(ob)) {
          count++;
        }
      }
    }

    return BigDecimal.valueOf(count);
  }

  /**
   * Gets the index of a value in a JSON array.
   *
   * @param json The JSON array to check.
   * @param searchFor The value to search for.
   * @param start The index to start from.
   * @return The index of the first occurance of the value in the array or -1 if it does not occur.
   * @throws ParserException
   */
  private BigDecimal JSONIndexOf(Object json, Object searchFor, int start) throws ParserException {
    if (!(json instanceof JSONArray)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              json == null ? "NULL" : json.toString(),
              "json.indexOf"));
    }
    JSONArray jarr = (JSONArray) json;

    if (searchFor instanceof String && convertToJSON((String) searchFor) != null) {
      searchFor = convertToJSON((String) searchFor);
    }

    for (int i = start, max = jarr.size(); i < max; i++) {
      Object ob = jarr.get(i);
      if (ob instanceof Float) {
        if (searchFor.equals(BigDecimal.valueOf((Float) ob))) {
          return BigDecimal.valueOf(i);
        }
      } else if (ob instanceof Integer) {
        if (searchFor.equals(BigDecimal.valueOf((Integer) ob))) {
          return BigDecimal.valueOf(i);
        }
      } else if (ob instanceof Double) {
        if (searchFor.equals(BigDecimal.valueOf((Double) ob))) {
          return BigDecimal.valueOf(i);
        }
      } else if (ob instanceof Long) {
        if (searchFor.equals(BigDecimal.valueOf((Long) ob))) {
          return BigDecimal.valueOf(i);
        }
      } else {
        if (searchFor.equals(ob)) {
          return BigDecimal.valueOf(i);
        }
      }
    }

    return BigDecimal.valueOf(-1);
  }

  /**
   * Evaluates each of the strings in the JSON object or array.
   *
   * @param res The variable resolver.
   * @param json the JSON object.
   * @return the json object with the strings evaluated.
   * @throws ParserException if there is an error.
   */
  public Object JSONEvaluate(MapToolVariableResolver res, Object json) throws ParserException {
    if (json instanceof JSONObject) {
      JSONObject jobj = (JSONObject) json;
      for (Object key : jobj.keySet()) {
        Object o = jobj.get(key);
        if (o instanceof JSONObject || o instanceof JSONArray) {
          jobj.put(key, JSONEvaluate(res, o));
        } else if (o instanceof String) {
          jobj.put(key, EvalMacroFunctions.evalMacro(res, res.getTokenInContext(), o.toString()));
        }
      }
    } else {
      JSONArray jarr = (JSONArray) json;
      for (int i = 0; i < jarr.size(); i++) {
        Object o = jarr.get(i);
        if (o instanceof JSONObject || o instanceof JSONArray) {
          jarr.set(i, JSONEvaluate(res, o));
        } else if (o instanceof String) {
          // For arrays we may have an extra "" so it can be stored in the array
          String line = o.toString();
          line = line.replaceFirst("^\"", "").replaceFirst("\"$", "");
          jarr.set(i, EvalMacroFunctions.evalMacro(res, res.getTokenInContext(), line));
        }
      }
    }
    return json;
  }

  /**
   * Shuffles the values in a json array.
   *
   * @param jsonArray the array to shuffle.
   * @return the shuffled array.
   * @throws ParserException if the object is not a JSON Array.
   */
  private JSONArray JSONShuffle(Object jsonArray) throws ParserException {
    if (!(jsonArray instanceof JSONArray)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              jsonArray == null ? "NULL" : jsonArray.toString(),
              "json.shuffle"));
    }
    // Create a new JSON Array to support immutable types in macros.
    JSONArray jarr = JSONArray.fromObject(jsonArray);
    Collections.shuffle(jarr);
    return jarr;
  }

  /**
   * Sorts a json array. If all values in the array are numbers then the values are sorted in
   * numeric order, otherwise values are sorted in string order.
   *
   * @param jsonArray The json array to sort.
   * @param direction The direction "ascending" or "descending" to sort the array.
   * @param fields A list of Strings defining which fields with the JSON objects to use for sorting
   *     and the precedence of those fields.
   * @return The sorted array.
   * @throws ParserException if the object is not a JSON array.
   */
  @SuppressWarnings("unchecked")
  private JSONArray JSONSort(Object jsonArray, String direction, List<String> fields)
      throws ParserException {
    if (!(jsonArray instanceof JSONArray)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray",
              jsonArray == null ? "NULL" : jsonArray.toString(),
              "json.sort"));
    }

    // Create a new JSON Array to support immutable types in macros.
    JSONArray jarr = JSONArray.fromObject(jsonArray);
    if (fields != null) {
      return JSONSortObjects(jarr, direction, fields);
    }

    // Check to see if we are all numbers
    boolean sortAsNumber = true;
    for (Object o : jarr) {
      if (!(o instanceof Double) && !(o instanceof Integer)) {
        sortAsNumber = false;
        break;
      }
    }

    boolean ascending = true;
    if (direction.toLowerCase().startsWith("d")) {
      ascending = false;
    }

    if (sortAsNumber) {
      Collections.sort(jarr, new JSONNumberComparator(ascending));
    } else {
      Collections.sort(jarr, new JSONStringComparator(ascending));
    }
    return jarr;
  }

  /**
   * Sorts the JSON objects in a JSON array
   *
   * @param jsonArray the array to sort.
   * @param direction the direction, ascending or descending to sort the array.
   * @param fields the fields to base the sort on.
   * @return The sorted array.
   * @throws ParserException when an error occurs.
   */
  private JSONArray JSONSortObjects(JSONArray jsonArray, String direction, List<String> fields)
      throws ParserException {
    for (Object o : jsonArray) {
      if (!(o instanceof JSONObject)) {
        throw new ParserException(
            I18N.getText("macro.function.json.arrayMustContainObjects", o.toString()));
      }
    }
    boolean ascending = true;
    if (direction.toLowerCase().startsWith("d")) {
      ascending = false;
    }

    JSONNumberComparator numberCompare = new JSONNumberComparator(ascending);
    JSONStringComparator stringCompare = new JSONStringComparator(ascending);

    List<Comparator<Object>> comparatorList = new ArrayList<Comparator<Object>>();
    // Generate the comparator we will use for each fld
    // Check to see if we are all numbers for each field
    for (Object fld : fields) {
      boolean sortAsNumber = true;
      for (Object jo : jsonArray) {
        Object o = ((JSONObject) jo).get(fld.toString());
        if (o == null) {
          throw new ParserException(
              I18N.getText("macro.function.json.notAllContainKey", fld.toString()));
        }
        if (!(o instanceof Integer) && !(o instanceof Double)) {
          sortAsNumber = false;
          break;
        }
      }
      if (sortAsNumber) {
        comparatorList.add(numberCompare);
      } else {
        comparatorList.add(stringCompare);
      }
    }

    Collections.sort(jsonArray, new JSONObjectComparator(ascending, fields, comparatorList));

    return jsonArray;
  }

  /**
   * Attempts to convert a string to a JSON object.
   *
   * @param obj The string to attempt to convert.
   * @return Either a JSONObject, JSONArray, or null.
   */
  public static Object convertToJSON(String obj) {
    if (obj.trim().startsWith("[")) {
      try {
        return JSONArray.fromObject(obj);
      } catch (Exception e) {
        return null;
      }
    }

    if (obj.trim().startsWith("{")) {
      try {
        return JSONObject.fromObject(obj);
      } catch (Exception e) {
        return null;
      }
    }

    return null;
  }

  /**
   * JSONify the given value, inducing JSON type from the Maptool string value. Because Maptool
   * arbitrarily convert null, true, and false from incoming json data into "null", "true" and
   * "false", this function does the opposite to allow Maptool to send json data
   *
   * @param value A Maptool value.
   * @return null, true or false instead of "null", "true, or "false", or the value unchanged
   */
  public static Object jsonify(Object value) {
    // the json library does not use the java null object, but one singleton of its own
    if ("null".equals(value)) {
      return JSONNull.getInstance();
    } else if ("true".equals(value)) {
      return true;
    } else if ("false".equals(value)) {
      return false;
    }
    return value;
  }

  /**
   * Append a value to a JSON array.
   *
   * @param obj The JSON object.
   * @param values The values to append to the array.
   * @return the JSON array.
   * @throws ParserException
   */
  private JSONArray JSONAppend(Object obj, List<Object> values) throws ParserException {
    if (obj == null || obj.toString().length() == 0) {
      obj = new JSONArray();
    }

    if (obj instanceof JSONArray) {
      // Create a new JSON Array to support immutable types in macros.
      JSONArray jarr = JSONArray.fromObject(obj);
      for (Object val : values.subList(1, values.size())) {
        jarr.add(jsonify(val));
      }
      return jarr;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.append.onlyArray",
              obj == null ? "NULL" : obj.toString(),
              "json.append"));
    }
  }

  /**
   * Gets a value from the JSON Object or Array.
   *
   * @param obj The JSON Object or Array.
   * @param keys The key for the object or index for the array.
   * @return the value.
   * @throws ParserException
   */
  private Object JSONGet(Object obj, List<Object> keys) throws ParserException {
    Object val = null;
    if (obj instanceof JSONObject) {
      JSONObject jobj = (JSONObject) obj;
      if (keys.size() == 1) {
        Object oval = jobj.get(keys.get(0).toString());
        if (oval == null) {
          val = "";
        } else {
          val = oval.toString();
        }
      } else {
        Map<String, Object> values = new HashMap<String, Object>();
        for (Object key : keys) {
          Object oval = jobj.get(key.toString());
          values.put(key.toString(), oval != null ? oval : "");
        }
        val = JSONObject.fromObject(values);
      }
    } else if (obj instanceof JSONArray) {
      JSONArray jarr = (JSONArray) obj;
      if (keys.size() == 1) {
        val = jarr.get(Integer.parseInt(keys.get(0).toString())).toString();
      } else {
        int start = Integer.parseInt(keys.get(0).toString());
        // Wrap around
        if (start < 0) {
          start = jarr.size() + start;
        }
        if (start >= jarr.size() || start < 0) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.getInvalidStartIndex", "json.get", start, jarr.size()));
        }
        int end = Integer.parseInt(keys.get(1).toString());
        if (end < 0) {
          end = jarr.size() + end;
        }
        if (end >= jarr.size() || end < 0) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.getInvalidEndIndex", "json.end", start, jarr.size()));
        }

        List<Object> values = new ArrayList<Object>();
        if (start > end) {
          for (int i = start; i >= end; i--) {
            values.add(jarr.get(i));
          }
        } else {
          for (int i = start; i <= end; i++) {
            values.add(jarr.get(i));
          }
        }
        val = JSONArray.fromObject(values);
      }
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.get"));
    }

    if (keys.size() == 1) {
      // Attempt to convert to a number ...
      try {
        return new BigDecimal((String) val);
      } catch (Exception e) {
        // Ignore
      }
    }
    return val == null ? "" : val;
  }

  /**
   * Converts a JSON object to a string property.
   *
   * @param obj The object to convert.
   * @param delim The delimiter used in the string property.
   * @return The string property of the object.
   * @throws ParserException If the object is not a JSON object.
   */
  private String JSONToStrProp(Object obj, String delim) throws ParserException {

    StringBuilder sb = new StringBuilder();
    if (obj instanceof JSONObject) {
      JSONObject jobj = (JSONObject) obj;
      for (Object ob : jobj.keySet()) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(ob).append("=").append(jobj.get(ob));
      }
      return sb.toString();
    } else if (obj instanceof JSONArray) {
      JSONArray jarr = (JSONArray) obj;
      for (int i = 0; i < jarr.size(); i++) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(i).append("=").append(jarr.get(i));
      }
      return sb.toString();
    } else if (obj instanceof String && ((String) obj).trim().length() == 0) {
      return obj.toString();
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.toStrProp"));
    }
  }

  /**
   * Converts a JSON object to a string list.
   *
   * @param obj The object to convert.
   * @param delim The delimiter used in the string list.
   * @return The string list of the object.
   * @throws ParserException If the object is not a JSON object.
   */
  private Object JSONToList(Object obj, String delim) throws ParserException {

    StringBuilder sb = new StringBuilder();
    if (obj instanceof JSONObject) {
      JSONObject jobj = (JSONObject) obj;
      for (Object ob : jobj.keySet()) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(ob);
      }
      return sb.toString();
    } else if (obj instanceof JSONArray) {
      JSONArray jarr = (JSONArray) obj;
      for (int i = 0; i < jarr.size(); i++) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(jarr.get(i));
      }
      return sb.toString();
    } else if (obj instanceof String && ((String) obj).trim().length() == 0) {
      return obj.toString();
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.toStrList"));
    }
  }

  /**
   * Gets a list of the JSON objects fields.
   *
   * @param obj The object to get the fields of.
   * @param delim The delimiter used in the string list.
   * @return The string property of the object.
   * @throws ParserException If the object is not a JSON object.
   */
  private Object JSONFields(Object obj, String delim) throws ParserException {

    StringBuilder sb = new StringBuilder();
    if (obj instanceof JSONObject) {
      JSONObject jobj = JSONObject.fromObject(obj);
      if ("json".equals(delim)) {
        return JSONArray.fromObject(jobj.keySet());
      } else {
        for (Object ob : jobj.keySet()) {
          if (sb.length() > 0) {
            sb.append(delim);
          }
          sb.append(ob.toString());
        }
        return sb.toString();
      }
    } else if (obj instanceof JSONArray) {
      JSONArray jarr = (JSONArray) obj;
      for (int i = 0; i < jarr.size(); i++) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(i);
      }
      return sb.toString();
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.fields"));
    }
  }

  /**
   * Gets the length of a JSON object. If the object is an array then the number of elements is
   * returned, if it is an object then the number of fields is returned.
   *
   * @param obj The JSON object.
   * @return the number of elements or fields.
   * @throws ParserException if obj is not a JSON object.
   */
  private BigDecimal JSONLength(Object obj) throws ParserException {

    if (obj instanceof JSONObject) {
      JSONObject jobj = (JSONObject) obj;
      return BigDecimal.valueOf(jobj.keySet().size());
    } else if (obj instanceof JSONArray) {
      JSONArray jarr = (JSONArray) obj;
      return BigDecimal.valueOf(jarr.size());
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.length"));
    }
  }

  /**
   * Sets the value of an element in a JSON Array or a Field in a JSON Object.
   *
   * @param obj The JSON object.
   * @param param The key/value pairs to set.
   * @return new JSON object.
   * @throws ParserException if the obj is not a JSON object.
   */
  private Object JSONSet(Object obj, List<Object> param) throws ParserException {

    if (obj == null || obj.toString().length() == 0) {
      obj = new JSONObject();
    }

    if (param.size() % 2 != 1) {
      throw new ParserException(I18N.getText("macro.function.json.setNoMatchingValue", "json.set"));
    }

    if (obj instanceof JSONObject) {
      // Create a new JSON object to preserve macro object immutable types.
      JSONObject jobj = JSONObject.fromObject(obj);
      for (int i = 1; i < param.size(); i += 2) {
        jobj.put(param.get(i).toString(), jsonify(param.get(i + 1)));
      }
      return jobj;
    } else if (obj instanceof JSONArray) {
      // Create a new JSON array to preserve macro object immutable types.
      JSONArray jarr = JSONArray.fromObject(obj);
      for (int i = 1; i < param.size(); i += 2) {
        jarr.set(Integer.parseInt(param.get(i).toString()), jsonify(param.get(i + 1)));
      }
      return jarr;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.set"));
    }
  }

  /**
   * Creates a JSON Array from a String list.
   *
   * @param list The string list.
   * @param delim The delimiter used to separate items in the string list.
   * @return The JSON Array.
   */
  public JSONArray fromStrList(String list, String delim) {
    delim = delim != null ? delim : ",";
    String[] stringArray = list.split(delim);

    // Empty list should generate empty JSON array
    if (stringArray.length == 1 && stringArray[0].length() == 0) return new JSONArray();

    Object[] array = new Object[stringArray.length];
    // Try to convert it to a number and if that works we store it that way
    for (int i = 0; i < stringArray.length; i++) {
      String value = stringArray[i].trim();
      // Empty list element generates an empty string in the JSON array
      if (value.length() == 0) array[i] = "";
      else {
        try {
          BigDecimal bd = new BigDecimal(value);
          array[i] = bd;
        } catch (NumberFormatException nfe) {
          array[i] = value;
        }
      }
    }
    return JSONArray.fromObject(array);
  }

  /**
   * Creates a JSON object from a String property list.
   *
   * @param prop The String Property list.
   * @param delim The delimiter used to separate items in the list.
   * @return The JSON Object.
   */
  public JSONObject fromStrProp(String prop, String delim) {
    delim = delim != null ? delim : ";";
    String[] props = prop.split(delim);
    HashMap<String, Object> obmap = new HashMap<String, Object>();
    for (String s : props) {
      String[] vals = s.split("=", 2);
      String key = vals[0].trim();
      if (vals.length > 1) {
        // Try to convert it to a number and if that works we store it that way
        String value = vals[1].trim();
        try {
          obmap.put(key, new BigDecimal(value));
        } catch (Exception e) {
          obmap.put(key, value);
        }
      } else {
        obmap.put(key, "");
      }
    }
    return JSONObject.fromObject(obmap);
  }

  /**
   * Gets the type of the JSON object.
   *
   * @param obj The json object.
   * @return the type of object.
   */
  private JSONObjectType getJSONObjectType(Object obj) {
    if (obj instanceof JSONObject) {
      return JSONObjectType.OBJECT;
    } else if (obj instanceof JSONArray) {
      return JSONObjectType.ARRAY;
    } else {
      String str = obj.toString().trim();
      if (str.startsWith("{") || str.startsWith("[")) {
        Object converted = asJSON(str);
        if (converted != null) {
          return getJSONObjectType(converted);
        }
      }
      return JSONObjectType.UNKNOWN;
    }
  }

  /**
   * Deletes a field from a JSON object or element from a JSON array.
   *
   * @param obj The JSON object.
   * @param key
   * @return The new JSON object.
   * @throws ParserException if obj can not be converted to a JSON object.
   */
  private Object JSONDelete(Object obj, String key) throws ParserException {

    if (obj instanceof JSONObject) {
      // Create a new JSON object so that old one remains immutable to macro
      JSONObject jobj = JSONObject.fromObject(obj);
      jobj.remove(key);
      return jobj;
    } else if (obj instanceof JSONArray) {
      // Create a new JSON array so that old one remains immutable to macro
      JSONArray jarr = JSONArray.fromObject(obj);
      jarr.remove(Integer.parseInt(key));
      return jarr;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.delete"));
    }
  }

  /**
   * Returns and indented version of a JSON string.
   *
   * @param obj The JSON string to ident.
   * @param indent The indention factor.
   * @return The indented string.
   * @throws ParserException If an error occurs parsing the JSON String.
   */
  private String JSONIndent(Object obj, int indent) throws ParserException {
    if (obj instanceof JSONObject) {
      return ((JSONObject) obj).toString(indent);
    } else if (obj instanceof JSONArray) {
      return ((JSONArray) obj).toString(indent);
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.unknownType",
              obj == null ? "NULL" : obj.toString(),
              "json.indent"));
    }
  }

  /**
   * Checks to see if a JSON object contains the specified key.
   *
   * @param obj The JSON Object.
   * @param key The key to check for.
   * @return true if the JSON object contains the key.
   * @throws ParserException
   */
  private boolean JSONContains(Object obj, String key) throws ParserException {

    if (obj != null && obj instanceof JSONObject) {
      return ((JSONObject) obj).containsKey(key);
    }

    if (obj instanceof JSONArray) {
      try {
        return ((JSONArray) obj).contains(Integer.parseInt(key));
      } catch (Exception e) {
        // Do nothing as we will try another conversion
      }

      try {
        return ((JSONArray) obj).contains(Double.parseDouble(key));
      } catch (Exception e) {
        // Do nothing as we will try it as a string
      }
      return ((JSONArray) obj).contains(key);
    }

    throw new ParserException(
        I18N.getText(
            "macro.function.json.unknownType",
            obj == null ? "NULL" : obj.toString(),
            "json.contains"));
  }

  /**
   * Reverses a json array.
   *
   * @param jsonArray The json array to reverse.
   * @return the reversed json array.
   * @throws ParserException if jsonArray is not a json array.
   */
  private JSONArray JSONReverse(Object jsonArray) throws ParserException {
    if (!(jsonArray instanceof JSONArray)) {
      throw new ParserException(I18N.getText("macro.function.json.onlyArray", "json.reverse"));
    }

    // Create a new JSON Array to preserve immutable state for macros.
    JSONArray jarr = JSONArray.fromObject(jsonArray);
    List<Object> arr = new LinkedList<Object>();
    for (int i = jarr.size() - 1; i >= 0; i--) {
      arr.add(jarr.get(i));
    }

    return JSONArray.fromObject(arr);
  }

  /** Compares two numbers from a json array. */
  private static class JSONNumberComparator implements Comparator<Object> {
    private final boolean ascending;

    public JSONNumberComparator(boolean ascending) {
      this.ascending = ascending;
    }

    public int compare(Object o1, Object o2) {
      BigDecimal v1;
      BigDecimal v2;

      if (o1 instanceof Integer) {
        v1 = BigDecimal.valueOf((Integer) o1);
      } else {
        v1 = BigDecimal.valueOf((Double) o1);
      }

      if (o2 instanceof Integer) {
        v2 = BigDecimal.valueOf((Integer) o2);
      } else {
        v2 = BigDecimal.valueOf((Double) o2);
      }
      return ascending ? v1.compareTo(v2) : v2.compareTo(v1);
    }
  }

  /** Compares two strings from a json array. */
  private static class JSONStringComparator implements Comparator<Object> {
    private final boolean ascending;

    public JSONStringComparator(boolean ascending) {
      this.ascending = ascending;
    }

    public int compare(Object o1, Object o2) {
      String s1 = o1.toString();
      String s2 = o2.toString();
      return ascending ? s1.compareTo(s2) : s2.compareTo(s1);
    }
  }

  /** Compares two objects from a json array. */
  private static class JSONObjectComparator implements Comparator<Object> {
    private final boolean ascending;
    private final List<String> fields;
    private final List<Comparator<Object>> comparators;

    public JSONObjectComparator(
        boolean ascending, List<String> fields, List<Comparator<Object>> comparators) {
      this.ascending = ascending;
      this.fields = fields;
      this.comparators = comparators;
    }

    public int compare(Object o1, Object o2) {
      JSONObject jo1 = (JSONObject) o1;
      JSONObject jo2 = (JSONObject) o2;

      for (int i = 0; i < fields.size(); i++) {
        Object f = fields.get(i);
        int c = comparators.get(i).compare(jo1.get(f), jo2.get(f));
        if (c != 0) {
          return c;
        }
      }
      return 0;
    }
  }

  /**
   * Returns a JSON object from the parameter.
   *
   * @param o The parameter.
   * @return a JSON object.
   */
  @SuppressWarnings("unused")
  private static JSONObject asJSONObject(Object o) {
    if (o instanceof JSONObject) {
      return (JSONObject) o;
    }

    return JSONObject.fromObject(o.toString());
  }

  /**
   * Returns a JSON Array from the parameter.
   *
   * @param o The parameter.
   * @return a JSON array.
   */
  @SuppressWarnings("unused")
  private static JSONArray asJSONArray(Object o) {
    if (o instanceof JSONArray) {
      return (JSONArray) o;
    }
    // Special cases we have to deal with cases where the parser
    // has already had a go at single index arrays.
    if (o instanceof BigDecimal) {
      JSONArray jarr = new JSONArray();
      jarr.add(o);
      return jarr;
    } else if (o instanceof String) {
      String s = ((String) o).trim();
      if (!s.startsWith("[") && !s.startsWith("{")) {
        JSONArray jarr = new JSONArray();
        if (o.toString().length() > 0) {
          jarr.add(o);
        }
        return jarr;
      }
    }

    return JSONArray.fromObject(o.toString());
  }

  /**
   * Returns a JSONObject or JSONArray from the parameter.
   *
   * @param o The parameter to convert.
   * @return The JSONObject or JSONArray.
   */
  public static Object asJSON(Object o) {
    if (o instanceof JSONArray) {
      return o;
    } else if (o instanceof JSONObject) {
      return o;
    }
    // Special cases we have to deal with cases where the parser
    // has already had a go at single index arrays.
    if (o instanceof BigDecimal) {
      JSONArray jarr = new JSONArray();
      jarr.add(o);
      return jarr;
    } else if (o instanceof String) {
      String s = ((String) o).trim();
      if (!s.startsWith("[") && !s.startsWith("{")) {
        if (o.toString().length() == 0) {
          return o.toString();
        }
        JSONArray jarr = new JSONArray();
        if (o.toString().length() > 0) {
          jarr.add(o);
        }
        return jarr;
      }
    }
    return convertToJSON(o.toString());
  }

  public JSONArray JSONRolls(List<Object> param) throws ParserException {
    String roll = param.get(0).toString();
    if (!(param.get(1) instanceof BigDecimal)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN", "json.rolls", 1, param.get(1).toString()));
    }
    int dim1 = ((BigDecimal) param.get(1)).intValue();

    int dim2;
    if (param.size() == 2) {
      dim2 = 1;
    } else if (!(param.get(2) instanceof BigDecimal)) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN", "json.rolls", 2, param.get(2).toString()));
    } else {
      dim2 = ((BigDecimal) param.get(2)).intValue();
    }

    ExpressionParser parser = new ExpressionParser(new MapToolVariableResolver(null));

    if (dim2 == 1) {
      Object[] rollArr = new Object[dim1];
      for (int i = 0; i < dim1; i++) {
        rollArr[i] = parser.evaluate(roll).getValue();
      }

      return JSONArray.fromObject(rollArr);
    } else {
      Object[][] rollArr = new Object[dim1][dim2];
      for (int i2 = 0; i2 < dim2; i2++) {
        for (int i = 0; i < dim1; i++) {
          rollArr[i][i2] = parser.evaluate(roll).getValue();
        }
      }
      return JSONArray.fromObject(rollArr);
    }
  }

  public JSONObject JSONObjRolls(List<Object> param) throws ParserException {
    JSONArray names;
    Object[] stats;
    Object[] rolls;

    Object o;
    o = asJSON(param.get(0));
    if (o instanceof JSONArray) {
      names = (JSONArray) o;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray", o == null ? "NULL" : o.toString(), "json.objrolls"));
    }

    o = asJSON(param.get(1));
    if (o instanceof JSONArray) {
      stats = ((JSONArray) o).toArray();
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.json.onlyArray", o == null ? "NULL" : o.toString(), "json.objrolls"));
    }

    if (param.get(2).toString().trim().startsWith("[")) {
      o = asJSON(param.get(2));
      if (o instanceof JSONArray) {
        rolls = ((JSONArray) o).toArray();
        if (rolls.length != stats.length) {
          throw new ParserException(I18N.getText("macro.function.json.matchingArrayOrRoll"));
        }
      } else {
        throw new ParserException(I18N.getText("macro.function.json.matchingArrayOrRoll"));
      }
    } else if (param.get(2) instanceof String) {
      String roll = (String) param.get(2);
      rolls = new String[stats.length];
      for (int i = 0; i < rolls.length; i++) {
        rolls[i] = roll;
      }
    } else {
      throw new ParserException(I18N.getText("macro.function.json.matchingArrayOrRoll"));
    }

    ExpressionParser parser = new ExpressionParser(new MapToolVariableResolver(null));
    JSONObject jobj = new JSONObject();
    for (Object name : names) {
      JSONObject jstatObj = new JSONObject();
      for (int i = 0; i < stats.length; i++) {
        jstatObj.put(stats[i], parser.evaluate(rolls[i].toString()).getValue());
      }
      jobj.put(name, jstatObj);
    }

    return jobj;
  }
}
