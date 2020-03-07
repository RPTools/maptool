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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;

/** A {@link NoteBookEntry} that contains nothing but notes. */
public class Note extends AbstractNoteBookEntry {

  /** The Notes of the {@code NoteBookmark}. */
  private MD5Key notesKey;


  /**
   * creates a new {@code Note}.
   *
   * @param id The id of the {@code Note}.
   * @param name The name of the {@code Note}.
   * @param zoneId The id of the zone of the {@code Note}, can be {@code null}.
   * @param notesKey The {@link MD5Key} for the {@link net.rptools.maptool.model.Asset} containing
   *                 the note can be null.
   */
  public Note(UUID id, String name, GUID zoneId, MD5Key notesKey) {
    super(id, name, zoneId, EntryZoneRequirements.ZONE_ALLOWED);
    Objects.requireNonNull(id, "ID for Note cannot be null");

    this.notesKey =  notesKey;
  }

  /**
   * creates a new {@code Note}, with a newly generated id.
   *
   * @param name The name of the {@code Note}.
   * @param zoneId The id of the zone of the {@code Note}, can be {@code null}.
   * @param notesKey The {@link MD5Key} for the {@link Asset} containing
   *                 the note can be null.
   */
  public Note(String name, GUID zoneId, MD5Key notesKey) {
    super(null, name, zoneId, EntryZoneRequirements.ZONE_ALLOWED);
    this.notesKey =  notesKey;
  }

  /**
   * Set the {@link MD5Key} for the {@link Asset} containing the note.
   *
   * @param key The {@link MD5Key} for the {@link Asset} containing the note.
   */
  public synchronized void setNotesKey(MD5Key key) {
    notesKey = key;
  }


  /**
   * Returns the {@link MD5Key} for the {@link Asset} containing the note.
   * @return the {@link MD5Key} for the {@link Asset} containing the note.
   */
  public synchronized Optional<MD5Key> getNotesKey() {
    return  Optional.ofNullable(notesKey);
  }

  @Override
  public NoteBookEntry setName(String name) {
    if (name.equals(getName())) {
      return this;
    } else {
      if (getZoneId().isPresent()) {
        return new Note(getId(), name, getZoneId().get(), notesKey);
      } else {
        return new Note(getId(), name, null, notesKey);
      }
    }
  }

  @Override
  public NoteBookEntry setZoneId(GUID id) {
    if (zoneWouldChange(id)) {
      return new Note(getId(), getName(), id, notesKey);
    } else {
      return this;
    }
  }

  @Override
  public synchronized Collection<MD5Key> getAssetKeys() {
    if (notesKey == null) {
      return Collections.emptySet();
    } else {
      return Set.of(notesKey);
    }
  }
}
