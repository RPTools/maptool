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
package net.rptools.maptool.model.library.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;

/** Interface for a library data object. */
public interface LibraryData {

  /**
   * Returns the name of the library.
   *
   * @return the name of the library
   */
  CompletableFuture<String> libraryName();

  /**
   * Returns the set of keys in the library.
   *
   * @return the set of keys in the library
   */
  CompletableFuture<Set<String>> getAllKeys();

  /**
   * Returns the data type of the specified key.
   *
   * @param key the key to get the data type of
   * @return the data type of the specified key
   */
  CompletableFuture<DataType> getDataType(String key);

  /**
   * Checks if the library has the specified key.
   *
   * @param key the key to check for.
   * @return true if the library has the specified key, false otherwise.
   */
  CompletableFuture<Boolean> isDefined(String key);

  /**
   * Returns the data value of the specified key.
   *
   * @param key the key to get the data value of
   * @return the data value of the specified key
   */
  CompletableFuture<DataValue> getValue(String key);

  /**
   * Sets the data value of the specified key.
   *
   * @param value the data value to set.
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setData(DataValue value);

  /**
   * Sets the data long value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setLongData(String name, long value);

  /**
   * Sets the double value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setDoubleData(String name, double value);

  /**
   * Sets the boolean value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setBooleanData(String name, boolean value);

  /**
   * Sets the string value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setStringData(String name, String value);

  /**
   * Sets the Json Array value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setJsonArrayData(String name, JsonArray value);

  /**
   * Sets the Json Object value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setJsonObjectData(String name, JsonObject value);

  /**
   * Sets the asset value of the specified key.
   *
   * @param name the name of the key to set
   * @param value the value to set
   * @return a future that completes when the data value is set
   */
  CompletableFuture<Void> setAssetData(String name, Asset value);

  /**
   * Returns if this library supports storing of static data.
   *
   * @return {@code true} if this library supports storing of static data, {@code false} otherwise.
   */
  boolean supportsStaticData();

  /**
   * Does the library have the specified static data.
   *
   * @param path the path of the static data to check for.
   * @return {@code true} if the library has the specified static data, {@code false} otherwise.
   */
  CompletableFuture<Boolean> hasStaticData(String path);

  /**
   * Does the library have the specified public static data.
   *
   * @param path the path of the public static data to check for.
   * @return {@code true} if the library has the specified public static data, {@code false}
   *     otherwise.
   */
  CompletableFuture<Boolean> hasPublicStaticData(String path);

  /**
   * Returns the static data value of the specified path.
   *
   * @param path the path of the static data to get.
   * @return the static data value of the specified path.
   */
  CompletableFuture<DataValue> getStaticData(String path);

  /**
   * Returns the public static data value of the specified path.
   *
   * @param path the path of the public static data to get.
   * @return the public static data value of the specified path.
   */
  CompletableFuture<DataValue> getPublicStaticData(String path);
}
