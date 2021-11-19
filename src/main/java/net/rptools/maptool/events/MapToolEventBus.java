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
package net.rptools.maptool.events;

import com.google.common.eventbus.EventBus;

/** Class to handle the MapTool event bus. */
public class MapToolEventBus {

  /** The main MapTool event bus. */
  private static final EventBus mainEventBus = new EventBus();

  /**
   * Returns the main MapTool {@link EventBus}.
   *
   * @return the main Maptool {@link EventBus}.
   */
  public EventBus getMainEventBus() {
    return mainEventBus;
  }
}
