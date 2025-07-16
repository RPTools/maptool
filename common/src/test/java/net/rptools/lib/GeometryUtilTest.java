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

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
  @MethodSource("areaProvider")
  void testConversionFromAreaToGeometry(Shape shape, List<Polygon> expectedPolygons) {
    var expectedGeometry =
        geometryFactory.createMultiPolygon(expectedPolygons.toArray(Polygon[]::new));

    Geometry geometry = GeometryUtil.toJts(shape);
    Collection<Polygon> polygons = GeometryUtil.toJtsPolygons(shape);

    assert expectedGeometry.equalsTopo(geometry) : "Geometry must have the correct topology";

    final var multiPolygon = geometryFactory.createMultiPolygon(polygons.toArray(Polygon[]::new));
    assert expectedGeometry.equalsTopo(multiPolygon) : "Polygons must have the correct topology";
  }

  private static Iterable<Arguments> areaProvider() {
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

      argumentsList.add(argumentSet("Connected boxes", area, List.of(polygon)));
    }
    // endregion

    // region Simple ring with highly precise backstep
    {
      final var path = new Path2D.Double();
      path.moveTo(-50.095238095238095, 1300.0);
      path.lineTo(-47.56210321998909, 1302.0459935530857);
      path.lineTo(-47.56210321998924, 1302.0459935530857);
      path.lineTo(-47.56211853027344, 1302.0460205078125);
      path.lineTo(-34.0, 1313.0);
      path.lineTo(-22.296453965448677, 1316.0530989655351);
      path.lineTo(-22.296453965448904, 1316.0530989655351);
      path.lineTo(-22.29646110534668, 1316.0531005859375);
      path.lineTo(-11.0, 1319.0);
      path.lineTo(-4.618527782440651E-14, 1331.2222222222222);
      path.lineTo(0.0, 1331.2222222222222);
      path.lineTo(0.0, 1300.0);
      path.closePath();
      final var area = new Area(path);
      final var polygons =
          new Polygon[] {
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(-47.56210321998909, 1302.0459935530857),
                  new Coordinate(-47.56211853027344, 1302.0460205078125),
                  new Coordinate(-34, 1313.0),
                  // Bit odd that this points comes first, but it gives good geometry.
                  new Coordinate(-22.29646110534668, 1316.0531005859375),
                  new Coordinate(-22.296453965448677, 1316.0530989655351),
                  new Coordinate(-11.0, 1319.0),
                  new Coordinate(0, 1331.22222),
                  new Coordinate(0, 1300),
                  new Coordinate(-50.095238095238095, 1300.0),
                  new Coordinate(-47.56210321998909, 1302.0459935530857)
                }),
          };

      argumentsList.add(
          argumentSet("Simple ring with highly precise backstep", area, List.of(polygons)));
    }
    // endregion

    // region Octothorpe with several tiny bowties
    {
      final var path = new Path2D.Double();
      {
        path.moveTo(31096.89298687769800, 19958.84411439553500);
        path.lineTo(31210.89219385643000, 20107.34783396915700);
        path.lineTo(31210.89219385642700, 20107.34783396915700);
        path.lineTo(31118.96156955349200, 20131.10263820686700);
        path.lineTo(31118.96156955349600, 20131.10263820686700);
        path.lineTo(31016.92922050016200, 20007.45592609495000);
        path.lineTo(31096.89298687770500, 19958.84411439553500);
        path.closePath();
      }
      {
        path.moveTo(30938.47499442061200, 19745.56575078792600);
        path.lineTo(30934.31191954007000, 19747.05410399316700);
        path.lineTo(31093.02554099086000, 19953.80609602096600);
        path.lineTo(31012.86872776624700, 20002.53526533626400);
        path.lineTo(30825.28245459803000, 19775.21102817247800);
        path.lineTo(30819.84768888939700, 19779.82141036563600);
        path.lineTo(31006.71603098942800, 20006.27563113827000);
        path.lineTo(30711.87783426228000, 20185.51454834266000);
        path.lineTo(30714.85047572821700, 20191.09653889346700);
        path.lineTo(31010.77652372334700, 20011.19629189695500);
        path.lineTo(31111.34617477415700, 20133.07045049518300);
        path.lineTo(31111.34617477416000, 20133.07045049518300);
        path.lineTo(30771.12499411203700, 20220.98334791573000);
        path.lineTo(30773.48922015583200, 20224.71923106947000);
        path.lineTo(31114.30270195864400, 20136.65328347926000);
        path.lineTo(31114.30270195864000, 20136.65328347926000);
        path.lineTo(31232.07575184261800, 20279.37518029587600);
        path.lineTo(31237.51051755124700, 20274.76479810271000);
        path.lineTo(31121.91809673798000, 20134.68547119094400);
        path.lineTo(31121.91809673797600, 20134.68547119094400);
        path.lineTo(31213.67668806736000, 20110.97512014432500);
        path.lineTo(31490.52127924277700, 20471.61316862705300);
        path.lineTo(31494.68435412332000, 20470.12481542181200);
        path.lineTo(31218.10407980696000, 20109.83108544582000);
        path.lineTo(31657.87127955135300, 19996.19557149924700);
        path.lineTo(31655.50705350755700, 19992.45968834550500);
        path.lineTo(31215.31958559603000, 20106.20379927065000);
        path.lineTo(31100.51043328870600, 19956.64498556843700);
        path.lineTo(31100.51043328870200, 19956.64498556843700);
        path.lineTo(31296.52685776363800, 19837.48209532163300);
        path.lineTo(31293.55421629769600, 19831.90010477082300);
        path.lineTo(31096.64298740186500, 19951.60696719386400);
        path.lineTo(30938.47499442061200, 19745.56575078792600);
        path.closePath();
      }

      final var polygons =
          new Polygon[] {
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(30934.31192, 19747.0541),
                  new Coordinate(31093.02554, 19953.8061),
                  new Coordinate(31012.86873, 20002.53527),
                  new Coordinate(30825.28245, 19775.21103),
                  new Coordinate(30819.84769, 19779.82141),
                  new Coordinate(31006.71603, 20006.27563),
                  new Coordinate(30711.87783, 20185.51455),
                  new Coordinate(30714.85048, 20191.09654),
                  new Coordinate(31010.77652, 20011.19629),
                  new Coordinate(31111.34617, 20133.07045),
                  new Coordinate(30771.12499, 20220.98335),
                  new Coordinate(30773.48922, 20224.71923),
                  new Coordinate(31114.3027, 20136.65328),
                  new Coordinate(31232.07575, 20279.37518),
                  new Coordinate(31237.51052, 20274.7648),
                  new Coordinate(31121.9181, 20134.68547),
                  new Coordinate(31213.67669, 20110.97512),
                  new Coordinate(31490.52128, 20471.61317),
                  new Coordinate(31494.68435, 20470.12482),
                  new Coordinate(31218.10408, 20109.83109),
                  new Coordinate(31657.87128, 19996.19557),
                  new Coordinate(31655.50705, 19992.45969),
                  new Coordinate(31215.31959, 20106.2038),
                  new Coordinate(31100.51043, 19956.64499),
                  new Coordinate(31296.52686, 19837.4821),
                  new Coordinate(31293.55422, 19831.9001),
                  new Coordinate(31096.64299, 19951.60697),
                  new Coordinate(30938.47499, 19745.56575),
                  new Coordinate(30934.31192, 19747.0541),
                },
                new Coordinate[] {
                  new Coordinate(31016.92922, 20007.45593),
                  new Coordinate(31096.89299, 19958.84411),
                  new Coordinate(31210.89219, 20107.34783),
                  new Coordinate(31118.96157, 20131.10264),
                  new Coordinate(31016.92922, 20007.45593),
                }),
          };

      argumentsList.add(
          argumentSet("Octothorpe with several tiny bowties", path, List.of(polygons)));
    }
    // endregion

    // region Geometry with a collapsible tiny polygon.
    {
      final var path = new Path2D.Double();
      // This first polygon is completely normal.
      path.moveTo(0, 0);
      path.lineTo(400, 0);
      path.lineTo(400, 400);
      path.lineTo(0, 400);
      path.closePath();
      // But this one is so tiny it must be eliminated.
      path.moveTo(-332.37481837239756, 1635.6524985778758);
      path.lineTo(-332.3748183723977, 1635.652498577876);
      path.lineTo(-332.37481837239727, 1635.652498577876);
      path.lineTo(-332.3748183723976, 1635.6524985778758);
      path.closePath();
      final var area = new Area(path);
      final var polygons =
          new Polygon[] {
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(0, 400),
                  new Coordinate(400, 400),
                  new Coordinate(400, 0),
                  new Coordinate(0, 0),
                  new Coordinate(0, 400),
                }),
          };

      argumentsList.add(
          argumentSet("Geometry with a collapsible tiny polygon.", area, List.of(polygons)));
    }
    // endregion

    // region Ocean contained in island's bounding box but not actually a child of that island.
    {
      final var path = new Path2D.Double();
      // Main big island.
      path.moveTo(-200, -100);
      path.lineTo(200, -100);
      path.lineTo(200, 200);
      path.lineTo(-200, 200);
      path.lineTo(-200, 1000);
      path.lineTo(-500, 1000);
      path.lineTo(-500, -1000);
      path.lineTo(-200, -1000);
      path.closePath();
      // Cut out an ocean
      path.moveTo(0, 0);
      path.lineTo(0, 100);
      path.lineTo(100, 100);
      path.lineTo(100, 0);
      path.closePath();
      // Smaller island whose bounding box encompasses the above ocean.
      path.moveTo(-100, -200);
      path.lineTo(-100, -300);
      path.lineTo(400, -300);
      path.lineTo(400, 400);
      path.lineTo(-100, 400);
      path.lineTo(-100, 300);
      path.lineTo(300, 300);
      path.lineTo(300, -200);
      path.closePath();

      final var area = new Area(path);
      final var polygons =
          new Polygon[] {

            // Second polygon is the smaller island.
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(-100, -200),
                  new Coordinate(300, -200),
                  new Coordinate(300, 300),
                  new Coordinate(-100, 300),
                  new Coordinate(-100, 400),
                  new Coordinate(400, 400),
                  new Coordinate(400, -300),
                  new Coordinate(-100, -300),
                  new Coordinate(-100, -200),
                }),

            // First polygon is the big island with the ocean.
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(-200, 1000),
                  new Coordinate(-200, 200),
                  new Coordinate(200, 200),
                  new Coordinate(200, -100),
                  new Coordinate(-200, -100),
                  new Coordinate(-200, -1000),
                  new Coordinate(-500, -1000),
                  new Coordinate(-500, 1000),
                  new Coordinate(-200, 1000),
                },
                new Coordinate[] {
                  new Coordinate(0, 100),
                  new Coordinate(0, 0),
                  new Coordinate(100, 0),
                  new Coordinate(100, 100),
                  new Coordinate(0, 100),
                }),
          };

      argumentsList.add(
          argumentSet(
              "Ocean contained in island's bounding box but not actually a child of that island.",
              area,
              List.of(polygons)));
    }
    // endregion

    // region Self-intersection must be handled reasonably
    {
      final var path = new Path2D.Double();
      // A simple bow-tie.
      path.moveTo(0, 0);
      path.lineTo(100, 0);
      path.lineTo(0, 100);
      path.lineTo(100, 100);
      path.closePath();

      final var area = new Area(path);
      final var polygons =
          new Polygon[] {
            // Reduces to two triangular polygons.
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(50, 50),
                  new Coordinate(100, 0),
                  new Coordinate(0, 0),
                  new Coordinate(50, 50),
                }),
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(0, 100),
                  new Coordinate(100, 100),
                  new Coordinate(50, 50),
                  new Coordinate(0, 100),
                }),
          };

      argumentsList.add(
          argumentSet("Self-intersection must be handled reasonably", area, List.of(polygons)));
    }
    // endregion

    // region Tiny self-intersection must not affect topological structure
    {
      final var path = new Path2D.Double();
      // A box with a self-intersection along one edge.
      path.moveTo(0, 0);
      path.lineTo(100, 0);

      // Introduce a tiny error that causes a self-intersection in the edge of the box.
      final var error = 1e-6;
      path.lineTo(100 + error, 50 + error);
      path.lineTo(100 + error, 50 - error);

      path.lineTo(100, 100);
      path.lineTo(0, 100);
      path.closePath();

      final var area = new Area(path);
      final var polygons =
          new Polygon[] {
            // Reduces to a single square.
            createPrecisePolygon(
                new Coordinate[] {
                  new Coordinate(0, 0),
                  new Coordinate(100, 0),
                  new Coordinate(100, 100),
                  new Coordinate(0, 100),
                  new Coordinate(0, 0),
                }),
          };

      argumentsList.add(
          argumentSet(
              "Tiny self-intersection must not affect topological structure",
              area,
              List.of(polygons)));
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

      argumentsList.add(argumentSet("Cut and paste back connected areas", area, List.of(polygons)));
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

      argumentsList.add(argumentSet("Tiny crack in area", area, List.of(polygon)));
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

      argumentsList.add(argumentSet("Polygon vertices touching edges", area, List.of(polygons)));
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

      argumentsList.add(argumentSet("Butt joint accuracy", area, List.of(polygon)));
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
