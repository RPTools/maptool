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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.math.BigDecimal;
import java.util.List;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenSightFunctions extends AbstractFunction {

  private static final TokenSightFunctions instance = new TokenSightFunctions();

  private TokenSightFunctions() {
    super(0, 3, "hasSight", "setHasSight", "getSightType", "setSightType", "canSeeToken");
  }

  public static TokenSightFunctions getInstance() {
    return instance;
  }

  private enum TokenLocations {
    TOP_LEFT,
    BOTTOM_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    CENTER
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    Token token;
    // For functions no parameters except option tokenID and mapname
    if (functionName.equalsIgnoreCase("hasSight")
        || functionName.equalsIgnoreCase("getSightType")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      if (functionName.equalsIgnoreCase("hasSight"))
        return token.getHasSight() ? BigDecimal.ONE : BigDecimal.ZERO;

      // if (functionName.equalsIgnoreCase("getSightType"))
      // Don't test to remove code warning for always true if statement
      String sightType = token.getSightType();
      if (sightType == null) sightType = "";
      return sightType;
    }

    // For functions with only 1 parameter and optional second parameter of tokenID & mapname
    FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
    token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);

    if (functionName.equalsIgnoreCase("setHasSight")) {
      boolean hasSight = !parameters.get(0).equals(BigDecimal.ZERO);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setHasSight, hasSight);
      return token.getHasSight() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    if (functionName.equalsIgnoreCase("setSightType")) {
      String sightType = parameters.get(0).toString();
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setSightType, sightType);
      return token.getSightType();
    }

    if (functionName.equalsIgnoreCase("canSeeToken")) {
      if (!token.getHasSight()) {
        return "[]";
      }
      ZoneRenderer zoneRenderer = token.getZoneRenderer();
      Area tokensVisibleArea =
          zoneRenderer.getZoneView().getVisibleArea(token, zoneRenderer.getPlayerView());
      if (tokensVisibleArea == null) {
        return "[]";
      }
      Token target = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 2);
      if (!target.isVisible() || (target.isVisibleOnlyToOwner() && !AppUtil.playerOwns(target))) {
        return "[]";
      }
      Zone zone = zoneRenderer.getZone();
      Grid grid = zone.getGrid();

      Rectangle bounds =
          target
              .getFootprint(grid)
              .getBounds(grid, grid.convert(new ZonePoint(target.getX(), target.getY())));
      if (!target.isSnapToGrid()) bounds = target.getBounds(zone);

      int x = (int) bounds.getX();
      int y = (int) bounds.getY();
      int w = (int) bounds.getWidth();
      int h = (int) bounds.getHeight();

      StringBuilder sb = new StringBuilder();
      sb.append("[");

      int halfX = x + (w) / 2;
      int halfY = y + (h) / 2;
      if (tokensVisibleArea.intersects(bounds)) {
        if (tokensVisibleArea.contains(new Point(x, y))) {
          // TOP_LEFT
          sb.append("\"");
          sb.append(TokenLocations.TOP_LEFT.toString());
          sb.append("\", ");
        }
        if (tokensVisibleArea.contains(new Point(x, y + h))) {
          // BOTTOM_LEFT
          sb.append("\"");
          sb.append(TokenLocations.BOTTOM_LEFT.toString());
          sb.append("\", ");
        }
        if (tokensVisibleArea.contains(new Point(x + w, y))) {
          // TOP_RIGHT
          sb.append("\"");
          sb.append(TokenLocations.TOP_RIGHT.toString());
          sb.append("\", ");
        }
        if (tokensVisibleArea.contains(new Point(x + w, y + h))) {
          // BOTTOM_RIGHT
          sb.append("\"");
          sb.append(TokenLocations.BOTTOM_RIGHT.toString());
          sb.append("\", ");
        }
        if (tokensVisibleArea.contains(new Point(halfX, halfY))) {
          // BOTTOM_RIGHT
          sb.append("\"");
          sb.append(TokenLocations.CENTER.toString());
          sb.append("\", ");
        }
      }
      if (sb.length() > 2 && sb.lastIndexOf(", ") == sb.length() - 2) {
        sb.replace(sb.length() - 2, sb.length(), "");
      }
      sb.append("]");
      return sb.toString();
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }
}
