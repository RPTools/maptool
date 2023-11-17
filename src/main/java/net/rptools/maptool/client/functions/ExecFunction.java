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

import com.google.gson.JsonArray;
import java.awt.*;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolExpressionParser;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.functions.json.JsonArrayFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.Function;
import net.rptools.parser.function.ParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExecFunction extends AbstractFunction {

  /** Singleton instance of the ExecFunction class. */
  private static final ExecFunction instance = new ExecFunction();

  private static final Logger log = LogManager.getLogger(ExecFunction.class);

  /** Object used for various operations on {@link JsonArray}s. */
  private JsonArrayFunctions jsonArrayFunctions =
      JSONMacroFunctions.getInstance().getJsonArrayFunctions();

  /**
   * Gets and instance of the ExecFunction class.
   *
   * @return an instance of ExecFunction.
   */
  public static ExecFunction getInstance() {
    return instance;
  }

  private ExecFunction() {
    super(0, UNLIMITED_PARAMETERS, "execFunction");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, args, 2, 5);
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    String execName = args.get(0).toString();
    Function function = parser.getFunction(execName);
    if (function == null) {
      throw new ParameterException(
          I18N.getText("macro.function.execFunction.incorrectName", functionName, execName));
    }

    JsonArray jsonArgs = FunctionUtil.paramAsJsonArray(functionName, args, 1);
    List<Object> execArgs = jsonArrayFunctions.jsonArrayAsMTScriptList(jsonArgs);

    boolean defer =
        args.size() > 2 ? FunctionUtil.paramAsBoolean(functionName, args, 2, true) : false;

    String strTargets = args.size() > 3 ? args.get(3).toString() : "self";
    String delim = args.size() > 4 ? args.get(4).toString() : ",";

    JsonArray jsonTargets;
    if ("json".equals(delim) || strTargets.charAt(0) == '[') {
      jsonTargets = jsonArrayFunctions.parseJsonArray(strTargets);
    } else {
      jsonTargets = new JsonArray();
      for (String t : StringUtil.split(strTargets, delim)) {
        jsonTargets.add(t.trim());
      }
    }
    if (jsonTargets.size() == 0) {
      return ""; // dont send to empty lists
    }

    Collection<String> targets = jsonArrayFunctions.jsonArrayToListOfStrings(jsonTargets);
    sendExecFunction(execName, execArgs, defer, targets);
    return "";
  }

  /**
   * Send the execFunction to targets, either immediately or with a delay
   *
   * @param execName the name of the function to execute
   * @param defer should the execFunction be delayed
   * @param targets the list of targets
   */
  private static void sendExecFunction(
      final String execName, List<Object> execArgs, boolean defer, Collection<String> targets) {
    if (defer) {
      EventQueue.invokeLater(() -> sendExecFunction(execName, execArgs, targets));
    } else {
      sendExecFunction(execName, execArgs, targets);
    }
  }

  /**
   * Send the execFunction. If target is local, run locally instead.
   *
   * @param functionName the name of the function.
   * @param execArgs the list of arguments to the function.
   * @param targets the list of targets.
   */
  private static void sendExecFunction(
      String functionName, List<Object> execArgs, Collection<String> targets) {
    String source = MapTool.getPlayer().getName();

    for (String target : targets) {
      MapTool.serverCommand().execFunction(target, source, functionName, execArgs);
    }
  }

  /**
   * Receive the execFunction, and execute it if need be.
   *
   * @param target the target player.
   * @param source the name of the player who sent the link.
   * @param functionName the name of the function.
   * @param execArgs the arguments of the function.
   */
  public static void receiveExecFunction(
      String target, String source, String functionName, List<Object> execArgs) {
    if (isMessageForMe(target, source)) {
      runExecFunction(functionName, execArgs);
    }
  }

  /**
   * Determines if the message / execLink / execFunction should be ran on the client.
   *
   * @param target the target player.
   * @param source the name of the player who sent the link.
   * @return is the message for the player or not
   */
  public static boolean isMessageForMe(String target, String source) {
    boolean isGM = MapTool.getPlayer().isGM();
    boolean fromSelf = source.equals(MapTool.getPlayer().getName());
    boolean targetSelf = target.equals(MapTool.getPlayer().getName());

    switch (target.toLowerCase()) {
      case "gm":
        return isGM;
      case "self":
        return fromSelf;
      case "gm-self":
        return isGM || fromSelf;
      case "not-self":
        return !fromSelf;
      case "not-gm":
        return !isGM;
      case "not-gm-self":
        return !isGM && !fromSelf;
      case "none":
        return false;
      case "all":
        return true;
      default:
        return targetSelf;
    }
  }

  /**
   * Return true if the message is for other clients, false otherwise.
   *
   * @param target the target of the message
   * @param source the source of the message
   * @return true if the message if for other clients, false otherwise
   */
  public static boolean isMessageGlobal(String target, String source) {
    if (target.equals(source)) return false;
    if (target.equalsIgnoreCase("none")) return false;
    if (target.equalsIgnoreCase("self")) return false;
    return true;
  }

  /**
   * Run the execFunction locally.
   *
   * @param functionName the name of the function
   * @param execArgs the arguments to the function
   */
  private static void runExecFunction(String functionName, List<Object> execArgs) {
    Parser parser = new MapToolExpressionParser().getParser();
    Function function = parser.getFunction(functionName);
    MapTool.getParser().enterTrustedContext(functionName, "execFunction");
    try {
      function.evaluate(parser, new MapToolVariableResolver(null), functionName, execArgs);
    } catch (ParserException pe) {
      log.error("execFunction failed:", pe);
    }
    MapTool.getParser().exitContext();
  }
}
