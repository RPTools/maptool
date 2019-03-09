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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Player;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class IsTrustedFunction extends AbstractFunction {
  private static final IsTrustedFunction instance = new IsTrustedFunction();

  private IsTrustedFunction() {
    super(0, 1, "isTrusted", "isGM", "isExternalMacroAccessAllowed");
  }

  public static IsTrustedFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("isTrusted")) {
      return MapTool.getParser().isMacroTrusted() ? BigDecimal.ONE : BigDecimal.ZERO;
    } else if (functionName.equalsIgnoreCase("isExternalMacroAccessAllowed")) {
      return AppPreferences.getAllowExternalMacroAccess() ? BigDecimal.ONE : BigDecimal.ZERO;
    } else {
      // functionName is isGM
      if (parameters.isEmpty())
        return MapTool.getPlayer().isGM() ? BigDecimal.ONE : BigDecimal.ZERO;
      else {

        return getGMs().contains(parameters.get(0)) ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    }
  }

  /**
   * retrieves a list of GMs
   *
   * @return copied from MacroLinkFunctions since its private there
   */
  private List<String> getGMs() {
    List<String> gms = new ArrayList<String>();

    Iterator<Player> pliter = MapTool.getPlayerList().iterator();
    while (pliter.hasNext()) {
      Player plr = pliter.next();
      if (plr.isGM()) {
        gms.add(plr.getName());
      }
    }
    return gms;
  }
}
