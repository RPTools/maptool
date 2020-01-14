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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MacroFunctions extends AbstractFunction {
  private static final Logger log = LogManager.getLogger(MacroFunctions.class);

  private static final MacroFunctions instance = new MacroFunctions();

  private static final String KEY_MISSING_COMMAND = "macro.function.MacroFunctions.missingCommand";
  private static final String KEY_MISSING_LABEL = "macro.function.MacroFunctions.missingLabel";
  private static final String KEY_NO_PERM = "macro.function.MacroFunctions.noPerm";
  private static final String KEY_NO_PERM_COMMAND = "macro.function.MacroFunctions.noPermCommand";
  private static final String KEY_NO_PERM_EDITABLE = "macro.function.MacroFunctions.noPermEditable";
  private static final String KEY_NO_PERM_OTHER = "macro.function.MacroFunctions.noPermOther";
  private static final String KEY_OUT_OF_RANGE = "macro.function.MacroFunctions.outOfRange";
  private static final String KEY_UNKNOWN_MACRO = "macro.function.general.unknownFunction";

  private MacroFunctions() {
    super(
        0,
        6,
        "hasMacro",
        "createMacro",
        "setMacroProps",
        "getMacros",
        "getMacroProps",
        "getMacroIndexes",
        "getMacroName",
        "getMacroLocation",
        "setMacroCommand",
        "getMacroCommand",
        "getMacroButtonIndex",
        "removeMacro",
        "getMacroGroup");
  }

  public static MacroFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("hasMacro")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      String label = parameters.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 1, 2);
      return token.getMacroNames(false).contains(label) ? BigDecimal.ONE : BigDecimal.ZERO;
    } else if (functionName.equalsIgnoreCase("createMacro")) {
      return createMacro(parser, parameters);
    } else if (functionName.equalsIgnoreCase("getMacros")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 1, 2);
      return getMacros(delim, token);
    } else if (functionName.equalsIgnoreCase("getMacroProps")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ";";
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 2, 3);
      return getMacroButtonProps(token, index, delim);
    } else if (functionName.equalsIgnoreCase("setMacroProps")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 5);
      Object value = parameters.get(0);
      String props = parameters.get(1).toString();
      String delim = parameters.size() > 2 ? parameters.get(2).toString() : ";";
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 3, 4);
      return setMacroProps(value, props, delim, token);
    } else if (functionName.equalsIgnoreCase("getMacroIndexes")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      String label = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 2, 3);
      return getMacroIndexes(label, delim, token);
    } else if (functionName.equalsIgnoreCase("getMacroName")) {
      return MapTool.getParser().getMacroName();
    } else if (functionName.equalsIgnoreCase("getMacroLocation")) {
      return MapTool.getParser().getMacroSource();
    } else if (functionName.equalsIgnoreCase("setMacroCommand")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 4);
      FunctionUtil.blockUntrustedMacro(functionName);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      String command = FunctionUtil.paramAsString(functionName, parameters, 1, true);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 2, 3);
      return setMacroCommand(index, command, token);
    } else if (functionName.equalsIgnoreCase("getMacroCommand")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 1, 2);
      return getMacroCommand(index, token);
    } else if (functionName.equalsIgnoreCase("getMacroButtonIndex")) {
      return BigDecimal.valueOf(MapTool.getParser().getMacroButtonIndex());
    } else if (functionName.equalsIgnoreCase("removeMacro")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 1, 2);
      return removeMacro(index, token);
    } else if (functionName.equalsIgnoreCase("getMacroGroup")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      String group = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 2, 3);
      return getMacroGroup(group, delim, token);
    } else { // should never happen, hopefully ;)
      throw new ParserException(I18N.getText(KEY_UNKNOWN_MACRO, functionName));
    }
  }

  /**
   * Gets the macro button properties for the specified macro on a token.
   *
   * @param token The token to get the macro properties for.
   * @param index The index of the macro button.
   * @param delim The delimiter to use.
   * @return the properties.
   * @throws ParserException if an error occurs.
   */
  public Object getMacroButtonProps(Token token, int index, String delim) throws ParserException {
    MacroButtonProperties mbp = token.getMacro(index, !MapTool.getParser().isMacroTrusted());
    if (mbp == null) {
      throw new ParserException(
          I18N.getText(KEY_OUT_OF_RANGE, "getMacroProps", index, token.getName()));
    }

    if ("json".equals(delim)) {
      JsonObject props = new JsonObject();
      props.addProperty("autoExecute", mbp.getAutoExecute());
      props.addProperty("color", mbp.getColorKey());
      props.addProperty("fontColor", mbp.getFontColorKey());
      props.addProperty("group", mbp.getGroup());
      props.addProperty("includeLabel", mbp.getIncludeLabel());
      props.addProperty("sortBy", mbp.getSortby());
      props.addProperty("index", mbp.getIndex());
      props.addProperty("label", mbp.getLabel());
      props.addProperty("fontSize", mbp.getFontSize());
      props.addProperty("minWidth", mbp.getMinWidth());
      props.addProperty("playerEditable", mbp.getAllowPlayerEdits());
      props.addProperty("command", mbp.getCommand());
      props.addProperty("maxWith", mbp.getMaxWidth());
      if (mbp.getToolTip() != null) {
        props.addProperty("tooltip", mbp.getToolTip());
      } else {
        props.addProperty("tooltooltip", "");
      }
      props.addProperty("toolapplyToSelected", mbp.getApplyToTokens());

      JsonArray compare = new JsonArray();

      if (mbp.getCompareGroup()) {
        compare.add("group");
      }
      if (mbp.getCompareSortPrefix()) {
        compare.add("sortPrefix");
      }
      if (mbp.getCompareCommand()) {
        compare.add("command");
      }
      if (mbp.getCompareIncludeLabel()) {
        compare.add("includeLabel");
      }
      if (mbp.getCompareAutoExecute()) {
        compare.add("autoExecute");
      }
      if (mbp.getCompareApplyToSelectedTokens()) {
        compare.add("applyToSelected");
      }

      props.add("compare", compare);

      JsonObject propsMetadata = new JsonObject();
      propsMetadata.addProperty("uuid", mbp.getMacroUUID());
      propsMetadata.addProperty(
          "commandChecksum", new MD5Key(mbp.getCommand().getBytes()).toString());
      propsMetadata.addProperty(
          "propsChecksum", new MD5Key(props.toString().getBytes()).toString());

      props.add("metadata", propsMetadata);

      return props;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("autoExecute=").append(mbp.getAutoExecute()).append(delim);
      sb.append("color=").append(mbp.getColorKey()).append(delim);
      sb.append("fontColor=").append(mbp.getFontColorKey()).append(delim);
      sb.append("group=").append(mbp.getGroup()).append(delim);
      sb.append("includeLabel=").append(mbp.getIncludeLabel()).append(delim);
      sb.append("sortBy=").append(mbp.getSortby()).append(delim);
      sb.append("index=").append(mbp.getIndex()).append(delim);
      sb.append("label=").append(mbp.getLabel()).append(delim);
      sb.append("fontSize=").append(mbp.getFontSize()).append(delim);
      sb.append("minWidth=").append(mbp.getMinWidth()).append(delim);
      sb.append("playerEditable=").append(mbp.getAllowPlayerEdits()).append(delim);
      sb.append("maxWidth=").append(mbp.getMaxWidth()).append(delim);
      if (mbp.getToolTip() != null) {
        sb.append("tooltip=").append(mbp.getToolTip()).append(delim);
      } else {
        sb.append("tooltip=").append("").append(delim);
      }
      sb.append("applyToSelected=").append(mbp.getApplyToTokens()).append(delim);
      return sb.toString();
    }
  }

  /**
   * Sets the properties for a specified macro button on the specified token.
   *
   * @param mbp the properties of the button
   * @param propString the String of the list
   * @param delim the delimiter
   * @throws ParserException if user doesn't have permission
   */
  public void setMacroProps(MacroButtonProperties mbp, String propString, String delim)
      throws ParserException {
    JsonElement json;

    // This should default to false for token buttons.
    mbp.setApplyToTokens(false);

    if (propString.trim().startsWith("{")) {
      // We are either a JSON string or an illegal string.
      json = JSONMacroFunctions.getInstance().asJsonElement(propString);
    } else {
      json =
          JSONMacroFunctions.getInstance().getJsonObjectFunctions().fromStrProp(propString, delim);
    }

    JsonObject jobj = json.getAsJsonObject();

    if (jobj.has("command") && !MapTool.getParser().isMacroTrusted()) {
      int index = mbp.getIndex();
      throw new ParserException(
          I18N.getText(KEY_NO_PERM_COMMAND, "setMacroProps", index, mbp.getToken().getName()));
    }
    for (Object o : jobj.keySet()) {
      String key = o.toString();
      String value = JSONMacroFunctions.getInstance().jsonToScriptString(jobj.get(key));

      if ("autoexecute".equalsIgnoreCase(key)) {
        mbp.setAutoExecute(boolVal(value));
      } else if ("color".equalsIgnoreCase(key)) {
        mbp.setColorKey(value);
      } else if ("fontColor".equalsIgnoreCase(key)) {
        mbp.setFontColorKey(value);
      } else if ("fontSize".equalsIgnoreCase(key)) {
        mbp.setFontSize(value);
      } else if ("group".equalsIgnoreCase(key)) {
        mbp.setGroup(value);
      } else if ("includeLabel".equalsIgnoreCase(key)) {
        mbp.setIncludeLabel(boolVal(value));
      } else if ("sortBy".equalsIgnoreCase(key)) {
        mbp.setSortby(value);
      } else if ("index".equalsIgnoreCase(key)) {
        mbp.setIndex(Integer.parseInt(value));
      } else if ("label".equalsIgnoreCase(key)) {
        mbp.setLabel(value);
      } else if ("minWidth".equalsIgnoreCase(key)) {
        mbp.setMinWidth(value);
      } else if ("maxWidth".equalsIgnoreCase(key)) {
        mbp.setMaxWidth(value);
      } else if ("playerEditable".equalsIgnoreCase(key)) {
        if (!MapTool.getParser().isMacroTrusted()) {
          int index = mbp.getIndex();
          throw new ParserException(
              I18N.getText(KEY_NO_PERM_EDITABLE, "setMacroProps", index, mbp.getToken().getName()));
        }
        mbp.setAllowPlayerEdits(boolVal(value));
      } else if ("command".equals(key)) {
        mbp.setCommand(value);
      } else if ("tooltip".equalsIgnoreCase(key)) {
        if (value.trim().length() == 0) {
          mbp.setToolTip(null);
        } else {
          mbp.setToolTip(value);
        }
      } else if ("applyToSelected".equalsIgnoreCase(key)) {
        mbp.setApplyToTokens(boolVal(value));
      } else if ("compare".equalsIgnoreCase(key)) {
        JsonArray compareArray = jobj.get("compare").getAsJsonArray();
        // First set everything to false as script will specify what is compared
        mbp.setCompareGroup(false);
        mbp.setCompareSortPrefix(false);
        mbp.setCompareCommand(false);
        mbp.setCompareIncludeLabel(false);
        mbp.setCompareAutoExecute(false);
        mbp.setCompareApplyToSelectedTokens(false);

        for (Object co : compareArray) {
          String comp = co.toString();
          if (comp.equalsIgnoreCase("group")) {
            mbp.setCompareGroup(true);
          } else if (comp.equalsIgnoreCase("sortPrefix")) {
            mbp.setCompareSortPrefix(true);
          } else if (comp.equalsIgnoreCase("command")) {
            mbp.setCompareCommand(true);
          } else if (comp.equalsIgnoreCase("includeLabel")) {
            mbp.setCompareIncludeLabel(true);
          } else if (comp.equalsIgnoreCase("autoExecute")) {
            mbp.setCompareAutoExecute(true);
          } else if (comp.equalsIgnoreCase("applyToSelected")) {
            mbp.setCompareApplyToSelectedTokens(true);
          }
        }
      }
    }
  }

  /**
   * Calculates the boolean value based in an input string.
   *
   * @param val The input string.
   * @return the boolean value of the input string.
   */
  private boolean boolVal(String val) {
    if ("true".equalsIgnoreCase(val)) {
      return true;
    }

    if ("false".equalsIgnoreCase(val)) {
      return false;
    }

    try {
      return Integer.parseInt(val) != 0;
    } catch (NumberFormatException e) {
      return true;
    }
  }

  /**
   * Gets the name of the macros for a token.
   *
   * @param delim delimiter used to separate the values in the returned String List
   * @param token token that the function is executed on
   * @return The string containing the macro names.
   */
  private Object getMacros(String delim, Token token) {

    String[] names = new String[token.getMacroNames(false).size()];

    if ("json".equals(delim)) {
      JsonArray jsonArray = new JsonArray();
      for (String name : token.getMacroNames(false)) {
        jsonArray.add(name);
      }
      return jsonArray;
    } else {
      return StringFunctions.getInstance().join(token.getMacroNames(false).toArray(names), delim);
    }
  }

  /**
   * Gets the indexes for all the macros on a token with the specified label.
   *
   * @param label the label for the macro buttons to return
   * @param delim the delimiter separating the indexes. If "json", returns a JSON Array.
   * @param token the token that the function is executed on
   * @return the indexes for the macro buttons.
   */
  private Object getMacroIndexes(String label, String delim, Token token) {
    List<String> strIndexes = new ArrayList<>();
    List<Integer> indexes = new ArrayList<>();
    for (MacroButtonProperties mbp : token.getMacroList(false)) {
      if (mbp.getLabel().equals(label)) {
        strIndexes.add(Integer.toString(mbp.getIndex()));
        indexes.add(mbp.getIndex());
      }
    }
    if ("json".equals(delim)) {
      JsonArray indArray = new JsonArray();
      for (int ind : indexes) {
        indArray.add(ind);
      }
      return indArray;
    } else {
      return StringFunctions.getInstance().join(strIndexes, delim);
    }
  }

  /**
   * Gets the command for a macro button on a token.
   *
   * @param index the index of the macro.
   * @param token the token to take the macro from.
   * @return the macro command or "" if it has no command.
   * @throws ParserException if there is no macro at the index.
   */
  private String getMacroCommand(int index, Token token) throws ParserException {

    MacroButtonProperties mbp = token.getMacro(index, false);
    if (mbp == null) {
      throw new ParserException(
          I18N.getText(KEY_OUT_OF_RANGE, "getMacroCommand", index, token.getName()));
    }
    String cmd = mbp.getCommand();
    return cmd != null ? cmd : "";
  }

  /**
   * Creates a macro button on a token. If There is only one argument in param and it is a json
   * string then the values in this are used to create the button on the token in context. If there
   * are two arguments and the first is a json string the second argument is the token to create the
   * button on. If the first argument is not a json string then it is the label for the new button.
   * The second argument is the command, if the third argument is specified then it is the
   * properties for the button. The fourth contains the delimiter for these properties (defaults to
   * ';' if not specified). The fifth argument is the token to create the macro button on, if no
   * token is specified it is created on the token in context.
   *
   * @param parser The parser.
   * @param param The arguments passed to the function.
   * @return the index of the newly created button.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal createMacro(Parser parser, List<Object> param) throws ParserException {
    FunctionUtil.checkNumberParam("createMacro", param, 1, 6);

    String label;
    String command;
    Token token;
    String prop;
    String delim;

    JsonObject jobj;

    try {
      jobj =
          JSONMacroFunctions.getInstance().asJsonElement(param.get(0).toString()).getAsJsonObject();
    } catch (ParserException | IllegalStateException e) {
      jobj = null;
    }

    if (jobj != null) {
      FunctionUtil.checkNumberParam("createMacro", param, 1, 3);
      if (!jobj.has("label")) {
        throw new ParserException(I18N.getText(KEY_MISSING_LABEL, "createMacro"));
      }
      label = JSONMacroFunctions.getInstance().jsonToScriptString(jobj.get("label"));
      if (!jobj.has("command")) {
        throw new ParserException(I18N.getText(KEY_MISSING_COMMAND, "createMacro"));
      }
      command = JSONMacroFunctions.getInstance().jsonToScriptString(jobj.get("command"));
      prop = param.get(0).toString();
      delim = "json";
      token = FunctionUtil.getTokenFromParam(parser, "createMacro", param, 1, 2);
    } else {
      FunctionUtil.checkNumberParam("createMacro", param, 2, 6);
      label = param.get(0).toString();
      command = param.get(1).toString();
      prop = param.size() > 2 ? param.get(2).toString() : null;
      delim = param.size() > 3 ? param.get(3).toString() : ";";
      token = FunctionUtil.getTokenFromParam(parser, "createMacro", param, 4, 5);
    }

    MacroButtonProperties mbp = new MacroButtonProperties(token.getMacroNextIndex());
    mbp.setCommand(command);
    if (prop != null) {
      setMacroProps(mbp, prop, delim);
    }

    mbp.setLabel(label);
    mbp.setSaveLocation("Token");
    mbp.setTokenId(token);
    mbp.save();

    MapTool.serverCommand().putToken(token.getZoneRenderer().getZone().getId(), token);
    return BigDecimal.valueOf(mbp.getIndex());
  }

  /**
   * Sets the properties for macro buttons on a token. If supplied an index then only that button is
   * changed, if it is a label then all buttons with that label are changed.
   *
   * @param value the button index or the label of the buttons to set the properties for
   * @param props a String Property List or JSON Object containing the properties for the button
   * @param delim delim the delimiter used in the String Property List
   * @param token the token that the macro button is located on
   * @return an empty string.
   * @throws ParserException if an error occurs.
   */
  private String setMacroProps(Object value, String props, String delim, Token token)
      throws ParserException {
    if ((value instanceof BigDecimal)) {
      int index = ((BigDecimal) value).intValue();
      MacroButtonProperties mbp = token.getMacro(index, false);

      if (mbp == null) {
        throw new ParserException(
            I18N.getText(KEY_OUT_OF_RANGE, "setMacroProps", index, token.getName()));
      }
      if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            I18N.getText(KEY_NO_PERM, "setMacroProps", index, token.getName()));
      }
      setMacroProps(mbp, props, delim);
      mbp.save();
    } else {
      for (MacroButtonProperties mbp : token.getMacroList(false)) {
        if (mbp.getLabel().equals(value.toString())) {
          if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
            String label = mbp.getLabel();
            int index = mbp.getIndex();
            MapTool.addLocalMessage(
                I18N.getText(KEY_NO_PERM_OTHER, "setMacroProps", label, index, token.getName()));
          } else {
            setMacroProps(mbp, props, delim);
            mbp.save();
          }
        }
      }
    }
    MapTool.serverCommand().putToken(token.getZoneRenderer().getZone().getId(), token);
    return "";
  }

  /**
   * Sets the command for a macro button on a token.
   *
   * @param index the index of the macro button
   * @param command a string containing the command to set on the macro button
   * @param token the token that the command is set on
   * @return An empty string.
   * @throws ParserException If an error occurs.
   */
  private String setMacroCommand(int index, String command, Token token) throws ParserException {

    MacroButtonProperties mbp = token.getMacro(index, false);
    if (mbp == null) {
      throw new ParserException(
          I18N.getText(KEY_OUT_OF_RANGE, "setMacroCommand", index, token.getName()));
    }
    mbp.setCommand(command);
    mbp.save();
    MapTool.serverCommand().putToken(token.getZoneRenderer().getZone().getId(), token);
    return "";
  }

  /**
   * Removes a macro button from a token.
   *
   * @param index the index of the macro button.
   * @param token the token the macro is located on.
   * @return the details of the button that was removed.
   * @throws ParserException if the index is invalid.
   */
  private String removeMacro(int index, Token token) throws ParserException {

    MacroButtonProperties mbp = token.getMacro(index, false);
    if (mbp == null) {
      throw new ParserException(
          I18N.getText(KEY_OUT_OF_RANGE, "removeMacro", index, token.getName()));
    }

    String label = mbp.getLabel();
    token.deleteMacroButtonProperty(mbp);
    return "Removed macro button " + label + "(index = " + index + ") from " + token.getName();
  }

  /**
   * Gets the index of the macros for a token in the specified group.
   *
   * @param group the name of the macro group to get the macro button indexes from.
   * @param delim the delimiter used in the string list returned, defaults to ",".
   * @param token the token that the macro group is located on.
   * @return The string containing the macro names.
   */
  private Object getMacroGroup(String group, String delim, Token token) {
    // Has to be a string or the list functions wont like it :\
    List<String> strIndexes = new LinkedList<>();
    List<Integer> indexes = new LinkedList<>();
    for (MacroButtonProperties props : token.getMacroList(false)) {
      if (props.getGroup().equals(group)) {
        strIndexes.add(String.valueOf(props.getIndex()));
        indexes.add(props.getIndex());
      }
    }

    String[] vals = new String[strIndexes.size()];

    if ("json".equals(delim)) {
      JsonArray jarray = new JsonArray();
      for (Integer i : indexes) {
        jarray.add(i);
      }
      return jarray;
    } else {
      return StringFunctions.getInstance().join(strIndexes.toArray(vals), delim);
    }
  }
}
