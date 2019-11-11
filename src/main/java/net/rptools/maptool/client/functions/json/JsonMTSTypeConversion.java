package net.rptools.maptool.client.functions.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;

/**
 * Class used to convert between Json Types and MT Script primitive types
 */
class JsonMTSTypeConversion {

  /** The object used to parse Json objects. */
  private final JsonParser parser;

  /** Empty String as a JSON element. */
  public  static final JsonPrimitive EMPTY_STRING_ELEMENT = new JsonPrimitive("");


  /**
   * Creates a new <code>JsonMTSTypeConversion</code> object.
   *
   * @param parser the Json parser to parse strings as Json objects.
   */
  JsonMTSTypeConversion(JsonParser parser) {
    this.parser = parser;
  }

  /**
   * This method returns the passed in object to the most appropriate type to pass back to MT
   * Script.
   *
   * @param val The object to convert.
   * @return the value to return to the scripting engine.
   */
  Object asScriptType(Object val) {
    if (val == null) {
      return "null";
    } else if (val instanceof JsonPrimitive) {
      JsonPrimitive jsonPrimitive = (JsonPrimitive) val;
      if (jsonPrimitive.isNumber()) {
        return jsonPrimitive.getAsBigDecimal();
      } else if (jsonPrimitive.isBoolean()) {
        return jsonPrimitive.getAsBoolean() ? "true" : "false";
      } else {
        return jsonPrimitive.getAsString();
      }
    } else if (val instanceof JsonElement) {
      return val;
    } else {
      return val.toString();
    }
  }

  /**
   * This method returns the object passed in as the appropriate json type.
   *
   * @param o the object to convert.
   * @return the json representation..
   */
  JsonElement asJsonElement(Object o) {
    if (o instanceof JsonElement) {
      return (JsonElement) o;
    }

    return parser.parse(o.toString());
  }

  /**
   * This method returns the object passed in as the appropriate json type. If the passed in object
   * is already a json type then it will return a clone of it.
   *
   * @param json The json object to convert or clone.
   * @return The new json.
   */
  JsonElement asClonedJsonElement(Object json) {
    if (json instanceof JsonElement) {
      JsonElement jsonElement = (JsonElement) json;
      return jsonElement.deepCopy();
    } else {
      return asJsonElement(json);
    }
  }

  /**
   * Converts a string into a json primitive value.
   *
   * @param value The string to convert.
   * @return a json primitive representation of this string.
   */
  JsonPrimitive convertPrimitiveFromString(String value) {
    // Empty list element generates an empty string in the JSON array
    if (value.length() == 0) {
      return EMPTY_STRING_ELEMENT;
    } else {
      // Try to convert it to a number and if that works store it that way
      try {
        BigDecimal bd = new BigDecimal(value);
        return new JsonPrimitive(bd);
      } catch (NumberFormatException nfe) { // Not a number
        return new JsonPrimitive(value);
      }
    }
  }



}
