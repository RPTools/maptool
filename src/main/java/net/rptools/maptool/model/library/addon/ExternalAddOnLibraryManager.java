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

import com.google.common.eventbus.Subscribe;
import io.methvin.watcher.DirectoryWatcher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import net.rptools.maptool.events.MapToolEventBus;

/**
 * Manages the external add-on libraries that are baked by the file system. This manager will watch
 * the external add-on library directory for changes and will update the add-on libraries available
 * but will not automatically update the add-on libraries that MapTool has loaded.
 */
public class ExternalAddOnLibraryManager {

  /** The add-on library manager that is used to register the external add-on libraries. */
  private final AddOnLibraryManager addOnLibraryManager;

  /** The add-on libraries that are registered. */
  private final Map<String, ExternalLibraryInfo> namespaceInfoMap = new ConcurrentHashMap<>();

  /** Is the external add-on library manager enabled. */
  private boolean isEnabled = false;

  /** The path to the external add-on libraries. */
  private Path externalLibraryPath = null;

  /** Directory watcher for watching the external add-on library directory. */
  private DirectoryWatcher directoryWatcher;

  /** Lock for managing enabled and path states. */
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Creates a new instance of the external add-on library manager. {@code init()} must be called
   * after construction.
   *
   * @param addOnLibraryManager the add-on library manager used to register the add-on libraries.
   */
  public ExternalAddOnLibraryManager(AddOnLibraryManager addOnLibraryManager) {
    this.addOnLibraryManager = addOnLibraryManager;
  }

  /** Initializes the external add-on library manager. */
  public void init() {
    var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.register(this);
  }

  /**
   * Handles the event when an add-on library is added to MapTool.
   *
   * @param library the add-on library that was added to MapTool.
   */
  @Subscribe
  public void onLibraryAdded(AddOnLibrary library) {
    library
        .getNamespace()
        .thenAccept(
            namespace -> {
              if (namespaceInfoMap.containsKey(namespace)) {
                var oldInfo = namespaceInfoMap.get(namespace);
                // We do not want to use the library or the library info from the add-on library as
                // disk may be different
                var newInfo =
                    new ExternalLibraryInfo(
                        namespace, oldInfo.library(), oldInfo.libraryInfo(), false, true);
                namespaceInfoMap.put(namespace, newInfo);
              }
            });
  }

  /**
   * Handles the event when an add-on library is removed from MapTool.
   *
   * @param library the add-on library that was removed from MapTool.
   */
  @Subscribe
  public void onLibraryRemoved(AddOnLibrary library) {
    library
        .getNamespace()
        .thenAccept(
            namespace -> {
              if (namespaceInfoMap.containsKey(namespace)) {
                var oldInfo = namespaceInfoMap.get(namespace);
                // We do not want to use the library or the library info from the add-on library as
                // disk may be different
                var newInfo =
                    new ExternalLibraryInfo(
                        namespace, oldInfo.library(), oldInfo.libraryInfo(), true, false);
                namespaceInfoMap.put(namespace, newInfo);
              }
            });
  }

  /**
   * Handles the event when an add-on library is updated in MapTool.
   *
   * @param library the add-on library that was updated in MapTool.
   */
  @Subscribe
  public void onLibraryUpdated(AddOnLibrary library) {
    library
        .getNamespace()
        .thenAccept(
            namespace -> {
              if (namespaceInfoMap.containsKey(namespace)) {
                var oldInfo = namespaceInfoMap.get(namespace);
                // We do not want to use the library or the library info from the add-on library as
                // disk may be different
                var newInfo =
                    new ExternalLibraryInfo(
                        namespace, oldInfo.library(), oldInfo.libraryInfo(), false, true);
                namespaceInfoMap.put(namespace, newInfo);
              }
            });
  }

  /**
   * Registers an external add-on library.
   *
   * @param addOnLibrary the add-on library to register.
   */
  private void registerExternalAddOnLibrary(AddOnLibrary addOnLibrary) {
    addOnLibrary
        .getNamespace()
        .thenAccept(
            namespace ->
                addOnLibrary
                    .getLibraryInfo()
                    .thenAccept(
                        libraryInfo -> {
                          boolean isInstalled =
                              addOnLibraryManager.isNamespaceRegistered(namespace);
                          var externalInfo =
                              new ExternalLibraryInfo(
                                  namespace, addOnLibrary, libraryInfo, true, isInstalled);
                          namespaceInfoMap.put(namespace, externalInfo);
                        }));
  }

  /**
   * Deregisters an external add-on library.
   *
   * @param namespace the namespace of the add-on library to deregister.
   */
  public void deregisterExternalAddOnLibrary(String namespace) {
    namespaceInfoMap.remove(namespace.toLowerCase());
  }

  /**
   * Refreshes an external add-on library.
   *
   * @param path the path to the add-on library.
   */
  public void refreshExternalAddOnLibrary(Path path) {
    registerExternalAddOnLibrary(path); // Allows us to change behaviour later without breaking API
  }

  /**
   * Registers an external add-on library.
   *
   * @param path the path to the add-on library.
   */
  public void registerExternalAddOnLibrary(Path path) {
    try {
      var lib = new AddOnLibraryImporter().importFromDirectory(path);
      registerExternalAddOnLibrary(lib);
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO: CDW
    }
  }

  /**
   * Gets the external libraries that have been registered.
   *
   * @return the external libraries.
   */
  public List<ExternalLibraryInfo> getLibraries() {
    return new ArrayList<>(namespaceInfoMap.values());
  }

  /**
   * Is the external add-on library manager enabled.
   *
   * @return {@code true} if the external add-on library manager is enabled.
   */
  public boolean isEnabled() {
    try {
      lock.lock();
      return isEnabled;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the enabled state of the external add-on library manager.
   *
   * @param enabled the enabled state.
   */
  public void setEnabled(boolean enabled) {
    try {
      lock.lock();
      if (isEnabled != enabled) {
        isEnabled = enabled;
        if (enabled) {
          startWatching();
        } else {
          stopWatching();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Gets the path to the external add-on libraries.
   *
   * @return the path to the external add-on libraries.
   */
  public Path getExternalLibraryPath() {
    try {
      lock.lock();
      return externalLibraryPath;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the path to the external add-on libraries.
   *
   * @param path the path to the external add-on libraries.
   */
  public void setExternalLibraryPath(Path path) {
    if (path != null && path.equals(externalLibraryPath)) {
      return;
    }
    try {
      lock.lock();
      externalLibraryPath = path;
      stopWatching();
      if (path != null) {
        startWatching();
      }
    } finally {
      lock.unlock();
    }
  }

  /** Stops watching the external add-on library directory. */
  private void stopWatching() {
    try {
      lock.lock();
      if (directoryWatcher != null) {
        try {
          directoryWatcher.close();
          directoryWatcher = null;
        } catch (IOException e) {
          throw new RuntimeException(e); // TODO: CDW
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /** Starts watching the external add-on library directory. */
  private void startWatching() {
    try {
      lock.lock();
      refreshAll();
      if (isEnabled && externalLibraryPath != null && Files.exists(externalLibraryPath)) {
        if (directoryWatcher != null) {
          directoryWatcher.watchAsync();
        } else {
          directoryWatcher = createDirectoryWatcher();
          directoryWatcher.watchAsync();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO: CDW
    } finally {
      lock.unlock();
    }
  }

  /** Refreshes all the external add-on libraries. */
  private void refreshAll() {
    if (externalLibraryPath == null) {
      return;
    }
    File[] directories = externalLibraryPath.toFile().listFiles(File::isDirectory);
    if (directories != null) {
      for (File directory : directories) {
        try {
          var lib = new AddOnLibraryImporter().importFromDirectory(directory.toPath());
          registerExternalAddOnLibrary(lib);
        } catch (IOException e) {
          throw new RuntimeException(e); // TODO: CDW
        }
      }
    }
  }

  /**
   * Makes the add-on library with the specified namespace available to MapTool.
   *
   * @param namespace the namespace of the add-on library to make available.
   */
  public void importLibrary(String namespace) {
    if (isEnabled) {
      var lib = namespaceInfoMap.get(namespace);
      if (lib != null) {
        addOnLibraryManager.registerLibrary(lib.library());
      }
    }
  }

  /**
   * Stops the add-on library with the specified namespace from being available to MapTool.
   *
   * @return the namespace of the add-on library to stop being available.
   * @throws IOException if an error occurs.
   */
  private DirectoryWatcher createDirectoryWatcher() throws IOException {
    return DirectoryWatcher.builder()
        .path(externalLibraryPath)
        .listener(
            event -> {
              switch (event.eventType()) {
                case CREATE -> {
                  if (event.path().relativize(externalLibraryPath).getNameCount() == 1) {
                    registerExternalAddOnLibrary(event.path());
                  }
                }
                case DELETE -> {
                  if (event.path().relativize(externalLibraryPath).getNameCount() == 1) {
                    deregisterExternalAddOnLibrary(event.path().getFileName().toString());
                  }
                }
                case MODIFY -> {
                  if (event.path().relativize(externalLibraryPath).getNameCount() == 1) {
                    refreshExternalAddOnLibrary(event.path());
                  }
                }
              }
            })
        .build();
  }
}

// TODO: CDW changes to the add on library directory will not automatically update the add on
// library that MT uses
