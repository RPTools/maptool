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
package net.rptools.clientserver.simple.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.simple.AbstractConnection;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractClientConnection extends AbstractConnection
    implements ClientConnection {
  private static final Logger log = LogManager.getLogger(AbstractClientConnection.class);

  private final Map<Object, List<byte[]>> outQueueMap = new HashMap<>();
  private final List<List<byte[]>> outQueueList = new LinkedList<>();

  private List<byte[]> getOutQueue(Object channel) {
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

  private byte[] compress(byte[] message) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(message.length);
      OutputStream ios = new LZMACompressorOutputStream(baos);
      ios.write(message);
      ios.close();

      var compressedMessage = baos.toByteArray();
      return compressedMessage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected synchronized void addMessage(Object channel, byte[] message) {
    List<byte[]> queue = getOutQueue(channel);
    queue.add(compress(message));
    // Queue up for sending
    outQueueList.add(queue);
  }

  protected synchronized boolean hasMoreMessages() {
    return !outQueueList.isEmpty();
  }

  protected synchronized byte[] nextMessage() {
    if (!hasMoreMessages()) {
      return null;
    }
    List<byte[]> queue = outQueueList.remove(0);

    if (queue.isEmpty()) return null;

    byte[] message = queue.remove(0);
    if (!queue.isEmpty()) {
      outQueueList.add(queue);
    }
    return message;
  }

  protected final void writeMessage(OutputStream out, byte[] message) throws IOException {
    int length = message.length;

    notifyListeners(ActivityListener.Direction.Outbound, ActivityListener.State.Start, length, 0);

    out.write(length >> 24);
    out.write(length >> 16);
    out.write(length >> 8);
    out.write(length);

    for (int i = 0; i < message.length; i++) {
      out.write(message[i]);

      if (i != 0 && i % ActivityListener.CHUNK_SIZE == 0) {
        notifyListeners(
            ActivityListener.Direction.Outbound, ActivityListener.State.Progress, length, i);
      }
    }
    out.flush();
    notifyListeners(
        ActivityListener.Direction.Outbound, ActivityListener.State.Complete, length, length);
  }
}
