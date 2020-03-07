package net.rptools.maptool.model.notebook.entry;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.rptools.maptool.model.GUID;

/**
 * Abstract class used to build up other {@link NoteBookEntry}s.
 */
abstract class AbstractNoteBookEntry implements NoteBookEntry {

  /** THe id of the {@link NoteBookEntry} */
  private final UUID id;

  /** THe name of the {@link NoteBookEntry} */
  private final String name;

  /** THe zoneId of the {@link NoteBookEntry} */
  private final GUID zoneId;

  /** The zone requirements for the entry. */
  private final EntryZoneRequirements zoneRequirements;


  /**
   * Creates a new {@code AbstractNoteBookEntry}.
   *
   * @param id the id of the {@link NoteBookEntry}, if null a new id will be generated.
   * @param name the name of the {@link NoteBookEntry}.
   * @param zoneId the zone id of the {@link NoteBookEntry}, can be null.
   * @param zoneRequirements the zone requirements for the {@link NoteBookEntry}.
   */
  AbstractNoteBookEntry(UUID id, String name, GUID zoneId, EntryZoneRequirements zoneRequirements) {
    this.id = id != null ? id : NoteBookEntry.generateId();
    this.name = Objects.requireNonNull(name, "Note Book Entry name can not be null.");
    this.zoneRequirements = Objects.requireNonNull(zoneRequirements, "Note Book Entry zome requirements can not be null.");

    switch (zoneRequirements) {
      case ZONE_IGNORED:
        this.zoneId = null;
        break;
      case ZONE_ALLOWED:
        this.zoneId = zoneId;
        break;
      case ZONE_REQUIRED:
        this.zoneId = Objects.requireNonNull(zoneId, "Required zone id can not be null for note book entry.");
        break;
      default:
        throw new AssertionError(); // Should never happen.
    }
  }

  /**
   * Checks to see if the passed in zone id is different from the current zone id.
   * If {@link #getZoneRequirements()} is {@link EntryZoneRequirements#ZONE_IGNORED}
   * this will always return false.
   *
   * @param zId the zone id to check.
   * @return {@code true} if zone is not being ignored and passed in zone id differs from current
   *         zoneId.
   */
  protected boolean zoneWouldChange(GUID zId) {
    // If the zone is always ignored then a change could never occur.
    if (zoneRequirements == EntryZoneRequirements.ZONE_IGNORED) {
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
  public EntryZoneRequirements getZoneRequirements() {
    return zoneRequirements;
  }
}
