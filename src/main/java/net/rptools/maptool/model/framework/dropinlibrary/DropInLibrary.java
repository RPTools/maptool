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
package net.rptools.maptool.model.framework.dropinlibrary;

import java.util.Objects;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;

public class DropInLibrary {

  private final String name;
  private final String version;
  private final String website;
  private final String authors;
  private final String gitUrl;
  private final String license;

  private DropInLibrary(DropInLibraryDto dto) {
    Objects.requireNonNull(dto, I18N.getText("library.error.invalidDefinition"));
    name = Objects.requireNonNull(dto.getName(), I18N.getText("library.error.emptyName"));
    version =
        Objects.requireNonNull(dto.getVersion(), I18N.getText("library.error.emptyVersion", name));
    website = Objects.requireNonNullElse(dto.getWebsite(), "");
    authors = Objects.requireNonNullElse(dto.getAuthors(), "");
    gitUrl = dto.getGitUrl();
    license = dto.getLicense();
  }

  public static DropInLibrary fromDto(DropInLibraryDto dto) {
    return new DropInLibrary(dto);
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getWebsite() {
    return website;
  }

  public String getAuthors() {
    return authors;
  }

  public String getGitUrl() {
    return gitUrl;
  }

  public String getLicense() {
    return license;
  }
}
