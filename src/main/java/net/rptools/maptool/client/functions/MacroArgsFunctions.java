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
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class MacroArgsFunctions extends AbstractFunction {
  private static final MacroArgsFunctions instance = new MacroArgsFunctions();

  private MacroArgsFunctions() {
    super(0, 1, "arg", "argCount");
  }

  public static MacroArgsFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    Object numArgs = parser.getVariable("macro.args.num");
    int argCount = 0;

    if (numArgs instanceof BigDecimal) {
      argCount = ((BigDecimal) numArgs).intValue();
    }

    if (functionName.equals("argCount")) {
      return BigDecimal.valueOf(argCount);
    }

    if (parameters.size() != 1 || !(parameters.get(0) instanceof BigDecimal)) {
      throw new ParserException(I18N.getText("macro.function.args.incorrectParam", "arg"));
    }

    int argNo = ((BigDecimal) parameters.get(0)).intValue();

    if (argCount == 0 && argNo == 0) {
      return parser.getVariable("macro.args");
    }

    if (argNo < 0 || argNo >= argCount) {
      throw new ParserException(
          I18N.getText("macro.function.args.outOfRange", "arg", argNo, argCount - 1));
    }

    return parser.getVariable("macro.args." + argNo);
  }
}
