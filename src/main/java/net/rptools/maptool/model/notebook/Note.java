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

import java.util.Optional;
import java.util.UUID;
import net.rptools.maptool.model.GUID;

/** A {@link NoteBookEntry} that contains nothing but notes. */
public class Note implements NoteBookEntry {

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
   * Creates a {@code NoteBookmark} object from the details contained in a {@link NoteBuilder}
   * object.
   *
   * @param builder the {@link NoteBuilder} used to create this object.
   */
  Note(NoteBuilder builder) {
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
