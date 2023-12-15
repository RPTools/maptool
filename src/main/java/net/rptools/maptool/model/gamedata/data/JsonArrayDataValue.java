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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Collection;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.gamedata.InvalidDataOperation;

/** The IntegerDataValue class represents a data value that is a list. */
public final class JsonArrayDataValue implements DataValue {

  /** The name of the value. */
  private final String name;

  /** The value. */
  private final JsonArray values;

  /** Has no value been set. */
  private final boolean undefined;

  /**
   * Creates a new JsonArrayDataValue.
   *
   * @note You can't store an Assert in a JsonArray you will have to convert it to another value
   *     first.
   * @param name the name of the value.
   * @param values the values.
   * @throws InvalidDataOperation if the values can not be stored in a JsonArray.
   */
  JsonArrayDataValue(String name, Collection<DataValue> values) {
    var array = new JsonArray();
    values.forEach(
        v -> {
          switch (v.getDataType()) {
            case BOOLEAN -> array.add(v.asBoolean());
            case LONG -> array.add(v.asLong());
            case DOUBLE -> array.add(v.asDouble());
            case STRING -> array.add(v.asString());
            case JSON_ARRAY -> array.add(v.asJsonArray());
            case JSON_OBJECT -> array.add(v.asJsonObject());
            case UNDEFINED -> array.add(JsonNull.INSTANCE);
          }
        });
    this.name = name;
    this.values = array;
    this.undefined = false;
  }

  /**
   * Creates a new JsonArrayDataValue.
   *
   * @param name the name of the value.
   * @param values the values.
   */
  JsonArrayDataValue(String name, JsonArray values) {
    this.name = name;
    this.values = values.deepCopy();
    this.undefined = false;
  }

  /**
   * Creates a new JsonArrayDataValue wit an undefined value.
   *
   * @param name the name of the value.
   */
  JsonArrayDataValue(String name) {
    this.name = name;
    this.values = null;
    this.undefined = true;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DataType getDataType() {
    return DataType.JSON_ARRAY;
  }

  @Override
  public boolean canBeConvertedTo(DataType dataType) {
    if (undefined) {
      return false;
    } else {
      return switch (dataType) {
        case LONG, DOUBLE, BOOLEAN, STRING, JSON_OBJECT, UNDEFINED -> false;
        case JSON_ARRAY, ASSET -> true;
      };
    }
  }

  @Override
  public long asLong() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.JSON_ARRAY, DataType.LONG);
    }
  }

  @Override
  public double asDouble() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.JSON_ARRAY, DataType.DOUBLE);
    }
  }

  @Override
  public String asString() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.JSON_ARRAY, DataType.STRING);
    }
  }

  @Override
  public boolean asBoolean() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.JSON_ARRAY, DataType.BOOLEAN);
    }
  }

  @Override
  public JsonArray asJsonArray() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      return values.deepCopy();
    }
  }

  @Override
  public JsonObject asJsonObject() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.JSON_ARRAY, DataType.JSON_OBJECT);
    }
  }

  @Override
  public boolean isUndefined() {
    return undefined;
  }

  @Override
  public Asset asAsset() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.JSON_ARRAY, DataType.ASSET);
    }
  }

  @Override
  public String toString() {
    return "JsonArrayDataValue{"
        + "name='"
        + name
        + '\''
        + ", values="
        + values
        + ", undefined="
        + undefined
        + '}';
  }
}
