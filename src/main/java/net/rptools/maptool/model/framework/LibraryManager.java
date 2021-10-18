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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.framework.dropinlibrary.DropInLibrary;
import net.rptools.maptool.model.framework.dropinlibrary.DropInLibraryManager;
import net.rptools.maptool.model.framework.dropinlibrary.TransferableDropInLibrary;
import net.rptools.maptool.model.framework.proto.CampaignDropInLibraryListDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class to manage the framework libraries. */
public class LibraryManager {

  /** Class for logging messages. */
  private static final Logger log = LogManager.getLogger(AppActions.class);

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
  private static final DropInLibraryManager dropInLibraryManager = new DropInLibraryManager();

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
    if (dropInLibraryManager.handles(path)) {
      return CompletableFuture.completedFuture(
          Optional.ofNullable(dropInLibraryManager.getLibrary(path)));
    } else if (LibraryToken.handles(path)) {
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

  /**
   * Checks if the current namespace has a drop in library registered.
   *
   * @param namespace the namespace to check.
   * @return {@code true} if a drop in library has been registered for this namespace.
   */
  public boolean dropInLibraryExists(String namespace) {
    return dropInLibraryManager.namespaceRegistered(namespace);
  }

  /**
   * Register a drop in library.
   *
   * @param dropInLib
   */
  public boolean registerDropInLibrary(DropInLibrary dropInLib) {
    try {
      dropInLibraryManager.registerLibrary(dropInLib);
      if (MapTool.isHostingServer()) {
        MapTool.serverCommand().addDropInLibrary(List.of(new TransferableDropInLibrary(dropInLib)));
      }
    } catch (ExecutionException | InterruptedException | IllegalStateException e) {
      log.error("Error registering drop in library", e);
      return false;
    }
    return true;
  }

  /**
   * Deregister the drop in library associated with the specified namespace.
   *
   * @param namespace the namespace to deregister.
   */
  public void deregisterDropInLibrary(String namespace) {
    dropInLibraryManager.deregisterLibrary(namespace);
    if (MapTool.isHostingServer()) {
      MapTool.serverCommand().removeDropInLibrary(List.of(namespace));
    }
  }

  /**
   * Register a drop in library, replacing any existing library.
   *
   * @param dropInLibrary the drop in library to register.
   */
  public boolean reregisterDropInLibrary(DropInLibrary dropInLibrary) {
    try {
      dropInLibraryManager.deregisterLibrary(dropInLibrary.getNamespace().get());
      dropInLibraryManager.registerLibrary(dropInLibrary);
      if (MapTool.isHostingServer()) {
        MapTool.serverCommand()
            .addDropInLibrary(List.of(new TransferableDropInLibrary(dropInLibrary)));
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error registering drop in library", e);
      return false;
    }
    return true;
  }

  /**
   * Returns a list of information about the registered libraries.
   *
   * @param libraryType the type of library to get the information about.
   * @return list of information about the registered libraries.
   * @throws ExecutionException if an error occurs while extracting information about the library.
   * @throws InterruptedException if an error occurs while extracting information about the library.
   */
  public List<LibraryInfo> getLibraries(LibraryType libraryType)
      throws ExecutionException, InterruptedException {
    List<Library> libraries =
        switch (libraryType) {
          case TOKEN -> LibraryToken.getLibraries().get();
          case DROP_IN -> dropInLibraryManager.getLibraries();
        };

    var libInfo = new ArrayList<LibraryInfo>();
    libraries.forEach(l -> l.getLibraryInfo().thenAccept(libInfo::add));
    return libInfo;
  }

  /**
   * Returns the information about the library with the specified namespace.
   *
   * @param namespace the namespace of the library to get the information about.
   * @return the information for the library.
   * @throws ExecutionException if an error occurs while extracting information about the library.
   * @throws InterruptedException if an error occurs while extracting information about the library.
   */
  public Optional<LibraryInfo> getLibraryInfo(String namespace)
      throws ExecutionException, InterruptedException {
    Optional<Library> library = getLibrary(namespace);
    if (library.isPresent()) {
      return Optional.ofNullable(library.get().getLibraryInfo().get());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the library for a given namespace.
   *
   * @param namespace the namespace of the library to return.
   * @return the library.
   * @throws ExecutionException if an error occurs while extracting information about the library.
   * @throws InterruptedException if an error occurs while extracting information about the library.
   */
  public Optional<Library> getLibrary(String namespace)
      throws ExecutionException, InterruptedException {
    var lib = dropInLibraryManager.getLibrary(namespace);
    if (lib == null) {
      lib = LibraryToken.getLibrary(namespace).get();
    }

    if (lib == null) {
      return Optional.empty();
    }
    return Optional.of(lib);
  }

  /**
   * Returns the {@link CampaignDropInLibraryListDto} containing all the drop in libraries.
   *
   * @return the {@link CampaignDropInLibraryListDto} containing all the drop in libraries.
   */
  public CompletableFuture<CampaignDropInLibraryListDto> dropInLibrariesToDto() {
    return dropInLibraryManager.toDto();
  }

  /** Removes all the drop in libraries. */
  public void removeAllDropInLibraries() {
    dropInLibraryManager.removeAllLibraries();
    if (MapTool.isHostingServer()) {
      MapTool.serverCommand().removeAllDropInLibraries();
    }
  }
}
