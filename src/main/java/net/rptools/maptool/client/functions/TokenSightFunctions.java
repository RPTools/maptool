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
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenSightFunctions extends AbstractFunction {

  private static final TokenSightFunctions instance = new TokenSightFunctions();

  private TokenSightFunctions() {
    super(0, 2, "hasSight", "setHasSight", "getSightType", "setSightType", "canSeeToken");
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
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    Token token;

    // For functions no parameters except option tokenID
    if (functionName.equals("hasSight") || functionName.equals("getSightType")) {
      if (parameters.size() == 1) {
        token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(0).toString()));
        }
      } else if (parameters.size() == 0) {
        token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
        }
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, 1, parameters.size()));
      }

      if (functionName.equals("hasSight"))
        return token.getHasSight() ? BigDecimal.ONE : BigDecimal.ZERO;

      if (functionName.equals("getSightType")) return token.getSightType();
    }

    // For functions with only 1 parameter and optional second parameter of tokenID
    if (parameters.size() > 2)
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));

    if (parameters.isEmpty())
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notenoughparms", functionName, 1, parameters.size()));

    if (parameters.size() == 2) {
      token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
      }
    } else {
      token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();

      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
      }
    }

    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = renderer.getZone();

    if (functionName.equals("setHasSight")) {
      token.setHasSight(!parameters.get(0).equals(BigDecimal.ZERO));
      zone.putToken(token);
      MapTool.serverCommand().putToken(zone.getId(), token);
      renderer.flushLight();
      return "";
    }

    if (functionName.equals("setSightType")) {
      token.setSightType(parameters.get(0).toString());
      zone.putToken(token);
      MapTool.serverCommand().putToken(zone.getId(), token);
      renderer.flushLight();
      return "";
    }

    if (functionName.equals("canSeeToken")) {
      if (!token.getHasSight()) {
        return "[]";
      }
      Area tokensVisibleArea = renderer.getZoneView().getVisibleArea(token);
      if (tokensVisibleArea == null) {
        return "[]";
      }
      Token target = null;
      try {
        target = FindTokenFunctions.findToken(parameters.get(0).toString(), zone.getName());
      } catch (Exception e) {
        if (e instanceof ClassCastException || token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.argumentTypeT", 2, functionName));
        }
      }
      if (target == null) {
        return "[]";
      }
      if (!target.isVisible() || (target.isVisibleOnlyToOwner() && !AppUtil.playerOwns(target))) {
        return "[]";
      }
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

    return "";
  }
}
