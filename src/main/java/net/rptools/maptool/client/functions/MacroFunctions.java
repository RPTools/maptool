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
import java.util.*;
import javax.swing.*;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.macrobuttons.MacroButtonHotKeyManager;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.client.ui.macrobuttons.panels.AbstractMacroPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
        "getMacroHotkeys",
        "getMacroIndexes",
        "getMacroIndices",
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
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    if (functionName.equalsIgnoreCase("createMacro")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 6);
      switch (parameters.size()) {
        case 2, 3 -> {
          String id = FunctionUtil.paramAsString(functionName, parameters, 1, true);
          if (id.equalsIgnoreCase("campaign")) {
            return createMacro(parameters, false);
          } else if (id.equalsIgnoreCase("gm")) {
            return createMacro(parameters, true);
          }
        }
        case 5, 6 -> {
          String id = FunctionUtil.paramAsString(functionName, parameters, 4, true);
          if (id.equalsIgnoreCase("campaign")) {
            return createMacro(parameters, false);
          } else if (id.equalsIgnoreCase("gm")) {
            return createMacro(parameters, true);
          }
        }
      }
      return createMacro((MapToolVariableResolver) resolver, parameters);

    } else if (functionName.equalsIgnoreCase("hasMacro")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      String label = parameters.get(0).toString();
      if (parameters.size() > 1) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 1, true);
        if (id.equalsIgnoreCase("campaign")) {
          return hasMacro(label, false) ? BigDecimal.ONE : BigDecimal.ZERO;
        } else if (id.equalsIgnoreCase("gm")) {
          return hasMacro(label, true) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return token.getMacroNames(false).contains(label) ? BigDecimal.ONE : BigDecimal.ZERO;

    } else if (functionName.equalsIgnoreCase("getMacros")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 3);
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      if (parameters.size() > 1) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 1, true);
        if (id.equalsIgnoreCase("campaign")) {
          return getMacros(delim, MapTool.getCampaign().getMacroButtonPropertiesArray());
        } else if (id.equalsIgnoreCase("gm")) {
          return getMacros(delim, MapTool.getCampaign().getGmMacroButtonPropertiesArray());
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return getMacros(delim, token);

    } else if (functionName.equalsIgnoreCase("getMacroProps")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ";";
      if (parameters.size() > 2) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 2, true);
        if (id.equalsIgnoreCase("campaign")) {
          return getMacroButtonProps(index, delim, false);
        } else if (id.equalsIgnoreCase("gm")) {
          return getMacroButtonProps(index, delim, true);
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return getMacroButtonProps(token, index, delim);

    } else if (functionName.equalsIgnoreCase("setMacroProps")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 5);
      Object value = parameters.get(0);
      String props = parameters.get(1).toString();
      String delim = parameters.size() > 2 ? parameters.get(2).toString() : ";";
      if (parameters.size() > 3) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 3, true);
        if (id.equalsIgnoreCase("campaign")) {
          return setMacroProps(value, props, delim, false);
        } else if (id.equalsIgnoreCase("gm")) {
          return setMacroProps(value, props, delim, true);
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 3, 4);
      return setMacroProps(value, props, delim, token);

    } else if (functionName.equalsIgnoreCase("getMacroIndexes")
        || functionName.equalsIgnoreCase("getMacroIndices")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      String label = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      if (parameters.size() > 2) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 2, true);
        if (id.equalsIgnoreCase("campaign")) {
          return getMacroIndexes(
              label, delim, MapTool.getCampaign().getMacroButtonPropertiesArray());
        } else if (id.equalsIgnoreCase("gm")) {
          return getMacroIndexes(
              label, delim, MapTool.getCampaign().getGmMacroButtonPropertiesArray());
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
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
      if (parameters.size() > 2) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 2, true);
        if (id.equalsIgnoreCase("gm")) {
          return setMacroCommand(index, command, true);
        } else if (id.equalsIgnoreCase("campaign")) {
          return setMacroCommand(index, command, false);
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return setMacroCommand(index, command, token);

    } else if (functionName.equalsIgnoreCase("getMacroCommand")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      if (parameters.size() > 1) { // check for panel parameter
        String id = FunctionUtil.paramAsString(functionName, parameters, 1, true);
        if (id.equalsIgnoreCase("gm")) {
          return getMacroCommand(index, true);
        } else if (id.equalsIgnoreCase("campaign")) {
          return getMacroCommand(index, false);
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return getMacroCommand(index, token);

    } else if (functionName.equalsIgnoreCase("getMacroButtonIndex")) {
      return BigDecimal.valueOf(MapTool.getParser().getMacroButtonIndex());
    } else if (functionName.equalsIgnoreCase("getMacroHotkeys")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      String delim = "json";
      if (!parameters.isEmpty()) {
        delim = parameters.getFirst().toString();
      }
      return getMacroHotkeys(delim);
    } else if (functionName.equalsIgnoreCase("getMacroGroup")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 4);
      String group = parameters.get(0).toString();
      String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
      if (parameters.size() > 2) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 2, true);
        if (id.equalsIgnoreCase("gm")) {
          return getMacroGroup(group, delim, true);
        } else if (id.equalsIgnoreCase("campaign")) {
          return getMacroGroup(group, delim, false);
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 2, 3);
      return getMacroGroup(group, delim, token);

    } else if (functionName.equalsIgnoreCase("removeMacro")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      int index = FunctionUtil.paramAsInteger(functionName, parameters, 0, false);
      if (parameters.size() > 1) {
        String id = FunctionUtil.paramAsString(functionName, parameters, 1, true);
        if (id.equalsIgnoreCase("gm")) {
          return removeMacro(index, true);
        } else if (id.equalsIgnoreCase("campaign")) {
          return removeMacro(index, false);
        }
      }
      Token token = FunctionUtil.getTokenFromParam(resolver, functionName, parameters, 1, 2);
      return removeMacro(index, token);
    }
    /* code should never happen, hopefully ;) */
    throw new ParserException(I18N.getText(KEY_UNKNOWN_MACRO, functionName));
  }

  private Object getMacroHotkeys(String delim) {
    Map<KeyStroke, MacroButton> keyStrokeMap = MacroButtonHotKeyManager.getKeyStrokeMap();
    JsonObject keyMacroObject = new JsonObject();

    for (KeyStroke ks : keyStrokeMap.keySet()) {
      MacroButton btn = keyStrokeMap.get(ks);
      String address = btn.getProperties().getLabel() + "@";
      switch (btn.getPanelClass()) {
        case "GlobalPanel", "globalpanel" -> address += "global";
        case "CampaignPanel", "campaignpanel" -> address += "campaign";
        case "GmPanel", "gmpanel" -> address += "gm";
        default -> address += btn.getToken().getName();
      }
      keyMacroObject.addProperty(ks.toString(), address);
    }
    if (delim.equalsIgnoreCase("json")) {
      return keyMacroObject;
    } else {
      return JSONMacroFunctions.getInstance()
          .getJsonObjectFunctions()
          .toStringProp(keyMacroObject, delim);
    }
  }

  /**
   * Campaign version of hasMacro().
   *
   * @param label match text
   * @param gmPanel true for GM panel, false for Campaign panel
   * @return boolean
   */
  private boolean hasMacro(String label, boolean gmPanel) {
    List<MacroButtonProperties> list = getCampaignMbpList(gmPanel);
    return getCampaignMbpByIndexOrLabel(list, null, label).right != null;
  }

  /**
   * Campaign version of getMacroButtonProps().
   *
   * @param index The index of the macro button.
   * @param delim The delimiter to use.
   * @return Properties string.
   * @throws ParserException if an error occurs.
   */
  public Object getMacroButtonProps(int index, String delim, boolean gmPanel)
      throws ParserException {
    List<MacroButtonProperties> list =
        gmPanel
            ? MapTool.getCampaign().getGmMacroButtonPropertiesArray()
            : MapTool.getCampaign().getMacroButtonPropertiesArray();
    MacroButtonProperties mbp = null;
    for (MacroButtonProperties mb : list) {
      if (mb.getIndex() == index) {
        mbp = mb;
        break;
      }
    }
    if (mbp == null) {
      throw new ParserException(I18N.getText(KEY_OUT_OF_RANGE, "getMacroProps", index, "panel"));
    }
    if ("json".equals(delim)) {
      return macroButtonPropertiesToJSON(mbp);
    } else {
      return macroButtonPropertiesToString(mbp, delim.equals("") ? "," : delim);
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
      return macroButtonPropertiesToJSON(mbp);
    } else {
      return macroButtonPropertiesToString(mbp, delim.equals("") ? "," : delim);
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
    macroButtonPropertiesFromJSON(mbp, null, json);
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
      } else if ("displayHotkey".equalsIgnoreCase(key)) {
        mbp.setDisplayHotKey(boolVal(value));
      } else if ("fontColor".equalsIgnoreCase(key)) {
        mbp.setFontColorKey(value);
      } else if ("fontSize".equalsIgnoreCase(key)) {
        mbp.setFontSize(value);
      } else if ("group".equalsIgnoreCase(key)) {
        mbp.setGroup(value);
      } else if ("hotkey".equalsIgnoreCase(key)) {
        if (MacroButtonHotKeyManager.isHotkeyAssigned(value)) {
          value = MacroButtonHotKeyManager.HOTKEYS[0];
        }
        mbp.setHotKey(value);
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
   * Campaign version of getMacros().
   *
   * @param delim delimiter used to separate the values in the returned String List
   * @param list MacroButtonProperties list
   * @return The string containing the macro names.
   */
  private Object getMacros(String delim, List<MacroButtonProperties> list) {
    String[] names = new String[list.size()];
    JsonArray jsonArray = new JsonArray();

    for (int i = 0; i < list.size(); i++) {
      String label = list.get(i).getLabel();
      jsonArray.add(label);
      names[i] = label;
    }

    if ("json".equals(delim)) {
      return jsonArray;
    } else {
      return StringFunctions.getInstance().join(names, delim);
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
   * Campaign version of getMacroIndexes().
   *
   * @param label the label for the macro buttons to return
   * @param delim the delimiter separating the indexes. If "json", returns a JSON Array.
   * @param list the panel macro list
   * @return the indexes for the macro buttons.
   */
  private Object getMacroIndexes(String label, String delim, List<MacroButtonProperties> list) {
    List<String> strIndexes = new ArrayList<>();
    List<Integer> indexes = new ArrayList<>();
    for (MacroButtonProperties mbp : list) {
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
      if (delim.equals("")) delim = ",";
      return StringFunctions.getInstance().join(strIndexes, delim);
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
   * Campaign version of getMacroCommand.
   *
   * @param index the index of the macro.
   * @param gmPanel True for GM Panel, false for Campaign panel.
   * @return the macro command or "" if it has no command.
   * @throws ParserException if there is no macro at the index.
   */
  private String getMacroCommand(int index, boolean gmPanel) throws ParserException {
    List<MacroButtonProperties> list = getCampaignMbpList(gmPanel);
    MacroButtonProperties mbp = getCampaignMbpByIndexOrLabel(list, index, null).getRight();
    if (mbp == null) {
      throw new ParserException(
          I18N.getText(
              KEY_OUT_OF_RANGE, "getMacroCommand", index, gmPanel ? "gm panel" : "campaign panel"));
    }
    return mbp.getCommand();
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
    return mbp.getCommand();
  }

  /**
   * Campaign version of createMacro.
   *
   * @param param The arguments passed to the function.
   * @param gmPanel True to create on GM panel, flase to create on Campaign panel
   * @return the index of the newly created button.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal createMacro(List<Object> param, boolean gmPanel) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText(KEY_NO_PERM, "createMacro", gmPanel ? "gm panel" : "campaign panel"));
    }
    String label;
    String command;
    String prop;
    String delim;

    JsonObject jobj;
    MacroButtonProperties mbp =
        new MacroButtonProperties(
            gmPanel
                ? MapTool.getCampaign().getGmMacroButtonNextIndex()
                : MapTool.getCampaign().getMacroButtonNextIndex());
    try {
      jobj =
          JSONMacroFunctions.getInstance().asJsonElement(param.get(0).toString()).getAsJsonObject();
    } catch (IllegalStateException e) {
      jobj = null;
    }
    if (jobj == null) {
      FunctionUtil.checkNumberParam("createMacro", param, 2, 6);
      label = param.get(0).toString();
      command = param.get(1).toString();
      prop = param.size() > 2 ? param.get(2).toString() : null;
      delim = param.size() > 3 ? param.get(3).toString() : ";";
      mbp = macroButtonPropertiesFromStringProp(mbp, prop, delim);
      mbp.setLabel(label);
      mbp.setCommand(command);
    } else {
      FunctionUtil.checkNumberParam("createMacro", param, 1, 3);
      prop = param.get(0).toString();
      macroButtonPropertiesFromJSON(mbp, prop, null);
      mbp.setCommand(JSONMacroFunctions.getInstance().jsonToScriptString(jobj.get("command")));
    }
    mbp.setSaveLocation(gmPanel ? "GmPanel" : "CampaignPanel");
    List<MacroButtonProperties> list = new ArrayList<>(1);
    list.add(0, mbp);
    MapTool.getCampaign().addMacroButtonPropertiesAtNextIndex(list, gmPanel);

    return BigDecimal.valueOf(mbp.getIndex());
  }

  /**
   * Creates a macro button on a token. If There is only one argument in param, and it is a json
   * string, then the values in this are used to create the button on the token in context. If there
   * are two arguments and the first is a json string the second argument is the token to create the
   * button on. If the first argument is not a json string then it is the label for the new button.
   * The second argument is the command, if the third argument is specified then it is the
   * properties for the button. The fourth contains the delimiter for these properties (defaults to
   * ';' if not specified). The fifth argument is the token to create the macro button on, if no
   * token is specified it is created on the token in context.
   *
   * @param param The arguments passed to the function.
   * @return the index of the newly created button.
   * @throws ParserException if an error occurs.
   */
  private BigDecimal createMacro(MapToolVariableResolver resolver, List<Object> param)
      throws ParserException {
    FunctionUtil.checkNumberParam("createMacro", param, 1, 6);
    String label;
    String command;
    Token token;
    String prop;
    String delim;

    JsonObject jobj;
    MacroButtonProperties mbp;

    try {
      jobj =
          JSONMacroFunctions.getInstance().asJsonElement(param.get(0).toString()).getAsJsonObject();
    } catch (IllegalStateException e) {
      jobj = null;
    }

    if (jobj == null) {
      FunctionUtil.checkNumberParam("createMacro", param, 2, 6);
      label = param.get(0).toString();
      command = param.get(1).toString();
      prop = param.size() > 2 ? param.get(2).toString() : null;
      delim = param.size() > 3 ? param.get(3).toString() : ";";
      token = FunctionUtil.getTokenFromParam(resolver, "createMacro", param, 4, 5);
      mbp =
          macroButtonPropertiesFromStringProp(
              new MacroButtonProperties(token.getMacroNextIndex()), param.get(0).toString(), delim);
      mbp.setLabel(label);
      mbp.setCommand(command);
    } else {
      FunctionUtil.checkNumberParam("createMacro", param, 1, 3);
      prop = param.get(0).toString();
      delim = "json";
      token = FunctionUtil.getTokenFromParam(resolver, "createMacro", param, 1, 2);
      mbp =
          macroButtonPropertiesFromJSON(
              new MacroButtonProperties(token.getMacroNextIndex()), prop, null);
    }
    mbp.setTokenId(token); // Token Id is used in the exception messages of setMacroProps
    mbp.setSaveLocation("Token");

    // Sets the props, if any
    if (prop != null) setMacroProps(mbp, prop, delim);

    // Untrusted macros are set to be editable by players
    if (!MapTool.getParser().isMacroTrusted()) mbp.setAllowPlayerEdits(true);

    MapTool.serverCommand().updateTokenProperty(token, Token.Update.saveMacro, mbp);
    return BigDecimal.valueOf(mbp.getIndex());
  }

  /**
   * Campaign version of setMacroProps
   *
   * @param value Index or Label
   * @param props Props to set
   * @param delim Delimiter
   * @param gmPanel True for GM panel, false for Campaign panel
   * @return An empty string.
   * @throws ParserException on error
   */
  private String setMacroProps(Object value, String props, String delim, boolean gmPanel)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText(
              KEY_NO_PERM_COMMAND,
              "setMacroProps",
              value.toString(),
              gmPanel ? "gm panel" : "campaign panel"));
    }
    MacroButtonProperties mbp;
    List<MacroButtonProperties> list = getCampaignMbpList(gmPanel);
    ImmutablePair<Integer, MacroButtonProperties> pair;
    if ((value instanceof BigDecimal)) {
      int index = ((BigDecimal) value).intValue();
      pair = getCampaignMbpByIndexOrLabel(list, index, null);
      mbp = pair.getRight();
      if (mbp == null) {
        throw new ParserException(
            I18N.getText(
                KEY_OUT_OF_RANGE, "setMacroProps", index, gmPanel ? "gm panel" : "campaign panel"));
      }
      if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            I18N.getText(
                KEY_NO_PERM, "setMacroProps", index, gmPanel ? "gm panel" : "campaign panel"));
      }
      if (delim.equals("json")) {
        try {
          macroButtonPropertiesFromJSON(
              mbp, null, JSONMacroFunctions.getInstance().asJsonElement(props));
        } catch (IllegalStateException e) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.unknownType",
                  "setMacroProps",
                  index,
                  gmPanel ? "gm panel" : "campaign panel"));
        }
      } else {
        mbp = macroButtonPropertiesFromStringProp(mbp, props, delim);
      }
    } else {
      pair = getCampaignMbpByIndexOrLabel(list, null, value.toString());
      mbp = pair.getRight();
      if (mbp == null) {
        throw new ParserException(
            I18N.getText(
                KEY_OUT_OF_RANGE, "setMacroProps", value, gmPanel ? "gm panel" : "campaign panel"));
      }
      if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            I18N.getText(
                KEY_NO_PERM, "setMacroProps", value, gmPanel ? "gm panel" : "campaign panel"));
      }
      if (delim.equals("json")) {
        try {
          mbp =
              macroButtonPropertiesFromJSON(
                  mbp, null, JSONMacroFunctions.getInstance().asJsonElement(props));
        } catch (IllegalStateException e) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.json.unknownType",
                  "setMacroProps",
                  value,
                  gmPanel ? "gm panel" : "campaign panel"));
        }
      } else {
        mbp = macroButtonPropertiesFromStringProp(mbp, props, delim);
      }
    }
    MapTool.getCampaign().saveMacroButtonProperty(mbp, gmPanel);
    return "";
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
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.saveMacro, mbp);
    } else {
      List<MacroButtonProperties> mbpList = new ArrayList<>();
      for (MacroButtonProperties mbp : token.getMacroList(false)) {
        if (mbp.getLabel().equals(value.toString())) {
          if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
            String label = mbp.getLabel();
            int index = mbp.getIndex();
            MapTool.addLocalMessage(
                I18N.getText(KEY_NO_PERM_OTHER, "setMacroProps", label, index, token.getName()));
          } else {
            setMacroProps(mbp, props, delim);
            mbpList.add(mbp);
          }
        }
      }
      // Replaces the matching macros with the new versions
      MapTool.serverCommand()
          .updateTokenProperty(token, Token.Update.saveMacroList, mbpList, false);
    }
    return "";
  }

  /**
   * Campaign version of setMacroCommand().
   *
   * @param index the index of the macro button
   * @param command a string containing the command to set on the macro button
   * @param gmPanel True for GM panel, false for Campaign panel.
   * @return An empty string.
   * @throws ParserException If an error occurs.
   */
  private String setMacroCommand(int index, String command, boolean gmPanel)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText(
              KEY_NO_PERM_COMMAND,
              "setMacroCommand",
              index,
              gmPanel ? "gm panel" : "campaign panel"));
    }
    List<MacroButtonProperties> list = getCampaignMbpList(gmPanel);
    ImmutablePair<Integer, MacroButtonProperties> pair =
        getCampaignMbpByIndexOrLabel(list, index, null);
    MacroButtonProperties mbp = pair.right;
    if (mbp == null) {
      throw new ParserException(
          I18N.getText(
              KEY_OUT_OF_RANGE, "setMacroCommand", index, gmPanel ? "gm panel" : "campaign panel"));
    }
    mbp.setCommand(command);
    list.set(pair.left, mbp);
    if (gmPanel) {
      MapTool.getCampaign().setGmMacroButtonPropertiesArray(list);
    } else {
      MapTool.getCampaign().setMacroButtonPropertiesArray(list);
    }
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
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.saveMacro, mbp);
    return "";
  }

  /**
   * Campaign version of removeMacro().
   *
   * @param index the index of the macro button.
   * @param gmPanel True for GM panel, false for Campaign panel.
   * @return the details of the button that was removed.
   * @throws ParserException if the index is invalid.
   */
  private String removeMacro(int index, boolean gmPanel) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText(
              KEY_NO_PERM_COMMAND, "removeMacro", index, gmPanel ? "gm panel" : "campaign panel"));
    }
    List<MacroButtonProperties> list = getCampaignMbpList(gmPanel);
    ImmutablePair<Integer, MacroButtonProperties> pair =
        getCampaignMbpByIndexOrLabel(list, index, null);
    if (pair.left == null) {
      throw new ParserException(
          I18N.getText(
              KEY_OUT_OF_RANGE, "removeMacro", index, gmPanel ? "gm panel" : "campaign panel"));
    } else {
      String label = pair.right.getLabel();
      int listIndex = pair.left;
      list.remove(listIndex);
      if (gmPanel) {
        MapTool.getCampaign().setGmMacroButtonPropertiesArray(list);
      } else {
        MapTool.getCampaign().setMacroButtonPropertiesArray(list);
      }
      AbstractMacroPanel macroPanel =
          gmPanel ? MapTool.getFrame().getGmPanel() : MapTool.getFrame().getCampaignPanel();
      MapTool.getFrame().updateKeyStrokes(); // ensure hotkeys are updated
      macroPanel.reset();
      return "Removed macro button "
          + label
          + "(index = "
          + index
          + ") from "
          + (gmPanel ? "gm panel" : "campaign panel");
    }
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
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.deleteMacro, index);
    MapTool.getFrame().updateKeyStrokes(); // ensure hotkeys are updated
    return "Removed macro button " + label + "(index = " + index + ") from " + token.getName();
  }

  /**
   * Campaign version of getMacroGroup
   *
   * @param group the name of the macro group to get the macro button indexes from.
   * @param delim the delimiter used in the string list returned, defaults to ",".
   * @param gmPanel True for GM panel, false for Campaign panel.
   * @return The string containing the macro names.
   */
  private Object getMacroGroup(String group, String delim, boolean gmPanel) {
    // Has to be a string or the list functions won't like it :\
    List<String> strIndexes = new LinkedList<>();
    List<Integer> indexes = new LinkedList<>();
    List<MacroButtonProperties> mbpList = getCampaignMbpList(gmPanel);
    delim = delim.isEmpty() ? "," : delim;
    for (MacroButtonProperties props : mbpList) {
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

  /**
   * Gets the index of the macros for a token in the specified group. *
   *
   * @param group the name of the macro group to get the macro button indexes from.
   * @param delim the delimiter used in the string list returned, defaults to ",".
   * @param token the token that the macro group is located on.
   * @return The string containing the macro names.
   */
  private Object getMacroGroup(String group, String delim, Token token) {
    // Has to be a string or the list functions won't like it :\
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

  /**
   * Getter for lists of macros in campaign panels.
   *
   * @param gmPanel True for GM panel, False for Campaign panel.
   * @return MacroButtonProperties list
   */
  private List<MacroButtonProperties> getCampaignMbpList(boolean gmPanel) {
    return gmPanel
        ? MapTool.getCampaign().getGmMacroButtonPropertiesArray()
        : MapTool.getCampaign().getMacroButtonPropertiesArray();
  }

  /**
   * Get the MacroButtonProperties from MacroButtonProperties lists.
   *
   * @param list The list to search
   * @param index Macro index to find. NULL to search by label
   * @param label Macro label to find. NULL when searching by index.
   * @return an Immutable pair of list index and the MacroButtonProperties
   */
  private ImmutablePair<Integer, MacroButtonProperties> getCampaignMbpByIndexOrLabel(
      List<MacroButtonProperties> list, Integer index, String label) {
    for (int i = 0; i < list.size(); i++) {
      MacroButtonProperties mbp = list.get(i);
      if (index == null) {
        if (mbp.getLabel().equals(label)) {
          return new ImmutablePair<>(i, mbp);
        }
      } else {
        if (mbp.getIndex() == index) {
          return new ImmutablePair<>(i, mbp);
        }
      }
    }
    return new ImmutablePair<>(null, null);
  }

  /**
   * Returns string constructed from Macro Button Properties
   *
   * @param mbp Button props
   * @param delim Delimiter to use
   * @return String
   */
  private String macroButtonPropertiesToString(MacroButtonProperties mbp, String delim) {
    StringBuilder sb = new StringBuilder();
    sb.append("applyToSelected=").append(mbp.getApplyToTokens()).append(delim);
    sb.append("autoExecute=").append(mbp.getAutoExecute()).append(delim);
    sb.append("color=").append(mbp.getColorKey()).append(delim);
    sb.append("displayHotkey=").append(mbp.getDisplayHotKey()).append(delim);
    sb.append("fontColor=").append(mbp.getFontColorKey()).append(delim);
    sb.append("fontSize=").append(mbp.getFontSize()).append(delim);
    sb.append("group=").append(mbp.getGroup()).append(delim);
    sb.append("hotkey=").append(mbp.getHotKey()).append(delim);
    sb.append("includeLabel=").append(mbp.getIncludeLabel()).append(delim);
    sb.append("index=").append(mbp.getIndex()).append(delim);
    sb.append("label=").append(mbp.getLabel()).append(delim);
    sb.append("maxWidth=").append(mbp.getMaxWidth()).append(delim);
    sb.append("minWidth=").append(mbp.getMinWidth()).append(delim);
    sb.append("playerEditable=").append(mbp.getAllowPlayerEdits()).append(delim);
    sb.append("sortBy=").append(mbp.getSortby()).append(delim);
    sb.append("tooltip=").append(mbp.getToolTip()).append(delim);
    return sb.toString();
  }

  /**
   * Builds MacroButtonProperties from JSON, either as String or JsonElement.
   *
   * @param mbp MacroButtonProperties to add properties to
   * @param propString Set to null to build from json
   * @param json Set to null to build from propString
   * @return MacroButtonProperties
   * @throws ParserException If missing label or permission failure;
   */
  private MacroButtonProperties macroButtonPropertiesFromJSON(
      MacroButtonProperties mbp, String propString, JsonElement json) throws ParserException {
    if (json == null) json = JSONMacroFunctions.getInstance().asJsonElement(propString);
    JsonObject jobj = json.getAsJsonObject();
    if (jobj != null) {
      if (!jobj.has("label") && mbp.getLabel().isEmpty())
        throw new ParserException(I18N.getText(KEY_MISSING_LABEL, "createMacro"));
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
        } else if ("displayHotkey".equalsIgnoreCase(key)) {
          mbp.setDisplayHotKey(boolVal(value));
        } else if ("fontColor".equalsIgnoreCase(key)) {
          mbp.setFontColorKey(value);
        } else if ("fontSize".equalsIgnoreCase(key)) {
          mbp.setFontSize(value);
        } else if ("group".equalsIgnoreCase(key)) {
          mbp.setGroup(value);
        } else if ("hotkey".equalsIgnoreCase(key)) {
          if (MacroButtonHotKeyManager.isHotkeyAssigned(value)) {
            value = MacroButtonHotKeyManager.HOTKEYS[0];
          }
          mbp.setHotKey(value);
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
                I18N.getText(
                    KEY_NO_PERM_EDITABLE, "setMacroProps", index, mbp.getToken().getName()));
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

    return mbp;
  }

  /**
   * Builds MacroButtonProperties from StringProp.
   *
   * @param mbp MacroButtonProperties to add properties to
   * @param propString Set to null to build from json
   * @param delim Delimiter used in propString (NOT "json")
   * @return MacroButtonProperties
   * @throws ParserException If missing label or permission failure
   */
  private MacroButtonProperties macroButtonPropertiesFromStringProp(
      MacroButtonProperties mbp, String propString, String delim) throws ParserException {
    JsonElement json =
        JSONMacroFunctions.getInstance().getJsonObjectFunctions().fromStrProp(propString, delim);
    return macroButtonPropertiesFromJSON(mbp, null, json);
  }

  /**
   * Builds JSON from Macro Button Props
   *
   * @param mbp Button props
   * @return JSON Object
   */
  private JsonObject macroButtonPropertiesToJSON(MacroButtonProperties mbp) {
    JsonObject props = new JsonObject();
    props.addProperty("applyToSelected", mbp.getApplyToTokens());
    props.addProperty("autoExecute", mbp.getAutoExecute());
    props.addProperty("color", mbp.getColorKey());
    props.addProperty("displayHotkey", mbp.getDisplayHotKey());
    props.addProperty("command", mbp.getCommand());
    props.addProperty("fontColor", mbp.getFontColorKey());
    props.addProperty("fontSize", mbp.getFontSize());
    props.addProperty("group", mbp.getGroup());
    props.addProperty("hotkey", mbp.getHotKey());
    props.addProperty("includeLabel", mbp.getIncludeLabel());
    props.addProperty("index", mbp.getIndex());
    props.addProperty("label", mbp.getLabel());
    props.addProperty("minWidth", mbp.getMinWidth());
    props.addProperty("maxWidth", mbp.getMaxWidth());
    props.addProperty("playerEditable", mbp.getAllowPlayerEdits());
    props.addProperty("sortBy", mbp.getSortby());
    props.addProperty("tooltip", mbp.getToolTip());

    JsonArray compare = new JsonArray();
    if (mbp.getCompareGroup()) compare.add("group");
    if (mbp.getCompareSortPrefix()) compare.add("sortPrefix");
    if (mbp.getCompareCommand()) compare.add("command");
    if (mbp.getCompareIncludeLabel()) compare.add("includeLabel");
    if (mbp.getCompareAutoExecute()) compare.add("autoExecute");
    if (mbp.getCompareApplyToSelectedTokens()) compare.add("applyToSelected");

    props.add("compare", compare);

    JsonObject propsMetadata = new JsonObject();
    propsMetadata.addProperty("uuid", mbp.getMacroUUID());
    propsMetadata.addProperty(
        "commandChecksum", new MD5Key(mbp.getCommand().getBytes()).toString());
    propsMetadata.addProperty("propsChecksum", new MD5Key(props.toString().getBytes()).toString());
    props.add("metadata", propsMetadata);

    return props;
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
}
