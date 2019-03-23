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
import net.rptools.parser.function.AbstractFunction;

public class DefineMacroFunction extends AbstractFunction {
  private static final DefineMacroFunction instance = new DefineMacroFunction();

  private DefineMacroFunction() {
    super(0, UNLIMITED_PARAMETERS, "defineFunction", "isFunctionDefined", "oldFunction");
  }

  public static DefineMacroFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("defineFunction")) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }

      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }

      String macro = parameters.get(1).toString();
      if (macro.toLowerCase().endsWith("@this")) {
        macro = macro.substring(0, macro.length() - 4) + MapTool.getParser().getMacroSource();
      }

      boolean ignoreOutput = false;
      if (parameters.size() > 2) {
        if (!(parameters.get(2) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  3,
                  parameters.get(2).toString()));
        }
        ignoreOutput = !BigDecimal.ZERO.equals(parameters.get(2));
      }
      boolean newVariableContext = true;
      if (parameters.size() > 3) {
        if (!(parameters.get(3) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  4,
                  parameters.get(3).toString()));
        }
        newVariableContext = !BigDecimal.ZERO.equals(parameters.get(3));
      }

      UserDefinedMacroFunctions.getInstance()
          .defineFunction(
              parser, parameters.get(0).toString(), macro, ignoreOutput, newVariableContext);
      return I18N.getText(
          "macro.function.defineFunction.functionDefined", parameters.get(0).toString());
    } else if (functionName.equals("oldFunction")) {
      return UserDefinedMacroFunctions.getInstance().executeOldFunction(parser, parameters);
    } else { // isFunctionDefined

      if (UserDefinedMacroFunctions.getInstance().isFunctionDefined(parameters.get(0).toString())) {
        return BigDecimal.ONE;
      }

      if (parser.getFunction(parameters.get(0).toString()) != null) {
        return BigDecimal.valueOf(2);
      }

      return BigDecimal.ZERO;
    }
  }
}
