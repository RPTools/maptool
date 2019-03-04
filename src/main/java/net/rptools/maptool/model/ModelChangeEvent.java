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
package net.rptools.maptool.model;

public class ModelChangeEvent {
  public Object model;
  public Object eventType;
  public Object arg;

  public ModelChangeEvent(Object model, Object eventType) {
    this(model, eventType, null);
  }

  public ModelChangeEvent(Object model, Object eventType, Object arg) {
    this.model = model;
    this.eventType = eventType;
    this.arg = arg;
  }

  public Object getModel() {
    return model;
  }

  public Object getArg() {
    return arg;
  }

  public Object getEvent() {
    return eventType;
  }

  @Override
  public String toString() {
    return "ModelChangeEvent: " + model + " - " + eventType + " - " + arg;
  }
}
