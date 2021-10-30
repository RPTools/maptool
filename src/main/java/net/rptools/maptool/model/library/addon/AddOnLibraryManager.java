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
package net.rptools.maptool.model.library.addon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryListDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryListDto.AddOnLibraryEntryDto;

/** Class for managing {@link AddOnLibrary} objects. */
public class AddOnLibraryManager {

  /** "Protocol" for add-on libraries. */
  private static final String LIBRARY_PROTOCOL = "lib";

  /** The add-on libraries that are registered. */
  private final Map<String, AddOnLibrary> namespaceLibraryMap = new ConcurrentHashMap<>();

  /**
   * Is there a add-on library that would handle this path. This just checks the protocol and
   * namespace, it won't check that the full path actually exists.
   *
   * @param path the path for the library (can be full path or just part of path).
   * @return if the library at the path is handled by a add-on library.
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
   * Registers the specified add-on library.
   *
   * @param library The add-on library to register.
   * @throws ExecutionException if there is an error fetching the namespace for the library.
   * @throws InterruptedException if there is an error fetching the namespace for the library.
   * @throws IllegalStateException if there is already a add-on library with the same namespace.
   */
  public void registerLibrary(AddOnLibrary library)
      throws ExecutionException, InterruptedException {
    String namespace = library.getNamespace().get().toLowerCase();
    var registeredLib = namespaceLibraryMap.computeIfAbsent(namespace, k -> library);
    if (registeredLib != library) {
      throw new IllegalStateException("Library is already registered");
    }
  }

  /**
   * Deregister the add-on library with the specified namespace.
   *
   * @param namespace the namespace of the library to deregister.
   */
  public void deregisterLibrary(String namespace) {
    namespaceLibraryMap.remove(namespace.toLowerCase());
  }

  /**
   * Returns a list of the registered add-on libraries.
   *
   * @return list of the registered add-on libraries.
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
   * @param path the path of the add-on library.
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
   * Returns the {@link AddOnLibraryListDto} containing all the add-on libraries.
   *
   * @return the {@link AddOnLibraryListDto} containing all the add-on libraries.
   */
  public CompletableFuture<AddOnLibraryListDto> toDto() {
    return CompletableFuture.supplyAsync(
        () -> {
          var dto = AddOnLibraryListDto.newBuilder();
          for (var library : namespaceLibraryMap.values()) {
            var detailDto = AddOnLibraryDto.newBuilder();
            try {
              detailDto.setName(library.getName().get());
              detailDto.setVersion(library.getVersion().get());
              detailDto.setWebsite(library.getWebsite().get());
              detailDto.setGitUrl(library.getGitUrl().get());
              detailDto.addAllAuthors(Arrays.asList(library.getAuthors().get()));
              detailDto.setLicense(library.getLicense().get());
              detailDto.setNamespace(library.getNamespace().get());
              detailDto.setDescription(library.getDescription().get());
              detailDto.setShortDescription(library.getShortDescription().get());
            } catch (InterruptedException | ExecutionException e) {
              throw new CompletionException(e);
            }
            var campDto = AddOnLibraryEntryDto.newBuilder();
            campDto.setDetails(detailDto);
            campDto.setMd5Hash(library.getAssetKey().toString());
            dto.addLibraries(campDto);
          }
          return dto.build();
        });
  }

  /** Remove all the add-on libraries. */
  public void removeAllLibraries() {
    namespaceLibraryMap.clear();
  }
}
