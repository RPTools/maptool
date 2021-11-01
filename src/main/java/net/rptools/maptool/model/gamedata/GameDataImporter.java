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
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;

public class GameDataImporter {

  private Object JSONValueFactory;

  public void importData(DataStoreDto dataStoreDto)
      throws ExecutionException, InterruptedException {
    var dataStore = new DataStoreManager().getDefaultDataStore();
    for (var data : dataStoreDto.getDataList()) {
      String type = data.getType();
      String namespace = data.getNamespace();
      System.out.println("Importing " + type + "." + namespace);
      dataStore.createNamespace(type, namespace).get();
      for (var value : data.getValuesList()) {
        var dataValue =
            switch (value.getValueCase()) {
              case STRING_VALUE -> DataValueFactory.fromString(
                  value.getName(), value.getStringValue());
              case LONG_VALUE -> DataValueFactory.fromLong(value.getName(), value.getLongValue());
              case DOUBLE_VALUE -> DataValueFactory.fromDouble(
                  value.getName(), value.getDoubleValue());
              case BOOLEAN_VALUE -> DataValueFactory.fromBoolean(
                  value.getName(), value.getBooleanValue());
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
              case UNDEFINED_VALUE, VALUE_NOT_SET -> DataValueFactory.undefined(value.getName());
            };
        dataStore.setProperty(type, namespace, dataValue).get();
      }
    }
  }
}
