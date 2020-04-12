package net.rptools.maptool.model.notebook.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.rptools.lib.MD5Key;
import net.rptools.lib.io.PackedFile;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.notebook.NoteBook;
import net.rptools.maptool.model.notebook.NoteBookManager;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.util.PersistenceUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used for persistence of {@link NoteBook}s.
 */
public class NoteBookPersistenceUtil {

  /** Logger used for logging information. */
  private static final Logger log = LogManager.getLogger(PersistenceUtil.class);


  /** The directory that holds the notebooks. */
  private static final String NOTE_BOOKS = "notebooks";

  /** The file which holds the information about the notebook. */
  private static final String NOTE_BOOK_INFO_FILE = "notebook-info.json";

  /**
   * Utility class used for persisting {@link NoteBook} information.
   */
  private static final class NoteBookInfo {

    /** The id of the {@link NoteBook}. */
    private UUID id;

    /** The name of the {@link NoteBook}. */
    private String name;

    /** The description of the {@link NoteBook}. */
    private String description;

    /** The version of the {@link NoteBook}. */
    private String version;

    /** The namespace of the {@link NoteBook}. */
    private String namespace;

    /** Is the {@link NoteBook} internal or not. */
    private boolean internal = false;
  }

  /**
   * Saves non internal {@link NoteBook}s to the {@link PackedFile} for the campaign.
   * @param packedFile the {@link PackedFile} for the campaign.
   * @param noteBookManager the {@link NoteBookManager} to load the {@link NoteBook}s into.
   * @throws IOException if an error occurs loading the {@link NoteBook}s.
   */
  public void saveCampaignNoteBooks(PackedFile packedFile, NoteBookManager noteBookManager)
      throws IOException {
    for (NoteBook noteBook : noteBookManager.getNoteBooks()) {
      if (!noteBook.isInternal()) {
        saveNoteBook(packedFile, noteBook);
      }
    }
  }


  /**
   * Loads the {@link NoteBook}s in the {@link PackedFile}.
   *
   * @param pakFile the {@link PackedFile} to load the {@link NoteBook}s from.
   * @param noteBookManager The {@link NoteBookManager} for the campaign.
   * @throws IOException when an error occurs reading the file contents.
   */
  public void loadCampaignNoteBooks(PackedFile pakFile, NoteBookManager noteBookManager)
      throws IOException {
    HashMap<String, Set<String>> noteBookFiles = new HashMap<>();
    Set<String> files = pakFile.getPaths().stream().filter(p -> p.startsWith(NOTE_BOOKS))
        .collect(Collectors.toSet());

    for (String fname : files) {
      String[] parts = fname.split("/", 3);
      noteBookFiles.putIfAbsent(parts[1], new HashSet<>());
      noteBookFiles.get(parts[1]).add(parts[2]);
    }

    for (Entry<String, Set<String>> entry : noteBookFiles.entrySet()) {
      noteBookManager.addNoteBook(loadNoteBook(pakFile, entry.getKey(), entry.getValue()));
    }
  }

  /**
   * Loads a {@link NoteBook} and its contents from a {@link PackedFile}.
   * @param packedFile The {@link PackedFile} to load the contents of.
   * @param noteBookDir The directory in the {@link PackedFile} to load the contents from.
   * @param noteBookFiles The files in the {@link PackedFile} containing the {@link NoteBookEntry}s.
   * @return A {@link NoteBook} loaded from the {@link PackedFile}.
   * @throws IOException if an error occurs loading the {@link NoteBook}.
   */
  private NoteBook loadNoteBook(PackedFile packedFile, String noteBookDir, Set<String> noteBookFiles) throws IOException {
    var nbePersistenceUtil = new NoteBookEntryPersistenceUtilFactory();

    Set<MD5Key> allAssetIds = new HashSet<>();

    NoteBookInfo nbi = loadNoteBookInfo(packedFile, noteBookDir);
    NoteBook noteBook = NoteBook.createNoteBook(nbi.id, nbi.name, nbi.description, nbi.version, nbi.namespace);

    Gson gson = new Gson();
    for (var noteFile : noteBookFiles) {
      String asString = IOUtils.toString(packedFile.getFileAsReader(noteFile));
      JsonObject jsonObject = gson.toJsonTree(asString).getAsJsonObject();
      NoteBookEntry entry = nbePersistenceUtil.fromJson(jsonObject);
      noteBook.putEntry(entry);
      allAssetIds.addAll(entry.getAssetKeys());
    }

    PersistenceUtil.loadAssets(allAssetIds, packedFile);

    return noteBook;
  }

  /**
   * Persists the contents of a {@link NoteBook}.
   *
   * @param packedFile The {@link PackedFile} to write the information to.
   * @param notebook The {@link NoteBook} to save the information.
   * @throws IOException if an error occurs writing a file.
   */
  private void saveNoteBook(PackedFile packedFile, NoteBook notebook) throws IOException {
    var nbePersistenceUtil = new NoteBookEntryPersistenceUtilFactory();
    String noteBookDir = NOTE_BOOKS + "/" + notebook.getVersionedNameSpace();
    saveNoteBookInfo(packedFile, noteBookDir, notebook);

    Set<Asset> allAssets = new HashSet<>();
    for (var entry : notebook.getEntries()) {
      packedFile.putFile(
          noteBookDir + "/" + entry.getPath(),
          nbePersistenceUtil.toJson(entry).toString().getBytes());

      for (MD5Key md5Key : entry.getAssetKeys()) {
        Asset asset = AssetManager.getAsset(md5Key);
        allAssets.add(asset);
        if (asset == null) {
          log.error("NoteBook: AssetId " + md5Key + " not found while saving?!");
          continue;
        }
      }
    }

    if (!allAssets.isEmpty()) {
      PersistenceUtil.putAssets(allAssets, packedFile);
    }
  }

  /**
   * Saves the note book information to the {@link PackedFile}.
   * @param packedFile The {@link PackedFile} to save the information ahou tthe note book to.
   * @param noteBookDir the directory that the note book will be saved into.
   * @param noteBook the note book being saved.
   * @throws IOException if an error occurs while writing the file.
   */
  private void saveNoteBookInfo(PackedFile packedFile, String noteBookDir, NoteBook noteBook)
      throws IOException {

    NoteBookInfo noteBookInfo = new NoteBookInfo();
    noteBookInfo.id = noteBook.getId();
    noteBookInfo.name = noteBook.getName();
    noteBookInfo.description = noteBook.getDescription();
    noteBookInfo.version = noteBook.getVersion();
    noteBookInfo.namespace = noteBook.getNamespace();
    noteBookInfo.internal = noteBook.isInternal();

    Gson gson = new Gson();

    packedFile.putFile(noteBookDir + "/" + NOTE_BOOK_INFO_FILE, gson.toJson(noteBookInfo));
  }

  /**
   * Returns the {@link NoteBookInfo} stored in the {@link PackedFile}.
   * @param packedFile The {@link PackedFile} to save the information ahou tthe note book to.
   * @param noteBookDir the directory that the note book will be saved into.
   * @return a {@link NoteBookInfo} created from the stored information.
   * @throws IOException if an error occurs while writing the file.
   */
  private NoteBookInfo loadNoteBookInfo(PackedFile packedFile, String noteBookDir)
      throws IOException {

    Gson gson = new Gson();

    return gson.fromJson(noteBookDir + "/" + NOTE_BOOK_INFO_FILE, NoteBookInfo.class);

  }


}
