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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.model.framework.Library;
import net.rptools.maptool.model.framework.proto.CampaignDropInLibraryListDto;
import net.rptools.maptool.model.framework.proto.CampaignDropInLibraryListDto.CampaignDropInLibraryDto;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;

/** Class for managing {@link DropInLibrary} objects. */
public class DropInLibraryManager {

  /** "Protocol" for drop in libraries. */
  private static final String LIBRARY_PROTOCOL = "lib";

  /** The drop in libraries that are registered. */
  private final Map<String, DropInLibrary> namespaceLibraryMap = new ConcurrentHashMap<>();

  /**
   * Is there a drop in library that would handle this path. This just checks the protocol and
   * namespace, it won't check that the full path actually exists.
   *
   * @param path the path for the library (can be full path or just part of path).
   * @return if the library at the path is handled by a drop in library.
   */
  public boolean handles(URL path) {
    if (path.getProtocol().toLowerCase().startsWith(LIBRARY_PROTOCOL)) {
      return namespaceRegistered(path.getHost());
    } else {
      return false;
    }
  }

  /**
   * Checks to see if this namespace is already registered.
   *
   * @param namespace the namespace to check.
   * @return {@code true} if the namespace is registered.
   */
  public boolean namespaceRegistered(String namespace) {
    return namespaceLibraryMap.containsKey(namespace.toLowerCase());
  }

  /**
   * Registers the specified drop in library.
   *
   * @param library The drop in library to register.
   * @throws ExecutionException if there is an error fetching the namespace for the library.
   * @throws InterruptedException if there is an error fetching the namespace for the library.
   * @throws IllegalStateException if there is already a drop in library with the same namespace.
   */
  public void registerLibrary(DropInLibrary library)
      throws ExecutionException, InterruptedException {
    String namespace = library.getNamespace().get().toLowerCase();
    var registeredLib = namespaceLibraryMap.computeIfAbsent(namespace, k -> library);
    if (registeredLib != library) {
      throw new IllegalStateException("Library is already registered");
    }
  }

  /**
   * Deregister the drop in library with the specified namespace.
   *
   * @param namespace the namespace of the library to deregister.
   */
  public void deregisterLibrary(String namespace) {
    namespaceLibraryMap.remove(namespace.toLowerCase());
  }

  /**
   * Returns a list of the registered drop in libraries.
   *
   * @return list of the registered drop in libraries.
   */
  public List<Library> getLibraries() {
    return new ArrayList<>(namespaceLibraryMap.values());
  }

  /**
   * Returns the library with the specified namespace. If no library exists for this namespace then
   * null is returned.
   *
   * @param namespace the namespace of the library.
   * @return the library for the namespace.
   */
  public Library getLibrary(String namespace) {
    return namespaceLibraryMap.getOrDefault(namespace.toLowerCase(), null);
  }

  /**
   * Returns the {@link Library} that will handle the lib:// uri that is passed in.
   *
   * @param path the path of the drop in library.
   * @return the {@link Library} representing the lib:// uri .
   */
  public Library getLibrary(URL path) {
    if (path.getProtocol().toLowerCase().startsWith(LIBRARY_PROTOCOL)) {
      return getLibrary(path.getHost());
    } else {
      return null;
    }
  }

  /**
   * Returns the {@link CampaignDropInLibraryListDto} containing all the drop in libraries.
   *
   * @return the {@link CampaignDropInLibraryListDto} containing all the drop in libraries.
   */
  public CompletableFuture<CampaignDropInLibraryListDto> toDto() {
    return CompletableFuture.supplyAsync(
        () -> {
          var dto = CampaignDropInLibraryListDto.newBuilder();
          for (var library : namespaceLibraryMap.values()) {
            var detailDto = DropInLibraryDto.newBuilder();
            try {
              detailDto.setName(library.getName().get());
              detailDto.setVersion(library.getVersion().get());
              detailDto.setWebsite(library.getWebsite().get());
              detailDto.setGithubUrl(library.getGitHubUrl().get());
              detailDto.addAllAuthors(Arrays.asList(library.getAuthors().get()));
              detailDto.setLicense(library.getLicense().get());
              detailDto.setNamespace(library.getNamespace().get());
              detailDto.setDescription(library.getDescription().get());
              detailDto.setShortDescription(library.getShortDescription().get());
            } catch (InterruptedException | ExecutionException e) {
              throw new CompletionException(e);
            }
            var campDto = CampaignDropInLibraryDto.newBuilder();
            campDto.setDetails(detailDto);
            campDto.setMd5Hash(library.getAssetKey().toString());
            dto.addLibraries(campDto);
          }
          return dto.build();
        });
  }

  /** Remove all the drop in libraries. */
  public void removeAllLibraries() {
    namespaceLibraryMap.clear();
  }
}
