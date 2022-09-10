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
package net.rptools.maptool.model.library.addon;

import com.google.protobuf.util.JsonFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.filechooser.FileFilter;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.library.proto.AddOnLibraryDto;
import net.rptools.maptool.model.library.proto.AddOnLibraryEventsDto;
import net.rptools.maptool.model.library.proto.MTScriptPropertiesDto;
import org.apache.tika.mime.MediaType;
import org.javatuples.Pair;

/** Class for importing Drop In Libraries. */
public class AddOnLibraryImporter {

  /** The file extension for add-on library files. */
  public static final String DROP_IN_LIBRARY_EXTENSION = ".mtlib";

  /** The name of the add-on library config file. */
  public static final String LIBRARY_INFO_FILE = "library.json";

  /** the directory where all the content files in the library live. */
  public static final String CONTENT_DIRECTORY = "library/";

  /** The name of the file with the macro script function properties. */
  public static final String MACROSCRIPT_PROPERTY_FILE = "mts_properties.json";

  /** The name of the file with event properties. */
  public static final String EVENT_PROPERTY_FILE = "events.json";

  /**
   * Returns the {@link FileFilter} for add on library files.
   *
   * @return the {@link FileFilter} for add on library files.
   */
  public static FileFilter getAddOnLibraryFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || isAddOnLibrary(f.getName());
      }

      @Override
      public String getDescription() {
        return I18N.getText("file.ext.addOnLib");
      }
    };
  }

  /**
   * Returns if this filename is a valid filename for an add-on library.
   *
   * @param fileName The name of the file to check.
   * @return {@code true} if this is a valid add-on library file name.
   */
  public static boolean isAddOnLibrary(String fileName) {
    return fileName.endsWith(DROP_IN_LIBRARY_EXTENSION);
  }

  /**
   * Returns if the asset in the file is an add-on..
   *
   * @param filename The name of the file to check.
   * @return {@code true} if this is contains an add-on.
   */
  public static boolean isAssetFileAddonLibrary(String filename) {
    try (var zip = new ZipFile(new File(filename))) {
      ZipEntry entry = zip.getEntry(LIBRARY_INFO_FILE);
      if (entry == null) {
        return false;
      } else {
        return true;
      }
    } catch (IOException ioe) {
      return false;
    }
  }

  /**
   * Imports the add-on library from the specified asset.
   *
   * @param asset the asset to use for import.
   * @return the {@link AddOnLibrary} that was imported.
   * @throws IOException if an error occurs while reading the asset.
   */
  public AddOnLibrary importFromAsset(Asset asset) throws IOException {
    // Copy the data to temporary file, its a bit hacky, but it works, and we can't create a
    // ZipFile from anything but a file.
    File tempFile = File.createTempFile("mtlib", "tmp");
    tempFile.deleteOnExit();

    try (OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
      outputStream.write(asset.getData());
    }

    return importFromFile(tempFile);
  }

  /**
   * Imports the add-on library from the specified file.
   *
   * @param file the file to use for import.
   * @return the {@link AddOnLibrary} that was imported.
   * @throws IOException if an error occurs while reading the asset.
   */
  public AddOnLibrary importFromFile(File file) throws IOException {
    var diiBuilder = AddOnLibraryDto.newBuilder();

    try (var zip = new ZipFile(file)) {
      ZipEntry entry = zip.getEntry(LIBRARY_INFO_FILE);
      if (entry == null) {
        throw new IOException(I18N.getText("library.error.addOn.noConfigFile", file.getPath()));
      }
      var builder = AddOnLibraryDto.newBuilder();
      JsonFormat.parser()
          .ignoringUnknownFields()
          .merge(new InputStreamReader(zip.getInputStream(entry)), builder);

      // MT MacroScript properties
      var pathAssetMap = processAssets(builder.getNamespace(), zip);
      var mtsPropBuilder = MTScriptPropertiesDto.newBuilder();
      ZipEntry mtsPropsZipEntry = zip.getEntry(MACROSCRIPT_PROPERTY_FILE);
      if (mtsPropsZipEntry != null) {
        JsonFormat.parser()
            .ignoringUnknownFields()
            .merge(new InputStreamReader(zip.getInputStream(mtsPropsZipEntry)), mtsPropBuilder);
      }

      // Event properties
      var eventPropBuilder = AddOnLibraryEventsDto.newBuilder();
      ZipEntry eventsZipEntry = zip.getEntry(EVENT_PROPERTY_FILE);
      if (eventsZipEntry != null) {
        JsonFormat.parser()
            .ignoringUnknownFields()
            .merge(new InputStreamReader(zip.getInputStream(eventsZipEntry)), eventPropBuilder);
      }
      var addOnLib = builder.build();
      byte[] data = Files.readAllBytes(file.toPath());
      var asset = Type.MTLIB.getFactory().apply(addOnLib.getNamespace(), data);
      addAsset(asset);

      return AddOnLibrary.fromDto(
          asset.getMD5Key(),
          addOnLib,
          mtsPropBuilder.build(),
          eventPropBuilder.build(),
          pathAssetMap);
    }
  }

  /**
   * Reads the assets from the add-on library and adds them to the asset manager.
   *
   * @param namespace the namespace of the add-on library.
   * @param zip the zipfile containing the add-on library.
   * @return a map of asset paths and asset details.
   * @throws IOException if there is an error reading the assets from the add-on library.
   */
  private Map<String, Pair<MD5Key, Type>> processAssets(String namespace, ZipFile zip)
      throws IOException {
    var pathAssetMap = new HashMap<String, Pair<MD5Key, Type>>();
    var entries =
        zip.stream()
            .filter(e -> !e.isDirectory())
            .filter(e -> e.getName().startsWith(CONTENT_DIRECTORY))
            .toList();
    for (var entry : entries) {
      String path = entry.getName().substring(CONTENT_DIRECTORY.length());
      try (InputStream inputStream = zip.getInputStream(entry)) {
        byte[] bytes = inputStream.readAllBytes();
        MediaType mediaType = Asset.getMediaType(entry.getName(), bytes);
        Asset asset =
            Type.fromMediaType(mediaType).getFactory().apply(namespace + "/" + path, bytes);
        addAsset(asset);
        pathAssetMap.put(path, Pair.with(asset.getMD5Key(), asset.getType()));
      }
    }
    return pathAssetMap;
  }

  /**
   * Adds the {@link Asset} to the {@link AssetManager} if it does not already exist.
   *
   * @param asset the {@link Asset} to add.
   */
  private void addAsset(Asset asset) {
    if (!AssetManager.hasAsset(asset)
        || AssetManager.getAsset(asset.getMD5Key()).getData().length == 0) {
      AssetManager.putAsset(asset);
    }
  }
}
