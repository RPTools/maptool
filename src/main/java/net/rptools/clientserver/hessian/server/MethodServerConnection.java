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

import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.hessian.HessianUtils;
import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.server.IServerConnection;
import net.rptools.clientserver.simple.server.ServerObserver;

/** @author drice */
public class MethodServerConnection implements IMethodServerConnection {
  private IServerConnection connection;

  public MethodServerConnection(IServerConnection connection) {
    this.connection = connection;
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
  public void close() {
    connection.close();
  }

  @Override
  public void handleDisconnect(AbstractConnection conn) {
    connection.handleDisconnect(conn);
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    connection.handleMessage(id, message);
  }

  @Override
  public void addObserver(ServerObserver observer) {
    connection.addObserver(observer);
  }

  @Override
  public void removeObserver(ServerObserver observer) {
    connection.removeObserver(observer);
  }

  @Override
  public void broadcastMessage(byte[] message) {
    connection.broadcastMessage(message);
  }

  @Override
  public void broadcastMessage(String[] exclude, byte[] message) {
    connection.broadcastMessage(exclude, message);
  }

  @Override
  public void sendMessage(String id, byte[] message) {
    connection.sendMessage(id, message);
  }

  @Override
  public void sendMessage(String id, Object channel, byte[] message) {
    connection.sendMessage(id, channel, message);
  }
}
