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
  CompletableFuture<DataValue> getValue(String key);

  /**
   * Sets the value for the given key.
   *
   * @param key the key to set the value for.
   * @param value the value to set.
   * @return nothing.
   */
  CompletableFuture<Void> setValue(String key, DataValue value);


  CompletableFuture<Void> setLong(String key, long value);

  CompletableFuture<Void> setDouble(String key, double value);

  CompletableFuture<Void> setString(String key, String value);




  /**
   * removes the value for the given key.
   *
   * @param key the key to remove the value for.
   * @return nothing.
   */
  CompletableFuture<Void> remove(String key);




}
