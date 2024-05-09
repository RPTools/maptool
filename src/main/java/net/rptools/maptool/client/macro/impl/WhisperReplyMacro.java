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

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.util.MessageUtil;

@MacroDefinition(
    name = "reply",
    aliases = {"rep"},
    description = "whisperreply.description")
public class WhisperReplyMacro extends AbstractMacro {
  public void execute(MacroContext context, String message, MapToolMacroContext executionContext) {
    String playerName = MapTool.getLastWhisperer();
    if (playerName == null) {
      MapTool.addMessage(
          TextMessage.me(
              context.getTransformationHistory(), I18N.getString("whisperreply.noTarget")));
      return;
    }
    // Validate
    if (!MapTool.getClient().isPlayerConnected(playerName)) {
      MapTool.addMessage(
          TextMessage.me(
              context.getTransformationHistory(),
              I18N.getText("msg.error.playerNotConnected", playerName)));
      return;
    }
    if (MapTool.getPlayer().getName().equalsIgnoreCase(playerName)) {
      MapTool.addMessage(
          TextMessage.me(context.getTransformationHistory(), I18N.getText("whisper.toSelf")));
      return;
    }
    // Send
    MapTool.addMessage(
        TextMessage.whisper(
            context.getTransformationHistory(),
            playerName,
            MessageUtil.getFormattedWhisperRecipient(
                message, MapTool.getFrame().getCommandPanel().getIdentity())));
    MapTool.addMessage(
        TextMessage.me(
            context.getTransformationHistory(),
            MessageUtil.getFormattedWhisperSender(message, playerName)));
  }
}
