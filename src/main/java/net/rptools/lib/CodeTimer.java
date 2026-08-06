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
package net.rptools.lib;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodeTimer {
  private static final ThreadLocal<CodeTimer> ROOT_TIMER =
      ThreadLocal.withInitial(() -> new CodeTimer(""));
  private static final ThreadLocal<List<CodeTimer>> timerStack =
      ThreadLocal.withInitial(ArrayList::new);
  private static final Logger log = LogManager.getLogger(CodeTimer.class);

  @FunctionalInterface
  public interface TimedSection<Ex extends Throwable> {
    void call(CodeTimer timer) throws Ex;
  }

  public static <Ex extends Exception> void using(String name, TimedSection<Ex> callback)
      throws Ex {
    var stack = timerStack.get();

    var timer = new CodeTimer(name);
    timer.setEnabled(AppState.isCollectProfilingData());

    stack.addLast(timer);
    try {
      timer.start("<root>");
      callback.call(timer);
    } finally {
      timer.stop("<root>");

      final var lastTimer = stack.removeLast();
      assert lastTimer == timer : "Timer stack is corrupted";

      timer.complete();
    }
  }

  public static CodeTimer get() {
    final var stack = timerStack.get();
    return stack.isEmpty() ? ROOT_TIMER.get() : stack.getLast();
  }

  private enum TimerEventType {
    Start,
    Stop
  }

  private record TimerEvent(TimerEventType type, long timeNs, String id, Object[] parameters) {}

  private record CounterEvent(long amount, String id) {}

  private ArrayList<TimerEvent> timerEvents = new ArrayList<>(100);
  private ArrayList<CounterEvent> counterEvents = new ArrayList<>(100);

  private final String name;
  private long threshold = 1;
  private TimeUnit reportingUnit = TimeUnit.MILLISECONDS;
  private boolean enabled;

  private CodeTimer(String n) {
    name = n;
    enabled = true;
  }

  public void setThreshold(long threshold) {
    this.threshold = TimeUnit.NANOSECONDS.convert(threshold, TimeUnit.MILLISECONDS);
  }

  public void setThreshold(long threshold, TimeUnit timeUnit) {
    this.threshold = TimeUnit.NANOSECONDS.convert(threshold, timeUnit);
  }

  public void setReportingUnit(TimeUnit reportingUnit) {
    this.reportingUnit = reportingUnit;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void increment(String id) {
    increment(id, 1);
  }

  public void increment(String id, int amount) {
    if (!enabled) {
      return;
    }
    counterEvents.add(new CounterEvent(amount, id));
  }

  public void start(String id, Object... parameters) {
    if (!enabled) {
      return;
    }
    timerEvents.add(new TimerEvent(TimerEventType.Start, System.nanoTime(), id, parameters));
  }

  public void stop(String id, Object... parameters) {
    if (!enabled) {
      return;
    }
    timerEvents.add(new TimerEvent(TimerEventType.Stop, System.nanoTime(), id, parameters));
  }

  private void complete() {
    if (!enabled) {
      return;
    }

    var name = this.name;
    var reportingUnit = this.reportingUnit;
    var threshold = this.threshold;
    var timerEvents = this.timerEvents;
    var counterEvents = this.counterEvents;
    this.timerEvents = new ArrayList<>();
    this.counterEvents = new ArrayList<>();

    EventQueue.invokeLater(
        () -> {
          String results = getResults(name, reportingUnit, threshold, timerEvents, counterEvents);
          MapTool.getProfilingNoteFrame().addText(results);
        });
  }

  private static String getResults(
      String name,
      TimeUnit reportingUnit,
      long threshold,
      List<TimerEvent> timerEvents,
      List<CounterEvent> counterEvents) {
    final Map<String, Long> counterMap = new LinkedHashMap<>();
    // Maps timer names to start times.
    final Map<String, Timer> timeMap = new LinkedHashMap<>();

    // Count up all the timing events.
    for (var event : timerEvents) {
      var id = String.format(event.id(), event.parameters());
      var timer = timeMap.computeIfAbsent(id, Timer::new);
      switch (event.type()) {
        case Start -> timer.startAt(event.timeNs());
        case Stop -> timer.stopAt(event.timeNs());
      }
    }

    // Count up all the counter events.
    for (var event : counterEvents) {
      counterMap.merge(event.id(), event.amount(), Long::sum);
    }

    StringBuilder builder = new StringBuilder(100);
    builder
        .append("Timer ")
        .append(name)
        .append(" (")
        .append(timeMap.size())
        .append(" elements)\n");
    var i = -1;
    for (var entry : timeMap.entrySet()) {
      ++i;

      var id = entry.getKey();
      long elapsed = entry.getValue().elapsedNs;
      if (elapsed < threshold) {
        continue;
      }

      builder.append(
          String.format(
              "  %3d.  %6d %s  %s\n",
              i,
              reportingUnit.convert(elapsed, TimeUnit.NANOSECONDS),
              StringUtil.formatTimeUnit(reportingUnit),
              id));
    }

    if (!counterMap.isEmpty()) {
      builder.append("\nCounters\n");
      i = -1;
      for (var entry : counterMap.entrySet()) {
        ++i;

        var id = entry.getKey();
        var count = entry.getValue();

        builder.append(String.format("  %3d.  %6d     %s\n", i, count, id));
      }
    }

    return builder.toString();
  }

  private static final class Timer {
    String name;
    long elapsedNs = 0;
    long startTimeNs = -1;

    public Timer(String name) {
      this.name = name;
    }

    public void startAt(long timeNs) {
      if (startTimeNs >= 0) {
        log.warn("Invalid timer state: attempted to start timer {} that was already started", name);
      }
      startTimeNs = timeNs;
    }

    public void stopAt(long timeNs) {
      if (startTimeNs < 0) {
        log.warn("Invalid timer state: attempted to stop timer {} that was not started", name);
        return;
      }
      elapsedNs += (timeNs - startTimeNs);
      startTimeNs = -1;
    }
  }
}
