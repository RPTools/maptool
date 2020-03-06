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

import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.notebook.MapMarker;

/** Builder class to create {@link MapBookmark} objects. */
public class MapBookmarkBuilder {
  /** The id for the bookmark. */
  private UUID id;

  /** Has the id value been set. */
  private boolean idSet;

  /** The name of the bookmark. */
  private String name;

  /** Has the name value been set. */
  private boolean nameSet;

  /** The reference value of the bookmark. */
  private String reference;

  /** Has the reference id value been set. */
  private boolean referenceSet;

  /** The id of the zone that the bookmark is on. */
  private GUID zoneId;

  /** Has the zone id been set. */
  private boolean zoneIdSet;

  /** The short notes of the bookmark. */
  private String shortNotes;

  /** Has the short notes been set. */
  private boolean shortNotesSet;

  /** The notes for the bookmark, either this or {@link #notes} will be set not both. */
  private MD5Key notesKey;

  /** The notes for the bookmark, either this or {@link #notesKey} will be set not both. */
  private String notes;

  /** Has the notes value been set. */
  private boolean notesSet;

  /** The {@link MapMarker} to display for the map bookmark. */
  private MapMarker mapMarker;

  /** Has the {@link MapMarker} to display been set. */
  private boolean mapMarkerSet;

  /** The ordering when comparing two bookmarks. */
  private double order;

  /** Has the order value been set. */
  private boolean orderSet;

  /**
   * Creates a {@code MapBookmarkBuilder} prepopulated with the details of an existing {@link
   * MapBookmark} and a specified id.
   *
   * @param mapBookmark The {@link MapBookmark} to copy the values from.
   * @param id The id to used for the {@link MapBookmark}.
   * @return the new builder.
   */
  private static MapBookmarkBuilder copy(MapBookmark mapBookmark, UUID id) {
    assert id != null : "ID can not be null for copied MapBookmarkBuilder.";

    MapBookmarkBuilder builder = new MapBookmarkBuilder();
    builder.setId(id);
    builder.setName(mapBookmark.getName());
    builder.setZoneId(mapBookmark.getZoneId().get());
    builder.setShortNotes(mapBookmark.getShortNotes());
    if (mapBookmark.getNotesKey().isPresent()) {
      builder.setNotesKey(mapBookmark.getNotesKey().get());
    } else {
      builder.setNotesKey(null);
    }
    builder.setMapMarker(mapBookmark.getMapMarker());
    builder.setOrder(mapBookmark.getOrder());

    return builder;
  }

  /**
   * Creates a {@code MapBookmarkBuilder} prepopulated with the details of an existing {@link
   * MapBookmark} retaining the id.
   *
   * @param mapBookmark The {@link MapBookmark} to copy the values from.
   * @return the new builder.
   */
  private static MapBookmarkBuilder copy(MapBookmark mapBookmark) {
    return copy(mapBookmark, mapBookmark.getId());
  }

  /**
   * Creates a {@code MapBookmarkBuilder} prepopulated with the details of an existing {@link
   * MapBookmark} assigning a new id.
   *
   * @param mapBookmark The {@link MapBookmark} to copy the values from.
   * @return the new builder.
   */
  private static MapBookmarkBuilder copyWithNewId(MapBookmark mapBookmark) {
    return copy(mapBookmark, NoteBookEntry.generateId());
  }

  /**
   * Returns the id of the {@link MapBookmark} that will be created.
   *
   * @return the id of the {@link MapBookmark} that will be created.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Returns the name of the {@link MapBookmark} that will be created.
   *
   * @return the name pf tje {@link MapBookmark} that will be created.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the reference id of the {@link MapBookmark} that will be created.
   *
   * @return the reference id of the {@link MapBookmark} that will be created.
   */
  public String getReference() {
    return reference;
  }

  /**
   * Returns the id of the {@link net.rptools.maptool.model.Zone} that this {@link MapBookmark} is
   * on that will be created.
   *
   * @return the id of the {@link net.rptools.maptool.model.Zone} that this {@link MapBookmark} is
   *     on that will be created.
   */
  public GUID getZoneId() {
    return zoneId;
  }

  /**
   * Returns the short notes of the {@link MapBookmark} that will be created.
   *
   * @return the short notes of the {@link MapBookmark} that will be created.
   */
  public String getShortNotes() {
    return shortNotes;
  }

  /**
   * Returns the notes for the {@link MapBookmark} that will be created.
   *
   * @return the notes for the {@link MapBookmark} that will be created.
   */
  public MD5Key getNotesKey() {
    return notesKey;
  }

  /**
   * Returns the {@link MapMarker} for the map bookmark.
   *
   * @returns the {@link MapMarker} for the map bookmark.
   */
  public MapMarker getMapMarker() {
    return mapMarker;
  }

  /**
   * Returns the value used to order the {@link MapBookmark} that will be crated.
   *
   * @return the value used to order the {@link MapBookmark} that will be crated.
   */
  public double getOrder() {
    return order;
  }

  /**
   * Sets the id of the {@link MapBookmark} that will be created.
   *
   * @param id the id of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setId(UUID id) {
    this.id = id;
    idSet = id != null;
    return this;
  }

  /**
   * Sets the name of the {@link MapBookmark} that will be created.
   *
   * @param name the id of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setName(String name) {
    this.name = name;
    nameSet = name != null;
    return this;
  }

  /**
   * Sets the reference id of the {@link MapBookmark} that will be created.
   *
   * @param ref the reference id of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setReference(String ref) {
    reference = ref;
    referenceSet = ref != null;
    return this;
  }

  /**
   * Sets the name of the {@link MapBookmark} that will be created.
   *
   * @param zoneId the id of the {@link net.rptools.maptool.model.Zone} for the {@link MapBookmark}
   *     that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setZoneId(GUID zoneId) {
    this.zoneId = zoneId;
    zoneIdSet = zoneId != null;
    return this;
  }

  /**
   * Sets the short notes of the {@link MapBookmark} that will be created.
   *
   * @param shortNotes the short notes of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setShortNotes(String shortNotes) {
    this.shortNotes = shortNotes;
    shortNotesSet = shortNotes != null;
    return this;
  }

  /**
   * Sets the notes of the {@link MapBookmark} that will be created.
   *
   * @param notesKey the notes of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setNotesKey(MD5Key notesKey) {
    this.notesKey = notesKey;
    this.notes = null;
    notesSet = notesKey != null;
    return this;
  }

  /**
   * Sets the notes of the {@link MapBookmark} that will be created.
   *
   * @param notes the notes of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setNotes(String notes) {
    this.notes = notes;
    this.notesKey = null;
    notesSet = notes != null && notes.length() > 0;

    return this;
  }

  /**
   * Sets the {@link MapMarker} for the map bookmark.
   *
   * @param marker the {@link MapMarker} to set.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setMapMarker(MapMarker marker) {
    mapMarker = marker;
    mapMarkerSet = marker != null;
    return this;
  }

  /**
   * Sets the scaling factor of the view for the {@link MapBookmark} that will be created.
   *
   * @param order the value used to order the {@link MapBookmark} that will be crated.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setOrder(double order) {
    this.order = order;
    orderSet = order != 0;
    return this;
  }

  /**
   * Builds a {@link MapBookmark} with the values provided to the builder.
   *
   * @return a {@link MapBookmark} created by the builder.
   */
  public MapBookmark build() {
    if (notesKey == null && notes != null && !notes.isEmpty()) {
      Asset asset = Asset.createHTMLAsset(name + "-notes", notes.getBytes());
      AssetManager.putAsset(asset);
    }

    return new MapBookmark(this);
  }

  /**
   * Has the order value been set.
   *
   * @return {@code true} if the order value has been set.
   */
  public boolean isIdSet() {
    return idSet;
  }

  /**
   * Has the name value been set.
   *
   * @return {@code true} if the name value has been set.
   */
  public boolean isNameSet() {
    return nameSet;
  }

  /**
   * Has the order value been set.
   *
   * @return {@code true} if the order value has been set.
   */
  public boolean isReferenceSet() {
    return referenceSet;
  }
  /**
   * Has the zone id value been set.
   *
   * @return {@code true} if the zone id value has been set.
   */
  public boolean isZoneIdSet() {
    return zoneIdSet;
  }

  /**
   * Has the short notes value been set.
   *
   * @return {@code true} if the short notes value has been set.
   */
  public boolean isShortNotesSet() {
    return shortNotesSet;
  }

  /**
   * Has the notes value been set.
   *
   * @return {@code true} if the notes value has been set.
   */
  public boolean isNotesSet() {
    return notesSet;
  }

  /**
   * Has the map marker value been set.
   *
   * @return {@code true} if the map marker value has been set.
   */
  public boolean isMapMarkerSet() {
    return mapMarkerSet;
  }
  /**
   * Has the order value been set.
   *
   * @return {@code true} if the order value has been set.
   */
  public boolean isOrderSet() {
    return orderSet;
  }
}
