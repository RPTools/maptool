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
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TokenCopyDeleteFunctions extends AbstractFunction {

  private static final TokenCopyDeleteFunctions instance = new TokenCopyDeleteFunctions();

  private static final String COPY_FUNC = "copyToken";
  private static final String REMOVE_FUNC = "removeToken";

  private TokenCopyDeleteFunctions() {
    super(1, 4, COPY_FUNC, REMOVE_FUNC);
  }

  public static TokenCopyDeleteFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
    if (functionName.equals(COPY_FUNC)) {
      return copyTokens(res, parameters);
    }

    if (functionName.equals(REMOVE_FUNC)) {
      return deleteToken(res, parameters);
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  private String deleteToken(MapToolVariableResolver res, List<Object> parameters)
      throws ParserException {
    Token token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);

    if (token == null) {
      throw new ParserException("Can not find token " + parameters.get(0));
    }
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    MapTool.serverCommand().removeToken(zone.getId(), token.getId());
    return "Deleted token " + token.getId() + " (" + token.getName() + ")";
  }

  /*
   * Token copyToken(String tokenId, Number numCopies: 1, String fromMap: (""|currentMap()), JSONObject updates: null) JSONArray copyToken(String tokenId, Number numCopies, String fromMap:
   * (""|currentMap()), JSONObject updates: null)
   */
  private Object copyTokens(MapToolVariableResolver res, List<Object> param)
      throws ParserException {
    Token token = null;
    int numberCopies = 1;
    String zoneName = null;
    JSONObject newVals = null;

    int size = param.size();
    switch (size) {
      default: // Come here with four or more parameters
        throw new ParserException(
            I18N.getText("macro.function.general.tooManyParam", COPY_FUNC, 4, size));
      case 4:
        Object o = JSONMacroFunctions.asJSON(param.get(3));
        if (!(o instanceof JSONObject)) {
          throw new ParserException(
              I18N.getText("macro.function.general.argumentTypeO", COPY_FUNC, 4));
        }
        newVals = (JSONObject) o;
      case 3:
        zoneName = param.get(2).toString();
      case 2:
        if (!(param.get(1) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.argumentTypeI", COPY_FUNC, 2, param.get(1).toString()));
        }
        numberCopies = ((BigDecimal) param.get(1)).intValue();
      case 1:
        token = FindTokenFunctions.findToken(param.get(0).toString(), zoneName);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownTokenOnMap",
                  COPY_FUNC,
                  param.get(0).toString(),
                  zoneName));
        }
        Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
        List<String> newTokens = new ArrayList<String>(numberCopies);
        List<Token> allTokens = zone.getTokens();
        for (int i = 0; i < numberCopies; i++) {
          Token t = new Token(token);

          if (allTokens != null) {
            for (Token tok : allTokens) {
              GUID tea = tok.getExposedAreaGUID();
              if (tea != null && tea.equals(t.getExposedAreaGUID())) {
                t.setExposedAreaGUID(new GUID());
              }
            }
          }
          setTokenValues(t, newVals, zone, res);
          zone.putToken(t);

          MapTool.serverCommand().putToken(zone.getId(), t);
          newTokens.add(t.getId().toString());
        }
        MapTool.getFrame().getCurrentZoneRenderer().flushLight();
        if (numberCopies == 1) {
          return newTokens.get(0);
        } else {
          return JSONArray.fromObject(newTokens);
        }
      case 0:
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeT", COPY_FUNC, 1)); // should be notEnoughParams
    }
  }

  private void setTokenValues(Token token, JSONObject vals, Zone zone, MapToolVariableResolver res)
      throws ParserException {
    JSONObject newVals = JSONObject.fromObject(vals);
    newVals = (JSONObject) JSONMacroFunctions.getInstance().JSONEvaluate(res, newVals);

    // FJE Should we remove the keys as we process them? We could then warn the user
    // if there are still keys in the hash at the end...

    // Update the Token Name.
    if (newVals.containsKey("name")) {
      if (newVals.getString("name").equals("")) {
        throw new ParserException(
            I18N.getText("macro.function.tokenName.emptyTokenNameForbidden", COPY_FUNC));
      }
      token.setName(newVals.getString("name"));
    } else {
      // check the token's name, don't change PC token names ... ever
      if (token.getType() != Token.Type.PC) {
        token.setName(MapToolUtil.nextTokenId(zone, token, true));
      }
    }

    // Label
    if (newVals.containsKey("label")) {
      token.setLabel(newVals.getString("label"));
    }

    // GM Name
    if (newVals.containsKey("gmName")) {
      token.setGMName(newVals.getString("gmName"));
    }

    // Layer
    if (newVals.containsKey("layer")) {
      boolean forceShape = true;
      if (newVals.containsKey("forceShape")) {
        String value = newVals.getString("forceShape");
        BigDecimal val = new BigDecimal(value);
        forceShape = !BigDecimal.ZERO.equals(val);
      }
      TokenPropertyFunctions.getInstance().setLayer(token, newVals.getString("layer"), forceShape);
    }

    int x = token.getX();
    int y = token.getY();

    // Location...
    boolean useDistance = false; // FALSE means to multiple x,y values by grid size
    if (newVals.containsKey("useDistance")) {
      if (newVals.getInt("useDistance") != 0) {
        useDistance = true;
      }
    }
    Grid grid =
        zone.getGrid(); // These won't change for a given execution; this could be more efficient
    if (!useDistance) {
      CellPoint cp = grid.convert(new ZonePoint(x, y));
      x = cp.x;
      y = cp.y;
    }

    boolean tokenMoved = false;
    boolean delta = false;
    if (newVals.containsKey("delta")) {
      if (newVals.getInt("delta") != 0) {
        delta = true;
      }
    }

    // X
    if (newVals.containsKey("x")) {
      int tmpX = newVals.getInt("x");
      x = tmpX + (delta ? x : 0);
      tokenMoved = true;
    }

    // Y
    if (newVals.containsKey("y")) {
      int tmpY = newVals.getInt("y");
      y = tmpY + (delta ? y : 0);
      tokenMoved = true;
    }

    if (tokenMoved) {
      // System.err.println(newVals + " @ (" + x + ", " + y + ")");
      TokenLocationFunctions.getInstance().moveToken(token, x, y, useDistance);
    }

    // Facing
    if (newVals.containsKey("facing")) {
      token.setFacing(newVals.getInt("facing"));
      // MapTool.getFrame().getCurrentZoneRenderer().flushLight(); // FJE Already part of
      // copyToken()
    }

    // Size
    if (newVals.containsKey("size")) { // FJE ... && token.isSnapToScale()) {
      String size = newVals.getString("size");
      if (size.equalsIgnoreCase("native") || size.equalsIgnoreCase("free")) {
        token.setSnapToScale(false);
      } else {
        for (TokenFootprint footprint : grid.getFootprints()) {
          if (footprint.getName().equalsIgnoreCase(size)) {
            token.setFootprint(grid, footprint);
            token.setSnapToScale(true);
            break;
          }
        }
      }
    }

    // tokenImage
    if (newVals.containsKey("tokenImage")) {
      String assetName = newVals.getString("tokenImage");
      TokenImage.setImage(token, assetName);
    }
    // handoutImage
    if (newVals.containsKey("handoutImage")) {
      String assetName = newVals.getString("handoutImage");
      TokenImage.setHandout(token, assetName);
    }
    // portraitImage
    if (newVals.containsKey("portraitImage")) {
      String assetName = newVals.getString("portraitImage");
      TokenImage.setPortrait(token, assetName);
    }
  }
}
