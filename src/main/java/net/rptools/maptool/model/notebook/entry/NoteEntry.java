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

import java.util.Objects;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;

/** A {@link NoteBookEntry} that contains nothing but notes. */
public class NoteEntry extends SingleAssetEntry {

  /**
   * creates a new {@code NoteEntry}.
   *
   * @param id The id of the {@code Note}.
   * @param name The name of the {@code Note}.
   * @param zoneId The id of the zone of the {@code Note}, can be {@code null}.
   * @param notesKey The {@link MD5Key} for the {@link net.rptools.maptool.model.Asset} containing
   *     the note can be null.
   * @param path The path of the {@code Note}.
   */
  public NoteEntry(UUID id, String name, GUID zoneId, MD5Key notesKey, String path) {
    super(id, name, zoneId, NoteBookEntryZoneRequirements.ZONE_ALLOWED, path, notesKey);
    Objects.requireNonNull(id, "ID for Note cannot be null");
    Objects.requireNonNull(notesKey, "notesKey cannot be null");
  }

  /**
   * creates a new {@code NoteEntry}, with a newly generated id.
   *
   * @param name The name of the {@code Note}.
   * @param zoneId The id of the zone of the {@code Note}, can be {@code null}.
   * @param notesKey The {@link MD5Key} for the {@link Asset} containing the note can be null.
   * @param path The path of the {@code Note}.
   */
  public NoteEntry(String name, GUID zoneId, MD5Key notesKey, String path) {
    super(
        NoteBookEntry.generateId(),
        name,
        zoneId,
        NoteBookEntryZoneRequirements.ZONE_ALLOWED,
        path,
        notesKey);
  }

  /**
   * Set the {@link MD5Key} for the {@link Asset} containing the note.
   *
   * @param key The {@link MD5Key} for the {@link Asset} containing the note.
   */
  public void setNotesKey(MD5Key key) {
    setAssetKey(key);
  }

  /**
   * Returns the {@link MD5Key} for the {@link Asset} containing the note.
   *
   * @return the {@link MD5Key} for the {@link Asset} containing the note.
   */
  public MD5Key getNotesKey() {
    return getAssetKey();
  }

  @Override
  public NoteBookEntry setName(String name) {
    Objects.requireNonNull(name, "name cannot be null.");
    if (name.equals(getName())) {
      return this;
    } else {
      return new NoteEntry(getId(), name, getZoneId().orElse(null), getNotesKey(), getPath());
    }
  }

  @Override
  public NoteBookEntry setZoneId(GUID id) {
    if (zoneWouldChange(id)) {
      return new NoteEntry(getId(), getName(), id, getNotesKey(), getPath());
    } else {
      return this;
    }
  }

  @Override
  public NoteBookEntry setPath(String path) {
    Objects.requireNonNull(path, "path cannot be null");
    if (getPath().equals(path)) {
      return this;
    } else {
      return new NoteEntry(getId(), getName(), getZoneId().orElse(null), getNotesKey(), getPath());
    }
  }

  @Override
  public NoteBookEntryType getType() {
    return NoteBookEntryType.NOTE;
  }
}
