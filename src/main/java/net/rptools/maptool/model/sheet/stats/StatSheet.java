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

import java.net.URL;
import java.util.Objects;
import java.util.Set;

/**
 * Record for maintaining stat sheet details.
 *
 * @param name The name of the stat sheet.
 * @param description The description of the stat sheet.
 * @param entry The entry point for the stat sheet.
 * @param propertyTypes The property types that this stat sheet belongs to, empty set = all property
 *     types.
 * @param namespace The namespace of the add-on that provides the spreadsheet.
 */
public record StatSheet(
    String name, String description, URL entry, Set<String> propertyTypes, String namespace) {
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StatSheet other)) {
      return false;
    }
    if (!Objects.equals(name, other.name)) {
      return false;
    }
    if (!Objects.equals(description, other.description)) {
      return false;
    }
    if (!Objects.equals(propertyTypes, other.propertyTypes)) {
      return false;
    }
    if (!Objects.equals(namespace, other.namespace)) {
      return false;
    }

    // Finally the URLs
    var thisUrl = entry == null ? null : entry.toString();
    var otherUrl = other.entry == null ? null : other.entry.toString();

    return Objects.equals(thisUrl, otherUrl);
  }

  @Override
  public int hashCode() {
    // Hash the URL as a string. E.g., we don't need hostname resolution just for a hashcode.
    return Objects.hash(
        name, description, entry == null ? null : entry.toString(), propertyTypes, namespace);
  }
}
