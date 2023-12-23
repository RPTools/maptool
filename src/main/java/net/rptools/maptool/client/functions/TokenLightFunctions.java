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
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenLightFunctions extends AbstractFunction {
  private static final TokenLightFunctions instance = new TokenLightFunctions();

  private static final String TOKEN_CATEGORY = "$unique";

  private TokenLightFunctions() {
    super(
        0,
        5,
        "hasLightSource",
        "clearLights",
        "setLight",
        "getLights",
        "createUniqueLightSource",
        "updateUniqueLightSource",
        "deleteUniqueLightSource",
        "getUniqueLightSource",
        "getUniqueLightSources",
        "getUniqueLightSourceNames");
  }

  public static TokenLightFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("hasLightSource")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 4);

      String type = (parameters.size() > 0) ? parameters.get(0).toString() : "*";
      String name = (parameters.size() > 1) ? parameters.get(1).toString() : "*";
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return hasLightSource(token, type, name) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equalsIgnoreCase("clearLights")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.clearLightSources);
      return "";
    }
    if (functionName.equalsIgnoreCase("setLight")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 3, 5);

      String type = parameters.get(0).toString();
      String name = parameters.get(1).toString();
      BigDecimal value = FunctionUtil.paramAsBigDecimal(functionName, parameters, 2, false);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 3, 4);
      return setLight(token, type, name, value);
    }
    if (functionName.equalsIgnoreCase("getLights")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 4);

      String type = parameters.size() > 0 ? parameters.get(0).toString() : "*";
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return getLights(token, type, delim);
    }
    if (functionName.equalsIgnoreCase("createUniqueLightSource")) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);

      JsonObject lightSource = FunctionUtil.paramAsJsonObject(functionName, parameters, 0);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return createUniqueLightSource(lightSource, token, false).getName();
    }
    if (functionName.equalsIgnoreCase("updateUniqueLightSource")) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);

      JsonObject lightSource = FunctionUtil.paramAsJsonObject(functionName, parameters, 0);
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return createUniqueLightSource(lightSource, token, true).getName();
    }
    if (functionName.equalsIgnoreCase("deleteUniqueLightSource")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);

      String name = parameters.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      deleteUniqueLightSource(name, token);
      return "";
    }
    if (functionName.equalsIgnoreCase("getUniqueLightSource")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);

      String name = parameters.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return Objects.requireNonNullElse(getUniqueLightSource(name, token), "");
    }
    if (functionName.equalsIgnoreCase("getUniqueLightSources")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return getUniqueLightSources(token);
    }
    if (functionName.equalsIgnoreCase("getUniqueLightSourceNames")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 2);

      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 0, 1);
      return getUniqueLightSourceNames(token);
    }
    return null;
  }

  /**
   * Gets the names of the light sources that are on.
   *
   * @param token The token to get the light sources for.
   * @param category The category to get the light sources for. If "*" then the light sources for
   *     all categories will be returned. If "$unique" then the light sources defined on the token
   *     will be returned.
   * @param delim the delimiter for the list.
   * @return a string list containing the lights that are on.
   * @throws ParserException if the light type can't be found.
   */
  private static String getLights(Token token, String category, String delim)
      throws ParserException {
    ArrayList<String> lightList = new ArrayList<String>();
    Map<String, Map<GUID, LightSource>> lightSourcesMap =
        MapTool.getCampaign().getLightSourcesMap();

    if (category.equals("*")) {
      // Look up on both token and campaign.
      for (LightSource ls : token.getUniqueLightSources()) {
        if (token.hasLightSource(ls)) {
          lightList.add(ls.getName());
        }
      }
      for (Map<GUID, LightSource> lsMap : lightSourcesMap.values()) {
        for (LightSource ls : lsMap.values()) {
          if (token.hasLightSource(ls)) {
            lightList.add(ls.getName());
          }
        }
      }
    } else if (TOKEN_CATEGORY.equals(category)) {
      for (LightSource ls : token.getUniqueLightSources()) {
        if (token.hasLightSource(ls)) {
          lightList.add(ls.getName());
        }
      }
    } else if (lightSourcesMap.containsKey(category)) {
      for (LightSource ls : lightSourcesMap.get(category).values()) {
        if (token.hasLightSource(ls)) {
          lightList.add(ls.getName());
        }
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.tokenLight.unknownLightType", "getLights", category));
    }

    if ("json".equals(delim)) {
      JsonArray jarr = new JsonArray();
      lightList.forEach(l -> jarr.add(new JsonPrimitive(l)));
      return jarr.toString();
    } else {
      return StringFunctions.getInstance().join(lightList, delim);
    }
  }

  /**
   * Sets the light value for a token.
   *
   * @param token the token to set the light for.
   * @param category the category of the light source. Use "$unique" for light sources defined on
   *     the token.
   * @param name the name of the light source.
   * @param val the value to set for the light source, 0 for off non 0 for on.
   * @return 0 if the light was not found, otherwise 1;
   * @throws ParserException if the light type can't be found.
   */
  private static BigDecimal setLight(Token token, String category, String name, BigDecimal val)
      throws ParserException {
    boolean found = false;
    Map<String, Map<GUID, LightSource>> lightSourcesMap =
        MapTool.getCampaign().getLightSourcesMap();

    Iterable<LightSource> sources;
    if (TOKEN_CATEGORY.equals(category)) {
      sources = token.getUniqueLightSources();
    } else if (lightSourcesMap.containsKey(category)) {
      sources = lightSourcesMap.get(category).values();
    } else {
      throw new ParserException(
          I18N.getText("macro.function.tokenLight.unknownLightType", "setLights", category));
    }

    final var updateAction =
        BigDecimal.ZERO.equals(val) ? Token.Update.removeLightSource : Token.Update.addLightSource;
    for (LightSource ls : sources) {
      if (name.equals(ls.getName())) {
        found = true;
        MapTool.serverCommand().updateTokenProperty(token, updateAction, ls);
      }
    }

    return found ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Checks to see if the token has a light source. The token is checked to see if it has a light
   * source with the name in the second parameter from the category in the first parameter. A "*"
   * for category indicates all categories are checked; a "*" for name indicates all names are
   * checked. The "$unique" category indicates that only light sources defined on the token are
   * checked.
   *
   * @param token the token to check.
   * @param category the type of light to check.
   * @param name the name of the light to check.
   * @return true if the token has the light source.
   * @throws ParserException if the light type can't be found.
   */
  public static boolean hasLightSource(Token token, String category, String name)
      throws ParserException {
    if ("*".equals(category) && "*".equals(name)) {
      return token.hasLightSources();
    }

    Map<String, Map<GUID, LightSource>> lightSourcesMap =
        MapTool.getCampaign().getLightSourcesMap();

    if ("*".equals(category)) {
      // Look up on both token and campaign.
      for (LightSource ls : token.getUniqueLightSources()) {
        if (ls.getName().equals(name) && token.hasLightSource(ls)) {
          return true;
        }
      }
      for (Map<GUID, LightSource> lsMap : lightSourcesMap.values()) {
        for (LightSource ls : lsMap.values()) {
          if (ls.getName().equals(name) && token.hasLightSource(ls)) {
            return true;
          }
        }
      }
    } else if (TOKEN_CATEGORY.equals(category)) {
      for (LightSource ls : token.getUniqueLightSources()) {
        if ((ls.getName().equals(name) || "*".equals(name)) && token.hasLightSource(ls)) {
          return true;
        }
      }
    } else if (lightSourcesMap.containsKey(category)) {
      for (LightSource ls : lightSourcesMap.get(category).values()) {
        if ((ls.getName().equals(name) || "*".equals(name)) && token.hasLightSource(ls)) {
          return true;
        }
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.tokenLight.unknownLightType", "hasLightSource", category));
    }

    return false;
  }

  private static LightSource createUniqueLightSource(
      JsonObject lightSourceDef, Token token, boolean isUpdate) throws ParserException {
    if (!lightSourceDef.has("name")) {
      throw new ParserException(I18N.getText("The light source must have a name."));
    }
    final String name = lightSourceDef.get("name").getAsString();

    // Modifications require the light source to exist. Creation requires it to not exists.
    final Optional<LightSource> existingSource =
        token.getUniqueLightSources().stream()
            .filter(source -> name.equals(source.getName()))
            .findFirst();
    if (isUpdate && existingSource.isEmpty()) {
      throw new ParserException(
          I18N.getText(
              "Light source %s is not defined for token %s", name, token.getId().toString()));
    }
    if (!isUpdate && existingSource.isPresent()) {
      throw new ParserException(
          I18N.getText(
              "Light source %s is already defined for token %s", name, token.getId().toString()));
    }

    final LightSource.Type type =
        lightSourceDef.has("type")
            ? LightSource.Type.valueOf(lightSourceDef.get("type").getAsString().toUpperCase())
            : LightSource.Type.NORMAL;
    final boolean scaleWithToken =
        lightSourceDef.has("scale") ? lightSourceDef.get("scale").getAsBoolean() : false;
    final boolean ignoreVBL =
        lightSourceDef.has("ignores-vbl")
            ? lightSourceDef.get("ignores-vbl").getAsBoolean()
            : false;
    final JsonArray lightDefs =
        lightSourceDef.has("lights") ? lightSourceDef.getAsJsonArray("lights") : new JsonArray();

    final var lights = new ArrayList<Light>();
    for (final var light : lightDefs) {
      lights.add(parseLightJson(light.getAsJsonObject(), type));
    }

    final var lightSource =
        LightSource.createRegular(
            name,
            existingSource.isPresent() ? existingSource.get().getId() : new GUID(),
            type,
            scaleWithToken,
            ignoreVBL,
            lights);
    token.addUniqueLightSource(lightSource);
    MapTool.serverCommand()
        .updateTokenProperty(token, Token.Update.createUniqueLightSource, lightSource);
    return lightSource;
  }

  private static void deleteUniqueLightSource(String name, Token token) {
    final var sourcesToRemove = new ArrayList<LightSource>();
    for (final LightSource source : token.getUniqueLightSources()) {
      if (name.equals(source.getName())) {
        sourcesToRemove.add(source);
      }
    }

    for (final LightSource source : sourcesToRemove) {
      token.removeUniqueLightSource(source.getId());
      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.deleteUniqueLightSource, source);
    }
  }

  private static JsonObject getUniqueLightSource(String name, Token token) {
    for (final LightSource source : token.getUniqueLightSources()) {
      if (name.equals(source.getName())) {
        return lightSourceToJson(source);
      }
    }

    return null;
  }

  private static JsonArray getUniqueLightSources(Token token) {
    final var result = new JsonArray();

    for (final LightSource source : token.getUniqueLightSources()) {
      result.add(lightSourceToJson(source));
    }

    return result;
  }

  private static JsonArray getUniqueLightSourceNames(Token token) {
    final var result = new JsonArray();

    for (final LightSource source : token.getUniqueLightSources()) {
      result.add(source.getName());
    }

    return result;
  }

  private static Light parseLightJson(JsonObject lightDef, LightSource.Type lightSourceType)
      throws ParserException {
    if (!lightDef.has("range")) {
      throw new ParserException(I18N.getText("A range must be provided for each light"));
    }
    final var range = lightDef.get("range").getAsDouble();

    final var shape =
        lightDef.has("shape")
            ? ShapeType.valueOf(lightDef.get("shape").getAsString())
            : ShapeType.CIRCLE;
    // Cones permit the fields arc and offset, but no other shape accepts them.
    if (shape != ShapeType.CONE && shape != ShapeType.BEAM) {
      if (lightDef.has("offset")) {
        throw new ParserException(
            I18N.getText("Facing offset provided but the shape is not a cone"));
      }
      if (lightDef.has("arc")) {
        throw new ParserException(I18N.getText("Arc provided but the shape is not a cone"));
      }
    }
    final var offset = lightDef.has("offset") ? lightDef.get("offset").getAsDouble() : 0;
    final var arc = lightDef.has("arc") ? lightDef.get("arc").getAsDouble() : 0;

    // Auras permit the gmOnly and ownerOnly flags, but no other type accepts them.
    if (lightSourceType != LightSource.Type.AURA) {
      if (lightDef.has("gmOnly")) {
        throw new ParserException(I18N.getText("gmOnly flag provided but the type is not an aura"));
      }
      if (lightDef.has("ownerOnly")) {
        throw new ParserException(
            I18N.getText("ownerOnly flag provided but the type is not an aura"));
      }
    }
    final var gmOnly = lightDef.has("gmOnly") ? !lightDef.get("gmOnly").getAsBoolean() : false;
    final var ownerOnly =
        lightDef.has("ownerOnly") ? !lightDef.get("ownerOnly").getAsBoolean() : false;

    final DrawableColorPaint colorPaint;
    if (lightDef.has("color")) {
      var colorString = lightDef.get("color").getAsString();
      if (!colorString.startsWith("#")) {
        // Make sure it is parsed as a hex color string, not something else.
        colorString = "#" + colorString;
      }

      colorPaint = new DrawableColorPaint(Color.decode(colorString));
    } else {
      colorPaint = null;
    }

    final var lumens = lightDef.has("lumens") ? lightDef.get("lumens").getAsInt() : 100;
    if (lumens == 0) {
      throw new ParserException(I18N.getText("Lumens must be non-zero."));
    }

    return new Light(shape, offset, range, arc, colorPaint, lumens, gmOnly, ownerOnly);
  }

  private static JsonObject lightSourceToJson(LightSource source) {
    final var lightSourceDef = new JsonObject();
    lightSourceDef.addProperty("name", source.getName());
    lightSourceDef.addProperty("type", source.getType().toString());
    lightSourceDef.addProperty("scale", source.isScaleWithToken());

    final var lightDefs = new JsonArray();
    for (final Light light : source.getLightList()) {
      lightDefs.add(lightToJson(source, light));
    }
    lightSourceDef.add("lights", lightDefs);
    return lightSourceDef;
  }

  private static JsonObject lightToJson(LightSource source, Light light) {
    final var lightDef = new JsonObject();
    lightDef.addProperty("shape", light.getShape().toString());

    if (light.getShape() == ShapeType.BEAM) {
      lightDef.addProperty("offset", light.getFacingOffset());
      lightDef.addProperty("arc", light.getArcAngle());
    }

    if (light.getShape() == ShapeType.CONE) {
      lightDef.addProperty("offset", light.getFacingOffset());
      lightDef.addProperty("arc", light.getArcAngle());
    }

    if (source.getType() == LightSource.Type.AURA) {
      lightDef.addProperty("gmOnly", light.isGM());
      lightDef.addProperty("ownerOnly", light.isOwnerOnly());
    }

    lightDef.addProperty("range", light.getRadius());
    if (light.getPaint() instanceof DrawableColorPaint paint) {
      lightDef.addProperty("color", toHex(paint.getColor()));
    }
    lightDef.addProperty("lumens", light.getLumens());

    return lightDef;
  }

  private static String toHex(int rgb) {
    return String.format("#%06X", rgb & 0x00FFFFFF);
  }
}
