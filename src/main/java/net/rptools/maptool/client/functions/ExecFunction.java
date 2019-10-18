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

import java.awt.*;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.Function;
import net.rptools.parser.function.ParameterException;
import net.sf.json.JSONArray;

public class ExecFunction extends AbstractFunction {

  /** Singleton instance of the ExecFunction class. */
  private static final ExecFunction instance = new ExecFunction();

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
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    FunctionUtil.checkNumberParam(functionName, args, 1, 5);
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    String execName = args.get(0).toString();
    Function function = parser.getFunction(execName);
    if (function == null) {
      throw new ParameterException(
          I18N.getText("macro.function.execFunction.incorrectName", functionName, execName));
    }

    JSONArray jsonArgs = FunctionUtil.paramAsJsonArray(functionName, args, 1);
    @SuppressWarnings("unchecked")
    ArrayList execArgs = new ArrayList(jsonArgs);

    boolean defer =
        args.size() > 2 ? FunctionUtil.paramAsBoolean(functionName, args, 2, true) : false;

    String strTargets = args.size() > 3 ? args.get(3).toString() : "self";
    String delim = args.size() > 4 ? args.get(4).toString() : ",";

    JSONArray jsonTargets;
    if ("json".equals(delim) || strTargets.charAt(0) == '[') {
      jsonTargets = JSONArray.fromObject(strTargets);
    } else {
      jsonTargets = new JSONArray();
      for (String t : strTargets.split(delim)) jsonTargets.add(t.trim());
    }
    if (jsonTargets.isEmpty()) {
      return ""; // dont send to empty lists
    }

    @SuppressWarnings("unchecked")
    // Returns an ArrayList<String>
    Collection<String> targets = JSONArray.toCollection(jsonTargets, List.class);

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
      final String execName, List<Object> execArgs, boolean defer, Collection<String> targets)
      throws ParserException {
    if (defer) {
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              sendExecFunction(execName, execArgs, targets);
            }
          });
    } else {
      sendExecFunction(execName, execArgs, targets);
    }
  }

  /**
   * Send the execFunction. If target is local, run locally instead.
   *
   * @param execName the name of the function.
   * @param execArgs the list of arguments to the function.
   * @param targets the list of targets.
   */
  private static void sendExecFunction(
      final String execName, List<Object> execArgs, Collection<String> targets) {
    String functionText = getExecFunctionText(execName, execArgs);
    boolean isGM = MapTool.getPlayer().isGM();
    String selfName = MapTool.getPlayer().getName();

    for (String target : targets) {
      if (target.equals(selfName)) {
        target = "self";
      }
      switch (target.toLowerCase()) {
        case "gm-self":
          MapTool.serverCommand().execFunction(functionText, "gm");
          if (isGM) break; // FALLTHRU if not a GM
        case "self":
          runExecFunction(functionText);
          break;
        case "none":
          break;
        default:
          MapTool.serverCommand().execFunction(functionText, target);
          break;
      }
    }
  }

  /**
   * Receive the execFunction, and execute it if need be.
   *
   * @param functionText the text of the function call.
   * @param target the target player.
   */
  public static void receiveExecFunction(final String functionText, String target) {
    boolean isGM = MapTool.getPlayer().isGM();
    String selfName = MapTool.getPlayer().getName();

    switch (target.toLowerCase()) {
      case "gm":
        if (isGM) {
          runExecFunction(functionText);
        }
        break;
      case "all":
        runExecFunction(functionText);
        break;
      default:
        if (target.equals(selfName)) {
          runExecFunction(functionText);
        }
        break;
    }
  }

  /**
   * Get a String corresponding to the function call from the function name and list of arguments.
   * The text is then intended to be run through runMacroBlock. This is a workaround as it is not
   * currently possible to run a macro directly as doing so would require a reference to the parser,
   * which is not available to us.
   *
   * @param execName the name of the function.
   * @param execArgs a list of arguments to the function.
   * @return the string of the function call.
   */
  private static String getExecFunctionText(final String execName, List<Object> execArgs) {
    StringBuilder functionText = new StringBuilder("[h:" + execName + "(");

    for (int i = 0; i < execArgs.size(); i++) {
      if (execArgs.get(i) instanceof String) {
        functionText.append('"').append(execArgs.get(i)).append('"');
      } else {
        functionText.append(execArgs.get(i).toString());
      }

      if (i < (execArgs.size() - 1)) {
        functionText.append(",");
      } else {
        functionText.append(")]");
      }
    }
    return functionText.toString();
  }

  private static void runExecFunction(final String functionText) {
    try {
      MapTool.getParser().runMacroBlock(null, functionText, "execFunction", "remote", true);
    } catch (ParserException ignored) {
    }
  }
}
