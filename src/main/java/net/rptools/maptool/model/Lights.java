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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * A collection of {@link LightSource} with unique IDs.
 *
 * <p>When adding a {@code LightSource}, if the collection already has one with the same ID it will
 * be replaced.
 */
public class Lights extends AbstractSet<LightSource> {
  public static Lights copyOf(Iterable<LightSource> sources) {
    var lights = new Lights();
    for (var source : sources) {
      lights.add(source);
    }
    return lights;
  }

  private final LinkedHashMap<GUID, LightSource> sources;

  public Lights() {
    this.sources = new LinkedHashMap<>();
  }

  public Lights(Lights other) {
    this();
    addAll(other);
  }

  @Override
  public int size() {
    return sources.size();
  }

  @Override
  public void clear() {
    sources.clear();
  }

  @Override
  public boolean contains(Object o) {
    return o instanceof LightSource lightSource && this.sources.containsKey(lightSource.getId());
  }

  @Override
  public boolean add(LightSource lightSource) {
    sources.put(lightSource.getId(), lightSource);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof LightSource lightSource) {
      var previous = sources.remove(lightSource.getId());
      return previous != null;
    }
    return false;
  }

  @Override
  public @Nonnull Iterator<LightSource> iterator() {
    return sources.values().iterator();
  }

  public Optional<LightSource> get(GUID lightSourceId) {
    return Optional.ofNullable(sources.get(lightSourceId));
  }
}
