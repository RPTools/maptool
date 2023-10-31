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
package net.rptools.maptool.model.grid;

import net.rptools.maptool.language.I18N;

public enum GridLineStyleType {
  SOLID("Grid.line.style.solid", 0),
  DASHED("Grid.line.style.dashed", 1),
  DOTTED("Grid.line.style.dotted", 2),
  INTERSECTION("Grid.line.style.intersection", 3),
  INTERSECTION_MIDPOINT("Grid.line.style.intersection_mid-point", 4);
  final String friendlyKey;
  final int index;
  final float[][] dashArray;

  GridLineStyleType(String friendly_, int index_) {
    float[][][] dashArrayValues =
        new float[][][] {
          {{32f}, {32f}, {32f}, {32f}, {32f}}, // solid
          { // dashed
            {1.1f, 1.8f, 1.1f},
            {0.9f, 2.2f, 0.9f},
            {0.8f, 2.4f, 0.8f},
            {0.6f, 2.6f, 0.6f},
            {0.5f, 2.8f, 0.6f},
          },
          { // dotted
            {1.0f, 1.0f},
            {0.94f, 1.06f},
            {0.88f, 1.12f},
            {0.82f, 1.18f},
            {0.76f, 1.24f}
          },
          { // intersection
            {2.0f, 28, 2.0f, 0},
            {4.0f, 24, 4.0f, 0},
            {5.5f, 21, 5.5f, 0},
            {7, 18, 7, 0},
            {9, 14, 9, 0}
          },
          { // intersection & midpoint
            {2.0f, 13.75f, 0.5f, 13.75f, 2.0f, 0f},
            {4.0f, 11.515f, 0.7f, 11.515f, 4.0f, 0f},
            {5.5f, 15f, 1f, 15f, 5.5f, 0f},
            {7f, 8.25f, 1.5f, 8.25f, 7f, 0f},
            {9f, 6f, 2f, 6f, 9f, 0f}
          }
        };
    this.friendlyKey = friendly_;
    this.index = index_;
    this.dashArray = dashArrayValues[index_];
  }

  public String getName() {
    return I18N.getText(friendlyKey);
  }
}
