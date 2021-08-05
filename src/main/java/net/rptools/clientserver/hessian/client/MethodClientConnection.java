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
import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.hessian.HessianUtils;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.IClientConnection;

/** @author drice */
public class MethodClientConnection implements IMethodClientConnection {
  private IClientConnection connection;

  public MethodClientConnection(IClientConnection connection) {
    this.connection = connection;
  }

  public void callMethod(String method, Object... parameters) {

    byte[] message = HessianUtils.methodToBytesGZ(method, parameters);
    sendMessage(message);
  }

  @Override
  public void sendMessage(byte[] message) {
    connection.sendMessage(message);
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    connection.sendMessage(channel, message);
  }

  @Override
  public String getId() {
    return connection.getId();
  }

  @Override
  public void addMessageHandler(MessageHandler handler) {
    connection.addMessageHandler(handler);
  }

  @Override
  public void removeMessageHandler(MessageHandler handler) {
    connection.removeMessageHandler(handler);
  }

  @Override
  public void addMessage(Object channel, byte[] message) {
    connection.addMessage(channel, message);
  }

  @Override
  public boolean hasMoreMessages() {
    return connection.hasMoreMessages();
  }

  @Override
  public byte[] nextMessage() {
    return connection.nextMessage();
  }

  @Override
  public void fireDisconnect() {
    connection.fireDisconnect();
  }

  @Override
  public void addActivityListener(ActivityListener listener) {
    connection.addActivityListener(listener);
  }

  @Override
  public void removeActivityListener(ActivityListener listener) {
    connection.removeActivityListener(listener);
  }

  @Override
  public void addDisconnectHandler(DisconnectHandler handler) {
    connection.addDisconnectHandler(handler);
  }

  @Override
  public void removeDisconnectHandler(DisconnectHandler handler) {
    connection.removeDisconnectHandler(handler);
  }

  @Override
  public void open() throws IOException {
    connection.open();
  }

  @Override
  public void close() {
    connection.close();
  }

  @Override
  public boolean isAlive() {
    return connection.isAlive();
  }
}
