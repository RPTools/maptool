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
package net.rptools.maptool.model;

import static org.junit.jupiter.api.Assertions.*;

import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRendererFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestScreenPoint {

  @Test
  @DisplayName("Test Conversion of Screen Points")
  void testConversion() throws Exception {
    ZoneRenderer renderer = ZoneRendererFactory.newRenderer(new Zone());
    renderer.moveViewBy(-100, -100);

    for (int i = -10; i < 10; i++) {
      for (int j = -10; j < 10; j++) {
        ZonePoint zp = new ZonePoint(i, j);
        assertEquals(zp, ScreenPoint.fromZonePoint(renderer, zp).convertToZone(renderer));
      }
    }
  }
}
