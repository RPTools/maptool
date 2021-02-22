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
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.util.MessageUtil;

@MacroDefinition(
    name = "say",
    aliases = {"s"},
    description = "say.description")
public class SayMacro extends AbstractMacro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    macro = processText(macro);
    String msg;

    if (executionContext != null) {
      msg =
          MessageUtil.getFormattedSay(
              macro,
              null,
              MapTool.getParser().isMacroPathTrusted(),
              executionContext.getName(),
              executionContext.getSource());
    } else {
      msg = MessageUtil.getFormattedSay(macro, null, false, null, null);
    }

    MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), msg));
  }
}
