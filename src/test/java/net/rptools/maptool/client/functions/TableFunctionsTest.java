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

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.LookupTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the following table operations:
 * <ol>
 * <li>tbl, table</li>
 * <li>tblImage, tableImage</li>
 * <li>addTableEntry</li>
 * <li>clearTable</li>
 * <li>copyTable</li>
 * <li>createTable</li>
 * <li>deleteTable</li>
 * <li>deleteTableEntry</li>
 * <li>getTableAccess</li>
 * <li>getTableEntry</li>
 * <li>getTableImage</li>
 * <li>getTableNames</li>
 * <li>getTablePickOnce</li>
 * <li>getTablePicksLeft</li>
 * <li>getTableRoll</li>
 * <li>getTableVisible</li>
 * <li>loadTable</li>
 * <li>resetTablePicks</li>
 * <li>setTableAccess</li>
 * <li>setTableEntry</li>
 * <li>setTableImage</li>
 * <li>setTablePickOnce</li>
 * <li>setTableRoll</li>
 * <li>setTableVisible</li>
 * </ol>
 */
class TableFunctionsTest {
    private static final LookupTable table = new LookupTable();

    private LookupTable populateTestTable() {
        table.clearEntries();
        table.setName("test");
        table.setRoll("1d6");
        table.setPickOnce(false);
        table.setVisible(false);
        table.setAllowLookup(true);
        return table;
    }

    @Test
    void table_creation() {
        // Kind of silly to check all these, but there could be
        // new logic added in the future that does processing in
        // the various get/set methods, so...
        LookupTable t = new LookupTable();
        // Must start empty
        assertTrue(t.getEntryList().isEmpty());
        // Check that individual fields can be set and retrieved
        t.setName("test");
        assertEquals("test", t.getName());
        t.setRoll("1d6");
        assertEquals("1d6", t.getRoll());
        MD5Key md5 = new MD5Key("1234");
        t.setTableImage(md5);
        assertEquals(md5, t.getTableImage());
        t.setPickOnce(true);
        assertTrue(t.getPickOnce());
        t.setVisible(true);
        assertTrue(t.getVisible());
        t.setAllowLookup(true);
        assertTrue(t.getAllowLookup());
    }

    @Test
    void table_tbl() {
        LookupTable t = populateTestTable();
        LookupTableFunction.getInstance().
    }

    @Test
    void parse_twoPropsFirstWithSpace() {
        String testProps = "a 1=1; nospace=2";

        StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

        inMap(map).key("A 1").hasValue("1");
        inMap(map).key("NOSPACE").hasValue("2");
    }

    @Test
    void parse_twoPropsLastWithSpace() {
        String testProps = "nospace=2; a 1=1";

        StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

        inMap(map).key("A 1").hasValue("1");
        inMap(map).key("NOSPACE").hasValue("2");
    }

    @Test
    void parse_onePropWithTwoSpaces() {
        String testProps = "a b 1=1";

        StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

        inMap(map).key("A B 1").hasValue("1");
    }
}
