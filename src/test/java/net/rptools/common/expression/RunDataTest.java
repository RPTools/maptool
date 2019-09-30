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
package net.rptools.common.expression;

import junit.framework.TestCase;

public class RunDataTest extends TestCase {

  public void testRandomIntInt() {
    RunData runData = new RunData(null);

    for (int i = 0; i < 10000; i++) {
      int value = runData.randomInt(10);
      assertTrue(1 <= value && value <= 10);
    }
  }

  public void testRandomIntIntInt() {
    RunData runData = new RunData(null);

    for (int i = 0; i < 10000; i++) {
      int value = runData.randomInt(10, 20);
      assertTrue(String.format("Value outside range: %s", value), 10 <= value && value <= 20);
    }
  }
}
