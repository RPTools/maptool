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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.List;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.macro.MacroManager.Scope;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Class that implements slash command alias functions. */
public class SlashCommands extends AbstractFunction {

  /** Creates a new {@code SlashCommands} object. */
  public SlashCommands() {
    super(0, 3, "slash.setAlias", "slash.getAliases", "slash.clearAlias");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    String fName = functionName.toLowerCase();
    return switch (fName) {
      case "slash.setalias" -> setSlashAlias(parameters);
      case "slash.getaliases" -> getSlashAliases();
      case "slash.clearalias" -> clearSlashAlias(parameters);
      default ->
          throw new ParserException(
              I18N.getText("macro.function.general.unknownFunction", functionName));
    };
  }

  /**
   * Sets a slash command alias.
   *
   * @param args The arguments to the function.
   * @return empty string
   * @throws ParserException if the number of arguments is incorrect.
   */
  private Object setSlashAlias(List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("slash.setAlias", args, 2, 3);
    String desc;
    if (args.size() == 3) {
      desc = args.get(2).toString();
    } else {
      desc = "";
    }
    MacroManager.setAlias(args.get(0).toString(), args.get(1).toString(), Scope.CAMPAIGN, desc);
    return "";
  }

  /**
   * Gets the slash command aliases.
   *
   * @return a JSON string containing the aliases.
   */
  private JsonElement getSlashAliases() {
    Gson gson = new Gson();
    return gson.toJsonTree(MacroManager.getAliasCommandMap());
  }

  /**
   * Clears a slash command alias.
   *
   * @param args The arguments to the function.
   * @return empty string
   * @throws ParserException if the number of arguments is incorrect.
   */
  private Object clearSlashAlias(List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("server.clearAlias", args, 1, 1);
    MacroManager.removeAlias(args.get(0).toString(), Scope.CAMPAIGN);
    return "";
  }
}
