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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PathTest {
  @Test
  @DisplayName("Test that a new path is empty")
  public void testNewPath() {
    var path = new Path<>();

    assertTrue(path.getCellPath().isEmpty());
    assertTrue(path.getWayPointList().isEmpty());
  }

  @Test
  @DisplayName(
      "Test that appending a waypoint to an empty path results in a path with one cell and one waypoint")
  public void testSingletonPath() {
    var path = new Path<ZonePoint>();
    var point = new ZonePoint(1, 3);

    path.appendWaypoint(point);

    assertIterableEquals(List.of(point), path.getCellPath());
    assertIterableEquals(List.of(point), path.getWayPointList());
  }

  @Test
  @DisplayName(
      "Test that appending a partial path to an empty path sets the first and last point as waypoints")
  public void testAddingPartialPathToEmptyPath() {
    var path = new Path<ZonePoint>();
    var partialPath =
        List.of(new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0));

    path.appendPartialPath(partialPath);

    assertIterableEquals(partialPath, path.getCellPath());
    var expectedWaypoints = List.of(new ZonePoint(1, 3), new ZonePoint(4, 0));
    assertIterableEquals(expectedWaypoints, path.getWayPointList());
  }

  @Test
  @DisplayName(
      "Test that appending an singleton partial path to an empty path adds the single point as the only cell and waypoint in the result")
  public void testAddingSingletonPartialPathToEmptyPath() {
    var path = new Path<ZonePoint>();
    var partialPath = List.of(new ZonePoint(1, 3));

    path.appendPartialPath(partialPath);

    assertIterableEquals(partialPath, path.getCellPath());
    assertIterableEquals(partialPath, path.getWayPointList());
  }

  @Test
  @DisplayName(
      "Test that appending a partial path to a singleton path extends the path and adds only the last point as a waypoint")
  public void testAddingPartialPathToSingletonPath() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));

    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));

    var expectedCellPoints =
        List.of(
            new ZonePoint(0, 4),
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(4, 0));
    assertIterableEquals(expectedCellPoints, path.getCellPath());
    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0));
    assertIterableEquals(expectedWaypoints, path.getWayPointList());
  }

  @Test
  @DisplayName(
      "Test that appending a partial path to a non-empty path extends the path and adds only the last point as a waypoint")
  public void testAddingPartialPathToPath() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));
    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));

    path.appendPartialPath(
        List.of(new ZonePoint(5, -1), new ZonePoint(6, -2), new ZonePoint(7, -3)));

    var expectedCellPoints =
        List.of(
            new ZonePoint(0, 4),
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(4, 0),
            new ZonePoint(5, -1),
            new ZonePoint(6, -2),
            new ZonePoint(7, -3));
    assertIterableEquals(expectedCellPoints, path.getCellPath());
    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0), new ZonePoint(7, -3));
    assertIterableEquals(expectedWaypoints, path.getWayPointList());
  }

  @Test
  @DisplayName(
      "Test that appending an empty partial path to a non-empty path does not modify the path")
  public void testAddingEmptyPartialPathToPath() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));
    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));

    path.appendPartialPath(List.of());

    var expectedCellPoints =
        List.of(
            new ZonePoint(0, 4),
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(4, 0));
    assertIterableEquals(expectedCellPoints, path.getCellPath());
    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0));
    assertIterableEquals(expectedWaypoints, path.getWayPointList());
  }

  @Test
  @DisplayName(
      "Test that appending an singleton partial path to a non-empty path adds the single point as a cell and as a waypoint")
  public void testAddingSingletonPartialPathToPath() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));
    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));

    path.appendPartialPath(List.of(new ZonePoint(5, -1)));

    var expectedCellPoints =
        List.of(
            new ZonePoint(0, 4),
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(4, 0),
            new ZonePoint(5, -1));
    assertIterableEquals(expectedCellPoints, path.getCellPath());
    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0), new ZonePoint(5, -1));
    assertIterableEquals(expectedWaypoints, path.getWayPointList());
  }

  @Test
  @DisplayName("Test that copying a path returns an equivalent path with unique point objects")
  public void testCopyPath() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));
    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));

    var copy = path.copy();

    var expectedCellPoints =
        List.of(
            new ZonePoint(0, 4),
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(4, 0));
    assertIterableEquals(expectedCellPoints, copy.getCellPath());
    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0));
    assertIterableEquals(expectedWaypoints, copy.getWayPointList());

    for (int i = 0; i < path.getCellPath().size(); ++i) {
      var originalPoint = path.getCellPath().get(i);
      var copyPoint = copy.getCellPath().get(i);
      assertNotSame(originalPoint, copyPoint);
    }
    for (int i = 0; i < path.getWayPointList().size(); ++i) {
      var originalPoint = path.getWayPointList().get(i);
      var copyPoint = copy.getWayPointList().get(i);
      assertNotSame(originalPoint, copyPoint);
    }
  }

  @Test
  @DisplayName("Test that isWaypoint() agrees with getWaypointList()")
  public void testIsWaypoint() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));
    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));
    path.appendPartialPath(
        List.of(new ZonePoint(5, -1), new ZonePoint(6, -2), new ZonePoint(7, -3)));

    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0), new ZonePoint(7, -3));
    for (var waypoint : expectedWaypoints) {
      assertTrue(
          path.isWaypoint(waypoint),
          () -> String.format("Point %s should be a waypoint", waypoint));
    }
    var expectedNotWaypoints =
        List.of(
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(5, -1),
            new ZonePoint(6, -2));
    for (var notWaypoint : expectedNotWaypoints) {
      assertFalse(
          path.isWaypoint(notWaypoint),
          () -> String.format("Point %s should not be a waypoint", notWaypoint));
    }
  }

  @Test
  @DisplayName("Test that an empty path can be converted to a DTO and back")
  public void testEmptyDto() {
    var path = new Path<ZonePoint>();

    var dto = path.toDto();
    var newPath = Path.fromDto(dto);

    assertTrue(newPath.getCellPath().isEmpty());
    assertTrue(newPath.getWayPointList().isEmpty());
  }

  @Test
  @DisplayName("Test that a path of ZonePoint can be converted to and from a DTO")
  public void testDtoZonePoint() {
    var path = new Path<ZonePoint>();
    path.appendWaypoint(new ZonePoint(0, 4));
    path.appendPartialPath(
        List.of(
            new ZonePoint(1, 3), new ZonePoint(2, 2), new ZonePoint(3, 1), new ZonePoint(4, 0)));
    path.appendPartialPath(
        List.of(new ZonePoint(5, -1), new ZonePoint(6, -2), new ZonePoint(7, -3)));

    var dto = path.toDto();
    var newPath = Path.fromDto(dto);

    var expectedCellPoints =
        List.of(
            new ZonePoint(0, 4),
            new ZonePoint(1, 3),
            new ZonePoint(2, 2),
            new ZonePoint(3, 1),
            new ZonePoint(4, 0),
            new ZonePoint(5, -1),
            new ZonePoint(6, -2),
            new ZonePoint(7, -3));
    assertIterableEquals(expectedCellPoints, newPath.getCellPath());

    var expectedWaypoints = List.of(new ZonePoint(0, 4), new ZonePoint(4, 0), new ZonePoint(7, -3));
    assertIterableEquals(expectedWaypoints, newPath.getWayPointList());

    // Make sure we didn't confuse the point type.
    for (var cell : newPath.getCellPath()) {
      assertInstanceOf(ZonePoint.class, cell);
    }
    for (var waypoint : newPath.getWayPointList()) {
      assertInstanceOf(ZonePoint.class, waypoint);
    }
  }

  @Test
  @DisplayName("Test that a path of CellPoint can be converted to and from a DTO")
  public void testDtoCellPoint() {
    var path = new Path<CellPoint>();
    path.appendWaypoint(new CellPoint(0, 4));
    path.appendPartialPath(
        List.of(
            new CellPoint(1, 3), new CellPoint(2, 2), new CellPoint(3, 1), new CellPoint(4, 0)));
    path.appendPartialPath(
        List.of(new CellPoint(5, -1), new CellPoint(6, -2), new CellPoint(7, -3)));

    var dto = path.toDto();
    var newPath = Path.fromDto(dto);

    var expectedCellPoints =
        List.of(
            new CellPoint(0, 4),
            new CellPoint(1, 3),
            new CellPoint(2, 2),
            new CellPoint(3, 1),
            new CellPoint(4, 0),
            new CellPoint(5, -1),
            new CellPoint(6, -2),
            new CellPoint(7, -3));
    assertIterableEquals(expectedCellPoints, newPath.getCellPath());

    var expectedWaypoints = List.of(new CellPoint(0, 4), new CellPoint(4, 0), new CellPoint(7, -3));
    assertIterableEquals(expectedWaypoints, newPath.getWayPointList());

    // Make sure we didn't confuse the point type.
    for (var cell : newPath.getCellPath()) {
      assertInstanceOf(CellPoint.class, cell);
    }
    for (var waypoint : newPath.getWayPointList()) {
      assertInstanceOf(CellPoint.class, waypoint);
    }
  }

  // TODO @Nested for derived tests.

  @Nested
  class DerivedPathTests {
    @Test
    @DisplayName(
        "Test that a non-snap-to-grid follower path is properly derived from a non-snap-to-grid leader path")
    public void testDeriveNonStgFollowingNonStg() {
      var path = new Path<ZonePoint>();
      path.appendWaypoint(new ZonePoint(52, 427));
      path.appendPartialPath(
          List.of(
              new ZonePoint(116, 364),
              new ZonePoint(231, 290),
              new ZonePoint(342, 172),
              new ZonePoint(429, 65)));
      path.appendPartialPath(
          List.of(new ZonePoint(502, -91), new ZonePoint(628, -171), new ZonePoint(756, -272)));
      var grid = mock(Grid.class);
      var leaderToken = mock(Token.class);
      when(leaderToken.isSnapToGrid()).thenReturn(false);
      // Position agrees with start of path.
      when(leaderToken.getX()).thenReturn(52);
      when(leaderToken.getY()).thenReturn(427);
      var followerToken = mock(Token.class);
      when(followerToken.isSnapToGrid()).thenReturn(false);
      // Offset a bit from the leader.
      when(followerToken.getX()).thenReturn(1012);
      when(followerToken.getY()).thenReturn(1184);

      var derived = path.derive(grid, leaderToken, followerToken);

      // All points are used, even though in practice non-STG tokens only have waypoints.
      var expectedCellPoints =
          List.of(
              new ZonePoint(1012, 1184),
              new ZonePoint(1076, 1121),
              new ZonePoint(1191, 1047),
              new ZonePoint(1302, 929),
              new ZonePoint(1389, 822),
              new ZonePoint(1462, 666),
              new ZonePoint(1588, 586),
              new ZonePoint(1716, 485));
      assertIterableEquals(expectedCellPoints, derived.getCellPath());
      var expectedWaypoints =
          List.of(new ZonePoint(1012, 1184), new ZonePoint(1389, 822), new ZonePoint(1716, 485));
      assertIterableEquals(expectedWaypoints, derived.getWayPointList());

      for (var cell : derived.getCellPath()) {
        assertInstanceOf(ZonePoint.class, cell);
      }
      for (var waypoint : derived.getWayPointList()) {
        assertInstanceOf(ZonePoint.class, waypoint);
      }
    }

    @Test
    @DisplayName(
        "Test that a snap-to-grid follower path is properly derived from a snap-to-grid leader path")
    public void testDeriveStgFollowingStg() {
      var path = new Path<CellPoint>();
      path.appendWaypoint(new CellPoint(0, 4));
      path.appendPartialPath(
          List.of(
              new CellPoint(1, 3), new CellPoint(2, 2), new CellPoint(3, 1), new CellPoint(4, 0)));
      path.appendPartialPath(
          List.of(new CellPoint(5, -1), new CellPoint(6, -2), new CellPoint(7, -3)));
      var grid = mock(Grid.class);
      when(grid.convert(new ZonePoint(52, 427))).thenReturn(new CellPoint(0, 4));
      when(grid.convert(new ZonePoint(1012, 1184))).thenReturn(new CellPoint(10, 11));

      var leaderToken = mock(Token.class);
      when(leaderToken.isSnapToGrid()).thenReturn(true);
      // Position agrees with start of path.
      when(leaderToken.getX()).thenReturn(52);
      when(leaderToken.getY()).thenReturn(427);
      var followerToken = mock(Token.class);
      when(followerToken.isSnapToGrid()).thenReturn(true);
      // Offset a bit from the leader.
      when(followerToken.getX()).thenReturn(1012);
      when(followerToken.getY()).thenReturn(1184);

      var derived = path.derive(grid, leaderToken, followerToken);

      // All points are used, even though in practice non-STG tokens only have waypoints.
      var expectedCellPoints =
          List.of(
              new CellPoint(10, 11),
              new CellPoint(11, 10),
              new CellPoint(12, 9),
              new CellPoint(13, 8),
              new CellPoint(14, 7),
              new CellPoint(15, 6),
              new CellPoint(16, 5),
              new CellPoint(17, 4));
      assertIterableEquals(expectedCellPoints, derived.getCellPath());
      var expectedWaypoints =
          List.of(new CellPoint(10, 11), new CellPoint(14, 7), new CellPoint(17, 4));
      assertIterableEquals(expectedWaypoints, derived.getWayPointList());

      for (var cell : derived.getCellPath()) {
        assertInstanceOf(CellPoint.class, cell);
      }
      for (var waypoint : derived.getWayPointList()) {
        assertInstanceOf(CellPoint.class, waypoint);
      }
    }

    @Test
    @DisplayName(
        "Test that a non-snap-to-grid follower path is properly derived from a snap-to-grid leader path")
    public void testDeriveNonStgFollowingStg() {
      var path = new Path<CellPoint>();
      path.appendWaypoint(new CellPoint(0, 4));
      path.appendPartialPath(
          List.of(
              new CellPoint(1, 3), new CellPoint(2, 2), new CellPoint(3, 1), new CellPoint(4, 0)));
      path.appendPartialPath(
          List.of(new CellPoint(5, -1), new CellPoint(6, -2), new CellPoint(7, -3)));
      var grid = mock(Grid.class);
      when(grid.convert(new CellPoint(0, 4))).thenReturn(new ZonePoint(0, 400));
      when(grid.convert(new CellPoint(1, 3))).thenReturn(new ZonePoint(100, 300));
      when(grid.convert(new CellPoint(2, 2))).thenReturn(new ZonePoint(200, 200));
      when(grid.convert(new CellPoint(3, 1))).thenReturn(new ZonePoint(300, 100));
      when(grid.convert(new CellPoint(4, 0))).thenReturn(new ZonePoint(400, 0));
      when(grid.convert(new CellPoint(5, -1))).thenReturn(new ZonePoint(500, -100));
      when(grid.convert(new CellPoint(6, -2))).thenReturn(new ZonePoint(600, -200));
      when(grid.convert(new CellPoint(7, -3))).thenReturn(new ZonePoint(700, -300));
      var leaderToken = mock(Token.class);
      when(leaderToken.isSnapToGrid()).thenReturn(true);
      // Position agrees with start of path.
      when(leaderToken.getX()).thenReturn(52);
      when(leaderToken.getY()).thenReturn(427);
      var followerToken = mock(Token.class);
      when(followerToken.isSnapToGrid()).thenReturn(false);
      // Offset a bit from the leader.
      when(followerToken.getX()).thenReturn(1012);
      when(followerToken.getY()).thenReturn(1184);

      var derived = path.derive(grid, leaderToken, followerToken);

      // Only the waypoints are used.
      var expectedCellPoints =
          List.of(new ZonePoint(960, 1157), new ZonePoint(1360, 757), new ZonePoint(1660, 457));
      assertIterableEquals(expectedCellPoints, derived.getCellPath());
      var expectedWaypoints = expectedCellPoints;
      assertIterableEquals(expectedWaypoints, derived.getWayPointList());

      for (var cell : derived.getCellPath()) {
        assertInstanceOf(ZonePoint.class, cell);
      }
      for (var waypoint : derived.getWayPointList()) {
        assertInstanceOf(ZonePoint.class, waypoint);
      }
    }

    @Test
    @DisplayName(
        "Test that a snap-to-grid follower path is properly derived from a non-snap-to-grid leader path")
    public void testDeriveStgFollowingNonStg() {
      var path = new Path<ZonePoint>();
      path.appendWaypoint(new ZonePoint(52, 427));
      path.appendPartialPath(
          List.of(
              new ZonePoint(116, 364),
              new ZonePoint(231, 290),
              new ZonePoint(342, 172),
              new ZonePoint(429, 65)));
      path.appendPartialPath(
          List.of(new ZonePoint(502, -91), new ZonePoint(628, -171), new ZonePoint(756, -272)));
      var grid = mock(Grid.class);
      when(grid.convert(new ZonePoint(1012, 1184))).thenReturn(new CellPoint(10, 11));
      when(grid.convert(new ZonePoint(1389, 822))).thenReturn(new CellPoint(13, 8));
      when(grid.convert(new ZonePoint(1716, 485))).thenReturn(new CellPoint(17, 4));
      var leaderToken = mock(Token.class);
      when(leaderToken.isSnapToGrid()).thenReturn(false);
      // Position agrees with start of path.
      when(leaderToken.getX()).thenReturn(52);
      when(leaderToken.getY()).thenReturn(427);
      var followerToken = mock(Token.class);
      when(followerToken.isSnapToGrid()).thenReturn(true);
      // Offset a bit from the leader.
      when(followerToken.getX()).thenReturn(1012);
      when(followerToken.getY()).thenReturn(1184);

      var derived = path.derive(grid, leaderToken, followerToken);

      // Only waypoints are used directly. The rest are interpolated.
      var expectedCellPoints =
          List.of(
              new CellPoint(10, 11),
              new CellPoint(11, 10),
              new CellPoint(12, 9),
              new CellPoint(13, 8),
              new CellPoint(14, 7),
              new CellPoint(15, 6),
              new CellPoint(16, 5),
              new CellPoint(17, 4));
      assertIterableEquals(expectedCellPoints, derived.getCellPath());
      var expectedWaypoints =
          List.of(new CellPoint(10, 11), new CellPoint(13, 8), new CellPoint(17, 4));
      assertIterableEquals(expectedWaypoints, derived.getWayPointList());

      for (var cell : derived.getCellPath()) {
        assertInstanceOf(CellPoint.class, cell);
      }
      for (var waypoint : derived.getWayPointList()) {
        assertInstanceOf(CellPoint.class, waypoint);
      }
    }

    @Test
    @DisplayName(
        "Test that a snap-to-grid follower path is properly derived from a non-snap-to-grid leader path using the naive walk")
    public void testDeriveStgFollowingNonStgWithDifferentSlope() {
      var path = new Path<ZonePoint>();
      path.appendWaypoint(new ZonePoint(52, 427));
      path.appendWaypoint(new ZonePoint(889, 838));
      path.appendWaypoint(new ZonePoint(445, -238));
      var grid = mock(Grid.class);
      when(grid.convert(new ZonePoint(1012, 1184))).thenReturn(new CellPoint(10, 11));
      when(grid.convert(new ZonePoint(1849, 1595))).thenReturn(new CellPoint(18, 15));
      when(grid.convert(new ZonePoint(1405, 519))).thenReturn(new CellPoint(14, 5));
      var leaderToken = mock(Token.class);
      when(leaderToken.isSnapToGrid()).thenReturn(false);
      // Position agrees with start of path.
      when(leaderToken.getX()).thenReturn(52);
      when(leaderToken.getY()).thenReturn(427);
      var followerToken = mock(Token.class);
      when(followerToken.isSnapToGrid()).thenReturn(true);
      // Offset a bit from the leader.
      when(followerToken.getX()).thenReturn(1012);
      when(followerToken.getY()).thenReturn(1184);

      var derived = path.derive(grid, leaderToken, followerToken);

      // Only waypoints are used directly. The rest are interpolated.
      var expectedCellPoints =
          List.of(
              new CellPoint(10, 11),
              new CellPoint(11, 12),
              new CellPoint(12, 13),
              new CellPoint(13, 14),
              new CellPoint(14, 15),
              new CellPoint(15, 15),
              new CellPoint(16, 15),
              new CellPoint(17, 15),
              new CellPoint(18, 15),
              new CellPoint(17, 14),
              new CellPoint(16, 13),
              new CellPoint(15, 12),
              new CellPoint(14, 11),
              new CellPoint(14, 10),
              new CellPoint(14, 9),
              new CellPoint(14, 8),
              new CellPoint(14, 7),
              new CellPoint(14, 6),
              new CellPoint(14, 5));
      assertIterableEquals(expectedCellPoints, derived.getCellPath());
      var expectedWaypoints =
          List.of(new CellPoint(10, 11), new CellPoint(18, 15), new CellPoint(14, 5));
      assertIterableEquals(expectedWaypoints, derived.getWayPointList());

      for (var cell : derived.getCellPath()) {
        assertInstanceOf(CellPoint.class, cell);
      }
      for (var waypoint : derived.getWayPointList()) {
        assertInstanceOf(CellPoint.class, waypoint);
      }
    }

    @Test
    @DisplayName(
        "Test that a snap-to-grid leader's derived path does not include extra waypoints when the path crosses the start or endpoint")
    public void testStgPathCrossingItself() {
      // Previous implementations of derive() would add waypoints anywhere along a path if the
      // position was a waypoint. So a path that crosses itself could be given many more waypoints
      // than were actually set. We don't do that anymore, and this test ensures it.

      var path = new Path<CellPoint>();
      path.appendWaypoint(new CellPoint(0, 0));
      path.appendPartialPath(
          List.of(
              new CellPoint(1, 0), new CellPoint(2, 0), new CellPoint(3, 0), new CellPoint(4, 0)));
      path.appendPartialPath(
          List.of(
              new CellPoint(3, 1), new CellPoint(2, 2), new CellPoint(1, 3), new CellPoint(0, 4)));
      path.appendPartialPath(
          List.of(
              new CellPoint(0, 3),
              new CellPoint(0, 2),
              new CellPoint(0, 1),
              // Note: this point is also the first point in the path.
              new CellPoint(0, 0),
              new CellPoint(0, -1),
              new CellPoint(0, -2)));
      path.appendPartialPath(
          List.of(
              new CellPoint(1, -1),
              // Note: this last point crosses through the first partial path.
              new CellPoint(2, 0)));
      var grid = mock(Grid.class);
      when(grid.convert(new ZonePoint(50, 50))).thenReturn(new CellPoint(0, 0));
      var leaderToken = mock(Token.class);
      when(leaderToken.isSnapToGrid()).thenReturn(true);
      // Position agrees with start of path.
      when(leaderToken.getX()).thenReturn(50);
      when(leaderToken.getY()).thenReturn(50);

      var derived = path.derive(grid, leaderToken, leaderToken);

      // All points are used, even though in practice non-STG tokens only have waypoints.
      var expectedCellPoints =
          List.of(
              new CellPoint(0, 0),
              new CellPoint(1, 0),
              new CellPoint(2, 0),
              new CellPoint(3, 0),
              new CellPoint(4, 0),
              new CellPoint(3, 1),
              new CellPoint(2, 2),
              new CellPoint(1, 3),
              new CellPoint(0, 4),
              new CellPoint(0, 3),
              new CellPoint(0, 2),
              new CellPoint(0, 1),
              new CellPoint(0, 0),
              new CellPoint(0, -1),
              new CellPoint(0, -2),
              new CellPoint(1, -1),
              new CellPoint(2, 0));
      assertIterableEquals(expectedCellPoints, derived.getCellPath());
      var expectedWaypoints =
          List.of(
              // Important that these are exactly the waypoints set, in the orer they were set.
              new CellPoint(0, 0),
              // Note: No (2, 0) waypoint here.
              new CellPoint(4, 0),
              new CellPoint(0, 4),
              // Note: New (0, 0) waypoint here.
              new CellPoint(0, -2),
              new CellPoint(2, 0));
      assertIterableEquals(expectedWaypoints, derived.getWayPointList());

      for (var cell : derived.getCellPath()) {
        assertInstanceOf(CellPoint.class, cell);
      }
      for (var waypoint : derived.getWayPointList()) {
        assertInstanceOf(CellPoint.class, waypoint);
      }
    }
  }
}
