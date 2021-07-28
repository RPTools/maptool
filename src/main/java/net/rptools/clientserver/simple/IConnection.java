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
package net.rptools.clientserver.simple;

import net.rptools.clientserver.ActivityListener;

public interface IConnection {
  void addMessageHandler(MessageHandler handler);

  void removeMessageHandler(MessageHandler handler);

  void addMessage(Object channel, byte[] message);

  boolean hasMoreMessages();

  byte[] nextMessage();

  void fireDisconnect();

  void addActivityListener(ActivityListener listener);

  void removeActivityListener(ActivityListener listener);

  void addDisconnectHandler(DisconnectHandler handler);

  void removeDisconnectHandler(DisconnectHandler handler);

  void close();
}
