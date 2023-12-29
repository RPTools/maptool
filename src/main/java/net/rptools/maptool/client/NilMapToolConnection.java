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
import java.util.ArrayList;
import java.util.List;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.maptool.client.ui.ActivityMonitorPanel;
import net.rptools.maptool.server.proto.Message;

public class NilMapToolConnection implements IMapToolConnection {
  private final List<ClientMessageHandler> handlers = new ArrayList<>();
  private boolean isAlive = true;

  @Override
  public void onCompleted(Runnable onCompleted) {
    onCompleted.run();
  }

  @Override
  public void start() throws IOException {}

  @Override
  public void addMessageHandler(ClientMessageHandler handler) {
    handlers.add(handler);
  }

  @Override
  public boolean isAlive() {
    return isAlive;
  }

  @Override
  public void close() {
    isAlive = false;
  }

  @Override
  public void sendMessage(Message msg) {
    // Black hole the message since there is not server on the other end.
  }

  @Override
  public void addActivityListener(ActivityMonitorPanel activityMonitor) {}

  @Override
  public void addDisconnectHandler(DisconnectHandler serverDisconnectHandler) {}
}
