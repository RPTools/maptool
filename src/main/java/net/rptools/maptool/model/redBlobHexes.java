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
package net.rptools.maptool.model;
// Generated code -- CC0 -- No Rights Reserved -- http://www.redblobgames.com/grids/hexagons/

import java.util.ArrayList;

class rbPoint {
  public rbPoint(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public final double x;
  public final double y;
}

class Hex {
  public Hex(int q, int r, int s) {
    this.q = q;
    this.r = r;
    this.s = s;
    if (q + r + s != 0) throw new IllegalArgumentException("q + r + s must be 0");
  }

  public final int q;
  public final int r;
  public final int s;

  public Hex add(Hex b) {
    return new Hex(q + b.q, r + b.r, s + b.s);
  }

  public Hex subtract(Hex b) {
    return new Hex(q - b.q, r - b.r, s - b.s);
  }

  public Hex scale(int k) {
    return new Hex(q * k, r * k, s * k);
  }

  public Hex rotateLeft() {
    return new Hex(-s, -q, -r);
  }

  public Hex rotateRight() {
    return new Hex(-r, -s, -q);
  }

  public static ArrayList<Hex> directions =
      new ArrayList<Hex>() {
        {
          add(new Hex(1, 0, -1));
          add(new Hex(1, -1, 0));
          add(new Hex(0, -1, 1));
          add(new Hex(-1, 0, 1));
          add(new Hex(-1, 1, 0));
          add(new Hex(0, 1, -1));
        }
      };

  public static Hex direction(int direction) {
    return Hex.directions.get(direction);
  }

  public Hex neighbor(int direction) {
    return add(Hex.direction(direction));
  }

  public static ArrayList<Hex> diagonals =
      new ArrayList<Hex>() {
        {
          add(new Hex(2, -1, -1));
          add(new Hex(1, -2, 1));
          add(new Hex(-1, -1, 2));
          add(new Hex(-2, 1, 1));
          add(new Hex(-1, 2, -1));
          add(new Hex(1, 1, -2));
        }
      };

  public Hex diagonalNeighbor(int direction) {
    return add(Hex.diagonals.get(direction));
  }

  public int length() {
    return (int) ((Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2);
  }

  public int distance(Hex b) {
    return subtract(b).length();
  }
}

class FractionalHex {
  public FractionalHex(double q, double r, double s) {
    this.q = q;
    this.r = r;
    this.s = s;
    if (Math.round(q + r + s) != 0) throw new IllegalArgumentException("q + r + s must be 0");
  }

  public final double q;
  public final double r;
  public final double s;

  public Hex hexRound() {
    int qi = (int) (Math.round(q));
    int ri = (int) (Math.round(r));
    int si = (int) (Math.round(s));
    double q_diff = Math.abs(qi - q);
    double r_diff = Math.abs(ri - r);
    double s_diff = Math.abs(si - s);
    if (q_diff > r_diff && q_diff > s_diff) {
      qi = -ri - si;
    } else if (r_diff > s_diff) {
      ri = -qi - si;
    } else {
      si = -qi - ri;
    }
    return new Hex(qi, ri, si);
  }

  public FractionalHex hexLerp(FractionalHex b, double t) {
    return new FractionalHex(
        q * (1.0 - t) + b.q * t, r * (1.0 - t) + b.r * t, s * (1.0 - t) + b.s * t);
  }

  public static ArrayList<Hex> hexLinedraw(Hex a, Hex b) {
    int N = a.distance(b);
    FractionalHex a_nudge = new FractionalHex(a.q + 1e-06, a.r + 1e-06, a.s - 2e-06);
    FractionalHex b_nudge = new FractionalHex(b.q + 1e-06, b.r + 1e-06, b.s - 2e-06);
    ArrayList<Hex> results =
        new ArrayList<Hex>() {
          {
          }
        };
    double step = 1.0 / Math.max(N, 1);
    for (int i = 0; i <= N; i++) {
      results.add(a_nudge.hexLerp(b_nudge, step * i).hexRound());
    }
    return results;
  }
}

class OffsetCoord {
  public OffsetCoord(int col, int row) {
    this.col = col;
    this.row = row;
  }

  public final int col;
  public final int row;
  public static int EVEN = 1;
  public static int ODD = -1;

  public static OffsetCoord qoffsetFromCube(int offset, Hex h) {
    int col = h.q;
    int row = h.r + (int) ((h.q + offset * (h.q & 1)) / 2);
    if (offset != OffsetCoord.EVEN && offset != OffsetCoord.ODD) {
      throw new IllegalArgumentException("offset must be EVEN (+1) or ODD (-1)");
    }
    return new OffsetCoord(col, row);
  }

  public static Hex qoffsetToCube(int offset, OffsetCoord h) {
    int q = h.col;
    int r = h.row - (int) ((h.col + offset * (h.col & 1)) / 2);
    int s = -q - r;
    if (offset != OffsetCoord.EVEN && offset != OffsetCoord.ODD) {
      throw new IllegalArgumentException("offset must be EVEN (+1) or ODD (-1)");
    }
    return new Hex(q, r, s);
  }

  public static OffsetCoord roffsetFromCube(int offset, Hex h) {
    int col = h.q + (int) ((h.r + offset * (h.r & 1)) / 2);
    int row = h.r;
    if (offset != OffsetCoord.EVEN && offset != OffsetCoord.ODD) {
      throw new IllegalArgumentException("offset must be EVEN (+1) or ODD (-1)");
    }
    return new OffsetCoord(col, row);
  }

  public static Hex roffsetToCube(int offset, OffsetCoord h) {
    int q = h.col - (int) ((h.row + offset * (h.row & 1)) / 2);
    int r = h.row;
    int s = -q - r;
    if (offset != OffsetCoord.EVEN && offset != OffsetCoord.ODD) {
      throw new IllegalArgumentException("offset must be EVEN (+1) or ODD (-1)");
    }
    return new Hex(q, r, s);
  }
}

class DoubledCoord {
  public DoubledCoord(int col, int row) {
    this.col = col;
    this.row = row;
  }

  public final int col;
  public final int row;

  public static DoubledCoord qdoubledFromCube(Hex h) {
    int col = h.q;
    int row = 2 * h.r + h.q;
    return new DoubledCoord(col, row);
  }

  public Hex qdoubledToCube() {
    int q = col;
    int r = (int) ((row - col) / 2);
    int s = -q - r;
    return new Hex(q, r, s);
  }

  public static DoubledCoord rdoubledFromCube(Hex h) {
    int col = 2 * h.q + h.r;
    int row = h.r;
    return new DoubledCoord(col, row);
  }

  public Hex rdoubledToCube() {
    int q = (int) ((col - row) / 2);
    int r = row;
    int s = -q - r;
    return new Hex(q, r, s);
  }
}

class Orientation {
  public Orientation(
      double f0,
      double f1,
      double f2,
      double f3,
      double b0,
      double b1,
      double b2,
      double b3,
      double start_angle) {
    this.f0 = f0;
    this.f1 = f1;
    this.f2 = f2;
    this.f3 = f3;
    this.b0 = b0;
    this.b1 = b1;
    this.b2 = b2;
    this.b3 = b3;
    this.start_angle = start_angle;
  }

  public final double f0;
  public final double f1;
  public final double f2;
  public final double f3;
  public final double b0;
  public final double b1;
  public final double b2;
  public final double b3;
  public final double start_angle;
}

class Layout {
  public Layout(Orientation orientation, rbPoint size, rbPoint origin) {
    this.orientation = orientation;
    this.size = size;
    this.origin = origin;
  }

  public final Orientation orientation;
  public final rbPoint size;
  public final rbPoint origin;
  public static Orientation pointy =
      new Orientation(
          Math.sqrt(3.0),
          Math.sqrt(3.0) / 2.0,
          0.0,
          3.0 / 2.0,
          Math.sqrt(3.0) / 3.0,
          -1.0 / 3.0,
          0.0,
          2.0 / 3.0,
          0.5);
  public static Orientation flat =
      new Orientation(
          3.0 / 2.0,
          0.0,
          Math.sqrt(3.0) / 2.0,
          Math.sqrt(3.0),
          2.0 / 3.0,
          0.0,
          -1.0 / 3.0,
          Math.sqrt(3.0) / 3.0,
          0.0);

  public rbPoint hexToPixel(Hex h) {
    Orientation M = orientation;
    double x = (M.f0 * h.q + M.f1 * h.r) * size.x;
    double y = (M.f2 * h.q + M.f3 * h.r) * size.y;
    return new rbPoint(x + origin.x, y + origin.y);
  }

  public FractionalHex pixelToHex(rbPoint p) {
    Orientation M = orientation;
    rbPoint pt = new rbPoint((p.x - origin.x) / size.x, (p.y - origin.y) / size.y);
    double q = M.b0 * pt.x + M.b1 * pt.y;
    double r = M.b2 * pt.x + M.b3 * pt.y;
    return new FractionalHex(q, r, -q - r);
  }

  public rbPoint hexCornerOffset(int corner) {
    Orientation M = orientation;
    double angle = 2.0 * Math.PI * (M.start_angle - corner) / 6.0;
    return new rbPoint(size.x * Math.cos(angle), size.y * Math.sin(angle));
  }

  public ArrayList<rbPoint> polygonCorners(Hex h) {
    ArrayList<rbPoint> corners =
        new ArrayList<rbPoint>() {
          {
          }
        };
    rbPoint center = hexToPixel(h);
    for (int i = 0; i < 6; i++) {
      rbPoint offset = hexCornerOffset(i);
      corners.add(new rbPoint(center.x + offset.x, center.y + offset.y));
    }
    return corners;
  }
}

// Tests

public class redBlobHexes {
  public redBlobHexes() {}

  public static void equalHex(String name, Hex a, Hex b) {
    if (!(a.q == b.q && a.s == b.s && a.r == b.r)) {
      redBlobHexes.complain(name);
    }
  }

  public static void equalOffsetcoord(String name, OffsetCoord a, OffsetCoord b) {
    if (!(a.col == b.col && a.row == b.row)) {
      redBlobHexes.complain(name);
    }
  }

  public static void equalDoubledcoord(String name, DoubledCoord a, DoubledCoord b) {
    if (!(a.col == b.col && a.row == b.row)) {
      redBlobHexes.complain(name);
    }
  }

  public static void equalInt(String name, int a, int b) {
    if (!(a == b)) {
      redBlobHexes.complain(name);
    }
  }

  public static void equalHexArray(String name, ArrayList<Hex> a, ArrayList<Hex> b) {
    redBlobHexes.equalInt(name, a.size(), b.size());
    for (int i = 0; i < a.size(); i++) {
      redBlobHexes.equalHex(name, a.get(i), b.get(i));
    }
  }

  public static void testHexArithmetic() {
    redBlobHexes.equalHex("hex_add", new Hex(4, -10, 6), new Hex(1, -3, 2).add(new Hex(3, -7, 4)));
    redBlobHexes.equalHex(
        "hex_subtract", new Hex(-2, 4, -2), new Hex(1, -3, 2).subtract(new Hex(3, -7, 4)));
  }

  public static void testHexDirection() {
    redBlobHexes.equalHex("hex_direction", new Hex(0, -1, 1), Hex.direction(2));
  }

  public static void testHexNeighbor() {
    redBlobHexes.equalHex("hex_neighbor", new Hex(1, -3, 2), new Hex(1, -2, 1).neighbor(2));
  }

  public static void testHexDiagonal() {
    redBlobHexes.equalHex(
        "hex_diagonal", new Hex(-1, -1, 2), new Hex(1, -2, 1).diagonalNeighbor(3));
  }

  public static void testHexDistance() {
    redBlobHexes.equalInt("hex_distance", 7, new Hex(3, -7, 4).distance(new Hex(0, 0, 0)));
  }

  public static void testHexRotateRight() {
    redBlobHexes.equalHex("hex_rotate_right", new Hex(1, -3, 2).rotateRight(), new Hex(3, -2, -1));
  }

  public static void testHexRotateLeft() {
    redBlobHexes.equalHex("hex_rotate_left", new Hex(1, -3, 2).rotateLeft(), new Hex(-2, -1, 3));
  }

  public static void testHexRound() {
    FractionalHex a = new FractionalHex(0.0, 0.0, 0.0);
    FractionalHex b = new FractionalHex(1.0, -1.0, 0.0);
    FractionalHex c = new FractionalHex(0.0, -1.0, 1.0);
    redBlobHexes.equalHex(
        "hex_round 1",
        new Hex(5, -10, 5),
        new FractionalHex(0.0, 0.0, 0.0)
            .hexLerp(new FractionalHex(10.0, -20.0, 10.0), 0.5)
            .hexRound());
    redBlobHexes.equalHex("hex_round 2", a.hexRound(), a.hexLerp(b, 0.499).hexRound());
    redBlobHexes.equalHex("hex_round 3", b.hexRound(), a.hexLerp(b, 0.501).hexRound());
    redBlobHexes.equalHex(
        "hex_round 4",
        a.hexRound(),
        new FractionalHex(
                a.q * 0.4 + b.q * 0.3 + c.q * 0.3,
                a.r * 0.4 + b.r * 0.3 + c.r * 0.3,
                a.s * 0.4 + b.s * 0.3 + c.s * 0.3)
            .hexRound());
    redBlobHexes.equalHex(
        "hex_round 5",
        c.hexRound(),
        new FractionalHex(
                a.q * 0.3 + b.q * 0.3 + c.q * 0.4,
                a.r * 0.3 + b.r * 0.3 + c.r * 0.4,
                a.s * 0.3 + b.s * 0.3 + c.s * 0.4)
            .hexRound());
  }

  public static void testHexLinedraw() {
    redBlobHexes.equalHexArray(
        "hex_linedraw",
        new ArrayList<Hex>() {
          {
            add(new Hex(0, 0, 0));
            add(new Hex(0, -1, 1));
            add(new Hex(0, -2, 2));
            add(new Hex(1, -3, 2));
            add(new Hex(1, -4, 3));
            add(new Hex(1, -5, 4));
          }
        },
        FractionalHex.hexLinedraw(new Hex(0, 0, 0), new Hex(1, -5, 4)));
  }

  public static void testLayout() {
    Hex h = new Hex(3, 4, -7);
    Layout flat = new Layout(Layout.flat, new rbPoint(10.0, 15.0), new rbPoint(35.0, 71.0));
    redBlobHexes.equalHex("layout", h, flat.pixelToHex(flat.hexToPixel(h)).hexRound());
    Layout pointy = new Layout(Layout.pointy, new rbPoint(10.0, 15.0), new rbPoint(35.0, 71.0));
    redBlobHexes.equalHex("layout", h, pointy.pixelToHex(pointy.hexToPixel(h)).hexRound());
  }

  public static void testOffsetRoundtrip() {
    Hex a = new Hex(3, 4, -7);
    OffsetCoord b = new OffsetCoord(1, -3);
    redBlobHexes.equalHex(
        "conversion_roundtrip even-q",
        a,
        OffsetCoord.qoffsetToCube(
            OffsetCoord.EVEN, OffsetCoord.qoffsetFromCube(OffsetCoord.EVEN, a)));
    redBlobHexes.equalOffsetcoord(
        "conversion_roundtrip even-q",
        b,
        OffsetCoord.qoffsetFromCube(
            OffsetCoord.EVEN, OffsetCoord.qoffsetToCube(OffsetCoord.EVEN, b)));
    redBlobHexes.equalHex(
        "conversion_roundtrip odd-q",
        a,
        OffsetCoord.qoffsetToCube(
            OffsetCoord.ODD, OffsetCoord.qoffsetFromCube(OffsetCoord.ODD, a)));
    redBlobHexes.equalOffsetcoord(
        "conversion_roundtrip odd-q",
        b,
        OffsetCoord.qoffsetFromCube(
            OffsetCoord.ODD, OffsetCoord.qoffsetToCube(OffsetCoord.ODD, b)));
    redBlobHexes.equalHex(
        "conversion_roundtrip even-r",
        a,
        OffsetCoord.roffsetToCube(
            OffsetCoord.EVEN, OffsetCoord.roffsetFromCube(OffsetCoord.EVEN, a)));
    redBlobHexes.equalOffsetcoord(
        "conversion_roundtrip even-r",
        b,
        OffsetCoord.roffsetFromCube(
            OffsetCoord.EVEN, OffsetCoord.roffsetToCube(OffsetCoord.EVEN, b)));
    redBlobHexes.equalHex(
        "conversion_roundtrip odd-r",
        a,
        OffsetCoord.roffsetToCube(
            OffsetCoord.ODD, OffsetCoord.roffsetFromCube(OffsetCoord.ODD, a)));
    redBlobHexes.equalOffsetcoord(
        "conversion_roundtrip odd-r",
        b,
        OffsetCoord.roffsetFromCube(
            OffsetCoord.ODD, OffsetCoord.roffsetToCube(OffsetCoord.ODD, b)));
  }

  public static void testOffsetFromCube() {
    redBlobHexes.equalOffsetcoord(
        "offset_from_cube even-q",
        new OffsetCoord(1, 3),
        OffsetCoord.qoffsetFromCube(OffsetCoord.EVEN, new Hex(1, 2, -3)));
    redBlobHexes.equalOffsetcoord(
        "offset_from_cube odd-q",
        new OffsetCoord(1, 2),
        OffsetCoord.qoffsetFromCube(OffsetCoord.ODD, new Hex(1, 2, -3)));
  }

  public static void testOffsetToCube() {
    redBlobHexes.equalHex(
        "offset_to_cube even-",
        new Hex(1, 2, -3),
        OffsetCoord.qoffsetToCube(OffsetCoord.EVEN, new OffsetCoord(1, 3)));
    redBlobHexes.equalHex(
        "offset_to_cube odd-q",
        new Hex(1, 2, -3),
        OffsetCoord.qoffsetToCube(OffsetCoord.ODD, new OffsetCoord(1, 2)));
  }

  public static void testDoubledRoundtrip() {
    Hex a = new Hex(3, 4, -7);
    DoubledCoord b = new DoubledCoord(1, -3);
    redBlobHexes.equalHex(
        "conversion_roundtrip doubled-q", a, DoubledCoord.qdoubledFromCube(a).qdoubledToCube());
    redBlobHexes.equalDoubledcoord(
        "conversion_roundtrip doubled-q", b, DoubledCoord.qdoubledFromCube(b.qdoubledToCube()));
    redBlobHexes.equalHex(
        "conversion_roundtrip doubled-r", a, DoubledCoord.rdoubledFromCube(a).rdoubledToCube());
    redBlobHexes.equalDoubledcoord(
        "conversion_roundtrip doubled-r", b, DoubledCoord.rdoubledFromCube(b.rdoubledToCube()));
  }

  public static void testDoubledFromCube() {
    redBlobHexes.equalDoubledcoord(
        "doubled_from_cube doubled-q",
        new DoubledCoord(1, 5),
        DoubledCoord.qdoubledFromCube(new Hex(1, 2, -3)));
    redBlobHexes.equalDoubledcoord(
        "doubled_from_cube doubled-r",
        new DoubledCoord(4, 2),
        DoubledCoord.rdoubledFromCube(new Hex(1, 2, -3)));
  }

  public static void testDoubledToCube() {
    redBlobHexes.equalHex(
        "doubled_to_cube doubled-q", new Hex(1, 2, -3), new DoubledCoord(1, 5).qdoubledToCube());
    redBlobHexes.equalHex(
        "doubled_to_cube doubled-r", new Hex(1, 2, -3), new DoubledCoord(4, 2).rdoubledToCube());
  }

  public static void testAll() {
    redBlobHexes.testHexArithmetic();
    redBlobHexes.testHexDirection();
    redBlobHexes.testHexNeighbor();
    redBlobHexes.testHexDiagonal();
    redBlobHexes.testHexDistance();
    redBlobHexes.testHexRotateRight();
    redBlobHexes.testHexRotateLeft();
    redBlobHexes.testHexRound();
    redBlobHexes.testHexLinedraw();
    redBlobHexes.testLayout();
    redBlobHexes.testOffsetRoundtrip();
    redBlobHexes.testOffsetFromCube();
    redBlobHexes.testOffsetToCube();
    redBlobHexes.testDoubledRoundtrip();
    redBlobHexes.testDoubledFromCube();
    redBlobHexes.testDoubledToCube();
  }

  public static void main(String[] args) {
    redBlobHexes.testAll();
  }

  public static void complain(String name) {
    System.out.println("FAIL " + name);
  }
}
