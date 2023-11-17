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
package net.rptools.dicelib.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RunDataTest {

  @Test
  public void testRandomIntInt() {
    RunData runData = new RunData(null);

    for (int i = 0; i < 10000; i++) {
      int value = runData.randomInt(10);
      assertTrue(1 <= value && value <= 10);
    }
  }

  @Test
  public void testRandomIntIntInt() {
    RunData runData = new RunData(null);

    for (int i = 0; i < 10000; i++) {
      int value = runData.randomInt(10, 20);
      assertTrue(10 <= value && value <= 20, String.format("Value outside range: %s", value));
    }
  }

  @Test
  public void testParentChild() {
    List<Integer> allRolls = new ArrayList<>();
    RunData parent = new RunData(null);
    RunData child = parent.createChildRunData(null);

    allRolls.add(parent.randomInt(20));
    for (int i = 0; i < 4; i++) {
      allRolls.add(child.randomInt(20));
    }

    assertEquals(allRolls, parent.getRolled());
  }
}
