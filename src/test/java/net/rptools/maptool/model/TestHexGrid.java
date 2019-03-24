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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestHexGrid {

  @Test
  @DisplayName("Convert Cell to Zone.")
  void testConvertCellToZone() {
    int start = -100;
    // int start = 0;
    HexGrid grid = new HexGridHorizontal();
    for (int y = start; y < 100; y++) {
      for (int x = start; x < 100; x++) {
        CellPoint cp = new CellPoint(x, y);
        ZonePoint zp = grid.convert(cp);
        assertEquals(cp, grid.convert(zp));
      }
    }
  }
}
