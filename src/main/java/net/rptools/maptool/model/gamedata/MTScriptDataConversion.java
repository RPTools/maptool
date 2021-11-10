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

import java.math.BigDecimal;
import net.rptools.maptool.model.gamedata.data.DataValue;

public class MTScriptDataConversion {

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
}
