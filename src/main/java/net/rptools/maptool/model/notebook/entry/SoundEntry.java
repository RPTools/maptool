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

/** {@link NoteBookEntry} that represents a sound. */
public class SoundEntry extends SingleAssetEntry {

  /**
   * Creates a new {@code ImageEntry}.
   *
   * @param id the id of the {@link NoteBookEntry}, if null a new id will be generated.
   * @param name the name of the {@link NoteBookEntry}.
   * @param zoneId the zone id of the {@link NoteBookEntry}, can be null.
   * @param soundKey The {@link MD5Key} for the {@link net.rptools.maptool.model.Asset} containing
   *     the note can be null.
   * @param path the path for the {@link NoteBookEntry}.
   */
  public SoundEntry(UUID id, String name, GUID zoneId, MD5Key soundKey, String path) {
    super(id, name, zoneId, NoteBookEntryZoneRequirements.ZONE_ALLOWED, path, soundKey);
  }

  /**
   * Creates a new {@code ImageEntry}.
   *
   * @param name the name of the {@link NoteBookEntry}.
   * @param zoneId the zone id of the {@link NoteBookEntry}, can be null.
   * @param soundKey The {@link MD5Key} for the {@link net.rptools.maptool.model.Asset} containing
   *     the note can be null.
   * @param path the path for the {@link NoteBookEntry}.
   */
  public SoundEntry(String name, GUID zoneId, MD5Key soundKey, String path) {
    this(NoteBookEntry.generateId(), name, zoneId, soundKey, path);
  }

  @Override
  public NoteBookEntry setName(String name) {
    Objects.requireNonNull(name, "name cannot be null");
    if (name.equals(getName())) {
      return this;
    } else {
      return new SoundEntry(getId(), name, getZoneId().orElse(null), getAssetKey(), getPath());
    }
  }

  @Override
  public NoteBookEntry setZoneId(GUID id) {
    if (zoneWouldChange(id)) {
      return new NoteEntry(getId(), getName(), id, getAssetKey(), getPath());
    } else {
      return this;
    }
  }

  @Override
  public NoteBookEntry setPath(String path) {
    Objects.requireNonNull(path, "path cannot be null");
    if (path.equals(getPath())) {
      return this;
    } else {
      return new SoundEntry(getId(), getName(), getZoneId().orElse(null), getAssetKey(), path);
    }
  }

  @Override
  public NoteBookEntryType getType() {
    return NoteBookEntryType.IMAGE;
  }

  /**
   * Set the {@link MD5Key} for the {@link Asset} containing the sound.
   *
   * @param key The {@link MD5Key} for the {@link Asset} containing the sound.
   */
  public void setSoundKey(MD5Key key) {
    setAssetKey(key);
  }

  /**
   * Returns the {@link MD5Key} for the {@link Asset} containing the sound.
   *
   * @return the {@link MD5Key} for the {@link Asset} containing the sound.
   */
  public MD5Key getSoundKey() {
    return getAssetKey();
  }
}
