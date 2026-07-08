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
 * A collection of {@link Halo}{@code s} with unique IDs.
 *
 * <p>When adding a {@code Halo}, if the collection already has one with the same ID it will be
 * replaced.
 */
public class Halos extends AbstractSet<Halo> {
  public static Halos copyOf(Iterable<Halo> halos) {
    var aHalos = new Halos();
    for (var halo : halos) {
      aHalos.add(halo);
    }
    return aHalos;
  }

  private final LinkedHashMap<GUID, Halo> guidHaloMap;

  public Halos() {
    this.guidHaloMap = new LinkedHashMap<>();
  }

  public Halos(Halos other) {
    this();
    addAll(other);
  }

  @Override
  public int size() {
    return guidHaloMap.size();
  }

  @Override
  public void clear() {
    guidHaloMap.clear();
  }

  @Override
  public boolean contains(Object o) {
    return o instanceof Halo halo && this.guidHaloMap.containsKey(halo.getId());
  }

  @Override
  public boolean add(Halo halo) {
    guidHaloMap.put(halo.getId(), halo);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Halo halo) {
      var previous = guidHaloMap.remove(halo.getId());
      return previous != null;
    }
    return false;
  }

  @Override
  public @Nonnull Iterator<Halo> iterator() {
    return guidHaloMap.values().iterator();
  }

  public Optional<Halo> get(GUID haloId) {
    return Optional.ofNullable(guidHaloMap.get(haloId));
  }
}
