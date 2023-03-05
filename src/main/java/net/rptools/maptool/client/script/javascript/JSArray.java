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
package net.rptools.maptool.client.script.javascript;

import java.util.*;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.*;

public class JSArray extends AbstractList<Object> implements ProxyArray {
  private ArrayList<Object> array;

  public JSArray() {
    array = new ArrayList<Object>();
  }

  public JSArray(int size) {
    array = new ArrayList<Object>(size);
  }

  public JSArray(Collection<? extends Object> collection) {
    array = new ArrayList<Object>(collection);
  }

  @Override
  public Object get(int i) {
    return array.get(i);
  }

  @Override
  public int size() {
    return array.size();
  }

  @Override
  public Object set(int i, Object elemen) {
    return array.set(i, elemen);
  }

  @Override
  public void add(int i, Object elemen) {
    array.add(i, elemen);
  }

  @Override
  public Object remove(int i) {
    return array.remove(i);
  }

  @Override
  public Object get(long index) {
    checkLong(index);
    return get((int) index);
  }

  @Override
  public long getSize() {
    return size();
  }

  @Override
  public void set(long index, Value value) {
    checkLong(index);
    array.set((int) index, value);
  }

  private void checkLong(long index) {
    if (index < Integer.MAX_VALUE) {
      return;
    } else {
      String message = "tried to access index " + index + " which doesn't fit in int";
      throw new ArrayIndexOutOfBoundsException(message);
    }
  }
}
