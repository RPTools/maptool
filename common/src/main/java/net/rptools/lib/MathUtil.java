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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* Utility class for useful mathematical methods */
public class MathUtil {
  private static final Logger log = LogManager.getLogger(MathUtil.class);

  /**
   * Returns a truncated double with the specified number of decimal places
   *
   * @param value to be truncated
   * @param decimalPlaces number of decimal places to use
   * @return truncated double value
   */
  public static double doublePrecision(double value, int decimalPlaces) {
    double divisor = Math.pow(10, -decimalPlaces);
    return divisor * Math.round(value / divisor);
  }

  public static boolean isInt(Object o) {
    if (o == null) {
      return false;
    } else {
      return o.getClass().isAssignableFrom(Integer.class);
    }
  }

  /**
   * Checks that a value lies within a specified tolerance. Useful for checking if a value is "close
   * enough"
   *
   * @param checkValue to be checked
   * @param referenceValue to be checked against
   * @param tolerance variance allowed
   * @return true if the value is within ± tolerance
   */
  public static boolean inTolerance(double checkValue, double referenceValue, double tolerance) {
    return checkValue <= referenceValue + tolerance && checkValue >= referenceValue - tolerance;
  }

  /**
   * Maps a value in one range to its equivalent in a second range
   *
   * @param valueToMap value in the first range that needs to be converted
   * @param in_min the minimum value for the original range
   * @param in_max the maximum value for the original range
   * @param out_min the minimum value for the target range
   * @param out_max the maximum value for the target range
   * @return the equivalent value of valueToMap in the target range
   */
  public static double mapToRange(
      double valueToMap, double in_min, double in_max, double out_min, double out_max) {
    return (valueToMap - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  }
}
