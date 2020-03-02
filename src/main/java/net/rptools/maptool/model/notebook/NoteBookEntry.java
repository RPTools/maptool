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

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;

public interface NoteBookEntry {
  /**
   * Returns a new id for a {@link MapBookmark}.
   *
   * @return a new id for a {@link MapBookmark}.
   */
  static UUID generateId() {
    return UUID.randomUUID();
  }

  /**
   * Returns the id of the {@code Bookmark}.
   *
   * @return the id of the {@code Bookmark}.
   */
  UUID getId();

  /**
   * Returns the name of the {@code Bookmark}.
   *
   * @return the name pf tje {@code Bookmark}.
   */
  String getName();

  /**
   * Returns the reference id for the {@code Bookmark}.
   *
   * @return the reference id for the {@code Bookmark}.
   */
  Optional<String> getReference();

  /**
   * Returns the id of the {@link net.rptools.maptool.model.Zone} that this {@code Bookmark} is on.
   *
   * @return the id of the {@link net.rptools.maptool.model.Zone} that this {@code Bookmark} is on.
   */
  Optional<GUID> getZoneId();

  /**
   * Returns the notes for the {@code Bookmark}.
   *
   * @return the notes for the {@code Bookmark}.
   */
  Optional<MD5Key> getNotesKey();

  /**
   * Returns all of the {@link MD5Key}s associated with the {@link Asset}s that the {@code
   * NoteBookEntry} contains.
   *
   * @return all of the {@link MD5Key}s associated with the {@link Asset}s.
   */
  Collection<MD5Key> getAssetKeys();
}
