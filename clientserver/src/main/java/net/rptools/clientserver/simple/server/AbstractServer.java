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
package net.rptools.clientserver.simple.server;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.rptools.clientserver.simple.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractServer implements Server {

  private static final Logger log = LogManager.getLogger(AbstractServer.class);

  private final List<ServerObserver> observerList = new CopyOnWriteArrayList<>();

  public AbstractServer() {}

  public void addObserver(ServerObserver observer) {
    observerList.add(observer);
  }

  public void removeObserver(ServerObserver observer) {
    observerList.remove(observer);
  }

  protected void fireClientConnect(Connection conn) {
    log.debug("Firing: clientConnect: {}", conn.getId());
    for (ServerObserver observer : observerList) {
      observer.connectionAdded(conn);
    }
  }
}
