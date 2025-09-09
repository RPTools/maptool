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

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
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
        () -> assertTrue(mds2.getPropertyTypes().get().contains("testType3")));
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
        () -> assertTrue(mds2.getPropertyNamespaces("testType2").get().contains("testNamespace2")));
  }

  @Test
  void hasPropertyNamespace() {
    assertAll(
        () -> assertTrue(mds1.hasPropertyNamespace("testType", "testNamespace").get()),
        () -> assertTrue(mds2.hasPropertyNamespace("testType2", "testNamespace").get()),
        () -> assertTrue(mds2.hasPropertyNamespace("testType2", "testNamespace2").get()),
        () -> assertFalse(emptyMds.hasPropertyNamespace("testType", "testNamespace").get()));
  }

  @Test
  void getPropertyDataType() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();
    assertAll(
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds1.getPropertyDataType("testType", "testNamespace", "boolean1").get()),
        () ->
            assertEquals(
                DataType.LONG,
                mds1.getPropertyDataType("testType", "testNamespace", "long1").get()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds1.getPropertyDataType("testType", "testNamespace", "double1").get()),
        () ->
            assertEquals(
                DataType.STRING,
                mds1.getPropertyDataType("testType", "testNamespace", "string1").get()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds1.getPropertyDataType("testType", "testNamespace", "jsonArray1").get()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds1.getPropertyDataType("testType", "testNamespace", "jsonObject1").get()),
        () ->
            assertEquals(
                DataType.UNDEFINED,
                mds1.getPropertyDataType("testType", "testNamespace", "invalid").get()));
  }

  @Test
  void getPropertyDataTypeMap() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());
    var cf7 =
        mds1.setProperty("testType", "testNamespace", DataValueFactory.undefined("undefined"));
    var cf8 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("ulong", DataType.LONG));
    var cf9 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("udouble", DataType.DOUBLE));
    var cf10 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("ustring", DataType.STRING));
    var cf11 =
        mds1.setProperty(
            "testType",
            "testNamespace",
            DataValueFactory.undefined("ujsonArray", DataType.JSON_ARRAY));
    var cf12 =
        mds1.setProperty(
            "testType",
            "testNamespace",
            DataValueFactory.undefined("ujsonObject", DataType.JSON_OBJECT));

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9, cf10, cf11, cf12).get();
    assertAll(
        () ->
            assertEquals(12, mds1.getPropertyDataTypeMap("testType", "testNamespace").get().size()),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("boolean1")),
        () ->
            assertEquals(
                DataType.LONG,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("long1")),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("double1")),
        () ->
            assertEquals(
                DataType.STRING,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("string1")),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("jsonArray1")),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("jsonObject1")),
        () ->
            assertEquals(
                DataType.UNDEFINED,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("undefined")),
        () ->
            assertEquals(
                DataType.LONG,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("ulong")),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("udouble")),
        () ->
            assertEquals(
                DataType.STRING,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("ustring")),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("ujsonArray")),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds1.getPropertyDataTypeMap("testType", "testNamespace").get().get("ujsonObject")));
  }

  @Test
  void hasProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());
    var cf7 =
        mds1.setProperty("testType", "testNamespace", DataValueFactory.undefined("undefined"));
    var cf8 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("ulong", DataType.LONG));
    var cf9 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("udouble", DataType.DOUBLE));
    var cf10 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("ustring", DataType.STRING));
    var cf11 =
        mds1.setProperty(
            "testType",
            "testNamespace",
            DataValueFactory.undefined("ujsonArray", DataType.JSON_ARRAY));
    var cf12 =
        mds1.setProperty(
            "testType",
            "testNamespace",
            DataValueFactory.undefined("ujsonObject", DataType.JSON_OBJECT));

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9, cf10, cf11, cf12).get();

    assertAll(
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "boolean1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "long1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "double1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "string1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "jsonArray1").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "jsonObject1").get()),
        () -> assertFalse(mds1.hasProperty("testType", "testNamespace", "invalid").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "undefined").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "ulong").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "udouble").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "ustring").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "ujsonArray").get()),
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "ujsonObject").get()));
  }

  @Test
  void isPropertyDefined() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());
    var cf7 =
        mds1.setProperty("testType", "testNamespace", DataValueFactory.undefined("undefined"));
    var cf8 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("ulong", DataType.LONG));
    var cf9 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("udouble", DataType.DOUBLE));
    var cf10 =
        mds1.setProperty(
            "testType", "testNamespace", DataValueFactory.undefined("ustring", DataType.STRING));
    var cf11 =
        mds1.setProperty(
            "testType",
            "testNamespace",
            DataValueFactory.undefined("ujsonArray", DataType.JSON_ARRAY));
    var cf12 =
        mds1.setProperty(
            "testType",
            "testNamespace",
            DataValueFactory.undefined("ujsonObject", DataType.JSON_OBJECT));

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9, cf10, cf11, cf12).get();

    assertAll(
        () -> assertTrue(mds1.hasProperty("testType", "testNamespace", "boolean1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "long1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "double1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "string1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "jsonArray1").get()),
        () -> assertTrue(mds1.isPropertyDefined("testType", "testNamespace", "jsonObject1").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "invalid").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "undefined").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "ulong").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "udouble").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "ustring").get()),
        () -> assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "ujsonArray").get()),
        () ->
            assertFalse(mds1.isPropertyDefined("testType", "testNamespace", "ujsonObject").get()));
  }

  @Test
  void setLongProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();

    assertAll(
        () -> {
          mds1.setLongProperty("testType", "testNamespace", "boolean1", 1L).get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setLongProperty("testType", "testNamespace", "boolean1", 0L).get();
          assertFalse(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setLongProperty("testType", "testNamespace", "boolean1", 34L).get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
        },
        () -> {
          mds1.setLongProperty("testType", "testNamespace", "long1", 99L).get();
          assertEquals(99L, mds1.getProperty("testType", "testNamespace", "long1").get().asLong());
        },
        () -> {
          mds1.setLongProperty("testType", "testNamespace", "double1", 99L).get();
          assertEquals(
              99.0, mds1.getProperty("testType", "testNamespace", "double1").get().asDouble());
        },
        () -> {
          mds1.setLongProperty("testType", "testNamespace", "string1", 99L).get();
          assertEquals(
              "99", mds1.getProperty("testType", "testNamespace", "string1").get().asString());
        },
        () -> {
          mds1.setLongProperty("testType", "testNamespace", "jsonArray1", 99L).get();
          assertEquals(
              1,
              mds1.getProperty("testType", "testNamespace", "jsonArray1")
                  .get()
                  .asJsonArray()
                  .size());
          assertEquals(
              99,
              mds1.getProperty("testType", "testNamespace", "jsonArray1")
                  .get()
                  .asJsonArray()
                  .get(0)
                  .getAsLong());
        },
        () -> {
          try {
            mds1.setLongProperty("testType", "testNamespace", "jsonObject1", 99L).get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        });
  }

  @Test
  void setDoubleProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();

    assertAll(
        () -> {
          mds1.setDoubleProperty("testType", "testNamespace", "boolean1", 1.0).get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setDoubleProperty("testType", "testNamespace", "boolean1", 0.0).get();
          assertFalse(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setDoubleProperty("testType", "testNamespace", "boolean1", 34.7).get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
        },
        () -> {
          mds1.setDoubleProperty("testType", "testNamespace", "long1", 99.2).get();
          assertEquals(99L, mds1.getProperty("testType", "testNamespace", "long1").get().asLong());
        },
        () -> {
          mds1.setDoubleProperty("testType", "testNamespace", "double1", 99.2).get();
          assertEquals(
              99.2, mds1.getProperty("testType", "testNamespace", "double1").get().asDouble());
        },
        () -> {
          mds1.setDoubleProperty("testType", "testNamespace", "string1", 99.2).get();
          assertEquals(
              "99.2", mds1.getProperty("testType", "testNamespace", "string1").get().asString());
        },
        () -> {
          mds1.setDoubleProperty("testType", "testNamespace", "jsonArray1", 99.2).get();
          assertEquals(
              1,
              mds1.getProperty("testType", "testNamespace", "jsonArray1")
                  .get()
                  .asJsonArray()
                  .size());
          assertEquals(
              99.2,
              mds1.getProperty("testType", "testNamespace", "jsonArray1")
                  .get()
                  .asJsonArray()
                  .get(0)
                  .getAsDouble());
        },
        () -> {
          try {
            mds1.setDoubleProperty("testType", "testNamespace", "jsonObject1", 99.2).get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        });
  }

  @Test
  void setStringProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();

    assertAll(
        () -> {
          mds1.setStringProperty("testType", "testNamespace", "boolean1", "1.0").get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setStringProperty("testType", "testNamespace", "boolean1", "0.0").get();
          assertFalse(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setStringProperty("testType", "testNamespace", "boolean1", "34.7").get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setStringProperty("testType", "testNamespace", "boolean1", "false").get();
          assertFalse(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setStringProperty("testType", "testNamespace", "boolean1", "true").get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
        },
        () -> {
          mds1.setStringProperty("testType", "testNamespace", "long1", "99.2").get();
          assertEquals(99L, mds1.getProperty("testType", "testNamespace", "long1").get().asLong());
        },
        () -> {
          mds1.setStringProperty("testType", "testNamespace", "double1", "99.2").get();
          assertEquals(
              99.2, mds1.getProperty("testType", "testNamespace", "double1").get().asDouble());
        },
        () -> {
          mds1.setStringProperty("testType", "testNamespace", "string1", "99.2").get();
          assertEquals(
              "99.2", mds1.getProperty("testType", "testNamespace", "string1").get().asString());
        },
        () -> {
          try {
            mds1.setStringProperty("testType", "testNamespace", "jsonArray1", "99.2").get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setStringProperty("testType", "testNamespace", "jsonObject1", "99.2").get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        });
  }

  @Test
  void setBooleanProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();

    assertAll(
        () -> {
          mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true).get();
          assertTrue(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
          mds1.setBooleanProperty("testType", "testNamespace", "boolean1", false).get();
          assertFalse(mds1.getProperty("testType", "testNamespace", "boolean1").get().asBoolean());
        },
        () -> {
          mds1.setBooleanProperty("testType", "testNamespace", "long1", true).get();
          assertEquals(1L, mds1.getProperty("testType", "testNamespace", "long1").get().asLong());
          mds1.setBooleanProperty("testType", "testNamespace", "long1", false).get();
          assertEquals(0L, mds1.getProperty("testType", "testNamespace", "long1").get().asLong());
        },
        () -> {
          mds1.setBooleanProperty("testType", "testNamespace", "double1", true).get();
          assertEquals(
              1.0, mds1.getProperty("testType", "testNamespace", "double1").get().asDouble());
          mds1.setBooleanProperty("testType", "testNamespace", "double1", false).get();
          assertEquals(
              0.0, mds1.getProperty("testType", "testNamespace", "double1").get().asDouble());
        },
        () -> {
          mds1.setBooleanProperty("testType", "testNamespace", "string1", true).get();
          assertEquals(
              "true", mds1.getProperty("testType", "testNamespace", "string1").get().asString());
          mds1.setBooleanProperty("testType", "testNamespace", "string1", false).get();
          assertEquals(
              "false", mds1.getProperty("testType", "testNamespace", "string1").get().asString());
        },
        () -> {
          try {
            mds1.setBooleanProperty("testType", "testNamespace", "jsonArray1", true).get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setBooleanProperty("testType", "testNamespace", "jsonObject1", true).get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        });
  }

  @Test
  void setJsonArrayProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();

    assertAll(
        () -> {
          try {
            mds1.setJsonArrayProperty("testType", "testNamespace", "boolean1", new JsonArray())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonArrayProperty("testType", "testNamespace", "long1", new JsonArray()).get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonArrayProperty("testType", "testNamespace", "double1", new JsonArray())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonArrayProperty("testType", "testNamespace", "string1", new JsonArray())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          var jsonArray1 = new JsonArray();
          jsonArray1.add(12);

          mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", jsonArray1).get();
          assertEquals(
              1,
              mds1.getProperty("testType", "testNamespace", "jsonArray1")
                  .get()
                  .asJsonArray()
                  .size());
          assertEquals(
              12,
              mds1.getProperty("testType", "testNamespace", "jsonArray1")
                  .get()
                  .asJsonArray()
                  .get(0)
                  .getAsLong());
        },
        () -> {
          try {
            mds1.setJsonArrayProperty("testType", "testNamespace", "jsonObject1", new JsonArray())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        });
  }

  @Test
  void setJsonObjectProperty() throws ExecutionException, InterruptedException {
    var cf1 = mds1.setBooleanProperty("testType", "testNamespace", "boolean1", true);
    var cf2 = mds1.setLongProperty("testType", "testNamespace", "long1", 1L);
    var cf3 = mds1.setDoubleProperty("testType", "testNamespace", "double1", 1.0);
    var cf4 = mds1.setStringProperty("testType", "testNamespace", "string1", "test");
    var cf5 = mds1.setJsonArrayProperty("testType", "testNamespace", "jsonArray1", new JsonArray());
    var cf6 =
        mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", new JsonObject());

    CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6).get();

    assertAll(
        () -> {
          try {
            mds1.setJsonObjectProperty("testType", "testNamespace", "boolean1", new JsonObject())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonObjectProperty("testType", "testNamespace", "long1", new JsonObject())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonObjectProperty("testType", "testNamespace", "double1", new JsonObject())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonObjectProperty("testType", "testNamespace", "string1", new JsonObject())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          try {
            mds1.setJsonObjectProperty("testType", "testNamespace", "jsonArray1", new JsonObject())
                .get();
          } catch (ExecutionException e) {
            assertEquals(InvalidDataOperation.class, e.getCause().getClass());
            if (e.getCause() instanceof InvalidDataOperation ido) {
              assertEquals(InvalidDataOperation.Type.INVALID_CONVERSION, ido.getErrorType());
            }
          }
        },
        () -> {
          var jsonObject1 = new JsonObject();
          jsonObject1.addProperty("test", 12);

          mds1.setJsonObjectProperty("testType", "testNamespace", "jsonObject1", jsonObject1).get();
          assertEquals(
              1,
              mds1.getProperty("testType", "testNamespace", "jsonObject1")
                  .get()
                  .asJsonObject()
                  .size());
          assertEquals(
              12,
              mds1.getProperty("testType", "testNamespace", "jsonObject1")
                  .get()
                  .asJsonObject()
                  .get("test")
                  .getAsLong());
        });
  }

  @Test
  void createNamespace() throws ExecutionException, InterruptedException {
    emptyMds.createNamespace("testType", "testNamespace").get();
    emptyMds.createNamespace("testType", "testNamespace2").get();
    emptyMds.createNamespace("testType2", "testNamespace3").get();
    emptyMds.createNamespace("testType2", "testNamespace4").get();
    emptyMds.createNamespace("testType2", "testNamespace5").get();

    assertAll(
        () -> assertEquals(2, emptyMds.getPropertyNamespaces("testType").get().size()),
        () -> assertEquals(3, emptyMds.getPropertyNamespaces("testType2").get().size()),
        () ->
            assertTrue(emptyMds.getPropertyNamespaces("testType").get().contains("testNamespace")),
        () ->
            assertTrue(emptyMds.getPropertyNamespaces("testType").get().contains("testNamespace2")),
        () ->
            assertTrue(
                emptyMds.getPropertyNamespaces("testType2").get().contains("testNamespace3")),
        () ->
            assertTrue(
                emptyMds.getPropertyNamespaces("testType2").get().contains("testNamespace4")),
        () ->
            assertTrue(
                emptyMds.getPropertyNamespaces("testType2").get().contains("testNamespace5")),
        () ->
            assertFalse(
                emptyMds.getPropertyNamespaces("testType").get().contains("testNamespace5")));
  }

  @Test
  void createNamespaceWithInitialData() throws ExecutionException, InterruptedException {
    var jsonArray1 = new JsonArray();
    var jsonArray2 = new JsonArray();
    var jsonArray3 = new JsonArray();
    var jsonObject1 = new JsonObject();
    var jsonObject2 = new JsonObject();
    var jsonObject3 = new JsonObject();
    var mds = new MemoryDataStore();
    mds.createNamespaceWithInitialData(
            "testType",
            "testNamespace",
            Set.of(
                DataValueFactory.fromBoolean("boolean1", true),
                DataValueFactory.fromLong("long1", 12),
                DataValueFactory.fromDouble("double1", 65.8),
                DataValueFactory.fromString("string1", "test"),
                DataValueFactory.fromJsonObject("jsonObject1", jsonObject1),
                DataValueFactory.fromJsonArray("jsonArray1", jsonArray1)))
        .get();

    mds.createNamespaceWithInitialData(
            "testType2",
            "testNamespace2",
            Set.of(
                DataValueFactory.fromBoolean("boolean2", false),
                DataValueFactory.fromLong("long2", 44),
                DataValueFactory.fromDouble("double2", 99.8),
                DataValueFactory.fromString("string2", "test2"),
                DataValueFactory.fromJsonObject("jsonObject2", jsonObject2),
                DataValueFactory.fromJsonArray("jsonArray2", jsonArray2)))
        .get();

    mds.createNamespaceWithInitialData(
            "testType2",
            "testNamespace3",
            Set.of(
                DataValueFactory.fromBoolean("boolean3", true),
                DataValueFactory.fromLong("long3", 24),
                DataValueFactory.fromDouble("double3", 29.8),
                DataValueFactory.fromString("string3", "test3"),
                DataValueFactory.fromJsonObject("jsonObject3", jsonObject3),
                DataValueFactory.fromJsonArray("jsonArray3", jsonArray3)))
        .get();

    assertAll(
        () -> assertEquals(1, mds.getPropertyNamespaces("testType").get().size()),
        () -> assertEquals(2, mds.getPropertyNamespaces("testType2").get().size()),
        () -> assertTrue(mds.getPropertyNamespaces("testType").get().contains("testNamespace")),
        () -> assertTrue(mds.getPropertyNamespaces("testType2").get().contains("testNamespace2")),
        () -> assertTrue(mds.getPropertyNamespaces("testType2").get().contains("testNamespace3")),
        () -> assertFalse(mds.getPropertyNamespaces("testType2").get().contains("testNamespace4")),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds.getProperty("testType", "testNamespace", "boolean1").get().getDataType()),
        () ->
            assertEquals(
                DataType.LONG,
                mds.getProperty("testType", "testNamespace", "long1").get().getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds.getProperty("testType", "testNamespace", "double1").get().getDataType()),
        () ->
            assertEquals(
                DataType.STRING,
                mds.getProperty("testType", "testNamespace", "string1").get().getDataType()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds.getProperty("testType", "testNamespace", "jsonObject1").get().getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds.getProperty("testType", "testNamespace", "jsonArray1").get().getDataType()),
        () ->
            assertTrue(mds.getProperty("testType", "testNamespace", "boolean1").get().asBoolean()),
        () ->
            assertEquals(12, mds.getProperty("testType", "testNamespace", "long1").get().asLong()),
        () ->
            assertEquals(
                65.8, mds.getProperty("testType", "testNamespace", "double1").get().asDouble()),
        () ->
            assertEquals(
                "test", mds.getProperty("testType", "testNamespace", "string1").get().asString()),
        () ->
            assertEquals(
                jsonObject1,
                mds.getProperty("testType", "testNamespace", "jsonObject1").get().asJsonObject()),
        () ->
            assertEquals(
                jsonArray1,
                mds.getProperty("testType", "testNamespace", "jsonArray1").get().asJsonArray()),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds.getProperty("testType2", "testNamespace2", "boolean2").get().getDataType()),
        () ->
            assertEquals(
                DataType.LONG,
                mds.getProperty("testType2", "testNamespace2", "long2").get().getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds.getProperty("testType2", "testNamespace2", "double2").get().getDataType()),
        () ->
            assertEquals(
                DataType.STRING,
                mds.getProperty("testType2", "testNamespace2", "string2").get().getDataType()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds.getProperty("testType2", "testNamespace2", "jsonObject2").get().getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds.getProperty("testType2", "testNamespace2", "jsonArray2").get().getDataType()),
        () ->
            assertFalse(
                mds.getProperty("testType2", "testNamespace2", "boolean2").get().asBoolean()),
        () ->
            assertEquals(
                44, mds.getProperty("testType2", "testNamespace2", "long2").get().asLong()),
        () ->
            assertEquals(
                99.8, mds.getProperty("testType2", "testNamespace2", "double2").get().asDouble()),
        () ->
            assertEquals(
                "test2",
                mds.getProperty("testType2", "testNamespace2", "string2").get().asString()),
        () ->
            assertEquals(
                jsonObject2,
                mds.getProperty("testType2", "testNamespace2", "jsonObject2").get().asJsonObject()),
        () ->
            assertEquals(
                jsonArray2,
                mds.getProperty("testType2", "testNamespace2", "jsonArray2").get().asJsonArray()),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds.getProperty("testType2", "testNamespace3", "boolean3").get().getDataType()),
        () ->
            assertEquals(
                DataType.LONG,
                mds.getProperty("testType2", "testNamespace3", "long3").get().getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds.getProperty("testType2", "testNamespace3", "double3").get().getDataType()),
        () ->
            assertEquals(
                DataType.STRING,
                mds.getProperty("testType2", "testNamespace3", "string3").get().getDataType()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds.getProperty("testType2", "testNamespace3", "jsonObject3").get().getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds.getProperty("testType2", "testNamespace3", "jsonArray3").get().getDataType()),
        () ->
            assertTrue(
                mds.getProperty("testType2", "testNamespace3", "boolean3").get().asBoolean()),
        () ->
            assertEquals(
                24, mds.getProperty("testType2", "testNamespace3", "long3").get().asLong()),
        () ->
            assertEquals(
                29.8, mds.getProperty("testType2", "testNamespace3", "double3").get().asDouble()),
        () ->
            assertEquals(
                "test3",
                mds.getProperty("testType2", "testNamespace3", "string3").get().asString()),
        () ->
            assertEquals(
                jsonObject3,
                mds.getProperty("testType2", "testNamespace3", "jsonObject3").get().asJsonObject()),
        () ->
            assertEquals(
                jsonArray3,
                mds.getProperty("testType2", "testNamespace3", "jsonArray3").get().asJsonArray()));
  }

  @Test
  void createNamespaceWithTypes() throws ExecutionException, InterruptedException {
    var mds = new MemoryDataStore();
    mds.createNamespaceWithTypes(
            "testType",
            "testNamespace",
            Map.of(
                "boolean1",
                DataType.BOOLEAN,
                "long1",
                DataType.LONG,
                "double1",
                DataType.DOUBLE,
                "string1",
                DataType.STRING,
                "jsonArray1",
                DataType.JSON_ARRAY,
                "jsonObject1",
                DataType.JSON_OBJECT))
        .get();
    mds.createNamespaceWithTypes(
            "testType2",
            "testNamespace2",
            Map.of(
                "boolean2",
                DataType.BOOLEAN,
                "long2",
                DataType.LONG,
                "double2",
                DataType.DOUBLE,
                "string2",
                DataType.STRING,
                "jsonArray2",
                DataType.JSON_ARRAY,
                "jsonObject2",
                DataType.JSON_OBJECT))
        .get();
    mds.createNamespaceWithTypes(
            "testType2",
            "testNamespace3",
            Map.of(
                "boolean3",
                DataType.BOOLEAN,
                "long3",
                DataType.LONG,
                "double3",
                DataType.DOUBLE,
                "string3",
                DataType.STRING,
                "jsonArray3",
                DataType.JSON_ARRAY,
                "jsonObject3",
                DataType.JSON_OBJECT))
        .get();

    assertAll(
        () -> assertEquals(1, mds.getPropertyNamespaces("testType").get().size()),
        () -> assertTrue(mds.getPropertyNamespaces("testType").get().contains("testNamespace")),
        () -> assertEquals(6, mds.getProperties("testType", "testNamespace").get().size()),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds.getPropertyDataType("testType", "testNamespace", "boolean1").get()),
        () ->
            assertEquals(
                DataType.LONG, mds.getPropertyDataType("testType", "testNamespace", "long1").get()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds.getPropertyDataType("testType", "testNamespace", "double1").get()),
        () ->
            assertEquals(
                DataType.STRING,
                mds.getPropertyDataType("testType", "testNamespace", "string1").get()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds.getPropertyDataType("testType", "testNamespace", "jsonArray1").get()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds.getPropertyDataType("testType", "testNamespace", "jsonObject1").get()),
        () ->
            assertEquals(
                DataType.UNDEFINED,
                mds.getPropertyDataType("testType", "testNamespace", "invalid").get()),
        () -> assertEquals(2, mds.getPropertyNamespaces("testType2").get().size()),
        () -> assertTrue(mds.getPropertyNamespaces("testType2").get().contains("testNamespace2")),
        () -> assertEquals(6, mds.getProperties("testType2", "testNamespace2").get().size()),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds.getPropertyDataType("testType2", "testNamespace2", "boolean2").get()),
        () ->
            assertEquals(
                DataType.LONG,
                mds.getPropertyDataType("testType2", "testNamespace2", "long2").get()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds.getPropertyDataType("testType2", "testNamespace2", "double2").get()),
        () ->
            assertEquals(
                DataType.STRING,
                mds.getPropertyDataType("testType2", "testNamespace2", "string2").get()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds.getPropertyDataType("testType2", "testNamespace2", "jsonArray2").get()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds.getPropertyDataType("testType2", "testNamespace2", "jsonObject2").get()),
        () ->
            assertEquals(
                DataType.UNDEFINED,
                mds.getPropertyDataType("testType2", "testNamespace2", "invalid").get()),
        () -> assertTrue(mds.getPropertyNamespaces("testType2").get().contains("testNamespace3")),
        () -> assertTrue(mds.getPropertyNamespaces("testType2").get().contains("testNamespace3")),
        () -> assertEquals(6, mds.getProperties("testType2", "testNamespace3").get().size()),
        () ->
            assertEquals(
                DataType.BOOLEAN,
                mds.getPropertyDataType("testType2", "testNamespace3", "boolean3").get()),
        () ->
            assertEquals(
                DataType.LONG,
                mds.getPropertyDataType("testType2", "testNamespace3", "long3").get()),
        () ->
            assertEquals(
                DataType.DOUBLE,
                mds.getPropertyDataType("testType2", "testNamespace3", "double3").get()),
        () ->
            assertEquals(
                DataType.STRING,
                mds.getPropertyDataType("testType2", "testNamespace3", "string3").get()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                mds.getPropertyDataType("testType2", "testNamespace3", "jsonArray3").get()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                mds.getPropertyDataType("testType2", "testNamespace3", "jsonObject3").get()),
        () ->
            assertEquals(
                DataType.UNDEFINED,
                mds.getPropertyDataType("testType2", "testNamespace3", "invalid").get()));
  }

  @Test
  void emptyValueCanBeUpdated() throws ExecutionException, InterruptedException {
    final String PROPERTY_TYPE = "pt";
    final String NAMESPACE = "ns";
    final String PROPERTY_NAME = "a";

    var mdsWithUndefined = new MemoryDataStore();
    mdsWithUndefined
        .createNamespaceWithTypes(
            PROPERTY_TYPE, NAMESPACE, Map.of(PROPERTY_NAME, DataType.UNDEFINED))
        .get();
    assertEquals(
        DataType.UNDEFINED,
        mdsWithUndefined.getProperty(PROPERTY_TYPE, NAMESPACE, PROPERTY_NAME).get().getDataType());

    mdsWithUndefined
        .setProperty(PROPERTY_TYPE, NAMESPACE, DataValueFactory.fromString(PROPERTY_NAME, "1"))
        .get();

    assertEquals(
        DataType.STRING,
        mdsWithUndefined.getProperty(PROPERTY_TYPE, NAMESPACE, PROPERTY_NAME).get().getDataType());
    assertEquals(
        "1",
        mdsWithUndefined.getProperty(PROPERTY_TYPE, NAMESPACE, PROPERTY_NAME).get().asString());
  }
}
