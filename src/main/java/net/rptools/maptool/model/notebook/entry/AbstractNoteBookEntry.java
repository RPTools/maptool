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
  private String name;

  /** THe zoneId of the {@link NoteBookEntry} */
  private GUID zoneId;

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
    }
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public synchronized String getName() {
    return name;
  }

  @Override
  public synchronized Optional<GUID> getZoneId() {
    return Optional.ofNullable(zoneId);
  }


  @Override
  public synchronized void setName(String name) {
    this.name = name;
  }

  @Override
  public synchronized void setZoneId(GUID zoneId) {
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
    }
  }

  @Override
  public EntryZoneRequirements getZoneRequirements() {
    return zoneRequirements;
  }
}
