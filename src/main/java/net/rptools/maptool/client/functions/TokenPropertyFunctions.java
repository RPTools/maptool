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

import java.awt.Image;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;
import net.rptools.maptool.util.TokenUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class TokenPropertyFunctions extends AbstractFunction {
  private static final TokenPropertyFunctions instance = new TokenPropertyFunctions();

  private TokenPropertyFunctions() {
    super(
        0,
        4,
        "getPropertyNames",
        "getAllPropertyNames",
        "getPropertyNamesRaw",
        "hasProperty",
        "isNPC",
        "isPC",
        "setPC",
        "setNPC",
        "getLayer",
        "setLayer",
        "getSize",
        "setSize",
        "getOwners",
        "isOwnedByAll",
        "isOwner",
        "resetProperty",
        "getProperty",
        "setProperty",
        "isPropertyEmpty",
        "getPropertyDefault",
        "sendToBack",
        "bringToFront",
        "getLibProperty",
        "setLibProperty",
        "getLibPropertyNames",
        "setPropertyType",
        "getPropertyType",
        "getRawProperty",
        "getTokenFacing",
        "setTokenFacing",
        "removeTokenFacing",
        "getTokenRotation",
        "getMatchingProperties",
        "getMatchingLibProperties",
        "isSnapToGrid",
        "setOwner",
        "setOwnedByAll",
        "getTokenNativeWidth",
        "getTokenNativeHeight",
        "getTokenWidth",
        "getTokenHeight",
        "setTokenWidth",
        "setTokenHeight",
        "getTokenShape",
        "setTokenShape",
        "getGMNotes",
        "setGMNotes",
        "getNotes",
        "setNotes",
        "setTokenSnapToGrid");
  }

  public static TokenPropertyFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    MapToolVariableResolver resolver = (MapToolVariableResolver) parser.getVariableResolver();

    // Cached for all those putToken() calls that are needed
    ZoneRenderer zoneR = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zoneR.getZone();

    /*
     * String type = getPropertyType(String tokenId: currentToken())
     */
    if (functionName.equals("getPropertyType")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return token.getPropertyType();
    }

    /*
     * String empty = setPropertyType(String propTypeName, String tokenId: currentToken())
     */
    if (functionName.equals("setPropertyType")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      token.setPropertyType(parameters.get(0).toString());
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(
          token); // TODO: FJE Should this be here? Added because other places have it...?!
      return "";
    }

    /*
     * String names = getPropertyNames(String delim: ",", String tokenId: currentToken())
     */
    if (functionName.equals("getPropertyNames") || functionName.equals("getPropertyNamesRaw")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      String pattern = ".*";
      return getPropertyNames(token, delim, pattern, functionName.equals("getPropertyNamesRaw"));
    }

    /*
     * String names = getMatchingProperties(String pattern, String delim: ",", String tokenId: currentToken())
     */
    if (functionName.equals("getMatchingProperties")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 2);
      String pattern = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      return getPropertyNames(token, delim, pattern, false);
    }

    /*
     * String names = getAllPropertyNames(String propType: "", String delim: ",")
     */
    if (functionName.equals("getAllPropertyNames")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      if (parameters.size() < 1) {
        return getAllPropertyNames(null, ",");
      } else {
        return getAllPropertyNames(
            parameters.get(0).toString(),
            parameters.size() > 1 ? parameters.get(1).toString() : ",");
      }
    }

    /*
     * Number zeroOne = hasProperty(String propName, String tokenId: currentToken())
     */
    if (functionName.equals("hasProperty")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      return hasProperty(token, parameters.get(0).toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isNPC(String tokenId: currentToken())
     */
    if (functionName.equals("isNPC")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return token.getType() == Token.Type.NPC ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isPC(String tokenId: currentToken())
     */
    if (functionName.equals("isPC")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return token.getType() == Token.Type.PC ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String empty = setPC(String tokenId: currentToken())
     */
    if (functionName.equals("setPC")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      token.setType(Token.Type.PC);
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      zoneR.flushLight();
      MapTool.getFrame().updateTokenTree();
      return "";
    }

    /*
     * String empty = setNPC(String tokenId: currentToken())
     */
    if (functionName.equals("setNPC")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      token.setType(Token.Type.NPC);
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      zoneR.flushLight();
      MapTool.getFrame().updateTokenTree();
      return "";
    }

    /*
     * String layer = getLayer(String tokenId: currentToken())
     */
    if (functionName.equals("getLayer")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return token.getLayer().name();
    }

    /*
     * String layer = setLayer(String layer, String tokenId: currentToken(), boolean forceShape: true)
     */
    if (functionName.equals("setLayer")) {
      boolean forceShape = true;
      checkNumberOfParameters(functionName, parameters, 1, 3);
      if (parameters.size() == 3) {
        forceShape = !BigDecimal.ZERO.equals(parameters.get(2));
      }
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      String layer = setLayer(token, parameters.get(0).toString(), forceShape);
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      zoneR.flushLight();
      MapTool.getFrame().updateTokenTree();
      return layer;
    }

    /*
     * String size = getSize(String tokenId: currentToken())
     */
    if (functionName.equals("getSize")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return getSize(token);
    }

    /*
     * String size = setSize(String size, String tokenId: currentToken())
     */
    if (functionName.equals("setSize")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      return setSize(token, parameters.get(0).toString());
    }

    /*
     * String owners = getOwners(String delim: ",", String tokenId: currentToken())
     */
    if (functionName.equals("getOwners")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      return getOwners(token, parameters.size() > 0 ? parameters.get(0).toString() : ",");
    }

    /*
     * Number zeroOne = isOwnedByAll(String tokenId: currentToken())
     */
    if (functionName.equals("isOwnedByAll")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return token.isOwnedByAll() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isOwner(String player: self, String tokenId: currentToken())
     */
    if (functionName.equals("isOwner")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      if (parameters.size() > 0) {
        return token.isOwner(parameters.get(0).toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      return token.isOwner(MapTool.getPlayer().getName()) ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String empty = resetProperty(String propName, String tokenId: currentToken())
     */
    if (functionName.equals("resetProperty")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      token.resetProperty(parameters.get(0).toString());
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      return "";
    }

    /*
     * String empty = setProperty(String propName, String value, String tokenId: currentToken())
     */
    if (functionName.equals("setProperty")) {
      checkNumberOfParameters(functionName, parameters, 2, 3);
      Token token = getTokenFromParam(resolver, functionName, parameters, 2);
      token.setProperty(parameters.get(0).toString(), parameters.get(1).toString());
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      return "";
    }

    /*
     * {String|Number} value = getRawProperty(String propName, String tokenId: currentToken())
     */
    if (functionName.equals("getRawProperty")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      Object val = token.getProperty(parameters.get(0).toString());
      if (val == null) {
        return "";
      }

      if (val instanceof String) {
        // try to convert to a number
        try {
          return new BigDecimal(val.toString()); // XXX Localization here?
        } catch (Exception e) {
          return val;
        }
      } else {
        return val;
      }
    }

    /*
     * {String|Number} value = getProperty(String propName, String tokenId: currentToken())
     */
    if (functionName.equals("getProperty")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      Object val = token.getEvaluatedProperty(parameters.get(0).toString());

      if (val instanceof String) {
        // try to convert to a number
        try {
          return new BigDecimal(val.toString());
        } catch (Exception e) {
          return val;
        }
      } else {
        return val;
      }
    }

    /*
     * Number zeroOne = isPropertyEmpty(String propName, String tokenId: currentToken())
     */
    if (functionName.equals("isPropertyEmpty")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      return token.getProperty(parameters.get(0).toString()) == null
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }

    /*
     * pre 1.3.b64 only took a single parameter
     *
     * Number zeroOne = getPropertyDefault(String propName, String propType: currentToken().getPropertyType())
     */
    if (functionName.equals("getPropertyDefault")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = resolver.getTokenInContext();
      String name = parameters.get(0).toString();
      String propType =
          parameters.size() > 1 ? parameters.get(1).toString() : token.getPropertyType();

      Object val = null;

      List<TokenProperty> propertyList =
          MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(propType);
      if (propertyList != null) {
        for (TokenProperty property : propertyList) {
          if (name.equalsIgnoreCase(property.getName())) {
            val = property.getDefaultValue();
            break;
          }
        }
      }
      if (val == null) {
        return "";
      }
      if (val instanceof String) {
        // try to convert to a number
        try {
          return new BigDecimal(val.toString());
        } catch (Exception e) {
          return val;
        }
      } else {
        return val;
      }
    }

    /*
     * String notes = getGMNotes(String tokenId: currentToken())
     */
    if (functionName.equals("getGMNotes")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      String notes = token.getGMNotes();
      return notes != null ? notes : "";
    }

    /*
     * String notes = setGMNotes(String notes, String tokenId: currentToken())
     */
    if (functionName.equals("setGMNotes")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      token.setGMNotes(parameters.get(0).toString());
      zone.putToken(token);
      return token.getGMNotes();
    }

    /*
     * String notes = getNotes(String tokenId: currentToken())
     */
    if (functionName.equals("getNotes")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      String notes = token.getNotes();
      return notes != null ? notes : "";
    }

    /*
     * String notes = setNotes(String notes, String tokenId: currentToken())
     */
    if (functionName.equals("setNotes")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      token.setNotes(parameters.get(0).toString());
      zone.putToken(token);
      return token.getNotes();
    }

    /*
     * String empty = bringToFront(String tokenId: currentToken())
     */
    if (functionName.equals("bringToFront")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      token.setZOrder(zone.getLargestZOrder() + 1);
      MapTool.serverCommand().putToken(zone.getId(), token);

      return BigDecimal.valueOf(token.getZOrder());
    }

    /*
     * String empty = sendToBack(String tokenId: currentToken())
     */
    if (functionName.equals("sendToBack")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      token.setZOrder(zone.getSmallestZOrder() - 1);
      MapTool.serverCommand().putToken(zone.getId(), token);

      return BigDecimal.valueOf(token.getZOrder());
    }

    /*
     * String value = getLibProperty(String propName, String tokenId: macroSource)
     */
    if (functionName.equals("getLibProperty")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      String location;
      if (parameters.size() > 1) {
        location = parameters.get(1).toString();
      } else {
        location = MapTool.getParser().getMacroSource();
      }
      Token token = MapTool.getParser().getTokenMacroLib(location);
      Object val = token.getProperty(parameters.get(0).toString());

      // Attempt to convert to a number ...
      try {
        val = new BigDecimal(val.toString());
      } catch (Exception e) {
        // Ignore, use previous value of "val"
      }
      return val == null ? "" : val;
    }

    /*
     * String empty = setLibProperty(String propName, String value, String tokenId: macroSource)
     */
    if (functionName.equals("setLibProperty")) {
      checkNumberOfParameters(functionName, parameters, 2, 3);
      String location;
      if (parameters.size() > 2) {
        location = parameters.get(2).toString();
      } else {
        location = MapTool.getParser().getMacroSource();
      }
      Token token = MapTool.getParser().getTokenMacroLib(location);
      token.setProperty(parameters.get(0).toString(), parameters.get(1).toString());
      Zone z = MapTool.getParser().getTokenMacroLibZone(location);
      MapTool.serverCommand().putToken(z.getId(), token);
      z.putToken(
          token); // Note: not `zone' since we want only the zone this particular token came from
      return "";
    }

    /*
     * String names = getLibPropertyNames(String tokenId: {macroSource | "*" | "this"}, String delim: ",")
     */
    if (functionName.equals("getLibPropertyNames")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      String location;
      if (parameters.size() > 0) {
        location = parameters.get(0).toString();
        if (location.equals("*") || location.equalsIgnoreCase("this")) {
          location = MapTool.getParser().getMacroSource();
        }
      } else {
        location = MapTool.getParser().getMacroSource();
      }
      Token token = MapTool.getParser().getTokenMacroLib(location);
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.tokenProperty.unknownLibToken", functionName, location));
      }
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      return getPropertyNames(token, delim, ".*", false);
    }

    /*
     * String names = getMatchingLibProperties(String pattern, String tokenId: {macroSource | "*" | "this"}, String delim: ",")
     */
    if (functionName.equals("getMatchingLibProperties")) {
      checkNumberOfParameters(functionName, parameters, 1, 3);
      String location;
      String pattern = parameters.get(0).toString();
      if (parameters.size() > 1) {
        location = parameters.get(1).toString();
        if (location.equals("*") || location.equalsIgnoreCase("this")) {
          location = MapTool.getParser().getMacroSource();
        }
      } else {
        location = MapTool.getParser().getMacroSource();
      }
      Token token = MapTool.getParser().getTokenMacroLib(location);
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.tokenProperty.unknownLibToken", functionName, location));
      }
      String delim = parameters.size() > 2 ? parameters.get(2).toString() : ",";
      return getPropertyNames(token, delim, pattern, false);
    }

    /*
     * Number facing = getTokenFacing(String tokenId: currentToken())
     */
    if (functionName.equals("getTokenFacing")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      if (token.getFacing() == null) {
        return ""; // XXX Should be -1 instead of a string?
      }
      return BigDecimal.valueOf(token.getFacing());
    }

    /*
     * Number degrees = getTokenRotation(String tokenId: currentToken())
     */
    if (functionName.equals("getTokenRotation")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);

      return BigDecimal.valueOf(token.getFacingInDegrees());
    }

    /*
     * String empty = setTokenFacing(Number facing, String tokenId: currentToken())
     */
    if (functionName.equals("setTokenFacing")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      BigDecimal facing = getBigDecimalFromParam(functionName, parameters, 0);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      token.setFacing(facing.intValue());
      MapTool.serverCommand().putToken(zone.getId(), token);
      zoneR
          .flushLight(); // FJE This isn't needed unless the token had a light source, right? Should
      // we check for that?
      zone.putToken(token);
      return "";
    }

    /*
     * String empty = removeTokenFacing(String tokenId: currentToken())
     */
    if (functionName.equals("removeTokenFacing")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      token.setFacing(null);
      MapTool.serverCommand().putToken(zone.getId(), token);
      zoneR.flushLight();
      zone.putToken(token);
      return "";
    }

    /*
     * Number zeroOne = isSnapToGrid(String tokenId: currentToken())
     */
    if (functionName.equals("isSnapToGrid")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      Token token = getTokenFromParam(resolver, functionName, parameters, 0);
      return token.isSnapToGrid() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String empty = setOwner(String playerName | JSONArray playerNames, String tokenId: currentToken())
     */
    if (functionName.equals("setOwner")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);
      boolean trusted = MapTool.getParser().isMacroTrusted();
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      // Remove current owners, but if this macro is untrusted and the current player is an owner,
      // keep the
      // ownership there.
      String myself = MapTool.getPlayer().getName();
      token.clearAllOwners();
      String s = parameters.get(0).toString();
      if (StringUtil.isEmpty(s)) {
        // Do nothing when trusted, since all ownership should be turned off for an empty string
        // used in such a macro.
      } else {
        Object json = JSONMacroFunctions.asJSON(parameters.get(0));
        if (json != null && json instanceof JSONArray) {
          for (Object o : (JSONArray) json) {
            token.addOwner(o.toString());
          }
        } else {
          token.addOwner(s);
        }
      }
      if (!trusted)
        token.addOwner(
            myself); // If not trusted we must have been in the owner list -- keep us there.
      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      return "";
    }

    /*
     * String empty = setOwner(0|1, String tokenId: currentToken())
     */
    if (functionName.equals("setOwnedByAll")) {
      // If not trusted, do nothing and return -1 result
      if (!MapTool.getParser().isMacroTrusted()) return -1;

      checkNumberOfParameters(functionName, parameters, 1, 2);
      Token token = getTokenFromParam(resolver, functionName, parameters, 1);
      BigDecimal ownedByAll = getBigDecimalFromParam(functionName, parameters, 0);

      if (ownedByAll.compareTo(BigDecimal.ZERO) == 0) {
        token.setOwnedByAll(false);
      } else {
        token.setOwnedByAll(true);
      }

      MapTool.serverCommand().putToken(zone.getId(), token);
      zone.putToken(token);
      return token.isOwnedByAll() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String newShape = getTokenShape(String tokenId: currentToken())
     *
     * See Token.TokenShape for return values. Currently "Top down", "Circle", and "Square".
     */
    if (functionName.equals("getTokenShape")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);

      Token token;
      token = getTokenFromContextOrParam(parser, functionName, parameters);
      return token.getShape().toString();
    }

    /*
     * String newShape = setTokenShape(String shape, String tokenId: currentToken())
     *
     * See Token.TokenShape for shape values. Currently "Top down", "Top_down", "Circle", and "Square".
     */
    if (functionName.equals("setTokenShape")) {
      checkNumberOfParameters(functionName, parameters, 0, 2);

      Token token;
      // TODO: should just call getTokenFromParam? This doesn't check if the macro is trusted
      // though...
      if (parameters.size() == 1) {
        token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
        if (token == null)
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", functionName));
      } else {
        token =
            getTokenFromParam(
                (MapToolVariableResolver) parser.getVariableResolver(),
                functionName,
                parameters,
                1);
        if (token == null)
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  functionName,
                  parameters.get(1).toString()));
      }
      Token.TokenShape newShape =
          Token.TokenShape.valueOf(
              parameters.get(0).toString().toUpperCase().trim().replace(" ", "_"));
      token.setShape(newShape);
      return token.getShape().toString();
    }

    /*
     * String newShape = getTokenWidth(String tokenId: currentToken())
     *
     * String newShape = getTokenHeight(String tokenId: currentToken())
     *
     * Returns pixel width/height for a given token. Useful for free size tokens.
     */
    if (functionName.equals("getTokenNativeWidth") || functionName.equals("getTokenNativeHeight")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);

      Token token;
      token = getTokenFromContextOrParam(parser, functionName, parameters);

      if (functionName.equals("getTokenNativeWidth")) {
        return BigDecimal.valueOf(token.getWidth());
      } else { // it wasn't 'getTokenWidth' which means functionName equals 'getTokenHeight'
        return BigDecimal.valueOf(token.getHeight());
      }
    }

    /*
     * String newShape = getTokenWidth(String tokenId: currentToken())
     *
     * String newShape = getTokenHeight(String tokenId: currentToken())
     *
     * Returns pixel width/height for a given token. Useful for free size tokens.
     */
    if (functionName.equals("getTokenWidth") || functionName.equals("getTokenHeight")) {
      checkNumberOfParameters(functionName, parameters, 0, 1);

      Token token;
      token = getTokenFromContextOrParam(parser, functionName, parameters);
      // Get the pixel width or height of a given token
      Rectangle tokenBounds = token.getBounds(zone);

      if (functionName.equals("getTokenWidth")) {
        return BigDecimal.valueOf(tokenBounds.width);
      } else { // it wasn't 'getTokenWidth' which means functionName equals 'getTokenHeight'
        return BigDecimal.valueOf(tokenBounds.height);
      }
    }

    /*
     * Sets the width/height for a given token. Useful for free size tokens.
     */
    if (functionName.equals("setTokenWidth") || functionName.equals("setTokenHeight")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);
      double magnitude = getBigDecimalFromParam(functionName, parameters, 0).doubleValue();
      Token token =
          getTokenFromParam(
              (MapToolVariableResolver) parser.getVariableResolver(), functionName, parameters, 1);
      Rectangle tokenBounds = token.getBounds(zone);
      double oldWidth = tokenBounds.width;
      double oldHeight = tokenBounds.height;
      token.setSnapToScale(false);

      if (functionName.equals("setTokenWidth")) {
        token.setScaleX(magnitude / token.getWidth());
        token.setScaleY(oldHeight / token.getHeight());
      } else { // it wasn't 'setTokenWidth' which means functionName equals 'setTokenHeight'
        token.setScaleX(oldWidth / token.getWidth());
        token.setScaleY(magnitude / token.getHeight());
      }
      return "";
    }

    /* Sets whether the token should snap to the grid or not */
    if (functionName.equals("setTokenSnapToGrid")) {
      checkNumberOfParameters(functionName, parameters, 1, 2);

      Object param = parameters.get(0);
      Token token =
          getTokenFromParam(
              (MapToolVariableResolver) parser.getVariableResolver(), functionName, parameters, 1);
      token.setSnapToGrid(AbstractTokenAccessorFunction.getBooleanValue(param));
      return "";
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  private Token getTokenFromContextOrParam(
      Parser parser, String functionName, List<Object> parameters) throws ParserException {
    Token token;
    if (parameters.isEmpty()) {
      token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
      if (token == null)
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
    } else {
      token =
          getTokenFromParam(
              (MapToolVariableResolver) parser.getVariableResolver(), functionName, parameters, 0);
      if (token == null)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
    }
    return token;
  }

  /**
   * Gets the size of the token.
   *
   * @param token The token to get the size of.
   * @return the size of the token.
   */
  private String getSize(Token token) {
    Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
    if (token.isSnapToScale()) {
      for (TokenFootprint footprint : grid.getFootprints()) {
        if (token.getFootprint(grid) == footprint) {
          return footprint.getName();
        }
      }
    }
    return "";
  }

  /**
   * Sets the size of the token.
   *
   * @param token The token to set the size of.
   * @param size The size to set the token to.
   * @return The new size of the token.
   * @throws ParserException if the size specified is an invalid size.
   */
  private String setSize(Token token, String size) throws ParserException {
    if (size.equalsIgnoreCase("native") || size.equalsIgnoreCase("free")) {
      token.setSnapToScale(false);
      return getSize(token);
    }
    token.setSnapToScale(true);
    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = renderer.getZone();
    Grid grid = zone.getGrid();
    for (TokenFootprint footprint : grid.getFootprints()) {
      if (footprint.getName().equalsIgnoreCase(size)) {
        token.setFootprint(grid, footprint);
        renderer.flush(token);
        renderer.repaint();
        MapTool.serverCommand().putToken(zone.getId(), token);
        zone.putToken(token);
        MapTool.getFrame().updateTokenTree();
        return getSize(token);
      }
    }
    throw new ParserException(
        I18N.getText("macro.function.tokenProperty.invalidSize", "setSize", size));
  }

  /**
   * Sets the layer of the token.
   *
   * @param token The token to move to a different layer.
   * @param layerName the name of the layer to move the token to.
   * @param forceShape normally <code>true</code>, but can be optionally set to <code>false</code>
   *     by MTscript
   * @return the name of the layer the token was moved to.
   * @throws ParserException if the layer name is invalid.
   */
  public String setLayer(Token token, String layerName, boolean forceShape) throws ParserException {
    Zone.Layer layer;

    if (layerName.equalsIgnoreCase(Zone.Layer.TOKEN.toString())) {
      layer = Zone.Layer.TOKEN;
    } else if (layerName.equalsIgnoreCase(Zone.Layer.BACKGROUND.toString())) {
      layer = Zone.Layer.BACKGROUND;
    } else if (layerName.equalsIgnoreCase("gm")
        || layerName.equalsIgnoreCase(Zone.Layer.GM.toString())) {
      layer = Zone.Layer.GM;
    } else if (layerName.equalsIgnoreCase(Zone.Layer.OBJECT.toString())) {
      layer = Zone.Layer.OBJECT;
    } else {
      throw new ParserException(
          I18N.getText("macro.function.tokenProperty.unknownLayer", "setLayer", layerName));
    }
    token.setLayer(layer);
    if (forceShape) {
      switch (layer) {
        case BACKGROUND:
        case OBJECT:
          token.setShape(Token.TokenShape.TOP_DOWN);
          break;
        case GM:
        case TOKEN:
          Image image = ImageManager.getImage(token.getImageAssetId());
          if (image == null || image == ImageManager.TRANSFERING_IMAGE) {
            token.setShape(Token.TokenShape.TOP_DOWN);
          } else {
            token.setShape(TokenUtil.guessTokenType(image));
          }
          break;
      }
    }
    return layerName;
  }

  /**
   * Checks to see if the token has the specified property.
   *
   * @param token The token to check.
   * @param name The name of the property to check.
   * @return true if the token has the property.
   */
  private boolean hasProperty(Token token, String name) {
    Object val = token.getProperty(name);
    if (val == null) {
      return false;
    }

    if (StringUtil.isEmpty(val.toString())) {
      return false;
    }

    return true;
  }

  /**
   * Gets all the property names for the specified type. If type is null then all the property names
   * for all types are returned.
   *
   * @param type The type of property.
   * @param delim The list delimiter.
   * @return a string list containing the property names.
   * @throws ParserException
   */
  private String getAllPropertyNames(String type, String delim) throws ParserException {
    if (type == null || type.length() == 0 || type.equals("*")) {
      Map<String, List<TokenProperty>> pmap =
          MapTool.getCampaign().getCampaignProperties().getTokenTypeMap();
      ArrayList<String> namesList = new ArrayList<String>();

      for (Entry<String, List<TokenProperty>> entry : pmap.entrySet()) {
        for (TokenProperty tp : entry.getValue()) {
          namesList.add(tp.getName());
        }
      }
      if ("json".equals(delim)) {
        return JSONArray.fromObject(namesList).toString();
      } else {
        return StringFunctions.getInstance().join(namesList, delim);
      }
    } else {
      List<TokenProperty> props =
          MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(type);
      if (props == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.tokenProperty.unknownPropType", "getAllPropertyNames", type));
      }
      ArrayList<String> namesList = new ArrayList<String>();
      for (TokenProperty tp : props) {
        namesList.add(tp.getName());
      }
      if ("json".equals(delim)) {
        return JSONArray.fromObject(namesList).toString();
      } else {
        return StringFunctions.getInstance().join(namesList);
      }
    }
  }

  /**
   * Creates a string list delimited by <b>delim</b> of the names of all the properties for a given
   * token. Returned strings are all lowercase.
   *
   * @param token The token to get the property names for.
   * @param delim The delimiter for the list.
   * @param pattern The regexp pattern to match.
   * @return the string list of property names.
   */
  private String getPropertyNames(Token token, String delim, String pattern, boolean raw) {
    List<String> namesList = new ArrayList<String>();
    Pattern pat = Pattern.compile(pattern);
    Set<String> propSet = (raw ? token.getPropertyNamesRaw() : token.getPropertyNames());
    String[] propArray = new String[propSet.size()];
    propSet.toArray(propArray);
    Arrays.sort(propArray);

    for (String name : propArray) {
      Matcher m = pat.matcher(name);
      if (m.matches()) {
        namesList.add(name);
      }
    }

    String[] names = new String[namesList.size()];
    namesList.toArray(names);
    if ("json".equals(delim)) {
      return JSONArray.fromObject(names).toString();
    } else {
      return StringFunctions.getInstance().join(names, delim);
    }
  }

  /**
   * Gets the owners for the token.
   *
   * @param token The token to get the owners for.
   * @param delim the delimiter for the list.
   * @return a string list of the token owners.
   */
  public String getOwners(Token token, String delim) {
    String[] owners = new String[token.getOwners().size()];
    token.getOwners().toArray(owners);
    if ("json".endsWith(delim)) {
      return JSONArray.fromObject(owners).toString();
    } else {
      return StringFunctions.getInstance().join(owners, delim);
    }
  }

  /**
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  private void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
  }

  /**
   * Checks if the object stored at the specified index is a BigDecimal and returns it if that is
   * the case. It is not safe to call this method without first checking the list size (possibly by
   * using <code>checkNumberOfParameters</code>).
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param index the index to find the BigDecimal at
   * @return the parameter cast to BigDecimal
   * @throws ParserException if the parameter did not contain a BigDecimal
   * @see checkNumberOfParameters
   */
  private BigDecimal getBigDecimalFromParam(String functionName, List<Object> parameters, int index)
      throws ParserException {
    Object param = parameters.get(index);
    if (param instanceof BigDecimal) {
      return (BigDecimal) param;
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN", functionName, index, param.toString()));
    }
  }

  /**
   * Gets the token from the specified index or returns the token in context. This method will check
   * the list size before trying to retrieve the token so it is safe to use for functions that have
   * the token as a optional argument.
   *
   * @param res the variable resolver
   * @param functionName The function name (used for generating exception messages).
   * @param param The parameters for the function.
   * @param index The index to find the token at.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  private Token getTokenFromParam(
      MapToolVariableResolver res, String functionName, List<Object> param, int index)
      throws ParserException {
    Token token;
    if (param.size() > index) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", functionName));
      }
      token = FindTokenFunctions.findToken(param.get(index).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", functionName, param.get(index).toString()));
      }
    } else {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
      }
    }
    return token;
  }
}
