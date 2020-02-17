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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.rptools.maptool.model.GUID;

/** {@code MapBookmark} represents a map bookmark on a game Map. */
public final class MapBookmark implements NoteBookEntry, Comparable<MapBookmark> {

  /** The id for the bookmark. */
  private final UUID id;

  /** The name of the bookmark. */
  private final String name;

  /** The reference id of the bookmark. */
  private final String reference;

  /** The id of the zone that the bookmark is on. */
  private final GUID zoneId;

  /** The short description of the bookmark. */
  private final String shortNotes;

  /** The notes for the bookmark. */
  private final String notes;

  /** The {@link MapMarker} for this Map BookMark. */
  private final MapMarker mapMarker;

  /** The order of the {@code MapBookMark} with respect to other {@code MapBookMark}s. */
  private final double order;

  /**
   * Creates a new {@code MapBookmark}.
   *
   * @param builder {@link MapBookmarkBuilder} used to build this {@code MapBookmark}.
   *
   * @throws IllegalStateException if all the required values are not set.
   */
  MapBookmark(MapBookmarkBuilder builder) {
    String error = "";
    boolean invalid = false;


    if (builder.isZoneIdSet()) {
      error = "The zone id must be set for a MapBookmark";
      invalid = true;
    }

    if (builder.isNameSet()) {
      if (!error.isEmpty()) {
        error += ", ";
      }
      error += "The name must be set for a MapBookmark";
      invalid = true;
    }

    if (builder.isNotesSet()) {
      if (!error.isEmpty()) {
        error += ",";
      }
      error +=  "The map marker must be set for a MapBookmark";
      invalid = true;
    }

    if (invalid) {
      throw new IllegalStateException(error);
    }

    id = builder.isIdSet() ? builder.getId() : NoteBookEntry.generateId();
    name = builder.getName();
    reference = builder.getReference();
    zoneId = builder.getZoneId();
    shortNotes = builder.isShortNotesSet() ? builder.getShortNotes() : "";
    notes = builder.isNotesSet() ? builder.getNotes() : "";
    mapMarker = builder.getMapMarker();
    order = builder.getOrder();
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
    return Optional.of(zoneId);
  }

  /**
   * Returns the {@link MapMarker} for the {@code Bookmark}.
   *
   * @return the {@link MapMarker} for the {@code Bookmark}.
   */
  public MapMarker getMapMarker() {
    return mapMarker;
  }

  /**
   * Returns the short notes of the {@code Bookmark}.
   *
   * @return the short notes of the {@code Bookmark}.
   */
  public String getShortNotes() {
    return shortNotes;
  }

  @Override
  public String getNotes() {
    return notes;
  }

  /**
   * Returns the order of this {@code MapBookmark} with respect to order {@link MapBookmark}s.
   *
   * @return the order of this {@code MapBookmark} with respect to order {@link MapBookmark}s.
   */
  public double getOrder() {
    return order;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MapBookmark bookmark = (MapBookmark) o;
    return Double.compare(bookmark.order, order) == 0
        && id.equals(bookmark.id)
        && name.equals(bookmark.name)
        && Objects.equals(reference, bookmark.reference)
        && zoneId.equals(bookmark.zoneId)
        && shortNotes.equals(bookmark.shortNotes)
        && notes.equals(bookmark.notes)
        && mapMarker.equals(bookmark.mapMarker);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, reference, zoneId, shortNotes, notes, mapMarker, order);
  }

  @Override
  public int compareTo(MapBookmark bookmark) {
    return Double.compare(order, bookmark.order);
  }
}
