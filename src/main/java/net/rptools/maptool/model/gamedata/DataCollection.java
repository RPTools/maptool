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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Interface that defines the methods that a DataStore must implement. */
public interface DataCollection {

  /**
   * Returns the namespace for this DataCollection
   *
   * @return the namespace for this DataCollection.
   */
  String getNameSpace();

  /**
   * Is the data collection persistent?
   *
   * @return is the data collection persistent?
   */
  boolean isPersistent();

  /**
   * Is the data collection local only (wont be sent to other clients)?
   *
   * @return is the data collection local only?
   */
  boolean isLocal();

  /**
   * Returns a list of all the keys in this DataCollection.
   *
   * @return a list of all the keys in this DataCollection.
   */
  CompletableFuture<List<String>> getKeys();

  /**
   * Returns if the key is contained in the given DataCollection.
   * This will return {@code true} if the key is contained in the DataCollection even if
   * the value for it has not been defined. If you want to check if the value is present
   * and defined use {link #isDefined(String)}.
   *
   * @param key the key to check for.
   * @return if the key is contained in the given DataCollection.
   */
  CompletableFuture<Boolean> hasKey(String key);

  /**
   * Returns the value for the given key.
   *
   * @param key the key to get the value for.
   * @return the value for the given key.
   */
  CompletableFuture<Optional<DataValue>> getValue(String key);

  /**
   * Sets the value for the given key.
   *
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setValue(String key, DataValue value);

  /**
   * Sets the Long value for the given key.
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setLong(String key, long value);

  /**
   * Sets the Double value for the given key.
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setDouble(String key, double value);

  /**
   * Sets the String value for the given key.
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setString(String key, String value);

  /**
   * Sets the Boolean value for the given key.
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setBoolean(String key, boolean value);

  /**
   * Sets the Long value for the given key.
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setList(String key, List<DataValue> value);

  /**
   * Sets the Map value for the given key.
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   * @throws InvalidDataOperation if the value cannot be converted to the correct type.
   */
  CompletableFuture<Void> setMap(String key, Map<String, DataValue> value);


  /**
   * Sets the type for a key.
   * @param key the key to set the type for.
   * @param type the type to define.
   * @return nothing
   */
  CompletableFuture<Void> setDataType(String key, DataType type);


  /**
   * Returns the data type for a key.
   * @param key the key to get the type for.
   * @return the data type for a key.
   */
  CompletableFuture<DataType> getDataType(String key);


  /**
   * Clears the value for the given key.
   * @param key  the key to clear the value for.
   * @return nothing.
   * @throws InvalidDataOperation if the key doesn't already exist as we wont be able to
   * determine type.
   */
  CompletableFuture<Void> setUndefined(String key);

  /**
   * removes the value for the given key.
   *
   * @param key the key to remove the value for.
   * @return nothing.
   */
  CompletableFuture<Void> remove(String key);


  /**
   * Changes the data type for a key.
   * @param key the key to change the type for.
   * @param dataType the new data type.
   * @return nothing.
   * @throws InvalidDataOperation if the key cannot be converted to the new type.
   */
  CompletableFuture<Void> changeDataType(String key, DataType dataType);


  /**
   * Returns the keys for the given DataCollection is defined and present.
   * @param key the key to get the keys for.
   * @return the keys for the given DataCollection is defined and present.
   */
  CompletableFuture<Boolean> isDefined(String key);
}
