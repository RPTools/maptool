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
package net.rptools.maptool.model.library;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.addon.AddOnLibraryData;
import net.rptools.maptool.model.library.addon.AddOnLibraryManager;
import net.rptools.maptool.model.library.addon.AddOnSlashCommandManager;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.model.library.builtin.BuiltInLibraryManager;
import net.rptools.maptool.model.library.proto.AddOnLibraryListDto;
import net.rptools.maptool.model.library.token.LibraryTokenManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class to manage the framework libraries. */
public class LibraryManager {

  /** Class for logging messages. */
  private static final Logger log = LogManager.getLogger(LibraryManager.class);

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

  /** Built in libraries */
  private static final BuiltInLibraryManager builtInLibraryManager = new BuiltInLibraryManager();

  /** Drop in libraries */
  private static final AddOnLibraryManager addOnLibraryManager = new AddOnLibraryManager();

  /** Library Tokens. */
  private static final LibraryTokenManager libraryTokenManager = new LibraryTokenManager();

  /** Listener for dealing with add-on slash commands. */
  private static final AddOnSlashCommandManager addOnSlashCommandManager =
      new AddOnSlashCommandManager();

  public static void init() {
    libraryTokenManager.init();
    builtInLibraryManager.loadBuiltIns();
    new MapToolEventBus().getMainEventBus().register(addOnSlashCommandManager);
  }

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
    if (builtInLibraryManager.handles(path) && builtInLibraryManager.getLibrary(path) != null) {
      return CompletableFuture.completedFuture(
          Optional.ofNullable(builtInLibraryManager.getLibrary(path)));
    } else if (addOnLibraryManager.handles(path)) {
      return CompletableFuture.completedFuture(
          Optional.ofNullable(addOnLibraryManager.getLibrary(path)));
    } else if (libraryTokenManager.handles(path)) {
      return libraryTokenManager.getLibrary(path);
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
    if (libraryTokenManager.handles(path)) {
      return libraryTokenManager.getLibrary(path).thenApply(Optional::isPresent);
    } else {
      return CompletableFuture.completedFuture(Boolean.FALSE);
    }
  }

  /**
   * Checks if the current namespace has an add-on library registered.
   *
   * @param namespace the namespace to check.
   * @return {@code true} if and add-on library has been registered for this namespace.
   */
  public boolean addOnLibraryExists(String namespace) {
    return addOnLibraryManager.namespaceRegistered(namespace);
  }

  /**
   * Register and add-on library.
   *
   * @param addOn the Add On to register.
   */
  public boolean registerAddOnLibrary(AddOnLibrary addOn) {
    try {
      addOnLibraryManager.registerLibrary(addOn);
      if (MapTool.isHostingServer()) {
        MapTool.serverCommand().addAddOnLibrary(List.of(new TransferableAddOnLibrary(addOn)));
      }
    } catch (ExecutionException | InterruptedException | IllegalStateException e) {
      log.error("Error registering add-on in library", e);
      return false;
    }
    return true;
  }

  /**
   * Deregister the add-on in library associated with the specified namespace.
   *
   * @param namespace the namespace to deregister.
   */
  public void deregisterAddOnLibrary(String namespace) {
    addOnLibraryManager.deregisterLibrary(namespace);
    if (MapTool.isHostingServer()) {
      MapTool.serverCommand().removeAddOnLibrary(List.of(namespace));
    }
  }

  /**
   * Register a add-on in library, replacing any existing library.
   *
   * @param addOnLibrary the add-on in library to register.
   */
  public boolean reregisterAddOnLibrary(AddOnLibrary addOnLibrary) {
    try {
      addOnLibraryManager.deregisterLibrary(addOnLibrary.getNamespace().get());
      addOnLibraryManager.registerLibrary(addOnLibrary);
      if (MapTool.isHostingServer()) {
        MapTool.serverCommand()
            .addAddOnLibrary(List.of(new TransferableAddOnLibrary(addOnLibrary)));
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error registering add-on in library", e);
      return false;
    }
    return true;
  }

  /**
   * Returns a list of information about the registered libraries.
   *
   * @param libraryType the propertyType of library to get the information about.
   * @return list of information about the registered libraries.
   * @throws ExecutionException if an error occurs while extracting information about the library.
   * @throws InterruptedException if an error occurs while extracting information about the library.
   */
  public List<LibraryInfo> getLibraries(LibraryType libraryType)
      throws ExecutionException, InterruptedException {
    List<Library> libraries =
        switch (libraryType) {
          case TOKEN -> libraryTokenManager.getLibraries().get();
          case ADD_ON -> addOnLibraryManager.getLibraries();
          case BUILT_IN -> builtInLibraryManager.getLibraries();
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
   * Returns the library with the specified namespace. This version of the method can be used to map
   * "@this" to the current library a MTScript macro is running from.
   *
   * @param namespace the namespace of the library to get.
   * @param context the context to use when mapping "@this" to the current library.
   * @return the library with the specified namespace.
   */
  public Optional<Library> getLibraryForMTScriptCall(
      String namespace, MapToolMacroContext context) {
    String ns = namespace;
    if ("@this".equalsIgnoreCase(namespace)) {
      if (context == null || context.getSource() == null || context.getSource().isEmpty()) {
        return Optional.empty();
      }
      ns = context.getSource().replaceFirst("(?i)^lib:", "");
    }
    return getLibrary(ns);
  }

  /**
   * Returns the library for a given namespace.
   *
   * @param namespace the namespace of the library to return.
   * @return the library.
   */
  public Optional<Library> getLibrary(String namespace) {
    var lib = builtInLibraryManager.getLibrary(namespace);
    if (lib == null) {
      lib = addOnLibraryManager.getLibrary(namespace);
    }
    if (lib == null) {
      lib = libraryTokenManager.getLibrary(namespace).join();
    }

    if (lib == null) {
      return Optional.empty();
    }
    return Optional.of(lib);
  }

  /**
   * Returns the {@link AddOnLibraryListDto} containing all the add-on in libraries.
   *
   * @return the {@link AddOnLibraryListDto} containing all the add-on in libraries.
   */
  public CompletableFuture<AddOnLibraryListDto> addOnLibrariesToDto() {
    return addOnLibraryManager.toDto();
  }

  /** de-registers all libraries from the library manager. */
  public void deregisterAllLibraries() {
    deregisterAddOnLibraries();
    libraryTokenManager.clearLibraries();
  }

  /** de-registers all the add-on in libraries. */
  public void deregisterAddOnLibraries() {
    addOnLibraryManager.removeAllLibraries();
    if (MapTool.isHostingServer()) {
      MapTool.serverCommand().removeAllAddOnLibraries();
    }
  }

  /**
   * Removes an add on library from the library manager. The difference between this method and
   * {@link #deregisterAddOnLibrary(String)} (String)} is that this method will flag the library as
   * needing initialization next time it is added.
   *
   * @param namespace the namespace of the library to remove.
   */
  public void removeAddOnLibrary(String namespace) {
    var library = addOnLibraryManager.getLibrary(namespace);
    if (library != null) {
      library
          .getLibraryData()
          .thenAccept(
              data -> {
                if (data instanceof AddOnLibraryData ald) {
                  ald.setNeedsToBeInitialized(true);
                }
              })
          .join();
      deregisterAddOnLibrary(namespace);
    }
  }

  /**
   * Removes all add-on libraries from the library manager. The difference between this method and
   * {@link #deregisterAddOnLibraries()} is that this method will flag the libraries as needing
   * initialization next time they are added.
   */
  public void removeAddOnLibraries() {
    for (var lib : addOnLibraryManager.getLibraries()) {
      lib.getLibraryData()
          .thenAccept(
              data -> {
                if (data instanceof AddOnLibraryData ald) {
                  ald.setNeedsToBeInitialized(true);
                }
              })
          .join();
    }
    deregisterAddOnLibraries();
  }

  /**
   * Returns the list of tokens that have handlers for the specified legacy token events.
   *
   * @param eventName the name of the event to match.
   * @return the list of tokens that have handlers for the specified legacy token events.
   */
  public CompletableFuture<List<Library>> getLegacyEventTargets(String eventName) {
    return CompletableFuture.supplyAsync(
        () -> {
          var addons = addOnLibraryManager.getLegacyEventTargets(eventName).join();
          var tokens = libraryTokenManager.getLegacyEventTargets(eventName).join();
          var libs = new HashSet<Library>(addons);
          var addonLibNamespaces =
              addons.stream().map(Library::getNamespace).collect(Collectors.toSet());
          // Only add lib:tokens if there are no addon libraries with the same namespace
          for (var token : tokens) {
            if (!addonLibNamespaces.contains(token.getNamespace())) {
              libs.add(token);
            }
          }
          return new ArrayList<>(libs);
        });
  }
}
