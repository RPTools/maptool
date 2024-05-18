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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DirectConnection extends AbstractConnection {
  private static final Logger log = LogManager.getLogger(DirectConnection.class);

  public record Pair(DirectConnection clientSide, DirectConnection serverSide) {}

  public static Pair create(String id) {
    var closed = new AtomicBoolean(false);
    var clientToServerQueue = new ArrayBlockingQueue<byte[]>(128);
    var serverToClientQueue = new ArrayBlockingQueue<byte[]>(128);

    var clientSide =
        new DirectConnection(closed, id + "-client", clientToServerQueue, serverToClientQueue);
    var serverSide =
        new DirectConnection(closed, id + "-server", serverToClientQueue, clientToServerQueue);

    return new Pair(clientSide, serverSide);
  }

  private final AtomicBoolean sharedClosedFlag;
  private final String id;
  private final BlockingQueue<byte[]> writeQueue;
  private final ReceiveThread receiveThread;

  private DirectConnection(
      AtomicBoolean sharedClosedFlag,
      String id,
      BlockingQueue<byte[]> writeQueue,
      BlockingQueue<byte[]> readQueue) {
    this.sharedClosedFlag = sharedClosedFlag;
    this.id = id;
    this.writeQueue = writeQueue;
    this.receiveThread = new ReceiveThread(readQueue);
  }

  @Override
  public void open() {
    receiveThread.start();
  }

  @Override
  protected void onClose() {
    // Tell the other end about our closure.
    sharedClosedFlag.set(true);
    receiveThread.interrupt();
  }

  @Override
  public void sendMessage(Object channel, byte[] message) {
    if (message.length == 0) {
      return;
    }

    boolean written = false;
    while (!written) {
      try {
        // Set a timeout so we have a chance to escape in case we weren't notified properly.
        written = writeQueue.offer(message, 10, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        // Just try again.
      }
    }
  }

  @Override
  public boolean isAlive() {
    return !sharedClosedFlag.get();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getError() {
    return null;
  }

  private final class ReceiveThread extends Thread {
    private final BlockingQueue<byte[]> readQueue;

    public ReceiveThread(BlockingQueue<byte[]> readQueue) {
      super("DirectConnection.ReceiveThread");
      this.readQueue = readQueue;
    }

    @Override
    public void run() {
      try {
        while (!DirectConnection.this.isClosed() && DirectConnection.this.isAlive()) {
          try {
            // Set a timeout so we have a chance to escape in case we weren't notified properly.
            byte[] message;
            try {
              message = readQueue.poll(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
              // Just try again.
              continue;
            }

            if (message != null) {
              DirectConnection.this.dispatchMessage(message);
            }
          } catch (Throwable t) {
            // don't let anything kill this thread via exception
            log.error("Unexpected error in receive thread", t);
          }
        }
      } finally {
        DirectConnection.this.close();
        fireDisconnect();
      }
    }
  }
}
