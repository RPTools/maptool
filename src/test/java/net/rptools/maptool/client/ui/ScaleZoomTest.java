package net.rptools.maptool.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class tests the zoom functionality of the Scale class, in preparation for modifications to implement fine
 * mouse-wheel zooming.
 * <p>The class is mostly regression tests. Magic numbers in this class were determined before fine mouse-wheel
 * zooming was implemented. That is: they were not chosen so they work with the fine wheel changes, they were picked to
 * check that the fine-wheel changes don't disturb existing behaviour.</p>
 * <p>
 * The publicly visible parts of scale zooming are:
 * </p>
 * <ul>
 *  <li>The {@link Scale#Scale(double, int, int)  constructor} where you give it a scale and it works out the zoom</li>
 *  <li>The {@link Scale#Scale(int, int, int)  constructor} where you give it a zoom and it works out the scale</li>
 *  <li>The  {@link Scale#withZoomLevel(int, int, int) withZoomLevel} builder (which IMO ought to be static) </li>
 *  <li>{@link Scale#zoomedIn(int, int) zoomedIn}  and {@link Scale#zoomedOut(int, int) zoomedOut}</li>
 * </ul>
 */


public class ScaleZoomTest {
  @Test
  void testConstructorByScale() {
    // this constructor does not snap the scale to the zoom level, which IMO may be a bug
    Scale s = new Scale(10.0, 10, 10);

    assertEquals(10.0, s.scale);
    assertEquals(32, s.zoomLevel);
  }

  @Test
  void testConstructorByZoomLevel() {
    Scale s = new Scale(3, 10, 10);

    assertEquals(3, s.zoomLevel);
    assertApprox(1.075 * 1.075 * 1.075, s.scale);
  }

  @Test
  void testConstructorByZoomLevelClampingMax() {
    Scale s = new Scale(200, 10, 10);

    assertEquals(175, s.zoomLevel);
    assertApprox(313676.0, s.scale);
  }

  @Test
  void testConstructorByZoomLevelClampingMin() {
    Scale s = new Scale(-200, 10, 10);

    assertEquals(-175, s.zoomLevel);
    assertApprox(1.0 / 313676.0, s.scale);
  }

  @Test
  void testWithZoomLevel() {
    Scale s = new Scale(10, 10, 10);
    Scale s2 = s.withZoomLevel(3, 10, 10);

    assertEquals(3, s2.zoomLevel);
    assertApprox(1.075 * 1.075 * 1.075, s2.scale);
  }

  @Test
  void testWithZoomLevelClampingMax() {
    Scale s = new Scale(10, 10, 10);
    Scale s2 = s.withZoomLevel(200, 10, 10);

    assertEquals(175, s2.zoomLevel);
    assertApprox(313676.0, s2.scale);
  }

  @Test
  void testWithZoomLevelClampingMin() {
    Scale s = new Scale(10, 10, 10);
    Scale s2 = s.withZoomLevel(-200, 10, 10);

    assertEquals(-175, s2.zoomLevel);
    assertApprox(1.0 / 313676.0, s2.scale);
  }

  @Test
  void testZoomedIn() {
    Scale s = new Scale(3, 10, 10);
    Scale s2 = s.zoomedIn(10, 10);
    assertEquals(4, s2.zoomLevel);
    assertApprox(1.075 * 1.075 * 1.075 * 1.075, s2.scale);
  }

  @Test
  void testZoomedOut() {
    Scale s = new Scale(3, 10, 10);
    Scale s2 = s.zoomedOut(10, 10);
    assertEquals(2, s2.zoomLevel);
    assertApprox(1.075 * 1.075, s2.scale);
  }

  @Test
  void testZoomedInWithScaleNotSnapped() {
    Scale s = new Scale(10.0, 10, 10);
    Scale s2 = s.zoomedIn(10, 10).zoomedOut(10, 10);

    assertEquals(32, s.zoomLevel);
    assertEquals(32, s2.zoomLevel);
    assertEquals(10.0, s.scale);
    // zooming in and out causes scale to be snapped to the major zoom increment
    // so will no longer be exactly 10
    assertApprox(10.117, s2.scale);
  }


  void assertApprox(double expected, double actual) {
    double error = actual / expected;
    if (error < .999 || error > 1.001) {
      fail("expected " + expected + " but got " + actual);
    }
  }


}
