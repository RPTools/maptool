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
package net.rptools.maptool.client.script.javascript.api;

import java.util.*;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class JSMap implements ProxyObject {

  private Map<String, Object> values;

  public JSMap(Map<String, Object> values) {
    this.values = values;
  }

  public static JSMap fromMap(Map<String, Object> values) {
    return new JSMap(values);
  }

  @Override
  public List<String> getMemberKeys() {
    return new ArrayList(values.keySet());
  }

  @Override
  public Value getMember(String key) {
    return (Value) values.get(key);
  }

  @Override
  public boolean hasMember(String key) {
    return values.containsKey(key);
  }

  @Override
  public void putMember(String key, Value value) {
    values.put(key, value);
  }
}
