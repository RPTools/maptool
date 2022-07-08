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
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Class that implements Data macro functions. */
public class ServerFunctions extends AbstractFunction {

  /** Creates a new {@code PlayerFunctions} object. */
  public ServerFunctions() {
    super(0, 0, "server.isServer", "server.isHosting", "server.isPersonal");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    String fName = functionName.toLowerCase();
    return switch (fName) {
      case "server.isserver" -> MapTool.isHostingServer() || MapTool.isPersonalServer()
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
      case "server.ishosting" -> MapTool.isHostingServer() ? BigDecimal.ONE : BigDecimal.ZERO;
      case "server.ispersonal" -> MapTool.isPersonalServer() ? BigDecimal.ONE : BigDecimal.ZERO;
      default -> throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    };
  }
}
