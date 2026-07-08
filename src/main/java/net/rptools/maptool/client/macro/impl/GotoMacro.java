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
import net.rptools.maptool.model.ZonePoint;

@MacroDefinition(
    name = "goto",
    aliases = {"g"},
    description = "goto.description")
public class GotoMacro implements Macro {
  private static final Pattern COORD_PATTERN = Pattern.compile("(-?\\d+)\\s*,?\\s*(-?\\d+)");

  public void execute(
      MacroContext context, String parameter, MapToolMacroContext executionContext) {
    var renderer = MapTool.getFrame().getCurrentZoneRenderer();
    var zone = renderer.getZone();

    Matcher m = COORD_PATTERN.matcher(parameter.trim());

    ZonePoint point;
    if (m.matches()) {
      // goto coordinate locations
      int x = Integer.parseInt(m.group(1));
      int y = Integer.parseInt(m.group(2));
      point = zone.getGrid().convert(new CellPoint(x, y));
    } else {
      // goto token location
      Token token = zone.getTokenByName(parameter);

      if (token == null) {
        return;
      }
      if (!MapTool.getPlayer().isGM() && !zone.isTokenVisible(token)) {
        return;
      }

      point = new ZonePoint(token.getX(), token.getY());
    }

    renderer
        .getViewModel()
        .setZoneScale(
            renderer
                .getViewModel()
                .getZoneScale()
                .centeredOn(point.x, point.y, renderer.getSize()));
  }
}
