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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class GridlessGridTest {
  private GridlessGrid grid;

  @BeforeEach
  void setUp() {
    this.grid = new GridlessGrid();
  }

  @ParameterizedTest
  @DisplayName("Test GridlessGrid.nextFacing(…)")
  @CsvFileSource(resources = "GridlessGrid.nextFacing.csv", useHeadersInDisplayName = true)
  void testNextFacing(
      boolean faceEdges, boolean faceVertices, boolean clockwise, int input, int expected) {
    int result = grid.nextFacing(input, faceEdges, faceVertices, clockwise);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @DisplayName("Test GridlessGrid.nearestFacing(…)")
  @CsvFileSource(resources = "GridlessGrid.nearestFacing.csv", useHeadersInDisplayName = true)
  void testNearestFacing(boolean faceEdges, boolean faceVertices, int input, int expected) {
    int result = grid.nearestFacing(input, faceEdges, faceVertices);
    assertEquals(expected, result);
  }
}
