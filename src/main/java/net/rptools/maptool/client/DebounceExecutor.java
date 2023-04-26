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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

/**
 * Manages throttling rapid successive attempts to execute a task.
 *
 * @see <a href="https://stackoverflow.com/a/18758408">Cancelling method calls when the same method
 *     is called multiple time</a>
 * @see <a href="https://github.com/ThomasGirard/JDebounce">JDebounce</a>
 * @author Philip Nichols (philip.j.nichols@gmail.com)
 * @version 1.5.3
 * @since 2019-08-23
 */
public class DebounceExecutor {

  /**
   * Thread factory for creating named threads. Other than thread naming, this factory relies on
   * defaultThreadFactory and should not add overhead.
   */
  private static final ThreadFactory threadFactory =
      (new com.google.common.util.concurrent.ThreadFactoryBuilder())
          .setNameFormat("debounce-executor-%d")
          .build();

  /**
   * A {@link ScheduledExecutorService} that will be used to run the debounced task when the delay
   * elapses.
   */
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(threadFactory);

  /** The time, in milliseconds, during which to throttle subsequent requests to run the task. */
  private final long delay;

  /** A {@link Runnable} representing the task to be managed. */
  private final Runnable task;

  /** A {@link long} indicating the time, in milliseconds, when the task was last scheduled for. */
  private final AtomicLong taskScheduledTime = new AtomicLong(-1);

  /**
   * Initializes a new instance of the {@link DebounceExecutor} class.
   *
   * <p>Note that this class swallows later attempted invocations of its managed task. This makes it
   * appropriate for repaint()-like tasks, where even an early-scheduled task will be working with
   * the current backbuffer once it finally executes. <i>It is not appropriate for tasks whose
   * correct execution relies on swallowing early invocations and running the last-posted one.</i>
   *
   * @param delay The time, in milliseconds, during which the DebounceExecutor will wait before
   *     executing the <i>task</i> and throttle subsequent requests.
   * @param task The task to be executed after the <i>delay</i> elapses.
   */
  public DebounceExecutor(long delay, @Nonnull Runnable task) {
    this.delay = delay;
    this.task = task;
  }

  /** Dispatches a task to be executed by this {@link DebounceExecutor} instance. */
  public void dispatch() {
    if (this.delay < 1) {
      this.task.run();
      return;
    }

    /*
     * There are three time windows we need to account for.
     * 1. The scheduled time has not yet passed, so we consider the execution redundant.
     * 2. The scheduled time has passed, but not by much. So we can run the task with a small delay.
     * 3. The scheduled time has long passed, so we can run the task right away.
     */

    final var taskScheduledTime = this.taskScheduledTime.get();
    final var now = System.currentTimeMillis();
    if (now >= taskScheduledTime) {
      // This request is not redundant, so we need to schedule it.
      final var nextTargetTime = Math.max(now, taskScheduledTime + this.delay);
      // If this check fails, that means someone beat us to the punch and our task is now redundant.
      if (this.taskScheduledTime.compareAndSet(taskScheduledTime, nextTargetTime)) {
        this.executor.schedule(this.task, nextTargetTime - now, TimeUnit.MILLISECONDS);
      }
    }
  }
}
