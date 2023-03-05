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
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class PlayerNameFunctions extends AbstractFunction {
  private static final PlayerNameFunctions instance = new PlayerNameFunctions();

  private PlayerNameFunctions() {
    super(0, 1, "getPlayerName", "getAllPlayerNames");
  }

  public static PlayerNameFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("getPlayerName")) {
      return MapTool.getPlayer().getName();
    } else if ("getAllPlayerNames".equalsIgnoreCase(functionName)) {
      List<Player> players = MapTool.getPlayerList();
      String[] playerArray = new String[players.size()];
      Iterator<Player> iter = players.iterator();

      int i = 0;
      while (iter.hasNext()) {
        playerArray[i] = iter.next().getName();
        i++;
      }
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      if ("json".equals(delim)) {
        JsonArray jarr = new JsonArray();
        Arrays.stream(playerArray).forEach(p -> jarr.add(new JsonPrimitive(p)));
        return jarr;
      } else {
        return StringFunctions.getInstance().join(playerArray, delim);
      }
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }
}
