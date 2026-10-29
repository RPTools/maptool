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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.clientserver.simple.connection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.rptools.clientserver.ActivityListener;
import org.junit.jupiter.api.Test;

/** How a connection reads messages, including from peers that send nonsense. */
class SocketConnectionFrameTest {
  private static final SocketConnection.FrameProgress IGNORE = (state, length, done) -> {};

  private static byte[] frame(int length, byte[] body) {
    byte[] out = new byte[4 + body.length];
    out[0] = (byte) (length >> 24);
    out[1] = (byte) (length >> 16);
    out[2] = (byte) (length >> 8);
    out[3] = (byte) length;
    System.arraycopy(body, 0, out, 4, body.length);
    return out;
  }

  private static byte[] randomBytes(int n) {
    byte[] b = new byte[n];
    new Random(42).nextBytes(b);
    return b;
  }

  /** A stream that hands out at most a few bytes per read, as a socket may. */
  private static InputStream trickle(byte[] data) {
    return new ByteArrayInputStream(data) {
      @Override
      public synchronized int read(byte[] b, int off, int len) {
        return super.read(b, off, Math.min(len, 7));
      }
    };
  }

  @Test
  void readsMessagesBackToBack() throws IOException {
    byte[] first = randomBytes(10_000);
    byte[] second = "second".getBytes();
    byte[] a = frame(first.length, first);
    byte[] b = frame(second.length, second);
    byte[] both = new byte[a.length + b.length];
    System.arraycopy(a, 0, both, 0, a.length);
    System.arraycopy(b, 0, both, a.length, b.length);

    InputStream in = trickle(both);
    assertArrayEquals(first, SocketConnection.readFrame(in, 1 << 20, IGNORE));
    assertArrayEquals(second, SocketConnection.readFrame(in, 1 << 20, IGNORE));
  }

  @Test
  void readsAnEmptyMessage() throws IOException {
    assertEquals(
        0,
        SocketConnection.readFrame(new ByteArrayInputStream(frame(0, new byte[0])), 10, IGNORE)
            .length);
  }

  @Test
  void growsTheBufferForMessagesLargerThanTheEagerAllocation() throws IOException {
    byte[] body = randomBytes(SocketConnection.EAGER_ALLOCATION_BYTES * 2 + 12_345);
    byte[] read =
        SocketConnection.readFrame(
            new ByteArrayInputStream(frame(body.length, body)), Integer.MAX_VALUE, IGNORE);
    assertArrayEquals(body, read);
  }

  @Test
  void reportsStartProgressAndCompletion() throws IOException {
    byte[] body = randomBytes(ActivityListener.CHUNK_SIZE * 3);
    List<ActivityListener.State> states = new ArrayList<>();
    SocketConnection.readFrame(
        new ByteArrayInputStream(frame(body.length, body)),
        Integer.MAX_VALUE,
        (state, length, done) -> {
          assertEquals(body.length, length);
          states.add(state);
        });
    assertEquals(ActivityListener.State.Start, states.get(0));
    assertTrue(states.contains(ActivityListener.State.Progress));
    assertEquals(ActivityListener.State.Complete, states.get(states.size() - 1));
  }

  @Test
  void aClosedStreamIsReportedAsClosed() {
    IOException e =
        assertThrows(
            IOException.class,
            () -> SocketConnection.readFrame(new ByteArrayInputStream(new byte[0]), 10, IGNORE));
    assertEquals("Stream closed", e.getMessage());
  }

  @Test
  void aStreamThatEndsInsideTheHeaderIsAnError() {
    assertThrows(
        EOFException.class,
        () -> SocketConnection.readFrame(new ByteArrayInputStream(new byte[] {0, 0}), 10, IGNORE));
  }

  @Test
  void aStreamThatEndsInsideTheBodyIsAnErrorNotAPaddedMessage() {
    // The old reader stored read()'s -1 as a byte and returned a message padded with 0xFF.
    byte[] truncated = frame(100, new byte[40]);
    assertThrows(
        EOFException.class,
        () -> SocketConnection.readFrame(new ByteArrayInputStream(truncated), 1000, IGNORE));
  }

  @Test
  void aNegativeLengthIsRejected() {
    byte[] header = {(byte) 0x96, 0x1D, 0x0B, 0x6D}; // a negative length, as a corrupt stream gives
    assertThrows(
        IOException.class,
        () -> SocketConnection.readFrame(new ByteArrayInputStream(header), 1 << 30, IGNORE));
  }

  @Test
  void aLengthAboveTheLimitIsRejectedBeforeReadingTheBody() {
    InputStream in = new ByteArrayInputStream(frame(1001, new byte[1001]));
    assertThrows(IOException.class, () -> SocketConnection.readFrame(in, 1000, IGNORE));
  }

  @Test
  void aHugeClaimedLengthDoesNotReserveMemoryUpFront() {
    // A peer that claims nearly a gigabyte and then sends little gets an error once the stream
    // ends. The buffer only grows as bytes arrive, so this costs a few megabytes rather than the
    // gigabyte claimed; the test checks the outcome, not the memory, which depends on the heap the
    // test runs with.
    int claimed = (1 << 30) - 1;
    byte[] bytes = frame(claimed, new byte[1000]);
    assertThrows(
        EOFException.class,
        () -> SocketConnection.readFrame(new ByteArrayInputStream(bytes), 1 << 30, IGNORE));
  }
}
