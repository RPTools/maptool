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

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.Area;
import net.rptools.maptool.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RadiusTemplateAreaTest {
  private RadiusTemplate testRadiusTemplate(ZonePoint vertex, int radius) {
    Campaign testCampaign = new Campaign();
    Zone testZone = new Zone();
    Grid testGrid = new SquareGrid();
    testGrid.setSize(50);
    testZone.setGrid(testGrid);
    testCampaign.putZone(testZone);

    RadiusTemplate radiusTemplate = new RadiusTemplate();
    radiusTemplate.setName("test");
    radiusTemplate.setVertex(vertex);
    radiusTemplate.setRadius(radius);

    radiusTemplate = Mockito.spy(radiusTemplate);
    Mockito.when(radiusTemplate.getCampaign()).thenReturn(testCampaign);
    return radiusTemplate;
  }

  @Test
  @DisplayName("Test getArea function on radius drawing template")
  void testRadiusDrawingArea() throws Exception {
    Campaign testCampaign = new Campaign();
    Zone testZone = new Zone();
    Grid testGrid = new SquareGrid();
    testGrid.setSize(50);
    testZone.setGrid(testGrid);
    testCampaign.putZone(testZone);

    RadiusTemplate radiusTemplate = testRadiusTemplate(new ZonePoint(50, 50), 1);
    Area area = radiusTemplate.getArea(testZone);
    assertFalse(area.isEmpty());
    // Radius is 1, so it should be a 2x2 square.
    // With a gridsize of 50, that's 100x100, with the ul origin at 0,0
    assertEquals(new Rectangle(0, 0, 100, 100), area.getBounds());

    // And now a slightly bigger radius
    radiusTemplate = testRadiusTemplate(new ZonePoint(50, 50), 2);
    area = radiusTemplate.getArea(testZone);
    // This one contains the same squares as above, + 2 more on each edge
    assert (area.contains(new Rectangle(0, 0, 100, 100)));
    // The full list of all 12 points a radius 2 template should contain
    Point[] expectedPoints =
        new Point[] {
          new Point(0, -50),
          new Point(50, -50),
          new Point(-50, 0),
          new Point(0, 0),
          new Point(50, 0),
          new Point(100, 0),
          new Point(-50, 50),
          new Point(0, 50),
          new Point(50, 50),
          new Point(100, 50),
          new Point(0, 100),
          new Point(50, 100),
        };
    for (Point p : expectedPoints) {
      assert (area.contains(new Rectangle(p.x, p.y, 50, 50)));
    }
    Point[] notInArea =
        new Point[] {
          new Point(-25, -50), new Point(1000, 1000),
        };
    for (Point p : notInArea) {
      assertFalse(area.contains(p));
    }
  }
}
