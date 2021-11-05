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
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.server.ServerConfig;

public class PlayerDatabaseFactory {

  public enum PlayerDatabaseType {
    PERSONAL_SERVER,
    LOCAL_PLAYER,
    DEFAULT,
    PASSWORD_FILE
  }

  interface DatabaseChangeTypeSupport {
    /**
     * Adds a property change listener for player database events. Only one type is valid {@link
     * PlayerDBPropertyChange#PROPERTY_CHANGE_DATABASE_CHANGED}.
     *
     * @param listener The property change listener to add.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener for player database events. Only one type is valid {@link
     * PlayerDBPropertyChange#PROPERTY_CHANGE_DATABASE_CHANGED}.
     *
     * @param listener The property change listener to remove.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Notifies that the database hase changed.
     *
     * @param oldDb the old database.
     * @param newDb the new database.
     */
    void databaseChanged(PlayerDatabase oldDb, PlayerDatabase newDb);
  }

  private static PlayerDatabase currentPlayerDatabase;

  private static final Map<PlayerDatabaseType, PlayerDatabase> playerDatabaseMap =
      new ConcurrentHashMap<>();

  private static final ReentrantLock lock = new ReentrantLock();

  private static final DatabaseChangeTypeSupport databaseChangeTypeSupport =
      new DatabaseChangeTypeSupport() {

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
          propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
          propertyChangeSupport.removePropertyChangeListener(listener);
        }

        @Override
        public void databaseChanged(PlayerDatabase oldDb, PlayerDatabase newDb) {
          propertyChangeSupport.firePropertyChange(
              PlayerDBPropertyChange.PROPERTY_CHANGE_DATABASE_CHANGED, oldDb, newDb);
        }
      };

  private static final File PASSWORD_FILE =
      AppUtil.getAppHome("config").toPath().resolve("passwords.json").toFile();
  private static final File PASSWORD_ADDITION_FILE =
      AppUtil.getAppHome("config").toPath().resolve("passwords_add.json").toFile();
  private static ServerConfig serverConfig;

  public static void setServerConfig(ServerConfig config) {
    try {
      lock.lock();
      serverConfig = config;
    } finally {
      lock.unlock();
    }
  }

  public static PlayerDatabase getCurrentPlayerDatabase() {
    try {
      lock.lock();
      return currentPlayerDatabase;
    } finally {
      lock.unlock();
    }
  }

  public static void setCurrentPlayerDatabase(PlayerDatabaseType playerDatabaseType) {
    try {
      lock.lock();
      var oldPlayerDatabase = getCurrentPlayerDatabase();
      currentPlayerDatabase = getPlayerDatabase(playerDatabaseType);
      databaseChangeTypeSupport.databaseChanged(oldPlayerDatabase, currentPlayerDatabase);
    } finally {
      lock.unlock();
    }
  }

  private static ServerConfig getServerConfig() {
    try {
      lock.lock();
      return serverConfig;
    } finally {
      lock.unlock();
    }
  }

  public static PlayerDatabase getPlayerDatabase(PlayerDatabaseType databaseType) {
    switch (databaseType) {
      case LOCAL_PLAYER:
      case PASSWORD_FILE:
        return playerDatabaseMap.computeIfAbsent(
            databaseType, PlayerDatabaseFactory::createPlayerDatabase);
      default:
        return createPlayerDatabase(databaseType);
    }
  }

  static DatabaseChangeTypeSupport getDatabaseChangeTypeSupport() {
    return databaseChangeTypeSupport;
  }

  private static PlayerDatabase createPlayerDatabase(PlayerDatabaseType databaseType) {
    try {
      switch (databaseType) {
        case LOCAL_PLAYER:
          return new LocalPlayerDatabase();
        case PASSWORD_FILE:
          return new PasswordFilePlayerDatabase(PASSWORD_FILE, PASSWORD_ADDITION_FILE);
        case PERSONAL_SERVER:
          return new PersonalServerPlayerDatabase();
        default:
          ServerConfig config = getServerConfig();
          return new DefaultPlayerDatabase(config.getPlayerPassword(), config.getGmPassword());
      }
    } catch (IOException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }
}
