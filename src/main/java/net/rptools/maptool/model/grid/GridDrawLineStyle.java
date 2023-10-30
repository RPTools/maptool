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

public enum GridDrawLineStyle {
  SOLID("Solid", 0),
  DASHED("Dashed", 1),
  DOTTED("Dotted", 2),
  INTERSECTION("Intersection", 3),
  INTERSECTION_MIDPOINT("Intersection & Mid-point", 4);
  private final String friendly;
  private final int index;
  private float dashUnit = 1;
  private float sideLength = 1;

  private float[][] dashArray;
  final float[][][] dashArrays =
      new float[][][] {
        { // solid
          {sideLength}, {sideLength}, {sideLength}, {sideLength}, {sideLength}
        },
        { // dashed
          {1.1f * dashUnit, 1.8f * dashUnit, 1.1f * dashUnit},
          {0.9f * dashUnit, 2.2f * dashUnit, 0.9f * dashUnit},
          {0.8f * dashUnit, 2.4f * dashUnit, 0.8f * dashUnit},
          {0.6f * dashUnit, 2.6f * dashUnit, 0.6f * dashUnit},
          {0.5f * dashUnit, 2.8f * dashUnit, 0.6f * dashUnit},
        },
        { // dotted
          {dashUnit, dashUnit},
          {0.94f * dashUnit, 1.06f * dashUnit},
          {0.88f * dashUnit, 1.12f * dashUnit},
          {0.82f * dashUnit, 1.18f * dashUnit},
          {0.76f * dashUnit, 1.24f * dashUnit}
        },
        { // intersection
          {2.0f * dashUnit, 28 * dashUnit, 2.0f * dashUnit, 0},
          {4.0f * dashUnit, 24 * dashUnit, 4.0f * dashUnit, 0},
          {5.5f * dashUnit, 21 * dashUnit, 5.5f * dashUnit, 0},
          {7 * dashUnit, 18 * dashUnit, 7 * dashUnit, 0},
          {9 * dashUnit, 14 * dashUnit, 9 * dashUnit, 0}
        },
        { // intersection & midpoint
          {
            2.0f * dashUnit,
            13.75f * dashUnit,
            0.5f * dashUnit,
            13.75f * dashUnit,
            2.0f * dashUnit,
            0
          },
          {
            4.0f * dashUnit,
            11.515f * dashUnit,
            0.7f * dashUnit,
            11.515f * dashUnit,
            4.0f * dashUnit,
            0
          },
          {5.5f * dashUnit, 15f * dashUnit, 1f * dashUnit, 15f * dashUnit, 5.5f * dashUnit, 0},
          {7f * dashUnit, 8.25f * dashUnit, 1.5f * dashUnit, 8.25f * dashUnit, 7f * dashUnit, 0},
          {9f * dashUnit, 6f * dashUnit, 2f * dashUnit, 6f * dashUnit, 9f * dashUnit, 0}
        }
      };

  GridDrawLineStyle(String friendly, int index) {
    this.friendly = friendly;
    this.index = index;
  }

  public int index() {
    return index;
  }

  public float[][] dashArray() {
    dashUnit = 1;
    return dashArrays[index];
  }

  public float[][] dashArray(float sideLength) {
    dashUnit = sideLength / 32;
    return dashArrays[index];
  }
  ;
}
