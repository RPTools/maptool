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
package net.rptools.lib;

public class AppEvent {

  private Enum<?> id;
  private Object source;
  private Object oldValue;
  private Object newValue;

  public AppEvent(Enum<?> id, Object source, Object oldValue, Object newValue) {
    this.id = id;
    this.source = source;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public Enum<?> getId() {
    return id;
  }

  public Object getSource() {
    return source;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }
}
