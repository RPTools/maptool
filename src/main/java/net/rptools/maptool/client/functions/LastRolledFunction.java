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
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class LastRolledFunction extends AbstractFunction {
  private static final LastRolledFunction instance = new LastRolledFunction();

  private LastRolledFunction() {
    super(0, 1, "lastRolled", "getRolled", "clearRolls", "getNewRolls");
  }

  public static LastRolledFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    JsonArray jarr = new JsonArray();
    if (functionName.equalsIgnoreCase("lastRolled")) {
      MapTool.getParser().getLastRolled().forEach(r -> jarr.add(new JsonPrimitive(r)));
    } else if (functionName.equalsIgnoreCase("getRolled")) {
      MapTool.getParser().getRolled().forEach(r -> jarr.add(new JsonPrimitive(r)));
    } else if (functionName.equalsIgnoreCase("clearRolls")) {
      MapTool.getParser().clearRolls();
    } else {
      MapTool.getParser().getNewRolls().forEach(r -> jarr.add(new JsonPrimitive(r)));
    }

    return jarr;
  }
}
