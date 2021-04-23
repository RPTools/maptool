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
package net.rptools.clientserver.hessian.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import net.rptools.clientserver.hessian.HessianUtils;

/** @author drice */
public class ClientConnection extends net.rptools.clientserver.simple.client.ClientConnection {

  public ClientConnection(String host, int port, String id)
      throws UnknownHostException, IOException {
    super(host, port, id);
  }

  public ClientConnection(Socket socket, String id) throws IOException {
    super(socket, id);
  }

  public void callMethod(String method, Object... parameters) {

    byte[] message = HessianUtils.methodToBytesGZ(method, parameters);
    sendMessage(message);
  }
}
