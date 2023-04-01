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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;
import net.rptools.maptool.model.gamedata.data.DataValueFactory;

/** Class for converting between GameData and MT Macro Script types. */
public class MTScriptDataConversion {

  /**
   * Converts a DataValue to a MT Script type. For Asset DataValues the asset handle is returned. If
   * you want to return the actual contents of an Asset DataValue use {@link
   * #convertToMTScriptDereferenceType(DataValue)}
   *
   * @param value The DataValue to convert.
   * @return The converted value.
   */
  public Object convertToMTScriptType(DataValue value) {
    if (value == null || value.isUndefined()) {
      return "";
    }

    return switch (value.getDataType()) {
      case LONG -> BigDecimal.valueOf(value.asLong());
      case DOUBLE -> BigDecimal.valueOf(value.asDouble());
      case STRING -> value.asString();
      case BOOLEAN -> value.asBoolean() ? BigDecimal.ONE : BigDecimal.ZERO;
      case JSON_OBJECT -> value.asJsonObject();
      case JSON_ARRAY -> value.asJsonArray();
      case ASSET -> "asset://" + value.asAsset().getMD5Key();
      case UNDEFINED -> "";
    };
  }

  /**
   * Converts a DataType to a MT Script type fetching the contents of an Asset and returning that
   * instead of just and asset handle where it makes sense. If the asset is a type that can not be
   * handled in MTScript then the asset handle will be returned.
   *
   * @param value The DataValue to convert.
   * @return The converted value.
   */
  public Object convertToMTScriptDereferenceType(DataValue value) {
    if (value != null && value.getDataType() == DataType.ASSET) {
      return switch (value.asAsset().getType()) {
        case HTML, TEXT, MARKDOWN, CSS, JAVASCRIPT, XML -> value.asAsset().getDataAsString();
        case JSON -> value.asAsset().getDataAsJson();
        default -> convertToMTScriptType(value);
      };
    } else {
      return convertToMTScriptType(value);
    }
  }

  /**
   * Converts a MT Script type to a DataValue. For Asset types the asset handle is returned.
   *
   * @param name The name of the value.
   * @param value The value to convert.
   * @return A DataValue from the given value.
   */
  public DataValue parseMTScriptString(String name, Object value) {
    if (value == null) {
      return DataValueFactory.undefined(name);
    }

    if (value instanceof String strval) {
      if (strval.isEmpty()) {
        return DataValueFactory.undefined(name);
      }
      if (strval.trim().startsWith("{") || strval.trim().startsWith("[")) {
        try {
          JsonObject jobj = JsonParser.parseString(strval).getAsJsonObject();
          if (jobj.isJsonArray()) {
            return DataValueFactory.fromJsonArray(name, jobj.getAsJsonArray());
          } else if (jobj.isJsonObject()) {
            return DataValueFactory.fromJsonObject(name, jobj);
          }
        } catch (Exception e) {
          // Ignore
        }
      }
      if (strval.startsWith("asset://")) {
        return DataValueFactory.fromAsset(name, new MD5Key(strval.substring("asset://".length())));
      }

      if (strval.equalsIgnoreCase("true") || strval.equalsIgnoreCase("false")) {
        return DataValueFactory.fromBoolean(name, strval.equalsIgnoreCase("true"));
      }
    }

    if (value instanceof Number nval) {
      if (nval.longValue() == nval.doubleValue()) {
        return DataValueFactory.fromLong(name, nval.longValue());
      } else {
        return DataValueFactory.fromDouble(name, nval.doubleValue());
      }
    }

    if (value instanceof Boolean bval) {
      return DataValueFactory.fromBoolean(name, bval);
    }

    if (value instanceof JsonObject jo) {
      if (jo.isJsonArray()) {
        return DataValueFactory.fromJsonArray(name, jo.getAsJsonArray());
      } else if (jo.isJsonObject()) {
        return DataValueFactory.fromJsonObject(name, jo);
      }
    }

    return DataValueFactory.fromString(name, value.toString());
  }
}
