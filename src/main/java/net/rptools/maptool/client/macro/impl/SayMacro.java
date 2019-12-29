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
package net.rptools.maptool.client.macro.impl;

import java.awt.Color;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;

@MacroDefinition(
    name = "say",
    aliases = {"s"},
    description = "say.description")
public class SayMacro extends AbstractMacro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    macro = processText(macro);
    StringBuilder sb = new StringBuilder();
    String identity = MapTool.getFrame().getCommandPanel().getIdentity();
    sb.append("<table cellpadding=0><tr>");

    if (MapTool.getFrame().getCommandPanel().isImpersonating()
        && AppPreferences.getShowAvatarInChat()) {
      Token token;
      GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
      if (guid != null)
        token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
      else token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokenByName(identity);
      if (token != null) {
        MD5Key imageId = token.getPortraitImage();
        if (imageId == null) {
          imageId = token.getImageAssetId();
        }
        sb.append("<td valign='top' width='40' style=\"padding-right:5px\"><img src=\"asset://")
            .append(imageId)
            .append("-40\" ></td>");
      }
    }
    sb.append("<td valign=top style=\"margin-right: 5px\">");
    if (executionContext != null
        && MapTool.getParser().isMacroPathTrusted()
        && !MapTool.getPlayer().isGM()) {
      sb.append("<span class='trustedPrefix' ")
          .append("title='")
          .append(executionContext.getName());
      sb.append("@").append(executionContext.getSource()).append("'>");
    }
    sb.append(identity).append(": ");
    if (executionContext != null
        && MapTool.getParser().isMacroPathTrusted()
        && !MapTool.getPlayer().isGM()) {
      sb.append("</span>");
    }
    sb.append("</td><td valign=top>");

    Color color = MapTool.getFrame().getCommandPanel().getTextColorWell().getColor();
    if (color != null) {
      sb.append("<span style='color:#")
          .append(String.format("%06X", (color.getRGB() & 0xFFFFFF)))
          .append("'>");
    }
    sb.append(macro);
    if (color != null) {
      sb.append("</span>");
    }
    sb.append("</td>");
    sb.append("</tr></table>");
    MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), sb.toString()));
  }
}
