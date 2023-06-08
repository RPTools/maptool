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
package net.rptools.maptool.model.library.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import net.rptools.maptool.model.library.data.LibraryData;

/** Class that represents data storage for built in libraries. */
public class BuiltInLibraryData implements LibraryData {

  private final MapToolBuiltInLibrary library;

  public BuiltInLibraryData(MapToolBuiltInLibrary library) {
    this.library = library;
  }

  @Override
  public CompletableFuture<String> libraryName() {
    return library.getName();
  }

  @Override
  public CompletableFuture<Set<String>> getAllKeys() {
    return CompletableFuture.completedFuture(Set.of());
  }

  @Override
  public CompletableFuture<DataType> getDataType(String key) {
    return CompletableFuture.completedFuture(DataType.UNDEFINED);
  }

  @Override
  public CompletableFuture<Boolean> isDefined(String key) {
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public CompletableFuture<DataValue> getValue(String key) {
    return CompletableFuture.completedFuture(DataValueFactory.undefined(key));
  }

  @Override
  public CompletableFuture<Void> setData(DataValue value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setLongData(String name, long value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setDoubleData(String name, double value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setBooleanData(String name, boolean value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setStringData(String name, String value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setJsonArrayData(String name, JsonArray value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setJsonObjectData(String name, JsonObject value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public CompletableFuture<Void> setAssetData(String name, Asset value) {
    throw new IllegalArgumentException("Cannot set data on built-in library");
  }

  @Override
  public boolean supportsStaticData() {
    return false;
  }

  @Override
  public CompletableFuture<Boolean> hasStaticData(String path) {
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public CompletableFuture<Boolean> hasPublicStaticData(String path) {
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public CompletableFuture<DataValue> getStaticData(String path) {
    return CompletableFuture.completedFuture(DataValueFactory.undefined(path));
  }

  @Override
  public CompletableFuture<DataValue> getPublicStaticData(String path) {
    return CompletableFuture.completedFuture(DataValueFactory.undefined(path));
  }
}
