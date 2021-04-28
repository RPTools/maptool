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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.hessian.client.IMethodClientConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.AbstractClientConnection;
import net.rptools.maptool.client.ui.ActivityMonitorPanel;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.server.Handshake;
import net.rptools.maptool.server.ServerConfig;

/** @author trevor */
public class MapToolConnection {
  private final Player player;
  private IMethodClientConnection connection;

  public MapToolConnection(ServerConfig config, Player player) throws IOException {
    this.connection = ConnectionFactory.getInstance().createClientConnection(null, config);
    this.player = player;
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.clientserver.simple.client.ClientConnection#sendHandshake( java.net.Socket)
   */
  public boolean sendHandshake() throws IOException {
    Handshake.Response response = null;
    try {
      var request = new Handshake.Request(player.getName(), player.getPassword(), player.getRole(), MapTool.getVersion());
      response = Handshake.sendHandshake(request,connection);
    } catch (IllegalBlockSizeException
        | InvalidKeyException
        | BadPaddingException
        | NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeySpecException e) {
      MapTool.showError("Handshake.msg.encodeInitFail", e);
      return false;
    }

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

  public void start() throws IOException {
    if (sendHandshake()) {
      connection.start();
    } else {
      connection.close();
    }
  }

  public void addMessageHandler(ClientMethodHandler handler) {
    connection.addMessageHandler(handler);
  }

  public void addActivityListener(ActivityMonitorPanel activityMonitor) {
    connection.addActivityListener(activityMonitor);
  }

  public void addDisconnectHandler(ServerDisconnectHandler serverDisconnectHandler) {
    connection.addDisconnectHandler(serverDisconnectHandler);
  }

  public boolean isAlive() {
    return connection.isAlive();
  }

  public void close() throws IOException {
    connection.close();
  }

  public void callMethod(String name, Object[] params) {
    connection.callMethod(name, params);
  }
}
