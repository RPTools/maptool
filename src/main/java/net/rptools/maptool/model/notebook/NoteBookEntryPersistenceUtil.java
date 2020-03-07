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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.rptools.maptool.model.notebook.entry.Note;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;

/** Utility Class to help with persistence of {@link NoteBookEntry}. */
public class NoteBookEntryPersistenceUtil {

  /** Type field in the Json String. */
  private static final String TYPE_FIELD = "__TYPE";

  /** Functions to turn a {@link JsonObject} into the appropriate type of {@link NoteBookEntry}. */
  private final Map<String, Function<JsonObject, NoteBookEntry>> fromString = new HashMap<>();

  /** Functions to turn {@link NoteBookEntry} into a {@link JsonObject}. */
  private final Map<String, Function<NoteBookEntry, JsonObject>> asString = new HashMap<>();

  /** Map between {@link NoteBookEntry}s and their builders. */
  private final Map<String, String> builders = new HashMap<>();

  /** Creates a new {@code NoteBookEntryPersistenceUtil} class. */
  public NoteBookEntryPersistenceUtil() {
    /*
    TODO: CDW:
    fromString.put(NoteBuilder.class.getSimpleName(), NoteBuilder::buildFromJson);

    asString.put(NoteBuilder.class.getSimpleName(), (note) -> NoteBuilder.toJson((Note) note));

    builders.put(Note.class.getSimpleName(), NoteBuilder.class.getSimpleName());
     */
  }

  /**
   * Converts a {@link NoteBookEntry} into a {@link String} so it can be persisted.
   *
   * @param entry the {@link NoteBookEntry} to convert into a {@link String}.
   * @return the {@link NoteBookEntry} converted into a {@link String}.
   * @throws IllegalStateException if there is a problem converting the {@link NoteBookEntry} to a
   *     {@link String}.
   */
  public String toString(NoteBookEntry entry) {
    String nbeName = entry.getClass().getSimpleName();
    if (!builders.containsKey(nbeName)) {
      throw new IllegalStateException("Unknown Note Book Entry Type " + nbeName);
    }

    String builderName = builders.get(nbeName);
    JsonObject jsonObject = asString.get(builderName).apply(entry);
    jsonObject.addProperty(TYPE_FIELD, nbeName);

    return jsonObject.toString();
  }

  /**
   * Converts a {@link String} from {@link #toString(NoteBookEntry)} into a {@link NoteBookEntry}.
   *
   * @param str the {@link String} to convert.
   * @return the {@link NoteBookEntry} converted from the {@link String}.
   * @throws IllegalStateException if there is a problem converting the {@link String} to {@link
   *     NoteBookEntry}.
   */
  public NoteBookEntry fromString(String str) {
    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
    if (!json.has(TYPE_FIELD)) {
      throw new IllegalStateException("Unknown Note Book Entry");
    }

    String nbeName = json.get(TYPE_FIELD).getAsString();

    json.remove(TYPE_FIELD);

    if (!builders.containsKey(nbeName)) {
      throw new IllegalStateException("Unknown Note Book Entry Type " + nbeName);
    }

    String builderName = builders.get(nbeName);

    return fromString.get(builderName).apply(json);
  }
}
