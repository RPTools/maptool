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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.List;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

/** Class used to implement MT Script related Json functions / utilities for JsonObjects. */
public class JsonObjectFunctions {

  /** Class used for conversion between Json and MTS types. */
  private final JsonMTSTypeConversion typeConversion;

  /**
   * Creates a new <code>JsonObjectFunctions</code> instance.
   *
   * @param converter the {@link JsonMTSTypeConversion} used to convert primitive types between json
   *     and MTS.
   */
  public JsonObjectFunctions(JsonMTSTypeConversion converter) {
    typeConversion = converter;
  }

  /**
   * Creates a {@link JsonObject} from a MTScript prop list.
   *
   * @param prop the MTS string property to convert into a {@link JsonObject}.
   * @param delim the delimiter used in the string properties.
   * @return a {@link JsonObject} convert4d from the string properties.
   */
  public JsonObject fromStrProp(String prop, String delim) {
    String[] propsArray = StringUtil.split(prop, delim);
    JsonObject jsonObject = new JsonObject();

    for (String s : propsArray) {
      String[] vals = s.split("=", 2);
      vals[0] = vals[0].trim();
      if (vals.length > 1) {
        vals[1] = vals[1].trim();
        jsonObject.add(vals[0], typeConversion.convertPrimitiveFromString(vals[1]));
      } else {
        jsonObject.add(vals[0], null);
      }
    }

    return jsonObject;
  }

  /**
   * Converts a {@link JsonObject} into a MT Script string property.
   *
   * @param jsonObject The JsonObject to convert.
   * @param delim The delimiter to user in converted string property.
   * @return the resultant string property.
   */
  public String toStringProp(JsonObject jsonObject, String delim) {
    StringBuilder sb = new StringBuilder();
    for (var entry : jsonObject.entrySet()) {
      if (sb.length() > 0) {
        sb.append(delim);
      }
      sb.append(entry.getKey()).append("=").append(typeConversion.asScriptType(entry.getValue()));
    }
    return sb.toString();
  }

  /**
   * Returns a new {@link JsonObject} with the specified key removed.
   *
   * @param jsonObject the {@link JsonObject} to base the returned value on.
   * @param key the key value to remove from the returned {@link JsonObject}.
   * @return a new {@link JsonObject} with the key removed.
   */
  public JsonObject remove(JsonObject jsonObject, String key) {
    JsonObject result = new JsonObject();
    for (var entry : jsonObject.entrySet()) {
      String keyName = entry.getKey();
      if (!key.equals(keyName)) {
        result.add(keyName, entry.getValue());
      }
    }

    return result;
  }

  /**
   * Checks if the {@link JsonObject} contains the specified value.
   *
   * @param jsonObject the {@link JsonObject} to check.
   * @param value the value to check for.
   * @return <code>true</code> if the {@link}
   */
  public boolean contains(JsonObject jsonObject, String value) {
    return jsonObject.has(value);
  }

  /**
   * Checks to see if there are no fields in this {@link JsonObject}.
   *
   * @param jsonObject the <code>JsonObject</code> to check.
   * @return <code>true</code> if there are no fields in the object, <code>false</code> otherwise.
   */
  public boolean isEmpty(JsonObject jsonObject) {
    return jsonObject.keySet().size() == 0;
  }

  /**
   * Returns a new {@link JsonObject} which contains the contents of all passed in objects merged.
   *
   * @param objects The {@link JsonObject}s to be merged.
   * @return the merged {@link JsonObject}.
   */
  public JsonObject merge(List<JsonObject> objects) {
    JsonObject result = new JsonObject();
    for (JsonObject obj : objects) {
      for (var entry : obj.entrySet()) {
        result.add(entry.getKey(), entry.getValue());
      }
    }

    return result;
  }

  /**
   * Returns a new {@link JsonObject} containing the fields of the passed in {@link JsonObject}
   * after removing all fields in the list of {@link JsonObject}s passed in.
   *
   * @param jsonObject The {@link JsonObject} which contains all the fields to add.
   * @param remove The list of {@link JsonObject}s which contain all the fields to remove.
   * @return the resulting object.
   */
  public JsonObject removeAll(JsonObject jsonObject, List<JsonObject> remove) {
    JsonObject result = shallowCopy(jsonObject);

    for (JsonObject jobj : remove) {
      for (String key : jobj.keySet()) {
        result.remove(key);
      }
    }

    return result;
  }

  /**
   * Returns a shallow copy of the {@link JsonObject}.
   *
   * @param jsonObject the {@link JsonObject} to make a shallow copy of.
   * @return a copy of the {@link JsonObject}.
   */
  JsonObject shallowCopy(JsonObject jsonObject) {
    JsonObject copy = new JsonObject();
    for (var entry : jsonObject.entrySet()) {
      copy.add(entry.getKey(), entry.getValue());
    }

    return copy;
  }

  /**
   * Creates a new JsonObject based on the one passed in and the values to be set.
   *
   * @param jsonObject The object to add to.
   * @param list the list of arguments from the script command.
   * @return The new json object.
   * @throws ParserException if an error occurs.
   */
  public JsonObject set(JsonObject jsonObject, List<Object> list) throws ParserException {
    if ((list.size() & 1) != 0) {
      throw new ParserException(I18N.getText("macro.function.json.setNoMatchingValue", "json.set"));
    }
    JsonObject newJsonObject = jsonObject.deepCopy();

    for (int i = 0; i < list.size(); i += 2) {
      Object value = list.get(i + 1);
      if (value instanceof String && value.toString().length() == 0) {
        value = "";
      }
      newJsonObject.add(list.get(i).toString(), typeConversion.asJsonElement(value));
    }

    return newJsonObject;
  }

  /**
   * Returns a new JsonObject containing the specified keys from.
   *
   * @param jsonObject The JsonObject to get the values from.
   * @param keys The keys to extract.
   * @return The new JsonObject.
   */
  public Object get(JsonObject jsonObject, List<Object> keys) {
    if (keys.size() == 1) {
      if (!jsonObject.has(keys.get(0).toString())) {
        return "";
      }
      return typeConversion.asScriptType(jsonObject.get(keys.get(0).toString()));
    }
    JsonObject newJsonObject = new JsonObject();
    for (Object key : keys) {
      String k = key.toString();
      JsonElement jsonElement = jsonObject.get(k);
      if (jsonElement == null
          || jsonElement.isJsonNull()) { // compatibility with previous MTScript code
        newJsonObject.add(k, new JsonPrimitive(""));
      } else {
        newJsonObject.add(k, jsonElement);
      }
    }

    return newJsonObject;
  }

  /**
   * Sets variable values based on the keys and values in a JsonObject.
   *
   * @param jsonObject The JsonObject to get the names and values from.
   * @param prefix The prefix for all the variable names.
   * @param suffix The suffix for all the variable names.
   * @return A JsonArray that contains the names of all variables set.
   * @throws ParserException if an error occurs while trying to set the variables.
   */
  public JsonArray toVars(
      VariableResolver resolver, JsonObject jsonObject, String prefix, String suffix)
      throws ParserException {
    JsonArray setVars = new JsonArray();
    for (String key : jsonObject.keySet()) {
      // add prefix and suffix
      String varName = prefix + key.trim() + suffix;
      // replace spaces by underscores
      varName = varName.replaceAll("\\s", "_");
      // delete special characters other than "." & "_" in var name
      varName = varName.replaceAll("[^a-zA-Z0-9._]", "");

      if (!varName.equals("")) {
        resolver.setVariable(varName, typeConversion.asScriptType(jsonObject.get(key)));
        setVars.add(varName);
      }
    }
    return setVars;
  }

  /**
   * Creates a MTS string list from the keys of JsonObject.
   *
   * @param json The JsonObject to create a string list of.
   * @param delim The delimiter to use between elements in the list.
   * @return the MTS string list.
   */
  public String toStringList(JsonObject json, String delim) {
    return String.join(delim, json.keySet());
  }

  /**
   * Returns a list of the fields in the JsonObject.
   *
   * @param json The JsonObject to get the fields from.
   * @param delim if "json" returns a JsonArray of the fields otherwise a string delimited by this
   *     value.
   * @return the list of fields.
   */
  public Object fields(JsonObject json, String delim) {
    if ("json".equals(delim)) {
      JsonArray array = new JsonArray();
      json.keySet().forEach(array::add);
      return array;
    } else {
      return String.join(delim, json.keySet());
    }
  }

  /**
   * Returns the number of keys in this JsonObject.
   *
   * @param json the JsonObject to get the number of fields for.
   * @return the number of keys in the JsonObject.
   */
  public BigDecimal length(JsonObject json) {
    return BigDecimal.valueOf(json.keySet().size());
  }
}
