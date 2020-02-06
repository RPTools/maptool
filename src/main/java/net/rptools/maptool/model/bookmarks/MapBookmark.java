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

import java.util.Objects;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;

/** {@code MapBookmark} represents a bookmark on a game Map. */
public final class MapBookmark implements Comparable<MapBookmark> {

  /** The id for the bookmark. */
  private final UUID id;

  /** The name of the bookmark. */
  private final String name;

  /** The id of the zone that the bookmark is on. */
  private final GUID zoneId;

  /** The image on the map for the bookmark. */
  private final MD5Key mapImage;

  /** The short description of the bookmark. */
  private final String shortDescription;

  /** The notes for the bookmark. */
  private final String notes;

  /** The x co-ordinate of the bookmark image. */
  private final double iconCenterX;

  /** The y co-ordinate of the bookmark. */
  private final double iconCenterY;

  /** The x co-ordinate of the bookmark image. */
  private final double viewCenterX;

  /** The y co-ordinate of the bookmark. */
  private final double viewCenterY;

  /** The scaling factor to use for the bookmark. */
  private final double scale;

  /** The order of the {@code MapBookMark} with respect to other {@code MapBookMark}s. */
  private final double order;

  /**
   * Returns a new id for a {@link MapBookmark}.
   *
   * @return a new id for a {@link MapBookmark}.
   */
  public static UUID generateId() {
    return UUID.randomUUID();
  }

  /**
   * Creates a new {@code MapBookmark}.
   *
   * @param id The id of the {@code MapBookmark}.
   * @param name The name of the {@code MapBookmark}.
   * @param zoneId The id of the {@link net.rptools.maptool.model.Zone} of the {@code MapBookmark}
   *     is on.
   * @param mapImage The image displayed on the map for the {@code MapBookmark}.
   * @param shortDesc The short description of the {@code MapBookmark}.
   * @param notes The notes for the {@code MapBookmark}.
   * @param iconX The x coordinate center of the {@code MapBookmark} in the {@link
   *     net.rptools.maptool.model.Zone}.
   * @param iconY The y coordinate name of the {@code MapBookmark} in the {@link
   *     net.rptools.maptool.model.Zone}.
   * @param viewX The x coordinate of the {@code MapBookmark} view.
   * @param viewY The y coordinate of the {@code MapBookmark} view.
   * @param scale The scale factor of the {@code MapBookmark} view.
   * @param order The order of the {@code MapBookmark} with respect to other {@code MapBookmark}s.
   */
  public MapBookmark(
      UUID id,
      String name,
      GUID zoneId,
      MD5Key mapImage,
      String shortDesc,
      String notes,
      double iconX,
      double iconY,
      double viewX,
      double viewY,
      double scale,
      double order) {

    assert name != null;
    assert zoneId != null;
    assert mapImage != null;

    this.id = id != null ? id : MapBookmark.generateId();
    this.name = name;
    this.zoneId = zoneId;
    this.mapImage = mapImage;
    this.shortDescription = shortDesc != null ? shortDesc : "";
    this.notes = notes != null ? notes : "";
    this.iconCenterX = iconX;
    this.iconCenterY = iconY;
    this.viewCenterX = viewX;
    this.viewCenterY = viewY;
    this.scale = scale;
    this.order = order;
  }

  /**
   * Creates a new {@code MapBookmark}.
   *
   * @param name The name of the {@code MapBookmark}.
   * @param zoneId The id of the {@link net.rptools.maptool.model.Zone} of the {@code MapBookmark}
   *     is on.
   * @param mapImage The image displayed on the map for the {@code MapBookmark}.
   * @param shortDesc The short description of the {@code MapBookmark}.
   * @param notes The notes for the {@code MapBookmark}.
   * @param iconX The x coordinate center of the {@code MapBookmark} in the {@link
   *     net.rptools.maptool.model.Zone}.
   * @param iconY The y coordinate name of the {@code MapBookmark} in the {@link
   *     net.rptools.maptool.model.Zone}.
   * @param viewX The x coordinate of the {@code MapBookmark} view.
   * @param viewY The y coordinate of the {@code MapBookmark} view.
   * @param scale The scale factor of the {@code MapBookmark} view.
   * @param order The order of the {@code MapBookmark} with respect to other {@code MapBookmark}s.
   */
  public MapBookmark(
      String name,
      GUID zoneId,
      MD5Key mapImage,
      String shortDesc,
      String notes,
      double iconX,
      double iconY,
      double viewX,
      double viewY,
      double scale,
      double order) {
    this(
        MapBookmark.generateId(),
        name,
        zoneId,
        mapImage,
        shortDesc,
        notes,
        iconX,
        iconY,
        viewX,
        viewY,
        scale,
        order);
  }

  /**
   * Returns the id of the {@code Bookmark}.
   *
   * @return the id of the {@code Bookmark}.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Returns the name of the {@code Bookmark}.
   *
   * @return the name pf tje {@code Bookmark}.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the id of the {@link net.rptools.maptool.model.Zone} that this {@code Bookmark} is on.
   *
   * @return the id of the {@link net.rptools.maptool.model.Zone} that this {@code Bookmark} is on.
   */
  public GUID getZoneId() {
    return zoneId;
  }

  /**
   * Returns the {@link MD5Key} of the {@link net.rptools.maptool.model.Asset} used to display this
   * {@code Bookmark} on the map.
   *
   * @return the {@link MD5Key} of the {@link net.rptools.maptool.model.Asset} used to display this
   *     {@code Bookmark} on the map.
   */
  public MD5Key getMapImage() {
    return mapImage;
  }

  /**
   * Returns the short description of the {@code Bookmark}.
   *
   * @return the short description of the {@code Bookmark}.
   */
  public String getShortDescription() {
    return shortDescription;
  }

  /**
   * Returns the notes for the {@code Bookmark}.
   *
   * @return the notes for the {@code Bookmark}.
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Returns the x coordinate of the center of the map icon for the {@code Bookmark}.
   *
   * @return the x coordinate of the center of the map icon for the {@code Bookmark}.
   */
  public double getIconCenterX() {
    return iconCenterX;
  }

  /**
   * Returns the y coordinate of the center of the map icon for the {@code Bookmark}.
   *
   * @return the y coordinate of the center of the map icon for the {@code Bookmark}.
   */
  public double getIconCenterY() {
    return iconCenterY;
  }

  /**
   * Returns the x coordinate of the center of the map view for the {@code Bookmark}.
   *
   * @return the x coordinate of the center of the map view for the {@code Bookmark}.
   */
  public double getViewCenterX() {
    return viewCenterX;
  }

  /**
   * Returns the y coordinate of the center of the map view for the {@code Bookmark}.
   *
   * @return the y coordinate of the center of the map view for the {@code Bookmark}.
   */
  public double getViewCenterY() {
    return viewCenterY;
  }

  /**
   * Returns the scaling factor for the map view for the {@code Bookmark}.
   *
   * @return the scaling factor for the map view for the {@code Bookmark}.
   */
  public double getScale() {
    return scale;
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
    return Double.compare(bookmark.iconCenterX, iconCenterX) == 0
        && Double.compare(bookmark.iconCenterY, iconCenterY) == 0
        && Double.compare(bookmark.viewCenterX, viewCenterX) == 0
        && Double.compare(bookmark.viewCenterY, viewCenterY) == 0
        && Double.compare(bookmark.scale, scale) == 0
        && Double.compare(bookmark.order, order) == 0
        && id.equals(bookmark.id)
        && name.equals(bookmark.name)
        && zoneId.equals(bookmark.zoneId)
        && mapImage.equals(bookmark.mapImage)
        && shortDescription.equals(bookmark.shortDescription)
        && notes.equals(bookmark.notes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
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

  @Override
  public int compareTo(MapBookmark bookmark) {
    return Double.compare(order, bookmark.order);
  }
}
