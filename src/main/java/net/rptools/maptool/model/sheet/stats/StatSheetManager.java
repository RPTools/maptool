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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import org.apache.commons.lang3.StringUtils;

/**
 * Manages the stat sheets that are available to the system. This includes the legacy stat sheet.
 */
public class StatSheetManager {

  /** The name of the legacy stat sheet. This is the stat sheet. */
  private static final String LEGACY_STATSHEET_NAME = "legacy-sheet";

  /** The namespace of the legacy stat sheet. */
  private static final String LEGACY_STATSHEET_NAMESPACE = "net.rptools.maptool";

  /** The namespace used for no stat sheet. */
  private static final String NO_STATSHEET_NAMESPACE = "net.rptools.maptool";

  /** The name used for no stat sheet. */
  private static final String NO_STATSHEET_NAME = "no-statsheet";

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

  public static final StatSheet NO_STATSHEET =
      new StatSheet(
          NO_STATSHEET_NAME,
          I18N.getText("token.statSheet.noStatSheetDescription"),
          null,
          Set.of(),
          NO_STATSHEET_NAMESPACE);

  /** The internal stat sheets that are always available. */
  private static final Set<StatSheet> internalStatSheets = Set.of(LEGACY_STATSHEET, NO_STATSHEET);

  /** The stat sheets that are available to the system. */
  private static final Map<StatSheet, String> statSheets = new ConcurrentHashMap<>();

  /** Adds the legacy and "no" stat sheet to the list of stat sheets. */
  static {
    statSheets.put(LEGACY_STATSHEET, "");
    statSheets.put(NO_STATSHEET, "");
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
   * Returns true if the stat sheet is the "no" stat sheet.
   *
   * @param sheet the stat sheet to check.
   * @return true if the stat sheet is the "no" stat sheet.
   */
  public boolean isNoStatSheet(StatSheetProperties sheet) {
    return isNoStatSheet(getStatSheet(sheet.id()));
  }

  /**
   * Returns true if the stat sheet is the "no" stat sheet.
   *
   * @param statSheet the stat sheet to check.
   * @return true if the stat sheet is the "no" stat sheet.
   */
  public boolean isNoStatSheet(StatSheet statSheet) {
    return NO_STATSHEET.equals(statSheet);
  }

  /**
   * Returns true if the location of the stat sheet can be set by the user.
   *
   * @param sheet the stat sheet to check.
   * @return true if the location of the stat sheet can be set by the user.
   */
  public boolean isLocationUserSettable(StatSheet sheet) {
    return sheet != null
        && !isLegacyStatSheet(sheet)
        && !isNoStatSheet(sheet)
        && sheet.namespace() != null;
  }

  /**
   * Returns true if the location of the stat sheet can be set by the user.
   *
   * @param sheet the stat sheet to check.
   * @return true if the location of the stat sheet can be set by the user.
   */
  public boolean isLocationUserSettable(StatSheetProperties sheet) {
    return isLocationUserSettable(getStatSheet(sheet.id()));
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
   * Returns the stat sheets ordered by internal vs non-internal and then alphabetically by
   * description.
   *
   * @param propertyType the property type of the stat sheet.
   * @return the id of the stat sheet.
   */
  public SortedSet<StatSheet> getOrderedStatSheets(String propertyType) {
    TreeSet<StatSheet> sheets =
        new TreeSet<StatSheet>((s1, s2) -> StatSheetManager.compareStatSheets(s1, s2));
    sheets.addAll(getStatSheets(propertyType));
    return sheets;
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
   * Compares two stat sheets. Internal stat sheets (null, legacy, and no stat sheet) come before
   * non-internal stat sheets. Among internal stat sheets, null (default) comes first, then legacy,
   * then no stat sheet. Non-internal stat sheets are sorted alphabetically by description.
   *
   * @param ss1 the first stat sheet.
   * @param ss2 the second stat sheet.
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second.
   */
  private static int compareStatSheets(StatSheet ss1, StatSheet ss2) {
    if (internalStatSheets.contains(ss1)) {
      if (internalStatSheets.contains(ss2)) {
        return Comparator.comparing((StatSheet ss) -> ss == null)
            .thenComparing(ss -> ss.description())
            .compare(ss1, ss2);
      } else {
        return -1; // Internal stat sheets come before non-internal stat sheets
      }
    } else if (internalStatSheets.contains(ss2)) {
      return 1; // Non-internal stat sheets come after internal stat sheets
    } else {
      return Comparator.comparing((StatSheet ss) -> ss.description()).compare(ss1, ss2);
    }
  }
}
