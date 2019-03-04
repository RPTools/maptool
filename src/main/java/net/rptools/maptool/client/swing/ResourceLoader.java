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
package net.rptools.maptool.client.swing;

import java.awt.Rectangle;
import java.util.StringTokenizer;

// This should really be in rplib
public class ResourceLoader {

  /** Rectangles are in the form x, y, width, height */
  public static Rectangle loadRectangle(String rectString) {

    StringTokenizer strtok = new StringTokenizer(rectString, ",");
    if (strtok.countTokens() != 4) {
      throw new IllegalArgumentException(
          "Could not load rectangle: '" + rectString + "', must be in the form x, y, w, h");
    }

    int x = Integer.parseInt(strtok.nextToken().trim());
    int y = Integer.parseInt(strtok.nextToken().trim());
    int w = Integer.parseInt(strtok.nextToken().trim());
    int h = Integer.parseInt(strtok.nextToken().trim());

    return new Rectangle(x, y, w, h);
  }
}
