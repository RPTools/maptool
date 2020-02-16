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
import net.rptools.maptool.model.notebook.NoteBookEntry;

public final class NoteBookTableTreeModel {

  private final TreeItem<TableTreeItemHolder> root =
      new TreeItem<>(new NoteBookGroupTreeItem("Top Level"));
  private final TreeItem<TableTreeItemHolder> noZoneRoot =
      new TreeItem<>(new NoteBookGroupTreeItem("No Map"));
  private final Map<GUID, TreeItem<TableTreeItemHolder>> zoneNodeMap = new HashMap<>();
  private final NoteBook noteBook;
  private final PropertyChangeListener propertyChangeListener = this::synchronizeModelOnJFXThread;

  private final TreeItem<TableTreeItemHolder> EMPTY_NOTE_BOOK =
      new TreeItem<>(new NoteBookGroupTreeItem("No Note Book Entries"));

  public static NoteBookTableTreeModel getTreeModelFor(NoteBook noteBook) {
    var model = new NoteBookTableTreeModel(noteBook);
    model.init();

    return model;
  }

  private NoteBookTableTreeModel(NoteBook nBook) {
    noteBook = nBook;
    root.setExpanded(true);
  }

  private void init() {
    initializeModel();
    if (root.getChildren().size() == 0) {
      root.getChildren().add(EMPTY_NOTE_BOOK);
    }
    noteBook.addPropertyChangeListener(propertyChangeListener);
  }

  public void dispose() {
    noteBook.removePropertyChangeListener(propertyChangeListener);
  }

  public TreeItem<TableTreeItemHolder> getRoot() {
    return root;
  }

  public void synchronizeModelOnJFXThread(PropertyChangeEvent event) {
    Platform.runLater(() -> synchronizeModel(event));
  }

  private void initializeModel() {
    // remove and add all the entries
    removeEntries(noteBook.getEntries());
    addedEntries(noteBook.getEntries());
  }

  private void synchronizeModel(PropertyChangeEvent event) {
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

    if (noZoneRoot.getChildren().size() == 0) {
      root.getChildren().remove(noZoneRoot);
    } else {
      if (!root.getChildren().contains(noZoneRoot)) {
        root.getChildren().add(noZoneRoot);
      }
    }

    if (root.getChildren().size() == 0) {
      root.getChildren().add(EMPTY_NOTE_BOOK);
    }
  }

  private void addedEntries(Set<NoteBookEntry> added) {
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
        parentNode = noZoneRoot;
      }

      TreeItem<TableTreeItemHolder> node = new TreeItem<>(new NoteBookEntryTreeItem(entry));
      parentNode.getChildren().add(node);
    }
  }

  private void removeEntries(Set<NoteBookEntry> entries) {
    var toRemove = new HashMap<TreeItem<TableTreeItemHolder>, TreeItem<TableTreeItemHolder>>();
    for (NoteBookEntry entry : entries) {
      TreeItem<TableTreeItemHolder> parentNode;
      if (entry.getZoneId().isPresent()) {
        parentNode = zoneNodeMap.get(entry.getZoneId().get());
      } else {
        parentNode = noZoneRoot;
      }

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
