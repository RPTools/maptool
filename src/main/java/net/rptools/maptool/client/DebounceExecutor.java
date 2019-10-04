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

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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

  /** The time, in milliseconds, during which to throttle subsequent requests to run the task. */
  private final long delay;

  /**
   * A {@link ScheduledExecutorService} that will be used to run the debounced task when the delay
   * elapses.
   */
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(threadFactory);

  /** A {@link ScheduledFuture} that represents the debounced task. */
  private ScheduledFuture<?> future;

  /**
   * The synchronization lock used during the critical section for determining how to dispose of any
   * single request.
   */
  private final Object syncLock = new Object();

  /** A {@link Runnable} representing the task to be managed. */
  private final Runnable task;

  /**
   * A {@link long} indicating the time, in milliseconds, when the task last entered a pending
   * state.
   */
  private long taskPendingSince = -1;

  /** A reference to the logging service. */
  // private static final Logger log = LogManager.getLogger(DebounceExecutor.class);

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
  public DebounceExecutor(long delay, Runnable task) {
    this.delay = delay;
    this.task = task;
  }

  /** Dispatches a task to be executed by this {@link DebounceExecutor} instance. */
  public void dispatch() {
    if (this.task == null) {
      // log.info("Exited debouncer because of a null task.");
      return;
    }
    if (this.delay < 1) {
      this.task.run();
      return;
    }
    synchronized (syncLock) {
      long now = (new Date()).getTime();
      if (this.taskPendingSince == -1 || now - this.taskPendingSince >= this.delay) {
        this.taskPendingSince = now;
        this.future = this.executor.schedule(this.task, this.delay, TimeUnit.MILLISECONDS);
      } /* else {
          log.info(
                String.format(
                      "Task execution was debounced. (now: %d; taskPendingSince: %d; delay: %d; now - taskPendingSince: %d)",
                      now, this.taskPendingSince, this.delay, now - this.taskPendingSince));
        } */
    }
  }
}
