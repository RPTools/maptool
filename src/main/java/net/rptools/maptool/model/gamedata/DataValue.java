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
package net.rptools.maptool.model.gamedata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

/** Interface for data values. */
public interface DataValue {

  /**
   * Returns the name of the data value.
   *
   * @return the name of the data value
   */
  String getName();

  /**
   * Returns the data type of the data value.
   *
   * @return the data type of the data value
   */
  DataType getDataType();

  /**
   * checks if the data value can be converted to the specified data type.
   *
   * @param dataType the data type to try to convert to.
   * @return true if the data value can be converted to the specified data type.
   */
  boolean canBeConvertedTo(DataType dataType);

  /**
   * Returns the data value as a Long if it can be converted.
   *
   * @return the data value as a Long if it can be converted.
   * @throws IllegalStateException if the data value cannot be converted to a Long.
   */
  long asLong();

  /**
   * Returns the data value as a Double if it can be converted.
   *
   * @return the data value as a Double if it can be converted.
   * @throws IllegalStateException if the data value cannot be converted to a Double.
   */
  double asDouble();

  /**
   * Returns the data value as a String. All scalar DataTypes can be converted to a String.
   *
   * @return the data value as a String.
   * @throws IllegalStateException if the data value cannot be converted to a boolean.
   */
  String asString();

  /**
   * Returns the data value as a Boolean if it can be converted.
   *
   * @return the data value as a Boolean if it can be converted.
   * @throws IllegalStateException if the data value cannot be converted to a boolean.
   */
  boolean asBoolean();

  /**
   * Returns the data value as a List, scalar values are converted to a single element list.
   *
   * @return the data value as a List.
   */
  List<DataValue> asList();

  /**
   * Returns the data value as a JsonArray, scalar values are converted to a single element array.
   *
   * @return the data value as a JsonArray.
   */
  default JsonArray asJsonArray() {
    JsonArray jsonArray = new JsonArray();
    asList()
        .forEach(
            e -> {
              switch (e.getDataType()) {
                case INTEGER -> jsonArray.add(e.asLong());
                case DOUBLE -> jsonArray.add(e.asDouble());
                case STRING -> jsonArray.add(e.asString());
                case BOOLEAN -> jsonArray.add(e.asBoolean());
                case LIST -> jsonArray.add(e.asJsonArray());
                case MAP -> jsonArray.add(e.asJsonObject());
              }
            });
    return jsonArray;
  }

  /**
   * Returns the data value as a Map if it can be converted.
   *
   * @return the data value as a Map if it can be converted.
   * @throws IllegalStateException if the data value cannot be converted to a Map.
   */
  Map<String, DataValue> asMap();

  /**
   * Returns the data value as a JsonObject if it can be converted.
   *
   * @return the data value as a JsonObject if it can be converted.
   * @throws IllegalStateException if the data value cannot be converted to a JsonObject.
   */
  default JsonObject asJsonObject() {
    JsonObject jsonObject = new JsonObject();
    asMap()
        .forEach(
            (key, value) -> {
              switch (value.getDataType()) {
                case INTEGER -> jsonObject.addProperty(key, value.asLong());
                case DOUBLE -> jsonObject.addProperty(key, value.asDouble());
                case STRING -> jsonObject.addProperty(key, value.asString());
                case BOOLEAN -> jsonObject.addProperty(key, value.asBoolean());
                case LIST -> jsonObject.add(key, value.asJsonArray());
                case MAP -> jsonObject.add(key, value.asJsonObject());
              }
            });
    return jsonObject;
  }
}
