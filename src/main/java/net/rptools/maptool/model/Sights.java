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
 * A set of {@link SightType} with unique names.
 *
 * <p>When adding a {@code SightType}, if the collection already has one with the same name it will
 * be replaced.
 */
public class Sights extends AbstractSet<SightType> {
  public static Sights copyOf(Iterable<SightType> sightTypes) {
    var sights = new Sights();
    for (var sightType : sightTypes) {
      sights.add(sightType);
    }
    return sights;
  }

  private final LinkedHashMap<String, SightType> sightTypes;

  public Sights() {
    this.sightTypes = new LinkedHashMap<>();
  }

  public Sights(Sights other) {
    this();
    addAll(other);
  }

  @Override
  public int size() {
    return sightTypes.size();
  }

  @Override
  public void clear() {
    sightTypes.clear();
  }

  @Override
  public boolean contains(Object o) {
    return o instanceof SightType sightType && this.sightTypes.containsKey(sightType.getName());
  }

  @Override
  public boolean add(SightType sightType) {
    sightTypes.put(sightType.getName(), sightType);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof SightType sightType) {
      var previous = sightTypes.remove(sightType.getName());
      return previous != null;
    }
    return false;
  }

  @Override
  public @Nonnull Iterator<SightType> iterator() {
    return sightTypes.values().iterator();
  }

  public Optional<SightType> get(String sightTypeName) {
    return Optional.ofNullable(sightTypes.get(sightTypeName));
  }
}
