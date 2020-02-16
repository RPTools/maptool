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

public class NoteBuilder {

  private UUID id;

  private boolean idSet;

  private String name;

  private boolean nameSet;

  private String reference;

  private boolean referenceSet;

  private GUID zoneId;

  private boolean zoneIdSet;

  private String notes;

  private boolean notesSet;

  public static NoteBuilder copy(Note noteBookmark, UUID id) {
    assert id != null : "ID can not be null for copied NoteBookmarkBuilder.";

    NoteBuilder builder = new NoteBuilder();

    builder.setId(id);
    builder.setName(noteBookmark.getName());
    if (noteBookmark.getReference().isPresent()) {
      builder.setReference(noteBookmark.getReference().get());
    }
    if (noteBookmark.getZoneId().isPresent()) {
      builder.setZoneId(noteBookmark.getZoneId().get());
    }
    builder.setNotes(noteBookmark.getNotes());

    return builder;
  }

  public static NoteBuilder copy(Note noteBookmark) {
    return copy(noteBookmark, noteBookmark.getId());
  }

  public static NoteBuilder copyWithNewId(Note noteBookmark) {
    return copy(noteBookmark, NoteBookEntry.generateId());
  }

  public NoteBuilder() {
    id = NoteBookEntry.generateId();
  }

  public UUID getId() {
    return id;
  }

  public NoteBuilder setId(UUID id) {
    this.id = id;
    idSet = id != null;
    return this;
  }

  public boolean isIdSet() {
    return idSet;
  }

  public String getName() {
    return name;
  }

  public NoteBuilder setName(String name) {
    this.name = name;
    nameSet = name != null;
    return this;
  }

  public boolean isNameSet() {
    return nameSet;
  }

  public String getReference() {
    return reference;
  }

  public NoteBuilder setReference(String reference) {
    if (reference == null || reference.isEmpty()) {
      this.reference = null;
    } else {
      this.reference = reference;
    }
    referenceSet = this.reference != null;
    return this;
  }

  public boolean isReferenceSet() {
    return referenceSet;
  }

  public GUID getZoneId() {
    return zoneId;
  }

  public NoteBuilder setZoneId(GUID zoneId) {
    this.zoneId = zoneId;
    zoneIdSet = zoneId != null;
    return this;
  }

  public boolean isZoneIdSet() {
    return zoneIdSet;
  }

  public String getNotes() {
    return notes;
  }

  public NoteBuilder setNotes(String notes) {
    this.notes = notes;
    notesSet = notes != null;
    return this;
  }

  public boolean isNotesSet() {
    return notesSet;
  }

  public Note build() {
    return new Note(this);
  }
}
