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
import java.util.concurrent.ExecutionException;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.connection.SocketConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 */
public class SocketServer extends AbstractServer {

  private static final Logger log = LogManager.getLogger(SocketServer.class);
  private final int port;
  private ServerSocket socket;
  private ListeningThread listeningThread;

  public SocketServer(int port, HandshakeProvider handshake, MessageHandler messageHandler) {
    super(handshake, messageHandler);
    this.port = port;
  }

  @Override
  public void start() throws IOException {
    socket = new ServerSocket(port);
    listeningThread = new ListeningThread(this, socket);
    listeningThread.start();
  }

  @Override
  public void close() {
    super.close();
    listeningThread.suppressErrors();
    log.debug("Server closing down");

    try {
      socket.close();
    } catch (IOException e) {
      log.warn(e.toString());
    }

    listeningThread.requestStop();
    log.debug("Server stopping listening thread");
    try {
      listeningThread.join();
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }

  public String getError() {
    return null;
  }

  ////
  // Threads
  private static class ListeningThread extends Thread {
    private final SocketServer server;
    private final ServerSocket socket;

    private boolean stopRequested = false;
    private boolean suppressErrors = false;

    private int nextConnectionId = 0;

    private synchronized String nextClientId(Socket socket) {
      return socket.getInetAddress().getHostAddress() + "-" + (nextConnectionId++);
    }

    public ListeningThread(SocketServer server, ServerSocket socket) {
      setName("SocketServer.ListeningThread");
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
          SocketConnection conn = new SocketConnection(id, s);
          server.handleConnection(conn);
        } catch (IOException | ExecutionException | InterruptedException e) {
          if (!suppressErrors) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }
  }
}
