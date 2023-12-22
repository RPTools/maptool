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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import net.rptools.lib.CodeTimer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public class EndpointSet {
  private final Coordinate origin;

  /**
   * A bounding box on all endpoints in {@link #endpoints}.
   *
   * <p>Used to safely add a ring around the problem set and vision, avoiding any possibility of
   * infinite results when no walls are present in a given direction. It also simplifies the
   * algorithm as we can always count open walls being available.
   */
  private final Envelope envelope;

  private final List<VisibilitySweepEndpoint> endpoints;

  public EndpointSet(Coordinate origin, Envelope bounds) {
    this.origin = origin;
    this.envelope = new Envelope(bounds);
    this.endpoints = new ArrayList<>();
  }

  public int size() {
    return endpoints.size();
  }

  public VisibilitySweepEndpoint add(Coordinate point) {
    final var endpoint = new VisibilitySweepEndpoint(point, origin);
    this.endpoints.add(endpoint);
    this.envelope.expandToInclude(point);
    return endpoint;
  }

  public Envelope getBounds() {
    return new Envelope(envelope);
  }

  public void simplify() {
    final var timer = CodeTimer.get();

    timer.start("sort");
    // We're unlikely to hit the parallelized case, but the use of Timsort is an advantage to us.
    endpoints.sort(
        Comparator.comparingDouble(VisibilitySweepEndpoint::getPseudoangle)
            .thenComparing(VisibilitySweepEndpoint::getDistance));
    timer.stop("sort");

    timer.start("deduplicate");
    deduplicateEndpoints(endpoints);
    timer.stop("deduplicate");
  }

  public @Nonnull Collection<VisibilitySweepEndpoint> getEndpoints() {
    return endpoints;
  }

  /**
   * Check the sorted endpoint list for duplicates and remove them.
   *
   * <p>This check relies on {@code #endpoints} being sorted, otherwise it becomes too expensive.
   */
  private void deduplicateEndpoints(List<VisibilitySweepEndpoint> endpoints) {
    // We might have duplicates we we don't want.
    VisibilitySweepEndpoint previous = null;
    for (var i = 0; i < endpoints.size(); ++i) {
      final var endpoint = endpoints.get(i);
      if (previous == null) {
        previous = endpoint;
        continue;
      }

      if (previous.getPoint().equals(endpoint.getPoint())) {
        // TODO Somehow update the counter.
        // duplicateEndpointCount += 1;

        // endpoint is a duplicate of previous, so merge into previous. Don't keep the duplicate.
        previous.mergeDuplicate(endpoint);

        // I could also .remove(), but that's expensive for long lists. We're just as well to skip
        // this while iterating later.
        endpoints.set(i, null);
        continue;
      }

      // Haven't seen this endpoint yet. Add it to the map.
      previous = endpoint;
    }

    // Don't keep trailing nulls, those will mess things up.
    while (endpoints.getLast() == null) {
      endpoints.removeLast();
    }
  }
}
