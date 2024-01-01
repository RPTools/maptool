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
import com.google.gson.JsonParser;
import java.util.List;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Chat related functions like broadcast()
 *
 * @author bdornauf
 */
public class ChatFunction extends AbstractFunction {
  private static final Logger log = LogManager.getLogger(ChatFunction.class);

  /** Ctor */
  public ChatFunction() {
    super(1, 3, "broadcast", "chat");
  }

  /** The singleton instance. */
  private static final ChatFunction instance = new ChatFunction();

  /**
   * Gets the instance.
   *
   * @return the instance.
   */
  public static ChatFunction getInstance() {
    return instance;
  }

  public static final List<String> chatBlackList =
      List.of(
          "alias",
          "cc",
          "color",
          "clearaliases",
          "loadaliases",
          "savealiases",
          "exp",
          "exper",
          "experiments",
          "loadtokenstates",
          "tsl",
          "savetokenstates",
          "tss");

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    if (functionName.equalsIgnoreCase("broadcast")) {
      return broadcast(resolver, parameters);
    } else if (functionName.equalsIgnoreCase("chat")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      return chat(parameters);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
  }

  /**
   * Function to expose "/" chat commands to use by macro.
   *
   * @param parameters command and arguments
   * @return true on success
   */
  private Object chat(List<Object> parameters) throws ParserException {
    String command = FunctionUtil.paramAsString("chat", parameters, 0, false).trim();
    String content =
        parameters.size() == 2
            ? FunctionUtil.paramAsString("chat", parameters, 1, false).trim()
            : "";
    log.info("Received " + parameters.size() + " parameters.");
    if (command.contains(" ")) {
      int index = command.indexOf(" ");
      log.info("Space found at index " + index);
      if (parameters.size() == 1) {
        content = command.substring(index);
        command = command.substring(0, index);
        log.info("command: " + command + "; content: " + content);
      } else {
        content = FunctionUtil.paramAsString("chat", parameters, 1, false);
        content = command.substring(index) + " " + content;
        command = command.substring(0, index);
        log.info("command: " + command + "; content: " + content);
      }
    }
    if (chatBlackList.contains(command.startsWith("/") ? command.substring(1) : command))
      throw new ParserException(I18N.getText("macro.function.general.noPerm", command));
    if (!command.startsWith("/")) command = "/" + command;
    if (!content.isEmpty()) {
      command += " " + content;
    }
    MacroManager.executeMacro(command);
    return "";
    // [chat("me does something")]<br>[chat("me ","does something")]
  }

  /**
   * broadcast sends a message to the chat panel of all clients using TextMessage.SAY
   *
   * @return empty string
   */
  private Object broadcast(VariableResolver resolver, List<Object> param) throws ParserException {
    // broadcast shall be trusted
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "broadcast"));
    }

    String message = null;
    String delim = ",";
    JsonArray jarray = null;
    switch (param.size()) {
      default:
        throw new ParserException(
            I18N.getText("macro.function.general.tooManyParam", "broadcast", 3, param.size()));
      case 0:
        throw new ParserException(
            I18N.getText("macro.function.general.notEnoughParam", "broadcast", 1, 0));
      case 3:
        delim = param.get(2).toString();
        // FALLTHRU
      case 2:
        String temp = param.get(1).toString().trim();
        if ("json".equals(delim) || temp.charAt(0) == '[')
          jarray = JsonParser.parseString(temp).getAsJsonArray();
        else {
          jarray = new JsonArray();
          for (String t : StringUtil.split(temp, delim)) {
            jarray.add(t.trim());
          }
        }
        if (jarray.size() == 0) {
          return ""; // dont send to empty lists
        }

        // FALLTHRU
      case 1:
        message = checkForCheating(param.get(0).toString());
        if (message != null) {
          if (jarray == null || jarray.size() == 0) {
            MapTool.addGlobalMessage(message);
          } else {
            @SuppressWarnings("unchecked")
            List<String> targets =
                JSONMacroFunctions.getInstance()
                    .getJsonArrayFunctions()
                    .jsonArrayToListOfStrings(jarray);
            MapTool.addGlobalMessage(message, targets);
          }
        }
        return "";
    }
  }

  /**
   * check if a message contains characters flagged as cheating and delete the message if found. As
   * well
   *
   * @param message
   * @return message
   */
  private String checkForCheating(String message) {
    // Detect whether the person is attempting to fake rolls.
    Pattern cheater_pattern = CommandPanel.CHEATER_PATTERN;

    if (cheater_pattern.matcher(message).find()) {
      MapTool.addServerMessage(TextMessage.me(null, "Cheater.  You have been reported."));
      MapTool.serverCommand()
          .message(
              TextMessage.gm(
                  null, MapTool.getPlayer().getName() + " was caught <i>cheating</i>: " + message));
      message = null;
    }
    return message;
  }
}
