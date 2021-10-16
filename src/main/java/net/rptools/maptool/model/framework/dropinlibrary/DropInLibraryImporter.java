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
package net.rptools.maptool.model.framework.dropinlibrary;

import com.google.protobuf.util.JsonFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;
import net.rptools.maptool.model.framework.proto.MTScriptPropertiesDto;
import org.apache.tika.mime.MediaType;
import org.javatuples.Pair;

/** Class for importing Drop In Libraries. */
public class DropInLibraryImporter {

  /** The file extension for drop in library files. */
  public static final String DROP_IN_LIBRARY_EXTENSION = ".mtlib";

  /** The name of the drop in library config file. */
  private static final String LIBRARY_INFO_FILE = "library.json";

  /** the directory where all the content files in the library live. */
  private static final String CONTENT_DIRECTORY = "library/";

  /** The name of the file with the macro script function properties. */
  private static final String MACROSCRIPT_PROPERTY_FILES = "mts_properties.json";

  /**
   * Returns the {@link FileFilter} for drop in library files.
   *
   * @return the {@link FileFilter} for drop in library files.
   */
  public static FileFilter getDropInLibraryFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || isDropInLibrary(f.getName());
      }

      @Override
      public String getDescription() {
        return I18N.getText("file.ext.dropInLib");
      }
    };
  }

  /**
   * Returns if this filename is a valid filename for a drop in library.
   *
   * @param fileName The name of the file to check.
   * @return {@code true} if this is a valid drop in library file name.
   */
  public static boolean isDropInLibrary(String fileName) {
    return fileName.endsWith(DROP_IN_LIBRARY_EXTENSION);
  }

  /**
   * Imports the drop in library from the specified file.
   *
   * @param file the file to use for import.
   * @return the {@link DropInLibrary} that was imported.
   * @throws IOException
   */
  public DropInLibrary importFromFile(File file) throws IOException {
    var diiBuilder = DropInLibraryDto.newBuilder();

    try (var zip = new ZipFile(file)) {
      ZipEntry entry = zip.getEntry(LIBRARY_INFO_FILE);
      if (entry == null) {
        throw new IOException(I18N.getText("library.error.dropin.noConfigFile", file.getPath()));
      }
      var builder = DropInLibraryDto.newBuilder();
      JsonFormat.parser().merge(new InputStreamReader(zip.getInputStream(entry)), builder);
      var pathAssetMap = processAssets(builder.getNamespace(), zip);
      var mtsPropBuilder = MTScriptPropertiesDto.newBuilder();
      ZipEntry mtsPropsZipEntry = zip.getEntry(MACROSCRIPT_PROPERTY_FILES);
      if (mtsPropsZipEntry != null) {
        JsonFormat.parser()
            .merge(new InputStreamReader(zip.getInputStream(mtsPropsZipEntry)), mtsPropBuilder);
      }
      var dropInLib = builder.build();
      byte[] data = Files.readAllBytes(file.toPath());
      var asset = Type.MTLIB.getFactory().apply(dropInLib.getNamespace(), data);
      addAsset(asset);

      return DropInLibrary.fromDto(
          asset.getMD5Key(), dropInLib, mtsPropBuilder.build(), pathAssetMap);
    }
  }

  /**
   * Reads the assets from the drop in library and adds them to the asset manager.
   *
   * @param namespace the namespace of the drop in library.
   * @param zip the zipfile containing the drop in library.
   * @return a map of asset paths and asset details.
   * @throws IOException if there is an error reading the assets from the drop in library.
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
   * Adds the {@lik Asset} to the {@link AssetManager} if it does not already exist.
   *
   * @param asset the {@link Asset} to add.
   */
  private void addAsset(Asset asset) {
    if (!AssetManager.hasAsset(asset)) {
      AssetManager.putAsset(asset);
    }
  }
}
