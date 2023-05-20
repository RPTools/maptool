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

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.ActivityListener.Direction;
import net.rptools.clientserver.ActivityListener.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public abstract class AbstractConnection implements Connection {
  // We don't need to make each list synchronized since the class is synchronized

  private static final Logger log = LogManager.getLogger(AbstractConnection.class);
  protected List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<MessageHandler>();
  protected List<ActivityListener> listeners = new CopyOnWriteArrayList<ActivityListener>();

  public final void addMessageHandler(MessageHandler handler) {
    messageHandlers.add(handler);
  }

  public final void removeMessageHandler(MessageHandler handler) {
    messageHandlers.remove(handler);
  }

  public final void dispatchMessage(String id, byte[] message) {
    if (messageHandlers.size() == 0) {
      log.warn("message received but not messageHandlers registered.");
    }

    for (MessageHandler handler : messageHandlers) {
      handler.handleMessage(id, message);
    }
  }

  public final void addActivityListener(ActivityListener listener) {
    listeners.add(listener);
  }

  public final void removeActivityListener(ActivityListener listener) {
    listeners.remove(listener);
  }

  protected final void notifyListeners(
      Direction direction, State state, int totalTransferSize, int currentTransferSize) {
    for (ActivityListener listener : listeners) {
      listener.notify(direction, state, totalTransferSize, currentTransferSize);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // static helper methods
  ///////////////////////////////////////////////////////////////////////////

  public abstract String getError();
}
