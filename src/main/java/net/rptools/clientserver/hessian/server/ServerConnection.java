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
package net.rptools.clientserver.hessian.server;

import java.io.IOException;
import net.rptools.clientserver.hessian.HessianUtils;

/** @author drice */
public class ServerConnection extends net.rptools.clientserver.simple.server.ServerConnection {
  public ServerConnection(int port) throws IOException {
    super(port);
  }

  public void broadcastCallMethod(String method, Object... parameters) {
    broadcastMessage(HessianUtils.methodToBytesGZ(method, parameters));
  }

  public void broadcastCallMethod(String[] exclude, String method, Object... parameters) {
    byte[] data = HessianUtils.methodToBytesGZ(method, parameters);
    broadcastMessage(exclude, data);
  }

  public void callMethod(String id, String method, Object... parameters) {
    byte[] data = HessianUtils.methodToBytesGZ(method, parameters);
    sendMessage(id, null, data);
  }

  public void callMethod(String id, Object channel, String method, Object... parameters) {
    byte[] data = HessianUtils.methodToBytesGZ(method, parameters);
    sendMessage(id, channel, data);
  }
}
