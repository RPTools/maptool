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
import net.rptools.maptool.language.I18N;

@MacroDefinition(
    name = "version",
    aliases = {"v"},
    description = "slashversion.description")

/** This class represents the /version command run from the chat panel. */
public class VersionMacro implements Macro {

  @Override
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    String versionString = MapTool.getVersion();
    if ("unspecified".equalsIgnoreCase(versionString)) {
      versionString += " (development build)";
    }
    MapTool.addLocalMessage(I18N.getText("slashversion.message", versionString));
  }
}
