package net.rptools.maptool.model.gamedata;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemoryDataStoreTest {


  private MemoryDataStore mds1;
  private MemoryDataStore mds2;

  private MemoryDataStore emptyMds;

  @BeforeEach
  void setUp() throws ExecutionException, InterruptedException {
    mds1 = new MemoryDataStore();
    mds1.createNamespace("testType", "testNamespace").get();

    mds2 = new MemoryDataStore();
    mds2.createNamespace("testType2", "testNamespace").get();
    mds2.createNamespace("testType2", "testNamespace2").get();
    mds2.createNamespace("testType3", "testNamespace3").get();

    emptyMds = new MemoryDataStore();
  }

  @Test
  void getPropertyTypes() {
    assertAll(
        () -> assertEquals(1, mds1.getPropertyTypes().get().size()),
        () -> assertEquals(2, mds2.getPropertyTypes().get().size()),
        () -> assertEquals(0, emptyMds.getPropertyTypes().get().size()),
        () -> assertTrue(mds1.getPropertyTypes().get().contains("testType")),
        () -> assertTrue(mds2.getPropertyTypes().get().contains("testType2")),
        () -> assertTrue(mds2.getPropertyTypes().get().contains("testType3"))
    );
  }

  @Test
  void getPropertyNamespaces() throws Exception {
    assertAll(
        () -> assertEquals(1, mds1.getPropertyNamespaces("testType").get().size()),
        () -> assertEquals(2, mds2.getPropertyNamespaces("testType2").get().size()),
        () -> assertEquals(1, mds2.getPropertyNamespaces("testType3").get().size()),
        () -> assertEquals(0, emptyMds.getPropertyNamespaces("testType").get().size()),
        () -> assertTrue(mds1.getPropertyNamespaces("testType").get().contains("testNamespace")),
        () -> assertTrue(mds2.getPropertyNamespaces("testType2").get().contains("testNamespace")),
        () -> assertTrue(mds2.getPropertyNamespaces("testType2").get().contains("testNamespace2"))
    );
  }

  @Test
  void hasPropertyNamespace() {
    assertAll(
        () -> assertTrue(mds1.hasPropertyNamespace("testType", "testNamespace").get()),
        () -> assertTrue(mds2.hasPropertyNamespace("testType2", "testNamespace").get()),
        () -> assertTrue(mds2.hasPropertyNamespace("testType2", "testNamespace2").get()),
        () -> assertFalse(emptyMds.hasPropertyNamespace("testType", "testNamespace").get())
    );
  }

  @Test
  void getPropertyDataType() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 = mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();
          assertAll(
              () -> assertEquals(DataType.BOOLEAN,
                  mds1.getPropertyDataType("testType", "testNamespace",
                      "boolean1").get()),
              () -> assertEquals(DataType.LONG,
                  mds1.getPropertyDataType("testType", "testNamespace",
                      "long1").get()),
              () -> assertEquals(DataType.DOUBLE,
                  mds1.getPropertyDataType("testType", "testNamespace",
                      "double1").get()),
              () -> assertEquals(DataType.STRING,
                  mds1.getPropertyDataType("testType", "testNamespace",
                      "string1").get()),
              () -> assertEquals(DataType.JSON_ARRAY, mds1.getPropertyDataType("testType",
                  "testNamespace", "jsonArray1").get()),
              () -> assertEquals(DataType.JSON_OBJECT, mds1.getPropertyDataType("testType",
                  "testNamespace", "jsonObject1").get()),
              () -> assertEquals(DataType.UNDEFINED,
                  mds1.getPropertyDataType("testType",
                      "testNamespace", "invalid").get())
          );
  }

  @Test
  void getPropertyDataTypeMap() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 = mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();
    assertAll(
        () -> assertEquals(6,
            mds1.getPropertyDataTypeMap("testType", "testNamespace").get().size()),
        () -> assertEquals(DataType.BOOLEAN, mds1.getPropertyDataTypeMap("testType",
            "testNamespace").get().get("boolean1")),
        () -> assertEquals(DataType.LONG, mds1.getPropertyDataTypeMap("testType",
            "testNamespace").get().get("long1")),
        () -> assertEquals(DataType.DOUBLE, mds1.getPropertyDataTypeMap("testType",
            "testNamespace").get().get("double1")),
        () -> assertEquals(DataType.STRING, mds1.getPropertyDataTypeMap("testType",
            "testNamespace").get().get("string1")),
        () -> assertEquals(DataType.JSON_ARRAY, mds1.getPropertyDataTypeMap("testType",
            "testNamespace").get().get("jsonarray1")),
        () -> assertEquals(DataType.JSON_OBJECT, mds1.getPropertyDataTypeMap("testType",
            "testNamespace").get().get("jsonobject1"))
    );
  }

  @Test
  void hasProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 = mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());
    var cf7 = mds1.setProperty("testType", "testNamespace", DataValueFactory.undefined("undefined"));

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6, cf7).get();

    assertAll(
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "boolean1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "long1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "double1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "string1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "jsonArray1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "jsonObject1").get()),
        () -> assertFalse(mds1.hasProperty("testType", "testNamespace", "invalid").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "undefined").get())
    );
  }

  @Test
  void isPropertyDefined() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 = mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());
    var cf7 = mds1.setProperty("testType", "testNamespace", DataValueFactory.undefined("undefined"));

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6, cf7).get();

    assertAll(
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "boolean1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "long1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "double1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "string1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "jsonArray1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "jsonObject1").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "invalid").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "undefined").get()),
    );
  }

  @Test
  void setProperty() {
  }

  @Test
  void setLongProperty() {
  }

  @Test
  void setDoubleProperty() {
  }

  @Test
  void setStringProperty() {
  }

  @Test
  void setBooleanProperty() {
  }

  @Test
  void setJsonArrayProperty() {
  }

  @Test
  void setJsonObjectProperty() {
  }

  @Test
  void createNamespace() {
  }

  @Test
  void createNamespaceWithInitialData() {
  }

  @Test
  void createNamespaceWithTypes() {
  }
}