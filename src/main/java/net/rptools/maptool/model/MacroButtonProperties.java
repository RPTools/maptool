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
package net.rptools.maptool.model;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.JTextComponent;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.MacroButtonHotKeyManager;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This (data)class is used by all Macro Buttons, including campaign, global and token macro
 * buttons.
 *
 * @see net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton
 */
public class MacroButtonProperties implements Comparable<MacroButtonProperties> {

  private static final Logger log = LogManager.getLogger(MacroButtonProperties.class);

  // private transient static final List<String> HTMLColors = Arrays.asList("aqua", "black", "blue",
  // "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "purple", "red", "silver",
  // "teal",
  // "white", "yellow");

  private String macroUUID =
      UUID.randomUUID()
          .toString(); // Jamz: Why a String and not UUID? Because stupid Hessian can't serialize
  // UUID, ug.

  private transient MacroButton button;
  private transient GUID tokenId;
  private String saveLocation;
  private int index;
  private String colorKey;
  private String hotKey;
  private String command;
  private String label;
  private String group;
  private String sortby;
  private boolean autoExecute;
  private boolean includeLabel; // include the macro label when printing output?
  private boolean
      applyToTokens; // when the button is clicked it will impersonate every selected token when
  // executing the macro
  private String fontColorKey;
  private String fontSize;
  private String minWidth;
  private String maxWidth;
  private Boolean allowPlayerEdits = true;
  private String toolTip;
  private Boolean displayHotKey = true;

  // constructor that creates a new instance, doesn't auto-save
  public MacroButtonProperties(
      int index,
      String colorKey,
      String hotKey,
      String command,
      String label,
      String group,
      String sortby,
      boolean autoExecute,
      boolean includeLabel,
      boolean applyToTokens,
      String fontColorKey,
      String fontSize,
      String minWidth,
      String maxWidth,
      String toolTip,
      boolean displayHotKey) {
    setIndex(index);
    setColorKey(colorKey);
    setHotKey(hotKey);
    setCommand(command);
    setLabel(label);
    setGroup(group);
    setSortby(sortby);
    setAutoExecute(autoExecute);
    setIncludeLabel(includeLabel);
    setApplyToTokens(applyToTokens);
    setFontColorKey(fontColorKey);
    setFontSize(fontSize);
    setMinWidth(minWidth);
    setMaxWidth(maxWidth);
    setButton(null);
    setTokenId((GUID) null);
    setSaveLocation("");
    setAllowPlayerEdits(AppPreferences.getAllowPlayerMacroEditsDefault());
    setDisplayHotKey(displayHotKey);
    setCompareGroup(true);
    setCompareSortPrefix(true);
    setCompareCommand(true);
    setCompareIncludeLabel(true);
    setCompareAutoExecute(true);
    setCompareApplyToSelectedTokens(true);
    setToolTip(toolTip);
  }

  // constructor that creates a new instance, doesn't auto save
  public MacroButtonProperties(int index) {
    setIndex(index);
    setColorKey("");
    setHotKey(MacroButtonHotKeyManager.HOTKEYS[0]);
    setCommand("");
    setLabel("(new)");
    setGroup("");
    setSortby("");
    setAutoExecute(true);
    setIncludeLabel(false);
    setApplyToTokens(false);
    setFontColorKey("");
    setFontSize("");
    setMinWidth("");
    setMaxWidth("");
    setButton(null);
    setTokenId((GUID) null);
    setSaveLocation("");
    setAllowPlayerEdits(AppPreferences.getAllowPlayerMacroEditsDefault());
    setDisplayHotKey(true);
    setCompareGroup(true);
    setCompareSortPrefix(true);
    setCompareCommand(true);
    setCompareIncludeLabel(true);
    setCompareAutoExecute(true);
    setCompareApplyToSelectedTokens(true);
    setToolTip(null);
  }

  // constructor for creating a new button in a specific button group, auto-saves
  public MacroButtonProperties(String panelClass, int index, String group) {
    this(index);
    setSaveLocation(panelClass);
    setGroup(group);
    setAllowPlayerEdits(AppPreferences.getAllowPlayerMacroEditsDefault());
    setDisplayHotKey(true);
    setCompareGroup(true);
    setCompareSortPrefix(true);
    setCompareCommand(true);
    setCompareIncludeLabel(true);
    setCompareAutoExecute(true);
    setCompareApplyToSelectedTokens(true);
    setToolTip(null);
    save();
  }

  // constructor for creating a new token button in a specific button group, auto-saves
  public MacroButtonProperties(Token token, int index, String group) {
    this(index);
    setSaveLocation("Token");
    setTokenId(token);
    setGroup(group);
    setAllowPlayerEdits(AppPreferences.getAllowPlayerMacroEditsDefault());
    setDisplayHotKey(true);
    setCompareGroup(true);
    setCompareSortPrefix(true);
    setCompareCommand(true);
    setCompareIncludeLabel(true);
    setCompareAutoExecute(true);
    setCompareApplyToSelectedTokens(true);
    setToolTip(null);
    save();
  }

  // constructor for creating a new copy of an existing button, auto-saves
  public MacroButtonProperties(String panelClass, int index, MacroButtonProperties properties) {
    this(index);
    setSaveLocation(panelClass);
    setColorKey(properties.getColorKey());
    // use the default hot key
    setCommand(properties.getCommand());
    setLabel(properties.getLabel());
    setGroup(properties.getGroup());
    setSortby(properties.getSortby());
    setAutoExecute(properties.getAutoExecute());
    setIncludeLabel(properties.getIncludeLabel());
    setApplyToTokens(properties.getApplyToTokens());
    setFontColorKey(properties.getFontColorKey());
    setFontSize(properties.getFontSize());
    setMinWidth(properties.getMinWidth());
    setMaxWidth(properties.getMaxWidth());
    setAllowPlayerEdits(properties.getAllowPlayerEdits());
    setDisplayHotKey(properties.getDisplayHotKey());
    setCompareIncludeLabel(properties.getCompareIncludeLabel());
    setCompareAutoExecute(properties.getCompareAutoExecute());
    setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
    setCompareGroup(properties.getCompareGroup());
    setCompareSortPrefix(properties.getCompareSortPrefix());
    setCompareCommand(properties.getCompareCommand());
    String tt = properties.getToolTip();
    setToolTip(tt);
    save();
  }

  /**
   * Constructor for creating a new copy of an existing token button, can auto-saves
   *
   * @param token the token for which to create the copy
   * @param index the index of the next macro
   * @param properties the MacroButtonProperties of the copied button
   * @param autoSave should the macro be autosaved or not?
   */
  public MacroButtonProperties(
      Token token, int index, MacroButtonProperties properties, boolean autoSave) {
    this(index);
    setSaveLocation("Token");
    setTokenId(token);
    setColorKey(properties.getColorKey());
    // use the default hot key
    setCommand(properties.getCommand());
    setLabel(properties.getLabel());
    setGroup(properties.getGroup());
    setSortby(properties.getSortby());
    setAutoExecute(properties.getAutoExecute());
    setIncludeLabel(properties.getIncludeLabel());
    setApplyToTokens(properties.getApplyToTokens());
    setFontColorKey(properties.getFontColorKey());
    setFontSize(properties.getFontSize());
    setMinWidth(properties.getMinWidth());
    setMaxWidth(properties.getMaxWidth());
    setAllowPlayerEdits(properties.getAllowPlayerEdits());
    setDisplayHotKey(properties.getDisplayHotKey());
    setCompareIncludeLabel(properties.getCompareIncludeLabel());
    setCompareAutoExecute(properties.getCompareAutoExecute());
    setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
    setCompareGroup(properties.getCompareGroup());
    setCompareSortPrefix(properties.getCompareSortPrefix());
    setCompareCommand(properties.getCompareCommand());
    String tt = properties.getToolTip();
    setToolTip(tt);
    if (autoSave) {
      save();
    }
  }

  /**
   * Constructor for creating a new copy of an existing token button, auto-saves
   *
   * @param token the token for which to create the copy
   * @param index the index of the next macro
   * @param properties the MacroButtonProperties of the copied button
   */
  public MacroButtonProperties(Token token, int index, MacroButtonProperties properties) {
    this(token, index, properties, true);
  }

  // constructor for creating common macro buttons on selection panel
  public MacroButtonProperties(int index, MacroButtonProperties properties) {
    this(index);
    setTokenId((Token) null);
    setColorKey(properties.getColorKey());
    // use the default hot key
    setCommand(properties.getCommand());
    setLabel(properties.getLabel());
    setGroup(properties.getGroup());
    setSortby(properties.getSortby());
    setAutoExecute(properties.getAutoExecute());
    setIncludeLabel(properties.getIncludeLabel());
    setApplyToTokens(properties.getApplyToTokens());
    setFontColorKey(properties.getFontColorKey());
    setFontSize(properties.getFontSize());
    setMinWidth(properties.getMinWidth());
    setMaxWidth(properties.getMaxWidth());
    setAllowPlayerEdits(properties.getAllowPlayerEdits());
    setDisplayHotKey(properties.getDisplayHotKey());
    setCompareIncludeLabel(properties.getCompareIncludeLabel());
    setCompareAutoExecute(properties.getCompareAutoExecute());
    setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
    setCompareGroup(properties.getCompareGroup());
    setCompareSortPrefix(properties.getCompareSortPrefix());
    setCompareCommand(properties.getCompareCommand());
    setToolTip(properties.getToolTip());
    commonMacro = true;
  }

  public MacroButtonProperties(Token token, Map<String, String> props) {
    this(
        props.containsKey("index")
            ? Integer.parseInt(props.get("index"))
            : token.getMacroNextIndex());
    setTokenId(token);
    if (props.containsKey("saveLocation")) setSaveLocation(props.get("saveLocation"));
    if (props.containsKey("colorKey")) setColorKey(props.get("colorKey"));
    if (props.containsKey("hotKey")) setHotKey(props.get("hotKey"));
    if (props.containsKey("command")) setCommand(props.get("command"));
    if (props.containsKey("label")) setLabel(props.get("label"));
    if (props.containsKey("group")) setGroup(props.get("group"));
    if (props.containsKey("sortby")) setSortby(props.get("sortby"));
    if (props.containsKey("autoExecute"))
      setAutoExecute(Boolean.parseBoolean(props.get("autoExecute")));
    if (props.containsKey("includeLabel"))
      setIncludeLabel(Boolean.parseBoolean(props.get("includeLabel")));
    if (props.containsKey("applyToTokens"))
      setApplyToTokens(Boolean.parseBoolean(props.get("applyToTokens")));
    if (props.containsKey("fontColorKey")) setFontColorKey(props.get("fontColorKey"));
    if (props.containsKey("fontSize")) setFontSize(props.get("fontSize"));
    if (props.containsKey("minWidth")) setMinWidth(props.get("minWidth"));
    if (props.containsKey("maxWidth")) setMaxWidth(props.get("maxWidth"));
    if (props.containsKey("allowPlayerEdits"))
      setAllowPlayerEdits(Boolean.valueOf(props.get("allowPlayerEdits")));
    if (props.containsKey("displayHotKey"))
      setAllowPlayerEdits(Boolean.valueOf(props.get("displayHotKey")));
    if (props.containsKey("toolTip")) setToolTip(props.get("toolTip"));
    if (props.containsKey("commonMacro")) setCommonMacro(Boolean.valueOf(props.get("commonMacro")));
    if (props.containsKey("compareGroup"))
      setCompareGroup(Boolean.valueOf(props.get("compareGroup")));
    if (props.containsKey("compareSortPrefix"))
      setCompareSortPrefix(Boolean.valueOf(props.get("compareSortPrefix")));
    if (props.containsKey("compareCommand"))
      setCompareCommand(Boolean.valueOf(props.get("compareCommand")));
    if (props.containsKey("compareIncludeLabel"))
      setCompareIncludeLabel(Boolean.valueOf(props.get("compareIncludeLabel")));
    if (props.containsKey("compareAutoExecute"))
      setCompareAutoExecute(Boolean.valueOf(props.get("compareAutoExecute")));
    if (props.containsKey("compareApplyToSelectedTokens"))
      setCompareApplyToSelectedTokens(Boolean.valueOf(props.get("compareApplyToSelectedTokens")));
  }

  public void save() {
    if (saveLocation.equals("Token") && tokenId != null) {
      Token token = getToken();
      if (token != null) {
        token.saveMacroButtonProperty(this);
      } else {
        MapTool.showError(I18N.getText("msg.error.macro.buttonNullToken", getLabel(), tokenId));
      }
    } else if (saveLocation.equals("GlobalPanel")) {
      MacroButtonPrefs.savePreferences(this);
    } else if (saveLocation.equals("CampaignPanel")) {
      MapTool.getCampaign().saveMacroButtonProperty(this);
    } else if (saveLocation.equals("GmPanel")) {
      MapTool.getCampaign().saveGmMacroButtonProperty(this);
    }
  }

  public void executeMacro() {
    executeCommand(tokenId);
  }

  /**
   * The top level method for executing macros with the given list of tokens as context. In essence,
   * this method calls the macro once for each token, and the token becomes the "impersonated" token
   * for the duration of the macro barring any use of the <b>token()</b> or <b>switchToken()</b>
   * roll options inside the macro itself.
   *
   * @param tokenList
   */
  public void executeMacro(Collection<Token> tokenList) {
    if (tokenList == null || tokenList.size() == 0) {
      executeCommand(null);
    } else if (commonMacro) {
      executeCommonMacro(tokenList);
    } else {
      if (tokenList.size() > 0) {
        for (Token token : tokenList) {
          executeCommand(token.getId());
        }
      }
    }
  }

  private void executeCommonMacro(Collection<Token> tokenList) {
    /*
     * This is actually one of the "common macro" buttons that are on the selection panel so we need to handle this case a little differently. If apply to all tokens is checked by the user then we
     * need to check that the command is part of the common values otherwise it would cause unexpected things to occur.
     */
    if (applyToTokens) {
      if (!compareCommand) {
        MapTool.showError("msg.error.cantApplyMacroToSelected");
        return;
      }
    }

    if (compareCommand) {
      for (Token token : tokenList) {
        executeCommand(token.getId());
      }
    } else {
      // We need to find the "matching" button for each token and ensure to run that one.
      for (Token nextToken : tokenList) {
        for (MacroButtonProperties nextMacro : nextToken.getMacroList(true)) {
          if (nextMacro.hashCodeForComparison() == hashCodeForComparison()) {
            nextMacro.executeCommand(nextToken.getId());
          }
        }
      }
    }
  }

  public void executeMacro(GUID tokenId) {
    executeCommand(tokenId);
  }

  private void executeCommand(GUID tokenId) {
    if (getCommand() != null) {

      String impersonatePrefix = "";
      if (tokenId != null) {
        impersonatePrefix = "/im " + tokenId + ":";
      }

      JTextComponent commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
      String oldText = commandArea.getText();

      if (getIncludeLabel()) {
        String commandToExecute = getLabel();
        commandArea.setText(impersonatePrefix + commandToExecute);

        MapTool.getFrame().getCommandPanel().commitCommand();
      }

      String commandsToExecute[] = parseMultiLineCommand(getCommand());

      ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
      Zone zone = (zr == null ? null : zr.getZone());
      Token contextToken = (zone == null ? null : zone.getToken(tokenId));
      String loc;
      for (String command : commandsToExecute) {
        // If we aren't auto execute, then append the text instead of replace it
        commandArea.setText(impersonatePrefix + (!getAutoExecute() ? oldText + " " : "") + command);
        if (getAutoExecute()) {
          boolean trusted = false;
          if (allowPlayerEdits == null) {
            allowPlayerEdits = false;
          }
          if (saveLocation.equals("CampaignPanel") || !allowPlayerEdits) {
            trusted = true;
          }
          if (saveLocation.equals("GlobalPanel")) {
            loc = "global";
            trusted = MapTool.getPlayer().isGM();
          } else if (saveLocation.equals("CampaignPanel")) {
            loc = "campaign";
          } else if (saveLocation.equals("GmPanel")) {
            loc = "gm";
            trusted = MapTool.getPlayer().isGM();
          } else if (contextToken != null) {
            // Should this IF stmt really be:
            // contextToken.matches("^[^:\\s]+:")
            // That would match any token with a string of text followed by a colon
            // with no spaces in front of the colon.
            if (contextToken.getName().toLowerCase().startsWith("lib:")) {
              loc = contextToken.getName();
            } else {
              loc = "Token:" + contextToken.getName();
            }
          } else {
            loc = MapToolLineParser.CHAT_INPUT;
          }
          MapToolMacroContext newMacroContext = new MapToolMacroContext(label, loc, trusted, index);
          MapTool.getFrame().getCommandPanel().commitCommand(newMacroContext);
        }
      }
      commandArea.requestFocusInWindow();
    }
  }

  private String[] parseMultiLineCommand(String multiLineCommand) {

    // lookahead for new macro "/" after "\n" to prevent unnecessary splitting.
    String pattern = "\n(?=/)";
    String[] parsedCommand = multiLineCommand.split(pattern);

    return parsedCommand;
  }

  public Token getToken() {
    Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(this.tokenId);

    // If token not in current map, look for token in other maps.
    if (token == null) {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        token = zr.getZone().getToken(this.tokenId);
        if (token != null) {
          break;
        }
      }
    }
    return token;
  }

  public void setTokenId(Token token) {
    if (token == null) {
      this.tokenId = null;
    } else {
      this.tokenId = token.getId();
    }
  }

  public void setTokenId(GUID tokenId) {
    this.tokenId = tokenId;
  }

  public void setSaveLocation(String saveLocation) {
    if (saveLocation.equals("ImpersonatePanel") || saveLocation.equals("SelectionPanel")) {
      this.saveLocation = "Token";
    } else {
      this.saveLocation = saveLocation;
    }
  }

  public void setButton(MacroButton button) {
    this.button = button;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String getColorKey() {
    if (colorKey == null || colorKey.equals("")) {
      return "default";
    }
    return colorKey;
  }

  public void setColorKey(String colorKey) {
    if (MapToolUtil.getColor(colorKey) != null) this.colorKey = colorKey;
  }

  public String getHotKey() {
    return hotKey;
  }

  public void setHotKey(String hotKey) {
    this.hotKey = hotKey;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getLabel() {
    return (label == null ? "" : label);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getGroup() {
    return (group == null ? "" : group);
  }

  public String getGroupForDisplay() {
    return this.group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getSortby() {
    return (sortby == null ? "" : sortby);
  }

  public void setSortby(String sortby) {
    this.sortby = sortby;
  }

  public boolean getAutoExecute() {
    return autoExecute;
  }

  public void setAutoExecute(boolean autoExecute) {
    this.autoExecute = autoExecute;
  }

  public boolean getIncludeLabel() {
    return includeLabel;
  }

  public void setIncludeLabel(boolean includeLabel) {
    this.includeLabel = includeLabel;
  }

  public boolean getApplyToTokens() {
    return applyToTokens;
  }

  public void setApplyToTokens(boolean applyToTokens) {
    this.applyToTokens = applyToTokens;
  }

  public static String[] getFontColors() {
    // return (String[]) HTMLColors.toArray();
    String[] array = MapToolUtil.getColorNames().toArray(new String[0]);
    return array;
  }

  /**
   * Returns the font color of this button as an HTML string. It might be one of the 16 colors
   * defined by the W3C as a standard HTML color (see <code>COLOR_MAP_HTML</code> for a list), but
   * if it's not then the color is converted to CSS format <b>#FF00FF</b> format and that string is
   * returned.
   *
   * @return string of the color
   */
  public String getFontColorAsHtml() {
    Color c = null;
    String font = getFontColorKey();
    if (MapToolUtil.isHtmlColor(font)) {
      return font;
    }
    c = MapToolUtil.getColor(font);
    if (c != null) {
      return "#" + Integer.toHexString(c.getRGB()).substring(2);
    }
    return "black";
  }

  public String getFontColorKey() {
    if (fontColorKey == null || StringUtil.isEmpty(fontColorKey)) {
      fontColorKey = "black";
      return fontColorKey;
    }
    Color c = MapToolUtil.getColor(fontColorKey);
    if (c != null) {
      return fontColorKey;
    }
    return "black";
  }

  public void setFontColorKey(String fontColorKey) {
    if (MapToolUtil.getColor(fontColorKey) != null) this.fontColorKey = fontColorKey;
  }

  public String getFontSize() {
    return (fontSize == null || fontSize.equals("") ? "1.00em" : fontSize);
  }

  public void setFontSize(String fontSize) {
    this.fontSize = (fontSize == null || fontSize.equals("") ? "1.00em" : fontSize);
  }

  public String getMinWidth() {
    return (minWidth == null ? "" : minWidth);
  }

  public void setMinWidth(String minWidth) {
    this.minWidth = minWidth;
  }

  public String getMaxWidth() {
    return (maxWidth == null ? "" : maxWidth);
  }

  public void setMaxWidth(String maxWidth) {
    this.maxWidth = maxWidth;
  }

  public Boolean getAllowPlayerEdits() {
    return allowPlayerEdits;
  }

  public void setAllowPlayerEdits(Boolean value) {
    allowPlayerEdits = value;
  }

  public Boolean getDisplayHotKey() {
    if (displayHotKey == null) displayHotKey = true;

    return displayHotKey;
  }

  public void setDisplayHotKey(Boolean value) {
    displayHotKey = value;
  }

  public String getSaveLocation() {
    return saveLocation;
  }

  public void setToolTip(String tt) {
    toolTip = (tt == null ? "" : tt);
  }

  public String getToolTip() {
    if (toolTip == null) toolTip = "";
    return toolTip;
  }

  public String getEvaluatedToolTip() {

    if (toolTip == null) {
      return "";
    }

    if (!toolTip.trim().startsWith("{") && !toolTip.trim().startsWith("[")) {
      return toolTip;
    }

    Token token = null;
    if (tokenId != null) {
      token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
    }
    try {
      MapToolMacroContext context =
          new MapToolMacroContext("ToolTip", token != null ? token.getName() : "", false, index);
      if (log.isDebugEnabled()) {
        log.debug(
            "Evaluating toolTip: "
                + (token != null ? "for token " + token.getName() + "(" + token.getId() + ")" : "")
                + "----------------------------------------------------------------------------------");
      }
      return MapTool.getParser().parseLine(token, toolTip, context);
    } catch (ParserException pe) {
      return toolTip;
    }
  }

  public boolean isDuplicateMacro(String source, Token token) {
    int macroHashCode = hashCodeForComparison();
    List<MacroButtonProperties> existingMacroList = null;
    if (source.equalsIgnoreCase("CampaignPanel")) {
      existingMacroList = MapTool.getCampaign().getMacroButtonPropertiesArray();
    } else if (source.equalsIgnoreCase("GmPanel")) {
      existingMacroList = MapTool.getCampaign().getGmMacroButtonPropertiesArray();
    } else if (source.equalsIgnoreCase("GlobalPanel")) {
      existingMacroList = MacroButtonPrefs.getButtonProperties();
    } else if (token != null) {
      existingMacroList = token.getMacroList(false);
    } else {
      return false;
    }
    for (MacroButtonProperties existingMacro : existingMacroList) {
      if (existingMacro.hashCodeForComparison() == macroHashCode) {
        return true;
      }
    }
    return false;
  }

  public void reset() {
    colorKey = "default";
    hotKey = MacroButtonHotKeyManager.HOTKEYS[0];
    command = "";
    label = String.valueOf(index);
    group = "";
    sortby = "";
    autoExecute = true;
    includeLabel = false;
    applyToTokens = false;
    fontColorKey = "black";
    fontSize = "";
    minWidth = "";
    maxWidth = "";
    toolTip = "";
  }

  // TODO: may have to rewrite hashcode and equals to only take index into account
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MacroButtonProperties that = (MacroButtonProperties) o;

    if (autoExecute != that.autoExecute) {
      return false;
    }
    if (includeLabel != that.includeLabel) {
      return false;
    }
    if (applyToTokens != that.applyToTokens) {
      return false;
    }
    if (index != that.index) {
      return false;
    }
    if (colorKey != null ? !colorKey.equals(that.colorKey) : that.colorKey != null) {
      return false;
    }
    if (command != null ? !command.equals(that.command) : that.command != null) {
      return false;
    }
    if (hotKey != null ? !hotKey.equals(that.hotKey) : that.hotKey != null) {
      return false;
    }
    if (label != null ? !label.equals(that.label) : that.label != null) {
      return false;
    }
    if (group != null ? !group.equals(that.group) : that.group != null) {
      return false;
    }
    if (sortby != null ? !sortby.equals(that.sortby) : that.sortby != null) {
      return false;
    }
    if (fontColorKey != null
        ? !fontColorKey.equals(that.fontColorKey)
        : that.fontColorKey != null) {
      return false;
    }
    if (fontSize != null ? !fontSize.equals(that.fontSize) : that.fontSize != null) {
      return false;
    }
    if (minWidth != null ? !minWidth.equals(that.minWidth) : that.minWidth != null) {
      return false;
    }
    if (maxWidth != null ? !maxWidth.equals(that.maxWidth) : that.maxWidth != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() { // modified so longest strings are at the end
    int result;
    result = index;
    result = 31 * result + (autoExecute ? 1 : 0);
    result = 31 * result + (includeLabel ? 1 : 0);
    result = 31 * result + (applyToTokens ? 1 : 0);
    result = 31 * result + (minWidth != null ? minWidth.hashCode() : 0);
    result = 31 * result + (maxWidth != null ? maxWidth.hashCode() : 0);
    result = 31 * result + (fontSize != null ? fontSize.hashCode() : 0);
    result = 31 * result + (fontColorKey != null ? fontColorKey.hashCode() : 0);
    result = 31 * result + (colorKey != null ? colorKey.hashCode() : 0);
    result = 31 * result + (hotKey != null ? hotKey.hashCode() : 0);
    result = 31 * result + (label != null ? label.hashCode() : 0);
    result = 31 * result + (group != null ? group.hashCode() : 0);
    result = 31 * result + (sortby != null ? sortby.hashCode() : 0);
    result = 31 * result + (command != null ? command.hashCode() : 0);
    return result;
  }

  // Don't include the index, so you can compare all the other properties between two macros
  // Also don't include hot key since they can't be the same anyway, or cosmetic fields
  public int hashCodeForComparison() {
    int result;
    result = 0;
    result = 31 * result + (getCompareAutoExecute() && autoExecute ? 1 : 0);
    result = 31 * result + (getCompareIncludeLabel() && includeLabel ? 1 : 0);
    result = 31 * result + (getCompareApplyToSelectedTokens() && applyToTokens ? 1 : 0);
    result = 31 * result + (getLabel() != null ? label.hashCode() : 0);
    result = 31 * result + (getCompareGroup() && group != null ? group.hashCode() : 0);
    result = 31 * result + (getCompareSortPrefix() && sortby != null ? sortby.hashCode() : 0);
    result = 31 * result + (getCompareCommand() && command != null ? command.hashCode() : 0);
    return result;
  }

  // function to enable sorting of buttons; uses the group first, then sortby field
  // concatenated with the label field. Case Insensitive
  public int compareTo(MacroButtonProperties b2) throws ClassCastException {
    if (b2 != this) {
      String b1group = getGroup();
      if (b1group == null) b1group = "";
      String b1sortby = getSortby();
      if (b1sortby == null) b1sortby = "";
      String b1label = getLabel();
      if (b1label == null) b1label = "";

      String b2group = b2.getGroup();
      if (b2group == null) b2group = "";
      String b2sortby = b2.getSortby();
      if (b2sortby == null) b2sortby = "";
      String b2label = b2.getLabel();
      if (b2label == null) b2label = "";

      // now parse the sort strings to help dice codes sort properly, use space as a separator
      String b1string = modifySortString(" " + b1group + " " + b1sortby + " " + b1label);
      String b2string = modifySortString(" " + b2group + " " + b2sortby + " " + b2label);
      return b1string.compareToIgnoreCase(b2string);
    }
    return 0;
  }

  // function to pad numbers with leading zeroes to help sort them appropriately.
  // So this will turn a 2d6 into 0002d0006, and 10d6 into 0010d0006, so the 2d6
  // will sort as lower.
  private static final Pattern sortStringPattern = Pattern.compile("(\\d+)");

  private static String modifySortString(String str) {
    StringBuffer result = new StringBuffer();
    Matcher matcher = sortStringPattern.matcher(str);
    while (matcher.find()) {
      matcher.appendReplacement(result, paddingString(matcher.group(1), 4, '0', true));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  // function found at http://www.rgagnon.com/javadetails/java-0448.html
  // to pad a string by inserting additional characters
  public static String paddingString(String s, int n, char c, boolean paddingLeft) {
    StringBuffer str = new StringBuffer(s);
    int strLength = str.length();
    if (n > 0 && n > strLength) {
      for (int i = 0; i <= n; i++) {
        if (paddingLeft) {
          if (i < n - strLength) str.insert(0, c);
        } else {
          if (i > strLength) str.append(c);
        }
      }
    }
    return str.toString();
  }

  // Begin comparison customization

  private Boolean commonMacro = false;
  private Boolean compareGroup = true;
  private Boolean compareSortPrefix = true;
  private Boolean compareCommand = true;
  private Boolean compareIncludeLabel = true;
  private Boolean compareAutoExecute = true;
  private Boolean compareApplyToSelectedTokens = true;

  public Boolean getCommonMacro() {
    return commonMacro;
  }

  public void setCommonMacro(Boolean value) {
    commonMacro = value;
  }

  public Boolean getCompareGroup() {
    return compareGroup;
  }

  public void setCompareGroup(Boolean value) {
    compareGroup = value;
  }

  public Boolean getCompareSortPrefix() {
    return compareSortPrefix;
  }

  public void setCompareSortPrefix(Boolean value) {
    compareSortPrefix = value;
  }

  public Boolean getCompareCommand() {
    return compareCommand;
  }

  public void setCompareCommand(Boolean value) {
    compareCommand = value;
  }

  public Boolean getCompareIncludeLabel() {
    return compareIncludeLabel;
  }

  public void setCompareIncludeLabel(Boolean value) {
    compareIncludeLabel = value;
  }

  public Boolean getCompareAutoExecute() {
    return compareAutoExecute;
  }

  public void setCompareAutoExecute(Boolean value) {
    compareAutoExecute = value;
  }

  public Boolean getCompareApplyToSelectedTokens() {
    return compareApplyToSelectedTokens;
  }

  public void setCompareApplyToSelectedTokens(Boolean value) {
    compareApplyToSelectedTokens = value;
  }

  public static void fixOldMacroCompare(MacroButtonProperties oldMacro) {
    if (oldMacro.getCommonMacro() == null) {
      oldMacro.setCommonMacro(new Boolean(true));
    }
    if (oldMacro.getAllowPlayerEdits() == null) {
      oldMacro.setAllowPlayerEdits(new Boolean(true));
    }
    if (oldMacro.getCompareApplyToSelectedTokens() == null) {
      oldMacro.setCompareApplyToSelectedTokens(new Boolean(true));
    }
    if (oldMacro.getCompareAutoExecute() == null) {
      oldMacro.setCompareAutoExecute(new Boolean(true));
    }
    if (oldMacro.getCompareCommand() == null) {
      oldMacro.setCompareCommand(new Boolean(true));
    }
    if (oldMacro.getCompareGroup() == null) {
      oldMacro.setCompareGroup(new Boolean(true));
    }
    if (oldMacro.getCompareIncludeLabel() == null) {
      oldMacro.setCompareIncludeLabel(new Boolean(true));
    }
    if (oldMacro.getCompareSortPrefix() == null) {
      oldMacro.setCompareSortPrefix(new Boolean(true));
    }
  }

  public static void fixOldMacroSetCompare(List<MacroButtonProperties> oldMacros) {
    for (MacroButtonProperties nextMacro : oldMacros) {
      fixOldMacroCompare(nextMacro);
    }
  }

  public String getMacroUUID() {
    if (macroUUID == null) macroUUID = UUID.randomUUID().toString();

    return macroUUID;
  }

  public Object readResolve() {
    if (commonMacro == null) commonMacro = false;
    if (compareGroup == null) compareGroup = true;
    if (compareSortPrefix == null) compareSortPrefix = true;
    if (compareCommand == null) compareCommand = true;
    if (compareIncludeLabel == null) compareIncludeLabel = true;
    if (compareAutoExecute == null) compareAutoExecute = true;
    if (compareApplyToSelectedTokens == null) compareApplyToSelectedTokens = true;
    if (allowPlayerEdits == null) allowPlayerEdits = true;
    return this;
  }
}
