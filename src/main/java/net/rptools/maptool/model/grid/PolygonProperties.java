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

import java.awt.geom.*;
import java.util.Arrays;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

record CellPolyPropsRecord(
    double aspectRatio,
    double[] centreOffset,
    double inradius,
    boolean irregular,
    int num_vertices,
    double rotation,
    double side_length,
    double[] side_lengths) {}

public class PolygonProperties {
  protected static final Logger log = LogManager.getLogger();
  // inradius = radius for inscribed circle(circle that touches edges), where the token sits
  // circumradius = radius for circumscribed circle( circle that touches vertices )
  double aspectRatio = Double.NaN;
  double[] centreOffset = new double[] {0, 0};
  double circumradius = Double.NaN;
  double inradius = 1;
  boolean irregular = false;
  int num_vertices = 0;
  Point2D.Double[] pathPoints;
  double perimeter = Double.NaN;
  GeneralPath polygonPath;
  GeneralPath polygonHalfPath;
  CellPolyPropsRecord record;
  double rotation = 0;
  Rectangle2D shapeBounds;
  double[] side_lengths = new double[] {};
  double side_length = Double.NaN;

  PolygonProperties(int num_vertices) {
    new PolygonProperties(num_vertices, 1);
  }

  PolygonProperties(int num_vertices, double inradius) {
    new PolygonProperties(num_vertices, inradius, 0);
  }

  PolygonProperties(int num_vertices, double diameter, double rotation) {
    if (num_vertices == 0) {
      this.inradius = Double.NaN;
    } else {
      this.num_vertices = num_vertices;
      this.inradius = diameter / 2.0;
      this.side_length = sideLengthFromInradius.apply(this.inradius);
      this.circumradius = circumradiusFromInradius.apply(this.inradius);
      this.perimeter = num_vertices * this.side_length;
      this.rotation = rotation;
      this.polygonPath = polygonTemplate(num_vertices);
      this.shapeBounds = this.polygonPath.getBounds2D();
      this.aspectRatio = this.shapeBounds.getWidth() / this.shapeBounds.getHeight();
    }
    getPathPoints(); // TODO: delete this line
    setRecord();
  }

  public PolygonProperties isometricise() {
    switch (num_vertices) {
      case 4 -> isometricise(45);
      case 3 -> {
        irregular = true;
        isometricise(60);
      }
      case 6 -> {
        irregular = true;
        isometricise(15);
      }
    }
    return this;
  }

  public PolygonProperties offsetCentre(double[] centreOffset_) {
    setCentreOffset(centreOffset_);
    return this;
  }

  private void setRecord() {
    this.record =
        new CellPolyPropsRecord(
            this.aspectRatio,
            this.centreOffset,
            this.inradius,
            this.irregular,
            this.num_vertices,
            this.rotation,
            this.side_length,
            this.side_lengths);
  }

  Function<Double, Double> sideLengthFromInradius =
      (value) -> 2 * value * Math.tan(Math.PI / (double) num_vertices);
  Function<Double, Double> sideLengthFromCircumradius =
      (value) -> 2 * value * Math.sin(Math.PI / num_vertices);
  Function<Double, Double> inRadiusFromSideLength =
      (value) -> value / 2 * Math.atan(Math.PI / num_vertices);
  Function<Double, Double> inRadiusFromCircumradius =
      (value) -> value * Math.cos(Math.PI / num_vertices);
  Function<Double, Double> circumradiusFromSideLength = // length/2 * cosec(Math.PI/num_vertices)
      (value) -> (value / 2) * (1 / Math.sin(Math.PI / num_vertices));
  Function<Double, Double> circumradiusFromInradius = // radius * sec(Math.PI/num_vertices)
      (value) -> value * 1 / Math.cos(Math.PI / num_vertices);

  //   Sec x = 1/ cos x.
  //   Sec x = Hypotenuse / Adjacent
  // Cosec x = 1 / sin x
  // Cosec x = Hypotenuse / Opposite
  public void setCentreOffset(double[] centreOffset_) {
    if (centreOffset != centreOffset_) {
      centreOffset = centreOffset_;
      AffineTransform at = new AffineTransform();
      GeneralPath p = polygonPath;
      GeneralPath hp = polygonPath;
      at.translate(centreOffset[0], centreOffset[1]);
      p.transform(at);
      hp.transform(at);
      polygonPath = p;
      polygonHalfPath = hp;
      setRecord();
    }
  }

  public GeneralPath polygonTemplate(int value) {
    GeneralPath p = new GeneralPath();
    if (value == 1 || value == 0) return p;
    int halfway = (int) Math.ceil(value / 2f);
    for (int i = 0; i < value - 1; i++) {
      if (i == halfway) this.polygonHalfPath = (GeneralPath) p.clone();
      if (i == 0) {
        p.moveTo(
            circumradius * Math.cos(i * Math.TAU / value),
            circumradius * Math.sin(i * Math.TAU / value));
      } else {
        p.lineTo(
            circumradius * Math.cos(i * 2 * Math.TAU / value),
            circumradius * Math.sin(i * 2 * Math.TAU / value));
      }
    }
    p.closePath();
    return p;
  }

  public PolygonProperties isometricise(double angle) {
    AffineTransform at = new AffineTransform();
    GeneralPath s = polygonPath;
    at.rotate(angle);
    s.transform(at);
    at.scale(1, 0.5);
    s.transform(at);
    polygonPath = s;
    if (irregular) {
      getPathPoints();
      iterateMe();
    }
    return this;
  }

  private void getPathPoints() {
    Point2D.Double[] pts = new Point2D.Double[num_vertices + 1];
    AffineTransform at = new AffineTransform();
    PathIterator pi = polygonPath.getPathIterator(at);
    int count = 0;
    while (!pi.isDone()) {
      double[] coords = new double[2];
      pi.currentSegment(coords);
      pts[count] = new Point2D.Double(coords[0], coords[1]);
      count++;
      pi.next();
    }
    pts[count] = pts[0];
    pathPoints = pts;
  }

  private void iterateMe() {
    Point2D.Double pt1, pt2;
    for (int i = 0; i < pathPoints.length - 1; i++) {
      pt1 = pathPoints[i];
      pt2 = pathPoints[i + 1];
      side_lengths[i] = pt1.distance(pt2);
      perimeter += side_lengths[i];
    }
  }

  @Override
  public String toString() {
    return "PolygonProperties{"
        + "\r\taspectRatio="
        + aspectRatio
        + ", centreOffset="
        + Arrays.toString(centreOffset)
        + ", circumradius="
        + circumradius
        + ", inradius="
        + inradius
        + ", num_vertices="
        + num_vertices
        + ", perimeter="
        + perimeter
        + ", polygonPath="
        + polygonPath
        + ", polygonHalfPath="
        + polygonHalfPath
        + ", record="
        + record
        + ", rotation="
        + rotation
        + ", shapeBounds="
        + shapeBounds
        + ", side_length="
        + side_length
        + ", side_lengths="
        + Arrays.toString(side_lengths)
        + '}';
  }
}
