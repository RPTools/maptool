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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.*;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.LookupTable;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.Test;

public class LookupTableTests {
  private static final LookupTable table = new LookupTable();

  private void buildTestTable() {
    table.clearEntries();
    table.setName("test");
    table.setRoll("1d6");
    table.setPickOnce(false);
    table.setVisible(false);
    table.setAllowLookup(true);
  }

  /** Doesn't register with the campaign's table map, so no unregistering is necessary. */
  @Test
  void table_creation() {
    // Kind of silly to check all these, but there could be
    // new logic added in the future that does processing in
    // the various get/set methods, so...
    buildTestTable();
    // Must start empty
    assertTrue(table.getEntryList().isEmpty());
    // Check that individual fields can be set and retrieved
    assertEquals("test", table.getName());
    assertEquals("1d6", table.getDefaultRoll());
    MD5Key md5 = new MD5Key("1234");
    table.setTableImage(md5);
    assertEquals(md5, table.getTableImage());
    assertFalse(table.getPickOnce());
    assertFalse(table.getVisible());
    assertTrue(table.getAllowLookup());
  }

  @Test
  void table_addOneEntry() throws ParserException {
    buildTestTable();
    // This entry must be the first one
    table.addEntry(10, 20, "result", null);

    // Check a roll that's in the middle of the range.
    LookupTable.LookupEntry le = table.getLookup("15");
    assertEquals(10, le.getMin());
    assertEquals(20, le.getMax());
    assertEquals("result", le.getValue());
    assertNull(le.getImageId());
  }

  private void populateTable() {
    buildTestTable();
    table.addEntry(10, 20, "result 1", null);
    table.addEntry(25, 30, "result 2", null);
    table.addEntry(40, 50, "result 3", null);
  }

  @Test
  void table_countEntries() {
    populateTable();

    assertEquals(3, table.getEntryList().size());
  }

  @Test
  void table_addMultipleEntries() throws ParserException {
    populateTable();

    LookupTable.LookupEntry le = table.getLookup("25");
    assertEquals(25, le.getMin());
    assertEquals(30, le.getMax());
    assertEquals("result 2", le.getValue());
    assertNull(le.getImageId());

    // Make sure both lookups return the same table entry
    LookupTable.LookupEntry le2 = table.getLookup("30");
    assertEquals(le, le2);
  }
}
