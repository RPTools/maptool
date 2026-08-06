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

    if (over instanceof SingleImageBarTokenOverlay one) {
      assetId.append(one.getAssetId().toString());
    } else if (over instanceof TwoImageBarTokenOverlay two) {
      if (value != null && value.doubleValue() > 0.5) {
        assetId.append(two.getTopAssetId().toString());
      } else {
        assetId.append(two.getBottomAssetId().toString());
      }
    } else if (over instanceof MultipleImageBarTokenOverlay many) {
      int increment = 0;
      int max_increments = many.getAssetIds().size();
      if (value != null) {
        increment = Math.clamp(over.findIncrement(value.doubleValue()), 0, max_increments);
      }
      assetId.append(many.getAssetIds().get(increment).toString());
    } else {
      throw new ParserException(
          I18N.getText("macro.function.barImage.notImage", functionName, barName));
    }

    if (size != null && size.intValue() != 0) {
      assetId.append("-");
      int i = Math.clamp(size.intValue(), 1, 500);
      assetId.append(i);
    }
    return assetId.toString();
  }
}
