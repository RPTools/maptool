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
package net.rptools.maptool.model.gamedata;

import static org.junit.jupiter.api.Assertions.*;

import net.rptools.maptool.model.gamedata.data.DataType;
import org.junit.jupiter.api.Test;

class InvalidDataOperationTest {

  @Test
  void createInvalidConversion() {
    var ex = InvalidDataOperation.createInvalidConversion(DataType.UNDEFINED, DataType.STRING);
    var ex1 = InvalidDataOperation.createInvalidConversion("AAA", DataType.LONG);

    assertAll(
        () -> assertEquals(ex.getErrorType(), InvalidDataOperation.Type.INVALID_CONVERSION),
        () -> assertEquals(ex1.getErrorType(), InvalidDataOperation.Type.INVALID_CONVERSION));
  }

  @Test
  void createAlreadyExists() {
    var ex = InvalidDataOperation.createAlreadyExists("aa");

    assertEquals(ex.getErrorType(), InvalidDataOperation.Type.ALREADY_EXISTS);
  }

  @Test
  void createUndefined() {
    var ex = InvalidDataOperation.createUndefined("aa");

    assertEquals(ex.getErrorType(), InvalidDataOperation.Type.UNDEFINED);
  }

  @Test
  void createNamespaceDoesNotExist() {
    var ex = InvalidDataOperation.createNamespaceDoesNotExist("aa", "bb");

    assertEquals(ex.getErrorType(), InvalidDataOperation.Type.NAMESPACE_DOES_NOT_EXIST);
  }

  @Test
  void createNamespaceAlreadyExists() {
    var ex = InvalidDataOperation.createNamespaceAlreadyExists("aa", "bb");

    assertEquals(ex.getErrorType(), InvalidDataOperation.Type.ALREADY_EXISTS);
  }
}
