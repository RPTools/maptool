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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaseModel {

  // Transient so that it isn't transfered over the wire
  private transient List<ModelChangeListener> listenerList =
      new CopyOnWriteArrayList<ModelChangeListener>();

  /**
   * Add the listener to the listenerList.
   *
   * @param listener the ModelChangeListener to add.
   */
  public void addModelChangeListener(ModelChangeListener listener) {
    listenerList.add(listener);
  }
  /**
   * Remove the listener from the listenerList.
   *
   * @param listener the ModelChangeListener to remove.
   */
  public void removeModelChangeListener(ModelChangeListener listener) {
    listenerList.remove(listener);
  }

  /**
   * Send the event to each listener in listenerList
   *
   * @param event the event
   */
  protected void fireModelChangeEvent(ModelChangeEvent event) {

    for (ModelChangeListener listener : listenerList) {
      listener.modelChanged(event);
    }
  }

  /**
   * Create a new listenerList, and returns this.
   *
   * @return this.
   */
  protected Object readResolve() {
    listenerList = new CopyOnWriteArrayList<ModelChangeListener>();
    return this;
  }
}
