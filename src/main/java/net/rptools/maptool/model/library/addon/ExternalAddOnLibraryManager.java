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
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.AddOnsAddedEvent;
import net.rptools.maptool.model.library.AddOnsRemovedEvent;
import net.rptools.maptool.model.library.ExternalAddonsUpdateEvent;
import net.rptools.maptool.model.library.LibraryInfo;

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
  private boolean enabled = false;

  /** The path to the external add-on libraries. */
  private Path externalLibraryPath = null;

  /** Is the external add-on library manager initialised. */
  private boolean initialised = false;

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

  /**
   * Initializes the external add-on library manager. It is safe to call {@see
   * setExternalLibraryPath(Path)} and {@see setEnabled(boolean)} before calling this method, but no
   * directory watching will occur until this method is called.
   *
   * @throws IOException if an error occurs.
   * @throws IllegalStateException if the external add-on library manager has already been
   *     initialised.
   */
  public void init() throws IOException {
    try {
      lock.lock();
      if (initialised) {
        throw new IllegalStateException("External add-on library manager already initialised");
      }
      initialised = true;
      startWatching();
    } finally {
      lock.unlock();
    }
    var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.register(this);
  }

  /**
   * Handles the event when an add-on library is added to MapTool.
   *
   * @param event the add-on library that was added to MapTool.
   */
  @Subscribe
  public void onLibraryAdded(AddOnsAddedEvent event) {
    boolean updated = false;
    for (LibraryInfo libraryInfo : event.addOns()) {
      var namespace = libraryInfo.namespace().toLowerCase();
      if (namespaceInfoMap.containsKey(namespace)) {
        var oldInfo = namespaceInfoMap.get(namespace);
        var newInfo =
            new ExternalLibraryInfo(
                namespace,
                oldInfo.libraryInfo(),
                false,
                true,
                oldInfo.backingDirectory(),
                externalLibraryPath.relativize(oldInfo.backingDirectory()).toString());
        namespaceInfoMap.put(namespace, newInfo);
        updated = true;
      }
    }
    if (updated) {
      new MapToolEventBus().getMainEventBus().post(new ExternalAddonsUpdateEvent());
    }
  }

  /**
   * Handles the event when an add-on library is removed from MapTool.
   *
   * @param event the add-on library that was removed from MapTool.
   */
  @Subscribe
  public void onLibraryRemoved(AddOnsRemovedEvent event) {
    boolean updated = false;
    for (LibraryInfo libraryInfo : event.addOns()) {
      var namespace = libraryInfo.namespace().toLowerCase();
      if (namespaceInfoMap.containsKey(namespace)) {
        var oldInfo = namespaceInfoMap.get(namespace);
        var newInfo =
            new ExternalLibraryInfo(
                namespace,
                oldInfo.libraryInfo(),
                true,
                false,
                oldInfo.backingDirectory(),
                externalLibraryPath.relativize(oldInfo.backingDirectory()).toString());
        namespaceInfoMap.put(namespace, newInfo);
        updated = true;
      }
    }
    if (updated) {
      new MapToolEventBus().getMainEventBus().post(new ExternalAddonsUpdateEvent());
    }
  }

  /**
   * Registers an external add-on library.
   *
   * @param info Information about the add-on library to register.
   */
  private void registerExternalAddOnLibrary(ExternalLibraryInfo info) {
    boolean isInstalled = addOnLibraryManager.isNamespaceRegistered(info.namespace());
    var externalInfo =
        new ExternalLibraryInfo(
            info.namespace(),
            info.libraryInfo(),
            true,
            isInstalled,
            info.backingDirectory(),
            externalLibraryPath.relativize(info.backingDirectory()).toString());
    namespaceInfoMap.put(info.namespace().toLowerCase(), externalInfo);
  }

  /**
   * Deregisters an external add-on library.
   *
   * @param path the backing path of the add-on library to deregister.
   */
  public void deregisterExternalAddOnLibrary(Path path) {
    namespaceInfoMap.values().stream()
        .filter(info -> info.backingDirectory().equals(path))
        .findFirst()
        .ifPresent(info -> namespaceInfoMap.remove(info.namespace().toLowerCase()));
    var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.post(new ExternalAddonsUpdateEvent());
  }

  /**
   * Refreshes an external add-on library.
   *
   * @param path the path to the add-on library.
   * @throws IOException if an error occurs.
   */
  public void refreshExternalAddOnLibrary(Path path) throws IOException {
    registerExternalAddOnLibrary(path); // Allows us to change behaviour later without breaking API
  }

  /**
   * Registers an external add-on library.
   *
   * @param path the path to the add-on library.
   * @throws IOException if an error occurs.
   */
  public void registerExternalAddOnLibrary(Path path) throws IOException {
    var lib = new AddOnLibraryImporter().getLibraryInfoFromDirectory(path);
    if (lib == null) {
      return;
    }
    boolean isInstalled = addOnLibraryManager.isNamespaceRegistered(lib.namespace());
    var info =
        new ExternalLibraryInfo(
            lib.namespace(),
            lib,
            false,
            isInstalled,
            path,
            externalLibraryPath.relativize(path).toString());
    registerExternalAddOnLibrary(info);
    var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.post(new ExternalAddonsUpdateEvent());
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
      return enabled;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the enabled state of the external add-on library manager.
   *
   * @param enabled the enabled state.
   * @throws IOException if an error occurs.
   */
  public void setEnabled(boolean enabled) throws IOException {
    try {
      lock.lock();
      if (this.enabled != enabled) {
        this.enabled = enabled;
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
   * @throws IOException if an error occurs.
   */
  public void setExternalLibraryPath(Path path) throws IOException {
    if (path != null && path.equals(externalLibraryPath)) {
      return;
    }
    try {
      lock.lock();
      externalLibraryPath = path;
      stopWatching();
      if (path != null && enabled) {
        startWatching();
      }
    } finally {
      lock.unlock();
    }
  }

  /** Stops watching the external add-on library directory. */
  private void stopWatching() throws IOException {
    try {
      lock.lock();
      if (directoryWatcher != null) {
        directoryWatcher.close();
        directoryWatcher = null;
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Starts watching the external add-on library directory.
   *
   * @throws IOException if an error occurs.
   */
  private void startWatching() throws IOException {
    try {
      lock.lock();
      refreshAll();
      if (enabled && externalLibraryPath != null && Files.exists(externalLibraryPath)) {
        if (directoryWatcher != null) {
          directoryWatcher.watchAsync();
        } else {
          directoryWatcher = createDirectoryWatcher();
          directoryWatcher.watchAsync();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Refreshes all the external add-on libraries.
   *
   * @throws IOException if an error occurs.
   */
  private void refreshAll() throws IOException {
    if (!initialised || !enabled || externalLibraryPath == null) {
      return;
    }
    File[] directories = externalLibraryPath.toFile().listFiles(File::isDirectory);
    if (directories != null) {
      for (File directory : directories) {
        try {
          registerExternalAddOnLibrary(directory.toPath());
        } catch (IOException e) {
          MapTool.showError(I18N.getText("library.dialog.read.failed", directory));
        }
      }
    }
    var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.post(new ExternalAddonsUpdateEvent());
  }

  /**
   * Makes the add-on library with the specified namespace available to MapTool.
   *
   * @param namespace the namespace of the add-on library to make available.
   */
  public void importLibrary(String namespace) throws IOException {
    if (enabled) {
      var libInfo = namespaceInfoMap.get(namespace.toLowerCase());
      if (libInfo != null) {
        var lib = new AddOnLibraryImporter().importFromDirectory(libInfo.backingDirectory());
        addOnLibraryManager.registerLibrary(lib);
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
              try {
                int basePathNameCount = externalLibraryPath.getNameCount();
                var path = event.path();
                if (path.getNameCount() <= basePathNameCount) {
                  return;
                }
                if (!path.startsWith(externalLibraryPath)) {
                  return;
                }
                path = externalLibraryPath.resolve(path.getName(basePathNameCount));
                switch (event.eventType()) {
                  case CREATE -> registerExternalAddOnLibrary(path);
                  case DELETE -> {
                    if (path.toFile().exists()) {
                      refreshExternalAddOnLibrary(path);
                    } else {
                      deregisterExternalAddOnLibrary(path);
                    }
                  }
                  case MODIFY -> refreshExternalAddOnLibrary(path);
                }
              } catch (IOException e) {
                MapTool.showError(I18N.getText("library.dialog.read.failed", event.path()));
              }
            })
        .build();
  }
}
