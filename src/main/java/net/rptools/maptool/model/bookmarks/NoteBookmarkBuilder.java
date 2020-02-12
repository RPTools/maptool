package net.rptools.maptool.model.bookmarks;

import java.awt.print.Book;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;

public class NoteBookmarkBuilder {

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


  public static NoteBookmarkBuilder copy(NoteBookmark noteBookmark, UUID id) {
    assert id != null : "ID can not be null for copied NoteBookmarkBuilder.";

    NoteBookmarkBuilder builder = new NoteBookmarkBuilder();

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

  public static NoteBookmarkBuilder copy(NoteBookmark noteBookmark) {
    return copy(noteBookmark, noteBookmark.getId());
  }


  public static NoteBookmarkBuilder copyWithNewId(NoteBookmark noteBookmark) {
    return copy(noteBookmark, Bookmark.generateId());
  }


  public NoteBookmarkBuilder() {
  }


  public UUID getId() {
    return id;
  }

  public NoteBookmarkBuilder setId(UUID id) {
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

  public NoteBookmarkBuilder setName(String name) {
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

  public NoteBookmarkBuilder setReference(String reference) {
    this.reference = reference;
    referenceSet = reference != null;
    return this;
  }

  public boolean isReferenceSet() {
    return referenceSet;
  }

  public GUID getZoneId() {
    return zoneId;
  }

  public NoteBookmarkBuilder setZoneId(GUID zoneId) {
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

  public NoteBookmarkBuilder setNotes(String notes) {
    this.notes = notes;
    notesSet = notes != null;
    return this;
  }

  public boolean isNotesSet() {
    return notesSet;
  }
}
