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
package net.rptools.maptool.model.notebook.entry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;

/** Class that represents a directory in the {@code NoteBook}. */
public final class DirectoryEntry extends AbstractNoteBookEntry implements NoteBookEntry {

  /** The child note book entries in this directory. */
  private final Set<NoteBookEntry> children = new HashSet<>();

  /**
   * Creates a new {@code DirectoryEntry} object.
   *
   * @param path The path of the directory.
   */
  public DirectoryEntry(String path) {
    this(null, path);
  }

  /**
   * Creates a new {@code DirectoryEntry} object.
   *
   * @param id the id of the {@code DirectoryEntry}.
   * @param path the path of the {@code DirectoryEntry}.
   */
  public DirectoryEntry(UUID id, String path) {
    super(
        null, path.replaceFirst(".*/", ""), null, NoteBookEntryZoneRequirements.ZONE_IGNORED, path);
  }

  @Override
  public UUID getId() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Optional<GUID> getZoneId() {
    return Optional.empty();
  }

  @Override
  public Collection<MD5Key> getAssetKeys() {
    return Collections.emptySet();
  }

  @Override
  public NoteBookEntryType getType() {
    return NoteBookEntryType.DIRECTORY;
  }

  @Override
  public NoteBookEntry setName(String name) {
    Objects.requireNonNull(name, "Name passed to setName cannot be null");
    if (name.equals(getName())) {
      return this;
    } else {
      return new DirectoryEntry(getId(), getPath());
    }
  }

  @Override
  public NoteBookEntry setZoneId(GUID zoneId) {
    return this; // Zone can never change as its ignored for directories.
  }

  @Override
  public NoteBookEntry setPath(String path) {
    return new DirectoryEntry(getId(), path);
  }

  /**
   * Returns the children in this directory.
   *
   * @return the children in this directory.
   */
  public synchronized Set<NoteBookEntry> getChildren() {
    return new HashSet<>(children);
  }

  /**
   * Removes a child {@link NoteBookEntry} from the directory.
   *
   * @param entry the {@link NoteBookEntry} to remove.
   */
  public synchronized void removeChild(NoteBookEntry entry) {
    children.remove(entry);
  }

  /**
   * Adds a child {@link NoteBookEntry} to the directory.
   *
   * @param entry the {@link NoteBookEntry} to add..
   */
  public synchronized void addChild(NoteBookEntry entry) {
    children.remove(entry);
    children.add(entry);
  }

  /**
   * Checks to see if the directory contains a {@link NoteBookEntry}.
   *
   * @param entry the {@link NoteBookEntry} to check for.
   * @return {@code true} if the directory contains the {@link NoteBookEntry}.
   */
  public synchronized boolean containsChild(NoteBookEntry entry) {
    return children.contains(entry);
  }
}
