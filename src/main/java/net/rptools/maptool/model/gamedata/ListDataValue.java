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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.rptools.maptool.language.I18N;

/** The IntegerDataValue class represents a data value that is a list. */
public final class ListDataValue implements DataValue {

  /** The name of the value. */
  private final String name;
  /** The value. */
  private final List<DataValue> values;

  /**
   * Creates a new ListDataValue.
   * @param name the name of the value.
   * @param values the values.
   */
  ListDataValue(String name, Collection<DataValue> values) {
    this.name = name;
    this.values = List.copyOf(values);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DataType getDataType() {
    return DataType.LIST;
  }

  @Override
  public boolean canBeConvertedTo(DataType dataType) {
    return switch (dataType) {
      case LONG, DOUBLE, BOOLEAN, STRING, MAP, UNDEFINED -> false;
      case LIST -> true;
    };
  }

  @Override
  public long asLong() {
    throw new IllegalStateException(
        I18N.getText("data.error.cantConvertTo", DataType.LIST.name(), DataType.LONG.name()));
  }

  @Override
  public double asDouble() {
    throw new IllegalStateException(
        I18N.getText("data.error.cantConvertTo", DataType.LIST.name(), DataType.DOUBLE.name()));
  }

  @Override
  public String asString() {
    throw new IllegalStateException(
        I18N.getText("data.error.cantConvertTo", DataType.LIST.name(), DataType.STRING.name()));
  }

  @Override
  public boolean asBoolean() {
    throw new IllegalStateException(
        I18N.getText("data.error.cantConvertTo", DataType.LIST.name(), DataType.BOOLEAN.name()));
  }

  @Override
  public List<DataValue> asList() {
    return values;
  }

  @Override
  public Map<String, DataValue> asMap() {
    throw new IllegalStateException(
        I18N.getText("data.error.cantConvertTo", DataType.LIST.name(), DataType.MAP.name()));
  }
}
