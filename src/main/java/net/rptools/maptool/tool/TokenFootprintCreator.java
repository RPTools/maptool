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
package net.rptools.maptool.tool;

import com.thoughtworks.xstream.XStream;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.model.TokenFootprint;

public class TokenFootprintCreator {
  public static void main(String[] args) {
    // List<TokenFootprint> footprintList = makeHorizHex();
    List<TokenFootprint> footprintList = makeVertHex();
    // List<TokenFootprint> footprintList = makeSquare();
    // List<TokenFootprint> footprintList = makeGridless();
    XStream xstream = FileUtil.getConfiguredXStream();
    System.out.println(xstream.toXML(footprintList));
  }

  private static Point[] points(int[][] points) {
    Point[] pa = new Point[points.length];
    for (int i = 0; i < points.length; i++) {
      pa[i] = new Point(points[i][0], points[i][1]);
    }
    return pa;
  }

  private static Point[] squarePoints(int size) {
    Point[] pa = new Point[size * size - 1];

    int indx = 0;
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        if (y == 0 && x == 0) {
          continue;
        }
        pa[indx] = new Point(x, y);
        indx++;
      }
    }
    return pa;
  }

  private static List<TokenFootprint> makeSquare() {
    List<TokenFootprint> footprintList =
        Arrays.asList(
            new TokenFootprint[] {
              // SQUARE
              new TokenFootprint("Medium", true, 1.0),
              new TokenFootprint("Large", squarePoints(2)),
              new TokenFootprint("Huge", squarePoints(3)),
              new TokenFootprint("Gargantuan", squarePoints(4)),
              new TokenFootprint("Colossal", squarePoints(6))
            });
    return footprintList;
  }

  private static List<TokenFootprint> makeVertHex() {
    List<TokenFootprint> footprintList =
        Arrays.asList(
            new TokenFootprint[] {
              // HEXES
              new TokenFootprint("1/6", false, .408),
              new TokenFootprint("1/4", false, .500),
              new TokenFootprint("1/3", false, .577),
              new TokenFootprint("1/2", false, .707),
              new TokenFootprint("2/3", false, .816),
              new TokenFootprint("Medium", true, 1.0),
              new TokenFootprint(
                  "Large",
                  points(
                      new int[][] {
                        {0, 1},
                        {1, 0},
                      })),
              new TokenFootprint(
                  "Huge",
                  points(
                      new int[][] {
                        {-1, -1},
                        {-1, 0},
                        {0, -1},
                        {0, 1},
                        {1, -1},
                        {1, 0}
                      })),
              new TokenFootprint(
                  "Humongous",
                  points(
                      new int[][] {
                        {-2, -1},
                        {-2, 0},
                        {-2, 1},
                        {-1, -2},
                        {-1, -1},
                        {-1, 0},
                        {-1, 1},
                        {0, -2},
                        {0, -1},
                        {0, 1},
                        {0, 2},
                        {1, -2},
                        {1, -1},
                        {1, 0},
                        {1, 1},
                        {2, -1},
                        {2, 0},
                        {2, 1}
                      }))
            });
    return footprintList;
  }

  private static List<TokenFootprint> makeHorizHex() {
    List<TokenFootprint> footprintList =
        Arrays.asList(
            new TokenFootprint[] {
              // Horizontal Hex Grid - Flipped x <> y from Vert grid
              new TokenFootprint("1/6", false, .408),
              new TokenFootprint("1/4", false, .500),
              new TokenFootprint("1/3", false, .577),
              new TokenFootprint("1/2", false, .707),
              new TokenFootprint("2/3", false, .816),
              new TokenFootprint("Medium", true, 1.0),
              new TokenFootprint(
                  "Large",
                  points(
                      new int[][] {
                        {1, 0},
                        {0, 1},
                      })),
              new TokenFootprint(
                  "Huge",
                  points(
                      new int[][] {
                        {0, 1},
                        {1, 0},
                        {-1, 0},
                        {-1, -1},
                        {0, -1},
                        {-1, 1} // Only one actually different as the others had mirrors already
                      })),
              new TokenFootprint(
                  "Humongous",
                  points(
                      new int[][] {
                        {-1, -2},
                        {0, -2},
                        {1, -2},
                        {-2, -1},
                        {-1, -1},
                        {0, -1},
                        {1, -1},
                        {-2, 0},
                        {-1, 0},
                        {1, 0},
                        {2, 0},
                        {-2, 1},
                        {-1, 1},
                        {0, 1},
                        {1, 1},
                        {-1, 2},
                        {0, 2},
                        {1, 2}
                      }))
            });
    return footprintList;
  }

  private static List<TokenFootprint> makeGridless() {
    List<TokenFootprint> footprintList =
        Arrays.asList(
            new TokenFootprint[] {
              new TokenFootprint("-11", false, 0.086),
              new TokenFootprint("-10", false, 0.107),
              new TokenFootprint("-9", false, 0.134),
              new TokenFootprint("-8", false, 0.168),
              new TokenFootprint("-7", false, 0.210),
              new TokenFootprint("-6", false, 0.262),
              new TokenFootprint("-5", false, 0.328),
              new TokenFootprint("-4", false, 0.410),
              new TokenFootprint("-3", false, 0.512),
              new TokenFootprint("-2", false, 0.640),
              new TokenFootprint("-1", false, 0.800),
              new TokenFootprint("0", true, 1.000),
              new TokenFootprint("1", false, 1.200),
              new TokenFootprint("2", false, 1.440),
              new TokenFootprint("3", false, 1.728),
              new TokenFootprint("4", false, 2.074),
              new TokenFootprint("5", false, 2.488),
              new TokenFootprint("6", false, 2.986),
              new TokenFootprint("7", false, 3.583),
              new TokenFootprint("8", false, 4.300),
              new TokenFootprint("9", false, 5.160),
              new TokenFootprint("10", false, 6.192),
              new TokenFootprint("11", false, 7.430),
              new TokenFootprint("12", false, 8.916),
              new TokenFootprint("13", false, 10.699),
              new TokenFootprint("14", false, 12.839),
              new TokenFootprint("15", false, 15.407),
              new TokenFootprint("16", false, 18.488),
              new TokenFootprint("17", false, 22.186),
              new TokenFootprint("18", false, 26.623),
              new TokenFootprint("19", false, 31.948),
              new TokenFootprint("20", false, 38.338)
            });
    return footprintList;
  }
}
