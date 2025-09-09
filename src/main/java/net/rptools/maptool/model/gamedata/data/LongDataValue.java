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
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.gamedata.InvalidDataOperation;

/** The LongDataValue class represents a data value that is an integer. */
public final class LongDataValue implements DataValue {

  /** The name of the value. */
  private final String name;

  /** The value. */
  private final long value;

  /** Has no value been set? */
  private final boolean undefined;

  /**
   * Creates a new LongDataValue.
   *
   * @param name the name of the value.
   * @param value the value.
   */
  LongDataValue(String name, long value) {
    this.name = name;
    this.value = value;
    this.undefined = false;
  }

  /**
   * Creates a new LongDataValue that is undefined.
   *
   * @param name the name of the value.
   */
  LongDataValue(String name) {
    this.name = name;
    this.value = 0;
    this.undefined = true;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DataType getDataType() {
    return DataType.LONG;
  }

  @Override
  public boolean canBeConvertedTo(DataType dataType) {
    if (undefined) {
      return false;
    } else {
      return switch (dataType) {
        case LONG, DOUBLE, BOOLEAN, STRING, JSON_ARRAY -> true;
        case JSON_OBJECT, UNDEFINED, ASSET -> false;
      };
    }
  }

  @Override
  public long asLong() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      return value;
    }
  }

  @Override
  public double asDouble() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      return value;
    }
  }

  @Override
  public String asString() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      return Long.toString(value);
    }
  }

  @Override
  public boolean asBoolean() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      return value != 0;
    }
  }

  @Override
  public JsonArray asJsonArray() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      var array = new JsonArray();
      array.add(value);
      return array;
    }
  }

  @Override
  public JsonObject asJsonObject() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      throw InvalidDataOperation.createInvalidConversion(DataType.LONG, DataType.JSON_OBJECT);
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
      throw InvalidDataOperation.createInvalidConversion(DataType.BOOLEAN, DataType.ASSET);
    }
  }

  @Override
  public String toString() {
    return "LongDataValue{"
        + "name='"
        + name
        + '\''
        + ", value="
        + value
        + ", undefined="
        + undefined
        + '}';
  }
}
