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
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.rptools.lib.MD5Key;
import net.rptools.lib.io.PackedFile;
import net.rptools.maptool.client.ui.notebook.NoteBookDependency;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.notebook.NoteBook;
import net.rptools.maptool.model.notebook.NoteBookManager;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.entry.NoteBookEntryType;
import net.rptools.maptool.util.PersistenceUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility class used for persistence of {@link NoteBook}s. */
public class NoteBookPersistenceUtil {

  /** Logger used for logging information. */
  private static final Logger log = LogManager.getLogger(PersistenceUtil.class);

  /** The directory that holds the notebooks. */
  private static final String NOTE_BOOKS = "notebooks";

  /** The file which holds the information about the {@link NoteBook}. */
  private static final String NOTE_BOOK_INFO_FILE = "notebook-info.json";

  /** The file which holds the license for the {@link NoteBook}. */
  private static final String NOTE_BOOK_LICENSE_FILE = "LICENSE.txt";

  /** The file which holds the readme for the {@link NoteBook}. */
  private static final String NOTE_BOOK_README_FILE = "README.md";

  /** The directory that note book entries are stored in. */
  private static final String ENTRY_DIR = "entries";

  /** Regular Expression pattern used for extracting information from Note Book content paths. */
  private final Pattern noteBookPathPattern =
      Pattern.compile("(" + NOTE_BOOKS + "/[^/]+/[^/]+)/(.*+)");

  /** Class used to hold persisted dependency information. */
  private static final class NoteBookDependencyInfo {

    /** The name space of the dependency. */
    private final String namespace;

    /** The version of the dependency. */
    private final String version;

    /**
     * Creates a new {@code NoteBookDependencyInfo} object.
     *
     * @param ns The name space for the dependency.
     * @param ver the version for the dependency.
     */
    private NoteBookDependencyInfo(String ns, String ver) {
      namespace = ns;
      version = ver;
    }
  }

  /** Utility class used for persisting {@link NoteBook} information. */
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

    /** The author of the {@link NoteBook}. */
    private String author;

    /** The license of the {@link NoteBook}. */
    private transient String license;

    /** The URL of the {@link NoteBook}. */
    private String URL;

    /** The Read Me for the {@link NoteBook}. */
    private transient String readMe;

    /** The dependencies for the {@code NoteBook}. */
    private NoteBookDependencyInfo[] dependencies;
  }

  /**
   * Saves non internal {@link NoteBook}s to the {@link PackedFile} for the campaign.
   *
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
    Set<String> files =
        pakFile.getPaths().stream()
            .filter(p -> p.startsWith(NOTE_BOOKS))
            .collect(Collectors.toSet());

    for (String fname : files) {
      Matcher matcher = noteBookPathPattern.matcher(fname);
      if (matcher.matches()) {
        String noteBookDir = matcher.group(1);
        String file = matcher.group(2);
        noteBookFiles.putIfAbsent(noteBookDir, new HashSet<>());
        noteBookFiles.get(noteBookDir).add(file);
      } else {
        // TODO: CDW
      }
    }

    for (Entry<String, Set<String>> entry : noteBookFiles.entrySet()) {
      noteBookManager.addNoteBook(loadNoteBook(pakFile, entry.getKey(), entry.getValue()));
    }
  }

  /**
   * Loads a {@link NoteBook} and its contents from a {@link PackedFile}.
   *
   * @param packedFile The {@link PackedFile} to load the contents of.
   * @param noteBookDir The directory in the {@link PackedFile} to load the contents from.
   * @param noteBookFiles The files in the {@link PackedFile} containing the {@link NoteBookEntry}s.
   * @return A {@link NoteBook} loaded from the {@link PackedFile}.
   * @throws IOException if an error occurs loading the {@link NoteBook}.
   */
  private NoteBook loadNoteBook(
      PackedFile packedFile, String noteBookDir, Set<String> noteBookFiles) throws IOException {
    var nbePersistenceUtil = new NoteBookEntryPersistenceUtilFactory();

    Set<MD5Key> allAssetIds = new HashSet<>();

    NoteBookInfo nbi = loadNoteBookInfo(packedFile, noteBookDir);
    NoteBook noteBook =
        NoteBook.createNoteBookWithId(
            nbi.id,
            nbi.name,
            nbi.description,
            nbi.version,
            nbi.namespace,
            nbi.author,
            nbi.license,
            nbi.URL,
            nbi.readMe);

    if (nbi.dependencies != null) {
      for (NoteBookDependencyInfo deps : nbi.dependencies) {
        noteBook.putDependency(new NoteBookDependency(deps.namespace, deps.version));
      }
    }

    // Load the individual entries.
    for (var noteFile : noteBookFiles) {
      if (noteFile.startsWith(ENTRY_DIR)) {
        JsonObject jsonObject =
            JsonParser.parseReader(packedFile.getFileAsReader(noteBookDir + "/" + noteFile))
                .getAsJsonObject();
        NoteBookEntry entry = nbePersistenceUtil.fromJson(jsonObject);
        noteBook.putEntry(entry);
        allAssetIds.addAll(entry.getAssetKeys());
      }
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
      if (entry.getType() != NoteBookEntryType.DIRECTORY) {
        packedFile.putFile(
            noteBookDir + "/" + ENTRY_DIR + "/" + entry.getPath(),
            nbePersistenceUtil.toJson(entry).toString().getBytes());

        for (MD5Key md5Key : entry.getAssetKeys()) {
          Asset asset = AssetManager.getAsset(md5Key);
          if (asset != null) {
            allAssets.add(asset);
          } else {
            log.error("NoteBook: AssetId " + md5Key + " not found while saving?!");
          }
        }
      }
    }

    if (!allAssets.isEmpty()) {
      PersistenceUtil.putAssets(allAssets, packedFile);
    }
  }

  /**
   * Saves the note book information to the {@link PackedFile}.
   *
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
    noteBookInfo.author = noteBook.getAuthor();
    noteBookInfo.license = noteBook.getLicense();
    noteBookInfo.URL = noteBook.getURL();

    Set<NoteBookDependency> dependencies = noteBook.getDependencies();
    if (!dependencies.isEmpty()) {
      List<NoteBookDependencyInfo> dependenciesInfo = new ArrayList<>();
      for (var dependency : dependencies) {
        dependenciesInfo.add(
            new NoteBookDependencyInfo(
                dependency.getNamespace(), dependency.getVersion()));
      }
      noteBookInfo.dependencies = dependenciesInfo.toArray(noteBookInfo.dependencies);
    }

    Gson gson = new Gson();

    packedFile.putFile(
        noteBookDir + "/" + NOTE_BOOK_INFO_FILE, gson.toJson(noteBookInfo).getBytes());

    if (noteBook.getLicense() != null && !noteBook.getLicense().isEmpty()) {
      packedFile.putFile(
          noteBookDir + "/" + NOTE_BOOK_LICENSE_FILE, noteBook.getLicense().getBytes());
    }

    if (noteBook.getReadMe() != null && !noteBook.getReadMe().isEmpty()) {
      packedFile.putFile(
          noteBookDir + "/" + NOTE_BOOK_README_FILE, noteBook.getReadMe().getBytes());
    }
  }

  /**
   * Returns the {@link NoteBookInfo} stored in the {@link PackedFile}.
   *
   * @param packedFile The {@link PackedFile} to save the information ahou tthe note book to.
   * @param noteBookDir the directory that the note book will be saved into.
   * @return a {@link NoteBookInfo} created from the stored information.
   * @throws IOException if an error occurs while writing the file.
   */
  private NoteBookInfo loadNoteBookInfo(PackedFile packedFile, String noteBookDir)
      throws IOException {

    final String noteBookLicenseFile = noteBookDir + "/" + NOTE_BOOK_LICENSE_FILE;
    final String noteBookReadMe = noteBookDir + "/" + NOTE_BOOK_README_FILE;

    Gson gson = new Gson();

    String asString =
        IOUtils.toString(
            packedFile.getFileAsInputStream(noteBookDir + "/" + NOTE_BOOK_INFO_FILE),
            StandardCharsets.UTF_8);
    NoteBookInfo nbi = gson.fromJson(asString, NoteBookInfo.class);
    if (packedFile.hasFile(noteBookLicenseFile)) {
      try (InputStream is = packedFile.getFileAsInputStream(noteBookLicenseFile)) {
        nbi.license = IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    }

    if (packedFile.hasFile(noteBookReadMe)) {
      try (InputStream is = packedFile.getFileAsInputStream(noteBookReadMe)) {
        nbi.readMe = IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    }

    return nbi;
  }
}
