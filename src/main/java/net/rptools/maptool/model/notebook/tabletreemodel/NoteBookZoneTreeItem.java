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
package net.rptools.maptool.model.notebook.tabletreemodel;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

/** Holder class that holds the id of the {@link Zone} in the model for the ui representation. */
public final class NoteBookZoneTreeItem implements TableTreeItemHolder {
  /** The id of the {@link Zone}. */
  private final GUID id;

  /**
   * Creates a new {@code NoteBookZoneTreeItem}.
   *
   * @param zoneId the id of the {@link Zone}.
   */
  NoteBookZoneTreeItem(GUID zoneId) {
    id = zoneId;
  }

  /**
   * Returns the id of the {@link Zone}.
   *
   * @return the id of the {@link Zone}.
   */
  public GUID getId() {
    return id;
  }
}
