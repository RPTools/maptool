package net.rptools.maptool.model.bookmarks;

import java.util.Optional;
import java.util.UUID;
import net.rptools.maptool.model.GUID;

/**
 * A {@link Bookmark} that contains nothing but notes.
 */
public class NoteBookmark implements Bookmark {

  /** The id of the {@code NoteBookmark}. */
  private final UUID id;

  /** Th ename of the {@code NoteBookmark}. */
  private final String name;

  /** The Reference id of the {@code NoteBookmark}. */
  private final String reference;

  /** The Zone Id of the {@code NoteBookmark}. */
  private final GUID zoneId;

  /** The Notes of the {@code NoteBookmark}. */
  private final String notes;

  /**
   * Creates a {@code NoteBookmark} object from the details contained in a
   * {@link NoteBookmarkBuilder} object.
   *
   * @param builder the {@link NoteBookmarkBuilder} used to create this object.
   */
  NoteBookmark(NoteBookmarkBuilder builder) {
    assert builder.isIdSet() : "ID can not be null for NoteBookmark";
    assert builder.isNameSet() : "Name can not be null for NoteBookmark";
    assert builder.isNotesSet() : "Notes can not be null for NoteBookmark";

    id = builder.getId();
    name = builder.getName();
    reference = builder.getReference();
    zoneId = builder.getZoneId();
    notes = builder.getNotes();

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
  public Optional<String> getReference() {
    return Optional.ofNullable(reference);
  }

  @Override
  public Optional<GUID> getZoneId() {
    return Optional.ofNullable(zoneId);
  }

  @Override
  public String getNotes() {
    return null;
  }
}
