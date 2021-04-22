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
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class BarImageFunction extends AbstractFunction {

  /** The singleton instance. */
  private static final BarImageFunction instance = new BarImageFunction();

  private BarImageFunction() {
    super(1, 3, "getBarImage");
  }

  /**
   * Gets the BarImage instance.
   *
   * @return the instance.
   */
  public static BarImageFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    String barName;
    BigDecimal size = null;
    BigDecimal value = null;

    barName = args.get(0).toString();
    if (args.size() > 1 && args.get(1) instanceof BigDecimal) {
      size = (BigDecimal) args.get(1);
    }
    if (args.size() > 2 && args.get(2) instanceof BigDecimal) {
      value = (BigDecimal) args.get(2);
    }
    BarTokenOverlay over = MapTool.getCampaign().getTokenBarsMap().get(barName);
    if (over == null) {
      throw new ParserException(
          I18N.getText("macro.function.barImage.unknownBar", "getBarImage()", barName));
    }

    StringBuilder assetId = new StringBuilder("asset://");

    if (over instanceof SingleImageBarTokenOverlay) {
      assetId.append(((SingleImageBarTokenOverlay) over).getAssetId().toString());

    } else if (over instanceof TwoImageBarTokenOverlay) {
      if (value != null) {
        if (value.doubleValue() > 0.5) {
          assetId.append(((TwoImageBarTokenOverlay) over).getTopAssetId().toString());
        } else {
          assetId.append(((TwoImageBarTokenOverlay) over).getBottomAssetId().toString());
        }
      } else {
        assetId.append(((TwoImageBarTokenOverlay) over).getBottomAssetId().toString());
      }
    } else if (over instanceof MultipleImageBarTokenOverlay) {
      int increment = 0;
      int max_increments = ((MultipleImageBarTokenOverlay) over).getAssetIds().length;
      if (value != null) {
        increment = Math.max(Math.min(over.findIncrement(value.doubleValue()), max_increments), 0);
      }
      assetId.append(((MultipleImageBarTokenOverlay) over).getAssetIds()[increment].toString());
    } else {
      throw new ParserException(
          I18N.getText("macro.function.barImage.notImage", functionName, barName));
    }
    // control flow should be the same with this code being outside of the if statement
    if (size != null && size.intValue() != 0) {
      assetId.append("-");
      int i = Math.max(Math.min(size.intValue(), 500), 1);
      assetId.append(i);
    }
    return assetId.toString();
  }
}
