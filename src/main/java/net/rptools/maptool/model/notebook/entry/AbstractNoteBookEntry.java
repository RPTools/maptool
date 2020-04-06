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
import java.util.Optional;
import java.util.UUID;
import net.rptools.maptool.model.GUID;

/** Abstract class used to build up other {@link NoteBookEntry}s. */
abstract class AbstractNoteBookEntry implements NoteBookEntry {

  /** THe id of the {@link NoteBookEntry} */
  private final UUID id;

  /** THe name of the {@link NoteBookEntry} */
  private final String name;

  /** THe zoneId of the {@link NoteBookEntry} */
  private final GUID zoneId;

  /** The zone requirements for the entry. */
  private final NoteBookEntryZoneRequirements zoneRequirements;

  /** The path of the {@link NoteBookEntry}. */
  private final String path;

  /**
   * Creates a new {@code AbstractNoteBookEntry}.
   *
   * @param id the id of the {@link NoteBookEntry}, if null a new id will be generated.
   * @param name the name of the {@link NoteBookEntry}.
   * @param zoneId the zone id of the {@link NoteBookEntry}, can be null.
   * @param zoneRequirements the zone requirements for the {@link NoteBookEntry}.
   * @param path the path for the {@link NoteBookEntry}.
   */
  AbstractNoteBookEntry(
      UUID id,
      String name,
      GUID zoneId,
      NoteBookEntryZoneRequirements zoneRequirements,
      String path) {
    this.id = id != null ? id : NoteBookEntry.generateId();
    this.name = Objects.requireNonNull(name, "Note Book Entry name cannot be null.");
    this.zoneRequirements =
        Objects.requireNonNull(
            zoneRequirements, "Note Book Entry zone requirements cannot be null.");
    this.path = Objects.requireNonNull(path, "Note Book Entry path cannot be null.");

    switch (zoneRequirements) {
      case ZONE_IGNORED:
        this.zoneId = null;
        break;
      case ZONE_ALLOWED:
        this.zoneId = zoneId;
        break;
      case ZONE_REQUIRED:
        this.zoneId =
            Objects.requireNonNull(zoneId, "Required zone id can not be null for note book entry.");
        break;
      default:
        throw new AssertionError(); // Should never happen.
    }
  }

  /**
   * Checks to see if the passed in zone id is different from the current zone id. If {@link
   * #getZoneRequirements()} is {@link NoteBookEntryZoneRequirements#ZONE_IGNORED} this will always
   * return false.
   *
   * @param zId the zone id to check.
   * @return {@code true} if zone is not being ignored and passed in zone id differs from current
   *     zoneId.
   */
  protected boolean zoneWouldChange(GUID zId) {
    // If the zone is always ignored then a change could never occur.
    if (zoneRequirements == NoteBookEntryZoneRequirements.ZONE_IGNORED) {
      return false;
    }

    // If it is null and being set to null no change
    if (zoneId == zId) {
      return false;
    }

    // If zoneId is not null then check to see if its equal to zId
    if (zoneId != null) {
      return !zoneId.equals(zId);
    }

    // If we get here it means that zoneId == null and zId != null so will create a change
    return true;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<GUID> getZoneId() {
    return Optional.ofNullable(zoneId);
  }

  @Override
  public NoteBookEntryZoneRequirements getZoneRequirements() {
    return zoneRequirements;
  }

  @Override
  public String getPath() {
    return path;
  }
}
