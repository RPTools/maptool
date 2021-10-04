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
package net.rptools.maptool.model.framework;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.rptools.maptool.model.framework.dropinlibrary.DropInLibrary;

/** Class to manage the framework libraries. */
public class LibraryManager {

  /** The reserved library name prefixes. */
  private static final Set<String> RESERVED_PREFIXES =
      Set.of(
          "rptools.",
          "maptool.",
          "maptools.",
          "tokentool.",
          "net.rptools.",
          "internal.",
          "_",
          "builtin.",
          "standard.",
          ".");

  /** The reserved library names. */
  private static final Set<String> RESERVED_NAMES =
      Set.of("rptools", "maptool", "maptools", "internal", "builtin", "standard");

  /** Drop in libraries */
  private static final Map<String, DropInLibrary> dropInLibraries = new ConcurrentHashMap<>();

  /**
   * Checks to see if this library name used a reserved prefix.
   *
   * @param name the name of the library
   * @return {@code true} if the name starts with a reserved prefix.
   */
  public boolean usesReservedPrefix(String name) {
    String lowerName = name.toLowerCase();
    return RESERVED_PREFIXES.stream().anyMatch(lowerName::startsWith);
  }

  /**
   * Checks to see if this library name is reserved.
   *
   * @param name the name of the library
   * @return {@code true} if the name is reserved.
   */
  public boolean usesReservedName(String name) {
    String lowerName = name.toLowerCase();
    return RESERVED_NAMES.stream().anyMatch(lowerName::equals);
  }

  /**
   * Returns the reserved prefix this library name starts with.
   *
   * @param name the name of the library.
   * @return the reserved prefix this library starts with or {@code null} if it does not start with
   *     a reserved prefix.
   */
  public String getReservedPrefix(String name) {
    String lowerName = name.toLowerCase();
    return RESERVED_PREFIXES.stream().filter(lowerName::startsWith).findFirst().orElse("");
  }

  /**
   * Returns the {@link Library} for the specified path (e.g. lib://macro/mymacro).
   *
   * @param path the path for the library (can be full path or just part of path).
   * @return the library.
   */
  public CompletableFuture<Optional<Library>> getLibrary(URL path) {
    if (LibraryToken.handles(path)) {
      return LibraryToken.getLibrary(path);
    } else {
      return CompletableFuture.completedFuture(Optional.empty());
    }
  }

  /**
   * Does the library exist.
   *
   * @param path the path for the library (can be full path or just part of path).
   * @return {@code true} if the library exists {@code false} if it does not.
   */
  public CompletableFuture<Boolean> libraryExists(URL path) {
    if (LibraryToken.handles(path)) {
      return LibraryToken.getLibrary(path).thenApply(Optional::isPresent);
    } else {
      return CompletableFuture.completedFuture(Boolean.FALSE);
    }
  }

  public void addDropInLibrary(DropInLibrary library) {}
}
