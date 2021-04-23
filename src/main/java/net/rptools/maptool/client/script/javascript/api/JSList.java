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
import org.graalvm.polyglot.proxy.ProxyArray;

public class JSList implements ProxyArray {

  private List<Object> values;

  public JSList(List<Object> values) {
    this.values = values;
  }

  public static JSList fromArray(Object[] values) {
    return new JSList(Arrays.asList(values));
  }

  public static JSList fromList(List<Object> values) {
    return new JSList(new ArrayList(values));
  }

  @Override
  public long getSize() {
    return values.size();
  }

  @Override
  public Value get(long index) {
    return (Value) values.get((int) index);
  }

  @Override
  public boolean remove(long index) {
    values.remove((int) index);
    return true;
  }

  @Override
  public void set(long index, Value value) {
    values.set((int) index, value);
  }
}
