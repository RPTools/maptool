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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.ClientConnection;
import org.apache.log4j.Logger;

/** @author drice */
public class ServerConnection extends AbstractConnection
    implements MessageHandler, DisconnectHandler {

  private static final Logger log = Logger.getLogger(ServerConnection.class);
  private final ServerSocket socket;
  private final ListeningThread listeningThread;
  private final DispatchThread dispatchThread;

  //    private final ReaperThread reaperThread;

  private final Map<String, ClientConnection> clients =
      Collections.synchronizedMap(new HashMap<String, ClientConnection>());
  private final List<ServerObserver> observerList =
      Collections.synchronizedList(new ArrayList<ServerObserver>());

  public ServerConnection(int port) throws IOException {
    socket = new ServerSocket(port);
    dispatchThread = new DispatchThread(this);
    dispatchThread.start();
    listeningThread = new ListeningThread(this, socket);
    listeningThread.start();
    //        reaperThread = new ReaperThread();
    //        reaperThread.start();  // There's a deadlock in there, no time to find it now though,
    // so revert to the old way
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

  /**
   * Server subclasses may override this method to perform serial handshaking before the connection
   * is accepted into its pool. By default, this just returns true.
   *
   * @param id
   * @return true if the connection should be added to the pool
   */
  public boolean handleConnectionHandshake(String id, Socket socket) {
    return true;
  }

  public void close() throws IOException {
    listeningThread.suppressErrors();
    log.debug("Server closing down");

    socket.close();

    synchronized (clients) {
      for (ClientConnection conn : clients.values()) {
        conn.close();
      }
    }

    listeningThread.requestStop();
    log.debug("Server stopping listening thread");
    try {
      listeningThread.join();
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
    //        reaperThread.requestStop();
    //        try {
    //            reaperThread.join();
    //        } catch (InterruptedException e) {
    //        	log.error(e.getMessage(), e);
    //        }
  }

  private void reapClients() {
    log.debug("About to reap clients");
    synchronized (clients) {
      log.debug("Reaping clients");

      for (Iterator<Map.Entry<String, ClientConnection>> i = clients.entrySet().iterator();
          i.hasNext(); ) {
        Map.Entry<String, ClientConnection> entry = i.next();
        ClientConnection conn = entry.getValue();
        if (!conn.isAlive()) {
          log.debug("\tReaping: " + conn.getId());
          try {
            i.remove();
            fireClientDisconnect(conn);
            conn.close();
          } catch (IOException e) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }
  }

  private void fireClientConnect(ClientConnection conn) {
    log.debug("Firing: clientConnect: " + conn.getId());
    for (ServerObserver observer : observerList) {
      observer.connectionAdded(conn);
    }
  }

  private void fireClientDisconnect(ClientConnection conn) {
    log.debug("Firing: clientDisconnect: " + conn.getId());
    for (ServerObserver observer : observerList) {
      observer.connectionRemoved(conn);
    }
  }

  ////
  // DISCONNECT HANDLER
  public void handleDisconnect(AbstractConnection conn) {
    if (conn instanceof ClientConnection) {
      log.debug("HandleDisconnect: " + ((ClientConnection) conn).getId());
      fireClientDisconnect((ClientConnection) conn);
    }
  }

  ////
  // Threads
  private static class ListeningThread extends Thread {
    private final ServerConnection server;
    private final ServerSocket socket;

    private boolean stopRequested = false;
    private boolean suppressErrors = false;

    private int nextConnectionId = 0;

    private synchronized String nextClientId(Socket socket) {
      return socket.getInetAddress().getHostAddress() + "-" + (nextConnectionId++);
    }

    public ListeningThread(ServerConnection server, ServerSocket socket) {
      this.server = server;
      this.socket = socket;
    }

    public void requestStop() {
      stopRequested = true;
    }

    public void suppressErrors() {
      suppressErrors = true;
    }

    @Override
    public void run() {
      while (!stopRequested) {
        try {
          Socket s = socket.accept();
          log.debug("Client connecting ...");

          String id = nextClientId(s);

          // Make sure the client is allowed
          if (!server.handleConnectionHandshake(id, s)) {
            log.debug("Client closing: bad handshake");
            s.close();
            continue;
          }
          ClientConnection conn = new ClientConnection(s, id);
          conn.addMessageHandler(server);
          conn.addDisconnectHandler(server);
          conn.start();

          log.debug("About to add new client");
          synchronized (server.clients) {
            server.reapClients();

            log.debug("Adding new client");
            server.clients.put(conn.getId(), conn);
            server.fireClientConnect(conn);
            // System.out.println("new client " + conn.getId() + " added, " + server.clients.size()
            // + " total");
          }
        } catch (IOException e) {
          if (!suppressErrors) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }
  }

  private class ReaperThread extends Thread {
    private boolean stopRequested = false;

    public void requestStop() {
      stopRequested = true;
    }

    @Override
    public void run() {
      while (!stopRequested) {
        try {
          reapClients();
        } catch (Throwable t) {
          log.error(t.getMessage(), t);
        }
        synchronized (this) {
          try {
            Thread.sleep(4000);
          } catch (InterruptedException e) {
            // Whatever.
          }
        }
      }
    }
  }

  private static class DispatchThread extends Thread implements MessageHandler {
    private final ServerConnection server;
    private final List<Message> queue = Collections.synchronizedList(new ArrayList<Message>());

    private boolean stopRequested = false;

    public DispatchThread(ServerConnection server) {
      this.server = server;
    }

    public void requestStop() {
      stopRequested = true;
    }

    public void handleMessage(String id, byte[] message) {
      queue.add(new Message(id, message));
      synchronized (this) {
        this.notify();
      }
    }

    @Override
    public void run() {
      while (!stopRequested) {
        while (queue.size() > 0) {
          Message msg = queue.remove(0);
          try {
            if (log.isDebugEnabled()) {
              log.debug("Server handling: " + msg.id);
            }
            server.handleMessage(msg.id, msg.message);
          } catch (Throwable t) {
            // Don't let anything kill this thread
            log.error(t.getMessage(), t);
          }
        }
        synchronized (this) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }
  }

  private static class Message {
    final String id;
    final byte[] message;

    public Message(String id, byte[] message) {
      this.id = id;
      this.message = message;
    }
  }
}
