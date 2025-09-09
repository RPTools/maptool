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

import com.google.gson.JsonParser;
import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;

/**
 * Imports data from the data transfer objects into the data store. This will not clear the data
 * store before importing.
 */
public class GameDataImporter {

  private final DataStore dataStore;

  /**
   * Creates a new GameDataImporter
   *
   * @param dataStore teh data store to import into.
   */
  public GameDataImporter(DataStore dataStore) {
    this.dataStore = dataStore;
    ;
  }

  public void importData(DataStoreDto dataStoreDto)
      throws ExecutionException, InterruptedException {
    var datStore = new DataStoreManager().getDefaultDataStore();
    for (var data : dataStoreDto.getDataList()) {
      importData(data);
    }
  }

  public void importData(GameDataDto data) throws ExecutionException, InterruptedException {
    String type = data.getType();
    String namespace = data.getNamespace();
    dataStore.createNamespace(type, namespace).get();
    for (var value : data.getValuesList()) {
      importData(type, namespace, value);
    }
  }

  public void importData(String type, String namespace, GameDataValueDto value)
      throws ExecutionException, InterruptedException {
    var dataValue =
        switch (value.getValueCase()) {
          case STRING_VALUE -> DataValueFactory.fromString(value.getName(), value.getStringValue());
          case LONG_VALUE -> DataValueFactory.fromLong(value.getName(), value.getLongValue());
          case DOUBLE_VALUE -> DataValueFactory.fromDouble(value.getName(), value.getDoubleValue());
          case BOOLEAN_VALUE ->
              DataValueFactory.fromBoolean(value.getName(), value.getBooleanValue());
          case ASSET_VALUE -> {
            var asset = AssetManager.getAssetAndWait(new MD5Key(value.getAssetValue()));
            yield DataValueFactory.fromAsset(value.getName(), asset);
          }
          case JSON_VALUE -> {
            var json = JsonParser.parseString(value.getJsonValue());
            if (json.isJsonArray()) {
              yield DataValueFactory.fromJsonArray(value.getName(), json.getAsJsonArray());
            } else {
              yield DataValueFactory.fromJsonObject(value.getName(), json.getAsJsonObject());
            }
          }
          case UNDEFINED_STRING_VALUE ->
              DataValueFactory.undefined(value.getName(), DataType.STRING);
          case UNDEFINED_LONG_VALUE -> DataValueFactory.undefined(value.getName(), DataType.LONG);
          case UNDEFINED_DOUBLE_VALUE ->
              DataValueFactory.undefined(value.getName(), DataType.DOUBLE);
          case UNDEFINED_BOOLEAN_VALUE ->
              DataValueFactory.undefined(value.getName(), DataType.BOOLEAN);
          case UNDEFINED_JSON_ARRAY_VALUE ->
              DataValueFactory.undefined(value.getName(), DataType.JSON_ARRAY);
          case UNDEFINED_JSON_OBJECT_VALUE ->
              DataValueFactory.undefined(value.getName(), DataType.JSON_OBJECT);
          case UNDEFINED_ASSET_VALUE -> DataValueFactory.undefined(value.getName(), DataType.ASSET);
          case UNDEFINED_VALUE, VALUE_NOT_SET -> DataValueFactory.undefined(value.getName());
        };
    dataStore.setProperty(type, namespace, dataValue).get();
  }
}
