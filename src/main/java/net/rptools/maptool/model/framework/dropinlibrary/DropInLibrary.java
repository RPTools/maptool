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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.framework.Library;
import net.rptools.maptool.model.framework.LibraryInfo;
import net.rptools.maptool.model.framework.LibraryNotValidException;
import net.rptools.maptool.model.framework.LibraryNotValidException.Reason;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;
import org.javatuples.Pair;

/** Class that implements drop in libraries. */
public class DropInLibrary implements Library {

  /** The name of the drop in library. */
  private final String name;
  /** The version of the drop in library. */
  private final String version;

  /** The website for the drop in library. */
  private final String website;

  /** The authors for the drop in library. */
  private final String[] authors;

  /** The github url of the drop in library. */
  private final String gitHubUrl;

  /** The license for the drop in library. */
  private final String license;

  /** The namespace for the drop in library. */
  private final String namespace;

  /** The description for the drop in library. */
  private final String description;

  /** The short description for the drop in library. */
  private final String shortDescription;

  /** The mapping between paths and asset information. */
  private final Map<String, Pair<MD5Key, Type>> pathAssetMap;

  /** if the drop in library allows URI access or not. */
  private final boolean allowsUriAccess;

  /**
   * Class used to represent Drop In Libraries.
   *
   * @param dto The Drop In Libraries Data Transfer Object.
   * @param pathAssetMap mapping of paths in the library to {@link MD5Key}s and {@link Asset.Type}s.
   */
  private DropInLibrary(DropInLibraryDto dto, Map<String, Pair<MD5Key, Asset.Type>> pathAssetMap) {
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

  public static DropInLibrary fromDto(
      DropInLibraryDto dto, Map<String, Pair<MD5Key, Asset.Type>> pathAssetMap) {
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

  /**
   * Returns the asset information for the specified location, this will take care of the mapping to
   * public/
   *
   * @param location the URI location passed in.
   * @return the asset information for the path, if there is no asset information at that path then
   *     null is returned.
   */
  private Pair<MD5Key, Asset.Type> getURILocation(URL location) {
    String path = "public" + location.getPath();
    return pathAssetMap.get(path);
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    if (allowsUriAccess) {
      return CompletableFuture.completedFuture(getURILocation(location) != null);
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.dropin.no.access", name));
    }
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    if (allowsUriAccess) {
      var values = getURILocation(location);
      if (values == null) {
        throw new IOException("Invalid Location");
      }
      if (!values.getValue1().isStringType()) {
        throw new LibraryNotValidException(
            Reason.BAD_CONVERSION,
            I18N.getText("library.error.dropin.notText", values.getValue1().name()));
      }
      return CompletableFuture.supplyAsync(
          () -> {
            Asset asset = AssetManager.getAsset(values.getValue0());
            return asset.getDataAsString();
          });
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.dropin.no.access", name));
    }
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    if (allowsUriAccess) {
      var values = getURILocation(location);
      if (values == null) {
        throw new IOException("Invalid Location");
      }
      return CompletableFuture.supplyAsync(
          () -> {
            Asset asset = AssetManager.getAsset(values.getValue0());
            return new ByteArrayInputStream(asset.getData());
          });
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.dropin.no.access", name));
    }
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
