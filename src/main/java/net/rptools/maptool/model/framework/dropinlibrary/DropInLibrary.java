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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.framework.Library;
import net.rptools.maptool.model.framework.LibraryInfo;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;

public class DropInLibrary implements Library {

  private final String name;
  private final String version;
  private final String website;
  private final String[] authors;
  private final String gitHubUrl;
  private final String license;
  private final String namespace;
  private final String description;
  private final String shortDescription;
  private final Map<String, MD5Key> pathAssetMap;
  private final boolean allowsUriAccess;

  /**
   * Class used to represent Drop In Libraries.
   *
   * @param dto The Drop In Libraries Data Transfer Object.
   * @param pathAssetMap mapping of paths in the library to {@link MD5Key}s.
   */
  private DropInLibrary(DropInLibraryDto dto, Map<String, MD5Key> pathAssetMap) {
    Objects.requireNonNull(dto, I18N.getText("library.error.invalidDefinition"));
    name = Objects.requireNonNull(dto.getName(), I18N.getText("library.error.emptyName"));
    version =
        Objects.requireNonNull(dto.getVersion(), I18N.getText("library.error.emptyVersion", name));
    website = Objects.requireNonNullElse(dto.getWebsite(), "");
    authors = dto.getAuthorsList().toArray(String[]::new);
    gitHubUrl = dto.getGithubUrl();
    license = dto.getLicense();
    namespace = dto.getNamespace();
    description = dto.getDescription();
    shortDescription = dto.getShortDescription();
    this.pathAssetMap = new HashMap<>(pathAssetMap);
    allowsUriAccess = dto.getAllowsUriAccess();
  }

  public static DropInLibrary fromDto(DropInLibraryDto dto, Map<String, MD5Key> pathAssetMap) {
    return new DropInLibrary(dto, pathAssetMap);
  }

  @Override
  public CompletableFuture<String> getDescription() {
    return CompletableFuture.completedFuture(description);
  }

  @Override
  public CompletableFuture<String> getShortDescription() {
    return CompletableFuture.completedFuture(shortDescription);
  }

  @Override
  public CompletableFuture<Boolean> allowsUriAccess() {
    return CompletableFuture.completedFuture(allowsUriAccess);
  }

  /**
   * Returns a list of the library tokens.
   *
   * @return list of library tokens
   */
  @Override
  public CompletableFuture<LibraryInfo> getLibraryInfo() {
    return CompletableFuture.completedFuture(
        new LibraryInfo(
            name,
            namespace,
            version,
            website,
            gitHubUrl,
            authors,
            license,
            description,
            shortDescription,
            allowsUriAccess));
  }

  @Override
  public CompletableFuture<String> getVersion() {
    return CompletableFuture.completedFuture(version);
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    return null;
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    return null;
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    return null;
  }

  @Override
  public CompletableFuture<String> getWebsite() {
    return CompletableFuture.completedFuture(website);
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return CompletableFuture.completedFuture(authors);
  }

  @Override
  public CompletableFuture<String> getGitHubUrl() {
    return CompletableFuture.completedFuture(gitHubUrl);
  }

  @Override
  public CompletableFuture<String> getLicense() {
    return CompletableFuture.completedFuture(license);
  }

  @Override
  public CompletableFuture<String> getNamespace() {
    return CompletableFuture.completedFuture(namespace);
  }

  @Override
  public CompletableFuture<String> getName() {
    return CompletableFuture.completedFuture(name);
  }
}
