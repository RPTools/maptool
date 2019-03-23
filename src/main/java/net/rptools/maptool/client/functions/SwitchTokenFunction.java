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

import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class SwitchTokenFunction extends AbstractFunction {
  private static final SwitchTokenFunction instance = new SwitchTokenFunction();

  private SwitchTokenFunction() {
    super(1, 1, "switchToken");
  }

  public static SwitchTokenFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
    if (parameters.size() < 1) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
    }
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    Token token = zone.resolveToken(parameters.get(0).toString());
    if (token == null) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
    }
    ((MapToolVariableResolver) parser.getVariableResolver()).setTokenIncontext(token);
    return "";
  }
}
