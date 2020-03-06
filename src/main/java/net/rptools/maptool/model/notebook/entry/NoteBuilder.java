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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Since;
import java.util.UUID;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

/** Builder class used for building {@link Note} objects. */
public class NoteBuilder implements NoteBookEntryBuilder {

  /** The current version of the {@link NoteBuilder} class */
  public static final double CURRENT_VERSION = 1.0;

  /** The version of the {@code NoteBuilder}. */
  private double version = 1.0;

  /** The id of the {@code Note}. */
  @Since(1.0)
  private UUID id;

  /** The name of the {@code Note}. */
  @Since(1.0)
  private String name;

  /** The Reference value of the {@code Note}. */
  @Since(1.0)
  private String reference;

  /** The id of the {@link Zone} of the {@code Note}. */
  @Since(1.0)
  private GUID zoneId;

  /** The notes for the {@code Note). */
  @Since(1.0)
  private MD5Key notesKey;

  /**
   * The notes value before it has been converted to an {@code Asset}, once it has been converted to
   * an {@code Asset} then {@link #notesKey} will be set and this unset.
   */
  private transient String notes;

  /**
   * Creates a new {@code NoteBuilder} populated with the values from the passed in {@link Note} and
   * sets the id to the specified value.
   *
   * @param note The {@link Note} to copy the values from.
   * @param id The id value to use.
   * @return a new {@code NoteBuilder}.
   * @throws IllegalStateException if {@code id} is {@code null}.
   */
  public static NoteBuilder copy(Note note, UUID id) {
    if (id == null) {
      throw new IllegalStateException("ID can not be null for copied NoteBookmarkBuilder.");
    }

    NoteBuilder builder = new NoteBuilder();

    builder.setId(id);
    builder.setName(note.getName());
    if (note.getReference().isPresent()) {
      builder.setReference(note.getReference().get());
    }
    if (note.getZoneId().isPresent()) {
      builder.setZoneId(note.getZoneId().get());
    }
    if (note.getNotesKey().isPresent()) {
      builder.setNotesKey(note.getNotesKey().get());
    }

    return builder;
  }

  /**
   * Creates a new {@code NoteBuilder} populated with the values from the passed in {@link Note}.
   *
   * @param note The {@link Note} to copy the values from.
   * @return a new {@code NoteBuilder}.
   */
  public static NoteBuilder copy(Note note) {
    return copy(note, note.getId());
  }

  /**
   * Creates a new {@code NoteBuilder} populated with the values from the passed in {@link Note} and
   * assigns a newly generated id.
   *
   * @param note The {@link Note} to copy the values from.
   * @return a new {@code NoteBuilder}.
   */
  public static NoteBuilder copyWithNewId(Note note) {
    return copy(note, NoteBookEntry.generateId());
  }

  /** Creates a new {@code NoteBuilder} with none of the values set. */
  public NoteBuilder() {
    id = NoteBookEntry.generateId();
  }

  /**
   * Returns the id that has been set.
   *
   * @eturn the id that has been set.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the id that will be used for the {@link Note} to be built.
   *
   * @param id the id that will be used.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Returns the name that will be used to build the {@link Note}.
   *
   * @return the name that will be used to build the {@link Note}.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name tht will be used to build the {@link Note}.
   *
   * @param name The name that wil be used.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Returns the reference that will be used for the {@link Note} to be built.
   *
   * @return the reference that has been set.
   */
  public String getReference() {
    return reference;
  }

  /**
   * Sets the reference value that will be used when building the {@link Note}.
   *
   * @param reference The reference value that will be set.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setReference(String reference) {
    if (reference == null || reference.isEmpty()) {
      this.reference = null;
    } else {
      this.reference = reference;
    }
    return this;
  }

  /**
   * Returns the id of the {@link Zone} for the {@link Note} will be built.
   *
   * @return the id of the {@link Zone} for the {@link Note} that will be built.
   */
  public GUID getZoneId() {
    return zoneId;
  }

  /**
   * Sets the id of the {@link Zone} for the {@link Note} that will be built.
   *
   * @param zoneId the id of the {@link Zone} for the {@link Note} that will be built.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setZoneId(GUID zoneId) {
    this.zoneId = zoneId;
    return this;
  }

  /**
   * Returns the notes for the {@link Note} that will be built.
   *
   * @return the notes for the {@link Note} that will be built.
   */
  public MD5Key getNotesKey() {
    if (notesKey == null && notes != null) {
      createNoteAsset();
    }
    return notesKey;
  }

  /**
   * Sets the notes for the {@link Note} that will be built.
   *
   * @param notes the notes for the {@link Note} that will be built.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setNotesKey(MD5Key notes) {
    this.notesKey = notes;
    return this;
  }

  /**
   * Sets the notes for the {@link Note} that will be built.
   *
   * @param key the notes for the {@link Note} that will be built.
   * @return {@code this} so that methods can be chained.
   */
  public NoteBuilder setNotesKey(String key) {
    this.notesKey = notesKey;
    this.notes = null;
    return this;
  }

  /**
   * Sets the notes value from a {@link String}.
   *
   * @param n The value of the note.
   * @return {@code this} so that methods can be chained.
   * @implNote The creation of the {@link Asset} is delayed until its fetched as this could be set
   *     before the name.
   */
  public NoteBuilder setNotes(String n) {
    this.notes = n;
    this.notesKey = null;
    return this;
  }

  /**
   * Builds a new {@link Note} with the details from this {@code NoteBuilder}.
   *
   * @return a {@link Note}.
   * @throws IllegalStateException if all the required values are not set.
   */
  public Note build() {
    return new Note(this);
  }

  /**
   * Returns a {@link JsonObject} representing the {@link NoteBuilder}. The {@link String} returned
   * by this function can be passed to {@link #buildFromJson(JsonObject)}.
   *
   * @return The {@link JsonObject} that later can be passed to {@link #buildFromJson(JsonObject)}
   *     to rebuild the {@link Note}.
   */
  public static JsonObject toJson(Note note) {
    Gson gson = new GsonBuilder().setVersion(CURRENT_VERSION).create();
    return gson.toJsonTree(note).getAsJsonObject();
  }

  /**
   * Builds a {@link Note} from the passed in {@code String} value. This method is a complement to
   * {@link #toJson(Note)}.
   *
   * @param json The {@link JsonObject} to build the {@link Note} for.
   * @return a {@link Note} built from the {@link JsonObject}}.
   * @throws IllegalStateException if the {@link JsonObject} can not be parsed correctly.
   */
  public static Note buildFromJson(JsonObject json) {
    Gson gson = new GsonBuilder().create();
    NoteBuilder builder = gson.fromJson(json, NoteBuilder.class);

    // In the future version conversion would happen here...

    return builder.build();
  }

  /**
   * Creates an {@link Asset} for the {@link #notes} and updates the {@link #notesKey}. This will
   * also result in the {@link #notes} being set to {@code null}.
   */
  private void createNoteAsset() {
    if (notes != null) {
      Asset asset = Asset.createHTMLAsset(name + "-notes", notes.getBytes());
      AssetManager.putAsset(asset);
      notesKey = asset.getMD5Key();
      notes = null;
    }
  }
}
