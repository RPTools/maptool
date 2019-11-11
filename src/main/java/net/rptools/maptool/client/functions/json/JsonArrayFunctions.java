package net.rptools.maptool.client.functions.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

/**
 * Class used to implement MT Script related Json functions / utilities for JsonArrays.
 */
public class JsonArrayFunctions {

  /**
   * Class used to compare numeric {@link JsonElement}s.
   */
  private static class JsonNumberComparator implements Comparator<JsonElement> {
    @Override
    public int compare(JsonElement o1, JsonElement o2) {
      return o1.getAsBigDecimal().compareTo(o2.getAsBigDecimal());
    }
  }

  /**
   * Class used to compare non numeric {@link JsonElement}s.
   */
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
   * @param converter the {@link JsonMTSTypeConversion} used to convert primitive types between json and MTS.
   */
  JsonArrayFunctions(JsonMTSTypeConversion converter) {
    typeConversion = converter;
  }

  /**
   * Converts a string list to json array.
   *
   * @param list The string list to convert.
   * @param delim The delimiter to use to split the string list.
   * @return the string list as a json array.
   */
  public JsonArray fromStringList(String list, String delim) {
    String[] stringList = list.split(delim);

    // An Empty list should generate an empty JSON array.
    if (stringList.length == 1 && stringList[0].length() == 0) {
      return EMPTY_JSON_ARRAY;
    }

    JsonArray jsonArray = new JsonArray();

    // Try to convert the value to a number and if that works we store it that way
    for (int i = 0; i < stringList.length; i++) {
      jsonArray.add(typeConversion.convertPrimitiveFromString(stringList[i].trim()));
    }

    return jsonArray;
  }

  /**
   * Converts a {@link JsonArray} into a MT Script string property.
   * @param jsonArray The JsonArray to convert.
   * @param delim The delimiter to user in converted string property.
   * @return the resultant string property.
   */
  public String toStringProp(JsonArray jsonArray, String delim) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < jsonArray.size(); i++) {
      if (sb.length() > 0) {
        sb.append(delim);
        sb.append(i).append("=").append(typeConversion.asScriptType(jsonArray.get(i)));
      }
    }
    return sb.toString();
  }

  /**
   * Converts the argument into a Json Array. If the argument is a primitive type then a {@link JsonArray}
   * containing only that primitive will be returned.
   *
   * @param o The {@link Object} to coerce into a {@link JsonArray}.
   * @return either the argument converted to a {@link JsonArray} or a {@link JsonArray} containing the object.
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
   * @param arrays the {@link JsonArray}s to concatenate.
   *
   * @return a new {@link JsonArray} which is a concatenation of the passed in arrays.
   */
  public JsonArray concatenate(List<JsonArray> arrays) {
    JsonArray result = new JsonArray();
    for (int i = 0; i < arrays.size(); i++) {
      result.add(arrays.get(i));
    }

    return result;
  }

  /**
   * Concatenates a {@link JsonArray} and other values returning a new {@link JsonArray}.
   * @param array The {@link JsonArray} top copy into the new array.
   * @param values the values to concatenate onto the end of the new array.
   *
   * @return a new {@link JsonArray} containing the concatenated arguments.
   */
  public JsonArray concatenate(JsonArray array, List<Object> values) {
    JsonArray array2 = new JsonArray();
    for (int i = 0; i < values.size(); i++) {
      array2.add(typeConversion.asJsonElement(values.get(i)));
    }

    return concatenate(List.of(array, array2));
  }

  /**
   * Returns a new {@link JsonArray} with the specified index removed.
   * @param jsonArray the array to to operate on.
   * @param index the index of the item to be excluded in the result.
   *
   * @return a new {@link JsonArray} without the specified index.
   */
  public JsonArray remove(JsonArray jsonArray, int index) {
    JsonArray result = new JsonArray();
    result.addAll(jsonArray);
    result.remove(index);
    return jsonArray;
  }

  /**
   * Returns a copy of a {@link JsonArray} sorted in ascending order.
   * If all of the elements in the array can be converted to numeric then the will be sorted as numbers,
   * otherwise they will be sorted as string.
   * @param jsonArray the array to sort.
   *
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
   * Returns a copy of a {@link JsonArray} sorted in descending order.
   * If all of the elements in the array can be converted to numeric then the will be sorted as numbers,
   * otherwise they will be sorted as string.
   * @param jsonArray the array to sort.
   *
   * @return sorted copy of the array.
   */
  public JsonElement sortDescending(JsonArray jsonArray) {
    List<JsonElement> list = jsonArrayToList(jsonArray);
    if (allNumbers(list)) {
      list.sort(new JsonNumberComparator().reversed());
    } else {
      list.sort(new JsonStringComparator().reversed());
    }

    return listToJsonArray(list);
  }


  /**
   * This method sorts a list of {@link JsonObject}s using the specified fields.
   * If the fields can be converted to a number it will be sorted numerically otherwise as a string.
   *
   * @param list the list of {@link JsonObject}s to sort.
   * @param fields the fields to sort by.
   * @throws ParserException if not all objects contain the keys to sort by.
   */
  private void sortObjects(List<JsonObject> list, List<String> fields) throws ParserException {
    var comparators = new ArrayList<Comparator>(fields.size());

    for (int i = 0; i < fields.size(); i++) {
      if (allNumbers(list, fields.get(i))) {
        comparators.add(new JsonNumberComparator());
      } else {
        comparators.add(new JsonStringComparator());
      }
    }
  }

  /**
   * Sorts a {@link JsonArray} of objects in ascending order by the value of the passed in keys.
   *
   * @param jsonArray the array to return a sorted copy of.
   * @param fields the fields to sort by.
   *
   * @return sorted copy of the passed in list.
   *
   * @throws ParserException if all of the objects do not have the required fields.
   */
  public JsonElement sortObjectsAscending(JsonArray jsonArray, List<String> fields)
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
   *
   * @return sorted copy of the passed in list.
   *
   * @throws ParserException if all of the objects do not have the required fields.
   */
  public JsonElement sorObjectsDescending(JsonArray jsonArray, List<String> fields)
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
   *
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
   *
   * @throws ParserException if the {@link JsonArray} contains anything other than {@link JsonObject}.
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
   *
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
   *
   * @return <code>true</code> if all {@link JsonElement}s can be converted to a number.
   */
  private boolean allNumbers(List<JsonElement> list) {
    for (int i = 0; i < list.size(); i++) {
      JsonElement jsonElement = list.get(i);
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
   * @param jsonArray the {@link JsonArray} to check.
   * @param value the value to check for.
   *
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
   * Checks to see if the specified key in all of the {@link JsonObject}s in the list can be converted to a number.
   *
   * @param list the list of {@link JsonObject}s to checks to check
   * @param key the key to check in each of the {@link JsonObject}s.
   * @return <code>true</code> if they key for all the {@link JsonObject}s can be converted to a number.
   *
   * @throws ParserException if any of the {@link JsonObject}s do not contain the key.
   */
  private boolean allNumbers(List<JsonObject> list, String key) throws ParserException {
    for (int i = 0; i < list.size(); i++) {
      JsonObject jsonObject = list.get(i);

      if (!jsonObject.has(key)) {
        throw new ParserException(
            I18N.getText("macro.function.json.notAllContainKey", key));
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
   * @param jsonArray the <code>JsonArray</code> to check.
   * @return <code>true</code> if there is nothing in the array otherwise <code>false</code>/
   */
  public boolean isEmpty(JsonArray jsonArray) {
    return jsonArray.size() == 0;
  }
}
