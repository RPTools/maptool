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
package net.rptools.maptool.model.notebook.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.rptools.maptool.model.notebook.entry.DirectoryEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;

/** Utility class used to help persist {@link DirectoryEntry}s. */
public class DirectoryEntryPersistenceUtil implements NoteBookEntryPersistenceUtil<DirectoryEntry> {

  @Override
  public JsonObject toJson(NoteBookEntry entry) {
    if (!(entry instanceof DirectoryEntry)) {
      throw new IllegalArgumentException("entry is not an instance of DirectoryEntry");
    }
    // This is simple now but done this way to support versioning in the future.
    return new Gson().toJsonTree(entry, DirectoryEntry.class).getAsJsonObject();
  }

  @Override
  public DirectoryEntry fromJson(JsonObject jsonObject) {
    // This is simple now but done this way to support versioning in the future.
    return new Gson().fromJson(jsonObject, DirectoryEntry.class);
  }
}
