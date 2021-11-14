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

/** Interface for data values. */
public interface DataValue {

  /**
   * Returns the name of the data value.
   *
   * @return the name of the data value
   */
  String getName();

  /**
   * Returns the data propertyType of the data value.
   *
   * @return the data propertyType of the data value
   */
  DataType getDataType();

  /**
   * checks if the data value can be converted to the specified data propertyType.
   *
   * @param dataType the data propertyType to try to convert to.
   * @return true if the data value can be converted to the specified data propertyType.
   */
  boolean canBeConvertedTo(DataType dataType);

  /**
   * Returns the data value as a Long if it can be converted.
   *
   * @return the data value as a Long if it can be converted.
   * @throws InvalidDataOperation if the data value cannot be converted to a Long or value is
   *     undefined.
   */
  long asLong();

  /**
   * Returns the data value as a Double if it can be converted.
   *
   * @return the data value as a Double if it can be converted.
   * @throws InvalidDataOperation if the data value cannot be converted to a Double or value is
   *     undefined.
   */
  double asDouble();

  /**
   * Returns the data value as a String. All scalar DataTypes can be converted to a String.
   *
   * @return the data value as a String.
   * @throws InvalidDataOperation if the data value cannot be converted to a string or value is
   *     undefined.
   */
  String asString();

  /**
   * Returns the data value as a Boolean if it can be converted.
   *
   * @return the data value as a Boolean if it can be converted.
   * @throws InvalidDataOperation if the data value cannot be converted to a boolean or value is
   *     undefined.
   */
  boolean asBoolean();

  /**
   * Returns the data value as a JsonArray, scalar values are converted to a single element array.
   *
   * @return the data value as a JsonArray.
   * @throws InvalidDataOperation if the data value cannot be converted to a json array or value is
   *     undefined.
   */
  JsonArray asJsonArray();

  /**
   * Returns the data value as a JsonObject if it can be converted.
   *
   * @return the data value as a JsonObject if it can be converted.
   * @throws InvalidDataOperation if the data value cannot be converted to a json object or value is
   *     undefined.
   */
  JsonObject asJsonObject();

  /**
   * Returns if the data value has not yet been set.
   *
   * @return if the data value has not yet been set.
   */
  boolean isUndefined();

  /**
   * Returns the data value as an Asset if it can be converted.
   *
   * @return the data value as an Asset if it can be converted.
   */
  Asset asAsset();
}
