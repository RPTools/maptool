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
package net.rptools.maptool.model.library.token;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.gamedata.MTScriptDataConversion;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import net.rptools.maptool.model.library.data.LibraryData;
import net.rptools.maptool.util.threads.ThreadExecutionHelper;

/** Class that represents the data for a token library. */
public class TokenLibraryData implements LibraryData {

  private final LibraryToken libraryToken;

  /**
   * Creates a the {@code TokenLibraryData} for a token library data object.
   *
   * @param libraryToken The library token to use.
   */
  TokenLibraryData(LibraryToken libraryToken) {
    this.libraryToken = libraryToken;
  }

  @Override
  public CompletableFuture<String> libraryName() {
    return libraryToken.getName();
  }

  @Override
  public CompletableFuture<Set<String>> getAllKeys() {
    return new ThreadExecutionHelper<Set<String>>()
        .runOnSwingThread(
            () -> {
              return libraryToken.getToken().join().getPropertyNames();
            });
  }

  /**
   * Returns the {@link DataValue} for the given key. This method must be run on thw swing thread.
   *
   * @param key The key to get the value for.
   * @return The value for the given key.
   */
  private DataValue getData(String key) {
    var val = libraryToken.getToken().join().getProperty(key);
    if (val == null) {
      return DataValueFactory.undefined(key);
    } else if (val instanceof Number n) {
      if (n.intValue() == n.doubleValue()) {
        return DataValueFactory.fromLong(key, n.longValue());
      } else {
        return DataValueFactory.fromDouble(key, n.doubleValue());
      }
    } else if (val instanceof Boolean b) {
      return DataValueFactory.fromBoolean(key, b);
    } else if (val instanceof String s) {
      try {
        double dval = Double.parseDouble(s.trim());
        if (dval == (long) dval) {
          return DataValueFactory.fromLong(key, (long) dval);
        } else {
          return DataValueFactory.fromDouble(key, dval);
        }
      } catch (NumberFormatException e) {
        // Do nothing if can't be parsed as a number
      }
      return DataValueFactory.fromString(key, s);
    } else {
      return DataValueFactory.fromString(key, val.toString());
    }
  }

  @Override
  public CompletableFuture<DataType> getDataType(String key) {
    return new ThreadExecutionHelper<DataType>().runOnSwingThread(() -> getData(key).getDataType());
  }

  @Override
  public CompletableFuture<Boolean> isDefined(String key) {
    return new ThreadExecutionHelper<Boolean>().runOnSwingThread(() -> !getData(key).isUndefined());
  }

  @Override
  public CompletableFuture<DataValue> getValue(String key) {
    return new ThreadExecutionHelper<DataValue>().runOnSwingThread(() -> getData(key));
  }

  @Override
  public CompletableFuture<Void> setData(DataValue value) {
    return new ThreadExecutionHelper<Void>()
        .runOnSwingThread(
            () -> {
              Object tokenVal = new MTScriptDataConversion().convertToMTScriptType(value);
              MapTool.serverCommand()
                  .updateTokenProperty(
                      libraryToken.getToken().get(),
                      Token.Update.setProperty,
                      value.getName(),
                      tokenVal.toString());
              return null;
            });
  }

  @Override
  public CompletableFuture<Void> setLongData(String name, long value) {
    return setData(DataValueFactory.fromLong(name, value));
  }

  @Override
  public CompletableFuture<Void> setDoubleData(String name, double value) {
    return setData(DataValueFactory.fromDouble(name, value));
  }

  @Override
  public CompletableFuture<Void> setBooleanData(String name, boolean value) {
    return setData(DataValueFactory.fromBoolean(name, value));
  }

  @Override
  public CompletableFuture<Void> setStringData(String name, String value) {
    return setData(DataValueFactory.fromString(name, value));
  }

  @Override
  public CompletableFuture<Void> setJsonArrayData(String name, JsonArray value) {
    return setData(DataValueFactory.fromJsonArray(name, value));
  }

  @Override
  public CompletableFuture<Void> setJsonObjectData(String name, JsonObject value) {
    return setData(DataValueFactory.fromJsonObject(name, value));
  }

  @Override
  public CompletableFuture<Void> setAssetData(String name, Asset value) {
    return setData(DataValueFactory.fromAsset(name, value));
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
