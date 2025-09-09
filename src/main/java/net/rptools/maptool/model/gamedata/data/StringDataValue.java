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

/** The IntegerDataValue class represents a data value that is a String. */
public final class StringDataValue implements DataValue {

  /** The name of the value. */
  private final String name;

  /** The value. */
  private final String value;

  /** Can this value be converted to a Number? */
  private final boolean canConvertToNumber;

  /** The numerical value of the string. */
  private final double doubleValue;

  /** Has no value been set? */
  private final boolean undefined;

  /**
   * Creates a new StringDataValue.
   *
   * @param name the name of the value.
   * @param value the value.
   */
  StringDataValue(String name, String value) {
    this.name = name;
    this.value = value;

    double dval = 0.0;
    boolean canConvert = true;
    try {
      dval = Double.parseDouble(value);
    } catch (NumberFormatException e) {
      canConvert = false;
    }
    doubleValue = dval;
    canConvertToNumber = canConvert;
    undefined = false;
  }

  StringDataValue(String name) {
    this.name = name;
    this.value = null;
    this.canConvertToNumber = false;
    this.doubleValue = Double.NaN;
    this.undefined = true;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }

  @Override
  public boolean canBeConvertedTo(DataType dataType) {
    if (undefined) {
      return false;
    } else {
      return switch (dataType) {
        case LONG, DOUBLE -> canConvertToNumber;
        case BOOLEAN ->
            canConvertToNumber || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
        case STRING, JSON_ARRAY -> true;
        case JSON_OBJECT, UNDEFINED, ASSET -> false;
      };
    }
  }

  @Override
  public long asLong() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      if (!canConvertToNumber) {
        throw InvalidDataOperation.createInvalidConversion(value, DataType.LONG);
      }
      return (long) doubleValue;
    }
  }

  @Override
  public double asDouble() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      if (!canConvertToNumber) {
        throw InvalidDataOperation.createInvalidConversion(value, DataType.DOUBLE);
      }
      return doubleValue;
    }
  }

  @Override
  public String asString() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      return value;
    }
  }

  @Override
  public boolean asBoolean() {
    if (undefined) {
      throw InvalidDataOperation.createUndefined(name);
    } else {
      if (!canConvertToNumber) {
        if (value.equalsIgnoreCase("true")) {
          return true;
        } else if (value.equalsIgnoreCase("false")) {
          return false;
        } else {
          throw InvalidDataOperation.createInvalidConversion(value, DataType.BOOLEAN);
        }
      }
      return doubleValue != 0;
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
      throw InvalidDataOperation.createInvalidConversion(DataType.STRING, DataType.JSON_OBJECT);
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
    return "StringDataValue{"
        + "name='"
        + name
        + '\''
        + ", value='"
        + value
        + '\''
        + ", canConvertToNumber="
        + canConvertToNumber
        + ", doubleValue="
        + doubleValue
        + ", undefined="
        + undefined
        + '}';
  }
}
