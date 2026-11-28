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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.util;

import java.awt.Color;
import java.util.List;
import javax.swing.UIManager;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeColor;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class MessageUtil {

  public static String getMessageCss() {
    if (ThemeSupport.shouldUseThemeColorsForChat()) {
      var gray = ThemeSupport.getThemeColorHexString(ThemeColor.GREY);
      var purple = ThemeSupport.getThemeColorHexString(ThemeColor.PURPLE);
      var blue = ThemeSupport.getThemeColorHexString(ThemeColor.BLUE);
      return ".emit { font-weight: bold; font-style: italic }"
          .concat(".ava-msg td { padding: 0px }")
          .concat(".ava-msg .avatar { width: 40px; text-align: center }")
          .concat(
              ".ava-msg .message { padding-left: 5px; margin-right: 5px; border-left: 3px "
                  + "solid ")
          .concat(gray)
          .concat("}")
          .concat(".emote .message { border-left-color: ")
          .concat(purple)
          .concat(" }")
          .concat(".say .prefix, .say .trusted-prefix { font-weight: bold }")
          .concat(".self { font-style: italic }")
          .concat(".system { color: ")
          .concat(blue)
          .concat("; font-style: italic }")
          .concat(".whisper { color: ")
          .concat(blue)
          .concat(" }");
    } else {
      return """
          .emit { font-weight: bold; font-style: italic }
          .ava-msg td { padding: 0px }
          .ava-msg .avatar { width: 40px; text-align: center }
          .ava-msg .message { padding-left: 5px; margin-right: 5px; border-left: 3px solid silver }
          .emote .message { border-left-color: #7AC07A }
          .say .prefix, .say .trusted-prefix { font-weight: bold }
          .self { font-style: italic }
          .system { color: blue; font-style: italic }
          .whisper { color: blue }
          """;
    }
  }

  private static String stringify(Element element) {
    var doc = new Document("");
    doc.outputSettings().syntax(Document.OutputSettings.Syntax.html).prettyPrint(false);
    doc.appendChild(element);
    return element.outerHtml();
  }

  public static String getFormattedEmit(String msg) {
    return stringify(new Element("div").text(msg));
  }

  public static String getFormattedEmote(String msg, Token token) {
    String identity =
        token == null ? MapTool.getClient().getImpersonation().getIdentity() : token.getName();
    var colouredMsgHtml = applyChatColorHtml(identity + " " + msg);

    return stringify(
        new Element("div")
            .addClass("emote")
            .appendChild(getAvatarMessageHtml(List.of(colouredMsgHtml), token, identity)));
  }

  public static String getFormattedEmotePossessive(String msg, Token token) {
    String identity =
        token == null ? MapTool.getClient().getImpersonation().getIdentity() : token.getName();
    identity += identity.endsWith("s") ? "' " : "'s ";
    var colouredMsgHtml = applyChatColorHtml(identity + msg);

    return stringify(
        new Element("div")
            .addClass("emote")
            .appendChild(getAvatarMessageHtml(List.of(colouredMsgHtml), token, identity)));
  }

  public static String getFormattedSay(
      String msg, Token token, boolean isTrusted, String macroName, String macroSource) {
    String identity =
        token == null ? MapTool.getClient().getImpersonation().getIdentity() : token.getName();
    var prefixHtml =
        List.of(
            getPrefixHtml(identity + ":", isTrusted, macroName, macroSource),
            new TextNode(" "),
            applyChatColorHtml(msg));

    return stringify(
        new Element("div")
            .addClass("say")
            .appendChild(getAvatarMessageHtml(prefixHtml, token, identity)));
  }

  public static String getFormattedOoc(String msg) {
    return stringify(
        new Element("div")
            .addClass("ooc")
            .text(MapTool.getPlayer().getName())
            .text(": ")
            .appendChild(applyChatColorHtml("(( " + msg + " ))")));
  }

  public static String getFormattedSelf(String msg) {
    return stringify(new Element("div").addClass("self").text(msg));
  }

  public static String getFormattedSystemMsg(String msg) {
    return stringify(new Element("div").addClass("system").text(msg));
  }

  public static String getFormattedToGmRecipient(
      String msg, String sender, boolean isTrusted, String macroName, String macroSource) {
    return stringify(
        new Element("div")
            .addClass("to-gm")
            .appendChild(
                getPrefixHtml(
                    I18N.getText("togm.saysToGM", sender), isTrusted, macroName, macroSource))
            .text(" ")
            .text(msg));
  }

  public static String getFormattedToGmSender(String msg) {
    return stringify(new Element("div").addClass("to-gm").text(I18N.getText("togm.self", msg)));
  }

  public static String getFormattedWhisperRecipient(String msg, String sender) {
    return stringify(
        new Element("div").addClass("whisper").text(I18N.getText("whisper.string", sender, msg)));
  }

  public static String getFormattedWhisperSender(String msg, String recipients) {
    return stringify(
        new Element("div")
            .addClass("whisper")
            .text(I18N.getText("whisper.you.string", recipients, msg)));
  }

  /**
   * Builds an HTML fragment with an the impersonated token represnted in the message.
   *
   * @param msgHtml An HTML fragment to use as the message.
   * @param token
   * @param identity
   * @return An HTML fragment
   */
  private static Element getAvatarMessageHtml(List<Node> msgHtml, Token token, String identity) {
    var element = new Element("table").addClass("ava-msg");

    var row = new Element("tr").attr("valign", "top");
    element.appendChild(row);

    if (AppPreferences.showAvatarInChat.get()) {
      if (token == null && MapTool.getClient().getImpersonation().isImpersonating()) {
        GUID guid = MapTool.getClient().getImpersonation().getIdentityGUID();
        // With no map open there is no token to take the avatar from.
        Zone zone = MapTool.getClient().getCurrentZone();
        if (zone == null) {
          token = null;
        } else if (guid != null) {
          token = zone.getToken(guid);
        } else {
          token = zone.getTokenByName(identity);
        }
      }

      var avatarCell = new Element("td").addClass("avatar");
      row.appendChild(avatarCell);

      if (token != null) {
        MD5Key imageId = token.getPortraitImage();
        if (imageId == null) {
          imageId = token.getImageAssetId();
        }
        var img = new Element("img");
        avatarCell.appendChild(img);
        img.attr("src", "asset://" + imageId + "-40");
      }
    }

    var messageCell = new Element("td").addClass("message").appendChildren(msgHtml);
    row.appendChild(messageCell);

    return element;
  }

  /**
   * Builds an HTML fragment to use as the start of a formatted message.
   *
   * @param prefixStr
   * @param isTrusted
   * @param macroName
   * @param macroSource
   * @return An HTML fragment
   */
  private static Element getPrefixHtml(
      String prefixStr, boolean isTrusted, String macroName, String macroSource) {
    var element = new Element("span");

    if (isTrusted && macroName != null && !MapTool.getPlayer().isGM()) {
      element.addClass("trusted-prefix");

      var title = macroName;
      if (macroSource != null && !macroSource.isEmpty()) {
        title += "@" + macroSource;
      }
      element.attr("title", title);
    } else {
      element.addClass("prefix");
    }
    element.text(prefixStr);

    return element;
  }

  public static String getChatColorHex() {
    if (MapTool.getFrame() == null || MapTool.getFrame().getCommandPanel() == null) {
      return "";
    }

    Color color = MapTool.getFrame().getCommandPanel().getTextColorWell().getColor();
    if (color == null) {
      return "";
    }
    return String.format("#%06X", color.getRGB() & 0x00FFFFFF);
  }

  public static String getDefaultBackgroundHex() {
    var color = UIManager.getColor("Panel.background");
    return String.format("#%06X", color.getRGB() & 0x00FFFFFF);
  }

  public static String getDefaultForegroundHex() {
    var color = UIManager.getColor("Panel.foreground");
    return String.format("#%06X", color.getRGB() & 0x00FFFFFF);
  }

  /**
   * Builds an HTML fragment to use as coloured text.
   *
   * @param str
   * @return An HTML fragment.
   */
  private static Element applyChatColorHtml(String str) {
    var element = new Element("span");

    var color = getChatColorHex();
    if (!color.isEmpty()) {
      element.attr("style", String.format("color:%s", color));
    }
    element.text(str);

    return element;
  }
}
