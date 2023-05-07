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
package net.rptools.maptool.model.sheet.stats;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.rptools.maptool.model.library.Library;

public class StatSheetManager {

  private static final Map<StatSheet, String> statSheets = new ConcurrentHashMap<>();

  public Set<StatSheet> getStatSheets(String propertyType) {
    return statSheets.keySet().stream()
        .filter(s -> s.propertyType().equals(propertyType))
        .collect(Collectors.toSet());
  }

  public void addStatSheet(StatSheet statSheet, Library library) throws IOException {
    var html = library.readAsHTMLContent(statSheet.entry()).join();
    statSheets.put(statSheet, html);
  }

  public void removeNamespace(String namespace) {
    statSheets.keySet().removeIf(s -> s.namespace().equals(namespace));
  }

  public StatSheet getStatSheet(String namespace, String name) {
    return statSheets.keySet().stream()
        .filter(s -> s.namespace().equals(namespace) && s.name().equals(name))
        .findFirst()
        .orElse(null);
  }

  public String getStatSheetContent(String namespace, String name) {
    return statSheets.entrySet().stream()
        .filter(s -> s.getKey().namespace().equals(namespace) && s.getKey().name().equals(name))
        .findFirst()
        .map(Map.Entry::getValue)
        .orElse(null);
  }
}
