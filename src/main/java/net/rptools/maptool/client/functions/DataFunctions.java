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
package net.rptools.maptool.client.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.gamedata.DataStore;
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.gamedata.MTScriptDataConversion;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Class that implements Data macro functions. */
public class DataFunctions extends AbstractFunction {

  /** Creates a new {@code PlayerFunctions} object. */
  public DataFunctions() {
    super(
        0,
        4,
        "data.listTypes",
        "data.listNamespaces",
        /*"data.createNamespace",*/
        "data.setData",
        "data.getData",
        "data.listData",
        "data.removeData",
        /*"data.clearNamespace",
        "data.clearAllData",*/
        "data.getStaticData");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    String fName = functionName.toLowerCase();
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    try {
      var dataStore = new DataStoreManager().getDefaultDataStore();

      switch (fName) {
        case "data.listtypes" -> {
          JsonArray json = new JsonArray();
          for (String type : dataStore.getPropertyTypes().get()) {
            json.add(type);
          }
          return json;
        }
        case "data.listnamespaces" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
          JsonArray json = new JsonArray();
          for (String namespace :
              dataStore.getPropertyNamespaces(parameters.get(0).toString()).get()) {
            json.add(namespace);
          }
          return json;
        }
        case "data.createnamespace" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
          dataStore.createNamespace(parameters.get(0).toString(), parameters.get(1).toString());
          return "";
        }
        case "data.setdata" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 4, 4);
          String type = parameters.get(0).toString();
          String namespace = parameters.get(1).toString();
          String name = parameters.get(2).toString();
          Object value = parameters.get(3);
          dataStore.setProperty(
              type, namespace, new MTScriptDataConversion().parseMTScriptString(name, value));
          return "";
        }
        case "data.listdata" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
          String type = parameters.get(0).toString();
          String namespace = parameters.get(1).toString();
          return listData(dataStore, type, namespace);
        }
        case "data.getdata" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
          String type = parameters.get(0).toString();
          String namespace = parameters.get(1).toString();
          String name = parameters.get(2).toString();

          var data = dataStore.getProperty(type, namespace, name).get();
          return switch (data.getDataType()) {
            case STRING -> data.asString();
            case BOOLEAN -> data.asBoolean() ? BigDecimal.ONE : BigDecimal.ZERO;
            case LONG -> BigDecimal.valueOf(data.asLong());
            case DOUBLE -> BigDecimal.valueOf(data.asDouble());
            case JSON_OBJECT -> data.asJsonObject();
            case JSON_ARRAY -> data.asJsonArray();
            case ASSET -> "asset://" + data.asAsset().getMD5Key().toString();
            case UNDEFINED -> throw new ParserException(I18N.getText("data.error.undefined", name));
          };
        }
        case "data.clearalldata" -> {
          MapTool.addLocalMessage(I18N.getText("msg.warning.prerelease.only", functionName));
          new DataStoreManager().getDefaultDataStore().clear();
          return "";
        }
        case "data.clearnamespace" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
          String type = parameters.get(0).toString();
          String namespace = parameters.get(1).toString();
          new DataStoreManager()
              .getDefaultDataStore()
              .clearNamespace(parameters.get(0).toString(), parameters.get(1).toString())
              .get();

          return "";
        }
        case "data.removedata" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
          String type = parameters.get(0).toString();
          String namespace = parameters.get(1).toString();
          String name = parameters.get(2).toString();
          new DataStoreManager().getDefaultDataStore().removeProperty(type, namespace, name).get();
          return "";
        }
        case "data.getstaticdata" -> {
          FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
          String namespace = parameters.get(0).toString();
          String path = parameters.get(1).toString();
          var context = MapTool.getParser().getContext();
          var libOpt = new LibraryManager().getLibraryForMTScriptCall(namespace, context);
          var lib =
              libOpt.orElseThrow(
                  () -> new ParserException(I18N.getText("library.error.notFound", namespace)));
          DataValue dataValue;
          if (lib.canMTScriptAccessPrivate(context)) {
            dataValue = lib.getLibraryData().thenCompose(data -> data.getStaticData(path)).join();
          } else {
            dataValue =
                lib.getLibraryData().thenCompose(data -> data.getPublicStaticData(path)).join();
          }
          return new MTScriptDataConversion().convertToMTScriptDereferenceType(dataValue);
        }
        default ->
            throw new ParserException(
                I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException(e);
    }
  }

  private JsonArray listData(DataStore dataStore, String type, String namespace)
      throws ParserException {
    try {
      var jarray = new JsonArray();
      dataStore
          .getProperties(type, namespace)
          .thenAccept(
              dataList -> {
                for (var data : dataList) {
                  var jobj = new JsonObject();
                  jobj.addProperty("name", data.getName());
                  jobj.addProperty("type", data.getDataType().name());
                  switch (data.getDataType()) {
                    case STRING -> jobj.addProperty("value", data.asString());
                    case LONG -> jobj.addProperty("value", data.asLong());
                    case DOUBLE -> jobj.addProperty("value", data.asDouble());
                    case BOOLEAN -> jobj.addProperty("value", data.asBoolean());
                    case JSON_OBJECT -> jobj.add("value", data.asJsonObject());
                    case JSON_ARRAY -> jobj.add("value", data.asJsonArray());
                    case ASSET ->
                        jobj.addProperty(
                            "value", "asset://" + data.asAsset().getMD5Key().toString());
                  }
                  jarray.add(jobj);
                }
              })
          .get();
      return jarray;
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException(e.getCause());
    }
  }
}
