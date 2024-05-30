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

import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.MessageUtil;
import net.rptools.maptool.util.StringUtil;

@MacroDefinition(
    name = "whisper",
    aliases = {"w"},
    description = "whisper.description")
public class WhisperMacro extends AbstractMacro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    String playerName = StringUtil.getFirstWord(macro);

    if (playerName == null) {
      MapTool.addMessage(
          TextMessage.me(
              context.getTransformationHistory(), "<b>" + I18N.getText("whisper.noName") + "</b>"));
      return;
    }
    int indexSpace =
        (macro.startsWith("\"")) ? macro.indexOf(" ", playerName.length() + 2) : macro.indexOf(" ");

    String message = processText(macro.substring(indexSpace + 1));
    List<Player> playerList = MapTool.getPlayerList();
    List<String> players = new ArrayList<String>();
    for (int count = 0; count < playerList.size(); count++) {
      Player p = playerList.get(count);
      String thePlayer = p.getName();
      players.add(thePlayer);
    }
    String playerNameMatch = StringUtil.findMatch(playerName, players);
    playerName = (!playerNameMatch.equals("")) ? playerNameMatch : playerName;

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
