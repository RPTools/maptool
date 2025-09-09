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

import java.awt.Dimension;

public final class AwtUtil {
  private AwtUtil() {
    throw new RuntimeException("AwtUtil is a static class");
  }

  public static void constrainTo(Dimension dim, int size) {
    boolean widthBigger = dim.width > dim.height;

    if (widthBigger) {
      dim.height = (int) ((dim.height / (double) dim.width) * size);
      dim.width = size;
    } else {
      dim.width = (int) ((dim.width / (double) dim.height) * size);
      dim.height = size;
    }
  }

  public static void constrainTo(Dimension dim, int width, int height) {
    boolean widthBigger = dim.width > dim.height;

    constrainTo(dim, widthBigger ? width : height);

    if ((widthBigger && dim.height > height) || (!widthBigger && dim.width > width)) {
      int size =
          (int)
              Math.round(
                  widthBigger
                      ? (height / (double) dim.height) * width
                      : (width / (double) dim.width) * dim.height);
      constrainTo(dim, size);
    }
  }
}
