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
import java.util.List;

import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.SocketClientConnection;
import org.apache.log4j.Logger;

/** @author drice */
public class SocketServerConnection extends AbstractServerConnection {

  private static final Logger log = Logger.getLogger(SocketServerConnection.class);
  private final ServerSocket socket;
  private final ListeningThread listeningThread;
  private final DispatchThread dispatchThread;


  public SocketServerConnection(int port, IHandshake handshake) throws IOException {
    super(handshake);
    socket = new ServerSocket(port);
    dispatchThread = new DispatchThread(this);
    dispatchThread.start();
    listeningThread = new ListeningThread(this, socket);
    listeningThread.start();
    //        reaperThread = new ReaperThread();
    //        reaperThread.start();  // There's a deadlock in there, no time to find it now though,
    // so revert to the old way
  }

  @Override
  public void close() throws IOException {
    super.close();
    listeningThread.suppressErrors();
    log.debug("Server closing down");

    socket.close();

    listeningThread.requestStop();
    log.debug("Server stopping listening thread");
    try {
      listeningThread.join();
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public boolean isAlive() {
    return !socket.isClosed();
  }

  ////
  // Threads
  private static class ListeningThread extends Thread {
    private final SocketServerConnection server;
    private final ServerSocket socket;

    private boolean stopRequested = false;
    private boolean suppressErrors = false;

    private int nextConnectionId = 0;

    private synchronized String nextClientId(Socket socket) {
      return socket.getInetAddress().getHostAddress() + "-" + (nextConnectionId++);
    }

    public ListeningThread(SocketServerConnection server, ServerSocket socket) {
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
          SocketClientConnection conn = new SocketClientConnection(id, s);
          server.handleConnection(conn);
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
    private final SocketServerConnection server;

    public ReaperThread(SocketServerConnection server) {
      this.server = server;
    }

    public void requestStop() {
      stopRequested = true;
    }

    @Override
    public void run() {
      while (!stopRequested) {
        try {
          server.reapClients();
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
    private final SocketServerConnection server;
    private final List<Message> queue = Collections.synchronizedList(new ArrayList<Message>());

    private boolean stopRequested = false;

    public DispatchThread(SocketServerConnection server) {
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
