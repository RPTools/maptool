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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;

/**
 * Interface for a data store. Classes that implement this interfaces may be proxied by other
 * classes that are responsible for replicating the data to other clients, as such they should not
 * call any of their own public methods. or they risk sending superfluous data to other clients.
 */
public interface DataStore {

  /**
   * Returns the types of properties that are stored in this data store.
   *
   * @return the types of properties that are stored in this data store.
   */
  CompletableFuture<Set<String>> getPropertyTypes();

  /**
   * Returns the namespaces of properties that are stored in this data store for a specific
   * propertyType.
   *
   * @param type the propertyType of properties to get the namespaces of.
   * @return the namespaces of properties that are stored in this data store for a specific
   *     propertyType.
   */
  CompletableFuture<Set<String>> getPropertyNamespaces(String type);

  /**
   * Checks if a property and namespace exists.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @return {@code true} if the property and namespace exists.
   */
  CompletableFuture<Boolean> hasPropertyNamespace(String type, String namespace);

  /**
   * Returns the data propertyType for a property.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @return the data propertyType for the property.
   */
  CompletableFuture<DataType> getPropertyDataType(String type, String namespace, String name);

  /**
   * Returns the property names and their data types for a type and namespace.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @return the property names and their data types for a type and namespace.
   */
  CompletableFuture<Map<String, DataType>> getPropertyDataTypeMap(String type, String namespace);

  /**
   * Checks if a property exists, the value may be undefined, if you want to check for both property
   * existing and value being defined use {@link #isPropertyDefined(String, String, String)}.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @return {@code true} if the property exists.
   */
  CompletableFuture<Boolean> hasProperty(String type, String namespace, String name);

  /**
   * Checks if a property exists, and has a value. property existing and value being defined use
   * {@link #isPropertyDefined(String, String, String)}.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @return {@code true} if the property exists and has a value.
   */
  CompletableFuture<Boolean> isPropertyDefined(String type, String namespace, String name);

  /**
   * Returns the value of a property.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @return the value of a property.
   */
  CompletableFuture<DataValue> getProperty(String type, String namespace, String name);

  /**
   * Returns all the properties for a type and namespace.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @return all the properties for a type and namespace.
   */
  CompletableFuture<Set<DataValue>> getProperties(String type, String namespace);

  /**
   * Sets the value of a property.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setProperty(String type, String namespace, DataValue value);

  /**
   * Sets the value of a property to a long.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setLongProperty(
      String type, String namespace, String name, long value);

  /**
   * Sets the value of a property to a double.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setDoubleProperty(
      String type, String namespace, String name, double value);

  /**
   * Sets the value of a property to a string.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setStringProperty(
      String type, String namespace, String name, String value);

  /**
   * Sets the value of a property to a boolean.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setBooleanProperty(
      String type, String namespace, String name, boolean value);

  /**
   * Sets the value of a property to a json array.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setJsonArrayProperty(
      String type, String namespace, String name, JsonArray value);

  /**
   * Sets the value of a property to a json object.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   * @param value the value of the property.
   * @return the value that was set.
   * @throws InvalidDataOperation exception if the value can not be converted to the properties'
   *     data propertyType.
   * @throws InvalidDataOperation exception if the namespace does not exist for the propertyType.
   */
  CompletableFuture<DataValue> setJsonObjectProperty(
      String type, String namespace, String name, JsonObject value);

  /**
   * Sets the value of a property to an asset.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @param value the value of the property.
   * @return the value that was set.
   */
  CompletableFuture<DataValue> setAssetProperty(
      String type, String namespace, String name, Asset value);

  /**
   * Removes a property from the Data store.
   *
   * @param type the propertyType of the property.
   * @param namespace the namespace of the property.
   * @param name the name of the property.
   * @return a {@code Void} completable future.
   */
  CompletableFuture<Void> removeProperty(String type, String namespace, String name);

  /**
   * Creates a new namespace with no initial data or types. This method can be called on an existing
   * namespace with no adverse effects.
   *
   * @param propertyType the propertyType of the namespace.
   * @param namespace the namespace to create.
   * @return a {@code Void} completable future.
   */
  CompletableFuture<Void> createNamespace(String propertyType, String namespace);

  /**
   * Creates a new namespace with the specified initial data. If the namespace already exists, this
   * method will overwrite any existing data with the same name.
   *
   * @param propertyType the propertyType of the namespace.
   * @param namespace the namespace to create.
   * @param initialData the initial data to set.
   * @return a {@code Void} completable future.
   */
  CompletableFuture<Void> createNamespaceWithInitialData(
      String propertyType, String namespace, Collection<DataValue> initialData);

  /**
   * Creates a new namespace with the specified with the specified types, initial data will be
   * undefined, this method will overwrite any existing data with the same name.
   *
   * @param propertyType the propertyType of the namespace.
   * @param namespace the namespace to create.
   * @param dataTypes the data types to set.
   * @return a {@code Void} completable future.
   * @throws InvalidDataOperation exception if the namespace already exists.
   */
  CompletableFuture<Void> createNamespaceWithTypes(
      String propertyType, String namespace, Map<String, DataType> dataTypes);

  /**
   * Returns the data transfer object representation of the data in the namespace.
   *
   * @param type the propertyType of the namespace.
   * @param namespace the namespace to get the data for.
   * @return a {@code CompletableFuture} containing the data transfer object representation of the
   *     data.
   */
  CompletableFuture<GameDataDto> toDto(String type, String namespace);

  /**
   * Returns the data transfer object representation of the data in the namespace.
   *
   * @param value the data value to convert to a dto.
   * @return a {@code CompletableFuture} containing the data transfer object representation of the
   */
  CompletableFuture<GameDataValueDto> toDto(DataValue value);

  /**
   * Returns all of the {@link MD5Key} of assets in the DataStore.
   *
   * @return a {@code CompletableFuture} containing a list of all of the {@link MD5Key} of assets in
   *     the data store.
   */
  CompletableFuture<Set<MD5Key>> getAssets();

  /** Removes all the data in the DataStore. */
  void clear();

  /**
   * Removes the namespace from the DataStore.
   *
   * @param propertyType the propertyType of the namespace.
   * @param namespace the namespace to remove.
   */
  CompletableFuture<Void> clearNamespace(String propertyType, String namespace);
}
