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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private record Segment(int segmentType, double[] coords) {}

  /**
   * Builds valid rings (closed shapes) from segments returned by a <code>PathIterator</code>.
   *
   * <p>There are some weaknesses in AWT's <code>PathIterator</code>. In particular, it can
   * sometimes return points that differ by only a minute amount. This causes issues downstream, as
   * it looks like a self-intersecting polygon.
   *
   * <p>The RingBuilder enforces the following rules:
   *
   * <ol>
   *   <li>All rings start with <code>SEG_MOVETO</code>.
   *   <li>All rings end with <code>SEG_CLOSE</code>.
   *   <li>All rings contain at least one <code>SEG_LINETO</code>.
   *   <li>All <code>SEG_LINETO</code> segments are distinct from their predecessor.
   * </ol>
   *
   * <p>Violation of rules (1)-(3) results in a ring being dropped entirely. Violation of rule (4)
   * results in the redundant segment being dropped from the ring.
   */
  private static final class RingBuilder {
    private static final Logger log = LogManager.getLogger(RingBuilder.class);

    private final List<List<Segment>> rings = new ArrayList<>();
    /** Includes only those segments that are not indistinguishable from their neighbours. */
    private List<Segment> currentRing = new ArrayList<>();
    /** Includes all segments added to the current ring. Used for logging. */
    private List<Segment> currentFullRing = new ArrayList<>();
    /** Determines which segment types are permitted for the next segment. */
    private final Set<Integer> allowedSegmentTypes;

    public RingBuilder() {
      this.allowedSegmentTypes = new HashSet<>();
      this.allowedSegmentTypes.add(PathIterator.SEG_MOVETO);
    }

    public List<List<Segment>> build() {
      return rings;
    }

    public void add(Segment segment) {
      final var epsilon = 1e-9;

      if (!this.allowedSegmentTypes.contains(segment.segmentType)) {
        log.debug(
            "Eliding ring due to unexpected segment. Unexpected segment type "
                + segment.segmentType
                + ". Expected one of "
                + this.allowedSegmentTypes);
        // Return to a new ring state.
        currentRing = new ArrayList<>();
        currentFullRing = new ArrayList<>();
        setAllowedSegmentTypes(PathIterator.SEG_MOVETO);
        return;
      }

      switch (segment.segmentType) {
        case PathIterator.SEG_MOVETO:
          assert currentRing.isEmpty() : "SEG_MOVETO must be the first segment in a ring";

          currentRing.add(segment);
          currentFullRing.add(segment);

          setAllowedSegmentTypes(PathIterator.SEG_LINETO); // Not allowed to immediately close.
          break;

        case PathIterator.SEG_CLOSE:
          assert currentRing.size() >= 1 : "SEG_CLOSE must at least come after SEG_MOVETO";
          assert currentRing.get(0).segmentType == PathIterator.SEG_MOVETO
              : "SEG_MOVETO must be the first segment in a ring";

          // Explicit end to the ring. This segment is considered part of the ring.
          currentRing.add(segment);
          currentFullRing.add(segment);

          // Note that SEG_CLOSE coordinates are not necessarily meaningful, so we don't do
          // elision on them.

          // If any SEG_LINETO segments have been elided from this ring, it may have become
          // degenerate.
          if (currentRing.size() == 2) {
            log.debug("Eliding degenerate ring: " + currentFullRing);
          } else {
            rings.add(currentRing);
          }

          currentRing = new ArrayList<>();
          currentFullRing = new ArrayList<>();
          setAllowedSegmentTypes(PathIterator.SEG_MOVETO);
          break;

        case PathIterator.SEG_LINETO:
          assert currentRing.size() >= 1 : "SEG_LINETO must at least come after SEG_MOVETO";
          assert currentRing.get(0).segmentType == PathIterator.SEG_MOVETO
              : "SEG_MOVETO must be the first segment in a ring";

          // Simple case of adding a segment.
          currentFullRing.add(segment);
          // Check whether we need to keep or elide the segment.
          if (distance(segment, currentRing.get(currentRing.size() - 1)) >= epsilon) {
            currentRing.add(segment);
          }
          setAllowedSegmentTypes(PathIterator.SEG_LINETO, PathIterator.SEG_CLOSE);
          break;

        default:
          throw new RuntimeException(
              String.format("Unable to handle segment type %d", segment.segmentType));
      }
    }

    private void setAllowedSegmentTypes(int... types) {
      this.allowedSegmentTypes.clear();
      for (var type : types) {
        this.allowedSegmentTypes.add(type);
      }
    }

    private double distance(Segment lhs, Segment rhs) {
      var diffX = rhs.coords[0] - lhs.coords[0];
      var diffY = rhs.coords[1] - lhs.coords[1];

      return Math.sqrt(diffX * diffX + diffY * diffY);
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

    // PathIterator::currentSegment() requires a 6-element array, in case of SEG_QUADTO.
    final double[] coords = new double[6];
    final var ringBuilder = new RingBuilder();
    for (; !forwardPathIterator.isDone(); forwardPathIterator.next()) {
      int segmentType = forwardPathIterator.currentSegment(coords);
      final var segment = new Segment(segmentType, Arrays.copyOf(coords, coords.length));
      ringBuilder.add(segment);
    }

    rings = ringBuilder.build();
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
