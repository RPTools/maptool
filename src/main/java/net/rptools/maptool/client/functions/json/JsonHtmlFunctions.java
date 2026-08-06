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

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import com.google.gson.*;
import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Nonnull;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class JsonHtmlFunctions {

  JsonHtmlFunctions(@Nonnull JsonMTSTypeConversion converter) {
    typeConversion = converter;
  }

  private static final Logger log = LogManager.getLogger(JsonHtmlFunctions.class);

  /** Class used for conversion between Json and MTS types. */
  private final JsonMTSTypeConversion typeConversion;

  /** SortMethod enumeration */
  private enum SortMethod {
    NONE,
    REVERSE,
    ASCENDING,
    DESCENDING,
  }

  /** Track the current depth in the json hierarchy during the conversion to html table(s) */
  private int jsonPathDepth;

  // region MTS function user options & defaults
  /** Return a specific order of object keys (if present) to start with */
  private JsonArray optionLeadObjectKeys;

  /** Return a specific order of object keys (if present) to end with */
  private JsonArray optionRearObjectKeys;

  /** Return object keys in a sorted order (for keys not defined as lead and rear object keys */
  private SortMethod optionSortObjectKeys;

  /** Sets any html attributes on the root table */
  private JsonObject optionAttributes;

  /** Adds a caption element to the root table */
  private String optionCaption;

  /** Whether to output html tables with a detail/summary wrapper */
  private boolean optionCollapsible;

  /** Whether a detail/summary wrapper is open by default */
  private Integer optionCollapsibleOpenDepth;

  /** Limit the recursion json path depth */
  private Integer optionDepthLimit;

  /** Whether to output the json path as html title attributes */
  private boolean optionTitles;

  /** Whether to pivot json objects within a json array */
  private boolean optionArrayOfObjects;

  /** Whether to pivot json objects within a json object */
  private boolean optionObjectOfObjects;

  /**
   * whether json values starting with "assetid://" should be returned as a html {@code <img>}
   * element
   */
  private boolean optionAssetImage;

  /**
   * Whether json values containing certain html characters in should be escaped.
   *
   * <p>See {@link JsonHtmlFunctions#escapeHtmlEntities(String)} for a list of html characters
   */
  private boolean optionEscapeHtml;

  /**
   * Whether to sanitize the html returned from jsonToHtmlTable to only things on a safelist.
   *
   * <p>See {@link JsonHtmlFunctions#sanitizeHtml(String)} for the safelist.
   */
  private boolean optionSanitizeHtml;

  /** Whether json values should be converted to input elements for use in a html form */
  private boolean optionInput;

  // endregion

  /**
   * Entry method to convert whatever json into nested html tables. First it determines any
   * conversion settings from either the options provided or from defaults and second it then calls
   * the main recursive method {@link #tabularizeJsonElement(JsonElement, String)}.
   *
   * @param functionName the name of the MTScript function
   * @param jsonElement the json to convert
   * @param options any conversion or html options
   * @return a html table or table of tables
   */
  public String jsonToHtmlTable(String functionName, JsonElement jsonElement, JsonObject options)
      throws ParserException {

    // set variables from provided options
    processJsonToHtmlTableOptions(functionName, options);

    // region create the root html table and process the json
    jsonPathDepth = 0;
    StringBuilder html = new StringBuilder();

    // add the root table
    html.append("<table");

    // if provided add any table attributes
    for (String key : optionAttributes.keySet()) {
      html.append(" ")
          .append(escapeHtmlEntities(key))
          .append("=")
          .append(optionAttributes.getAsJsonPrimitive(key));
    }
    html.append(">");

    // if provided add a table caption
    if (!optionCaption.isEmpty()) {
      html.append("<caption>").append(optionCaption).append("</caption>");
    }

    // add the converted json
    html.append("<tr><td>");
    html.append(tabularizeJsonElement(jsonElement, ""));
    html.append("</td></tr>");
    html.append("</table>");

    if (optionSanitizeHtml) {
      return sanitizeHtml(html.toString());
    } else {
      return html.toString();
    }
  }

  /**
   * Set the options as provided to the MTScript function, or set to default values
   *
   * @param functionName the name of the MTScript function
   * @param options JsonObject containing the option keys (lowercase) and values
   */
  private void processJsonToHtmlTableOptions(String functionName, JsonObject options)
      throws ParserException {
    // region non-boolean options
    optionAttributes = getAsObject(functionName, options, "attributes", new JsonObject());
    optionCaption = getAsString(functionName, options, "caption", "");
    optionCollapsibleOpenDepth = getAsInt(functionName, options, "collapsibleopendepth", -1);
    optionDepthLimit = getAsInt(functionName, options, "depthlimit", 50);
    optionLeadObjectKeys = getAsArray(functionName, options, "leadobjectkeys", new JsonArray());
    optionRearObjectKeys = getAsArray(functionName, options, "rearobjectkeys", new JsonArray());
    optionSortObjectKeys = parseSortMethod(functionName, options, "sortobjectkeys");
    // endregion

    // region boolean options
    optionTitles = getAsBoolean(functionName, options, "titles", true);
    optionCollapsible = getAsBoolean(functionName, options, "collapsible", true);
    optionArrayOfObjects = getAsBoolean(functionName, options, "arrayofobjects", true);
    optionObjectOfObjects = getAsBoolean(functionName, options, "objectofobjects", true);
    optionAssetImage = getAsBoolean(functionName, options, "assetimage", true);
    optionEscapeHtml = getAsBoolean(functionName, options, "escapehtml", true);
    optionSanitizeHtml = getAsBoolean(functionName, options, "sanitizehtml", true);
    optionInput = getAsBoolean(functionName, options, "input", false);
    // endregion
  }

  private boolean getAsBoolean(
      String functionName, JsonObject json, String key, boolean defaultValue)
      throws ParserException {
    if (!json.has(key)) return defaultValue;
    JsonElement element = json.get(key);

    if (element.isJsonPrimitive()) {
      if (element.getAsJsonPrimitive().isBoolean()) return element.getAsBoolean();
      if (element.getAsJsonPrimitive().isNumber())
        return !element.getAsBigDecimal().equals(BigDecimal.ZERO);
    }
    throw new ParserException(
        I18N.getText("macro.function.jsonhtml.onlyBoolean", json.get(key), key, functionName));
  }

  private int getAsInt(String functionName, JsonObject json, String key, int defaultValue)
      throws ParserException {
    if (!json.has(key)) return defaultValue;
    try {
      return Integer.parseInt(json.get(key).getAsString());
    } catch (NumberFormatException e) {
      throw new ParserException(
          I18N.getText("macro.function.jsonhtml.onlyInteger", json.get(key), key, functionName));
    }
  }

  private String getAsString(String functionName, JsonObject json, String key, String defaultValue)
      throws ParserException {
    if (json.has(key) && !json.get(key).isJsonPrimitive()) {
      throw new ParserException(
          I18N.getText("macro.function.jsonhtml.onlyString", json.get(key), key, functionName));
    }
    return json.has(key) ? json.get(key).getAsString() : defaultValue;
  }

  private JsonArray getAsArray(
      String functionName, JsonObject json, String key, JsonArray defaultValue)
      throws ParserException {
    if (json.has(key) && !json.get(key).isJsonArray()) {
      throw new ParserException(
          I18N.getText("macro.function.jsonhtml.onlyArray", json.get(key), key, functionName));
    }
    return json.has(key) ? json.getAsJsonArray(key) : defaultValue;
  }

  private JsonObject getAsObject(
      String functionName, JsonObject json, String key, JsonObject defaultValue)
      throws ParserException {
    if (json.has(key) && !json.get(key).isJsonObject()) {
      throw new ParserException(
          I18N.getText("macro.function.jsonhtml.onlyObject", json.get(key), key, functionName));
    }
    return json.has(key) ? json.getAsJsonObject(key) : defaultValue;
  }

  private SortMethod parseSortMethod(String functionName, JsonObject json, String key)
      throws ParserException {
    String sort = getAsString(functionName, json, key, "n");
    if (sort.isEmpty()) return SortMethod.NONE;
    switch (sort.toLowerCase().charAt(0)) {
      case 'a':
        return SortMethod.ASCENDING;
      case 'd':
        return SortMethod.DESCENDING;
      case 'r':
        return SortMethod.REVERSE;
      case 'n':
        return SortMethod.NONE;
      default:
        throw new ParserException(
            I18N.getText("macro.function.jsonhtml.invalidSortMethod", sort, key, functionName));
    }
  }

  /**
   * Convert json into nested html tables, by recursively looping through the json elements and
   * building the html string for the objects, array, and values.
   *
   * <p>It assesses what type of {@link JsonElement} is at the current iteration json path location,
   * then will either output the value stored at the specific json path, or if another json element
   * recursively call another method to convert the json type found into a table with constituent
   * rows and cells.
   *
   * <ul>
   *   <li>{@link #tabularizeJsonArray(JsonArray, String)}
   *   <li>{@link #tabularizeJsonArrayOfObjects(JsonArray, String)}
   *   <li>{@link #tabularizeJsonObject(JsonObject, String)}
   *   <li>{@link #tabularizeJsonObjectOfObjects(JsonObject, String)}
   *
   * @param jsonElement the json
   * @param jsonPath the current json path which is being processed
   * @return the html
   */
  private String tabularizeJsonElement(JsonElement jsonElement, String jsonPath) {

    if (jsonPathDepth > optionDepthLimit) {
      throw new RuntimeException(
          I18N.getText(
              "macro.function.jsonhtml.exceededDepthLimit", optionDepthLimit, jsonPathDepth));
    }

    StringBuilder html = new StringBuilder();

    switch (jsonElement) {
      case JsonArray ja -> {
        // pivot if we have to
        if (optionArrayOfObjects && isArrayOfObjects(ja)) {
          html.append(tabularizeJsonArrayOfObjects(ja, jsonPath));
        } else {
          html.append(tabularizeJsonArray(ja, jsonPath));
        }
      }
      case JsonObject jo -> {
        // pivot if we have to
        if (optionObjectOfObjects && isObjectOfObjects(jo)) {
          html.append(tabularizeJsonObjectOfObjects(jo, jsonPath));
        } else {
          html.append(tabularizeJsonObject(jo, jsonPath));
        }
      }
      case JsonNull jn -> {
        // provide some indication of the JsonNull value
        html.append("<span")
            .append(htmlAttr("class", "json-null"))
            .append(">")
            .append(jn)
            .append("</span>");
      }
      case null -> {
        // should not get here, but just in case...
        // provide some indication of the JsonElement being Null
        html.append("<span").append(htmlAttr("class", "json-element-null")).append("></span>");
      }
      default -> {
        String jsonValue = typeConversion.jsonToScriptString(jsonElement);
        if (optionInput) {
          // return values as a html text input
          html.append("<input")
              .append(htmlAttr("type", "text"))
              .append(htmlAttr("value", jsonValue))
              .append(htmlAttr("data-json-path", jsonPath))
              .append(">");
        } else {
          if (optionAssetImage && jsonValue.trim().toLowerCase().matches("^asset://.*")) {
            // return asset values as a html img
            html.append("<img")
                .append(htmlAttr("class", "asset"))
                .append(htmlAttr("src", escapeHtmlEntities(jsonValue)))
                .append(">");
          } else if (optionEscapeHtml) {
            // return values as html escaped
            html.append(escapeHtmlEntities(jsonValue));
          } else {
            // otherwise just return the value
            html.append(jsonValue);
          }
        }
      }
    }

    return html.toString();
  }

  /**
   * Convert a json array into a html table.
   *
   * @param jsonArray the json array
   * @param jsonPath the current json path which is being processed
   * @return html
   */
  private String tabularizeJsonArray(JsonArray jsonArray, String jsonPath) {

    jsonPathDepth++;
    StringBuilder html = new StringBuilder();

    // add html details/summary if wanted
    if (optionCollapsible) {
      html.append("<details")
          .append(htmlAttr("class", "json-array"))
          .append(htmlAttr("data-json-path-depth", jsonPathDepth));
      if (!jsonArray.isEmpty() && jsonPathDepth <= optionCollapsibleOpenDepth
          || optionCollapsibleOpenDepth == -1) {
        html.append(" open");
      }
      html.append(">");
      html.append("<summary").append(htmlAttr("class", "json-array")).append(">");
      html.append("<span")
          .append(htmlAttr("class", "json-array"))
          .append(">")
          .append(String.format("Array [%s]", jsonArray.size()))
          .append("</span>");
      html.append("</summary>");
    }

    // add a table to hold the array data
    html.append("<table").append(htmlAttr("class", "json-array")).append(">");
    html.append("<tbody>");
    // loop through each item in the array
    for (int i = 0; i < jsonArray.size(); i++) {
      String currentJsonPath = jsonPath + "[" + i + "]";

      // add a row for each item
      html.append("<tr>");

      // add a row header
      html.append("<th")
          .append(htmlAttrStandard("json-array", currentJsonPath, null, i))
          .append(htmlAttr("scope", "row"))
          .append(">")
          .append(i)
          .append("</th>");

      // add the contents
      html.append("<td")
          .append(htmlAttrStandard("json-value", currentJsonPath, null, i))
          .append(">")
          .append(tabularizeJsonElement(jsonArray.get(i), currentJsonPath))
          .append("</td>");
      html.append("</tr>");
    }
    html.append("</tbody>");

    html.append("</table>");
    if (optionCollapsible) {
      html.append("</details>");
    }
    jsonPathDepth--;
    return html.toString();
  }

  /**
   * Pivots a json array of json objects into a html table with the array as rows and child objects
   * as columns.
   *
   * @param jsonArray the json array of json objects.
   * @param jsonPath the json path to this json array
   * @return a html table
   */
  private String tabularizeJsonArrayOfObjects(JsonArray jsonArray, String jsonPath) {

    jsonPathDepth++;
    StringBuilder html = new StringBuilder();

    // compile a set of unique keys across all child objects - these will be the table column
    // headers.  If the child objects have a vastly different structure, then maybe
    // pivoting is not appropriate.
    HashSet<String> childKeys = new HashSet<>();
    int joKeysMin = 0;
    int joKeysMax = 0;
    for (int i = 0; i < jsonArray.size(); i++) {
      if (jsonArray.get(i) instanceof JsonObject jo) {
        childKeys.addAll(jo.keySet());
        if (i == 0) {
          joKeysMin = jo.size();
          joKeysMax = jo.size();
        } else {
          joKeysMin = Math.min(joKeysMin, jo.size());
          joKeysMax = Math.max(joKeysMax, jo.size());
        }
      }
    }

    // order child object keys
    LinkedHashSet<String> orderedChildKeys = orderKeys(childKeys);

    // add html details/summary if wanted
    if (optionCollapsible) {
      html.append("<details")
          .append(htmlAttr("class", "json-array-of-objects"))
          .append(htmlAttr("data-json-path-depth", jsonPathDepth));
      if (!jsonArray.isEmpty() && jsonPathDepth <= optionCollapsibleOpenDepth
          || optionCollapsibleOpenDepth == -1) {
        html.append(" open");
      }
      html.append(">");
      html.append("<summary").append(htmlAttr("class", "json-array-of-objects")).append(">");
      html.append("<span")
          .append(htmlAttr("class", "json-array"))
          .append(">")
          .append(String.format("Array [%s]", jsonArray.size()))
          .append("</span>");
      html.append(" of ");
      String summaryTextObjects =
          (joKeysMin == joKeysMax && joKeysMin == childKeys.size())
              ? String.format("Objects {%s}", childKeys.size())
              : String.format("Objects {%s (%s-%s)}", childKeys.size(), joKeysMin, joKeysMax);
      html.append("<span")
          .append(htmlAttr("class", "json-object"))
          .append(">")
          .append(summaryTextObjects)
          .append("</span>");
      html.append("</summary>");
    }

    // add the table
    html.append("<table").append(htmlAttr("class", "json-array-of-objects")).append(">");
    html.append("<thead>");

    // add the table header row for the array index and for each unique object key
    html.append("<tr>");

    // parent array index column header
    String jsonPathArrayHeader = jsonPath + "[*]";
    html.append("<th")
        .append(htmlAttrStandard("json-array-of-objects", jsonPathArrayHeader, null, "*"))
        .append(htmlAttr("scope", "col"))
        .append(">")
        .append("*")
        .append("</th>");

    // child object key column headers
    for (String childKey : orderedChildKeys) {
      String jsonPathObjectHeader = jsonPathArrayHeader + "['" + childKey + "']";
      html.append("<th")
          .append(htmlAttrStandard("json-object", jsonPathObjectHeader, childKey, null))
          .append(htmlAttr("scope", "col"))
          .append(">")
          .append(optionEscapeHtml ? escapeHtmlEntities(childKey) : childKey)
          .append("</th>");
    }
    html.append("</tr>");
    html.append("</thead>");

    html.append("<tbody>");
    jsonPathDepth++;
    // add a row for every item in the array, adding a column for the array index and for the
    // content of each object key
    for (int i = 0; i < jsonArray.size(); i++) {

      html.append("<tr>");

      // parent array index row header
      String jsonPathArray = jsonPath + "[" + i + "]";
      html.append("<th")
          .append(htmlAttrStandard("json-array-of-objects", jsonPathArray, null, i))
          .append(htmlAttr("scope", "row"))
          .append(">")
          .append(i)
          .append("</th>");

      // get the child json object for this parent array item
      JsonObject jo = (JsonObject) jsonArray.get(i);

      // loop through each key in the list of child object keys
      for (String childKey : orderedChildKeys) {
        String jsonPathChildObject = jsonPathArray + "['" + childKey + "']";
        if (jo.get(childKey) != null) {
          html.append("<td")
              .append(htmlAttrStandard("json-value", jsonPathChildObject, childKey, i))
              .append(">")
              .append(tabularizeJsonElement(jo.get(childKey), jsonPathChildObject))
              .append("</td>");
        } else {
          // some child objects may not have a key in the compiled list of keys from all objects
          html.append("<td")
              .append(htmlAttrStandard("json-path-leaf-missing", jsonPathChildObject, childKey, i))
              .append(">")
              .append("</td>");
        }
      }
      html.append("</tr>");
    }
    jsonPathDepth--;
    html.append("</tbody>");
    html.append("</table>");

    if (optionCollapsible) {
      html.append("</details>");
    }
    jsonPathDepth--;
    return html.toString();
  }

  /**
   * Convert a json object into a html table.
   *
   * @param jsonObject the json object
   * @param jsonPath the current json path which is being processed
   * @return html
   */
  private String tabularizeJsonObject(JsonObject jsonObject, String jsonPath) {

    jsonPathDepth++;
    StringBuilder html = new StringBuilder();

    // add html details/summary if wanted
    if (optionCollapsible) {
      html.append("<details")
          .append(htmlAttr("class", "json-object"))
          .append(htmlAttr("data-json-path-depth", jsonPathDepth));
      if (!jsonObject.isEmpty() && jsonPathDepth <= optionCollapsibleOpenDepth
          || optionCollapsibleOpenDepth == -1) {
        html.append(" open");
      }
      html.append(">");
      html.append("<summary").append(htmlAttr("class", "json-object")).append(">");
      html.append("<span")
          .append(htmlAttr("class", "json-object"))
          .append(">")
          .append(String.format("Object {%s}", jsonObject.size()))
          .append("</span>");
      html.append("</summary>");
    }

    // add a table to hold the object data
    html.append("<table").append(htmlAttr("class", "json-object")).append(">");
    html.append("<tbody>");
    LinkedHashSet<String> orderedObjectKeys = orderKeys(jsonObject.keySet());

    // loop through each key in the list of object keys
    for (String key : orderedObjectKeys) {
      String currentJsonPath = jsonPath + "['" + key + "']";

      // add a row for each object key
      html.append("<tr>");

      // add a row header
      html.append("<th")
          .append(htmlAttrStandard("json-object", currentJsonPath, key, null))
          .append(htmlAttr("scope", "row"))
          .append(">")
          .append(optionEscapeHtml ? escapeHtmlEntities(key) : key)
          .append("</th>");

      // add the contents
      html.append("<td")
          .append(htmlAttrStandard("json-value", currentJsonPath, key, null))
          .append(">")
          .append(tabularizeJsonElement(jsonObject.get(key), currentJsonPath))
          .append("</td>");
      html.append("</tr>");
    }
    html.append("</tbody>");

    html.append("</table>");
    if (optionCollapsible) {
      html.append("</details>");
    }
    jsonPathDepth--;
    return html.toString();
  }

  /**
   * Pivots a json object of json objects into a html table with the object as rows and child
   * objects as columns.
   *
   * @param jsonObject the json object of json objects.
   * @param jsonPath the json path to this json array
   * @return a html table
   */
  private String tabularizeJsonObjectOfObjects(JsonObject jsonObject, String jsonPath) {

    jsonPathDepth++;
    StringBuilder html = new StringBuilder();

    // compile a set of unique keys across all child objects - these will be the table column
    // headers.  If the child objects have a vastly different structure, then maybe
    // pivoting is not appropriate.
    HashSet<String> childKeys = new HashSet<>();
    int joKeysMin = 0;
    int joKeysMax = 0;
    int i = 0;
    for (String key : jsonObject.keySet()) {
      if (jsonObject.get(key) instanceof JsonObject jo) {
        childKeys.addAll(jo.keySet());
        if (i == 0) {
          joKeysMin = jo.size();
          joKeysMax = jo.size();
        } else {
          joKeysMin = Math.min(joKeysMin, jo.size());
          joKeysMax = Math.max(joKeysMax, jo.size());
        }
      }
      i++;
    }

    // order child object keys
    LinkedHashSet<String> orderedChildKeys = orderKeys(childKeys);

    // add html details/summary if wanted
    if (optionCollapsible) {
      html.append("<details")
          .append(htmlAttr("class", "json-object-of-objects"))
          .append(htmlAttr("data-json-path-depth", jsonPathDepth));
      if (!jsonObject.isEmpty() && jsonPathDepth <= optionCollapsibleOpenDepth
          || optionCollapsibleOpenDepth == -1) {
        html.append(" open");
      }
      html.append(">");
      html.append("<summary").append(htmlAttr("class", "json-object-of-objects")).append(">");
      html.append("<span")
          .append(htmlAttr("class", "json-object"))
          .append(">")
          .append(String.format("Object {%s}", jsonObject.size()))
          .append("</span>");
      html.append(" of ");
      String summaryTextObjects =
          (joKeysMin == joKeysMax && joKeysMin == childKeys.size())
              ? String.format("Objects {%s}", childKeys.size())
              : String.format("Objects {%s (%s-%s)}", childKeys.size(), joKeysMin, joKeysMax);
      html.append("<span")
          .append(htmlAttr("class", "json-object"))
          .append(">")
          .append(summaryTextObjects)
          .append("</span>");
      html.append("</summary>");
    }

    // add the table
    html.append("<table").append(htmlAttr("class", "json-object-of-objects")).append(">");
    html.append("<thead>");

    // add the table header row for the parent object and for each unique child object key
    html.append("<tr>");

    // parent object key column header
    String jsonPathParentObjectHeader = jsonPath + "['*']";
    html.append("<th")
        .append(htmlAttrStandard("json-object-of-objects", jsonPathParentObjectHeader, "*", null))
        .append(htmlAttr("scope", "col"))
        .append(">")
        .append("*")
        .append("</th>");

    // child object key column headers
    for (String childKey : orderedChildKeys) {
      String jsonPathChildObjectHeader = jsonPathParentObjectHeader + "['" + childKey + "']";
      html.append("<th")
          .append(htmlAttrStandard("json-object", jsonPathChildObjectHeader, childKey, null))
          .append(htmlAttr("scope", "col"))
          .append(">")
          .append(optionEscapeHtml ? escapeHtmlEntities(childKey) : childKey)
          .append("</th>");
    }
    html.append("</tr>");
    html.append("</thead>");

    html.append("<tbody>");
    jsonPathDepth++;

    // order parent object keys
    HashSet<String> parentKeys = new HashSet<>(jsonObject.keySet());
    LinkedHashSet<String> orderedParentKeys = orderKeys(parentKeys);

    // add a row for every key in the parent object
    // add a column for the parent key and for the content of each object key
    for (String parentKey : orderedParentKeys) {
      html.append("<tr>");

      // parent object key row header
      String jsonPathParentObject = jsonPath + "['" + parentKey + "']";
      html.append("<th")
          .append(htmlAttrStandard("json-object-of-objects", jsonPathParentObject, parentKey, null))
          .append(htmlAttr("scope", "row"))
          .append(">")
          .append(optionEscapeHtml ? escapeHtmlEntities(parentKey) : parentKey)
          .append("</th>");

      // get the child json object for this parent object key
      JsonObject jo = (JsonObject) jsonObject.get(parentKey);

      // loop through each key in the list of child object keys
      for (String childKey : orderedChildKeys) {
        String jsonPathChildObject = jsonPathParentObject + "['" + childKey + "']";
        if (jo.get(childKey) != null) {
          html.append("<td")
              .append(htmlAttrStandard("json-value", jsonPathChildObject, childKey, null))
              .append(">")
              .append(tabularizeJsonElement(jo.get(childKey), jsonPathChildObject))
              .append("</td>");
        } else {
          // some child objects may not have a key in the compiled list of keys from all objects
          html.append("<td")
              .append(
                  htmlAttrStandard("json-path-leaf-missing", jsonPathChildObject, childKey, null))
              .append(">")
              .append("</td>");
        }
      }
      html.append("</tr>");
    }
    jsonPathDepth--;
    html.append("</tbody>");

    html.append("</table>");
    if (optionCollapsible) {
      html.append("</details>");
    }
    jsonPathDepth--;
    return html.toString();
  }

  /**
   * Standardize html attribute additions for given data type as they require a leading space.
   *
   * <p>Note that the {@link #sanitizeHtml(String)} will quote any unquoted html attribute values
   * anyway, so no point handling different data type separately (e.g. json-array-index=1 despite
   * being HTML5 compliant gets converted by jsoup clean to json-array-index="1" and jsoup does not
   * offer a way of preventing this).
   *
   * @param attribute the html attribute
   * @param value the html attribute's value
   * @return a formatted string representing the html attribute and value
   */
  private String htmlAttr(String attribute, Object value) {
    return String.format(" %s=\"%s\"", attribute, value.toString());
  }

  /**
   * Standardizes html element opening tag attributes for jsonToHtml
   *
   * @param classAttr the class name
   * @param jsonPath the json path
   * @param jsonKey the json key
   * @param jsonIndex the json index
   * @return a formatted string representing the html attributes and values
   */
  private String htmlAttrStandard(
      String classAttr, String jsonPath, String jsonKey, Object jsonIndex) {
    return (classAttr != null ? htmlAttr("class", classAttr) : "")
        + (optionTitles && jsonPath != null ? htmlAttr("title", jsonPath) : "")
        + (jsonPath != null ? htmlAttr("data-json-path", jsonPath) : "")
        + (jsonKey != null ? htmlAttr("data-json-key", jsonKey) : "")
        + (jsonIndex != null ? htmlAttr("data-json-index", jsonIndex) : "");
  }

  /**
   * Replace specific characters (e.g. <code>&<>\'</code>) with html entity escaped versions.
   *
   * @param string the string which may contain characters to be escaped
   * @return the html with specific characters escaped
   */
  private String escapeHtmlEntities(String string) {
    // escapeHtml4 does not replace apostrophes so we do that
    return escapeHtml4(string).replaceAll("'", "&apos;");
  }

  /**
   * Remove any html tags and attributes not defined on the safelist below.
   *
   * @param html the html to sanitize
   * @return the sanitized html
   */
  private String sanitizeHtml(String html) {
    Safelist safelist =
        Safelist.basic()
            .addTags(
                "table", "caption", "thead", "tbody", "tfoot", "tr", "th", "td", "details",
                "summary", "img", "input")
            .addAttributes(
                ":all",
                "id",
                "class",
                "title",
                "data-json-path",
                "data-json-key",
                "data-json-index",
                "data-json-path-depth")
            .addAttributes("details", "open")
            .addAttributes("img", "src")
            .addAttributes("input", "type", "value", "min", "max", "step")
            .addAttributes("th", "scope")
            .addProtocols("img", "src", "asset");
    return Jsoup.clean(html, safelist);
  }

  /**
   * Check if a json array only contains json objects.
   *
   * @param jsonArray the json array to check
   * @return true if the json array just contains json objects, otherwise false
   */
  private boolean isArrayOfObjects(JsonArray jsonArray) {
    boolean check = true;
    if (jsonArray.isEmpty()) {
      check = false;
    } else {
      for (int i = 0; i < jsonArray.size(); i++) {
        if (!jsonArray.get(i).isJsonObject()) {
          check = false;
          break;
        }
      }
    }
    return check;
  }

  /**
   * Check if a json object only contains other json objects.
   *
   * @param jsonObject the json object to check
   * @return true if the json object just contains other json object, otherwise false
   */
  private boolean isObjectOfObjects(JsonObject jsonObject) {
    boolean check = true;
    if (jsonObject.isEmpty()) {
      check = false;
    } else {
      for (String key : jsonObject.keySet()) {
        if (!jsonObject.get(key).isJsonObject()) {
          check = false;
          break;
        }
      }
    }
    return check;
  }

  /**
   * Order a set of json object keys, using optional lead/rear object keys to position them at the
   * start/end, and an optional sort method for all keys in between .
   *
   * @param jsonKeys the keys
   * @return a ordered list of keys
   */
  private LinkedHashSet<String> orderKeys(Set<String> jsonKeys) {

    LinkedHashSet<String> orderedObjectKeys = new LinkedHashSet<>();

    // add lead object keys to the set
    if (!optionLeadObjectKeys.isEmpty()) {
      for (int i = 0; i < optionLeadObjectKeys.size(); i++) {
        String leadObjectKey = optionLeadObjectKeys.get(i).getAsString();
        if (jsonKeys.contains(leadObjectKey)) {
          orderedObjectKeys.add(leadObjectKey);
        }
      }
    }

    // sort all object keys by the sort method
    List<String> sortedObjectKeys;
    if (optionSortObjectKeys.equals(SortMethod.ASCENDING)) {
      sortedObjectKeys = jsonKeys.stream().sorted().toList();
    } else if (optionSortObjectKeys.equals(SortMethod.DESCENDING)) {
      sortedObjectKeys = jsonKeys.stream().sorted().toList().reversed();
    } else if (optionSortObjectKeys.equals(SortMethod.REVERSE)) {
      sortedObjectKeys = jsonKeys.stream().toList().reversed();
    } else {
      sortedObjectKeys = jsonKeys.stream().toList();
    }

    // add sorted object keys to the set
    orderedObjectKeys.addAll(sortedObjectKeys);

    // add rear object keys to the set
    if (!optionRearObjectKeys.isEmpty()) {
      for (int i = 0; i < optionRearObjectKeys.size(); i++) {
        String rearObjectKey = optionRearObjectKeys.get(i).getAsString();
        if (jsonKeys.contains(rearObjectKey)) {
          orderedObjectKeys.remove(rearObjectKey);
          orderedObjectKeys.add(rearObjectKey);
        }
      }
    }
    return orderedObjectKeys;
  }
}
