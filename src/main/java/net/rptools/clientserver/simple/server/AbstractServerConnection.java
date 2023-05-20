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
import java.util.concurrent.ExecutionException;
import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.maptool.server.Handshake;
import net.rptools.maptool.server.HandshakeObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractServerConnection extends AbstractConnection
    implements MessageHandler, DisconnectHandler, ServerConnection, HandshakeObserver {

  private static final Logger log = LogManager.getLogger(AbstractServerConnection.class);
  //    private final ReaperThread reaperThread;

  private final Map<String, ClientConnection> clients =
      Collections.synchronizedMap(new HashMap<String, ClientConnection>());
  private final List<ServerObserver> observerList =
      Collections.synchronizedList(new ArrayList<ServerObserver>());

  private final HandshakeProvider handshakeProvider;

  public AbstractServerConnection(HandshakeProvider handshakeProvider) {
    this.handshakeProvider = handshakeProvider;
  }

  public void addObserver(ServerObserver observer) {
    observerList.add(observer);
  }

  public void removeObserver(ServerObserver observer) {
    observerList.remove(observer);
  }

  public void handleMessage(String id, byte[] message) {
    dispatchMessage(id, message);
  }

  public void broadcastMessage(byte[] message) {
    synchronized (clients) {
      for (ClientConnection conn : clients.values()) {
        conn.sendMessage(message);
      }
    }
  }

  public void broadcastMessage(String[] exclude, byte[] message) {
    Set<String> excludeSet = new HashSet<String>();
    for (String e : exclude) {
      excludeSet.add(e);
    }
    synchronized (clients) {
      for (Map.Entry<String, ClientConnection> entry : clients.entrySet()) {
        if (!excludeSet.contains(entry.getKey())) {
          entry.getValue().sendMessage(message);
        }
      }
    }
  }

  public void sendMessage(String id, byte[] message) {
    sendMessage(id, null, message);
  }

  public void sendMessage(String id, Object channel, byte[] message) {
    ClientConnection client = clients.get(id);
    client.sendMessage(channel, message);
  }

  public void close() {
    synchronized (clients) {
      for (ClientConnection conn : clients.values()) {
        conn.close();
      }
    }
  }

  protected void reapClients() {
    log.debug("About to reap clients");
    synchronized (clients) {
      log.debug("Reaping clients");

      for (Iterator<Map.Entry<String, ClientConnection>> i = clients.entrySet().iterator();
          i.hasNext(); ) {
        Map.Entry<String, ClientConnection> entry = i.next();
        ClientConnection conn = entry.getValue();
        if (!conn.isAlive()) {
          log.debug("\tReaping: " + conn.getId());
          i.remove();
          try {
            fireClientDisconnect(conn);
            conn.close();
          } catch (Exception e) {
            // Don't want to raise an error if notification of removing a dead connection failed
          }
        }
      }
    }
  }

  protected void fireClientConnect(ClientConnection conn) {
    log.debug("Firing: clientConnect: " + conn.getId());
    for (ServerObserver observer : observerList) {
      observer.connectionAdded(conn);
    }
  }

  protected void fireClientDisconnect(ClientConnection conn) {
    log.debug("Firing: clientDisconnect: " + conn.getId());
    for (ServerObserver observer : observerList) {
      observer.connectionRemoved(conn);
    }
  }

  ////
  // DISCONNECT HANDLER
  public void handleDisconnect(ClientConnection conn) {
    log.debug("HandleDisconnect: " + conn.getId());
    fireClientDisconnect(conn);
  }

  protected void handleConnection(ClientConnection conn)
      throws ExecutionException, InterruptedException {
    var handshake = handshakeProvider.getConnectionHandshake(conn);
    handshake.addObserver(this);
    // Make sure the client is allowed
    handshake.startHandshake();
  }

  public void onCompleted(Handshake handshake) {
    handshake.removeObserver(this);
    var conn = handshake.getConnection();
    handshakeProvider.releaseHandshake(conn);
    if (handshake.isSuccessful()) {
      conn.addMessageHandler(this);
      conn.addDisconnectHandler(this);

      log.debug("About to add new client");
      synchronized (clients) {
        reapClients();

        log.debug("Adding new client");
        clients.put(conn.getId(), conn);
        fireClientConnect(conn);
        // System.out.println("new client " + conn.getId() + " added, " + server.clients.size()
        // + " total");
      }
    } else {
      log.debug("Client closing: bad handshake");
      conn.close();
    }
  }
}
