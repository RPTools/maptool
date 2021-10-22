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

import java.util.List;
import java.util.Map;
import net.rptools.maptool.language.I18N;

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

  /**
   * Creates a new StringDataValue.
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
    return switch (dataType) {
      case LONG, DOUBLE, BOOLEAN -> canConvertToNumber;
      case STRING, LIST -> true;
      case MAP, UNDEFINED -> false;
    };
  }

  @Override
  public long asLong() {
    if (!canConvertToNumber) {
      throw InvalidDataOperation.createInvalidConversion(value, DataType.LONG);
    }
    return (long) doubleValue;
  }

  @Override
  public double asDouble() {
    if (!canConvertToNumber) {
      throw InvalidDataOperation.createInvalidConversion(value, DataType.DOUBLE);
    }
    return doubleValue;
  }

  @Override
  public String asString() {
    return value;
  }

  @Override
  public boolean asBoolean() {
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

  @Override
  public List<DataValue> asList() {
    return List.of(this);
  }

  @Override
  public Map<String, DataValue> asMap() {
    throw InvalidDataOperation.createInvalidConversion(DataType.STRING, DataType.MAP);
  }
}
