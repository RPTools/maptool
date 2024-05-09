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

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtilTest {
  private static final PrecisionModel precisionModel = GeometryUtil.getPrecisionModel();
  private static final GeometryFactory geometryFactory = new GeometryFactory(precisionModel);

  @ParameterizedTest
  @DisplayName(
      "Verify that meaningful topology is returned when converting AWT Area to JTS Geometry")
  @MethodSource("areaProvider2")
  void testConversionFromAreaToGeometry2(Area area, List<Polygon> expectedPolygons) {
    var expectedGeometry =
        geometryFactory.createMultiPolygon(expectedPolygons.toArray(Polygon[]::new));

    Geometry geometry = GeometryUtil.toJts(area);
    Collection<Polygon> polygons = GeometryUtil.toJtsPolygons(area);

    assert expectedGeometry.equalsTopo(geometry) : "Geometry must have the correct topology";

    final var multiPolygon = geometryFactory.createMultiPolygon(polygons.toArray(Polygon[]::new));
    assert expectedGeometry.equalsTopo(multiPolygon) : "Polygons must have the correct topology";
  }

  private static Iterable<Arguments> areaProvider2() {
    final var argumentsList = new ArrayList<Arguments>();

    // region Connected boxes
    {
      /*
       * This shape is two squares connected by a thin rectangle. The JTS representation should be
       * a single polygon with no holes.
       */

      final var path = new Path2D.Double();
      path.moveTo(500.0, -300.0);
      path.lineTo(500.0, -200.0);
      path.lineTo(599.0, -200.0);
      path.lineTo(599.0, -100.0);
      path.lineTo(300.0, -100.0);
      path.lineTo(300.0, 200.0);
      path.lineTo(600.0, 200.0);
      path.lineTo(600.0, -300.0);
      path.lineTo(600.0, -300.0);
      path.closePath();
      final var area = new Area(path);

      final var polygon =
          createPrecisePolygon(
              new Coordinate[] {
                new Coordinate(500, -300),
                new Coordinate(500, -200),
                new Coordinate(599, -200),
                new Coordinate(599, -100),
                new Coordinate(300, -100),
                new Coordinate(300, 200),
                new Coordinate(600, 200),
                new Coordinate(600, -300),
                new Coordinate(500, -300),
              });

      argumentsList.add(Arguments.of(Named.of("Connected boxes", area), List.of(polygon)));
    }
    // endregion

    // region Cut and paste back connected areas
    {
      /*
       * This shape is a decagon with two thin trapezoids butted up against it along the diagonals.
       * JTS should treat this as a single shape, despite slight inaccuracies at the intersections
       * making it look like three separate shapes.
       */

      final var path = new Path2D.Double();
      path.moveTo(108.0, -237.0);
      path.lineTo(108.0, 34.666666666666664);
      path.lineTo(110.0, 36.0);
      path.lineTo(110.0, -237.0);
      path.closePath();
      path.moveTo(97.0, 34.0);
      path.lineTo(88.0, 40.0);
      path.lineTo(85.0, 50);
      path.lineTo(88.0, 60.0);
      path.lineTo(97.0, 66.0);
      path.lineTo(107.0, 66.0);
      path.lineTo(116.0, 60.0);
      path.lineTo(119.0, 50);
      path.lineTo(116.0, 40.0);
      path.lineTo(107.0, 34.0);
      path.closePath();
      path.moveTo(110.0, 64.0);
      path.lineTo(108.0, 65.33333333333333);
      path.lineTo(108.0, 336.0);
      path.lineTo(110.0, 336.0);
      path.closePath();
      final var area = new Area(path);

      final var polygons =
          new Polygon[] {
            createPrecisePolygon(
                new Coordinate[] {
                  // Second trapezoid
                  new Coordinate(108.0, -237.0),
                  new Coordinate(108.0, 34.666666666666664),

                  // Start the decagon
                  new Coordinate(107.0, 34.0),
                  new Coordinate(97.0, 34.0),
                  new Coordinate(88.0, 40.0),
                  new Coordinate(85.0, 50),
                  new Coordinate(88.0, 60.0),
                  new Coordinate(97.0, 66.0),
                  new Coordinate(107.0, 66.0),

                  // First trapezoid
                  new Coordinate(108.0, 65.33333333333333),
                  new Coordinate(108.0, 336.0),
                  new Coordinate(110.0, 336.0),
                  new Coordinate(110.0, 64.0),

                  // Back to the decagon
                  new Coordinate(116.0, 60.0),
                  new Coordinate(119.0, 50),
                  new Coordinate(116.0, 40.0),

                  // Second trapezoid
                  new Coordinate(110.0, 36.0),
                  new Coordinate(110.0, -237.0),

                  // Close the decagon
                  // new Coordinate(107.0, 34.0),

                  new Coordinate(108.0, -237.0),
                }),
          };

      argumentsList.add(
          Arguments.of(Named.of("Cut and paste back connected areas", area), List.of(polygons)));
    }
    // endregion

    // region Tiny crack in walls
    {
      /*
       * This shape represents a mostly rectangular room, but with the top right corner trimmed at
       * a 45° angle and a doorway inserted. The slight inaccuracies in the doorway lead to a tiny
       * crack through the wall, but we still want JTS to treat this as a single polygon with one
       * hole.
       */

      final var path = new Path2D.Double();
      // Problem ring
      path.moveTo(425.37261969692196, 169.3726043021015);
      path.lineTo(425.3726196273217, 169.37260437170178);
      path.lineTo(425.3726196273216, 169.37260437170178);
      path.lineTo(425.37261969692196, 169.3726043021015);
      path.closePath();

      path.moveTo(383.37867981847495, 129.5);
      path.lineTo(424.3119596527195, 170.43326473749002);
      path.lineTo(425.0190663035106, 169.72615782590816);
      path.lineTo(470.27382686129107, 214.98094890126663);
      path.lineTo(470.6273803120475, 214.62739568892906);
      // Problem point
      path.lineTo(470.62738031204753, 214.62739568892906);
      path.lineTo(469.566720394907, 215.68805599725565);
      path.lineTo(510.5, 256.6213205055942);
      path.lineTo(510.5, 382.5);
      path.lineTo(129.5, 382.5);
      path.lineTo(129.5, 129.5);
      path.closePath();

      path.moveTo(126.5, 126.5);
      path.lineTo(126.5, 128.0);
      path.lineTo(126.5, 384.0);
      path.lineTo(126.5, 385.5);
      path.lineTo(513.5, 385.5);
      path.lineTo(513.5, 384.0);
      path.lineTo(513.5, 256.0);
      path.lineTo(513.5, 255.37867949440576);
      path.lineTo(513.0606599761868, 254.93933963262717);
      path.lineTo(471.6880403472805, 213.56673526250998);
      path.lineTo(470.9809336964894, 214.27384217409178);
      // Problem point.
      path.lineTo(470.98093369648933, 214.27384217409178);
      path.lineTo(425.72617313870893, 169.01905109873337);
      path.lineTo(425.3726197142864, 169.3726042847371);
      path.lineTo(426.433279605093, 168.31194400274435);
      path.lineTo(385.06065997618674, 126.93933963262715);
      path.lineTo(384.62132018152505, 126.5);
      path.closePath();
      final var area = new Area(path);

      // JTS should recognize this as a ring with one hole. If we're not careful, it ends up having
      // a superfluous hole.
      final var polygon =
          createPrecisePolygon(
              new Coordinate[] {
                new Coordinate(126.5, 126.5),
                new Coordinate(126.5, 128.0),
                new Coordinate(126.5, 384.0),
                new Coordinate(126.5, 385.5),
                new Coordinate(513.5, 385.5),
                new Coordinate(513.5, 384.0),
                new Coordinate(513.5, 256.0),
                new Coordinate(513.5, 255.37867949440576),
                new Coordinate(513.0606599761868, 254.93933963262717),
                new Coordinate(471.6880403472805, 213.56673526250998),
                new Coordinate(470.9809336964894, 214.27384217409178),
                new Coordinate(425.72617313870893, 169.01905109873337),
                // This point must be removed at reduced precision for correctness.
                // new Coordinate(425.3726197142864, 169.3726042847371),
                new Coordinate(426.433279605093, 168.31194400274435),
                new Coordinate(385.06065997618674, 126.93933963262715),
                new Coordinate(384.62132018152505, 126.5),
                new Coordinate(126.5, 126.5),
              },
              new Coordinate[] {
                new Coordinate(383.37867981847495, 129.5),
                new Coordinate(424.3119596527195, 170.43326473749002),
                new Coordinate(425.0190663035106, 169.72615782590816),
                new Coordinate(470.27382686129107, 214.98094890126663),
                // This point must be removed at reduced precision for correctness.
                // new Coordinate(470.6273803120475, 214.62739568892906),
                new Coordinate(469.566720394907, 215.68805599725565),
                new Coordinate(510.5, 256.6213205055942),
                new Coordinate(510.5, 382.5),
                new Coordinate(129.5, 382.5),
                new Coordinate(129.5, 129.5),
                new Coordinate(383.37867981847495, 129.5),
              });

      argumentsList.add(Arguments.of(Named.of("Tiny crack in area", area), List.of(polygon)));
    }
    // endregion

    // region Polygon vertices touching edges
    {
      /*
       * This shape is a number of nested polygons with vertex-on-edge intersections:
       * 1. The outer shape is a rectangle with a rectangular hole.
       * 2. Nested in that is a diamond with all four corners touching the edge of the hole.
       * 3. A square hole is punched out of the diamond, with all four corners of the square
       *    touching edges of the diamond.
       *
       * AWT and JTS treat this case quite differently from one another.
       *
       * The path below is how AWT represents this area: one subpath for the outer edge of the
       * rectangle; another two subpaths for the left and right triangular leftovers of the diamond;
       * and one final subpath to represent the rectangular hole and top and bottom triangular
       * leftovers of the diamond, with self-intersections.
       *
       *
       */

      // This shape is a rectangle with a rectangular hole.
      // A diamond is in the hole with all four corners touching the edge of the hole.
      // A square is punched out of the diamond with all corners touching the edge of the diamond.
      // This tests a big difference between how AWT and JTS treat polygons.

      final var path = new Path2D.Double();
      path.moveTo(-100.0, -100.0);
      path.lineTo(-500.0, 100.0);
      path.lineTo(-100.0, 300.0);
      path.lineTo(-100.0, -100.0);
      path.closePath();
      path.moveTo(300.0, -100.0);
      path.lineTo(300.0, 300.0);
      path.lineTo(700.0, 100.0);
      path.lineTo(300.0, -100.0);
      path.closePath();
      path.moveTo(700.0, -200.0);
      path.lineTo(700.0, 400.0);
      path.lineTo(100.0, 400.0);
      path.lineTo(300.0, 300.0);
      path.lineTo(-100.0, 300.0);
      path.lineTo(100.0, 400.0);
      path.lineTo(-500.0, 400.0);
      path.lineTo(-500.0, -200.0);
      path.lineTo(100.0, -200.0);
      path.lineTo(-100.0, -100.0);
      path.lineTo(300.0, -100.0);
      path.lineTo(100.0, -200.0);
      path.closePath();
      path.moveTo(-600.0, -300.0);
      path.lineTo(-600.0, 500.0);
      path.lineTo(800.0, 500.0);
      path.lineTo(800.0, -300.0);
      path.closePath();
      final var area = new Area(path);

      // JTS should represent the shape as a rectangle with a hole, and four triangles in the hole.
      final var polygons =
          new Polygon[] {
            // Outer rectangle.
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(-600.0, -300.0),
                  new Coordinate(-600.0, 500.0),
                  new Coordinate(800.0, 500.0),
                  new Coordinate(800.0, -300.0),
                  new Coordinate(-600.0, -300.0),
                },
                new Coordinate[] {
                  new Coordinate(700.0, 100.0),
                  new Coordinate(700.0, 400.0),
                  new Coordinate(100.0, 400.0),
                  new Coordinate(-500.0, 400.0),
                  new Coordinate(-500.0, 100.0),
                  new Coordinate(-500.0, -200.0),
                  new Coordinate(100.0, -200.0),
                  new Coordinate(700.0, -200.0),
                  new Coordinate(700.0, 100.0),
                }),
            // Inner triangles
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(-100.0, 300.0),
                  new Coordinate(-100.0, -100.0),
                  new Coordinate(-500.0, 100.0),
                  new Coordinate(-100.0, 300.0),
                }),
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(-100.0, -100.0),
                  new Coordinate(300.0, -100.0),
                  new Coordinate(100.0, -200.0),
                  new Coordinate(-100.0, -100.0),
                }),
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(300.0, 300.0),
                  new Coordinate(-100.0, 300.0),
                  new Coordinate(100.0, 400.0),
                  new Coordinate(300.0, 300.0),
                }),
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(300.0, -100.0),
                  new Coordinate(300.0, 300.0),
                  new Coordinate(700.0, 100.0),
                  new Coordinate(300.0, -100.0),
                }),
          };

      argumentsList.add(
          Arguments.of(Named.of("Polygon vertices touching edges", area), List.of(polygons)));
    }
    // endregion

    // region Butt joint accuracy
    {
      /*
       * This shape is taken from a merger of map VBL and token VBL. Visually it is a simple closed
       * ring with a matching hole punched out. But when the token VBL portion was transferred to
       * the map, this resulted in a stitching artifact that confuses JTS at high precisions. We
       * want JTS to treat this as a polygon with one hole, but if we try to be too precise JTS can
       * sometimes treat the hole as a polygon in its own right, breaking vision and pathfinding.
       */

      final var path = new Path2D.Double();
      path.moveTo(4999.0, 6201.0);
      path.lineTo(4999.0, 7300.0);
      path.lineTo(4999.0, 7301.0);
      path.lineTo(4998.722296378061, 7301.0);
      path.lineTo(4974.217345883012, 7400.0);
      path.lineTo(4974.0, 7400.0);
      path.lineTo(4974.0, 7547.585786437627);
      path.lineTo(4962.0, 7535.585786437627);
      path.lineTo(4962.0, 7535.585786819458);
      path.lineTo(4950.707106590271, 7524.292892456055);
      path.lineTo(4950.0, 7524.0);
      path.lineTo(4801.0, 7524.0);
      path.lineTo(4801.0, 7500.0);
      path.lineTo(4801.0, 7499.0);
      path.lineTo(3501.0, 7499.0);
      path.lineTo(3501.0, 6554.0);
      path.lineTo(3501.0, 6300.0);
      path.lineTo(3501.0, 6201.0);
      path.closePath();

      path.moveTo(3499.0, 6199.0);
      path.lineTo(3499.0, 6200.0);
      path.lineTo(3499.0, 6300.0);
      path.lineTo(3499.0, 6554.0);
      path.lineTo(3499.0, 7500.0);
      path.lineTo(3499.0, 7501.0);
      path.lineTo(4799.0, 7501.0);
      path.lineTo(4799.0, 7526.0);
      path.lineTo(4824.0, 7526.0);
      path.lineTo(4824.0, 7549.585786437628);
      // Problem point
      path.lineTo(4823.999999999999, 7549.585786437628);
      path.lineTo(4822.585786437627, 7551.0);
      path.lineTo(4825.414213562373, 7551.0);
      path.lineTo(4825.707106781187, 7550.707106781187);
      path.lineTo(4826.0, 7550.0);
      path.lineTo(4826.0, 7550.414213180542);
      path.lineTo(4850.414215087891, 7526.0);
      path.lineTo(4949.585786819458, 7526.0);
      path.lineTo(4962.0, 7538.414213180542);
      // Problem point
      path.lineTo(4962.0, 7538.414213562372);
      // Problem point
      path.lineTo(4961.999999999999, 7538.414213562372);
      // Problem point
      path.lineTo(4974.000000000001, 7550.414213562374);
      path.lineTo(4974.0, 7550.414213562374);
      path.lineTo(4974.0, 7551.0);
      path.lineTo(4976.0, 7551.0);
      path.lineTo(4976.0, 7549.0);
      path.lineTo(4976.0, 7401.121922632632);
      path.lineTo(5000.970705214058, 7300.240273567836);
      path.lineTo(5001.277703621939, 7299.0);
      path.lineTo(5001.0, 7299.0);
      path.lineTo(5001.0, 6200.0);
      path.lineTo(5001.0, 6199.0);
      path.closePath();
      final var area = new Area(path);

      final var polygon =
          createPrecisePolygon(
              new Coordinate[] {
                new Coordinate(4824.0, 7549.585786437628),
                // Here we removed the problem point.
                new Coordinate(4822.585786437627, 7551.0),
                new Coordinate(4825.414213562373, 7551.0),
                new Coordinate(4825.707106781187, 7550.707106781187),
                new Coordinate(4826.0, 7550.0),
                new Coordinate(4826.0, 7550.414213180542),
                new Coordinate(4850.414215087891, 7526.0),
                new Coordinate(4949.585786819458, 7526.0),
                new Coordinate(4962.0, 7538.414213180542),
                new Coordinate(
                    4962.0,
                    7538.414213562372), // This shouldn't really exist for reasonable precisions.
                // Here we removed two problem points.
                new Coordinate(4974.0, 7550.414213562374),
                new Coordinate(4974.0, 7551.0),
                new Coordinate(4976.0, 7551.0),
                new Coordinate(4976.0, 7549.0),
                new Coordinate(4976.0, 7401.121922632632),
                new Coordinate(5000.970705214058, 7300.240273567836),
                new Coordinate(5001.277703621939, 7299.0),
                new Coordinate(5001.0, 7299.0),
                new Coordinate(5001.0, 6200.0),
                new Coordinate(5001.0, 6199.0),
                new Coordinate(3499.0, 6199.0),
                new Coordinate(3499.0, 6200.0),
                new Coordinate(3499.0, 6300.0),
                new Coordinate(3499.0, 6554.0),
                new Coordinate(3499.0, 7500.0),
                new Coordinate(3499.0, 7501.0),
                new Coordinate(4799.0, 7501.0),
                new Coordinate(4799.0, 7526.0),
                new Coordinate(4824.0, 7526.0),
                new Coordinate(4824.0, 7549.585786437628),
              },
              new Coordinate[] {
                new Coordinate(4999.0, 6201.0),
                new Coordinate(4999.0, 7300.0),
                new Coordinate(4999.0, 7301.0),
                new Coordinate(4998.722296378061, 7301.0),
                new Coordinate(4974.217345883012, 7400.0),
                new Coordinate(4974.0, 7400.0),
                new Coordinate(4974.0, 7547.585786437627),
                new Coordinate(4962.0, 7535.585786437627),
                new Coordinate(4962.0, 7535.585786819),
                new Coordinate(4950.707106590271, 7524.292892456055),
                new Coordinate(4950.0, 7524.0),
                new Coordinate(4801.0, 7524.0),
                new Coordinate(4801.0, 7500.0),
                new Coordinate(4801.0, 7499.0),
                new Coordinate(3501.0, 7499.0),
                new Coordinate(3501.0, 6554.0),
                new Coordinate(3501.0, 6300.0),
                new Coordinate(3501.0, 6201.0),
                new Coordinate(4999.0, 6201.0),
              });

      argumentsList.add(Arguments.of(Named.of("Butt joint accuracy", area), List.of(polygon)));
    }
    // endregion

    return argumentsList;
  }

  private static LinearRing createPreciseRing(Coordinate[] ring) {
    for (Coordinate coordinate : ring) {
      precisionModel.makePrecise(coordinate);
    }

    return geometryFactory.createLinearRing(ring);
  }

  private static Polygon createPrecisePolygon(Coordinate[] shell, Coordinate[]... holes) {
    final var shellRing = createPreciseRing(shell);
    final var holeRings =
        Arrays.stream(holes).map(GeometryUtilTest::createPreciseRing).toArray(LinearRing[]::new);

    return geometryFactory.createPolygon(shellRing, holeRings);
  }
}
