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
package net.rptools.maptool.language;

import com.google.gson.JsonElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMessageFactory {
  protected final Map<String, Object> messageParams;
  protected String msgKey;

  protected AbstractMessageFactory(final String i18nKey) {
    this.msgKey = i18nKey;
    messageParams = new HashMap<>();
  }

  /** Persuade likely value types to something meaningful */
  protected String stringify(Object value) {
    return switch (value) {
      case JsonElement je -> je.toString();
      case List<?> list -> Arrays.deepToString(list.toArray());
      case Object[] array -> Arrays.deepToString(array);
      case null, default -> String.valueOf(value);
    };
  }

  public AbstractMessageFactory namedValue(final String name, final Object value) {
    messageParams.put(name, stringify(value));
    return this;
  }

  public String build() {
    return I18N.getMessage(msgKey, messageParams);
  }
}
