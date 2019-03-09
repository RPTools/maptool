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
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class MacroDialogFunctions extends AbstractFunction {
  private static final MacroDialogFunctions instance = new MacroDialogFunctions();

  private MacroDialogFunctions() {
    super(1, 1, "isDialogVisible", "isFrameVisible", "closeDialog", "resetFrame", "closeFrame");
  }

  public static MacroDialogFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("isDialogVisible")) {
      return HTMLFrameFactory.isVisible(false, parameters.get(0).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equals("isFrameVisible")) {
      return HTMLFrameFactory.isVisible(true, parameters.get(0).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equals("closeDialog")) {
      HTMLFrameFactory.close(false, parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("closeFrame")) {
      HTMLFrameFactory.close(true, parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("resetFrame")) {
      HTMLFrame.center(parameters.get(0).toString());
      return "";
    }
    return null;
  }
}
