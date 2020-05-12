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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import net.rptools.maptool.client.ui.notebook.NoteBookDependency;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntryType;

/**
 * {@code MapBookmarkManager} class is used to manage all the {@code NoteBookEntry}s in a campaign.
 * This class is thread safe so may be used from multiple threads.
 *
 * <p>Taking out a lock on a {@code NoteBook} (e.g. {code syncronized(notebook) {}} will block
 * changes via <ui>
 * <li>{@link #setDescription(String)}
 * <li>{@link #setName(String)}></il>
 * <li>{@link #setVersion(String)}
 * <li>{@link #setNamespace(String)} (String)}
 * <li>{@link #setAuthor(String)} (String)}
 * <li>{@link #setLicense(String)} (String)}
 * <li>{@link #setURL(String)} (String)} </ui> while the lock is held and can be used when you
 *     require these to remain stable while performing an operation.
 *
 * @implNote Maintaining the thread safety of this class depends on keeping to the following
 *     conventions when changing this class.
 *     <ul>
 *       <li>Whenever {@link #removedZones} or {@link #idEntryMap} are modified then a {@link
 *           #writeLock} must be obtained.
 *       <li>Whenever {@link #removedZones} or {@link #idEntryMap} are read then a {@link #readLock}
 *           must be obtained.
 *     </ul>
 *     Also do <strong>not</strong> fire the property change events while holding a lock.
 */
public class NoteBook implements Comparable<NoteBook> {

  /**
   * Name of event fired when a zone is removed. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = the id of the zone that was removed.
   *   <li>{@code newValue} = null.
   * </ul>
   */
  public static final String ZONE_REMOVED_EVENT = "Zone Removed";

  /**
   * Name of event fired when a entries are added. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = {@code Set<NoteBookEntry>} containing values that were replaced}.
   *   <li>{@code newValue} = {@code Set<NoteBookEntry>} containing the added values.
   * </ul>
   */
  public static final String ENTRIES_ADDED_EVENT = "Entries Added";

  /**
   * Name of event fired when entries are removed. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = {@code Set<NoteBookEntry>} containing the removed values.
   *   <li>{@code newValue} = null.
   * </ul>
   */
  public static final String ENTRIES_REMOVED_EVENT = "Entries Removed";

  /**
   * Name of event fired when the name of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old name of the {@code NoteBook}.
   *   <li>{@code newValue} = The new name of the {@code NoteBook}.
   * </ul>
   */
  public static final String NAME_CHANGED = "Name Changed";

  /**
   * Name of event fired when the version of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old version of the {@code NoteBook}.
   *   <li>{@code newValue} = The new version of the {@code NoteBook}.
   * </ul>
   */
  public static final String VERSION_CHANGED = "Version Changed";

  /**
   * Name of event fired when the namespace of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old namespace of the {@code NoteBook}.
   *   <li>{@code newValue} = The new namespace of the {@code NoteBook}.
   * </ul>
   */
  public static final String NAMESPACE_CHANGED = "Namespace Changed";

  /**
   * Name of event fired when the description of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old description of the {@code NoteBook}.
   *   <li>{@code newValue} = The new description of the {@code NoteBook}.
   * </ul>
   */
  public static final String DESCRIPTION_CHANGED = "Description Changed";

  /**
   * Name of event fired when the versioned name space of the {@code NoteBook} changes. This event
   * will be fired when either the name space or teh version change For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old versioned name space of the {@code NoteBook}.
   *   <li>{@code newValue} = The new versioned name space of the {@code NoteBook}.
   * </ul>
   */
  public static final String VERSIONED_NAMESPACE_CHANGED = "Versioned Namespace Changed";

  /**
   * Name of event fired when the URL of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old URL of the {@code NoteBook}.
   *   <li>{@code newValue} = The new URL of the {@code NoteBook}.
   * </ul>
   */
  public static final String URL_CHANGED = "URL Changed";

  /**
   * Name of event fired when the author of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old author of the {@code NoteBook}.
   *   <li>{@code newValue} = The new author of the {@code NoteBook}.
   * </ul>
   */
  public static final String AUTHOR_CHANGED = "Author Changed";

  /**
   * Name of event fired when the license of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old license of the {@code NoteBook}.
   *   <li>{@code newValue} = The new license of the {@code NoteBook}.
   * </ul>
   */
  public static final String LICENSE_CHANGED = "License Changed";

  /**
   * Name of event fired when the read me of the {@code NoteBook} changes. For this event:
   *
   * <ul>
   *   <li>{@code oldValue} = The old read me of the {@code NoteBook}.
   *   <li>{@code newValue} = The new read me of the {@code NoteBook}.
   * </ul>
   */
  public static final String README_CHANGED = "Read Me Changed";

  /** Values that a namespace can not start with as they are reserved. */
  private static final String[] RESERVED_NAMESPACE_BEGINNING = {
    "maptool", "rptool", "mt.", "util.", "utils.", "tokentool", "sheet.", "character."
  };

  /** Regular Expression to check that namespace valid characters. */
  private static final String VALID_NAMESPACE_CHARS_REGEX = "[A-z0-9\\.-]+";

  /** Regular Expression to check that there are no sequential special characters. */
  private static final String INVALID_NAMESPACE_SPECIAL_CHAR_SEQ = ".*(\\.\\.|\\.-|-\\.).*";

  /**
   * Regular Expression to check that the version number is valid. This is the regular expression
   * for semantic versioning See https://semver.org/
   */
  private static final String VALID_VERSION_REGEX =
      "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

  /** Class used to hold a {@code MapBookEntry} and the path it is in the tree. */
  private static final class EntryDetails {
    /** The path that the {@code MapBookEntry} can be found in the tree. */
    private final String path;

    /** The {@code MapBookEntry}. */
    private final NoteBookEntry entry;

    /** The {@link GUID} zoneId for the {@link Zone} this entry is for. */
    private final GUID zoneId;

    /**
     * Creates a new {@code EntryPath} object.
     *
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
     *
     * @return the path in the tree.
     */
    public String getPath() {
      return path;
    }

    /**
     * Returns the {@code NoteBookEntry}.
     *
     * @return the {@code NoteBookEntry}.
     */
    public NoteBookEntry getEntry() {
      return entry;
    }

    /**
     * Returns the {@link GUID} zone id this {@code NoteBookEntry} has.
     *
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

  /** Mapping between id and {@code NoteBookEntry}. */
  private final Map<UUID, EntryDetails> idEntryMap = new HashMap<>();

  /** Mapping between the path and the id of an entry in the {@code NoteBook}. */
  private final Map<String, UUID> pathIdMap = new HashMap<>();

  /** Provides property change support for the {@code NoteBook}. */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /** Lock used to ensure consistency of all collections. */
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  /**
   * The read lock for the collections, you <strong>must</strong> use this lock whenever you are
   * reading the values from {@link #removedZones}, {@link #idEntryMap} or {@link #pathIdMap}.
   */
  private final Lock readLock = readWriteLock.readLock();

  /**
   * The write lock for the collections, you <strong>must</strong> use this lock whenever you are
   * modifying the contents for {@link #removedZones}, {@link #idEntryMap} or {@link #pathIdMap}.
   *
   * @see #readLock
   */
  private final Lock writeLock = readWriteLock.writeLock();

  /** The id of the {@code NoteBook}. */
  private final UUID id;

  /** The name of the {@code NoteBook}. */
  private String name;

  /** The description of the {@code NoteBook}. */
  private String description;

  /** The version of the {@code NoteBook}. */
  private String version;

  /** The namespace of the {@code NoteBook}. */
  private String namespace;

  /** Is this an internal MapTool {@code NoteBook}. */
  private final boolean internal;

  /** The author of the {@code NoteBook}. */
  private String author;

  /** The URL for the {@code NoteBook}. */
  private String URL;

  /** The required dependencies for the {@code NoteBook}. */
  private final Set<NoteBookDependency> dependencies = new HashSet<>();

  /** The license for the {@code NoteBook}. */
  private String license;

  /** The read me for the {@code NoteBook}. */
  private String readMe;

  /**
   * Returns the version error if there is one.
   *
   * @param version The version to check.
   * @return a {@link String} containing the error message or empty optional if there is no error.
   */
  public static Optional<String> versionError(String version) {
    if (version == null || version.trim().isEmpty()) {
      return Optional.of(I18N.getText("noteBooks.error.version.empty"));
    }

    if (!version.matches(VALID_VERSION_REGEX)) {
      return Optional.of(I18N.getText("noteBooks.error.version.invalidFormat"));
    }

    return Optional.empty();
  }

  /**
   * Return if the version has an error or not. The error can be retrieved with {@link
   * #versionError(String)}.
   *
   * @param version The version to check.
   * @return {@code true} if the version has an error, {@code false} otherwise.
   */
  public static boolean versionHasError(String version) {
    return versionError(version).isPresent();
  }

  /**
   * Returns the namespace error if there is one.
   *
   * @param namespace The namespace to check.
   * @return a {@link String} containing the error message or empty optional if there is no error.
   */
  public static Optional<String> nameSpaceError(String namespace) {
    if (namespace == null || namespace.isEmpty()) {
      return Optional.of(I18N.getText("noteBooks.error.namespace.cannotBeEmpty"));
    }

    // Check to make sure there is no white space.
    String withoutSpace = namespace.replaceAll("\\s", "");
    if (!withoutSpace.equals(namespace)) {
      return Optional.of(I18N.getText("noteBooks.error.namespace.noSpaces"));
    }

    // Check to make sure it does not begin with a reserved namespace
    String lowerNamespace = namespace.toLowerCase();
    for (String ns : RESERVED_NAMESPACE_BEGINNING) {
      if (lowerNamespace.startsWith(ns)) {
        return Optional.of(I18N.getText("noteBooks.error.namespace.invalidStart", ns));
      }
    }

    // Check that the namespace only contains valid characters.
    if (!namespace.matches(VALID_NAMESPACE_CHARS_REGEX)) {
      return Optional.of(
          I18N.getText("noteBooks.error.namespace.canOnlyContain", "a-z A-Z 0-9 . -"));
    }

    // Check that the namespace does not start or end with a - or .
    if (namespace.startsWith(".")
        || namespace.startsWith("-")
        || namespace.endsWith(".")
        || namespace.endsWith("-")) {
      return Optional.of(I18N.getText("noteBooks.error.namespace.invalidStartEnd", ". -"));
    }

    // Check that the namespace does not have two consecutive special characters.
    if (namespace.matches(INVALID_NAMESPACE_SPECIAL_CHAR_SEQ)) {
      return Optional.of(I18N.getText("noteBooks.error.namespace.consecutiveSpecial"));
    }

    // Check that there is at least one . in the namespace.
    long dotCount = namespace.chars().filter(ch -> ch == '.').count();
    if (dotCount == 0) {
      return Optional.of(I18N.getText("noteBooks.error.namespace.noDot"));
    }

    // Passed all checks so it is valid.
    return Optional.empty();
  }

  /**
   * Return if the namespace has an error or not. The error can be retrieved with {@link
   * #nameSpaceError(String)}.
   *
   * @param namespace The namespace to check.
   * @return {@code true} if the namespace has an error, {@code false} otherwise.
   */
  public static boolean nameSpaceHasError(String namespace) {
    Optional<String> error = nameSpaceError(namespace);
    return error.isPresent();
  }

  /**
   * Creates a new {@code NoteBook} object.
   *
   * @param id The id of the {@code NoteBook}.
   * @param name The name of the {@code NoteBook}.
   * @param description The description of the {@code NoteBook}.
   * @param version The version of the {@code NoteBook}.
   * @param namespace The namespace of the {@code NoteBook}.
   * @param author The author of the {@code NoteBook}.
   * @param license The license of the {@code NoteBook}.
   * @param readme The Read Me of the {@code NoteBook}.
   * @param url The url of the {@code NoteBook}.
   * @param internal {@code true} if this ia an internal uneditable MapTool {@code NoteBook}.
   */
  private NoteBook(
      UUID id,
      String name,
      String description,
      String version,
      String namespace,
      String author,
      String license,
      String url,
      String readme,
      boolean internal) {
    Objects.requireNonNull(id, "id cannot be null.");
    Objects.requireNonNull(name, "name cannot be null.");
    Objects.requireNonNull(name, "version cannot be null.");
    Objects.requireNonNull(name, "namespace cannot be null.");

    this.id = id;
    this.name = name;
    this.description = description != null ? description : "";
    this.version = version;
    this.namespace = namespace;
    this.author = author != null ? author : "";
    this.license = license != null ? license : "";
    this.readMe = readme != null ? readme : "";
    this.URL = url != null ? url : "";
    this.internal = internal;
  }

  /**
   * Creates a new {@code NoteBook} object.
   *
   * @param name The name of the {@code NoteBook}.
   * @param description The description of the {@code NoteBook}.
   * @param version The version of the {@code NoteBook}.
   * @param namespace The namespace of the {@code NoteBook}.
   * @param author The author of the {@code NoteBook}.
   * @param license The license of the {@code NoteBook}.
   * @param url The url of the {@code NoteBook}.
   * @param readme The readme of the {@code NoteBook}.
   * @return a new {@code NoteBook} object.
   */
  public static NoteBook createNoteBook(
      String name,
      String description,
      String version,
      String namespace,
      String author,
      String license,
      String url,
      String readme) {
    return new NoteBook(
        UUID.randomUUID(),
        name,
        description,
        version,
        namespace,
        author,
        license,
        url,
        readme,
        false);
  }

  /**
   * Creates a new {@code NoteBook} object.
   *
   * @param id The id of the {@code NoteBook}.
   * @param name The name of the {@code NoteBook}.
   * @param description The description of the {@code NoteBook}.
   * @param version The version of the {@code NoteBook}.
   * @param namespace The namespace of the {@code NoteBook}.
   * @param author The author of the {@code NoteBook}.
   * @param license The license of the {@code NoteBook}.
   * @param url The url of the {@code NoteBook}.
   * @param readme The readme of the {@code NoteBook}.
   * @return a new {@code NoteBook} object.
   */
  public static NoteBook createNoteBookWithId(
      UUID id,
      String name,
      String description,
      String version,
      String namespace,
      String author,
      String license,
      String url,
      String readme) {
    return new NoteBook(
        id, name, description, version, namespace, author, license, url, readme, false);
  }

  /**
   * Returns the id of the {@code NoteBook}.
   *
   * @return the id of the {@code NoteBook}.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the name for the {@code NoteBook}.
   *
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
   *
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
   *
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
   *
   * @return the name of the {@code NoteBook}.
   */
  public synchronized String getName() {
    return name;
  }

  /**
   * Returns the description of the {@code NoteBook}.
   *
   * @return the description of the {@code NoteBook}.
   */
  public synchronized String getDescription() {
    return description;
  }

  /**
   * Returns the version of the {@code NoteBook}.
   *
   * @return the version of the {@code NoteBook}.
   */
  public synchronized String getVersion() {
    return version;
  }

  /**
   * Returns if this is an internal MapTool {@code NoteBook} or not.
   *
   * @return {@code true }if this is an internal MapTool {@code NoteBook}.
   */
  public boolean isInternal() {
    return internal;
  }

  /**
   * Returns the author of the {@code NoteBook}.
   *
   * @return the author of the {@code NoteBook}.
   */
  public synchronized String getAuthor() {
    return author;
  }

  /**
   * Sets the author for the {@code NoteBook}.
   *
   * @param auth the author of the {@code NoteBook}.
   */
  public void setAuthor(String auth) {
    String oldAuthor;
    synchronized (this) {
      if (author.equals(auth)) {
        return;
      }
      oldAuthor = description;
      author = auth;
    }

    fireChangeEvent(AUTHOR_CHANGED, oldAuthor, auth);
  }

  /**
   * Returns the URL of the {@code NoteBook}.
   *
   * @return the URL of the {@code NoteBook}.
   */
  public synchronized String getURL() {
    return URL;
  }

  /**
   * Sets the URL for the {@code NoteBook}.
   *
   * @param newURL the URL of the {@code NoteBook}.
   */
  public void setURL(String newURL) {
    String oldURL;
    synchronized (this) {
      if (URL.equals(newURL)) {
        return;
      }
      oldURL = URL;
      URL = newURL;
    }

    fireChangeEvent(URL_CHANGED, oldURL, newURL);
  }

  /**
   * Returns the license of the {@code NoteBook}.
   *
   * @return the license of the {@code NoteBook}.
   */
  public synchronized String getLicense() {
    return license;
  }

  /**
   * Sets the read me for the {@code NoteBook}.
   *
   * @param newReadMe the license of the {@code NoteBook}.
   */
  public void setReadMe(String newReadMe) {
    String oldReadMe;
    synchronized (this) {
      if (readMe.equals(newReadMe)) {
        return;
      }
      oldReadMe = readMe;
      readMe = newReadMe;
    }

    fireChangeEvent(README_CHANGED, oldReadMe, newReadMe);
  }

  /**
   * Returns the read me of the {@code NoteBook}.
   *
   * @return the read me of the {@code NoteBook}.
   */
  public synchronized String getReadMe() {
    return readMe;
  }

  /**
   * Sets the license for the {@code NoteBook}.
   *
   * @param newLicense the license of the {@code NoteBook}.
   */
  public void setLicense(String newLicense) {
    String oldLicense;
    synchronized (this) {
      if (license.equals(newLicense)) {
        return;
      }
      oldLicense = license;
      license = newLicense;
    }

    fireChangeEvent(LICENSE_CHANGED, oldLicense, newLicense);
  }

  /**
   * Sets the namespace of the {@code NoteBook}.
   *
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
   *
   * @return the name space of this {@code NoteBook}.
   */
  public synchronized String getNamespace() {
    return namespace;
  }

  /**
   * Returns the versioned name space, which is a combination of the namespace and version of the
   * {@code NoteBook}.
   *
   * @return the versioned name space.
   */
  public synchronized String getVersionedNameSpace() {
    return namespace + "/" + version;
  }

  /**
   * Adds the specified dependency to the list of dependencies for this {@code NoteBook}.
   *
   * @param dependency The dependency to add.
   */
  public synchronized void putDependency(NoteBookDependency dependency) {
    dependencies.add(dependency);
  }

  /**
   * Returns the dependencies for this {@code NoteBook}.
   *
   * @return the dependencies for this {@code NoteBook}.
   */
  public synchronized Set<NoteBookDependency> getDependencies() {
    return new HashSet<>(dependencies);
  }

  /**
   * Removes the specified dependency from the {@link NoteBook}.
   *
   * @param dependency The dependency to add.
   */
  public synchronized void removeDependency(NoteBookDependency dependency) {
    dependencies.remove(namespace);
  }

  /**
   * Adds or replaces a {@code NoteBookEntry} to the note book being managed. This will replace
   * <strong>any</strong> {@code NoteBookEntry} with the same id.
   *
   * @see NoteBookEntry#getId()
   * @param entry the {@code NoteBookEntry} to add or replace.
   */
  public void putEntry(NoteBookEntry entry) {
    putEntry(entry, true);
  }

  /**
   * Adds or replaces a {@code NoteBookEntry} to the note book being managed. This will replace
   * <strong>any</strong> {@code NoteBookEntry} with the same id. This version of the function
   * allows you to specify if the listeners should be notified of the change. If you pass {@code
   * false} to this you are expected to perform the notification, this is to support bulk updates.
   *
   * <p>Since a path can only point to a single {@code NoteBookEntry} if the passed in {@link
   * NoteBookEntry} has the path of an existing entry then that existing entry will be removed.
   *
   * @see NoteBookEntry#getId()
   * @param entry the {@code NoteBookEntry} to add or replace.
   * @param firePropertyChange {@code true} if listeners should be notified.
   * @return a {@link Set<NoteBook>} containing any entries that were replaced.
   */
  private Set<NoteBookEntry> putEntry(NoteBookEntry entry, boolean firePropertyChange) {
    writeLock.lock();
    Set<NoteBookEntry> removed = new HashSet<>();
    try {

      /*
       * First remove any entry with the same path if its not the same id.
       */
      if (pathIdMap.containsKey(entry.getPath())) {
        UUID existingPathId = pathIdMap.get(entry.getPath());
        if (!entry.getId().equals(existingPathId)) {
          EntryDetails oldEntry = idEntryMap.get(existingPathId);
          removeEntry(oldEntry.getEntry(), false);
          removed.add(oldEntry.getEntry());
        }
      }

      /*
       * Check to see if we are replacing an entry and if so remove it from the other maps, as
       * the reference or the zone may have changed.
       */
      EntryDetails oldEntry = idEntryMap.get(entry.getId());
      if (oldEntry != null) {
        if (!oldEntry.getEntry().getType().equals(entry.getType())) {
          throw new IllegalStateException("Can only replace note book entry with same type");
        }
        removeEntry(oldEntry.getEntry(), false);
        removed.add(oldEntry.getEntry());
      }

      /*
       * If the zone is in the list of zone that have been removed then drop the entry silently.
       * This is to avoid storing entries for zones that have already been removed.
       */
      if (entry.getZoneId().isPresent() && removedZones.contains(entry.getZoneId().get())) {
        return new HashSet<>();
      }

      EntryDetails entryDetails =
          new EntryDetails(entry.getPath(), entry, entry.getZoneId().orElse(null));
      idEntryMap.put(entry.getId(), entryDetails);
      pathIdMap.put(entry.getPath(), entry.getId());

      // If there was a removed entry and it had children add them to the new entry
      if (oldEntry.getEntry().getType() == NoteBookEntryType.DIRECTORY) {}

    } finally {
      writeLock.unlock();
    }

    if (firePropertyChange) {
      fireChangeEvent(ENTRIES_ADDED_EVENT, Collections.unmodifiableSet(removed), Set.of(entry));
    }

    return removed;
  }

  /**
   * Adds or replaces a {@code NoteBookEntry} to the note book being managed. This will replace
   * <strong>any</strong> {@code NoteBookEntry} with the same id.
   *
   * @see NoteBookEntry#getId()
   * @param entries the {@code NoteBookEntry}s to add or replace.
   */
  public void putEntries(Collection<NoteBookEntry> entries) {
    if (!entries.isEmpty()) {
      Set<NoteBookEntry> oldEntries = new HashSet<>();
      writeLock.lock();
      try {
        for (NoteBookEntry entry : entries) {
          Set<NoteBookEntry> removed = putEntry(entry, false);
          oldEntries.addAll(removed);
        }
      } finally {
        writeLock.unlock();
      }
      fireChangeEvent(ENTRIES_ADDED_EVENT, oldEntries, new HashSet<>(entries));
    }
  }

  /**
   * Returns the {@code NoteBookEntry}s being managed.
   *
   * @return the {@code NoteBookEntry}s being managed.
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
   * Returns the {@code NoteBookEntry}s for a {@link Zone}.
   *
   * @param zone the {@link Zone} to return the {@code NoteBookEntry}s for.
   * @return the {@code NoteBookEntry}s for a {@link Zone}.
   */
  public Collection<NoteBookEntry> getZoneEntries(Zone zone) {
    return getZoneEntries(zone.getId());
  }

  /**
   * Returns a {@link Map} of {@link Zone}s and the {@code NoteBookEntry}s for them. Entries with no
   * zone are in the {@code Map} with the key of {@code null}.
   *
   * @return a {@link Map} of {@link Zone}s and the {@code NoteBookEntry}s for the them.
   */
  public Map<GUID, Set<NoteBookEntry>> getEntriesByZone() {
    Map<GUID, Set<NoteBookEntry>> entries = new HashMap<>();
    readLock.lock();
    try {
      for (EntryDetails ed : idEntryMap.values()) {
        entries.computeIfAbsent(ed.getZoneId(), k -> new HashSet<>());
        entries.get(ed.getZoneId()).add(ed.getEntry());
      }
    } finally {
      readLock.unlock();
    }
    return entries;
  }

  /**
   * Returns the {@code NoteBookEntry}s for a {@link Zone}.
   *
   * @param zoneId the id of the {@link Zone} to return the {@code NoteBookEntry}s.
   * @return the {@code NoteBookEntry}s for a {@link Zone}.
   */
  public Set<NoteBookEntry> getZoneEntries(GUID zoneId) {
    Objects.requireNonNull(zoneId, "zoneId cannot be null for getZoneEntries");
    readLock.lock();
    try {
      return idEntryMap.values().stream()
          .filter(e -> zoneId.equals(e.getZoneId()))
          .map(EntryDetails::getEntry)
          .collect(Collectors.toSet());
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Returns the {@code NoteBookEntry}s not attached to any {@link Zone}.
   *
   * @return the {@code NoteBookEntry}s not attached to any {@link Zone}.
   */
  public Collection<NoteBookEntry> getZoneLessEntries() {
    readLock.lock();
    try {
      return idEntryMap.values().stream()
          .filter(e -> e.getZoneId() == null)
          .map(EntryDetails::getEntry)
          .collect(Collectors.toSet());
    } finally {
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
   * Removes multiple {@code NoteBookEntry}s.
   *
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
   * Removes a {@code NoteBookEntry} from the note book. This version of the function allows you to
   * specify if the listeners should be notified of the change. If you pass {@code false} to this
   * you are expected to perform the notification, this is to support bulk updates.
   *
   * @param entry The {@code NoteBookEntry} to remove.
   * @param firePropertyChange {@code true} if listeners should be notified.
   */
  private void removeEntry(NoteBookEntry entry, boolean firePropertyChange) {
    Objects.requireNonNull(entry, "entry canno tbe null for removeEntry");
    writeLock.lock();
    try {
      idEntryMap.remove(entry.getId());
      pathIdMap.remove(entry.getPath());
    } finally {
      writeLock.unlock();
    }

    if (firePropertyChange) {
      fireChangeEvent(ENTRIES_REMOVED_EVENT, Set.of(entry), null);
    }
  }

  /**
   * Removes a {@code NoteBookEntry} from the note book.
   *
   * @param entry The {@code NoteBookEntry} to remove.
   */
  public void removeEntry(NoteBookEntry entry) {
    removeEntry(entry, true);
  }

  /**
   * Returns the {@code NoteBookEntry} for a certain id.
   *
   * @param id the id of the {@code NoteBookEntry} to retrieve.
   * @return the {@code NoteBookEntry} for the id.
   */
  public Optional<NoteBookEntry> getById(UUID id) {
    Objects.requireNonNull(id, "id passed to getById must not be null");
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
   * Returns the {@code NoteBookEntry} at the specified path.
   *
   * @param path the path to get the value for.
   * @return the {@code NoteBookEntry} at the specified path.
   */
  public Optional<NoteBookEntry> getByPath(String path) {
    Objects.requireNonNull(path, "path passed to getByPAth must not be null");
    readLock.lock();
    try {
      if (pathIdMap.containsKey(path)) {
        return Optional.ofNullable(idEntryMap.get(pathIdMap.get(path)).getEntry());
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
   *     {@link #removedZones} so new {@code NoteBookEntry}s can be added. There is no need to fire
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

  /**
   * Returns the mapping between the paths and entries in the {@code NoteBook}.
   *
   * @return the mapping between the paths and entries in the {@code NoteBook}.
   */
  public Map<String, NoteBookEntry> getEntryPaths() {
    Map<String, NoteBookEntry> entries = new TreeMap<>();
    for (EntryDetails ed : idEntryMap.values()) {
      entries.put(ed.getPath(), ed.getEntry());
    }
    return entries;
  }

  public void makePath(String path) {
    int ind = 0;
    while (true) {
      ind = path.indexOf("/", ind);
      if (ind < 0) {
        break;
      }
    }
  }

  /**
   * Returns <code>true</code> if the path exists in the {@code NoteBook}.
   *
   * @param path The path to check.
   * @return <code>true</code> if the path exists otherwise <code>false</code>.
   */
  public boolean containsPath(String path) {
    return getByPath(path).isPresent();
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

    return getVersion().compareTo(o.getVersion());
  }
}
