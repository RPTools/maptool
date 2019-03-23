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
package net.rptools.maptool.webapi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.sf.json.JSONObject;

public class MTWebClientManager {

  /** Create the singleton instance. */
  private static final MTWebClientManager instance = new MTWebClientManager();

  /** The connected clients. */
  private Set<MTWebSocket> clientSockets = Collections.synchronizedSet(new HashSet<MTWebSocket>());

  /** Create a new MTWebClientManager. */
  private MTWebClientManager() {};

  /**
   * Returns the singleton instance of MTWebClientManager.
   *
   * @return the instance of MTWebClientManager.
   */
  public static MTWebClientManager getInstance() {
    return instance;
  }

  /**
   * Return the sessions for the clients that are connected.
   *
   * @return the sessions for the clients that are connected.
   */
  Collection<MTWebSocket> getClientSessions() {
    return Collections.unmodifiableCollection(clientSockets);
  }

  /**
   * Sends a message to all sessions.
   *
   * @param messageType the type of the message.
   * @param data the data to send
   */
  public void sendToAllSessions(String messageType, JSONObject data) {
    for (MTWebSocket ws : clientSockets) {
      ws.sendMessage(messageType, data);
    }
  }

  /**
   * Adds a client to the list of clients being managed.
   *
   * @param wcs The web socket of the client.
   */
  void addClient(MTWebSocket wcs) {
    clientSockets.add(wcs);
    sendInitialInfo(wcs);
  }

  /**
   * Removes a client from the list of clients being managed.
   *
   * @param wcs The web socket of the client.
   */
  void removeClient(MTWebSocket wcs) {
    clientSockets.remove(wcs);
  }

  /**
   * Send the initiative information to the specified client.
   *
   * @param wcs the web socket of the client.
   */
  void sendInitialInfo(MTWebSocket wcs) {
    WebAppInitiative.getInstance().sendInitiative(wcs);
  }
}
