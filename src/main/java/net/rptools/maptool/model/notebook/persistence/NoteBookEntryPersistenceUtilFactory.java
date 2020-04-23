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

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntryType;

/** Utility Class to help with persistence of {@link NoteBookEntry}. */
public class NoteBookEntryPersistenceUtilFactory
    implements NoteBookEntryPersistenceUtil<NoteBookEntry> {

  /** Type field in the Json. */
  private static final String TYPE_FIELD = "type";

  /**
   * Mapping between the {@link NoteBookEntryType}s and the {@link NoteBookEntryPersistenceUtil}s
   * used to convert them.
   */
  private final Map<NoteBookEntryType, NoteBookEntryPersistenceUtil<? extends NoteBookEntry>>
      persistenceUtilMap = new HashMap<>();

  /** Creates a new {@code NoteBookEntryPersistenceUtil} class. */
  public NoteBookEntryPersistenceUtilFactory() {
    persistenceUtilMap.put(NoteBookEntryType.DIRECTORY, new DirectoryEntryPersistenceUtil());
    persistenceUtilMap.put(NoteBookEntryType.NOTE, new NotePersistenceUtil());
  }

  @Override
  public JsonObject toJson(NoteBookEntry entry) {
    if (!persistenceUtilMap.containsKey(entry.getType())) {
      throw new IllegalStateException("Unknown NoteBookEntry type " + entry.getType());
    }
    return persistenceUtilMap.get(entry.getType()).toJson(entry);
  }

  @Override
  public NoteBookEntry fromJson(JsonObject jsonObject) {
    String entryTypeName = jsonObject.get(TYPE_FIELD).getAsString();
    NoteBookEntryType noteBookEntryType = NoteBookEntryType.valueOf(entryTypeName);

    if (!persistenceUtilMap.containsKey(noteBookEntryType)) {
      throw new IllegalStateException("Unknown NoteBookEntry type " + noteBookEntryType);
    }
    return persistenceUtilMap.get(noteBookEntryType).fromJson(jsonObject);
  }
}
