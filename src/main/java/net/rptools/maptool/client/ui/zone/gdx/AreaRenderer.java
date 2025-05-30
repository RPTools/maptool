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
package net.rptools.maptool.client.ui.zone.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.rptools.lib.gdx.Earcut;
import net.rptools.lib.gdx.Joiner;
import space.earlygrey.shapedrawer.DefaultSideEstimator;
import space.earlygrey.shapedrawer.ShapeDrawer;
import space.earlygrey.shapedrawer.ShapeUtils;
import space.earlygrey.shapedrawer.SideEstimator;

public class AreaRenderer {
  public record TriangledPolygon(float[] vertices, short[] indices) {}

  private final ShapeDrawer drawer;
  private final TextureRegion whitePixel;

  private final FloatArray tmpFloat = new FloatArray();

  private final IntArray segmentIndicies = new IntArray();

  private final Color color = Color.WHITE.cpy();

  public AreaRenderer(ShapeDrawer drawer) {
    this.drawer = drawer;
    this.whitePixel = drawer.getRegion();
  }

  public ShapeDrawer getShapeDrawer() {
    return drawer;
  }

  public void setColor(Color value) {
    color.set(Objects.requireNonNullElse(value, Color.WHITE));
    textureRegion = whitePixel;
  }

  private final float[] floatsFromArea = new float[6];
  private final Vector2 tmpVector = new Vector2();
  private final Vector2 tmpVector0 = new Vector2();
  private final Vector2 tmpVector1 = new Vector2();
  private final Vector2 tmpVector2 = new Vector2();
  private final Vector2 tmpVector3 = new Vector2();
  private final Vector2 tmpVectorOut = new Vector2();

  private TextureRegion textureRegion = null;

  public TextureRegion getTextureRegion() {
    return textureRegion;
  }

  public void setTextureRegion(TextureRegion textureRegion) {
    this.textureRegion = textureRegion;
  }

  public List<TriangledPolygon> triangulate(Area area) {
    if (area == null || area.isEmpty()) return new ArrayList<>();

    var result = new ArrayList<TriangledPolygon>();

    pathToFloatArray(area.getPathIterator(null));

    if (segmentIndicies.size == 1) {
      removeStartFromEnd();
      var vertices = tmpFloat.toArray();
      var indices = Earcut.earcut(vertices).toArray();
      result.add(new TriangledPolygon(vertices, indices));
      return result;
    }

    var lastSegmentIndex = 0;
    var polygons = new ArrayList<Polygon>();
    // Polygons in a PathIterator are ordered. If polygon p contains q, q comes first.
    // So we draw polygons that contains others, those others are the holes.
    var floats = tmpFloat.toArray();
    for (int i = 1; i <= segmentIndicies.size; i++) {
      var idx = i == segmentIndicies.size ? floats.length / 2 : segmentIndicies.get(i);
      var vertexCount = idx - lastSegmentIndex;
      var currentPolyVertices = new float[2 * vertexCount];
      System.arraycopy(floats, 2 * lastSegmentIndex, currentPolyVertices, 0, 2 * vertexCount);
      lastSegmentIndex = idx;

      var poly = new Polygon(currentPolyVertices);
      var holes = new ArrayList<Polygon>();

      for (int j = 0; j < polygons.size(); j++) {
        var prevPoly = polygons.get(j);
        var prevVertices = prevPoly.getVertices();
        if (poly.contains(prevVertices[0], prevVertices[1])) {
          holes.add(prevPoly);
        }
      }
      if (holes.isEmpty()) {
        polygons.add(poly);
        continue;
      }
      tmpFloat.clear();
      tmpFloat.addAll(poly.getVertices());

      short[] holeIndices = new short[holes.size()];
      var lastPoly = poly;
      var lastHoleIndex = 0;
      for (int j = 0; j < holes.size(); j++) {
        lastHoleIndex += lastPoly.getVertices().length;
        holeIndices[j] = (short) (lastHoleIndex / 2);
        lastPoly = holes.get(j);
        polygons.remove(lastPoly);
        tmpFloat.addAll(lastPoly.getVertices());
      }

      var indices = Earcut.earcut(tmpFloat.toArray(), holeIndices, (short) 2);
      result.add(new TriangledPolygon(tmpFloat.toArray(), indices.toArray()));
    }

    for (var poly : polygons) {
      var indices = Earcut.earcut(poly.getVertices());
      result.add(new TriangledPolygon(poly.getVertices(), indices.toArray()));
    }
    return result;
  }

  public void fillArea(PolygonSpriteBatch batch, Area area) {
    if (area == null || area.isEmpty()) return;
    for (var poly : triangulate(area)) {
      var polyRegion = new PolygonRegion(textureRegion, poly.vertices, poly.indices);
      paintRegion(batch, polyRegion);
    }
  }

  public void drawArea(PolygonSpriteBatch batch, Area area, boolean rounded, float thickness) {
    if (area == null || area.isEmpty()) return;

    pathToFloatArray(area.getPathIterator(null));

    if (segmentIndicies.size == 1) {
      removeStartFromEnd(); // start and end vertices are equal. we don't want this
      var polygon =
          drawPathWithJoin(tmpFloat, thickness, rounded ? JoinType.Round : JoinType.Pointy, false);
      paintPolygon(batch, polygon);
    } else {
      var floats = tmpFloat.toArray();
      var lastSegmentIndex = 0;
      for (int i = 1; i <= segmentIndicies.size; i++) {
        var idx = i == segmentIndicies.size ? floats.length / 2 : segmentIndicies.get(i);
        var vertexCount = (idx - lastSegmentIndex);

        tmpFloat.ensureCapacity(2 * vertexCount);
        System.arraycopy(floats, 2 * lastSegmentIndex, tmpFloat.items, 0, 2 * vertexCount);
        tmpFloat.setSize(2 * vertexCount);
        removeStartFromEnd();
        var polygon =
            drawPathWithJoin(
                tmpFloat, thickness, rounded ? JoinType.Round : JoinType.Pointy, false);
        paintPolygon(batch, polygon);
        lastSegmentIndex = idx;
      }
    }
  }

  private void removeStartFromEnd() {
    while (tmpFloat.get(0) == tmpFloat.get(tmpFloat.size - 2)
        && tmpFloat.get(1) == tmpFloat.get(tmpFloat.size - 1)) {
      // make sure we don't have last and first point the same
      tmpFloat.pop();
      tmpFloat.pop();
    }
  }

  public void paintPolygon(PolygonSpriteBatch batch, TriangledPolygon polygon) {
    var polyReg = new PolygonRegion(textureRegion, polygon.vertices, polygon.indices);
    paintRegion(batch, polyReg);
  }

  public void paintVertices(PolygonSpriteBatch batch, float[] vertices, short[] holeIndices) {
    var indices = Earcut.earcut(vertices, holeIndices, (short) 2).toArray();
    var polyReg = new PolygonRegion(textureRegion, vertices, indices);
    paintRegion(batch, polyReg);
  }

  protected void paintRegion(PolygonSpriteBatch batch, PolygonRegion polygonRegion) {
    var oldColor = new Color(batch.getColor());
    batch.setColor(color);
    batch.draw(polygonRegion, 0, 0);
    batch.setColor(oldColor);
  }

  public FloatArray pathToFloatArray(PathIterator it) {
    tmpFloat.clear();
    segmentIndicies.clear();

    var index = 0;
    for (; !it.isDone(); it.next()) {
      int type = it.currentSegment(floatsFromArea);

      switch (type) {
        case PathIterator.SEG_MOVETO:
          //                   System.out.println("Move to: ( " + floatsFromArea[0] + ", " +
          // floatsFromArea[1] + ")");
          tmpFloat.add(floatsFromArea[0], -floatsFromArea[1]);
          segmentIndicies.add(index);
          index += 1;
          break;
        case PathIterator.SEG_CLOSE:
          //                   System.out.println("Close");

          break;
        // return tmpFloat;
        case PathIterator.SEG_LINETO:
          //                  System.out.println("Line to: ( " + floatsFromArea[0] + ", " +
          // floatsFromArea[1] + ")");

          if (tmpFloat.get(tmpFloat.size - 2) != floatsFromArea[0]
              || tmpFloat.get(tmpFloat.size - 1) != -floatsFromArea[1]) {
            tmpFloat.add(floatsFromArea[0], -floatsFromArea[1]);
            index += 1;
          }
          break;
        case PathIterator.SEG_QUADTO:
          //                  System.out.println("quadratic bezier with: ( " + floatsFromArea[0] +
          // ", " + floatsFromArea[1] +
          //                          "), (" + floatsFromArea[2] + ", " + floatsFromArea[3] + ")");

          tmpVector0.set(tmpFloat.get(tmpFloat.size - 2), tmpFloat.get(tmpFloat.size - 1));
          tmpVector1.set(floatsFromArea[0], -floatsFromArea[1]);
          tmpVector2.set(floatsFromArea[2], -floatsFromArea[3]);
          for (var i = 1; i <= GdxRenderer.POINTS_PER_BEZIER; i++) {
            Bezier.quadratic(
                tmpVectorOut,
                i / GdxRenderer.POINTS_PER_BEZIER,
                tmpVector0,
                tmpVector1,
                tmpVector2,
                tmpVector);
            tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
            index += 1;
          }
          break;
        case PathIterator.SEG_CUBICTO:
          //                    System.out.println("cubic bezier with: ( " + floatsFromArea[0] + ",
          // " + floatsFromArea[1] +
          //                            "), (" + floatsFromArea[2] + ", " + floatsFromArea[3] +
          //                            "), (" + floatsFromArea[4] + ", " + floatsFromArea[5] +
          // ")");

          tmpVector0.set(tmpFloat.get(tmpFloat.size - 2), tmpFloat.get(tmpFloat.size - 1));
          tmpVector1.set(floatsFromArea[0], -floatsFromArea[1]);
          tmpVector2.set(floatsFromArea[2], -floatsFromArea[3]);
          tmpVector3.set(floatsFromArea[4], -floatsFromArea[5]);
          for (var i = 1; i <= GdxRenderer.POINTS_PER_BEZIER; i++) {
            Bezier.cubic(
                tmpVectorOut,
                i / GdxRenderer.POINTS_PER_BEZIER,
                tmpVector0,
                tmpVector1,
                tmpVector2,
                tmpVector3,
                tmpVector);
            tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
            index += 1;
          }
          break;
        default:
          System.out.println("Type: " + type);
      }
    }

    return tmpFloat;
  }

  private final Vector2 A = new Vector2();
  private final Vector2 B = new Vector2();
  private final Vector2 C = new Vector2();
  private final Vector2 D = new Vector2();
  private final Vector2 E = new Vector2();
  private final Vector2 E0 = new Vector2();
  private final Vector2 D0 = new Vector2();
  private final Vector2 AB = new Vector2();
  private final Vector2 BC = new Vector2();
  private final Vector2 vec1 = new Vector2();

  public enum JoinType {
    Pointy,
    Smooth,
    Round
  }

  private final Vector2 vert1 = new Vector2();
  private final Vector2 vert2 = new Vector2();
  private final Vector2 vert3 = new Vector2();
  private final Vector2 vert4 = new Vector2();

  private void pushQuad(FloatArray vertices, ShortArray indices) {
    var index = vertices.size / 2;
    vertices.add(vert1.x);
    vertices.add(vert1.y);
    vertices.add(vert2.x);
    vertices.add(vert2.y);
    vertices.add(vert3.x);
    vertices.add(vert3.y);
    vertices.add(vert4.x);
    vertices.add(vert4.y);
    indices.add(index);
    indices.add(index + 1);
    indices.add(index + 2);
    indices.add(index);
    indices.add(index + 2);
    indices.add(index + 3);
  }

  private void pushTriangle(FloatArray vertices, ShortArray indices) {
    var index = vertices.size / 2;
    vertices.add(vert1.x);
    vertices.add(vert1.y);
    vertices.add(vert2.x);
    vertices.add(vert2.y);
    vertices.add(vert3.x);
    vertices.add(vert3.y);
    indices.add(index);
    indices.add(index + 1);
    indices.add(index + 2);
  }

  public TriangledPolygon drawPathWithJoin(
      FloatArray path, float lineWidth, JoinType joinType, boolean open) {
    // this code was adapted from shapedrawer
    float halfWidth = lineWidth / 2f;
    boolean pointyJoin = joinType == JoinType.Pointy;

    var vertices = new FloatArray();
    var indices = new ShortArray();

    if (path.size == 2) {
      var x = path.get(0);
      var y = path.get(1);
      if (joinType == JoinType.Round) {
        vertices.add(x + halfWidth, y);
        addArc(vertices, indices, x, y, halfWidth, 0, MathUtils.PI2 - 0.1f, false);
        vertices.add(x + halfWidth, y);
      } else {
        vert1.set(x - halfWidth, y - halfWidth);
        vert2.set(x - halfWidth, y + halfWidth);
        vert3.set(x + halfWidth, y + halfWidth);
        vert4.set(x + halfWidth, y - halfWidth);
        pushQuad(vertices, indices);
      }
      return new TriangledPolygon(vertices.toArray(), indices.toArray());
    }

    if (path.size == 4) {
      A.set(path.get(0), path.get(1));
      B.set(path.get(2), path.get(3));

      if (joinType == JoinType.Round) {
        Joiner.prepareFlatEndpoint(B, A, D, E, halfWidth);

        vertices.add(D.x);
        vertices.add(D.y);
        vec1.set(D).add(-A.x, -A.y);
        var angle = vec1.angleRad();
        addArc(vertices, indices, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
        vertices.add(E.x);
        vertices.add(E.y);

        vert1.set(D);
        vert2.set(E);

        Joiner.prepareFlatEndpoint(A, B, D, E, halfWidth);
        vertices.add(D.x);
        vertices.add(D.y);
        vec1.set(D).add(-B.x, -B.y);
        angle = vec1.angleRad();
        addArc(vertices, indices, B.x, B.y, halfWidth, angle, angle + MathUtils.PI, false);

        vertices.add(E.x);
        vertices.add(E.y);

        vert3.set(D);
        vert4.set(E);
        pushQuad(vertices, indices);

      } else {
        Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
        E0.set(D);
        vert1.set(D);
        vert2.set(E);

        Joiner.prepareSquareEndpoint(A, B, D, E, halfWidth);
        vert3.set(D);
        vert4.set(E);
        pushQuad(vertices, indices);
      }
      return new TriangledPolygon(vertices.toArray(), indices.toArray());
    }

    for (int i = 2; i < path.size - 2; i += 2) {
      A.set(path.get(i - 2), path.get(i - 1));
      B.set(path.get(i), path.get(i + 1));
      C.set(path.get(i + 2), path.get(i + 3));

      if (pointyJoin) {
        Joiner.preparePointyJoin(A, B, C, D, E, halfWidth);
      } else {
        Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
      }
      vert3.set(D);
      vert4.set(E);

      if (i == 2) {
        if (open) {
          Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
          if (joinType == JoinType.Round) {
            vec1.set(B).sub(A).setLength(halfWidth);
            D.add(vec1);
            E.add(vec1);
            vertices.add(D.x);
            vertices.add(D.y);
            vec1.set(D).add(-A.x, -A.y);
            var angle = vec1.angleRad();
            addArc(vertices, indices, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
            vertices.add(E.x);
            vertices.add(E.y);
          }

          vert1.set(E);
          vert2.set(D);

        } else {
          vec1.set(path.get(path.size - 2), path.get(path.size - 1));
          if (pointyJoin) {
            Joiner.preparePointyJoin(vec1, A, B, D0, E0, halfWidth);
          } else {
            Joiner.prepareSmoothJoin(vec1, A, B, D0, E0, halfWidth, true);
          }
          vert1.set(E0);
          vert2.set(D0);
        }
      }

      float x3, y3, x4, y4;
      if (pointyJoin) {
        x3 = vert3.x;
        y3 = vert3.y;
        x4 = vert4.x;
        y4 = vert4.y;
      } else {
        Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
        x3 = D.x;
        y3 = D.y;
        x4 = E.x;
        y4 = E.y;
      }

      pushQuad(vertices, indices);
      if (!pointyJoin) drawSmoothJoinFill(vertices, indices, A, B, C, D, E, halfWidth, joinType);
      vert1.set(x4, y4);
      vert2.set(x3, y3);
    }

    if (open) {
      // draw last link on path
      Joiner.prepareFlatEndpoint(B, C, D, E, halfWidth);
      if (joinType == JoinType.Round) {

        vertices.add(D.x);
        vertices.add(D.y);
        vec1.set(D).add(-C.x, -C.y);
        var angle = vec1.angleRad();
        addArc(vertices, indices, C.x, C.y, halfWidth, angle, angle + MathUtils.PI, false);
        vertices.add(E.x);
        vertices.add(E.y);
      } else {
        vec1.set(C).sub(B).setLength(halfWidth);
        D.add(vec1);
        E.add(vec1);
      }

      vert3.set(E);
      vert4.set(D);
      pushQuad(vertices, indices);
    } else {
      if (pointyJoin) {
        // draw last link on path
        A.set(path.get(0), path.get(1));
        Joiner.preparePointyJoin(B, C, A, D, E, halfWidth);
        vert3.set(D);
        vert4.set(E);
        pushQuad(vertices, indices);

        // draw connection back to first vertex
        vert1.set(D);
        vert2.set(E);
        vert3.set(E0);
        vert4.set(D0);
        pushQuad(vertices, indices);
      } else {
        // draw last link on path
        A.set(B);
        B.set(C);
        C.set(path.get(0), path.get(1));
        Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
        vert3.set(D);
        vert4.set(E);
        pushQuad(vertices, indices);
        drawSmoothJoinFill(vertices, indices, A, B, C, D, E, halfWidth, joinType);

        // draw connection back to first vertex
        Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
        vert3.set(E);
        vert4.set(D);
        A.set(path.get(2), path.get(3));
        Joiner.prepareSmoothJoin(B, C, A, D, E, halfWidth, false);
        vert1.set(D);
        vert2.set(E);
        pushQuad(vertices, indices);
        drawSmoothJoinFill(vertices, indices, B, C, A, D, E, halfWidth, joinType);
      }
    }
    return new TriangledPolygon(vertices.toArray(), indices.toArray());
  }

  private void drawSmoothJoinFill(
      FloatArray vertices,
      ShortArray indices,
      Vector2 A,
      Vector2 B,
      Vector2 C,
      Vector2 D,
      Vector2 E,
      float halfLineWidth,
      JoinType joinType) {
    boolean bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfLineWidth, false);
    vert1.set(bendsLeft ? E : D);
    vert2.set(bendsLeft ? D : E);
    if (bendsLeft) {
      vec1.set(E);
    } else {
      vec1.set(D);
    }

    bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfLineWidth, true);
    vert3.set(bendsLeft ? E : D);
    pushTriangle(vertices, indices);

    if (joinType == JoinType.Round) {
      if (bendsLeft) {
        AB.set(B).sub(A);
        BC.set(C).sub(B);
        vec1.add(-B.x, -B.y);
        var angle = vec1.angleRad();
        var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
        vertices.add(vert1.x);
        vertices.add(vert1.y);
        addArc(vertices, indices, B.x, B.y, halfLineWidth, angle, angle + angleDiff, false);
        vertices.add(vert3.x);
        vertices.add(vert3.y);
      } else {
        AB.set(B).sub(A);
        BC.set(C).sub(B);
        vec1.add(-B.x, -B.y);
        var angle = vec1.angleRad();
        var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
        vertices.add(vert1.x);
        vertices.add(vert1.y);
        addArc(vertices, indices, B.x, B.y, halfLineWidth, angle, angle + angleDiff, true);
        vertices.add(vert3.x);
        vertices.add(vert3.y);
      }
    }
  }

  private void addArc(
      FloatArray vertices,
      ShortArray indices,
      float centreX,
      float centreY,
      float radius,
      float startAngle,
      float endAngle,
      boolean clockwise) {
    var oldSize = vertices.size;
    var oldVertexCount = oldSize / 2;

    if (startAngle < 0) {
      startAngle += MathUtils.PI2;
    }

    if (endAngle < 0) {
      endAngle += MathUtils.PI2;
    }

    var deltaAngle = (endAngle + MathUtils.PI2 - startAngle) % MathUtils.PI2;
    if (clockwise) {
      deltaAngle = MathUtils.PI2 - deltaAngle;
    }
    var sides = estimateSidesRequired(radius, radius);
    sides *= deltaAngle / MathUtils.PI2;

    var dAnglePerSide = deltaAngle / sides;
    var angle = startAngle;
    angle += dAnglePerSide;
    sides -= 1;
    if (clockwise) {
      dAnglePerSide *= -1;
      angle += 2 * dAnglePerSide;
    }

    for (var i = 1; i <= sides; i++) {
      var cos = MathUtils.cos(angle);
      var sin = MathUtils.sin(angle);
      angle += dAnglePerSide;
      var x = centreX + cos * radius;
      var y = centreY + sin * radius;

      vertices.add(x);
      vertices.add(y);
    }
    var vertexCount = (vertices.size - oldSize) / 2;

    for (int j = 0; j < vertexCount; j++) {
      indices.add(oldVertexCount - 1);
      indices.add(oldVertexCount + j);
      indices.add(oldVertexCount + j + 1);
    }
  }

  private final SideEstimator sideEstimator = new DefaultSideEstimator();

  protected int estimateSidesRequired(float radiusX, float radiusY) {
    return sideEstimator.estimateSidesRequired(1, radiusX, radiusY);
  }
}
