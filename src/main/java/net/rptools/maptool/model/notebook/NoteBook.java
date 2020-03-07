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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.notebook.entry.DirectoryEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.entry.tree.NoteBookEntryNode;

/**
 * {@code MapBookmarkManager} class is used to manage all the {@link NoteBookEntry}s in a campaign.
 * This class is thread safe so may be used from multiple threads.
 *
 * Taking out a lock on a {@code NoteBook} (e.g. {code syncronized(notebook) {}} will block changes
 * via
 * <ui>
 *  <li>{@link #setDescription(String)}</li>
 *  <li>{@link #setName(String)}></il>
 *  <li>{@link #setVersion(String)}</li>
 *  <li>{@link #setNamespace(String)} (String)}</li>
 * </ui>
 * while the lock is held and can be used when you require these to remain stable while performing
 * an operation.
 *
 * @implNote Maintaining the thread safety of this class depends on keeping to the following
 *     conventions when changing this class.
 *     <ul>
 *       <li>
 *         Whenever {@link #removedZones} or {@link #idEntryMap} are modified then a
 *         {@link #writeLock} must be obtained.
 *       </li>
 *       <li>
 *         Whenever {@link #removedZones} or {@link #idEntryMap} are read then a
 *         {@link #readLock} must be obtained.
 *       </li>
 *     </ul>
 *     Also do <strong>not</strong> fire the property change events while holding a lock.
 *
 *
 */
public class NoteBook implements Comparable<NoteBook> {


  /**
   * Name of event fired when a zone is removed.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = the id of the zone that was removed.
   *   <li>{@code newValue} = null.
   * </ul>
   */
  public static final String ZONE_REMOVED_EVENT = "Zone Removed";

  /**
   * Name of event fired when a entries are added.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = {@code Set<NoteBookEntry>} containing values that were replaced}.
   *   <li>{@code newValue} = {@code Set<NoteBookEntry>} containing the added values.
   * </ul>
   */
  public static final String ENTRIES_ADDED_EVENT = "Entries Added";

  /**
   * Name of event fired when entries are removed.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = {@code Set<NoteBookEntry>} containing the removed values.
   *   <li>{@code newValue} = null.
   * </ul>
   */
  public static final String ENTRIES_REMOVED_EVENT = "Entries Removed";

  /**
   * Name of event fired when the name of the {@code NoteBook} changes.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old name of the {@code NoteBook}.
   *   <li>{@code newValue} = The new name of the {@code NoteBook}.
   * </ul>
   */
  public static final String NAME_CHANGED = "Name Changed";

  /**
   * Name of event fired when the version of the {@code NoteBook} changes.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old version of the {@code NoteBook}.
   *   <li>{@code newValue} = The new version of the {@code NoteBook}.
   * </ul>
   */
  public static final String VERSION_CHANGED = "Version Changed";

  /**
   * Name of event fired when the namespace of the {@code NoteBook} changes.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old namespace of the {@code NoteBook}.
   *   <li>{@code newValue} = The new namespace of the {@code NoteBook}.
   * </ul>
   */
  public static final String NAMESPACE_CHANGED = "Namespace Changed";

  /**
   * Name of event fired when the description of the {@code NoteBook} changes.
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old description of the {@code NoteBook}.
   *   <li>{@code newValue} = The new description of the {@code NoteBook}.
   * </ul>
   */
  public static final String DESCRIPTION_CHANGED = "Description Changed";

  /**
   * Name of event fired when the versioned name space of the {@code NoteBook} changes.
   * This event will be fired when either the name space or teh version change
   * For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old versioned name space of the {@code NoteBook}.
   *   <li>{@code newValue} = The new versioned name space of the {@code NoteBook}.
   * </ul>
   */
  public static final String VERSIONED_NAMESPACE_CHANGED = "Versioned Namespace Changed";


  /**
   * Class used to hold a {@code MapBookEntry} and the path it is in the tree.
   */
  private static final class EntryDetails {
    /** The path that the {@code MapBookEntry} can be found in the tree. */
    private final String path;

    /** The {@code MapBookEntry}. */
    private final NoteBookEntry entry;

    /** The {@link GUID} zoneId for the {@link Zone} this entry is for. */
    private final GUID zoneId;
    /**
     * Creates a new {@code EntryPath} object.
     * @param path the path in the tree.
     * @param entry the {@code NoteBookEntry}.
     */
    public EntryDetails(String path, NoteBookEntry entry, GUID zoneId) {
      this.path = Objects.requireNonNull(path, "The path for EntryDetails cannot be null");
      this.entry = Objects.requireNonNull(entry, "The entry for EntryDetails cannot be null");
      this.zoneId = zoneId;
    }

    /**
     * Returns the path in the tree.
     * @return the path in the tree.
     */
    public String getPath() {
      return path;
    }

    /**
     * Returns the {@code NoteBookEntry}.
     * @return the {@code NoteBookEntry}.
     */
    public NoteBookEntry getEntry() {
      return entry;
    }

    /**
     * Returns the {@link GUID} zone id this {@code NoteBookEntry} has.
     * @return the {@link GUID} zone id this {@code NoteBookEntry} has.
     */
    public GUID getZoneId() {
      return zoneId;
    }
  }

  /**
   * The list of {@link Zone}s that have been removed so that we don't end up adding {@link
   * NoteBookEntry}s for {@link Zone}s that have been removed.
   */
  private final Set<GUID> removedZones = new HashSet<>();

  /** Mapping between id and {@link NoteBookEntry}. */
  private final Map<UUID, EntryDetails> idEntryMap = new HashMap<>();


  /** Provides property change support for the {@code NoteBook}. */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /** Lock used to ensure consistency of all collections. */
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  /**
   * The read lock for the collections, you <strong>must</strong> use this lock whenever you are
   * reading the values from {@link #removedZones}, {@link #idEntryMap} or {@link #root} tree.
   */
  private final Lock readLock = readWriteLock.readLock();

  /**
   * The write lock for the collections, you <strong>must</strong> use this lock whenever you are
   * modifying the contents for {@link #removedZones}, {@link #idEntryMap} or {@link #root} tree.
   *
   * @see #readLock
   */
  private final Lock writeLock = readWriteLock.writeLock();


  /** The name of the {@code NoteBook}. */
  private String name;

  /** The description of the {@code NoteBook}. */
  private String description;

  /** The version of the {@code NoteBook}. */
  private String version;

  /** The namespace of the {@code NoteBook}. */
  private String namespace;

  /** Is this an internal  MapTool {@code NoteBook}. */
  private final boolean internal;

  /** The root node for the note book entries. */
  private final NoteBookEntryNode root = new NoteBookEntryNode(new DirectoryEntry("/"));


  /**
   * Creates a new {@code NoteBook} object.
   * @param name The name of the {@code NoteBook}.
   * @param description The description of the {@code NoteBook}.
   * @param version The version of the {@code NoteBook}.
   * @param namespace The namespace of the {@code NoteBook}.
   * @param internal {@code true} if this ia an internal uneditable MapTool {@code NoteBook}.
   */
  private NoteBook(String name, String description, String version, String namespace, boolean internal) {
    this.name = name;
    this.description = description;
    this.version = version;
    this.namespace = namespace;
    this.internal = internal;
  }


  /**
   * Creates a new {@code NoteBook} object.
   * @param name The name of the {@code NoteBook}.
   * @param description The description of the {@code NoteBook}.
   * @param version The version of the {@code NoteBook}.
   * @param namespace The namespace of the {@code NoteBook}.
   *
   * @return a new {@code NoteBook} object.
   */
  public static NoteBook createNoteBook(String name, String description, String version, String namespace) {
    return new NoteBook(name, description, version, namespace, false);
  }


  /**
   * Sets the name for the {@code NoteBook}.
   * @param newName the name to set.
   */
  public void setName(String newName) {
    String oldName;
    synchronized (this) {
      if (name.equals(newName)) {
        return;
      }
      oldName = name;
      name = newName;
    }

    fireChangeEvent(NAME_CHANGED, oldName, newName);
  }

  /**
   * Sets the description for the {@code NoteBook}.
   * @param desc the description to set.
   */
  public void setDescription(String desc) {
    String oldDesc;
    synchronized (this) {
      if (description.equals(desc)) {
        return;
      }
      oldDesc = description;
      description = desc;
    }

    fireChangeEvent(DESCRIPTION_CHANGED, oldDesc, desc);
  }

  /**
   * Sets the version for the {@code NoteBook}
   * @param ver the version to set.
   */
  public void setVersion(String ver) {
    String oldVer;
    String oldvns;
    String newvns;
    synchronized (this) {
      if (version.equals(ver)) {
        return;
      }
      oldVer = version;
      oldvns = getVersionedNameSpace();
      version = ver;
      newvns = getVersionedNameSpace();
    }

    fireChangeEvent(VERSION_CHANGED, oldVer, ver);
    fireChangeEvent(VERSIONED_NAMESPACE_CHANGED, oldvns, newvns);
  }


  /**
   * Returns the name of the {@code NoteBook}.
   * @return the name of the {@code NoteBook}.
   */
  public synchronized String getName() {
    return name;
  }

  /**
   * Returns the description of the {@code NoteBook}.
   * @return the description of the {@code NoteBook}.
   */
  public synchronized String getDescription() {
    return description;
  }

  /**
   * Returns the version of the {@code NoteBook}.
   * @return the version of the {@code NoteBook}.
   */
  public synchronized String getVersion() {
    return version;
  }


  /**
   * Returns if this is an internal MapTool {@code NoteBook} or not.
   * @return  {@code true }if this is an internal MapTool {@code NoteBook}.
   */
  public boolean isInternal() {
    return internal;
  }


  /**
   * Sets the namespace of the {@code NoteBook}.
   * @param ns the name space of the {@code NoteBook}.
   */
  public void setNamespace(String ns) {
    String oldns;
    String oldvns;
    String newvns;
    synchronized (this) {
      if (namespace.equals(ns)) {
        return;
      }
      oldns = namespace;
      oldvns = getVersionedNameSpace();
      namespace = ns;
      newvns = getVersionedNameSpace();
    }

    fireChangeEvent(NAMESPACE_CHANGED, oldns, ns);
    fireChangeEvent(VERSIONED_NAMESPACE_CHANGED, oldvns, newvns);
  }


  /**
   * Returns the name space of this {@code NoteBook}.
   * @return the name space of this {@code NoteBook}.
   */
  public synchronized String getNamespace() {
    return namespace;
  }


  /**
   * Returns the versioned name space, which is a combination of the namespace and version of the
   * {@code NoteBook}.
   * @return the versioned name space.
   */
  public synchronized String getVersionedNameSpace() {
    return namespace + "/" + version;
  }


  /**
   * Adds or replaces a {@link NoteBookEntry} to the note book being managed. This will replace
   * <strong>any</strong> {@link NoteBookEntry} with the same id.
   *
   * @see NoteBookEntry#getId()
   * @param path  the path for the added {@link NoteBookEntry}.
   * @param entry the {@link NoteBookEntry} to add or replace.
   */
  public void putEntry(String path, NoteBookEntry entry) {
    putEntry(path, entry, true);
  }

  /**
   * Adds or replaces a {@link NoteBookEntry} to the note book being managed. This will replace
   * <strong>any</strong> {@link NoteBookEntry} with the same id. This version of the function
   * allows you to specify if the listeners should be notified of the change. If you pass {@code
   * false} to this you are expected to perform the notification, this is to support bulk updates.
   *
   * @see NoteBookEntry#getId()
   * @param path  the path for the added {@link NoteBookEntry}.
   * @param entry the {@link NoteBookEntry} to add or replace.
   * @param firePropertyChange {@code true} if listeners should be notified.
   */
  private void putEntry(String path, NoteBookEntry entry, boolean firePropertyChange) {
    writeLock.lock();
    EntryDetails oldEntry = idEntryMap.get(entry.getId());
    try {

      /*
       * Check to see if we are replacing an entry and if so remove it from the other maps, as
       * the reference or the zone may have changed.
       */
      if (oldEntry != null) {
        removeEntry(oldEntry.getEntry(), false);
      }

      /*
       * If the zone is in the list of zone that have been removed then drop the entry silently.
       * This is to avoid storing entries for zones that have already been removed.
       */
      if (entry.getZoneId().isPresent() && removedZones.contains(entry.getZoneId().get())) {
        return;
      }

      idEntryMap.put(entry.getId(), new EntryDetails(path, entry, entry.getZoneId().orElse(null)));
    } finally {
      writeLock.unlock();
    }

    if (firePropertyChange) {
      Set<NoteBookEntry> removed = new HashSet<>();
      if (oldEntry != null) {
        removed.add(oldEntry.getEntry());
      }
      fireChangeEvent(ENTRIES_ADDED_EVENT, removed, Set.of(entry));
    }
  }

  /**
   * Adds or replaces a {@link NoteBookEntry} to the note book being managed. This will replace
   * <strong>any</strong> {@link NoteBookEntry} with the same id.
   *
   * @see NoteBookEntry#getId()
   * @param entries the {@link NoteBookEntry}s to add or replace.
   */
  public void putEntries(Map<String, NoteBookEntry> entries) {
    Map<String, NoteBookEntry> added = new HashMap<>(entries);
    if (!added.isEmpty()) {
      Map<String, NoteBookEntry> oldEntries = new HashMap<>();
      writeLock.lock(); // Want to lock the whole transaction
      try {
        for (Entry<String, NoteBookEntry> entry : added.entrySet()) {
          if (idEntryMap.containsKey(entry.getValue().getId())) {
            EntryDetails entryPath = idEntryMap.get(entry.getValue().getId());
            oldEntries.put(entryPath.getPath(), entryPath.getEntry());
            putEntry(entryPath.getPath(), entryPath.getEntry(), false);
          }
        }
      } finally {
        writeLock.unlock();
      }
      fireChangeEvent(ENTRIES_ADDED_EVENT, oldEntries, added.values());
    }
  }

  /**
   * Returns the {@link NoteBookEntry}s being managed.
   *
   * @return the {@link NoteBookEntry}s being managed.
   */
  public Set<NoteBookEntry> getEntries() {
    readLock.lock();
    try {
      return idEntryMap.values().stream().map(EntryDetails::getEntry).collect(Collectors.toSet());
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Returns the {@link NoteBookEntry}s for a {@link Zone}.
   *
   * @param zone the {@link Zone} to return the {@link NoteBookEntry}s for.
   * @return the {@link NoteBookEntry}s for a {@link Zone}.
   */
  public Collection<NoteBookEntry> getZoneEntries(Zone zone) {
    return getZoneEntries(zone.getId());
  }

  /**
   * Returns a {@link Map} of {@link Zone}s and the {@link NoteBookEntry}s for the them. Entries
   * with no zone are in the {@code Map} with the key of {@code null}.
   *
   * @return a {@link Map} of {@link Zone}s and the {@link NoteBookEntry}s for the them.
   */
  public Map<GUID, Set<NoteBookEntry>> getEntriesByZone() {
    Map<GUID, Set<NoteBookEntry>> entries = new HashMap<>();
    readLock.lock();
    try {
      addToZoneMap(root, entries);
    } finally {
      readLock.unlock();
    }
    return entries;
  }


  /**
   * Add this nodes entries to the zone map, then all and child nodes entries.
   * @param node The node to add and recurse.
   * @param entries the zone entry map to add to.
   */
  private void addToZoneMap(NoteBookEntryNode node, Map<GUID, Set<NoteBookEntry>> entries) {
    NoteBookEntry entry = node.getEntry();
    if (entry.getZoneId().isPresent()) {
      entries.computeIfAbsent(entry.getZoneId().get(), k -> new HashSet<>());
      entries.get(entry.getZoneId().get()).add(entry);
    } else {
      entries.computeIfAbsent(null, k -> new HashSet<>());
      entries.get(null).add(entry);
    }

    for (NoteBookEntryNode child : node.getChildren()) {
      addToZoneMap(child, entries);
    }
  }

  /**
   * Returns the {@link NoteBookEntry}s for a {@link Zone}.
   *
   * @param zoneId the id of the {@link Zone} to return the {@link NoteBookEntry}s.
   * @return the {@link NoteBookEntry}s for a {@link Zone}.
   */
  public Set<NoteBookEntry> getZoneEntries(GUID zoneId) {
    Objects.requireNonNull(zoneId, "zoneId cannot be null for getZoneEntries");
    readLock.lock();
    try {
      return idEntryMap.values().stream().filter(e -> zoneId.equals(e.getZoneId())).map(
          EntryDetails::getEntry).collect(Collectors.toSet()
      );
    } finally{
      readLock.unlock();
    }
  }

  /**
   * Returns the {@link NoteBookEntry}s not attached to any {@link Zone}.
   *
   * @return the {@link NoteBookEntry}s not attached to any {@link Zone}.
   */
  public Collection<NoteBookEntry> getZoneLessEntries() {
    readLock.lock();
    try {
      return idEntryMap.values().stream().filter(e -> e.getZoneId() == null).map(
          EntryDetails::getEntry).collect(Collectors.toSet()
      );
    } finally{
      readLock.unlock();
    }
  }

  /**
   * Called when a {@link Zone} is removed from the campaign as we are no longer interested in any
   * of the notes associated with it at this point.
   *
   * @param zoneId the id of the {@link Zone} that has been removed.
   */
  public void zoneRemoved(GUID zoneId) {
    Objects.requireNonNull(zoneId, "zoneId cannot be null for zoneRemoved");
    writeLock.lock();
    try {
      Set<NoteBookEntry> zoneEntries = getZoneEntries(zoneId);

      removeEntries(zoneEntries);
      /*
       * add this to the list of Zones that have been removed as we do not want to add
       * any notes for Zones that have been removed, which could happen due to the perils
       * of multithreading.
       */
      removedZones.add(zoneId);
    } finally {
      writeLock.unlock();
    }

    fireChangeEvent(ZONE_REMOVED_EVENT, zoneId, null);
  }

  /**
   * Removes multiple {@link NoteBookEntry}s.
   * @param entries the entries to remove.
   */
  public void removeEntries(Set<NoteBookEntry> entries) {
    Set<NoteBookEntry> removed = new HashSet<>(entries);
    if (!removed.isEmpty()) {
      writeLock.lock();
      try {
        for (NoteBookEntry entry : removed) {
          removeEntry(entry, false);
        }
      } finally {
        writeLock.unlock();
      }

      fireChangeEvent(ENTRIES_REMOVED_EVENT, removed, null);
    }
  }

  /**
   * Removes a {@link NoteBookEntry} from the note book. This version of the function allows you to
   * specify if the listeners should be notified of the change. If you pass {@code false} to this
   * you are expected to perform the notification, this is to support bulk updates.
   *
   * @param entry The {@link NoteBookEntry} to remove.
   * @param firePropertyChange {@code true} if listeners should be notified.
   */
  private void removeEntry(NoteBookEntry entry, boolean firePropertyChange) {
    Objects.requireNonNull(entry, "entry canno tbe null for removeEntry");
    writeLock.lock();
    try {
      idEntryMap.remove(entry.getId());
    } finally {
      writeLock.unlock();
    }

    if (firePropertyChange) {
      fireChangeEvent(ENTRIES_REMOVED_EVENT, Set.of(entry), null);
    }
  }

  /**
   * Removes a {@link NoteBookEntry} from the note book.
   *
   * @param entry The {@link NoteBookEntry} to remove.
   */
  public void removeEntry(NoteBookEntry entry) {
    removeEntry(entry, true);
  }

  /**
   * Returns the {@link NoteBookEntry} for a certain id.
   *
   * @param id the id of the {@link NoteBookEntry} to retrieve.
   * @return the {@link NoteBookEntry} for the id.
   */
  public Optional<NoteBookEntry> getById(UUID id) {
    Objects.requireNonNull(id, "id passed to getById must no tbe null");
    readLock.lock();
    try {
      if (idEntryMap.containsKey(id)) {
        return Optional.of(idEntryMap.get(id).getEntry());
      } else {
        return Optional.empty();
      }
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Called when a {@link Zone} is added to the campaign.
   *
   * @param zoneId the id of the {@link Zone} that was added to the campaign.
   * @implNote {@code MapBookmarkManager} doesn't really care if a <strong>new</strong> {@link Zone}
   *     is added to the campaign but if it is a {@link Zone} that has previously been removed and
   *     is now being re-added then we need to make sure that the {@link Zone} is removed from
   *     {@link #removedZones} so new {@link NoteBookEntry}s can be added. There is no need to fire
   *     off a property change event as the changes are not visible to anyone.
   */
  public void zoneAdded(GUID zoneId) {
    Objects.requireNonNull(zoneId, "zoneId passed to zoneAdded cannot be null");
    writeLock.lock();
    try {
      /*
       * This is to deal with the unlikely case where a zone is removed and readded so we can
       * continue adding new notes.
       */
      removedZones.remove(zoneId);
    } finally {
      writeLock.unlock();
    }
  }

  /** Notify {@link PropertyChangeListener}s that the {@code NoteBook} has been changed. */
  private void fireChangeEvent(String eventName, Object oldValue, Object newValue) {
    assert !readWriteLock.isWriteLockedByCurrentThread()
        : "Cannot fire property change events while holding lock";
    propertyChangeSupport.firePropertyChange(eventName, oldValue, newValue);
  }

  /**
   * Adds a {@link PropertyChangeListener} to listen for changes to the {@code NoteBook}.
   *
   * @param propertyChangeListener the {@link PropertyChangeListener} to listen for changes.
   */
  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    // No need for locking a PropertyChangeSupport is thread safe.
    propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Removes a {@link PropertyChangeListener} from those listening for changes to the {@code
   * NoteBook}.
   *
   * @param propertyChangeListener the {@link PropertyChangeListener} to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    // No need for locking a PropertyChangeSupport is thread safe.
    propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
  }

  @Override
  public int compareTo(NoteBook o) {
    int res = Boolean.compare(o.isInternal(), isInternal()); // reversed as false < true
    if (res != 0) {
      return res;
    }

    res = getName().compareTo(o.getName());
    if (res != 0) {
      return res;
    }

    return getVersion().compareTo(getVersion());
  }
}
