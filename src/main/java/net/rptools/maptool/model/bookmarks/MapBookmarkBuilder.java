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
package net.rptools.maptool.model.bookmarks;

import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;

/** Builder class to create {@link MapBookmark} objects. */
public class MapBookmarkBuilder {
  /** The id for the bookmark. */
  private UUID id;

  /** The name of the bookmark. */
  private String name;

  /** The id of the zone that the bookmark is on. */
  private GUID zoneId;

  /** The image on the map for the bookmark. */
  private MD5Key mapImage;

  /** The short description of the bookmark. */
  private String shortDescription;

  /** The notes for the bookmark. */
  private String notes;

  /** The x co-ordinate of the bookmark image. */
  private double iconCenterX;

  /** The y co-ordinate of the bookmark. */
  private double iconCenterY;

  /** The x co-ordinate of the bookmark image. */
  private double viewCenterX;

  /** The y co-ordinate of the bookmark. */
  private double viewCenterY;

  /** The scaling factor to use for the bookmark. */
  private double scale;

  /** The ordering when comparing two bookmarks. */
  private double order;

  /**
   * Creates a {@code MapBookmarkBuilder} prepopulated with the details of an existing {@link
   * MapBookmark}.
   *
   * @param mapBookmark The {@link MapBookmark} to copy the values from.
   * @param id The id to used for the {@link MapBookmark}.
   * @return the new builder.
   */
  private static MapBookmarkBuilder copy(MapBookmark mapBookmark, UUID id) {
    MapBookmarkBuilder builder = new MapBookmarkBuilder();
    builder.id = id;
    builder.name = mapBookmark.getName();
    builder.zoneId = mapBookmark.getZoneId();
    builder.mapImage = mapBookmark.getMapImage();
    builder.shortDescription = mapBookmark.getShortDescription();
    builder.notes = mapBookmark.getNotes();
    builder.iconCenterX = mapBookmark.getIconCenterX();
    builder.iconCenterY = mapBookmark.getIconCenterY();
    builder.viewCenterX = mapBookmark.getViewCenterX();
    builder.viewCenterY = mapBookmark.getViewCenterY();
    builder.scale = mapBookmark.getScale();

    return builder;
  }

  /**
   * Creates a {@code MapBookmarkBuilder} prepopulated with the details of an existing {@link
   * MapBookmark}.
   *
   * @param mapBookmark The {@link MapBookmark} to copy the values from.
   * @return the new builder.
   */
  public static MapBookmarkBuilder copyWithSameId(MapBookmark mapBookmark) {
    return copy(mapBookmark, mapBookmark.getId());
  }

  /**
   * Creates a {@code MapBookmarkBuilder} preopulated with the details of an existing {@link
   * MapBookmark}, but having a new id.
   *
   * @param mapBookmark The {@link MapBookmark} to copy the values from.
   * @return the new builder.
   */
  public static MapBookmarkBuilder copyWithNewId(MapBookmark mapBookmark) {
    return copy(mapBookmark, MapBookmark.generateId());
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
   * Returns the {@link MD5Key} of the {@link net.rptools.maptool.model.Asset} used to display this
   * {@link MapBookmark} on the map that will be created.
   *
   * @return the {@link MD5Key} of the {@link net.rptools.maptool.model.Asset} used to display this
   *     {@link MapBookmark} on the map that will be created.
   */
  public MD5Key getMapImage() {
    return mapImage;
  }

  /**
   * Returns the short description of the {@link MapBookmark} that will be created.
   *
   * @return the short description of the {@link MapBookmark} that will be created.
   */
  public String getShortDescription() {
    return shortDescription;
  }

  /**
   * Returns the notes for the {@link MapBookmark} that will be created.
   *
   * @return the notes for the {@link MapBookmark} that will be created.
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Returns the x coordinate of the center of the map icon for the {@link MapBookmark} that will be
   * created.
   *
   * @return the x coordinate of the center of the map icon for the {@link MapBookmark} that will be
   *     created.
   */
  public double getIconCenterX() {
    return iconCenterX;
  }

  /**
   * Returns the y coordinate of the center of the map icon for the {@link MapBookmark} that will be
   * created.
   *
   * @return the y coordinate of the center of the map icon for the {@link MapBookmark} that will be
   *     created.
   */
  public double getIconCenterY() {
    return iconCenterY;
  }

  /**
   * Returns the x coordinate of the center of the map view for the {@link MapBookmark} that will be
   * created.
   *
   * @return the x coordinate of the center of the map view for the {@link MapBookmark} that will be
   *     created.
   */
  public double getViewCenterX() {
    return viewCenterX;
  }

  /**
   * Returns the y coordinate of the center of the map view for the {@link MapBookmark} that will be
   * created.
   *
   * @return the y coordinate of the center of the map view for the {@link MapBookmark} that will be
   *     created.
   */
  public double getViewCenterY() {
    return viewCenterY;
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
   * Returns the scaling factor for the map view for the {@link MapBookmark} that will be created.
   *
   * @return the scaling factor for the map view for the {@link MapBookmark} that will be created.
   */
  public double getScale() {
    return scale;
  }

  /**
   * Sets the id of the {@link MapBookmark} that will be created.
   *
   * @param id the id of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setId(UUID id) {
    this.id = id;
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
    return this;
  }

  /**
   * Sets the name of the {@link MapBookmark} that will be created.
   *
   * @param mapImage the id of the {@link MD5Key} for {@link net.rptools.maptool.model.Asset} of the
   *     image for the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setMapImage(MD5Key mapImage) {
    this.mapImage = mapImage;
    return this;
  }

  /**
   * Sets the short description of the {@link MapBookmark} that will be created.
   *
   * @param shortDescription the short description of the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
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
    return this;
  }

  /**
   * Sets the x coordinate of the center of the icon for the {@link MapBookmark} that will be
   * created.
   *
   * @param iconCenterX the x coordinate of the center the icon for the {@link MapBookmark} that
   *     will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setIconCenterX(double iconCenterX) {
    this.iconCenterX = iconCenterX;
    return this;
  }

  /**
   * Sets the y coordinate of the center of the icon for the {@link MapBookmark} that will be
   * created.
   *
   * @param iconCenterY the y coordinate of the center the icon for the {@link MapBookmark} that
   *     will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setIconCenterY(double iconCenterY) {
    this.iconCenterY = iconCenterY;
    return this;
  }

  /**
   * Sets the x coordinate of the center of the view for the {@link MapBookmark} that will be
   * created.
   *
   * @param viewCenterX the x coordinate of the center the view for the {@link MapBookmark} that
   *     will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setViewCenterX(double viewCenterX) {
    this.viewCenterX = viewCenterX;
    return this;
  }

  /**
   * Sets the y coordinate of the center of the view for the {@link MapBookmark} that will be
   * created.
   *
   * @param viewCenterY the y coordinate of the center the view for the {@link MapBookmark} that
   *     will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setViewCenterY(double viewCenterY) {
    this.viewCenterY = viewCenterY;
    return this;
  }

  /**
   * Sets the scaling factor of the view for the {@link MapBookmark} that will be created.
   *
   * @param scale the scaling factor view for the {@link MapBookmark} that will be created.
   * @return {@code this} so methods can be chained.
   */
  public MapBookmarkBuilder setScale(double scale) {
    this.scale = scale;
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
    return this;
  }

  /**
   * Builds a {@link MapBookmark} with the values provided to the builder.
   *
   * @return a {@link MapBookmark} created by the builder.
   */
  public MapBookmark build() {
    return new MapBookmark(
        id != null ? id : MapBookmark.generateId(),
        name,
        zoneId,
        mapImage,
        shortDescription,
        notes,
        iconCenterX,
        iconCenterY,
        viewCenterX,
        viewCenterY,
        scale,
        order);
  }
}
