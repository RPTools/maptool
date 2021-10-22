package net.rptools.maptool.model.gamedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to manage a data collection.
 */
public class StandardDataCollection implements DataCollection{


  /** the namespace of the data collection */
  private final String namespace;

  /** Is the data collection persistent? */
  private final boolean persistent;

  /** is the data collection local only? */
  private final boolean local;

  /** Map of keys to values */
  private final Map<String, DataValue> valueMap = new ConcurrentHashMap<>();
  /** Map of keys to data types */
  private final Map<String, DataType> typeMap = new ConcurrentHashMap<>();

  /**
   * Creates a new {@code StandardDataCollection} instance.
   * @param namespace the namespace for the data collection.
   * @param isPersistent whether the data collection is persistent.
   * @param isLocal whether the data collection is local.
   */
  private StandardDataCollection(String namespace, boolean isPersistent, boolean isLocal) {
    this.namespace = namespace;
    this.persistent = isPersistent;
    this.local = isLocal;
  }

  @Override
  public String getNameSpace() {
    return namespace;
  }

  @Override
  public boolean isPersistent() {
    return persistent;
  }

  @Override
  public boolean isLocal() {
    return local;
  }

  @Override
  public CompletableFuture<List<String>> getKeys() {
    return  CompletableFuture.completedFuture(new ArrayList<>(typeMap.keySet()));
  }

  @Override
  public CompletableFuture<Boolean> hasKey(String key) {
    return CompletableFuture.completedFuture(typeMap.containsKey(key));
  }

  @Override
  public CompletableFuture<Optional<DataValue>> getValue(String key) {
    return CompletableFuture.completedFuture(Optional.ofNullable(valueMap.get(key)));
  }

  /**
   * Returns the {@code DataValue} for the given key, or throws an exception if the key is not defined.
   * @param key the key to get the value for.
   * @return the {@code DataValue} for the given key.
   * @throws InvalidDataOperation if the key is not defined.
   */
  private DataValue getFailOnUndefined(String key) {
    DataValue dataValue = valueMap.get(key);
    if (dataValue == null) {
      throw InvalidDataOperation.createUndefined(key);
    }
    return dataValue;
  }


  @Override
  public CompletableFuture<Void> setValue(String key, DataValue value) {
    DataType dataType = typeMap.get(key);
    if (dataType == null) {
      typeMap.put(key, value.getDataType());
      valueMap.put(key, value);
    } else {
      valueMap.put(key, value.getDataType().convert(value, dataType));
    }
    return null;
  }

  @Override
  public CompletableFuture<Void> setLong(String key, long value) {
    var dataValue = new LongDataValue(key, value);
    return setValue(key, dataValue);
  }

  @Override
  public CompletableFuture<Void> setDouble(String key, double value) {
    var dataValue = new DoubleDataValue(key, value);
    return setValue(key, dataValue);
  }

  @Override
  public CompletableFuture<Void> setString(String key, String value) {
    var dataValue = new StringDataValue(key, value);
    return setValue(key, dataValue);
  }

  @Override
  public CompletableFuture<Void> setBoolean(String key, boolean value) {
    var dataValue = new BooleanDataValue(key, value);
    return setValue(key, dataValue);
  }

  @Override
  public CompletableFuture<Void> setList(String key, List<DataValue> value) {
    var dataValue = new ListDataValue(key, value);
    return setValue(key, dataValue);
  }

  @Override
  public CompletableFuture<Void> setMap(String key, Map<String, DataValue> value) {
    var dataValue = new MapDataValue(key, value);
    return setValue(key, dataValue);
  }

  @Override
  public CompletableFuture<Void> setDataType(String key, DataType type) {
    return null;
  }

  @Override
  public CompletableFuture<DataType> getDataType(String key) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setUndefined(String key) {
    return null;
  }

  @Override
  public CompletableFuture<Void> remove(String key) {
    return null;
  }

  @Override
  public CompletableFuture<Void> changeDataType(String key, DataType dataType) {
    return null;
  }

  @Override
  public CompletableFuture<Boolean> isDefined(String key) {
    return null;
  }
}
