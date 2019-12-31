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

@MacroDefinition(
    name = "rollsecret",
    aliases = {"rsec"},
    description = "rollsecret.description")
public class RollSecretMacro extends AbstractRollMacro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    String result = roll(macro);
    if (result != null) {
      if (!MapTool.getPlayer().isGM()) {
        MapTool.addMessage(
            new TextMessage(
                TextMessage.Channel.GM,
                null,
                MapTool.getPlayer().getName(),
                "* " + I18N.getText("rollsecret.gm.string", MapTool.getPlayer().getName(), result),
                context.getTransformationHistory()));
        MapTool.addMessage(
            new TextMessage(
                TextMessage.Channel.ME,
                null,
                MapTool.getPlayer().getName(),
                I18N.getText("rollsecret.self.string"),
                context.getTransformationHistory()));
      } else {
        MapTool.addMessage(
            new TextMessage(
                TextMessage.Channel.GM,
                null,
                MapTool.getPlayer().getName(),
                "* " + I18N.getText("rollsecret.gmself.string", result),
                context.getTransformationHistory()));
      }
    }
  }
}
