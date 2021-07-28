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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public class SocketClientConnection extends AbstractClientConnection {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(SocketClientConnection.class);

  private Socket socket;
  private DataOutputStream dos;
  private DataInputStream dis;

  public SocketClientConnection(String id, String hostName, int port) throws IOException {
    this(id, new Socket(hostName, port));
  }

  public SocketClientConnection(String id, Socket socket) throws IOException {
    super(id);
    this.socket = socket;
    this.dis = new DataInputStream(socket.getInputStream());
    this.dos = new DataOutputStream(socket.getOutputStream());
  }

  public boolean isAlive() {
    return !socket.isClosed();
  }

  public synchronized void close() {
    if (isStopRequested()) {
      return;
    }
    try {
      socket.close();
    } catch (IOException e) {
      log.warn(e.toString());
    }
    super.close();
  }

  public DataInputStream getInputStream() {
    return dis;
  }

  public DataOutputStream getOutputSream() {
    return dos;
  }
}
