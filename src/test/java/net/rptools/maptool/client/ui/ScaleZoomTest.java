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
package net.rptools.maptool.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * This class tests the zoom functionality of the Scale class, in preparation for modifications to
 * implement fine mouse-wheel zooming.
 *
 * <p>The class is mostly regression tests. Magic numbers in this class were determined before fine
 * mouse-wheel zooming was implemented. That is: they were not chosen so they work with the fine
 * wheel changes, they were picked to check that the fine-wheel changes don't disturb existing
 * behaviour.
 *
 * <p>update: The addition of fine zooming changed some of the magic numbers after implementation of
 * fine zoom, as a scale of 10 is now rounded to a closer zoom level. This is expected.
 *
 * <p>update: as above, but now the zoom can be ultrafine
 *
 * <p>The publicly visible parts of scale zooming are:
 *
 * <ul>
 *   <li>The {@link Scale#Scale(double, int, int) constructor} where you give it a scale and it
 *       works out the zoom
 *   <li>The {@link Scale#Scale(int, int, int) constructor} where you give it a zoom and it works
 *       out the scale
 *   <li>The {@link Scale#withZoomLevel(int, int, int) withZoomLevel} builder (which IMO ought to be
 *       static)
 *   <li>{@link Scale#zoomedIn(int, int) zoomedIn} and {@link Scale#zoomedOut(int, int) zoomedOut}
 * </ul>
 */
public class ScaleZoomTest {
  int ULTRAFINE_ZOOM = 24 * 24;

  @Test
  void testConstructorByScale() {
    // this constructor does not snap the scale to the zoom level, which IMO may be a bug
    Scale s = new Scale(10.0, 10, 10);

    assertEquals(10.0, s.scale);

    // THe introduction of fine zoom alters this magic number. This is expected
    assertEquals(18339, s.fineZoomLevel);
  }

  @Test
  void testConstructorByScale1to1() {
    // this constructor does not snap the scale to the zoom level, which IMO may be a bug
    Scale s = new Scale(1.0, 10, 10);

    assertEquals(1.0, s.scale);
    assertEquals(0, s.fineZoomLevel);
  }

  @Test
  void testConstructorByZoomLevel() {
    Scale s = new Scale(3, 10, 10);

    assertEquals(3 * ULTRAFINE_ZOOM, s.fineZoomLevel);
    assertApprox(1.075 * 1.075 * 1.075, s.scale);
  }

  @Test
  void testConstructorByZoomLevel1to1() {
    Scale s = new Scale(0, 10, 10);

    assertEquals(0, s.fineZoomLevel);
    assertApprox(1, s.scale);
  }

  @Test
  void testConstructorByZoomLevelClampingMax() {
    Scale s = new Scale(200, 10, 10);

    assertEquals(175 * ULTRAFINE_ZOOM, s.fineZoomLevel);
    assertApprox(313676.0, s.scale);
  }

  @Test
  void testConstructorByZoomLevelClampingMin() {
    Scale s = new Scale(-200, 10, 10);

    assertEquals(-175 * ULTRAFINE_ZOOM, s.fineZoomLevel);
    assertApprox(1.0 / 313676.0, s.scale);
  }

  @Test
  void testWithZoomLevel() {
    Scale s = new Scale(10, 10, 10);
    Scale s2 = s.withZoomLevel(3, 10, 10);

    assertEquals(3 * ULTRAFINE_ZOOM, s2.fineZoomLevel);
    assertApprox(1.075 * 1.075 * 1.075, s2.scale);
  }

  @Test
  void testWithZoomLevelClampingMax() {
    Scale s = new Scale(10, 10, 10);
    Scale s2 = s.withZoomLevel(200, 10, 10);

    assertEquals(175 * ULTRAFINE_ZOOM, s2.fineZoomLevel);
    assertApprox(313676.0, s2.scale);
  }

  @Test
  void testWithZoomLevelClampingMin() {
    Scale s = new Scale(10, 10, 10);
    Scale s2 = s.withZoomLevel(-200, 10, 10);

    assertEquals(-175 * ULTRAFINE_ZOOM, s2.fineZoomLevel);
    assertApprox(1.0 / 313676.0, s2.scale);
  }

  @Test
  void testZoomedIn() {
    Scale s = new Scale(3, 10, 10);
    Scale s2 = s.zoomedIn(10, 10);
    assertEquals(4 * ULTRAFINE_ZOOM, s2.fineZoomLevel);
    assertApprox(1.075 * 1.075 * 1.075 * 1.075, s2.scale);
  }

  @Test
  void testZoomedOut() {
    Scale s = new Scale(3, 10, 10);
    Scale s2 = s.zoomedOut(10, 10);
    assertEquals(2 * ULTRAFINE_ZOOM, s2.fineZoomLevel);
    assertApprox(1.075 * 1.075, s2.scale);
  }

  @Test
  void testZoomedInWithScaleNotSnapped() {
    Scale s = new Scale(10.0, 10, 10);
    Scale s2 = s.zoomedIn(10, 10).zoomedOut(10, 10);

    // THe introduction of fine zoom alters this magic number. This is expected
    assertEquals(18339, s.fineZoomLevel);
    // THe introduction of fine zoom alters this magic number. This is expected
    assertEquals(18339, s2.fineZoomLevel);
    assertEquals(10.0, s.scale);
    // zooming in and out causes scale to be snapped to the major zoom increment
    // so will no longer be exactly 10
    assertApprox(10, s2.scale);
  }

  void assertApprox(double expected, double actual) {
    double error = actual / expected;
    if (error < .99 || error > 1.01) {
      fail("expected " + expected + " but got " + actual);
    }
  }
}
