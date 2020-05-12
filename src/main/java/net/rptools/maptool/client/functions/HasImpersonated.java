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
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class HasImpersonated extends AbstractFunction {
  private static final HasImpersonated instance = new HasImpersonated();

  private HasImpersonated() {
    super(0, 1, "hasImpersonated");
  }

  public static HasImpersonated getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    int psize = parameters.size();
    boolean global =
        psize > 0 ? FunctionUtil.paramAsBoolean(functionName, parameters, 0, false) : false;
    Token t = null;
    if (global) {
      // Global impersonation, aka the one from the Impersonate Panel
      GUID guid = MapTool.getFrame().getImpersonatePanel().getTokenId();
      if (guid != null) {
        // Searches all maps to find impersonated token
        t = FindTokenFunctions.getInstance().findToken(guid.toString());
      }
    } else {
      // Impersonation specific to the macro context
      Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
      if (guid != null) t = zone.getToken(guid);
      else t = zone.resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());
    }
    return t == null ? BigDecimal.ZERO : BigDecimal.ONE;
  }
}
