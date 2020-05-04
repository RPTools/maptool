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
package net.rptools.maptool.model.notebook.tabletreemodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import net.rptools.maptool.model.notebook.NoteBook;
import net.rptools.maptool.model.notebook.entry.DirectoryEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;

/**
 * A model class used for creating and maintaining a {@link TreeItem} for the campaign note book.
 *
 * @note this class should only be called on the JavaFX thread, with the exception of {@link
 *     #synchronizeModelOnJFXThread} method which ensures that it is running on the correct thread
 *     so that it can be safely used from any listener on any thread.
 */
public final class NoteBookTableTreeModel {

  /** The root node of the tree. */
  private final TreeItem<NoteBookEntry> root;

  /** The {@link NoteBook} this tree represents. */
  private final NoteBook noteBook;

  /** The {@link PropertyChangeListener} for listening to changes. */
  private final PropertyChangeListener propertyChangeListener = this::synchronizeModelOnJFXThread;

  /** Maps {@link NoteBookEntry}s to the tree item. */
  private final Map<UUID, TreeItem<NoteBookEntry>> entryTreeItemMap = new HashMap<>();

  /**
   * Returns a {@code NoteBookTableTreeModel} for a campaign {@link NoteBook}.
   *
   * @param noteBook the {@link NoteBook} this model will represent.
   * @return a {@code NoteBookTableTreeModel} representing the campaign {@link NoteBook}.
   * @throws IllegalStateException if not run on the JavaFX thread.
   */
  public static NoteBookTableTreeModel getTreeModelFor(NoteBook noteBook) {
    if (!Platform.isFxApplicationThread()) {
      throw new IllegalStateException(
          "NoteBookTableTreeModel.getTreeModelFor() must be run on the JavaFX thread.");
    }
    var model = new NoteBookTableTreeModel(noteBook);
    model.init();

    return model;
  }

  /**
   * Creates a new {@code NoteBookTableTreeModel}.
   *
   * @param nBook the campaign {@link NoteBook} this represents.
   */
  private NoteBookTableTreeModel(NoteBook nBook) {
    assert Platform.isFxApplicationThread()
        : "NoteBookTableTreeModel() must be run on the JavaFX thread.";

    noteBook = nBook;
    root = new TreeItem<>(new DirectoryEntry("/"));
    root.setExpanded(true);
  }

  /** Initializes the tree. */
  private void init() {
    assert Platform.isFxApplicationThread() : "init() must be run on the JavaFX thread.";

    initializeModel();
    noteBook.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Call to dispose of any resources that this {@code NoteBookTableTreeModel} is holding when you
   * no longer need it.
   *
   * @note This method is safe to run on any thread.
   */
  public void dispose() {
    noteBook.removePropertyChangeListener(propertyChangeListener);
  }

  /**
   * Returns the root node of the tree.
   *
   * @return the root node of the tree.
   * @throws IllegalStateException if not run on the JavaFX thread.
   */
  public TreeItem<NoteBookEntry> getRoot() {
    if (!Platform.isFxApplicationThread()) {
      throw new IllegalStateException(
          "NoteBookTableTreeModel.getTreeModelFor() must be run on the JavaFX thread.");
    }
    return root;
  }

  /**
   * The method to be called when the campaign {@link NoteBook} changes. This runs {@link
   * #synchronizeModel(PropertyChangeEvent)} on the JavaFX thread.
   *
   * @param event The {@link PropertyChangeEvent} from the campaign {@link NoteBook}.
   * @note this method is safe to run on any thread.
   */
  private void synchronizeModelOnJFXThread(PropertyChangeEvent event) {
    Platform.runLater(() -> synchronizeModel(event));
  }

  /** Initializes the model and tree structures. */
  private void initializeModel() {
    assert Platform.isFxApplicationThread() : "initializeModel() must be run on the JavaFX thread.";

    // remove and add all the entries, first get a copy of all the entries to delete.
    Set<NoteBookEntry> toRemove =
        entryTreeItemMap.keySet().stream()
            .map(k -> entryTreeItemMap.get(k).getValue())
            .collect(Collectors.toSet());
    removeEntries(toRemove);

    addedEntries(noteBook.getEntries());
  }

  /**
   * Adds a {@link NoteBookEntry} to the correct place in the tree.
   *
   * @param entry the {@link NoteBookEntry} to add.
   * @throws
   * @implNote when adding several entries make sure you add them in a way that all the directories
   *     are created before any children in those directories or you will get an {@link
   *     IllegalStateException}.
   * @throws IllegalStateException if you try to add an entry to a non existent path.
   */
  private void addEntry(NoteBookEntry entry) {
    assert Platform.isFxApplicationThread() : "initializeModel() must be run on the JavaFX thread.";
    String[] parts = entry.getPath().split("/");
    String parentPath = String.join("/", Arrays.copyOfRange(parts, 0, parts.length - 1));
    TreeItem<NoteBookEntry> parent = getNodeFromPath(parentPath);
    if (parent == null) {
      throw new IllegalStateException("Parent path " + parentPath + " not found.");
    }
    TreeItem<NoteBookEntry> newNode = new TreeItem<>(entry);
    parent.getChildren().add(newNode);
    entryTreeItemMap.put(entry.getId(), newNode);
  }

  /**
   * Returns the {@link TreeItem} node for a given path.
   *
   * @param path the path as a string with "/" to get the {@link TreeItem} node for.
   * @return the {@link TreeItem} node for a given path.
   */
  private TreeItem<NoteBookEntry> getNodeFromPath(String path) {
    assert Platform.isFxApplicationThread() : "getNodeFromPath() must be run on the JavaFX thread.";
    if (path.isEmpty()) {
      return root;
    } else {
      String[] paths = path.split("/");
      if (paths.length > 0 && paths[0].isEmpty()) {
        paths = Arrays.copyOfRange(paths, 1, paths.length);
      }
      return getNodeFromPath(paths, root);
    }
  }

  /**
   * Returns the {@link TreeItem} node for a given path.
   *
   * @param paths an array of path strings to get the {@link TreeItem} node for.
   * @param node The node to begin searching from.
   * @return the {@link TreeItem} corresponding to the path.
   */
  private TreeItem<NoteBookEntry> getNodeFromPath(String[] paths, TreeItem<NoteBookEntry> node) {
    assert Platform.isFxApplicationThread() : "getNodeFromPath() must be run on the JavaFX thread.";
    if (paths.length == 0) {
      return node;
    }

    for (TreeItem<NoteBookEntry> child : node.getChildren()) {
      NoteBookEntry entry = child.getValue();
      if (entry instanceof DirectoryEntry && entry.getName().equals(paths[0])) {
        if (paths.length == 1) {
          return child;
        } else {
          return getNodeFromPath(Arrays.copyOfRange(paths, 1, paths.length), child);
        }
      }
    }

    return null;
  }

  /**
   * Synchronizes the model with the changes coming from the {@link NoteBook}. This should not be
   * called directly instead ig you want to register this class with a {@link
   * PropertyChangeListener} you should register the {@link
   * #synchronizeModelOnJFXThread(PropertyChangeEvent)} which will ensure that this is run on the
   * correct thread.
   *
   * @param event The {@link PropertyChangeEvent} coming from the {@link NoteBook}.
   */
  private void synchronizeModel(PropertyChangeEvent event) {

    assert Platform.isFxApplicationThread()
        : "synchronizeModel() must be run on the JavaFX thread.";

    switch (event.getPropertyName()) {
      case NoteBook.ZONE_REMOVED_EVENT:
        /*
         * No need to do anything here, if entries are removed due to removed zone we will deal
         * with them in the ENTRIES_REMOVED_EVENT
         */
        break;
      case NoteBook.ENTRIES_ADDED_EVENT:
        {
          assert event.getOldValue() instanceof Set : "Expected Set of NoteBookEntry for oldValue.";
          assert event.getNewValue() instanceof Set : "Expected Set of NoteBookEntry for newValue.";
          @SuppressWarnings("unchecked")
          var removed = (Set<NoteBookEntry>) event.getOldValue();
          @SuppressWarnings("unchecked")
          var added = (Set<NoteBookEntry>) event.getNewValue();

          removeEntries(removed);
          addedEntries(added);
        }
        break;
      case NoteBook.ENTRIES_REMOVED_EVENT:
        {
          assert event.getOldValue() instanceof Set : "Expected Set of NoteBookEntry for oldValue.";
          @SuppressWarnings("unchecked")
          var removed = (Set<NoteBookEntry>) event.getOldValue();
          removeEntries(removed);
        }
        break;
    }
  }

  /**
   * Adds the {@link NoteBookEntry} that have been added to the {@link NoteBook} to the tree.
   *
   * @param added The {@link NoteBookEntry}s that have been added.
   */
  private void addedEntries(Set<NoteBookEntry> added) {
    assert Platform.isFxApplicationThread() : "addedEntries() must be run on the JavaFX thread.";

    Set<NoteBookEntry> entries = new HashSet<>(added);
    /*
     * First directories in directory order to ensure all our directories are created before
     * the entries that belong in the direct ores.
     */
    Set<NoteBookEntry> directories =
        new TreeSet<>(
            (e1, e2) -> {
              long level1 = e1.getName().chars().filter(c -> c == '/').count();
              long level2 = e2.getName().chars().filter(c -> c == '/').count();
              int comp = Long.compareUnsigned(level1, level2);
              if (comp != 0) {
                return comp;
              } else {
                return e1.getName().compareTo(e2.getName());
              }
            });
    directories.addAll(
        entries.stream().filter(e -> e instanceof DirectoryEntry).collect(Collectors.toSet()));
    for (NoteBookEntry dir : directories) {
      addEntry(dir);
    }

    // Add the remaining entries, order should not matter as we have all the directories already
    entries.removeAll(directories);

    for (NoteBookEntry entry : entries) {
      addEntry(entry);
    }
  }

  /**
   * Removes {@link NoteBookEntry} that have been removed from the {@link NoteBook}.
   *
   * @param entries the {@link NoteBookEntry}s that were removed.
   */
  private void removeEntries(Set<NoteBookEntry> entries) {
    assert Platform.isFxApplicationThread() : "removeEntries() must be run on the JavaFX thread.";

    Set<NoteBookEntry> removed = new HashSet<>(entries);
    for (NoteBookEntry entry : removed) {
      if (entryTreeItemMap.containsKey(entry.getId())) {
        TreeItem<NoteBookEntry> node = entryTreeItemMap.get(entry.getId());
        TreeItem<NoteBookEntry> parentNode = node.getParent();
        if (parentNode != null) {
          parentNode.getChildren().remove(node);
        }
        entryTreeItemMap.remove(entry.getId());
      }
    }
  }
}
