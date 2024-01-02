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
package net.rptools.maptool.model.drawing;

import net.rptools.maptool.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TriangleTemplatePerformanceTest {
  @Test
  @DisplayName("Test performance of getConePath")
  void testRDrawingArea() throws Exception {
    ZonePoint[] startPoints =
        new ZonePoint[] {
          new ZonePoint(100, -50),
          new ZonePoint(50, 0),
          new ZonePoint(100, 0),
          new ZonePoint(150, 0),
          new ZonePoint(50, 50),
          new ZonePoint(100, 50),
          new ZonePoint(150, 50),
          new ZonePoint(100, 100),
        };
    int thetaSplits = 200;
    double [] testThetas = new double[thetaSplits];
    for (int i = 0; i < thetaSplits; i++) {
      testThetas[i] = Math.PI / (double) i;
    }

    int numberOfRadiuses = 200;
    int [] testRadiuses = new int[numberOfRadiuses];
    for (int i = 1; i <= numberOfRadiuses; i++) {
      testRadiuses[i-1] = i * 5;
    }

    long trianglesCalculated = 0;
    long startTime = System.currentTimeMillis();
    for (ZonePoint startPoint : startPoints) {
      for (double theta : testThetas) {
        for (int radius : testRadiuses) {
          Path2D.Double conePath = TriangleTemplate.getConePath(startPoint, radius, 100, theta);
          trianglesCalculated++;
          assertNotNull(conePath);
        }
      }
    }
    long endTime = System.currentTimeMillis();
    System.out.print(trianglesCalculated);
    System.out.print(" Triangles Calculated in Duration Using Transformed Stencil: ");
    System.out.println(endTime - startTime);

    long trianglesCalculatedInneficient = 0;
    long startTimeInefficient = System.currentTimeMillis();
    for (ZonePoint startPoint : startPoints) {
      for (double theta : testThetas) {
        for (int radius : testRadiuses) {
          Path2D.Double conePath = TriangleTemplate.getConePathInneficientMethod(startPoint, radius, 100, theta);
          trianglesCalculatedInneficient++;
          assertNotNull(conePath);
        }
      }
    }
    long endTimeInefficient = System.currentTimeMillis();
    System.out.print(trianglesCalculatedInneficient);
    System.out.print(" Triangles Calculated in Duration Using Re-Render from Scratch: ");
    System.out.println(endTimeInefficient - startTimeInefficient);
  }
}
