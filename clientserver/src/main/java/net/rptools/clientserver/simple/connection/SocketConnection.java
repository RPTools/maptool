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
package net.rptools.clientserver.simple.connection;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 */
public class SocketConnection extends AbstractConnection implements Connection {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(SocketConnection.class);

  private final String id;
  private SendThread send;
  private ReceiveThread receive;
  private Socket socket;
  private String hostName;
  private int port;

  public SocketConnection(String id, String hostName, int port) {
    this.id = id;
    this.hostName = hostName;
    this.port = port;
  }

  public SocketConnection(String id, Socket socket) {
    this.id = id;
    this.socket = socket;

    initialize(socket);
  }

  @Override
  public String getId() {
    return id;
  }

  private void initialize(Socket socket) {
    this.socket = socket;
    this.send = new SendThread(socket);
    this.receive = new ReceiveThread(socket);

    this.send.start();
    this.receive.start();
  }

  @Override
  public void open() throws IOException {
    initialize(new Socket(hostName, port));
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    addMessage(channel, message);
  }

  @Override
  protected void onClose() {
    receive.interrupt();
    send.interrupt();

    try {
      socket.close();
    } catch (IOException e) {
      log.warn("Failed to close socket", e);
    }
  }

  @Override
  public boolean isAlive() {
    return !socket.isClosed();
  }

  @Override
  public String getError() {
    return null;
  }

  // /////////////////////////////////////////////////////////////////////////
  // send thread
  // /////////////////////////////////////////////////////////////////////////
  private class SendThread extends Thread {
    private final Socket socket;

    public SendThread(Socket socket) {
      setName("SocketConnection.SendThread");
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        final OutputStream out;
        try {
          out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
          log.error("Unable to get socket output stream", e);
          return;
        }

        while (!SocketConnection.this.isClosed() && SocketConnection.this.isAlive()) {
          // Blocks for a time until a message is received.
          byte[] message = SocketConnection.this.nextMessage();
          if (message == null) {
            // No message available. Thread may also have been interrupted as part of stopping.
            continue;
          }

          try {
            SocketConnection.this.writeMessage(out, message);
          } catch (IOException e) {
            log.error("Error while writing message. Closing connection.", e);
            return;
          }
        }
      } finally {
        SocketConnection.this.close();
      }
    }
  }

  // /////////////////////////////////////////////////////////////////////////
  // receive thread
  // /////////////////////////////////////////////////////////////////////////
  private class ReceiveThread extends Thread {
    private final Socket socket;

    public ReceiveThread(Socket socket) {
      setName("SocketConnection.ReceiveThread");
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        final InputStream in;
        try {
          in = socket.getInputStream();
        } catch (IOException e) {
          log.error("Unable to get socket input stream", e);
          return;
        }

        while (!SocketConnection.this.isClosed() && SocketConnection.this.isAlive()) {
          try {
            byte[] message = SocketConnection.this.readMessage(in);
            SocketConnection.this.dispatchCompressedMessage(message);
          } catch (SocketTimeoutException e) {
            log.warn("Lost client {}", SocketConnection.this.getId(), e);
            return;
          } catch (IOException e) {
            log.error(e);
            return;
          } catch (Throwable t) {
            // don't let anything kill this thread via exception
            log.error("Unexpected error", t);
          }
        }
      } finally {
        SocketConnection.this.close();
        fireDisconnect();
      }
    }
  }
}
