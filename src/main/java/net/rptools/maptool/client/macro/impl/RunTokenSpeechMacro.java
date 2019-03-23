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

import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;

/** Macro to run the speech ID on the given token */
@MacroDefinition(
  name = "tsay",
  aliases = {"ts"},
  description = "tokenspeech.description"
)
public class RunTokenSpeechMacro implements Macro {
  /** @see net.rptools.maptool.client.macro.Macro#execute(java.lang.String) */
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    Set<GUID> selectedTokenSet = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
    if (selectedTokenSet.size() == 0) {
      MapTool.addLocalMessage(I18N.getText("msg.error.noTokensSelected"));
      return;
    }
    for (GUID tokenId : selectedTokenSet) {
      Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
      if (token == null) {
        continue;
      }
      String tmacro = token.getSpeech(macro);
      if (tmacro == null) {
        continue;
      }
      MapTool.getFrame()
          .getCommandPanel()
          .getCommandTextArea()
          .setText("/im " + token.getId() + ": " + tmacro);
      MapTool.getFrame().getCommandPanel().commitCommand();
    }
  }
}
