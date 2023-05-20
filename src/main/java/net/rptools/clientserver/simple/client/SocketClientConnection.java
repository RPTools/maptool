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
package net.rptools.clientserver.simple.client;

import java.io.*;
import java.net.Socket;
import net.rptools.clientserver.simple.AbstractConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public class SocketClientConnection extends AbstractConnection implements ClientConnection {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(SocketClientConnection.class);

  private final String id;
  private SendThread send;
  private ReceiveThread receive;
  private Socket socket;
  private String hostName;
  private int port;

  public SocketClientConnection(String id, String hostName, int port) throws IOException {
    this.id = id;
    this.hostName = hostName;
    this.port = port;
  }

  public SocketClientConnection(String id, Socket socket) throws IOException {
    this.id = id;
    this.hostName = socket.getInetAddress().getHostName();
    this.port = socket.getPort();
    initialize(socket);
  }

  private void initialize(Socket socket) throws IOException {
    this.socket = socket;
    this.send = new SendThread(new BufferedOutputStream(socket.getOutputStream()));
    this.receive = new ReceiveThread(this, socket.getInputStream());
    this.send.start();
    this.receive.start();
  }

  public String getId() {
    return id;
  }

  @Override
  public void open() throws IOException {
    initialize(new Socket(hostName, port));
  }

  @Override
  public void sendMessage(byte[] message) {
    sendMessage(null, message);
  }

  public void sendMessage(Object channel, byte[] message) {
    addMessage(channel, message);
    synchronized (send) {
      send.notify();
    }
  }

  protected boolean isStopRequested() {
    return send.stopRequested;
  }

  public synchronized void close() {
    if (isStopRequested()) {
      return;
    }
    send.requestStop();
    receive.requestStop();

    try {
      socket.close();
    } catch (IOException e) {
      log.warn(e.toString());
    }
  }

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
    private final OutputStream out;
    private boolean stopRequested = false;

    public SendThread(OutputStream out) {
      setName("SocketClientConnection.SendThread");
      this.out = out;
    }

    public void requestStop() {
      this.stopRequested = true;
      synchronized (this) {
        this.notify();
      }
    }

    @Override
    public void run() {
      try {
        while (!stopRequested && SocketClientConnection.this.isAlive()) {
          try {
            while (SocketClientConnection.this.hasMoreMessages()) {
              try {
                byte[] message = SocketClientConnection.this.nextMessage();
                if (message == null) {
                  continue;
                }
                SocketClientConnection.this.writeMessage(out, message);
              } catch (IndexOutOfBoundsException e) {
                // just ignore and wait
              }
            }
            synchronized (this) {
              if (!stopRequested) {
                this.wait();
              }
            }
          } catch (InterruptedException e) {
            // do nothing
          }
        }
      } catch (IOException e) {
        log.error(e);
        fireDisconnect();
      }
    }
  }

  // /////////////////////////////////////////////////////////////////////////
  // receive thread
  // /////////////////////////////////////////////////////////////////////////
  private class ReceiveThread extends Thread {
    private final SocketClientConnection conn;
    private final InputStream in;
    private boolean stopRequested = false;

    public ReceiveThread(SocketClientConnection conn, InputStream in) {
      setName("SocketClientConnection.ReceiveThread");
      this.conn = conn;
      this.in = in;
    }

    public void requestStop() {
      stopRequested = true;
    }

    @Override
    public void run() {
      while (!stopRequested && conn.isAlive()) {
        try {
          byte[] message = conn.readMessage(in);
          conn.dispatchCompressedMessage(conn.id, message);
        } catch (IOException e) {
          log.error(e);
          fireDisconnect();
          break;
        } catch (Throwable t) {
          log.error(t);
          // don't let anything kill this thread via exception
          t.printStackTrace();
        }
      }
    }
  }
}
