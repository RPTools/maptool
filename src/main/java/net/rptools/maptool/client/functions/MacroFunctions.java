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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class MacroFunctions extends AbstractFunction {
  private static final MacroFunctions instance = new MacroFunctions();

  private MacroFunctions() {
    super(
        0,
        5,
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
    MapToolVariableResolver resolver = ((MapToolVariableResolver) parser.getVariableResolver());

    if (functionName.equals("hasMacro")) {
      return hasMacro(resolver, parameters);
    } else if (functionName.equals("createMacro")) {
      return createMacro(resolver, parameters);
    } else if (functionName.equals("getMacros")) {
      return getMacros(resolver, parameters);
    } else if (functionName.equals("getMacroProps")) {
      return getMacroProps(resolver, parameters);
    } else if (functionName.equals("setMacroProps")) {
      return setMacroProps(resolver, parameters);
    } else if (functionName.equals("getMacroIndexes")) {
      return getMacroIndexes(resolver, parameters);
    } else if (functionName.equals("getMacroName")) {
      return MapTool.getParser().getMacroName();
    } else if (functionName.equals("getMacroLocation")) {
      return MapTool.getParser().getMacroSource();
    } else if (functionName.equals("setMacroCommand")) {
      return setMacroCommand(resolver, parameters);
    } else if (functionName.equals("getMacroCommand")) {
      return getMacroCommand(resolver, parameters);
    } else if (functionName.equals("getMacroButtonIndex")) {
      return BigDecimal.valueOf(MapTool.getParser().getMacroButtonIndex());
    } else if (functionName.equals("removeMacro")) {
      return removeMacro(resolver, parameters);
    } else if (functionName.equals("getMacroGroup")) {
      return getMacroGroup(resolver, parameters);
    } else { // should never happen, hopefully ;)
      throw new ParserException("Unkown function: " + functionName);
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
      throw new ParserException("No macro at index " + index);
    }

    if ("json".equals(delim)) {
      Map<String, Object> props = new HashMap<String, Object>();
      props.put("autoExecute", mbp.getAutoExecute());
      props.put("color", mbp.getColorKey());
      props.put("fontColor", mbp.getFontColorKey());
      props.put("group", mbp.getGroup());
      props.put("includeLabel", mbp.getIncludeLabel());
      props.put("sortBy", mbp.getSortby());
      props.put("index", mbp.getIndex());
      props.put("label", mbp.getLabel());
      props.put("fontSize", mbp.getFontSize());
      props.put("minWidth", mbp.getMinWidth());
      props.put("playerEditable", mbp.getAllowPlayerEdits());
      props.put("command", mbp.getCommand());
      props.put("maxWith", mbp.getMaxWidth());
      if (mbp.getToolTip() != null) {
        props.put("tooltip", mbp.getToolTip());
      } else {
        props.put("tooltip", "");
      }
      props.put("applyToSelected", mbp.getApplyToTokens());

      JSONArray compare = new JSONArray();

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

      props.put("compare", compare);

      return JSONObject.fromObject(props);
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
    JSONObject jobj;

    // This should default to false for token buttons.
    mbp.setApplyToTokens(false);

    if (propString.trim().startsWith("{")) {
      // We are either a JSON string or an illegal string.
      jobj = JSONObject.fromObject(propString);
    } else {
      jobj = JSONMacroFunctions.getInstance().fromStrProp(propString, delim);
    }
    if (jobj.containsKey("command") && !MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          "setMacroProps(): You do not have permision to change the macro command.");
    }
    if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          "setMacroProps(): You do not have permision to change macros which are not player editable.");
    }
    for (Object o : jobj.keySet()) {
      String key = o.toString();
      String value = jobj.getString(key);

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
      } else if ("fontSize".equalsIgnoreCase(key)) {
        mbp.setFontSize(value);
      } else if ("minWidth".equalsIgnoreCase(key)) {
        mbp.setMinWidth(value);
      } else if ("maxWidth".equalsIgnoreCase(key)) {
        mbp.setMaxWidth(value);
      } else if ("playerEditable".equalsIgnoreCase(key)) {
        if (!MapTool.getParser().isMacroTrusted()) {
          throw new ParserException(
              "setMacroProps(): You do not have permission to change player editable status");
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
        JSONArray compareArray = jobj.getJSONArray("compare");
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
      if (Integer.parseInt(val) == 0) {
        return false;
      } else {
        return true;
      }
    } catch (NumberFormatException e) {
      return true;
    }
  }

  /**
   * Checks to see if the token has the specified macro. The first value in param is the name of the
   * macro to check, if there is a second argument then it is the token to check, otherwise the
   * token in context is checked.
   *
   * @param resolver The variable resolver.
   * @param param The parameters.
   * @return BigDecimal.ONE if the token has the macro, BigDecimal.ZERO if it does not.
   * @throws ParserException If an error occurs.
   */
  private BigDecimal hasMacro(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    Token token;
    String macro;
    if (param.size() == 2) { // Second parameter should be token name/id.
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "hasMacro(): You do not have the permission to specify the token.");
      }
      token = FindTokenFunctions.findToken(param.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            "hasMacro(): Unknown Token or Token ID, " + param.get(1).toString());
      }
      macro = param.get(0).toString();
    } else if (param.size() == 1) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("hasMacro(): No impersonated token");
      }
      macro = param.get(0).toString();
    } else {
      throw new ParserException("hasMacro(): Incorrect number of parameters.");
    }

    return token.getMacroNames(false).contains(macro) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Gets the name of the macros for a token. If param has 2 arguments then the first is resolved as
   * the token name the second is the macro, if it only has one then the token in context is used
   * and the argument is the macro name.
   *
   * @param resolver The variable resolver.
   * @param param The parameters.
   * @return The string containing the macro names.
   * @throws ParserException If an error occurs.
   */
  private String getMacros(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    Token token;
    String delim;

    if (param.size() == 0) {
      delim = ",";
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacros(): No impersonated token.");
      }
    } else if (param.size() == 1) {
      delim = param.get(0).toString();
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacros(): No impersonated token.");
      }
    } else if (param.size() == 2) { // Token is second parameter
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "getMacros(): You do not have the permission to specify the token.");
      }
      delim = param.get(0).toString();
      token = FindTokenFunctions.findToken(param.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            "getMacros(): Unknown Token or Token ID, " + param.get(1).toString());
      }
    } else {
      throw new ParserException("getMacros(): Incorrect number of parameters.");
    }

    String[] names = new String[token.getMacroNames(false).size()];

    if ("json".equals(delim)) {
      return JSONArray.fromObject(token.getMacroNames(false).toArray(names)).toString();
    } else {
      return StringFunctions.getInstance().join(token.getMacroNames(false).toArray(names), delim);
    }
  }

  /**
   * Gets the macro properties for a macro button on a token. If param has 1 value it used as the
   * index of the button, if there are 2 values then the second is used as the delimiter. If there
   * is a third value then it is the token to get the button property from. If no token is specified
   * then the token in context is used.
   *
   * @param resolver The variable resolver.
   * @param param The parameters to the function.
   * @return The properties for the button.
   * @throws ParserException if an error occurs.
   */
  private Object getMacroProps(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    Token token;
    String delim;

    if (param.size() < 1) {
      throw new ParserException("getMacroProps(): Not enough parameters.");
    }

    if (!(param.get(0) instanceof BigDecimal)) {
      throw new ParserException("getMacroProps(): first argument must be a number.");
    }
    int index = ((BigDecimal) param.get(0)).intValue();

    if (param.size() == 1) {
      delim = ";";
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroProps(): No impersonated token.");
      }
    } else if (param.size() == 2) {
      delim = param.get(1).toString();
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroProps(): No impersonated token.");
      }
    } else if (param.size() == 3) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "hasMacroProps(): You do not have the permission to specify the token.");
      }
      delim = param.get(1).toString();
      token = FindTokenFunctions.findToken(param.get(2).toString(), null);
      if (token == null) {
        throw new ParserException(
            "getMacroProps(): Unknown Token or Token ID, " + param.get(2).toString());
      }

    } else {
      throw new ParserException("getMacroProps(): Incorrect number of parameters.");
    }

    return getMacroButtonProps(token, index, delim);
  }

  /**
   * Gets the indexes for all the macros on a token with the specified label.
   *
   * @param resolver The variable resolver.
   * @param param The list of parameters. The first parameter is the label to get the indexes for,
   *     the second parameter if it exists is the delimiter to use (',' if not specified) and the
   *     third is the token to get the indexes from. If no token is specified then the token in
   *     context is used.
   * @return the indexes for the macro buttons.
   * @throws ParserException if an error occurs.
   */
  private String getMacroIndexes(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    Token token;
    String delim;
    String label;

    if (param.size() < 1) {
      throw new ParserException("getMacroIndexes(): Not enough parameters.");
    }
    label = param.get(0).toString();

    if (param.size() == 1) {
      delim = ",";
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroIndexes(): No impersonated token.");
      }
    } else if (param.size() == 2) {
      delim = param.get(1).toString();
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroIndexes(): No impersonated token.");
      }
    } else if (param.size() == 3) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "getMacroIndexes(): You do not have the permission to specify the token.");
      }
      delim = param.get(1).toString();
      token = FindTokenFunctions.findToken(param.get(2).toString(), null);
      if (token == null) {
        throw new ParserException(
            "getMacroIndexes(): Unknown Token or Token ID, " + param.get(2).toString());
      }
    } else {
      throw new ParserException("getMacroIndexes(): Incorrect number of parameters.");
    }

    List<String> indexes = new ArrayList<String>();
    for (MacroButtonProperties mbp : token.getMacroList(false)) {
      if (mbp.getLabel().equals(label)) {
        indexes.add(Integer.toString(mbp.getIndex()));
      }
    }
    if ("json".equals(delim)) {
      return JSONArray.fromObject(indexes).toString();
    } else {
      return StringFunctions.getInstance().join(indexes, delim);
    }
  }

  /**
   * Gets the command for a macro button on a token. The fist value in param is the index of the
   * macro button to get the command from. If there is a second value in param then this is the
   * token to get the command from. If no token is specified then the token in context is used.
   *
   * @param resolver The variable resolver.
   * @param param The parameters.
   * @return the macro command or "" if it has no command.
   * @throws ParserException if there is an error.
   */
  private String getMacroCommand(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    if (param.size() < 1) {
      throw new ParserException("getMacroCommand(): Not enough parameters.");
    }

    if (!(param.get(0) instanceof BigDecimal)) {
      throw new ParserException("getMacroCommand(): First argument must be a number.");
    }

    int index = ((BigDecimal) param.get(0)).intValue();

    Token token;

    if (param.size() == 1) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroCommand(): No impersonated token.");
      }
    } else if (param.size() == 2) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "getMacroCommand(): You do not have the permission to specify the token.");
      }

      token = FindTokenFunctions.findToken(param.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            "getMacroCommand(): Unknown Token or Token ID, " + param.get(1).toString());
      }
    } else {
      throw new ParserException("getMacroCommand(): Incorrect number of parameters.");
    }

    MacroButtonProperties mbp = token.getMacro(index, false);
    if (mbp == null) {
      throw new ParserException(
          "getMacroCommand(): Macro at index "
              + index
              + " does not exist for token "
              + token.getName());
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
   * @param resolver The variable resolver.
   * @param param The arguments passed to the function.
   * @return the index of the newly created button.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal createMacro(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    if (param.size() < 1) {
      throw new ParserException("createMacro(): Not enough parameters.");
    }

    String label;
    String command;
    Token token;
    String prop;
    String delim;

    if (param.size() == 1) { // Only valid if its a json object
      JSONObject jobj;
      jobj = JSONObject.fromObject(param.get(0).toString());
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("createMacro(): No impersonated token.");
      }

      if (!jobj.containsKey("label")) {
        throw new ParserException("createMacro(): Missing label.");
      }
      label = jobj.getString("label");

      if (!jobj.containsKey("command")) {
        throw new ParserException("createMacro(): Missing command.");
      }
      command = jobj.getString("command");
      prop = param.get(0).toString();
      delim = "json";
    } else if (param.size() == 2) { // either (json, token) or (label, command)
      JSONObject jobj;
      try {
        jobj = JSONObject.fromObject(param.get(0).toString());
        if (!jobj.containsKey("label")) {
          throw new ParserException("createMacro(): Missing label.");
        }
        label = jobj.getString("label");

        if (!jobj.containsKey("command")) {
          throw new ParserException("createMacro(): Missing command.");
        }
        command = jobj.getString("command");

        if (!MapTool.getParser().isMacroTrusted()) {
          throw new ParserException(
              "createMacro(): You do not have the permission to specify the token.");
        }

        token = FindTokenFunctions.findToken(param.get(1).toString(), null);
        if (token == null) {
          throw new ParserException(
              "createMacro(): Unknown Token or Token ID, " + param.get(1).toString());
        }
        prop = param.get(0).toString();
        delim = "json";
      } catch (JSONException e) {
        label = param.get(0).toString();
        command = param.get(1).toString();
        prop = null;
        delim = null;
        token = resolver.getTokenInContext();
        if (token == null) {
          throw new ParserException("createMacro(): No impersonated token.");
        }
      }
    } else if (param.size() == 3) { // label, command, props
      label = param.get(0).toString();
      command = param.get(1).toString();
      prop = param.get(2).toString();
      delim = ";";
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("createMacro(): No impersonated token.");
      }
    } else if (param.size() == 4) { // label, command, props, delim
      label = param.get(0).toString();
      command = param.get(1).toString();
      prop = param.get(2).toString();
      delim = param.get(3).toString();
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("createMacro(): No impersonated token.");
      }
    } else if (param.size() == 5) { // label, command, props, delim, token
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "createMacro(): You do not have the permission to specify the token.");
      }
      label = param.get(0).toString();
      command = param.get(1).toString();
      prop = param.get(2).toString();
      delim = param.get(3).toString();
      token = FindTokenFunctions.findToken(param.get(4).toString(), null);
      if (token == null) {
        throw new ParserException(
            "createMacro(): Unknown Token or Token ID, " + param.get(4).toString());
      }
    } else {
      throw new ParserException("createMacro(): Incorrect number of parameters.");
    }

    MacroButtonProperties mbp = new MacroButtonProperties(token.getMacroNextIndex());
    mbp.setCommand(command);
    if (prop != null) {
      setMacroProps(mbp, prop, delim);
    }

    mbp.setLabel(label);
    mbp.setSaveLocation("Token");
    mbp.setTokenId(token);
    mbp.setApplyToTokens(false);
    mbp.save();

    updateToken(token);
    return BigDecimal.valueOf(mbp.getIndex());
  }

  /**
   * Sets the properties for macro buttons on a token. The first argument in params is the button
   * index or label of the buttons to set the properties for, if it is an index then only that
   * button is changed, if it is a label then all buttons with that label are changed. The second
   * argument is the properties to change, the third if specified is the delimiter for the
   * properties (defaults to ';') if there is a fourth argument then it is the token that contains
   * the buttons. If no token is specified then the token in context is used.
   *
   * @param resolver The variable resolver.
   * @param param The arguments passed to the function.
   * @return an empty string.
   * @throws ParserException if an error occurs.
   */
  private String setMacroProps(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    if (param.size() < 2) {
      throw new ParserException("setMacroProps(): Not enough parameters.");
    }
    Token token;

    if (param.size() == 2 || param.size() == 3) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("setMacroProps(): No impersonated token.");
      }
    } else if (param.size() == 4) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "setMacroProps(): You do not have the permission to specify the token.");
      }
      token = FindTokenFunctions.findToken(param.get(3).toString(), null);
      if (token == null) {
        throw new ParserException(
            "setMacroProps(): Unknown Token or Token ID, " + param.get(3).toString());
      }
    } else {
      throw new ParserException("setMacroProps(): Incorrect number of parameters.");
    }

    if ((param.get(0) instanceof BigDecimal)) {
      int index = ((BigDecimal) param.get(0)).intValue();
      MacroButtonProperties mbp = token.getMacro(index, false);

      if (mbp == null) {
        throw new ParserException(
            "setMacroProps(): No macro at index " + index + " for " + token.getName());
      }
      if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "setMacroProps: You do not have permissions to edit macro button at index "
                + index
                + " on "
                + token.getName());
      }
      String delim = param.size() > 2 ? param.get(2).toString() : ";";
      setMacroProps(mbp, param.get(1).toString(), delim);
      mbp.save();
    } else {
      for (MacroButtonProperties mbp : token.getMacroList(false)) {
        String delim = param.size() > 2 ? param.get(2).toString() : ";";
        if (mbp.getLabel().equals(param.get(0).toString())) {
          if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
            MapTool.addLocalMessage(
                "Warning: You can not edit macro button "
                    + mbp.getLabel()
                    + " index = "
                    + mbp.getIndex()
                    + " on "
                    + token.getName());
          } else {
            setMacroProps(mbp, param.get(1).toString(), delim);
            mbp.save();
          }
        }
      }
    }
    updateToken(token);
    return "";
  }

  /**
   * Sets the command for a macro button on a token. The first value param is the index of the macro
   * button to change. The second value param is the command to set for the button. The third value
   * if present is the token that the button is for. If no token is specified then the token in
   * context is used.
   *
   * @param res The variable resolver.
   * @param param The arguments.
   * @return An empty string.
   * @throws ParserException If an error occurs.
   */
  private String setMacroCommand(MapToolVariableResolver res, List<Object> param)
      throws ParserException {
    if (param.size() < 2) {
      throw new ParserException("setMacroCommand(): Not enough parameters.");
    }

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          "setMacroCommand(): You do not have permission to call this function.");
    }

    if (!(param.get(0) instanceof BigDecimal)) {
      throw new ParserException("setMacroCommand(): First Argument must be a number.");
    }
    int index = ((BigDecimal) param.get(0)).intValue();

    Token token;

    if (param.size() == 2) {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException("setMacroCommand(): No impersonated token.");
      }
    } else if (param.size() == 3) {
      token = FindTokenFunctions.findToken(param.get(2).toString(), null);
      if (token == null) {
        throw new ParserException(
            "setMacroCommand(): Unknown Token or Token ID, " + param.get(3).toString());
      }
    } else {
      throw new ParserException("setMacroCommand(): Incorrect number of parameters.");
    }

    MacroButtonProperties mbp = token.getMacro(index, false);
    if (mbp == null) {
      throw new ParserException(
          "setMacroCommand(): No button at index " + index + " for " + token.getName());
    }

    mbp.setCommand(param.get(1).toString());
    mbp.save();
    updateToken(token);
    return "";
  }

  /**
   * Removes a macro button from a token. The first argument in param is the index of the button to
   * remove. The second argument in param is the token to remove the button from, if the token is
   * not specified then the token in context is used.
   *
   * @param res The variable resolver.
   * @param param The arguments.
   * @return the details of the button that was removed.
   * @throws ParserException if an error occurs.
   */
  private String removeMacro(MapToolVariableResolver res, List<Object> param)
      throws ParserException {
    if (param.size() < 1) {
      throw new ParserException("removeMacro(): Not enough parameters.");
    }

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException("removeMacro(): You do not have permission to call this function.");
    }

    if (!(param.get(0) instanceof BigDecimal)) {
      throw new ParserException("removeMacro(): First Argument must be a number.");
    }
    int index = ((BigDecimal) param.get(0)).intValue();

    Token token;

    if (param.size() == 1) {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException("removeMacro(): No impersonated token.");
      }
    } else if (param.size() == 2) {
      token = FindTokenFunctions.findToken(param.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            "removeMacro(): Unknown Token or Token ID, " + param.get(1).toString());
      }
    } else {
      throw new ParserException("removeMacro(): Incorrect number of parameters.");
    }

    MacroButtonProperties mbp = token.getMacro(index, false);
    if (mbp == null) {
      throw new ParserException(
          "removeMacro(): No button at index " + index + " for " + token.getName());
    }

    String label = mbp.getLabel();
    token.deleteMacroButtonProperty(mbp);
    StringBuilder sb = new StringBuilder();
    sb.append("Removed macro button ").append(label).append("(index = ").append(index);
    sb.append(") from ").append(token.getName());
    return sb.toString();
  }

  /**
   * Gets the index of the macros for a token in the specified group.
   *
   * @param resolver The variable resolver.
   * @param param The parameters.
   * @return The string containing the macro names.
   * @throws ParserException If an error occurs.
   */
  private String getMacroGroup(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    Token token;
    String delim;
    String group;

    if (param.size() == 0) {
      throw new ParserException("getMacroGroup(): Not enough parameters");
    } else if (param.size() == 1) {
      delim = ",";
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroGroup(): No impersonated token.");
      }
      group = param.get(0).toString();
    } else if (param.size() == 2) {
      group = param.get(0).toString();
      delim = param.get(1).toString();
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException("getMacroGroup(): No impersonated token.");
      }
    } else if (param.size() == 3) { // Token is third parameter
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            "getMacroGroup(): You do not have the permission to specify the token.");
      }
      group = param.get(0).toString();
      delim = param.get(1).toString();
      token = FindTokenFunctions.findToken(param.get(2).toString(), null);
      if (token == null) {
        throw new ParserException(
            "getMacroGroup(): Unknown Token or Token ID, " + param.get(2).toString());
      }
    } else {
      throw new ParserException("getMacroGroup(): Incorrect number of parameters.");
    }

    List<String> indexes =
        new LinkedList<String>(); // Has to be a string or the list functions wont like it :\
    for (MacroButtonProperties props : token.getMacroList(false)) {
      if (props.getGroup().equals(group)) {
        indexes.add(String.valueOf(props.getIndex()));
      }
    }

    String[] vals = new String[indexes.size()];

    if ("json".equals(delim)) {
      return JSONArray.fromObject(indexes.toArray(vals)).toString();
    } else {
      return StringFunctions.getInstance().join(indexes.toArray(vals), delim);
    }
  }

  /**
   * Updates the token locally and remotely.
   *
   * @param token The token to update.
   */
  private void updateToken(Token token) {
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
  }
}
