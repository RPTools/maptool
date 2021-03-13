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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.ActivityListener.Direction;
import net.rptools.clientserver.ActivityListener.State;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public abstract class AbstractConnection {
  // We don't need to make each list synchronized since the class is synchronized

  protected Map<Object, List<byte[]>> outQueueMap = new HashMap<Object, List<byte[]>>();
  protected List<List<byte[]>> outQueueList = new LinkedList<List<byte[]>>();
  protected List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<MessageHandler>();
  protected List<ActivityListener> listeners = new CopyOnWriteArrayList<ActivityListener>();
  protected List<DisconnectHandler> disconnectHandlers =
      new CopyOnWriteArrayList<DisconnectHandler>();

  public final void addMessageHandler(MessageHandler handler) {
    messageHandlers.add(handler);
  }

  public final void removeMessageHandler(MessageHandler handler) {
    messageHandlers.remove(handler);
  }

  protected final void dispatchMessage(String id, byte[] message) {
    for (MessageHandler handler : messageHandlers) {
      handler.handleMessage(id, message);
    }
  }

  public synchronized void addMessage(byte[] message) {
    addMessage(null, message);
  }

  public synchronized void addMessage(Object channel, byte[] message) {
    List<byte[]> queue = getOutQueue(channel);
    queue.add(message);
    // Queue up for sending
    outQueueList.add(queue);
  }

  protected List<byte[]> getOutQueue(Object channel) {
    // Ordinarily I would synchronize this method, but I imagine the channels will be initialized
    // once
    // at the beginning of execution.  Thus get(channel) will only return once right at the
    // beginning
    // no sense incurring the cost of synchronizing the method on the class for that.
    List<byte[]> queue = outQueueMap.get(channel);
    if (queue == null) {
      queue = Collections.synchronizedList(new ArrayList<byte[]>());
      outQueueMap.put(channel, queue);
    }
    return queue;
  }

  public synchronized boolean hasMoreMessages() {
    return !outQueueList.isEmpty();
  }

  public synchronized byte[] nextMessage() {
    if (!hasMoreMessages()) {
      return null;
    }
    List<byte[]> queue = outQueueList.remove(0);

    byte[] message = queue.remove(0);
    if (!queue.isEmpty()) {
      outQueueList.add(queue);
    }
    return message;
  }

  public final void fireDisconnect() {
    for (DisconnectHandler handler : disconnectHandlers) {
      handler.handleDisconnect(this);
    }
  }

  public final void addActivityListener(ActivityListener listener) {
    listeners.add(listener);
  }

  public final void removeActivityListener(ActivityListener listener) {
    listeners.remove(listener);
  }

  public final void addDisconnectHandler(DisconnectHandler handler) {
    disconnectHandlers.add(handler);
  }

  public final void removeDisconnectHandler(DisconnectHandler handler) {
    disconnectHandlers.remove(handler);
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
  protected final void writeMessage(OutputStream out, byte[] message) throws IOException {
    int length = message.length;

    notifyListeners(Direction.Outbound, State.Start, length, 0);

    out.write(length >> 24);
    out.write(length >> 16);
    out.write(length >> 8);
    out.write(length);

    for (int i = 0; i < message.length; i++) {
      out.write(message[i]);

      if (i != 0 && i % ActivityListener.CHUNK_SIZE == 0) {
        notifyListeners(Direction.Outbound, State.Progress, length, i);
      }
    }
    out.flush();
    notifyListeners(Direction.Outbound, State.Complete, length, length);
  }

  protected final byte[] readMessage(InputStream in) throws IOException {
    int b32 = in.read();
    int b24 = in.read();
    int b16 = in.read();
    int b8 = in.read();

    if (b32 < 0) {
      throw new IOException("Stream closed");
    }
    int length = (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;

    notifyListeners(Direction.Inbound, State.Start, length, 0);

    byte[] ret = new byte[length];
    for (int i = 0; i < length; i++) {
      ret[i] = (byte) in.read();

      if (i != 0 && i % ActivityListener.CHUNK_SIZE == 0) {
        notifyListeners(Direction.Inbound, State.Progress, length, i);
      }
    }
    notifyListeners(Direction.Inbound, State.Complete, length, length);
    return ret;
  }
}
