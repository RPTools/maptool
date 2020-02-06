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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

/**
 * {@code MapBookmarkManager} class is used to manage all the {@link MapBookmark}s in a campaign.
 * This class is thread safe so may be used from multiple threads.
 *
 * @implNote The thread safety of this class depends on the following
 *     <ul>
 *       <li>Whenever the {@link #removedZones} {@code Set} is updated a write lock {@link
 *           #removedZonesWriteLock} must be obtained.
 *       <li>Whenever the {@link #removedZones} {@code Set} is read a read lock {@link
 *           #removedZonesReadLock} must be obtained.
 *       <li>Whenever the {@link #zoneMapBookmarks} {@code Set} is updated or read a read lock
 *           {@link #removedZonesReadLock} must be obtained.
 *     </ul>
 *     There is no need to obtain a write lock when modifying {@link #zoneMapBookmarks} as
 *     concurrent hash map will take care of this, we are just interested in making sure that adding
 *     and removal of {@link Zone}s is consistent across both collections.
 */
public final class MapBookmarkManager {

  /** The {@link MapBookmark}s for each {@link Zone} */
  private final Map<GUID, Map<UUID, MapBookmark>> zoneMapBookmarks = new ConcurrentHashMap<>();

  /**
   * The list of {@link Zone}s that have been removed so that we don't end up adding {@link
   * MapBookmark}s for {@link Zone}s that have been removed.
   */
  private final Set<GUID> removedZones = new HashSet<>();

  /** Lock used to ensure consistency of both collections. */
  private final ReentrantReadWriteLock removedZonesLock = new ReentrantReadWriteLock();

  /**
   * The read lock for the collections, you <strong>must</strong> use this lock whenever you are
   * reading the values from {@link #removedZones} or {@link #zoneMapBookmarks}.
   */
  private final Lock removedZonesReadLock = removedZonesLock.readLock();

  /**
   * The write lock for the collections, you <strong>must</strong> use this lock whenever you are
   * modifying the contents for {@link #removedZones}. You shouldn't use this lock if you are only
   * modifying {@link #zoneMapBookmarks}.
   *
   * @see #removedZonesReadLock
   */
  private final Lock removedZonesWriteLock = removedZonesLock.writeLock();

  /**
   * Adds or replaces a {@link MapBookmark} to the bookmarks being managed. This will replace
   * <strong>any</strong> {@link MapBookmark} with the same id.
   *
   * @see MapBookmark#getId()
   * @param bookmark the {@link MapBookmark} to add or replace.
   */
  public void putMapBookmark(MapBookmark bookmark) {
    removedZonesReadLock.lock();
    try {
      /*
       * If the zone is in the list of zone that have been removed then drop the bookmark silently.
       * This is to avoid storing bookmarks for zones that have already been removed.
       */
      if (!removedZones.contains(bookmark.getZoneId())) {
        zoneMapBookmarks.putIfAbsent(bookmark.getZoneId(), new ConcurrentHashMap<>());
        zoneMapBookmarks.get(bookmark.getZoneId()).put(bookmark.getId(), bookmark);
      }
    } finally {
      removedZonesReadLock.unlock();
    }
  }
  /**
   * Adds or replaces a {@link MapBookmark} to the bookmarks being managed. This will replace
   * <strong>any</strong> {@link MapBookmark} with the same id.
   *
   * @see MapBookmark#getId()
   * @param bookmarks the {@link MapBookmark}s to add or replace.
   */
  public void putMapBookmarks(Collection<MapBookmark> bookmarks) {
    removedZonesReadLock.lock();
    try {
      for (MapBookmark bookmark : bookmarks) {
        /*
         * If the zone is in the list of zone that have been removed then drop the bookmark silently.
         * This is to avoid storing bookmarks for zones that have already been removed.
         */
        if (!removedZones.contains(bookmark.getZoneId())) {
          zoneMapBookmarks.putIfAbsent(bookmark.getZoneId(), new ConcurrentHashMap<>());
          zoneMapBookmarks.get(bookmark.getZoneId()).put(bookmark.getId(), bookmark);
        }
      }
    } finally {
      removedZonesReadLock.unlock();
    }
  }

  /**
   * Returns the {@link MapBookmark}s being managed.
   *
   * @return the {@link MapBookmark}s being managed.
   */
  public Collection<MapBookmark> getMapBookmarks() {
    Set<MapBookmark> bookmarks;
    removedZonesReadLock.lock();
    try {
      bookmarks =
          zoneMapBookmarks.values().stream()
              .flatMap(m -> m.values().stream())
              .collect(Collectors.toSet());
    } finally {
      removedZonesReadLock.unlock();
    }
    return bookmarks;
  }

  /**
   * Returns the {@link MapBookmark}s for a {@link Zone}.
   *
   * @param zone the {@link Zone} to return the {@link MapBookmark}s for.
   * @return the {@link MapBookmark}s for a {@link Zone}.
   */
  public Collection<MapBookmark> getZoneBookmarks(Zone zone) {
    return getZoneBookmarks(zone.getId());
  }

  /**
   * Returns the {@link MapBookmark}s for a {@link Zone}.
   *
   * @param zoneId the id of the {@link Zone} to return the {@link MapBookmark}s.
   * @return the {@link MapBookmark}s for a {@link Zone}.
   */
  public Collection<MapBookmark> getZoneBookmarks(GUID zoneId) {
    Collection<MapBookmark> bookmarks = null;
    removedZonesReadLock.lock();
    try {
      if (zoneMapBookmarks.containsKey(zoneId)) {
        bookmarks = zoneMapBookmarks.get(zoneId).values();
      } else {
        bookmarks = Collections.emptySet();
      }
    } finally {
      removedZonesReadLock.unlock();
    }

    return bookmarks;
  }

  /**
   * Called when a {@link Zone} is removed from the campaign as we are no longer interested in any
   * of the bookmarks associated with it at this point.
   *
   * @param zoneId the id of the {@link Zone} that has been removed.
   */
  public void zoneRemoved(GUID zoneId) {
    removedZonesWriteLock.lock();
    try {
      /*
       * First add this to the list of Zones that have been removed as we do not want to add
       * any bookmarks for Zones that have been removed, which could happen due to the perils
       * of multithreading.
       */
      removedZones.add(zoneId);
      zoneMapBookmarks.remove(zoneId);
    } finally {
      removedZonesWriteLock.unlock();
    }
  }

  /**
   * Called when a {@link Zone} is added to the campaign.
   *
   * @param zoneId the id of the {@link Zone} that was added to the campaign.
   * @implNote {@code MapBookmarkManager} doesn't really care if a <strong>new</strong> {@link Zone}
   *     is added to the campaign but if it is a {@link Zone} that has previously been removed and
   *     is now being re-added then we need to make sure that the {@link Zone} is removed from
   *     {@link #removedZones} so new {@link MapBookmark}s can be added.
   */
  public void zoneAdded(GUID zoneId) {
    removedZonesWriteLock.lock();
    try {
      /*
       * This is to deal with the unlikely case where a zone is removed and readded so we can
       * continue adding new bookmarks.
       */
      removedZones.remove(zoneId);
    } finally {
      removedZonesWriteLock.unlock();
    }
  }
}
