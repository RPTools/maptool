package net.rptools.maptool.model.gamedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StandardDataCollection implements DataCollection{


  private final String namespace;
  private final boolean persistent;

  private final boolean local;

  private final Map<String, DataValue> valueMap = new ConcurrentHashMap<>();
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
    return !valueMap.containsKey(key);l
    }
  }

  @Override
  public CompletableFuture<Void> setValue(String key, DataValue value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setLong(String key, long value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setDouble(String key, double value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setString(String key, String value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setBoolean(String key, boolean value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setList(String key, List<String> value) {
    return null;
  }

  @Override
  public CompletableFuture<Void> setMap(String key, Map<String, String> value) {
    return null;
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
}
