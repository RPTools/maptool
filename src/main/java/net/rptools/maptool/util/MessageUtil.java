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
package net.rptools.maptool.util;

import java.awt.Color;
import javax.swing.UIManager;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeColor;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;

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

  public static String getFormattedEmit(String msg) {
    return "<div class='emit'>" + msg + "</div>";
  }

  public static String getFormattedEmote(String msg, Token token) {
    String identity =
        token == null ? MapTool.getFrame().getCommandPanel().getIdentity() : token.getName();
    msg = applyChatColor(identity + " " + msg);

    return "<div class='emote'>" + getAvatarMessage(msg, token, identity) + "</div>";
  }

  public static String getFormattedEmotePlural(String msg, Token token) {
    String identity =
        token == null ? MapTool.getFrame().getCommandPanel().getIdentity() : token.getName();
    msg = applyChatColor(identity + "'s " + msg);

    return "<div class='emote'>" + getAvatarMessage(msg, token, identity) + "</div>";
  }

  public static String getFormattedSay(
      String msg, Token token, boolean isTrusted, String macroName, String macroSource) {
    String identity =
        token == null ? MapTool.getFrame().getCommandPanel().getIdentity() : token.getName();
    msg = getPrefix(identity + ":", isTrusted, macroName, macroSource) + " " + applyChatColor(msg);

    return "<div class='say'>" + getAvatarMessage(msg, token, identity) + "</div>";
  }

  public static String getFormattedOoc(String msg) {
    return "<div class='ooc'>"
        + MapTool.getPlayer().getName()
        + ": "
        + applyChatColor("(( " + msg + " ))")
        + "</div>";
  }

  public static String getFormattedSelf(String msg) {
    return "<div class='self'>" + msg + "</div>";
  }

  public static String getFormattedSystemMsg(String msg) {
    return "<div class='system'>" + msg + "</div>";
  }

  public static String getFormattedToGmRecipient(
      String msg, String sender, boolean isTrusted, String macroName, String macroSource) {
    return "<div class='to-gm'>"
        + getPrefix(I18N.getText("togm.saysToGM", sender), isTrusted, macroName, macroSource)
        + " "
        + msg
        + "</div>";
  }

  public static String getFormattedToGmSender(String msg) {
    return "<div class='to-gm'>" + I18N.getText("togm.self", msg) + "</div>";
  }

  public static String getFormattedWhisperRecipient(String msg, String sender) {
    return "<div class='whisper'>" + I18N.getText("whisper.string", sender, msg) + "</div>";
  }

  public static String getFormattedWhisperSender(String msg, String recipients) {
    return "<div class='whisper'>" + I18N.getText("whisper.you.string", recipients, msg) + "</div>";
  }

  private static String getAvatarMessage(String msg, Token token, String identity) {
    StringBuilder sb = new StringBuilder();

    sb.append("<table class='ava-msg'><tr valign='top'>");

    if (AppPreferences.showAvatarInChat.get()) {
      if (token == null && MapTool.getFrame().getCommandPanel().isImpersonating()) {
        GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
        if (guid != null)
          token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
        else token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokenByName(identity);
      }
      sb.append("<td class='avatar'>");
      if (token != null) {
        MD5Key imageId = token.getPortraitImage();
        if (imageId == null) {
          imageId = token.getImageAssetId();
        }
        sb.append("<img src=\"asset://").append(imageId).append("-40\" >");
      }
      sb.append("</td>");
    }

    sb.append("<td class='message'>").append(msg).append("</td></tr></table>");

    return sb.toString();
  }

  private static String getPrefix(
      String prefixStr, boolean isTrusted, String macroName, String macroSource) {
    StringBuilder sb = new StringBuilder();

    if (isTrusted && macroName != null && !MapTool.getPlayer().isGM()) {
      sb.append("<span class='trusted-prefix' title='").append(macroName);
      if (macroSource != null && macroSource.length() > 0) {
        sb.append("@").append(macroSource);
      }
      sb.append("'>");
    } else {
      sb.append("<span class='prefix'>");
    }

    sb.append(prefixStr).append("</span>");

    return sb.toString();
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

  private static String applyChatColor(String str) {
    var color = getChatColorHex();
    if (color.isEmpty()) {
      return str;
    }
    return String.format("<span style='color:" + color + "'>%s</span>", str);
  }
}
