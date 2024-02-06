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
package net.rptools.maptool.client.ui.zone.vbl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import net.rptools.lib.CodeTimer;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/** Collects, sorts and deduplicates endpoints of vision blocking segments. */
public class EndpointSet {
  // The buckets are eighths of the plane.
  private static final int BUCKET_COUNT = 8;

  private final Coordinate origin;

  /**
   * A bounding box on all endpoints in {@link #buckets}.
   *
   * <p>Used to safely add a ring around the problem set and vision, avoiding any possibility of
   * infinite results when no walls are present in a given direction. It also simplifies the
   * algorithm as we can always count open walls being available.
   */
  private final Envelope envelope;

  /**
   * All endpoints to check during the sweep.
   *
   * <p>Endpoints reference other endpoints, forming a graph where each edge is a vision blocking
   * line segment connecting two endpoints.
   *
   * <p>The endpoints are bucketed in order to improve sorting efficiency, both in terms of buckets
   * being already sorted relative to one another and in terms of avoiding special cases in {@link
   * #comparePolar(org.locationtech.jts.geom.Coordinate, org.locationtech.jts.geom.Coordinate)}.
   */
  private final VisibilitySweepEndpoint[][] buckets;

  private final int[] bucketSizes;

  public EndpointSet(Coordinate origin) {
    this.origin = origin;
    this.envelope = new Envelope();

    this.buckets = new VisibilitySweepEndpoint[BUCKET_COUNT][];
    Arrays.setAll(this.buckets, i -> new VisibilitySweepEndpoint[32]);

    this.bucketSizes = new int[BUCKET_COUNT];
    Arrays.fill(this.bucketSizes, 0);
  }

  public int size() {
    return Arrays.stream(this.bucketSizes).sum();
  }

  public VisibilitySweepEndpoint add(Coordinate point) {
    final var endpoint = new VisibilitySweepEndpoint(point);

    // 0 .. 3
    int quadrantIndex = (point.x >= origin.x ? 0b01 : 0b00) ^ (point.y > origin.y ? 0b11 : 0b00);
    // 0 .. 1
    int quadrantHalfIndex =
        (quadrantIndex & 0b01)
            ^ ((Math.abs(point.x - origin.x) < Math.abs(point.y - origin.y)) ? 0b1 : 0b0);
    // 0 .. 7
    int index = (quadrantIndex << 1) | quadrantHalfIndex;

    assert this.bucketSizes[index] <= this.buckets[index].length;

    final var size = this.bucketSizes[index]++;
    if (size == this.buckets[index].length) {
      this.buckets[index] = Arrays.copyOf(this.buckets[index], 2 * size);
    }
    this.buckets[index][size] = endpoint;

    this.envelope.expandToInclude(point);

    return endpoint;
  }

  public Envelope getBounds() {
    return new Envelope(envelope);
  }

  public void simplify() {
    final var timer = CodeTimer.get();

    timer.start("sort");
    for (int i = 0; i < buckets.length; ++i) {
      final var bucket = buckets[i];
      final var size = bucketSizes[i];

      // We're unlikely to hit the parallelized case, but the use of Timsort is an advantage to us.
      Arrays.parallelSort(bucket, 0, size, (l, r) -> comparePolar(l.getPoint(), r.getPoint()));
    }
    timer.stop("sort");

    timer.start("deduplicate");
    for (int i = 0; i < buckets.length; ++i) {
      final var bucket = buckets[i];
      final var size = bucketSizes[i];

      deduplicateEndpoints(bucket, size);
    }
    timer.stop("deduplicate");
  }

  public @Nonnull Iterable<VisibilitySweepEndpoint> getEndpoints() {
    return () -> new BucketIterator(buckets, bucketSizes);
  }

  /**
   * Check the sorted endpoint list for duplicates and remove them.
   *
   * <p>This check relies on {@code #endpoints} being sorted, otherwise it becomes too expensive.
   */
  private void deduplicateEndpoints(VisibilitySweepEndpoint[] endpoints, int size) {
    assert size < endpoints.length;

    if (size == 0) {
      return;
    }

    // We might have duplicates we don't want.
    @Nonnull VisibilitySweepEndpoint previous = endpoints[0];
    for (var i = 1; i < size; ++i) {
      final var endpoint = endpoints[i];
      if (!previous.getPoint().equals(endpoint.getPoint())) {
        // Haven't seen this endpoint yet. Keep it around.
        previous = endpoint;
        continue;
      }

      // TODO Somehow increment the main counter.
      //  duplicateEndpointCount += 1;

      // endpoint is a duplicate of previous, so merge into previous. Don't keep the duplicate.
      previous.mergeDuplicate(endpoint);

      // I could also .remove(), but that's expensive for long lists. We're just as well to skip
      // this while iterating later.
      endpoints[i] = null;
    }
  }

  /**
   * Compares two points for their polar ordering around an origin.
   *
   * <p>The coordinates are ordered by polar angle ranging in the interval {@code [-π, π)}. If the
   * angle is equal, they will be ordered so that the point closer to the origin comes first.
   *
   * <p>The implementation does not actually compute angles, but is instead based on the clockwise /
   * counterclockwise orientation of the points around the origin.
   *
   * @param a The first coordinate to compare.
   * @param b The second coordinate to compare.
   * @return The comparison result of {@code a} and {@code b}.
   */
  private int comparePolar(Coordinate a, Coordinate b) {
    // a and b are in the same half-plane, i.e., they definitely don't straddle the x-axis, nor do
    // they straddle origin on the x-axis. Now orientation is sufficient to compare, i.e.,
    // "increase" means move counterclockwise.
    final var orientation = Orientation.index(origin, a, b);
    if (orientation == Orientation.COUNTERCLOCKWISE) {
      return -1;
    }
    if (orientation == Orientation.CLOCKWISE) {
      return 1;
    }

    // Points are collinear with the origin. As a fallback, sort by distance. It's not important
    // which way, we just need to be consistent.
    // Since points are in the same quadrant, comparing y-values is almost sufficient for distance.
    // Points on the x-axis are the only ones not captured.

    final var absAY = Math.abs(a.y);
    final var absBY = Math.abs(b.y);
    final var result = Double.compare(absAY, absBY);
    if (result != 0) {
      return result;
    }

    final var absAX = Math.abs(a.x);
    final var absBX = Math.abs(b.x);
    return Double.compare(absAX, absBX);
  }

  private static final class BucketIterator implements Iterator<VisibilitySweepEndpoint> {
    private final VisibilitySweepEndpoint[][] buckets;
    private final int[] bucketSizes;
    private int bucketIndex;
    private int index;

    // Invariant: (bucketIndex >= buckets.length) || (index < bucketsSizes[bucketIndex]).

    public BucketIterator(VisibilitySweepEndpoint[][] buckets, int[] bucketSizes) {
      assert buckets.length == bucketSizes.length;

      this.buckets = buckets;
      this.bucketSizes = bucketSizes;
      this.bucketIndex = 0;
      this.index = 0;

      // Skip any leading empty buckets.
      while (bucketIndex < buckets.length && index == bucketSizes[bucketIndex]) {
        ++bucketIndex;
      }
    }

    @Override
    public boolean hasNext() {
      return bucketIndex < buckets.length;
    }

    @Override
    public VisibilitySweepEndpoint next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      final var element = buckets[bucketIndex][index];

      ++index;
      while (bucketIndex < buckets.length && index == bucketSizes[bucketIndex]) {
        index = 0;
        ++bucketIndex;
      }

      return element;
    }
  }
}
