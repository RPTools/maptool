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
import net.rptools.clientserver.ActivityListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 */
public class SocketConnection extends AbstractConnection implements Connection {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(SocketConnection.class);

  // Only valid for open connections.
  private SendThread send;
  private ReceiveThread receive;
  private Socket socket;

  // Only valid for pending connections before #open() is called.
  private String hostName;
  private int port;

  public SocketConnection(String id, String hostName, int port) {
    super(id);
    this.hostName = hostName;
    this.port = port;
  }

  public SocketConnection(String id, Socket socket) {
    super(id);
    connect(socket);
  }

  private void connect(Socket socket) {
    assert this.socket != null : "Should only call #connect() not already open";

    this.socket = socket;
    this.send = new SendThread(socket);
    this.receive = new ReceiveThread(socket);

    this.send.start();
    this.receive.start();
  }

  @Override
  public void open() throws IOException {
    if (this.socket != null) {
      throw new IOException("The connection has already been opened.");
    }

    connect(new Socket(hostName, port));
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    addMessage(channel, message);
  }

  @Override
  protected void onClose() {
    if (socket == null) {
      // Not open, so nothing to do.
      return;
    }

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
    return socket != null && !socket.isClosed();
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
            writeMessage(out, message);
          } catch (IOException e) {
            log.error("Error while writing message. Closing connection.", e);
            return;
          }
        }
      } finally {
        SocketConnection.this.close();
      }
    }

    protected final void writeMessage(OutputStream out, byte[] message) throws IOException {
      int length = message.length;

      notifyListeners(ActivityListener.Direction.Outbound, ActivityListener.State.Start, length, 0);

      out.write(length >> 24);
      out.write(length >> 16);
      out.write(length >> 8);
      out.write(length);

      for (int i = 0; i < message.length; i++) {
        out.write(message[i]);

        if (i != 0 && i % ActivityListener.CHUNK_SIZE == 0) {
          notifyListeners(
              ActivityListener.Direction.Outbound, ActivityListener.State.Progress, length, i);
        }
      }
      out.flush();
      notifyListeners(
          ActivityListener.Direction.Outbound, ActivityListener.State.Complete, length, length);
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
            byte[] message = readMessage(in);
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

    private byte[] readMessage(InputStream in) throws IOException {
      int b32 = in.read();
      int b24 = in.read();
      int b16 = in.read();
      int b8 = in.read();

      if (b32 < 0) {
        throw new IOException("Stream closed");
      }
      int length = (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;

      notifyListeners(ActivityListener.Direction.Inbound, ActivityListener.State.Start, length, 0);

      byte[] ret = new byte[length];
      for (int i = 0; i < length; i++) {
        ret[i] = (byte) in.read();

        if (i != 0 && i % ActivityListener.CHUNK_SIZE == 0) {
          notifyListeners(
              ActivityListener.Direction.Inbound, ActivityListener.State.Progress, length, i);
        }
      }
      notifyListeners(
          ActivityListener.Direction.Inbound, ActivityListener.State.Complete, length, length);
      return ret;
    }
  }
}
