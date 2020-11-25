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
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class HasImpersonated extends AbstractFunction {
  private static final HasImpersonated instance = new HasImpersonated();

  private HasImpersonated() {
    super(0, 1, "hasImpersonated", "impersonate");
  }

  public static HasImpersonated getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    int psize = parameters.size();
    if (functionName.equalsIgnoreCase("hasImpersonated")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      boolean global =
          psize > 0 ? FunctionUtil.paramAsBoolean(functionName, parameters, 0, false) : false;
      return hasImpersonated(global) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equalsIgnoreCase("impersonate")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      if (psize > 0 && parameters.get(0).toString().equals("")) {
        // Stop impersonating
        MapTool.getFrame().getCommandPanel().clearGlobalIdentity();
        return "";
      } else {
        Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, -1);
        MapTool.getFrame().getCommandPanel().setGlobalIdentity(token);
        return token.getName();
      }
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Returns the current impersonation status. The impersonation can be either global (impersonated
   * panel) or specific to the macro context.
   *
   * @param global whether the global impersonation should be queried
   * @return true if there is a token impersonated, false if not
   */
  private boolean hasImpersonated(boolean global) {
    if (global) {
      // Global impersonation, aka the one from the Impersonate Panel
      return MapTool.getFrame().getCommandPanel().isGlobalImpersonatingToken();
    } else {
      return MapTool.getFrame().getCommandPanel().isImpersonatingToken();
    }
  }
}
