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
package net.rptools.maptool.client.walker.astar;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reverses the order of rings returned by a PathIterator so that it works better with JST's
 * ShapeReader::read().
 *
 * <p>Even though this helps, Geometry::buffer() should still be called on the result, to ensure
 * that holes are associated with the correct shells.
 */
public class ReverseShapePathIterator implements PathIterator {
  /**
   * Represents a segment as returned by PathIterator::currentSegment(). I.e., a segment type and a
   * list of coordinates.
   */
  private static class Segment {
    public int segmentType;
    public double[] coords;

    Segment(int segmentType, double[] coords) {
      this.segmentType = segmentType;
      this.coords = coords;
    }
  }

  private final int windingRule;
  /** All rings read from the forward path iterator, in the same order. */
  private final List<List<Segment>> rings;
  /** Used to walk backwards through the list of rings. */
  private int ringIndex;
  /** Used to walk forwards through the list of segments within a ring. */
  private int segmentIndex;

  /**
   * Build a reverse path iterator from a forward path iterator.
   *
   * <p>The forward path iterator is eagerly consumed and not used again after the constructor
   * exists.
   *
   * @param forwardPathIterator A PathIterator as returned by Area::getPathIterator(). Consumed
   *     during this constructor.
   */
  public ReverseShapePathIterator(PathIterator forwardPathIterator) {
    windingRule = forwardPathIterator.getWindingRule();
    rings = new ArrayList<>();

    // PathIterator::currentSegment() reqiures a 6-element array, in case of SEG_QUADTO.
    final double[] coords = new double[6];
    // Assume all rings are started with SEG_MOVETO and may be explicitly ended with SEG_CLOSE.
    List<Segment> currentRing = new ArrayList<>();
    for (; !forwardPathIterator.isDone(); forwardPathIterator.next()) {
      int segmentType = forwardPathIterator.currentSegment(coords);
      final Segment segment = new Segment(segmentType, Arrays.copyOf(coords, coords.length));

      switch (segmentType) {
        case PathIterator.SEG_MOVETO:
          // May be the first segment we see, in which case it starts the first ring. Otherwise it
          // implicitly ends the previous ring, to which this segment does not belong.
          if (!currentRing.isEmpty()) {
            rings.add(currentRing);
            currentRing = new ArrayList<>();
          }
          currentRing.add(segment);
          break;

        case PathIterator.SEG_CLOSE:
          // Explicit end to the ring. This segment is considered part of the ring.
          currentRing.add(segment);
          rings.add(currentRing);
          currentRing = new ArrayList<>();
          break;

        case PathIterator.SEG_LINETO:
          // Simple case of adding a segment.
          currentRing.add(segment);
          break;

        default:
          throw new RuntimeException(
              String.format("Unable to handle segment type %d", segmentType));
      }
    }

    ringIndex = rings.size() - 1;
  }

  @Override
  public int getWindingRule() {
    return windingRule;
  }

  @Override
  public boolean isDone() {
    return ringIndex < 0;
  }

  @Override
  public void next() {
    List<Segment> currentRing = rings.get(ringIndex);
    ++segmentIndex;
    if (segmentIndex >= currentRing.size()) {
      --ringIndex;
      segmentIndex = 0;
    }
  }

  @Override
  public int currentSegment(float[] coords) {
    Segment segment = rings.get(ringIndex).get(segmentIndex);
    for (int i = 0; i < segment.coords.length; ++i) {
      coords[i] = (float) segment.coords[i];
    }
    return segment.segmentType;
  }

  @Override
  public int currentSegment(double[] coords) {
    Segment segment = rings.get(ringIndex).get(segmentIndex);
    System.arraycopy(segment.coords, 0, coords, 0, segment.coords.length);
    return segment.segmentType;
  }
}
