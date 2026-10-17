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
package net.rptools.lib;

import java.awt.Color;

public final class ColorUtil {
  private ColorUtil() {}

  public static Color withAlpha(Color color, int alpha) {
    alpha = Math.clamp(alpha, 0, 255);
    return new Color(color.getRGB(), color.getGreen(), color.getBlue(), alpha);
  }
}
