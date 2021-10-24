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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import org.apache.log4j.Logger;

/** Class that implements the DataStore interface. */
public class MemoryDataStore implements DataStore {

  private record PropertyTypeNamespace(String propertyType, String namespace)

  /** Class used to cache definitions. */
  private record Data(
      String propertyType,
      String namespace,
      String name,
      long collectionId,
      long dataId,
      DataValue dataValue
 ) {};


  private final ConcurrentHashMap<String, PropertyTypeNamespace> propertyTypeNamespaceMap =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<PropertyTypeNamespace, Data> dataMap = new ConcurrentHashMap<>();



  /** Class for logging. */
  private static final Logger log = Logger.getLogger(MemoryDataStore.class);


  /**
   * Creates a new MemoryDataStore.
   *
   */
  MemoryDataStore() {
  }


  @Override
  public CompletableFuture<Set<String>> getPropertyTypes() {
    return CompletableFuture.completedFuture(new HashSet<>(propertyTypeNamespaceMap.keySet()));
  }

  @Override
  public CompletableFuture<Set<String>> getPropertyNamespaces(String type) {
    return CompletableFuture.supplyAsync(
        () -> {
          var namespaces = new HashSet<String>();
          try (PreparedStatement stmt =
              connection.getConnection().prepareStatement(GET_NAMESPACES)) {
            stmt.setString(1, type);
            try (var resultSet = stmt.executeQuery()) {
              namespaces.add(resultSet.getString(1));
            }
          } catch (SQLException e) {
            log.error("Unable to fetch property namespaces", e);
            throw new CompletionException(e);
          }
          return namespaces;
        });
  }

  @Override
  public CompletableFuture<Boolean> hasPropertyNamespace(String type, String namespace) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (PreparedStatement stmt =
              connection.getConnection().prepareStatement(GET_NAMESPACE_DETAILS)) {
            stmt.setString(1, type);
            stmt.setString(2, namespace);
            stmt.executeQuery();
            try (var resultSet = stmt.executeQuery()) {
              if (resultSet.next()) {
                return Boolean.TRUE;
              } else {
                return Boolean.FALSE;
              }
            }
          } catch (SQLException e) {
            log.error("Unable to fetch property namespaces", e);
            throw new CompletionException(e);
          }
        });
  }

  @Override
  public CompletableFuture<DataType> getPropertyDataType(
      String type, String namespace, String name) {
    return CompletableFuture.supplyAsync(
        () -> {
          var def = getDefinition(type, namespace, name);
          if (def != null) {
            return def.dataType();
          } else {
            return DataType.UNDEFINED;
          }
        });
  }

  /**
   * Extracts the string value from the result set.
   *
   * @param resultSet the result set to extract the value from.
   * @return the string value.
   * @throws SQLException if there is an SQL error while extracting the value.
   * @throws IOException if there is an IO error while extracting the value.
   */
  private String getStringValue(ResultSet resultSet) throws SQLException, IOException {
    if (resultSet.getBoolean("is_clob")) {
      var clob = resultSet.getClob("clob_value");
      return new String(clob.getAsciiStream().readAllBytes(), StandardCharsets.UTF_8);
    } else {
      return resultSet.getString("string_value");
    }
  }

  private Map<String, DataValue> getPropertyValues(String type, String namespace)
      throws SQLException, IOException {
    var valueMap = new HashMap<String, DataValue>();
    try (PreparedStatement stmt =
        connection.getConnection().prepareStatement(GET_PROPERTY_VALUES)) {
      stmt.setString(1, type);
      stmt.setString(2, namespace);
      try (var resultSet = stmt.executeQuery()) {
        while (resultSet.next()) {
          String name = resultSet.getString("name");
          DataType dataType = DataType.valueOf(resultSet.getString("type"));
          DataValue dataValue = null;
          if (resultSet.getBoolean("defined")) {
            dataValue =
                switch (dataType) {
                  case LONG -> DataValueFactory.fromLong(
                      name, Math.round(resultSet.getDouble("numeric_value")));
                  case DOUBLE -> DataValueFactory.fromDouble(
                      name, resultSet.getDouble("numeric_value"));
                  case BOOLEAN -> DataValueFactory.fromBoolean(
                      name, resultSet.getDouble("numeric_value") != 0);
                  case STRING -> DataValueFactory.fromString(name, getStringValue(resultSet));
                  case JSON_ARRAY -> DataValueFactory.fromJsonArray(
                      name, JsonParser.parseString(getStringValue(resultSet)).getAsJsonArray());
                  case JSON_OBJECT -> DataValueFactory.fromJsonObject(
                      name, JsonParser.parseString(getStringValue(resultSet)).getAsJsonObject());
                  case UNDEFINED -> DataValueFactory.undefined(name, DataType.UNDEFINED);
                };
          }
          valueMap.put(name, dataValue);
        }
      }
    }
    return valueMap;
  }

  @Override
  public CompletableFuture<Map<String, DataType>> getPropertyDataTypeMap(
      String type, String namespace) {
    return null;
  }

  @Override
  public CompletableFuture<Boolean> hasProperty(String type, String namespace, String name) {
    return CompletableFuture.supplyAsync(
        () -> {
          var def = getDefinition(type, namespace, name);
          if (def != null) {
            return Boolean.TRUE;
          } else {
            return Boolean.FALSE;
          }
        });
  }

  @Override
  public CompletableFuture<Boolean> isPropertyDefined(String type, String namespace, String name) {
    return CompletableFuture.supplyAsync(
        () -> {
          var def = getDefinition(type, namespace, name);
          if (def != null) {
            return def.defined();
          } else {
            return Boolean.FALSE;
          }
        });
  }

  @Override
  public CompletableFuture<Void> setProperty(String type, String namespace, DataValue value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setLongProperty(
      String type, String namespace, String name, long value) {
    return setProperty(type, namespace, DataValueFactory.fromLong(name, value));
  }

  @Override
  public CompletableFuture<Void> setDoubleProperty(
      String type, String namespace, String name, double value) {
    return setProperty(type, namespace, DataValueFactory.fromDouble(name, value));
  }

  @Override
  public CompletableFuture<Void> setStringProperty(
      String type, String namespace, String name, String value) {
    return setProperty(type, namespace, DataValueFactory.fromString(name, value));
  }

  @Override
  public CompletableFuture<Void> setBooleanProperty(
      String type, String namespace, String name, boolean value) {
    return setProperty(type, namespace, DataValueFactory.fromBoolean(name, value));
  }

  @Override
  public CompletableFuture<Void> setJsonArrayProperty(
      String type, String namespace, String name, JsonArray value) {
    return setProperty(type, namespace, DataValueFactory.fromJsonArray(name, value));
  }

  @Override
  public CompletableFuture<Void> setJsonObjectProperty(
      String type, String namespace, String name, JsonObject value) {
    return setProperty(type, namespace, DataValueFactory.fromJsonObject(name, value));
  }
}
