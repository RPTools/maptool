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

/** The UndefinedDataValue class represents a data value that is an undefined value. */
public final class UndefinedDataValue implements DataValue {

  /** The name of the value. */
  private final String name;

  /**
   * Creates a new UndefinedDataValue.
   *
   * @param name the name of the value.
   */
  UndefinedDataValue(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DataType getDataType() {
    return DataType.UNDEFINED;
  }

  @Override
  public boolean canBeConvertedTo(DataType dataType) {
    return false;
  }

  @Override
  public long asLong() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public double asDouble() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public String asString() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public boolean asBoolean() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public JsonArray asJsonArray() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public JsonObject asJsonObject() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public boolean isUndefined() {
    return true;
  }

  @Override
  public Asset asAsset() {
    throw InvalidDataOperation.createUndefined(name);
  }

  @Override
  public String toString() {
    return "UndefinedDataValue{" + "name='" + name + '\'' + '}';
  }
}
