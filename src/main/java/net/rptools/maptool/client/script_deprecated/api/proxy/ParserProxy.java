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
package net.rptools.maptool.client.script_deprecated.api.proxy;

import java.math.BigDecimal;
import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

public class ParserProxy {
  private final Parser parser;

  public ParserProxy(Parser parser) {
    this.parser = parser;
  }

  public void setVariable(String name, Object value) throws ParserException {
    if (value instanceof Number)
      parser.setVariable(name, new BigDecimal(((Number) value).doubleValue()));
    else parser.setVariable(name, value);
  }

  public Object getVariable(String variableName) throws ParserException {
    return parser.getVariable(variableName);
  }

  public Object evaluate(String expression) throws ParserException {
    Expression xp = parser.parseExpression(expression);
    return xp.evaluate();
  }
}
