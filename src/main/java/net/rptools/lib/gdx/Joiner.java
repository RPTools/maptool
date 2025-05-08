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
package net.rptools.lib.gdx;

import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeUtils;

// taken from shapedrawer because its protected there
public class Joiner {

  static final Vector2 AB = new Vector2(), BC = new Vector2(), v = new Vector2();

  // All methods here set D and E based on A,B,C.
  // D is always on left E is on right, relative to AB.
  // Treat straight line as special case as in this case mitres have undefined length.
  // "Inside point" refers to whichever of D or E is on the smaller angle side, vice versa for
  // "outside point".

  // see
  // https://math.stackexchange.com/questions/1849784/calculate-miter-points-of-stroked-vectors-in-cartesian-plane
  public static float preparePointyJoin(
      Vector2 A, Vector2 B, Vector2 C, Vector2 D, Vector2 E, float halfLineWidth) {
    AB.set(B).sub(A);
    BC.set(C).sub(B);
    float angle = ShapeUtils.angleRad(AB, BC);
    if (ShapeUtils.epsilonEquals(angle, 0) || ShapeUtils.epsilonEquals(angle, ShapeUtils.PI2)) {
      prepareStraightJoin(B, D, E, halfLineWidth);
      return angle;
    }
    float len = (float) (halfLineWidth / Math.sin(angle));
    boolean bendsLeft = angle < 0;
    AB.setLength(len);
    BC.setLength(len);
    Vector insidePoint = bendsLeft ? D : E;
    Vector outsidePoint = bendsLeft ? E : D;
    insidePoint.set(B).sub(AB).add(BC);
    outsidePoint.set(B).add(AB).sub(BC);
    return angle;
  }

  public static boolean prepareSmoothJoin(
      Vector2 A,
      Vector2 B,
      Vector2 C,
      Vector2 D,
      Vector2 E,
      float halfLineWidth,
      boolean startOfEdge) {
    AB.set(B).sub(A);
    BC.set(C).sub(B);
    float angle = ShapeUtils.angleRad(AB, BC);
    if (ShapeUtils.epsilonEquals(angle, 0) || ShapeUtils.epsilonEquals(angle, ShapeUtils.PI2)) {
      prepareStraightJoin(B, D, E, halfLineWidth);
      return true;
    }
    float len = (float) (halfLineWidth / Math.sin(angle));
    AB.setLength(len);
    BC.setLength(len);
    boolean bendsLeft = angle < 0;
    Vector insidePoint = bendsLeft ? D : E;
    Vector outsidePoint = bendsLeft ? E : D;
    insidePoint.set(B).sub(AB).add(BC);
    // edgeDirection points towards the relevant edge - is this being calculated for the start of BC
    // or the end of AB?
    Vector2 edgeDirection = startOfEdge ? BC : AB;
    // rotate edgeDirection PI/2 towards outsidePoint
    if (bendsLeft) {
      v.set(edgeDirection.y, -edgeDirection.x); // rotate PI/2 to the right (clockwise)
    } else {
      v.set(-edgeDirection.y, edgeDirection.x); // rotate PI/2 to the left (anticlockwise)
    }
    v.setLength(halfLineWidth);
    outsidePoint.set(B).add(v);
    return bendsLeft;
  }

  public static void prepareStraightJoin(Vector2 B, Vector2 D, Vector2 E, float halfLineWidth) {
    AB.setLength(halfLineWidth);
    D.set(-AB.y, AB.x).add(B);
    E.set(AB.y, -AB.x).add(B);
  }

  public static Vector2 prepareFlatEndpoint(
      float pathPointX,
      float pathPointY,
      float endPointX,
      float endPointY,
      Vector2 D,
      Vector2 E,
      float halfLineWidth) {
    v.set(endPointX, endPointY).sub(pathPointX, pathPointY).setLength(halfLineWidth);
    D.set(v.y, -v.x).add(endPointX, endPointY);
    E.set(-v.y, v.x).add(endPointX, endPointY);
    return v;
  }

  public static void prepareFlatEndpoint(
      Vector2 pathPoint, Vector2 endPoint, Vector2 D, Vector2 E, float halfLineWidth) {
    prepareFlatEndpoint(pathPoint.x, pathPoint.y, endPoint.x, endPoint.y, D, E, halfLineWidth);
  }

  public static void prepareSquareEndpoint(
      float pathPointX,
      float pathPointY,
      float endPointX,
      float endPointY,
      Vector2 D,
      Vector2 E,
      float halfLineWidth) {
    v.set(endPointX, endPointY).sub(pathPointX, pathPointY).setLength(halfLineWidth);
    D.set(v.y, -v.x).add(endPointX, endPointY).add(v);
    E.set(-v.y, v.x).add(endPointX, endPointY).add(v);
  }

  public static void prepareSquareEndpoint(
      Vector2 pathPoint, Vector2 endPoint, Vector2 D, Vector2 E, float halfLineWidth) {
    prepareSquareEndpoint(pathPoint.x, pathPoint.y, endPoint.x, endPoint.y, D, E, halfLineWidth);
  }

  public static void prepareRadialEndpoint(Vector2 A, Vector2 D, Vector2 E, float halfLineWidth) {
    v.set(A).setLength(halfLineWidth);
    D.set(A).sub(v);
    E.set(A).add(v);
  }
}
