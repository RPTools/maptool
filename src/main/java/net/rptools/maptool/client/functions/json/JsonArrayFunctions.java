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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

/** Class used to implement MT Script related Json functions / utilities for JsonArrays. */
public class JsonArrayFunctions {

  /** Class used to compare numeric {@link JsonElement}s. */
  private static class JsonNumberComparator implements Comparator<JsonElement> {
    @Override
    public int compare(JsonElement o1, JsonElement o2) {
      return o1.getAsBigDecimal().compareTo(o2.getAsBigDecimal());
    }
  }

  /** Class used to compare non numeric {@link JsonElement}s. */
  private static class JsonStringComparator implements Comparator<JsonElement> {
    @Override
    public int compare(JsonElement o1, JsonElement o2) {
      return o1.getAsString().compareTo(o2.getAsString());
    }
  }

  /**
   * Class used to compare two {@link JsonObject}s using {@link Comparator}s for each of the fields.
   */
  private static class JsonObjectComparator implements Comparator<JsonObject> {
    private final List<String> fields;
    private final List<Comparator<JsonElement>> comparators;

    /**
     * Creates a new instance of <code>JsonObjectComparator</code>.
     *
     * @param fields The fields used to compare {@link JsonObject}s.
     * @param comparators The {@link Comparator}s used to compare {@link JsonObject}s.
     */
    private JsonObjectComparator(List<String> fields, List<Comparator<JsonElement>> comparators) {
      this.fields = fields;
      this.comparators = comparators;
    }

    public int compare(JsonObject jo1, JsonObject jo2) {

      for (int i = 0; i < fields.size(); i++) {
        String field = fields.get(i);
        int c = comparators.get(i).compare(jo1.get(field), jo2.get(field));
        if (c != 0) {
          return c;
        }
      }
      return 0;
    }
  }

  /** Class used for conversion between Json and MTS types. */
  private final JsonMTSTypeConversion typeConversion;

  /** An empty {@link JsonArray}. */
  public static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();

  /**
   * Creates a new <code>JsonArrayFunctions</code> instance.
   *
   * @param converter the {@link JsonMTSTypeConversion} used to convert primitive types between json
   *     and MTS.
   */
  JsonArrayFunctions(JsonMTSTypeConversion converter) {
    typeConversion = converter;
  }

  /**
   * Converts a string list to json array.
   *
   * @param strList The string list to convert.
   * @param delim The delimiter to use to split the string list.
   * @return the string list as a json array.
   */
  public JsonArray fromStringList(String strList, String delim) {
    String[] list = StringUtil.split(strList, delim);

    // An Empty list should generate an empty JSON array.
    if (list.length == 1 && list[0].length() == 0) {
      return EMPTY_JSON_ARRAY;
    }

    JsonArray jsonArray = new JsonArray();

    for (String s : list) {
      // Try to convert the value to a number and if that works we store it that way
      jsonArray.add(typeConversion.convertPrimitiveFromString(s.trim()));
    }

    return jsonArray;
  }

  /**
   * Converts a {@link JsonArray} into a MT Script string property.
   *
   * @param jsonArray The JsonArray to convert.
   * @param delim The delimiter to user in converted string property.
   * @return the resultant string property.
   */
  public String toStringProp(JsonArray jsonArray, String delim) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < jsonArray.size(); i++) {
      if (sb.length() > 0) {
        sb.append(delim);
      }
      sb.append(i).append("=").append(typeConversion.asScriptType(jsonArray.get(i)));
    }
    return sb.toString();
  }

  /**
   * Converts the argument into a Json Array. If the argument is a primitive type then a {@link
   * JsonArray} containing only that primitive will be returned.
   *
   * @param o The {@link Object} to coerce into a {@link JsonArray}.
   * @return either the argument converted to a {@link JsonArray} or a {@link JsonArray} containing
   *     the object.
   */
  public JsonArray coerceToJsonArray(Object o) {
    JsonElement jsonElement = typeConversion.asJsonElement(o);

    if (jsonElement.isJsonArray()) {
      return jsonElement.getAsJsonArray();
    }

    JsonArray jsonArray = new JsonArray();
    jsonArray.add(jsonElement);

    return jsonArray;
  }

  /**
   * Concatenates multiple {@link JsonArray}s returning a new array.
   *
   * @param arrays the {@link JsonArray}s to concatenate.
   * @return a new {@link JsonArray} which is a concatenation of the passed in arrays.
   */
  public JsonArray concatenate(List<JsonArray> arrays) {
    JsonArray result = new JsonArray();
    for (JsonArray array : arrays) {
      result.addAll(array);
    }
    return result;
  }

  /**
   * Concatenates a {@link JsonArray} and other values returning a new {@link JsonArray}.
   *
   * @param array The {@link JsonArray} top copy into the new array.
   * @param values the values to concatenate onto the end of the new array.
   * @return a new {@link JsonArray} containing the concatenated arguments.
   */
  public JsonArray concatenate(JsonArray array, List<?> values) {
    JsonArray array2 = new JsonArray();
    array2.addAll(array);
    for (Object value : values) {
      array2.add(typeConversion.asJsonElement(value));
    }
    return array2;
  }

  /**
   * Returns a new {@link JsonArray} with the specified index removed.
   *
   * @param jsonArray the array to to operate on.
   * @param index the index of the item to be excluded in the result.
   * @return a new {@link JsonArray} without the specified index.
   */
  public JsonArray remove(JsonArray jsonArray, int index) {
    JsonArray result = new JsonArray();
    result.addAll(jsonArray);
    result.remove(index);
    return result;
  }

  /**
   * Returns a copy of a {@link JsonArray} sorted in ascending order. If all of the elements in the
   * array can be converted to numeric then the will be sorted as numbers, otherwise they will be
   * sorted as string.
   *
   * @param jsonArray the array to sort.
   * @return sorted copy of the array.
   */
  public JsonArray sortAscending(JsonArray jsonArray) {
    List<JsonElement> list = jsonArrayToList(jsonArray);
    if (allNumbers(list)) {
      list.sort(new JsonNumberComparator());
    } else {
      list.sort(new JsonStringComparator());
    }

    return listToJsonArray(list);
  }

  /**
   * Returns a copy of a {@link JsonArray} sorted in descending order. If all of the elements in the
   * array can be converted to numeric then the will be sorted as numbers, otherwise they will be
   * sorted as string.
   *
   * @param jsonArray the array to sort.
   * @return sorted copy of the array.
   */
  public JsonArray sortDescending(JsonArray jsonArray) {
    List<JsonElement> list = jsonArrayToList(jsonArray);
    if (allNumbers(list)) {
      list.sort(new JsonNumberComparator().reversed());
    } else {
      list.sort(new JsonStringComparator().reversed());
    }

    return listToJsonArray(list);
  }

  /**
   * This method sorts a list of {@link JsonObject}s using the specified fields. If the fields can
   * be converted to a number it will be sorted numerically otherwise as a string.
   *
   * @param list the list of {@link JsonObject}s to sort.
   * @param fields the fields to sort by.
   * @throws ParserException if not all objects contain the keys to sort by.
   */
  private void sortObjects(List<JsonObject> list, List<String> fields) throws ParserException {
    var comparators = new ArrayList<Comparator<JsonElement>>(fields.size());

    for (String field : fields) {
      if (allNumbers(list, field)) {
        comparators.add(new JsonNumberComparator());
      } else {
        comparators.add(new JsonStringComparator());
      }
    }

    list.sort(new JsonObjectComparator(fields, comparators));
  }

  /**
   * Sorts a {@link JsonArray} of objects in ascending order by the value of the passed in keys.
   *
   * @param jsonArray the array to return a sorted copy of.
   * @param fields the fields to sort by.
   * @return sorted copy of the passed in list.
   * @throws ParserException if all of the objects do not have the required fields.
   */
  public JsonArray sortObjectsAscending(JsonArray jsonArray, List<String> fields)
      throws ParserException {
    List<JsonObject> list = jsonArrayToListOfObjects(jsonArray);
    sortObjects(list, fields);
    return listToJsonArray(list);
  }

  /**
   * Sorts a {@link JsonArray} of objects in descending order by the value of the passed in keys.
   *
   * @param jsonArray the array to return a sorted copy of.
   * @param fields the fields to sort by.
   * @return sorted copy of the passed in list.
   * @throws ParserException if all of the objects do not have the required fields.
   */
  public JsonArray sortObjectsDescending(JsonArray jsonArray, List<String> fields)
      throws ParserException {
    List<JsonObject> list = jsonArrayToListOfObjects(jsonArray);
    sortObjects(list, fields);
    Collections.reverse(list);
    return listToJsonArray(list);
  }

  /**
   * Converts a {@link JsonArray} into a list of {@link JsonElement}s.
   *
   * @param jsonArray the <code>JsonArray</code> to convert.
   * @return list of {@link JsonElement}s.
   */
  private List<JsonElement> jsonArrayToList(JsonArray jsonArray) {
    List<JsonElement> list = new ArrayList<>(jsonArray.size());
    for (JsonElement ele : jsonArray) {
      list.add(ele);
    }

    return list;
  }

  /**
   * Converts a {@link JsonArray} to a list of {@link JsonObject}s.
   *
   * @param jsonArray the <code>JsonArray</code> to convert.
   * @return List containing the {@link JsonObject}s that are in the {@link JsonArray}.
   * @throws ParserException if the {@link JsonArray} contains anything other than {@link
   *     JsonObject}.
   */
  private List<JsonObject> jsonArrayToListOfObjects(JsonArray jsonArray) throws ParserException {
    List<JsonObject> list = new ArrayList<>(jsonArray.size());
    for (JsonElement ele : jsonArray) {
      if (!ele.isJsonObject()) {
        throw new ParserException(
            I18N.getText("macro.function.json.arrayMustContainObjects", ele.toString()));
      }
      list.add(ele.getAsJsonObject());
    }

    return list;
  }

  /**
   * Converts a list of {@link JsonElement}s to a {@link JsonArray}.
   *
   * @param list the list of {@link JsonElement}s to convert.
   * @return a {@link JsonArray} containing all the {@link JsonElement}s.
   */
  private JsonArray listToJsonArray(List<? extends JsonElement> list) {
    JsonArray jsonArray = new JsonArray();
    for (JsonElement ele : list) {
      jsonArray.add(ele);
    }

    return jsonArray;
  }

  /**
   * Checks to see if all of the {@link JsonElement}s in the list can be converted to a number.
   *
   * @param list the list to check.
   * @return <code>true</code> if all {@link JsonElement}s can be converted to a number.
   */
  private boolean allNumbers(List<JsonElement> list) {
    for (JsonElement jsonElement : list) {
      if (!jsonElement.isJsonPrimitive()) {
        return false;
      }

      try {
        jsonElement.getAsBigDecimal();
      } catch (NumberFormatException nfe) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the {@link JsonArray} contains the specified value.
   *
   * @param jsonArray the {@link JsonArray} to check.
   * @param value the value to check for.
   * @return <code>true</code> if the {@link}
   */
  public boolean contains(JsonArray jsonArray, Object value) {
    JsonElement jsonValue = typeConversion.asJsonElement(value);
    for (int i = 0; i < jsonArray.size(); i++) {
      if (jsonArray.get(i).equals(jsonValue)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks to see if the specified key in all of the {@link JsonObject}s in the list can be
   * converted to a number.
   *
   * @param list the list of {@link JsonObject}s to checks to check
   * @param key the key to check in each of the {@link JsonObject}s.
   * @return <code>true</code> if they key for all the {@link JsonObject}s can be converted to a
   *     number.
   * @throws ParserException if any of the {@link JsonObject}s do not contain the key.
   */
  private boolean allNumbers(List<JsonObject> list, String key) throws ParserException {
    for (JsonObject jsonObject : list) {
      if (!jsonObject.has(key)) {
        throw new ParserException(I18N.getText("macro.function.json.notAllContainKey", key));
      }

      JsonElement jsonVal = jsonObject.get(key);
      if (!jsonVal.isJsonPrimitive()) {
        return false;
      }

      try {
        jsonVal.getAsBigDecimal();
      } catch (NumberFormatException nfe) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns a new {@link JsonArray} with the contents of the passed in {@link JsonArray} shuffled.
   *
   * @param jsonArray the {@link JsonArray} to copy and shuffle.
   * @return the new {@link JsonArray} with shuffled contents of the passed in array.
   */
  public JsonArray shuffle(JsonArray jsonArray) {
    List<JsonElement> list = jsonArrayToList(jsonArray);
    Collections.shuffle(list);
    return listToJsonArray(list);
  }

  /**
   * Returns a new {@link JsonArray} with the contents of the passed in {@link JsonArray} reversed.
   *
   * @param jsonArray the {@link JsonArray} to copy and reversed.
   * @return the new {@link JsonArray} with reversed contents of the passed in array.
   */
  public JsonArray reverse(JsonArray jsonArray) {
    List<JsonElement> list = jsonArrayToList(jsonArray);
    Collections.reverse(list);
    return listToJsonArray(list);
  }

  /**
   * Checks if the {@link JsonArray} is empty.
   *
   * @param jsonArray the <code>JsonArray</code> to check.
   * @return <code>true</code> if there is nothing in the array otherwise <code>false</code>/
   */
  public boolean isEmpty(JsonArray jsonArray) {
    return jsonArray.size() == 0;
  }

  /**
   * Returns the number of occurrences of a value in a {@link JsonArray} starting at a specific
   * index.
   *
   * @param jsonArray The {@link JsonArray} to search through.
   * @param value The value to search for.
   * @param start The first index in the {@link JsonArray} to search from.
   * @return The number of times the value occurs in the array.
   */
  public long count(JsonArray jsonArray, JsonElement value, int start) {
    long count = 0;
    for (int i = start; i < jsonArray.size(); i++) {
      JsonElement jsonElement = jsonArray.get(i);
      if (jsonElement.equals(value)) {
        count++;
      }
    }

    return count;
  }

  /**
   * Returns the index of the first occurrence of a value in a {@link JsonArray} starting at the
   * specified index. If the value is not found then -1 is returned.
   *
   * @param jsonArray The {@link JsonArray} to check.
   * @param value The value to check for.
   * @param start The index in the array to start at.
   * @return the index of the value or -1 if it is not found.
   */
  public long indexOf(JsonArray jsonArray, JsonElement value, int start) {
    for (int i = start; i < jsonArray.size(); i++) {
      JsonElement jsonElement = jsonArray.get(i);
      if (jsonElement.equals(value)) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Returns a new {@link JsonArray} which contains the contents of all passed in arrays merged.
   *
   * @param arrays The arrays to merge.
   * @return The merged array.
   */
  public JsonArray merge(List<JsonArray> arrays) {
    JsonArray result = new JsonArray();
    for (JsonArray array : arrays) {
      result.addAll(array);
    }

    return result;
  }

  /**
   * Returns a new {@link JsonArray} with the unique contents of the passed in array.
   *
   * @param jsonArray the {@link JsonArray} to get the unique values of.
   * @return a new {@link JsonArray} containing the unique values in the passed in array.
   */
  public JsonArray unique(JsonArray jsonArray) {
    List<JsonElement> list = jsonArrayToList(jsonArray);
    List<JsonElement> unique = list.stream().distinct().collect(Collectors.toList());

    return listToJsonArray(unique);
  }

  /**
   * Merges multiple lists of {@link JsonElement}s into a single new list.
   *
   * @param lists The list of lists to merge.
   * @return the new list with all elements merged.
   */
  private List<JsonElement> mergeLists(List<List<JsonElement>> lists) {
    List<JsonElement> result = new ArrayList<>();
    for (var list : lists) {
      result.addAll(list);
    }

    return result;
  }

  /**
   * Merges a list of {@link JsonElement}s into a single new list.
   *
   * @param arrays The list of arrays to merge.
   * @return the new list with all elements merged.
   */
  private List<JsonElement> mergeJsonArraysToList(List<JsonArray> arrays) {
    List<List<JsonElement>> lists = new ArrayList<>();
    for (var array : arrays) {
      lists.add(jsonArrayToList(array));
    }

    return mergeLists(lists);
  }

  /**
   * Returns a new {@link JsonArray} with the contents of the other passed in arrays removed from
   * it.
   *
   * @param jsonArray The array with initial values.
   * @param removed The list of arrays of values to remove.
   * @return The new resulting array.
   */
  public JsonArray removeAll(JsonArray jsonArray, List<JsonArray> removed) {
    List<JsonElement> result = jsonArrayToList(jsonArray);
    List<JsonElement> toRemove = mergeJsonArraysToList(removed);

    result.removeAll(toRemove);

    return listToJsonArray(result);
  }

  /**
   * Converts the keys of a {@link JsonObject} to a list of {@link JsonElement}s (actually {@link
   * JsonPrimitive}).
   *
   * @param jsonObject the {@link JsonElement} to extract the keys from.
   * @return a list of the keys.
   */
  private List<JsonElement> objectKeysAsElements(JsonObject jsonObject) {
    List<JsonElement> jsonElements = new ArrayList<>();
    for (String key : jsonObject.keySet()) {
      jsonElements.add(new JsonPrimitive(key));
    }

    return jsonElements;
  }

  /**
   * Performs a union on multiple {@link JsonArray}/{@link JsonObject}s returning a new {@link
   * JsonArray} with the result. Note: this is not a merge, if the same value appears more than once
   * in the input {@link JsonArray}/{@link JsonObject} it will only appear once in the output.
   *
   * @param elements The {@link JsonArray}s to perform the merge on.
   * @return the result of the union.
   */
  public JsonArray union(List<JsonElement> elements) {
    List<JsonElement> allElements = new ArrayList<>();
    for (JsonElement jsonElement : elements) {
      if (jsonElement.isJsonObject()) {
        allElements.addAll(objectKeysAsElements(jsonElement.getAsJsonObject()));
      } else {
        allElements.addAll(jsonArrayToList(jsonElement.getAsJsonArray()));
      }
    }
    List<JsonElement> unique = allElements.stream().distinct().collect(Collectors.toList());

    return listToJsonArray(unique);
  }

  /**
   * Returns a new {@link JsonArray} with an intersection of all passed in {@link JsonArray}/{@link
   * JsonObject}s.
   *
   * @param elements The {@link JsonArray}s to take the intersection of.
   * @return a {@link JsonArray} containing an intersection of the passed in {@link
   *     JsonArray}/{@link JsonObject}s.
   */
  public JsonArray intersection(List<JsonElement> elements) {
    List<JsonElement> intersection = new ArrayList<>();

    boolean firstTime = true;
    for (JsonElement jsonElement : elements) {
      List<JsonElement> ele;
      if (jsonElement.isJsonArray()) {
        ele = jsonArrayToList(jsonElement.getAsJsonArray());
      } else {
        ele = objectKeysAsElements(jsonElement.getAsJsonObject());
      }
      if (firstTime) {
        intersection.addAll(ele);
        firstTime = false;
      } else {
        intersection.retainAll(ele);
      }
    }

    return listToJsonArray(intersection);
  }

  /**
   * Returns a new {@link JsonArray} containing the elements in the first {@link JsonArray}/{@link
   * JsonObject} which are not in the other {@link JsonArray}/{@link JsonObject}.
   *
   * @param elements the {@link JsonArray}/{@link JsonObject} to get the difference of.
   * @return the resultant {@link JsonArray}.
   */
  public JsonArray difference(List<JsonElement> elements) {
    List<JsonElement> difference = new ArrayList<>();

    boolean firstTime = true;
    for (JsonElement jsonElement : elements) {
      List<JsonElement> ele;
      if (jsonElement.isJsonArray()) {
        ele = jsonArrayToList(jsonElement.getAsJsonArray());
      } else {
        ele = objectKeysAsElements(jsonElement.getAsJsonObject());
      }
      if (firstTime) {
        difference.addAll(ele);
        firstTime = false;
      } else {
        difference.removeAll(ele);
      }
    }

    return listToJsonArray(difference.stream().distinct().collect(Collectors.toList()));
  }

  /**
   * Returns if all the keys/values in {@link JsonObject}/{@link JsonArray}s are contained in the
   * first {@link JsonObject}/{@link JsonArray} passed in.
   *
   * @param elements the {@link JsonObject}/{@link JsonArray}s to check.
   * @return <code>true</code> if the first value is a superset of all subsequent values.
   */
  public boolean isSubset(List<JsonElement> elements) {
    List<JsonElement> first;
    if (elements.get(0).isJsonObject()) {
      first = objectKeysAsElements(elements.get(0).getAsJsonObject());
    } else {
      first = jsonArrayToList(elements.get(0).getAsJsonArray());
    }

    Set<JsonElement> remaining = new HashSet<>();
    for (int i = 1; i < elements.size(); i++) {
      JsonElement jsonElement = elements.get(i);
      if (jsonElement.isJsonObject()) {
        remaining.addAll(objectKeysAsElements(jsonElement.getAsJsonObject()));
      } else {
        remaining.addAll(jsonArrayToList(jsonElement.getAsJsonArray()));
      }
    }

    return first.containsAll(remaining);
  }

  /**
   * Returns a new {@link JsonArray} that contains the values in <code>removeFrom</code> with the
   * first occurrence of each values in <code>toRemove</code> removed.
   *
   * @param removeFrom the {@link JsonArray} that contains the values to start with.
   * @param toRemove the {@link JsonArray} that contains the values to remove the first occurrence
   *     of.
   * @return the resulting {@link JsonArray}.
   */
  public JsonArray removeFirst(JsonArray removeFrom, JsonArray toRemove) {
    List<JsonElement> removeFromList = jsonArrayToList(removeFrom);
    List<JsonElement> toRemoveList = jsonArrayToList(toRemove);

    for (JsonElement jsonElement : toRemoveList) {
      removeFromList.remove(jsonElement);
    }

    return listToJsonArray(removeFromList);
  }

  /**
   * Converts a {@link JsonArray} into a list of MT Script objects.
   *
   * @param jsonArray The {@link JsonArray} to convert.
   * @return the list of {@link JsonArray} objects.
   */
  public List<Object> jsonArrayAsMTScriptList(JsonArray jsonArray) {
    List<Object> list = new ArrayList<>();
    for (JsonElement jsonElement : jsonArray) {
      list.add(typeConversion.asScriptType(jsonElement));
    }

    return list;
  }

  /**
   * Parses a <code>String</code> and returns it as a {@link JsonArray}.
   *
   * @param json the <code>String</code> to parse.
   * @return the parsed {@link JsonArray}.
   */
  public JsonArray parseJsonArray(String json) {
    JsonElement jsonElement = typeConversion.asJsonElement(json);
    return jsonElement.getAsJsonArray();
  }

  /**
   * Converts the elements in a {@link JsonArray} into a list of <code>String</code>s.
   *
   * @param jsonArray The {@link JsonArray} to convert to a list of <code>String</code>s.
   * @return The list of <code>String</code>s.
   */
  public List<String> jsonArrayToListOfStrings(JsonArray jsonArray) {
    List<String> list = new ArrayList<>(jsonArray.size());
    for (JsonElement jsonElement : jsonArray) {
      list.add(jsonElement.getAsString());
    }

    return list;
  }

  /**
   * Sets the values of the specified indexes in a Json Array.
   *
   * @param jsonArray the Json array to copy and set values of.
   * @param list The list of indexes and values passed from the script.
   * @return the new JsonArray.
   */
  public JsonArray set(JsonArray jsonArray, List<Object> list) {
    JsonArray newArray = shallowCopy(jsonArray);
    for (int i = 0; i < list.size(); i += 2) {
      BigDecimal index = (BigDecimal) list.get(i);
      Object value = list.get(i + 1);
      if (value instanceof String && value.toString().length() == 0) {
        value = "";
      }

      newArray.set(index.intValue(), typeConversion.asJsonElement(value));
    }

    return newArray;
  }

  /**
   * Returns a shallow copy of the passed in {@link JsonArray}.
   *
   * @param jsonArray The {@link JsonArray} to return a shallow copy of.
   * @return the copy of the array.
   */
  JsonArray shallowCopy(JsonArray jsonArray) {
    JsonArray result = new JsonArray();
    result.addAll(jsonArray);
    return result;
  }

  /**
   * Returns the specified index of a JsonArray as a script type.
   *
   * @param jsonArray the JsonArray to get the index of
   * @param index The index to retrieve.
   * @return the value at the specified index as a script type.
   */
  public Object get(JsonArray jsonArray, int index) {
    return typeConversion.asScriptType(jsonArray.get(index));
  }

  /**
   * Returns a subset of the JsonArray between two indicies.
   *
   * @param jsonArray The JsonArray to get the sub array from.
   * @param startInd The starting index of the sub array.
   * @param endInd The ending index of the sub array.
   * @return the subset JsonArray
   */
  public JsonArray get(JsonArray jsonArray, int startInd, int endInd) {
    JsonArray newArray = new JsonArray();
    int start = startInd >= 0 ? startInd : jsonArray.size() + startInd;
    int end = endInd >= 0 ? endInd : jsonArray.size() + endInd;

    if (end >= start) {
      for (int i = start; i <= end; i++) {
        newArray.add(jsonArray.get(i));
      }
    } else {
      for (int i = start; i >= end; i--) {
        newArray.add(jsonArray.get(i));
      }
    }

    return newArray;
  }

  /**
   * Creates variables from a JsonArray.
   *
   * @param resolver variable map
   * @param jsonArray The array containing the values to set.
   * @param varName The name of the variable to append the index to and set.
   * @return A JsonArray of variables that have been set.
   * @throws ParserException if an error occurs setting the variables.
   */
  public JsonArray toVars(VariableResolver resolver, JsonArray jsonArray, String varName)
      throws ParserException {

    JsonArray setVars = new JsonArray();
    // replace spaces by underscores
    String name = varName.replaceAll("\\s", "_");
    // delete special characters other than "." & "_" in var name
    name = name.replaceAll("[^a-zA-Z0-9._]", "");

    if (!varName.equals("")) {
      for (int i = 0; i < jsonArray.size(); i++) {
        resolver.setVariable(name + i, typeConversion.asScriptType(jsonArray.get(i)));
        setVars.add(varName + i);
      }
    }

    return setVars;
  }

  /**
   * Creates a MTS string list from a JsonArray.
   *
   * @param json The JsonArray to create a string list of.
   * @param delim The delimiter to use between elements in the list.
   * @return the MTS string list.
   */
  public String toStringList(JsonArray json, String delim) {
    StringBuilder sb = new StringBuilder();
    for (JsonElement ele : json) {
      if (sb.length() > 0) {
        sb.append(delim);
      }

      if (ele.isJsonPrimitive()) {
        sb.append(ele.getAsString());
      } else {
        sb.append(ele.toString());
      }
    }

    return sb.toString();
  }

  /**
   * Returns the fields (indexes) of the passed in JsonArray.
   *
   * @param json The JsonArray to get the fields (indexes) of.
   * @param delim if "json" the list will be returned as a JsonArray, otherwise used as delimiter in
   *     a string list.
   * @return the list of fields.
   */
  public Object fields(JsonArray json, String delim) {
    if (delim.equals("json")) {
      JsonArray array = new JsonArray();
      for (int i = 0; i < json.size(); i++) {
        array.add(i);
      }
      return array;
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < json.size(); i++) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(i);
      }
      return sb.toString();
    }
  }

  /**
   * Returns the length of the JsonArray.
   *
   * @param json the JsonArray to get the length of.
   * @return the length of the JsonArray
   */
  public BigDecimal length(JsonArray json) {
    return BigDecimal.valueOf(json.size());
  }
}
