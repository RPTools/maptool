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
package net.rptools.maptool.model.gamedata.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collection;

/** Class to create DataValues from different values. */
public class DataValueFactory {

  /**
   * Returns a DataValue from the given long value.
   *
   * @param name The name of the value.
   * @param value The value to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromLong(String name, long value) {
    return new LongDataValue(name, value);
  }

  /**
   * Returns a DataValue from the given string value.
   *
   * @param name The name of the value.
   * @param value The value to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromString(String name, String value) {
    return new StringDataValue(name, value);
  }

  /**
   * Returns a DataValue from the given boolean value.
   *
   * @param name The name of the value.
   * @param value The value to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromBoolean(String name, boolean value) {
    return new BooleanDataValue(name, value);
  }

  /**
   * Returns a DataValue from the given double value.
   *
   * @param name The name of the value.
   * @param value The value to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromDouble(String name, double value) {
    return new DoubleDataValue(name, value);
  }

  /**
   * Returns a DataValue from the given JsonArray value.
   *
   * @param name The name of the value.
   * @param array The value to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromJsonArray(String name, JsonArray array) {
    return new JsonArrayDataValue(name, array);
  }

  /**
   * Returns a DataValue from the given collection of DataValues.
   *
   * @param name The name of the value.
   * @param values The values to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromCollection(String name, Collection<DataValue> values) {
    return new JsonArrayDataValue(name, values);
  }

  /**
   * Returns a DataValue from the given JsonObject value.
   *
   * @param name The name of the value.
   * @param obj The value to create a DataValue from.
   * @return A DataValue from the given value.
   */
  public static DataValue fromJsonObject(String name, JsonObject obj) {
    return new JsonObjectDataValue(name, obj);
  }

  /**
   * Returns a DataValue that represents an undefined value.
   *
   * @param name The name of the value.
   * @param dataType The type of the value.
   * @return A DataValue that represents an undefined value.
   */
  public static DataValue undefined(String name, DataType dataType) {
    return switch (dataType) {
      case LONG -> new LongDataValue(name);
      case DOUBLE -> new DoubleDataValue(name);
      case BOOLEAN -> new BooleanDataValue(name);
      case STRING -> new StringDataValue(name);
      case JSON_ARRAY -> new JsonArrayDataValue(name);
      case JSON_OBJECT -> new JsonObjectDataValue(name);
      case UNDEFINED -> new UndefinedDataValue(name);
    };
  }
}
