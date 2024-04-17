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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.geom.Area;
import net.rptools.maptool.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ConeTemplateAreaTest {
  private ConeTemplate testConeTemplate(
      ZonePoint vertex, int radius, AbstractTemplate.Direction direction) {
    Campaign testCampaign = new Campaign();
    Zone testZone = new Zone();
    Grid testGrid = new SquareGrid();
    testGrid.setSize(50);
    testZone.setGrid(testGrid);
    testCampaign.putZone(testZone);

    ConeTemplate coneTemplate = new ConeTemplate();
    coneTemplate.setName("test");
    coneTemplate.setVertex(vertex);
    coneTemplate.setRadius(radius);
    coneTemplate.setDirection(direction);

    coneTemplate = Mockito.spy(coneTemplate);
    Mockito.when(coneTemplate.getCampaign()).thenReturn(testCampaign);
    return coneTemplate;
  }

  @Test
  @DisplayName("Test getArea function on cone drawing template")
  void testRDrawingArea() throws Exception {
    Campaign testCampaign = new Campaign();
    Zone testZone = new Zone();
    Grid testGrid = new SquareGrid();
    testGrid.setSize(50);
    testZone.setGrid(testGrid);
    testCampaign.putZone(testZone);

    ConeTemplate coneTemplate =
        testConeTemplate(new ZonePoint(50, 50), 3, AbstractTemplate.Direction.EAST);
    Area area = coneTemplate.getArea(testZone);
    // This should look like the following:
    //
    // □■□
    // ■■■
    // ■■■
    // □■□
    // Assert the area matches that
    ZonePoint[] expectedPoints =
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
    for (ZonePoint zonePoint : expectedPoints) {
      assert (area.contains(zonePoint.x, zonePoint.y));
    }
    ZonePoint[] expectedNotInArea =
        new ZonePoint[] {
          new ZonePoint(0, 0),
          new ZonePoint(50, -50),
          new ZonePoint(150, -50),
          new ZonePoint(50, -50),
          new ZonePoint(50, 150),
          new ZonePoint(200, 50),
        };
    for (ZonePoint zonePoint : expectedNotInArea) {
      assertFalse(area.contains(zonePoint.x, zonePoint.y));
    }
  }
}
