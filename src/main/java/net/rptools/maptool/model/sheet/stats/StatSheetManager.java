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

/**
 * Manages the stat sheets that are available to the system. This includes the legacy stat sheet.
 */
public class StatSheetManager {

  /** The name of the legacy stat sheet. This is the stat sheet. */
  private static final String LEGACY_STATSHEET_NAME = "legacy-sheet";

  /** The namespace of the legacy stat sheet. */
  private static final String LEGACY_STATSHEET_NAMESPACE = "net.rptools.maptool";

  /** The id of the legacy stat sheet. */
  public static final String LEGACY_STATSHEET_ID =
      LEGACY_STATSHEET_NAMESPACE + "." + LEGACY_STATSHEET_NAME;

  /** The legacy stat sheet. */
  public static final StatSheet LEGACY_STATSHEET =
      new StatSheet(
          LEGACY_STATSHEET_NAME,
          I18N.getText("token.statSheet.legacyStatSheetDescription"),
          null,
          Set.of(),
          LEGACY_STATSHEET_NAMESPACE);

  /** The stat sheets that are available to the system. */
  private static final Map<StatSheet, String> statSheets = new ConcurrentHashMap<>();

  /** adds the legacy stat sheet to the list of stat sheets. */
  static {
    statSheets.put(LEGACY_STATSHEET, "");
  }

  /**
   * Returns the default stat sheet.
   *
   * @return the default stat sheet.
   */
  public StatSheet getDefaultStatSheet() {
    return LEGACY_STATSHEET;
  }

  /**
   * Returns the id of the default stat sheet.
   *
   * @return the id of the default stat sheet.
   */
  public String getDefaultStatSheetId() {
    return getId(LEGACY_STATSHEET);
  }

  /**
   * Returns the name and namespace of the stat sheet with the given id.
   *
   * @param id the id of the stat sheet.
   * @return the id of the stat sheet.
   */
  private String[] splitId(String id) {
    var namespace = StringUtils.substringBeforeLast(id, ".");
    var name = StringUtils.substringAfterLast(id, ".");
    return new String[] {namespace, name};
  }

  /**
   * Returns true if the stat sheet is the legacy stat sheet.
   *
   * @param sheet the stat sheet to check.
   * @return true if the stat sheet is the legacy stat sheet.
   */
  public boolean isLegacyStatSheet(StatSheetProperties sheet) {
    return isLegacyStatSheet(getStatSheet(sheet.id()));
  }

  /**
   * Returns true if the stat sheet is the legacy stat sheet.
   *
   * @param statSheet the stat sheet to check.
   * @return true if the stat sheet is the legacy stat sheet.
   */
  public boolean isLegacyStatSheet(StatSheet statSheet) {
    return statSheet == null || LEGACY_STATSHEET.equals(statSheet);
  }

  /**
   * Returns the stat sheet with the given id.
   *
   * @param id the id of the stat sheet.
   * @return the stat sheet with the given id.
   */
  public StatSheet getStatSheet(String id) {
    var name = splitId(id);
    if (name.length != 2) {
      return null;
    }
    return getStatSheet(name[0], name[1]);
  }

  /**
   * Returns the id of the stat sheet with the given namespace and name.
   *
   * @param namespace the namespace of the stat sheet.
   * @param name the name of the stat sheet.
   * @return the id of the stat sheet with the given namespace and name.
   */
  public String getId(String namespace, String name) {
    return namespace + "." + name;
  }

  /**
   * Returns the id of the stat sheet.
   *
   * @param propertyType the property type of the stat sheet.
   * @return the id of the stat sheet.
   */
  public Set<StatSheet> getStatSheets(String propertyType) {
    return statSheets.keySet().stream()
        .filter(s -> s.propertyTypes().isEmpty() || s.propertyTypes().contains((propertyType)))
        .collect(Collectors.toSet());
  }

  /**
   * Returns the id of the stat sheet.
   *
   * @param statSheet the stat sheet.
   * @param library the library to use to read the stat sheet.
   * @throws IOException if an error occurs reading the stat sheet.
   */
  public void addStatSheet(StatSheet statSheet, Library library) throws IOException {
    var html = library.readAsString(statSheet.entry()).join();
    statSheets.put(statSheet, html);
  }

  /**
   * Removes the stat sheet with the given id.
   *
   * @param namespace the namespace of the stat sheet.
   */
  public void removeNamespace(String namespace) {
    statSheets.keySet().removeIf(s -> s.namespace().equals(namespace));
  }

  /**
   * Returns the stat sheet with the given namespace and name.
   *
   * @param namespace the namespace of the stat sheet.
   * @param name the name of the stat sheet.
   * @return the stat sheet with the given namespace and name.
   */
  public StatSheet getStatSheet(String namespace, String name) {
    return statSheets.keySet().stream()
        .filter(s -> s.namespace().equals(namespace) && s.name().equals(name))
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the stat sheet with the given namespace and name.
   *
   * @param namespace the namespace of the stat sheet.
   * @param name the name of the stat sheet.
   * @return the stat sheet with the given namespace and name.
   */
  public String getStatSheetContent(String namespace, String name) {
    return statSheets.entrySet().stream()
        .filter(s -> s.getKey().namespace().equals(namespace) && s.getKey().name().equals(name))
        .findFirst()
        .map(Map.Entry::getValue)
        .orElse(null);
  }

  /**
   * Returns the stat sheet with the given id.
   *
   * @param id the id of the stat sheet.
   * @return the stat sheet with the given id.
   */
  public String getStatSheetContent(String id) {
    var name = splitId(id);
    if (name.length != 2) {
      return null;
    }
    return getStatSheetContent(name[0], name[1]);
  }

  /**
   * Returns the id of the stat sheet.
   *
   * @param ss the stat sheet.
   * @return the id of the stat sheet.
   */
  public String getId(StatSheet ss) {
    return getId(ss.namespace(), ss.name());
  }
}
