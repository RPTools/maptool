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
package net.rptools.maptool.model.player;

import java.beans.PropertyChangeListener;

public interface PlayerDBPropertyChange {

  /**
   * Property change event name for when a player is added. Some databases may not support this
   * event, so you will also need to listen to {@link #PROPERTY_CHANGE_DATABASE_CHANGED} for changes
   * to players in the database.
   */
  final String PROPERTY_CHANGE_PLAYER_ADDED = "player added";

  /**
   * Property change event name for when a player is removed. Some databases may not support this
   * event, so you will also need to listen to {@link #PROPERTY_CHANGE_DATABASE_CHANGED} for changes
   * to players in the database.
   */
  final String PROPERTY_CHANGE_PLAYER_REMOVED = "player removed";

  /** Property change event name for when a player is changed. */
  final String PROPERTY_CHANGE_PLAYER_CHANGED = "player changed";

  /**
   * Property change event name for when the database is changed or there are mas updates. Some
   * databases may only support this event and not player added/removed/changed
   */
  final String PROPERTY_CHANGE_DATABASE_CHANGED = "database changed";

  /**
   * Adds a property change listener for player database events.
   *
   * @param listener The property change listener to add.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a property change listener for player database events.
   *
   * @param listener The property change listener to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener);
}
