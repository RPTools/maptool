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
package net.rptools.clientserver.simple.connection;

import java.io.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractConnection implements Connection {
  private static final Logger log = LogManager.getLogger(AbstractConnection.class);

  private final String id;
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final BlockingQueue<byte[]> outQueue = new LinkedBlockingQueue<>();

  private final List<DisconnectHandler> disconnectHandlers = new CopyOnWriteArrayList<>();
  private final List<ActivityListener> listeners = new CopyOnWriteArrayList<>();
  private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();

  protected AbstractConnection(String id) {
    this.id = id;
  }

  @Override
  public final String getId() {
    return id;
  }

  @Override
  public final void close() {
    if (closed.compareAndSet(false, true)) {
      onClose();
    }
  }

  protected final boolean isClosed() {
    return closed.get();
  }

  protected abstract void onClose();

  private byte[] compress(byte[] message) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(message.length);
      OutputStream ios = new ZstdCompressorOutputStream(baos);
      ios.write(message);
      ios.close();
      var compressedMessage = baos.toByteArray();
      return compressedMessage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private byte[] inflate(byte[] compressedMessage) {
    InputStream bytesIn = new ByteArrayInputStream(compressedMessage);
    try {
      InputStream ios = new ZstdCompressorInputStream(bytesIn);
      var decompressed = ios.readAllBytes();
      ios.close();
      return decompressed;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void addMessage(Object channel, byte[] message) {
    outQueue.add(compress(message));
  }

  protected byte[] nextMessage() {
    try {
      // Bit paranoid, but don't wait forever for a message - that can perpetually block the thread.
      return outQueue.poll(10, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return null;
    }
  }

  public final void addMessageHandler(MessageHandler handler) {
    messageHandlers.add(handler);
  }

  public final void removeMessageHandler(MessageHandler handler) {
    messageHandlers.remove(handler);
  }

  protected void dispatchMessage(byte[] message) {
    var id = getId();
    if (messageHandlers.isEmpty()) {
      log.warn("message received but not messageHandlers registered for {}.", id);
    }

    for (MessageHandler handler : messageHandlers) {
      handler.handleMessage(id, message);
    }
  }

  protected final void dispatchCompressedMessage(byte[] compressedMessage) {
    var message = inflate(compressedMessage);
    dispatchMessage(message);
  }

  protected final void fireDisconnect() {
    for (DisconnectHandler handler : disconnectHandlers) {
      handler.handleDisconnect(this);
    }
  }

  public final void addDisconnectHandler(DisconnectHandler handler) {
    disconnectHandlers.add(handler);
  }

  public final void removeDisconnectHandler(DisconnectHandler handler) {
    disconnectHandlers.remove(handler);
  }

  public final void addActivityListener(ActivityListener listener) {
    listeners.add(listener);
  }

  public final void removeActivityListener(ActivityListener listener) {
    listeners.remove(listener);
  }

  protected void notifyListeners(
      ActivityListener.Direction direction,
      ActivityListener.State state,
      int totalTransferSize,
      int currentTransferSize) {
    for (ActivityListener listener : listeners) {
      listener.notify(direction, state, totalTransferSize, currentTransferSize);
    }
  }
}
