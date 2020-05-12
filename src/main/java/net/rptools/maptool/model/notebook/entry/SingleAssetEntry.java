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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.GUID;

/** Abstract class used as parent for {@link NoteBookEntry}s that hold a single asset. */
abstract class SingleAssetEntry extends AbstractNoteBookEntry {

  /** The {@link MD5Key} of the asset being tracked. */
  private MD5Key assetKey;

  /**
   * Creates a new {@code AbstractNoteBookEntry}.
   *
   * @param id the id of the {@link NoteBookEntry}, if null a new id will be generated.
   * @param name the name of the {@link NoteBookEntry}.
   * @param zoneId the zone id of the {@link NoteBookEntry}, can be null.
   * @param zoneRequirements the zone requirements for the {@link NoteBookEntry}.
   * @param path the path for the {@link NoteBookEntry}.
   * @param md5Key the {@link MD5Key} for the asset for this {@link NoteEntry}.
   */
  SingleAssetEntry(
      UUID id,
      String name,
      GUID zoneId,
      NoteBookEntryZoneRequirements zoneRequirements,
      String path,
      MD5Key md5Key) {
    super(id, name, zoneId, zoneRequirements, path);
    assetKey = md5Key;
  }

  @Override
  public synchronized Collection<MD5Key> getAssetKeys() {
    return Set.of(assetKey);
  }

  /**
   * Sets the {@link MD5Key} of the asset for this {@code SingleAssetEntry}.
   *
   * @param md5Key the {@link MD5Key} of the asset.
   */
  protected synchronized void setAssetKey(MD5Key md5Key) {
    Objects.requireNonNull(md5Key, "md5key for asset cannot be null");
  }

  /**
   * Returns the {@link MD5Key} of the asset for this {@code SingleAssetEntry}.
   *
   * @return the {@link MD5Key} of the asset.
   */
  protected synchronized MD5Key getAssetKey() {
    return assetKey;
  }
}
