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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.swing.text.JTextComponent;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.macrobuttons.MacroButtonHotKeyManager;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.server.proto.MacroButtonPropertiesDto;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * This (data)class is used by all Macro Buttons, including campaign, global and token macro
 * buttons.
 *
 * @see net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton
 */
public class MacroButtonProperties implements Comparable<MacroButtonProperties> {

  private static final Logger log = LogManager.getLogger(MacroButtonProperties.class);

  // Jamz: Why a String and not UUID? Because stupid Hessian can't serialize UUID, ug.
  private @Nonnull String macroUUID = UUID.randomUUID().toString();

  private transient GUID tokenId;
  private @Nonnull String saveLocation = "";
  private int index;
  private @Nonnull String colorKey = "default";
  private @Nonnull String hotKey = MacroButtonHotKeyManager.HOTKEYS[0];
  private @Nonnull String command = "";
  private @Nonnull String label = "";
  private @Nonnull String group = "";
  private @Nonnull String sortby = "";
  private boolean autoExecute;

  /** If {@code true}, include the macro lable when printing output. */
  private boolean includeLabel;

  /**
   * If {@code true}, when the button is clicked it will impersonate every selected token when
   * executing the macro.
   */
  private boolean applyToTokens;

  private @Nonnull String fontColorKey = "black";
  private @Nonnull String fontSize = "1.00em";
  private @Nonnull String minWidth = "";
  private @Nonnull String maxWidth = "";
  private @Nonnull Boolean allowPlayerEdits = AppPreferences.getAllowPlayerMacroEditsDefault();
  private @Nonnull String toolTip = "";
  private @Nonnull Boolean displayHotKey = true;

  private MacroButtonProperties() {}

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
      boolean allowPlayerEdits,
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
    setTokenId((GUID) null);
    setSaveLocation("");
    setAllowPlayerEdits(allowPlayerEdits);
    setDisplayHotKey(displayHotKey);
    setCompareGroup(true);
    setCompareSortPrefix(true);
    setCompareCommand(true);
    setCompareIncludeLabel(true);
    setCompareAutoExecute(true);
    setCompareApplyToSelectedTokens(true);
    setToolTip(toolTip);
  }

  // exact copy constructor (including UUID!)
  public MacroButtonProperties(MacroButtonProperties other) {
    macroUUID = other.macroUUID; // IMPORTANT
    setCommonMacro(other.commonMacro);
    setIndex(other.index);
    setColorKey(other.colorKey);
    setHotKey(other.hotKey);
    setCommand(other.command);
    setLabel(other.label);
    setGroup(other.group);
    setSortby(other.sortby);
    setAutoExecute(other.autoExecute);
    setIncludeLabel(other.includeLabel);
    setApplyToTokens(other.applyToTokens);
    setFontColorKey(other.fontColorKey);
    setFontSize(other.fontSize);
    setMinWidth(other.minWidth);
    setMaxWidth(other.maxWidth);
    setTokenId(other.tokenId);
    setSaveLocation(other.saveLocation);
    setAllowPlayerEdits(other.allowPlayerEdits);
    setDisplayHotKey(other.displayHotKey);
    setCompareGroup(other.compareGroup);
    setCompareSortPrefix(other.compareSortPrefix);
    setCompareCommand(other.compareCommand);
    setCompareIncludeLabel(other.compareIncludeLabel);
    setCompareAutoExecute(other.compareAutoExecute);
    setCompareApplyToSelectedTokens(other.compareApplyToSelectedTokens);
    setToolTip(other.toolTip);
  }

  // constructor that creates a new instance, doesn't auto save
  public MacroButtonProperties(int index) {
    setIndex(index);
    setColorKey("default");
    setHotKey(MacroButtonHotKeyManager.HOTKEYS[0]);
    setCommand("");
    setLabel("(new)");
    setGroup("");
    setSortby("");
    setAutoExecute(true);
    setIncludeLabel(false);
    setApplyToTokens(false);
    setFontColorKey("default");
    setFontSize("");
    setMinWidth("");
    setMaxWidth("");
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

  /**
   * Creates a copy of an existing button on the designated panel. Auto-saves.
   *
   * @param panelClass the panel name where the new button is being created
   * @param index the next index to use on the panel
   * @param properties the properties from which to copy
   */
  public MacroButtonProperties(String panelClass, int index, MacroButtonProperties properties) {
    this(panelClass, index, properties, true);
  }

  /**
   * Creates a copy of an existing button on the designated panel. Optionally auto-saves. Auto-save
   * should be requested unless multiple buttons are being created at once, and the intention is to
   * save in bulk.
   *
   * @param panelClass the panel name where the new button is being created
   * @param index the next index to use on the panel
   * @param properties the properties from which to copy
   * @param autoSave whether to automatically call {@link #save()}
   */
  public MacroButtonProperties(
      String panelClass, int index, MacroButtonProperties properties, boolean autoSave) {
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
      setAllowPlayerEdits(Boolean.parseBoolean(props.get("allowPlayerEdits")));
    if (props.containsKey("displayHotKey"))
      setAllowPlayerEdits(Boolean.parseBoolean(props.get("displayHotKey")));
    if (props.containsKey("toolTip")) setToolTip(props.get("toolTip"));
    if (props.containsKey("commonMacro"))
      setCommonMacro(Boolean.parseBoolean(props.get("commonMacro")));
    if (props.containsKey("compareGroup"))
      setCompareGroup(Boolean.parseBoolean(props.get("compareGroup")));
    if (props.containsKey("compareSortPrefix"))
      setCompareSortPrefix(Boolean.parseBoolean(props.get("compareSortPrefix")));
    if (props.containsKey("compareCommand"))
      setCompareCommand(Boolean.parseBoolean(props.get("compareCommand")));
    if (props.containsKey("compareIncludeLabel"))
      setCompareIncludeLabel(Boolean.parseBoolean(props.get("compareIncludeLabel")));
    if (props.containsKey("compareAutoExecute"))
      setCompareAutoExecute(Boolean.parseBoolean(props.get("compareAutoExecute")));
    if (props.containsKey("compareApplyToSelectedTokens"))
      setCompareApplyToSelectedTokens(
          Boolean.parseBoolean(props.get("compareApplyToSelectedTokens")));
  }

  public void save() {
    if (saveLocation.equals("Token") && tokenId != null) {
      Token token = getToken();
      if (token != null) {
        MapTool.serverCommand().updateTokenProperty(token, Token.Update.saveMacro, this);
      } else {
        MapTool.showError(I18N.getText("msg.error.macro.buttonNullToken", getLabel(), tokenId));
      }
    } else if (saveLocation.equals("GlobalPanel")) {
      MacroButtonPrefs.savePreferences(this);
    } else if (saveLocation.equals("CampaignPanel")) {
      MapTool.getCampaign().saveMacroButtonProperty(this, false);
    } else if (saveLocation.equals("GmPanel")) {
      MapTool.getCampaign().saveMacroButtonProperty(this, true);
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
   * @param tokenList tokens to execute macro on
   */
  public void executeMacro(Collection<Token> tokenList) {
    if (tokenList == null || tokenList.size() == 0) {
      executeCommand(null);
    } else if (commonMacro) {
      executeCommonMacro(tokenList);
    } else {
      for (Token token : tokenList) {
        executeCommand(token.getId());
      }
    }
  }

  private void executeCommonMacro(Collection<Token> tokenList) {
    /*
     * This is actually one of the "common macro" buttons that are on the selection panel so we need to handle this case a little differently. If apply to all tokens is checked by the user then we
     * need to check that the command is part of the common values otherwise it would cause unexpected things to occur.
     */
    if (applyToTokens && (!compareCommand)) {
      MapTool.showError("msg.error.cantApplyMacroToSelected");
      return;
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
    String impersonatePrefix = "";
    if (tokenId != null) {
      impersonatePrefix = "/im " + tokenId + ":";
    }

    JTextComponent commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
    String oldText = commandArea.getText();

    if (getIncludeLabel()) {
      MapTool.getFrame().getCommandPanel().commitCommand(impersonatePrefix + getLabel());
    }

    String commandsToExecute[] = parseMultiLineCommand(getCommand());

    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = (zr == null ? null : zr.getZone());
    Token contextToken = (zone == null ? null : zone.getToken(tokenId));
    String loc;
    for (String command : commandsToExecute) {
      // If we aren't auto execute, then append the text instead of replace it
      command = impersonatePrefix + (!getAutoExecute() ? oldText + " " : "") + command;
      if (getAutoExecute()) {
        boolean trusted = false;
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
        MapTool.getFrame().getCommandPanel().commitCommand(command, newMacroContext);
      } else {
        commandArea.setText(command);
      }
    }
    commandArea.requestFocusInWindow();
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

  public void setSaveLocation(@Nonnull String saveLocation) {
    if (saveLocation.equals("ImpersonatePanel") || saveLocation.equals("SelectionPanel")) {
      this.saveLocation = "Token";
    } else {
      this.saveLocation = saveLocation;
    }
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public @Nonnull String getColorKey() {
    return colorKey;
  }

  public void setColorKey(String colorKey) {
    if (MapToolUtil.getColor(colorKey) != null) {
      this.colorKey = colorKey;
    }
  }

  public @Nonnull String getHotKey() {
    return hotKey;
  }

  public void setHotKey(@Nonnull String hotKey) {
    this.hotKey = hotKey;
  }

  public @Nonnull String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = (command == null ? "" : command);
  }

  public @Nonnull String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = (label == null ? "" : label);
  }

  public @Nonnull String getGroup() {
    return group;
  }

  public @Nonnull String getGroupForDisplay() {
    return this.group;
  }

  public void setGroup(String group) {
    this.group = (group == null ? "" : group);
  }

  public @Nonnull String getSortby() {
    return sortby;
  }

  public void setSortby(String sortby) {
    this.sortby = (sortby == null ? "" : sortby);
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
    String font = getFontColorKey();
    if (MapToolUtil.isHtmlColor(font)) {
      return font;
    }
    Color c = MapToolUtil.getColor(font);
    if (c != null) {
      return "#" + Integer.toHexString(c.getRGB()).substring(2);
    }
    return "black";
  }

  public String getFontColorKey() {
    Color c = MapToolUtil.getColor(fontColorKey);
    if (c != null) {
      return fontColorKey;
    }
    return "black";
  }

  public void setFontColorKey(String fontColorKey) {
    this.fontColorKey =
        switch (fontColorKey) {
          case "", "default" -> "default";
          default -> MapToolUtil.getColor(fontColorKey) != null ? fontColorKey : "default";
        };
  }

  public @Nonnull String getFontSize() {
    return fontSize;
  }

  public void setFontSize(String fontSize) {
    this.fontSize = (fontSize == null || fontSize.equals("") ? "1.00em" : fontSize);
  }

  public @Nonnull String getMinWidth() {
    return minWidth;
  }

  public void setMinWidth(String minWidth) {
    this.minWidth = (minWidth == null ? "" : minWidth);
  }

  public @Nonnull String getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(String maxWidth) {
    this.maxWidth = (maxWidth == null ? "" : maxWidth);
  }

  public boolean getAllowPlayerEdits() {
    return allowPlayerEdits;
  }

  public void setAllowPlayerEdits(boolean value) {
    allowPlayerEdits = value;
  }

  public boolean getDisplayHotKey() {
    return displayHotKey;
  }

  public void setDisplayHotKey(boolean value) {
    displayHotKey = value;
  }

  public @Nonnull String getSaveLocation() {
    return saveLocation;
  }

  public void setToolTip(String tt) {
    toolTip = (tt == null ? "" : tt);
  }

  public @Nonnull String getToolTip() {
    return toolTip;
  }

  public String getEvaluatedToolTip() {
    final var trimmed = toolTip.trim();
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
      return toolTip;
    }

    Token token = null;
    if (tokenId != null) {
      token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
    }
    try {
      MapToolMacroContext context =
          new MapToolMacroContext("ToolTip", token != null ? token.getName() : "", false, index);
      if (token == null) {
        log.debug(
            "Evaluating toolTip: ----------------------------------------------------------------------------------");
      } else {
        log.debug(
            "Evaluating toolTip: for token {} ({})----------------------------------------------------------------------------------",
            token.getName(),
            token.getId());
      }
      return MapTool.getParser().parseLine(token, toolTip, context);
    } catch (ParserException pe) {
      return toolTip;
    }
  }

  public boolean isDuplicateMacro(String source, Token token) {
    int macroHashCode = hashCodeForComparison();
    List<MacroButtonProperties> existingMacroList;
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
    fontSize = "1.00em";
    minWidth = "";
    maxWidth = "";
    allowPlayerEdits = AppPreferences.getAllowPlayerMacroEditsDefault();
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
    if (!Objects.equals(colorKey, that.colorKey)) {
      return false;
    }
    if (!Objects.equals(command, that.command)) {
      return false;
    }
    if (!Objects.equals(hotKey, that.hotKey)) {
      return false;
    }
    if (!Objects.equals(label, that.label)) {
      return false;
    }
    if (!Objects.equals(group, that.group)) {
      return false;
    }
    if (!Objects.equals(sortby, that.sortby)) {
      return false;
    }
    if (!Objects.equals(fontColorKey, that.fontColorKey)) {
      return false;
    }
    if (!Objects.equals(fontSize, that.fontSize)) {
      return false;
    }
    if (!Objects.equals(minWidth, that.minWidth)) {
      return false;
    }
    if (!Objects.equals(maxWidth, that.maxWidth)) {
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
    result = 31 * result + minWidth.hashCode();
    result = 31 * result + maxWidth.hashCode();
    result = 31 * result + fontSize.hashCode();
    result = 31 * result + fontColorKey.hashCode();
    result = 31 * result + colorKey.hashCode();
    result = 31 * result + hotKey.hashCode();
    result = 31 * result + label.hashCode();
    result = 31 * result + group.hashCode();
    result = 31 * result + sortby.hashCode();
    result = 31 * result + command.hashCode();
    return result;
  }

  // Don't include the index, so you can compare all the other properties between two macros
  // Also don't include hot key since they can't be the same anyway, or cosmetic fields
  public int hashCodeForComparison() {
    int result;
    result = 0;
    result = 31 * result + (compareAutoExecute && autoExecute ? 1 : 0);
    result = 31 * result + (compareIncludeLabel && includeLabel ? 1 : 0);
    result = 31 * result + (compareApplyToSelectedTokens && applyToTokens ? 1 : 0);
    result = 31 * result + label.hashCode();
    result = 31 * result + (compareGroup ? group.hashCode() : 0);
    result = 31 * result + (compareSortPrefix ? sortby.hashCode() : 0);
    result = 31 * result + (compareCommand ? command.hashCode() : 0);
    return result;
  }

  // function to enable sorting of buttons; uses the group first, then sortby field
  // concatenated with the label field. Case Insensitive
  public int compareTo(@NotNull MacroButtonProperties b2) throws ClassCastException {
    if (b2 != this) {
      String b1group = getGroup();
      String b1sortby = getSortby();
      String b1label = getLabel();

      String b2group = b2.getGroup();
      String b2sortby = b2.getSortby();
      String b2label = b2.getLabel();

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
    StringBuilder result = new StringBuilder();
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
    StringBuilder str = new StringBuilder(s);
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

  private @Nonnull Boolean commonMacro = false;
  private @Nonnull Boolean compareGroup = true;
  private @Nonnull Boolean compareSortPrefix = true;
  private @Nonnull Boolean compareCommand = true;
  private @Nonnull Boolean compareIncludeLabel = true;
  private @Nonnull Boolean compareAutoExecute = true;
  private @Nonnull Boolean compareApplyToSelectedTokens = true;

  public boolean getCommonMacro() {
    return commonMacro;
  }

  public void setCommonMacro(boolean value) {
    commonMacro = value;
  }

  public boolean getCompareGroup() {
    return compareGroup;
  }

  public void setCompareGroup(boolean value) {
    compareGroup = value;
  }

  public boolean getCompareSortPrefix() {
    return compareSortPrefix;
  }

  public void setCompareSortPrefix(boolean value) {
    compareSortPrefix = value;
  }

  public boolean getCompareCommand() {
    return compareCommand;
  }

  public void setCompareCommand(boolean value) {
    compareCommand = value;
  }

  public boolean getCompareIncludeLabel() {
    return compareIncludeLabel;
  }

  public void setCompareIncludeLabel(boolean value) {
    compareIncludeLabel = value;
  }

  public boolean getCompareAutoExecute() {
    return compareAutoExecute;
  }

  public void setCompareAutoExecute(boolean value) {
    compareAutoExecute = value;
  }

  public boolean getCompareApplyToSelectedTokens() {
    return compareApplyToSelectedTokens;
  }

  public void setCompareApplyToSelectedTokens(boolean value) {
    compareApplyToSelectedTokens = value;
  }

  public @Nonnull String getMacroUUID() {
    return macroUUID;
  }

  @SuppressWarnings("ConstantConditions")
  public Object readResolve() {
    if (commonMacro == null) {
      commonMacro = false;
    }
    if (compareGroup == null) {
      compareGroup = true;
    }
    if (compareSortPrefix == null) {
      compareSortPrefix = true;
    }
    if (compareCommand == null) {
      compareCommand = true;
    }
    if (compareIncludeLabel == null) {
      compareIncludeLabel = true;
    }
    if (compareAutoExecute == null) {
      compareAutoExecute = true;
    }
    if (compareApplyToSelectedTokens == null) {
      compareApplyToSelectedTokens = true;
    }
    if (allowPlayerEdits == null) {
      allowPlayerEdits = AppPreferences.getAllowPlayerMacroEditsDefault();
    }
    if (macroUUID == null) {
      macroUUID = UUID.randomUUID().toString();
    }
    if (displayHotKey == null) {
      displayHotKey = true;
    }
    if (command == null) {
      command = "";
    }
    if (sortby == null) {
      sortby = "";
    }
    if (group == null) {
      group = "";
    }
    if (label == null) {
      label = "";
    }
    if (hotKey == null) {
      hotKey = MacroButtonHotKeyManager.HOTKEYS[0];
    }
    if (StringUtil.isEmpty(colorKey)) {
      colorKey = "default";
    }
    if (StringUtil.isEmpty(fontColorKey)) {
      fontColorKey = "black";
    }
    if (fontSize == null) {
      fontSize = "1.00em";
    }
    if (minWidth == null) {
      minWidth = "";
    }
    if (maxWidth == null) {
      maxWidth = "";
    }
    if (toolTip == null) {
      toolTip = "";
    }
    if (saveLocation == null) {
      saveLocation = "";
    }
    return this;
  }

  public static MacroButtonProperties fromDto(MacroButtonPropertiesDto dto) {
    var macro = new MacroButtonProperties();
    macro.macroUUID = dto.getMacroId();
    macro.saveLocation = dto.getSaveLocation();
    macro.index = dto.getIndex();
    macro.colorKey = dto.getColorKey();
    macro.hotKey = dto.getHotKey();
    macro.command = dto.getCommand();
    macro.label = dto.getLabel();
    macro.group = dto.getGroup();
    macro.sortby = dto.getSortby();
    macro.autoExecute = dto.getAutoExecute();
    macro.includeLabel = dto.getIncludeLabel();
    macro.applyToTokens = dto.getApplyToTokens();
    macro.fontColorKey = dto.getFontColorKey();
    macro.fontSize = dto.getFontSize();
    macro.minWidth = dto.getMinWidth();
    macro.maxWidth = dto.getMaxWidth();
    macro.allowPlayerEdits = dto.getAllowPlayerEdits();
    macro.toolTip = dto.getToolTip();
    macro.displayHotKey = dto.getDisplayHotKey();
    return macro;
  }

  public MacroButtonPropertiesDto toDto() {
    var dto = MacroButtonPropertiesDto.newBuilder();
    dto.setMacroId(macroUUID);
    dto.setSaveLocation(saveLocation);
    dto.setIndex(index);
    dto.setColorKey(colorKey);
    dto.setHotKey(hotKey);
    dto.setCommand(command);
    dto.setLabel(label);
    dto.setGroup(group);
    dto.setSortby(sortby);
    dto.setAutoExecute(autoExecute);
    dto.setIncludeLabel(includeLabel);
    dto.setApplyToTokens(applyToTokens);
    dto.setFontColorKey(fontColorKey);
    dto.setFontSize(fontSize);
    dto.setMinWidth(minWidth);
    dto.setMaxWidth(maxWidth);
    dto.setAllowPlayerEdits(allowPlayerEdits);
    dto.setToolTip(toolTip);
    dto.setDisplayHotKey(displayHotKey);
    return dto.build();
  }
}
