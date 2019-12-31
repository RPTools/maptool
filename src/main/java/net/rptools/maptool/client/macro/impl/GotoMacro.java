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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

@MacroDefinition(
    name = "goto",
    aliases = {"g"},
    description = "goto.description")
public class GotoMacro implements Macro {
  private static Pattern COORD_PATTERN = Pattern.compile("(-?\\d+)\\s*,?\\s*(-?\\d+)");

  public void execute(
      MacroContext context, String parameter, MapToolMacroContext executionContext) {
    Matcher m = COORD_PATTERN.matcher(parameter.trim());
    if (m.matches()) {
      // goto coordinate locations
      int x = Integer.parseInt(m.group(1));
      int y = Integer.parseInt(m.group(2));

      MapTool.getFrame().getCurrentZoneRenderer().centerOn(new CellPoint(x, y));
    } else {
      // goto token location
      Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      Token token = zone.getTokenByName(parameter);

      if (!MapTool.getPlayer().isGM() && !zone.isTokenVisible(token)) {
        return;
      }
      if (token != null) {
        int x = token.getX();
        int y = token.getY();
        MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(x, y));
      }
    }
  }
}
