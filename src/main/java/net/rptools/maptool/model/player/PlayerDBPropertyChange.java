package net.rptools.maptool.model.player;

import java.beans.PropertyChangeListener;

interface PlayerDBPropertyChange {

  /**
   * Property change event name for when a player is added.
   * Some databases may not support this event, so you will also need to listen to
   * {@link #PROPERTY_CHANGE_DATABASE_CHANGED} for changes to players in the database.
   */
  final String PROPERTY_CHANGE_PLAYER_ADDED = "player added";

  /**
   * Property change event name for when a player is removed.
   * Some databases may not support this event, so you will also need to listen to
   * {@link #PROPERTY_CHANGE_DATABASE_CHANGED} for changes to players in the database.
   */
  final String PROPERTY_CHANGE_PLAYER_REMOVE = "player removed";

  /** Property change event name for when a player is changed. */
  final String PROPERTY_CHANGE_PLAYER_CHANGED = "player changed";
  /**
   * Property change event name for when the database is changed or there are mas updates.
   * Some databases may only support this event and not player added/removed/changed
   */
  final String PROPERTY_CHANGE_DATABASE_CHANGED = "database changed";


  /**
   * Adds a property change listener for player database events.
   * @param listener The property change listener to add.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a property change listener for player database events.
   * @param listener The property change listener to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener);
}
