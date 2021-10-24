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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import org.apache.log4j.Logger;

/** Class that implements the DataStore interface. */
public class MemoryDataStore implements DataStore {

  private record PropertyTypeNamespace(String propertyType, String namespace) {}
  ;

  /** Class used to cache definitions. */
  private final ConcurrentHashMap<String, Set<String>> propertyTypeNamespaceMap =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<PropertyTypeNamespace, Map<String, DataValue>> namespaceDataMap =
      new ConcurrentHashMap<>();

  /** Class for logging. */
  private static final Logger log = Logger.getLogger(MemoryDataStore.class);

  /** Creates a new MemoryDataStore. */
  MemoryDataStore() {}

  /**
   * Returns if the namespace exists for the property type.
   *
   * @return if the namespace exists for the property type.
   */
  private boolean checkPropertyNamespace(String propertyType, String namespace) {
    if (propertyTypeNamespaceMap.containsKey(propertyType)) {
      var propertyTypeNamespaces = propertyTypeNamespaceMap.get(propertyType);
      return propertyTypeNamespaces.contains(namespace);
    }
    return false;
  }

  @Override
  public CompletableFuture<Set<String>> getPropertyTypes() {
    return CompletableFuture.completedFuture(new HashSet<>(propertyTypeNamespaceMap.keySet()));
  }

  @Override
  public CompletableFuture<Set<String>> getPropertyNamespaces(String type) {

    return CompletableFuture.supplyAsync(
        () -> {
          var propertyTypeNamespace = propertyTypeNamespaceMap.get(type);
          if (propertyTypeNamespace != null) {
            return new HashSet<>(propertyTypeNamespace);
          } else {
            return new HashSet<>();
          }
        });
  }

  @Override
  public CompletableFuture<Boolean> hasPropertyNamespace(String type, String namespace) {
    return CompletableFuture.completedFuture(checkPropertyNamespace(type, namespace));
  }

  /**
   * Returns the data value for the given property type, namespace and name. This will return null
   * if the data does not exist.
   *
   * @param type the property type.
   * @param namespace the property namespace.
   * @param name the property name.
   * @return the data value.
   */
  private DataValue getData(String type, String namespace, String name) {
    var propertyTypeNamespace = new PropertyTypeNamespace(type, namespace);
    var values = namespaceDataMap.get(propertyTypeNamespace);
    return values.get(name);
  }

  @Override
  public CompletableFuture<DataType> getPropertyDataType(
      String type, String namespace, String name) {
    return CompletableFuture.supplyAsync(
        () -> {
          var data = getData(type, namespace, name);
          return data == null ? DataType.UNDEFINED : data.getDataType();
        });
  }

  @Override
  public CompletableFuture<Map<String, DataType>> getPropertyDataTypeMap(
      String type, String namespace) {
    return CompletableFuture.supplyAsync(
        () -> {
          var dataTypeMap = new HashMap<String, DataType>();
          var values = namespaceDataMap.get(new PropertyTypeNamespace(type, namespace));
          if (values != null) {
            for (var value : values.values()) {
              dataTypeMap.put(value.getName(), value.getDataType());
            }
          }

          return dataTypeMap;
        });
  }

  @Override
  public CompletableFuture<Boolean> hasProperty(String type, String namespace, String name) {
    return CompletableFuture.supplyAsync(
        () -> {
          var def = getData(type, namespace, name);
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
          var data = getData(type, namespace, name);
          if (data != null) {
            return !data.isUndefined();
          } else {
            return Boolean.FALSE;
          }
        });
  }

  @Override
  public CompletableFuture<Void> setProperty(String type, String namespace, DataValue value) {

    if (!checkPropertyNamespace(type, namespace)) {
      throw InvalidDataOperation.createNamespaceDoesNotExist(namespace, type);
    }

    var existing = getData(type, namespace, value.getName());
    // If no value exists we can put anything there, if a value exists we have to check type is
    // correct
    if (existing == null) {
      var dataMap =
          namespaceDataMap.computeIfAbsent(
              new PropertyTypeNamespace(type, namespace), k -> new ConcurrentHashMap<>());
      dataMap.put(value.getName(), value);
    }
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

  @Override
  public CompletableFuture<Void> createNamespace(String propertyType, String namespace) {
    return createNamespaceWithInitialData(propertyType, namespace, List.of());
  }

  @Override
  public CompletableFuture<Void> createNamespaceWithInitialData(
      String propertyType, String namespace, Collection<DataValue> initialData) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (checkPropertyNamespace(propertyType, namespace)) {
            throw InvalidDataOperation.createNamespaceAlreadyExists(namespace, propertyType);
          }

          propertyTypeNamespaceMap.computeIfAbsent(
              propertyType,
              k -> {
                Set<String> newNamespaces = ConcurrentHashMap.newKeySet();
                newNamespaces.add(namespace);
                return newNamespaces;
              });

          var dataMap =
              namespaceDataMap.computeIfAbsent(
                  new PropertyTypeNamespace(propertyType, namespace),
                  k -> new ConcurrentHashMap<>());

          for (var dataValue : initialData) {
            dataMap.put(dataValue.getName(), dataValue);
          }

          return null;
        });
  }

  @Override
  public CompletableFuture<Void> createNamespaceWithTypes(
      String propertyType, String namespace, Map<String, DataType> dataTypes) {
    return createNamespaceWithInitialData(
        propertyType,
        namespace,
        dataTypes.entrySet().stream()
            .map(e -> DataValueFactory.undefined(e.getKey(), e.getValue()))
            .toList());
  }
}
