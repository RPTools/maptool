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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
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

  public SocketConnection(String id, String hostName, int port) throws IOException {
    this.id = id;
    this.hostName = hostName;
    this.port = port;
  }

  public SocketConnection(String id, Socket socket) throws IOException {
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
      setName("SocketConnection.SendThread");
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
        while (!stopRequested && SocketConnection.this.isAlive()) {
          try {
            while (SocketConnection.this.hasMoreMessages()) {
              try {
                byte[] message = SocketConnection.this.nextMessage();
                if (message == null) {
                  continue;
                }
                SocketConnection.this.writeMessage(out, message);
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
    private final SocketConnection conn;
    private final InputStream in;
    private boolean stopRequested = false;

    public ReceiveThread(SocketConnection conn, InputStream in) {
      setName("SocketConnection.ReceiveThread");
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
