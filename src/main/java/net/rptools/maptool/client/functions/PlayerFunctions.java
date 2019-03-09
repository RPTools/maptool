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

import java.util.Iterator;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.ObservableList;
import net.rptools.maptool.model.Player;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class PlayerFunctions extends AbstractFunction {
  private static final PlayerFunctions instance = new PlayerFunctions();

  private PlayerFunctions() {
    super(0, 1, "getPlayerName", "getAllPlayerNames");
  }

  public static PlayerFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("getPlayerName")) {
      return MapTool.getPlayer().getName();
    } else {
      ObservableList<Player> players = MapTool.getPlayerList();
      String[] playerArray = new String[players.size()];
      Iterator<Player> iter = players.iterator();

      int i = 0;
      while (iter.hasNext()) {
        playerArray[i] = iter.next().getName();
        i++;
      }
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      if ("json".equals(delim)) {
        return JSONArray.fromObject(playerArray).toString();
      } else {
        return StringFunctions.getInstance().join(playerArray, delim);
      }
    }
  }
}
