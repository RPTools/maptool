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
package net.rptools.maptool.client;

import java.io.IOException;
import java.net.Socket;
import net.rptools.clientserver.hessian.client.ClientConnection;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.server.Handshake;

/** @author trevor */
public class MapToolConnection extends ClientConnection {
  private final Player player;

  public MapToolConnection(String host, int port, Player player) throws IOException {
    super(host, port, null);
    this.player = player;
  }

  public MapToolConnection(Socket socket, Player player) throws IOException {
    super(socket, null);
    this.player = player;
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.clientserver.simple.client.ClientConnection#sendHandshake( java.net.Socket)
   */
  @Override
  public boolean sendHandshake(Socket s) throws IOException {
    Handshake.Response response =
        Handshake.sendHandshake(
            new Handshake.Request(
                player.getName(), player.getPassword(), player.getRole(), MapTool.getVersion()),
            s);

    if (response.code != Handshake.Code.OK) {
      MapTool.showError("ERROR: " + response.message);
      return false;
    }
    boolean result = response.code == Handshake.Code.OK;
    if (result) {
      MapTool.setServerPolicy(response.policy);
    }
    return result;
  }
}
