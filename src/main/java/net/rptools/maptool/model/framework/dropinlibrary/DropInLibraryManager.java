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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.model.framework.Library;
import net.rptools.maptool.model.framework.LibraryInfo;

/** Class for managing {@link DropInLibrary} objects. */
public class DropInLibraryManager {
  /** The drop in libraries that are registered. */
  private final Map<String, DropInLibrary> namespaceLibraryMap = new ConcurrentHashMap<>();

  /**
   * Checks to see if this namespace is already registered.
   *
   * @param namespace the namespace to check.
   * @return {@code true} if the namespace is registered.
   */
  public boolean namespaceRegistered(String namespace) {
    return namespaceLibraryMap.containsKey(namespace);
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
    String namespace = library.getNamespace().get();
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
    namespaceLibraryMap.remove(namespace);
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
   * Returns the library with the specified namespace. If no library exists for this namespace
   * then null is returned.
   * @param namespace the namespace of the library.
   * @return the library for the namespace.
   */
  public Library getLibrary(String namespace) {
    return namespaceLibraryMap.getOrDefault(namespace, null);
  }
}
