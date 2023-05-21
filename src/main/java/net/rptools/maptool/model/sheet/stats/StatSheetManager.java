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
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import org.apache.commons.lang.StringUtils;

public class StatSheetManager {
  private static final String LEGACY_STATSHEET_NAME = "legacy-sheet";
  private static final String LEGACY_STATSHEET_NAMESPACE = "net.rptools.maptool";

  public static final String LEGACY_STATSHEET_ID =
      LEGACY_STATSHEET_NAMESPACE + "." + LEGACY_STATSHEET_NAME;

  public static final StatSheet LEGACY_STATSHEET =
      new StatSheet(
          LEGACY_STATSHEET_NAME,
          I18N.getText("token.statSheet.legacyStatSheetDescription"),
          null,
          Set.of(),
          LEGACY_STATSHEET_NAMESPACE);

  private static final Map<StatSheet, String> statSheets = new ConcurrentHashMap<>();

  static {
    statSheets.put(LEGACY_STATSHEET, "");
  }

  private String[] splitId(String id) {
    var namespace = StringUtils.substringBeforeLast(id, ".");
    var name = StringUtils.substringAfterLast(id, ".");
    return new String[] {namespace, name};
  }

  public boolean isLegacyStatSheet(StatSheetProperties sheet) {
    return isLegacyStatSheet(getStatSheet(sheet.id()));
  }

  public boolean isLegacyStatSheet(StatSheet statSheet) {
    return statSheet == null || LEGACY_STATSHEET.equals(statSheet);
  }

  public StatSheet getStatSheet(String id) {
    var name = splitId(id);
    if (name.length != 2) {
      return null;
    }
    return getStatSheet(name[0], name[1]);
  }

  public String getId(String namespace, String name) {
    return namespace + "." + name;
  }

  public Set<StatSheet> getStatSheets(String propertyType) {
    return statSheets.keySet().stream()
        .filter(s -> s.propertyTypes().isEmpty() || s.propertyTypes().contains((propertyType)))
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

  public String getStatSheetContent(String id) {
    var name = splitId(id);
    if (name.length != 2) {
      return null;
    }
    return getStatSheetContent(name[0], name[1]);
  }

  public String getId(StatSheet ss) {
    return getId(ss.namespace(), ss.name());
  }
}
