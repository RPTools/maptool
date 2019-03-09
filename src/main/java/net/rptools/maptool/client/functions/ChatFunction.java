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

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

/**
 * Chat related functions like broadcast()
 *
 * @author bdornauf
 */
public class ChatFunction extends AbstractFunction {
  /** Ctor */
  public ChatFunction() {
    super(1, 3, "broadcast");
  }

  /** The singleton instance. */
  private static final ChatFunction instance = new ChatFunction();

  /**
   * Gets the Input instance.
   *
   * @return the instance.
   */
  public static ChatFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    MapToolVariableResolver resolver = ((MapToolVariableResolver) parser.getVariableResolver());

    if (functionName.equals("broadcast")) {
      return broadcast(resolver, parameters);
    } else {
      throw new ParserException("Unknown function: " + functionName);
    }
  }

  /**
   * broadcast sends a message to the chat panel of all clients using TextMessage.SAY
   *
   * @return empty string
   */
  private Object broadcast(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    // broadcast shall be trusted
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "broadcast"));
    }

    String message = null;
    String delim = ",";
    JSONArray jarray = null;
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
        if ("json".equals(delim) || temp.charAt(0) == '[') jarray = JSONArray.fromObject(temp);
        else {
          jarray = new JSONArray();
          for (String t : temp.split(delim)) jarray.add(t.trim());
        }
        if (jarray.isEmpty()) return ""; // dont send to empty lists

        // FALLTHRU
      case 1:
        message = checkForCheating(param.get(0).toString());
        if (message != null) {
          if (jarray == null || jarray.isEmpty()) {
            MapTool.addGlobalMessage(message);
          } else {
            @SuppressWarnings("unchecked")
            Collection<String> targets =
                JSONArray.toCollection(jarray, List.class); // Returns an ArrayList<String>
            MapTool.addGlobalMessage(message, (List<String>) targets);
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
