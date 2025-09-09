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

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** A collection of {@link LightSource} grouped into categories. */
public class CategorizedLights {
  public record Category(String name, Lights lights) {}

  public static CategorizedLights copyOf(Map<String, ? extends Map<GUID, LightSource>> sources) {
    var categorized = new CategorizedLights();
    for (var entry : sources.entrySet()) {
      categorized.addAllToCategory(entry.getKey(), entry.getValue().values());
    }
    return categorized;
  }

  private final TreeMap<String, Lights> allLights;

  public CategorizedLights() {
    allLights = new TreeMap<>();
  }

  public CategorizedLights(CategorizedLights other) {
    this();
    for (var entry : other.allLights.entrySet()) {
      allLights.put(entry.getKey(), new Lights(entry.getValue()));
    }
  }

  public boolean isEmpty() {
    return allLights.isEmpty();
  }

  public void clear() {
    allLights.clear();
  }

  public Iterable<Category> getCategories() {
    return Iterables.transform(allLights.entrySet(), e -> new Category(e.getKey(), e.getValue()));
  }

  /**
   * Looks up a category, adding it if it doesn't already exist.
   *
   * @param name The name of the category to find.
   * @return The existing category if present, otherwise the new category.
   */
  public Optional<Category> getCategory(String name) {
    var lights = this.allLights.get(name);
    if (lights == null) {
      return Optional.empty();
    }

    return Optional.of(new Category(name, lights));
  }

  public void addToCategory(String categoryName, LightSource light) {
    addAllToCategory(categoryName, List.of(light));
  }

  public void addAllToCategory(String categoryName, Collection<LightSource> lights) {
    if (lights.isEmpty()) {
      // Don't create a category if we don't have to.
      return;
    }
    allLights.computeIfAbsent(categoryName, c -> new Lights()).addAll(lights);
  }

  public void addAll(CategorizedLights other) {
    for (var entry : other.allLights.entrySet()) {
      addAllToCategory(entry.getKey(), entry.getValue());
    }
  }
}
