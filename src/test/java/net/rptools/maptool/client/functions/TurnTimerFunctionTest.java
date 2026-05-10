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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.rptools.parser.ParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the scheduling/lifecycle parts of {@link TurnTimerFunction} that do not require a
 * running MapTool — registration, replacement, cancellation, remaining-time math, and the
 * initiative auto-restart hook. Callback dispatch is intentionally not exercised here: it requires
 * the live macro parser. Tests use empty-callback timers so {@code fire()} becomes a no-op when
 * they elapse.
 */
class TurnTimerFunctionTest {

  @AfterEach
  void clearTimers() {
    TurnTimerFunction.cancelAll();
  }

  @Test
  void startRegistersATimerThatCountsDown() throws Exception {
    TurnTimerFunction.start("t1", 2.0, "", "");
    double remaining = TurnTimerFunction.remainingSeconds("t1");
    assertTrue(remaining > 1.5 && remaining <= 2.0, "expected ~2s remaining, was " + remaining);
  }

  @Test
  void stopRemovesTheTimer() throws Exception {
    TurnTimerFunction.start("t2", 5.0, "", "");
    assertTrue(TurnTimerFunction.stop("t2"));
    assertEquals(0.0, TurnTimerFunction.remainingSeconds("t2"));
    assertFalse(TurnTimerFunction.stop("t2"));
  }

  @Test
  void startWithSameNameReplacesThePriorTimer() throws Exception {
    TurnTimerFunction.start("dup", 60.0, "", "");
    double first = TurnTimerFunction.remainingSeconds("dup");
    TurnTimerFunction.start("dup", 2.0, "", "");
    double second = TurnTimerFunction.remainingSeconds("dup");
    assertTrue(first > 30, "first timer should have a long remaining; was " + first);
    assertTrue(second <= 2.0, "replacement should shorten remaining; was " + second);
  }

  @Test
  void invalidDurationsAreRejected() {
    assertThrows(ParserException.class, () -> TurnTimerFunction.start("bad", 0.0, "", ""));
    assertThrows(ParserException.class, () -> TurnTimerFunction.start("bad", -1.0, "", ""));
  }

  @Test
  void timerElapsesToZero() throws Exception {
    TurnTimerFunction.start("quick", 0.05, "", "");
    Thread.sleep(120);
    assertEquals(0.0, TurnTimerFunction.remainingSeconds("quick"));
  }

  @Test
  void cancelAllClearsEverything() throws Exception {
    TurnTimerFunction.start("a", 10.0, "", "");
    TurnTimerFunction.start("b", 10.0, "", "");
    TurnTimerFunction.cancelAll();
    assertEquals(0.0, TurnTimerFunction.remainingSeconds("a"));
    assertEquals(0.0, TurnTimerFunction.remainingSeconds("b"));
  }
}
