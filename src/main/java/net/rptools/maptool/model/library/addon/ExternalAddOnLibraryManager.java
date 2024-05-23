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

import io.methvin.watcher.DirectoryWatcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import net.rptools.maptool.model.library.LibraryInfo;

public class ExternalAddOnLibraryManager {

  /** The add-on library manager that is used to register the add-on libraries. */
  private final AddOnLibraryManager addOnLibraryManager;

  /** The add-on libraries that are registered. */
  private final Map<String, AddOnLibrary> namespaceLibraryMap = new ConcurrentHashMap<>();

  /** Cache of the library info for the namespace. */
  private final Map<String, LibraryInfo> namespaceLibraryInfoMap = new ConcurrentHashMap<>();

  /** Cache of external library info. */
  private final List<LibraryInfo> externalLibraryInfo;

  /** Is the external add-on library manager enabled. */
  private boolean isEnabled = false;

  /** The path to the external add-on libraries. */
  private Path externalLibraryPath = null;

  /** Directory watcher for watching the external add-on library directory. */
  private DirectoryWatcher directoryWatcher;

  /** Lock for managing enabled and path states. */
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Creates a new instance of the external add-on library manager.
   *
   * @param addOnLibraryManager the add-on library manager used to register the add-on libraries.
   */
  public ExternalAddOnLibraryManager(AddOnLibraryManager addOnLibraryManager) {
    this.addOnLibraryManager = addOnLibraryManager;
    externalLibraryInfo = Collections.synchronizedList(new ArrayList<>());
  }

  public void registerExternalAddOnLibrary(AddOnLibrary addOnLibrary) {
    addOnLibrary
        .getNamespace()
        .thenAccept(
            namespace -> {
              if (addOnLibraryManager.isNamespaceRegistered(namespace)) {
                addOnLibraryManager.deregisterLibrary(namespace);
              }
              namespaceLibraryMap.put(namespace, addOnLibrary);
              addOnLibraryManager.registerLibrary(addOnLibrary);
              addOnLibrary
                  .getLibraryInfo()
                  .thenAccept(
                      libraryInfo -> {
                        namespaceLibraryInfoMap.put(namespace, libraryInfo);
                      });
              cacheExternalLibraryInfo();
            });
  }

  public void deregisterExternalAddOnLibrary(AddOnLibrary addOnLibrary) {
    addOnLibrary
        .getNamespace()
        .thenAccept(
            namespace -> {
              namespaceLibraryMap.remove(namespace);
              namespaceLibraryMap.remove(namespace);
              addOnLibrary.getLibraryInfo().thenAccept(externalLibraryInfo::remove);
              addOnLibraryManager.deregisterLibrary(namespace);
              cacheExternalLibraryInfo();
            });
  }

  private void cacheExternalLibraryInfo() {
    externalLibraryInfo.clear();
    var infoList =
        namespaceLibraryInfoMap.values().stream()
            .sorted((li1, li12) -> li1.name().compareToIgnoreCase(li12.name()))
            .toList();
    externalLibraryInfo.addAll(infoList);
  }

  public void refreshExternalAddOnLibrary(String namespace, Path path) {
    try {
      var lib = new AddOnLibraryImporter().importFromDirectory(path);
      addOnLibraryManager.replaceLibrary(lib);
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO: CDW
    }
  }

  public List<LibraryInfo> getLibraries() {
    return externalLibraryInfo;
  }

  public boolean isEnabled() {
    try {
      lock.lock();
      return isEnabled;
    } finally {
      lock.unlock();
    }
  }

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

  public Path getExternalLibraryPath() {
    try {
      lock.lock();
      return externalLibraryPath;
    } finally {
      lock.unlock();
    }
  }

  public void setExternalLibraryPath(Path path) {
    if (path != null && path.equals(externalLibraryPath)) {
      return;
    }
    try {
      lock.lock();
      externalLibraryPath = path;
      stopWatching();
      if (path == null) {
        isEnabled = false;
      } else {
        startWatching();
      }
    } finally {
      lock.unlock();
    }
  }

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

  private void startWatching() {
    try {
      lock.lock();
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

  private DirectoryWatcher createDirectoryWatcher() throws IOException {
    return DirectoryWatcher.builder()
        .path(externalLibraryPath)
        .listener(
            event -> {
              switch (event.eventType()) {
                case CREATE -> {
                  if (event.path().relativize(externalLibraryPath).getNameCount() == 1) {
                    try {
                      var lib = new AddOnLibraryImporter().importFromDirectory(event.path());
                      registerExternalAddOnLibrary(lib);
                    } catch (IOException e) {
                      throw new RuntimeException(e); // TODO: CDW
                    }
                  }
                }
                case DELETE -> {
                  if (event.path().relativize(externalLibraryPath).getNameCount() == 1) {
                    deregisterExternalAddOnLibrary(
                        namespaceLibraryMap.get(event.path().getFileName().toString()));
                  }
                }
                case MODIFY -> {
                  if (event.path().relativize(externalLibraryPath).getNameCount() == 1) {
                    refreshExternalAddOnLibrary(
                        event.path().getFileName().toString(), event.path());
                  }
                }
              }
            })
        .build();
  }
}
