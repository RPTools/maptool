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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

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
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    FunctionUtil.blockUntrustedMacro(functionName);
    int psize = parameters.size();

    if (functionName.equals(COPY_FUNC)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 2);
      int nCopies = psize > 1 ? FunctionUtil.paramAsInteger(functionName, parameters, 1, false) : 1;
      JsonObject newVals;
      if (psize > 3) newVals = FunctionUtil.paramAsJsonObject(functionName, parameters, 3);
      else newVals = new JsonObject();

      return copyTokens((MapToolVariableResolver) resolver, token, nCopies, newVals);
    }

    if (functionName.equals(REMOVE_FUNC)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);

      return deleteToken(token);
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Deletes the token.
   *
   * @param token the token
   * @return a string describing which token got deleted
   */
  private String deleteToken(Token token) {
    Zone zone = token.getZoneRenderer().getZone();
    MapTool.serverCommand().removeToken(zone.getId(), token.getId());
    return "Deleted token " + token.getId() + " (" + token.getName() + ")";
  }

  /**
   * Token copyToken(String tokenId, Number numCopies: 1, String fromMap: (""|currentMap()),
   * JsonObject updates: null) JsonArray copyToken(String tokenId, Number numCopies, String fromMap:
   * (""|currentMap()), JsonObject updates: null)
   *
   * @param token the token to copy
   * @param nCopies the number of copies
   * @param newVals a json object holding the new values of the copies
   * @param res the MapToolVariableResolver
   */
  private Object copyTokens(
      MapToolVariableResolver res, Token token, int nCopies, JsonObject newVals)
      throws ParserException {

    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    List<String> newTokens = new ArrayList<>(nCopies);
    List<Token> allTokens = zone.getTokens();
    for (int i = 0; i < nCopies; i++) {
      Token t = new Token(token);

      // Make sure the exposedAreaGUID stays unique
      if (allTokens != null) {
        for (Token tok : allTokens) {
          GUID tea = tok.getExposedAreaGUID();
          if (tea != null && tea.equals(t.getExposedAreaGUID())) {
            t.setExposedAreaGUID(new GUID());
          }
        }
      }
      // setTokenValues() handles the naming of the new token and must be called even if
      // nothing was passed for the updates parameter (newVals).
      setTokenValues(t, newVals, zone, res);

      MapTool.serverCommand().putToken(zone.getId(), t);
      newTokens.add(t.getId().toString());
    }
    if (nCopies == 1) {
      return newTokens.get(0);
    } else {
      JsonArray jsonArray = new JsonArray();
      for (String val : newTokens) {
        jsonArray.add(val);
      }
      return jsonArray;
    }
  }

  /**
   * This change various properties of a token. It is intended to be run before the token is sent to
   * the server via putToken and as such, should only make local changes.
   *
   * @param token the token to change
   * @param vals a JsonObject containing the new values
   * @param zone the zone where the token is
   * @param res the MapToolVariableResolver
   */
  private void setTokenValues(Token token, JsonObject vals, Zone zone, MapToolVariableResolver res)
      throws ParserException {
    // Evaluates the content of the json
    JsonObject newVals = JSONMacroFunctions.getInstance().jsonEvaluate(vals, res).getAsJsonObject();

    // FJE Should we remove the keys as we process them? We could then warn the user
    // if there are still keys in the hash at the end...

    // Update the Token Name.
    if (newVals.has("name")) {
      if (newVals.get("name").getAsString().equals("")) {
        throw new ParserException(
            I18N.getText("macro.function.tokenName.emptyTokenNameForbidden", COPY_FUNC));
      }
      token.setName(newVals.get("name").getAsString());
    } else {
      // check the token's name, don't change PC token names ... ever
      if (token.getType() != Token.Type.PC) {
        token.setName(MapToolUtil.nextTokenId(zone, token, true));
      }
    }

    // Label
    if (newVals.has("label")) {
      token.setLabel(newVals.get("label").getAsString());
    }

    // GM Name
    if (newVals.has("gmName")) {
      token.setGMName(newVals.get("gmName").getAsString());
    }

    // Layer
    if (newVals.has("layer")) {
      boolean forceShape = true;
      if (newVals.has("forceShape")) {
        String value = newVals.get("forceShape").getAsString();
        BigDecimal val = new BigDecimal(value);
        forceShape = !BigDecimal.ZERO.equals(val);
      }
      Zone.Layer layer = TokenPropertyFunctions.getLayer(newVals.get("layer").getAsString());
      Token.TokenShape tokenShape = TokenPropertyFunctions.getTokenShape(token, layer, forceShape);
      token.setLayer(layer);
      if (tokenShape != null) {
        token.setShape(tokenShape);
      }
    }

    int x = token.getX();
    int y = token.getY();

    // Location...
    boolean useDistance = false; // FALSE means to multiple x,y values by grid size
    if (newVals.has("useDistance")) {
      useDistance = newVals.get("useDistance").getAsInt() != 0;
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
    if (newVals.has("delta")) {
      delta = newVals.get("delta").getAsInt() != 0;
    }

    // X
    if (newVals.has("x")) {
      int tmpX = newVals.get("x").getAsInt();
      x = tmpX + (delta ? x : 0);
      tokenMoved = true;
    }

    // Y
    if (newVals.has("y")) {
      int tmpY = newVals.get("y").getAsInt();
      y = tmpY + (delta ? y : 0);
      tokenMoved = true;
    }

    if (tokenMoved) {
      // System.err.println(newVals + " @ (" + x + ", " + y + ")");
      ZonePoint zp = TokenLocationFunctions.getZonePoint(x, y, useDistance);
      token.setX(zp.x);
      token.setY(zp.y);
    }

    // Facing
    if (newVals.has("facing")) {
      token.setFacing(newVals.get("facing").getAsInt());
      // MapTool.getFrame().getCurrentZoneRenderer().flushLight(); // FJE Already part of
      // copyToken()
    }

    // Size
    if (newVals.has("size")) { // FJE ... && token.isSnapToScale()) {
      String size = newVals.get("size").getAsString();
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

    // legacy use, from pre 1.5.7.
    if (!newVals.has("tokenHandout") && newVals.has("handoutImage")) {
      // handoutImage -> tokenHandout
      newVals.add("tokenHandout", newVals.get("handoutImage"));
    }
    if (!newVals.has("tokenPortrait") && newVals.has("portraitImage")) {
      // portraitImage -> tokenPortrait
      newVals.add("tokenPortrait", newVals.get("portraitImage"));
    }

    // tokenImage
    if (newVals.has("tokenImage")) {
      MD5Key md5key = TokenImage.getMD5Key(newVals.get("tokenImage").getAsString(), COPY_FUNC);
      token.setImageAsset(null, md5key);
    }
    // handoutImage
    if (newVals.has("tokenHandout")) {
      MD5Key md5key = TokenImage.getMD5Key(newVals.get("tokenHandout").getAsString(), COPY_FUNC);
      token.setCharsheetImage(md5key);
    }
    // portraitImage
    if (newVals.has("tokenPortrait")) {
      MD5Key md5key = TokenImage.getMD5Key(newVals.get("tokenPortrait").getAsString(), COPY_FUNC);
      token.setPortraitImage(md5key);
    }
  }
}
