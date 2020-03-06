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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.notebook.NoteBook;
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
  private final TreeItem<TableTreeItemHolder> root =
      new TreeItem<>(new NoteBookGroupTreeItem("Top Level"));

  /** The node to place all entries that dont belong to a map. */
  private final TreeItem<TableTreeItemHolder> noZoneParent =
      new TreeItem<>(new NoteBookGroupTreeItem("No Map"));

  /** Mapping between zones and the nodes that representing those zones in the tree. */
  private final Map<GUID, TreeItem<TableTreeItemHolder>> zoneNodeMap = new HashMap<>();

  /** The campaign {@link NoteBook} the create and maintain the tree for. */
  private final NoteBook noteBook;

  /** The {@link PropertyChangeListener} to listed to changes for the {@link NoteBook}. */
  private final PropertyChangeListener propertyChangeListener = this::synchronizeModelOnJFXThread;

  /** The group node to use when the {@link NoteBook} is empty. */
  private final TreeItem<TableTreeItemHolder> EMPTY_NOTE_BOOK =
      new TreeItem<>(new NoteBookGroupTreeItem("No Note Book Entries"));

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
    root.setExpanded(true);
  }

  /** Initializes the tree. */
  private void init() {
    assert Platform.isFxApplicationThread() : "init() must be run on the JavaFX thread.";

    initializeModel();
    if (root.getChildren().size() == 0) {
      root.getChildren().add(EMPTY_NOTE_BOOK);
    }
    noteBook.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Call to dispose of any resources that this {@code NoteBookTableTreeModel} is holding when you
   * no longer need it.
   *
   * @note This method is safe to run on any thread.
   */
  public void dispose() {
    // noteBook.removePropertyChangeListener is safe to run on any thread.
    noteBook.removePropertyChangeListener(propertyChangeListener);
  }

  /**
   * Returns the root node of the tree.
   *
   * @return the root node of the tree.
   * @throws IllegalStateException if not run on the JavaFX thread.
   */
  public TreeItem<TableTreeItemHolder> getRoot() {
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

    // remove and add all the entries
    removeEntries(noteBook.getEntries());
    addedEntries(noteBook.getEntries());
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

    /*
     * Always remove the EMPTY_NOTE_BOOK node upfront, we will re add later if it exists as it is
     * always added at the end if we have no nodes.
     */
    root.getChildren().remove(EMPTY_NOTE_BOOK);

    switch (event.getPropertyName()) {
      case NoteBook.ZONE_REMOVED_EVENT:
        {
          assert event.getOldValue() instanceof GUID : "Expected Zone Id on zone removed event.";
          TreeItem<TableTreeItemHolder> node = zoneNodeMap.get((GUID) event.getOldValue());
          node.getParent().getChildren().remove(node);
        }
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

    if (noZoneParent.getChildren().size() == 0) {
      root.getChildren().remove(noZoneParent);
    } else {
      if (!root.getChildren().contains(noZoneParent)) {
        root.getChildren().add(noZoneParent);
      }
    }

    if (root.getChildren().size() == 0) {
      root.getChildren().add(EMPTY_NOTE_BOOK);
    }
  }

  /**
   * Adds the {@link NoteBookEntry} that have been added to the {@link NoteBook} to the tree.
   *
   * @param added The {@link NoteBookEntry}s that have been added.
   */
  private void addedEntries(Set<NoteBookEntry> added) {
    assert Platform.isFxApplicationThread() : "addedEntries() must be run on the JavaFX thread.";

    TreeItem<TableTreeItemHolder> parentNode;
    for (NoteBookEntry entry : added) {
      if (entry.getZoneId().isPresent()) {
        GUID zoneId = entry.getZoneId().get();
        if (!zoneNodeMap.containsKey(zoneId)) {
          TreeItem<TableTreeItemHolder> node = new TreeItem<>(new NoteBookZoneTreeItem(zoneId));
          zoneNodeMap.put(zoneId, node);
          root.getChildren().add(node);
        }
        parentNode = zoneNodeMap.get(zoneId);
      } else {
        parentNode = noZoneParent;
      }

      TreeItem<TableTreeItemHolder> node = new TreeItem<>(new NoteBookEntryTreeItem(entry));
      parentNode.getChildren().add(node);
    }

    if (noZoneParent.getChildren().size() == 0 && root.getChildren().contains(noZoneParent)) {
      root.getChildren().remove(noZoneParent);
    } else {
      if (!root.getChildren().contains(noZoneParent)) {
        root.getChildren().add(noZoneParent);
      }
    }
  }

  /**
   * Removes {@link NoteBookEntry} that have been removed from the {@link NoteBook}.
   *
   * @param entries the {@link NoteBookEntry}s that were removed.
   */
  private void removeEntries(Set<NoteBookEntry> entries) {
    assert Platform.isFxApplicationThread() : "removeEntries() must be run on the JavaFX thread.";

    var toRemove = new HashMap<TreeItem<TableTreeItemHolder>, TreeItem<TableTreeItemHolder>>();
    for (NoteBookEntry entry : entries) {
      TreeItem<TableTreeItemHolder> parentNode;
      if (entry.getZoneId().isPresent()) {
        parentNode = zoneNodeMap.get(entry.getZoneId().get());
      } else {
        parentNode = noZoneParent;
      }

      /*
       * First collate the entries that were removed into a set that we can use to remove them
       * from the tree later on to avoid concurrent modification exceptions.
       */
      if (parentNode != null) {
        for (var node : parentNode.getChildren()) {
          if (node.getValue() instanceof NoteBookEntryTreeItem) {
            var item = (NoteBookEntryTreeItem) node.getValue();
            if (item.getEntry().getId().equals(entry.getId())) {
              toRemove.put(parentNode, node);
            }
          }
        }
      }
    }

    for (var remove : toRemove.entrySet()) {
      remove.getKey().getChildren().remove(remove.getValue());
    }
  }
}
