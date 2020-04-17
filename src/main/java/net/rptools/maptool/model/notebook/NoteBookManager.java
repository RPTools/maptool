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
package net.rptools.maptool.model.notebook;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** This class manages all the {@link NoteBook}s loaded. */
public final class NoteBookManager {

  /**
   * Name of event fired when a {@link NoteBook} are added. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = a {@link Set} of any {@link NoteBook}s that were replaced by {@code
   *       newValue}.
   *   <li>{@code newValue} = a {@link Set} of {@link NoteBook}s that were added.
   * </ul>
   */
  public static final String NOTE_BOOKS_ADDED = "Note Book Added";

  /**
   * Name of event fired when a {@link NoteBook}s are removed. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = a {@link Set} of {@link NoteBook}s that were removed.
   *   <li>{@code newValue} = null.
   * </ul>
   */
  public static final String NOTE_BOOKS_REMOVED = "Note Book Removed";

  /** {@link PropertyChangeListener} for listening to {@link NoteBook} changes. */
  private final PropertyChangeListener propertyChangeListener = this::noteBookChanged;

  /**
   * The {@link Map} used to map versioned names spaces to {@link NoteBook}s.
   *
   * @see NoteBook#getVersionedNameSpace()
   */
  private final Map<String, NoteBook> noteBooks = new ConcurrentHashMap<>();

  /**
   * {@link PropertyChangeSupport} object for registering {@link PropertyChangeListener}s and firing
   * events.
   */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * Returns all the {@link NoteBook}s being managed.
   *
   * @return all the {@link NoteBook}s being managed.
   */
  public NavigableSet<NoteBook> getNoteBooks() {
    return new TreeSet<>(noteBooks.values());
  }

  /**
   * Returns the {@code NoteBook} for the specified versioned name space.
   *
   * @param versionedNameSpace the versioned name space.
   * @return the {@code NoteBook} for the versioned name space.
   * @see NoteBook#getVersionedNameSpace()
   */
  public NoteBook getVersionedNoteBook(String versionedNameSpace) {
    Objects.requireNonNull(versionedNameSpace, "versioned name space can not be null.");
    return noteBooks.get(versionedNameSpace);
  }

  /**
   * Returns if there is a {@link NoteBook} for the specified versioned name space.
   *
   * @param versionedNameSpace the versioned name space to check.
   * @return {@code true} if there is a {@code NoteBook} for the versioned name space.
   */
  public boolean hasVersionedNameSpace(String versionedNameSpace) {
    Objects.requireNonNull(versionedNameSpace, "versioned name space can not be null.");
    return noteBooks.containsKey(versionedNameSpace);
  }

  /**
   * Adds a {@link NoteBook} to be managed if no {@link NoteBook} already exists with the same
   * versioned name space. If a {@link NoteBook} already exists with the same versioned name space
   * it will not be added and the existing {@link NoteBook} will be returned.
   *
   * @param noteBook The {@link NoteBook} to add.
   * @return {@code null} if no {@link NoteBook} existed with the same versioned name space and this
   *     one was added, or the existing {@code NoteBook} with the same versioned name space (and the
   *     new one will not be added).
   * @see NoteBook#getVersionedNameSpace()
   */
  public NoteBook addNoteBook(NoteBook noteBook) {
    Objects.requireNonNull(noteBook, "added note book can not be null.");
    NoteBook existing;
    String nameSpace = noteBook.getVersionedNameSpace();
    existing = noteBooks.get(nameSpace);
    noteBooks.put(nameSpace, noteBook);

    noteBook.addPropertyChangeListener(propertyChangeListener);

    if (existing != null) {
      existing.removePropertyChangeListener(propertyChangeListener);
      propertyChangeSupport.firePropertyChange(
          NOTE_BOOKS_ADDED, Set.of(existing), Set.of(noteBook));
    } else {
      propertyChangeSupport.firePropertyChange(NOTE_BOOKS_ADDED, Set.of(), Set.of(noteBook));
    }
    return existing;
  }

  /**
   * Returns all of the {@link NoteBook}s with the specified name space (non versioned).
   *
   * @param nameSpace the name space to get the {@link NoteBook}s for.
   * @return all of the {@link NoteBook}s with the specified name space (non versioned).
   * @see NoteBook#setNamespace(String)
   */
  public NavigableSet<NoteBook> getNoteBooks(String nameSpace) {
    return noteBooks.values().stream()
        .filter(n -> n.getNamespace().equals(nameSpace))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Removes a {@code NoteBook}.
   *
   * @param noteBook the {@link NoteBook} to remove.
   */
  public void removeNoteBook(NoteBook noteBook) {
    Objects.requireNonNull(noteBook, "removed note book can not be null.");

    noteBook.removePropertyChangeListener(propertyChangeListener);
    String nameSpace = noteBook.getVersionedNameSpace();
    if (noteBooks.remove(nameSpace, noteBook)) {
      propertyChangeSupport.firePropertyChange(NOTE_BOOKS_REMOVED, Set.of(noteBook), null);
    }
  }

  /**
   * Called when a property changes on the {@link NoteBook}.
   *
   * @param event the {@link PropertyChangeEvent} for the change.
   */
  private void noteBookChanged(PropertyChangeEvent event) {
    assert event != null : "Property Change Event is null";
    if (event.getPropertyName().equals(NoteBook.VERSIONED_NAMESPACE_CHANGED)) {
      NoteBook noteBook = (NoteBook) event.getSource();
      noteBooks.remove(event.getOldValue().toString(), noteBook);
      noteBooks.put(event.getNewValue().toString(), noteBook);
    }
  }

  /**
   * Adds a {@link PropertyChangeListener}.
   *
   * @param pcl the {@link PropertyChangeListener} to add.
   */
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    propertyChangeSupport.addPropertyChangeListener(pcl);
  }

  /**
   * Removes a {@link PropertyChangeListener}.
   *
   * @param pcl the {@link PropertyChangeListener} to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    propertyChangeSupport.removePropertyChangeListener(pcl);
  }
}
