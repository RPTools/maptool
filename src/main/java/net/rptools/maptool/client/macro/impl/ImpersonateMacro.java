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
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;

@MacroDefinition(
    name = "impersonate",
    aliases = {"im"},
    description = "impersonate.description",
    expandRolls = false)
public class ImpersonateMacro implements Macro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    final MapToolFrame frame = MapTool.getFrame(); // cached for quicker access
    final CommandPanel cpanel = frame.getCommandPanel();
    if (macro != null) macro = macro.trim();

    // Clear current identity
    if (macro == null || macro.length() == 0) {
      cpanel.setIdentity(new CommandPanel.TokenIdentity());
      return;
    }
    // Figure out what we want to impersonate
    String name = macro;
    int index = macro.indexOf(':');
    if (index > 0 && macro.substring(0, index).equalsIgnoreCase("lib")) {
      index = macro.indexOf(':', index + 1);
    }
    if (index > 0) {
      name = macro.substring(0, index).trim();
      macro = macro.substring(index + 1);
    }
    Token token = frame.getCurrentZoneRenderer().getZone().resolveToken(name);
    if (token != null) {
      name = token.getName();
    }
    // Permission
    if (!canImpersonate(token)) {
      if (token != null) {
        MapTool.addLocalMessage(I18N.getText("impersonate.mustOwn", name));
      } else {
        MapTool.addLocalMessage(I18N.getText("msg.error.gmRequired"));
      }
      return;
    }
    // Impersonate
    if (index > 0) {
      // Enter impersonation context for the duration of the macro
      cpanel.enterContextIdentity(new CommandPanel.TokenIdentity(token, name));
      MacroManager.executeMacro(macro, executionContext);
      cpanel.leaveContextIdentity();
    } else {
      // Set current identity
      cpanel.setIdentity(new CommandPanel.TokenIdentity(token, name, canLoadTokenMacros(token)));
    }
  }

  /**
   * Returns whether the player is allowed to impersonate the token or not.
   *
   * @param token the token to impersonate. Can be null for name-only impersonation.
   * @return true if the player is allowed to impersonate, false otherwise
   */
  private boolean canImpersonate(Token token) {
    // my addition
    if (!MapTool.getServerPolicy().isRestrictedImpersonation()) {
      return true;
    }
    if (MapTool.getPlayer().isGM()) {
      return true;
    }
    if (token == null) {
      return false;
    }
    return token.isOwner(MapTool.getPlayer().getName());
  }

  /**
   * Returns whether the player is allowed to run macros on the token.
   *
   * @param token the token
   * @return true if the player can run macro on token, false otherwise.
   */
  private boolean canLoadTokenMacros(Token token) {
    if (token == null) {
      return false;
    }
    return MapTool.getPlayer().isGM() || token.isOwner(MapTool.getPlayer().getName());
  }
}
