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
package net.rptools.lib;

public class DebugUtil {

  /**
   * Get the bits in the number represented as a string
   *
   * @param num
   * @return the bits in the number represented as a string
   */
  public static String getBits(long num) {
    String str = "";
    for (int i = 0; i < 64; i++) {
      str = (num & 1) + str;
      num >>= 1;

      if (i % 4 == 3) {
        str = " " + str;
      }
    }
    return str;
  }

  /**
   * Get the bits in the number represented as a string
   *
   * @param num
   * @return the bits in the number represented as a string
   */
  public static String getBits(int num) {
    String str = "";
    for (int i = 0; i < 32; i++) {
      str = (num & 1) + str;
      num >>= 1;

      if (i % 4 == 3) {
        str = " " + str;
      }
    }
    return str;
  }
}
