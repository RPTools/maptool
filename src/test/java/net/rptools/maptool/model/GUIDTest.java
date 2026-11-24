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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GUIDTest {
  @Test
  void testDto() {
    var guid = new GUID("57CF2A35CF3441A28CD02BE5B9D6F56B");
    var dto = guid.toDto();

    assertEquals(
        0x57_CF_2A_35_CF_34_41_A2L, dto.getHigh(), "High bits must match the original GUID");
    assertEquals(0x8C_D0_2B_E5_B9_D6_F5_6BL, dto.getLow(), "Low bits must match the original GUID");

    var newGuid = GUID.fromDto(dto);

    assertEquals(guid, newGuid, "The reconstituted GUID must be the same as the original");
  }
}
