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

import net.rptools.maptool.model.notebook.entry.NoteBookEntry;

/** Holder class that holds {@link NoteBookEntry}s in the model for the ui representation. */
public final class NoteBookEntryTreeItem implements TableTreeItemHolder {
  /** The {@link NoteBookEntry} to be displayed. */
  private final NoteBookEntry entry;

  /**
   * Creates a new {@code NoteBookEntryTreeItem} to be held in the {@link
   * javafx.scene.control.TreeItem} node for the UI.
   *
   * @param entry the {@link NoteBookEntry} to hold.
   */
  NoteBookEntryTreeItem(NoteBookEntry entry) {
    this.entry = entry;
  }

  /**
   * Returns the {@link NoteBookEntry} being held.
   *
   * @return the {@link NoteBookEntry} being held.
   */
  public NoteBookEntry getEntry() {
    return entry;
  }
}
