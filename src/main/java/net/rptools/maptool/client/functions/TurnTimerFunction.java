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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import java.awt.EventQueue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Macro functions for arbitrary-length, non-blocking turn timers. Scheduling is backed by a single
 * dedicated thread; expiry callbacks are bounced onto the EDT and dispatched via the macro parser
 * so they execute in the same context as any other event-driven macro (e.g. {@code
 * onInitiativeChange}).
 *
 * <p>Timers live in static memory only. They are not persisted with the campaign and do not survive
 * a restart or {@code setCampaign} call.
 */
public class TurnTimerFunction extends AbstractFunction {

  private static final Logger LOGGER = LogManager.getLogger(TurnTimerFunction.class);

  /** Reserved timer name used by the initiative auto-restart template. */
  public static final String INITIATIVE_TIMER_NAME = "__initiative__";

  private static final ThreadFactory THREAD_FACTORY =
      new ThreadFactoryBuilder().setNameFormat("turn-timer-%d").setDaemon(true).build();

  private static final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);

  private static final ConcurrentHashMap<String, TimerEntry> timers = new ConcurrentHashMap<>();

  private static volatile InitiativeTemplate initiativeTemplate;

  private TurnTimerFunction() {
    super(
        0,
        4,
        "startTimer",
        "stopTimer",
        "getTimerRemaining",
        "getTimerStatus",
        "getTimerNames",
        "setInitiativeTimer",
        "clearInitiativeTimer");
  }

  private static final TurnTimerFunction instance = new TurnTimerFunction();

  public static TurnTimerFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    return switch (functionName.toLowerCase()) {
      case "starttimer" -> evalStart(args);
      case "stoptimer" -> evalStop(args);
      case "gettimerremaining" -> evalRemaining(args);
      case "gettimerstatus" -> evalStatus(args);
      case "gettimernames" -> evalNames(args);
      case "setinitiativetimer" -> evalSetInitiative(args);
      case "clearinitiativetimer" -> evalClearInitiative(args);
      default ->
          throw new ParserException(
              I18N.getText("macro.function.general.unknownFunction", functionName));
    };
  }

  private Object evalStart(List<Object> args) throws ParserException {
    FunctionUtil.blockUntrustedMacro("startTimer");
    FunctionUtil.checkNumberParam("startTimer", args, 2, 4);
    String name = args.get(0).toString();
    double seconds = parseSeconds("startTimer", args.get(1));
    String callback = args.size() > 2 ? args.get(2).toString() : "";
    String callbackArgs = args.size() > 3 ? args.get(3).toString() : "";
    start(name, seconds, callback, callbackArgs);
    return BigDecimal.ONE;
  }

  private Object evalStop(List<Object> args) throws ParserException {
    FunctionUtil.blockUntrustedMacro("stopTimer");
    FunctionUtil.checkNumberParam("stopTimer", args, 1, 1);
    return stop(args.get(0).toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private Object evalRemaining(List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("getTimerRemaining", args, 1, 1);
    return BigDecimal.valueOf(remainingSeconds(args.get(0).toString()))
        .setScale(3, RoundingMode.HALF_UP);
  }

  private Object evalStatus(List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("getTimerStatus", args, 1, 1);
    return timers.containsKey(args.get(0).toString()) ? "running" : "none";
  }

  private Object evalNames(List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("getTimerNames", args, 0, 0);
    JsonArray arr = new JsonArray();
    timers.keySet().forEach(arr::add);
    return arr;
  }

  private Object evalSetInitiative(List<Object> args) throws ParserException {
    FunctionUtil.blockUntrustedMacro("setInitiativeTimer");
    FunctionUtil.checkNumberParam("setInitiativeTimer", args, 1, 3);
    double seconds = parseSeconds("setInitiativeTimer", args.get(0));
    if (seconds <= 0) {
      clearInitiativeTimer();
      return BigDecimal.ONE;
    }
    String callback = args.size() > 1 ? args.get(1).toString() : "";
    String callbackArgs = args.size() > 2 ? args.get(2).toString() : "";
    initiativeTemplate =
        new InitiativeTemplate(Math.round(seconds * 1000.0), callback, callbackArgs);
    start(INITIATIVE_TIMER_NAME, seconds, callback, callbackArgs);
    return BigDecimal.ONE;
  }

  private Object evalClearInitiative(List<Object> args) throws ParserException {
    FunctionUtil.blockUntrustedMacro("clearInitiativeTimer");
    FunctionUtil.checkNumberParam("clearInitiativeTimer", args, 0, 0);
    clearInitiativeTimer();
    return BigDecimal.ONE;
  }

  private static double parseSeconds(String functionName, Object raw) throws ParserException {
    double seconds;
    if (raw instanceof Number n) {
      seconds = n.doubleValue();
    } else {
      try {
        seconds = Double.parseDouble(raw.toString());
      } catch (NumberFormatException e) {
        throw new ParserException(
            I18N.getText("macro.function.turnTimer.invalidDuration", functionName));
      }
    }
    if (Double.isNaN(seconds) || Double.isInfinite(seconds)) {
      throw new ParserException(
          I18N.getText("macro.function.turnTimer.invalidDuration", functionName));
    }
    return seconds;
  }

  /* -------- core scheduling (also reachable from tests) -------- */

  static void start(String name, double seconds, String callback, String callbackArgs)
      throws ParserException {
    if (seconds <= 0) {
      throw new ParserException(
          I18N.getText("macro.function.turnTimer.invalidDuration", "startTimer"));
    }
    long delayMs = Math.round(seconds * 1000.0);
    long expiresAt = System.currentTimeMillis() + delayMs;
    ScheduledFuture<?> future =
        scheduler.schedule(() -> fire(name), delayMs, TimeUnit.MILLISECONDS);
    TimerEntry prior = timers.put(name, new TimerEntry(future, expiresAt, callback, callbackArgs));
    if (prior != null) {
      prior.future().cancel(false);
    }
  }

  static boolean stop(String name) {
    TimerEntry removed = timers.remove(name);
    if (removed == null) {
      return false;
    }
    removed.future().cancel(false);
    return true;
  }

  static double remainingSeconds(String name) {
    TimerEntry entry = timers.get(name);
    if (entry == null) {
      return 0.0;
    }
    long remaining = entry.expiresAtMillis() - System.currentTimeMillis();
    return Math.max(0L, remaining) / 1000.0;
  }

  /**
   * Called from {@code InitiativeList.handleInitiativeChangeCommitMacroEvent} after a successful
   * initiative pass. Restarts the configured initiative timer, if one is set.
   */
  public static void onInitiativeChanged() {
    InitiativeTemplate tpl = initiativeTemplate;
    if (tpl == null) {
      return;
    }
    long expiresAt = System.currentTimeMillis() + tpl.millis();
    ScheduledFuture<?> future =
        scheduler.schedule(() -> fire(INITIATIVE_TIMER_NAME), tpl.millis(), TimeUnit.MILLISECONDS);
    TimerEntry prior =
        timers.put(
            INITIATIVE_TIMER_NAME, new TimerEntry(future, expiresAt, tpl.callback(), tpl.args()));
    if (prior != null) {
      prior.future().cancel(false);
    }
  }

  /** Cancels every active timer and drops any initiative-timer template. */
  public static void cancelAll() {
    initiativeTemplate = null;
    timers.values().forEach(entry -> entry.future().cancel(false));
    timers.clear();
  }

  private static void clearInitiativeTimer() {
    initiativeTemplate = null;
    stop(INITIATIVE_TIMER_NAME);
  }

  private static void fire(String name) {
    TimerEntry entry = timers.remove(name);
    if (entry == null || entry.callback().isEmpty()) {
      return;
    }
    EventQueue.invokeLater(
        () -> {
          try {
            MapTool.getParser()
                .runMacro(
                    new MapToolVariableResolver(null), null, entry.callback(), entry.args(), false);
          } catch (ParserException e) {
            LOGGER.warn("Turn timer '{}' callback failed: {}", name, e.getMessage(), e);
            MapTool.addLocalMessage(
                I18N.getText("macro.function.turnTimer.callbackFailed", name, e.getMessage()));
          }
        });
  }

  private record TimerEntry(
      ScheduledFuture<?> future, long expiresAtMillis, String callback, String args) {}

  private record InitiativeTemplate(long millis, String callback, String args) {}
}
