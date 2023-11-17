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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.gamedata.MTScriptDataConversion;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.data.LibraryData;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenPropertyFunctions extends AbstractFunction {
  private static final TokenPropertyFunctions instance = new TokenPropertyFunctions();

  private TokenPropertyFunctions() {
    super(
        0,
        5,
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
        "resetSize",
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
        "isFlippedX",
        "isFlippedY",
        "isFlippedIso",
        "flipTokenX",
        "flipTokenY",
        "flipTokenIso",
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
        "getTokenLayoutProps",
        "setTokenLayoutProps",
        "setTokenSnapToGrid",
        "getAllowsURIAccess",
        "setAllowsURIAccess",
        "getDefaultPropertyType",
        "getPropertyDisplayName");
  }

  public static TokenPropertyFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    /*
     * String type = getPropertyType(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getPropertyType")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.getPropertyType();
    }

    /*
     * String empty = setPropertyType(String propTypeName, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setPropertyType")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setPropertyType, parameters.get(0).toString());
      return "";
    }

    /*
     * String names = getPropertyNames(String delim: ",", String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getPropertyNames")
        || functionName.equalsIgnoreCase("getPropertyNamesRaw")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      String pattern = ".*";
      return getPropertyNames(
          token, delim, pattern, functionName.equalsIgnoreCase("getPropertyNamesRaw"));
    }

    /*
     * String names = getMatchingProperties(String pattern, String delim: ",", String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getMatchingProperties")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      String pattern = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      return getPropertyNames(token, delim, pattern, false);
    }

    /*
     * String names = getAllPropertyNames(String propType: "", String delim: ",")
     */
    if (functionName.equalsIgnoreCase("getAllPropertyNames")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      if (parameters.size() < 1) {
        return getAllPropertyNames(null, ",");
      } else {
        return getAllPropertyNames(
            parameters.get(0).toString(),
            parameters.size() > 1 ? parameters.get(1).toString() : ",");
      }
    }

    /*
     * Number zeroOne = hasProperty(String propName, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("hasProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return hasProperty(token, parameters.get(0).toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isNPC(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isNPC")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.getType() == Token.Type.NPC ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isPC(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isPC")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.getType() == Token.Type.PC ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String empty = setPC(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setPC")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setPC);
      return "";
    }

    /*
     * String empty = setNPC(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setNPC")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setNPC);
      return "";
    }

    /*
     * String layer = getLayer(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getLayer")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.getLayer().name();
    }

    /*
     * String layer = setLayer(String layer, String tokenId: currentToken(), boolean forceShape: true, string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setLayer")) {
      boolean forceShape = true;
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      if (parameters.size() > 2) {
        forceShape = !BigDecimal.ZERO.equals(parameters.get(2));
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 3);
      String layer = setLayer(token, parameters.get(0).toString(), forceShape);
      return layer;
    }

    /*
     * String size = getSize(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getSize")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return getSize(token);
    }

    /*
     * String size = setSize(String size, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setSize")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);

      return setSize(token, parameters.get(0).toString());
    }

    if (functionName.equalsIgnoreCase("resetSize")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      resetSize(token);

      return "";
    }

    /*
     * String owners = getOwners(String delim: ",", String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getOwners")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return getOwners(token, parameters.size() > 0 ? parameters.get(0).toString() : ",");
    }

    /*
     * Number zeroOne = isOwnedByAll(String tokenId: currentToken())
     */
    if (functionName.equalsIgnoreCase("isOwnedByAll")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.isOwnedByAll() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isOwner(String player: self, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isOwner")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      if (parameters.size() > 0) {
        return token.isOwner(parameters.get(0).toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
      }
      return token.isOwner(MapTool.getPlayer().getName()) ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String empty = resetProperty(String propName, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("resetProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      String property = parameters.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);

      MapTool.serverCommand().updateTokenProperty(token, Token.Update.resetProperty, property);
      return "";
    }

    /*
     * String empty = setProperty(String propName, String value, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 4);
      String property = parameters.get(0).toString();
      String value = parameters.get(1).toString();

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setProperty, property, value);
      return "";
    }

    /*
     * {String|Number} value = getRawProperty(String propName, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getRawProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
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
     * {String|Number} value = getProperty(String propName, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
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
     * Number zeroOne = isPropertyEmpty(String propName, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isPropertyEmpty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return token.getProperty(parameters.get(0).toString()) == null
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }

    /*
     * pre 1.3.b64 only took a single parameter
     *
     * Number zeroOne = getPropertyDefault(String propName, String propType: currentToken().getPropertyType())
     */
    if (functionName.equalsIgnoreCase("getPropertyDefault")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String name = parameters.get(0).toString();

      String propType;
      if (parameters.size() > 1) {
        propType = parameters.get(1).toString();
      } else {
        Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, -1, -1);
        propType = token.getPropertyType();
      }
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
     * String notes = getGMNotes(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getGMNotes")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      String notes = token.getGMNotes();
      return notes != null ? notes : "";
    }

    /*
     * String notes = setGMNotes(String notes, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setGMNotes")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      String gmNotes = parameters.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setGMNotes, gmNotes);
      return token.getGMNotes();
    }

    /*
     * String notes = getNotes(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getNotes")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      String notes = token.getNotes();
      return notes != null ? notes : "";
    }

    /*
     * String notes = setNotes(String notes, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setNotes")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      String notes = parameters.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setNotes, notes);
      return token.getNotes();
    }

    /*
     * String empty = bringToFront(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("bringToFront")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      Zone zone = token.getZoneRenderer().getZone();
      int zOrder = zone.getLargestZOrder() + 1;
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setZOrder, zOrder);
      return BigDecimal.valueOf(token.getZOrder());
    }

    /*
     * String empty = sendToBack(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("sendToBack")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      Zone zone = token.getZoneRenderer().getZone();
      int zOrder = zone.getSmallestZOrder() - 1;
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setZOrder, zOrder);
      return BigDecimal.valueOf(token.getZOrder());
    }

    /*
     * String value = getLibProperty(String propName, String tokenId: macroSource)
     */
    if (functionName.equalsIgnoreCase("getLibProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String propertyName = parameters.get(0).toString();
      String location;
      if (parameters.size() > 1) {
        location = parameters.get(1).toString();
      } else {
        location = MapTool.getParser().getMacroSource();
      }

      String libName;
      if (location.toLowerCase().startsWith("lib:")) {
        libName = location.substring(4);
      } else {
        libName = location;
      }

      try {
        var library =
            new LibraryManager()
                .getLibrary(libName)
                .orElseThrow(
                    () ->
                        new ParserException(
                            I18N.getText(
                                "macro.function.tokenProperty.unknownLibToken",
                                functionName,
                                libName)));
        return library
            .getLibraryData()
            .thenCompose(libData -> libData.getValue(propertyName))
            .thenApply(data -> new MTScriptDataConversion().convertToMTScriptType(data))
            .get();
      } catch (ExecutionException | InterruptedException e) {
        throw new ParserException(e.getCause());
      }
    }

    /*
     * String empty = setLibProperty(String propName, String value, String tokenId: macroSource)
     */
    if (functionName.equalsIgnoreCase("setLibProperty")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 3);
      String property = parameters.get(0).toString();
      Object value = parameters.get(1);

      String location;
      if (parameters.size() > 2) {
        location = parameters.get(2).toString();
      } else {
        location = MapTool.getParser().getMacroSource();
      }

      String libName;
      if (location.toLowerCase().startsWith("lib:")) {
        libName = location.substring(4);
      } else {
        libName = location;
      }

      var library =
          new LibraryManager()
              .getLibrary(libName)
              .orElseThrow(
                  () ->
                      new ParserException(
                          I18N.getText(
                              "macro.function.tokenProperty.unknownLibToken",
                              functionName,
                              libName)));
      library
          .getLibraryData()
          .thenCompose(
              libData ->
                  libData.setData(
                      new MTScriptDataConversion().parseMTScriptString(property, value)));
      return "";
    }

    /*
     * String names = getLibPropertyNames(String tokenId: {macroSource | "*" | "this"}, String delim: ",")
     */
    if (functionName.equalsIgnoreCase("getLibPropertyNames")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      String location;
      if (parameters.size() > 0) {
        location = parameters.get(0).toString();
        if (location.equals("*") || location.equalsIgnoreCase("this")) {
          location = MapTool.getParser().getMacroSource();
        }
      } else {
        location = MapTool.getParser().getMacroSource();
      }

      String libName;
      if (location.toLowerCase().startsWith("lib:")) {
        libName = location.substring(4);
      } else {
        libName = location;
      }
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      return getMatchingLibProperties(libName, delim, ".*", functionName);
    }

    /*
     * String names = getMatchingLibProperties(String pattern, String tokenId: {macroSource | "*" | "this"}, String delim: ",")
     */
    if (functionName.equalsIgnoreCase("getMatchingLibProperties")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
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

      String libName;
      if (location.toLowerCase().startsWith("lib:")) {
        libName = location.substring(4);
      } else {
        libName = location;
      }

      String delim = parameters.size() > 2 ? parameters.get(2).toString() : ",";
      return getMatchingLibProperties(libName, delim, pattern, functionName);
    }

    /*
     * Number facing = getTokenFacing(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getTokenFacing")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      if (token.getFacing() == null) {
        return ""; // XXX Should be -1 instead of a string?
      }
      return BigDecimal.valueOf(token.getFacing());
    }

    /*
     * Number degrees = getTokenRotation(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getTokenRotation")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);

      return BigDecimal.valueOf(token.getFacingInDegrees());
    }

    /*
     * String empty = setTokenFacing(Number facing, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setTokenFacing")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      BigDecimal facing = getBigDecimalFromParam(functionName, parameters, 0);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setFacing, facing.intValue());
      return "";
    }

    /*
     * String empty = removeTokenFacing(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("removeTokenFacing")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.removeFacing);
      return "";
    }

    /*
     * Number zeroOne = isSnapToGrid(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isSnapToGrid")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.isSnapToGrid() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isFlippedX(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isFlippedX")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.isFlippedX() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isFlippedY(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isFlippedY")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.isFlippedY() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Number zeroOne = isFlippedIso(String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("isFlippedIso")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.isFlippedIso() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String empty = setOwner(String playerName | JSONArray playerNames, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setOwner")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      boolean trusted = MapTool.getParser().isMacroTrusted();
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();
      // Remove current owners, but if this macro is untrusted and the current player is an owner,
      // keep the
      // ownership there.
      String myself = MapTool.getPlayer().getName();
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.clearAllOwners);
      String s = parameters.get(0).toString();
      if (StringUtil.isEmpty(s)) {
        // Do nothing when trusted, since all ownership should be turned off for an empty string
        // used in such a macro.
      } else {
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(parameters.get(0));
        if (json != null && json.isJsonArray()) {
          for (JsonElement ele : json.getAsJsonArray()) {
            MapTool.serverCommand()
                .updateTokenProperty(token, Token.Update.addOwner, ele.getAsString());
          }
        } else {
          MapTool.serverCommand().updateTokenProperty(token, Token.Update.addOwner, s);
        }
      }
      if (!trusted) // If not trusted we must have been in the owner list -- keep us there.
      {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.addOwner, myself);
      }
      return "";
    }

    /*
     * String empty = setOwnedByAll(0|1, String tokenId: currentToken(), string mapName: current map)
     */
    if (functionName.equalsIgnoreCase("setOwnedByAll")) {
      // If not trusted, do nothing and return -1 result
      if (!MapTool.getParser().isMacroTrusted()) return -1;

      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      BigDecimal ownedByAll = getBigDecimalFromParam(functionName, parameters, 0);

      if (ownedByAll.compareTo(BigDecimal.ZERO) == 0) {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.setOwnedByAll, false);
      } else {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.setOwnedByAll, true);
      }
      return token.isOwnedByAll() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String newShape = getTokenShape(String tokenId: currentToken(), string mapName: current map)
     *
     * See Token.TokenShape for return values. Currently "TOP_DOWN", "CIRCLE", "SQUARE", and "FIGURE".
     */
    if (functionName.equalsIgnoreCase("getTokenShape")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.getShape().name();
    }

    /*
     * String newShape = setTokenShape(String shape, String tokenId: currentToken(), string mapName: current map)
     *
     * See Token.TokenShape for shape values. Currently "top down", "top_down", "circle", "square", and "figure".
     */
    if (functionName.equalsIgnoreCase("setTokenShape")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      Token.TokenShape newShape =
          Token.TokenShape.valueOf(
              parameters.get(0).toString().toUpperCase().trim().replace(" ", "_"));
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setShape, newShape.name());
      return token.getShape().name();
    }

    /*
     * String newShape = getTokenWidth(String tokenId: currentToken(), string mapName: current map)
     *
     * String newShape = getTokenHeight(String tokenId: currentToken(), string mapName: current map)
     *
     * Returns pixel width/height for a given token. Useful for free size tokens.
     */
    if (functionName.equalsIgnoreCase("getTokenNativeWidth")
        || functionName.equalsIgnoreCase("getTokenNativeHeight")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);

      if (functionName.equalsIgnoreCase("getTokenNativeWidth")) {
        return BigDecimal.valueOf(token.getWidth());
      } else { // it wasn't 'getTokenWidth' which means functionName equals 'getTokenHeight'
        return BigDecimal.valueOf(token.getHeight());
      }
    }

    /*
     * String newShape = getTokenWidth(String tokenId: currentToken(), string mapName: current map)
     *
     * String newShape = getTokenHeight(String tokenId: currentToken(), string mapName: current map)
     *
     * Returns pixel width/height for a given token. Useful for free size tokens.
     */
    if (functionName.equalsIgnoreCase("getTokenWidth")
        || functionName.equalsIgnoreCase("getTokenHeight")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();

      // Get the pixel width or height of a given token
      Rectangle tokenBounds = token.getBounds(zone);

      if (functionName.equalsIgnoreCase("getTokenWidth")) {
        return BigDecimal.valueOf(tokenBounds.width);
      } else { // it wasn't 'getTokenWidth' which means functionName equals 'getTokenHeight'
        return BigDecimal.valueOf(tokenBounds.height);
      }
    }

    /*
     * String newWidth   = setTokenWidth(String width, String tokenId: currentToken(), string mapName: current map)
     *
     * String newHeight  = setTokenHeight(String height, String tokenId: currentToken(), string mapName: current map)
     *
     * Sets the width/height for a given token. Useful for free size tokens.
     */
    if (functionName.equalsIgnoreCase("setTokenWidth")
        || functionName.equalsIgnoreCase("setTokenHeight")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      ZoneRenderer zoneR = token.getZoneRenderer();
      Zone zone = zoneR.getZone();

      double magnitude = getBigDecimalFromParam(functionName, parameters, 0).doubleValue();
      Rectangle tokenBounds = token.getBounds(zone);

      double oldWidth = tokenBounds.width;
      double oldHeight = tokenBounds.height;
      double newScaleX;
      double newScaleY;
      if (functionName.equalsIgnoreCase("setTokenWidth")) {
        newScaleX = magnitude / token.getWidth();
        newScaleY = oldHeight / token.getHeight();
      } else { // it wasn't 'setTokenWidth' which means functionName equals 'setTokenHeight'
        newScaleX = oldWidth / token.getWidth();
        newScaleY = magnitude / token.getHeight();
      }
      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setScaleXY, newScaleX, newScaleY);
      return magnitude;
    }

    /*
     * Number newSnapToGrid   = setTokenSnapToGrid(Bool snapToGrid, String tokenId: currentToken(), string mapName: current map)
     *
     * Sets whether the token should snap to the grid or not
     */
    if (functionName.equalsIgnoreCase("setTokenSnapToGrid")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      Boolean toGrid = FunctionUtil.getBooleanValue(parameters.get(0));
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setSnapToGrid, toGrid);
      return token.isSnapToGrid() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Boolean flippedX   = flipTokenX(String tokenId: currentToken(), string mapName: current map)
     *
     * Toggles Flipped X setting
     */
    if (functionName.equalsIgnoreCase("flipTokenX")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipX);
      return token.isFlippedX() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Boolean flippedY   = flipTokenY(String tokenId: currentToken(), string mapName: current map)
     *
     * Toggles Flipped Y setting
     */
    if (functionName.equalsIgnoreCase("flipTokenY")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipY);
      return token.isFlippedY() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * Boolean flippedIso   = flipTokenIso(String tokenId: currentToken(), string mapName: current map)
     *
     * Toggles Flipped Iso setting
     */
    if (functionName.equalsIgnoreCase("flipTokenIso")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipIso);
      return token.isFlippedIso() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * String/JsonObject = getTokenLayoutProps(String delim: ',', String tokenId: currentToken(), String mapName: current map)
     */
    if (functionName.equalsIgnoreCase("getTokenLayoutProps")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);

      Double scale = token.getSizeScale();
      int xOffset = token.getAnchorX();
      int yOffset = token.getAnchorY();

      if ("json".equals(delim)) {
        JsonObject jarr = new JsonObject();
        jarr.addProperty("scale", scale);
        jarr.addProperty("xOffset", xOffset);
        jarr.addProperty("yOffset", yOffset);
        return jarr;
      } else {
        return "scale=" + scale + delim + "xOffset=" + xOffset + delim + "yOffset=" + yOffset;
      }
    }

    /*
     * setTokenLayoutProps(scale, xOffset, yOffset, token: currentToken(), mapName = current map)
     */
    if (functionName.equalsIgnoreCase("setTokenLayoutProps")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 3, 5);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 3, 4);

      double scale;
      if (!"".equals(parameters.get(0))) {
        scale = FunctionUtil.paramAsDouble(functionName, parameters, 0, false);
      } else {
        scale = token.getSizeScale();
      }
      int xOffset;
      if (!"".equals(parameters.get(1))) {
        xOffset = FunctionUtil.paramAsInteger(functionName, parameters, 1, false);
      } else {
        xOffset = token.getAnchorX();
      }
      int yOffset;
      if (!"".equals(parameters.get(2))) {
        yOffset = FunctionUtil.paramAsInteger(functionName, parameters, 2, false);
      } else {
        yOffset = token.getAnchorY();
      }

      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setLayout, scale, xOffset, yOffset);
      return "";
    }

    /*
     * getAllowsURLAccess(token: currentToken(), mapName = current map)
     */
    if (functionName.equalsIgnoreCase("getAllowsURIAccess")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);
      FunctionUtil.blockUntrustedMacro(functionName);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return token.getAllowURIAccess() ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /*
     * setAllowsURLAccess(token: currentToken(), mapName = current map)
     */
    if (functionName.equalsIgnoreCase("setAllowsURIAccess")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      FunctionUtil.blockUntrustedMacro(functionName);
      BigDecimal allowURIAccess = getBigDecimalFromParam(functionName, parameters, 0);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      if (!Token.isValidLibTokenName(token.getName())) {
        throw new ParserException(
            I18N.getText("macro.setAllowsURIAccess.notLibToken", token.getName()));
      }
      var libraryManager = new LibraryManager();
      String name = token.getName().substring(4);
      if (libraryManager.usesReservedPrefix(name)) {
        throw new ParserException(
            I18N.getText(
                "macro.setAllowsURIAccess.reservedPrefix", libraryManager.getReservedPrefix(name)));
      } else if (libraryManager.usesReservedName(name)) {
        throw new ParserException(
            I18N.getText("macro.setAllowsURIAccess.reserved", token.getName()));
      }
      token.setAllowURIAccess(!allowURIAccess.equals(BigDecimal.ZERO));
      return "";
    }

    /*
     * getDefaultPropertyType
     */
    if (functionName.equalsIgnoreCase("getDefaultPropertyType")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      FunctionUtil.blockUntrustedMacro(functionName);
      return MapTool.getCampaign().getCampaignProperties().getDefaultTokenPropertyType();
    }

    /*
     * getPropertyDisplayName(Token Type Name, Property Name)
     */
    if (functionName.equalsIgnoreCase("getPropertyDisplayName")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      var props = MapTool.getCampaign().getTokenTypeMap().get(parameters.get(0).toString());
      if (props == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownPropertyType",
                functionName,
                parameters.get(0).toString()));
      }
      final var propName = parameters.get(1).toString();
      for (var prop : props) {
        if (prop.getName().equals(propName)) {
          return prop.hasDisplayName() ? prop.getDisplayName() : prop.getName();
        }
      }
      throw new ParserException(
          I18N.getText(
              "macro.function.general.unknownProperty",
              functionName,
              parameters.get(1).toString(),
              parameters.get(0).toString()));
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Gets the size of the token.
   *
   * @param token The token to get the size of.
   * @return the size of the token.
   */
  private String getSize(Token token) {
    Grid grid = token.getZoneRenderer().getZone().getGrid();
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
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setSnapToScale, false);
      return getSize(token);
    }
    Grid grid = token.getZoneRenderer().getZone().getGrid();
    for (TokenFootprint footprint : grid.getFootprints()) {
      if (footprint.getName().equalsIgnoreCase(size)) {
        MapTool.serverCommand()
            .updateTokenProperty(token, Token.Update.setFootprint, grid, footprint);
        return getSize(token);
      }
    }
    throw new ParserException(
        I18N.getText("macro.function.tokenProperty.invalidSize", "setSize", size));
  }

  /**
   * Resets the size of the token.
   *
   * @param token The token to reset the size of.
   */
  private void resetSize(Token token) {
    Grid grid = token.getZoneRenderer().getZone().getGrid();
    TokenFootprint footprint = grid.getDefaultFootprint();
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setFootprint, grid, footprint);
  }

  /**
   * Get the Zone.Layer element corresponding to a layerName
   *
   * @param layerName the String of the name of the layer
   * @throws ParserException if the layer name is invalid.
   * @return the Zone.Layer corresponding to the layerName
   */
  public static Zone.Layer getLayer(String layerName) throws ParserException {
    Zone.Layer layer;
    try {
      layer = Zone.Layer.getByName(layerName.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ParserException(
          I18N.getText("macro.function.tokenProperty.unknownLayer", "setLayer", layerName));
    }

    return layer;
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
  private static String setLayer(Token token, String layerName, boolean forceShape)
      throws ParserException {
    Zone.Layer layer = getLayer(layerName);
    token.setLayer(layer);

    if (forceShape) {
      final var shape = token.guessAndSetShape();
      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.setLayerShape, layer.name(), shape.name());
    } else {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setLayer, layer.name());
    }

    return layer.name();
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

    return !StringUtil.isEmpty(val.toString());
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
    ArrayList<String> namesList = new ArrayList<String>();
    if (type == null || type.length() == 0 || type.equals("*")) {
      Map<String, List<TokenProperty>> pmap =
          MapTool.getCampaign().getCampaignProperties().getTokenTypeMap();

      for (Entry<String, List<TokenProperty>> entry : pmap.entrySet()) {
        for (TokenProperty tp : entry.getValue()) {
          namesList.add(tp.getName());
        }
      }
      if ("json".equals(delim)) {
        JsonArray jarr = new JsonArray();
        namesList.forEach(jarr::add);
        return jarr.toString();
      } else {
        return StringFunctions.getInstance().join(namesList, delim);
      }
    } else {
      List<TokenProperty> props =
          MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(type);
      // If property type not found return an empty string or JSON array
      if (props != null) {
        for (TokenProperty tp : props) {
          namesList.add(tp.getName());
        }
      }
      if ("json".equals(delim)) {
        JsonArray jarr = new JsonArray();
        namesList.forEach(n -> jarr.add(new JsonPrimitive(n)));
        return jarr.toString();
      } else {
        return StringFunctions.getInstance().join(namesList, delim);
      }
    }
  }

  /**
   * Returns the matching library property names for the specified library.
   *
   * @param libName the name of the library to get the property names for.
   * @param delim the list delimiter, or "json" for a json array.
   * @param pattern the pattern to match against the property names.
   * @param functionName the name of the MTS function calling this method.
   * @return the list of matching property names.
   * @throws ParserException if the library name is invalid, or another error occurs.
   */
  private Object getMatchingLibProperties(
      String libName, String delim, String pattern, String functionName) throws ParserException {
    try {
      var library =
          new LibraryManager()
              .getLibrary(libName)
              .orElseThrow(
                  () ->
                      new ParserException(
                          I18N.getText(
                              "macro.function.tokenProperty.unknownLibToken",
                              functionName,
                              libName)));
      return library
          .getLibraryData()
          .thenCompose(LibraryData::getAllKeys)
          .thenApply(
              keys -> {
                if (delim.equalsIgnoreCase("json")) {
                  var jarr = new JsonArray();
                  keys.stream().filter(k -> k.matches(pattern)).forEach(jarr::add);
                  return jarr;
                } else {
                  return keys.stream()
                      .filter(k -> k.matches(pattern))
                      .collect(Collectors.joining(delim));
                }
              })
          .get();
    } catch (ExecutionException | InterruptedException e) {
      throw new ParserException(e.getCause());
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
      JsonArray jarr = new JsonArray();
      Arrays.stream(names).forEach(n -> jarr.add(new JsonPrimitive(n)));
      return jarr.toString();
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
    var owners = new ArrayList<>(token.getOwners());
    if ("json".endsWith(delim)) {
      JsonArray jarr = new JsonArray();
      owners.forEach(o -> jarr.add(new JsonPrimitive(o)));
      return jarr.toString();
    } else {
      return StringFunctions.getInstance().join(owners, delim);
    }
  }

  /**
   * Checks if the object stored at the specified index is a BigDecimal and returns it if that is
   * the case. It is not safe to call this method without first checking the list size (possibly by
   * using <code>FunctionUtil.checkNumberParam</code>).
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param index the index to find the BigDecimal at
   * @return the parameter cast to BigDecimal
   * @throws ParserException if the parameter did not contain a BigDecimal
   * @see FunctionUtil#checkNumberParam
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
}
