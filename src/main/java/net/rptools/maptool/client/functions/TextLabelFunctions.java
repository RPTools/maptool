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
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** */
public class TextLabelFunctions extends AbstractFunction {
  public TextLabelFunctions() {
    super(0, 0, false, "showTextLabels", "hideTextLabels", "getTextLabelStatus");
  }

  /** The singleton instance. */
  private static final TextLabelFunctions instance = new TextLabelFunctions();

  /**
   * Gets the instance.
   *
   * @return the singleton instance.
   */
  public static TextLabelFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    switch (functionName) {
      case "showTextLabels":
        AppState.setShowTextLabels(true);
        MapTool.getFrame().getCurrentZoneRenderer().repaint();
        return "";
      case "hideTextLabels":
        AppState.setShowTextLabels(false);
        MapTool.getFrame().getCurrentZoneRenderer().repaint();
        return "";
      case "getTextLabelStatus":
        return AppState.getShowTextLabels() ? "show" : "hide";
      default:
        throw new ParserException(
            I18N.getText("macro.function.general.unknownFunction", functionName));
    }
  }
}
