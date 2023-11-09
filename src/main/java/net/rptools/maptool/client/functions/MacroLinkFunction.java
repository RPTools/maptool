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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.exceptions.AbortFunctionException;
import net.rptools.maptool.client.functions.exceptions.AssertFunctionException;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.MessageUtil;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MacroLinkFunction extends AbstractFunction {
  private static final Logger log = LogManager.getLogger(MacroLinkFunction.class);

  private enum OutputTo {
    SELF,
    NONE,
    GM,
    ALL,
    SELF_AND_GM,
    LIST
  }

  /** Singleton instance of the MacroLinkFunction class. */
  private static final MacroLinkFunction instance = new MacroLinkFunction();

  /** Moving all the patterns together to ensure that we don't forget any */
  static final Pattern AUTOEXEC_PATTERN =
      Pattern.compile("([^:]*)://(.*)/([^/]*)/([^?]*)(?:\\?(.*))?");

  static final Pattern TOOLTIP_PATTERN =
      Pattern.compile("([^:]*)://(.*)/([^/]*)/([^?]*)(?:\\?(.*))?");
  /** Pattern to distinguish a link (group 1) from its data (group 2). */
  public static final Pattern LINK_DATA_PATTERN =
      Pattern.compile("((?s)[^:]*://.*/[^/]*/[^?]*\\?)(.*)?");

  static final Pattern MACROLINK_PATTERN =
      Pattern.compile("(?s)([^:]*)://(.*)/([^/]*)/([^?]*)(?:\\?(.*))?");

  /**
   * Gets and instance of the MacroLinkFunction class.
   *
   * @return an instance of MacroLinkFunction.
   */
  public static MacroLinkFunction getInstance() {
    return instance;
  }

  private MacroLinkFunction() {
    super(1, 5, "macroLink", "macroLinkText", "execLink");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {

    boolean formatted;

    String macroName;
    String linkText;
    String linkArgs;
    String linkWho;
    String linkTarget;

    if ("macroLink".equalsIgnoreCase(functionName)) {
      formatted = true;
      linkText = args.get(0).toString();
      if (args.size() < 2) {
        throw new ParserException(
            I18N.getText("macro.function.macroLink.missingName", "macroLink"));
      }
      macroName = args.get(1).toString();

      linkWho = args.size() > 2 ? args.get(2).toString() : "none";
      linkArgs = args.size() > 3 ? args.get(3).toString() : "";
      linkTarget = args.size() > 4 ? args.get(4).toString() : "Impersonated";

    } else if ("macroLinkText".equalsIgnoreCase(functionName)) {
      formatted = false;
      linkText = "";
      macroName = args.get(0).toString();

      linkWho = args.size() > 1 ? args.get(1).toString() : "none";
      linkArgs = args.size() > 2 ? args.get(2).toString() : "";
      linkTarget = args.size() > 3 ? args.get(3).toString() : "Impersonated";

    } else if ("execLink".equalsIgnoreCase(functionName)) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }
      FunctionUtil.checkNumberParam(functionName, args, 1, 4);

      String link = args.get(0).toString();
      boolean defer =
          args.size() > 1 ? FunctionUtil.paramAsBoolean(functionName, args, 1, true) : false;
      String strTargets = args.size() > 2 ? args.get(2).toString().trim() : "self";
      String delim = args.size() > 3 ? args.get(3).toString() : ",";

      JsonArray jsonTargets;
      if ("json".equals(delim) || strTargets.charAt(0) == '[')
        jsonTargets = JSONMacroFunctions.getInstance().asJsonElement(strTargets).getAsJsonArray();
      else {
        jsonTargets = new JsonArray();
        for (String t : StringUtil.split(strTargets, delim)) {
          jsonTargets.add(t.trim());
        }
      }
      if (jsonTargets.size() == 0) {
        return ""; // dont send to empty lists
      }

      List<String> targets = new ArrayList<>();
      for (JsonElement ele : jsonTargets) {
        targets.add(ele.getAsString());
      }
      sendExecLink(link, defer, targets);
      return "";
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }

    StringBuilder sb = new StringBuilder();

    if (formatted) {
      sb.append("<a href='");
    }
    sb.append(createMacroText(macroName, linkWho, linkTarget, linkArgs));
    if (formatted) {
      sb.append("'>").append(linkText).append("</a>");
    }
    return sb.toString();
  }

  /**
   * Send the execLink to targets, either immediately or with a delay
   *
   * @param link the macroLinkText
   * @param defer should the execLink be delayed
   * @param targets the list of targets
   */
  private static void sendExecLink(final String link, boolean defer, Collection<String> targets) {
    if (defer) {
      EventQueue.invokeLater(() -> sendExecLink(link, targets));
    } else {
      sendExecLink(link, targets);
    }
  }

  /**
   * Send the execLink. If target is local, run locally instead.
   *
   * @param link the macroLinkText
   * @param targets the list of targets
   */
  private static void sendExecLink(final String link, Collection<String> targets) {
    String source = MapTool.getPlayer().getName();

    for (String target : targets) {
      MapTool.serverCommand().execLink(link, target, source);
    }
  }

  /**
   * Receive an execLink, and run it if the player is a target.
   *
   * @param link the macroLinkText
   * @param target the target.
   * @param source the name of the source.
   */
  public static void receiveExecLink(final String link, String target, String source) {
    if (ExecFunction.isMessageForMe(target, source)) {
      runMacroLink(link);
    }
  }

  /**
   * This method generates a string in the form of a macro invocation.
   *
   * <p>The resulting output is of the form <code>macro://</code><i>macroName</i> <code>/</code>
   * <i>who</i> <code>/</code> <i>target</i><code>?</code> <i>args</i>
   *
   * <p>The <code>args</code> parameter is a String which is converted to a property list and then
   * back to a String.
   *
   * @param macroName such as <code>MacroName@Lib:Core</code>
   * @param who where output should go
   * @param target the string <code>impersonated</code>, <code>all</code>
   * @param args the arguments to append to the end of the macro invocation
   * @return the String of the macro invocation
   */
  public String createMacroText(String macroName, String who, String target, String args) {
    if (macroName.toLowerCase().endsWith("@this")) {
      macroName =
          macroName.substring(0, macroName.length() - 4) + MapTool.getParser().getMacroSource();
    }
    return "macro://" + macroName + "/" + who + "/" + target + "?" + encode(args);
  }

  private String encode(String str) {
    JSONMacroFunctions.getInstance().asJsonElement(str);
    return URLEncoder.encode(str, StandardCharsets.UTF_8);
  }

  private String decode(String str) {
    return JSONMacroFunctions.getInstance()
        .asJsonElement(URLDecoder.decode(str, StandardCharsets.UTF_8))
        .getAsString();
  }

  /**
   * Converts a URL argument string into a property list.
   *
   * @param args the URL argument string.
   * @return a property list representation of the arguments.
   */
  public static String argsToStrPropList(String args) {
    String[] vals = args.split("&");
    StringBuilder propList = new StringBuilder();

    for (String s : vals) {
      String decoded = URLDecoder.decode(s, StandardCharsets.UTF_8);
      if (propList.length() != 0) {
        propList.append(" ; ");
      }
      propList.append(decoded);
    }
    return propList.toString();
  }

  /**
   * Takes a Property list and creates a URL Ready argument list.
   *
   * @param props The property list to convert.
   * @return a string that can be used as an argument to a url.
   */
  public String strPropListToArgs(String props) {
    String[] vals = props.split(";");
    StringBuilder args = new StringBuilder();
    for (String s : vals) {
      s = s.trim();
      String encoded = URLEncoder.encode(s, StandardCharsets.UTF_8);
      if (args.length() != 0) {
        args.append("&");
      }
      args.append(encoded);
    }

    return args.toString();
  }

  /**
   * Returns the link data as a json element.
   *
   * @param linkData a string containing the encoded link data
   * @return the link data, decoded and converted to json element
   */
  public JsonElement getLinkDataAsJson(String linkData) {
    if (linkData == null || linkData.isBlank()) {
      return null;
    }
    String decodedLinkData = URLDecoder.decode(linkData, StandardCharsets.UTF_8);

    if (!decodedLinkData.startsWith("[") && !decodedLinkData.startsWith("{")) {
      return new JsonPrimitive(decodedLinkData);
    } else {
      return JSONMacroFunctions.getInstance().asJsonElement(decodedLinkData);
    }
  }

  /**
   * Gets a string that describes the macro link.
   *
   * @param link the link to get the tool tip of.
   * @return a string containing the tool tip.
   */
  public String macroLinkToolTip(String link) {
    Matcher m = TOOLTIP_PATTERN.matcher(link);
    StringBuilder tip = new StringBuilder();

    if (m.matches() && m.group(1).equalsIgnoreCase("macro")) {

      tip.append("<html>");
      if (isAutoExecLink(link)) {
        tip.append("<tr><th style='color: red'><u>&laquo;")
            .append(I18N.getText("macro.function.macroLink.autoExecToolTip"))
            .append("&raquo;</b></u></th></tr>");
      } else {
        tip.append("<tr><th><u>&laquo;Macro Link&raquo;</b></u></th></tr>");
      }
      tip.append("<table>");
      tip.append("<tr><th>Output to</th><td>").append(m.group(3)).append("</td></td>");
      tip.append("<tr><th>Command</th><td>").append(m.group(2)).append("</td></td>");
      String val = m.group(5);
      if (val != null) {
        try {
          Double.parseDouble(val);
          // Do nothing as its a number
        } catch (NumberFormatException e) {
          val = "\"" + argsToStrPropList(val) + "\"";
        }
        tip.append("<tr><th>")
            .append(I18N.getText("macro.function.macroLink.arguments"))
            .append(val)
            .append("</td></tr>");
      }
      String[] targets = m.group(4).split(",");
      tip.append("</table>");
      tip.append("<b>")
          .append(I18N.getText("macro.function.macroLink.executeOn"))
          .append("</b><ul>");
      ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
      Zone zone = zr != null ? zr.getZone() : null;
      for (String t : targets) {
        String name;
        if (t.equalsIgnoreCase("impersonated")) {
          name = I18N.getText("macro.function.macroLink.impersonated");
        } else if (t.equalsIgnoreCase("selected")) {
          name = I18N.getText("macro.function.macroLink.selected");
        } else {
          Token token = zone != null ? zone.resolveToken(t) : null;
          name = token != null ? token.getName() : I18N.getText("macro.function.macroLink.unknown");
        }
        tip.append("<li>").append(name).append("</li>");
      }
      tip.append("</ul>");

      tip.append("</html>");
    }

    return tip.toString();
  }

  /**
   * Runs the macro specified by the link.
   *
   * @param link the link to the macro.
   */
  public static void runMacroLink(String link) {
    runMacroLink(link, false);
  }

  /**
   * Runs the macro specified by the link.
   *
   * @param link the link to the macro.
   * @param setVars should the variables be set in the macro context as well as passed in as
   *     macro.args.
   */
  public static void runMacroLink(String link, boolean setVars) {
    if (link == null || link.length() == 0) {
      return;
    }
    Matcher m = MACROLINK_PATTERN.matcher(link);

    if (m.matches() && m.group(1).equalsIgnoreCase("macro")) {
      OutputTo outputTo;
      String macroName = "";
      String args = "";
      Set<String> outputToPlayers = new HashSet<String>();

      String who = m.group(3);
      if (who.equalsIgnoreCase("self")) {
        outputTo = OutputTo.SELF;
      } else if (who.equalsIgnoreCase("gm")) {
        outputTo = OutputTo.GM;
      } else if (who.equalsIgnoreCase("none")) {
        outputTo = OutputTo.NONE;
      } else if (who.equalsIgnoreCase("all") || who.equalsIgnoreCase("say")) {
        outputTo = OutputTo.ALL;
      } else if (who.equalsIgnoreCase("gm-self") || who.equalsIgnoreCase("gmself")) {
        outputTo = OutputTo.SELF_AND_GM;
      } else if (who.equalsIgnoreCase("list")) {
        outputTo = OutputTo.LIST;
      } else {
        outputTo = OutputTo.NONE;
      }
      macroName = m.group(2);

      String val = m.group(5);
      if (val != null) {
        try {
          Double.parseDouble(val);
          // Do nothing as its a number
        } catch (NumberFormatException e) {
          val = argsToStrPropList(val);
        }
        args = val;
        try {
          JsonObject jobj = JSONMacroFunctions.getInstance().asJsonElement(args).getAsJsonObject();
          if (jobj.has("mlOutputList")) {
            for (JsonElement ele : jobj.get("mlOutputList").getAsJsonArray()) {
              outputToPlayers.add(ele.getAsString());
            }
          }
        } catch (Exception e) {
          // Do nothing as we just dont populate the list.
        }
      }

      String[] targets = m.group(4).split(",");
      ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
      Zone zone = zr != null ? zr.getZone() : null;

      try {
        for (String t : targets) {
          if (zone == null) {
            doOutput(null, outputTo, macroName, args, outputToPlayers);
          } else if (t.equalsIgnoreCase("impersonated")) {
            CommandPanel cmd = MapTool.getFrame().getCommandPanel();
            GUID guid = cmd.getIdentityGUID();
            Token token = guid != null ? zone.getToken(guid) : zone.resolveToken(cmd.getIdentity());

            doOutput(token, outputTo, macroName, args, outputToPlayers);
          } else if (t.equalsIgnoreCase("selected")) {
            for (GUID id : zr.getSelectedTokenSet()) {
              doOutput(zone.getToken(id), outputTo, macroName, args, outputToPlayers);
            }
          } else {
            doOutput(zone.resolveToken(t), outputTo, macroName, args, outputToPlayers);
          }
        }
      } catch (AbortFunctionException e) {
        // Do nothing
      } catch (AssertFunctionException afe) {
        MapTool.addLocalMessage(afe.getMessage());
      } catch (ParserException e) {
        e.addMacro(macroName);
        e.addMacro("macroLink");
        MapTool.addErrorMessage(e);
      }
    }
  }

  /**
   * Run the macro and display the output.
   *
   * @param token the token on which the macro is executed
   * @param outputTo who should get the output
   * @param macroName the name of the macro
   * @param args the arguments of the macro
   * @param playerList the list of players who are to receive the output
   * @throws ParserException if the macro cannot be executed
   */
  private static void doOutput(
      Token token, OutputTo outputTo, String macroName, String args, Set<String> playerList)
      throws ParserException {

    // Execute the macro
    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    String line = MapTool.getParser().runMacro(resolver, token, macroName, args);

    // Don't output blank messages. Fixes #1867.
    if ("".equals(line)) {
      return;
    }
    /*
     * First we check our player list to make sure we are not sending things out multiple times or the wrong way. This looks a little ugly, but all it is doing is searching for the strings "say",
     * "gm", or "gmself", and if it contains no other strings changes it to a more appropriate for such as /togm, /self, etc. If it contains other names then gm, self etc will be replaced with
     * player names.
     */
    if (outputTo == OutputTo.LIST) {
      if (playerList == null) {
        outputTo = OutputTo.NONE;
      } else if (playerList.contains("all") || playerList.contains("say")) {
        outputTo = OutputTo.ALL;
      } else if (playerList.contains("gmself") || playerList.contains("gm-self")) {
        playerList.remove("gmself");
        playerList.remove("gm-self");
        if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
          outputTo = OutputTo.SELF_AND_GM;
        } else {
          playerList.addAll(MapTool.getGMs());
          playerList.add(getSelf());
        }
      } else if (playerList.contains("gm") && playerList.contains("self")) {
        playerList.remove("gm");
        playerList.remove("self");
        if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
          outputTo = OutputTo.SELF_AND_GM;
        } else {
          playerList.addAll(MapTool.getGMs());
          playerList.add(getSelf());
        }
      } else if (playerList.contains("gm")) {
        playerList.remove("gm");
        if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
          outputTo = OutputTo.GM;
        } else {
          playerList.addAll(MapTool.getGMs());
          playerList.add(getSelf());
        }
      } else if (playerList.contains("self")) {
        playerList.remove("self");
        if (playerList.size() == 0) { // if that was only thing in the list then dont use whispers
          outputTo = OutputTo.SELF;
        } else {
          playerList.add(getSelf());
        }
      }
    }

    switch (outputTo) {
      case SELF:
        MapTool.addLocalMessage(MessageUtil.getFormattedSelf(line));
        break;
      case SELF_AND_GM:
        MapTool.addLocalMessage(MessageUtil.getFormattedToGmSender(line));
        // Intentionally falls through
      case GM:
        MapTool.addMessage(
            TextMessage.gm(
                null,
                MessageUtil.getFormattedToGmRecipient(
                    line,
                    MapTool.getPlayer().getName(),
                    MapTool.getParser().isMacroPathTrusted(),
                    macroName,
                    null)));
        break;
      case ALL:
        MapTool.addMessage(
            TextMessage.say(
                null,
                MessageUtil.getFormattedSay(
                    line, token, MapTool.getParser().isMacroPathTrusted(), macroName, null)));
        break;
      case LIST:
        StringBuilder sb = new StringBuilder();
        for (String name : playerList) {
          doWhisper(line, token, name);
          if (sb.length() > 0) {
            sb.append(", ");
          }
          sb.append(name);
        }
        MapTool.addLocalMessage(MessageUtil.getFormattedWhisperSender(line, sb.toString()));

        break;
      case NONE:
        // Do nothing with the output.
        break;
    }
  }

  private static void doWhisper(String message, Token token, String playerName) {
    List<Player> playerList = MapTool.getPlayerList();
    List<String> players = new ArrayList<>();
    for (int count = 0; count < playerList.size(); count++) {
      Player p = playerList.get(count);
      String thePlayer = p.getName();
      players.add(thePlayer);
    }
    String playerNameMatch = StringUtil.findMatch(playerName, players);
    playerName = (!playerNameMatch.equals("")) ? playerNameMatch : playerName;

    // Validate
    if (!MapTool.isPlayerConnected(playerName)) {
      MapTool.addLocalMessage(I18N.getText("msg.error.playerNotConnected", playerName));
      return;
    }
    if (MapTool.getPlayer().getName().equalsIgnoreCase(playerName)) {
      return;
    }

    // Send
    MapTool.addMessage(
        TextMessage.whisper(
            null,
            playerName,
            MessageUtil.getFormattedWhisperRecipient(
                message, MapTool.getFrame().getCommandPanel().getIdentity())));
  }

  private static String getSelf() {
    return MapTool.getPlayer().getName();
  }

  /**
   * Runs the macro specified by the link if it is auto executable otherwise does nothing..
   *
   * @param link the link to the macro.
   */
  public void processMacroLink(String link) {
    if (isAutoExecLink(link)) {
      runMacroLink(link);
    }
  }

  /**
   * Runs the macro specified by the link if it is auto executable otherwise does nothing..
   *
   * @param link the link to the macro.
   */
  private boolean isAutoExecLink(String link) {
    Matcher m = AUTOEXEC_PATTERN.matcher(link);

    if (m.matches() && m.group(1).equalsIgnoreCase("macro")) {
      String command = m.group(2);
      try {
        String[] parts = command.split("@");
        if (parts.length > 1) {
          var lib = new LibraryManager().getLibrary(parts[1].substring(4));
          if (lib.isEmpty()) {
            return false;
          }
          var library = lib.get();
          var macroInfo = library.getMTScriptMacroInfo(parts[0]).get();
          if (macroInfo.isEmpty()) {
            return false;
          }

          return macroInfo.get().trusted() && macroInfo.get().autoExecute();
        }

      } catch (ExecutionException | InterruptedException e) {
        log.error("Exception while handling macro " + command, e);
      }
    }
    return false;
  }
}
