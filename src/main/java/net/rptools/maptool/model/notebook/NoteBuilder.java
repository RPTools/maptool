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

import java.util.UUID;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

/** Builder class used for building {@link Note} objects. */
public class NoteBuilder {

  /** The id of the {@code Note}. */
  private UUID id;

  /** Has the id valie been set. */
  private boolean idSet;

  /** The name of the {@code Note}. */
  private String name;

  /** Has the name value been set. */
  private boolean nameSet;

  /** The Reference value of the {@code Note}. */
  private String reference;

  /** Has the reference value been set. */
  private boolean referenceSet;

  /** The id of the {@link Zone} of the {@code Note}. */
  private GUID zoneId;

  /** Has the {@link Zone} id value been set. */
  private boolean zoneIdSet;

  /** The notes for the {@code Note). */
  private String notes;

  /** Has the nores value been set. */
  private boolean notesSet;

  /**
   * Creates a new {@code NoteBuilder} populated with the values from the passed in {@link Note} and
   * sets the id to the specified value.
   *
   * @param note The {@link Note} to copy the values from.
   * @param id The id value to use.
   * @return a new {@code NoteBuilder}.}
   */
  public static NoteBuilder copy(Note note, UUID id) {
    assert id != null : "ID can not be null for copied NoteBookmarkBuilder.";

    NoteBuilder builder = new NoteBuilder();

    builder.setId(id);
    builder.setName(note.getName());
    if (note.getReference().isPresent()) {
      builder.setReference(note.getReference().get());
    }
    if (note.getZoneId().isPresent()) {
      builder.setZoneId(note.getZoneId().get());
    }
    builder.setNotes(note.getNotes());

    return builder;
  }

  /**
   * Creates a new {@code NoteBuilder} populated with the values from the passed in {@link Note}.
   *
   * @param note The {@link Note} to copy the values from.
   * @return a new {@code NoteBuilder}.
   */
  public static NoteBuilder copy(Note note) {
    return copy(note, note.getId());
  }

  /**
   * Creates a new {@code NoteBuilder} populated with the values from the passed in {@link Note} and
   * assigns a newly generated id.
   *
   * @param note The {@link Note} to copy the values from.
   * @return a new {@code NoteBuilder}.
   */
  public static NoteBuilder copyWithNewId(Note note) {
    return copy(note, NoteBookEntry.generateId());
  }

  /** Creates a new {@code NoteBuilder} with none of the values set. */
  public NoteBuilder() {
    id = NoteBookEntry.generateId();
  }

  /**
   * Returns the id that has been set.
   *
   * @eturn the id that has been set.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the id that will be used for the {@link Note} to be built.
   *
   * @param id the id that will be used.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setId(UUID id) {
    this.id = id;
    idSet = id != null;
    return this;
  }

  /**
   * Returns if the id has been set.
   *
   * @return {@code true} if the id has been set.
   */
  public boolean isIdSet() {
    return idSet;
  }

  /**
   * Returns the name that will be used to build the {@link Note}.
   *
   * @return the name that will be used to build the {@link Note}.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name tht will be used to build the {@link Note}.
   *
   * @param name The name that wil be used.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setName(String name) {
    this.name = name;
    nameSet = name != null;
    return this;
  }

  /**
   * Returns if the name has been set.
   *
   * @return {@code true} if the name has been set.
   */
  public boolean isNameSet() {
    return nameSet;
  }

  /**
   * Returns the reference that will be used for the {@link Note} to be built.
   *
   * @return the reference that has been set.
   */
  public String getReference() {
    return reference;
  }

  /**
   * Sets the reference value that will be used when building the {@link Note}.
   *
   * @param reference The reference value that will be set.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setReference(String reference) {
    if (reference == null || reference.isEmpty()) {
      this.reference = null;
    } else {
      this.reference = reference;
    }
    referenceSet = this.reference != null;
    return this;
  }

  /**
   * Has the reference value been set.
   * @return {@code true} if the reference value has been set.
   */
  public boolean isReferenceSet() {
    return referenceSet;
  }

  /**
   * Returns the id of the {@link Zone} for the {@link Note} will be built.
   *
   * @return the id of the {@link Zone} for the {@link Note} that will be built.
   */
  public GUID getZoneId() {
    return zoneId;
  }

  /**
   * Sets the id of the {@link Zone} for the {@link Note} that will be built.
   * @param zoneId the id of the {@link Zone} for the {@link Note} that will be built.
   *
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setZoneId(GUID zoneId) {
    this.zoneId = zoneId;
    zoneIdSet = zoneId != null;
    return this;
  }

  /**
   * Has the zone id been set.
   *
   * @return {@code true} if the zone id has been set.
   */
  public boolean isZoneIdSet() {
    return zoneIdSet;
  }

  /**
   * Returns the notes for the {@link Note} that will be built.
   *
   * @return the notes for the {@link Note} that will be built.
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Sets the notes for the {@link Note} that will be built.
   * @param notes the notes for the {@link Note} that will be built.
   *
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setNotes(String notes) {
    this.notes = notes;
    notesSet = notes != null;
    return this;
  }

  /**
   * Returns if the notes have been set.
   *
   * @return {@code true} if the notes have been set.
   */
  public boolean isNotesSet() {
    return notesSet;
  }

  /**
   * Builds a new {@link Note} with the details from this {@code NoteBuilder}.
   *
   * @return a {@link Note}.
   *
   * @throws IllegalStateException if all the required values are not set.
   */
  public Note build() {
    return new Note(this);
  }
}
